package Lapr.DataBaseAssociation;

import Sprint1.Domain.Wagon;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Access Object Data for Wagon.
 * Retrieves wagon data from Oracle database.
 * 
 * @author Development Team
 * @version 1.0
 */
public class WagonAOD {
    private static final String USERNAME = "System";
    private static final String PASSWORD = "G0442526BD";
    private static final String DATABASE = "jdbc:oracle:thin:@vsgate-s1.dei.isep.ipp.pt:10715:XE";

    /**
     * Gets all wagons from database.
     * Loads wagons with their type, empty weight, and max load capacity.
     * 
     * @return list of all wagons from database
     */
    public List<Wagon> getAllWagons() {
        List<Wagon> wagons = new ArrayList<>();
        
        // Query to get all wagons with their model info (weight and payload)
        String sql = "SELECT " +
                     "  w.wagon_id, " +
                     "  wt.type_name, " +
                     "  wm.weight_t, " +
                     "  wm.payload " +
                     "FROM Wagon w " +
                     "JOIN WagonModel wm ON w.WagonModelmodelID = wm.modelID " +
                     "JOIN WagonType wt ON wm.WagonTypetype_id = wt.type_id " +
                     "ORDER BY w.wagon_id";
        
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                try {
                    String wagonId = String.valueOf(rs.getInt("wagon_id"));
                    String type = rs.getString("type_name");
                    double emptyWeightKg = rs.getDouble("weight_t") * 1000.0; // Convert tons to kg
                    double maxLoadWeightKg = rs.getDouble("payload") * 1000.0; // Convert tons to kg
                    
                    Wagon wagon = new Wagon(wagonId, type, emptyWeightKg, maxLoadWeightKg);
                    wagons.add(wagon);
                } catch (Exception e) {
                    System.err.println("Error creating wagon from database row: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("Total wagons loaded from database: " + wagons.size());
            
        } catch (SQLException e) {
            System.err.println("Error retrieving wagons from database: " + e.getMessage());
            e.printStackTrace();
        }
        
        return wagons;
    }
}

