package Controller;

import Domain.PickPath;
import Domain.PickStop;
import Domain.ENUM.PathStrategy;
import pt.ipp.isep.dei.PickPathService;

import java.util.List;
import java.util.Map;

/**
 * Controller para Pick Path Sequencing (USEI04)
 * Responsável por orquestrar as operações de sequenciação de picking
 */
public class PickPathController {

    /**
     * Processa a sequenciação de picking para uma lista de stops
     *
     * @param inputStops Lista de PickStops (pode ter duplicados)
     * @return Map com ambas as estratégias e seus resultados
     */
    public Map<PathStrategy, PickPath> processPickPathSequencing(List<PickStop> inputStops) {
        // Validação básica
        if (inputStops == null || inputStops.isEmpty()) {
            throw new IllegalArgumentException("Stop list cannot be null or empty");
        }
        
        // Usar o PickPathService que já criaste
        return PickPathService.processPickPathSequencing(inputStops);
    }

    /**
     * Consolida stops duplicados (mesmo aisle/bay)
     *
     * @param originalStops Lista original com possíveis duplicados
     * @return Lista consolidada
     */
    public List<PickStop> consolidateStops(List<PickStop> originalStops) {
        if (originalStops == null || originalStops.isEmpty()) {
            throw new IllegalArgumentException("Stop list cannot be null or empty");
        }
        
        return PickPathService.consolidateStops(originalStops);
    }

    /**
     * Gera paths para ambas as estratégias (assume stops já consolidados)
     *
     * @param consolidatedStops Lista de stops já consolidados
     * @return Map com ambas as estratégias
     */
    public Map<PathStrategy, PickPath> generateBothPaths(List<PickStop> consolidatedStops) {
        if (consolidatedStops == null || consolidatedStops.isEmpty()) {
            throw new IllegalArgumentException("Stop list cannot be null or empty");
        }
        
        return PickPathService.generateBothPaths(consolidatedStops);
    }
}
