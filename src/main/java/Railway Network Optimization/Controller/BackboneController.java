package Controller;

import Domain.*;
import Interfaces.GraphRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controller for orchestrating minimal backbone network operations.
 * 
 * This controller coordinates between:
 * - GraphRepository (data loading)
 * - MinimalBackboneService (MST computation)
 * - DotExporter (DOT format export)
 * - GraphvizRenderer (SVG rendering)
 * 
 * This controller contains NO domain logic - only orchestration.
 * All business logic is delegated to domain services.
 *
 * @author Development Team
 * @version 1.0
 */
public class BackboneController {
    
    private final GraphRepository graphRepository;
    private final MinimalBackboneService backboneService;
    private final DotExporter dotExporter;
    private final GraphvizRenderer graphvizRenderer;
    
    /**
     * Constructs a BackboneController with default dependencies.
     */
    public BackboneController() {
        this.graphRepository = new Repository.BelgiumGraphRepository();
        this.backboneService = new MinimalBackboneService();
        this.dotExporter = new DotExporter();
        this.graphvizRenderer = new GraphvizRenderer();
    }
    
    /**
     * Constructs a BackboneController with custom dependencies.
     * Useful for dependency injection and testing.
     *
     * @param graphRepository   the graph repository
     * @param backboneService   the MST service
     * @param dotExporter       the DOT exporter
     * @param graphvizRenderer the SVG renderer
     */
    public BackboneController(GraphRepository graphRepository, 
                              MinimalBackboneService backboneService,
                              DotExporter dotExporter,
                              GraphvizRenderer graphvizRenderer) {
        if (graphRepository == null) {
            throw new IllegalArgumentException("GraphRepository cannot be null");
        }
        if (backboneService == null) {
            throw new IllegalArgumentException("MinimalBackboneService cannot be null");
        }
        if (dotExporter == null) {
            throw new IllegalArgumentException("DotExporter cannot be null");
        }
        if (graphvizRenderer == null) {
            throw new IllegalArgumentException("GraphvizRenderer cannot be null");
        }
        
        this.graphRepository = graphRepository;
        this.backboneService = backboneService;
        this.dotExporter = dotExporter;
        this.graphvizRenderer = graphvizRenderer;
    }
    
    /**
     * Generates the minimal backbone network (MST) from the loaded graph.
     * 
     * Orchestration:
     * 1. Load graph from repository
     * 2. Compute MST using backbone service
     * 
     * @return the minimal backbone network as a Graph
     * @throws Exception if there's an error loading the graph or computing MST
     */
    public Graph generateBackbone() throws Exception {
        // Load graph from repository
        Graph fullGraph = graphRepository.loadGraph();
        
        // Compute MST
        Graph mstGraph = backboneService.computeMinimalBackbone(fullGraph);
        
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
     * Gets the temporal complexity analysis for the MST algorithm.
     * 
     * @return a string containing the complexity analysis
     */
    public String getComplexityAnalysis() {
        return backboneService.getComplexityAnalysis();
    }
}

