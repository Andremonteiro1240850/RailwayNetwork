package Controller;

import Domain.*;
import Repository.RailwayGraphRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller that orchestrates the upgrade plan functionality for USEI11 (Directed Line Upgrade Plan).
 * 
 * This class follows the MVC pattern and coordinates:
 * 1. Uses RailwayGraphRepository to build a DIRECTED graph (for USEI11)
 * 2. Calls Domain algorithms (TopologicalSort, CycleDetection) on the directed graph
 * 3. Processes and prepares results for UI
 * 
 * The Controller uses RailwayGraphRepository which ensures the graph is DIRECTED,
 * as required by USEI11 for analyzing upgrade dependencies.
 * 
 * @author Development Team
 * @version 1.0
 */
public class UpgradePlanController {
    private final RailwayGraphRepository graphRepository;
    private RailwayGraph graph;
    
    /**
     * Constructs a new UpgradePlanController with a RailwayGraphRepository.
     * This ensures the graph is DIRECTED, as required for USEI11.
     * 
     * @param graphRepository the repository that builds the directed graph
     * @throws IllegalArgumentException if graphRepository is null
     */
    public UpgradePlanController(RailwayGraphRepository graphRepository) {
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
     * which is required for USEI11 (Directed Line Upgrade Plan).
     * The RailwayGraphRepository ensures that edges are added in a single direction,
     * preserving the directional dependencies for upgrade planning.
     * 
     * @throws IllegalStateException if repositories are empty or graph cannot be built
     */
    public void buildGraph() {
        if (graphRepository == null) {
            throw new IllegalStateException("RailwayGraphRepository is not initialized");
        }
        
        // Use RailwayGraphRepository to build the DIRECTED graph for USEI11
        // This ensures the graph maintains directional dependencies (A->B means A must be upgraded before B)
        this.graph = graphRepository.buildGraph();
    }
    
    /**
     * Calculates the upgrade plan for the railway network.
     * 
     * This method orchestrates:
     * 1. Domain layer: Calls cycle detection algorithm
     * 2. Domain layer: Calls topological sort algorithm (if no cycles)
     * 3. Controller layer: Processes and packages results for UI
     * 
     * @return an UpgradePlanResult containing either the topological order or cycle information
     * @throws IllegalStateException if data hasn't been loaded yet
     */
    public UpgradePlanResult calculateUpgradePlan() {
        if (graph == null) {
            throw new IllegalStateException("Data must be loaded before calculating upgrade plan");
        }
        
        // STEP 1: Domain layer - Check for cycles using Domain algorithm
        CycleDetection.CycleInfo cycleInfo = CycleDetection.findCycles(graph);
        
        if (cycleInfo.hasCycles()) {
            // Graph has cycles - return cycle information
            return new UpgradePlanResult(null, cycleInfo);
        } else {
            // STEP 2: Domain layer - Perform topological sort using Domain algorithm
            try {
                List<Station> topologicalOrder = TopologicalSort.order(graph);
                // STEP 3: Controller layer - Package result for UI
                return new UpgradePlanResult(topologicalOrder, null);
            } catch (TopologicalSort.CycleDetectedException e) {
                // This shouldn't happen if cycle detection worked correctly,
                // but handle it just in case
                return new UpgradePlanResult(null, cycleInfo);
            }
        }
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
     * Gets degree information for a station.
     * 
     * @param stationId the station ID
     * @return a DegreeInfo object with in-degree, out-degree, and total degree
     * @throws IllegalStateException if data hasn't been loaded yet
     */
    public DegreeInfo getStationDegreeInfo(String stationId) {
        if (graph == null) {
            throw new IllegalStateException("Data must be loaded first");
        }
        int inDegree = graph.getIncomingEdges(stationId).size();
        int outDegree = graph.getOutgoingEdges(stationId).size();
        return new DegreeInfo(inDegree, outDegree);
    }
    
    /**
     * Gets all stations in the graph.
     * 
     * @return a collection of all stations
     * @throws IllegalStateException if data hasn't been loaded yet
     */
    public java.util.Collection<Station> getAllStations() {
        if (graph == null) {
            throw new IllegalStateException("Data must be loaded first");
        }
        return graph.getAllNodes();
    }
    
    /**
     * Gets all edges in the graph.
     * 
     * @return a list of all edges
     * @throws IllegalStateException if data hasn't been loaded yet
     */
    public java.util.List<Line> getAllEdges() {
        if (graph == null) {
            throw new IllegalStateException("Data must be loaded first");
        }
        return graph.getAllEdges();
    }
    
    // ============================================================================
    // Inner Classes
    // ============================================================================
    
    /**
     * Represents degree information for a station in the directed graph.
     */
    public static class DegreeInfo {
        private final int inDegree;
        private final int outDegree;
        
        public DegreeInfo(int inDegree, int outDegree) {
            this.inDegree = inDegree;
            this.outDegree = outDegree;
        }
        
        public int getInDegree() {
            return inDegree;
        }
        
        public int getOutDegree() {
            return outDegree;
        }
        
        public int getTotalDegree() {
            return inDegree + outDegree;
        }
    }
    
    /**
     * Represents the result of the upgrade plan calculation.
     */
    public static class UpgradePlanResult {
        private final List<Station> topologicalOrder;
        private final CycleDetection.CycleInfo cycleInfo;
        
        public UpgradePlanResult(List<Station> topologicalOrder, CycleDetection.CycleInfo cycleInfo) {
            this.topologicalOrder = topologicalOrder;
            this.cycleInfo = cycleInfo;
        }
        
        /**
         * Checks if the result contains a valid topological order.
         * 
         * @return true if topological order exists, false if cycles were found
         */
        public boolean hasTopologicalOrder() {
            return topologicalOrder != null;
        }
        
        /**
         * Gets the topological order of stations.
         * 
         * @return the ordered list of stations, or null if cycles exist
         */
        public List<Station> getTopologicalOrder() {
            return topologicalOrder != null ? new ArrayList<>(topologicalOrder) : null;
        }
        
        /**
         * Gets the cycle information.
         * 
         * @return the cycle info, or null if no cycles exist
         */
        public CycleDetection.CycleInfo getCycleInfo() {
            return cycleInfo;
        }
        
        /**
         * Checks if cycles were detected.
         * 
         * @return true if cycles exist, false otherwise
         */
        public boolean hasCycles() {
            return cycleInfo != null && cycleInfo.hasCycles();
        }
    }
}
