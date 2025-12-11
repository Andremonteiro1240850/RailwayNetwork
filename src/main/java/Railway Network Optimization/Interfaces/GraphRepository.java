package Interfaces;

import Domain.Graph;

/**
 * Repository interface for loading and managing graph data structures.
 * 
 * This interface follows Clean Architecture principles, defining the contract
 * for graph data loading without implementation details.
 *
 * @author Development Team
 * @version 1.0
 */
public interface GraphRepository {
    
    /**
     * Loads the graph from the data source and constructs a complete Graph object.
     * 
     * The graph should be undirected, with all stations and connections properly loaded.
     * Invalid data should be ignored deterministically (same input always produces same output).
     * Duplicate stations should be preserved (first occurrence kept).
     *
     * @return a complete Graph object with all stations and connections loaded
     * @throws Exception if there's an error loading the data
     */
    Graph loadGraph() throws Exception;
    
    /**
     * Gets the number of stations successfully loaded.
     *
     * @return the number of stations loaded
     */
    int getLoadedStationCount();
    
    /**
     * Gets the number of connections successfully loaded.
     *
     * @return the number of connections loaded
     */
    int getLoadedConnectionCount();
    
    /**
     * Gets the number of stations that were rejected during loading.
     *
     * @return the number of rejected stations
     */
    int getRejectedStationCount();
    
    /**
     * Gets the number of connections that were rejected during loading.
     *
     * @return the number of rejected connections
     */
    int getRejectedConnectionCount();
}

