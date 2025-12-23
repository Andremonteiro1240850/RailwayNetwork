
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- apagar dados dos testes ---');

    DELETE FROM facility WHERE facility_id BETWEEN 9000 AND 9999;
    COMMIT;
    DBMS_OUTPUT.PUT_LINE('concluído com sucesso.');
END;
/


DECLARE
    v_id facility.facility_id%TYPE;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 1: Inserção Válida ---');
    v_id := registFacility(9001, 'Estação de Portimão', 1);
    DBMS_OUTPUT.PUT_LINE('Sucesso: Facility ' || v_id || ' registada.');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Erro Inesperado: ' || SQLERRM);
END;
/


DECLARE
    v_id facility.facility_id%TYPE;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 2: Nome Inválido---');
    v_id := registFacility(9002, '   ', 1);
    DBMS_OUTPUT.PUT_LINE('Falha: A função permitiu um nome vazio!');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20002 THEN
            DBMS_OUTPUT.PUT_LINE('OK: Erro -20002 capturado (Nome obrigatório).');
        ELSE
            DBMS_OUTPUT.PUT_LINE('Erro Incorreto: ' || SQLERRM);
        END IF;
END;
/


DECLARE
    v_id facility.facility_id%TYPE;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 3: ID Duplicado ---');
    v_id := registFacility(9001, 'Nova Estação', 1);
    DBMS_OUTPUT.PUT_LINE('Falha: A função permitiu um ID duplicado!');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20001 THEN
            DBMS_OUTPUT.PUT_LINE('OK: Erro -20001 capturado (ID existente).');
        ELSE
            DBMS_OUTPUT.PUT_LINE('Erro Incorreto: ' || SQLERRM);
        END IF;
END;
/


DECLARE
    v_id facility.facility_id%TYPE;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 4: Nome Duplicado ---');
    v_id := registFacility(9003, '  Estação de Portimão  ', 1);
    DBMS_OUTPUT.PUT_LINE('Falha: A função permitiu um nome duplicado!');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20003 THEN
            DBMS_OUTPUT.PUT_LINE('OK: Erro -20003 capturado (Nome existente).');
        ELSE
            DBMS_OUTPUT.PUT_LINE('Erro Incorreto: ' || SQLERRM);
        END IF;
END;
/


DECLARE
    v_id facility.facility_id%TYPE;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 5: Tipo Inexistente ---');
    v_id := registFacility(9004, 'Terminal de Carga', 999);
    DBMS_OUTPUT.PUT_LINE('Falha: A função permitiu um tipo de instalação inexistente!');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -20004 THEN
            DBMS_OUTPUT.PUT_LINE('OK: Erro -20004 capturado (Tipo inválido).');
        ELSE
            DBMS_OUTPUT.PUT_LINE('Erro Incorreto: ' || SQLERRM);
        END IF;
END;
/


BEGIN
    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('---Rollback efetuado para manter integridade ---');
END;
/