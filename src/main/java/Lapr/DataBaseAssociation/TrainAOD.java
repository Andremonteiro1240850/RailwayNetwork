package Lapr.DataBaseAssociation;

import Lapr.Domain.Train;
import Sprint1.Domain.Facility;
import Sprint1.Domain.Locomotive;
import Sprint1.Domain.Wagon;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Access Object Data for Train.
 * Retrieves train data from Oracle database using PL/SQL functions.
 * 
 * NOTE: This class is currently not fully implemented as Train is an immutable class
 * that requires all parameters in its constructor. To properly load trains from database,
 * you would need to also load routes, locomotives, and wagons from their respective tables.
 * 
 * @author Development Team
 * @version 1.0
 */
public class TrainAOD {
    private static final String USERNAME = "System";
    private static final String PASSWORD = "G0442526BD";
    private static final String DATABASE = "jdbc:oracle:thin:@vsgate-s1.dei.isep.ipp.pt:10715:XE";

    /**
     * Gets all trains that have been assembled and assigned to routes (USLP09).
     * USLP10: These are the trains that can be dispatched.
     * Only returns trains that have been fully assembled (train_id != 9999) and have routes assigned.
     * 
     * @return list of trains that are ready for dispatch
     */
    public List<Train> getAllTrains() {
        List<Train> trains = new ArrayList<>();
        
        // Get all trains that have been assembled (train_id != 9999) and have routes
        String sql = "SELECT DISTINCT t.train_id, t.operator_id " +
                     "FROM Train t " +
                     "JOIN Train_route tr ON t.train_id = tr.Train_id " +
                     "WHERE t.train_id != 9999 " + // Exclude dummy train
                     "ORDER BY t.train_id";
        
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                try {
                    int trainIdInt = rs.getInt("train_id");
                    String trainId = String.valueOf(trainIdInt);
                    
                    // Load route (stations) for this train
                    List<Facility> route = loadRouteForTrain(conn, trainIdInt);
                    if (route == null || route.size() < 2) {
                        System.err.println("WARNING: Train " + trainId + " has invalid route (less than 2 stations), skipping");
                        continue;
                    }
                    
                    // Load locomotives for this train
                    List<Locomotive> locomotives = loadLocomotivesForTrain(conn, trainIdInt);
                    if (locomotives == null || locomotives.isEmpty()) {
                        System.err.println("WARNING: Train " + trainId + " has no locomotives, skipping");
                        continue;
                    }
                    
                    // Load wagons for this train (through Freight)
                    List<Wagon> wagons = loadWagonsForTrain(conn, trainIdInt);
                    if (wagons == null) {
                        wagons = new ArrayList<>(); // Empty list if no wagons
                    }
                    
                    // Calculate total weight from wagons
                    double totalWeight = wagons.stream()
                        .mapToDouble(w -> w.getEmptyWeightKg() + w.getMaxLoadWeightKg()) // Assume fully loaded
                        .sum();
                    
                    // For USLP10, departure time will be set by the user during dispatch
                    // Use a default time (now) - this will be overridden when user sets departure time
                    LocalDateTime defaultDepartureTime = LocalDateTime.now();
                    
                    // Create Train object
                    // selectedStations is the same as route for now (user can specify path in USLP10)
                    Train train = new Train(
                        trainId,
                        route,           // Full route
                        route,           // Selected stations (same as route initially)
                        locomotives,
                        wagons,
                        defaultDepartureTime,
                        totalWeight
                    );
                    
                    trains.add(train);
                    System.out.println("Loaded train " + trainId + " with " + route.size() + " stations, " +
                                     locomotives.size() + " locomotives, " + wagons.size() + " wagons");
                    
                } catch (Exception e) {
                    System.err.println("Error creating train from database row: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("Total trains loaded from database: " + trains.size());
            
        } catch (SQLException e) {
            System.err.println("Error retrieving trains from database: " + e.getMessage());
            e.printStackTrace();
        }
        
        return trains;
    }
    
    /**
     * Loads the route (stations) for a train from Train_route table.
     * 
     * @param conn the database connection
     * @param trainId the train ID
     * @return list of facilities (stations) in order, or null if error
     */
    private List<Facility> loadRouteForTrain(Connection conn, int trainId) {
        List<Facility> route = new ArrayList<>();
        
        String sql = "SELECT tr.Facility_id, f.name " +
                     "FROM Train_route tr " +
                     "JOIN Facility f ON tr.Facility_id = f.facility_id " +
                     "WHERE tr.Train_id = ? " +
                     "ORDER BY tr.\"Order\"";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, trainId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String facilityId = String.valueOf(rs.getInt("Facility_id"));
                    String facilityName = rs.getString("name");
                    Facility facility = new Facility(facilityId, facilityName);
                    route.add(facility);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading route for train " + trainId + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        
        return route;
    }
    
    /**
     * Loads locomotives for a train from Locomotive_Train table.
     * 
     * @param conn the database connection
     * @param trainId the train ID
     * @return list of locomotives, or null if error
     */
    private List<Locomotive> loadLocomotivesForTrain(Connection conn, int trainId) {
        List<Locomotive> locomotives = new ArrayList<>();
        LocomotiveAOD locomotiveAOD = new LocomotiveAOD();
        
        String sql = "SELECT lt.Locomotivelocomotive_id " +
                     "FROM Locomotive_Train lt " +
                     "WHERE lt.Traintrain_id = ? " +
                     "ORDER BY lt.Adddate ASC";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, trainId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int locomotiveIdInt = rs.getInt("Locomotivelocomotive_id");
                    String locomotiveId = String.valueOf(locomotiveIdInt);
                    
                    // Get full locomotive details
                    // We need to get all locomotives and find the one with matching ID
                    List<Locomotive> allLocomotives = locomotiveAOD.getAllLocomotives();
                    Locomotive locomotive = allLocomotives.stream()
                        .filter(l -> l.getId().equals(locomotiveId))
                        .findFirst()
                        .orElse(null);
                    
                    if (locomotive != null) {
                        locomotives.add(locomotive);
                    } else {
                        System.err.println("WARNING: Locomotive " + locomotiveId + " not found in database");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading locomotives for train " + trainId + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        
        return locomotives;
    }
    
    /**
     * Loads wagons for a train through Freight -> Wagon_In_Freight.
     * 
     * @param conn the database connection
     * @param trainId the train ID
     * @return list of wagons, or empty list if no wagons
     */
    private List<Wagon> loadWagonsForTrain(Connection conn, int trainId) {
        List<Wagon> wagons = new ArrayList<>();
        WagonAOD wagonAOD = new WagonAOD();
        
        // Get wagons through Freight -> Wagon_In_Freight
        String sql = "SELECT DISTINCT wif.Wagon_id " +
                     "FROM Freight f " +
                     "JOIN Wagon_In_Freight wif ON f.freight_id = wif.Freight_id " +
                     "WHERE f.FreighTrainTraintrain_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, trainId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int wagonIdInt = rs.getInt("Wagon_id");
                    String wagonId = String.valueOf(wagonIdInt);
                    
                    // Get full wagon details
                    // We need to get all wagons and find the one with matching ID
                    List<Wagon> allWagons = wagonAOD.getAllWagons();
                    Wagon wagon = allWagons.stream()
                        .filter(w -> w.getWagonId().equals(wagonId))
                        .findFirst()
                        .orElse(null);
                    
                    if (wagon != null) {
                        wagons.add(wagon);
                    } else {
                        System.err.println("WARNING: Wagon " + wagonId + " not found in database");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading wagons for train " + trainId + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // Return empty list instead of null
        }
        
        return wagons;
    }
    
    /**
     * Creates a new train in the database.
     * 
     * @param operatorId the operator ID
     * @param maxLength the maximum train length
     * @return the generated train ID, or -1 if failed
     */
    public int createTrain(String operatorId, double maxLength) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
            // Ensure auto-commit is enabled for this operation
            conn.setAutoCommit(true);
            
            // Get next train ID
            int trainId = getNextTrainId(conn);
            
            String sql = "INSERT INTO Train (train_id, operator_id, max_length) VALUES (?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, trainId);
                stmt.setString(2, operatorId);
                stmt.setDouble(3, maxLength);
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    System.out.println("Train created successfully with ID: " + trainId);
                    return trainId;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating train: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
        
        return -1;
    }
    
    /**
     * Gets the next available train ID.
     */
    private int getNextTrainId(Connection conn) throws SQLException {
        String sql = "SELECT NVL(MAX(train_id), 0) + 1 FROM Train";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 1; // Default if table is empty
    }
    
    /**
     * Associates a locomotive with a train.
     * 
     * @param trainId the train ID
     * @param operatorId the operator ID
     * @param locomotiveId the locomotive ID
     * @return true if successful
     */
    public boolean addLocomotiveToTrain(int trainId, String operatorId, int locomotiveId) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
            // Ensure auto-commit is enabled for this operation
            conn.setAutoCommit(true);
            
            String sql = "INSERT INTO Locomotive_Train (Locomotivelocomotive_id, Traintrain_id, " +
                        "Trainoperator_id, Adddate) VALUES (?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, locomotiveId);
                stmt.setInt(2, trainId);
                stmt.setString(3, operatorId);
                stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    System.out.println("Locomotive " + locomotiveId + " added to train " + trainId);
                    return true;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error adding locomotive to train: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
        
        return false;
    }
    
    /**
     * Assigns a route (freight) to a train by updating the Freight table and FreighTrain table.
     * 
     * @param routeId the route ID (freight_id)
     * @param trainId the train ID
     * @param operatorId the operator ID
     * @param locomotiveId the locomotive ID (first locomotive)
     * @return true if successful
     */
    public boolean assignRouteToTrain(int routeId, int trainId, String operatorId, int locomotiveId) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
            conn.setAutoCommit(false);
            
            // 1. Update Freight table
            String updateFreightSql = "UPDATE Freight SET FreighTrainTraintrain_id = ? WHERE freight_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateFreightSql)) {
                stmt.setInt(1, trainId);
                stmt.setInt(2, routeId);
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    conn.rollback();
                    System.err.println("Failed to update Freight table");
                    return false;
                }
            }
            
            // 2. Update or insert into FreighTrain table
            // First, try to delete existing entry (if route was assigned to dummy train)
            String deleteFreighTrainSql = "DELETE FROM FreighTrain WHERE Freightfreight_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteFreighTrainSql)) {
                stmt.setInt(1, routeId);
                stmt.executeUpdate();
            }
            
            // Insert new entry
            String insertFreighTrainSql = "INSERT INTO FreighTrain (Freightfreight_id, Traintrain_id, " +
                                        "TrainLocomotivelocomotive_id, Trainoperator_id) " +
                                        "VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertFreighTrainSql)) {
                stmt.setInt(1, routeId);
                stmt.setInt(2, trainId);
                stmt.setInt(3, locomotiveId);
                stmt.setString(4, operatorId);
                stmt.executeUpdate();
            }
            
            conn.commit();
            System.out.println("Route " + routeId + " assigned to train " + trainId);
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error assigning route to train: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error rolling back: " + rollbackEx.getMessage());
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
        
        return false;
    }
    
    /**
     * Creates train route entries in Train_route table.
     * 
     * @param trainId the train ID
     * @param operatorId the operator ID
     * @param locomotiveId the locomotive ID (first locomotive)
     * @param stationIds list of facility IDs in order
     * @return true if successful
     */
    public boolean createTrainRoute(int trainId, String operatorId, int locomotiveId, List<Integer> stationIds) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
            conn.setAutoCommit(false);
            
            String sql = "INSERT INTO Train_route (Facility_id, Train_id, \"Order\", " +
                        "TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, " +
                        "Arrival_Time, RouteDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int order = 1;
                Timestamp routeDate = Timestamp.valueOf(LocalDateTime.now());
                
                for (Integer facilityId : stationIds) {
                    stmt.setInt(1, facilityId);
                    stmt.setInt(2, trainId);
                    stmt.setInt(3, order++);
                    stmt.setInt(4, locomotiveId);
                    stmt.setString(5, operatorId);
                    stmt.setInt(6, 0); // departure_time placeholder
                    stmt.setInt(7, 0); // arrival_time placeholder
                    stmt.setTimestamp(8, routeDate);
                    
                    stmt.executeUpdate();
                }
                
                conn.commit();
                System.out.println("Train route created with " + stationIds.size() + " stations");
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating train route: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error rolling back: " + rollbackEx.getMessage());
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
        
        return false;
    }
    
    /**
     * Creates a train and assigns it to a route in a single transaction.
     * This ensures all operations are committed together or rolled back together.
     * 
     * @param operatorId the operator ID
     * @param maxLength the maximum train length
     * @param locomotiveIds list of locomotive IDs to add
     * @param routeId the route ID (freight_id) to assign
     * @param stationIds list of facility IDs in order for the route
     * @return the created train ID, or -1 if failed
     */
    public int createTrainAndAssignRouteInTransaction(String operatorId, double maxLength,
                                                      List<Integer> locomotiveIds, int routeId,
                                                      List<Integer> stationIds) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
            conn.setAutoCommit(false); // Disable auto-commit for transaction control
            
            try {
                // 1. Get next train ID
                int trainId = getNextTrainId(conn);
                
                // 2. Create train
                String createTrainSql = "INSERT INTO Train (train_id, operator_id, max_length) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(createTrainSql)) {
                    stmt.setInt(1, trainId);
                    stmt.setString(2, operatorId);
                    stmt.setDouble(3, maxLength);
                    stmt.executeUpdate();
                }
                
                // 3. Add locomotives
                String addLocomotiveSql = "INSERT INTO Locomotive_Train (Locomotivelocomotive_id, Traintrain_id, " +
                                         "Trainoperator_id, Adddate) VALUES (?, ?, ?, ?)";
                for (int locomotiveId : locomotiveIds) {
                    try (PreparedStatement stmt = conn.prepareStatement(addLocomotiveSql)) {
                        stmt.setInt(1, locomotiveId);
                        stmt.setInt(2, trainId);
                        stmt.setString(3, operatorId);
                        stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                        stmt.executeUpdate();
                    }
                }
                
                // 4. Update Freight table
                String updateFreightSql = "UPDATE Freight SET FreighTrainTraintrain_id = ? WHERE freight_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateFreightSql)) {
                    stmt.setInt(1, trainId);
                    stmt.setInt(2, routeId);
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected == 0) {
                        throw new SQLException("Failed to update Freight table - route not found");
                    }
                }
                
                // 5. Delete old FreighTrain entry (if exists)
                String deleteFreighTrainSql = "DELETE FROM FreighTrain WHERE Freightfreight_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteFreighTrainSql)) {
                    stmt.setInt(1, routeId);
                    stmt.executeUpdate();
                }
                
                // 6. Insert new FreighTrain entry
                int firstLocomotiveId = locomotiveIds.get(0);
                String insertFreighTrainSql = "INSERT INTO FreighTrain (Freightfreight_id, Traintrain_id, " +
                                            "TrainLocomotivelocomotive_id, Trainoperator_id) " +
                                            "VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertFreighTrainSql)) {
                    stmt.setInt(1, routeId);
                    stmt.setInt(2, trainId);
                    stmt.setInt(3, firstLocomotiveId);
                    stmt.setString(4, operatorId);
                    stmt.executeUpdate();
                }
                
                // 7. Create train route entries
                String insertRouteSql = "INSERT INTO Train_route (Facility_id, Train_id, \"Order\", " +
                                      "TrainLocomotivelocomotive_id, Trainoperator_id, departure_time, " +
                                      "Arrival_Time, RouteDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                Timestamp routeDate = Timestamp.valueOf(LocalDateTime.now());
                int order = 1;
                for (Integer facilityId : stationIds) {
                    try (PreparedStatement stmt = conn.prepareStatement(insertRouteSql)) {
                        stmt.setInt(1, facilityId);
                        stmt.setInt(2, trainId);
                        stmt.setInt(3, order++);
                        stmt.setInt(4, firstLocomotiveId);
                        stmt.setString(5, operatorId);
                        stmt.setInt(6, 0); // departure_time placeholder
                        stmt.setInt(7, 0); // arrival_time placeholder
                        stmt.setTimestamp(8, routeDate);
                        stmt.executeUpdate();
                    }
                }
                
                // Commit all changes
                conn.commit();
                System.out.println("Train " + trainId + " created and assigned to route " + routeId + " successfully");
                return trainId;
                
            } catch (SQLException e) {
                // Rollback on any error
                if (conn != null) {
                    conn.rollback();
                    System.err.println("Transaction rolled back due to error: " + e.getMessage());
                }
                throw e;
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating train and assigning route: " + e.getMessage());
            e.printStackTrace();
            return -1;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset to default
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Represents a locomotive with status information.
     */
    public static class LocomotiveWithStatus {
        private Locomotive locomotive;
        private String status; // "IN_TRANSIT" or "PARKED"
        private String locationInfo; // Destination name (if in transit) or station name (if parked)
        private Double distanceFromStart; // Distance from route start (only for parked)
        
        public LocomotiveWithStatus(Locomotive locomotive, String status, String locationInfo, Double distanceFromStart) {
            this.locomotive = locomotive;
            this.status = status;
            this.locationInfo = locationInfo;
            this.distanceFromStart = distanceFromStart;
        }
        
        public Locomotive getLocomotive() { return locomotive; }
        public String getStatus() { return status; }
        public String getLocationInfo() { return locationInfo; }
        public Double getDistanceFromStart() { return distanceFromStart; }
    }
    
    /**
     * Represents a wagon with status information.
     */
    public static class WagonWithStatus {
        private int wagonId;
        private String cargoType;
        private String status; // "IN_TRANSIT" or "PARKED"
        private String locationInfo; // Destination name (if in transit) or station name (if parked)
        private Double distanceFromStart; // Distance from route start (only for parked)
        
        public WagonWithStatus(int wagonId, String cargoType, String status, String locationInfo, Double distanceFromStart) {
            this.wagonId = wagonId;
            this.cargoType = cargoType;
            this.status = status;
            this.locationInfo = locationInfo;
            this.distanceFromStart = distanceFromStart;
        }
        
        public int getWagonId() { return wagonId; }
        public String getCargoType() { return cargoType; }
        public String getStatus() { return status; }
        public String getLocationInfo() { return locationInfo; }
        public Double getDistanceFromStart() { return distanceFromStart; }
    }
    
    /**
     * Gets locomotives with status information for a given route.
     * Locomotives "in transit" show final destination, "parked" show station and distance from route start.
     * 
     * @param routeStartFacilityId the starting facility ID of the route
     * @param routeEndFacilityId the ending facility ID of the route (final destination)
     * @param allLocomotives list of all locomotives to check
     * @return list of locomotives with status information, sorted by status (in transit first) and distance (descending for parked)
     */
    public List<LocomotiveWithStatus> getLocomotivesWithStatus(int routeStartFacilityId, int routeEndFacilityId, 
                                                               List<Locomotive> allLocomotives) {
        List<LocomotiveWithStatus> result = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD)) {
            for (Locomotive loco : allLocomotives) {
                int locomotiveId = Integer.parseInt(loco.getId());
                
                // Check if locomotive is in transit (associated with an active train route)
                String inTransitQuery = "SELECT MAX(tr.Facility_id) as final_destination_id, " +
                                       "MAX(f.name) as final_destination_name " +
                                       "FROM Locomotive_Train lt " +
                                       "JOIN Train_route tr ON lt.Traintrain_id = tr.Train_id " +
                                       "  AND lt.Trainoperator_id = tr.Trainoperator_id " +
                                       "JOIN Facility f ON tr.Facility_id = f.facility_id " +
                                       "WHERE lt.Locomotivelocomotive_id = ? " +
                                       "  AND tr.\"Order\" = (SELECT MAX(\"Order\") FROM Train_route " +
                                       "                      WHERE Train_id = tr.Train_id " +
                                       "                        AND Trainoperator_id = tr.Trainoperator_id)";
                
                try (PreparedStatement stmt = conn.prepareStatement(inTransitQuery)) {
                    stmt.setInt(1, locomotiveId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next() && rs.getObject("final_destination_id") != null) {
                            // Locomotive is in transit
                            String destinationName = rs.getString("final_destination_name");
                            result.add(new LocomotiveWithStatus(loco, "IN_TRANSIT", destinationName, null));
                            continue;
                        }
                    }
                }
                
                // Locomotive is parked - get its location (assume at route start for simplicity)
                // In a real system, this would query a location table
                String parkedLocationQuery = "SELECT name FROM Facility WHERE facility_id = ?";
                String parkedLocation = "Unknown";
                try (PreparedStatement stmt = conn.prepareStatement(parkedLocationQuery)) {
                    stmt.setInt(1, routeStartFacilityId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            parkedLocation = rs.getString("name");
                        }
                    }
                }
                
                // Calculate distance from route start (simplified - using direct segment if available)
                double distance = calculateDistanceFromStart(conn, routeStartFacilityId, routeStartFacilityId);
                
                result.add(new LocomotiveWithStatus(loco, "PARKED", parkedLocation, distance));
            }
            
            // Sort: IN_TRANSIT first, then PARKED by distance descending
            result.sort((a, b) -> {
                if (!a.getStatus().equals(b.getStatus())) {
                    return a.getStatus().equals("IN_TRANSIT") ? -1 : 1;
                }
                if (a.getStatus().equals("PARKED") && a.getDistanceFromStart() != null && b.getDistanceFromStart() != null) {
                    return Double.compare(b.getDistanceFromStart(), a.getDistanceFromStart()); // Descending
                }
                return 0;
            });
            
        } catch (SQLException e) {
            System.err.println("Error getting locomotives with status: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Gets wagons with status information for a given route.
     * Wagons "in transit" show final destination, "parked" show station and distance from route start.
     * 
     * @param routeStartFacilityId the starting facility ID of the route
     * @param routeEndFacilityId the ending facility ID of the route (final destination)
     * @param wagonIds list of wagon IDs to check
     * @return list of wagons with status information, sorted by status (in transit first) and distance (descending for parked)
     */
    public List<WagonWithStatus> getWagonsWithStatus(int routeStartFacilityId, int routeEndFacilityId,
                                                     List<Integer> wagonIds) {
        List<WagonWithStatus> result = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD)) {
            for (Integer wagonId : wagonIds) {
                // Get wagon cargo type
                String cargoType = "Unknown";
                String cargoQuery = "SELECT wm.WagonTypetype_id, wt.type_name as cargo_type " +
                                   "FROM Wagon w " +
                                   "JOIN WagonModel wm ON w.WagonModelmodelID = wm.modelID " +
                                   "JOIN WagonType wt ON wm.WagonTypetype_id = wt.type_id " +
                                   "WHERE w.wagon_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(cargoQuery)) {
                    stmt.setInt(1, wagonId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            cargoType = rs.getString("cargo_type");
                        }
                    }
                }
                
                // Check if wagon is in transit (associated with a freight that has an active train)
                String inTransitQuery = "SELECT MAX(tr.Facility_id) as final_destination_id, " +
                                       "MAX(f.name) as final_destination_name " +
                                       "FROM Wagon_In_Freight wif " +
                                       "JOIN Freight fr ON wif.Freight_id = fr.freight_id " +
                                       "JOIN Train_route tr ON fr.FreighTrainTraintrain_id = tr.Train_id " +
                                       "  AND fr.FreighTrainTraintrain_id != 9999 " +
                                       "JOIN Facility f ON tr.Facility_id = f.facility_id " +
                                       "WHERE wif.Wagon_id = ? " +
                                       "  AND tr.\"Order\" = (SELECT MAX(\"Order\") FROM Train_route " +
                                       "                      WHERE Train_id = tr.Train_id " +
                                       "                        AND Trainoperator_id = tr.Trainoperator_id)";
                
                try (PreparedStatement stmt = conn.prepareStatement(inTransitQuery)) {
                    stmt.setInt(1, wagonId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next() && rs.getObject("final_destination_id") != null) {
                            // Wagon is in transit
                            String destinationName = rs.getString("final_destination_name");
                            result.add(new WagonWithStatus(wagonId, cargoType, "IN_TRANSIT", destinationName, null));
                            continue;
                        }
                    }
                }
                
                // Wagon is parked - get its location (assume at route start for simplicity)
                String parkedLocationQuery = "SELECT name FROM Facility WHERE facility_id = ?";
                String parkedLocation = "Unknown";
                try (PreparedStatement stmt = conn.prepareStatement(parkedLocationQuery)) {
                    stmt.setInt(1, routeStartFacilityId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            parkedLocation = rs.getString("name");
                        }
                    }
                }
                
                // Calculate distance from route start
                double distance = calculateDistanceFromStart(conn, routeStartFacilityId, routeStartFacilityId);
                
                result.add(new WagonWithStatus(wagonId, cargoType, "PARKED", parkedLocation, distance));
            }
            
            // Sort: IN_TRANSIT first, then PARKED by distance descending
            result.sort((a, b) -> {
                if (!a.getStatus().equals(b.getStatus())) {
                    return a.getStatus().equals("IN_TRANSIT") ? -1 : 1;
                }
                if (a.getStatus().equals("PARKED") && a.getDistanceFromStart() != null && b.getDistanceFromStart() != null) {
                    return Double.compare(b.getDistanceFromStart(), a.getDistanceFromStart()); // Descending
                }
                return 0;
            });
            
        } catch (SQLException e) {
            System.err.println("Error getting wagons with status: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Calculates distance from route start to a given facility.
     * Uses segment lengths if available, otherwise returns 0.
     */
    private double calculateDistanceFromStart(Connection conn, int startFacilityId, int targetFacilityId) {
        if (startFacilityId == targetFacilityId) {
            return 0.0;
        }
        
        // Try to find a direct segment connection
        String segmentQuery = "SELECT SUM(s.length_m) / 1000.0 as distance_km " +
                             "FROM Line l " +
                             "JOIN Segment s ON l.line_id = s.line_id " +
                             "WHERE (l.start_facility_id = ? AND l.end_facility_id = ?) " +
                             "   OR (l.start_facility_id = ? AND l.end_facility_id = ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(segmentQuery)) {
            stmt.setInt(1, startFacilityId);
            stmt.setInt(2, targetFacilityId);
            stmt.setInt(3, targetFacilityId);
            stmt.setInt(4, startFacilityId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double distance = rs.getDouble("distance_km");
                    if (!rs.wasNull() && distance > 0) {
                        return distance;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating distance: " + e.getMessage());
        }
        
        return 0.0; // Default if no connection found
    }
    
}