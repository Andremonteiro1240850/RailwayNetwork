package Service;

import Domain.ENUM.PathStrategy;
import Domain.PickPath;
import Domain.PickStop;
import Controller.PickPathController;

import java.util.*;

/**
 * The type Pick path service.
 */
public class PickPathService {

    /**
     * Gera paths para ambas as estratégias a partir de uma lista de PickStops
     * NOTA: Assume que os stops já foram consolidados (merged)!
     * Delega os cálculos para o Controller
     *
     * @param stops the stops
     * @return the map
     */
    public static Map<PathStrategy, PickPath> generateBothPaths(List<PickStop> stops) {
        Map<PathStrategy, PickPath> results = new HashMap<>();
        
        // Criar instância do controller para usar os métodos de cálculo
        PickPathController controller = new PickPathController();

        // Estratégia A - ASC_SWEEP
        PickPath ascSweepPath = controller.createCalculatedPath(PathStrategy.ASC_SWEEP, stops);
        results.put(PathStrategy.ASC_SWEEP, ascSweepPath);

        // Estratégia B - NEAREST_NEIGHBOUR
        PickPath nearestNeighbourPath = controller.createCalculatedPath(PathStrategy.NEAREST_NEIGHBOUR, stops);
        results.put(PathStrategy.NEAREST_NEIGHBOUR, nearestNeighbourPath);

        return results;
    }

    /**
     * Consolida stops duplicados (mesmo aisle/bay) somando as quantidades
     * Delega para o domínio (PickStop) que contém a regra de negócio
     *
     * @param originalStops the original stops
     * @return the list
     */
    public static List<PickStop> consolidateStops(List<PickStop> originalStops) {
        return PickStop.consolidateStops(originalStops);
    }

    /**
     * Método principal para USEI04 - Processa completo
     *
     * @param inputStops the input stops
     * @return the map
     */
    public static Map<PathStrategy, PickPath> processPickPathSequencing(List<PickStop> inputStops) {
        // 1. Consolidar stops duplicados
        List<PickStop> consolidatedStops = consolidateStops(inputStops);

        // 2. Gerar ambos os paths
        return generateBothPaths(consolidatedStops);
    }
}
