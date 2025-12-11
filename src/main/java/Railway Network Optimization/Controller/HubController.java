/**package Controller;

import Domain.RailwayGraph;
import Domain.Station;
import Repository.LineRepository;
import Repository.StationRepository;

import java.util.Comparator;
import java.util.List;
import java.util.function.BinaryOperator;

public class HubController {
    public HubController() {
    }

    //usamos um djikstra adaptado para o brandes (é diferente do original) para encontrar os caminhos mais curtos
    private static <Station> void dijkstraBrandes(
            RailwayGraph grafo,
            Station source,
            Comparator<Double> ce,
            BinaryOperator<Double> sum,
            Double zero,
            Double[] dist,
            int[] sigma,
            List<Station>[] pred
    )
        // TODO: Implementar algoritmo de Brandes
        // Por agora, retorna array vazio
        return new double[0.0];
}
 */
