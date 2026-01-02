SET SERVEROUTPUT ON;

/* =========================================================
   TEST 1: OK - arrival + departure (train stopped)
   ========================================================= */
DECLARE
v_res NUMBER;
    v_cnt_log NUMBER;
    v_cnt_dep NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 1: Register arrival + departure (5421)');

SAVEPOINT sp_t1;

-- cleanup for rerun (will be rolled back)
DELETE FROM TrainLogDeparture WHERE TrainLogTraintrain_id = 5421;
DELETE FROM TrainLog          WHERE Traintrain_id = 5421;

v_res := usbd42_register_train_movement(
        5421,
        '   mEdWaY   ', -- test case/space-insensitive
        TO_TIMESTAMP('2025-10-03 15:05:00','YYYY-MM-DD HH24:MI:SS'),
        TO_TIMESTAMP('2025-10-03 15:10:00','YYYY-MM-DD HH24:MI:SS')
    );

SELECT COUNT(*) INTO v_cnt_log FROM TrainLog WHERE Traintrain_id = 5421;
SELECT COUNT(*) INTO v_cnt_dep FROM TrainLogDeparture WHERE TrainLogTraintrain_id = 5421;

IF v_res <> 1 OR v_cnt_log <> 1 OR v_cnt_dep <> 1 THEN
        RAISE_APPLICATION_ERROR(-20042, 'TEST 1 failed: unexpected counts/return.');
END IF;

    DBMS_OUTPUT.PUT_LINE('✓ SUCCESS: Movement registered (return=' || v_res || ')');
    DBMS_OUTPUT.PUT_LINE('✓ TrainLog=' || v_cnt_log || ' | TrainLogDeparture=' || v_cnt_dep);
    DBMS_OUTPUT.PUT_LINE(' ');

ROLLBACK TO sp_t1; -- keep DB clean
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✗ ERROR: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
ROLLBACK TO sp_t1;
END;
/



/* =========================================================
   TEST 2: OK - arrival only (no stop)
   ========================================================= */
DECLARE
v_res NUMBER;
    v_cnt_log NUMBER;
    v_cnt_dep NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 2: Register arrival only (5437)');

SAVEPOINT sp_t2;

-- cleanup for rerun (will be rolled back)
DELETE FROM TrainLogDeparture WHERE TrainLogTraintrain_id = 5437;
DELETE FROM TrainLog          WHERE Traintrain_id = 5437;

v_res := usbd42_register_train_movement(
        5437,
        'Medway',
        TO_TIMESTAMP('2025-10-06 18:00:00','YYYY-MM-DD HH24:MI:SS'),
        NULL
    );

SELECT COUNT(*) INTO v_cnt_log FROM TrainLog WHERE Traintrain_id = 5437;
SELECT COUNT(*) INTO v_cnt_dep FROM TrainLogDeparture WHERE TrainLogTraintrain_id = 5437;

IF v_res <> 1 OR v_cnt_log <> 1 OR v_cnt_dep <> 0 THEN
        RAISE_APPLICATION_ERROR(-20042, 'TEST 2 failed: unexpected counts/return.');
END IF;

    DBMS_OUTPUT.PUT_LINE('✓ SUCCESS: Arrival registered (return=' || v_res || ')');
    DBMS_OUTPUT.PUT_LINE('✓ TrainLog=' || v_cnt_log || ' | TrainLogDeparture=' || v_cnt_dep);
    DBMS_OUTPUT.PUT_LINE(' ');

ROLLBACK TO sp_t2; -- keep DB clean
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✗ ERROR: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
ROLLBACK TO sp_t2;
END;
/



/* =========================================================
   TEST 3: ERROR - Train not found
   ========================================================= */
DECLARE
v_res NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 3: Train not found (expected error)');

    v_res := usbd42_register_train_movement(
        999999,
        'Medway',
        TO_TIMESTAMP('2025-10-06 18:00:00','YYYY-MM-DD HH24:MI:SS'),
        NULL
    );

    DBMS_OUTPUT.PUT_LINE('✗ ERROR: Should have failed but returned=' || v_res);
    DBMS_OUTPUT.PUT_LINE(' ');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/



/* =========================================================
   TEST 4: ERROR - Missing operator_id (mandatory)
   ========================================================= */
DECLARE
v_res NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 4: Missing operator_id (expected error)');

    v_res := usbd42_register_train_movement(
        5421,
        NULL,
        TO_TIMESTAMP('2025-10-03 15:05:00','YYYY-MM-DD HH24:MI:SS'),
        NULL
    );

    DBMS_OUTPUT.PUT_LINE('✗ ERROR: Should have failed but returned=' || v_res);
    DBMS_OUTPUT.PUT_LINE(' ');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/



/* =========================================================
   TEST 5: ERROR - Arrival is NULL (mandatory)
   ========================================================= */
DECLARE
v_res NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 5: Missing arrival timestamp (expected error)');

    v_res := usbd42_register_train_movement(
        5421,
        'Medway',
        NULL,
        NULL
    );

    DBMS_OUTPUT.PUT_LINE('✗ ERROR: Should have failed but returned=' || v_res);
    DBMS_OUTPUT.PUT_LINE(' ');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/



/* =========================================================
   TEST 6: ERROR - Invalid chronology (departure < arrival)
   ========================================================= */
DECLARE
v_res NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 6: Invalid chronology departure < arrival (expected error)');

SAVEPOINT sp_t6;

-- cleanup for rerun (will be rolled back)
DELETE FROM TrainLogDeparture WHERE TrainLogTraintrain_id = 5435;
DELETE FROM TrainLog          WHERE Traintrain_id = 5435;

v_res := usbd42_register_train_movement(
        5435,
        'Medway',
        TO_TIMESTAMP('2025-10-03 22:00:00','YYYY-MM-DD HH24:MI:SS'),
        TO_TIMESTAMP('2025-10-03 21:59:00','YYYY-MM-DD HH24:MI:SS')
    );

    DBMS_OUTPUT.PUT_LINE('✗ ERROR: Should have failed but returned=' || v_res);
    DBMS_OUTPUT.PUT_LINE(' ');

ROLLBACK TO sp_t6;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
ROLLBACK TO sp_t6;
END;
/



/* =========================================================
   TEST 7: ERROR - Duplicate movement (same train_id)
   ========================================================= */
DECLARE
v_res NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 7: Duplicate movement (expected error)');

SAVEPOINT sp_t7;

-- cleanup for rerun (will be rolled back)
DELETE FROM TrainLogDeparture WHERE TrainLogTraintrain_id = 5421;
DELETE FROM TrainLog          WHERE Traintrain_id = 5421;

-- first call OK
v_res := usbd42_register_train_movement(
        5421,
        'Medway',
        TO_TIMESTAMP('2025-10-03 15:05:00','YYYY-MM-DD HH24:MI:SS'),
        NULL
    );
    DBMS_OUTPUT.PUT_LINE('First insert OK (return=' || v_res || ')');

    -- second call should fail (movement already exists)
    v_res := usbd42_register_train_movement(
        5421,
        'Medway',
        TO_TIMESTAMP('2025-10-03 16:00:00','YYYY-MM-DD HH24:MI:SS'),
        NULL
    );

    DBMS_OUTPUT.PUT_LINE('✗ ERROR: Should have failed but returned=' || v_res);
    DBMS_OUTPUT.PUT_LINE(' ');

ROLLBACK TO sp_t7;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
ROLLBACK TO sp_t7;
END;
/
