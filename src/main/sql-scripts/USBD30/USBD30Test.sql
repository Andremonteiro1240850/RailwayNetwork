SET SERVEROUTPUT ON;

DECLARE
  v_cur           SYS_REFCURSOR;
  v_id            facility.facility_id%TYPE;
  v_name          facility.name%TYPE;
  v_type          facility_type.name%TYPE;

  v_count   PLS_INTEGER := 0;
  has_8     BOOLEAN := FALSE;
  has_14    BOOLEAN := FALSE;
  has_17    BOOLEAN := FALSE;
BEGIN
  v_cur := fn_usbd30_facilities_silos_sem_warehouses;

  LOOP
    FETCH v_cur INTO v_id, v_name, v_type;
    EXIT WHEN v_cur%NOTFOUND;

    v_count := v_count + 1;

    IF v_id = 8  THEN has_8  := TRUE; END IF;
    IF v_id = 14 THEN has_14 := TRUE; END IF;
    IF v_id = 17 THEN has_17 := TRUE; END IF;
  END LOOP;
  CLOSE v_cur;

  IF v_count >= 1 AND has_8 AND has_14 AND NOT has_17 THEN
    DBMS_OUTPUT.PUT_LINE('USBD30 TEST: OK  (rows=' || v_count || ')');
  ELSE
    DBMS_OUTPUT.PUT_LINE(
      'USBD30 TEST: FAIL -> rows=' || v_count ||
      ', has_8='  || CASE WHEN has_8  THEN 'Y' ELSE 'N' END ||
      ', has_14=' || CASE WHEN has_14 THEN 'Y' ELSE 'N' END ||
      ', has_17=' || CASE WHEN has_17 THEN 'Y' ELSE 'N' END
    );
  END IF;
END;
/
