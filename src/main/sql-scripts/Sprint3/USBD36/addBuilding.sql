CREATE OR REPLACE FUNCTION usbd36_add_building(
    p_building_id         IN Building.ID%TYPE,
    p_name                IN Building.name%TYPE,
    p_number_of_buildings IN Building.BUILDINGNUMBER%TYPE,
    p_building_type_id    IN Building.BuildingTypeID%TYPE,
    p_facility_id         IN Building.Facilityfacility_id%TYPE
) RETURN Building.ID%TYPE
IS
    v_cnt NUMBER;
BEGIN
    -- Facility existe?
SELECT COUNT(*) INTO v_cnt
FROM Facility
WHERE facility_id = p_facility_id;

IF v_cnt = 0 THEN
        raise_application_error(-20036, 'USBD36: FACILITY_NOT_FOUND');
END IF;

    -- BuildingType existe?
SELECT COUNT(*) INTO v_cnt
FROM BuildingType
WHERE id = p_building_type_id;

IF v_cnt = 0 THEN
        raise_application_error(-20036, 'USBD36: BUILDINGTYPE_NOT_FOUND');
END IF;

    -- numberOfBuildings válido?
    IF p_number_of_buildings IS NULL OR p_number_of_buildings <= 0 THEN
        raise_application_error(-20036, 'USBD36: INVALID_NUMBEROFBUILDINGS');
END IF;

    -- Insert
INSERT INTO Building (ID, name, BUILDINGNUMBER, BuildingTypeID, Facilityfacility_id)
VALUES (p_building_id, p_name, p_number_of_buildings, p_building_type_id, p_facility_id);

RETURN p_building_id;

EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        raise_application_error(-20036, 'USBD36: DUPLICATE_BUILDING');
        return null;
WHEN OTHERS THEN
        raise_application_error(-20036, 'USBD36: ERROR ' || SQLERRM);
        return null;
END;
/
