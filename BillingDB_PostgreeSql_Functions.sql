-- ============================================================================
-- DATABASE FUNCTIONS FOR BILLING SYSTEM
-- ============================================================================
-- Author: Billing System Team
-- Database: PostgreSQL
-- Purpose: Complete billing system functions for subscriber, plan, 
--          invoice, CDR, and authentication management
-- ============================================================================

-- ============================================================================
-- FUNCTION: change_plan_with_prorating
-- ============================================================================
-- DESCRIPTION:
--   Changes a subscriber's plan with prorated billing calculation.
--   When a subscriber changes plans mid-month, this function:
--   1. Calculates the remaining days in the current billing cycle
--   2. Prorates the new plan's allowances based on remaining days
--   3. Preserves already consumed usage from the old plan
--   4. Updates the subscriber's plan ID
--
-- PARAMETERS:
--   p_subscriber_id (INTEGER) - ID of the subscriber changing plans
--   p_new_plan_id   (INTEGER) - ID of the new plan to assign
--
-- RETURNS: VOID
--
-- ALGORITHM:
--   - v_remaining_days / v_total_days = proration ratio
--   - New bundle = (includedunits + freeunits) * ratio - already_used
--   - Old usage is preserved and new usage records are created
--
-- EXAMPLE:
--   SELECT change_plan_with_prorating(100, 5);
--   -- Subscriber 100 changes to plan 5 with prorated allowances
-- ============================================================================

CREATE OR REPLACE FUNCTION public.change_plan_with_prorating(p_subscriber_id integer, p_new_plan_id integer)
 RETURNS void
 LANGUAGE plpgsql
AS $function$
DECLARE
    v_today DATE := CURRENT_DATE;
    v_month_start DATE;
    v_month_end DATE;
    v_total_days INT;
    v_remaining_days INT;
    v_ratio DECIMAL;

    v_old_usage RECORD;
BEGIN

    -- =========================
    -- Billing Cycle Calculation
    -- =========================
    v_month_start := date_trunc('month', v_today);
    v_month_end := (v_month_start + interval '1 month - 1 day')::date;

    v_total_days := v_month_end - v_month_start + 1;
    v_remaining_days := v_month_end - v_today + 1;

    v_ratio := v_remaining_days::DECIMAL / v_total_days;

    -- =========================
    -- Save old usage (critical for preserving consumed units)
    -- =========================
    CREATE TEMP TABLE tmp_usage AS
    SELECT *
    FROM subscriber_usage
    WHERE subscriberid = p_subscriber_id
      AND billingcycle = v_month_start;

    -- =========================
    -- Delete old usage to replace with prorated values
    -- =========================
    DELETE FROM subscriber_usage
    WHERE subscriberid = p_subscriber_id
      AND billingcycle = v_month_start;

    -- =========================
    -- Insert NEW prorated bundle
    -- Formula: CEIL((includedunits + freeunits) * v_ratio) - already_used
    -- =========================
    INSERT INTO subscriber_usage (
        subscriberid,
        servicetype,
        usedunits,
        billingcycle
    )
    SELECT
        p_subscriber_id,
        pa.servicetype,

        GREATEST(
            CEIL((pa.includedunits + COALESCE(pf.freeunits,0)) * v_ratio)
            - COALESCE(tu.usedunits,0),
            0
        ),

        v_month_start

    FROM plan_allowances pa
    LEFT JOIN plan_free_units pf
        ON pa.planid = pf.planid
       AND pa.servicetype = pf.servicetype
    LEFT JOIN tmp_usage tu
        ON tu.servicetype = pa.servicetype

    WHERE pa.planid = p_new_plan_id;

    -- =========================
    -- Update subscriber's plan
    -- =========================
    UPDATE subscribers
    SET planid = p_new_plan_id
    WHERE subscriberid = p_subscriber_id;

    DROP TABLE tmp_usage;

END;
$function$;


-- ============================================================================
-- FUNCTION: create_subscriber
-- ============================================================================
-- DESCRIPTION:
--   Creates a new subscriber with comprehensive validation and initialization.
--   Performs duplicate checking on International ID and MSISDN before insertion.
--   Automatically initializes usage records for the current billing cycle.
--
-- PARAMETERS:
--   p_msisdn          (VARCHAR) - Subscriber's phone number (unique)
--   p_name            (VARCHAR) - Full name of subscriber
--   p_internationalid (VARCHAR) - National/International ID (unique)
--   p_address         (VARCHAR) - Physical address
--   p_plan_id         (INTEGER) - Foreign key reference to plans table
--
-- RETURNS: INTEGER
--   > 0  - Success: Returns the newly created subscriber ID
--   -1   - Error: International ID already exists in the system
--   -2   - Error: MSISDN already exists in the system
--
-- EXAMPLE:
--   SELECT create_subscriber('+20123456789', 'John Doe', 'ID123456', '123 Main St', 1);
--   -- Returns: 42 (new subscriber ID)
-- ============================================================================

CREATE OR REPLACE FUNCTION public.create_subscriber(p_msisdn character varying, p_name character varying, p_internationalid character varying, p_address character varying, p_plan_id integer)
 RETURNS integer
 LANGUAGE plpgsql
AS $function$
DECLARE
    v_subscriber_id INT;
    v_internationalid_exists INT;
BEGIN
    -- Check if international ID already exists
    SELECT COUNT(*) INTO v_internationalid_exists
    FROM subscribers 
    WHERE internationalid = p_internationalid;
    
    -- If international ID exists, return -1
    IF v_internationalid_exists > 0 THEN
        RETURN -1;
    END IF;
    
    -- Check if MSISDN already exists
    SELECT COUNT(*) INTO v_internationalid_exists
    FROM subscribers 
    WHERE msisdn = p_msisdn;
    
    -- If MSISDN exists, return -2
    IF v_internationalid_exists > 0 THEN
        RETURN -2;
    END IF;

    -- Insert new subscriber
    INSERT INTO subscribers (
        msisdn, name, internationalid, address, planid
    )
    VALUES (
        p_msisdn, p_name, p_internationalid, p_address, p_plan_id
    )
    RETURNING subscriberid INTO v_subscriber_id;

    -- Insert usage records for the new subscriber (current billing cycle)
    INSERT INTO subscriber_usage (subscriberid, servicetype, usedunits, billingcycle)
    VALUES
        (v_subscriber_id, 'VOICE', 0, date_trunc('month', CURRENT_DATE)),
        (v_subscriber_id, 'SMS',   0, date_trunc('month', CURRENT_DATE)),
        (v_subscriber_id, 'DATA',  0, date_trunc('month', CURRENT_DATE));

    RETURN v_subscriber_id;

END;
$function$;


-- ============================================================================
-- FUNCTION: deletesoftsubscriberbyid
-- ============================================================================
-- DESCRIPTION:
--   Performs a soft delete on a subscriber by setting the isdeleted flag to true.
--   This preserves historical data for billing and reporting purposes while
--   removing the subscriber from active views.
--
-- PARAMETERS:
--   p_id (INTEGER) - ID of the subscriber to soft delete
--
-- RETURNS: VOID
--
-- NOTES:
--   - The subscriber record remains in the database
--   - All associated invoices and usage history are preserved
--   - Subscriber will not appear in getallsubscriber() queries
--
-- EXAMPLE:
--   SELECT deletesoftsubscriberbyid(100);
--   -- Subscriber 100 is now marked as deleted
-- ============================================================================

CREATE OR REPLACE FUNCTION public.deletesoftsubscriberbyid(p_id integer)
 RETURNS void
 LANGUAGE plpgsql
AS $function$ 
begin
  update subscribers set isdeleted = true where subscriberid = P_ID;
end;
$function$;


-- ============================================================================
-- FUNCTION: get_invoices
-- ============================================================================
-- DESCRIPTION:
--   Retrieves all invoices with comprehensive subscriber and plan information.
--   Joins invoices with subscribers and plans to provide a complete view
--   including subscriber details, plan names, and formatted month names.
--
-- PARAMETERS: None
--
-- RETURNS: TABLE with columns:
--   invoiceid     (INTEGER) - Unique invoice identifier
--   name          (TEXT)    - Subscriber full name
--   msisdn        (TEXT)    - Subscriber phone number
--   month_name    (TEXT)    - Billing month (e.g., 'January')
--   due_date      (DATE)    - Invoice due date (last day of month)
--   totalamount   (NUMERIC) - Total invoice amount
--   subscriberid  (INTEGER) - Subscriber ID (foreign key)
--   address       (VARCHAR) - Subscriber address
--   planname      (VARCHAR) - Name of the subscription plan
--
-- EXAMPLE:
--   SELECT * FROM get_invoices();
--   -- Returns all invoices with complete subscriber details
-- ============================================================================

CREATE OR REPLACE FUNCTION public.get_invoices()
 RETURNS TABLE(invoiceid integer, name text, msisdn text, month_name text, due_date date, totalamount numeric, subscriberid integer, address character varying, planname character varying)
 LANGUAGE plpgsql
AS $function$
BEGIN
    RETURN QUERY
    SELECT 
        I.invoiceid::INT,
        S.name::TEXT,
        S.msisdn::TEXT,
        TRIM(TO_CHAR(I.createdat, 'Month'))::TEXT,
        (date_trunc('month', I.createdat) + INTERVAL '1 month - 1 day')::DATE,
        I.totalamount::NUMERIC,
        S.subscriberid,
        S.address,
        P.planname 
    FROM invoices I
    JOIN subscribers S 
        ON I.subscriberid = S.subscriberid 
    JOIN plans P 
        ON S.planid = P.planid;
END;
$function$;


-- ============================================================================
-- FUNCTION: getallplans
-- ============================================================================
-- DESCRIPTION:
--   Retrieves all plans with their allowances and free units aggregated.
--   Joins plans with plan_allowances and plan_free_units to provide a
--   complete view of each plan's features including data, SMS, and voice quotas.
--
-- PARAMETERS: None
--
-- RETURNS: TABLE with columns:
--   planid           (INTEGER)     - Unique plan identifier
--   planname         (VARCHAR)     - Name of the plan
--   monthlyfee       (NUMERIC)     - Monthly subscription fee
--   description      (VARCHAR)     - Plan description
--   isactive         (BOOLEAN)     - Whether plan is active
--   data_allowance   (INTEGER)     - Included data in KB
--   sms_allowance    (INTEGER)     - Included SMS count
--   voice_allowance  (INTEGER)     - Included voice minutes
--   data_free        (INTEGER)     - Free data bonus in KB
--   sms_free         (INTEGER)     - Free SMS bonus
--   voice_free       (INTEGER)     - Free voice minutes bonus
--   color            (VARCHAR)     - Brand color for UI
--
-- EXAMPLE:
--   SELECT * FROM getallplans();
--   -- Returns all plans with complete allowance information
-- ============================================================================

CREATE OR REPLACE FUNCTION public.getallplans()
 RETURNS TABLE(planid integer, planname character varying, monthlyfee numeric, description character varying, isactive boolean, data_allowance integer, sms_allowance integer, voice_allowance integer, data_free integer, sms_free integer, voice_free integer, color character varying)
 LANGUAGE plpgsql
AS $function$
begin
  return query
    SELECT 
      P.planid,
      P.planname,
      P.monthlyfee,
      P.description,
      P.isactive,
      
      -- Allowances (included units)
      COALESCE(MAX(CASE WHEN A.servicetype = 'DATA' THEN A.includedunits END), 0) AS data_allowance,
      COALESCE(MAX(CASE WHEN A.servicetype = 'SMS' THEN A.includedunits END), 0) AS sms_allowance,
      COALESCE(MAX(CASE WHEN A.servicetype = 'VOICE' THEN A.includedunits END), 0) AS voice_allowance,
      
      -- Free Units
      COALESCE(MAX(CASE WHEN F.servicetype = 'DATA' THEN F.freeunits END), 0) AS data_free,
      COALESCE(MAX(CASE WHEN F.servicetype = 'SMS' THEN F.freeunits END), 0) AS sms_free,
      COALESCE(MAX(CASE WHEN F.servicetype = 'VOICE' THEN F.freeunits END), 0) AS voice_free,   
      P.color
      
    FROM plans P 
    LEFT JOIN plan_allowances A ON P.planid = A.planid 
    LEFT JOIN plan_free_units F ON P.planid = F.planid
    GROUP BY P.planid, P.planname, P.monthlyfee, P.description, P.isactive, P.color
    ORDER BY P.planid;
end;
$function$;


-- ============================================================================
-- FUNCTION: getallsubscriber
-- ============================================================================
-- DESCRIPTION:
--   Retrieves all active (non-deleted) subscribers with their current plan names.
--   Excludes soft-deleted subscribers from the result set.
--
-- PARAMETERS: None
--
-- RETURNS: TABLE with columns:
--   subscriberid     (INTEGER)  - Unique subscriber identifier
--   msisdn           (VARCHAR)  - Phone number
--   name             (VARCHAR)  - Subscriber full name
--   internationalid  (VARCHAR)  - National/International ID
--   address          (VARCHAR)  - Physical address
--   isactive         (BOOLEAN)  - Active/Inactive status
--   plan             (VARCHAR)  - Current plan name
--
-- NOTES:
--   - Only returns subscribers where isdeleted = false
--   - Orders results by subscriberid ascending
--   - Plan name is retrieved via JOIN with plans table
--
-- EXAMPLE:
--   SELECT * FROM getallsubscriber();
--   -- Returns all active subscribers with their plan names
-- ============================================================================

CREATE OR REPLACE FUNCTION public.getallsubscriber()
 RETURNS TABLE(subscriberid integer, msisdn character varying, name character varying, internationalid character varying, address character varying, isactive boolean, plan character varying)
 LANGUAGE plpgsql
AS $function$
begin 
    return query
    select 
        S.subscriberid,
        S.msisdn,
        S.name,
        S.internationalid,
        S.address,
        S.isactive,
        P.planname
        
    from subscribers S 
    join plans P on S.planid = P.planid where S.isdeleted = false
    order by S.subscriberid;
end;
$function$;


-- ============================================================================
-- FUNCTION: getnumofbillsmonthly
-- ============================================================================
-- DESCRIPTION:
--   Returns the total number of invoices generated in the current month.
--   Used for dashboard statistics and monthly reporting.
--
-- PARAMETERS: None
--
-- RETURNS: INTEGER - Count of invoices created in the current month
--
-- NOTES:
--   - Compares month name of createdat with current month
--   - Uses TRIM(TO_CHAR()) for month name comparison
--
-- EXAMPLE:
--   SELECT getnumofbillsmonthly();
--   -- Returns: 145 (invoices this month)
-- ============================================================================

CREATE OR REPLACE FUNCTION public.getnumofbillsmonthly()
 RETURNS integer
 LANGUAGE plpgsql
AS $function$
declare
V_No_Bills int;
begin
select count(*) into V_No_Bills from invoices where TRIM(TO_CHAR(createdat,'month')) = TRIM(TO_CHAR(CURRENT_DATE,'month'));
return V_No_Bills;
end;
$function$;


-- ============================================================================
-- FUNCTION: getnumofsubscribers
-- ============================================================================
-- DESCRIPTION:
--   Returns the total count of active (non-deleted) subscribers in the system.
--   Used for dashboard metrics and business intelligence.
--
-- PARAMETERS: None
--
-- RETURNS: INTEGER - Total number of active subscribers
--
-- NOTES:
--   - Counts only subscribers where isdeleted = false
--   - Does not consider isactive status (includes both active and inactive)
--
-- EXAMPLE:
--   SELECT getnumofsubscribers();
--   -- Returns: 1250 (total active subscribers)
-- ============================================================================

CREATE OR REPLACE FUNCTION public.getnumofsubscribers()
 RETURNS integer
 LANGUAGE plpgsql
AS $function$
declare
V_NO_Subs int;
begin
select count(*) into V_NO_Subs from subscribers where isdeleted = false;
return V_NO_Subs;
end;
$function$;


-- ============================================================================
-- FUNCTION: getplannamebyid
-- ============================================================================
-- DESCRIPTION:
--   Retrieves the plan name for a given plan ID.
--   Simple lookup function used by servlets for display purposes.
--
-- PARAMETERS:
--   p_id (INTEGER) - Plan ID to look up
--
-- RETURNS: VARCHAR(50) - Name of the plan
--
-- EXAMPLE:
--   SELECT getplannamebyid(3);
--   -- Returns: 'Premium'
-- ============================================================================

CREATE OR REPLACE FUNCTION public.getplannamebyid(p_id integer)
 RETURNS character varying
 LANGUAGE plpgsql
AS $function$
  declare
  V_planname varchar(50);
  begin 
  select planname into V_planname from Plans where planid = P_ID;
  return V_planname;
  end;
$function$;


-- ============================================================================
-- FUNCTION: getrevenue
-- ============================================================================
-- DESCRIPTION:
--   Calculates the total monthly revenue in thousands (K) format.
--   Sums all invoice amounts for the current month and divides by 1000.
--   Used for dashboard revenue display in K format.
--
-- PARAMETERS: None
--
-- RETURNS: NUMERIC(2,1) - Monthly revenue in thousands (e.g., 45.5 = 45,500 EGP)
--
-- NOTES:
--   - Revenue = ROUND(SUM(totalamount) / 1000.0, 1)
--   - Only includes invoices from the current month
--   - Returns 0 if no invoices exist
--
-- EXAMPLE:
--   SELECT getrevenue();
--   -- Returns: 125.5 (125,500 EGP monthly revenue)
-- ============================================================================

CREATE OR REPLACE FUNCTION public.getrevenue()
 RETURNS numeric
 LANGUAGE plpgsql
AS $function$
declare 
V_Revenue NUMERIC(2,1);
begin
select ROUND(SUM(totalamount) / 1000.0, 1) into V_Revenue from invoices WHERE TRIM(TO_CHAR(createdat, 'Month')) = TRIM(TO_CHAR(CURRENT_DATE, 'Month'));
return V_Revenue;
end;
$function$;


-- ============================================================================
-- FUNCTION: getsubscriberbyid
-- ============================================================================
-- DESCRIPTION:
--   Retrieves detailed information for a specific subscriber by ID.
--   Includes plan information and both active/deleted subscribers.
--
-- PARAMETERS:
--   p_id (INTEGER) - Subscriber ID to retrieve
--
-- RETURNS: TABLE with columns:
--   subscriberid     (INTEGER)  - Unique subscriber identifier
--   msisdn           (VARCHAR)  - Phone number
--   name             (VARCHAR)  - Subscriber full name
--   internationalid  (VARCHAR)  - National/International ID
--   address          (VARCHAR)  - Physical address
--   isactive         (BOOLEAN)  - Active/Inactive status
--   plan             (VARCHAR)  - Current plan name
--   planid           (INTEGER)  - Current plan ID (for updates)
--
-- EXAMPLE:
--   SELECT * FROM getsubscriberbyid(100);
--   -- Returns subscriber 100 with all details including plan name and ID
-- ============================================================================

CREATE OR REPLACE FUNCTION public.getsubscriberbyid(p_id integer)
 RETURNS TABLE(subscriberid integer, msisdn character varying, name character varying, internationalid character varying, address character varying, isactive boolean, plan character varying, planid integer)
 LANGUAGE plpgsql
AS $function$
begin 
    return query
    select 
        S.subscriberid,
        S.msisdn,
        S.name,
        S.internationalid,
        S.address,
        S.isactive,
        P.planname,
        S.planid
    from subscribers S 
    join plans P on S.planid = P.planid 
    where S.subscriberid = P_ID;
end;
$function$;


-- ============================================================================
-- FUNCTION: getsubscriberbymsisdn
-- ============================================================================
-- DESCRIPTION:
--   Searches for a subscriber by their phone number (MSISDN).
--   Only returns active (non-deleted) subscribers.
--   Used for search functionality in the subscriber management interface.
--
-- PARAMETERS:
--   p_msisdn (VARCHAR) - Phone number to search for
--
-- RETURNS: TABLE with columns:
--   subscriberid     (INTEGER)  - Unique subscriber identifier
--   msisdn           (VARCHAR)  - Phone number
--   name             (VARCHAR)  - Subscriber full name
--   internationalid  (VARCHAR)  - National/International ID
--   address          (VARCHAR)  - Physical address
--   isactive         (BOOLEAN)  - Active/Inactive status
--   plan             (VARCHAR)  - Current plan name
--
-- NOTES:
--   - Only returns subscribers where isdeleted = false
--   - Returns null if subscriber not found
--
-- EXAMPLE:
--   SELECT * FROM getsubscriberbymsisdn('+20123456789');
--   -- Returns subscriber with matching phone number
-- ============================================================================

CREATE OR REPLACE FUNCTION public.getsubscriberbymsisdn(p_msisdn character varying)
 RETURNS TABLE(subscriberid integer, msisdn character varying, name character varying, internationalid character varying, address character varying, isactive boolean, plan character varying)
 LANGUAGE plpgsql
AS $function$
begin 
    return query
    select 
        S.subscriberid,
        S.msisdn,
        S.name,
        S.internationalid,
        S.address,
        S.isactive,
        P.planname
        
    from subscribers S 
    join plans P on S.planid = P.planid 
    where S.msisdn = P_msisdn and S.isdeleted = false;
end;
$function$;


-- ============================================================================
-- FUNCTION: insert_cdr
-- ============================================================================
-- DESCRIPTION:
--   Inserts a single Call Detail Record (CDR) into the system.
--   Called by CDR processing engine when loading call data files.
--
-- PARAMETERS:
--   p_caller       (VARCHAR)         - Calling party phone number
--   p_called       (VARCHAR)         - Called party phone number
--   p_starttime    (TIMESTAMP)       - Call start timestamp
--   p_duration     (INTEGER)         - Call duration in seconds (or units)
--   p_servicetype  (VARCHAR)         - Service type (VOICE/SMS/DATA)
--   p_fileid       (INTEGER)         - Reference to cdr_files table
--   p_status       (VARCHAR)         - Default 'NEW' (NEW/PROCESSED/ERROR)
--
-- RETURNS: VOID
--
-- EXAMPLE:
--   SELECT insert_cdr('+20123456789', '+20987654321', NOW(), 120, 'VOICE', 5);
-- ============================================================================

CREATE OR REPLACE FUNCTION public.insert_cdr(p_caller character varying, p_called character varying, p_starttime timestamp without time zone, p_duration integer, p_servicetype character varying, p_fileid integer, p_status character varying DEFAULT 'NEW'::character varying)
 RETURNS void
 LANGUAGE plpgsql
AS $function$
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
$function$;


-- ============================================================================
-- FUNCTION: insert_cdr_file
-- ============================================================================
-- DESCRIPTION:
--   Registers a CDR file in the system and returns a file ID.
--   Prevents duplicate file processing by checking filename existence.
--
-- PARAMETERS:
--   p_filename (VARCHAR) - Name of the uploaded CDR file
--
-- RETURNS: INTEGER - Newly created file ID
--
-- EXCEPTIONS:
--   Raises exception if file with same name already exists
--
-- EXAMPLE:
--   SELECT insert_cdr_file('cdr_20240101_001.csv');
--   -- Returns: 42 (new file ID)
-- ============================================================================

CREATE OR REPLACE FUNCTION public.insert_cdr_file(p_filename character varying)
 RETURNS integer
 LANGUAGE plpgsql
AS $function$
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
$function$;


-- ============================================================================
-- FUNCTION: loginadmin
-- ============================================================================
-- DESCRIPTION:
--   Authenticates an admin user by checking username and password.
--   Returns admin details if credentials are valid.
--
-- PARAMETERS:
--   p_username (VARCHAR) - Admin username
--   p_password (VARCHAR) - Admin password
--
-- RETURNS: TABLE with columns:
--   id        (INTEGER)     - Admin ID
--   fullname  (TEXT)        - Admin's full name
--   username  (VARCHAR)     - Admin username
--
-- NOTES:
--   - Returns empty result set if credentials are invalid
--   - Used by LoginRedirectServlet for authentication
--
-- EXAMPLE:
--   SELECT * FROM loginadmin('admin', 'password123');
--   -- Returns admin record if credentials match
-- ============================================================================

CREATE OR REPLACE FUNCTION public.loginadmin(p_username character varying, p_password character varying)
 RETURNS TABLE(id integer, fullname text, username character varying)
 LANGUAGE plpgsql
AS $function$
BEGIN
    RETURN QUERY
    SELECT a.id, a.fullname, a.username
    FROM admins a
    WHERE a.username = p_username
      AND a.password = p_password;
END;
$function$;


-- ============================================================================
-- FUNCTION: process_cdrs_and_generate_invoice
-- ============================================================================
-- DESCRIPTION:
--   Comprehensive CDR processing function that:
--   1. Processes all NEW CDRs for a given file
--   2. Calculates usage against subscriber allowances
--   3. Applies proration when allowance exceeded
--   4. Generates or updates invoices
--   5. Calculates actual vs charged costs
--   6. Updates CDR status to PROCESSED
--
-- PARAMETERS:
--   p_file_id (INTEGER) - CDR file ID to process
--
-- RETURNS: VOID
--
-- PROCESSING LOGIC:
--   For each CDR:
--     1. Determine billing cycle from starttime
--     2. Check subscriber's plan allowances (bundled + free)
--     3. Subtract already used units in current cycle
--     4. Apply overage charges for excess usage
--     5. Add monthly subscription fee once per cycle
--     6. Store rated CDR and update invoice totals
--
-- NOTES:
--   - Only processes CDRs with status = 'NEW'
--   - Updates file status to 'PROCESSED' when complete
--   - Creates new invoice for each billing cycle if none exists
--
-- EXAMPLE:
--   SELECT process_cdrs_and_generate_invoice(5);
--   -- Processes all CDRs from file 5 and generates invoices
-- ============================================================================

CREATE OR REPLACE FUNCTION public.process_cdrs_and_generate_invoice(p_file_id integer)
 RETURNS void
 LANGUAGE plpgsql
AS $function$
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

    -- =========================
    -- Determine Billing Cycle
    -- =========================
    v_cycle := date_trunc('month', rec.starttime)::date;
    v_plan_id := rec.planid;

    -- =========================
    -- Get Monthly Subscription Fee
    -- =========================
    SELECT monthlyfee INTO v_monthly_fee
    FROM plans
    WHERE planid = v_plan_id;

    -- =========================
    -- Convert Duration to Units
    -- VOICE: minutes (duration/60), SMS/DATA: units (duration)
    -- =========================
    IF rec.servicetype = 'VOICE' THEN
        v_units := CEIL(rec.duration / 60.0);
    ELSE
        v_units := rec.duration;
    END IF;

    -- =========================
    -- Get Current Usage or Initialize
    -- =========================
    INSERT INTO subscriber_usage(subscriberid, servicetype, usedunits, billingcycle)
    VALUES (rec.subscriberid, rec.servicetype, 0, v_cycle)
    ON CONFLICT (subscriberid, servicetype, billingcycle)
    DO UPDATE SET usedunits = subscriber_usage.usedunits
    RETURNING usedunits INTO v_used;

    -- =========================
    -- Get Plan Allowances (Bundled + Free)
    -- =========================
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

    -- =========================
    -- Get Overage Rate
    -- =========================
    SELECT COALESCE(rateperunit,0)
    INTO v_rate
    FROM rates
    WHERE servicetype = rec.servicetype;

    -- =========================
    -- Allocate Usage: Bundled First
    -- =========================
    v_from_bundle := LEAST(v_units, GREATEST(v_remaining,0));
    v_units := v_units - v_from_bundle;
    v_remaining := v_remaining - v_from_bundle;

    -- =========================
    -- Allocate Usage: Free Second
    -- =========================
    v_from_free := LEAST(v_units, GREATEST(v_remaining,0));
    v_units := v_units - v_from_free;
    v_remaining := v_remaining - v_from_free;

    -- =========================
    -- Remaining usage is paid (overage)
    -- =========================
    v_from_paid := v_units;

    -- =========================
    -- Calculate Costs
    -- =========================
    v_charged_cost := v_from_paid * v_rate;  -- Only charge for overage
    v_actual_cost := (v_from_bundle + v_from_free + v_from_paid) * v_rate;

    -- =========================
    -- Update Subscriber Usage
    -- =========================
    UPDATE subscriber_usage
    SET usedunits = usedunits + (v_from_bundle + v_from_free + v_from_paid)
    WHERE subscriberid = rec.subscriberid
      AND servicetype = rec.servicetype
      AND billingcycle = v_cycle;

    -- =========================
    -- Store Rated CDR
    -- =========================
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

    -- =========================
    -- Generate or Update Invoice
    -- =========================
    SELECT invoiceid INTO v_invoice_id
    FROM invoices
    WHERE subscriberid = rec.subscriberid
      AND startdate = v_cycle
    LIMIT 1;

    -- Create new invoice if none exists for this cycle
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
            v_monthly_fee,  -- Start with monthly fee
            v_cycle,
            (v_cycle + INTERVAL '1 month - 1 day')::date,
            NOW(),
            'GENERATED'
        )
        RETURNING invoiceid INTO v_invoice_id;
    END IF;

    -- Add CDR cost to invoice
    INSERT INTO invoice_items(invoiceid, cdrid, cost)
    VALUES (v_invoice_id, rec.cdrid, v_charged_cost);

    -- Update invoice total
    UPDATE invoices
    SET totalamount = totalamount + v_charged_cost
    WHERE invoiceid = v_invoice_id;

    -- =========================
    -- Mark CDR as Processed
    -- =========================
    UPDATE cdrs
    SET status = 'PROCESSED'
    WHERE cdrid = rec.cdrid;

END LOOP;

-- =========================
-- Mark CDR File as Fully Processed
-- =========================
UPDATE cdr_files
SET status = 'PROCESSED',
    processedat = NOW()
WHERE fileid = p_file_id;

END;
$function$;


-- ============================================================================
-- FUNCTION: update_plan
-- ============================================================================
-- DESCRIPTION:
--   Comprehensive plan update function that updates or inserts allowances
--   and free units for all service types (VOICE, SMS, DATA).
--
-- PARAMETERS:
--   p_planid          (INTEGER)  - Plan ID to update
--   p_planname        (VARCHAR)  - New plan name
--   p_monthlyfee      (NUMERIC)  - New monthly fee
--   p_description     (VARCHAR)  - New description
--   p_isactive        (BOOLEAN)  - Active status
--   p_color           (VARCHAR)  - UI brand color
--   p_voice_allowance (INTEGER)  - Included voice minutes
--   p_data_allowance  (INTEGER)  - Included data in KB
--   p_sms_allowance   (INTEGER)  - Included SMS count
--   p_voice_free      (INTEGER)  - Free voice minutes bonus
--   p_data_free       (INTEGER)  - Free data bonus in KB
--   p_sms_free        (INTEGER)  - Free SMS bonus
--
-- RETURNS: BOOLEAN
--   TRUE  - Update successful
--   FALSE - Update failed (plan not found or exception occurred)
--
-- NOTES:
--   - Uses UPSERT logic (UPDATE if exists, INSERT if not)
--   - Updates both plan_allowances and plan_free_units tables
--
-- EXAMPLE:
--   SELECT update_plan(5, 'Premium Plus', 299, 'Best plan ever', true, '#FF0000', 1000, 10240, 500, 100, 5120, 50);
--   -- Returns: true
-- ============================================================================

CREATE OR REPLACE FUNCTION public.update_plan(p_planid integer, p_planname character varying, p_monthlyfee numeric, p_description character varying, p_isactive boolean, p_color character varying, p_voice_allowance integer, p_data_allowance integer, p_sms_allowance integer, p_voice_free integer, p_data_free integer, p_sms_free integer)
 RETURNS boolean
 LANGUAGE plpgsql
AS $function$
BEGIN
    -- Check if plan exists
    IF NOT EXISTS(SELECT 1 FROM plans WHERE planid = p_planid) THEN
        RETURN FALSE;
    END IF;
    
    -- Update main plans table
    UPDATE plans 
    SET 
        planname = p_planname,
        monthlyfee = p_monthlyfee,
        description = p_description,
        isactive = p_isactive,
        color = p_color
    WHERE planid = p_planid;
    
    -- Update or insert voice allowance
    IF EXISTS(SELECT 1 FROM plan_allowances WHERE planid = p_planid AND servicetype = 'VOICE') THEN
        UPDATE plan_allowances SET includedunits = p_voice_allowance 
        WHERE planid = p_planid AND servicetype = 'VOICE';
    ELSE
        INSERT INTO plan_allowances (planid, servicetype, includedunits)
        VALUES (p_planid, 'VOICE', p_voice_allowance);
    END IF;
    
    -- Update or insert data allowance
    IF EXISTS(SELECT 1 FROM plan_allowances WHERE planid = p_planid AND servicetype = 'DATA') THEN
        UPDATE plan_allowances SET includedunits = p_data_allowance 
        WHERE planid = p_planid AND servicetype = 'DATA';
    ELSE
        INSERT INTO plan_allowances (planid, servicetype, includedunits)
        VALUES (p_planid, 'DATA', p_data_allowance);
    END IF;
    
    -- Update or insert SMS allowance
    IF EXISTS(SELECT 1 FROM plan_allowances WHERE planid = p_planid AND servicetype = 'SMS') THEN
        UPDATE plan_allowances SET includedunits = p_sms_allowance 
        WHERE planid = p_planid AND servicetype = 'SMS';
    ELSE
        INSERT INTO plan_allowances (planid, servicetype, includedunits)
        VALUES (p_planid, 'SMS', p_sms_allowance);
    END IF;
    
    -- Update or insert voice free units
    IF EXISTS(SELECT 1 FROM plan_free_units WHERE planid = p_planid AND servicetype = 'VOICE') THEN
        UPDATE plan_free_units SET freeunits = p_voice_free 
        WHERE planid = p_planid AND servicetype = 'VOICE';
    ELSE
        INSERT INTO plan_free_units (planid, servicetype, freeunits)
        VALUES (p_planid, 'VOICE', p_voice_free);
    END IF;
    
    -- Update or insert data free units
    IF EXISTS(SELECT 1 FROM plan_free_units WHERE planid = p_planid AND servicetype = 'DATA') THEN
        UPDATE plan_free_units SET freeunits = p_data_free 
        WHERE planid = p_planid AND servicetype = 'DATA';
    ELSE
        INSERT INTO plan_free_units (planid, servicetype, freeunits)
        VALUES (p_planid, 'DATA', p_data_free);
    END IF;
    
    -- Update or insert SMS free units
    IF EXISTS(SELECT 1 FROM plan_free_units WHERE planid = p_planid AND servicetype = 'SMS') THEN
        UPDATE plan_free_units SET freeunits = p_sms_free 
        WHERE planid = p_planid AND servicetype = 'SMS';
    ELSE
        INSERT INTO plan_free_units (planid, servicetype, freeunits)
        VALUES (p_planid, 'SMS', p_sms_free);
    END IF;
    
    RETURN TRUE;
    
EXCEPTION WHEN OTHERS THEN
    RETURN FALSE;
END;
$function$;


-- ============================================================================
-- FUNCTION: update_plan_status
-- ============================================================================
-- DESCRIPTION:
--   Toggles a plan's active/inactive status without modifying other fields.
--   Used to enable or disable plans for new subscriptions.
--
-- PARAMETERS:
--   p_planid  (INTEGER) - Plan ID to update
--   p_isactive (BOOLEAN) - New active status (true = active, false = inactive)
--
-- RETURNS: BOOLEAN
--   TRUE  - Status updated successfully
--   FALSE - Plan not found or update failed
--
-- NOTES:
--   - Existing subscribers on inactive plans are not affected
--   - Inactive plans cannot be assigned to new subscribers
--
-- EXAMPLE:
--   SELECT update_plan_status(3, false);
--   -- Returns: true (Plan 3 is now inactive)
-- ============================================================================

CREATE OR REPLACE FUNCTION public.update_plan_status(p_planid integer, p_isactive boolean)
 RETURNS boolean
 LANGUAGE plpgsql
AS $function$
BEGIN
    -- Check if plan exists
    IF NOT EXISTS(SELECT 1 FROM plans WHERE planid = p_planid) THEN
        RETURN FALSE;
    END IF;
    
    -- Update only the status
    UPDATE plans 
    SET isactive = p_isactive
    WHERE planid = p_planid;
    
    -- Return TRUE if a row was updated
    RETURN FOUND;
    
EXCEPTION WHEN OTHERS THEN
    RETURN FALSE;
END;
$function$;


-- ============================================================================
-- FUNCTION: updatesubscriberwithoutplan
-- ============================================================================
-- DESCRIPTION:
--   Updates subscriber information without changing their subscription plan.
--   Used when editing name, address, status, or other non-plan fields.
--
-- PARAMETERS:
--   p_msisdn           (VARCHAR)  - New phone number
--   p_name             (VARCHAR)  - New name
--   p_internationalid  (VARCHAR)  - New International ID
--   p_address          (VARCHAR)  - New address
--   p_planid           (INTEGER)  - Current plan ID (unchanged)
--   p_isactive         (BOOLEAN)  - New active status
--   p_update_sub_id    (INTEGER)  - Subscriber ID to update
--
-- RETURNS: BOOLEAN
--   TRUE  - Update successful
--   FALSE - Subscriber not found
--
-- EXAMPLE:
--   SELECT updatesubscriberwithoutplan('+20123456789', 'Jane Smith', 'ID123456', '456 New St', 1, true, 100);
--   -- Returns: true
-- ============================================================================

CREATE OR REPLACE FUNCTION public.updatesubscriberwithoutplan(p_msisdn character varying, p_name character varying, p_internationalid character varying, p_address character varying, p_planid integer, p_isactive boolean, p_update_sub_id integer)
 RETURNS boolean
 LANGUAGE plpgsql
AS $function$ 
BEGIN 
  UPDATE subscribers SET 
    msisdn = P_msisdn,
    name = P_name,
    internationalid = P_internationalid,
    address = P_address,
    planid = P_planid,
    isactive = P_isactive 
  WHERE subscriberid = P_Update_Sub_ID;

  RETURN FOUND;
END;
$function$;


-- ============================================================================
-- END OF DATABASE FUNCTIONS
-- ============================================================================
