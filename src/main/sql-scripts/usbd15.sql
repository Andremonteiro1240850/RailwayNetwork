

SELECT
    f.name AS nome_instalacao,
    COUNT(l.id_line) AS total_linhas_partida
FROM
    facility f
        JOIN
    line l ON f.id_facility = l.fk_facility_start
GROUP BY
    f.id_facility, f.name
ORDER BY
    total_linhas_partida DESC, nome_instalacao ASC;
