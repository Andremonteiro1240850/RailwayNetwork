DECLARE
  v_cur           SYS_REFCURSOR;
  v_facility_id   FACILITY.FACILITY_ID%TYPE;
  v_facility_name FACILITY.NAME%TYPE;
  v_facility_type FACILITY_TYPE.NAME%TYPE;
BEGIN
  v_cur := fn_usbd30_facilities_silos_sem_warehouses();

  LOOP
    FETCH v_cur INTO v_facility_id, v_facility_name, v_facility_type;
    EXIT WHEN v_cur%NOTFOUND;

    DBMS_OUTPUT.PUT_LINE('Facility ID: ' || v_facility_id ||
                         ' | Nome: ' || v_facility_name ||
                         ' | Tipo: ' || v_facility_type);
  END LOOP;

  CLOSE v_cur;

EXCEPTION
  WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE('Erro: ' || SQLERRM);
END;
/
