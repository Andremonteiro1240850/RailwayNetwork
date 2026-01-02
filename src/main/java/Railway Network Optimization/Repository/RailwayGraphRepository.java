package Repository;

import Domain.RailwayGraph;
import Domain.Line;
import Domain.DataLoader;
import Domain.Station;

/**
 * Repository for managing RailwayGraph instances for USEI11.
 * 
 * This repository is responsible for:
 * - Loading data from CSV files (via DataLoader)
 * - Building and caching RailwayGraph instances
 * - Providing access to the graph structure
 * 
 * The repository follows the Repository pattern and acts as a data access layer
 * for the graph structure, abstracting away the details of graph construction.
 * 
 * @author Development Team
 * @version 1.0
 */
public class RailwayGraphRepository {
    
    // Shared repositories cache - USEI11 and USEI15 share the same data base
    // but each builds its own graph (USEI11 ignores weights, USEI15 uses cost)
    private static StationRepository sharedStationRepository = null;
    private static LineRepository sharedLineRepository = null;
    
    private RailwayGraph cachedGraph;
    private StationRepository stationRepository;
    private LineRepository lineRepository;
    private DataLoader dataLoader;
    
    // Default file paths for Sprint3 data
    private static final String DEFAULT_STATIONS_PATH = "train_station_dataset/Sprint3/stations.csv";
    private static final String DEFAULT_LINES_PATH = "train_station_dataset/Sprint3/lines.csv";
    
    /**
     * Constructs a new RailwayGraphRepository.
     * The repository will use DataLoader to load data when needed.
     */
    public RailwayGraphRepository() {
        this.cachedGraph = null;
        this.stationRepository = null;
        this.lineRepository = null;
        this.dataLoader = null;
    }
    
    /**
     * Constructs a RailwayGraphRepository with existing repositories.
     * This allows the repository to use pre-loaded data.
     * 
     * @param stationRepository the station repository (must be populated)
     * @param lineRepository the line repository (must be populated)
     * @throws IllegalArgumentException if repositories are null
     */
    public RailwayGraphRepository(StationRepository stationRepository, LineRepository lineRepository) {
        if (stationRepository == null) {
            throw new IllegalArgumentException("StationRepository cannot be null");
        }
        if (lineRepository == null) {
            throw new IllegalArgumentException("LineRepository cannot be null");
        }
        this.stationRepository = stationRepository;
        this.lineRepository = lineRepository;
        this.cachedGraph = null;
        this.dataLoader = null;
    }
    
    /**
     * Loads data from CSV files and populates the repositories.
     * USEI11: loads from CSV and creates shared cache of repositories.
     * USEI15: reuses shared repositories if available, otherwise loads from CSV.
     * 
     * @param stationsPath path to the stations CSV file
     * @param linesPath path to the lines CSV file
     * @throws IllegalStateException if data loading fails
     */
    public void loadData(String stationsPath, String linesPath) {
        // Check if shared repositories exist (from USEI11)
        if (sharedStationRepository != null && sharedLineRepository != null) {
            // Reuse shared repositories - no need to reload from CSV
            this.stationRepository = sharedStationRepository;
            this.lineRepository = sharedLineRepository;
            this.cachedGraph = null; // Will build own graph from shared data
            return;
        }
        
        // First time loading - load from CSV (USEI11 behavior)
        try {
            if (dataLoader == null) {
                dataLoader = new DataLoader(false); // Non-verbose mode
            }
            
            dataLoader.loadAllData(stationsPath, linesPath);
            
            // Store in shared cache for USEI15 to reuse
            sharedStationRepository = dataLoader.getStationRepository();
            sharedLineRepository = dataLoader.getLineRepository();
            
            // Use the loaded repositories
            this.stationRepository = sharedStationRepository;
            this.lineRepository = sharedLineRepository;
            
            // Invalidate cached graph since data has changed
            this.cachedGraph = null;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load data from CSV files: " + e.getMessage(), e);
        }
    }
    
    /**
     * Loads data from default CSV file paths.
     * Uses the default paths: train_station_dataset/Sprint3/stations.csv and lines.csv
     * 
     * @throws IllegalStateException if data loading fails
     */
    public void loadData() {
        loadData(DEFAULT_STATIONS_PATH, DEFAULT_LINES_PATH);
    }
    
    /**
     * Sets the repositories directly (useful when repositories are already loaded elsewhere).
     * 
     * @param stationRepository the station repository
     * @param lineRepository the line repository
     * @throws IllegalArgumentException if repositories are null
     */
    public void setRepositories(StationRepository stationRepository, LineRepository lineRepository) {
        if (stationRepository == null) {
            throw new IllegalArgumentException("StationRepository cannot be null");
        }
        if (lineRepository == null) {
            throw new IllegalArgumentException("LineRepository cannot be null");
        }
        this.stationRepository = stationRepository;
        this.lineRepository = lineRepository;
        // Invalidate cached graph since repositories changed
        this.cachedGraph = null;
    }
    
    /**
     * Builds the RailwayGraph from the current repositories.
     * If a cached graph exists and repositories haven't changed, returns the cached version.
     * 
     * @return the built RailwayGraph
     * @throws IllegalStateException if repositories are not loaded or empty
     */
    public RailwayGraph buildGraph() {
        // Check if repositories are available
        if (stationRepository == null || lineRepository == null) {
            throw new IllegalStateException(
                "Repositories not loaded. Call loadData() or setRepositories() first."
            );
        }
        
        if (stationRepository.isEmpty()) {
            throw new IllegalStateException("StationRepository is empty. Please load data first.");
        }
        
        if (lineRepository.isEmpty()) {
            throw new IllegalStateException("LineRepository is empty. Please load data first.");
        }
        
        // Return cached graph if available
        if (cachedGraph != null) {
            return cachedGraph;
        }
        
        // Build new graph from repositories
        // Each US builds its own graph: USEI11 ignores weights, USEI15 uses cost
        RailwayGraph graph = new RailwayGraph();
        
        // Add all stations (nodes) to the graph
        for (Station station : stationRepository.getAll()) {
            graph.addNode(station);
        }
        
        // Add all lines (edges) to the graph
        int skippedEdges = 0;
        for (Line line : lineRepository.getAll()) {
            try {
                graph.addEdge(line);
            } catch (IllegalArgumentException e) {
                // Skip invalid edges (e.g., missing source or destination nodes)
                skippedEdges++;
                System.err.println("Warning: Skipped invalid edge: " + e.getMessage());
            }
        }
        
        if (skippedEdges > 0) {
            System.out.println("Warning: " + skippedEdges + " edges were skipped due to invalid references.");
        }
        
        // Cache the graph for future use (instance-specific)
        // Each US has its own graph view: USEI11 (no weights), USEI15 (with cost)
        this.cachedGraph = graph;
        
        return graph;
    }
    
    /**
     * Gets the RailwayGraph, building it if necessary.
     * This is a convenience method that calls buildGraph().
     * 
     * @return the RailwayGraph
     * @throws IllegalStateException if repositories are not loaded
     */
    public RailwayGraph getGraph() {
        return buildGraph();
    }
    
    /**
     * Gets the cached graph without rebuilding.
     * 
     * @return the cached graph, or null if not built yet
     */
    public RailwayGraph getCachedGraph() {
        return cachedGraph;
    }
    
    /**
     * Clears the cached graph, forcing a rebuild on next access.
     * Useful when data has been updated.
     */
    public void clearCache() {
        this.cachedGraph = null;
    }
    
    /**
     * Checks if the graph has been built and cached.
     * 
     * @return true if a cached graph exists, false otherwise
     */
    public boolean isGraphCached() {
        return cachedGraph != null;
    }
    
    /**
     * Gets the station repository.
     * 
     * @return the station repository, or null if not loaded
     */
    public StationRepository getStationRepository() {
        return stationRepository;
    }
    
    /**
     * Gets the line repository.
     * 
     * @return the line repository, or null if not loaded
     */
    public LineRepository getLineRepository() {
        return lineRepository;
    }
    
    /**
     * Checks if data has been loaded (repositories are available and not empty).
     * 
     * @return true if data is loaded, false otherwise
     */
    public boolean isDataLoaded() {
        return stationRepository != null && 
               lineRepository != null && 
               !stationRepository.isEmpty() && 
               !lineRepository.isEmpty();
    }
    
    /**
     * Gets statistics about the loaded data.
     * 
     * @return a string with statistics about stations and lines
     */
    public String getStatistics() {
        if (!isDataLoaded()) {
            return "No data loaded.";
        }
        
        int stationCount = stationRepository.size();
        int lineCount = lineRepository.size();
        
        StringBuilder sb = new StringBuilder();
        sb.append("Data Statistics:\n");
        sb.append("  Stations: ").append(stationCount).append("\n");
        sb.append("  Lines: ").append(lineCount).append("\n");
        
        if (cachedGraph != null) {
            sb.append("  Graph nodes: ").append(cachedGraph.getNodeCount()).append("\n");
            sb.append("  Graph edges: ").append(cachedGraph.getEdgeCount()).append("\n");
        } else {
            sb.append("  Graph: Not built yet\n");
        }
        
        return sb.toString();
    }
}

