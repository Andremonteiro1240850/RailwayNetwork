

BEGIN
    DBMS_OUTPUT.PUT_LINE('--- US36: LIMPEZA PARA REPETIR SUCESSO ---');

    DELETE FROM Building
    WHERE UPPER(name) = UPPER('Test Warehouse US36');

    COMMIT;

    DBMS_OUTPUT.PUT_LINE('LIMPEZA OK: Building removido e COMMIT feito.');
END;
/






DECLARE
    v_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTES US36: addBuildingToFacility ---');

    ------------------------------------------------------------------
    -- 1) CASO DE SUCESSO (COMMIT)
    ------------------------------------------------------------------
    BEGIN
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

    ------------------------------------------------------------------
    -- 2) FACILITY INEXISTENTE (deve dar -20001)
    ------------------------------------------------------------------
    BEGIN
        v_id := addBuildingToFacility(
                facilityIdentifier     => 9999,
                buildingName           => 'Invalid Facility Test',
                buildingNumber         => 1,
                buildingTypeIdentifier => 1
                );

        DBMS_OUTPUT.PUT_LINE('ERRO: deveria falhar (facility inexistente)');
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE(' (facility inexistente): ' || SQLERRM);
            ROLLBACK;
    END;

    ------------------------------------------------------------------
    -- 3) BUILDING TYPE INEXISTENTE (deve dar -20002)
    ------------------------------------------------------------------
    BEGIN
        v_id := addBuildingToFacility(
                facilityIdentifier     => 50,
                buildingName           => 'Invalid Type Test',
                buildingNumber         => 1,
                buildingTypeIdentifier => 999
                );

        DBMS_OUTPUT.PUT_LINE('ERRO: deveria falhar (building type inexistente)');
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE(' (building type inexistente): ' || SQLERRM);
            ROLLBACK;
    END;

    ------------------------------------------------------------------
    -- 4) NOME DUPLICADO (deve dar -20003)
    ------------------------------------------------------------------
    BEGIN
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
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE(' (nome duplicado): ' || SQLERRM);
            ROLLBACK;
    END;

END;
/
