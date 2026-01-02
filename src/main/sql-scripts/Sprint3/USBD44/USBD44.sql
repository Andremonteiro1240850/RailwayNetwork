-- This procedure adds a segment to an existing line.
-- If the segment has a siding, then it must also be added.
--
-- The procedure uses a record type to receive all necessary data,
-- avoiding individual parameter passing and abbreviations.
--
-- Exceptions:
--   -20001: Line not found
--   -20002: Segment order already exists
--   -20003: Segment type not found
--   -20004: Invalid electrified flag (must be 'Y' or 'N')
--   -20005: Invalid segment type (must be 'SINGLE', 'DOUBLE', or 'QUAD')
--   -20006: Invalid length (must be > 0)
--   -20007: Siding ID provided but siding size not provided
--   -20008: Segment ID already exists

CREATE OR REPLACE TYPE segment_line_association_record AS OBJECT (
    segment_identifier              NUMBER,
    line_identifier                 NUMBER,
    length_in_meters                NUMBER,
    segment_order_value             NUMBER,
    electrified_flag_value          CHAR,
    segment_type_track_number_value NUMBER,
    segment_type_track_type_value   VARCHAR2(255),
    siding_identifier               NUMBER,
    siding_size_value               NUMBER
);
/

CREATE OR REPLACE PROCEDURE prc_usbd44_add_segment_to_line(
    segment_data IN segment_line_association_record
) IS
    -- Variables for validations
    line_exists_check             NUMBER;
    segment_order_exists_check    NUMBER;
    segment_identifier_exists_check       NUMBER;
    segment_type_exists_check      NUMBER;
    siding_exists_check            NUMBER := 0;
    
    -- Custom exceptions
    line_not_found_exception             EXCEPTION;
    segment_order_exists_exception        EXCEPTION;
    segment_identifier_exists_exception           EXCEPTION;
    segment_type_not_found_exception      EXCEPTION;
    invalid_electrified_flag_exception    EXCEPTION;
    invalid_segment_type_exception        EXCEPTION;
    invalid_length_exception              EXCEPTION;
    siding_size_required_exception        EXCEPTION;

BEGIN
    -- ===========================================
    -- VALIDATIONS
    -- ===========================================
    
    -- 1. Validate that the line exists
    BEGIN
        SELECT COUNT(*)
        INTO line_exists_check
        FROM Line
        WHERE line_id = segment_data.line_identifier;
        
        IF line_exists_check = 0 THEN
            RAISE line_not_found_exception;
        END IF;
    END;
    
    -- 2. Validate that segment_identifier doesn't already exist
    BEGIN
        SELECT COUNT(*)
        INTO segment_identifier_exists_check
        FROM Segment
        WHERE segment_id = segment_data.segment_identifier;
        
        IF segment_identifier_exists_check > 0 THEN
            RAISE segment_identifier_exists_exception;
        END IF;
    END;
    
    -- 3. Validate that segment_order_value doesn't already exist (UNIQUE constraint)
    BEGIN
        SELECT COUNT(*)
        INTO segment_order_exists_check
        FROM Segment
        WHERE segment_order = segment_data.segment_order_value;
        
        IF segment_order_exists_check > 0 THEN
            RAISE segment_order_exists_exception;
        END IF;
    END;
    
    -- 4. Validate length > 0
    IF segment_data.length_in_meters <= 0 THEN
        RAISE invalid_length_exception;
    END IF;
    
    -- 5. Validate electrified_flag_value
    IF segment_data.electrified_flag_value NOT IN ('Y', 'N') THEN
        RAISE invalid_electrified_flag_exception;
    END IF;
    
    -- 6. Validate segment type exists
    BEGIN
        SELECT COUNT(*)
        INTO segment_type_exists_check
        FROM SegmentType
        WHERE trackType = segment_data.segment_type_track_type_value;
        
        IF segment_type_exists_check = 0 THEN
            RAISE segment_type_not_found_exception;
        END IF;
    END;
    
    -- 7. Validate segment type value
    IF segment_data.segment_type_track_type_value NOT IN ('SINGLE', 'DOUBLE', 'QUAD') THEN
        RAISE invalid_segment_type_exception;
    END IF;
    
    -- 8. If siding_identifier is provided, validate and create if necessary
    IF segment_data.siding_identifier IS NOT NULL THEN
        BEGIN
            SELECT COUNT(*)
            INTO siding_exists_check
            FROM Siding
            WHERE SIding_id = segment_data.siding_identifier;
            
            -- If siding doesn't exist, we need the size to create it
            IF siding_exists_check = 0 THEN
                IF segment_data.siding_size_value IS NULL THEN
                    RAISE siding_size_required_exception;
                END IF;
            END IF;
        END;
    END IF;
    
    -- ===========================================
    -- TRANSACTION: Add segment and siding if needed
    -- ===========================================
    
    -- Create siding if it doesn't exist and siding_identifier is provided
    IF segment_data.siding_identifier IS NOT NULL AND siding_exists_check = 0 THEN
        INSERT INTO Siding (SIding_id, "Size")
        VALUES (segment_data.siding_identifier, segment_data.siding_size_value);
    END IF;
    
    -- Insert the segment
    INSERT INTO Segment (
        segment_id,
        line_id,
        length_m,
        segment_order,
        electrified_flag,
        SegmentTypetrackNumber,
        SegmentTypetrackType,
        SidingSIding_id
    ) VALUES (
        segment_data.segment_identifier,
        segment_data.line_identifier,
        segment_data.length_in_meters,
        segment_data.segment_order_value,
        segment_data.electrified_flag_value,
        segment_data.segment_type_track_number_value,
        segment_data.segment_type_track_type_value,
        segment_data.siding_identifier
    );
    
    -- Commit the transaction
    COMMIT;
    
    -- Success message (optional)
    IF segment_data.siding_identifier IS NOT NULL THEN
        DBMS_OUTPUT.PUT_LINE('Segment ' || segment_data.segment_identifier || ' successfully added to line ' || 
                             segment_data.line_identifier || ' with siding ' || segment_data.siding_identifier || '.');
    ELSE
        DBMS_OUTPUT.PUT_LINE('Segment ' || segment_data.segment_identifier || ' successfully added to line ' || 
                             segment_data.line_identifier || ' without siding.');
    END IF;

EXCEPTION
    WHEN line_not_found_exception THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20001, 'Line with ID ' || segment_data.line_identifier || ' not found.');
    
    WHEN segment_identifier_exists_exception THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20008, 'Segment with ID ' || segment_data.segment_identifier || ' already exists.');
    
    WHEN segment_order_exists_exception THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20002, 'Segment order ' || segment_data.segment_order_value || 
                                 ' already exists. Each segment must have a unique order.');
    
    WHEN segment_type_not_found_exception THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20003, 'Segment type ' || segment_data.segment_type_track_type_value || ' not found.');
    
    WHEN invalid_electrified_flag_exception THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20004, 'Invalid electrified flag. Must be ''Y'' or ''N''.');
    
    WHEN invalid_segment_type_exception THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20005, 'Invalid segment type. Must be ''SINGLE'', ''DOUBLE'', or ''QUAD''.');
    
    WHEN invalid_length_exception THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20006, 'Invalid length. Must be greater than 0.');
    
    WHEN siding_size_required_exception THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20007, 'Siding ID ' || segment_data.siding_identifier || 
                                 ' does not exist. Siding size must be provided to create it.');
    
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20999, 'Unexpected error while adding segment to line: ' || SQLERRM);
END prc_usbd44_add_segment_to_line;
/

