-- USBD36 - As a Planner, I want to add a building to an existing facility.
--
-- This procedure uses a record type to receive all necessary data,
-- avoiding individual parameter passing and abbreviations.
-- All validations are wrapped in anonymous blocks.
--
-- Exceptions:
--   -20036: Facility not found
--   -20037: Building type not found
--   -20038: Invalid number of buildings
--   -20039: Duplicate building

CREATE OR REPLACE TYPE building_facility_association_record AS OBJECT (
    building_identifier             NUMBER,
    building_name_value             VARCHAR2(255),
    number_of_buildings_value       NUMBER,
    building_type_identifier        NUMBER,
    facility_identifier             NUMBER
);
/

CREATE OR REPLACE FUNCTION usbd36_add_building(
    building_data IN building_facility_association_record
) RETURN Building.ID%TYPE
IS
    -- Variables for validations (not abbreviated)
    facility_existence_check        NUMBER;
    building_type_existence_check  NUMBER;
    
    -- Custom exceptions
    facility_not_found_exception            EXCEPTION;
    building_type_not_found_exception       EXCEPTION;
    invalid_number_of_buildings_exception   EXCEPTION;
    
BEGIN
    -- ===========================================
    -- VALIDATIONS (all in anonymous blocks)
    -- ===========================================
    
    -- 1. Validate facility exists
    BEGIN
        SELECT COUNT(*)
        INTO facility_existence_check
        FROM Facility
        WHERE facility_id = building_data.facility_identifier;
        
        IF facility_existence_check = 0 THEN
            RAISE facility_not_found_exception;
        END IF;
    END;
    
    -- 2. Validate building type exists
    BEGIN
        SELECT COUNT(*)
        INTO building_type_existence_check
        FROM BuildingType
        WHERE id = building_data.building_type_identifier;
        
        IF building_type_existence_check = 0 THEN
            RAISE building_type_not_found_exception;
        END IF;
    END;
    
    -- 3. Validate number of buildings
    BEGIN
        IF building_data.number_of_buildings_value IS NULL OR building_data.number_of_buildings_value <= 0 THEN
            RAISE invalid_number_of_buildings_exception;
        END IF;
    END;
    
    -- ===========================================
    -- TRANSACTION: Insert Building
    -- ===========================================
    
    -- Insert Building
    BEGIN
        INSERT INTO Building (ID, name, BUILDINGNUMBER, BuildingTypeID, Facilityfacility_id)
        VALUES (
            building_data.building_identifier,
            building_data.building_name_value,
            building_data.number_of_buildings_value,
            building_data.building_type_identifier,
            building_data.facility_identifier
        );
    END;
    
    RETURN building_data.building_identifier;
    
EXCEPTION
    WHEN facility_not_found_exception THEN
        RAISE_APPLICATION_ERROR(-20036, 'USBD36: FACILITY_NOT_FOUND: Facility with ID ' || building_data.facility_identifier || ' does not exist.');
    
    WHEN building_type_not_found_exception THEN
        RAISE_APPLICATION_ERROR(-20037, 'USBD36: BUILDINGTYPE_NOT_FOUND: Building type with ID ' || building_data.building_type_identifier || ' does not exist.');
    
    WHEN invalid_number_of_buildings_exception THEN
        RAISE_APPLICATION_ERROR(-20038, 'USBD36: INVALID_NUMBEROFBUILDINGS: Number of buildings must be greater than 0.');
    
    WHEN DUP_VAL_ON_INDEX THEN
        RAISE_APPLICATION_ERROR(-20039, 'USBD36: DUPLICATE_BUILDING: A building with this identifier or name already exists.');
    
    WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20040, 'USBD36: ERROR: ' || SQLERRM);
END usbd36_add_building;
/
