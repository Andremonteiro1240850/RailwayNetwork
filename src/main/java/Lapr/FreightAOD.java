package Lapr;

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



}
