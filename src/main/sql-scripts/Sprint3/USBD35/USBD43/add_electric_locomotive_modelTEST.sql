BEGIN
    DBMS_OUTPUT.PUT_LINE('--- apagar dados dos testes ---');

    DELETE FROM Electric_Locomotive WHERE LocomotiveModellocomotive_model_id LIKE 'TEST%';
    DELETE FROM LocomotiveModel WHERE locomotive_model_id LIKE 'TEST%';
    COMMIT;
    DBMS_OUTPUT.PUT_LINE('concluído com sucesso.');
END;
/


DECLARE
    v_id VARCHAR2(255);
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 1: Inserção Válida ---');
    v_id := add_electric_locomotive_model(
        'TEST_E4001',
        4000,
        350,
        'Siemens',
        20000,
        90,
        4400,
        200,
        2,
        25000
    );
    DBMS_OUTPUT.PUT_LINE('Sucesso: Modelo de locomotiva elétrica ' || v_id || ' adicionado.');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Erro Inesperado: ' || SQLERRM);
END;
/


DECLARE
    v_id VARCHAR2(255);
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 2: ID Vazio ---');
    v_id := add_electric_locomotive_model(
        '   ',
        4000,
        350,
        'Siemens',
        20000,
        90,
        4400,
        200,
        2,
        25000
    );
    DBMS_OUTPUT.PUT_LINE('Falha: A função permitiu um ID vazio!');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20001 THEN
            DBMS_OUTPUT.PUT_LINE('OK: Erro -20001 capturado (ID obrigatório).');
        ELSE
            DBMS_OUTPUT.PUT_LINE('Erro Incorreto: ' || SQLERRM);
        END IF;
END;
/


DECLARE
    v_id VARCHAR2(255);
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 3: Make Vazio ---');
    v_id := add_electric_locomotive_model(
        'TEST_E4002',
        4000,
        350,
        '   ',
        20000,
        90,
        4400,
        200,
        2,
        25000
    );
    DBMS_OUTPUT.PUT_LINE('Falha: A função permitiu um make vazio!');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20002 THEN
            DBMS_OUTPUT.PUT_LINE('OK: Erro -20002 capturado (Make obrigatório).');
        ELSE
            DBMS_OUTPUT.PUT_LINE('Erro Incorreto: ' || SQLERRM);
        END IF;
END;
/


DECLARE
    v_id VARCHAR2(255);
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 4: Potência Inválida (zero) ---');
    v_id := add_electric_locomotive_model(
        'TEST_E4003',
        0,
        350,
        'Siemens',
        20000,
        90,
        4400,
        200,
        2,
        25000
    );
    DBMS_OUTPUT.PUT_LINE('Falha: A função permitiu potência zero!');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20003 THEN
            DBMS_OUTPUT.PUT_LINE('OK: Erro -20003 capturado (Potência inválida).');
        ELSE
            DBMS_OUTPUT.PUT_LINE('Erro Incorreto: ' || SQLERRM);
        END IF;
END;
/


DECLARE
    v_id VARCHAR2(255);
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 5: Tensão Inválida (negativa) ---');
    v_id := add_electric_locomotive_model(
        'TEST_E4004',
        4000,
        350,
        'Siemens',
        20000,
        90,
        4400,
        200,
        2,
        -1000
    );
    DBMS_OUTPUT.PUT_LINE('Falha: A função permitiu tensão negativa!');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20010 THEN
            DBMS_OUTPUT.PUT_LINE('OK: Erro -20010 capturado (Tensão inválida).');
        ELSE
            DBMS_OUTPUT.PUT_LINE('Erro Incorreto: ' || SQLERRM);
        END IF;
END;
/


DECLARE
    v_id VARCHAR2(255);
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 6: ID Duplicado ---');
    -- Primeiro inserir um modelo válido
    v_id := add_electric_locomotive_model(
        'TEST_E4005',
        4000,
        350,
        'Siemens',
        20000,
        90,
        4400,
        200,
        2,
        25000
    );
    DBMS_OUTPUT.PUT_LINE('Modelo ' || v_id || ' inserido com sucesso.');
    
    -- Tentar inserir novamente com o mesmo ID
    v_id := add_electric_locomotive_model(
        'TEST_E4005',
        5000,
        400,
        'Alstom',
        21000,
        95,
        4500,
        220,
        3,
        30000
    );
    DBMS_OUTPUT.PUT_LINE('Falha: A função permitiu um ID duplicado!');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20011 THEN
            DBMS_OUTPUT.PUT_LINE('OK: Erro -20011 capturado (ID duplicado).');
        ELSE
            DBMS_OUTPUT.PUT_LINE('Erro Incorreto: ' || SQLERRM);
        END IF;
END;
/


BEGIN
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('---Rollback efetuado para manter integridade ---');
END;
/

