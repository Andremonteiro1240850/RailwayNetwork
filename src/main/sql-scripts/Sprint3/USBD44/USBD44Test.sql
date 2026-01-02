DECLARE
    segment_record segment_line_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 1: Add segment without siding');
    -- Add a new segment to line 1 (Ramal São Bento - Campanhã)
    -- Using segment_order 100 (to avoid conflicts with existing orders 1-16)
    segment_record := segment_line_association_record(
        segment_identifier => 100,
        line_identifier => 1,
        length_in_meters => 5000,
        segment_order_value => 100,
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


DECLARE
    segment_record segment_line_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 2: Add segment with existing siding');
    -- Add a segment to line 3 with existing siding 1
    segment_record := segment_line_association_record(
        segment_identifier => 101,
        line_identifier => 3,
        length_in_meters => 8000,
        segment_order_value => 101,
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

DECLARE
    segment_record segment_line_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 3: Add segment with new siding');
    -- Add a segment to line 4 with a new siding (ID 3)
    segment_record := segment_line_association_record(
        segment_identifier => 102,
        line_identifier => 4,
        length_in_meters => 3500,
        segment_order_value => 102,
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

DECLARE
    segment_record segment_line_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 4: Line not found');
    segment_record := segment_line_association_record(
        segment_identifier => 103,
        line_identifier => 99999,
        length_in_meters => 5000,
        segment_order_value => 103,
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

DECLARE
    segment_record segment_line_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 5: Segment order already exists');
    -- Try to use segment_order 1 which already exists
    segment_record := segment_line_association_record(
        segment_identifier => 104,
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

DECLARE
    segment_record segment_line_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 6: Segment ID already exists');
    -- Try to use segment_id 1 which already exists
    segment_record := segment_line_association_record(
        segment_identifier => 1,  -- Already exists
        line_identifier => 1,
        length_in_meters => 5000,
        segment_order_value => 104,
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

DECLARE
    segment_record segment_line_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 7: Invalid electrified flag');
    segment_record := segment_line_association_record(
        segment_identifier => 105,
        line_identifier => 1,
        length_in_meters => 5000,
        segment_order_value => 105,
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

DECLARE
    segment_record segment_line_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 8: Invalid segment type');
    segment_record := segment_line_association_record(
        segment_identifier => 106,
        line_identifier => 1,
        length_in_meters => 5000,
        segment_order_value => 106,
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

DECLARE
    segment_record segment_line_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 9: Invalid length (<= 0)');
    segment_record := segment_line_association_record(
        segment_identifier => 107,
        line_identifier => 1,
        length_in_meters => 0,  -- Invalid
        segment_order_value => 107,
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


DECLARE
    segment_record segment_line_association_record;
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 10: Siding size required for new siding');
    -- Try to create a new siding (ID 4) without providing size
    segment_record := segment_line_association_record(
        segment_identifier => 108,
        line_identifier => 1,
        length_in_meters => 5000,
        segment_order_value => 108,
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
WHERE s.segment_id >= 100
ORDER BY s.segment_id;

