-- TEST 1: Successful association
DECLARE
    association_record freight_train_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 1: Successful association');
    DBMS_OUTPUT.PUT_LINE('Description: Associates freight 2001 (5 wagons) with train 5421, operator ''Medway'', locomotive 5630');
    DBMS_OUTPUT.PUT_LINE('Expected: SUCCESS - Freight associated with train');
    -- Associate freight 2001 (has 5 wagons) with train 5421, operator 'Medway', locomotive 5630
    -- Freight 2001: wagons 2001, 2002, 2004, 2005, 2006
    -- Train 5421: has locomotives 5621, 5623, 5630 associated
    association_record := freight_train_association_record(
        freight_identifier => 2001,
        train_identifier => 5421,
        operator_identifier => 'Medway',
        locomotive_identifier => 5630
    );
    prc_usbd34_associate_freight_with_train(association_record);
    
    DBMS_OUTPUT.PUT_LINE('✓ SUCCESS: Freight associated with train');
    DBMS_OUTPUT.PUT_LINE(' ');
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✗ ERROR: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- TEST 2: Freight not found
DECLARE
    association_record freight_train_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 2: Freight not found');
    DBMS_OUTPUT.PUT_LINE('Description: Attempts to associate non-existent freight 99999 with train 5421');
    DBMS_OUTPUT.PUT_LINE('Expected: ERROR - Freight not found');
    association_record := freight_train_association_record(
        freight_identifier => 99999,
        train_identifier => 5421,
        operator_identifier => 'Medway',
        locomotive_identifier => 5630
    );
    prc_usbd34_associate_freight_with_train(association_record);
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- TEST 3: Train not found
DECLARE
    association_record freight_train_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 3: Train not found');
    DBMS_OUTPUT.PUT_LINE('Description: Attempts to associate freight 2001 with non-existent train 99999');
    DBMS_OUTPUT.PUT_LINE('Expected: ERROR - Train not found');
    association_record := freight_train_association_record(
        freight_identifier => 2001,
        train_identifier => 99999,
        operator_identifier => 'Medway',
        locomotive_identifier => 5630
    );
    prc_usbd34_associate_freight_with_train(association_record);
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- TEST 4: Locomotive not found
DECLARE
    association_record freight_train_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 4: Locomotive not found');
    DBMS_OUTPUT.PUT_LINE('Description: Attempts to use non-existent locomotive 99999 with train 5421');
    DBMS_OUTPUT.PUT_LINE('Expected: ERROR - Locomotive not found');
    association_record := freight_train_association_record(
        freight_identifier => 2001,
        train_identifier => 5421,
        operator_identifier => 'Medway',
        locomotive_identifier => 99999
    );
    prc_usbd34_associate_freight_with_train(association_record);

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- TEST 5: Locomotive not associated with train
DECLARE
    association_record freight_train_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 5: Locomotive not associated with train');
    DBMS_OUTPUT.PUT_LINE('Description: Attempts to use locomotive 1903 (belongs to train 5435) with train 5421');
    DBMS_OUTPUT.PUT_LINE('Expected: ERROR - Locomotive not associated with train');
    -- Locomotive 1903 exists and is associated with train 5435, not with train 5421
    -- This should raise an error when trying to use it with train 5421
    association_record := freight_train_association_record(
        freight_identifier => 2001,
        train_identifier => 5421,
        operator_identifier => 'Medway',
        locomotive_identifier => 1903
    );
    prc_usbd34_associate_freight_with_train(association_record);
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- TEST 6: Freight has no wagons
DECLARE
    association_record freight_train_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 6: Freight has no wagons');
    DBMS_OUTPUT.PUT_LINE('Description: Attempts to associate freight without wagons (using non-existent freight 9999)');
    DBMS_OUTPUT.PUT_LINE('Expected: ERROR - Freight not found or freight has no wagons');

    -- NOTE: All existing freights in the database have wagons.
    -- To test this scenario, you would need to create a freight without wagons first.
    -- For now, using a non-existent freight ID to demonstrate the validation
    -- In a real scenario, you would create a freight and then try to associate it
    association_record := freight_train_association_record(
        freight_identifier => 9999,  -- Non-existent freight (will fail at freight validation first)
        train_identifier => 5421,
        operator_identifier => 'Medway',
        locomotive_identifier => 5630
    );
    prc_usbd34_associate_freight_with_train(association_record);
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE('NOTE: To test "no wagons" scenario, create a freight without wagons first');
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- TEST 7: Update existing association (same train)
DECLARE
    association_record freight_train_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 7: Update existing association (same train)');
    DBMS_OUTPUT.PUT_LINE('Description: Updates freight 2001 association to use locomotive 5623 instead of 5621');
    DBMS_OUTPUT.PUT_LINE('Expected: SUCCESS - Association updated');
    -- Freight 2001 is already associated with train 5421 with locomotive 5621
    -- Update it to use locomotive 5623 (also associated with train 5421)
    association_record := freight_train_association_record(
        freight_identifier => 2001,
        train_identifier => 5421,
        operator_identifier => 'Medway',
        locomotive_identifier => 5623
    );
    prc_usbd34_associate_freight_with_train(association_record);
    
    DBMS_OUTPUT.PUT_LINE('✓ SUCCESS: Association updated');
    DBMS_OUTPUT.PUT_LINE(' ');
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✗ ERROR: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- TEST 8: Associate freight with different train
DECLARE
    association_record freight_train_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 8: Associate freight with different train');
    DBMS_OUTPUT.PUT_LINE('Description: Attempts to associate freight 2002 (currently with train 5421) with train 5435');
    DBMS_OUTPUT.PUT_LINE('Expected: ERROR - Freight already associated with different train');
    -- Try to associate freight 2002 (currently with train 5421) with train 5435
    -- This should raise an error because freight is already associated with a different train
    association_record := freight_train_association_record(
        freight_identifier => 2002,
        train_identifier => 5435,
        operator_identifier => 'Medway',
        locomotive_identifier => 1903
    );
    prc_usbd34_associate_freight_with_train(association_record);
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- TEST 9: Associate freight 2003 with train 5421 (different locomotive)
DECLARE
    association_record freight_train_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 9: Associate freight with same train using different locomotive');
    DBMS_OUTPUT.PUT_LINE('Description: Associates freight 2003 with train 5421 using locomotive 5630');
    DBMS_OUTPUT.PUT_LINE('Expected: SUCCESS - Association updated');
    -- Freight 2003 has 2 wagons (1011, 1012) and is currently with train 5421
    -- Update to use locomotive 5630
    association_record := freight_train_association_record(
        freight_identifier => 2003,
        train_identifier => 5421,
        operator_identifier => 'Medway',
        locomotive_identifier => 5630
    );
    prc_usbd34_associate_freight_with_train(association_record);
    
    DBMS_OUTPUT.PUT_LINE('✓ SUCCESS: Freight 2003 associated with train 5421');
    DBMS_OUTPUT.PUT_LINE(' ');
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✗ ERROR: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

BEGIN
    DBMS_OUTPUT.PUT_LINE('VERIFICATION: Current freight-train associations');
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
JOIN FreighTrain ft ON ft.Freightfreight_id = f.FreighTrainFreigth_id 
                   AND ft.Traintrain_id = f.FreighTrainTraintrain_id
ORDER BY f.freight_id;
