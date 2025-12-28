//sucesso

DECLARE
v_line_id NUMBER;
BEGIN
SAVEPOINT sp_test;

v_line_id := usbd45_add_line(
        9000, 50, 12, 'Test Line OK', 'IP', 1668,
        90000, 3000, 90000, 'Y', 2, 'DOUBLE', NULL
    );

    DBMS_OUTPUT.PUT_LINE('SUCCESS: line_id=' || v_line_id);

ROLLBACK TO sp_test;
END;
/


//terminais invalidos
DECLARE
v_line_id NUMBER;
BEGIN
SAVEPOINT sp_test;

v_line_id := usbd45_add_line(
        9001, 50, 50, 'Bad Terminals', 'IP', 1668,
        90001, 3000, 90001, 'Y', 2, 'DOUBLE', NULL
    );

    DBMS_OUTPUT.PUT_LINE('UNEXPECTED SUCCESS: line_id=' || v_line_id);
ROLLBACK TO sp_test;

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('EXPECTED ERROR: ' || SQLERRM);
ROLLBACK TO sp_test;
END;
/


//owner nao existe
DECLARE
v_line_id NUMBER;
BEGIN
SAVEPOINT sp_test;

v_line_id := usbd45_add_line(
        9002, 50, 12, 'Bad Owner', 'NOPE', 1668,
        90002, 3000, 90002, 'Y', 2, 'DOUBLE', NULL
    );

    DBMS_OUTPUT.PUT_LINE('UNEXPECTED SUCCESS: line_id=' || v_line_id);
ROLLBACK TO sp_test;

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('EXPECTED ERROR: ' || SQLERRM);
ROLLBACK TO sp_test;
END;
/


//segmento inválido

DECLARE
v_line_id NUMBER;
BEGIN
SAVEPOINT sp_test;

v_line_id := usbd45_add_line(
        9003, 50, 12, 'Bad Segment', 'IP', 1668,
        90003, 0, 90003, 'Y', 2, 'DOUBLE', NULL
    );

    DBMS_OUTPUT.PUT_LINE('UNEXPECTED SUCCESS: line_id=' || v_line_id);
ROLLBACK TO sp_test;

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('EXPECTED ERROR: ' || SQLERRM);
ROLLBACK TO sp_test;
END;
/


//nome da linha duplicado

DECLARE
v_line_id NUMBER;
BEGIN
SAVEPOINT sp_test;

v_line_id := usbd45_add_line(
        9004, 50, 12, 'Duplicate Line', 'IP', 1668,
        90004, 3000, 90004, 'Y', 2, 'DOUBLE', NULL
    );
    DBMS_OUTPUT.PUT_LINE('First insert OK: line_id=' || v_line_id);

    v_line_id := usbd45_add_line(
        9005, 50, 12, 'Duplicate Line', 'IP', 1668,
        90005, 3000, 90005, 'Y', 2, 'DOUBLE', NULL
    );
    DBMS_OUTPUT.PUT_LINE('UNEXPECTED SUCCESS: line_id=' || v_line_id);

ROLLBACK TO sp_test;

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('EXPECTED ERROR: ' || SQLERRM);
ROLLBACK TO sp_test;
END;
/
