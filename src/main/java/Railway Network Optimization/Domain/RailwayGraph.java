package Domain;

import java.util.*;

/**
 * Represents a directed graph of railway stations and their connections.
 * This graph is used to model upgrade dependencies between stations.
 * 
 * This class belongs to the Domain layer and contains the structure and logic
 * for representing a directed graph. It does NOT know about repositories or data sources.
 * 
 * @author Development Team
 * @version 1.0
 */
public class RailwayGraph {
    private final Map<String, Station> nodes;
    private final Map<String, List<Line>> outgoingEdges;
    private final Map<String, List<Line>> incomingEdges;
    private final List<Line> allEdges;
    
    /**
     * Constructs an empty RailwayGraph.
     */
    public RailwayGraph() {
        this.nodes = new HashMap<>();
        this.outgoingEdges = new HashMap<>();
        this.incomingEdges = new HashMap<>();
        this.allEdges = new ArrayList<>();
    }
    
    /**
     * Adds a station node to the graph.
     *
     * @param node the station to add
     * @throws IllegalArgumentException if node is null
     */
    public void addNode(Station node) {
        if (node == null) {
            throw new IllegalArgumentException("Node cannot be null");
        }
        nodes.put(node.getId(), node);
        outgoingEdges.putIfAbsent(node.getId(), new ArrayList<>());
        incomingEdges.putIfAbsent(node.getId(), new ArrayList<>());
    }
    
    /**
     * Adds a directed edge to the graph.
     * Both source and destination nodes must already exist in the graph.
     *
     * @param edge the line/edge to add
     * @throws IllegalArgumentException if edge is null or nodes don't exist
     */
    public void addEdge(Line edge) {
        if (edge == null) {
            throw new IllegalArgumentException("Edge cannot be null");
        }
        String fromId = edge.getDepartureStationId();
        String toId = edge.getArrivalStationId();
        
        if (!nodes.containsKey(fromId)) {
            throw new IllegalArgumentException("Source node does not exist: " + fromId);
        }
        if (!nodes.containsKey(toId)) {
            throw new IllegalArgumentException("Destination node does not exist: " + toId);
        }
        
        outgoingEdges.get(fromId).add(edge);
        incomingEdges.get(toId).add(edge);
        allEdges.add(edge);
    }
    
    /**
     * Gets a station by its ID.
     *
     * @param stationId the station ID
     * @return the station, or null if not found
     */
    public Station getNode(String stationId) {
        return nodes.get(stationId);
    }
    
    /**
     * Gets all stations in the graph.
     *
     * @return a collection of all nodes
     */
    public Collection<Station> getAllNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }
    
    /**
     * Gets all outgoing edges from a given station.
     *
     * @param stationId the station ID
     * @return a list of outgoing edges, or empty list if station doesn't exist
     */
    public List<Line> getOutgoingEdges(String stationId) {
        return Collections.unmodifiableList(
            outgoingEdges.getOrDefault(stationId, new ArrayList<>())
        );
    }
    
    /**
     * Gets all incoming edges to a given station.
     *
     * @param stationId the station ID
     * @return a list of incoming edges, or empty list if station doesn't exist
     */
    public List<Line> getIncomingEdges(String stationId) {
        return Collections.unmodifiableList(
            incomingEdges.getOrDefault(stationId, new ArrayList<>())
        );
    }
    
    /**
     * Gets all edges in the graph.
     *
     * @return a list of all edges
     */
    public List<Line> getAllEdges() {
        return Collections.unmodifiableList(allEdges);
    }
    
    /**
     * Gets the number of nodes in the graph.
     *
     * @return the number of nodes
     */
    public int getNodeCount() {
        return nodes.size();
    }
    
    /**
     * Gets the number of edges in the graph.
     *
     * @return the number of edges
     */
    public int getEdgeCount() {
        return allEdges.size();
    }
    
    /**
     * Checks if a node exists in the graph.
     *
     * @param stationId the station ID
     * @return true if the node exists, false otherwise
     */
    public boolean containsNode(String stationId) {
        return nodes.containsKey(stationId);
    }
    
    /**
     * Gets all neighbor nodes (stations reachable via outgoing edges) from a given station.
     *
     * @param stationId the station ID
     * @return a set of neighbor stations
     */
    public Set<Station> getNeighbors(String stationId) {
        Set<Station> neighbors = new HashSet<>();
        for (Line edge : getOutgoingEdges(stationId)) {
            Station neighbor = getNode(edge.getArrivalStationId());
            if (neighbor != null) {
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }
}

