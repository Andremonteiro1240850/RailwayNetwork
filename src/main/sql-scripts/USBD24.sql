CREATE OR REPLACE FUNCTION ListEndpointsByRoute (TrainRouteID IN Train.train_id%TYPE)
    RETURN SYS_REFCURSOR IS
    EndPointListCursor SYS_REFCURSOR;

BEGIN
    OPEN EndPointListCursor FOR
        SELECT
            Facility.name AS FacilityName,
            Train_route."Order" AS StopOrder
        FROM
            Train_route
                JOIN Facility
                     ON Train_route.facility_id = Facility.facility_id
        WHERE
            Train_route.train_id = TrainRouteID
        ORDER BY
            Train_route."Order" ASC;

    RETURN EndPointListCursor;
END ListEndpointsByRoute;
/