
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- US45: LIMPEZA PARA REPETIR TESTES ---');

DELETE FROM Segment WHERE segment_id BETWEEN 90000 AND 90099;
DELETE FROM Line    WHERE line_id    BETWEEN 9000  AND 9099;

COMMIT;

DBMS_OUTPUT.PUT_LINE('LIMPEZA OK (COMMIT).');
END;
/

-- ============================================================
-- TESTE 1: SUCESSO (COMMIT)
-- ============================================================
DECLARE
    v_result_line_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 1: Sucesso ---');

    v_result_line_id := usbd45_add_line(
        9000, 50, 12, 'Test Line OK', 'IP', 1668,
        90000, 3000, 1, 'Y', 2, 'DOUBLE', NULL
    );

    DBMS_OUTPUT.PUT_LINE('SUCESSO: line_id=' || v_result_line_id);

    COMMIT;
    DBMS_OUTPUT.PUT_LINE('COMMIT executado (sucesso ficou gravado).');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('FALHA NO SUCESSO: ' || SQLERRM);
        ROLLBACK;
END;
/

-- ============================================================
-- TESTE 2: TERMINAIS IGUAIS (-20045)
-- ============================================================
DECLARE
    v_result_line_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 2: Terminais iguais ---');

    v_result_line_id := usbd45_add_line(
        9001, 50, 50, 'Bad Terminals', 'IP', 1668,
        90001, 3000, 1, 'Y', 2, 'DOUBLE', NULL
    );

    DBMS_OUTPUT.PUT_LINE('ERRO: devia falhar, mas deu sucesso: line_id=' || v_result_line_id);
    ROLLBACK;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('OK (erro esperado): ' || SQLERRM);
        ROLLBACK;
END;
/

-- ============================================================
-- TESTE 3: OWNER INEXISTENTE (-20048)
-- ============================================================
DECLARE
    v_result_line_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 3: Owner inexistente ---');

    v_result_line_id := usbd45_add_line(
        9002, 50, 12, 'Bad Owner', 'NOPE', 1668,
        90002, 3000, 1, 'Y', 2, 'DOUBLE', NULL
    );

    DBMS_OUTPUT.PUT_LINE('ERRO: devia falhar, mas deu sucesso: line_id=' || v_result_line_id);
    ROLLBACK;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('OK (erro esperado): ' || SQLERRM);
        ROLLBACK;
END;
/

-- ============================================================
-- TESTE 4: SEGMENT LENGTH INVÁLIDO (-20050)
-- ============================================================
DECLARE
    v_result_line_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 4: Segment length inválido ---');

    v_result_line_id := usbd45_add_line(
        9003, 50, 12, 'Bad Segment', 'IP', 1668,
        90003, 0, 1, 'Y', 2, 'DOUBLE', NULL
    );

    DBMS_OUTPUT.PUT_LINE('ERRO: devia falhar, mas deu sucesso: line_id=' || v_result_line_id);
    ROLLBACK;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('OK (erro esperado): ' || SQLERRM);
        ROLLBACK;
END;
/

-- ============================================================
-- TESTE 5: DUPLICATE KEY (line_id repetido) (-20054)
-- Nota: este teste assume que o TESTE 1 foi COMMITADO e não foi limpo.
-- ============================================================
DECLARE
    v_result_line_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 5: Duplicate key (line_id repetido) ---');

    v_result_line_id := usbd45_add_line(
        9000, 50, 12, 'DupKey Line', 'IP', 1668,
        90005, 3000, 1, 'Y', 2, 'DOUBLE', NULL
    );

    DBMS_OUTPUT.PUT_LINE('ERRO: devia falhar, mas deu sucesso: line_id=' || v_result_line_id);
    ROLLBACK;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('OK (erro esperado): ' || SQLERRM);
        ROLLBACK;
END;
/

-- ============================================================
-- TESTE 6: START FACILITY INEXISTENTE (-20046)
-- ============================================================
DECLARE
    v_result_line_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 6: Start facility inexistente ---');

    v_result_line_id := usbd45_add_line(
        9006, 9999, 12, 'Bad Start Facility', 'IP', 1668,
        90006, 3000, 1, 'Y', 2, 'DOUBLE', NULL
    );

    DBMS_OUTPUT.PUT_LINE('ERRO: devia falhar, mas deu sucesso: line_id=' || v_result_line_id);
    ROLLBACK;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('OK (erro esperado): ' || SQLERRM);
        ROLLBACK;
END;
/

-- ============================================================
-- TESTE 7: END FACILITY INEXISTENTE (-20047)
-- ============================================================
DECLARE
    v_result_line_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 7: End facility inexistente ---');

    v_result_line_id := usbd45_add_line(
        9007, 50, 9999, 'Bad End Facility', 'IP', 1668,
        90007, 3000, 1, 'Y', 2, 'DOUBLE', NULL
    );

    DBMS_OUTPUT.PUT_LINE('ERRO: devia falhar, mas deu sucesso: line_id=' || v_result_line_id);
    ROLLBACK;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('OK (erro esperado): ' || SQLERRM);
        ROLLBACK;
END;
/

-- ============================================================
-- TESTE 8: GAUGE INEXISTENTE (-20049)
-- ============================================================
DECLARE
    v_result_line_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 8: Gauge inexistente ---');

    v_result_line_id := usbd45_add_line(
        9008, 50, 12, 'Bad Gauge', 'IP', 999999,
        90008, 3000, 1, 'Y', 2, 'DOUBLE', NULL
    );

    DBMS_OUTPUT.PUT_LINE('ERRO: devia falhar, mas deu sucesso: line_id=' || v_result_line_id);
    ROLLBACK;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('OK (erro esperado): ' || SQLERRM);
        ROLLBACK;
END;
/

-- ============================================================
-- TESTE 9: ELECTRIFIED FLAG INVÁLIDA (-20051)
-- ============================================================
DECLARE
    v_result_line_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 9: Electrified flag inválida ---');

    v_result_line_id := usbd45_add_line(
        9009, 50, 12, 'Bad Flag', 'IP', 1668,
        90009, 3000, 1, 'X', 2, 'DOUBLE', NULL
    );

    DBMS_OUTPUT.PUT_LINE('ERRO: devia falhar, mas deu sucesso: line_id=' || v_result_line_id);
    ROLLBACK;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('OK (erro esperado): ' || SQLERRM);
        ROLLBACK;
END;
/

-- ============================================================
-- TESTE 10: SEGMENTTYPE INEXISTENTE (combinação) (-20052)
-- ============================================================
DECLARE
    v_result_line_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 10: SegmentType inexistente ---');

    v_result_line_id := usbd45_add_line(
        9010, 50, 12, 'Bad SegmentType', 'IP', 1668,
        90010, 3000, 1, 'Y', 99, 'DOUBLE', NULL
    );

    DBMS_OUTPUT.PUT_LINE('ERRO: devia falhar, mas deu sucesso: line_id=' || v_result_line_id);
    ROLLBACK;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('OK (erro esperado): ' || SQLERRM);
        ROLLBACK;
END;
/

-- ============================================================
-- TESTE 11: SIDING INEXISTENTE (-20053)
-- ============================================================
DECLARE
    v_result_line_id NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('--- TESTE 11: Siding inexistente ---');

    v_result_line_id := usbd45_add_line(
        9011, 50, 12, 'Bad Siding', 'IP', 1668,
        90011, 3000, 1, 'Y', 2, 'DOUBLE', 9999
    );

    DBMS_OUTPUT.PUT_LINE('ERRO: devia falhar, mas deu sucesso: line_id=' || v_result_line_id);
    ROLLBACK;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('OK (erro esperado): ' || SQLERRM);
        ROLLBACK;
END;
/

BEGIN
    DBMS_OUTPUT.PUT_LINE('=== FIM DOS TESTES US45 ===');
END;
/
