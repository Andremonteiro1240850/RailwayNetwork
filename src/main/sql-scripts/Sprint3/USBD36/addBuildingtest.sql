-- Test cases for USBD36 - Add Building to Facility
-- All test cases use anonymous blocks and the record type

-- Test 1: Success case
DECLARE
    building_record building_facility_association_record;
    result_building_id NUMBER;
BEGIN
    SAVEPOINT sp_test;
    
    DBMS_OUTPUT.PUT_LINE('TEST 1: Success case');
    
    building_record := building_facility_association_record(
        building_identifier => 10000,
        building_name_value => 'USBD36 Test Building OK',
        number_of_buildings_value => 1,
        building_type_identifier => 1,
        facility_identifier => 50
    );
    
    result_building_id := usbd36_add_building(building_record);
    DBMS_OUTPUT.PUT_LINE('SUCCESS: building_id=' || result_building_id || ' facility_id=50');
    
    ROLLBACK TO sp_test;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('ERROR: ' || SQLERRM);
        ROLLBACK TO sp_test;
END;
/

-- Test 2: Facility does not exist
DECLARE
    building_record building_facility_association_record;
    result_building_id NUMBER;
BEGIN
    SAVEPOINT sp_test;
    
    DBMS_OUTPUT.PUT_LINE('TEST 2: Facility does not exist');
    
    building_record := building_facility_association_record(
        building_identifier => 10001,
        building_name_value => 'USBD36 Bad Facility',
        number_of_buildings_value => 1,
        building_type_identifier => 1,
        facility_identifier => 999999
    );
    
    result_building_id := usbd36_add_building(building_record);
    DBMS_OUTPUT.PUT_LINE('UNEXPECTED SUCCESS: building_id=' || result_building_id);
    ROLLBACK TO sp_test;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('EXPECTED ERROR: ' || SQLERRM);
        ROLLBACK TO sp_test;
END;
/

-- Test 3: Building type does not exist
DECLARE
    building_record building_facility_association_record;
    result_building_id NUMBER;
BEGIN
    SAVEPOINT sp_test;
    
    DBMS_OUTPUT.PUT_LINE('TEST 3: Building type does not exist');
    
    building_record := building_facility_association_record(
        building_identifier => 10002,
        building_name_value => 'USBD36 Bad Type',
        number_of_buildings_value => 1,
        building_type_identifier => 999999,
        facility_identifier => 50
    );
    
    result_building_id := usbd36_add_building(building_record);
    DBMS_OUTPUT.PUT_LINE('UNEXPECTED SUCCESS: building_id=' || result_building_id);
    ROLLBACK TO sp_test;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('EXPECTED ERROR: ' || SQLERRM);
        ROLLBACK TO sp_test;
END;
/

-- Test 4: Invalid number of buildings
DECLARE
    building_record building_facility_association_record;
    result_building_id NUMBER;
BEGIN
    SAVEPOINT sp_test;
    
    DBMS_OUTPUT.PUT_LINE('TEST 4: Invalid number of buildings');
    
    building_record := building_facility_association_record(
        building_identifier => 10003,
        building_name_value => 'USBD36 Bad Number',
        number_of_buildings_value => 0,
        building_type_identifier => 1,
        facility_identifier => 50
    );
    
    result_building_id := usbd36_add_building(building_record);
    DBMS_OUTPUT.PUT_LINE('UNEXPECTED SUCCESS: building_id=' || result_building_id);
    ROLLBACK TO sp_test;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('EXPECTED ERROR: ' || SQLERRM);
        ROLLBACK TO sp_test;
END;
/

-- Test 5: Duplicate building (same name or id)
DECLARE
    building_record building_facility_association_record;
    result_building_id NUMBER;
BEGIN
    SAVEPOINT sp_test;
    
    DBMS_OUTPUT.PUT_LINE('TEST 5: Duplicate building');
    
    -- First insert (should succeed)
    building_record := building_facility_association_record(
        building_identifier => 10004,
        building_name_value => 'USBD36 Duplicate Name',
        number_of_buildings_value => 1,
        building_type_identifier => 1,
        facility_identifier => 50
    );
    
    result_building_id := usbd36_add_building(building_record);
    DBMS_OUTPUT.PUT_LINE('First insert OK: building_id=' || result_building_id);
    
    -- Second insert with same name (should fail)
    building_record := building_facility_association_record(
        building_identifier => 10005,
        building_name_value => 'USBD36 Duplicate Name',
        number_of_buildings_value => 1,
        building_type_identifier => 1,
        facility_identifier => 50
    );
    
    result_building_id := usbd36_add_building(building_record);
    DBMS_OUTPUT.PUT_LINE('UNEXPECTED SUCCESS: building_id=' || result_building_id);
    ROLLBACK TO sp_test;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('EXPECTED ERROR: ' || SQLERRM);
        ROLLBACK TO sp_test;
END;
/
