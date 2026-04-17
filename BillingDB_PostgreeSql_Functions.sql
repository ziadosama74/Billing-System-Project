-- ============================================================
-- Function: insert_cdr_file
-- Purpose: Insert new CDR file and return its ID
-- ============================================================

CREATE OR REPLACE FUNCTION public.insert_cdr_file(p_filename VARCHAR)
RETURNS INT
LANGUAGE plpgsql
AS $$
DECLARE
    new_file_id INT;
BEGIN
    -- Check if file already exists
    IF EXISTS (
        SELECT 1 FROM cdr_files WHERE filename = p_filename
    ) THEN
        RAISE EXCEPTION 'File already exists: %', p_filename;
    END IF;

    -- Insert new file
    INSERT INTO cdr_files (filename, receivedat, status)
    VALUES (p_filename, NOW(), 'RECEIVED')
    RETURNING fileid INTO new_file_id;

    RETURN new_file_id;
END;
$$;

-- ============================================================
-- Function: insert_cdr
-- Purpose: Insert a single CDR record into CDRs table
-- ============================================================

CREATE OR REPLACE FUNCTION public.insert_cdr(
    p_caller VARCHAR,
    p_called VARCHAR,
    p_starttime TIMESTAMP,
    p_duration INT,
    p_servicetype VARCHAR,
    p_fileid INT,
    p_status VARCHAR DEFAULT 'NEW'
)
RETURNS VOID
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO cdrs (
        caller, called, starttime,
        duration, servicetype, fileid, status
    )
    VALUES (
        p_caller, p_called, p_starttime,
        p_duration, p_servicetype, p_fileid, p_status
    );
END;
$$;

-- ===============================================================
-- Function: process_cdrs_and_generate_invoice
-- Purpose: Full Rating Engine (Bundles + Free + Paid + Contract)
-- ===============================================================

CREATE OR REPLACE FUNCTION process_cdrs_and_generate_invoice(p_file_id INT)
RETURNS VOID
LANGUAGE plpgsql
AS $$
DECLARE
    rec RECORD;

    v_rate DECIMAL;
    v_cost DECIMAL := 0;

    v_invoice_id INT;

    v_included INT;
    v_free INT;
    v_used INT := 0;

    v_units INT;              -- usage units after conversion
    v_remaining INT;

    v_contract_type VARCHAR;
BEGIN

-- =========================
-- Loop on CDRs
-- =========================
FOR rec IN
    SELECT c.*, s.subscriberid
    FROM cdrs c
    JOIN subscribers s ON c.caller = s.msisdn
    WHERE c.fileid = p_file_id
      AND c.status = 'NEW'
LOOP

    -- =========================
    -- Convert Units
    -- =========================
    IF rec.servicetype = 'VOICE' THEN
        v_units := CEIL(rec.duration / 60.0); -- seconds → minutes
    ELSE
        v_units := rec.duration;
    END IF;

    -- =========================
    -- Get Plan Included Units
    -- =========================
    SELECT includedunits INTO v_included
    FROM plan_allowances pa
    JOIN subscriber_contracts sc ON sc.planid = pa.planid
    WHERE sc.subscriberid = rec.subscriberid
      AND pa.servicetype = rec.servicetype
    LIMIT 1;

    -- =========================
    -- Get Free Units
    -- =========================
    SELECT freeunits INTO v_free
    FROM plan_free_units pf
    JOIN subscriber_contracts sc ON sc.planid = pf.planid
    WHERE sc.subscriberid = rec.subscriberid
      AND pf.servicetype = rec.servicetype
    LIMIT 1;

    -- =========================
    -- Get Used Units (this month)
    -- =========================
    SELECT COALESCE(SUM(usedunits), 0) INTO v_used
    FROM subscriber_usage
    WHERE subscriberid = rec.subscriberid
      AND servicetype = rec.servicetype
      AND date_trunc('month', billingcycle) = date_trunc('month', rec.starttime);

    -- =========================
    -- Calculate Remaining
    -- =========================
    v_remaining := (v_included + v_free) - v_used;

    -- =========================
    -- Get Contract Type
    -- =========================
    SELECT ct.name INTO v_contract_type
    FROM subscriber_contracts sc
    JOIN contract_types ct ON ct.contracttypeid = sc.contracttypeid
    WHERE sc.subscriberid = rec.subscriberid
    LIMIT 1;

    -- =========================
    -- Pricing Logic
    -- =========================
    IF v_remaining >= v_units THEN
        v_cost := 0;
    ELSE
        IF v_contract_type = 'LIMITED' THEN
            -- block usage
            UPDATE cdrs SET status = 'ERROR'
            WHERE cdrid = rec.cdrid;
            CONTINUE;
        ELSE
            -- Unlimited → charge extra
            SELECT rateperunit INTO v_rate
            FROM rates
            WHERE servicetype = rec.servicetype;

            v_cost := (v_units - GREATEST(v_remaining,0)) * v_rate;
        END IF;
    END IF;

    -- =========================
    -- Insert Usage
    -- =========================
    INSERT INTO subscriber_usage (
        subscriberid, servicetype, usedunits, billingcycle
    )
    VALUES (
        rec.subscriberid, rec.servicetype, v_units, rec.starttime
    );

    -- =========================
    -- Insert Rated CDR
    -- =========================
    INSERT INTO rated_cdrs (cdrid, subscriberid, cost, ratedat)
    VALUES (rec.cdrid, rec.subscriberid, v_cost, NOW());

    -- =========================
    -- Invoice Handling
    -- =========================
    SELECT invoiceid INTO v_invoice_id
    FROM invoices
    WHERE subscriberid = rec.subscriberid
      AND date_trunc('month', startdate) = date_trunc('month', rec.starttime)
    LIMIT 1;

    IF v_invoice_id IS NULL THEN
        INSERT INTO invoices (
            subscriberid, totalamount, startdate, enddate, createdat, status
        )
        VALUES (
            rec.subscriberid,
            0,
            date_trunc('month', rec.starttime),
            (date_trunc('month', rec.starttime) + interval '1 month - 1 day')::date,
            NOW(),
            'GENERATED'
        )
        RETURNING invoiceid INTO v_invoice_id;
    END IF;

    -- =========================
    -- Insert Invoice Item
    -- =========================
    INSERT INTO invoice_items (invoiceid, cdrid, cost)
    VALUES (v_invoice_id, rec.cdrid, v_cost);

    -- =========================
    -- Update Invoice Total
    -- =========================
    UPDATE invoices
    SET totalamount = totalamount + v_cost
    WHERE invoiceid = v_invoice_id;

    -- =========================
    -- Update CDR Status
    -- =========================
    UPDATE cdrs
    SET status = 'PROCESSED'
    WHERE cdrid = rec.cdrid;

END LOOP;

-- =========================
-- Update File Status
-- =========================
UPDATE cdr_files
SET status = 'PROCESSED',
    processedat = NOW()
WHERE fileid = p_file_id;
END;
$$;

-- ===============================================================
-- Function: create_subscriber_with_contract
-- Purpose: Add new subscriber with plan + contract
-- ===============================================================

CREATE OR REPLACE FUNCTION create_subscriber_with_contract(
    p_msisdn VARCHAR,
    p_name VARCHAR,
    p_internationalid VARCHAR,
    p_address VARCHAR,
    p_plan_id INT,
    p_contract_type_name VARCHAR, -- LIMITED / UNLIMITED
    p_start_date DATE,
    p_end_date DATE DEFAULT NULL
)
RETURNS INT
LANGUAGE plpgsql
AS $$
DECLARE
    v_subscriber_id INT;
    v_contract_type_id INT;
BEGIN

    -- =========================
    -- 1. Insert Subscriber
    -- =========================
    INSERT INTO subscribers (
        msisdn, name, internationalid, address
    )
    VALUES (
        p_msisdn, p_name, p_internationalid, p_address
    )
    RETURNING subscriberid INTO v_subscriber_id;

    -- =========================
    -- 2. Get Contract Type ID
    -- =========================
    SELECT contracttypeid INTO v_contract_type_id
    FROM contract_types
    WHERE LOWER(name) = LOWER(p_contract_type_name)
    LIMIT 1;

    IF v_contract_type_id IS NULL THEN
        RAISE EXCEPTION 'Invalid contract type: %', p_contract_type_name;
    END IF;

    -- =========================
    -- 3. Insert Contract
    -- =========================
    INSERT INTO subscriber_contracts (
        subscriberid, planid, contracttypeid, startdate, enddate
    )
    VALUES (
        v_subscriber_id, p_plan_id, v_contract_type_id, p_start_date, p_end_date
    );

    -- =========================
    -- 4. Initialize Usage (optional but recommended)
    -- =========================
    INSERT INTO subscriber_usage (subscriberid, servicetype, usedunits, billingcycle)
    VALUES
        (v_subscriber_id, 'VOICE', 0, p_start_date),
        (v_subscriber_id, 'SMS',   0, p_start_date),
        (v_subscriber_id, 'DATA',  0, p_start_date);

    -- =========================
    -- Return Subscriber ID
    -- =========================
    RETURN v_subscriber_id;

END;
$$;
