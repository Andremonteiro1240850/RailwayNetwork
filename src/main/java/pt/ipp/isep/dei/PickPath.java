package pt.ipp.isep.dei;
import java.util.List;


import java.util.*;

public class PickPath {
    private PathStrategy strategy;
    private int totalDistance;
    private List<PickStop> stops;

    // Construtor
    public PickPath(PathStrategy strategy, List<PickStop> stops) {
        this.strategy = strategy;
        this.stops = new ArrayList<>(stops);
        calculatePath(); // Calcula automaticamente ao criar
    }

    // Método principal que calcula o percurso baseado na estratégia
    public void calculatePath() {
        List<PickStop> calculatedPath;

        switch (strategy) {
            case ASC_SWEEP:
                calculatedPath = calculateAscSweep();
                break;
            case NEAREST_NEIGHBOUR:
                calculatedPath = calculateNearestNeighbour();
                break;
            default:
                calculatedPath = new ArrayList<>(stops);
        }

        this.stops = calculatedPath;
        this.totalDistance = calculateTotalDistance();
    }

    // Estratégia A: Ordenação ascendente por aisle e bay
    private List<PickStop> calculateAscSweep() {
        List<PickStop> sorted = new ArrayList<>(stops);
        sorted.sort(Comparator
                .comparing(PickStop::getAisle)
                .thenComparing(PickStop::getBay));
        return sorted;
    }

    // Estratégia B: Vizinho mais próximo
    private List<PickStop> calculateNearestNeighbour() {
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

    // Encontra o stop mais próximo da posição atual
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

    // Fórmula de distância conforme especificado no enunciado
    public static int calculateDistance(int a1, int b1, int a2, int b2) {
        if (a1 == a2) {
            // Mesmo corredor
            return Math.abs(b1 - b2);
        } else {
            // Corredores diferentes: b1 + distância horizontal + b2
            return b1 + (Math.abs(a1 - a2) * 3) + b2;
        }
    }

    // Calcula distância total do percurso
    private int calculateTotalDistance() {
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

    // Getters e Setters (os que você já tinha)
    public void setStrategy(PathStrategy strategy) {
        this.strategy = strategy;
        calculatePath(); // Recalcula quando muda a estratégia
    }

    public void setStops(List<PickStop> stops) {
        this.stops = stops;
        calculatePath(); // Recalcula quando mudam os stops
    }

    public PathStrategy getStrategy() { return strategy; }
    public int getTotalDistance() { return totalDistance; }
    public List<PickStop> getStops() { return stops; }

    @Override
    public String toString() {
        return "PickPath{" +
                "strategy=" + strategy +
                ", totalDistance=" + totalDistance +
                ", stops=" + stops +
                '}';
    }
}
