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
}