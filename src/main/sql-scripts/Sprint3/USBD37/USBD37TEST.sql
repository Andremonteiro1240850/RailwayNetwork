DECLARE
v_new_id     NUMBER;
    v_cnt_wagon  NUMBER;
    v_cnt_links  NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 1: SUCCESS (auto id)');

    v_new_id := fn_register_wagon(
            NULL,
            'Medway',
            2024,
            1104,
            SYS.ODCINUMBERLIST(1668, 1435)
                );

SELECT COUNT(*) INTO v_cnt_wagon
FROM Wagon
WHERE wagon_id = v_new_id;

SELECT COUNT(*) INTO v_cnt_links
FROM Gauge_Wagon
WHERE Wagonwagon_id = v_new_id;

DBMS_OUTPUT.PUT_LINE('✓ NEW_WAGON_ID=' || v_new_id);
    DBMS_OUTPUT.PUT_LINE('✓ Wagon count=' || v_cnt_wagon);
    DBMS_OUTPUT.PUT_LINE('✓ Gauge_Wagon links count=' || v_cnt_links);

FOR g IN (
        SELECT Gaugecode
        FROM Gauge_Wagon
        WHERE Wagonwagon_id = v_new_id
        ORDER BY Gaugecode
        ) LOOP
            DBMS_OUTPUT.PUT_LINE('GAUGE=' || g.Gaugecode);
END LOOP;

    IF v_cnt_wagon <> 1 OR v_cnt_links <> 2 THEN
        RAISE_APPLICATION_ERROR(-20037, 'Teste falhou: counts inesperados.');
END IF;

    DBMS_OUTPUT.PUT_LINE('TEST 1 PASSOU');
    DBMS_OUTPUT.PUT_LINE(' ');

ROLLBACK; -- não sujar BD
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✗ ERROR: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
ROLLBACK;
END;
/


/* =========================================================
   TEST 2: SUCCESS (explicit wagon_id)
   ========================================================= */
DECLARE
v_new_id     NUMBER;
    v_cnt_wagon  NUMBER;
    v_cnt_links  NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 2: SUCCESS (explicit wagon_id=999999)');

    v_new_id := fn_register_wagon(
            999999,
            'Medway',
            2024,
            1104,
            SYS.ODCINUMBERLIST(1668)
                );

SELECT COUNT(*) INTO v_cnt_wagon
FROM Wagon
WHERE wagon_id = v_new_id;

SELECT COUNT(*) INTO v_cnt_links
FROM Gauge_Wagon
WHERE Wagonwagon_id = v_new_id;

DBMS_OUTPUT.PUT_LINE('✓ NEW_WAGON_ID=' || v_new_id);
    DBMS_OUTPUT.PUT_LINE('✓ Wagon count=' || v_cnt_wagon);
    DBMS_OUTPUT.PUT_LINE('✓ Gauge_Wagon links count=' || v_cnt_links);

    IF v_cnt_wagon <> 1 OR v_cnt_links <> 1 THEN
        RAISE_APPLICATION_ERROR(-20037, 'Teste falhou: counts inesperados.');
END IF;

    DBMS_OUTPUT.PUT_LINE('TEST 2 PASSOU');
    DBMS_OUTPUT.PUT_LINE(' ');

ROLLBACK;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✗ ERROR: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
ROLLBACK;
END;
/


/* =========================================================
   TEST 3: Expected error - missing gauges
   ========================================================= */
DECLARE
v_new_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 3: Missing gauges (expected error)');

    v_new_id := fn_register_wagon(
            NULL,
            'Medway',
            2024,
            1104,
            NULL
                );

    DBMS_OUTPUT.PUT_LINE('✗ ERROR: Should have failed but returned=' || v_new_id);
    DBMS_OUTPUT.PUT_LINE(' ');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
ROLLBACK;
END;
/


/* =========================================================
   TEST 4: Expected error - operator does not exist
   ========================================================= */
DECLARE
v_new_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 4: Invalid operator (expected error)');

    v_new_id := fn_register_wagon(
            NULL,
            'NO_SUCH_OPERATOR',
            2024,
            1104,
            SYS.ODCINUMBERLIST(1668)
                );

    DBMS_OUTPUT.PUT_LINE('✗ ERROR: Should have failed but returned=' || v_new_id);
    DBMS_OUTPUT.PUT_LINE(' ');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
ROLLBACK;
END;
/


/* =========================================================
   TEST 5: Expected error - wagon model does not exist
   ========================================================= */
DECLARE
v_new_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 5: Invalid wagon model (expected error)');

    v_new_id := fn_register_wagon(
            NULL,
            'Medway',
            2024,
            999999,
            SYS.ODCINUMBERLIST(1668)
                );

    DBMS_OUTPUT.PUT_LINE('✗ ERROR: Should have failed but returned=' || v_new_id);
    DBMS_OUTPUT.PUT_LINE(' ');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
ROLLBACK;
END;
/


/* =========================================================
   TEST 6: Expected error - gauge does not exist
   ========================================================= */
DECLARE
v_new_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 6: Invalid gauge code (expected error)');

    v_new_id := fn_register_wagon(
            NULL,
            'Medway',
            2024,
            1104,
            SYS.ODCINUMBERLIST(9999)
                );

    DBMS_OUTPUT.PUT_LINE('✗ ERROR: Should have failed but returned=' || v_new_id);
    DBMS_OUTPUT.PUT_LINE(' ');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
ROLLBACK;
END;
/


/* =========================================================
   TEST 7: Expected error - duplicate gauge in input list
   ========================================================= */
DECLARE
v_new_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 7: Duplicate gauge in input list (expected error)');

    v_new_id := fn_register_wagon(
            NULL,
            'Medway',
            2024,
            1104,
            SYS.ODCINUMBERLIST(1668, 1668)
                );

    DBMS_OUTPUT.PUT_LINE('✗ ERROR: Should have failed but returned=' || v_new_id);
    DBMS_OUTPUT.PUT_LINE(' ');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
ROLLBACK;
END;
/


/* =========================================================
   TEST 8: Expected error - wagon_id already exists (use one from your seed)
   Example: 77077 exists in your inserts
   ========================================================= */
DECLARE
v_new_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 8: Wagon ID already exists (expected error)');

    v_new_id := fn_register_wagon(
            77077,
            'Medway',
            2024,
            1104,
            SYS.ODCINUMBERLIST(1668)
                );

    DBMS_OUTPUT.PUT_LINE('✗ ERROR: Should have failed but returned=' || v_new_id);
    DBMS_OUTPUT.PUT_LINE(' ');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
ROLLBACK;
END;
/
