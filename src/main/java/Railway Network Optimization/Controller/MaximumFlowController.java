package Controller;

import Domain.*;
import Repository.RailwayGraphRepository;

/**
 * Controller for USEI14 - Maximum Flow Between Two Stations.
 * 
 * This controller orchestrates the maximum flow calculation:
 * 1. Uses RailwayGraphRepository to access the directed graph (reused from USEI11)
 * 2. Calls Domain algorithm (FordFulkersonAlgorithm) to compute maximum flow
 * 3. Processes and packages results for UI
 * 
 * The controller ensures that the same directed graph model from USEI11 is reused,
 * without creating duplicate graph structures.
 * 
 * @author Development Team
 * @version 1.0
 */
public class MaximumFlowController {
    private final RailwayGraphRepository graphRepository;
    private RailwayGraph graph;
    
    /**
     * Constructs a new MaximumFlowController with a RailwayGraphRepository.
     * This reuses the same directed graph created for USEI11.
     * 
     * @param graphRepository the repository that provides access to the directed graph
     * @throws IllegalArgumentException if graphRepository is null
     */
    public MaximumFlowController(RailwayGraphRepository graphRepository) {
        if (graphRepository == null) {
            throw new IllegalArgumentException("RailwayGraphRepository cannot be null");
        }
        this.graphRepository = graphRepository;
        this.graph = null;
    }
    
    /**
     * Builds the railway graph from the repository.
     * This reuses the same graph structure used in USEI11.
     * 
     * @throws IllegalStateException if graph cannot be built
     */
    public void buildGraph() {
        if (graphRepository == null) {
            throw new IllegalStateException("RailwayGraphRepository is not initialized");
        }
        
        // Reuse the same directed graph from USEI11
        this.graph = graphRepository.buildGraph();
    }
    
    /**
     * Computes the maximum flow from source to sink station.
     * 
     * @param sourceStationId the source station ID
     * @param sinkStationId the sink station ID
     * @return a MaximumFlowResult containing the maximum flow value and related information
     * @throws IllegalStateException if graph hasn't been built yet
     * @throws IllegalArgumentException if stations don't exist or are invalid
     */
    public MaximumFlowResult computeMaximumFlow(String sourceStationId, String sinkStationId) {
        if (graph == null) {
            throw new IllegalStateException("Graph must be built before computing maximum flow");
        }
        
        if (sourceStationId == null || sinkStationId == null) {
            throw new IllegalArgumentException("Source and sink station IDs cannot be null");
        }
        
        // Get station objects for result
        Station sourceStation = graph.getNode(sourceStationId);
        Station sinkStation = graph.getNode(sinkStationId);
        
        if (sourceStation == null) {
            throw new IllegalArgumentException("Source station not found: " + sourceStationId);
        }
        if (sinkStation == null) {
            throw new IllegalArgumentException("Sink station not found: " + sinkStationId);
        }
        
        // Call Domain algorithm to compute maximum flow
        int maxFlow = FordFulkersonAlgorithm.computeMaxFlow(graph, sourceStationId, sinkStationId);
        
        // Package result for UI
        return new MaximumFlowResult(sourceStation, sinkStation, maxFlow);
    }
    
    /**
     * Gets all stations in the graph for UI selection.
     * 
     * @return a collection of all stations
     * @throws IllegalStateException if graph hasn't been built yet
     */
    public java.util.Collection<Station> getAllStations() {
        if (graph == null) {
            throw new IllegalStateException("Graph must be built first");
        }
        return graph.getAllNodes();
    }
    
    /**
     * Gets a station by its ID.
     * 
     * @param stationId the station ID
     * @return the station, or null if not found
     * @throws IllegalStateException if graph hasn't been built yet
     */
    public Station getStationById(String stationId) {
        if (graph == null) {
            throw new IllegalStateException("Graph must be built first");
        }
        return graph.getNode(stationId);
    }
    
    /**
     * Gets graph statistics.
     * 
     * @return a string with graph statistics
     * @throws IllegalStateException if graph hasn't been built yet
     */
    public String getGraphStatistics() {
        if (graph == null) {
            throw new IllegalStateException("Graph must be built first");
        }
        
        return String.format("Graph Statistics:\n" +
                "  Nodes: %d\n" +
                "  Edges: %d",
                graph.getNodeCount(),
                graph.getEdgeCount());
    }
    
    /**
     * Represents the result of a maximum flow calculation.
     */
    public static class MaximumFlowResult {
        private final Station sourceStation;
        private final Station sinkStation;
        private final int maxFlowValue;
        
        public MaximumFlowResult(Station sourceStation, Station sinkStation, int maxFlowValue) {
            this.sourceStation = sourceStation;
            this.sinkStation = sinkStation;
            this.maxFlowValue = maxFlowValue;
        }
        
        /**
         * Gets the source station.
         * 
         * @return the source station
         */
        public Station getSourceStation() {
            return sourceStation;
        }
        
        /**
         * Gets the sink station.
         * 
         * @return the sink station
         */
        public Station getSinkStation() {
            return sinkStation;
        }
        
        /**
         * Gets the maximum flow value (trains per day).
         * 
         * @return the maximum flow value
         */
        public int getMaxFlowValue() {
            return maxFlowValue;
        }
        
        /**
         * Gets the source station ID.
         * 
         * @return the source station ID
         */
        public String getSourceStationId() {
            return sourceStation.getId();
        }
        
        /**
         * Gets the sink station ID.
         * 
         * @return the sink station ID
         */
        public String getSinkStationId() {
            return sinkStation.getId();
        }
    }
}

