package Domain;
import Domain.ENUM.PathStrategy;

import java.util.List;
import java.util.ArrayList;

/**
 * Entidade de domínio PickPath - representa um percurso de picking
 * Contém apenas estado e regras de negócio básicas
 */
public class PickPath {
    private PathStrategy strategy;
    private int totalDistance;
    private List<PickStop> stops;

    /**
     * Construtor para criar um PickPath
     *
     * @param strategy a estratégia utilizada
     * @param stops a lista de stops ordenados
     * @param totalDistance a distância total calculada
     */
    public PickPath(PathStrategy strategy, List<PickStop> stops, int totalDistance) {
        this.strategy = strategy;
        this.stops = new ArrayList<>(stops);
        this.totalDistance = totalDistance;
    }

    /**
     * Gets strategy.
     *
     * @return the strategy
     */
    public PathStrategy getStrategy() { 
        return strategy; 
    }

    /**
     * Gets total distance.
     *
     * @return the total distance
     */
    public int getTotalDistance() { 
        return totalDistance; 
    }

    /**
     * Gets stops.
     *
     * @return the stops
     */
    public List<PickStop> getStops() { 
        return new ArrayList<>(stops); 
    }

    @Override
    public String toString() {
        return "PickPath{" +
                "strategy=" + strategy +
                ", totalDistance=" + totalDistance +
                ", stops=" + stops +
                '}';
    }
}
