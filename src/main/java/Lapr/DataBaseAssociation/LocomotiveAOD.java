package Lapr.DataBaseAssociation;

import Sprint1.Domain.Locomotive;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * Access Object Data for Locomotive.
 * Retrieves locomotive power data from Oracle database using PL/SQL functions.
 * 
 * @author Development Team
 * @version 1.0
 */
public class LocomotiveAOD {
    private static final String USERNAME = "System";
    private static final String PASSWORD = "G0442526BD";
    private static final String DATABASE = "jdbc:oracle:thin:@vsgate-s1.dei.isep.ipp.pt:10715:XE";

    /**
     * Gets locomotive power from database using PL/SQL function.
     * 
     * @param locomotiveId the locomotive ID
     * @return power in kilowatts, or 0.0 if not found or error
     */
    public double getLocomotivePower(String locomotiveId) {
        String sql = "{ ? = call GET_LOCOMOTIVE_POWER(?) }";
        
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.registerOutParameter(1, Types.DOUBLE);
            stmt.setString(2, locomotiveId);
            stmt.execute();
            
            return stmt.getDouble(1);
            
        } catch (SQLException e) {
            System.err.println("Error retrieving locomotive power from database: " + e.getMessage());
            e.printStackTrace();
            return 0.0; // Default to 0 if error
        }
    }
    
    /**
     * Gets locomotive mass from database using PL/SQL function.
     * 
     * @param locomotiveId the locomotive ID
     * @return mass in kilograms, or 0.0 if not found or error
     */
    public double getLocomotiveMass(String locomotiveId) {
        String sql = "{ ? = call GET_LOCOMOTIVE_MASS(?) }";
        
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.registerOutParameter(1, Types.DOUBLE);
            stmt.setString(2, locomotiveId);
            stmt.execute();
            
            return stmt.getDouble(1);
            
        } catch (SQLException e) {
            System.err.println("Error retrieving locomotive mass from database: " + e.getMessage());
            e.printStackTrace();
            return 0.0; // Default to 0 if error
        }
    }
    
    /**
     * Gets all locomotives from database.
     * USLP10: All data must come from database, not CSV.
     * 
     * @return list of all locomotives from database
     */
    public List<Locomotive> getAllLocomotives() {
        List<Locomotive> locomotives = new ArrayList<>();
        Set<String> addedIds = new HashSet<>(); // Track added locomotive IDs to avoid duplicates

        String sql = "SELECT " +
                     "  l.locomotive_id, " +
                     "  lm.max_speed, " +
                     "  g.width AS gauge_width, " +
                     "  lm.power, " +
                     "  CASE " +
                     "    WHEN dl.LocomotiveModellocomotive_model_id IS NOT NULL THEN 'diesel' " +
                     "    WHEN el.LocomotiveModellocomotive_model_id IS NOT NULL THEN 'electric' " +
                     "    ELSE 'unknown' " +
                     "  END AS loco_type " +
                     "FROM Locomotive l " +
                     "JOIN LocomotiveModel lm ON l.locomotive_model_id = lm.locomotive_model_id " +
                     "JOIN Locomotive_Gauge lg ON l.locomotive_id = lg.Locomotivelocomotive_id " +
                     "JOIN Gauge g ON lg.Gaugecode = g.code " +
                     "LEFT JOIN Diesel_Locomotive dl ON lm.locomotive_model_id = dl.LocomotiveModellocomotive_model_id " +
                     "LEFT JOIN Electric_Locomotive el ON lm.locomotive_model_id = el.LocomotiveModellocomotive_model_id " +
                     "ORDER BY l.locomotive_id, g.code";

        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                try {
                    String id = String.valueOf(rs.getInt("locomotive_id"));

                    if (addedIds.contains(id)) {
                        continue; // Skip duplicates
                    }

                    String type = rs.getString("loco_type");
                    double maxSpeed = rs.getDouble("max_speed");
                    double gauge = rs.getDouble("gauge_width");
                    double powerKw = rs.getDouble("power");

                    Locomotive locomotive = new Locomotive(id, type, maxSpeed, gauge, powerKw);
                    locomotives.add(locomotive);
                    addedIds.add(id);
                } catch (Exception e) {
                    System.err.println("Error creating locomotive from database row: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            System.out.println("Total locomotives loaded from database: " + locomotives.size());
        } catch (SQLException e) {
            System.err.println("Error retrieving locomotives from database: " + e.getMessage());
            e.printStackTrace();
        }
        return locomotives;
    }
}

