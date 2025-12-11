DECLARE
rc SYS_REFCURSOR;
    v_LOCOMOTIVE_ID       LOCOMOTIVE.LOCOMOTIVE_ID%TYPE;
    v_OPERATOR_ID         LOCOMOTIVE.OPERATOR_ID%TYPE;
    v_MODEL_ID            LOCOMOTIVE.LOCOMOTIVE_MODEL_ID%TYPE;
    v_GAUGECODE           LOCOMOTIVE.GAUGECODE%TYPE;
BEGIN

    rc := getLocomotivesWithMultipleGauges;


    LOOP
FETCH rc INTO v_LOCOMOTIVE_ID, v_OPERATOR_ID, v_MODEL_ID, v_GAUGECODE;
        EXIT WHEN rc%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE(
            'LOCOMOTIVE_ID = ' || v_LOCOMOTIVE_ID ||
            ', OPERATOR = '    || v_OPERATOR_ID ||
            ', MODEL = '       || v_MODEL_ID ||
            ', GAUGE = '       || v_GAUGECODE
        );
END LOOP;

CLOSE rc;
END;
/

