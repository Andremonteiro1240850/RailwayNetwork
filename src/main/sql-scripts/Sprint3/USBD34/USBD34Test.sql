-- ===========================================
-- USBD34 Test Script
-- ===========================================
-- This script tests the prc_usbd34_associate_freight_with_train procedure
-- with various scenarios including success cases and error cases.
--
-- Real IDs used from initial_data.sql:
--   Trains: 5421 (Medway), 5435 (Medway), 5437 (Medway)
--   Locomotives: 5621, 5623, 5630 (train 5421), 1903 (train 5435), 5034 (train 5437)
--   Freights: 2001 (5 wagons), 2002 (1 wagon), 2003 (2 wagons), 2004 (1 wagon), 
--             2005 (4 wagons), 2006 (2 wagons), 2007 (3 wagons), 2050, 2051
--
-- Note: Before running these tests, ensure:
--   1. The procedure is created (run USBD34.sql first)
--   2. Initial data is loaded (run initial_data.sql)
--   3. The locomotive is already associated with the train via Locomotive_Train
-- ===========================================

-- Enable output (if needed in IntelliJ SQL Console)
-- SET SERVEROUTPUT ON;

-- ========================================
-- TEST 1: Successful association
-- ========================================
DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('TEST 1: Successful association');
    DBMS_OUTPUT.PUT_LINE('========================================');
    
    -- Associate freight 2001 (has 5 wagons) with train 5421, operator 'Medway', locomotive 5630
    -- Freight 2001: wagons 2001, 2002, 2004, 2005, 2006
    -- Train 5421: has locomotives 5621, 5623, 5630 associated
    prc_usbd34_associate_freight_with_train(
        p_freight_id    => 2001,
        p_train_id      => 5421,
        p_operator_id   => 'Medway',
        p_locomotive_id => 5630
    );
    
    DBMS_OUTPUT.PUT_LINE('✓ SUCCESS: Freight associated with train');
    DBMS_OUTPUT.PUT_LINE(' ');
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✗ ERROR: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- ========================================
-- TEST 2: Freight not found
-- ========================================
DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('TEST 2: Freight not found');
    DBMS_OUTPUT.PUT_LINE('========================================');
    
    prc_usbd34_associate_freight_with_train(
        p_freight_id    => 99999,
        p_train_id      => 5421,
        p_operator_id   => 'Medway',
        p_locomotive_id => 5630
    );
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- ========================================
-- TEST 8: Associate freight with different train
-- ========================================
DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('TEST 8: Associate freight with different train');
    DBMS_OUTPUT.PUT_LINE('========================================');
    
    -- Try to associate freight 2002 (currently with train 5421) with train 5435
    -- This should raise an error because freight is already associated with a different train
    prc_usbd34_associate_freight_with_train(
        p_freight_id    => 2002,
        p_train_id      => 5435,
        p_operator_id   => 'Medway',
        p_locomotive_id => 1903
    );
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- ========================================
-- TEST 9: Associate freight 2003 with train 5421 (different locomotive)
-- ========================================
DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('TEST 9: Associate freight 2003 with train 5421');
    DBMS_OUTPUT.PUT_LINE('========================================');
    
    -- Freight 2003 has 2 wagons (1011, 1012) and is currently with train 5421
    -- Update to use locomotive 5630
    prc_usbd34_associate_freight_with_train(
        p_freight_id    => 2003,
        p_train_id      => 5421,
        p_operator_id   => 'Medway',
        p_locomotive_id => 5630
    );
    
    DBMS_OUTPUT.PUT_LINE('✓ SUCCESS: Freight 2003 associated with train 5421');
    DBMS_OUTPUT.PUT_LINE(' ');
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✗ ERROR: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- ========================================
-- TEST 3: Train not found
-- ========================================
DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('TEST 3: Train not found');
    DBMS_OUTPUT.PUT_LINE('========================================');
    
    prc_usbd34_associate_freight_with_train(
        p_freight_id    => 2001,
        p_train_id      => 99999,
        p_operator_id   => 'Medway',
        p_locomotive_id => 5630
    );
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- ========================================
-- TEST 4: Locomotive not found
-- ========================================
DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('TEST 4: Locomotive not found');
    DBMS_OUTPUT.PUT_LINE('========================================');
    
    prc_usbd34_associate_freight_with_train(
        p_freight_id    => 2001,
        p_train_id      => 5421,
        p_operator_id   => 'Medway',
        p_locomotive_id => 99999
    );
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- ========================================
-- TEST 5: Locomotive not associated with train
-- ========================================
DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('TEST 5: Locomotive not associated with train');
    DBMS_OUTPUT.PUT_LINE('========================================');
    
    -- Locomotive 1903 exists and is associated with train 5435, not with train 5421
    -- This should raise an error when trying to use it with train 5421
    prc_usbd34_associate_freight_with_train(
        p_freight_id    => 2001,
        p_train_id      => 5421,
        p_operator_id   => 'Medway',
        p_locomotive_id => 1903
    );
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- ========================================
-- TEST 6: Freight has no wagons
-- ========================================
DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('TEST 6: Freight has no wagons');
    DBMS_OUTPUT.PUT_LINE('========================================');
    
    -- NOTE: All existing freights in the database have wagons.
    -- To test this scenario, you would need to create a freight without wagons first.
    -- For now, using a non-existent freight ID to demonstrate the validation
    -- In a real scenario, you would create a freight and then try to associate it
    prc_usbd34_associate_freight_with_train(
        p_freight_id    => 9999,  -- Non-existent freight (will fail at freight validation first)
        p_train_id      => 5421,
        p_operator_id   => 'Medway',
        p_locomotive_id => 5630
    );
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE('NOTE: To test "no wagons" scenario, create a freight without wagons first');
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- ========================================
-- TEST 7: Update existing association (same train)
-- ========================================
DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('TEST 7: Update existing association (same train)');
    DBMS_OUTPUT.PUT_LINE('========================================');
    
    -- Freight 2001 is already associated with train 5421 with locomotive 5621
    -- Update it to use locomotive 5623 (also associated with train 5421)
    prc_usbd34_associate_freight_with_train(
        p_freight_id    => 2001,
        p_train_id      => 5421,
        p_operator_id   => 'Medway',
        p_locomotive_id => 5623
    );
    
    DBMS_OUTPUT.PUT_LINE('✓ SUCCESS: Association updated');
    DBMS_OUTPUT.PUT_LINE(' ');
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✗ ERROR: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- ========================================
-- VERIFICATION: Query current associations
-- ========================================
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('VERIFICATION: Current freight-train associations');
    DBMS_OUTPUT.PUT_LINE('========================================');
END;
/

SELECT
    f.freight_id AS "Freight ID",
    ft.Traintrain_id AS "Train ID",
    ft.Trainoperator_id AS "Operator",
    ft.TrainLocomotivelocomotive_id AS "Locomotive ID",
    (SELECT COUNT(*) 
     FROM Wagon_In_Freight wif 
     WHERE wif.Freight_id = f.freight_id) AS "Wagon Count"
FROM Freight f
JOIN FreighTrain ft ON ft.Freigth_id = f.FreighTrainFreigth_id 
                   AND ft.Traintrain_id = f.FreighTrainTraintrain_id
ORDER BY f.freight_id;

