package Domain;

import java.util.*;

/**
 * Represents an undirected graph of railway stations and their connections.
 * 
 * The graph maintains:
 * - A list of all stations
 * - An adjacency map for efficient neighbor lookups
 * 
 * Edges are always added in both directions (undirected graph).
 * This is a domain model class following Clean Architecture principles.
 *
 * @author Development Team
 * @version 1.0
 */
public class Graph {
    private final List<Station> stations;
    private final Map<String, Station> stationById;
    private final Map<String, List<Connection>> adjacencyMap;
    
    /**
     * Constructs an empty Graph.
     */
    public Graph() {
        this.stations = new ArrayList<>();
        this.stationById = new HashMap<>();
        this.adjacencyMap = new HashMap<>();
    }
    
    /**
     * Adds a station to the graph.
     * If a station with the same ID already exists, it is preserved (duplicates are ignored).
     *
     * @param station the station to add (cannot be null)
     * @throws IllegalArgumentException if station is null
     */
    public void addStation(Station station) {
        if (station == null) {
            throw new IllegalArgumentException("Station cannot be null");
        }
        
        String id = station.getId();
        if (!stationById.containsKey(id)) {
            stations.add(station);
            stationById.put(id, station);
            adjacencyMap.putIfAbsent(id, new ArrayList<>());
        }
    }
    
    /**
     * Adds a connection between two stations.
     * 
     * The connection is added in both directions (undirected graph):
     * - origin -> destination
     * - destination -> origin
     * 
     * Both stations must exist in the graph before adding the connection.
     *
     * @param connection the connection to add (cannot be null)
     * @throws IllegalArgumentException if connection is null or stations don't exist
     */
    public void addConnection(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }
        
        String originId = connection.getOriginId();
        String destinationId = connection.getDestinationId();
        
        if (!stationById.containsKey(originId)) {
            throw new IllegalArgumentException("Origin station '" + originId + "' does not exist in graph");
        }
        if (!stationById.containsKey(destinationId)) {
            throw new IllegalArgumentException("Destination station '" + destinationId + "' does not exist in graph");
        }
        
        // Add connection in both directions (undirected graph)
        adjacencyMap.get(originId).add(connection);
        adjacencyMap.get(destinationId).add(connection.reverse());
    }
    
    /**
     * Gets all neighbors (connected stations) of a given station.
     * 
     * Returns a list of station IDs that are directly connected to the specified station.
     * Time complexity: O(degree), where degree is the number of neighbors.
     *
     * @param stationId the station ID to get neighbors for
     * @return a list of neighbor station IDs (empty list if station not found or has no neighbors)
     */
    public List<String> getNeighbors(String stationId) {
        if (stationId == null || stationId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Connection> connections = adjacencyMap.get(stationId.trim());
        if (connections == null) {
            return new ArrayList<>();
        }
        
        List<String> neighbors = new ArrayList<>();
        for (Connection conn : connections) {
            // For each connection, determine which is the neighbor
            if (conn.getOriginId().equals(stationId)) {
                neighbors.add(conn.getDestinationId());
            } else {
                neighbors.add(conn.getOriginId());
            }
        }
        
        return neighbors;
    }
    
    /**
     * Gets a station by its ID.
     * Time complexity: O(1).
     *
     * @param stationId the station ID
     * @return the station, or null if not found
     */
    public Station getStation(String stationId) {
        if (stationId == null) {
            return null;
        }
        return stationById.get(stationId.trim());
    }
    
    /**
     * Gets all stations in the graph.
     *
     * @return an unmodifiable list of all stations
     */
    public List<Station> getStations() {
        return Collections.unmodifiableList(stations);
    }
    
    /**
     * Gets all connections for a specific station.
     *
     * @param stationId the station ID
     * @return an unmodifiable list of connections, or empty list if station not found
     */
    public List<Connection> getConnections(String stationId) {
        if (stationId == null || stationId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Connection> connections = adjacencyMap.get(stationId.trim());
        return connections != null ? Collections.unmodifiableList(connections) : new ArrayList<>();
    }
    
    /**
     * Gets all connections in the graph.
     * Note: Since edges are stored in both directions, this returns each edge twice.
     * For a unique set of edges, use getAllUniqueConnections().
     *
     * @return a list of all connections (each edge appears twice)
     */
    public List<Connection> getAllConnections() {
        List<Connection> allConnections = new ArrayList<>();
        for (List<Connection> connections : adjacencyMap.values()) {
            allConnections.addAll(connections);
        }
        return allConnections;
    }
    
    /**
     * Gets all unique connections in the graph.
     * Since the graph is undirected, each edge is stored in both directions.
     * This method returns only one direction per edge (originId < destinationId).
     *
     * @return a set of unique connections
     */
    public Set<Connection> getAllUniqueConnections() {
        Set<Connection> uniqueConnections = new HashSet<>();
        Set<String> processed = new HashSet<>();
        
        for (List<Connection> connections : adjacencyMap.values()) {
            for (Connection conn : connections) {
                String key1 = conn.getOriginId() + "->" + conn.getDestinationId();
                String key2 = conn.getDestinationId() + "->" + conn.getOriginId();
                
                if (!processed.contains(key1) && !processed.contains(key2)) {
                    uniqueConnections.add(conn);
                    processed.add(key1);
                    processed.add(key2);
                }
            }
        }
        
        return uniqueConnections;
    }
    
    /**
     * Gets the number of stations in the graph.
     *
     * @return the number of stations
     */
    public int getStationCount() {
        return stations.size();
    }
    
    /**
     * Gets the number of unique connections (edges) in the graph.
     *
     * @return the number of unique edges
     */
    public int getConnectionCount() {
        return getAllUniqueConnections().size();
    }
    
    /**
     * Checks if the graph contains a station with the given ID.
     *
     * @param stationId the station ID to check
     * @return true if the station exists, false otherwise
     */
    public boolean containsStation(String stationId) {
        if (stationId == null) {
            return false;
        }
        return stationById.containsKey(stationId.trim());
    }
    
    @Override
    public String toString() {
        return String.format("Graph{stations=%d, connections=%d}", 
                getStationCount(), getConnectionCount());
    }
}

