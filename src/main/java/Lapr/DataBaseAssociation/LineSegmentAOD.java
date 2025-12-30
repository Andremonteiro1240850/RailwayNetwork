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
     * Gets segment data from database using PL/SQL function.
     * 
     * @param segmentId the segment ID
     * @return SegmentData containing length, inclination, speed limit, etc., or null if not found
     */
    public SegmentData getSegmentData(String segmentId) {
        String sql = "{ ? = call GET_SEGMENT_DATA(?) }";
        
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setString(2, segmentId);
            stmt.execute();
            
            ResultSet rs = (ResultSet) stmt.getObject(1);
            
            if (rs.next()) {
                return new SegmentData(
                    rs.getDouble("LENGTH_KM"),
                    rs.getDouble("INCLINATION_PERCENT"), // Inclination in percentage (e.g., 2.5 for 2.5%)
                    rs.getObject("SPEED_LIMIT_KMH") != null ? rs.getDouble("SPEED_LIMIT_KMH") : null,
                    rs.getDouble("GAUGE_MM"),
                    rs.getBoolean("ELECTRIFIED")
                );
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving segment data from database: " + e.getMessage());
            e.printStackTrace();
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
                segments.add(new SegmentData(
                    rs.getDouble("LENGTH_KM"),
                    rs.getDouble("INCLINATION_PERCENT"),
                    rs.getObject("SPEED_LIMIT_KMH") != null ? rs.getDouble("SPEED_LIMIT_KMH") : null,
                    rs.getDouble("GAUGE_MM"),
                    rs.getBoolean("ELECTRIFIED")
                ));
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving all segment data from database: " + e.getMessage());
            e.printStackTrace();
        }
        
        return segments;
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
        
        public SegmentData(double lengthKm, double inclinationPercent, Double speedLimitKmH, 
                          double gaugeMm, boolean electrified) {
            this.lengthKm = lengthKm;
            this.inclinationPercent = inclinationPercent;
            this.speedLimitKmH = speedLimitKmH;
            this.gaugeMm = gaugeMm;
            this.electrified = electrified;
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
    }
}

