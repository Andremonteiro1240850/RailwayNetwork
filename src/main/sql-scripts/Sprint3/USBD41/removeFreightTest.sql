
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- INICIANDO SETUP DE DADOS ---');

    -- Limpeza preventiva de dados de teste (IDs 9000-9999)
    DELETE FROM Freight WHERE freight_id BETWEEN 9000 AND 9999;
    DELETE FROM FreighTrain WHERE Freigth_id BETWEEN 9000 AND 9999;
    DELETE FROM Train_route WHERE Train_id BETWEEN 9000 AND 9999;
    DELETE FROM Train WHERE train_id BETWEEN 9000 AND 9999;
    DELETE FROM Locomotive WHERE locomotive_id BETWEEN 8000 AND 8999;

    -- Inserção segura de dados mestre (Ignora se já existirem)
    BEGIN
        INSERT INTO Operator (operator_id, name) VALUES ('OP_TEST', 'Operador Teste');
    EXCEPTION WHEN OTHERS THEN NULL; -- Ignora erro de duplicado
    END;

    BEGIN
        INSERT INTO Facility_type (type_id, name) VALUES (99, 'Tipo Teste');
    EXCEPTION WHEN OTHERS THEN NULL;
    END;

    BEGIN
        INSERT INTO LocomotiveModel (locomotive_model_id, power, acelaration, make, lenght, weight, heigth, max_speed, bogies_number)
        VALUES ('MOD_TEST', 5000, 10, 'Siemens', 20, 80, 4, 160, 4);
    EXCEPTION WHEN OTHERS THEN NULL;
    END;

    BEGIN
        INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (9001, 'Estação A', 99);
        INSERT INTO Facility (facility_id, name, Facility_typetype_id) VALUES (9002, 'Estação B', 99);
    EXCEPTION WHEN OTHERS THEN NULL;
    END;

    COMMIT;
    DBMS_OUTPUT.PUT_LINE('--- SETUP CONCLUÍDO ---');
END;
/


-- Cenário 1: Remoção Válida
DECLARE
    v_res NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('Cenário 1: Remoção Válida (Comboio no futuro)');
    INSERT INTO Train (train_id, operator_id, max_length) VALUES (9500, 'OP_TEST', 500);
    INSERT INTO Locomotive (locomotive_id, operator_id, entry_year, locomotive_model_id, Traintrain_id, Trainoperator_id)
    VALUES (8500, 'OP_TEST', 2020, 'MOD_TEST', 9500, 'OP_TEST');
    INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time, routeDate)
    VALUES (9001, 9500, 1, 8500, 'OP_TEST', 1200, 1500, TRUNC(SYSDATE) + 1);
    INSERT INTO FreighTrain (Freigth_id, Traintrain_id, TrainLocomotivelocomotive_id, Trainoperator_id) VALUES (9001, 9500, 8500, 'OP_TEST');
    INSERT INTO Freight (freight_id, Originfacility_id, Destinationfacility_id, FreighTrainFreigth_id, createDate, FreighTrainTraintrain_id)
    VALUES (9001, 9001, 9002, 9001, SYSDATE, 9500);

    v_res := remove_freight_from_train(9001);
    DBMS_OUTPUT.PUT_LINE('RESULTADO: Sucesso, ID ' || v_res || ' removido.');
    ROLLBACK;
EXCEPTION WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE('ERRO INESPERADO: ' || SQLERRM);
    ROLLBACK;
END;
/

-- Cenário 2: Carga inexistente
DECLARE
    v_res NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('Cenário 2: Carga inexistente (-20001)');
    v_res := remove_freight_from_train(9999);
    Rollback;
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20001 THEN DBMS_OUTPUT.PUT_LINE('OK: Erro -20001 capturado.');
        ELSE DBMS_OUTPUT.PUT_LINE('FALHA: Erro incorreto -> ' || SQLERRM); END IF;
        rollback;
END;
/

-- Cenário 3: Carga sem associação
DECLARE
    v_res NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('Cenário 3: Carga sem associação a comboio (-20002)');
    INSERT INTO Train (train_id, operator_id, max_length) VALUES (9503, 'OP_TEST', 500);
    INSERT INTO Freight (freight_id, Originfacility_id, Destinationfacility_id, FreighTrainFreigth_id, createDate, FreighTrainTraintrain_id)
    VALUES (9003, 9001, 9002, 9003, SYSDATE, 9503);

    v_res := remove_freight_from_train(9003);
    rollback;
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20002 THEN DBMS_OUTPUT.PUT_LINE('OK: Erro -20002 capturado.');
        ELSE DBMS_OUTPUT.PUT_LINE('FALHA: Erro incorreto -> ' || SQLERRM); END IF;
        ROLLBACK;
END;
/

-- Cenário 4: Data passada
DECLARE
    v_res NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('Cenário 4: Data passada (-20003)');
    INSERT INTO Train (train_id, operator_id, max_length) VALUES (9504, 'OP_TEST', 500);
    INSERT INTO Locomotive (locomotive_id, operator_id, entry_year, locomotive_model_id, Traintrain_id, Trainoperator_id)
    VALUES (8504, 'OP_TEST', 2020, 'MOD_TEST', 9504, 'OP_TEST');
    INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time, routeDate)
    VALUES (9001, 9504, 1, 8504, 'OP_TEST', 1200, 1500, TRUNC(SYSDATE) - 1);
    INSERT INTO FreighTrain (Freigth_id, Traintrain_id, TrainLocomotivelocomotive_id, Trainoperator_id) VALUES (9004, 9504, 8504, 'OP_TEST');
    INSERT INTO Freight (freight_id, Originfacility_id, Destinationfacility_id, FreighTrainFreigth_id, createDate, FreighTrainTraintrain_id)
    VALUES (9004, 9001, 9002, 9004, SYSDATE, 9504);

    v_res := remove_freight_from_train(9004);
    rollback;
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20003 THEN DBMS_OUTPUT.PUT_LINE('OK: Erro -20003 capturado.');
        ELSE DBMS_OUTPUT.PUT_LINE('FALHA: Erro incorreto -> ' || SQLERRM); END IF;
        ROLLBACK;
END;
/

-- Cenário 5: Hora passada
DECLARE
    v_res NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('Cenário 5: Hora de partida já passou hoje (-20003)');
    INSERT INTO Train (train_id, operator_id, max_length) VALUES (9505, 'OP_TEST', 500);
    INSERT INTO Locomotive (locomotive_id, operator_id, entry_year, locomotive_model_id, Traintrain_id, Trainoperator_id)
    VALUES (8505, 'OP_TEST', 2020, 'MOD_TEST', 9505, 'OP_TEST');
    INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time, routeDate)
    VALUES (9001, 9505, 1, 8505, 'OP_TEST', 0001, 0500, TRUNC(SYSDATE));
    INSERT INTO FreighTrain (Freigth_id, Traintrain_id, TrainLocomotivelocomotive_id, Trainoperator_id) VALUES (9005, 9505, 8505, 'OP_TEST');
    INSERT INTO Freight (freight_id, Originfacility_id, Destinationfacility_id, FreighTrainFreigth_id, createDate, FreighTrainTraintrain_id)
    VALUES (9005, 9001, 9002, 9005, SYSDATE, 9505);

    v_res := remove_freight_from_train(9005);
    rollback;
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20003 THEN DBMS_OUTPUT.PUT_LINE('OK: Bloqueio por hora correto.');
        ELSE DBMS_OUTPUT.PUT_LINE('FALHA: ' || SQLERRM); END IF;
        ROLLBACK;
END;
/

-- Cenário 6: Sem rota
DECLARE
    v_res NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('Cenário 6: Comboio sem rota (-20004)');
    INSERT INTO Train (train_id, operator_id, max_length) VALUES (9506, 'OP_TEST', 500);
    INSERT INTO Locomotive (locomotive_id, operator_id, entry_year, locomotive_model_id, Traintrain_id, Trainoperator_id)
    VALUES (8506, 'OP_TEST', 2020, 'MOD_TEST', 9506, 'OP_TEST');
    INSERT INTO FreighTrain (Freigth_id, Traintrain_id, TrainLocomotivelocomotive_id, Trainoperator_id) VALUES (9006, 9506, 8506, 'OP_TEST');
    INSERT INTO Freight (freight_id, Originfacility_id, Destinationfacility_id, FreighTrainFreigth_id, createDate, FreighTrainTraintrain_id)
    VALUES (9006, 9001, 9002, 9006, SYSDATE, 9506);

    v_res := remove_freight_from_train(9006);
    rollback;
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20004 THEN DBMS_OUTPUT.PUT_LINE('OK: Identificada ausência de rota.');
        ELSE DBMS_OUTPUT.PUT_LINE('FALHA: ' || SQLERRM); END IF;
        ROLLBACK;
END;
/

-- Cenário 7: Ordem incorreta
DECLARE
    v_res NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('Cenário 7: Rota sem ponto de partida inicial (-20004)');
    INSERT INTO Train (train_id, operator_id, max_length) VALUES (9507, 'OP_TEST', 500);
    INSERT INTO Locomotive (locomotive_id, operator_id, entry_year, locomotive_model_id, Traintrain_id, Trainoperator_id)
    VALUES (8507, 'OP_TEST', 2020, 'MOD_TEST', 9507, 'OP_TEST');
    INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time, routeDate)
    VALUES (9001, 9507, 2, 8507, 'OP_TEST', 1200, 1500, TRUNC(SYSDATE) + 1);
    INSERT INTO FreighTrain (Freigth_id, Traintrain_id, TrainLocomotivelocomotive_id, Trainoperator_id) VALUES (9007, 9507, 8507, 'OP_TEST');
    INSERT INTO Freight (freight_id, Originfacility_id, Destinationfacility_id, FreighTrainFreigth_id, createDate, FreighTrainTraintrain_id)
    VALUES (9007, 9001, 9002, 9007, SYSDATE, 9507);

    v_res := remove_freight_from_train(9007);
    rollback ;
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20004 THEN DBMS_OUTPUT.PUT_LINE('OK: Filtro Order=1 está a funcionar.');
        ELSE DBMS_OUTPUT.PUT_LINE('FALHA: ' || SQLERRM); END IF;
        ROLLBACK;
END;
/

-- Cenário 8: Verificação física
DECLARE
    v_res NUMBER;
    v_count NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('Cenário 8: Verificação física do DELETE');
    INSERT INTO Train (train_id, operator_id, max_length) VALUES (9508, 'OP_TEST', 500);
    INSERT INTO Locomotive (locomotive_id, operator_id, entry_year, locomotive_model_id, Traintrain_id, Trainoperator_id)
    VALUES (8508, 'OP_TEST', 2020, 'MOD_TEST', 9508, 'OP_TEST');
    INSERT INTO Train_route (Facility_id, Train_id, "Order", TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, Arrival_Time, routeDate)
    VALUES (9001, 9508, 1, 8508, 'OP_TEST', 1200, 1500, TRUNC(SYSDATE) + 1);
    INSERT INTO FreighTrain (Freigth_id, Traintrain_id, TrainLocomotivelocomotive_id, Trainoperator_id) VALUES (9008, 9508, 8508, 'OP_TEST');
    INSERT INTO Freight (freight_id, Originfacility_id, Destinationfacility_id, FreighTrainFreigth_id, createDate, FreighTrainTraintrain_id)
    VALUES (9008, 9001, 9002, 9008, SYSDATE, 9508);

    v_res := remove_freight_from_train(9008);
    SELECT COUNT(*) INTO v_count FROM FreighTrain WHERE Freigth_id = 9008;

    IF v_count = 0 THEN
        DBMS_OUTPUT.PUT_LINE('SUCESSO: Registo eliminado de FreighTrain.');
    ELSE
        DBMS_OUTPUT.PUT_LINE('FALHA: Registo ainda consta na tabela.');
    END IF;
    ROLLBACK;
END;
/