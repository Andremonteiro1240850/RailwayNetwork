-- Parameters:
--   p_segment_id              - ID of the new segment
--   p_line_id                 - ID of the existing line
--   p_length_m                - Length of the segment in meters (must be > 0)
--   p_segment_order           - Order of the segment (must be unique)
--   p_electrified_flag        - 'Y' or 'N' (electrified status)
--   p_segment_type_track_number - Track number for SegmentType
--   p_segment_type_track_type   - Track type: 'SINGLE', 'DOUBLE', or 'QUAD'
--   p_siding_id               - ID of the siding (NULL if no siding)
--   p_siding_size             - Size of the siding (required if siding_id doesn't exist)

-- Exceptions:
--   -20001: Line not found
--   -20002: Segment order already exists
--   -20003: Segment type not found
--   -20004: Invalid electrified flag (must be 'Y' or 'N')
--   -20005: Invalid segment type (must be 'SINGLE', 'DOUBLE', or 'QUAD')
--   -20006: Invalid length (must be > 0)
--   -20007: Siding ID provided but siding size not provided
--   -20008: Segment ID already exists

CREATE OR REPLACE PROCEDURE prc_usbd44_add_segment_to_line(
    p_segment_id              IN NUMBER,
    p_line_id                 IN NUMBER,
    p_length_m                IN NUMBER,
    p_segment_order           IN NUMBER,
    p_electrified_flag        IN CHAR,
    p_segment_type_track_number IN NUMBER,
    p_segment_type_track_type   IN VARCHAR2,
    p_siding_id               IN NUMBER DEFAULT NULL,
    p_siding_size             IN NUMBER DEFAULT NULL
) IS
    -- Variables for validations
    v_line_exists             NUMBER;
    v_segment_order_exists    NUMBER;
    v_segment_id_exists       NUMBER;
    v_segment_type_exists      NUMBER;
    v_siding_exists            NUMBER;
    
    -- Custom exceptions
    line_not_found             EXCEPTION;
    segment_order_exists        EXCEPTION;
    segment_id_exists           EXCEPTION;
    segment_type_not_found      EXCEPTION;
    invalid_electrified_flag    EXCEPTION;
    invalid_segment_type        EXCEPTION;
    invalid_length              EXCEPTION;
    siding_size_required        EXCEPTION;

BEGIN
    -- ===========================================
    -- VALIDATIONS
    -- ===========================================
    
    -- 1. Validate that the line exists
    BEGIN
        SELECT COUNT(*)
        INTO v_line_exists
        FROM Line
        WHERE line_id = p_line_id;
        
        IF v_line_exists = 0 THEN
            RAISE line_not_found;
        END IF;
    END;
    
    -- 2. Validate that segment_id doesn't already exist
    BEGIN
        SELECT COUNT(*)
        INTO v_segment_id_exists
        FROM Segment
        WHERE segment_id = p_segment_id;
        
        IF v_segment_id_exists > 0 THEN
            RAISE segment_id_exists;
        END IF;
    END;
    
    -- 3. Validate that segment_order doesn't already exist (UNIQUE constraint)
    BEGIN
        SELECT COUNT(*)
        INTO v_segment_order_exists
        FROM Segment
        WHERE segment_order = p_segment_order;
        
        IF v_segment_order_exists > 0 THEN
            RAISE segment_order_exists;
        END IF;
    END;
    
    -- 4. Validate length > 0
    IF p_length_m <= 0 THEN
        RAISE invalid_length;
    END IF;
    
    -- 5. Validate electrified_flag
    IF p_electrified_flag NOT IN ('Y', 'N') THEN
        RAISE invalid_electrified_flag;
    END IF;
    
    -- 6. Validate segment type exists
    BEGIN
        SELECT COUNT(*)
        INTO v_segment_type_exists
        FROM SegmentType
        WHERE trackType = p_segment_type_track_type;
        
        IF v_segment_type_exists = 0 THEN
            RAISE segment_type_not_found;
        END IF;
    END;
    
    -- 7. Validate segment type value
    IF p_segment_type_track_type NOT IN ('SINGLE', 'DOUBLE', 'QUAD') THEN
        RAISE invalid_segment_type;
    END IF;
    
    -- 8. If siding_id is provided, validate and create if necessary
    IF p_siding_id IS NOT NULL THEN
        BEGIN
            SELECT COUNT(*)
            INTO v_siding_exists
            FROM Siding
            WHERE SIding_id = p_siding_id;
            
            -- If siding doesn't exist, we need the size to create it
            IF v_siding_exists = 0 THEN
                IF p_siding_size IS NULL THEN
                    RAISE siding_size_required;
                END IF;
            END IF;
        END;
    END IF;
    
    -- ===========================================
    -- TRANSACTION: Add segment and siding if needed
    -- ===========================================
    
    -- Create siding if it doesn't exist and siding_id is provided
    IF p_siding_id IS NOT NULL AND v_siding_exists = 0 THEN
        INSERT INTO Siding (SIding_id, "Size")
        VALUES (p_siding_id, p_siding_size);
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
        p_segment_id,
        p_line_id,
        p_length_m,
        p_segment_order,
        p_electrified_flag,
        p_segment_type_track_number,
        p_segment_type_track_type,
        p_siding_id
    );
    
    -- Commit the transaction
    COMMIT;
    
    -- Success message (optional)
    IF p_siding_id IS NOT NULL THEN
        DBMS_OUTPUT.PUT_LINE('Segment ' || p_segment_id || ' successfully added to line ' || 
                             p_line_id || ' with siding ' || p_siding_id || '.');
    ELSE
        DBMS_OUTPUT.PUT_LINE('Segment ' || p_segment_id || ' successfully added to line ' || 
                             p_line_id || ' without siding.');
    END IF;

EXCEPTION
    WHEN line_not_found THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20001, 'Line with ID ' || p_line_id || ' not found.');
    
    WHEN segment_id_exists THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20008, 'Segment with ID ' || p_segment_id || ' already exists.');
    
    WHEN segment_order_exists THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20002, 'Segment order ' || p_segment_order || 
                                 ' already exists. Each segment must have a unique order.');
    
    WHEN segment_type_not_found THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20003, 'Segment type ' || p_segment_type_track_type || ' not found.');
    
    WHEN invalid_electrified_flag THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20004, 'Invalid electrified flag. Must be ''Y'' or ''N''.');
    
    WHEN invalid_segment_type THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20005, 'Invalid segment type. Must be ''SINGLE'', ''DOUBLE'', or ''QUAD''.');
    
    WHEN invalid_length THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20006, 'Invalid length. Must be greater than 0.');
    
    WHEN siding_size_required THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20007, 'Siding ID ' || p_siding_id || 
                                 ' does not exist. Siding size must be provided to create it.');
    
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20999, 'Unexpected error while adding segment to line: ' || SQLERRM);
END prc_usbd44_add_segment_to_line;
/

