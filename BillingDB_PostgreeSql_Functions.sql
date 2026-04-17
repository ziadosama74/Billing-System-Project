-- ============================================================
-- Function: insert_cdr_file
-- Purpose : Insert a new CDR file into the system
--           - Prevent duplicate filenames
--           - Store file metadata
--           - Return generated file ID
-- ============================================================
CREATE OR REPLACE FUNCTION public.insert_cdr_file(p_filename VARCHAR)
RETURNS INT
LANGUAGE plpgsql
AS $$
DECLARE
    new_file_id INT;
BEGIN
    IF EXISTS (
        SELECT 1 FROM cdr_files WHERE filename = p_filename
    ) THEN
        RAISE EXCEPTION 'File already exists: %', p_filename;
    END IF;

    INSERT INTO cdr_files (filename, receivedat, status)
    VALUES (p_filename, NOW(), 'RECEIVED')
    RETURNING fileid INTO new_file_id;

    RETURN new_file_id;
END;
$$;
-- ============================================================
-- Function: insert_cdr
-- Purpose : Insert a single Call Detail Record (CDR)
--           - Stores raw usage data (VOICE / SMS / DATA)
--           - Linked to a specific file
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
-- ============================================================
-- Function: process_cdrs_and_generate_invoice
-- Purpose : Full billing pipeline processor
--           - Reads NEW CDRs
--           - Applies bundle logic (Main → Free → Paid)
--           - Calculates actual vs charged cost
--           - Updates subscriber usage
--           - Generates invoices with monthly fee + overusage
-- ============================================================
CREATE OR REPLACE FUNCTION public.process_cdrs_and_generate_invoice(p_file_id integer)
RETURNS void
LANGUAGE plpgsql
AS $$
DECLARE
    rec RECORD;

    v_plan_id INT;
    v_rate DECIMAL := 0;
    v_units INT;

    v_bundle INT := 0;
    v_free INT := 0;
    v_used INT := 0;

    v_remaining INT;

    v_from_bundle INT := 0;
    v_from_free INT := 0;
    v_from_paid INT := 0;

    v_charged_cost DECIMAL := 0;
    v_actual_cost DECIMAL := 0;

    v_invoice_id INT;
    v_cycle DATE;
    v_monthly_fee DECIMAL := 0;
BEGIN

FOR rec IN
    SELECT c.*, s.subscriberid, s.planid
    FROM cdrs c
    JOIN subscribers s ON c.caller = s.msisdn
    WHERE c.fileid = p_file_id
      AND c.status = 'NEW'
LOOP

    v_cycle := date_trunc('month', rec.starttime)::date;
    v_plan_id := rec.planid;

    SELECT monthlyfee INTO v_monthly_fee
    FROM plans
    WHERE planid = v_plan_id;

    IF rec.servicetype = 'VOICE' THEN
        v_units := CEIL(rec.duration / 60.0);
    ELSE
        v_units := rec.duration;
    END IF;

    INSERT INTO subscriber_usage(subscriberid, servicetype, usedunits, billingcycle)
    VALUES (rec.subscriberid, rec.servicetype, 0, v_cycle)
    ON CONFLICT (subscriberid, servicetype, billingcycle)
    DO UPDATE SET usedunits = subscriber_usage.usedunits
    RETURNING usedunits INTO v_used;

    SELECT COALESCE(includedunits,0)
    INTO v_bundle
    FROM plan_allowances
    WHERE planid = v_plan_id
      AND servicetype = rec.servicetype;

    SELECT COALESCE(freeunits,0)
    INTO v_free
    FROM plan_free_units
    WHERE planid = v_plan_id
      AND servicetype = rec.servicetype;

    v_remaining := (v_bundle + v_free) - v_used;

    SELECT COALESCE(rateperunit,0)
    INTO v_rate
    FROM rates
    WHERE servicetype = rec.servicetype;

    v_from_bundle := LEAST(v_units, GREATEST(v_remaining,0));
    v_units := v_units - v_from_bundle;
    v_remaining := v_remaining - v_from_bundle;

    v_from_free := LEAST(v_units, GREATEST(v_remaining,0));
    v_units := v_units - v_from_free;
    v_remaining := v_remaining - v_from_free;

    v_from_paid := v_units;

    v_charged_cost := v_from_paid * v_rate;

    v_actual_cost :=
        (v_from_bundle + v_from_free + v_from_paid) * v_rate;

    UPDATE subscriber_usage
    SET usedunits = usedunits + (v_from_bundle + v_from_free + v_from_paid)
    WHERE subscriberid = rec.subscriberid
      AND servicetype = rec.servicetype
      AND billingcycle = v_cycle;

    INSERT INTO rated_cdrs(
        cdrid,
        subscriberid,
        actual_cost,
        charged_cost,
        ratedat
    )
    VALUES (
        rec.cdrid,
        rec.subscriberid,
        v_actual_cost,
        v_charged_cost,
        NOW()
    );

    SELECT invoiceid INTO v_invoice_id
    FROM invoices
    WHERE subscriberid = rec.subscriberid
      AND startdate = v_cycle
    LIMIT 1;

    IF v_invoice_id IS NULL THEN
        INSERT INTO invoices(
            subscriberid,
            totalamount,
            startdate,
            enddate,
            createdat,
            status
        )
        VALUES (
            rec.subscriberid,
            v_monthly_fee,
            v_cycle,
            (v_cycle + INTERVAL '1 month - 1 day')::date,
            NOW(),
            'GENERATED'
        )
        RETURNING invoiceid INTO v_invoice_id;
    END IF;

    INSERT INTO invoice_items(invoiceid, cdrid, cost)
    VALUES (v_invoice_id, rec.cdrid, v_charged_cost);

    UPDATE invoices
    SET totalamount = totalamount + v_charged_cost
    WHERE invoiceid = v_invoice_id;

    UPDATE cdrs
    SET status = 'PROCESSED'
    WHERE cdrid = rec.cdrid;

END LOOP;

UPDATE cdr_files
SET status = 'PROCESSED',
    processedat = NOW()
WHERE fileid = p_file_id;

END;
$$;
-- ============================================================
-- Function: create_subscriber
-- Purpose : Create a new subscriber
--           - Assign plan
--           - Initialize usage for VOICE, SMS, DATA
-- ============================================================
CREATE OR REPLACE FUNCTION public.create_subscriber(
    p_msisdn VARCHAR,
    p_name VARCHAR,
    p_internationalid VARCHAR,
    p_address VARCHAR,
    p_plan_id INT
)
RETURNS INT
LANGUAGE plpgsql
AS $$
DECLARE
    v_subscriber_id INT;
BEGIN

    INSERT INTO subscribers (
        msisdn, name, internationalid, address, planid
    )
    VALUES (
        p_msisdn, p_name, p_internationalid, p_address, p_plan_id
    )
    RETURNING subscriberid INTO v_subscriber_id;

    INSERT INTO subscriber_usage (subscriberid, servicetype, usedunits, billingcycle)
    VALUES
        (v_subscriber_id, 'VOICE', 0, date_trunc('month', CURRENT_DATE)),
        (v_subscriber_id, 'SMS',   0, date_trunc('month', CURRENT_DATE)),
        (v_subscriber_id, 'DATA',  0, date_trunc('month', CURRENT_DATE));

    RETURN v_subscriber_id;

END;
$$;
