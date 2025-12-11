package Controller;


import Domain.*;
import Repository.StationRepository;
import Repository.LineRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller that orchestrates the upgrade plan functionality.
 * 
 * This class follows the MVC pattern and acts as the "chef" that coordinates:
 * 1. Gets raw data from Repositories (StationRepository, LineRepository)
 * 2. Converts raw data into Domain objects (builds RailwayGraph)
 * 3. Calls Domain algorithms (TopologicalSort, CycleDetection)
 * 4. Processes and prepares results for UI
 * 
 * The Controller does NOT load data from files - it receives repositories
 * that are already populated. Data loading is handled elsewhere (e.g., in UI or initialization).
 * 
 * @author Development Team
 * @version 1.0
 */
public class UpgradePlanController {
    private final StationRepository stationRepository;
    private final LineRepository lineRepository;
    private RailwayGraph graph;
    
    /**
     * Constructs a new UpgradePlanController with repositories.
     * 
     * The repositories should already be populated with data before constructing the controller.
     * 
     * @param stationRepository the repository containing stations
     * @param lineRepository the repository containing lines/edges
     * @throws IllegalArgumentException if repositories are null
     */
    public UpgradePlanController(StationRepository stationRepository, LineRepository lineRepository) {
        if (stationRepository == null) {
            throw new IllegalArgumentException("StationRepository cannot be null");
        }
        if (lineRepository == null) {
            throw new IllegalArgumentException("LineRepository cannot be null");
        }
        this.stationRepository = stationRepository;
        this.lineRepository = lineRepository;
        this.graph = null;
    }
    
    /**
     * Builds the railway graph from the repositories.
     * 
     * This method belongs to the Controller layer as it orchestrates:
     * - Getting raw data from Repositories (stations and lines)
     * - Converting/constructing Domain objects (RailwayGraph)
     * 
     * The Domain layer (RailwayGraph) doesn't know about repositories,
     * so the Controller bridges this gap.
     * 
     * @throws IllegalStateException if repositories are empty
     */
    public void buildGraph() {
        if (stationRepository.isEmpty()) {
            throw new IllegalStateException("StationRepository is empty. Please load data first.");
        }
        if (lineRepository.isEmpty()) {
            throw new IllegalStateException("LineRepository is empty. Please load data first.");
        }
        
        // Build Domain graph structure from raw data in repositories
        this.graph = new RailwayGraph();
        
        // Add all nodes (stations) to the graph
        for (Station station : stationRepository.getAll()) {
            graph.addNode(station);
        }
        
        // Add all edges (lines) to the graph
        for (Line line : lineRepository.getAll()) {
            try {
                graph.addEdge(line);
            } catch (IllegalArgumentException e) {
                // Skip invalid edges (shouldn't happen if data is consistent)
                System.err.println("Warning: Skipped invalid edge: " + e.getMessage());
            }
        }
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
     * Gets the in-degree (number of incoming edges) of a station.
     * 
     * @param stationId the station ID
     * @return the in-degree of the station
     * @throws IllegalStateException if data hasn't been loaded yet
     */
    public int getStationInDegree(String stationId) {
        if (graph == null) {
            throw new IllegalStateException("Data must be loaded first");
        }
        return graph.getIncomingEdges(stationId).size();
    }
    
    /**
     * Gets the out-degree (number of outgoing edges) of a station.
     * 
     * @param stationId the station ID
     * @return the out-degree of the station
     * @throws IllegalStateException if data hasn't been loaded yet
     */
    public int getStationOutDegree(String stationId) {
        if (graph == null) {
            throw new IllegalStateException("Data must be loaded first");
        }
        return graph.getOutgoingEdges(stationId).size();
    }
    
    /**
     * Gets the total degree (in-degree + out-degree) of a station.
     * 
     * @param stationId the station ID
     * @return the total degree of the station
     * @throws IllegalStateException if data hasn't been loaded yet
     */
    public int getStationDegree(String stationId) {
        if (graph == null) {
            throw new IllegalStateException("Data must be loaded first");
        }
        return getStationInDegree(stationId) + getStationOutDegree(stationId);
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
        int inDegree = getStationInDegree(stationId);
        int outDegree = getStationOutDegree(stationId);
        return new DegreeInfo(inDegree, outDegree);
    }
    
    /**
     * Represents degree information for a station.
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
