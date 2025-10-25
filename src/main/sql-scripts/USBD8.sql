WITH loco_types (locomotive_id, loco_type) AS (
    SELECT Locomotivelocomotive_id, 'DIESEL'   FROM Diesel_Locomotive
    UNION ALL
    SELECT Locomotivelocomotive_id, 'ELECTRIC' FROM Electric_Locomotive
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
WHERE lt.loco_type = '&p_type'      -- escreve: ELECTRIC ou DIESEL
ORDER BY l.locomotive_id;



WITH loco_types (locomotive_id, loco_type) AS (
    SELECT Locomotivelocomotive_id, 'ELECTRIC' FROM Electric_Locomotive
)
SELECT
    l.locomotive_id,
    l.operator_id,
    l.entry_year,
    l.locomotive_model_id,
    g.code  AS gauge_code,
    g.width AS gauge_width,
    lt.loco_type,
    e.voltage AS voltage
FROM Locomotive l
         JOIN loco_types lt ON lt.locomotive_id = l.locomotive_id
         JOIN Gauge g       ON g.code = l.Gaugecode
         JOIN Electric_Locomotive e ON e.Locomotivelocomotive_id = l.locomotive_id
ORDER BY l.locomotive_id;



WITH loco_types (locomotive_id, loco_type) AS (
    SELECT Locomotivelocomotive_id, 'DIESEL' FROM Diesel_Locomotive
)
SELECT
    l.locomotive_id,
    l.operator_id,
    l.entry_year,
    l.locomotive_model_id,
    g.code  AS gauge_code,
    g.width AS gauge_width,
    lt.loco_type,
    d.fuelcapacity AS fuelcapacity
FROM Locomotive l
         JOIN loco_types lt ON lt.locomotive_id = l.locomotive_id
         JOIN Gauge g       ON g.code = l.Gaugecode
         JOIN Diesel_Locomotive d ON d.Locomotivelocomotive_id = l.locomotive_id
ORDER BY l.locomotive_id;
