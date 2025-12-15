package Domain;

import java.util.*;

/**
 * Implements the Ford-Fulkerson algorithm for computing maximum flow in a directed graph.
 * 
 * This algorithm finds the maximum flow from a source station to a sink station
 * in a directed flow network, respecting edge capacities and flow conservation constraints.
 * 
 * The algorithm works by:
 * 1. Repeatedly finding augmenting paths from source to sink in the residual graph
 * 2. For each path, determining the minimum residual capacity (bottleneck)
 * 3. Adding this flow value to the total flow
 * 4. Updating the residual graph by decreasing forward edge capacities and increasing reverse edge capacities
 * 5. Continuing until no more augmenting paths exist
 * 
 * Flow conservation: For every intermediate station, total incoming flow equals total outgoing flow.
 * Capacity constraint: Flow on each edge never exceeds its capacity.
 * 
 * @author Development Team
 * @version 1.0
 */
public class FordFulkersonAlgorithm {
    
    /**
     * Represents an edge in the residual graph with its residual capacity.
     */
    private static class ResidualEdge {
        final String to;
        int residualCapacity;
        final Line originalLine; // Reference to original line for capacity lookup
        
        ResidualEdge(String to, int residualCapacity, Line originalLine) {
            this.to = to;
            this.residualCapacity = residualCapacity;
            this.originalLine = originalLine;
        }
    }
    
    /**
     * Computes the maximum flow from source to sink in the given directed graph.
     * 
     * @param graph the directed graph with edge capacities
     * @param sourceStationId the source station ID
     * @param sinkStationId the sink station ID
     * @return the maximum flow value
     * @throws IllegalArgumentException if graph is null, stations don't exist, or are the same
     */
    public static int computeMaxFlow(RailwayGraph graph, String sourceStationId, String sinkStationId) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        if (sourceStationId == null || sinkStationId == null) {
            throw new IllegalArgumentException("Source and sink station IDs cannot be null");
        }
        if (sourceStationId.equals(sinkStationId)) {
            throw new IllegalArgumentException("Source and sink stations cannot be the same");
        }
        if (!graph.containsNode(sourceStationId)) {
            throw new IllegalArgumentException("Source station does not exist: " + sourceStationId);
        }
        if (!graph.containsNode(sinkStationId)) {
            throw new IllegalArgumentException("Sink station does not exist: " + sinkStationId);
        }
        
        // Build residual graph: Map<from, List<ResidualEdge>>
        // Each edge has forward capacity (original capacity) and reverse capacity (initially 0)
        Map<String, List<ResidualEdge>> residualGraph = buildResidualGraph(graph);
        
        int maxFlow = 0;
        
        // Repeatedly find augmenting paths until none exist
        while (true) {
            // Find augmenting path using BFS
            List<String> augmentingPath = findAugmentingPath(residualGraph, sourceStationId, sinkStationId);
            
            if (augmentingPath == null || augmentingPath.isEmpty()) {
                // No more augmenting paths exist
                break;
            }
            
            // Find minimum residual capacity along the path (bottleneck)
            int minCapacity = findMinResidualCapacity(residualGraph, augmentingPath);
            
            if (minCapacity <= 0) {
                break; // No capacity available
            }
            
            // Add flow to total
            maxFlow += minCapacity;
            
            // Update residual graph: decrease forward edges, increase reverse edges
            updateResidualGraph(residualGraph, augmentingPath, minCapacity);
        }
        
        return maxFlow;
    }
    
    /**
     * Builds the initial residual graph from the original graph.
     * Forward edges have capacity equal to the original edge capacity.
     * Reverse edges are created with initial capacity 0.
     * 
     * @param graph the original directed graph
     * @return the residual graph as a map from station ID to list of residual edges
     */
    private static Map<String, List<ResidualEdge>> buildResidualGraph(RailwayGraph graph) {
        Map<String, List<ResidualEdge>> residualGraph = new HashMap<>();
        
        // Initialize all stations
        for (Station station : graph.getAllNodes()) {
            residualGraph.put(station.getId(), new ArrayList<>());
        }
        
        // Add forward edges with original capacity
        // Reverse edges will be created dynamically when needed during flow updates
        for (Line line : graph.getAllEdges()) {
            String from = line.getDepartureStationId();
            String to = line.getArrivalStationId();
            int capacity = line.getCapacity();
            
            // Add forward edge with original capacity
            residualGraph.get(from).add(new ResidualEdge(to, capacity, line));
        }
        
        return residualGraph;
    }
    
    /**
     * Finds an augmenting path from source to sink using BFS.
     * 
     * @param residualGraph the residual graph
     * @param source the source station ID
     * @param sink the sink station ID
     * @return a list of station IDs representing the path, or null if no path exists
     */
    private static List<String> findAugmentingPath(Map<String, List<ResidualEdge>> residualGraph, 
                                                   String source, String sink) {
        // BFS to find path
        Queue<String> queue = new LinkedList<>();
        Map<String, String> parent = new HashMap<>();
        Set<String> visited = new HashSet<>();
        
        queue.offer(source);
        visited.add(source);
        parent.put(source, null);
        
        while (!queue.isEmpty()) {
            String current = queue.poll();
            
            if (current.equals(sink)) {
                // Reconstruct path
                return reconstructPath(parent, source, sink);
            }
            
            // Explore neighbors with positive residual capacity
            List<ResidualEdge> edges = residualGraph.get(current);
            if (edges != null) {
                for (ResidualEdge edge : edges) {
                    if (edge.residualCapacity > 0 && !visited.contains(edge.to)) {
                        visited.add(edge.to);
                        parent.put(edge.to, current);
                        queue.offer(edge.to);
                    }
                }
            }
        }
        
        return null; // No path found
    }
    
    /**
     * Reconstructs the path from parent map.
     */
    private static List<String> reconstructPath(Map<String, String> parent, String source, String sink) {
        List<String> path = new ArrayList<>();
        String current = sink;
        
        while (current != null) {
            path.add(0, current); // Add to front
            current = parent.get(current);
        }
        
        return path;
    }
    
    /**
     * Finds the minimum residual capacity along the augmenting path.
     */
    private static int findMinResidualCapacity(Map<String, List<ResidualEdge>> residualGraph, 
                                               List<String> path) {
        int minCapacity = Integer.MAX_VALUE;
        
        for (int i = 0; i < path.size() - 1; i++) {
            String from = path.get(i);
            String to = path.get(i + 1);
            
            // Find edge capacity
            List<ResidualEdge> edges = residualGraph.get(from);
            for (ResidualEdge edge : edges) {
                if (edge.to.equals(to)) {
                    minCapacity = Math.min(minCapacity, edge.residualCapacity);
                    break;
                }
            }
        }
        
        return minCapacity == Integer.MAX_VALUE ? 0 : minCapacity;
    }
    
    /**
     * Updates the residual graph after sending flow along the augmenting path.
     * Decreases forward edge capacities and increases reverse edge capacities.
     */
    private static void updateResidualGraph(Map<String, List<ResidualEdge>> residualGraph,
                                           List<String> path, int flow) {
        for (int i = 0; i < path.size() - 1; i++) {
            String from = path.get(i);
            String to = path.get(i + 1);
            
            // Decrease forward edge capacity
            List<ResidualEdge> forwardEdges = residualGraph.get(from);
            for (ResidualEdge edge : forwardEdges) {
                if (edge.to.equals(to)) {
                    edge.residualCapacity -= flow;
                    break;
                }
            }
            
            // Increase reverse edge capacity (or create if doesn't exist)
            List<ResidualEdge> reverseEdges = residualGraph.get(to);
            boolean reverseExists = false;
            for (ResidualEdge edge : reverseEdges) {
                if (edge.to.equals(from)) {
                    edge.residualCapacity += flow;
                    reverseExists = true;
                    break;
                }
            }
            
            // If reverse edge doesn't exist, create it
            if (!reverseExists) {
                reverseEdges.add(new ResidualEdge(from, flow, null));
            }
        }
    }
}

