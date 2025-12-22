
CREATE OR REPLACE FUNCTION registFacility (
    p_facility_id       IN facility.facility_id%TYPE,
    p_facility_name     IN facility.name%TYPE,
    p_facility_type_id  IN facility.facility_typetype_id%TYPE
) RETURN NUMBER IS
    v_count NUMBER;

BEGIN

    IF p_facility_name IS NULL OR TRIM(p_facility_name) IS NULL THEN
        RAISE_APPLICATION_ERROR(-20002, 'Erro: Nome obrigatório.');
END IF;

SELECT COUNT(*) INTO v_count FROM facility WHERE facility_id = p_facility_id;
IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'Erro: ID já existente.');
END IF;

SELECT COUNT(*) INTO v_count FROM facility WHERE name = TRIM(p_facility_name);
IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20003, 'Erro: Nome já existente.');
END IF;

select count(*) into v_count from facility_type where type_id = p_facility_type_id;
if v_count = 0 then
   RAISE_APPLICATION_ERROR(-20004, 'Erro: ID de tipo inválido.');
end if;

INSERT INTO facility (facility_id, name, facility_typetype_id)
VALUES (p_facility_id, TRIM(p_facility_name), p_facility_type_id);

RETURN p_facility_id;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('Erro técnico: ' || SQLERRM);
        RAISE;
END registFacility;
/