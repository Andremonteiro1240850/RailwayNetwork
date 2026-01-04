
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- US36: LIMPEZA PARA REPETIR SUCESSO ---');

    DELETE FROM Building
    WHERE UPPER(name) = UPPER('Test Warehouse US36');

    COMMIT;

    DBMS_OUTPUT.PUT_LINE('LIMPEZA OK: Building removido e COMMIT feito.');
END;
/

-- ============================================================
-- TESTE 1: CASO DE SUCESSO (COMMIT)
-- ============================================================
DECLARE
    v_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 1: Caso de sucesso ---');
    
    v_id := addBuildingToFacility(
            facilityIdentifier     => 50,
            buildingName           => 'Test Warehouse US36',
            buildingNumber         => 99,
            buildingTypeIdentifier => 1
            );

    DBMS_OUTPUT.PUT_LINE('SUCESSO: Building criado com ID = ' || v_id);

    COMMIT;
    DBMS_OUTPUT.PUT_LINE('COMMIT executado (sucesso ficou gravado).');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('FALHA NO SUCESSO: ' || SQLERRM);
        ROLLBACK;
END;
/

-- ============================================================
-- TESTE 2: FACILITY INEXISTENTE
-- ============================================================
DECLARE
    v_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 2: Facility inexistente ---');
    
    v_id := addBuildingToFacility(
            facilityIdentifier     => 9999,
            buildingName           => 'Invalid Facility Test',
            buildingNumber         => 1,
            buildingTypeIdentifier => 1
            );

    DBMS_OUTPUT.PUT_LINE('ERRO: deveria falhar (facility inexistente)');
    ROLLBACK;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('OK (erro esperado - facility inexistente): ' || SQLERRM);
        ROLLBACK;
END;
/

-- ============================================================
-- TESTE 3: BUILDING TYPE INEXISTENTE
-- ============================================================
DECLARE
    v_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 3: Building type inexistente ---');
    
    v_id := addBuildingToFacility(
            facilityIdentifier     => 50,
            buildingName           => 'Invalid Type Test',
            buildingNumber         => 1,
            buildingTypeIdentifier => 999
            );

    DBMS_OUTPUT.PUT_LINE('ERRO: deveria falhar (building type inexistente)');
    ROLLBACK;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('OK (erro esperado - building type inexistente): ' || SQLERRM);
        ROLLBACK;
END;
/

-- ============================================================
-- TESTE 4: NOME DUPLICADO (deve dar -20003)
-- ============================================================
DECLARE
    v_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 4: Nome duplicado ---');
    
    v_id := addBuildingToFacility(
            facilityIdentifier     => 50,
            buildingName           => 'Duplicate Name Test',
            buildingNumber         => 1,
            buildingTypeIdentifier => 1
            );

    v_id := addBuildingToFacility(
            facilityIdentifier     => 50,
            buildingName           => 'Duplicate Name Test',
            buildingNumber         => 2,
            buildingTypeIdentifier => 1
            );

    DBMS_OUTPUT.PUT_LINE('ERRO: deveria falhar (nome duplicado)');
    ROLLBACK;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('OK (erro esperado - nome duplicado): ' || SQLERRM);
        ROLLBACK;
END;
/
