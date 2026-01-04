CREATE OR REPLACE FUNCTION associate_locomotive_with_train(
    p_locomotive_id IN NUMBER,
    p_train_id IN NUMBER,
    p_operator_id IN VARCHAR2
) RETURN NUMBER IS
    -- Variáveis para validação
    v_train_exists NUMBER;
    v_locomotive_operator VARCHAR2(255);
    v_association_exists NUMBER;

    -- Exceções personalizadas
    locomotive_not_found EXCEPTION;
    train_not_found EXCEPTION;
    operator_mismatch EXCEPTION;
    association_already_exists EXCEPTION;

BEGIN
    -- 1. Verificar se a locomotiva existe
BEGIN
SELECT operator_id
INTO v_locomotive_operator
FROM Locomotive
WHERE locomotive_id = p_locomotive_id;
EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE locomotive_not_found;
END;

    -- 2. Verificar se o comboio existe com o operador especificado
SELECT COUNT(*)
INTO v_train_exists
FROM Train
WHERE train_id = p_train_id
  AND operator_id = p_operator_id;

IF v_train_exists = 0 THEN
        RAISE train_not_found;
END IF;

    -- 3. Verificar se o operador da locomotiva corresponde ao operador fornecido
    IF v_locomotive_operator != p_operator_id THEN
        RAISE operator_mismatch;
END IF;

    -- 4. Verificar se a associação já existe
SELECT COUNT(*)
INTO v_association_exists
FROM Locomotive_Train
WHERE Locomotivelocomotive_id = p_locomotive_id
  AND Traintrain_id = p_train_id
  AND Trainoperator_id = p_operator_id;

IF v_association_exists > 0 THEN
        RAISE association_already_exists;
END IF;

    -- 5. Inserir a associação entre locomotiva e comboio
INSERT INTO Locomotive_Train (
    Locomotivelocomotive_id,
    Traintrain_id,
    Trainoperator_id,
    Adddate
) VALUES (
             p_locomotive_id,
             p_train_id,
             p_operator_id,
             SYSTIMESTAMP
         );

RETURN p_locomotive_id; -- Sucesso

EXCEPTION
    WHEN locomotive_not_found THEN
        RAISE_APPLICATION_ERROR(-20001, 'Locomotiva com ID ' || p_locomotive_id || ' não encontrada.');
        RETURN NULL;

    WHEN train_not_found THEN
        RAISE_APPLICATION_ERROR(-20002, 'Comboio com ID ' || p_train_id || ' e operador ' || p_operator_id || ' não encontrado.');
        RETURN NULL;

    WHEN operator_mismatch THEN
        RAISE_APPLICATION_ERROR(-20003, 'A locomotiva pertence ao operador ' || v_locomotive_operator || ' mas foi especificado o operador ' || p_operator_id || '.');
        RETURN NULL;

    WHEN association_already_exists THEN
        RAISE_APPLICATION_ERROR(-20004, 'A locomotiva ' || p_locomotive_id || ' já está associada ao comboio ' || p_train_id || '.');
        RETURN NULL;

    WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20999, 'Erro inesperado ao associar locomotiva ao comboio: ' || SQLERRM);
        RETURN NULL;

END associate_locomotive_with_train;
/