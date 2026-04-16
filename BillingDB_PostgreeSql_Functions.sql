#-- ============================================================
#--  Function: insert_cdr_file
#--  Purpose: Insert new CDR file and return its ID
#-- ============================================================

CREATE OR REPLACE FUNCTION public.insert_cdr_file(p_filename character varying)
RETURNS integer
LANGUAGE plpgsql
AS $function$
DECLARE
    new_file_id INT;
BEGIN
    #-- Check if file already exists
    IF EXISTS (
        SELECT 1 FROM CDR_Files WHERE FileName = p_filename
    ) THEN
        RAISE EXCEPTION 'File already exists: %', p_filename;
    END IF;

    INSERT INTO CDR_Files (FileName, ReceivedAt, Status)
    VALUES (p_filename, NOW(), 'RECEIVED')
    RETURNING FileID INTO new_file_id;

    RETURN new_file_id;
END;
$function$;


#-- ============================================================
#--  Function: insert_cdr
#--  Purpose: Insert a single CDR record into CDRs table
#-- ============================================================

CREATE OR REPLACE FUNCTION public.insert_cdr(
    p_caller character varying,
    p_called character varying,
    p_starttime timestamp without time zone,
    p_duration integer,
    p_servicetype character varying,
    p_fileid integer,
    p_status character varying
)
RETURNS void
LANGUAGE plpgsql
AS $function$
BEGIN
    INSERT INTO CDRs (
        Caller, Called, StartTime,
        Duration, ServiceType, FileID, Status
    )
    VALUES (
        p_caller, p_called, p_starttime,
        p_duration, p_servicetype, p_fileid, p_status
    );
END;
$function$;


#-- ===============================================================
#--  Function: process_cdrs_and_generate_invoice
#--  Purpose: Process CDRs, calculate cost, and generate invoices
#-- ===============================================================

CREATE OR REPLACE FUNCTION process_cdrs_and_generate_invoice(p_file_id INT)
RETURNS VOID
LANGUAGE plpgsql
AS $$
DECLARE
    rec RECORD;
    v_plan_id INT;
    v_rate DECIMAL;
    v_cost DECIMAL;
    v_invoice_id INT;
BEGIN

#-- loop on all NEW CDRs for this file
FOR rec IN
    SELECT c.*, s.subscriberid, s.planid
    FROM cdrs c
    JOIN subscribers s ON c.caller = s.msisdn
    WHERE c.fileid = p_file_id
      AND c.status = 'NEW'
LOOP

    #-- get rate
    SELECT rateperunit INTO v_rate
    FROM rates
    WHERE planid = rec.planid
      AND servicetype = rec.servicetype;

    #-- calculate cost
    v_cost := rec.duration * v_rate;

    #-- insert into rated_cdrs
    INSERT INTO rated_cdrs (cdrid, subscriberid, cost, ratedat)
    VALUES (rec.cdrid, rec.subscriberid, v_cost, now());

   # -- check if invoice exists (same month)
    SELECT invoiceid INTO v_invoice_id
    FROM invoices
    WHERE subscriberid = rec.subscriberid
      AND date_trunc('month', startdate) = date_trunc('month', rec.starttime)
    LIMIT 1;

    #-- if not exists → create invoice
    IF v_invoice_id IS NULL THEN
        INSERT INTO invoices (subscriberid, totalamount, startdate, enddate, createdat, status)
        VALUES (
            rec.subscriberid,
            0,
            date_trunc('month', rec.starttime),
            (date_trunc('month', rec.starttime) + interval '1 month - 1 day')::date,
            now(),
            'GENERATED'
        )
        RETURNING invoiceid INTO v_invoice_id;
    END IF;

    #-- insert invoice item
    INSERT INTO invoice_items (invoiceid, cdrid, cost)
    VALUES (v_invoice_id, rec.cdrid, v_cost);

    #-- update invoice total
    UPDATE invoices
    SET totalamount = totalamount + v_cost
    WHERE invoiceid = v_invoice_id;

    #-- update CDR status
    UPDATE cdrs
    SET status = 'PROCESSED'
    WHERE cdrid = rec.cdrid;

END LOOP;

#-- after all processed → update file
UPDATE cdr_files
SET status = 'PROCESSED',
    processedat = now()
WHERE fileid = p_file_id;

END;
$$;

