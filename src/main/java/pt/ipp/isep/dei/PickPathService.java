package pt.ipp.isep.dei;

import Domain.ENUM.PathStrategy;
import Domain.PickPath;
import Domain.PickStop;

import java.util.HashMap;


import Domain.ENUM.PathStrategy;
import java.util.*;

/**
 * The type Pick path service.
 */
public class PickPathService {

    /**
     * Gera paths para ambas as estratégias a partir de uma lista de PickStops
     * NOTA: Assume que os stops já foram consolidados (merged)!
     *
     * @param stops the stops
     * @return the map
     */
    public static Map<PathStrategy, PickPath> generateBothPaths(List<PickStop> stops) {
            Map<PathStrategy, PickPath> results = new HashMap<>();

            // Estratégia A - ASC_SWEEP
            PickPath ascSweepPath = new PickPath(PathStrategy.ASC_SWEEP, stops);
            results.put(PathStrategy.ASC_SWEEP, ascSweepPath);

            // Estratégia B - NEAREST_NEIGHBOUR
            PickPath nearestNeighbourPath = new PickPath(PathStrategy.NEAREST_NEIGHBOUR, stops);
            results.put(PathStrategy.NEAREST_NEIGHBOUR, nearestNeighbourPath);

            return results;
        }

    /**
     * Consolida stops duplicados (mesmo aisle/bay) somando as quantidades
     * CRÍTICO: Faz o merge antes de calcular os paths!
     *
     * @param originalStops the original stops
     * @return the list
     */
    public static List<PickStop> consolidateStops(List<PickStop> originalStops) {
            Map<String, PickStop> consolidatedMap = new HashMap<>();

            for (PickStop stop : originalStops) {
                String key = stop.getAisle() + "-" + stop.getBay();

                if (consolidatedMap.containsKey(key)) {
                    // Já existe - soma a quantidade
                    PickStop existing = consolidatedMap.get(key);
                    existing.setTotalQty(existing.getTotalQty() + stop.getTotalQty());
                } else {
                    // Novo - cria uma cópia
                    consolidatedMap.put(key, new PickStop(
                            stop.getSeq(),
                            stop.getAisle(),
                            stop.getBay(),
                            stop.getTotalQty()
                    ));
                }
            }

            return new ArrayList<>(consolidatedMap.values());
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
