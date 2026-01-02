-- Test cases for USBD45 - Add Line with Segment
-- All test cases use anonymous blocks and the record type

-- Test 1: Success case
DECLARE
    line_record line_with_segment_record;
    result_line_id NUMBER;
BEGIN
    SAVEPOINT sp_test;
    
    DBMS_OUTPUT.PUT_LINE('TEST 1: Success case');
    
    line_record := line_with_segment_record(
        line_identifier => 9000,
        start_facility_identifier => 50,
        end_facility_identifier => 12,
        line_name_value => 'Test Line OK',
        owner_identifier => 'IP',
        gauge_code_value => 1668,
        segment_identifier => 90000,
        segment_length_in_meters => 3000,
        segment_order_value => 90000,
        electrified_flag_value => 'Y',
        segment_type_track_number_value => 2,
        segment_type_track_type_value => 'DOUBLE',
        siding_identifier => NULL
    );
    
    result_line_id := usbd45_add_line(line_record);
    DBMS_OUTPUT.PUT_LINE('SUCCESS: line_id=' || result_line_id);
    
    ROLLBACK TO sp_test;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('ERROR: ' || SQLERRM);
        ROLLBACK TO sp_test;
END;
/

-- Test 2: Invalid terminals (start = end)
DECLARE
    line_record line_with_segment_record;
    result_line_id NUMBER;
BEGIN
    SAVEPOINT sp_test;
    
    DBMS_OUTPUT.PUT_LINE('TEST 2: Invalid terminals');
    
    line_record := line_with_segment_record(
        line_identifier => 9001,
        start_facility_identifier => 50,
        end_facility_identifier => 50,
        line_name_value => 'Bad Terminals',
        owner_identifier => 'IP',
        gauge_code_value => 1668,
        segment_identifier => 90001,
        segment_length_in_meters => 3000,
        segment_order_value => 90001,
        electrified_flag_value => 'Y',
        segment_type_track_number_value => 2,
        segment_type_track_type_value => 'DOUBLE',
        siding_identifier => NULL
    );
    
    result_line_id := usbd45_add_line(line_record);
    DBMS_OUTPUT.PUT_LINE('UNEXPECTED SUCCESS: line_id=' || result_line_id);
    ROLLBACK TO sp_test;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('EXPECTED ERROR: ' || SQLERRM);
        ROLLBACK TO sp_test;
END;
/

-- Test 3: Owner does not exist
DECLARE
    line_record line_with_segment_record;
    result_line_id NUMBER;
BEGIN
    SAVEPOINT sp_test;
    
    DBMS_OUTPUT.PUT_LINE('TEST 3: Owner does not exist');
    
    line_record := line_with_segment_record(
        line_identifier => 9002,
        start_facility_identifier => 50,
        end_facility_identifier => 12,
        line_name_value => 'Bad Owner',
        owner_identifier => 'NOPE',
        gauge_code_value => 1668,
        segment_identifier => 90002,
        segment_length_in_meters => 3000,
        segment_order_value => 90002,
        electrified_flag_value => 'Y',
        segment_type_track_number_value => 2,
        segment_type_track_type_value => 'DOUBLE',
        siding_identifier => NULL
    );
    
    result_line_id := usbd45_add_line(line_record);
    DBMS_OUTPUT.PUT_LINE('UNEXPECTED SUCCESS: line_id=' || result_line_id);
    ROLLBACK TO sp_test;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('EXPECTED ERROR: ' || SQLERRM);
        ROLLBACK TO sp_test;
END;
/

-- Test 4: Invalid segment length
DECLARE
    line_record line_with_segment_record;
    result_line_id NUMBER;
BEGIN
    SAVEPOINT sp_test;
    
    DBMS_OUTPUT.PUT_LINE('TEST 4: Invalid segment length');
    
    line_record := line_with_segment_record(
        line_identifier => 9003,
        start_facility_identifier => 50,
        end_facility_identifier => 12,
        line_name_value => 'Bad Segment',
        owner_identifier => 'IP',
        gauge_code_value => 1668,
        segment_identifier => 90003,
        segment_length_in_meters => 0,
        segment_order_value => 90003,
        electrified_flag_value => 'Y',
        segment_type_track_number_value => 2,
        segment_type_track_type_value => 'DOUBLE',
        siding_identifier => NULL
    );
    
    result_line_id := usbd45_add_line(line_record);
    DBMS_OUTPUT.PUT_LINE('UNEXPECTED SUCCESS: line_id=' || result_line_id);
    ROLLBACK TO sp_test;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('EXPECTED ERROR: ' || SQLERRM);
        ROLLBACK TO sp_test;
END;
/

-- Test 5: Duplicate line name
DECLARE
    line_record line_with_segment_record;
    result_line_id NUMBER;
BEGIN
    SAVEPOINT sp_test;
    
    DBMS_OUTPUT.PUT_LINE('TEST 5: Duplicate line name');
    
    -- First insert (should succeed)
    line_record := line_with_segment_record(
        line_identifier => 9004,
        start_facility_identifier => 50,
        end_facility_identifier => 12,
        line_name_value => 'Duplicate Line',
        owner_identifier => 'IP',
        gauge_code_value => 1668,
        segment_identifier => 90004,
        segment_length_in_meters => 3000,
        segment_order_value => 90004,
        electrified_flag_value => 'Y',
        segment_type_track_number_value => 2,
        segment_type_track_type_value => 'DOUBLE',
        siding_identifier => NULL
    );
    
    result_line_id := usbd45_add_line(line_record);
    DBMS_OUTPUT.PUT_LINE('First insert OK: line_id=' || result_line_id);
    
    -- Second insert with same name (should fail)
    line_record := line_with_segment_record(
        line_identifier => 9005,
        start_facility_identifier => 50,
        end_facility_identifier => 12,
        line_name_value => 'Duplicate Line',
        owner_identifier => 'IP',
        gauge_code_value => 1668,
        segment_identifier => 90005,
        segment_length_in_meters => 3000,
        segment_order_value => 90005,
        electrified_flag_value => 'Y',
        segment_type_track_number_value => 2,
        segment_type_track_type_value => 'DOUBLE',
        siding_identifier => NULL
    );
    
    result_line_id := usbd45_add_line(line_record);
    DBMS_OUTPUT.PUT_LINE('UNEXPECTED SUCCESS: line_id=' || result_line_id);
    ROLLBACK TO sp_test;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('EXPECTED ERROR: ' || SQLERRM);
        ROLLBACK TO sp_test;
END;
/
