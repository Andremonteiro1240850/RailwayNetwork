

SELECT
    f.name AS nome_instalacao,
    COUNT(l.line_id) AS total_linhas_partida
FROM
    Facility f
        JOIN
    Line l ON f.facility_id = l.start_facility_id
GROUP BY
    f.facility_id, f.name
ORDER BY
    total_linhas_partida DESC, nome_instalacao ASC;



