package Lapr.DataBaseAssociation;

import Lapr.Domain.Freight;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FreightAOD {
    private static final String USERNAME = "System";
    private static final String PASSWORD = "G0442526BD";
    private static final String DATABASE = "jdbc:oracle:thin:@vsgate-s1.dei.isep.ipp.pt:10715:XE";

    /**
     * Gets all freights from database.
     * 
     * @return list of all freights
     */
    public List<Freight> getAllFreights() {
        List<Freight> freights = new ArrayList<>();
        
        String sql = "SELECT FREIGHT_ID, CARGOTYPE, TRAINTRAIN_ID, TRAINLOCOMOTIVELOCOMOTIVE_ID, TRAINWAGON_ID " +
                     "FROM Freight " +
                     "ORDER BY FREIGHT_ID";
        
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Freight freight = new Freight();
                freight.setId(rs.getInt("FREIGHT_ID"));
                freight.setCargoType(rs.getString("CARGOTYPE"));
                freight.setTrainID(rs.getInt("TRAINTRAIN_ID"));
                freight.setLocomotiveID(rs.getInt("TRAINLOCOMOTIVELOCOMOTIVE_ID"));
                freight.setWagonID(rs.getInt("TRAINWAGON_ID"));
                freights.add(freight);
            }
            
            System.out.println("Loaded " + freights.size() + " freights from database");
            
        } catch (SQLException e) {
            System.err.println("Error retrieving freights from database: " + e.getMessage());
            e.printStackTrace();
        }

        return freights;
    }

    /**
     * Gets all pending freights (not assigned to any route).
     * 
     * @return list of pending freights
     */
    public List<Freight> getPendingFreights() {
        List<Freight> freights = new ArrayList<>();
        
        String sql = "SELECT FREIGHT_ID, CARGOTYPE, TRAINTRAIN_ID, TRAINLOCOMOTIVELOCOMOTIVE_ID, TRAINWAGON_ID " +
                     "FROM Freight " +
                     "WHERE TRAINTRAIN_ID IS NULL OR TRAINTRAIN_ID = 0 " +
                     "ORDER BY FREIGHT_ID";
        
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Freight freight = new Freight();
                freight.setId(rs.getInt("FREIGHT_ID"));
                freight.setCargoType(rs.getString("CARGOTYPE"));
                freight.setTrainID(rs.getInt("TRAINTRAIN_ID"));
                freight.setLocomotiveID(rs.getInt("TRAINLOCOMOTIVELOCOMOTIVE_ID"));
                freight.setWagonID(rs.getInt("TRAINWAGON_ID"));
                freights.add(freight);
            }
            
            System.out.println("Loaded " + freights.size() + " pending freights from database");
            
        } catch (SQLException e) {
            System.err.println("Error retrieving pending freights from database: " + e.getMessage());
            e.printStackTrace();
        }
        
        return freights;
    }
    
}
