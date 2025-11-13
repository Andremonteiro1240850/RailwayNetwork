package Controller;

import Domain.PickPath;
import Domain.PickStop;
import Domain.Trolley;
import Domain.ENUM.PathStrategy;
import Repository.TrolleyRepository;

import java.util.*;

/**
 * Controller para Pick Path Sequencing (USEI04)
 * Responsável por orquestrar as operações de sequenciação de picking
 */
public class PickPathController {
    
    private TrolleyRepository trolleyRepository;

    /**
     * Construtor com repositório de trolleys.
     * Permite aceder aos trolleys criados no USEI03.
     *
     * @param trolleyRepository Repositório que guarda os trolleys do USEI03
     */
    public PickPathController(TrolleyRepository trolleyRepository) {
        this.trolleyRepository = trolleyRepository;
    }

    /**
     * Construtor padrão sem repositório.
     * Usado quando não é necessário aceder a trolleys do USEI03.
     */
    public PickPathController() {
        this.trolleyRepository = null;
    }

    /**
     * Processa a sequenciação completa de percursos de picking.
     * Método principal do USEI04 - Orquestra todo o processo de sequenciação.
     * 
     * Fluxo:
     * 1. Consolida stops duplicados
     * 2. Gera percursos para ambas estratégias (que aplica estratégia + calcula distância)
     * 3. Retorna Map com ambos os resultados
     * 
     * @param inputStops Lista de PickStops (pode ter duplicados - mesmo aisle/bay)
     * @return Map com ambas as estratégias (ASC_SWEEP e NEAREST_NEIGHBOUR) e seus percursos calculados
     */
    public Map<PathStrategy, PickPath> processPickPathSequencing(List<PickStop> inputStops) {
        if (inputStops == null || inputStops.isEmpty()) {
            throw new IllegalArgumentException("Stop list cannot be null or empty");
        }
        
        List<PickStop> consolidatedStops = consolidateStops(inputStops);
        return generateBothPaths(consolidatedStops);
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
    public List<PickStop> consolidateStops(List<PickStop> originalStops) {
        return PickStop.consolidateStops(originalStops);
    }

    /**
     * Gera percursos para ambas as estratégias (ASC_SWEEP e NEAREST_NEIGHBOUR).
     * IMPORTANTE: Assume que os stops já foram consolidados (sem duplicados)!
     * Cria um percurso para cada estratégia e retorna ambos num Map.
     *
     * @param consolidatedStops Lista de stops já consolidados (sem duplicados)
     * @return Map com ambos os percursos calculados (um para cada estratégia)
     */
    public Map<PathStrategy, PickPath> generateBothPaths(List<PickStop> consolidatedStops) {
        Map<PathStrategy, PickPath> results = new HashMap<>();
        
        PickPath ascSweepPath = createCalculatedPath(PathStrategy.ASC_SWEEP, consolidatedStops);
        results.put(PathStrategy.ASC_SWEEP, ascSweepPath);
        
        PickPath nearestNeighbourPath = createCalculatedPath(PathStrategy.NEAREST_NEIGHBOUR, consolidatedStops);
        results.put(PathStrategy.NEAREST_NEIGHBOUR, nearestNeighbourPath);
        
        return results;
    }

    /**
     * Obtém todos os trolleys criados no USEI03 (Picking Plans).
     * Acede ao repositório que guarda os trolleys.
     * 
     * @return Lista de todos os trolleys disponíveis com os seus PickingItems
     * @throws IllegalStateException Se o repositório não estiver inicializado
     */
    public List<Trolley> getTrolleysFromUS03() {
        if (trolleyRepository == null) {
            throw new IllegalStateException("TrolleyRepository not initialized. Cannot access trolley data from US03.");
        }
        
        return trolleyRepository.getAllTrolleys();
    }

    /**
     * Calcula a distância entre dois pontos no armazém conforme a fórmula especificada.
     * Método estático que pode ser chamado sem instanciar o controller.
     * 
     * Fórmula:
     * - Mesmo corredor (a1 == a2): |b1 - b2|
     * - Corredores diferentes: b1 + (|a1 - a2| * 3) + b2
     *
     * @param a1 Corredor do ponto 1
     * @param b1 Bay do ponto 1
     * @param a2 Corredor do ponto 2
     * @param b2 Bay do ponto 2
     * @return Distância calculada entre os dois pontos
     */
    public static int calculateDistance(int a1, int b1, int a2, int b2) {
        if (a1 == a2) {
            return Math.abs(b1 - b2);
        } else {
            return b1 + (Math.abs(a1 - a2) * 3) + b2;
        }
    }

    /**
     * Calcula a distância total de um percurso completo.
     * IMPORTANTE: Recebe stops JÁ ORDENADOS pela estratégia (não calcula antes de ordenar!).
     * Soma as distâncias entre cada par consecutivo de stops, começando da entrada (0,0).
     *
     * @param stops Lista de stops já ordenados/otimizados pela estratégia
     * @return Distância total do percurso
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
     * Implementa a estratégia ASC_SWEEP (Ascending Sweep).
     * Ordena stops por corredor (aisle) em ordem ascendente, depois por bay em ordem ascendente.
     * 
     * Exemplo: [(3,4), (1,8), (2,2)] → [(1,8), (2,2), (3,4)]
     *
     * @param stops Lista de stops para ordenar
     * @return Lista ordenada (não modifica a original)
     */
    public List<PickStop> calculateAscSweep(List<PickStop> stops) {
        List<PickStop> sorted = new ArrayList<>(stops);
        sorted.sort(Comparator
                .comparing(PickStop::getAisle)
                .thenComparing(PickStop::getBay));
        return sorted;
    }

    /**
     * Implementa a estratégia NEAREST_NEIGHBOUR (Vizinho Mais Próximo).
     * Algoritmo ganancioso que sempre escolhe o stop mais próximo da posição atual.
     * Começa na entrada (0,0) e repete até visitar todos os stops.
     *
     * @param stops Lista de stops para otimizar
     * @return Lista otimizada com ordem de visitação (não modifica a original)
     */
    public List<PickStop> calculateNearestNeighbour(List<PickStop> stops) {
        List<PickStop> unvisited = new ArrayList<>(stops);
        List<PickStop> path = new ArrayList<>();

        int currentAisle = 0;
        int currentBay = 0;

        while (!unvisited.isEmpty()) {
            PickStop nearest = findNearestStop(currentAisle, currentBay, unvisited);
            path.add(nearest);
            unvisited.remove(nearest);

            currentAisle = nearest.getAisle();
            currentBay = nearest.getBay();
        }

        return path;
    }

    /**
     * Encontra o stop mais próximo da posição atual.
     * Método auxiliar privado usado pela estratégia NEAREST_NEIGHBOUR.
     * Calcula distância para todos os stops e retorna o mais próximo.
     *
     * @param currentAisle Corredor atual
     * @param currentBay Bay atual
     * @param stops Lista de stops disponíveis
     * @return Stop com menor distância da posição atual
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
     * Cria um PickPath completo calculado com uma estratégia específica.
     * IMPORTANTE: Primeiro aplica a estratégia (ordena stops), depois calcula distância.
     * 
     * Ordem de execução:
     * 1. Aplica estratégia → ordena/otimiza stops
     * 2. Calcula distância total dos stops já ordenados
     * 3. Cria e retorna PickPath com estratégia, stops ordenados e distância total
     *
     * @param strategy Estratégia a usar (ASC_SWEEP ou NEAREST_NEIGHBOUR)
     * @param stops Lista de stops para calcular
     * @return PickPath completo com estratégia, stops ordenados e distância total
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
