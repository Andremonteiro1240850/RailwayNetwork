package Lapr.DataBaseAssociation;

import Lapr.Domain.Freight;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FreightAOD {
    private static final String USERNAME = "System";
    private static final String PASSWORD = "G0442526BD";
    private static final String DATABASE = "jdbc:oracle:thin:@vsgate-s1.dei.isep.ipp.pt:10715:XE";

    public List<Freight> getAllFreights() {
        List<Freight> freights = new ArrayList<>();
        String sql = "SELECT FREIGHT_ID, CARGOTYPE, TRAINTRAIN_ID, TRAINLOCOMOTIVELOCOMOTIVE_ID, TRAINWAGON_ID FROM FREIGHT";


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

        } catch (SQLException e) {
            System.err.println("Erro ao buscar freights: " + e.getMessage());
            e.printStackTrace();
        }

        return freights;
    }

    /**
     * Gets all pending freights (not assigned to any route) using PL/SQL function.
     * Falls back to direct SQL if PL/SQL function is not available.
     * 
     * @return list of pending freights
     */
    public List<Freight> getPendingFreights() {
        List<Freight> freights = new ArrayList<>();
        
        // Try PL/SQL function first (as per requirements)
        String plsqlCall = "{ ? = call GET_PENDING_FREIGHTS() }";
        
        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
             CallableStatement stmt = conn.prepareCall(plsqlCall)) {
            
            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.execute();
            
            ResultSet rs = (ResultSet) stmt.getObject(1);
            
            while (rs.next()) {
                Freight freight = new Freight();
                freight.setId(rs.getInt("FREIGHT_ID"));
                freight.setCargoType(rs.getString("CARGOTYPE"));
                freight.setTrainID(rs.getInt("TRAINTRAIN_ID"));
                freight.setLocomotiveID(rs.getInt("TRAINLOCOMOTIVELOCOMOTIVE_ID"));
                freight.setWagonID(rs.getInt("TRAINWAGON_ID"));
                freights.add(freight);
            }
            
            System.out.println("Loaded " + freights.size() + " pending freights from database via PL/SQL function");
            
        } catch (SQLException e) {
            // If PL/SQL function doesn't exist, fallback to direct SQL
            System.err.println("PL/SQL function GET_PENDING_FREIGHTS not available, using direct SQL: " + e.getMessage());
            return getPendingFreightsFallback();
        }
        
        return freights;
    }
    
    /**
     * Fallback method using direct SQL query if PL/SQL function is not available.
     * Gets freights that are not assigned to any route (TRAINTRAIN_ID is NULL or 0).
     */
    private List<Freight> getPendingFreightsFallback() {
        List<Freight> freights = new ArrayList<>();
        String sql = "SELECT FREIGHT_ID, CARGOTYPE, TRAINTRAIN_ID, TRAINLOCOMOTIVELOCOMOTIVE_ID, TRAINWAGON_ID " +
                     "FROM FREIGHT " +
                     "WHERE TRAINTRAIN_ID IS NULL OR TRAINTRAIN_ID = 0";

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

            System.out.println("Loaded " + freights.size() + " pending freights from database via direct SQL");

        } catch (SQLException e) {
            System.err.println("Error retrieving pending freights: " + e.getMessage());
            e.printStackTrace();
        }

        return freights;
    }



}
