CREATE OR REPLACE FUNCTION usbd42_register_train_movement (
    p_train_id      IN Train.train_id%TYPE,
    p_operator_id   IN Train.operator_id%TYPE,
    p_arrival_ts    IN TrainLog.Arrival%TYPE,
    p_departure_ts  IN TrainLogDeparture.Departure%TYPE DEFAULT NULL
) RETURN NUMBER
IS
    /* canonical operator from DB */
    v_operator_id   Train.operator_id%TYPE;

    /* validations */
    v_train_exists      NUMBER;
    v_movement_exists   NUMBER;

    /* Custom exceptions (estilo colegas) */
    ex_missing_params          EXCEPTION; -- -20002
    ex_arrival_missing         EXCEPTION; -- -20003
    ex_train_not_found         EXCEPTION; -- -20001
    ex_invalid_chronology      EXCEPTION; -- -20004
    ex_movement_already_exists EXCEPTION; -- -20005
BEGIN
SAVEPOINT sp_usbd42_register_train_movement;

/* ===========================================
   VALIDATIONS
   =========================================== */

-- 1) Mandatory parameters
IF p_train_id IS NULL OR p_operator_id IS NULL THEN
        RAISE ex_missing_params;
END IF;

    -- 2) Arrival mandatory
    IF p_arrival_ts IS NULL THEN
        RAISE ex_arrival_missing;
END IF;

    -- 3) Train exists (case + spaces insensitive)
SELECT COUNT(*)
INTO v_train_exists
FROM Train
WHERE train_id = p_train_id
  AND UPPER(TRIM(operator_id)) = UPPER(TRIM(p_operator_id));

IF v_train_exists = 0 THEN
        RAISE ex_train_not_found;
END IF;


SELECT operator_id
INTO v_operator_id
FROM Train
WHERE train_id = p_train_id
  AND UPPER(TRIM(operator_id)) = UPPER(TRIM(p_operator_id));

-- 4) Chronology: if stopped, departure >= arrival
IF p_departure_ts IS NOT NULL AND p_departure_ts < p_arrival_ts THEN
        RAISE ex_invalid_chronology;
END IF;

    -- 5) Movement already registered? (TrainLog PK is Traintrain_id)
SELECT COUNT(*)
INTO v_movement_exists
FROM TrainLog
WHERE Traintrain_id = p_train_id;

IF v_movement_exists > 0 THEN
        RAISE ex_movement_already_exists;
END IF;

    /* ===========================================
       TRANSACTION
       =========================================== */

INSERT INTO TrainLog (Traintrain_id, Trainoperator_id, Arrival, TrainLogTraintrain_id)
VALUES (p_train_id, v_operator_id, p_arrival_ts, p_train_id);

IF p_departure_ts IS NOT NULL THEN
        INSERT INTO TrainLogDeparture (TrainLogTraintrain_id, Departure)
        VALUES (p_train_id, p_departure_ts);
END IF;

RETURN 1;

EXCEPTION
    WHEN ex_train_not_found THEN
        ROLLBACK TO sp_usbd42_register_train_movement;
        RAISE_APPLICATION_ERROR(
            -20001,
            'USBD42: Train not found (train_id=' || p_train_id ||
            ', operator_id=' || TRIM(p_operator_id) || ').'
        );
RETURN 0;

WHEN ex_missing_params THEN
        ROLLBACK TO sp_usbd42_register_train_movement;
        RAISE_APPLICATION_ERROR(-20002, 'USBD42: train_id and operator_id are mandatory.');
RETURN 0;

WHEN ex_arrival_missing THEN
        ROLLBACK TO sp_usbd42_register_train_movement;
        RAISE_APPLICATION_ERROR(-20003, 'USBD42: arrival timestamp is mandatory.');
RETURN 0;

WHEN ex_invalid_chronology THEN
        ROLLBACK TO sp_usbd42_register_train_movement;
        RAISE_APPLICATION_ERROR(-20004, 'USBD42: Invalid chronology (departure < arrival).');
RETURN 0;

WHEN ex_movement_already_exists THEN
        ROLLBACK TO sp_usbd42_register_train_movement;
        RAISE_APPLICATION_ERROR(
            -20005,
            'USBD42: Movement already registered for train_id=' || p_train_id || '.'
        );
RETURN 0;

WHEN OTHERS THEN
        ROLLBACK TO sp_usbd42_register_train_movement;
        RAISE_APPLICATION_ERROR(
            -20999,
            'USBD42: Unexpected error while registering movement: ' || SQLERRM
        );
RETURN 0;
END usbd42_register_train_movement;
/
