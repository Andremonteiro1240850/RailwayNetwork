CREATE OR REPLACE FUNCTION getLocomotivesWithMultipleGauges
RETURN SYS_REFCURSOR
AS
    locomotiveResultCursor SYS_REFCURSOR;
BEGIN
OPEN locomotiveResultCursor FOR
SELECT DISTINCT
    locomotive.LOCOMOTIVE_ID,
    locomotive.OPERATOR_ID,
    locomotive.LOCOMOTIVE_MODEL_ID,
    locomotive.GAUGECODE
FROM   LOCOMOTIVE locomotive
WHERE  locomotive.OPERATOR_ID = 'PT509017800'
  AND    locomotive.LOCOMOTIVE_MODEL_ID IN (
    SELECT LOCOMOTIVE_MODEL_ID
    FROM   LOCOMOTIVE
    GROUP BY LOCOMOTIVE_MODEL_ID
    HAVING COUNT(DISTINCT GAUGECODE) > 1
)
ORDER BY locomotive.LOCOMOTIVE_ID;

RETURN locomotiveResultCursor;
END;
/






SET SERVEROUTPUT ON;

DECLARE
locomotiveResultCursor SYS_REFCURSOR;

    locomotiveIdentifier       LOCOMOTIVE.LOCOMOTIVE_ID%TYPE;
    operatorIdentifier         LOCOMOTIVE.OPERATOR_ID%TYPE;
    locomotiveModelIdentifier  LOCOMOTIVE.LOCOMOTIVE_MODEL_ID%TYPE;
    gaugeCode                  LOCOMOTIVE.GAUGECODE%TYPE;
BEGIN
    locomotiveResultCursor := getLocomotivesWithMultipleGauges;

    LOOP
FETCH locomotiveResultCursor
        INTO  locomotiveIdentifier,
              operatorIdentifier,
              locomotiveModelIdentifier,
              gaugeCode;

        EXIT WHEN locomotiveResultCursor%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE(
            'LOCOMOTIVE_ID = '          || locomotiveIdentifier      ||
            ', OPERATOR_ID = '          || operatorIdentifier        ||
            ', LOCOMOTIVE_MODEL_ID = '  || locomotiveModelIdentifier ||
            ', GAUGECODE = '            || gaugeCode
        );
END LOOP;

CLOSE locomotiveResultCursor;
END;
/
