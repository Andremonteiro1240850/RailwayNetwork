package Sprint1.Domain;
import Sprint1.Domain.ENUM.PathStrategy;

import java.util.List;
import java.util.ArrayList;

/**
 * Entidade de domínio PickPath - representa um percurso de picking calculado.
 * Guarda a estratégia usada, os stops ordenados/otimizados e a distância total.
 * Contém apenas estado e regras de negócio básicas (validações).
 */
public class PickPath {
    private PathStrategy strategy;
    private int totalDistance;
    private List<PickStop> stops;

    /**
     * Construtor para criar um PickPath.
     * Valida todos os inputs e cria cópia defensiva da lista de stops.
     *
     * @param strategy Estratégia utilizada (ASC_SWEEP ou NEAREST_NEIGHBOUR)
     * @param stops Lista de stops já ordenados/otimizados pela estratégia
     * @param totalDistance Distância total calculada do percurso
     * @throws IllegalArgumentException Se strategy for null, stops for null, ou distância for negativa
     */
    public PickPath(PathStrategy strategy, List<PickStop> stops, int totalDistance) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }
        if (stops == null) {
            throw new IllegalArgumentException("Stops cannot be null");
        }
        if (totalDistance < 0) {
            throw new IllegalArgumentException("Total distance cannot be negative");
        }

        this.strategy = strategy;
        this.stops = new ArrayList<>(stops);
        this.totalDistance = totalDistance;
    }

    /**
     * Retorna a estratégia utilizada para calcular este percurso.
     *
     * @return Estratégia (ASC_SWEEP ou NEAREST_NEIGHBOUR)
     */
    public PathStrategy getStrategy() {
        return strategy;
    }

    /**
     * Retorna a distância total do percurso.
     *
     * @return Distância total calculada
     */
    public int getTotalDistance() {
        return totalDistance;
    }

    /**
     * Retorna a lista de stops ordenados/otimizados.
     * Retorna cópia defensiva para evitar modificações externas.
     *
     * @return Cópia da lista de stops ordenados
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
