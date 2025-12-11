package Sprint3.Domain;

import Domain.Station;
import Domain.Line;

import java.util.*;

/**
 * Implements cycle detection algorithm for directed graphs using DFS with coloring.
 * 
 * Uses a three-color DFS algorithm:
 * - WHITE: unvisited node
 * - GRAY: node being processed (in current DFS path)
 * - BLACK: node completely processed
 * 
 * A cycle is detected when we encounter a GRAY node during DFS.
 * 
 * Time Complexity: O(V + E) where V is the number of vertices and E is the number of edges.
 * Space Complexity: O(V) for the color map and recursion stack.
 * 
 * @author Development Team
 * @version 1.0
 */
public class CycleDetection {
    
    private enum Color {
        WHITE,  // Unvisited
        GRAY,   // Currently being processed (in DFS path)
        BLACK   // Completely processed
    }
    
    /**
     * Detects if the graph contains cycles.
     * 
     * @param graph the railway graph to check
     * @return true if cycles are detected, false otherwise
     * @throws IllegalArgumentException if graph is null
     */
    public static boolean hasCycles(RailwayGraph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        
        Map<String, Color> color = new HashMap<>();
        
        // Initialize all nodes as WHITE
        for (Station node : graph.getAllNodes()) {
            color.put(node.getId(), Color.WHITE);
        }
        
        // Perform DFS from each unvisited node
        for (Station node : graph.getAllNodes()) {
            if (color.get(node.getId()) == Color.WHITE) {
                if (dfsHasCycle(graph, node, color)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Finds all cycles in the graph and returns information about them.
     * 
     * @param graph the railway graph to analyze
     * @return a CycleInfo object containing nodes and edges involved in cycles
     * @throws IllegalArgumentException if graph is null
     */
    public static CycleInfo findCycles(RailwayGraph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        
        Set<String> cycleNodes = new HashSet<>();
        Set<Line> cycleEdges = new HashSet<>();
        Map<String, Color> color = new HashMap<>();
        Map<String, Station> parent = new HashMap<>();
        List<List<Station>> cycles = new ArrayList<>();
        
        // Initialize all nodes as WHITE
        for (Station node : graph.getAllNodes()) {
            color.put(node.getId(), Color.WHITE);
        }
        
        // Perform DFS from each unvisited node
        for (Station node : graph.getAllNodes()) {
            if (color.get(node.getId()) == Color.WHITE) {
                dfsFindCycles(graph, node, color, parent, cycleNodes, cycleEdges, cycles);
            }
        }
        
        return new CycleInfo(cycleNodes, cycleEdges, cycles);
    }
    
    /**
     * Helper method for cycle detection using DFS.
     * 
     * @param graph the graph
     * @param node the current node
     * @param color the color map
     * @return true if a cycle is found
     */
    private static boolean dfsHasCycle(RailwayGraph graph, Station node, Map<String, Color> color) {
        String nodeId = node.getId();
        color.put(nodeId, Color.GRAY);
        
        // Check all neighbors
        for (Line edge : graph.getOutgoingEdges(nodeId)) {
            String neighborId = edge.getArrivalStationId();
            Color neighborColor = color.get(neighborId);
            
            // If neighbor is GRAY, we found a back edge (cycle)
            if (neighborColor == Color.GRAY) {
                return true;
            }
            
            // If neighbor is WHITE, continue DFS
            if (neighborColor == Color.WHITE) {
                Station neighbor = graph.getNode(neighborId);
                if (neighbor != null && dfsHasCycle(graph, neighbor, color)) {
                    return true;
                }
            }
        }
        
        color.put(nodeId, Color.BLACK);
        return false;
    }
    
    /**
     * Helper method for finding cycles using DFS.
     * 
     * @param graph the graph
     * @param node the current node
     * @param color the color map
     * @param parent the parent map for cycle reconstruction
     * @param cycleNodes set to collect nodes in cycles
     * @param cycleEdges set to collect edges in cycles
     * @param cycles list to collect complete cycle paths
     */
    private static void dfsFindCycles(
            RailwayGraph graph,
            Station node,
            Map<String, Color> color,
            Map<String, Station> parent,
            Set<String> cycleNodes,
            Set<Line> cycleEdges,
            List<List<Station>> cycles) {
        
        String nodeId = node.getId();
        color.put(nodeId, Color.GRAY);
        
        // Check all neighbors
        for (Line edge : graph.getOutgoingEdges(nodeId)) {
            String neighborId = edge.getArrivalStationId();
            Color neighborColor = color.get(neighborId);
            
            // If neighbor is GRAY, we found a back edge (cycle)
            if (neighborColor == Color.GRAY) {
                Station neighbor = graph.getNode(neighborId);
                if (neighbor != null) {
                    // Reconstruct the cycle path
                    List<Station> cycle = reconstructCycle(graph, node, neighbor, parent);
                    if (!cycle.isEmpty()) {
                        cycles.add(cycle);
                        
                        // Mark all nodes and edges in the cycle
                        for (Station cycleNode : cycle) {
                            cycleNodes.add(cycleNode.getId());
                        }
                    }
                }
                cycleEdges.add(edge);
                cycleNodes.add(nodeId);
                cycleNodes.add(neighborId);
            }
            
            // If neighbor is WHITE, continue DFS
            if (neighborColor == Color.WHITE) {
                Station neighbor = graph.getNode(neighborId);
                if (neighbor != null) {
                    parent.put(neighborId, node);
                    dfsFindCycles(graph, neighbor, color, parent, cycleNodes, cycleEdges, cycles);
                }
            }
        }
        
        color.put(nodeId, Color.BLACK);
    }
    
    /**
     * Reconstructs a cycle path from the parent map.
     * When we find a back edge from 'start' to 'end', we trace back from 'start'
     * through the parent map until we reach 'end', then add 'end' to complete the cycle.
     * 
     * @param graph the graph (needed to get node by ID)
     * @param start the start node of the cycle (current node)
     * @param end the end node (the GRAY node we're returning to)
     * @param parent the parent map
     * @return a list of nodes forming the cycle
     */
    private static List<Station> reconstructCycle(
            RailwayGraph graph, Station start, Station end, Map<String, Station> parent) {
        List<Station> cycle = new ArrayList<>();
        
        // If start and end are the same, it's a self-loop
        if (start.equals(end)) {
            cycle.add(start);
            return cycle;
        }
        
        // Trace back from start to end through parent chain
        Station current = start;
        Set<String> visited = new HashSet<>();
        
        while (current != null && !current.equals(end) && !visited.contains(current.getId())) {
            cycle.add(current);
            visited.add(current.getId());
            current = parent.get(current.getId());
        }
        
        // If we found the end node, complete the cycle
        if (current != null && current.equals(end)) {
            cycle.add(end);
            // Add start again to show the cycle is complete
            cycle.add(start);
        } else {
            // If we couldn't reconstruct the full cycle, at least add the nodes we know
            if (cycle.isEmpty()) {
                cycle.add(start);
                cycle.add(end);
            }
        }
        
        return cycle;
    }
    
    /**
     * Represents information about cycles found in the graph.
     */
    public static class CycleInfo {
        private final Set<String> cycleNodeIds;
        private final Set<Line> cycleEdges;
        private final List<List<Station>> cycles;
        
        public CycleInfo(Set<String> cycleNodeIds, Set<Line> cycleEdges, List<List<Station>> cycles) {
            this.cycleNodeIds = new HashSet<>(cycleNodeIds);
            this.cycleEdges = new HashSet<>(cycleEdges);
            this.cycles = new ArrayList<>(cycles);
        }
        
        /**
         * Gets the IDs of all nodes involved in cycles.
         * 
         * @return a set of station IDs
         */
        public Set<String> getCycleNodeIds() {
            return Collections.unmodifiableSet(cycleNodeIds);
        }
        
        /**
         * Gets all edges involved in cycles.
         * 
         * @return a set of edges
         */
        public Set<Line> getCycleEdges() {
            return Collections.unmodifiableSet(cycleEdges);
        }
        
        /**
         * Gets the list of complete cycle paths.
         * 
         * @return a list of cycles, where each cycle is a list of nodes
         */
        public List<List<Station>> getCycles() {
            return Collections.unmodifiableList(cycles);
        }
        
        /**
         * Checks if any cycles were found.
         * 
         * @return true if cycles exist, false otherwise
         */
        public boolean hasCycles() {
            return !cycleNodeIds.isEmpty();
        }
    }
}
