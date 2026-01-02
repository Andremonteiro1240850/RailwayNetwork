-- USBD45 - As a Planner, I want to add a new line. A line must have at least one segment.
--
-- This procedure uses a record type to receive all necessary data,
-- avoiding individual parameter passing and abbreviations.
-- All validations are wrapped in anonymous blocks.
--
-- Exceptions:
--   -20045: Invalid terminals (start = end)
--   -20046: Start facility not found
--   -20047: End facility not found
--   -20048: Owner not found
--   -20049: Gauge not found
--   -20050: Invalid segment length
--   -20051: Invalid electrified flag
--   -20052: Segment type not found
--   -20053: Siding not found
--   -20054: Duplicate key

CREATE OR REPLACE TYPE line_with_segment_record AS OBJECT (
    line_identifier                  NUMBER,
    start_facility_identifier       NUMBER,
    end_facility_identifier         NUMBER,
    line_name_value                 VARCHAR2(255),
    owner_identifier                VARCHAR2(10),
    gauge_code_value                NUMBER,
    segment_identifier              NUMBER,
    segment_length_in_meters        NUMBER,
    segment_order_value             NUMBER,
    electrified_flag_value          CHAR,
    segment_type_track_number_value NUMBER,
    segment_type_track_type_value   VARCHAR2(255),
    siding_identifier               NUMBER
);
/

CREATE OR REPLACE FUNCTION usbd45_add_line(
    line_data IN line_with_segment_record
) RETURN Line.line_id%TYPE
IS
    -- Variables for validations (not abbreviated)
    facility_existence_check        NUMBER;
    owner_existence_check           NUMBER;
    gauge_existence_check           NUMBER;
    segment_type_existence_check    NUMBER;
    siding_existence_check          NUMBER;
    
    -- Custom exceptions
    invalid_terminals_exception              EXCEPTION;
    start_facility_not_found_exception       EXCEPTION;
    end_facility_not_found_exception         EXCEPTION;
    owner_not_found_exception                EXCEPTION;
    gauge_not_found_exception                EXCEPTION;
    invalid_segment_length_exception         EXCEPTION;
    invalid_electrified_flag_exception       EXCEPTION;
    segment_type_not_found_exception         EXCEPTION;
    siding_not_found_exception               EXCEPTION;
    
BEGIN
    -- ===========================================
    -- VALIDATIONS (all in anonymous blocks)
    -- ===========================================
    
    -- 1. Validate terminals must be different
    BEGIN
        IF line_data.start_facility_identifier = line_data.end_facility_identifier THEN
            RAISE invalid_terminals_exception;
        END IF;
    END;
    
    -- 2. Validate start facility exists
    BEGIN
        SELECT COUNT(*)
        INTO facility_existence_check
        FROM Facility
        WHERE facility_id = line_data.start_facility_identifier;
        
        IF facility_existence_check = 0 THEN
            RAISE start_facility_not_found_exception;
        END IF;
    END;
    
    -- 3. Validate end facility exists
    BEGIN
        SELECT COUNT(*)
        INTO facility_existence_check
        FROM Facility
        WHERE facility_id = line_data.end_facility_identifier;
        
        IF facility_existence_check = 0 THEN
            RAISE end_facility_not_found_exception;
        END IF;
    END;
    
    -- 4. Validate owner exists
    BEGIN
        SELECT COUNT(*)
        INTO owner_existence_check
        FROM Owner
        WHERE owner_id = line_data.owner_identifier;
        
        IF owner_existence_check = 0 THEN
            RAISE owner_not_found_exception;
        END IF;
    END;
    
    -- 5. Validate gauge exists
    BEGIN
        SELECT COUNT(*)
        INTO gauge_existence_check
        FROM Gauge
        WHERE code = line_data.gauge_code_value;
        
        IF gauge_existence_check = 0 THEN
            RAISE gauge_not_found_exception;
        END IF;
    END;
    
    -- 6. Validate segment length
    BEGIN
        IF line_data.segment_length_in_meters IS NULL OR line_data.segment_length_in_meters <= 0 THEN
            RAISE invalid_segment_length_exception;
        END IF;
    END;
    
    -- 7. Validate electrified flag
    BEGIN
        IF line_data.electrified_flag_value NOT IN ('Y', 'N') THEN
            RAISE invalid_electrified_flag_exception;
        END IF;
    END;
    
    -- 8. Validate segment type exists
    BEGIN
        SELECT COUNT(*)
        INTO segment_type_existence_check
        FROM SegmentType
        WHERE trackType = line_data.segment_type_track_type_value;
        
        IF segment_type_existence_check = 0 THEN
            RAISE segment_type_not_found_exception;
        END IF;
    END;
    
    -- 9. Validate siding exists (if provided)
    BEGIN
        IF line_data.siding_identifier IS NOT NULL THEN
            SELECT COUNT(*)
            INTO siding_existence_check
            FROM Siding
            WHERE SIding_id = line_data.siding_identifier;
            
            IF siding_existence_check = 0 THEN
                RAISE siding_not_found_exception;
            END IF;
        END IF;
    END;
    
    -- ===========================================
    -- TRANSACTION: Insert Line and Segment
    -- ===========================================
    
    -- Insert Line
    BEGIN
        INSERT INTO Line (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode)
        VALUES (
            line_data.line_identifier,
            line_data.start_facility_identifier,
            line_data.end_facility_identifier,
            line_data.line_name_value,
            line_data.owner_identifier,
            line_data.gauge_code_value
        );
    END;
    
    -- Insert mandatory Segment
    BEGIN
        INSERT INTO Segment (
            segment_id, line_id, length_m, segment_order, electrified_flag,
            SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id
        )
        VALUES (
            line_data.segment_identifier,
            line_data.line_identifier,
            line_data.segment_length_in_meters,
            line_data.segment_order_value,
            line_data.electrified_flag_value,
            line_data.segment_type_track_number_value,
            line_data.segment_type_track_type_value,
            line_data.siding_identifier
        );
    END;
    
    RETURN line_data.line_identifier;
    
EXCEPTION
    WHEN invalid_terminals_exception THEN
        RAISE_APPLICATION_ERROR(-20045, 'INVALID_TERMINALS: Start and end facilities must be different.');
    
    WHEN start_facility_not_found_exception THEN
        RAISE_APPLICATION_ERROR(-20046, 'START_FACILITY_NOT_FOUND: Facility with ID ' || line_data.start_facility_identifier || ' does not exist.');
    
    WHEN end_facility_not_found_exception THEN
        RAISE_APPLICATION_ERROR(-20047, 'END_FACILITY_NOT_FOUND: Facility with ID ' || line_data.end_facility_identifier || ' does not exist.');
    
    WHEN owner_not_found_exception THEN
        RAISE_APPLICATION_ERROR(-20048, 'OWNER_NOT_FOUND: Owner with ID ' || line_data.owner_identifier || ' does not exist.');
    
    WHEN gauge_not_found_exception THEN
        RAISE_APPLICATION_ERROR(-20049, 'GAUGE_NOT_FOUND: Gauge with code ' || line_data.gauge_code_value || ' does not exist.');
    
    WHEN invalid_segment_length_exception THEN
        RAISE_APPLICATION_ERROR(-20050, 'INVALID_SEGMENT_LENGTH: Segment length must be greater than 0.');
    
    WHEN invalid_electrified_flag_exception THEN
        RAISE_APPLICATION_ERROR(-20051, 'INVALID_ELECTRIFIED_FLAG: Electrified flag must be ''Y'' or ''N''.');
    
    WHEN segment_type_not_found_exception THEN
        RAISE_APPLICATION_ERROR(-20052, 'SEGMENTTYPE_NOT_FOUND: Segment type ' || line_data.segment_type_track_type_value || ' does not exist.');
    
    WHEN siding_not_found_exception THEN
        RAISE_APPLICATION_ERROR(-20053, 'SIDING_NOT_FOUND: Siding with ID ' || line_data.siding_identifier || ' does not exist.');
    
    WHEN DUP_VAL_ON_INDEX THEN
        RAISE_APPLICATION_ERROR(-20054, 'DUPLICATE_KEY: A record with this key already exists.');
    
    WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20055, 'ERROR: ' || SQLERRM);
END usbd45_add_line;
/
