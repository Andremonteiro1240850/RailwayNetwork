-- DELETE FROM Building WHERE UPPER(name) = UPPER('Test Warehouse US36');
-- COMMIT;


DECLARE
    v_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE US36: addBuildingToFacility ---');

    -- 1) CASO DE SUCESSO
    BEGIN
        v_id := addBuildingToFacility(
                facilityIdentifier     => 50,
                buildingName           => 'Test Warehouse US36',
                buildingNumber         => 99,
                buildingTypeIdentifier => 1
                );

        DBMS_OUTPUT.PUT_LINE('SUCESSO: Building criado com ID = ' || v_id);
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('ERRO INESPERADO NO CASO DE SUCESSO: ' || SQLERRM);
    END;




    ------------------------------------------------------------------
    -- 2) FACILITY INEXISTENTE
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
            DBMS_OUTPUT.PUT_LINE('(facility inexistente): ' || SQLERRM);
    END;


    ------------------------------------------------------------------
    -- 3) BUILDING TYPE INEXISTENTE
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
            DBMS_OUTPUT.PUT_LINE('(building type inexistente): ' || SQLERRM);
    END;

    ------------------------------------------------------------------
    -- 4) NOME DUPLICADO
    ------------------------------------------------------------------
    BEGIN
        -- Primeiro insert (válido)
        v_id := addBuildingToFacility(
                facilityIdentifier     => 50,
                buildingName           => 'Duplicate Name Test',
                buildingNumber         => 1,
                buildingTypeIdentifier => 1
                );

        -- Segundo insert com o mesmo nome (deve falhar)
        v_id := addBuildingToFacility(
                facilityIdentifier     => 50,
                buildingName           => 'Duplicate Name Test',
                buildingNumber         => 2,
                buildingTypeIdentifier => 1
                );

        DBMS_OUTPUT.PUT_LINE('ERRO: deveria falhar (nome duplicado)');
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('(nome duplicado): ' || SQLERRM);
    END;

    -- LIMPEZA FINAL

    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('ROLLBACK executado. Base de dados limpa.');

END;
/
