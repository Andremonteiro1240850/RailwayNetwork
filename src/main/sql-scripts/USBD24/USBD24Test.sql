SET SERVEROUTPUT ON;

DECLARE
RouteCursor   SYS_REFCURSOR;
    FacilityId    FACILITY.FACILITY_ID%TYPE;
    FacilityName  FACILITY.NAME%TYPE;
    RouteOrder    TRAIN_ROUTE."ORDER"%TYPE;
BEGIN

    RouteCursor := ListEndpointsByRoute(1);

    LOOP
FETCH RouteCursor INTO
            FacilityId,
            FacilityName,
            RouteOrder;

        EXIT WHEN RouteCursor%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE(
              'FacilityId = ' || FacilityId
           || ' | Facility = '  || FacilityName
           || ' | Order = '     || RouteOrder
        );
END LOOP;

CLOSE RouteCursor;

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Erro: ' || SQLCODE || ' - ' || SQLERRM);
END;
/
