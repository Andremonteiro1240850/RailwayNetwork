package Lapr.DataBaseAssociation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for retrieving available wagons from Oracle database.
 * 
 * An available wagon is defined as:
 * - A wagon NOT associated with any Freight that is assigned to a Train.
 * 
 * Wagons are grouped and ordered by cargo type (WagonType).
 * 
 * @author Development Team
 * @version 1.0
 */
public class AvailableWagonDAO {
    private static final String USERNAME = "System";
    private static final String PASSWORD = "G0442526BD";
    private static final String DATABASE = "jdbc:oracle:thin:@vsgate-s1.dei.isep.ipp.pt:10715:XE";

    /**
     * Represents an available wagon with its cargo type information.
     */
    public static class AvailableWagon {
        private final int wagonId;
        private final String cargoType; // WagonType name
        private final String wagonTypeId;
        private final double emptyWeightKg;
        private final double maxLoadWeightKg;
        private final int modelId;
        private final String modelName;

        public AvailableWagon(int wagonId, String cargoType, String wagonTypeId, 
                            double emptyWeightKg, double maxLoadWeightKg, 
                            int modelId, String modelName) {
            this.wagonId = wagonId;
            this.cargoType = cargoType;
            this.wagonTypeId = wagonTypeId;
            this.emptyWeightKg = emptyWeightKg;
            this.maxLoadWeightKg = maxLoadWeightKg;
            this.modelId = modelId;
            this.modelName = modelName;
        }

        public int getWagonId() { return wagonId; }
        public String getCargoType() { return cargoType; }
        public String getWagonTypeId() { return wagonTypeId; }
        public double getEmptyWeightKg() { return emptyWeightKg; }
        public double getMaxLoadWeightKg() { return maxLoadWeightKg; }
        public int getModelId() { return modelId; }
        public String getModelName() { return modelName; }
    }

    /**
     * Gets all available wagons from database, grouped and ordered by cargo type.
     * 
     * A wagon is available if:
     * - It is NOT in Wagon_In_Freight, OR
     * - It is in Wagon_In_Freight but the Freight is NOT in FreighTrain (not assigned to a Train)
     * 
     * @return list of available wagons, ordered by cargo type (WagonType)
     */
    public List<AvailableWagon> getAvailableWagons() {
        List<AvailableWagon> wagons = new ArrayList<>();
        
        // Query to get available wagons:
        // Wagons that are either:
        // 1. Not in Wagon_In_Freight, OR
        // 2. In Wagon_In_Freight but the Freight is NOT in FreighTrain
        String sql = "SELECT DISTINCT " +
                     "  w.wagon_id, " +
                     "  wt.type_name AS cargo_type, " +
                     "  wt.type_id AS wagon_type_id, " +
                     "  wm.weight_t * 1000 AS empty_weight_kg, " + // Convert tons to kg
                     "  wm.payload * 1000 AS max_load_weight_kg, " + // Convert tons to kg
                     "  wm.modelID AS model_id, " +
                     "  wm.name AS model_name " +
                     "FROM Wagon w " +
                     "JOIN WagonModel wm ON w.WagonModelmodelID = wm.modelID " +
                     "JOIN WagonType wt ON wm.WagonTypetype_id = wt.type_id " +
                     "WHERE w.wagon_id NOT IN ( " +
                     "  SELECT DISTINCT wif.Wagon_id " +
                     "  FROM Wagon_In_Freight wif " +
                     "  JOIN FreighTrain ft ON wif.Freight_id = ft.Freightfreight_id " +
                     ") " +
                     "ORDER BY wt.type_name, w.wagon_id";
        
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                try {
                    int wagonId = rs.getInt("wagon_id");
                    String cargoType = rs.getString("cargo_type");
                    String wagonTypeId = String.valueOf(rs.getInt("wagon_type_id"));
                    double emptyWeightKg = rs.getDouble("empty_weight_kg");
                    double maxLoadWeightKg = rs.getDouble("max_load_weight_kg");
                    int modelId = rs.getInt("model_id");
                    String modelName = rs.getString("model_name");
                    
                    AvailableWagon wagon = new AvailableWagon(
                        wagonId, cargoType, wagonTypeId,
                        emptyWeightKg, maxLoadWeightKg,
                        modelId, modelName
                    );
                    wagons.add(wagon);
                } catch (Exception e) {
                    System.err.println("Error creating available wagon from database row: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("Total available wagons loaded from database: " + wagons.size());
            
        } catch (SQLException e) {
            System.err.println("Error retrieving available wagons from database: " + e.getMessage());
            e.printStackTrace();
        }
        
        return wagons;
    }

    /**
     * Gets available wagons grouped by cargo type.
     * 
     * @return map of cargo type to list of available wagons
     */
    public java.util.Map<String, List<AvailableWagon>> getAvailableWagonsByCargoType() {
        List<AvailableWagon> allWagons = getAvailableWagons();
        java.util.Map<String, List<AvailableWagon>> grouped = new java.util.LinkedHashMap<>();
        
        for (AvailableWagon wagon : allWagons) {
            String cargoType = wagon.getCargoType();
            grouped.computeIfAbsent(cargoType, k -> new ArrayList<>()).add(wagon);
        }
        
        return grouped;
    }
}

