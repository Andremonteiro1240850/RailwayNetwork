package Domain;

import java.util.*;

/**
 * Implements the Ford-Fulkerson algorithm for computing maximum flow in a directed graph.
 * 
 * This implementation strictly follows the formal definition of the Ford-Fulkerson algorithm
 * as taught in academic contexts (ESINF Graphs material).
 * 
 * Key features:
 * - Complete residual graph with forward and reverse edges created from the beginning
 * - Augmenting paths defined as sequences of residual edges, not just vertices
 * - BFS stores explicit references to residual edges used to reach each node
 * - Bidirectional linkage between forward and reverse residual edges
 * - Correct handling of multiple edges between the same pair of nodes
 * 
 * The algorithm works by:
 * 1. Building a complete residual graph with all forward and reverse edges
 * 2. Repeatedly finding augmenting paths (sequences of residual edges) from source to sink
 * 3. For each path, computing the bottleneck as the minimum residual capacity among path edges
 * 4. Updating the residual graph by decreasing forward edge capacities and increasing reverse edge capacities
 * 5. Continuing until no augmenting path with positive residual capacity exists
 * 
 * Flow conservation: For every intermediate station, total incoming flow equals total outgoing flow.
 * Capacity constraint: Flow on each edge never exceeds its capacity.
 * 
 * @author Development Team
 * @version 2.0
 */
public class FordFulkersonAlgorithm {
    
    /**
     * Represents a residual edge in the residual graph.
     * Each residual edge maintains:
     * - Destination node
     * - Residual capacity
     * - Reference to the corresponding reverse residual edge (for bidirectional linkage)
     * - Original line (for tracking purposes)
     */
    private static class ResidualEdge {
        final String to;
        int residualCapacity;
        ResidualEdge reverseEdge; // Explicit bidirectional linkage
        final Line originalLine; // Reference to original line (null for reverse edges)
        
        ResidualEdge(String to, int residualCapacity, Line originalLine) {
            this.to = to;
            this.residualCapacity = residualCapacity;
            this.originalLine = originalLine;
            this.reverseEdge = null; // Will be set during graph construction
        }
        
        /**
         * Sets the reverse edge and establishes bidirectional linkage.
         */
        void setReverseEdge(ResidualEdge reverse) {
            this.reverseEdge = reverse;
        }
    }
    
    /**
     * Computes the maximum flow from source to sink in the given directed graph.
     * 
     * This method implements the formal Ford-Fulkerson algorithm:
     * - Builds a complete residual graph with all forward and reverse edges
     * - Finds augmenting paths as sequences of residual edges
     * - Updates residual capacities correctly using bidirectional edge references
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
        
        // Build complete residual graph with all forward and reverse edges
        // Forward edges have capacity = original capacity, reverse edges have capacity = 0
        Map<String, List<ResidualEdge>> residualGraph = buildResidualGraph(graph);
        
        int maxFlow = 0;
        
        // Repeatedly find augmenting paths until none exist
        while (true) {
            // Find augmenting path as a sequence of residual edges
            List<ResidualEdge> augmentingPath = findAugmentingPath(residualGraph, sourceStationId, sinkStationId);
            
            if (augmentingPath == null || augmentingPath.isEmpty()) {
                // No more augmenting paths exist
                break;
            }
            
            // Find minimum residual capacity along the path (bottleneck)
            // This is the minimum capacity among all residual edges in the path
            int minCapacity = findMinResidualCapacity(augmentingPath);
            
            if (minCapacity <= 0) {
                break; // No capacity available
            }
            
            // Add flow to total
            maxFlow += minCapacity;
            
            // Update residual graph: decrease forward edge capacities, increase reverse edge capacities
            // Uses explicit bidirectional linkage between forward and reverse edges
            updateResidualGraph(augmentingPath, minCapacity);
        }
        
        return maxFlow;
    }
    
    /**
     * Builds the complete residual graph from the original graph.
     * 
     * For every original directed edge (u → v) with capacity c:
     * - Creates a forward residual edge (u → v) with capacity c
     * - Creates a reverse residual edge (v → u) with capacity 0
     * - Establishes explicit bidirectional linkage between forward and reverse edges
     * 
     * All reverse edges are created from the beginning, not dynamically during execution.
     * This ensures the residual graph is complete and follows the formal definition.
     * 
     * @param graph the original directed graph
     * @return the complete residual graph as a map from station ID to list of residual edges
     */
    private static Map<String, List<ResidualEdge>> buildResidualGraph(RailwayGraph graph) {
        Map<String, List<ResidualEdge>> residualGraph = new HashMap<>();
        
        // Initialize all stations with empty edge lists
        for (Station station : graph.getAllNodes()) {
            residualGraph.put(station.getId(), new ArrayList<>());
        }
        
        // Process all original edges to create forward and reverse residual edges
        for (Line line : graph.getAllEdges()) {
            String from = line.getDepartureStationId();
            String to = line.getArrivalStationId();
            int capacity = line.getCapacity();
            
            // Create forward residual edge with original capacity
            ResidualEdge forwardEdge = new ResidualEdge(to, capacity, line);
            
            // Create reverse residual edge with capacity 0 (must exist from the beginning)
            ResidualEdge reverseEdge = new ResidualEdge(from, 0, null);
            
            // Establish bidirectional linkage
            forwardEdge.setReverseEdge(reverseEdge);
            reverseEdge.setReverseEdge(forwardEdge);
            
            // Add forward edge to residual graph
            residualGraph.get(from).add(forwardEdge);
            
            // Add reverse edge to residual graph (must exist from the beginning)
            residualGraph.get(to).add(reverseEdge);
        }
        
        return residualGraph;
    }
    
    /**
     * Represents a path entry storing both the source node and the residual edge used.
     * This is needed to correctly reconstruct the augmenting path.
     */
    private static class PathEntry {
        final String from;
        final ResidualEdge edge;
        
        PathEntry(String from, ResidualEdge edge) {
            this.from = from;
            this.edge = edge;
        }
    }
    
    /**
     * Finds an augmenting path from source to sink using BFS.
     * 
     * An augmenting path is defined as a sequence of residual edges (not just vertices)
     * from source to sink, where each edge has positive residual capacity.
     * 
     * This implementation stores, for each visited node, both the source node and the 
     * residual edge through which it was reached. This is required by the formal definition
     * to correctly reconstruct the path as a sequence of residual edges.
     * 
     * @param residualGraph the residual graph
     * @param source the source station ID
     * @param sink the sink station ID
     * @return a list of residual edges representing the augmenting path, or null if no path exists
     */
    private static List<ResidualEdge> findAugmentingPath(Map<String, List<ResidualEdge>> residualGraph, 
                                                         String source, String sink) {
        // BFS to find path
        Queue<String> queue = new LinkedList<>();
        // Store both the source node and the residual edge used to reach each node
        // This allows correct reconstruction of the path as a sequence of residual edges
        Map<String, PathEntry> pathInfo = new HashMap<>();
        Set<String> visited = new HashSet<>();
        
        queue.offer(source);
        visited.add(source);
        pathInfo.put(source, new PathEntry(null, null)); // Source has no incoming edge
        
        while (!queue.isEmpty()) {
            String current = queue.poll();
            
            if (current.equals(sink)) {
                // Reconstruct path as a sequence of residual edges
                return reconstructPath(pathInfo, source, sink);
            }
            
            // Explore neighbors through residual edges with positive residual capacity
            List<ResidualEdge> edges = residualGraph.get(current);
            if (edges != null) {
                for (ResidualEdge edge : edges) {
                    // Only consider edges with positive residual capacity
                    if (edge.residualCapacity > 0 && !visited.contains(edge.to)) {
                        visited.add(edge.to);
                        // Store both the source node (current) and the residual edge used
                        pathInfo.put(edge.to, new PathEntry(current, edge));
                        queue.offer(edge.to);
                    }
                }
            }
        }
        
        return null; // No path found
    }
    
    /**
     * Reconstructs the augmenting path as a sequence of residual edges.
     * 
     * The path is built backwards from sink to source using the stored path information.
     * Each step uses the residual edge that was stored when the node was first reached.
     * 
     * @param pathInfo map from node ID to path entry (source node and residual edge)
     * @param source the source station ID
     * @param sink the sink station ID
     * @return a list of residual edges representing the path from source to sink
     */
    private static List<ResidualEdge> reconstructPath(Map<String, PathEntry> pathInfo, 
                                                       String source, String sink) {
        List<ResidualEdge> path = new ArrayList<>();
        String current = sink;
        
        // Build path backwards from sink to source
        while (current != null && !current.equals(source)) {
            PathEntry entry = pathInfo.get(current);
            if (entry == null || entry.edge == null) {
                // Should not happen if path exists, but handle gracefully
                break;
            }
            path.add(0, entry.edge); // Add to front to maintain source-to-sink order
            // Move to the source node of this edge
            current = entry.from;
        }
        
        return path;
    }
    
    /**
     * Finds the minimum residual capacity along an augmenting path.
     * 
     * The bottleneck value is the minimum residual capacity among all residual edges
     * that compose the augmenting path.
     * 
     * @param path the augmenting path as a sequence of residual edges
     * @return the minimum residual capacity (bottleneck value)
     */
    private static int findMinResidualCapacity(List<ResidualEdge> path) {
        if (path == null || path.isEmpty()) {
            return 0;
        }
        
        int minCapacity = Integer.MAX_VALUE;
        
        for (ResidualEdge edge : path) {
            minCapacity = Math.min(minCapacity, edge.residualCapacity);
        }
        
        return minCapacity == Integer.MAX_VALUE ? 0 : minCapacity;
    }
    
    /**
     * Updates the residual graph after sending flow along an augmenting path.
     * 
     * For each residual edge in the path:
     * - Decreases the residual capacity of the forward edge by the flow amount
     * - Increases the residual capacity of the corresponding reverse edge by the flow amount
     * 
     * Uses explicit bidirectional linkage between forward and reverse edges to ensure
     * correct flow cancellation. This is a key requirement of the formal algorithm.
     * 
     * @param path the augmenting path as a sequence of residual edges
     * @param flow the flow amount to send along the path (bottleneck value)
     */
    private static void updateResidualGraph(List<ResidualEdge> path, int flow) {
        for (ResidualEdge edge : path) {
            // Decrease residual capacity of this edge
            edge.residualCapacity -= flow;
            
            // Increase residual capacity of the corresponding reverse edge
            // Using explicit bidirectional linkage
            if (edge.reverseEdge != null) {
                edge.reverseEdge.residualCapacity += flow;
            }
        }
    }
}
