CREATE OR REPLACE FUNCTION registFacility (
    p_facility_id       IN facility.facility_id%TYPE,
    p_facility_name     IN facility.name%TYPE,
    p_facility_type_id  IN facility.facility_typetype_id%TYPE -- Validar se é typetype mesmo
) RETURN NUMBER IS
    v_count NUMBER;
    v_clean_name facility.name%TYPE := TRIM(p_facility_name);
BEGIN

    IF v_clean_name IS NULL THEN
        RAISE_APPLICATION_ERROR(-20002, 'Erro: Nome obrigatório.');
    END IF;

    SELECT COUNT(*) INTO v_count FROM facility WHERE facility_id = p_facility_id;
    IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'Erro: ID já existente.');
    END IF;


    SELECT COUNT(*) INTO v_count FROM facility WHERE UPPER(name) = UPPER(v_clean_name);
    IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20003, 'Erro: Nome já existente.');
    END IF;


    SELECT COUNT(*) INTO v_count FROM facility_type WHERE type_id = p_facility_type_id;
    IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20004, 'Erro: ID de tipo inválido.');
    END IF;


    INSERT INTO facility (facility_id, name, facility_typetype_id)
    VALUES (p_facility_id, v_clean_name, p_facility_type_id);

    RETURN p_facility_id;

END registFacility;
/