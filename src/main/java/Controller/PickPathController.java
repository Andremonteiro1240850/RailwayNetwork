package Controller;

import Domain.PickPath;
import Domain.PickStop;
import Domain.Trolley;
import Domain.ENUM.PathStrategy;
import pt.ipp.isep.dei.PickPathService;
import Repository.TrolleyRepository;

import java.util.List;
import java.util.Map;

/**
 * Controller para Pick Path Sequencing (USEI04)
 * Responsável por orquestrar as operações de sequenciação de picking
 */
public class PickPathController {
    
    private TrolleyRepository trolleyRepository;

    /**
     * Instantiates a new Pick path controller.
     *
     * @param trolleyRepository the trolley repository
     */
    public PickPathController(TrolleyRepository trolleyRepository) {
        this.trolleyRepository = trolleyRepository;
    }
    
    /**
     * Instantiates a new Pick path controller (default constructor for backward compatibility).
     */
    public PickPathController() {
        this.trolleyRepository = null;
    }

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
    
    /**
     * Gets trolleys from US03 (Picking Plans)
     *
     * @return List of trolleys from US03
     */
    public List<Trolley> getTrolleysFromUS03() {
        if (trolleyRepository == null) {
            throw new IllegalStateException("TrolleyRepository not initialized. Cannot access trolley data from US03.");
        }
        
        return trolleyRepository.getAllTrolleys();
    }
}
