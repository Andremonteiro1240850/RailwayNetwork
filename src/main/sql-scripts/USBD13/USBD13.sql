WITH loco_types (locomotive_id, loco_type) AS (
    SELECT Locomotivelocomotive_id, 'DIESEL'  FROM Diesel_Locomotive
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
    lt.loco_type
FROM Locomotive l
         JOIN loco_types lt ON lt.locomotive_id = l.locomotive_id
         JOIN Gauge g       ON g.code = l.Gaugecode
WHERE lt.loco_type  = '&p_type'
  AND l.operator_id = '&p_operator'
  AND g.code        = '&p_gauge_code'
ORDER BY l.locomotive_id;