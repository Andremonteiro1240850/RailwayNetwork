CREATE OR REPLACE FUNCTION remove_freight_from_train (
    p_freight_id      IN Freight.freight_id%TYPE,
    p_data_referencia IN DATE DEFAULT SYSDATE
) RETURN NUMBER IS
    v_count          NUMBER;
    v_train_id       FreighTrain.Traintrain_id%TYPE;
    v_departure_time NUMBER;
    v_route_date     DATE;
    v_current_time   NUMBER;
BEGIN

    SELECT COUNT(*) INTO v_count FROM Freight WHERE freight_id = p_freight_id;
    IF v_count = 0 THEN
        raise_application_error(-20001, 'Erro: ID de carga inválido');
    END IF;


    BEGIN
        SELECT ft.Traintrain_id, tr.departure_time, TRUNC(tr.RouteDate)
        INTO v_train_id, v_departure_time, v_route_date
        FROM FreighTrain ft
                 JOIN Train_route tr ON ft.Traintrain_id = tr.Train_id
        WHERE ft.Freightfreight_id = p_freight_id
          AND tr."Order" = 1; -- Valida o ponto de partida
    EXCEPTION
        WHEN NO_DATA_FOUND THEN

            SELECT COUNT(*) INTO v_count FROM FreighTrain WHERE Freightfreight_id = p_freight_id;
            IF v_count = 0 THEN
                raise_application_error(-20002, 'Erro: não há nenhum comboio a carregar esta carga');
            ELSE
                raise_application_error(-20004, 'Erro: Rota não encontrada para o comboio.');
            END IF;
    END;

    v_current_time := TO_NUMBER(TO_CHAR(p_data_referencia, 'HH24MI'));

    IF v_route_date < TRUNC(p_data_referencia) THEN
        raise_application_error(-20003, 'Erro: O comboio já partiu em data passada');
    ELSIF v_route_date = TRUNC(p_data_referencia) AND v_departure_time <= v_current_time THEN
        raise_application_error(-20003, 'Erro: O comboio de hoje já partiu ou está em trânsito');
    END IF;

    DELETE FROM FreighTrain
    WHERE Freightfreight_id = p_freight_id
      AND Traintrain_id = v_train_id;

    RETURN p_freight_id;
END remove_freight_from_train;