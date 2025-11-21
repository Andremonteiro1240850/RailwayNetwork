CREATE OR REPLACE FUNCTION fn_usbd30_facilities_silos_sem_warehouses
RETURN SYS_REFCURSOR
IS
  v_cur SYS_REFCURSOR;
BEGIN
  OPEN v_cur FOR
    SELECT DISTINCT 
           f.FACILITY_ID,
           f.NAME AS FACILITY_NAME,
           ft.NAME AS FACILITY_TYPE
    FROM   FACILITY f
           INNER JOIN FACILITY_TYPE ft ON f.FACILITY_TYPETYPE_ID = ft.TYPE_ID
           INNER JOIN BUILDING b_silo ON b_silo.FACILITYFACILITY_ID = f.FACILITY_ID
           INNER JOIN BUILDINGTYPE bt_silo ON b_silo.BUILDINGTYPEID = bt_silo.ID
    WHERE  bt_silo.NAME = 'SILO'
      AND  NOT EXISTS (
             SELECT 1
             FROM   BUILDING b_warehouse
                    INNER JOIN BUILDINGTYPE bt_warehouse 
                      ON b_warehouse.BUILDINGTYPEID = bt_warehouse.ID
             WHERE  b_warehouse.FACILITYFACILITY_ID = f.FACILITY_ID
               AND  bt_warehouse.NAME = 'WAREHOUSE'
           )
    ORDER BY f.FACILITY_ID;
  
  RETURN v_cur;

EXCEPTION
  WHEN NO_DATA_FOUND THEN
    OPEN v_cur FOR SELECT 1 AS dummy FROM dual WHERE 1 = 0;
    RETURN v_cur;
  WHEN OTHERS THEN
    OPEN v_cur FOR SELECT 1 AS dummy FROM dual WHERE 1 = 0;
    RETURN v_cur;
END fn_usbd30_facilities_silos_sem_warehouses;
/
