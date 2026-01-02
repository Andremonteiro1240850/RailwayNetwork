-- This procedure associates a freight with a train.
-- All wagons included in the freight will be associated with the train.
--
-- The procedure uses a record type to receive all necessary data,
-- avoiding individual parameter passing and abbreviations.
--
-- Exceptions:
--   -20001: Freight not found
--   -20002: Train not found with specified operator
--   -20003: Locomotive not found
--   -20004: Locomotive is not associated with the specified train
--   -20005: Freight has no wagons associated
--   -20006: Freight is already associated with a different train

CREATE OR REPLACE TYPE freight_train_association_record AS OBJECT (
    freight_identifier    NUMBER,
    train_identifier     NUMBER,
    operator_identifier  VARCHAR2(255),
    locomotive_identifier NUMBER
);
/

CREATE OR REPLACE PROCEDURE prc_usbd34_associate_freight_with_train(
    association_data IN freight_train_association_record
) IS
    -- Variables for validations
    freight_exists_check          NUMBER;
    train_exists_check            NUMBER;
    locomotive_exists_check       NUMBER;
    locomotive_train_exists_check NUMBER;
    wagon_count_value             NUMBER;
    current_train_identifier      NUMBER;
    current_operator_identifier   VARCHAR2(255);
    
    -- Custom exceptions
    freight_not_found_exception            EXCEPTION;
    train_not_found_exception              EXCEPTION;
    locomotive_not_found_exception         EXCEPTION;
    locomotive_not_in_train_exception      EXCEPTION;
    freight_has_no_wagons_exception        EXCEPTION;
    freight_already_associated_exception   EXCEPTION;

BEGIN
    -- ===========================================
    -- VALIDATIONS
    -- ===========================================
    
    -- 1. Validate that the freight exists
    BEGIN
        SELECT COUNT(*)
        INTO freight_exists_check
        FROM Freight
        WHERE freight_id = association_data.freight_identifier;
        
        IF freight_exists_check = 0 THEN
            RAISE freight_not_found_exception;
        END IF;
    END;
    
    -- 2. Validate that the train exists with the specified operator
    BEGIN
        SELECT COUNT(*)
        INTO train_exists_check
        FROM Train
        WHERE train_id = association_data.train_identifier
          AND operator_id = association_data.operator_identifier;
        
        IF train_exists_check = 0 THEN
            RAISE train_not_found_exception;
        END IF;
    END;
    
    -- 3. Validate that the locomotive exists
    BEGIN
        SELECT COUNT(*)
        INTO locomotive_exists_check
        FROM Locomotive
        WHERE locomotive_id = association_data.locomotive_identifier;
        
        IF locomotive_exists_check = 0 THEN
            RAISE locomotive_not_found_exception;
        END IF;
    END;
    
    -- 4. Validate that the locomotive is associated with the specified train
    BEGIN
        SELECT COUNT(*)
        INTO locomotive_train_exists_check
        FROM Locomotive_Train
        WHERE Locomotivelocomotive_id = association_data.locomotive_identifier
          AND Traintrain_id = association_data.train_identifier
          AND Trainoperator_id = association_data.operator_identifier;
        
        IF locomotive_train_exists_check = 0 THEN
            RAISE locomotive_not_in_train_exception;
        END IF;
    END;
    
    -- 5. Validate that the freight has at least one wagon
    BEGIN
        SELECT COUNT(*)
        INTO wagon_count_value
        FROM Wagon_In_Freight
        WHERE Freight_id = association_data.freight_identifier;
        
        IF wagon_count_value = 0 THEN
            RAISE freight_has_no_wagons_exception;
        END IF;
    END;
    
    -- 6. Check if freight is already associated with a different train
    --    (Allow update if same train, raise error if different train)
    BEGIN
        SELECT freight_train_association.Traintrain_id, freight_train_association.Trainoperator_id
        INTO current_train_identifier, current_operator_identifier
        FROM Freight freight_record
        JOIN FreighTrain freight_train_association ON freight_train_association.Freightfreight_id = freight_record.FreighTrainFreigth_id 
                           AND freight_train_association.Traintrain_id = freight_record.FreighTrainTraintrain_id
        WHERE freight_record.freight_id = association_data.freight_identifier;
        
        -- If already associated with a different train, raise error
        IF current_train_identifier IS NOT NULL 
           AND (current_train_identifier != association_data.train_identifier OR current_operator_identifier != association_data.operator_identifier) THEN
            RAISE freight_already_associated_exception;
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
    MERGE INTO FreighTrain freight_train_table
    USING (SELECT association_data.freight_identifier AS freight_id,
                  association_data.train_identifier AS train_id,
                  association_data.locomotive_identifier AS locomotive_id,
                  association_data.operator_identifier AS operator_id
           FROM dual) source_data
    ON (freight_train_table.Freightfreight_id = source_data.freight_id AND freight_train_table.Traintrain_id = source_data.train_id)
    WHEN MATCHED THEN
        UPDATE SET
            freight_train_table.TrainLocomotivelocomotive_id = source_data.locomotive_id,
            freight_train_table.Trainoperator_id = source_data.operator_id
    WHEN NOT MATCHED THEN
        INSERT (Freightfreight_id, Traintrain_id, TrainLocomotivelocomotive_id, Trainoperator_id)
        VALUES (source_data.freight_id, source_data.train_id, source_data.locomotive_id, source_data.operator_id);
    
    -- Update Freight table to reference the FreighTrain association
    UPDATE Freight
    SET FreighTrainFreigth_id = association_data.freight_identifier,
        FreighTrainTraintrain_id = association_data.train_identifier
    WHERE freight_id = association_data.freight_identifier;
    
    -- Commit the transaction
    COMMIT;
    
    -- Success message (optional, can be removed if not needed)
    DBMS_OUTPUT.PUT_LINE('Freight ' || association_data.freight_identifier || ' successfully associated with train ' || 
                         association_data.train_identifier || ' (operator: ' || association_data.operator_identifier || '). ' ||
                         wagon_count_value || ' wagon(s) are now associated with the train.');

EXCEPTION
    WHEN freight_not_found_exception THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20001, 'Freight with ID ' || association_data.freight_identifier || ' not found.');
    
    WHEN train_not_found_exception THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20002, 'Train with ID ' || association_data.train_identifier || 
                                 ' and operator ' || association_data.operator_identifier || ' not found.');
    
    WHEN locomotive_not_found_exception THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20003, 'Locomotive with ID ' || association_data.locomotive_identifier || ' not found.');
    
    WHEN locomotive_not_in_train_exception THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20004, 'Locomotive ' || association_data.locomotive_identifier || 
                                 ' is not associated with train ' || association_data.train_identifier || 
                                 ' (operator: ' || association_data.operator_identifier || ').');
    
    WHEN freight_has_no_wagons_exception THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20005, 'Freight ' || association_data.freight_identifier || 
                                 ' has no wagons associated. Cannot associate freight without wagons.');
    
    WHEN freight_already_associated_exception THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20006, 'Freight ' || association_data.freight_identifier || 
                                 ' is already associated with a different train. ' ||
                                 'Current train: ' || current_train_identifier || 
                                 ' (operator: ' || current_operator_identifier || ').');
    
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20999, 'Unexpected error while associating freight with train: ' || SQLERRM);
END prc_usbd34_associate_freight_with_train;
/

