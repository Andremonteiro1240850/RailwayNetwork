package Domain;

import java.util.*;

/**
 * Implements the Bellman-Ford algorithm for finding shortest paths in a directed graph
 * with edges that may have negative costs.
 * 
 * This algorithm can:
 * - Find shortest paths from a source to a target
 * - Detect negative cycles in the graph
 * 
 * Time complexity: O(V × E), where V = vertices, E = edges
 *
 * @author Development Team
 * @version 1.0
 */
public class BellmanFord {
    
    /**
     * Result of the Bellman-Ford algorithm execution.
     * Contains either a shortest path or information about a negative cycle.
     */
    public static class ShortestPathResult {
        private final List<PathStep> path;
        private final double totalCost;
        private final boolean hasPath;
        private final boolean hasNegativeCycle;
        private final NegativeCycleInfo cycleInfo;
        
        /**
         * Constructs a result with a valid path.
         */
        public ShortestPathResult(List<PathStep> path, double totalCost) {
            this.path = path != null ? new ArrayList<>(path) : null;
            this.totalCost = totalCost;
            this.hasPath = path != null && !path.isEmpty();
            this.hasNegativeCycle = false;
            this.cycleInfo = null;
        }
        
        /**
         * Constructs a result with a negative cycle.
         */
        public ShortestPathResult(NegativeCycleInfo cycleInfo) {
            this.path = null;
            this.totalCost = Double.NaN;
            this.hasPath = false;
            this.hasNegativeCycle = true;
            this.cycleInfo = cycleInfo;
        }
        
        /**
         * Constructs a result indicating no path exists.
         */
        public ShortestPathResult() {
            this.path = null;
            this.totalCost = Double.NaN;
            this.hasPath = false;
            this.hasNegativeCycle = false;
            this.cycleInfo = null;
        }
        
        public List<PathStep> getPath() {
            return path != null ? new ArrayList<>(path) : null;
        }
        
        public double getTotalCost() {
            return totalCost;
        }
        
        public boolean hasPath() {
            return hasPath;
        }
        
        public boolean hasNegativeCycle() {
            return hasNegativeCycle;
        }
        
        public NegativeCycleInfo getCycleInfo() {
            return cycleInfo;
        }
    }
    
    /**
     * Represents a step in the path with station ID and accumulated cost.
     */
    public static class PathStep {
        private final String stationId;
        private final double accumulatedCost;
        
        public PathStep(String stationId, double accumulatedCost) {
            this.stationId = stationId;
            this.accumulatedCost = accumulatedCost;
        }
        
        public String getStationId() {
            return stationId;
        }
        
        public double getAccumulatedCost() {
            return accumulatedCost;
        }
        
        @Override
        public String toString() {
            return String.format("%s, %.2f", stationId, accumulatedCost);
        }
    }
    
    /**
     * Information about a detected negative cycle.
     */
    public static class NegativeCycleInfo {
        private final List<String> cycleStations;
        private final List<CycleEdge> cycleEdges;
        private final double cycleCost;
        
        public NegativeCycleInfo(List<String> cycleStations, List<CycleEdge> cycleEdges, double cycleCost) {
            this.cycleStations = new ArrayList<>(cycleStations);
            this.cycleEdges = new ArrayList<>(cycleEdges);
            this.cycleCost = cycleCost;
        }
        
        public List<String> getCycleStations() {
            return new ArrayList<>(cycleStations);
        }
        
        public List<CycleEdge> getCycleEdges() {
            return new ArrayList<>(cycleEdges);
        }
        
        public double getCycleCost() {
            return cycleCost;
        }
    }
    
    /**
     * Represents an edge in a negative cycle.
     */
    public static class CycleEdge {
        private final String from;
        private final String to;
        private final double cost;
        
        public CycleEdge(String from, String to, double cost) {
            this.from = from;
            this.to = to;
            this.cost = cost;
        }
        
        public String getFrom() {
            return from;
        }
        
        public String getTo() {
            return to;
        }
        
        public double getCost() {
            return cost;
        }
        
        @Override
        public String toString() {
            return String.format("%s → %s (cost: %.2f)", from, to, cost);
        }
    }
    
    /**
     * Finds the shortest path from source to target using Bellman-Ford algorithm.
     * 
     * @param graph the directed graph
     * @param sourceId the source station ID
     * @param targetId the target station ID
     * @return ShortestPathResult containing the path or cycle information
     * @throws IllegalArgumentException if graph, sourceId, or targetId is null
     */
    public static ShortestPathResult findShortestPath(RailwayGraph graph, String sourceId, String targetId) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        if (sourceId == null || sourceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Source ID cannot be null or empty");
        }
        if (targetId == null || targetId.trim().isEmpty()) {
            throw new IllegalArgumentException("Target ID cannot be null or empty");
        }
        
        sourceId = sourceId.trim();
        targetId = targetId.trim();
        
        // Check if source and target exist
        if (!graph.containsNode(sourceId)) {
            throw new IllegalArgumentException("Source station does not exist: " + sourceId);
        }
        if (!graph.containsNode(targetId)) {
            throw new IllegalArgumentException("Target station does not exist: " + targetId);
        }
        
        // Special case: source == target
        if (sourceId.equals(targetId)) {
            List<PathStep> path = new ArrayList<>();
            path.add(new PathStep(sourceId, 0.0));
            return new ShortestPathResult(path, 0.0);
        }
        
        // Initialize distances and predecessors
        Map<String, Double> distances = new HashMap<>();
        Map<String, String> predecessors = new HashMap<>();
        
        // Initialize all distances to infinity
        for (Station station : graph.getAllNodes()) {
            distances.put(station.getId(), Double.MAX_VALUE);
            predecessors.put(station.getId(), null);
        }
        
        // Distance from source to itself is 0
        distances.put(sourceId, 0.0);
        
        // Relax edges V-1 times
        int vertexCount = graph.getNodeCount();
        for (int i = 0; i < vertexCount - 1; i++) {
            boolean relaxed = false;
            
            for (Line edge : graph.getAllEdges()) {
                String u = edge.getDepartureStationId();
                String v = edge.getArrivalStationId();
                double cost = edge.getCost();
                
                // Check for relaxation
                if (distances.get(u) != Double.MAX_VALUE && 
                    distances.get(u) + cost < distances.get(v)) {
                    distances.put(v, distances.get(u) + cost);
                    predecessors.put(v, u);
                    relaxed = true;
                }
            }
            
            // Early termination if no relaxation occurred
            if (!relaxed) {
                break;
            }
        }
        
        // Check for negative cycles (iteration V)
        for (Line edge : graph.getAllEdges()) {
            String u = edge.getDepartureStationId();
            String v = edge.getArrivalStationId();
            double cost = edge.getCost();
            
            if (distances.get(u) != Double.MAX_VALUE && 
                distances.get(u) + cost < distances.get(v)) {
                // Negative cycle detected!
                NegativeCycleInfo cycleInfo = detectNegativeCycle(graph, v, predecessors);
                return new ShortestPathResult(cycleInfo);
            }
        }
        
        // Check if target is reachable
        if (distances.get(targetId) == Double.MAX_VALUE) {
            return new ShortestPathResult(); // No path exists
        }
        
        // Reconstruct path
        List<PathStep> path = reconstructPath(sourceId, targetId, predecessors, distances);
        return new ShortestPathResult(path, distances.get(targetId));
    }
    
    /**
     * Detects and extracts information about a negative cycle.
     * 
     * @param graph the graph
     * @param startVertex the vertex where the cycle was detected
     * @param predecessors the predecessor map
     * @return NegativeCycleInfo with cycle details
     */
    private static NegativeCycleInfo detectNegativeCycle(RailwayGraph graph, String startVertex, 
                                                         Map<String, String> predecessors) {
        // Find a vertex in the negative cycle by following predecessors
        Set<String> visited = new HashSet<>();
        String current = startVertex;
        
        // Find a cycle by following predecessors
        while (current != null && !visited.contains(current)) {
            visited.add(current);
            current = predecessors.get(current);
        }
        
        if (current == null) {
            // Fallback: use startVertex
            current = startVertex;
        }
        
        // Extract the cycle
        List<String> cycleStations = new ArrayList<>();
        List<CycleEdge> cycleEdges = new ArrayList<>();
        Set<String> cycleSet = new HashSet<>();
        
        String cycleStart = current;
        String node = cycleStart;
        double totalCycleCost = 0.0;
        
        // Build cycle by following predecessors
        do {
            if (cycleSet.contains(node)) {
                // Found the start of the cycle
                break;
            }
            cycleSet.add(node);
            cycleStations.add(node);
            
            String prev = predecessors.get(node);
            if (prev != null) {
                // Find the edge from prev to node
                Line edge = findEdge(graph, prev, node);
                if (edge != null) {
                    cycleEdges.add(new CycleEdge(prev, node, edge.getCost()));
                    totalCycleCost += edge.getCost();
                }
                node = prev;
            } else {
                break;
            }
        } while (node != null && !node.equals(cycleStart));
        
        // Reverse to get correct order
        Collections.reverse(cycleStations);
        Collections.reverse(cycleEdges);
        
        return new NegativeCycleInfo(cycleStations, cycleEdges, totalCycleCost);
    }
    
    /**
     * Reconstructs the path from source to target.
     * 
     * @param sourceId the source station ID
     * @param targetId the target station ID
     * @param predecessors the predecessor map
     * @param distances the distance map
     * @return list of PathStep objects representing the path
     */
    private static List<PathStep> reconstructPath(String sourceId, String targetId, 
                                                   Map<String, String> predecessors,
                                                   Map<String, Double> distances) {
        List<PathStep> path = new ArrayList<>();
        List<String> stationIds = new ArrayList<>();
        
        // Build path backwards from target to source
        String current = targetId;
        while (current != null) {
            stationIds.add(0, current); // Add at beginning
            current = predecessors.get(current);
            
            // Safety check to avoid infinite loops
            if (stationIds.size() > 1000) {
                break;
            }
        }
        
        // Convert to PathStep list with accumulated costs
        for (String stationId : stationIds) {
            double cost = distances.get(stationId);
            path.add(new PathStep(stationId, cost));
        }
        
        return path;
    }
    
    /**
     * Finds an edge between two stations in the graph.
     * 
     * @param graph the graph
     * @param fromId the departure station ID
     * @param toId the arrival station ID
     * @return the Line edge, or null if not found
     */
    private static Line findEdge(RailwayGraph graph, String fromId, String toId) {
        for (Line edge : graph.getOutgoingEdges(fromId)) {
            if (edge.getArrivalStationId().equals(toId)) {
                return edge;
            }
        }
        return null;
    }
}

