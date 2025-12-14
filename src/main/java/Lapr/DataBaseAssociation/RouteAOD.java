package Lapr.DataBaseAssociation;

import Lapr.Domain.Route; // Supondo que existe uma classe Route no domínio
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RouteAOD {
    private static final String USERNAME = "System";
    private static final String PASSWORD = "G0442526BD";
    private static final String DATABASE = "jdbc:oracle:thin:@vsgate-s1.dei.isep.ipp.pt:10715:XE";

    public List<Route> getAllRoutes() {
        List<Route> routes = new ArrayList<>();
        String sql = "SELECT FACILITY_ID, TRAIN_ID, ORDER FROM TRAIN_ROUTE";

        try (Connection conn = DriverManager.getConnection(DATABASE, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Route route = new Route();
                route.setFacilityID(rs.getInt("FACILITY_ID"));
                route.setTrainID(rs.getInt("TRAIN_ID"));
                route.setOrder(rs.getInt("ORDER"));
                routes.add(route);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar routes: " + e.getMessage());
            e.printStackTrace();
        }

        return routes;
    }
}