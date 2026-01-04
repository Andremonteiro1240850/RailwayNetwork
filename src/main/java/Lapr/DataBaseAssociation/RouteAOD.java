package Lapr.DataBaseAssociation;

import Lapr.Domain.Route;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RouteAOD {
    private static final String USERNAME = "System";
    private static final String PASSWORD = "G0442526BD";
    private static final String DATABASE = "jdbc:oracle:thin:@vsgate-s1.dei.isep.ipp.pt:10715:XE";

    /**
     * Gets all routes from database.
     * 
     * @return list of routes
     */
    public List<Route> getAllRoutes() {
        List<Route> routes = new ArrayList<>();
        
        String sql = "SELECT FACILITY_ID, TRAIN_ID, \"ORDER\" " +
                     "FROM Train_route " +
                     "ORDER BY TRAIN_ID, \"ORDER\"";
        
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Route route = new Route();
                route.setFacilityID(rs.getInt("FACILITY_ID"));
                route.setTrainID(rs.getInt("TRAIN_ID"));
                route.setOrder(rs.getInt("ORDER"));
                routes.add(route);
            }
            
            System.out.println("Loaded " + routes.size() + " routes from database");
            
        } catch (SQLException e) {
            System.err.println("Error retrieving routes from database: " + e.getMessage());
            e.printStackTrace();
        }
        
        return routes;
    }
    
    /**
     * Creates a simple route (single freight).
     * 
     * @param trainId the train ID
     * @param stationIds list of station IDs in order
     * @param freightId the freight ID to assign
     * @return true if successful, false otherwise
     */
    public boolean createSimpleRoute(int trainId, List<Integer> stationIds, int freightId) {
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD)) {
            conn.setAutoCommit(false);
            
            // Insert route stations
            String insertRouteSql = "INSERT INTO Train_route (TRAIN_ID, FACILITY_ID, \"ORDER\", TRAINOPERATOR_ID) " +
                                   "VALUES (?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(insertRouteSql)) {
                String operatorId = "OP001"; // Default operator
                int order = 1;
                for (Integer stationId : stationIds) {
                    stmt.setInt(1, trainId);
                    stmt.setInt(2, stationId);
                    stmt.setInt(3, order++);
                    stmt.setString(4, operatorId);
                    stmt.executeUpdate();
                }
            }
            
            // Update freight with train ID
            String updateFreightSql = "UPDATE Freight SET TRAINTRAIN_ID = ? WHERE FREIGHT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateFreightSql)) {
                stmt.setInt(1, trainId);
                stmt.setInt(2, freightId);
                stmt.executeUpdate();
            }
            
            conn.commit();
            System.out.println("Simple route created successfully");
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error creating simple route: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Creates a complex route (multiple freights).
     * 
     * @param trainId the train ID
     * @param stationIds list of station IDs in order
     * @param freightIds list of freight IDs to assign
     * @return true if successful, false otherwise
     */
    public boolean createComplexRoute(int trainId, List<Integer> stationIds, List<Integer> freightIds) {
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD)) {
            conn.setAutoCommit(false);
            
            // Insert route stations
            String insertRouteSql = "INSERT INTO Train_route (TRAIN_ID, FACILITY_ID, \"ORDER\", TRAINOPERATOR_ID) " +
                                   "VALUES (?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(insertRouteSql)) {
                String operatorId = "OP001"; // Default operator
                int order = 1;
                for (Integer stationId : stationIds) {
                    stmt.setInt(1, trainId);
                    stmt.setInt(2, stationId);
                    stmt.setInt(3, order++);
                    stmt.setString(4, operatorId);
                    stmt.executeUpdate();
                }
            }
            
            // Update freights with train ID
            String updateFreightSql = "UPDATE Freight SET TRAINTRAIN_ID = ? WHERE FREIGHT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateFreightSql)) {
                for (Integer freightId : freightIds) {
                    stmt.setInt(1, trainId);
                    stmt.setInt(2, freightId);
                    stmt.executeUpdate();
                }
            }
            
            conn.commit();
            System.out.println("Complex route created successfully");
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error creating complex route: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
}
