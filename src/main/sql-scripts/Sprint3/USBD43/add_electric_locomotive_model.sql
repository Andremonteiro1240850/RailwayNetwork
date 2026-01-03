CREATE OR REPLACE FUNCTION add_electric_locomotive_model(
    p_locomotive_model_id IN LocomotiveModel.locomotive_model_id%TYPE,
    p_make                 IN LocomotiveModel.make%TYPE,
    p_power                IN LocomotiveModel.power%TYPE,
    p_acceleration         IN LocomotiveModel.acelaration%TYPE,
    p_length               IN LocomotiveModel.lenght%TYPE,
    p_weight               IN LocomotiveModel.weight%TYPE,
    p_height               IN LocomotiveModel.heigth%TYPE,
    p_max_speed            IN LocomotiveModel.max_speed%TYPE,
    p_bogies_number        IN LocomotiveModel.bogies_number%TYPE,
    p_voltage              IN Electric_Locomotive.Voltage%TYPE
) RETURN LocomotiveModel.locomotive_model_id%TYPE IS
    v_count NUMBER;
    v_clean_model_id LocomotiveModel.locomotive_model_id%TYPE := TRIM(p_locomotive_model_id);
    v_clean_make LocomotiveModel.make%TYPE := TRIM(p_make);
BEGIN

    -- 1. Validar locomotive_model_id (não pode ser NULL ou vazio)
    IF v_clean_model_id IS NULL THEN
        RAISE_APPLICATION_ERROR(-20001, 'Erro: ID do modelo obrigatório.');
    END IF;

    -- 2. Validar make (não pode ser NULL ou vazio)
    IF v_clean_make IS NULL THEN
        RAISE_APPLICATION_ERROR(-20002, 'Erro: Fabricante (make) obrigatório.');
    END IF;

    -- 3. Validar power (deve ser maior que 0)
    IF p_power IS NULL OR p_power <= 0 THEN
        RAISE_APPLICATION_ERROR(-20003, 'Erro: Potência deve ser maior que zero.');
    END IF;

    -- 4. Validar acceleration (deve ser maior que 0)
    IF p_acceleration IS NULL OR p_acceleration <= 0 THEN
        RAISE_APPLICATION_ERROR(-20004, 'Erro: Aceleração deve ser maior que zero.');
    END IF;

    -- 5. Validar length (deve ser maior que 0)
    IF p_length IS NULL OR p_length <= 0 THEN
        RAISE_APPLICATION_ERROR(-20005, 'Erro: Comprimento deve ser maior que zero.');
    END IF;

    -- 6. Validar weight (deve ser maior que 0)
    IF p_weight IS NULL OR p_weight <= 0 THEN
        RAISE_APPLICATION_ERROR(-20006, 'Erro: Peso deve ser maior que zero.');
    END IF;

    -- 7. Validar height (deve ser maior que 0)
    IF p_height IS NULL OR p_height <= 0 THEN
        RAISE_APPLICATION_ERROR(-20007, 'Erro: Altura deve ser maior que zero.');
    END IF;

    -- 8. Validar max_speed (deve ser maior que 0)
    IF p_max_speed IS NULL OR p_max_speed <= 0 THEN
        RAISE_APPLICATION_ERROR(-20008, 'Erro: Velocidade máxima deve ser maior que zero.');
    END IF;

    -- 9. Validar bogies_number (deve ser maior que 0)
    IF p_bogies_number IS NULL OR p_bogies_number <= 0 THEN
        RAISE_APPLICATION_ERROR(-20009, 'Erro: Número de bogies deve ser maior que zero.');
    END IF;

    -- 10. Validar voltage (deve ser maior que 0)
    IF p_voltage IS NULL OR p_voltage <= 0 THEN
        RAISE_APPLICATION_ERROR(-20010, 'Erro: Tensão (voltage) deve ser maior que zero.');
    END IF;

    -- 11. Verificar se o modelo já existe
    SELECT COUNT(*) INTO v_count 
    FROM LocomotiveModel 
    WHERE locomotive_model_id = v_clean_model_id;

    IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20011, 'Erro: ID do modelo já existente.');
    END IF;

    -- 12. Inserir na tabela LocomotiveModel
    INSERT INTO LocomotiveModel (
        locomotive_model_id,
        power,
        acelaration,
        make,
        lenght,
        weight,
        heigth,
        max_speed,
        bogies_number
    ) VALUES (
        v_clean_model_id,
        p_power,
        p_acceleration,
        v_clean_make,
        p_length,
        p_weight,
        p_height,
        p_max_speed,
        p_bogies_number
    );

    -- 13. Inserir na tabela Electric_Locomotive (especialização)
    INSERT INTO Electric_Locomotive (
        LocomotiveModellocomotive_model_id,
        Voltage
    ) VALUES (
        v_clean_model_id,
        p_voltage
    );

    RETURN v_clean_model_id;

END add_electric_locomotive_model;
/