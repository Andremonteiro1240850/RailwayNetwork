package Lapr.DataBaseAssociation;

import Sprint2.Domain.LineSegment;
import Sprint1.Domain.Facility;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Access Object Data for LineSegment.
 * Retrieves line segment data from Oracle database using PL/SQL functions.
 * 
 * @author Development Team
 * @version 1.0
 */
public class LineSegmentAOD {
    private static final String USERNAME = "System";
    private static final String PASSWORD = "G0442526BD";
    private static final String DATABASE = "jdbc:oracle:thin:@vsgate-s1.dei.isep.ipp.pt:10715:XE";

    /**
     * Gets segment data from database for a line.
     * Since a Line can have multiple Segments, this aggregates data from all segments.
     * 
     * @param lineId the line ID (note: this is actually a line_id, not segment_id)
     * @return SegmentData containing aggregated length, average inclination, etc., or null if not found
     */
    public SegmentData getSegmentData(String lineId) {
        // Query to aggregate data from all segments of a line
        // Note: segmentId parameter is actually line_id in our context
        // Try PL/SQL function first, fallback to direct SQL if it fails
        String plsqlCall = "{ ? = call GET_SEGMENT_DATA(?) }";
        
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
             CallableStatement stmt = conn.prepareCall(plsqlCall)) {
            
            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setString(2, lineId);
            stmt.execute();
            
            ResultSet rs = (ResultSet) stmt.getObject(1);
            
            if (rs.next()) {
                // Get track type (default to SINGLE if not found)
                String trackType = "SINGLE";
                try {
                    String trackTypeStr = rs.getString("TRACK_TYPE");
                    if (trackTypeStr != null && !trackTypeStr.trim().isEmpty()) {
                        trackType = trackTypeStr.trim().toUpperCase();
                    }
                } catch (SQLException e) {
                    // Column might not exist in PL/SQL function result
                }
                
                // Get siding info
                Integer sidingId = null;
                Integer sidingSize = null;
                try {
                    Object sidingIdObj = rs.getObject("SIDING_ID");
                    if (sidingIdObj != null) {
                        sidingId = rs.getInt("SIDING_ID");
                        Object sidingSizeObj = rs.getObject("SIDING_SIZE");
                        if (sidingSizeObj != null) {
                            sidingSize = rs.getInt("SIDING_SIZE");
                        }
                    }
                } catch (SQLException e) {
                    // Column might not exist in PL/SQL function result
                }
                
                return new SegmentData(
                    rs.getDouble("LENGTH_KM"),
                    rs.getDouble("INCLINATION_PERCENT"),
                    rs.getObject("SPEED_LIMIT_KMH") != null ? rs.getDouble("SPEED_LIMIT_KMH") : null,
                    rs.getDouble("GAUGE_MM"),
                    rs.getBoolean("ELECTRIFIED"),
                    trackType,
                    sidingId,
                    sidingSize
                );
            }
            
        } catch (SQLException e) {
            // If PL/SQL function fails, try direct SQL query
            System.out.println("PL/SQL function failed, trying direct SQL query for line " + lineId);
        }
        
        // Fallback: Direct SQL query to get segment data (for a specific segment, not aggregated)
        // Note: This should ideally get data for a specific segment_id, but we're using line_id
        // For now, we'll get the first segment's data or aggregate
        String sql = "SELECT " +
                     "  s.segment_id, " +
                     "  SUM(s.length_m) / 1000.0 AS total_length_km, " +
                     "  0.0 AS avg_inclination_percent, " + // Default inclination
                     "  NULL AS min_speed_limit_kmh, " +
                     "  MAX(g.width) AS gauge_mm, " +
                     "  MAX(CASE WHEN s.electrified_flag = 'Y' THEN 1 ELSE 0 END) AS is_electrified, " +
                     "  MAX(s.SegmentTypetrackType) AS track_type, " +
                     "  MAX(s.SidingSIding_id) AS siding_id, " +
                     "  MAX(sid.\"Size\") AS siding_size " +
                     "FROM Line l " +
                     "JOIN Segment s ON l.line_id = s.line_id " +
                     "JOIN Gauge g ON l.Gaugecode = g.code " +
                     "LEFT JOIN Siding sid ON s.SidingSIding_id = sid.SIding_id " +
                     "WHERE l.line_id = ? " +
                     "GROUP BY s.segment_id " +
                     "ORDER BY s.segment_order " +
                     "FETCH FIRST 1 ROW ONLY";
        
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, Integer.parseInt(lineId));
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                double lengthKm = rs.getDouble("total_length_km");
                double avgInclination = rs.getDouble("avg_inclination_percent");
                Double speedLimitKmH = rs.getObject("min_speed_limit_kmh") != null ? 
                    rs.getDouble("min_speed_limit_kmh") : null;
                double gaugeMm = rs.getDouble("gauge_mm");
                boolean electrified = rs.getInt("is_electrified") == 1;
                
                // Get track type
                String trackType = "SINGLE";
                String trackTypeStr = rs.getString("track_type");
                if (trackTypeStr != null && !trackTypeStr.trim().isEmpty()) {
                    trackType = trackTypeStr.trim().toUpperCase();
                }
                
                // Get siding info
                Integer sidingId = null;
                Integer sidingSize = null;
                Object sidingIdObj = rs.getObject("siding_id");
                if (sidingIdObj != null) {
                    sidingId = rs.getInt("siding_id");
                    Object sidingSizeObj = rs.getObject("siding_size");
                    if (sidingSizeObj != null) {
                        sidingSize = rs.getInt("siding_size");
                    }
                }
                
                // If no segments found, length will be 0
                if (lengthKm <= 0) {
                    System.err.println("WARNING: Line " + lineId + " has no segments or zero length");
                    return null;
                }
                
                return new SegmentData(lengthKm, avgInclination, speedLimitKmH, gaugeMm, electrified, 
                                     trackType, sidingId, sidingSize);
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving segment data from database for line " + lineId + ": " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Invalid line ID format: " + lineId);
        }
        
        return null;
    }
    
    /**
     * Gets all segment data from database using PL/SQL function.
     * 
     * @return list of all segment data
     */
    public List<SegmentData> getAllSegmentData() {
        List<SegmentData> segments = new ArrayList<>();
        String sql = "{ ? = call GET_ALL_SEGMENT_DATA() }";
        
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.execute();
            
            ResultSet rs = (ResultSet) stmt.getObject(1);
            
            while (rs.next()) {
                // Get track type
                String trackType = "SINGLE";
                try {
                    String trackTypeStr = rs.getString("TRACK_TYPE");
                    if (trackTypeStr != null && !trackTypeStr.trim().isEmpty()) {
                        trackType = trackTypeStr.trim().toUpperCase();
                    }
                } catch (SQLException e) {
                    // Column might not exist
                }
                
                // Get siding info
                Integer sidingId = null;
                Integer sidingSize = null;
                try {
                    Object sidingIdObj = rs.getObject("SIDING_ID");
                    if (sidingIdObj != null) {
                        sidingId = rs.getInt("SIDING_ID");
                        Object sidingSizeObj = rs.getObject("SIDING_SIZE");
                        if (sidingSizeObj != null) {
                            sidingSize = rs.getInt("SIDING_SIZE");
                        }
                    }
                } catch (SQLException e) {
                    // Column might not exist
                }
                
                segments.add(new SegmentData(
                    rs.getDouble("LENGTH_KM"),
                    rs.getDouble("INCLINATION_PERCENT"),
                    rs.getObject("SPEED_LIMIT_KMH") != null ? rs.getDouble("SPEED_LIMIT_KMH") : null,
                    rs.getDouble("GAUGE_MM"),
                    rs.getBoolean("ELECTRIFIED"),
                    trackType,
                    sidingId,
                    sidingSize
                ));
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving all segment data from database: " + e.getMessage());
            e.printStackTrace();
        }
        
        return segments;
    }
    
    /**
     * Gets all facilities (stations) from database.
     * USLP10: All data must come from database, not CSV.
     * 
     * @return list of all facilities from database
     */
    public List<Facility> getAllFacilities() {
        List<Facility> facilities = new ArrayList<>();
        String sql = "SELECT facility_id, name FROM Facility ORDER BY facility_id";
        
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String id = String.valueOf(rs.getInt("facility_id"));
                String name = rs.getString("name");
                Facility facility = new Facility(id, name);
                facilities.add(facility);
            }
            
            System.out.println("Total facilities loaded from database: " + facilities.size());
            
        } catch (SQLException e) {
            System.err.println("Error retrieving facilities from database: " + e.getMessage());
            e.printStackTrace();
        }
        
        return facilities;
    }
    
    /**
     * Gets all line segments from database.
     * USLP10: All data must come from database, not CSV.
     * Creates LineSegments from the Line table which connects facilities.
     * Calculates total length from Segment table and determines electrification status.
     * 
     * @param facilitiesMap map of facility ID to Facility object (for quick lookup)
     * @return list of all line segments from database
     */
    public List<LineSegment> getAllLineSegments(java.util.Map<String, Facility> facilitiesMap) {
        List<LineSegment> lineSegments = new ArrayList<>();
        
        // Query to get all lines with their start and end facilities, gauge, 
        // total length (sum of segments), electrification status, track type, and siding
        // USLP10: Load TrackType and Siding from database
        String sql = "SELECT " +
                     "  l.line_id, " +
                     "  l.name AS line_name, " +
                     "  l.start_facility_id, " +
                     "  l.end_facility_id, " +
                     "  g.width AS gauge_mm, " +
                     "  NVL(SUM(s.length_m), 0) / 1000.0 AS total_length_km, " + // Sum segment lengths, convert m to km
                     "  MAX(CASE WHEN s.electrified_flag = 'Y' THEN 1 ELSE 0 END) AS is_electrified, " +
                     "  MAX(s.SegmentTypetrackType) AS track_type, " + // Get track type (SINGLE, DOUBLE, QUAD)
                     "  MAX(s.SidingSIding_id) AS siding_id, " + // Get siding ID
                     "  MAX(sid.\"Size\") AS siding_size " + // Get siding size
                     "FROM Line l " +
                     "JOIN Gauge g ON l.Gaugecode = g.code " +
                     "LEFT JOIN Segment s ON l.line_id = s.line_id " +
                     "LEFT JOIN Siding sid ON s.SidingSIding_id = sid.SIding_id " +
                     "GROUP BY l.line_id, l.name, l.start_facility_id, l.end_facility_id, g.width " +
                     "ORDER BY l.line_id";
        
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            int loadedCount = 0;
            while (rs.next()) {
                try {
                    String lineId = String.valueOf(rs.getInt("line_id"));
                    String lineName = rs.getString("line_name");
                    String startFacilityId = String.valueOf(rs.getInt("start_facility_id"));
                    String endFacilityId = String.valueOf(rs.getInt("end_facility_id"));
                    double gaugeMm = rs.getDouble("gauge_mm");
                    double totalLengthKm = rs.getDouble("total_length_km");
                    boolean isElectrified = rs.getInt("is_electrified") == 1;
                    
                    // Get track type from database (USLP10 requirement)
                    String trackType = "SINGLE"; // Default to SINGLE
                    String trackTypeStr = rs.getString("track_type");
                    if (trackTypeStr != null && !trackTypeStr.trim().isEmpty()) {
                        trackType = trackTypeStr.trim().toUpperCase();
                    }
                    
                    // Get siding info from database (USLP10 requirement)
                    Integer sidingId = null;
                    Integer sidingSize = null;
                    Object sidingIdObj = rs.getObject("siding_id");
                    if (sidingIdObj != null) {
                        sidingId = rs.getInt("siding_id");
                        Object sidingSizeObj = rs.getObject("siding_size");
                        if (sidingSizeObj != null) {
                            sidingSize = rs.getInt("siding_size");
                        }
                    }
                    
                    Facility startFacility = facilitiesMap.get(startFacilityId);
                    Facility endFacility = facilitiesMap.get(endFacilityId);
                    
                    if (startFacility == null || endFacility == null) {
                        System.err.println("WARNING: Facility not found for line " + lineId + 
                                         " (start: " + startFacilityId + ", end: " + endFacilityId + ")");
                        continue;
                    }
                    
                    // Use calculated length from segments, or default if no segments found
                    if (totalLengthKm <= 0.0) {
                        totalLengthKm = 10.0; // Default length if no segments found
                        System.err.println("WARNING: Line " + lineId + " has no segments, using default length 10km");
                    }
                    
                    // Create LineSegment with data from database including TrackType and Siding
                    // Note: Speed limit and inclination will be loaded per-segment when calculating travel time
                    // USLP10: TrackType and Siding are loaded from database here
                    LineSegment segment = new LineSegment(
                        lineId,
                        lineName,
                        startFacility,
                        endFacility,
                        totalLengthKm,
                        gaugeMm,
                        isElectrified,
                        null, // Speed limit - will be loaded from Segment table per segment
                        0.0,  // Inclination - will be loaded from Segment table per segment
                        trackType, // TrackType loaded from database
                        sidingId,  // Siding ID loaded from database
                        sidingSize // Siding size loaded from database
                    );
                    
                    lineSegments.add(segment);
                    loadedCount++;
                } catch (Exception e) {
                    System.err.println("Error creating line segment from database row: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("Total line segments loaded from database: " + loadedCount);
            
        } catch (SQLException e) {
            System.err.println("Error retrieving line segments from database: " + e.getMessage());
            e.printStackTrace();
        }
        
        return lineSegments;
    }
    
    /**
     * Data class to hold segment information retrieved from database.
     */
    public static class SegmentData {
        private final double lengthKm;
        private final double inclinationPercent; // Inclination in percentage (e.g., 2.5 for 2.5%)
        private final Double speedLimitKmH;
        private final double gaugeMm;
        private final boolean electrified;
        private final String trackType;
        private final Integer sidingId;
        private final Integer sidingSize;
        
        public SegmentData(double lengthKm, double inclinationPercent, Double speedLimitKmH, 
                          double gaugeMm, boolean electrified) {
            this(lengthKm, inclinationPercent, speedLimitKmH, gaugeMm, electrified, 
                 "SINGLE", null, null);
        }
        
        public SegmentData(double lengthKm, double inclinationPercent, Double speedLimitKmH, 
                          double gaugeMm, boolean electrified, String trackType, 
                          Integer sidingId, Integer sidingSize) {
            this.lengthKm = lengthKm;
            this.inclinationPercent = inclinationPercent;
            this.speedLimitKmH = speedLimitKmH;
            this.gaugeMm = gaugeMm;
            this.electrified = electrified;
            this.trackType = (trackType != null && !trackType.trim().isEmpty()) ? trackType.trim().toUpperCase() : "SINGLE";
            this.sidingId = sidingId;
            this.sidingSize = sidingSize;
        }
        
        public double getLengthKm() {
            return lengthKm;
        }
        
        public double getInclinationPercent() {
            return inclinationPercent;
        }
        
        public Double getSpeedLimitKmH() {
            return speedLimitKmH;
        }
        
        public double getGaugeMm() {
            return gaugeMm;
        }
        
        public boolean isElectrified() {
            return electrified;
        }
        
        public String getTrackType() {
            return trackType;
        }
        
        public Integer getSidingId() {
            return sidingId;
        }
        
        public Integer getSidingSize() {
            return sidingSize;
        }
    }
}

