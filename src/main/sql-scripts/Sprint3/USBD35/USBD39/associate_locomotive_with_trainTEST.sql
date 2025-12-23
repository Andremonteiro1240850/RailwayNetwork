-- Teste 1: Associação bem-sucedida
DECLARE
v_result NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('=== TESTE 1: Associação bem-sucedida ===');
    v_result := associate_locomotive_with_train(5630, 5421, 'Medway');

    IF v_result = 1 THEN
        DBMS_OUTPUT.PUT_LINE('Sucesso: Locomotiva 5630 associada ao comboio 5421');
ELSE
        DBMS_OUTPUT.PUT_LINE('Falha na associação');
END IF;
END;
/

-- Teste 2: Locomotiva inexistente
DECLARE
v_result NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('=== TESTE 2: Locomotiva inexistente ===');
    v_result := associate_locomotive_with_train(9999, 5421, 'Medway');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Erro esperado: ' || SQLERRM);
END;
/

-- Teste 3: Comboio inexistente
DECLARE
v_result NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('=== TESTE 3: Comboio inexistente ===');
    v_result := associate_locomotive_with_train(5621, 9999, 'Medway');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Erro esperado: ' || SQLERRM);
END;
/

-- Teste 4: Associação duplicada
DECLARE
v_result NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('=== TESTE 4: Associação duplicada ===');
    v_result := associate_locomotive_with_train(5621, 5421, 'Medway');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Erro esperado: ' || SQLERRM);
END;
/

-- Teste 5: Operador incompatível
DECLARE
v_result NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('=== TESTE 5: Operador incompatível ===');
    v_result := associate_locomotive_with_train(335001, 5421, 'Medway');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Erro esperado: ' || SQLERRM);
END;
/

-- Consulta para verificar associações
SELECT
    lt.Locomotivelocomotive_id AS "ID Locomotiva",
    lt.Traintrain_id AS "ID Comboio",
    lt.Trainoperator_id AS "Operador",
    TO_CHAR(lt.Adddate, 'DD-MM-YYYY HH24:MI:SS') AS "Data Associação"
FROM Locomotive_Train lt
ORDER BY lt.Adddate DESC;