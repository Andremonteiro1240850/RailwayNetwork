package Lapr.DataBaseAssociation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Access Object Data for Vehicles (Locomotives and Wagons).
 * Retrieves vehicle data from Oracle database using PL/SQL functions.
 * Distinguishes between vehicles in transit and parked vehicles.
 * 
 * For USLP09: Assemble and Assign Train to Route
 * 
 * @author Development Team
 * @version 1.0
 */
public class VehicleAOD {
    private static final String USERNAME = "System";
    private static final String PASSWORD = "G0442526BD";
    private static final String DATABASE = "jdbc:oracle:thin:@vsgate-s1.dei.isep.ipp.pt:10715:XE";

    /**
     * Represents a vehicle (locomotive or wagon) with its status information.
     */
    public static class VehicleInfo {
        private final String vehicleId;
        private final String vehicleType; // "LOCOMOTIVE" or "WAGON"
        private final String status; // "IN_TRANSIT" or "PARKED"
        private final String finalDestination; // For in transit vehicles: route final destination
        private final String parkedStation; // For parked vehicles: station where parked
        private final double distanceFromStart; // Distance from route start point (for parked vehicles)

        public VehicleInfo(String vehicleId, String vehicleType, String status, 
                          String finalDestination, String parkedStation, double distanceFromStart) {
            this.vehicleId = vehicleId;
            this.vehicleType = vehicleType;
            this.status = status;
            this.finalDestination = finalDestination;
            this.parkedStation = parkedStation;
            this.distanceFromStart = distanceFromStart;
        }

        public String getVehicleId() { return vehicleId; }
        public String getVehicleType() { return vehicleType; }
        public String getStatus() { return status; }
        public String getFinalDestination() { return finalDestination; }
        public String getParkedStation() { return parkedStation; }
        public double getDistanceFromStart() { return distanceFromStart; }
    }

    /**
     * Gets all locomotives with their status (in transit or parked).
     * For in transit: shows final destination of the route.
     * For parked: shows station where parked.
     * 
     * @param routeStartStationId the starting station ID of the route (for distance calculation)
     * @return list of locomotive information
     */
    public List<VehicleInfo> getLocomotivesWithStatus(String routeStartStationId) {
        List<VehicleInfo> locomotives = new ArrayList<>();
        
        // PL/SQL function call to get locomotives with status
        // Note: This assumes a PL/SQL function exists. If not, we'll use a query.
        String sql = "{ ? = call GET_LOCOMOTIVES_WITH_STATUS(?) }";
        
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setString(2, routeStartStationId);
            stmt.execute();
            
            ResultSet rs = (ResultSet) stmt.getObject(1);
            
            while (rs.next()) {
                String locoId = String.valueOf(rs.getInt("locomotive_id"));
                String status = rs.getString("status"); // "IN_TRANSIT" or "PARKED"
                String finalDestination = rs.getString("final_destination");
                String parkedStation = rs.getString("parked_station");
                double distance = rs.getDouble("distance_from_start");
                
                locomotives.add(new VehicleInfo(locoId, "LOCOMOTIVE", status, 
                                               finalDestination, parkedStation, distance));
            }
            
        } catch (SQLException e) {
            // If PL/SQL function doesn't exist, fallback to direct query
            System.err.println("PL/SQL function not available, using direct query: " + e.getMessage());
            return getLocomotivesWithStatusFallback(routeStartStationId);
        }
        
        return locomotives;
    }

    /**
     * Fallback method using direct SQL query if PL/SQL function is not available.
     */
    private List<VehicleInfo> getLocomotivesWithStatusFallback(String routeStartStationId) {
        List<VehicleInfo> locomotives = new ArrayList<>();
        
        // Query to get locomotives with their status
        // This is a simplified version - actual implementation depends on database schema
        String sql = "SELECT " +
                     "  l.locomotive_id, " +
                     "  CASE " +
                     "    WHEN t.train_id IS NOT NULL THEN 'IN_TRANSIT' " +
                     "    ELSE 'PARKED' " +
                     "  END AS status, " +
                     "  r.final_destination, " +
                     "  f.facility_id AS parked_station, " +
                     "  CASE " +
                     "    WHEN t.train_id IS NOT NULL THEN 0 " +
                     "    ELSE CALCULATE_DISTANCE(?, f.facility_id) " +
                     "  END AS distance_from_start " +
                     "FROM Locomotive l " +
                     "LEFT JOIN Train_Locomotive tl ON l.locomotive_id = tl.Locomotivelocomotive_id " +
                     "LEFT JOIN Train t ON tl.Traintrain_id = t.train_id AND t.status = 'IN_TRANSIT' " +
                     "LEFT JOIN Route r ON t.route_id = r.route_id " +
                     "LEFT JOIN Facility f ON l.parked_at = f.facility_id " +
                     "ORDER BY " +
                     "  CASE WHEN t.train_id IS NOT NULL THEN 0 ELSE 1 END, " +
                     "  distance_from_start DESC";
        
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, routeStartStationId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String locoId = String.valueOf(rs.getInt("locomotive_id"));
                String status = rs.getString("status");
                String finalDestination = rs.getString("final_destination");
                String parkedStation = rs.getString("parked_station");
                double distance = rs.getDouble("distance_from_start");
                
                locomotives.add(new VehicleInfo(locoId, "LOCOMOTIVE", status, 
                                               finalDestination, parkedStation, distance));
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving locomotives with status: " + e.getMessage());
            e.printStackTrace();
        }
        
        return locomotives;
    }

    /**
     * Gets all wagons with their status (in transit or parked).
     * For in transit: shows final destination of the route.
     * For parked: shows station where parked, ordered by distance from route start (descending).
     * 
     * @param routeStartStationId the starting station ID of the route (for distance calculation)
     * @return list of wagon information, ordered by status (in transit first) and distance (descending for parked)
     */
    public List<VehicleInfo> getWagonsWithStatus(String routeStartStationId) {
        List<VehicleInfo> wagons = new ArrayList<>();
        
        // PL/SQL function call to get wagons with status
        String sql = "{ ? = call GET_WAGONS_WITH_STATUS(?) }";
        
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
             CallableStatement stmt = conn.prepareCall(sql)) {
            
            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setString(2, routeStartStationId);
            stmt.execute();
            
            ResultSet rs = (ResultSet) stmt.getObject(1);
            
            while (rs.next()) {
                String wagonId = String.valueOf(rs.getInt("wagon_id"));
                String status = rs.getString("status");
                String finalDestination = rs.getString("final_destination");
                String parkedStation = rs.getString("parked_station");
                double distance = rs.getDouble("distance_from_start");
                
                wagons.add(new VehicleInfo(wagonId, "WAGON", status, 
                                          finalDestination, parkedStation, distance));
            }
            
        } catch (SQLException e) {
            // If PL/SQL function doesn't exist, fallback to direct query
            System.err.println("PL/SQL function not available, using direct query: " + e.getMessage());
            return getWagonsWithStatusFallback(routeStartStationId);
        }
        
        return wagons;
    }

    /**
     * Fallback method using direct SQL query if PL/SQL function is not available.
     */
    private List<VehicleInfo> getWagonsWithStatusFallback(String routeStartStationId) {
        List<VehicleInfo> wagons = new ArrayList<>();
        
        String sql = "SELECT " +
                     "  w.wagon_id, " +
                     "  CASE " +
                     "    WHEN t.train_id IS NOT NULL THEN 'IN_TRANSIT' " +
                     "    ELSE 'PARKED' " +
                     "  END AS status, " +
                     "  r.final_destination, " +
                     "  f.facility_id AS parked_station, " +
                     "  CASE " +
                     "    WHEN t.train_id IS NOT NULL THEN 0 " +
                     "    ELSE CALCULATE_DISTANCE(?, f.facility_id) " +
                     "  END AS distance_from_start " +
                     "FROM Wagon w " +
                     "LEFT JOIN Train_Wagon tw ON w.wagon_id = tw.Wagonwagon_id " +
                     "LEFT JOIN Train t ON tw.Traintrain_id = t.train_id AND t.status = 'IN_TRANSIT' " +
                     "LEFT JOIN Route r ON t.route_id = r.route_id " +
                     "LEFT JOIN Facility f ON w.parked_at = f.facility_id " +
                     "ORDER BY " +
                     "  CASE WHEN t.train_id IS NOT NULL THEN 0 ELSE 1 END, " +
                     "  distance_from_start DESC";
        
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, routeStartStationId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String wagonId = String.valueOf(rs.getInt("wagon_id"));
                String status = rs.getString("status");
                String finalDestination = rs.getString("final_destination");
                String parkedStation = rs.getString("parked_station");
                double distance = rs.getDouble("distance_from_start");
                
                wagons.add(new VehicleInfo(wagonId, "WAGON", status, 
                                          finalDestination, parkedStation, distance));
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving wagons with status: " + e.getMessage());
            e.printStackTrace();
        }
        
        return wagons;
    }
}

