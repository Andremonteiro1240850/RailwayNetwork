package Lapr.DataBaseAssociation;

import Lapr.Domain.Train; // Supondo que existe uma classe Train no domínio
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TrainAOD {
    private static final String USERNAME = "System";
    private static final String PASSWORD = "G0442526BD";
    private static final String DATABASE = "jdbc:oracle:thin:@vsgate-s1.dei.isep.ipp.pt:10715:XE";

    public List<Train> getAllTrains() {
        List<Train> trains = new ArrayList<>();
        String sql = "SELECT TRAIN_ID, LOCOMOTIVELOCOMOTIVE_ID, WAGON_ID, OPERATOR_ID, DATE_D, TIME_T FROM TRAIN";

        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Train train = new Train();
                train.setTrainID(rs.getInt("TRAIN_ID"));
                train.setLocomotiveID(rs.getInt("LOCOMOTIVELOCOMOTIVE_ID"));
                train.setWagonID(rs.getInt("WAGON_ID"));
                train.setOperatorID(rs.getString("OPERATOR_ID"));
                train.setDate(rs.getString("DATE_D"));
                train.setTime(rs.getString("TIME_T"));
                trains.add(train);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar trains: " + e.getMessage());
            e.printStackTrace();
        }

        return trains;
    }
}