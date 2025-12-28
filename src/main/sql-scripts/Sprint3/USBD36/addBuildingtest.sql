//sucesso

DECLARE
v_id NUMBER;
BEGIN
SAVEPOINT sp_test;

v_id := usbd36_add_building(10000, 'USBD36 Test Building OK', 1, 1, 50);
    DBMS_OUTPUT.PUT_LINE('SUCCESS: building_id=' || v_id || ' facility_id=50');

ROLLBACK TO sp_test;
END;
/


//facility nao existe
DECLARE
v_id NUMBER;
BEGIN
SAVEPOINT sp_test;

v_id := usbd36_add_building(10001, 'USBD36 Bad Facility', 1, 1, 999999);
    DBMS_OUTPUT.PUT_LINE('UNEXPECTED SUCCESS: building_id=' || v_id);

ROLLBACK TO sp_test;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('EXPECTED ERROR: ' || SQLERRM);
ROLLBACK TO sp_test;
END;
/


//building type nao existe
DECLARE
v_id NUMBER;
BEGIN
SAVEPOINT sp_test;

v_id := usbd36_add_building(10002, 'USBD36 Bad Type', 1, 999999, 50);
    DBMS_OUTPUT.PUT_LINE('UNEXPECTED SUCCESS: building_id=' || v_id);

ROLLBACK TO sp_test;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('EXPECTED ERROR: ' || SQLERRM);
ROLLBACK TO sp_test;
END;
/


//numberOfBuildings inválido
DECLARE
v_id NUMBER;
BEGIN
SAVEPOINT sp_test;

v_id := usbd36_add_building(10003, 'USBD36 Bad Number', 0, 1, 50);
    DBMS_OUTPUT.PUT_LINE('UNEXPECTED SUCCESS: building_id=' || v_id);

ROLLBACK TO sp_test;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('EXPECTED ERROR: ' || SQLERRM);
ROLLBACK TO sp_test;
END;
/


//duplicado(mesmo nome ou id)
DECLARE
v_id NUMBER;
BEGIN
SAVEPOINT sp_test;

-- Primeiro insert (ok)
v_id := usbd36_add_building(10004, 'USBD36 Duplicate Name', 1, 1, 50);
    DBMS_OUTPUT.PUT_LINE('First insert OK: building_id=' || v_id);

    -- Segundo insert com o mesmo NAME (deve falhar por UNIQUE(name))
    v_id := usbd36_add_building(10005, 'USBD36 Duplicate Name', 1, 1, 50);
    DBMS_OUTPUT.PUT_LINE('UNEXPECTED SUCCESS: building_id=' || v_id);

ROLLBACK TO sp_test;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('EXPECTED ERROR: ' || SQLERRM);
ROLLBACK TO sp_test;
END;
/

