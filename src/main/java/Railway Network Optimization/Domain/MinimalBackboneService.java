package Domain;

import java.util.*;

/**
 * Service for computing minimal backbone networks (Minimum Spanning Tree) using Kruskal's algorithm with Union-Find.
 * 
 * Computes the Minimum Spanning Tree (MST) of an undirected graph.
 * The algorithm is deterministic:
 * - Edges sorted by distance (ascending)
 * - Ties broken by stationId comparison (ascending)
 * 
 * This service follows Clean Architecture principles:
 * - Pure domain logic with no external dependencies
 * - Deterministic output for same input
 *
 * @author Development Team
 * @version 1.0
 */
public class MinimalBackboneService {
    
    /**
     * Union-Find data structure for Kruskal's algorithm.
     * Supports efficient union and find operations for cycle detection.
     */
    private static class UnionFind {
        private final Map<String, String> parent;
        private final Map<String, Integer> rank;
        
        /**
         * Constructs a Union-Find structure for the given station IDs.
         *
         * @param stationIds the list of station IDs
         */
        public UnionFind(List<String> stationIds) {
            parent = new HashMap<>();
            rank = new HashMap<>();
            
            for (String id : stationIds) {
                parent.put(id, id);
                rank.put(id, 0);
            }
        }
        
        /**
         * Finds the root of the set containing the given station.
         * Uses path compression for efficiency.
         *
         * @param stationId the station ID
         * @return the root station ID
         */
        public String find(String stationId) {
            if (!parent.containsKey(stationId)) {
                return stationId;
            }
            
            // Path compression
            if (!parent.get(stationId).equals(stationId)) {
                parent.put(stationId, find(parent.get(stationId)));
            }
            
            return parent.get(stationId);
        }
        
        /**
         * Unions two sets containing the given stations.
         * Uses union by rank for efficiency.
         *
         * @param stationId1 the first station ID
         * @param stationId2 the second station ID
         * @return true if the union was successful (stations were in different sets), false otherwise
         */
        public boolean union(String stationId1, String stationId2) {
            String root1 = find(stationId1);
            String root2 = find(stationId2);
            
            if (root1.equals(root2)) {
                return false; // Already in the same set (would create cycle)
            }
            
            // Union by rank
            int rank1 = rank.getOrDefault(root1, 0);
            int rank2 = rank.getOrDefault(root2, 0);
            
            if (rank1 < rank2) {
                parent.put(root1, root2);
            } else if (rank1 > rank2) {
                parent.put(root2, root1);
            } else {
                parent.put(root2, root1);
                rank.put(root1, rank1 + 1);
            }
            
            return true; // Successfully merged
        }
    }
    
    /**
     * Edge comparator for deterministic sorting.
     * Primary sort: distance (ascending)
     * Secondary sort: originId (ascending)
     * Tertiary sort: destinationId (ascending)
     */
    private static class EdgeComparator implements Comparator<Connection> {
        @Override
        public int compare(Connection c1, Connection c2) {
            // Primary: distance (ascending)
            int distCompare = Double.compare(c1.getDistance(), c2.getDistance());
            if (distCompare != 0) {
                return distCompare;
            }
            
            // Secondary: originId (ascending)
            int originCompare = c1.getOriginId().compareTo(c2.getOriginId());
            if (originCompare != 0) {
                return originCompare;
            }
            
            // Tertiary: destinationId (ascending)
            return c1.getDestinationId().compareTo(c2.getDestinationId());
        }
    }
    
    /**
     * Computes the minimal backbone network (MST) from a given graph.
     * 
     * The MST connects all reachable stations with minimum total track length.
     * The algorithm is deterministic:
     * - Edges sorted by distance (ascending)
     * - Ties broken by stationId (ascending)
     *
     * @param graph the input graph to compute MST from
     * @return a new Graph containing only the edges of the MST
     * @throws IllegalArgumentException if graph is null or empty
     */
    public Graph computeMinimalBackbone(Graph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        
        if (graph.getStationCount() == 0) {
            throw new IllegalArgumentException("Graph cannot be empty");
        }
        
        // Create new graph for MST
        Graph mstGraph = new Graph();
        
        // Add all stations to MST graph
        for (Station station : graph.getStations()) {
            mstGraph.addStation(station);
        }
        
        // Get all unique connections (edges)
        Set<Connection> allConnections = graph.getAllUniqueConnections();
        
        if (allConnections.isEmpty()) {
            return mstGraph; // No connections, return graph with only stations
        }
        
        // Sort edges deterministically: distance (asc), then originId (asc), then destinationId (asc)
        List<Connection> sortedEdges = new ArrayList<>(allConnections);
        sortedEdges.sort(new EdgeComparator());
        
        // Initialize Union-Find for cycle detection
        List<String> stationIds = new ArrayList<>();
        for (Station station : graph.getStations()) {
            stationIds.add(station.getId());
        }
        UnionFind uf = new UnionFind(stationIds);
        
        // Kruskal's algorithm: add edges in sorted order if they don't create cycles
        int edgesAdded = 0;
        int targetEdges = graph.getStationCount() - 1; // MST has V-1 edges
        
        for (Connection edge : sortedEdges) {
            if (edgesAdded >= targetEdges) {
                break; // MST is complete
            }
            
            String originId = edge.getOriginId();
            String destinationId = edge.getDestinationId();
            
            // Check if adding this edge would create a cycle
            if (uf.union(originId, destinationId)) {
                // No cycle, add edge to MST
                mstGraph.addConnection(edge);
                edgesAdded++;
            }
            // If union returns false, the edge would create a cycle, so skip it
        }
        
        return mstGraph;
    }
    
    /**
     * Gets the temporal complexity analysis information for the MST algorithm.
     * 
     * @return a string containing the complexity analysis
     */
    public String getComplexityAnalysis() {
        StringBuilder sb = new StringBuilder();
        sb.append("TEMPORAL COMPLEXITY ANALYSIS - Minimal Backbone Network (USEI12)\n");
        sb.append("=".repeat(80)).append("\n\n");
        
        sb.append("Algorithm: Kruskal's Minimum Spanning Tree with Union-Find\n");
        sb.append("Implementation: Union-Find with path compression and union by rank\n\n");
        
        sb.append("Time Complexity: O(|E| log |E|)\n");
        sb.append("  Where |E| = number of edges (connections)\n");
        sb.append("  Dominated by: Edge sorting (Timsort) - O(|E| log |E|)\n");
        sb.append("  Kruskal's algorithm: O(|E| × α(|V|)) ≈ O(|E|)\n");
        sb.append("    Where |V| = number of vertices (stations)\n");
        sb.append("    α(|V|) = inverse Ackermann function (practically constant ≤ 4)\n\n");
        
        sb.append("Space Complexity: O(|V| + |E|)\n");
        sb.append("  - Union-Find structures: O(|V|)\n");
        sb.append("  - Sorted edges list: O(|E|)\n");
        sb.append("  - MST graph: O(|V| + |V| - 1) = O(|V|)\n\n");
        
        sb.append("Phase Breakdown:\n");
        sb.append("  1. Validation: O(1)\n");
        sb.append("  2. Add stations to MST: O(|V|)\n");
        sb.append("  3. Sort edges: O(|E| log |E|) [DOMINANT]\n");
        sb.append("  4. Initialize Union-Find: O(|V|)\n");
        sb.append("  5. Kruskal's algorithm: O(|E| × α(|V|)) ≈ O(|E|)\n\n");
        
        sb.append("Note: For practical purposes, α(|V|) is treated as constant.\n");
        sb.append("The overall complexity is dominated by edge sorting: O(|E| log |E|).\n");
        
        return sb.toString();
    }
}

