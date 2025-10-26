package Controller;

import Domain.PickPath;
import Domain.PickStop;
import Domain.Trolley;
import Domain.ENUM.PathStrategy;
import Service.PickPathService;
import Repository.TrolleyRepository;

import java.util.*;

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
     * Delega para o domínio que contém a regra de negócio
     *
     * @param originalStops Lista original com possíveis duplicados
     * @return Lista consolidada
     */
    public List<PickStop> consolidateStops(List<PickStop> originalStops) {
        if (originalStops == null || originalStops.isEmpty()) {
            throw new IllegalArgumentException("Stop list cannot be null or empty");
        }
        
        return PickStop.consolidateStops(originalStops);
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

    // ========== MÉTODOS DE CÁLCULO MOVIDOS DO DOMÍNIO ==========

    /**
     * Calcula a distância entre dois pontos conforme especificado no enunciado
     *
     * @param a1 aisle do ponto 1
     * @param b1 bay do ponto 1
     * @param a2 aisle do ponto 2
     * @param b2 bay do ponto 2
     * @return distância calculada
     */
    public static int calculateDistance(int a1, int b1, int a2, int b2) {
        if (a1 == a2) {
            // Mesmo corredor
            return Math.abs(b1 - b2);
        } else {
            // Corredores diferentes: b1 + distância horizontal + b2
            return b1 + (Math.abs(a1 - a2) * 3) + b2;
        }
    }

    /**
     * Calcula a distância total de um percurso
     *
     * @param stops lista de stops ordenados
     * @return distância total
     */
    public int calculateTotalDistance(List<PickStop> stops) {
        if (stops.isEmpty()) return 0;

        int total = 0;
        int currentAisle = 0;
        int currentBay = 0;

        for (PickStop stop : stops) {
            total += calculateDistance(currentAisle, currentBay,
                    stop.getAisle(), stop.getBay());
            currentAisle = stop.getAisle();
            currentBay = stop.getBay();
        }

        return total;
    }

    /**
     * Estratégia A: Ordenação ascendente por aisle e bay
     *
     * @param stops lista de stops para ordenar
     * @return lista ordenada
     */
    public List<PickStop> calculateAscSweep(List<PickStop> stops) {
        List<PickStop> sorted = new ArrayList<>(stops);
        sorted.sort(Comparator
                .comparing(PickStop::getAisle)
                .thenComparing(PickStop::getBay));
        return sorted;
    }

    /**
     * Estratégia B: Vizinho mais próximo
     *
     * @param stops lista de stops para otimizar
     * @return lista otimizada
     */
    public List<PickStop> calculateNearestNeighbour(List<PickStop> stops) {
        List<PickStop> unvisited = new ArrayList<>(stops);
        List<PickStop> path = new ArrayList<>();

        // Começa na entrada (0,0)
        int currentAisle = 0;
        int currentBay = 0;

        while (!unvisited.isEmpty()) {
            PickStop nearest = findNearestStop(currentAisle, currentBay, unvisited);
            path.add(nearest);
            unvisited.remove(nearest);

            // Atualiza posição atual
            currentAisle = nearest.getAisle();
            currentBay = nearest.getBay();
        }

        return path;
    }

    /**
     * Encontra o stop mais próximo da posição atual
     *
     * @param currentAisle aisle atual
     * @param currentBay bay atual
     * @param stops lista de stops disponíveis
     * @return stop mais próximo
     */
    private PickStop findNearestStop(int currentAisle, int currentBay, List<PickStop> stops) {
        PickStop nearest = null;
        int minDistance = Integer.MAX_VALUE;

        for (PickStop stop : stops) {
            int distance = calculateDistance(currentAisle, currentBay,
                    stop.getAisle(), stop.getBay());
            if (distance < minDistance) {
                minDistance = distance;
                nearest = stop;
            }
        }
        return nearest;
    }

    /**
     * Cria um PickPath calculado com a estratégia especificada
     *
     * @param strategy estratégia a usar
     * @param stops lista de stops para calcular
     * @return PickPath calculado
     */
    public PickPath createCalculatedPath(PathStrategy strategy, List<PickStop> stops) {
        List<PickStop> calculatedStops;
        
        switch (strategy) {
            case ASC_SWEEP:
                calculatedStops = calculateAscSweep(stops);
                break;
            case NEAREST_NEIGHBOUR:
                calculatedStops = calculateNearestNeighbour(stops);
                break;
            default:
                calculatedStops = new ArrayList<>(stops);
        }

        int totalDistance = calculateTotalDistance(calculatedStops);
        return new PickPath(strategy, calculatedStops, totalDistance);
    }
}
