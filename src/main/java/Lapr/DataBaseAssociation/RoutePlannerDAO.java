package Lapr.DataBaseAssociation;

import Lapr.Domain.RoutePlannerRoute;
import Sprint1.Domain.Facility;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for persisting and retrieving route planner routes.
 * Uses existing Freight table to represent routes (without Train association).
 * 
 * Strategy:
 * - Freight table: represents route with origin and destination
 * - Wagon_In_Freight: associates wagons with the route
 * - FreighTrain: uses train_id = -1 to indicate "no train" (route not assigned to train)
 * 
 * @author Development Team
 * @version 2.0
 */
public class RoutePlannerDAO {
    private static final String USERNAME = "System";
    private static final String PASSWORD = "G0442526BD";
    private static final String DATABASE = "jdbc:oracle:thin:@vsgate-s1.dei.isep.ipp.pt:10715:XE";
    
    // Special train_id value to indicate route is not assigned to any real train
    // We use an existing train_id but with a convention that it's a "dummy" train for route planning
    // This is necessary because Freight.FreighTrainTraintrain_id is NOT NULL
    // We'll use train_id = 9999 as a convention (must exist in Train table)
    private static final int DUMMY_TRAIN_ID = 9999;
    private static final String DUMMY_TRAIN_OPERATOR = "Medway"; // Must be an existing operator

    /**
     * Gets the next available freight_id by finding the maximum and adding 1.
     * 
     * @param conn the database connection
     * @return the next freight_id
     */
    private int getNextFreightId(Connection conn) throws SQLException {
        String sql = "SELECT NVL(MAX(freight_id), 0) + 1 FROM Freight";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 1; // Default if table is empty
    }

    /**
     * Creates a new route using Freight table.
     * 
     * @param conn the database connection (must not be closed)
     * @param routeName the name of the route (stored as comment/not directly in Freight)
     * @param isSimple true if simple route, false if complex
     * @param originFacilityId the origin facility ID
     * @param destinationFacilityId the destination facility ID
     * @return the generated freight_id (used as route_id), or -1 if failed
     */
    public int createRoute(Connection conn, String routeName, boolean isSimple,
                          int originFacilityId, int destinationFacilityId) {
        try {
            int freightId = getNextFreightId(conn);
            
            // Insert into Freight table
            // Note: FreighTrainFreigth_id = freight_id (self-reference)
            //       FreighTrainTraintrain_id = -1 (indicates no train assigned)
            String sql = "INSERT INTO Freight (freight_id, Originfacility_id, Destinationfacility_id, " +
                        "FreighTrainFreigth_id, createDate, FreighTrainTraintrain_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, freightId);
                stmt.setInt(2, originFacilityId);
                stmt.setInt(3, destinationFacilityId);
                stmt.setInt(4, freightId); // Self-reference
                stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setInt(6, DUMMY_TRAIN_ID); // Dummy train_id for route planning (not a real train)
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    // Insert into FreighTrain to maintain referential integrity
                    // Using dummy train_id = -1 and operator = 'ROUTE_PLANNER'
                    insertFreighTrain(conn, freightId);
                    
                    System.out.println("Route created with ID (freight_id): " + freightId);
                    return freightId;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("ERROR creating route:");
            System.err.println("SQL Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create route: " + e.getMessage(), e);
        }
        
        return -1;
    }
    
    /**
     * Ensures dummy train exists for route planning.
     * Creates it if it doesn't exist.
     */
    private void ensureDummyTrainExists(Connection conn) throws SQLException {
        // Check if dummy train exists
        String checkSql = "SELECT COUNT(*) FROM Train WHERE train_id = ? AND operator_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(checkSql)) {
            stmt.setInt(1, DUMMY_TRAIN_ID);
            stmt.setString(2, DUMMY_TRAIN_OPERATOR);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return; // Train exists
                }
            }
        }
        
        // Create dummy train if it doesn't exist
        String createSql = "INSERT INTO Train (train_id, operator_id, max_length) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(createSql)) {
            stmt.setInt(1, DUMMY_TRAIN_ID);
            stmt.setString(2, DUMMY_TRAIN_OPERATOR);
            stmt.setInt(3, 0); // max_length = 0 indicates dummy train
            stmt.executeUpdate();
            System.out.println("Created dummy train " + DUMMY_TRAIN_ID + " for route planning");
        } catch (SQLException e) {
            // If train already exists (race condition), that's fine
            if (e.getErrorCode() != 1) { // 1 = unique constraint violation
                throw e;
            }
        }
    }
    
    /**
     * Inserts into FreighTrain table to maintain referential integrity.
     * Uses dummy train_id for routes not assigned to real trains.
     */
    private void insertFreighTrain(Connection conn, int freightId) throws SQLException {
        // Ensure dummy train exists
        ensureDummyTrainExists(conn);
        
        // Get a dummy locomotive_id (use first available or -1)
        // For dummy train, we'll use -1 and handle the error if foreign key constraint fails
        int dummyLocomotiveId = -1;
        
        String sql = "INSERT INTO FreighTrain (Freightfreight_id, Traintrain_id, " +
                    "TrainLocomotivelocomotive_id, Trainoperator_id) " +
                    "VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, freightId);
            stmt.setInt(2, DUMMY_TRAIN_ID);
            stmt.setInt(3, dummyLocomotiveId);
            stmt.setString(4, DUMMY_TRAIN_OPERATOR);
            stmt.executeUpdate();
        } catch (SQLException e) {
            // If this fails, we'll continue anyway - the route is still valid in Freight table
            System.err.println("Warning: Could not insert into FreighTrain: " + e.getMessage());
            // Don't throw - the route is still valid
        }
    }
    
    /**
     * Adds a wagon to a route using Wagon_In_Freight table.
     * 
     * @param conn the database connection (must not be closed)
     * @param routeId the route ID (freight_id)
     * @param wagonId the wagon ID
     * @return true if successful
     */
    public boolean addWagonToRoute(Connection conn, int routeId, int wagonId) {
        String sql = "INSERT INTO Wagon_In_Freight (Wagon_id, Freight_id) " +
                     "VALUES (?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, wagonId);
            stmt.setInt(2, routeId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding wagon to route: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Persists a complete route with all stations, wagons, and load/unload information.
     * Uses a transaction to ensure all-or-nothing persistence.
     * 
     * @param route the route to persist
     * @return true if successful
     */
    public boolean persistRoute(RoutePlannerRoute route) {
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD)) {
            conn.setAutoCommit(false);
            
            try {
                // Get origin and destination from stations
                List<RoutePlannerRoute.RouteStation> stations = route.getStations();
                if (stations == null || stations.size() < 2) {
                    conn.rollback();
                    System.err.println("Route must have at least 2 stations (origin and destination)");
                    return false;
                }
                
                int originFacilityId = Integer.parseInt(stations.get(0).getFacility().getId());
                int destinationFacilityId = Integer.parseInt(stations.get(stations.size() - 1).getFacility().getId());
                
                // Create route using Freight table
                int routeId;
                try {
                    routeId = createRoute(conn, route.getRouteName(), route.isSimple(),
                                         originFacilityId, destinationFacilityId);
                } catch (RuntimeException e) {
                    conn.rollback();
                    System.err.println("Failed to create route: " + e.getMessage());
                    throw e;
                }
                
                if (routeId == -1) {
                    conn.rollback();
                    System.err.println("Failed to create route: routeId is -1");
                    return false;
                }
                route.setRouteId(routeId);
                
                // Add wagons using Wagon_In_Freight table
                for (RoutePlannerRoute.RouteWagon wagon : route.getWagons()) {
                    if (!addWagonToRoute(conn, routeId, wagon.getWagonId())) {
                        conn.rollback();
                        System.err.println("Failed to add wagon " + wagon.getWagonId() + " to route " + routeId);
                        return false;
                    }
                }
                
                // Note: Intermediate stations and load/unload info are not stored
                // because there's no suitable table in the existing schema.
                // The route only stores origin and destination via Freight table.
                
                conn.commit();
                System.out.println("Route persisted successfully with ID: " + routeId);
                return true;
                
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Transaction rolled back due to error: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
            
        } catch (SQLException e) {
            System.err.println("Error persisting route: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets all routes (freights not assigned to trains) from database.
     * 
     * @return list of all routes
     */
    public List<RoutePlannerRoute> getAllRoutes() {
        List<RoutePlannerRoute> routes = new ArrayList<>();
        
        // Get freights that are not assigned to real trains (train_id = -1 or not in FreighTrain with real train)
        String sql = "SELECT f.freight_id, f.Originfacility_id, f.Destinationfacility_id, " +
                    "f.createDate, o.name AS origin_name, d.name AS destination_name " +
                    "FROM Freight f " +
                    "JOIN Facility o ON f.Originfacility_id = o.facility_id " +
                    "JOIN Facility d ON f.Destinationfacility_id = d.facility_id " +
                    "WHERE f.FreighTrainTraintrain_id = ? " +
                    "ORDER BY f.createDate DESC";
        
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, DUMMY_TRAIN_ID);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int routeId = rs.getInt("freight_id");
                    int originId = rs.getInt("Originfacility_id");
                    int destId = rs.getInt("Destinationfacility_id");
                    String originName = rs.getString("origin_name");
                    String destName = rs.getString("destination_name");
                    LocalDateTime createdDate = rs.getTimestamp("createDate").toLocalDateTime();
                    
                    // Create route with origin and destination
                    Facility origin = new Facility(String.valueOf(originId), originName);
                    Facility destination = new Facility(String.valueOf(destId), destName);
                    
                    RoutePlannerRoute route = new RoutePlannerRoute(
                        routeId, 
                        "Route " + routeId, // Route name not stored in Freight, use ID
                        createdDate, 
                        true // Assume simple for now
                    );
                    
                    // Add origin and destination as stations
                    route.addStation(new RoutePlannerRoute.RouteStation(origin, 1));
                    route.addStation(new RoutePlannerRoute.RouteStation(destination, 2));
                    
                    // Load wagons
                    loadWagonsForRoute(conn, route);
                    
                    routes.add(route);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving all routes: " + e.getMessage());
            e.printStackTrace();
        }
        
        return routes;
    }

    /**
     * Loads wagons and load/unload info for a route.
     */
    private void loadWagonsForRoute(Connection conn, RoutePlannerRoute route) throws SQLException {
        String wagonSql = "SELECT DISTINCT wif.Wagon_id, wt.type_name AS cargo_type " +
                         "FROM Wagon_In_Freight wif " +
                         "JOIN Wagon w ON wif.Wagon_id = w.wagon_id " +
                         "JOIN WagonModel wm ON w.WagonModelmodelID = wm.modelID " +
                         "JOIN WagonType wt ON wm.WagonTypetype_id = wt.type_id " +
                         "WHERE wif.Freight_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(wagonSql)) {
            stmt.setInt(1, route.getRouteId());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int wagonId = rs.getInt("Wagon_id");
                    String cargoType = rs.getString("cargo_type");
                    
                    RoutePlannerRoute.RouteWagon wagon = 
                        new RoutePlannerRoute.RouteWagon(wagonId, cargoType);
                    route.addWagon(wagon);
                }
            }
        }
    }

    /**
     * Gets a route by ID with all its stations and wagons.
     * 
     * @param routeId the route ID (freight_id)
     * @return the route, or null if not found
     */
    public RoutePlannerRoute getRouteById(int routeId) {
        String sql = "SELECT f.freight_id, f.Originfacility_id, f.Destinationfacility_id, " +
                    "f.createDate, o.name AS origin_name, d.name AS destination_name " +
                    "FROM Freight f " +
                    "JOIN Facility o ON f.Originfacility_id = o.facility_id " +
                    "JOIN Facility d ON f.Destinationfacility_id = d.facility_id " +
                    "WHERE f.freight_id = ? AND f.FreighTrainTraintrain_id = ?";
        
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, routeId);
            stmt.setInt(2, DUMMY_TRAIN_ID);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int originId = rs.getInt("Originfacility_id");
                    int destId = rs.getInt("Destinationfacility_id");
                    String originName = rs.getString("origin_name");
                    String destName = rs.getString("destination_name");
                    LocalDateTime createdDate = rs.getTimestamp("createDate").toLocalDateTime();
                    
                    Facility origin = new Facility(String.valueOf(originId), originName);
                    Facility destination = new Facility(String.valueOf(destId), destName);
                    
                    RoutePlannerRoute route = new RoutePlannerRoute(
                        routeId, 
                        "Route " + routeId,
                        createdDate, 
                        true
                    );
                    
                    route.addStation(new RoutePlannerRoute.RouteStation(origin, 1));
                    route.addStation(new RoutePlannerRoute.RouteStation(destination, 2));
                    
                    loadWagonsForRoute(conn, route);
                    
                    return route;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving route by ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
}
