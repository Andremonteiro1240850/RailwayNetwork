CREATE OR REPLACE FUNCTION function_usbd25_list_facilities_without_stop
RETURN SYS_REFCURSOR
AS
    cursor_train_route_facilities_without_stop SYS_REFCURSOR;
    automatically_selected_train_identifier NUMBER;
BEGIN
SELECT train_id
INTO automatically_selected_train_identifier
FROM Train
ORDER BY "date" DESC, time DESC
    FETCH FIRST 1 ROWS ONLY;

OPEN cursor_train_route_facilities_without_stop FOR
SELECT
    Facility.facility_id,
    Facility.name,
    Train_route."Order"
FROM Train_route
         INNER JOIN Facility
                    ON Facility.facility_id = Train_route.Facility_id
WHERE Train_route.Train_id = automatically_selected_train_identifier
ORDER BY Train_route."Order";

RETURN cursor_train_route_facilities_without_stop;
END;
/
