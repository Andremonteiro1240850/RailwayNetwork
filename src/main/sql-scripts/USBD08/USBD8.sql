WITH loco_types AS (
    SELECT Locomotivelocomotive_id AS locomotive_id, 'DIESEL' AS loco_type
    FROM Diesel_Locomotive
    UNION ALL
    SELECT Locomotivelocomotive_id AS locomotive_id, 'ELECTRIC' AS loco_type
    FROM Electric_Locomotive
)
SELECT
    l.locomotive_id,
    l.operator_id,
    l.entry_year,
    l.locomotive_model_id,
    g.code  AS gauge_code,
    g.width AS gauge_width,
    lt.loco_type,
    CASE WHEN lt.loco_type = 'ELECTRIC' THEN e.voltage END      AS voltage,
    CASE WHEN lt.loco_type = 'DIESEL'   THEN d.fuelcapacity END AS fuelcapacity
FROM Locomotive l
         JOIN loco_types lt ON lt.locomotive_id = l.locomotive_id
         JOIN Gauge g       ON g.code = l.Gaugecode
         LEFT JOIN Electric_Locomotive e ON e.Locomotivelocomotive_id = l.locomotive_id
         LEFT JOIN Diesel_Locomotive   d ON d.Locomotivelocomotive_id = l.locomotive_id
WHERE lt.loco_type = '&p_type'
ORDER BY l.locomotive_id;
































































