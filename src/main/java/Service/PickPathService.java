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
     * Gera percursos para ambas as estratégias (ASC_SWEEP e NEAREST_NEIGHBOUR).
     * IMPORTANTE: Assume que os stops já foram consolidados (sem duplicados)!
     * Cria um percurso para cada estratégia e retorna ambos num Map.
     * 
     * Delega os cálculos para o Controller que contém os algoritmos.
     *
     * @param stops Lista de stops já consolidados (sem duplicados)
     * @return Map com ambos os percursos: chave=estratégia, valor=PickPath calculado
     */
    public static Map<PathStrategy, PickPath> generateBothPaths(List<PickStop> stops) {
        Map<PathStrategy, PickPath> results = new HashMap<>();
        
        PickPathController controller = new PickPathController();

        PickPath ascSweepPath = controller.createCalculatedPath(PathStrategy.ASC_SWEEP, stops);
        results.put(PathStrategy.ASC_SWEEP, ascSweepPath);

        PickPath nearestNeighbourPath = controller.createCalculatedPath(PathStrategy.NEAREST_NEIGHBOUR, stops);
        results.put(PathStrategy.NEAREST_NEIGHBOUR, nearestNeighbourPath);

        return results;
    }

    /**
     * Consolida stops duplicados (mesmo aisle/bay) somando as quantidades.
     * Delega para o domínio (PickStop) que contém a regra de negócio.
     * 
     * Exemplo: [(1,2,qty:5), (1,2,qty:3), (2,3,qty:2)] → [(1,2,qty:8), (2,3,qty:2)]
     *
     * @param originalStops Lista original com possíveis duplicados
     * @return Lista consolidada sem duplicados (quantidades somadas)
     */
    public static List<PickStop> consolidateStops(List<PickStop> originalStops) {
        return PickStop.consolidateStops(originalStops);
    }

    /**
     * Método principal do USEI04 - Orquestra todo o processo de sequenciação.
     * Este é o método que chama todos os outros métodos na ordem correta.
     * 
     * Fluxo:
     * 1. Consolida stops duplicados
     * 2. Gera percursos para ambas estratégias (que aplica estratégia + calcula distância)
     * 3. Retorna Map com ambos os resultados
     *
     * @param inputStops Lista de stops (pode ter duplicados)
     * @return Map com ambos os percursos calculados (ASC_SWEEP e NEAREST_NEIGHBOUR)
     */
    public static Map<PathStrategy, PickPath> processPickPathSequencing(List<PickStop> inputStops) {
        List<PickStop> consolidatedStops = consolidateStops(inputStops);

        return generateBothPaths(consolidatedStops);
    }
}
