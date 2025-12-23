CREATE OR REPLACE FUNCTION associate_locomotive_with_train(
    p_locomotive_id IN NUMBER,
    p_train_id IN NUMBER,
    p_operator_id IN VARCHAR2
) RETURN NUMBER IS
    -- Variáveis para validação
    v_locomotive_exists NUMBER;
    v_train_exists NUMBER;
    v_operator_match NUMBER;
    v_association_exists NUMBER;
    v_gauge_compatible NUMBER;
    v_locomotive_operator VARCHAR2(255);
    v_train_operator VARCHAR2(255);

    -- Exceções personalizadas
    locomotive_not_found EXCEPTION;
    train_not_found EXCEPTION;
    operator_mismatch EXCEPTION;
    association_already_exists EXCEPTION;
    gauge_incompatible EXCEPTION;

BEGIN
    -- Verificar se a locomotiva existe
SELECT COUNT(*)
INTO v_locomotive_exists
FROM Locomotive
WHERE locomotive_id = p_locomotive_id;

IF v_locomotive_exists = 0 THEN
        RAISE locomotive_not_found;
END IF;

    -- Verificar se o comboio existe
SELECT COUNT(*)
INTO v_train_exists
FROM Train
WHERE train_id = p_train_id
  AND operator_id = p_operator_id;

IF v_train_exists = 0 THEN
        RAISE train_not_found;
END IF;

    -- Obter o operador da locomotiva
SELECT operator_id
INTO v_locomotive_operator
FROM Locomotive
WHERE locomotive_id = p_locomotive_id;

-- Obter o operador do comboio
SELECT operator_id
INTO v_train_operator
FROM Train
WHERE train_id = p_train_id
  AND operator_id = p_operator_id;

-- Verificar se o operador da locomotiva corresponde ao operador do comboio
IF v_locomotive_operator != v_train_operator THEN
        RAISE operator_mismatch;
END IF;

    -- Verificar se a associação já existe
SELECT COUNT(*)
INTO v_association_exists
FROM Locomotive_Train
WHERE Locomotivelocomotive_id = p_locomotive_id
  AND Traintrain_id = p_train_id
  AND Trainoperator_id = p_operator_id;

IF v_association_exists > 0 THEN
        RAISE association_already_exists;
END IF;

    -- Verificar compatibilidade de bitola (gauge)
    -- Uma locomotiva pode ser associada se existir pelo menos uma bitola comum
SELECT COUNT(*)
INTO v_gauge_compatible
FROM Locomotive_Gauge lg
         INNER JOIN Line l ON lg.Gaugecode = l.Gaugecode
WHERE lg.Locomotivelocomotive_id = p_locomotive_id;

IF v_gauge_compatible = 0 THEN
        RAISE gauge_incompatible;
END IF;

    -- Inserir a associação
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

COMMIT;

RETURN 1; -- Sucesso

EXCEPTION
    WHEN locomotive_not_found THEN
        RAISE_APPLICATION_ERROR(-20001, 'Locomotiva não encontrada com o ID: ' || p_locomotive_id);
RETURN 0;

WHEN train_not_found THEN
        RAISE_APPLICATION_ERROR(-20002, 'Comboio não encontrado com o ID: ' || p_train_id || ' e operador: ' || p_operator_id);
RETURN 0;

WHEN operator_mismatch THEN
        RAISE_APPLICATION_ERROR(-20003, 'O operador da locomotiva (' || v_locomotive_operator || ') não corresponde ao operador do comboio (' || v_train_operator || ')');
RETURN 0;

WHEN association_already_exists THEN
        RAISE_APPLICATION_ERROR(-20004, 'A locomotiva ' || p_locomotive_id || ' já está associada ao comboio ' || p_train_id);
RETURN 0;

WHEN gauge_incompatible THEN
        RAISE_APPLICATION_ERROR(-20005, 'A locomotiva não tem bitola compatível com as linhas disponíveis');
RETURN 0;

WHEN OTHERS THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20999, 'Erro inesperado: ' || SQLERRM);
RETURN 0;

END associate_locomotive_with_train;
/