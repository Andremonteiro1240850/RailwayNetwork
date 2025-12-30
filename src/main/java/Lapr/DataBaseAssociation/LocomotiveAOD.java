package Lapr.DataBaseAssociation;

import java.sql.*;

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
}

