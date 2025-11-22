CREATE OR REPLACE FUNCTION ListEndpointsByRoute (
    TrainRouteID IN TRAIN.TRAIN_ID%TYPE
)
    RETURN SYS_REFCURSOR
    IS
    EndPointListCursor SYS_REFCURSOR;

BEGIN
    OPEN EndPointListCursor FOR
        SELECT
            FACILITY.NAME AS FacilityName,
            TRAIN_ROUTE."ORDER" AS StopOrder
        FROM
            TRAIN_ROUTE
                JOIN FACILITY
                     ON TRAIN_ROUTE.FACILITY_ID = FACILITY.FACILITY_ID
        WHERE
            TRAIN_ROUTE.TRAIN_ID = TrainRouteID
        ORDER BY
            TRAIN_ROUTE."ORDER" ASC;

    RETURN EndPointListCursor;
END ListEndpointsByRoute;
/


