SET SERVEROUTPUT ON;

-- Limpar associações de teste anteriores (se existirem)
DELETE FROM Locomotive_Train
WHERE Locomotivelocomotive_id = 5630
  AND Traintrain_id = 5421;
COMMIT;

-- ========================================
-- TESTE 1: Associação bem-sucedida
-- ========================================
DECLARE
v_result NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('TESTE 1: Associação bem-sucedida');
    DBMS_OUTPUT.PUT_LINE('========================================');

    v_result := associate_locomotive_with_train(5630, 5421, 'Medway');

    IF v_result = 1 THEN
        DBMS_OUTPUT.PUT_LINE('✓ SUCESSO: Locomotiva 5630 associada ao comboio 5421');
END IF;

    DBMS_OUTPUT.PUT_LINE(' ');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✗ ERRO: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- ========================================
-- TESTE 2: Locomotiva inexistente
-- ========================================
DECLARE
v_result NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('TESTE 2: Locomotiva inexistente');
    DBMS_OUTPUT.PUT_LINE('========================================');

    v_result := associate_locomotive_with_train(99999, 5421, 'Medway');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Erro esperado capturado: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- ========================================
-- TESTE 3: Comboio inexistente
-- ========================================
DECLARE
v_result NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('TESTE 3: Comboio inexistente');
    DBMS_OUTPUT.PUT_LINE('========================================');

    v_result := associate_locomotive_with_train(5621, 99999, 'Medway');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Erro esperado capturado: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- ========================================
-- TESTE 4: Associação duplicada
-- ========================================
DECLARE
v_result NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('TESTE 4: Tentativa de associação duplicada');
    DBMS_OUTPUT.PUT_LINE('========================================');

    -- Tentar associar a mesma locomotiva que já foi associada no teste 1
    v_result := associate_locomotive_with_train(5630, 5421, 'Medway');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Erro esperado capturado: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- ========================================
-- TESTE 5: Operador incompatível
-- ========================================
DECLARE
v_result NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('TESTE 5: Operador incompatível');
    DBMS_OUTPUT.PUT_LINE('========================================');

    -- Locomotiva 335001 pertence a Captrain, mas tentamos associar a um comboio Medway
    v_result := associate_locomotive_with_train(335001, 5421, 'Medway');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Erro esperado capturado: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- ========================================
-- TESTE 6: Operador inexistente
-- ========================================
DECLARE
v_result NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('========================================');
    DBMS_OUTPUT.PUT_LINE('TESTE 6: Operador inexistente');
    DBMS_OUTPUT.PUT_LINE('========================================');

    v_result := associate_locomotive_with_train(5621, 5421, 'OperadorInexistente');

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Erro esperado capturado: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- ========================================
-- VERIFICAÇÃO FINAL: Consultar associações
-- ========================================
DBMS_OUTPUT.PUT_LINE('========================================');
DBMS_OUTPUT.PUT_LINE('VERIFICAÇÃO: Associações existentes');
DBMS_OUTPUT.PUT_LINE('========================================');

SELECT
    lt.Locomotivelocomotive_id AS "ID Locomotiva",
    lm.locomotive_model_id AS "Modelo",
    lt.Traintrain_id AS "ID Comboio",
    lt.Trainoperator_id AS "Operador",
    TO_CHAR(lt.Adddate, 'DD-MM-YYYY HH24:MI:SS') AS "Data Associação"
FROM Locomotive_Train lt
         INNER JOIN Locomotive l ON lt.Locomotivelocomotive_id = l.locomotive_id
         INNER JOIN LocomotiveModel lm ON l.locomotive_model_id = lm.locomotive_model_id
WHERE lt.Traintrain_id = 5421
ORDER BY lt.Adddate DESC;