DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 1: Add segment without siding');
    -- Add a new segment to line 1 (Ramal São Bento - Campanhã)
    -- Using segment_order 100 (to avoid conflicts with existing orders 1-16)
    prc_usbd44_add_segment_to_line(
        p_segment_id              => 100,
        p_line_id                 => 1,
        p_length_m                => 5000,
        p_segment_order           => 100,
        p_electrified_flag        => 'Y',
        p_segment_type_track_number => 2,
        p_segment_type_track_type   => 'DOUBLE',
        p_siding_id               => NULL,
        p_siding_size             => NULL
    );
    
    DBMS_OUTPUT.PUT_LINE('✓ SUCCESS: Segment added without siding');
    DBMS_OUTPUT.PUT_LINE(' ');
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✗ ERROR: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/


DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 2: Add segment with existing siding');
    -- Add a segment to line 3 with existing siding 1
    prc_usbd44_add_segment_to_line(
        p_segment_id              => 101,
        p_line_id                 => 3,
        p_length_m                => 8000,
        p_segment_order           => 101,
        p_electrified_flag        => 'Y',
        p_segment_type_track_number => 1,
        p_segment_type_track_type   => 'SINGLE',
        p_siding_id               => 1,  -- Existing siding
        p_siding_size             => NULL  -- Not needed, siding exists
    );
    
    DBMS_OUTPUT.PUT_LINE('✓ SUCCESS: Segment added with existing siding');
    DBMS_OUTPUT.PUT_LINE(' ');
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✗ ERROR: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 3: Add segment with new siding');
    -- Add a segment to line 4 with a new siding (ID 3)
    prc_usbd44_add_segment_to_line(
        p_segment_id              => 102,
        p_line_id                 => 4,
        p_length_m                => 3500,
        p_segment_order           => 102,
        p_electrified_flag        => 'N',
        p_segment_type_track_number => 1,
        p_segment_type_track_type   => 'SINGLE',
        p_siding_id               => 3,  -- New siding
        p_siding_size             => 500  -- Required to create new siding
    );
    
    DBMS_OUTPUT.PUT_LINE('✓ SUCCESS: Segment added with new siding created');
    DBMS_OUTPUT.PUT_LINE(' ');
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✗ ERROR: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 4: Line not found');
    prc_usbd44_add_segment_to_line(
        p_segment_id              => 103,
        p_line_id                 => 99999,
        p_length_m                => 5000,
        p_segment_order           => 103,
        p_electrified_flag        => 'Y',
        p_segment_type_track_number => 2,
        p_segment_type_track_type   => 'DOUBLE',
        p_siding_id               => NULL,
        p_siding_size             => NULL
    );
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 5: Segment order already exists');
    -- Try to use segment_order 1 which already exists
    prc_usbd44_add_segment_to_line(
        p_segment_id              => 104,
        p_line_id                 => 1,
        p_length_m                => 5000,
        p_segment_order           => 1,  -- Already exists
        p_electrified_flag        => 'Y',
        p_segment_type_track_number => 2,
        p_segment_type_track_type   => 'DOUBLE',
        p_siding_id               => NULL,
        p_siding_size             => NULL
    );
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 6: Segment ID already exists');
    -- Try to use segment_id 1 which already exists
    prc_usbd44_add_segment_to_line(
        p_segment_id              => 1,  -- Already exists
        p_line_id                 => 1,
        p_length_m                => 5000,
        p_segment_order           => 104,
        p_electrified_flag        => 'Y',
        p_segment_type_track_number => 2,
        p_segment_type_track_type   => 'DOUBLE',
        p_siding_id               => NULL,
        p_siding_size             => NULL
    );
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 7: Invalid electrified flag');
    prc_usbd44_add_segment_to_line(
        p_segment_id              => 105,
        p_line_id                 => 1,
        p_length_m                => 5000,
        p_segment_order           => 105,
        p_electrified_flag        => 'X',  -- Invalid
        p_segment_type_track_number => 2,
        p_segment_type_track_type   => 'DOUBLE',
        p_siding_id               => NULL,
        p_siding_size             => NULL
    );
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 8: Invalid segment type');
    prc_usbd44_add_segment_to_line(
        p_segment_id              => 106,
        p_line_id                 => 1,
        p_length_m                => 5000,
        p_segment_order           => 106,
        p_electrified_flag        => 'Y',
        p_segment_type_track_number => 2,
        p_segment_type_track_type   => 'TRIPLE',  -- Invalid
        p_siding_id               => NULL,
        p_siding_size             => NULL
    );
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/

DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 9: Invalid length (<= 0)');
    prc_usbd44_add_segment_to_line(
        p_segment_id              => 107,
        p_line_id                 => 1,
        p_length_m                => 0,  -- Invalid
        p_segment_order           => 107,
        p_electrified_flag        => 'Y',
        p_segment_type_track_number => 2,
        p_segment_type_track_type   => 'DOUBLE',
        p_siding_id               => NULL,
        p_siding_size             => NULL
    );
    
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('✓ Expected error caught: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE(' ');
END;
/


DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('TEST 10: Siding size required for new siding');
    -- Try to create a new siding (ID 4) without providing size
    prc_usbd44_add_segment_to_line(
        p_segment_id              => 108,
        p_line_id                 => 1,
        p_length_m                => 5000,
        p_segment_order           => 108,
        p_electrified_flag        => 'Y',
        p_segment_type_track_number => 1,
        p_segment_type_track_type   => 'SINGLE',
        p_siding_id               => 4,  -- New siding
        p_siding_size             => NULL  -- Missing size
    );
    
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

