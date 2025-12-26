CREATE OR REPLACE FUNCTION remove_freight_from_train (
    p_freight_id      IN Freight.freight_id%TYPE,
    p_data_referencia IN DATE DEFAULT SYSDATE
) RETURN NUMBER IS
    v_count          NUMBER;
    v_train_id       FREIGHTRAIN.TRAINTRAIN_ID%TYPE;
    v_departure_time NUMBER;
    v_route_date     DATE;
    v_current_time   NUMBER;
    v_current_date   DATE;

BEGIN

    SELECT count(*) INTO v_count FROM Freight WHERE freight_id = p_freight_id;
    IF v_count = 0 THEN
        raise_application_error(-20001, 'Erro: ID de carga inválido');
    END IF;

    SELECT count(*) INTO v_count FROM FreighTrain WHERE Freigth_id = p_freight_id;
    IF v_count = 0 THEN
        raise_application_error(-20002, 'Erro: não há nenhum comboio a carregar esta carga');
    END IF;


    SELECT FreighTrain.Traintrain_id, Train_route.departure_time, TRUNC(Train_route.routeDate)
    INTO v_train_id, v_departure_time, v_route_date
    FROM FreighTrain
             JOIN Train_route  ON FreighTrain.Traintrain_id = Train_route.Train_id
    WHERE FreighTrain.Freigth_id = p_freight_id
      AND Train_route."Order" = 1;

    v_current_date := TRUNC(p_data_referencia);
    v_current_time := TO_NUMBER(TO_CHAR(p_data_referencia, 'HH24MI'));

    IF v_route_date < v_current_date THEN
        raise_application_error(-20003, 'Erro: O comboio já partiu em data passada (' || TO_CHAR(v_route_date, 'DD/MM/YYYY') || ')');

    ELSIF v_route_date = v_current_date THEN
        IF v_departure_time <= v_current_time THEN
            raise_application_error(-20003, 'Erro: O comboio de hoje já partiu ou está em trânsito');
        END IF;
    END IF;

    DELETE FROM FreighTrain
    WHERE Freigth_id = p_freight_id
      AND Traintrain_id = v_train_id;

    RETURN p_freight_id;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        raise_application_error(-20004, 'Erro: Rota não encontrada para o comboio.');
        RETURN NULL;
    WHEN OTHERS THEN
        RAISE;
END remove_freight_from_train;
/