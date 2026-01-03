CREATE OR REPLACE FUNCTION addBuildingToFacility (
    facilityIdentifier        IN Facility.facility_id%TYPE,
    buildingName              IN Building.name%TYPE,
    buildingNumber            IN Building.BuildingNumber%TYPE,
    buildingTypeIdentifier    IN BuildingType.ID%TYPE
) RETURN NUMBER
AS
    createdBuildingIdentifier Building.ID%TYPE;
    v_count                   NUMBER;
    v_clean_name              Building.name%TYPE := TRIM(buildingName);
BEGIN
    -- 0) Validação básica de input
    IF v_clean_name IS NULL THEN
        raise_application_error(-20004, 'Building name is mandatory.');
    END IF;

    -- 1) Validar que a Facility existe
    SELECT COUNT(*)
    INTO v_count
    FROM Facility
    WHERE facility_id = facilityIdentifier;

    IF v_count = 0 THEN
        raise_application_error(-20001, 'Facility does not exist.');
    END IF;

    -- 2) Validar que o BuildingType existe
    SELECT COUNT(*)
    INTO v_count
    FROM BuildingType
    WHERE ID = buildingTypeIdentifier;

    IF v_count = 0 THEN
        raise_application_error(-20002, 'BuildingType does not exist.');
    END IF;

    -- 3) Secção crítica: evitar race conditions no ID + duplicados
    LOCK TABLE Building IN EXCLUSIVE MODE;

    -- 4) Validar nome (mantive a tua regra: nome único global)
    SELECT COUNT(*)
    INTO v_count
    FROM Building
    WHERE UPPER(name) = UPPER(v_clean_name);

    IF v_count > 0 THEN
        raise_application_error(-20003, 'Building name already exists.');
    END IF;

    -- 5) Gerar novo ID (sem sequence)
    SELECT NVL(MAX(ID), 0) + 1
    INTO createdBuildingIdentifier
    FROM Building;

    -- 6) Inserir
    INSERT INTO Building (ID, name, BuildingNumber, BuildingTypeID, Facilityfacility_id)
    VALUES (createdBuildingIdentifier, v_clean_name, buildingNumber, buildingTypeIdentifier, facilityIdentifier);

    -- 7) NÃO há COMMIT aqui (o teste/chamador decide)
    RETURN createdBuildingIdentifier;
END;
/
