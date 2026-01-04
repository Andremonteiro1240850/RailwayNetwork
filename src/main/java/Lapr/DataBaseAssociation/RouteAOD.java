package Lapr.DataBaseAssociation;

import Lapr.Domain.Route; // Supondo que existe uma classe Route no domínio
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RouteAOD {
    private static final String USERNAME = "System";
    private static final String PASSWORD = "G0442526BD";
    private static final String DATABASE = "jdbc:oracle:thin:@vsgate-s1.dei.isep.ipp.pt:10715:XE";

    /**
     * Gets all routes from database using PL/SQL function.
     * Falls back to direct SQL if PL/SQL function is not available.
     * 
     * @return list of routes
     */
    public List<Route> getAllRoutes() {
        List<Route> routes = new ArrayList<>();
        
        // Try PL/SQL function first (as per requirements)
        String plsqlCall = "{ ? = call GET_ALL_ROUTES() }";
        
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
             CallableStatement stmt = conn.prepareCall(plsqlCall)) {
            
            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.execute();
            
            ResultSet rs = (ResultSet) stmt.getObject(1);
            
            while (rs.next()) {
                Route route = new Route();
                route.setFacilityID(rs.getInt("FACILITY_ID"));
                route.setTrainID(rs.getInt("TRAIN_ID"));
                route.setOrder(rs.getInt("ORDER"));
                routes.add(route);
            }
            
            System.out.println("Loaded " + routes.size() + " routes from database via PL/SQL function");
            
        } catch (SQLException e) {
            // If PL/SQL function doesn't exist, fallback to direct SQL
            System.err.println("PL/SQL function GET_ALL_ROUTES not available, using direct SQL: " + e.getMessage());
            return getAllRoutesFallback();
        }
        
        return routes;
    }
    
    /**
     * Fallback method using direct SQL query if PL/SQL function is not available.
     * Note: ORDER is a reserved word in SQL, so we need to quote it.
     */
    private List<Route> getAllRoutesFallback() {
        List<Route> routes = new ArrayList<>();
        // ORDER is a reserved word, so we need to quote it
        String sql = "SELECT FACILITY_ID, TRAIN_ID, \"ORDER\" FROM TRAIN_ROUTE ORDER BY TRAIN_ID, \"ORDER\"";

        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            int count = 0;
            while (rs.next()) {
                Route route = new Route();
                route.setFacilityID(rs.getInt("FACILITY_ID"));
                route.setTrainID(rs.getInt("TRAIN_ID"));
                route.setOrder(rs.getInt("ORDER"));
                routes.add(route);
                count++;
            }
            
            System.out.println("Loaded " + count + " routes from database via direct SQL");

        } catch (SQLException e) {
            System.err.println("Error retrieving routes from database: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
        }

        return routes;
    }
    
    /**
     * Creates a simple route (single freight) using PL/SQL function.
     * Falls back to direct SQL if PL/SQL function is not available.
     * 
     * @param trainId the train ID
     * @param stationIds list of station IDs in order
     * @param freightId the freight ID to assign
     * @return true if successful, false otherwise
     */
    public boolean createSimpleRoute(int trainId, List<Integer> stationIds, int freightId) {
        // Try PL/SQL function first (as per requirements)
        String plsqlCall = "{ ? = call CREATE_SIMPLE_ROUTE(?, ?, ?) }";
        
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
             CallableStatement stmt = conn.prepareCall(plsqlCall)) {
            
            // Convert station IDs list to array or comma-separated string
            // For PL/SQL, we'll use a comma-separated string or pass as array
            String stationIdsStr = stationIds.stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
            
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setInt(2, trainId);
            stmt.setString(3, stationIdsStr);
            stmt.setInt(4, freightId);
            stmt.execute();
            
            int result = stmt.getInt(1);
            
            if (result == 1) {
                System.out.println("Simple route created successfully via PL/SQL function");
                return true;
            } else {
                System.err.println("PL/SQL function returned failure code: " + result);
                return false;
            }
            
        } catch (SQLException e) {
            // If PL/SQL function doesn't exist, fallback to direct SQL
            System.err.println("PL/SQL function CREATE_SIMPLE_ROUTE not available, using direct SQL: " + e.getMessage());
            return createSimpleRouteFallback(trainId, stationIds, freightId);
        }
    }
    
    /**
     * Fallback method using direct SQL if PL/SQL function is not available.
     */
    private boolean createSimpleRouteFallback(int trainId, List<Integer> stationIds, int freightId) {
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD)) {
            conn.setAutoCommit(false);
            
            try {
                // Insert route stations
                String insertRouteSql = "INSERT INTO TRAIN_ROUTE (TRAIN_ID, FACILITY_ID, \"ORDER\") VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertRouteSql)) {
                    for (int i = 0; i < stationIds.size(); i++) {
                        stmt.setInt(1, trainId);
                        stmt.setInt(2, stationIds.get(i));
                        stmt.setInt(3, i + 1);
                        stmt.executeUpdate();
                    }
                }
                
                // Update freight to assign it to the train
                String updateFreightSql = "UPDATE FREIGHT SET TRAINTRAIN_ID = ? WHERE FREIGHT_ID = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateFreightSql)) {
                    stmt.setInt(1, trainId);
                    stmt.setInt(2, freightId);
                    stmt.executeUpdate();
                }
                
                conn.commit();
                System.out.println("Simple route created successfully via direct SQL");
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating simple route: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Creates a complex route (multiple freights) using PL/SQL function.
     * Falls back to direct SQL if PL/SQL function is not available.
     * 
     * @param trainId the train ID
     * @param stationIds list of station IDs in order
     * @param freightIds list of freight IDs to assign
     * @return true if successful, false otherwise
     */
    public boolean createComplexRoute(int trainId, List<Integer> stationIds, List<Integer> freightIds) {
        // Try PL/SQL function first (as per requirements)
        String plsqlCall = "{ ? = call CREATE_COMPLEX_ROUTE(?, ?, ?) }";
        
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
             CallableStatement stmt = conn.prepareCall(plsqlCall)) {
            
            // Convert lists to comma-separated strings
            String stationIdsStr = stationIds.stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
            String freightIdsStr = freightIds.stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
            
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setInt(2, trainId);
            stmt.setString(3, stationIdsStr);
            stmt.setString(4, freightIdsStr);
            stmt.execute();
            
            int result = stmt.getInt(1);
            
            if (result == 1) {
                System.out.println("Complex route created successfully via PL/SQL function");
                return true;
            } else {
                System.err.println("PL/SQL function returned failure code: " + result);
                return false;
            }
            
        } catch (SQLException e) {
            // If PL/SQL function doesn't exist, fallback to direct SQL
            System.err.println("PL/SQL function CREATE_COMPLEX_ROUTE not available, using direct SQL: " + e.getMessage());
            return createComplexRouteFallback(trainId, stationIds, freightIds);
        }
    }
    
    /**
     * Fallback method using direct SQL if PL/SQL function is not available.
     */
    private boolean createComplexRouteFallback(int trainId, List<Integer> stationIds, List<Integer> freightIds) {
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD)) {
            conn.setAutoCommit(false);
            
            try {
                // Insert route stations
                String insertRouteSql = "INSERT INTO TRAIN_ROUTE (TRAIN_ID, FACILITY_ID, \"ORDER\") VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertRouteSql)) {
                    for (int i = 0; i < stationIds.size(); i++) {
                        stmt.setInt(1, trainId);
                        stmt.setInt(2, stationIds.get(i));
                        stmt.setInt(3, i + 1);
                        stmt.executeUpdate();
                    }
                }
                
                // Update freights to assign them to the train
                String updateFreightSql = "UPDATE FREIGHT SET TRAINTRAIN_ID = ? WHERE FREIGHT_ID = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateFreightSql)) {
                    for (int freightId : freightIds) {
                        stmt.setInt(1, trainId);
                        stmt.setInt(2, freightId);
                        stmt.executeUpdate();
                    }
                }
                
                conn.commit();
                System.out.println("Complex route created successfully via direct SQL");
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating complex route: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}