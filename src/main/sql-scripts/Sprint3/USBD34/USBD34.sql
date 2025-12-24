-- Parameters:
--   p_freight_id    - ID of the freight to associate
--   p_train_id      - ID of the train
--   p_operator_id   - ID of the train operator
--   p_locomotive_id - ID of the locomotive (required for FreighTrain table)

-- Exceptions:
--   -20001: Freight not found
--   -20002: Train not found with specified operator
--   -20003: Locomotive not found
--   -20004: Locomotive is not associated with the specified train
--   -20005: Freight has no wagons associated
--   -20006: Freight is already associated with a different train


CREATE OR REPLACE PROCEDURE prc_usbd34_associate_freight_with_train(
    p_freight_id    IN NUMBER,
    p_train_id      IN NUMBER,
    p_operator_id   IN VARCHAR2,
    p_locomotive_id IN NUMBER
) IS
    -- Variables for validations
    v_freight_exists          NUMBER;
    v_train_exists            NUMBER;
    v_locomotive_exists       NUMBER;
    v_locomotive_train_exists NUMBER;
    v_wagon_count             NUMBER;
    v_current_train_id        NUMBER;
    v_current_operator_id     VARCHAR2(255);
    
    -- Custom exceptions
    freight_not_found            EXCEPTION;
    train_not_found              EXCEPTION;
    locomotive_not_found         EXCEPTION;
    locomotive_not_in_train      EXCEPTION;
    freight_has_no_wagons        EXCEPTION;
    freight_already_associated    EXCEPTION;

BEGIN
    -- ===========================================
    -- VALIDATIONS
    -- ===========================================
    
    -- 1. Validate that the freight exists
    BEGIN
        SELECT COUNT(*)
        INTO v_freight_exists
        FROM Freight
        WHERE freight_id = p_freight_id;
        
        IF v_freight_exists = 0 THEN
            RAISE freight_not_found;
        END IF;
    END;
    
    -- 2. Validate that the train exists with the specified operator
    BEGIN
        SELECT COUNT(*)
        INTO v_train_exists
        FROM Train
        WHERE train_id = p_train_id
          AND operator_id = p_operator_id;
        
        IF v_train_exists = 0 THEN
            RAISE train_not_found;
        END IF;
    END;
    
    -- 3. Validate that the locomotive exists
    BEGIN
        SELECT COUNT(*)
        INTO v_locomotive_exists
        FROM Locomotive
        WHERE locomotive_id = p_locomotive_id;
        
        IF v_locomotive_exists = 0 THEN
            RAISE locomotive_not_found;
        END IF;
    END;
    
    -- 4. Validate that the locomotive is associated with the specified train
    BEGIN
        SELECT COUNT(*)
        INTO v_locomotive_train_exists
        FROM Locomotive_Train
        WHERE Locomotivelocomotive_id = p_locomotive_id
          AND Traintrain_id = p_train_id
          AND Trainoperator_id = p_operator_id;
        
        IF v_locomotive_train_exists = 0 THEN
            RAISE locomotive_not_in_train;
        END IF;
    END;
    
    -- 5. Validate that the freight has at least one wagon
    BEGIN
        SELECT COUNT(*)
        INTO v_wagon_count
        FROM Wagon_In_Freight
        WHERE Freight_id = p_freight_id;
        
        IF v_wagon_count = 0 THEN
            RAISE freight_has_no_wagons;
        END IF;
    END;
    
    -- 6. Check if freight is already associated with a different train
    --    (Allow update if same train, raise error if different train)
    BEGIN
        SELECT ft.Traintrain_id, ft.Trainoperator_id
        INTO v_current_train_id, v_current_operator_id
        FROM Freight f
        JOIN FreighTrain ft ON ft.Freigth_id = f.FreighTrainFreigth_id 
                           AND ft.Traintrain_id = f.FreighTrainTraintrain_id
        WHERE f.freight_id = p_freight_id;
        
        -- If already associated with a different train, raise error
        IF v_current_train_id IS NOT NULL 
           AND (v_current_train_id != p_train_id OR v_current_operator_id != p_operator_id) THEN
            RAISE freight_already_associated;
        END IF;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            -- Freight not yet associated, which is fine
            NULL;
    END;
    
    -- ===========================================
    -- TRANSACTION: Associate freight with train
    -- ===========================================
    
    -- Insert or update FreighTrain entry
    -- Using MERGE to handle both insert and update cases
    MERGE INTO FreighTrain ft
    USING (SELECT p_freight_id AS freight_id,
                  p_train_id AS train_id,
                  p_locomotive_id AS locomotive_id,
                  p_operator_id AS operator_id
           FROM dual) src
    ON (ft.Freigth_id = src.freight_id AND ft.Traintrain_id = src.train_id)
    WHEN MATCHED THEN
        UPDATE SET
            ft.TrainLocomotivelocomotive_id = src.locomotive_id,
            ft.Trainoperator_id = src.operator_id
    WHEN NOT MATCHED THEN
        INSERT (Freigth_id, Traintrain_id, TrainLocomotivelocomotive_id, Trainoperator_id)
        VALUES (src.freight_id, src.train_id, src.locomotive_id, src.operator_id);
    
    -- Update Freight table to reference the FreighTrain association
    UPDATE Freight
    SET FreighTrainFreigth_id = p_freight_id,
        FreighTrainTraintrain_id = p_train_id
    WHERE freight_id = p_freight_id;
    
    -- Commit the transaction
    COMMIT;
    
    -- Success message (optional, can be removed if not needed)
    DBMS_OUTPUT.PUT_LINE('Freight ' || p_freight_id || ' successfully associated with train ' || 
                         p_train_id || ' (operator: ' || p_operator_id || '). ' ||
                         v_wagon_count || ' wagon(s) are now associated with the train.');

EXCEPTION
    WHEN freight_not_found THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20001, 'Freight with ID ' || p_freight_id || ' not found.');
    
    WHEN train_not_found THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20002, 'Train with ID ' || p_train_id || 
                                 ' and operator ' || p_operator_id || ' not found.');
    
    WHEN locomotive_not_found THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20003, 'Locomotive with ID ' || p_locomotive_id || ' not found.');
    
    WHEN locomotive_not_in_train THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20004, 'Locomotive ' || p_locomotive_id || 
                                 ' is not associated with train ' || p_train_id || 
                                 ' (operator: ' || p_operator_id || ').');
    
    WHEN freight_has_no_wagons THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20005, 'Freight ' || p_freight_id || 
                                 ' has no wagons associated. Cannot associate freight without wagons.');
    
    WHEN freight_already_associated THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20006, 'Freight ' || p_freight_id || 
                                 ' is already associated with a different train. ' ||
                                 'Current train: ' || v_current_train_id || 
                                 ' (operator: ' || v_current_operator_id || ').');
    
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20999, 'Unexpected error while associating freight with train: ' || SQLERRM);
END prc_usbd34_associate_freight_with_train;
/

