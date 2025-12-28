CREATE OR REPLACE FUNCTION usbd45_add_line(
    p_line_id              IN Line.line_id%TYPE,
    p_start_facility_id     IN Line.start_facility_id%TYPE,
    p_end_facility_id       IN Line.end_facility_id%TYPE,
    p_line_name             IN Line.name%TYPE,
    p_owner_id              IN Line.owner_id%TYPE,
    p_gauge_code            IN Line.Gaugecode%TYPE,

    p_segment_id            IN Segment.segment_id%TYPE,
    p_length_m              IN Segment.length_m%TYPE,
    p_segment_order         IN Segment.segment_order%TYPE,
    p_electrified_flag      IN Segment.electrified_flag%TYPE,
    p_track_number          IN Segment.SegmentTypetrackNumber%TYPE,
    p_track_type            IN Segment.SegmentTypetrackType%TYPE,
    p_siding_id             IN Segment.SidingSIding_id%TYPE
) RETURN Line.line_id%TYPE
IS
    v_cnt NUMBER;
BEGIN
    -- Terminals must be different
    IF p_start_facility_id = p_end_facility_id THEN
        raise_application_error(-20045, 'INVALID_TERMINALS');
END IF;

    -- Start facility exists
SELECT COUNT(*) INTO v_cnt FROM Facility WHERE facility_id = p_start_facility_id;
IF v_cnt = 0 THEN
        raise_application_error(-20045, 'START_FACILITY_NOT_FOUND');
END IF;

    -- End facility exists
SELECT COUNT(*) INTO v_cnt FROM Facility WHERE facility_id = p_end_facility_id;
IF v_cnt = 0 THEN
        raise_application_error(-20045, 'END_FACILITY_NOT_FOUND');
END IF;

    -- Owner exists
SELECT COUNT(*) INTO v_cnt FROM Owner WHERE owner_id = p_owner_id;
IF v_cnt = 0 THEN
        raise_application_error(-20045, 'OWNER_NOT_FOUND');
END IF;

    -- Gauge exists
SELECT COUNT(*) INTO v_cnt FROM Gauge WHERE code = p_gauge_code;
IF v_cnt = 0 THEN
        raise_application_error(-20045, 'GAUGE_NOT_FOUND');
END IF;

    -- Segment validations
    IF p_length_m IS NULL OR p_length_m <= 0 THEN
        raise_application_error(-20045, 'INVALID_SEGMENT_LENGTH');
END IF;

    IF p_electrified_flag NOT IN ('Y','N') THEN
        raise_application_error(-20045, 'INVALID_ELECTRIFIED_FLAG');
END IF;

    -- Segment type exists
SELECT COUNT(*) INTO v_cnt FROM SegmentType WHERE trackType = p_track_type;
IF v_cnt = 0 THEN
        raise_application_error(-20045, 'SEGMENTTYPE_NOT_FOUND');
END IF;

    -- Siding exists (if provided)
    IF p_siding_id IS NOT NULL THEN
SELECT COUNT(*) INTO v_cnt FROM Siding WHERE SIding_id = p_siding_id;
IF v_cnt = 0 THEN
            raise_application_error(-20045, 'SIDING_NOT_FOUND');
END IF;
END IF;

    -- Insert Line
INSERT INTO Line (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode)
VALUES (p_line_id, p_start_facility_id, p_end_facility_id, p_line_name, p_owner_id, p_gauge_code);

-- Insert mandatory Segment
INSERT INTO Segment (
    segment_id, line_id, length_m, segment_order, electrified_flag,
    SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id
)
VALUES (
           p_segment_id, p_line_id, p_length_m, p_segment_order, p_electrified_flag,
           p_track_number, p_track_type, p_siding_id
       );

RETURN p_line_id;

EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        raise_application_error(-20045, 'DUPLICATE_KEY');
WHEN OTHERS THEN
        raise_application_error(-20045, 'ERROR ' || SQLERRM);
END;
/
