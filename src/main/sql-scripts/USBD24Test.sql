DECLARE
    EndPoint SYS_REFCURSOR;
    FacilityName FACILITY.NAME%TYPE;
    StopOrder   TRAIN_ROUTE."ORDER"%TYPE;

BEGIN
    EndPoint := ListEndpointsByRoute(1);
    IF EndPoint IS NOT NULL THEN
        LOOP
            FETCH EndPoint INTO FacilityName, StopOrder;
            EXIT WHEN EndPoint%NOTFOUND;
            DBMS_OUTPUT.PUT_LINE('Facility: ' || FacilityName || ', Order: ' || StopOrder);
        END LOOP;
    ELSE
        DBMS_OUTPUT.PUT_LINE('No endpoints found for the given route.');
    END IF;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('An error occurred: ' || SQLERRM);
END;
/

--------------------------------------------------------------------------------------------------------

DECLARE
EndPoint SYS_REFCURSOR;
    FacilityName FACILITY.NAME%TYPE;
    StopOrder   TRAIN_ROUTE."ORDER"%TYPE;

BEGIN
    EndPoint := ListEndpointsByRoute(2);
    IF EndPoint IS NOT NULL THEN
        LOOP
            FETCH EndPoint INTO FacilityName, StopOrder;
            EXIT WHEN EndPoint%NOTFOUND;
            DBMS_OUTPUT.PUT_LINE('Facility: ' || FacilityName || ', Order: ' || StopOrder);
END LOOP;
ELSE
        DBMS_OUTPUT.PUT_LINE('No endpoints found for the given route.');
END IF;
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('An error occurred: ' || SQLERRM);
END;
/




