-- Limpar associações de teste anteriores (se existirem)
-- Nota: SET SERVEROUTPUT ON não é necessário no IntelliJ SQL Console
DELETE FROM Electric_Locomotive WHERE LocomotiveModellocomotive_model_id LIKE 'TEST%';
DELETE FROM LocomotiveModel WHERE locomotive_model_id LIKE 'TEST%';
COMMIT;

-- ========================================
-- TESTE 1: Inserção bem-sucedida
-- ========================================
DECLARE
    v_result LocomotiveModel.locomotive_model_id%TYPE;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('TESTE 1: Inserção bem-sucedida');
    DBMS_OUTPUT.PUT_LINE('========================================');

    v_result := add_electric_locomotive_model(
        'TEST_E4001',
        'Siemens',
        4000,
        350,
        20000,
        90,
        4400,
        200,
        2,
        25000
    );

    IF v_result IS NOT NULL THEN
        COMMIT; -- Confirmar a transação após sucesso
        DBMS_OUTPUT.PUT_LINE('✓ SUCESSO: Modelo de locomotiva elétrica ' || v_result || ' adicionado.');
    END IF;

    DBMS_OUTPUT.PUT_LINE(' ');
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK; -- Reverter em caso de erro
        DBMS_OUTPUT.PUT_LINE('✗ ERRO: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- ========================================
-- TESTE 2: ID vazio
-- ========================================
DECLARE
    v_result LocomotiveModel.locomotive_model_id%TYPE;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('TESTE 2: ID vazio');
    DBMS_OUTPUT.PUT_LINE('========================================');

    v_result := add_electric_locomotive_model(
        '   ',
        'Siemens',
        4000,
        350,
        20000,
        90,
        4400,
        200,
        2,
        25000
    );

EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20001 THEN
            DBMS_OUTPUT.PUT_LINE('✓ Erro esperado capturado: ID obrigatório.');
        ELSE
            DBMS_OUTPUT.PUT_LINE('✗ Erro inesperado: ' || SQLERRM);
        END IF;
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- ========================================
-- TESTE 3: Make vazio
-- ========================================
DECLARE
    v_result LocomotiveModel.locomotive_model_id%TYPE;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('TESTE 3: Make vazio');
    DBMS_OUTPUT.PUT_LINE('========================================');

    v_result := add_electric_locomotive_model(
        'TEST_E4002',
        '   ',
        4000,
        350,
        20000,
        90,
        4400,
        200,
        2,
        25000
    );

EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20002 THEN
            DBMS_OUTPUT.PUT_LINE('✓ Erro esperado capturado: Fabricante obrigatório.');
        ELSE
            DBMS_OUTPUT.PUT_LINE('✗ Erro inesperado: ' || SQLERRM);
        END IF;
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- ========================================
-- TESTE 4: Potência inválida (zero)
-- ========================================
DECLARE
    v_result LocomotiveModel.locomotive_model_id%TYPE;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('TESTE 4: Potência inválida (zero)');
    DBMS_OUTPUT.PUT_LINE('========================================');

    v_result := add_electric_locomotive_model(
        'TEST_E4003',
        'Siemens',
        0,
        350,
        20000,
        90,
        4400,
        200,
        2,
        25000
    );

EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20003 THEN
            DBMS_OUTPUT.PUT_LINE('✓ Erro esperado capturado: Potência deve ser maior que zero.');
        ELSE
            DBMS_OUTPUT.PUT_LINE('✗ Erro inesperado: ' || SQLERRM);
        END IF;
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- ========================================
-- TESTE 5: Tensão inválida (negativa)
-- ========================================
DECLARE
    v_result LocomotiveModel.locomotive_model_id%TYPE;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('TESTE 5: Tensão inválida (negativa)');
    DBMS_OUTPUT.PUT_LINE('========================================');

    v_result := add_electric_locomotive_model(
        'TEST_E4004',
        'Siemens',
        4000,
        350,
        20000,
        90,
        4400,
        200,
        2,
        -1000
    );

EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20010 THEN
            DBMS_OUTPUT.PUT_LINE('✓ Erro esperado capturado: Tensão deve ser maior que zero.');
        ELSE
            DBMS_OUTPUT.PUT_LINE('✗ Erro inesperado: ' || SQLERRM);
        END IF;
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- ========================================
-- TESTE 6: ID duplicado
-- ========================================
DECLARE
    v_result LocomotiveModel.locomotive_model_id%TYPE;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('TESTE 6: Tentativa de ID duplicado');
    DBMS_OUTPUT.PUT_LINE('========================================');

    -- Primeiro inserir um modelo válido
    v_result := add_electric_locomotive_model(
        'TEST_E4005',
        'Siemens',
        4000,
        350,
        20000,
        90,
        4400,
        200,
        2,
        25000
    );
    
    IF v_result IS NOT NULL THEN
        COMMIT;
        DBMS_OUTPUT.PUT_LINE('Modelo ' || v_result || ' inserido com sucesso.');
    END IF;
    
    -- Tentar inserir novamente com o mesmo ID
    v_result := add_electric_locomotive_model(
        'TEST_E4005',
        'Alstom',
        5000,
        400,
        21000,
        95,
        4500,
        220,
        3,
        30000
    );

EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20011 THEN
            DBMS_OUTPUT.PUT_LINE('✓ Erro esperado capturado: ID do modelo já existente.');
        ELSE
            DBMS_OUTPUT.PUT_LINE('✗ Erro inesperado: ' || SQLERRM);
        END IF;
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- ========================================
-- VERIFICAÇÃO FINAL: Consultar modelos criados
-- ========================================
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('VERIFICAÇÃO: Modelos de teste criados');
    DBMS_OUTPUT.PUT_LINE('========================================');
END;
/

SELECT
    lm.locomotive_model_id AS "ID Modelo",
    lm.make AS "Fabricante",
    lm.power AS "Potência",
    lm.max_speed AS "Velocidade Máxima",
    el.Voltage AS "Tensão"
FROM LocomotiveModel lm
         INNER JOIN Electric_Locomotive el ON lm.locomotive_model_id = el.LocomotiveModellocomotive_model_id
WHERE lm.locomotive_model_id LIKE 'TEST%'
ORDER BY lm.locomotive_model_id;

