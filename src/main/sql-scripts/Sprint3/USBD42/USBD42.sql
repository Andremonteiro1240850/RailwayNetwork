CREATE OR REPLACE FUNCTION usbd42_register_train_movement (
    p_train_id      IN Train.train_id%TYPE,
    p_operator_id   IN Train.operator_id%TYPE,
    p_arrival_ts    IN TrainLog.Arrival%TYPE,
    p_departure_ts  IN TrainLogDeparture.Departure%TYPE DEFAULT NULL
) RETURN NUMBER
    IS
    -- Variables for validations
    v_train_exists        NUMBER;
    v_movement_exists     NUMBER;
    v_departure_exists    NUMBER;

    -- Custom exceptions (estilo colegas)
    missing_params            EXCEPTION;
    arrival_missing           EXCEPTION;
    train_not_found           EXCEPTION;
    invalid_chronology        EXCEPTION;
    movement_already_exists   EXCEPTION;
    departure_already_exists  EXCEPTION;

BEGIN
    /* ===========================================
       VALIDATIONS
       =========================================== */

    -- 1) Mandatory parameters
    IF p_train_id IS NULL OR p_operator_id IS NULL THEN
        RAISE missing_params;
END IF;

    -- 2) Arrival mandatory
    IF p_arrival_ts IS NULL THEN
        RAISE arrival_missing;
END IF;

    -- 3) Train exists
SELECT COUNT(*)
INTO v_train_exists
FROM Train
WHERE train_id    = p_train_id
  AND operator_id = p_operator_id;

IF v_train_exists = 0 THEN
        RAISE train_not_found;
END IF;

    -- 4) Chronology: if stopped, departure >= arrival
    IF p_departure_ts IS NOT NULL AND p_departure_ts < p_arrival_ts THEN
        RAISE invalid_chronology;
END IF;

    -- 5) Movement already registered? (TrainLog PK is Traintrain_id)
SELECT COUNT(*)
INTO v_movement_exists
FROM TrainLog
WHERE Traintrain_id = p_train_id;

IF v_movement_exists > 0 THEN
        RAISE movement_already_exists;
END IF;

    -- 6) If departure provided, ensure not already registered
    IF p_departure_ts IS NOT NULL THEN
SELECT COUNT(*)
INTO v_departure_exists
FROM TrainLogDeparture
WHERE TrainLogTraintrain_id = p_train_id;

IF v_departure_exists > 0 THEN
            RAISE departure_already_exists;
END IF;
END IF;

INSERT INTO TrainLog (Traintrain_id, Trainoperator_id, Arrival, TrainLogTraintrain_id)
VALUES (p_train_id, p_operator_id, p_arrival_ts, p_train_id);

IF p_departure_ts IS NOT NULL THEN
        INSERT INTO TrainLogDeparture (TrainLogTraintrain_id, Departure)
        VALUES (p_train_id, p_departure_ts);
END IF;

COMMIT;
RETURN 1;

EXCEPTION
    WHEN missing_params THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20002, 'USBD42: train_id and operator_id are mandatory.');
RETURN 0;

WHEN arrival_missing THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20003, 'USBD42: arrival timestamp is mandatory.');
RETURN 0;

WHEN train_not_found THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20001, 'USBD42: Train not found (train_id=' || p_train_id ||
                                        ', operator_id=' || p_operator_id || ').');
RETURN 0;

WHEN invalid_chronology THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20004, 'USBD42: Invalid chronology (departure < arrival).');
RETURN 0;

WHEN movement_already_exists THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20005, 'USBD42: Movement already registered for train_id=' || p_train_id || '.');
RETURN 0;

WHEN departure_already_exists THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20006, 'USBD42: Departure already registered for train_id=' || p_train_id || '.');
RETURN 0;

WHEN OTHERS THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20999, 'USBD42: Unexpected error while registering movement: ' || SQLERRM);
RETURN 0;
END usbd42_register_train_movement;
/