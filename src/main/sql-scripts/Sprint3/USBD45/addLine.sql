-- ============================================================
-- USBD45 (PPT-SAFE)
-- As a Planner, I want to add a new line. A line must have at least one segment.
--  - Sem CREATE TYPE ... AS OBJECT
--  - Sem passagem por referência (só IN)
--  - Sem argumentos nomeados (nos testes uso positional)
--  - Sem COMMIT/ROLLBACK dentro da função
--  - Padrão US36: bloco de limpeza + testes num bloco (COMMIT só no sucesso)
-- ============================================================

-- ============================================================
-- FUNÇÃO (PPT-safe)
-- ============================================================
CREATE OR REPLACE FUNCTION usbd45_add_line(
    p_line_id            IN Line.line_id%TYPE,
    p_start_facility_id  IN Facility.facility_id%TYPE,
    p_end_facility_id    IN Facility.facility_id%TYPE,
    p_line_name          IN Line.name%TYPE,
    p_owner_id           IN Owner.owner_id%TYPE,
    p_gauge_code         IN Gauge.code%TYPE,

    p_segment_id         IN Segment.segment_id%TYPE,
    p_segment_length_m   IN Segment.length_m%TYPE,
    p_segment_order      IN Segment.segment_order%TYPE,
    p_electrified_flag   IN Segment.electrified_flag%TYPE,
    p_track_number       IN SegmentType.numberOfTracks%TYPE,
    p_track_type         IN SegmentType.trackType%TYPE,
    p_siding_id          IN Siding.SIding_id%TYPE
) RETURN Line.line_id%TYPE
    IS
    v_count NUMBER;

    invalid_terminals_exception        EXCEPTION;
    start_facility_not_found_exception EXCEPTION;
    end_facility_not_found_exception   EXCEPTION;
    owner_not_found_exception          EXCEPTION;
    gauge_not_found_exception          EXCEPTION;
    invalid_segment_length_exception   EXCEPTION;
    invalid_electrified_flag_exception EXCEPTION;
    segment_type_not_found_exception   EXCEPTION;
    siding_not_found_exception         EXCEPTION;
    duplicate_key_exception            EXCEPTION;
    invalid_line_name_exception        EXCEPTION;
    invalid_segment_order_exception    EXCEPTION;

BEGIN
    -- 0) line name obrigatório
    BEGIN
        IF p_line_name IS NULL OR TRIM(p_line_name) IS NULL THEN
            RAISE invalid_line_name_exception;
        END IF;
    END;

    -- 1) terminais diferentes
    BEGIN
        IF p_start_facility_id = p_end_facility_id THEN
            RAISE invalid_terminals_exception;
        END IF;
    END;

    -- 2) start facility existe
    BEGIN
        SELECT COUNT(*) INTO v_count
        FROM Facility
        WHERE facility_id = p_start_facility_id;

        IF v_count = 0 THEN
            RAISE start_facility_not_found_exception;
        END IF;
    END;

    -- 3) end facility existe
    BEGIN
        SELECT COUNT(*) INTO v_count
        FROM Facility
        WHERE facility_id = p_end_facility_id;

        IF v_count = 0 THEN
            RAISE end_facility_not_found_exception;
        END IF;
    END;

    -- 4) owner existe
    BEGIN
        SELECT COUNT(*) INTO v_count
        FROM Owner
        WHERE owner_id = p_owner_id;

        IF v_count = 0 THEN
            RAISE owner_not_found_exception;
        END IF;
    END;

    -- 5) gauge existe
    BEGIN
        SELECT COUNT(*) INTO v_count
        FROM Gauge
        WHERE code = p_gauge_code;

        IF v_count = 0 THEN
            RAISE gauge_not_found_exception;
        END IF;
    END;

    -- 6) comprimento do segmento válido
    BEGIN
        IF p_segment_length_m IS NULL OR p_segment_length_m <= 0 THEN
            RAISE invalid_segment_length_exception;
        END IF;
    END;

    -- 6.1) ordem do segmento válida
    BEGIN
        IF p_segment_order IS NULL OR p_segment_order <= 0 THEN
            RAISE invalid_segment_order_exception;
        END IF;
    END;

    -- 7) electrified flag válida
    BEGIN
        IF p_electrified_flag NOT IN ('Y', 'N') THEN
            RAISE invalid_electrified_flag_exception;
        END IF;
    END;

    -- 8) SegmentType existe (trackType + numberOfTracks)
    BEGIN
        SELECT COUNT(*) INTO v_count
        FROM SegmentType
        WHERE trackType = p_track_type
          AND numberOfTracks = p_track_number;

        IF v_count = 0 THEN
            RAISE segment_type_not_found_exception;
        END IF;
    END;

    -- 9) Siding existe (se fornecido)
    BEGIN
        IF p_siding_id IS NOT NULL THEN
            SELECT COUNT(*) INTO v_count
            FROM Siding
            WHERE SIding_id = p_siding_id;

            IF v_count = 0 THEN
                RAISE siding_not_found_exception;
            END IF;
        END IF;
    END;

    -- 10) Duplicados por chave (antes de inserir)
    BEGIN
        SELECT COUNT(*) INTO v_count
        FROM Line
        WHERE line_id = p_line_id;

        IF v_count > 0 THEN
            RAISE duplicate_key_exception;
        END IF;

        SELECT COUNT(*) INTO v_count
        FROM Segment
        WHERE segment_id = p_segment_id;

        IF v_count > 0 THEN
            RAISE duplicate_key_exception;
        END IF;
    END;

    -- Inserir Line
    INSERT INTO Line (line_id, start_facility_id, end_facility_id, name, owner_id, Gaugecode)
    VALUES (p_line_id, p_start_facility_id, p_end_facility_id, p_line_name, p_owner_id, p_gauge_code);

    -- Inserir Segment obrigatório (se falhar, remove a Line para não ficar "solta")
    BEGIN
        INSERT INTO Segment (
            segment_id, line_id, length_m, segment_order, electrified_flag,
            SegmentTypetrackNumber, SegmentTypetrackType, SidingSIding_id
        )
        VALUES (
                   p_segment_id, p_line_id, p_segment_length_m, p_segment_order, p_electrified_flag,
                   p_track_number, p_track_type, p_siding_id
               );
    EXCEPTION
        WHEN OTHERS THEN
            DELETE FROM Line WHERE line_id = p_line_id;
            RAISE;
    END;

    RETURN p_line_id;

EXCEPTION
    WHEN invalid_terminals_exception THEN
        RAISE_APPLICATION_ERROR(-20045, 'INVALID_TERMINALS: Start and end facilities must be different.');

    WHEN start_facility_not_found_exception THEN
        RAISE_APPLICATION_ERROR(-20046, 'START_FACILITY_NOT_FOUND: Start facility does not exist.');

    WHEN end_facility_not_found_exception THEN
        RAISE_APPLICATION_ERROR(-20047, 'END_FACILITY_NOT_FOUND: End facility does not exist.');

    WHEN owner_not_found_exception THEN
        RAISE_APPLICATION_ERROR(-20048, 'OWNER_NOT_FOUND: Owner does not exist.');

    WHEN gauge_not_found_exception THEN
        RAISE_APPLICATION_ERROR(-20049, 'GAUGE_NOT_FOUND: Gauge does not exist.');

    WHEN invalid_segment_length_exception THEN
        RAISE_APPLICATION_ERROR(-20050, 'INVALID_SEGMENT_LENGTH: Segment length must be greater than 0.');

    WHEN invalid_electrified_flag_exception THEN
        RAISE_APPLICATION_ERROR(-20051, 'INVALID_ELECTRIFIED_FLAG: Electrified flag must be ''Y'' or ''N''.');

    WHEN segment_type_not_found_exception THEN
        RAISE_APPLICATION_ERROR(-20052, 'SEGMENTTYPE_NOT_FOUND: Segment type/track number does not exist.');

    WHEN siding_not_found_exception THEN
        RAISE_APPLICATION_ERROR(-20053, 'SIDING_NOT_FOUND: Siding does not exist.');

    WHEN duplicate_key_exception THEN
        RAISE_APPLICATION_ERROR(-20054, 'DUPLICATE_KEY: line_id or segment_id already exists.');

    WHEN invalid_line_name_exception THEN
        RAISE_APPLICATION_ERROR(-20056, 'INVALID_LINE_NAME: Line name is mandatory.');

    WHEN invalid_segment_order_exception THEN
        RAISE_APPLICATION_ERROR(-20057, 'INVALID_SEGMENT_ORDER: Segment order must be greater than 0.');

    WHEN DUP_VAL_ON_INDEX THEN
        RAISE_APPLICATION_ERROR(-20054, 'DUPLICATE_KEY: A record with this key already exists.');

    WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20055, 'ERROR: ' || SQLERRM);
END usbd45_add_line;
/
