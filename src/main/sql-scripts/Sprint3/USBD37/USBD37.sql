CREATE OR REPLACE FUNCTION fn_register_wagon(
    p_wagon_id        IN Wagon.wagon_id%TYPE DEFAULT NULL,
    p_operator_id     IN Wagon.operator_id%TYPE,
    p_service_year    IN Wagon.service_year%TYPE,
    p_wagon_model_id  IN Wagon.WagonModelmodelID%TYPE,
    p_gauge_codes     IN SYS.ODCINUMBERLIST
) RETURN NUMBER
    IS
    v_wagon_id     Wagon.wagon_id%TYPE;
    v_operator_db  Operator.operator_id%TYPE;  -- operator_id canónico da BD
    v_count        NUMBER;


    TYPE t_seen IS TABLE OF NUMBER INDEX BY VARCHAR2(50);
    v_seen t_seen;

    /* Custom exceptions */
    ex_missing_params          EXCEPTION; -- -20001
    ex_missing_gauges          EXCEPTION; -- -20013
    ex_operator_not_found      EXCEPTION; -- -20011
    ex_operator_ambiguous      EXCEPTION; -- -20018
    ex_model_not_found         EXCEPTION; -- -20010
    ex_wagon_exists            EXCEPTION; -- -20012
    ex_invalid_wagon_id        EXCEPTION; -- -20019
    ex_null_gauge              EXCEPTION; -- -20014
    ex_gauge_not_found         EXCEPTION; -- -20015
    ex_duplicate_gauge_input   EXCEPTION; -- -20016
    ex_invalid_year            EXCEPTION; -- -20017

    v_bad_gauge NUMBER;
BEGIN
SAVEPOINT sp_fn_register_wagon;

/* =========================
   VALIDATIONS
   ========================= */

IF p_operator_id IS NULL OR p_service_year IS NULL OR p_wagon_model_id IS NULL THEN
        RAISE ex_missing_params;
END IF;

    IF p_service_year < 1800 OR p_service_year > 2100 THEN
        RAISE ex_invalid_year;
END IF;

    IF p_gauge_codes IS NULL OR p_gauge_codes.COUNT = 0 THEN
        RAISE ex_missing_gauges;
END IF;


BEGIN
SELECT operator_id
INTO v_operator_db
FROM Operator
WHERE UPPER(TRIM(operator_id)) = UPPER(TRIM(p_operator_id));
EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE ex_operator_not_found;
WHEN TOO_MANY_ROWS THEN
            RAISE ex_operator_ambiguous;
END;


SELECT COUNT(*)
INTO v_count
FROM WagonModel
WHERE modelID = p_wagon_model_id;

IF v_count = 0 THEN
        RAISE ex_model_not_found;
END IF;


    IF p_wagon_id IS NOT NULL THEN
        IF p_wagon_id <= 0 THEN
            RAISE ex_invalid_wagon_id;
END IF;

SELECT COUNT(*)
INTO v_count
FROM Wagon
WHERE wagon_id = p_wagon_id;

IF v_count > 0 THEN
            RAISE ex_wagon_exists;
END IF;

        v_wagon_id := p_wagon_id;
ELSE
        /* geração “manual” (sem sequences) */
SELECT NVL(MAX(wagon_id), 0) + 1
INTO v_wagon_id
FROM Wagon;
END IF;

    /* =========================
       TRANSACTION
       ========================= */


INSERT INTO Wagon (wagon_id, operator_id, service_year, WagonModelmodelID)
VALUES (v_wagon_id, v_operator_db, p_service_year, p_wagon_model_id);

FOR i IN 1 .. p_gauge_codes.COUNT LOOP
            IF p_gauge_codes(i) IS NULL THEN
                RAISE ex_null_gauge;
END IF;

            /* detetar repetidas no próprio input */
            IF v_seen.EXISTS(TO_CHAR(p_gauge_codes(i))) THEN
                v_bad_gauge := p_gauge_codes(i);
                RAISE ex_duplicate_gauge_input;
END IF;
            v_seen(TO_CHAR(p_gauge_codes(i))) := 1;

            /* validar gauge existe */
SELECT COUNT(*)
INTO v_count
FROM Gauge
WHERE code = p_gauge_codes(i);

IF v_count = 0 THEN
                v_bad_gauge := p_gauge_codes(i);
                RAISE ex_gauge_not_found;
END IF;

            /* criar ligação */
BEGIN
INSERT INTO Gauge_Wagon (Gaugecode, Wagonwagon_id)
VALUES (p_gauge_codes(i), v_wagon_id);
EXCEPTION
                WHEN DUP_VAL_ON_INDEX THEN
                    NULL; -- se por algum motivo já existir, ignorar
END;
END LOOP;

RETURN v_wagon_id;

EXCEPTION
    WHEN ex_missing_params THEN
        ROLLBACK TO sp_fn_register_wagon;
        RAISE_APPLICATION_ERROR(-20001, 'Missing required parameters.');
RETURN NULL;

WHEN ex_invalid_year THEN
        ROLLBACK TO sp_fn_register_wagon;
        RAISE_APPLICATION_ERROR(-20017, 'Invalid service year: ' || p_service_year || '.');
RETURN NULL;

WHEN ex_missing_gauges THEN
        ROLLBACK TO sp_fn_register_wagon;
        RAISE_APPLICATION_ERROR(-20013, 'At least one gauge must be provided.');
RETURN NULL;

WHEN ex_operator_not_found THEN
        ROLLBACK TO sp_fn_register_wagon;
        RAISE_APPLICATION_ERROR(-20011, 'Operator does not exist: ' || p_operator_id);
RETURN NULL;

WHEN ex_model_not_found THEN
        ROLLBACK TO sp_fn_register_wagon;
        RAISE_APPLICATION_ERROR(-20010, 'Wagon model does not exist: ' || p_wagon_model_id);
RETURN NULL;

WHEN ex_wagon_exists THEN
        ROLLBACK TO sp_fn_register_wagon;
        RAISE_APPLICATION_ERROR(-20012, 'Wagon already exists with ID: ' || p_wagon_id);
RETURN NULL;

WHEN ex_null_gauge THEN
        ROLLBACK TO sp_fn_register_wagon;
        RAISE_APPLICATION_ERROR(-20014, 'Gauge code cannot be NULL.');
RETURN Null;

WHEN ex_gauge_not_found THEN
        ROLLBACK TO sp_fn_register_wagon;
        RAISE_APPLICATION_ERROR(-20015, 'Gauge does not exist: ' || v_bad_gauge);
RETURN NULL;

WHEN ex_duplicate_gauge_input THEN
        ROLLBACK TO sp_fn_register_wagon;
        RAISE_APPLICATION_ERROR(-20016, 'Duplicate gauge code in input list: ' || v_bad_gauge);
RETURN NULL;

WHEN OTHERS THEN
        ROLLBACK TO sp_fn_register_wagon;
        RAISE_APPLICATION_ERROR(-20999, 'Unexpected error in fn_register_wagon: ' || SQLERRM);
RETURN NULL;
END;
/

