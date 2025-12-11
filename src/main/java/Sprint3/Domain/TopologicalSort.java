package Sprint3.Domain;

import Domain.Station;
import Domain.Line;

import java.util.*;

/**
 * Implements topological sorting algorithms for directed acyclic graphs (DAGs).
 * 
 * Topological sorting produces a linear ordering of vertices that respects the direction 
 * of edges: if there exists an edge (u → v), then u must appear before v in the final list.
 * 
 * This class provides two implementations:
 * 1. Kahn's Algorithm: Based on in-degree observation
 * 2. DFS-based Algorithm: Based on depth-first search with post-order processing
 * 
 * Both methods require the graph to be directed and acyclic. If cycles are detected,
 * a CycleDetectedException is thrown.
 * 
 * @author Development Team
 * @version 1.0
 */
public class TopologicalSort {
    
    /**
     * Performs topological sort using Kahn's algorithm.
     * 
     * Kahn's algorithm works by observing the in-degree of each vertex. It calculates
     * the in-degree (number of incoming edges) for all vertices. Vertices with in-degree 0
     * can appear at the beginning of the ordering since they don't depend on any other vertex.
     * These vertices are placed in a queue and processed one by one: each time we remove
     * a vertex from the queue and add it to the result, we traverse its adjacent vertices
     * and decrease their in-degree, effectively "removing" the influence of the current vertex.
     * If an adjacent vertex's in-degree reaches zero, it becomes ready and is also added to
     * the queue. This process continues while there are vertices in the queue. At the end,
     * if all vertices were processed, the obtained list is a valid topological ordering;
     * if not, the graph contains a cycle and no topological ordering is possible.
     * 
     * Time Complexity: O(V + E) where V is the number of vertices and E is the number of edges.
     * Space Complexity: O(V) for the queue and in-degree map.
     * 
     * Algorithm steps:
     * 1. Calculate in-degree for each vertex (how many edges arrive at each vertex)
     * 2. Add all vertices with in-degree 0 to a queue (they have no dependencies)
     * 3. While queue is not empty:
     *    a. Remove a vertex from queue and add to result
     *    b. For each neighbor (via outgoing edges), decrease its in-degree by 1
     *    c. If any neighbor's in-degree becomes 0, add it to the queue
     * 4. If result size != total vertices, there's a cycle (some vertices couldn't be processed)
     * 
     * @param graph the railway graph to sort
     * @return a list of stations in topological order (respecting edge directions)
     * @throws CycleDetectedException if the graph contains cycles
     * @throws IllegalArgumentException if graph is null
     */
    public static List<Station> order(RailwayGraph graph) throws CycleDetectedException {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        
        // Map to store in-degree of each node
        Map<String, Integer> inDegree = new HashMap<>();
        
        // Initialize in-degree for all nodes
        for (Station node : graph.getAllNodes()) {
            inDegree.put(node.getId(), 0);
        }
        
        // Calculate in-degree for each node
        for (Line edge : graph.getAllEdges()) {
            String toId = edge.getArrivalStationId();
            inDegree.put(toId, inDegree.get(toId) + 1);
        }
        
        // Queue for nodes with in-degree 0
        Queue<Station> queue = new LinkedList<>();
        
        // Add all nodes with in-degree 0 to queue
        for (Station node : graph.getAllNodes()) {
            if (inDegree.get(node.getId()) == 0) {
                queue.offer(node);
            }
        }
        
        // Result list
        List<Station> result = new ArrayList<>();
        
        // Process nodes
        while (!queue.isEmpty()) {
            Station current = queue.poll();
            result.add(current);
            
            // Decrease in-degree of all neighbors
            for (Line edge : graph.getOutgoingEdges(current.getId())) {
                String neighborId = edge.getArrivalStationId();
                int newInDegree = inDegree.get(neighborId) - 1;
                inDegree.put(neighborId, newInDegree);
                
                // If in-degree becomes 0, add to queue
                if (newInDegree == 0) {
                    queue.offer(graph.getNode(neighborId));
                }
            }
        }
        
        // If result size doesn't match total nodes, there's a cycle
        if (result.size() != graph.getNodeCount()) {
            throw new CycleDetectedException(
                "Graph contains cycles. Only " + result.size() + 
                " out of " + graph.getNodeCount() + " nodes could be ordered."
            );
        }
        
        return result;
    }
    
    /**
     * Performs topological sort using Depth-First Search (DFS) algorithm.
     * 
     * The DFS-based method works by choosing any unvisited vertex and starting a DFS.
     * During exploration, each vertex is only added to the final list after all its
     * neighbors (successors) have been fully explored. This ensures that it only
     * appears after all vertices it depends on. At the end, the order in which vertices
     * were added to the structure (typically a stack or list with insertions at the beginning)
     * is reversed. Just like the previous method, DFS only works correctly if the graph
     * has no cycles; otherwise, DFS would detect a cycle and topological ordering would
     * not be possible.
     * 
     * Time Complexity: O(V + E) where V is the number of vertices and E is the number of edges.
     * Space Complexity: O(V) for the recursion stack and visited/temp marks.
     * 
     * Algorithm steps:
     * 1. For each unvisited vertex:
     *    a. Perform DFS starting from that vertex
     *    b. Mark vertices as visited during exploration
     *    c. When backtracking (after exploring all neighbors), add vertex to result stack
     * 2. Reverse the result stack to get topological order
     * 3. If a back edge is detected (visiting a vertex in temporary mark state), cycle exists
     * 
     * @param graph the railway graph to sort
     * @return a list of stations in topological order (respecting edge directions)
     * @throws CycleDetectedException if the graph contains cycles
     * @throws IllegalArgumentException if graph is null
     */
    public static List<Station> orderDFS(RailwayGraph graph) throws CycleDetectedException {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        
        // Color states for DFS cycle detection
        // WHITE = unvisited, GRAY = currently being processed (in recursion stack), BLACK = fully processed
        Map<String, Color> color = new HashMap<>();
        List<Station> result = new ArrayList<>();
        
        // Initialize all vertices as WHITE (unvisited)
        for (Station node : graph.getAllNodes()) {
            color.put(node.getId(), Color.WHITE);
        }
        
        // Perform DFS from each unvisited vertex
        for (Station node : graph.getAllNodes()) {
            if (color.get(node.getId()) == Color.WHITE) {
                dfsVisit(graph, node, color, result);
            }
        }
        
        // Reverse the result list (DFS adds vertices in reverse order)
        Collections.reverse(result);
        
        // Verify all vertices were processed
        if (result.size() != graph.getNodeCount()) {
            throw new CycleDetectedException(
                "Graph contains cycles. Only " + result.size() + 
                " out of " + graph.getNodeCount() + " nodes could be ordered."
            );
        }
        
        return result;
    }
    
    /**
     * Helper method for DFS-based topological sort.
     * Performs DFS starting from a given vertex and adds it to result after all neighbors are processed.
     * 
     * @param graph the railway graph
     * @param current the current vertex being processed
     * @param color map tracking the state of each vertex (WHITE/GRAY/BLACK)
     * @param result the list to which vertices are added in post-order
     * @throws CycleDetectedException if a cycle is detected (back edge found)
     */
    private static void dfsVisit(RailwayGraph graph, Station current, 
                                 Map<String, Color> color, List<Station> result) 
            throws CycleDetectedException {
        // Mark current vertex as GRAY (being processed)
        color.put(current.getId(), Color.GRAY);
        
        // Visit all neighbors (successors via outgoing edges)
        for (Line edge : graph.getOutgoingEdges(current.getId())) {
            String neighborId = edge.getArrivalStationId();
            Station neighbor = graph.getNode(neighborId);
            
            if (neighbor == null) {
                continue; // Skip if neighbor doesn't exist
            }
            
            Color neighborColor = color.get(neighborId);
            
            // If neighbor is GRAY, we found a back edge = cycle detected
            if (neighborColor == Color.GRAY) {
                throw new CycleDetectedException(
                    "Cycle detected: back edge from " + current.getId() + " to " + neighborId
                );
            }
            
            // If neighbor is WHITE (unvisited), recursively visit it
            if (neighborColor == Color.WHITE) {
                dfsVisit(graph, neighbor, color, result);
            }
            // If neighbor is BLACK, it's already fully processed, skip
        }
        
        // Mark current vertex as BLACK (fully processed) and add to result
        // This happens after all neighbors have been explored (post-order)
        color.put(current.getId(), Color.BLACK);
        result.add(current);
    }
    
    /**
     * Color states for DFS cycle detection.
     */
    private enum Color {
        WHITE,  // Unvisited
        GRAY,   // Currently being processed (in recursion stack)
        BLACK   // Fully processed
    }
    
    /**
     * Custom exception thrown when a cycle is detected during topological sorting.
     */
    public static class CycleDetectedException extends Exception {
        public CycleDetectedException(String message) {
            super(message);
        }
    }
}
