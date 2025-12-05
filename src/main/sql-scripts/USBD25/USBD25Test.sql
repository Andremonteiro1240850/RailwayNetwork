SET SERVEROUTPUT ON;

DECLARE
cur_result      SYS_REFCURSOR;

    facility_id     facility.facility_id%TYPE;
    facility_name   facility.name%TYPE;
    facility_order  train_route."ORDER"%TYPE;
BEGIN
    cur_result := FUNCTION_USBD25_LIST_FACILITIES_WITHOUT_STOP;

    LOOP
FETCH cur_result INTO
            facility_id,
            facility_name,
            facility_order;

        EXIT WHEN cur_result%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE(
              'FACILITY_ID = ' || facility_id
           || ' | NAME = '     || facility_name
           || ' | ORDER = '    || facility_order
        );
END LOOP;

CLOSE cur_result;

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Erro: ' || SQLCODE || ' - ' || SQLERRM);
END;
/
