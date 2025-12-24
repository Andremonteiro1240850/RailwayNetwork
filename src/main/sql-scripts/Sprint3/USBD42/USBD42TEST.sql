
/* =========================================================
   TEST 1: OK - arrival + departure (train stopped)
   ========================================================= */
DECLARE
v_res NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 1: Register arrival + departure (5421)');

    -- cleanup for rerun
DELETE FROM TrainLogDeparture WHERE TrainLogTraintrain_id = 5421;
DELETE FROM TrainLog          WHERE Traintrain_id = 5421;
COMMIT;

v_res := usbd42_register_train_movement(
            5421,
            'Medway',
            TO_TIMESTAMP('2025-10-03 15:05:00','YYYY-MM-DD HH24:MI:SS'),
            TO_TIMESTAMP('2025-10-03 15:10:00','YYYY-MM-DD HH24:MI:SS')
             );

    DBMS_OUTPUT.PUT_LINE('✓ SUCCESS: Movement registered (return=' || v_res || ')');
    DBMS_OUTPUT.PUT_LINE(' ');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✗ ERROR: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/


/* =========================================================
   TEST 2: OK - arrival only (no stop)
   ========================================================= */
DECLARE
v_res NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 2: Register arrival only (5437)');

    -- cleanup for rerun
DELETE FROM TrainLogDeparture WHERE TrainLogTraintrain_id = 5437;
DELETE FROM TrainLog          WHERE Traintrain_id = 5437;
COMMIT;

v_res := usbd42_register_train_movement(
            5437,
            'Medway',
            TO_TIMESTAMP('2025-10-06 18:00:00','YYYY-MM-DD HH24:MI:SS'),
            NULL
             );

    DBMS_OUTPUT.PUT_LINE('✓ SUCCESS: Arrival registered without departure (return=' || v_res || ')');
    DBMS_OUTPUT.PUT_LINE(' ');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✗ ERROR: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
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

    -- cleanup for rerun (so we don't fail earlier on duplicate)
DELETE FROM TrainLogDeparture WHERE TrainLogTraintrain_id = 5435;
DELETE FROM TrainLog          WHERE Traintrain_id = 5435;
COMMIT;

v_res := usbd42_register_train_movement(
            5435,
            'Medway',
            TO_TIMESTAMP('2025-10-03 22:00:00','YYYY-MM-DD HH24:MI:SS'),
            TO_TIMESTAMP('2025-10-03 21:59:00','YYYY-MM-DD HH24:MI:SS')
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
   TEST 7: ERROR - Duplicate movement (same train_id)
   ========================================================= */
DECLARE
v_res NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 7: Duplicate movement (expected error)');

    -- cleanup and insert once
DELETE FROM TrainLogDeparture WHERE TrainLogTraintrain_id = 5421;
DELETE FROM TrainLog          WHERE Traintrain_id = 5421;
COMMIT;

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

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/
DECLARE
v_res NUMBER;
BEGIN
DELETE FROM TrainLogDeparture WHERE TrainLogTraintrain_id = 5421;
DELETE FROM TrainLog          WHERE Traintrain_id = 5421;
COMMIT;

v_res := usbd42_register_train_movement(
            5421,
            'Medway',
            TO_TIMESTAMP('2025-10-03 15:05:00','YYYY-MM-DD HH24:MI:SS'),
            TO_TIMESTAMP('2025-10-03 15:10:00','YYYY-MM-DD HH24:MI:SS')
             );

    DBMS_OUTPUT.PUT_LINE('FINAL 5421 -> ' || v_res);
END;
/


/* =========================================================
   VERIFICATION QUERIES
   ========================================================= */
BEGIN
    DBMS_OUTPUT.PUT_LINE('VERIFICATION: Movements registered for (5421, 5435, 5437)');
    DBMS_OUTPUT.PUT_LINE(' ');
END;
/

SELECT
    tl.Traintrain_id   AS "TRAIN_ID",
    tl.Trainoperator_id AS "OPERATOR_ID",
    TO_CHAR(tl.Arrival,'YYYY-MM-DD HH24:MI:SS') AS "ARRIVAL",
    (SELECT TO_CHAR(tld.Departure,'YYYY-MM-DD HH24:MI:SS')
     FROM TrainLogDeparture tld
     WHERE tld.TrainLogTraintrain_id = tl.Traintrain_id) AS "DEPARTURE"
FROM TrainLog tl
WHERE tl.Traintrain_id IN (5421, 5435, 5437)
ORDER BY tl.Traintrain_id;
