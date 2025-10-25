SELECT
    o.owner_id,
    o.name       AS owner_name,
    l.line_id,
    s.segment_id,
    st.type      AS track_type
FROM
    Owner o
    JOIN Line l        ON l.owner_id = o.owner_id
    JOIN Segment s     ON s.line_id = l.line_id
    JOIN SegmentType st ON st.trackNumber = s.SegmentTypetrackNumber
WHERE
    o.owner_id = '&p_owner_id'
ORDER BY
    st.type,
    s.segment_id;
