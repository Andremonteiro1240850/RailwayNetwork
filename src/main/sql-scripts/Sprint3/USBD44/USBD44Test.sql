-- TEST 1: Add segment without siding
DECLARE
    segment_record segment_line_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 1: Add segment without siding');
    DBMS_OUTPUT.PUT_LINE('Description: Adds a new segment to line 1 (Ramal São Bento - Campanhã) without siding');
    DBMS_OUTPUT.PUT_LINE('Expected: SUCCESS - Segment added without siding');
    
    -- Cleanup: Delete segment if it exists from previous test runs
    DELETE FROM Segment WHERE segment_id = 200;
    
    -- Add a new segment to line 1 (Ramal São Bento - Campanhã)
    -- Using segment_order 200 (to avoid conflicts with existing orders 1-32 and previous test runs)
    -- Using segment_id 200 (to avoid conflicts with existing IDs 1-61 and previous test runs)
    segment_record := segment_line_association_record(
        segment_identifier => 200,
        line_identifier => 1,
        length_in_meters => 5000,
        segment_order_value => 200,
        electrified_flag_value => 'Y',
        segment_type_track_number_value => 2,
        segment_type_track_type_value => 'DOUBLE',
        siding_identifier => NULL,
        siding_size_value => NULL
    );
    prc_usbd44_add_segment_to_line(segment_record);
    
    DBMS_OUTPUT.PUT_LINE('✓ SUCCESS: Segment added without siding');
    DBMS_OUTPUT.PUT_LINE(' ');
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✗ ERROR: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- TEST 2: Add segment with existing siding
DECLARE
    segment_record segment_line_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 2: Add segment with existing siding');
    DBMS_OUTPUT.PUT_LINE('Description: Adds a segment to line 7 with existing siding 1 (Size: 864)');
    DBMS_OUTPUT.PUT_LINE('Expected: SUCCESS - Segment added with existing siding');
    
    -- Cleanup: Delete segment if it exists from previous test runs
    DELETE FROM Segment WHERE segment_id = 201;
    
    -- Add a segment to line 7 with existing siding 1
    -- Siding 1 exists in database with size 864
    segment_record := segment_line_association_record(
        segment_identifier => 201,
        line_identifier => 7,
        length_in_meters => 8000,
        segment_order_value => 201,
        electrified_flag_value => 'Y',
        segment_type_track_number_value => 1,
        segment_type_track_type_value => 'SINGLE',
        siding_identifier => 1,  -- Existing siding
        siding_size_value => NULL  -- Not needed, siding exists
    );
    prc_usbd44_add_segment_to_line(segment_record);
    
    DBMS_OUTPUT.PUT_LINE('✓ SUCCESS: Segment added with existing siding');
    DBMS_OUTPUT.PUT_LINE(' ');
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✗ ERROR: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- TEST 3: Add segment with new siding
DECLARE
    segment_record segment_line_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 3: Add segment with new siding');
    DBMS_OUTPUT.PUT_LINE('Description: Adds a segment to line 4 with a new siding (ID 3, Size: 500)');
    DBMS_OUTPUT.PUT_LINE('Expected: SUCCESS - Segment added with new siding created');
    
    -- Cleanup: Delete segment and siding if they exist from previous test runs
    DELETE FROM Segment WHERE segment_id = 202;
    DELETE FROM Siding WHERE SIding_id = 3;
    
    -- Add a segment to line 4 with a new siding (ID 3)
    -- Siding 3 does not exist, so it will be created with size 500
    segment_record := segment_line_association_record(
        segment_identifier => 202,
        line_identifier => 4,
        length_in_meters => 3500,
        segment_order_value => 202,
        electrified_flag_value => 'N',
        segment_type_track_number_value => 1,
        segment_type_track_type_value => 'SINGLE',
        siding_identifier => 3,  -- New siding
        siding_size_value => 500  -- Required to create new siding
    );
    prc_usbd44_add_segment_to_line(segment_record);
    
    DBMS_OUTPUT.PUT_LINE('✓ SUCCESS: Segment added with new siding created');
    DBMS_OUTPUT.PUT_LINE(' ');
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✗ ERROR: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- TEST 4: Line not found
DECLARE
    segment_record segment_line_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 4: Line not found');
    DBMS_OUTPUT.PUT_LINE('Description: Attempts to add segment to non-existent line 99999');
    DBMS_OUTPUT.PUT_LINE('Expected: ERROR - Line not found');
    segment_record := segment_line_association_record(
        segment_identifier => 203,
        line_identifier => 99999,
        length_in_meters => 5000,
        segment_order_value => 203,
        electrified_flag_value => 'Y',
        segment_type_track_number_value => 2,
        segment_type_track_type_value => 'DOUBLE',
        siding_identifier => NULL,
        siding_size_value => NULL
    );
    prc_usbd44_add_segment_to_line(segment_record);
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- TEST 5: Segment order already exists
DECLARE
    segment_record segment_line_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 5: Segment order already exists');
    DBMS_OUTPUT.PUT_LINE('Description: Attempts to use segment_order 1 which already exists in line 1');
    DBMS_OUTPUT.PUT_LINE('Expected: ERROR - Segment order already exists');
    -- Try to use segment_order 1 which already exists in line 1
    segment_record := segment_line_association_record(
        segment_identifier => 204,
        line_identifier => 1,
        length_in_meters => 5000,
        segment_order_value => 1,  -- Already exists
        electrified_flag_value => 'Y',
        segment_type_track_number_value => 2,
        segment_type_track_type_value => 'DOUBLE',
        siding_identifier => NULL,
        siding_size_value => NULL
    );
    prc_usbd44_add_segment_to_line(segment_record);
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- TEST 6: Segment ID already exists
DECLARE
    segment_record segment_line_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 6: Segment ID already exists');
    DBMS_OUTPUT.PUT_LINE('Description: Attempts to use segment_id 1 which already exists');
    DBMS_OUTPUT.PUT_LINE('Expected: ERROR - Segment ID already exists');
    -- Try to use segment_id 1 which already exists
    segment_record := segment_line_association_record(
        segment_identifier => 1,  -- Already exists
        line_identifier => 1,
        length_in_meters => 5000,
        segment_order_value => 105,
        electrified_flag_value => 'Y',
        segment_type_track_number_value => 2,
        segment_type_track_type_value => 'DOUBLE',
        siding_identifier => NULL,
        siding_size_value => NULL
    );
    prc_usbd44_add_segment_to_line(segment_record);
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- TEST 7: Invalid electrified flag
DECLARE
    segment_record segment_line_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 7: Invalid electrified flag');
    DBMS_OUTPUT.PUT_LINE('Description: Attempts to use invalid electrified flag ''X'' (must be ''Y'' or ''N'')');
    DBMS_OUTPUT.PUT_LINE('Expected: ERROR - Invalid electrified flag');
    segment_record := segment_line_association_record(
        segment_identifier => 206,
        line_identifier => 1,
        length_in_meters => 5000,
        segment_order_value => 206,
        electrified_flag_value => 'X',  -- Invalid
        segment_type_track_number_value => 2,
        segment_type_track_type_value => 'DOUBLE',
        siding_identifier => NULL,
        siding_size_value => NULL
    );
    prc_usbd44_add_segment_to_line(segment_record);
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- TEST 8: Invalid segment type
DECLARE
    segment_record segment_line_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 8: Invalid segment type');
    DBMS_OUTPUT.PUT_LINE('Description: Attempts to use invalid segment type ''TRIPLE'' (must be ''SINGLE'', ''DOUBLE'', or ''QUAD'')');
    DBMS_OUTPUT.PUT_LINE('Expected: ERROR - Invalid segment type');
    segment_record := segment_line_association_record(
        segment_identifier => 207,
        line_identifier => 1,
        length_in_meters => 5000,
        segment_order_value => 207,
        electrified_flag_value => 'Y',
        segment_type_track_number_value => 2,
        segment_type_track_type_value => 'TRIPLE',  -- Invalid
        siding_identifier => NULL,
        siding_size_value => NULL
    );
    prc_usbd44_add_segment_to_line(segment_record);
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- TEST 9: Invalid length (<= 0)
DECLARE
    segment_record segment_line_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 9: Invalid length (<= 0)');
    DBMS_OUTPUT.PUT_LINE('Description: Attempts to add segment with length 0 (must be > 0)');
    DBMS_OUTPUT.PUT_LINE('Expected: ERROR - Invalid length');
    segment_record := segment_line_association_record(
        segment_identifier => 208,
        line_identifier => 1,
        length_in_meters => 0,  -- Invalid
        segment_order_value => 208,
        electrified_flag_value => 'Y',
        segment_type_track_number_value => 2,
        segment_type_track_type_value => 'DOUBLE',
        siding_identifier => NULL,
        siding_size_value => NULL
    );
    prc_usbd44_add_segment_to_line(segment_record);
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- TEST 10: Siding size required for new siding
DECLARE
    segment_record segment_line_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 10: Siding size required for new siding');
    DBMS_OUTPUT.PUT_LINE('Description: Attempts to create new siding (ID 4) without providing size');
    DBMS_OUTPUT.PUT_LINE('Expected: ERROR - Siding size required for new siding');
    -- Try to create a new siding (ID 4) without providing size
    segment_record := segment_line_association_record(
        segment_identifier => 209,
        line_identifier => 1,
        length_in_meters => 5000,
        segment_order_value => 209,
        electrified_flag_value => 'Y',
        segment_type_track_number_value => 1,
        segment_type_track_type_value => 'SINGLE',
        siding_identifier => 4,  -- New siding
        siding_size_value => NULL  -- Missing size
    );
    prc_usbd44_add_segment_to_line(segment_record);
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

-- VERIFICATION: Query added segments
BEGIN
    DBMS_OUTPUT.PUT_LINE('VERIFICATION: Recently added segments');
END;
/

SELECT
    s.segment_id AS "Segment ID",
    s.line_id AS "Line ID",
    l.name AS "Line Name",
    s.length_m AS "Length (m)",
    s.segment_order AS "Order",
    s.electrified_flag AS "Electrified",
    s.SegmentTypetrackType AS "Track Type",
    s.SidingSIding_id AS "Siding ID",
    sid."Size" AS "Siding Size"
FROM Segment s
JOIN Line l ON s.line_id = l.line_id
LEFT JOIN Siding sid ON s.SidingSIding_id = sid.SIding_id
WHERE s.segment_id >= 200
ORDER BY s.segment_id;
