package Controller;

import Domain.*;
import Repository.RailwayGraphRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Controller that orchestrates the shortest path functionality for USEI15 (Risk-Aware Shortest Paths).
 * 
 * This class follows the MVC pattern and coordinates:
 * 1. Uses RailwayGraphRepository to build a DIRECTED graph (reusing USEI11 graph)
 * 2. Calls Domain algorithm (BellmanFord) on the directed graph
 * 3. Processes and prepares results for UI
 * 
 * The Controller uses RailwayGraphRepository which ensures the graph is DIRECTED,
 * and uses cost (not distance) as edge weights, allowing for negative costs.
 * 
 * @author Development Team
 * @version 1.0
 */
public class ShortestPathController {
    private final RailwayGraphRepository graphRepository;
    private RailwayGraph graph;
    
    /**
     * Constructs a new ShortestPathController with a RailwayGraphRepository.
     * This ensures the graph is DIRECTED, as required for USEI15.
     * 
     * @param graphRepository the repository that builds the directed graph
     * @throws IllegalArgumentException if graphRepository is null
     */
    public ShortestPathController(RailwayGraphRepository graphRepository) {
        if (graphRepository == null) {
            throw new IllegalArgumentException("RailwayGraphRepository cannot be null");
        }
        this.graphRepository = graphRepository;
        this.graph = null;
    }
    
    /**
     * Builds the railway graph using RailwayGraphRepository.
     * 
     * This method uses RailwayGraphRepository to build a DIRECTED graph,
     * which is required for USEI15 (Risk-Aware Shortest Paths).
     * The RailwayGraphRepository ensures that edges are added in a single direction,
     * and each edge contains cost information for the Bellman-Ford algorithm.
     * 
     * @throws IllegalStateException if repositories are empty or graph cannot be built
     */
    public void buildGraph() {
        if (graphRepository == null) {
            throw new IllegalStateException("RailwayGraphRepository is not initialized");
        }
        
        // Use RailwayGraphRepository to build the DIRECTED graph for USEI15
        // This reuses the same graph structure from USEI11
        this.graph = graphRepository.buildGraph();
    }
    
    /**
     * Calculates the shortest path between two stations using Bellman-Ford algorithm.
     * 
     * This method orchestrates:
     * 1. Domain layer: Calls Bellman-Ford algorithm
     * 2. Controller layer: Processes and packages results for UI
     * 
     * @param sourceId the source station ID
     * @param targetId the target station ID
     * @return a ShortestPathResult containing either the path or cycle information
     * @throws IllegalStateException if data hasn't been loaded yet
     * @throws IllegalArgumentException if sourceId or targetId is invalid
     */
    public ShortestPathResult calculateShortestPath(String sourceId, String targetId) {
        if (graph == null) {
            throw new IllegalStateException("Data must be loaded before calculating shortest path. Call buildGraph() first.");
        }
        
        if (sourceId == null || sourceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Source station ID cannot be null or empty");
        }
        if (targetId == null || targetId.trim().isEmpty()) {
            throw new IllegalArgumentException("Target station ID cannot be null or empty");
        }
        
        // Call Domain algorithm
        BellmanFord.ShortestPathResult result = BellmanFord.findShortestPath(graph, sourceId.trim(), targetId.trim());
        
        // Convert to Controller result format
        return new ShortestPathResult(result);
    }
    
    /**
     * Gets the current graph statistics.
     * 
     * @return a string with graph statistics
     * @throws IllegalStateException if data hasn't been loaded yet
     */
    public String getGraphStatistics() {
        if (graph == null) {
            throw new IllegalStateException("Data must be loaded first");
        }
        
        return String.format("Graph Statistics:\n" +
                "  Nodes: %d\n" +
                "  Edges: %d",
                graph.getNodeCount(),
                graph.getEdgeCount());
    }
    
    /**
     * Gets a station by its ID from the graph.
     * 
     * @param stationId the station ID
     * @return the station, or null if not found
     * @throws IllegalStateException if data hasn't been loaded yet
     */
    public Station getStationById(String stationId) {
        if (graph == null) {
            throw new IllegalStateException("Data must be loaded first");
        }
        return graph.getNode(stationId);
    }
    
    /**
     * Gets all stations in the graph.
     * 
     * @return a collection of all stations
     * @throws IllegalStateException if data hasn't been loaded yet
     */
    public Collection<Station> getAllStations() {
        if (graph == null) {
            throw new IllegalStateException("Data must be loaded first");
        }
        return graph.getAllNodes();
    }
    
    /**
     * Checks if the graph has been built.
     * 
     * @return true if graph is built, false otherwise
     */
    public boolean isGraphBuilt() {
        return graph != null;
    }
    
    // ============================================================================
    // Inner Classes
    // ============================================================================
    
    /**
     * Represents the result of a shortest path calculation.
     * Wraps the Domain layer result for Controller/UI use.
     */
    public static class ShortestPathResult {
        private final BellmanFord.ShortestPathResult domainResult;
        
        /**
         * Constructs a ShortestPathResult from a Domain result.
         * 
         * @param domainResult the result from Bellman-Ford algorithm
         */
        public ShortestPathResult(BellmanFord.ShortestPathResult domainResult) {
            this.domainResult = domainResult;
        }
        
        /**
         * Checks if a valid path exists.
         * 
         * @return true if a path exists, false otherwise
         */
        public boolean hasPath() {
            return domainResult != null && domainResult.hasPath();
        }
        
        /**
         * Checks if a negative cycle was detected.
         * 
         * @return true if a negative cycle exists, false otherwise
         */
        public boolean hasNegativeCycle() {
            return domainResult != null && domainResult.hasNegativeCycle();
        }
        
        /**
         * Gets the path as a formatted string.
         * Format: "stid1, cost1, stid2, cost2, ..., stidn, costn"
         * 
         * @return formatted path string, or null if no path
         */
        public String getPathString() {
            if (!hasPath()) {
                return null;
            }
            
            List<BellmanFord.PathStep> path = domainResult.getPath();
            if (path == null || path.isEmpty()) {
                return null;
            }
            
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < path.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(path.get(i).getStationId());
                sb.append(", ");
                sb.append(String.format("%.2f", path.get(i).getAccumulatedCost()));
            }
            
            return sb.toString();
        }
        
        /**
         * Gets the list of path steps.
         * 
         * @return list of PathStep objects, or null if no path
         */
        public List<BellmanFord.PathStep> getPath() {
            return domainResult != null ? domainResult.getPath() : null;
        }
        
        /**
         * Gets the total cost to reach the target.
         * 
         * @return total cost, or NaN if no path exists
         */
        public double getTotalCost() {
            return domainResult != null ? domainResult.getTotalCost() : Double.NaN;
        }
        
        /**
         * Gets the list of stations involved in a negative cycle.
         * 
         * @return list of station IDs, or null if no cycle
         */
        public List<String> getCycleStations() {
            if (!hasNegativeCycle()) {
                return null;
            }
            BellmanFord.NegativeCycleInfo cycleInfo = domainResult.getCycleInfo();
            return cycleInfo != null ? cycleInfo.getCycleStations() : null;
        }
        
        /**
         * Gets the list of edges involved in a negative cycle.
         * 
         * @return list of CycleEdge objects, or null if no cycle
         */
        public List<BellmanFord.CycleEdge> getCycleEdges() {
            if (!hasNegativeCycle()) {
                return null;
            }
            BellmanFord.NegativeCycleInfo cycleInfo = domainResult.getCycleInfo();
            return cycleInfo != null ? cycleInfo.getCycleEdges() : null;
        }
        
        /**
         * Gets the total cost of the negative cycle.
         * 
         * @return cycle cost, or NaN if no cycle
         */
        public double getCycleCost() {
            if (!hasNegativeCycle()) {
                return Double.NaN;
            }
            BellmanFord.NegativeCycleInfo cycleInfo = domainResult.getCycleInfo();
            return cycleInfo != null ? cycleInfo.getCycleCost() : Double.NaN;
        }
        
        /**
         * Gets a formatted string representation of the cycle.
         * 
         * @return formatted cycle string, or null if no cycle
         */
        public String getCycleString() {
            if (!hasNegativeCycle()) {
                return null;
            }
            
            StringBuilder sb = new StringBuilder();
            List<String> stations = getCycleStations();
            List<BellmanFord.CycleEdge> edges = getCycleEdges();
            
            if (stations != null && !stations.isEmpty()) {
                sb.append("Stations: [");
                for (int i = 0; i < stations.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(stations.get(i));
                }
                sb.append("]\n");
            }
            
            if (edges != null && !edges.isEmpty()) {
                sb.append("Edges:\n");
                for (BellmanFord.CycleEdge edge : edges) {
                    sb.append("  ").append(edge.toString()).append("\n");
                }
                sb.append("Total cycle cost: ").append(String.format("%.2f", getCycleCost()));
            }
            
            return sb.toString();
        }
    }
}

