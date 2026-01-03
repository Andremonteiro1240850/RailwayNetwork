package Controller;

import Domain.*;
import Interfaces.GraphRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Controller for orchestrating minimal backbone network operations.
 * 
 * This controller coordinates between:
 * - GraphRepository (data loading)
 * - DotExporter (DOT format export)
 * - GraphvizRenderer (SVG rendering)
 * 
 * This controller also contains the MST computation logic (Kruskal's algorithm)
 * for easier explanation and understanding.
 *
 * @author Development Team
 * @version 2.0
 */
public class BackboneController {
    
    private final GraphRepository graphRepository;
    private final DotExporter dotExporter;
    private final GraphvizRenderer graphvizRenderer;
    
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
     * Constructs a BackboneController with default dependencies.
     */
    public BackboneController() {
        this.graphRepository = new Repository.BelgiumGraphRepository();
        this.dotExporter = new DotExporter();
        this.graphvizRenderer = new GraphvizRenderer();
    }
    
    /**
     * Constructs a BackboneController with custom dependencies.
     * Useful for dependency injection and testing.
     *
     * @param graphRepository   the graph repository
     * @param dotExporter       the DOT exporter
     * @param graphvizRenderer the SVG renderer
     */
    public BackboneController(GraphRepository graphRepository, 
                              DotExporter dotExporter,
                              GraphvizRenderer graphvizRenderer) {
        if (graphRepository == null) {
            throw new IllegalArgumentException("GraphRepository cannot be null");
        }
        if (dotExporter == null) {
            throw new IllegalArgumentException("DotExporter cannot be null");
        }
        if (graphvizRenderer == null) {
            throw new IllegalArgumentException("GraphvizRenderer cannot be null");
        }
        
        this.graphRepository = graphRepository;
        this.dotExporter = dotExporter;
        this.graphvizRenderer = graphvizRenderer;
    }
    
    /**
     * Computes the minimal backbone network (MST) from a given graph using Kruskal's algorithm.
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
    private Graph computeMinimalBackbone(Graph graph) {
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
     * Generates the minimal backbone network (MST) from the loaded graph.
     * 
     * Orchestration:
     * 1. Load graph from repository
     * 2. Compute MST using Kruskal's algorithm
     * 
     * @return the minimal backbone network as a Graph
     * @throws Exception if there's an error loading the graph or computing MST
     */
    public Graph generateBackbone() throws Exception {
        // Load graph from repository
        Graph fullGraph = graphRepository.loadGraph();
        
        // Compute MST
        Graph mstGraph = computeMinimalBackbone(fullGraph);
        
        return mstGraph;
    }
    
    /**
     * Exports a graph to DOT format and saves it to a file.
     * 
     * Orchestration:
     * 1. Convert graph to DOT string using exporter
     * 2. Write DOT string to file
     * 
     * @param graph      the graph to export
     * @param outputPath  the path where the DOT file should be written
     * @return the path to the created DOT file
     * @throws IOException if there's an error writing the file
     * @throws IllegalArgumentException if graph is null or outputPath is invalid
     */
    public String exportDot(Graph graph, String outputPath) throws IOException {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        if (outputPath == null || outputPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Output path cannot be null or empty");
        }
        
        // Export graph to DOT format
        String dotContent = dotExporter.exportToDot(graph);
        
        // Write to file
        Path dotPath = Paths.get(outputPath);
        
        // Create parent directory if it doesn't exist
        Path parent = dotPath.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        
        Files.write(dotPath, dotContent.getBytes(StandardCharsets.UTF_8));
        
        return dotPath.toAbsolutePath().toString();
    }
    
    /**
     * Exports a graph to SVG format using Graphviz.
     * 
     * Orchestration:
     * 1. Export graph to DOT format
     * 2. Write DOT to temporary file
     * 3. Render SVG using Graphviz
     * 4. Clean up temporary DOT file
     * 
     * @param graph      the graph to export
     * @param outputPath the path where the SVG file should be written
     * @return the path to the created SVG file
     * @throws IOException if there's an error writing files
     * @throws GraphvizRenderer.GraphvizException if Graphviz execution fails
     * @throws IllegalArgumentException if graph is null or outputPath is invalid
     */
    public String exportSvg(Graph graph, String outputPath) throws IOException, GraphvizRenderer.GraphvizException {
        if (graph == null) {
            throw new IllegalArgumentException("Graph cannot be null");
        }
        if (outputPath == null || outputPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Output path cannot be null or empty");
        }
        
        // Export graph to DOT format
        String dotContent = dotExporter.exportToDot(graph);
        
        // Create temporary DOT file
        Path tempDotFile = Files.createTempFile("backbone_", ".dot");
        
        try {
            // Write DOT content to temporary file
            Files.write(tempDotFile, dotContent.getBytes(StandardCharsets.UTF_8));
            
            // Render SVG using Graphviz
            graphvizRenderer.renderSvg(tempDotFile.toString(), outputPath);
            
            return Paths.get(outputPath).toAbsolutePath().toString();
            
        } finally {
            // Clean up temporary DOT file
            try {
                Files.deleteIfExists(tempDotFile);
            } catch (IOException e) {
                // Ignore cleanup errors
            }
        }
    }
    
    /**
     * Generates the minimal backbone network and exports it to DOT format.
     * 
     * Convenience method that combines generateBackbone() and exportDot().
     *
     * @param outputPath the path where the DOT file should be written
     * @return the path to the created DOT file
     * @throws Exception if there's an error in any step
     */
    public String generateAndExportDot(String outputPath) throws Exception {
        Graph mstGraph = generateBackbone();
        return exportDot(mstGraph, outputPath);
    }
    
    /**
     * Generates the minimal backbone network and exports it to SVG format.
     * 
     * Convenience method that combines generateBackbone() and exportSvg().
     *
     * @param outputPath the path where the SVG file should be written
     * @return the path to the created SVG file
     * @throws Exception if there's an error in any step
     */
    public String generateAndExportSvg(String outputPath) throws Exception {
        Graph mstGraph = generateBackbone();
        return exportSvg(mstGraph, outputPath);
    }
    
    /**
     * Gets statistics about the loaded graph.
     * 
     * @return a string with graph statistics
     * @throws Exception if there's an error loading the graph
     */
    public String getGraphStatistics() throws Exception {
        Graph graph = graphRepository.loadGraph();
        
        return String.format(
                "Graph Statistics:\n" +
                "  Stations: %d\n" +
                "  Connections: %d\n" +
                "  Rejected Stations: %d\n" +
                "  Rejected Connections: %d",
                graphRepository.getLoadedStationCount(),
                graphRepository.getLoadedConnectionCount(),
                graphRepository.getRejectedStationCount(),
                graphRepository.getRejectedConnectionCount()
        );
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

