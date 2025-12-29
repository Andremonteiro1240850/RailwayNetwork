CREATE OR REPLACE FUNCTION add_electric_locomotive_model (
    p_locomotive_model_id IN LocomotiveModel.locomotive_model_id%TYPE,
    p_power               IN LocomotiveModel.power%TYPE,
    p_acelaration         IN LocomotiveModel.acelaration%TYPE,
    p_make                IN LocomotiveModel.make%TYPE,
    p_lenght              IN LocomotiveModel.lenght%TYPE,
    p_weight              IN LocomotiveModel.weight%TYPE,
    p_heigth              IN LocomotiveModel.heigth%TYPE,
    p_max_speed           IN LocomotiveModel.max_speed%TYPE,
    p_bogies_number       IN LocomotiveModel.bogies_number%TYPE,
    p_voltage              IN Electric_Locomotive.Voltage%TYPE
) RETURN VARCHAR2 IS
    v_count NUMBER;

BEGIN
    -- 1. Validar campos obrigatórios
    IF p_locomotive_model_id IS NULL OR TRIM(p_locomotive_model_id) IS NULL THEN
        RAISE_APPLICATION_ERROR(-20001, 'Erro: ID do modelo obrigatório.');
    END IF;

    IF p_make IS NULL OR TRIM(p_make) IS NULL THEN
        RAISE_APPLICATION_ERROR(-20002, 'Erro: Fabricante (make) obrigatório.');
    END IF;

    -- 2. Validar valores numéricos positivos
    IF p_power <= 0 THEN
        RAISE_APPLICATION_ERROR(-20003, 'Erro: Potência deve ser maior que zero.');
    END IF;

    IF p_acelaration <= 0 THEN
        RAISE_APPLICATION_ERROR(-20004, 'Erro: Aceleração deve ser maior que zero.');
    END IF;

    IF p_lenght <= 0 THEN
        RAISE_APPLICATION_ERROR(-20005, 'Erro: Comprimento deve ser maior que zero.');
    END IF;

    IF p_weight <= 0 THEN
        RAISE_APPLICATION_ERROR(-20006, 'Erro: Peso deve ser maior que zero.');
    END IF;

    IF p_heigth <= 0 THEN
        RAISE_APPLICATION_ERROR(-20007, 'Erro: Altura deve ser maior que zero.');
    END IF;

    IF p_max_speed <= 0 THEN
        RAISE_APPLICATION_ERROR(-20008, 'Erro: Velocidade máxima deve ser maior que zero.');
    END IF;

    IF p_bogies_number <= 0 THEN
        RAISE_APPLICATION_ERROR(-20009, 'Erro: Número de bogies deve ser maior que zero.');
    END IF;

    IF p_voltage <= 0 THEN
        RAISE_APPLICATION_ERROR(-20010, 'Erro: Tensão (voltage) deve ser maior que zero.');
    END IF;

    -- 3. Verificar se o ID já existe
    SELECT COUNT(*) INTO v_count 
    FROM LocomotiveModel 
    WHERE locomotive_model_id = TRIM(p_locomotive_model_id);
    
    IF v_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20011, 'Erro: ID do modelo já existente.');
    END IF;

    -- 4. Inserir na tabela LocomotiveModel
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
        TRIM(p_locomotive_model_id),
        p_power,
        p_acelaration,
        TRIM(p_make),
        p_lenght,
        p_weight,
        p_heigth,
        p_max_speed,
        p_bogies_number
    );

    -- 5. Inserir na tabela Electric_Locomotive
    INSERT INTO Electric_Locomotive (
        LocomotiveModellocomotive_model_id,
        Voltage
    ) VALUES (
        TRIM(p_locomotive_model_id),
        p_voltage
    );

    RETURN TRIM(p_locomotive_model_id);

EXCEPTION
    WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20999, 'Erro técnico ao adicionar modelo de locomotiva elétrica: ' || SQLERRM);
        RETURN NULL;
END add_electric_locomotive_model;
/

