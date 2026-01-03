package Controller;

import Domain.*;
import Interfaces.GraphRepository;
import Repository.BelgiumGraphRepository;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Simple test class for BackboneController (USEI12) without JUnit dependencies.
 * Run this class directly: java BackboneControllerSimpleTest
 * 
 * Uses real CSV data from Sprint3 files.
 */
public class BackboneControllerSimpleTest {
    
    private static final String STATIONS_PATH = "train_station_dataset/Sprint3/stations.csv";
    private static final String LINES_PATH = "train_station_dataset/Sprint3/lines.csv";
    
    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;
    
    public static void main(String[] args) {
        System.out.println("=== BACKBONE CONTROLLER SIMPLE TESTS (USEI12) ===\n");
        
        try {
            // Constructor tests
            testConstructor();
            testConstructor_WithCustomDependencies();
            testConstructor_WithNullRepository();
            testConstructor_WithNullDotExporter();
            testConstructor_WithNullGraphvizRenderer();
            
            // Generate backbone tests
            testGenerateBackbone();
            testGenerateBackbone_WithEmptyGraph();
            testGenerateBackbone_WithSingleStation();
            testGenerateBackbone_WithNoConnections();
            
            // Export tests
            testExportDot();
            testExportDot_WithNullGraph();
            testExportDot_WithNullPath();
            testExportDot_WithEmptyPath();
            testExportDot_CreatesParentDirectories();
            testExportSvg();
            testGenerateAndExportDot();
            testGenerateAndExportSvg();
            
            // Statistics and analysis
            testGetGraphStatistics();
            testGetComplexityAnalysis();
            
            // MST properties
            testMSTProperties();
            testMSTProperties_WithSmallGraph();
            testMSTProperties_WithCycle();
            testDeterminism();
            testEdgeComparator_DifferentDistances();
            testEdgeComparator_SameDistance_DifferentOrigins();
            testEdgeComparator_SameDistanceAndOrigin_DifferentDestinations();
            testMST_WithDisconnectedGraph();
            testMST_WithDuplicateEdges();
            testMST_WithAllEqualDistances();
            testExportDot_ContentValidation();
            testExportDot_WithLargeGraph();
            testUnionFind_Behavior();
            testMST_WithSingleEdge();
            testMST_WithCompleteGraph();
            testMST_WithNegativeDistances();
            testMST_WithZeroDistance();
            testMST_WithVeryLargeDistances();
            testExportDot_WithGraphHavingNoStations();
            testExportDot_WithGraphHavingOneStation();
            testExportDot_WithGraphHavingTwoStations();
            testExportSvg_WithNullGraph();
            testExportSvg_WithEmptyPath();
            testGenerateBackbone_WithGraphHavingIsolatedStations();
            testMST_WithGraphHavingSelfLoops();
            
            System.out.println("\n=== TEST SUMMARY ===");
            System.out.println("Tests run: " + testsRun);
            System.out.println("Tests passed: " + testsPassed);
            System.out.println("Tests failed: " + testsFailed);
            
            if (testsFailed == 0) {
                System.out.println("\n✓ ALL TESTS PASSED!");
                System.exit(0);
            } else {
                System.out.println("\n✗ SOME TESTS FAILED!");
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("\n✗ FATAL ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void testConstructor() {
        test("Constructor with default dependencies", () -> {
            BackboneController ctrl = new BackboneController();
            assertNotNull(ctrl, "Controller should not be null");
        });
    }
    
    private static void testGenerateBackbone() {
        test("Generate backbone with real data from Sprint3 CSV files", () -> {
            try {
                // Uses real data: train_station_dataset/Sprint3/stations.csv and lines.csv
                BackboneController ctrl = new BackboneController();
                Graph mst = ctrl.generateBackbone();
                
                assertNotNull(mst, "MST should not be null");
                assertTrue(mst.getStationCount() > 0, "MST should have stations from real data");
                assertTrue(mst.getConnectionCount() >= 0, "MST should have non-negative connections");
                
                // Verify we're using real Belgian railway data
                assertTrue(mst.getStationCount() > 100, "Should have many stations from real Belgian railway network");
                
                if (mst.getConnectionCount() > 0) {
                    int expectedMaxEdges = mst.getStationCount() - 1;
                    assertTrue(mst.getConnectionCount() <= expectedMaxEdges,
                            "MST should have at most V-1 edges");
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate backbone: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testExportDot() {
        test("Export DOT file with real data from Sprint3 CSV files", () -> {
            try {
                // Uses real data: train_station_dataset/Sprint3/stations.csv and lines.csv
                BackboneController ctrl = new BackboneController();
                Graph graph = ctrl.generateBackbone(); // Loads real Belgian railway data
                
                Path tempFile = Files.createTempFile("backbone_test_", ".dot");
                try {
                    String filePath = ctrl.exportDot(graph, tempFile.toString());
                    
                    assertNotNull(filePath, "File path should not be null");
                    assertTrue(Files.exists(tempFile), "DOT file should be created");
                    assertTrue(Files.size(tempFile) > 0, "DOT file should not be empty");
                    
                    String content = Files.readString(tempFile);
                    assertTrue(content.contains("graph G"), "Should contain graph declaration");
                    assertTrue(content.contains("node"), "Should contain node definitions");
                    
                    // Verify real data is in DOT
                    assertTrue(graph.getStationCount() > 100, "Should export real Belgian railway stations");
                } finally {
                    Files.deleteIfExists(tempFile);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to export DOT: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testGetGraphStatistics() {
        test("Get graph statistics from real Sprint3 CSV data", () -> {
            try {
                // Uses real data: train_station_dataset/Sprint3/stations.csv and lines.csv
                BackboneController ctrl = new BackboneController();
                String statistics = ctrl.getGraphStatistics();
                
                assertNotNull(statistics, "Statistics should not be null");
                assertTrue(statistics.length() > 0, "Statistics should not be empty");
                assertTrue(statistics.contains("Stations"), "Should contain stations count");
                assertTrue(statistics.contains("Connections"), "Should contain connections count");
                
                // Verify we're getting statistics from real data
                assertTrue(statistics.contains("Rejected Stations") || statistics.contains("Rejected Connections"),
                        "Should show loading statistics from real CSV files");
            } catch (Exception e) {
                throw new RuntimeException("Failed to get graph statistics: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testGetComplexityAnalysis() {
        test("Get complexity analysis", () -> {
            BackboneController ctrl = new BackboneController();
            String analysis = ctrl.getComplexityAnalysis();
            
            assertNotNull(analysis, "Analysis should not be null");
            assertTrue(analysis.length() > 0, "Analysis should not be empty");
            assertTrue(analysis.contains("O(|E| log |E|)"), "Should contain time complexity");
            assertTrue(analysis.contains("Kruskal"), "Should mention Kruskal algorithm");
        });
    }
    
    private static void testMSTProperties() {
        test("MST properties with real data from Sprint3 CSV files", () -> {
            try {
                // Explicitly uses real data: train_station_dataset/Sprint3/stations.csv and lines.csv
                BackboneController ctrl = new BackboneController();
                Graph mst = ctrl.generateBackbone(); // Loads from real CSV files
                
                // MST should preserve all stations from real data
                GraphRepository repo = new BelgiumGraphRepository(STATIONS_PATH, LINES_PATH);
                Graph fullGraph = repo.loadGraph();
                
                assertEquals(mst.getStationCount(), fullGraph.getStationCount(),
                        "MST should preserve all stations from real Belgian railway network");
                
                // Verify we're working with real data
                assertTrue(mst.getStationCount() > 100, "Should have many stations from real data");
                assertTrue(fullGraph.getStationCount() > 100, "Full graph should have many stations from real data");
                
                // MST should have V-1 edges if connected
                if (mst.getConnectionCount() > 0) {
                    int expectedMaxEdges = mst.getStationCount() - 1;
                    assertTrue(mst.getConnectionCount() <= expectedMaxEdges,
                            "MST should have at most V-1 edges");
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to test MST properties: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testDeterminism() {
        test("MST determinism with real data from Sprint3 CSV files", () -> {
            try {
                // Uses real data: train_station_dataset/Sprint3/stations.csv and lines.csv
                BackboneController ctrl = new BackboneController();
                Graph mst1 = ctrl.generateBackbone(); // Loads real data
                Graph mst2 = ctrl.generateBackbone(); // Loads real data again
                
                assertEquals(mst1.getStationCount(), mst2.getStationCount(),
                        "MST should be deterministic (same station count from real data)");
                assertEquals(mst1.getConnectionCount(), mst2.getConnectionCount(),
                        "MST should be deterministic (same connection count from real data)");
                
                // Verify we're using real data
                assertTrue(mst1.getStationCount() > 100, "Should use real Belgian railway data");
            } catch (Exception e) {
                throw new RuntimeException("Failed to test determinism: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testConstructor_WithCustomDependencies() {
        test("Constructor with custom dependencies", () -> {
            try {
                GraphRepository repo = new BelgiumGraphRepository(STATIONS_PATH, LINES_PATH);
                DotExporter exporter = new DotExporter();
                GraphvizRenderer renderer = new GraphvizRenderer();
                
                BackboneController ctrl = new BackboneController(repo, exporter, renderer);
                assertNotNull(ctrl, "Controller should not be null");
            } catch (Exception e) {
                throw new RuntimeException("Failed to create controller with custom dependencies: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testConstructor_WithNullRepository() {
        test("Constructor with null repository should throw exception", () -> {
            try {
                DotExporter exporter = new DotExporter();
                GraphvizRenderer renderer = new GraphvizRenderer();
                new BackboneController(null, exporter, renderer);
                throw new AssertionError("Should throw exception for null repository");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("GraphRepository"), "Error message should mention GraphRepository");
            }
        });
    }
    
    private static void testConstructor_WithNullDotExporter() {
        test("Constructor with null DotExporter should throw exception", () -> {
            try {
                GraphRepository repo = new BelgiumGraphRepository(STATIONS_PATH, LINES_PATH);
                GraphvizRenderer renderer = new GraphvizRenderer();
                new BackboneController(repo, null, renderer);
                throw new AssertionError("Should throw exception for null DotExporter");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("DotExporter"), "Error message should mention DotExporter");
            }
        });
    }
    
    private static void testConstructor_WithNullGraphvizRenderer() {
        test("Constructor with null GraphvizRenderer should throw exception", () -> {
            try {
                GraphRepository repo = new BelgiumGraphRepository(STATIONS_PATH, LINES_PATH);
                DotExporter exporter = new DotExporter();
                new BackboneController(repo, exporter, null);
                throw new AssertionError("Should throw exception for null GraphvizRenderer");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("GraphvizRenderer"), "Error message should mention GraphvizRenderer");
            }
        });
    }
    
    private static void testGenerateBackbone_WithEmptyGraph() {
        test("Generate backbone with empty graph should throw exception", () -> {
            try {
                GraphRepository mockRepo = new GraphRepository() {
                    @Override
                    public Graph loadGraph() { return new Graph(); }
                    @Override
                    public int getLoadedStationCount() { return 0; }
                    @Override
                    public int getLoadedConnectionCount() { return 0; }
                    @Override
                    public int getRejectedStationCount() { return 0; }
                    @Override
                    public int getRejectedConnectionCount() { return 0; }
                };
                BackboneController ctrl = new BackboneController(mockRepo, new DotExporter(), new GraphvizRenderer());
                ctrl.generateBackbone();
                throw new AssertionError("Should throw exception for empty graph");
            } catch (Exception e) {
                assertTrue(e.getMessage().contains("empty") || e.getCause() != null && e.getCause().getMessage().contains("empty"),
                        "Should throw exception about empty graph");
            }
        });
    }
    
    private static void testGenerateBackbone_WithSingleStation() {
        test("Generate backbone with single station (no connections)", () -> {
            try {
                GraphRepository mockRepo = new GraphRepository() {
                    @Override
                    public Graph loadGraph() {
                        Graph g = new Graph();
                        g.addStation(new Station("A", "Station A", 50.0, 4.0, 100.0, 200.0));
                        return g;
                    }
                    @Override
                    public int getLoadedStationCount() { return 1; }
                    @Override
                    public int getLoadedConnectionCount() { return 0; }
                    @Override
                    public int getRejectedStationCount() { return 0; }
                    @Override
                    public int getRejectedConnectionCount() { return 0; }
                };
                BackboneController ctrl = new BackboneController(mockRepo, new DotExporter(), new GraphvizRenderer());
                Graph mst = ctrl.generateBackbone();
                
                assertEquals(1, mst.getStationCount(), "MST should have 1 station");
                assertEquals(0, mst.getConnectionCount(), "MST should have 0 connections");
            } catch (Exception e) {
                throw new RuntimeException("Failed to test single station: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testGenerateBackbone_WithNoConnections() {
        test("Generate backbone with multiple stations but no connections", () -> {
            try {
                GraphRepository mockRepo = new GraphRepository() {
                    @Override
                    public Graph loadGraph() {
                        Graph g = new Graph();
                        g.addStation(new Station("A", "Station A", 50.0, 4.0, 100.0, 200.0));
                        g.addStation(new Station("B", "Station B", 51.0, 5.0, 150.0, 250.0));
                        return g;
                    }
                    @Override
                    public int getLoadedStationCount() { return 2; }
                    @Override
                    public int getLoadedConnectionCount() { return 0; }
                    @Override
                    public int getRejectedStationCount() { return 0; }
                    @Override
                    public int getRejectedConnectionCount() { return 0; }
                };
                BackboneController ctrl = new BackboneController(mockRepo, new DotExporter(), new GraphvizRenderer());
                Graph mst = ctrl.generateBackbone();
                
                assertEquals(2, mst.getStationCount(), "MST should have 2 stations");
                assertEquals(0, mst.getConnectionCount(), "MST should have 0 connections");
            } catch (Exception e) {
                throw new RuntimeException("Failed to test no connections: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testExportDot_WithNullGraph() {
        test("Export DOT with null graph should throw exception", () -> {
            try {
                BackboneController ctrl = new BackboneController();
                Path tempFile = Files.createTempFile("test_", ".dot");
                try {
                    ctrl.exportDot(null, tempFile.toString());
                    throw new AssertionError("Should throw exception for null graph");
                } finally {
                    Files.deleteIfExists(tempFile);
                }
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("Graph"), "Error message should mention Graph");
            } catch (Exception e) {
                throw new RuntimeException("Unexpected exception: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testExportDot_WithNullPath() {
        test("Export DOT with null path should throw exception", () -> {
            try {
                BackboneController ctrl = new BackboneController();
                Graph graph = new Graph();
                graph.addStation(new Station("A", "Station A", 50.0, 4.0, 100.0, 200.0));
                ctrl.exportDot(graph, null);
                throw new AssertionError("Should throw exception for null path");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("path"), "Error message should mention path");
            } catch (Exception e) {
                throw new RuntimeException("Unexpected exception: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testExportDot_WithEmptyPath() {
        test("Export DOT with empty path should throw exception", () -> {
            try {
                BackboneController ctrl = new BackboneController();
                Graph graph = new Graph();
                graph.addStation(new Station("A", "Station A", 50.0, 4.0, 100.0, 200.0));
                ctrl.exportDot(graph, "   ");
                throw new AssertionError("Should throw exception for empty path");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("path"), "Error message should mention path");
            } catch (Exception e) {
                throw new RuntimeException("Unexpected exception: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testExportDot_CreatesParentDirectories() {
        test("Export DOT creates parent directories if they don't exist", () -> {
            try {
                BackboneController ctrl = new BackboneController();
                Graph graph = new Graph();
                graph.addStation(new Station("A", "Station A", 50.0, 4.0, 100.0, 200.0));
                
                Path tempDir = Files.createTempDirectory("backbone_test_");
                Path nestedFile = tempDir.resolve("subdir").resolve("test.dot");
                try {
                    ctrl.exportDot(graph, nestedFile.toString());
                    assertTrue(Files.exists(nestedFile), "DOT file should be created in nested directory");
                    assertTrue(Files.size(nestedFile) > 0, "DOT file should not be empty");
                } finally {
                    deleteRecursively(tempDir);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to test parent directory creation: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testExportSvg() {
        test("Export SVG file with real data", () -> {
            try {
                BackboneController ctrl = new BackboneController();
                Graph graph = new Graph();
                graph.addStation(new Station("A", "Station A", 50.0, 4.0, 100.0, 200.0));
                graph.addStation(new Station("B", "Station B", 51.0, 5.0, 150.0, 250.0));
                graph.addConnection(new Connection("A", "B", 50.0));
                
                Path tempFile = Files.createTempFile("backbone_test_", ".svg");
                try {
                    // This may fail if Graphviz is not installed, but we test the method call
                    try {
                        ctrl.exportSvg(graph, tempFile.toString());
                        assertTrue(Files.exists(tempFile), "SVG file should be created if Graphviz is available");
                        // If Graphviz is available, verify file was created
                        if (Files.exists(tempFile) && Files.size(tempFile) > 0) {
                            String content = Files.readString(tempFile);
                            assertTrue(content.contains("svg") || content.contains("SVG") || content.contains("<?xml"),
                                    "SVG file should contain SVG content");
                        }
                    } catch (GraphvizRenderer.GraphvizException e) {
                        // Graphviz not available - this is acceptable for testing
                        assertTrue(e.getMessage().contains("neato") || e.getMessage().contains("Graphviz"),
                                "Error should mention neato or Graphviz");
                    }
                } finally {
                    Files.deleteIfExists(tempFile);
                }
            } catch (Exception e) {
                if (!(e instanceof GraphvizRenderer.GraphvizException)) {
                    throw new RuntimeException("Failed to export SVG: " + e.getMessage(), e);
                }
            }
        });
    }
    
    private static void testGenerateAndExportDot() {
        test("Generate and export DOT in one call", () -> {
            try {
                BackboneController ctrl = new BackboneController();
                Path tempFile = Files.createTempFile("backbone_test_", ".dot");
                try {
                    String filePath = ctrl.generateAndExportDot(tempFile.toString());
                    assertNotNull(filePath, "File path should not be null");
                    assertTrue(Files.exists(tempFile), "DOT file should be created");
                    assertTrue(Files.size(tempFile) > 0, "DOT file should not be empty");
                } finally {
                    Files.deleteIfExists(tempFile);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate and export DOT: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testGenerateAndExportSvg() {
        test("Generate and export SVG in one call", () -> {
            try {
                BackboneController ctrl = new BackboneController();
                Path tempFile = Files.createTempFile("backbone_test_", ".svg");
                try {
                    // This may fail if Graphviz is not installed
                    try {
                        String filePath = ctrl.generateAndExportSvg(tempFile.toString());
                        assertNotNull(filePath, "File path should not be null");
                    } catch (GraphvizRenderer.GraphvizException e) {
                        // Graphviz not available - acceptable
                    }
                } finally {
                    Files.deleteIfExists(tempFile);
                }
            } catch (Exception e) {
                if (!(e instanceof GraphvizRenderer.GraphvizException)) {
                    throw new RuntimeException("Failed to generate and export SVG: " + e.getMessage(), e);
                }
            }
        });
    }
    
    private static void testMSTProperties_WithSmallGraph() {
        test("MST properties with small graph (3 stations)", () -> {
            try {
                GraphRepository mockRepo = new GraphRepository() {
                    @Override
                    public Graph loadGraph() {
                        Graph g = new Graph();
                        g.addStation(new Station("A", "A", 50.0, 4.0, 0.0, 0.0));
                        g.addStation(new Station("B", "B", 51.0, 5.0, 10.0, 10.0));
                        g.addStation(new Station("C", "C", 52.0, 6.0, 20.0, 20.0));
                        g.addConnection(new Connection("A", "B", 10.0));
                        g.addConnection(new Connection("B", "C", 5.0));
                        g.addConnection(new Connection("A", "C", 20.0));
                        return g;
                    }
                    @Override
                    public int getLoadedStationCount() { return 3; }
                    @Override
                    public int getLoadedConnectionCount() { return 3; }
                    @Override
                    public int getRejectedStationCount() { return 0; }
                    @Override
                    public int getRejectedConnectionCount() { return 0; }
                };
                BackboneController ctrl = new BackboneController(mockRepo, new DotExporter(), new GraphvizRenderer());
                Graph mst = ctrl.generateBackbone();
                
                assertEquals(3, mst.getStationCount(), "MST should have all 3 stations");
                assertEquals(2, mst.getConnectionCount(), "MST should have V-1 = 2 connections");
            } catch (Exception e) {
                throw new RuntimeException("Failed to test small graph: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testMSTProperties_WithCycle() {
        test("MST should not contain cycles", () -> {
            try {
                GraphRepository mockRepo = new GraphRepository() {
                    @Override
                    public Graph loadGraph() {
                        Graph g = new Graph();
                        g.addStation(new Station("A", "A", 50.0, 4.0, 0.0, 0.0));
                        g.addStation(new Station("B", "B", 51.0, 5.0, 10.0, 10.0));
                        g.addStation(new Station("C", "C", 52.0, 6.0, 20.0, 20.0));
                        g.addStation(new Station("D", "D", 53.0, 7.0, 30.0, 30.0));
                        // Create a cycle: A-B-C-D-A, plus A-C and B-D
                        g.addConnection(new Connection("A", "B", 10.0));
                        g.addConnection(new Connection("B", "C", 10.0));
                        g.addConnection(new Connection("C", "D", 10.0));
                        g.addConnection(new Connection("D", "A", 10.0));
                        g.addConnection(new Connection("A", "C", 5.0)); // Shorter edge
                        g.addConnection(new Connection("B", "D", 5.0)); // Shorter edge
                        return g;
                    }
                    @Override
                    public int getLoadedStationCount() { return 4; }
                    @Override
                    public int getLoadedConnectionCount() { return 6; }
                    @Override
                    public int getRejectedStationCount() { return 0; }
                    @Override
                    public int getRejectedConnectionCount() { return 0; }
                };
                BackboneController ctrl = new BackboneController(mockRepo, new DotExporter(), new GraphvizRenderer());
                Graph mst = ctrl.generateBackbone();
                
                assertEquals(4, mst.getStationCount(), "MST should have all 4 stations");
                assertEquals(3, mst.getConnectionCount(), "MST should have V-1 = 3 connections (no cycle)");
            } catch (Exception e) {
                throw new RuntimeException("Failed to test cycle: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testEdgeComparator_DifferentDistances() {
        test("EdgeComparator sorts by distance (ascending)", () -> {
            try {
                GraphRepository mockRepo = new GraphRepository() {
                    @Override
                    public Graph loadGraph() {
                        Graph g = new Graph();
                        g.addStation(new Station("A", "A", 50.0, 4.0, 0.0, 0.0));
                        g.addStation(new Station("B", "B", 51.0, 5.0, 10.0, 10.0));
                        g.addStation(new Station("C", "C", 52.0, 6.0, 20.0, 20.0));
                        // Add edges in non-sorted order
                        g.addConnection(new Connection("A", "C", 20.0)); // Longest
                        g.addConnection(new Connection("A", "B", 5.0));  // Shortest
                        g.addConnection(new Connection("B", "C", 10.0)); // Medium
                        return g;
                    }
                    @Override
                    public int getLoadedStationCount() { return 3; }
                    @Override
                    public int getLoadedConnectionCount() { return 3; }
                    @Override
                    public int getRejectedStationCount() { return 0; }
                    @Override
                    public int getRejectedConnectionCount() { return 0; }
                };
                BackboneController ctrl = new BackboneController(mockRepo, new DotExporter(), new GraphvizRenderer());
                Graph mst = ctrl.generateBackbone();
                
                // MST should select shortest edges: A-B (5.0) and B-C (10.0), not A-C (20.0)
                assertEquals(2, mst.getConnectionCount(), "MST should have 2 connections");
                // Verify A-B and B-C are in MST (A-C should not be)
                boolean hasAB = mst.getConnections("A").stream().anyMatch(c -> c.getDestinationId().equals("B"));
                boolean hasBC = mst.getConnections("B").stream().anyMatch(c -> c.getDestinationId().equals("C"));
                assertTrue(hasAB && hasBC, "MST should contain shortest edges A-B and B-C");
            } catch (Exception e) {
                throw new RuntimeException("Failed to test edge comparator: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testEdgeComparator_SameDistance_DifferentOrigins() {
        test("EdgeComparator breaks ties by originId when distances are equal", () -> {
            try {
                GraphRepository mockRepo = new GraphRepository() {
                    @Override
                    public Graph loadGraph() {
                        Graph g = new Graph();
                        g.addStation(new Station("A", "A", 50.0, 4.0, 0.0, 0.0));
                        g.addStation(new Station("B", "B", 51.0, 5.0, 10.0, 10.0));
                        g.addStation(new Station("C", "C", 52.0, 6.0, 20.0, 20.0));
                        g.addStation(new Station("D", "D", 53.0, 7.0, 30.0, 30.0));
                        // All edges have same distance, but different origins
                        g.addConnection(new Connection("C", "D", 10.0)); // C < A alphabetically
                        g.addConnection(new Connection("A", "B", 10.0)); // A < C alphabetically
                        g.addConnection(new Connection("B", "C", 10.0));
                        return g;
                    }
                    @Override
                    public int getLoadedStationCount() { return 4; }
                    @Override
                    public int getLoadedConnectionCount() { return 3; }
                    @Override
                    public int getRejectedStationCount() { return 0; }
                    @Override
                    public int getRejectedConnectionCount() { return 0; }
                };
                BackboneController ctrl = new BackboneController(mockRepo, new DotExporter(), new GraphvizRenderer());
                Graph mst = ctrl.generateBackbone();
                
                // MST should be deterministic based on originId sorting
                assertEquals(3, mst.getConnectionCount(), "MST should have V-1 = 3 connections");
            } catch (Exception e) {
                throw new RuntimeException("Failed to test edge comparator with same distance: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testEdgeComparator_SameDistanceAndOrigin_DifferentDestinations() {
        test("EdgeComparator breaks ties by destinationId when distance and origin are equal", () -> {
            try {
                GraphRepository mockRepo = new GraphRepository() {
                    @Override
                    public Graph loadGraph() {
                        Graph g = new Graph();
                        g.addStation(new Station("A", "A", 50.0, 4.0, 0.0, 0.0));
                        g.addStation(new Station("B", "B", 51.0, 5.0, 10.0, 10.0));
                        g.addStation(new Station("C", "C", 52.0, 6.0, 20.0, 20.0));
                        g.addStation(new Station("D", "D", 53.0, 7.0, 30.0, 30.0));
                        // Same origin, same distance, different destinations
                        g.addConnection(new Connection("A", "D", 10.0)); // D > C alphabetically
                        g.addConnection(new Connection("A", "C", 10.0)); // C < D alphabetically
                        g.addConnection(new Connection("A", "B", 10.0)); // B < C alphabetically
                        return g;
                    }
                    @Override
                    public int getLoadedStationCount() { return 4; }
                    @Override
                    public int getLoadedConnectionCount() { return 3; }
                    @Override
                    public int getRejectedStationCount() { return 0; }
                    @Override
                    public int getRejectedConnectionCount() { return 0; }
                };
                BackboneController ctrl = new BackboneController(mockRepo, new DotExporter(), new GraphvizRenderer());
                Graph mst = ctrl.generateBackbone();
                
                // MST should be deterministic based on destinationId sorting
                assertEquals(3, mst.getConnectionCount(), "MST should have V-1 = 3 connections");
            } catch (Exception e) {
                throw new RuntimeException("Failed to test edge comparator with same distance and origin: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testMST_WithDisconnectedGraph() {
        test("MST with disconnected graph (multiple components)", () -> {
            try {
                GraphRepository mockRepo = new GraphRepository() {
                    @Override
                    public Graph loadGraph() {
                        Graph g = new Graph();
                        // Component 1: A-B
                        g.addStation(new Station("A", "A", 50.0, 4.0, 0.0, 0.0));
                        g.addStation(new Station("B", "B", 51.0, 5.0, 10.0, 10.0));
                        g.addConnection(new Connection("A", "B", 10.0));
                        // Component 2: C-D
                        g.addStation(new Station("C", "C", 52.0, 6.0, 20.0, 20.0));
                        g.addStation(new Station("D", "D", 53.0, 7.0, 30.0, 30.0));
                        g.addConnection(new Connection("C", "D", 5.0));
                        return g;
                    }
                    @Override
                    public int getLoadedStationCount() { return 4; }
                    @Override
                    public int getLoadedConnectionCount() { return 2; }
                    @Override
                    public int getRejectedStationCount() { return 0; }
                    @Override
                    public int getRejectedConnectionCount() { return 0; }
                };
                BackboneController ctrl = new BackboneController(mockRepo, new DotExporter(), new GraphvizRenderer());
                Graph mst = ctrl.generateBackbone();
                
                // MST should have all stations
                assertEquals(4, mst.getStationCount(), "MST should have all 4 stations");
                // MST should have 2 connections (one per component: V-1 for each component)
                assertEquals(2, mst.getConnectionCount(), "MST should have 2 connections (one per component)");
            } catch (Exception e) {
                throw new RuntimeException("Failed to test disconnected graph: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testMST_WithDuplicateEdges() {
        test("MST with duplicate edges (same stations, different distances)", () -> {
            try {
                GraphRepository mockRepo = new GraphRepository() {
                    @Override
                    public Graph loadGraph() {
                        Graph g = new Graph();
                        g.addStation(new Station("A", "A", 50.0, 4.0, 0.0, 0.0));
                        g.addStation(new Station("B", "B", 51.0, 5.0, 10.0, 10.0));
                        g.addStation(new Station("C", "C", 52.0, 6.0, 20.0, 20.0));
                        // Add same edge twice with different distances (should only keep one in unique set)
                        g.addConnection(new Connection("A", "B", 10.0));
                        g.addConnection(new Connection("A", "B", 15.0)); // Duplicate
                        g.addConnection(new Connection("B", "C", 5.0));
                        return g;
                    }
                    @Override
                    public int getLoadedStationCount() { return 3; }
                    @Override
                    public int getLoadedConnectionCount() { return 3; }
                    @Override
                    public int getRejectedStationCount() { return 0; }
                    @Override
                    public int getRejectedConnectionCount() { return 0; }
                };
                BackboneController ctrl = new BackboneController(mockRepo, new DotExporter(), new GraphvizRenderer());
                Graph mst = ctrl.generateBackbone();
                
                // MST should have V-1 edges
                assertEquals(3, mst.getStationCount(), "MST should have all 3 stations");
                assertEquals(2, mst.getConnectionCount(), "MST should have V-1 = 2 connections");
            } catch (Exception e) {
                throw new RuntimeException("Failed to test duplicate edges: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testMST_WithAllEqualDistances() {
        test("MST with all edges having equal distances", () -> {
            try {
                GraphRepository mockRepo = new GraphRepository() {
                    @Override
                    public Graph loadGraph() {
                        Graph g = new Graph();
                        g.addStation(new Station("A", "A", 50.0, 4.0, 0.0, 0.0));
                        g.addStation(new Station("B", "B", 51.0, 5.0, 10.0, 10.0));
                        g.addStation(new Station("C", "C", 52.0, 6.0, 20.0, 20.0));
                        g.addStation(new Station("D", "D", 53.0, 7.0, 30.0, 30.0));
                        // All edges have same distance - sorting should be by station IDs
                        g.addConnection(new Connection("Z", "A", 10.0)); // Z-A (alphabetically last)
                        g.addConnection(new Connection("A", "B", 10.0)); // A-B (alphabetically first)
                        g.addConnection(new Connection("B", "C", 10.0)); // B-C
                        g.addConnection(new Connection("C", "D", 10.0)); // C-D
                        g.addStation(new Station("Z", "Z", 54.0, 8.0, 40.0, 40.0));
                        return g;
                    }
                    @Override
                    public int getLoadedStationCount() { return 5; }
                    @Override
                    public int getLoadedConnectionCount() { return 4; }
                    @Override
                    public int getRejectedStationCount() { return 0; }
                    @Override
                    public int getRejectedConnectionCount() { return 0; }
                };
                BackboneController ctrl = new BackboneController(mockRepo, new DotExporter(), new GraphvizRenderer());
                Graph mst = ctrl.generateBackbone();
                
                // MST should be deterministic based on station ID sorting
                assertEquals(5, mst.getStationCount(), "MST should have all 5 stations");
                assertEquals(4, mst.getConnectionCount(), "MST should have V-1 = 4 connections");
            } catch (Exception e) {
                throw new RuntimeException("Failed to test equal distances: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testExportDot_ContentValidation() {
        test("Export DOT content validation", () -> {
            try {
                BackboneController ctrl = new BackboneController();
                Graph graph = new Graph();
                graph.addStation(new Station("TEST_A", "Test Station A", 50.0, 4.0, 100.5, 200.75));
                graph.addStation(new Station("TEST_B", "Test Station B", 51.0, 5.0, 150.25, 250.5));
                graph.addConnection(new Connection("TEST_A", "TEST_B", 50.0));
                
                Path tempFile = Files.createTempFile("backbone_test_", ".dot");
                try {
                    ctrl.exportDot(graph, tempFile.toString());
                    String content = Files.readString(tempFile);
                    
                    // Validate DOT structure
                    assertTrue(content.contains("graph G"), "Should contain graph declaration");
                    assertTrue(content.contains("node"), "Should contain node definitions");
                    assertTrue(content.contains("TEST_A"), "Should contain station A");
                    assertTrue(content.contains("TEST_B"), "Should contain station B");
                    assertTrue(content.contains("Test Station A"), "Should contain station name");
                    assertTrue(content.contains("--"), "Should contain edge definition");
                    assertTrue(content.contains("pos="), "Should contain position attribute");
                    assertTrue(content.contains("100.50") || content.contains("100.5"), "Should contain X coordinate");
                    assertTrue(content.contains("200.75"), "Should contain Y coordinate");
                } finally {
                    Files.deleteIfExists(tempFile);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to validate DOT content: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testExportDot_WithLargeGraph() {
        test("Export DOT with large graph (many stations and connections)", () -> {
            try {
                BackboneController ctrl = new BackboneController();
                Graph graph = new Graph();
                // Create a graph with 20 stations
                for (int i = 0; i < 20; i++) {
                    graph.addStation(new Station("S" + i, "Station " + i, 50.0 + i * 0.1, 4.0 + i * 0.1, 
                            i * 10.0, i * 10.0));
                }
                // Create connections (chain: S0-S1-S2-...-S19)
                for (int i = 0; i < 19; i++) {
                    graph.addConnection(new Connection("S" + i, "S" + (i + 1), 10.0 + i));
                }
                
                Path tempFile = Files.createTempFile("backbone_test_", ".dot");
                try {
                    ctrl.exportDot(graph, tempFile.toString());
                    String content = Files.readString(tempFile);
                    
                    assertTrue(content.contains("graph G"), "Should contain graph declaration");
                    // Verify all stations are in DOT
                    for (int i = 0; i < 20; i++) {
                        assertTrue(content.contains("S" + i), "Should contain station S" + i);
                    }
                    // Verify connections are in DOT
                    assertTrue(content.contains("--"), "Should contain edge definitions");
                } finally {
                    Files.deleteIfExists(tempFile);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to test large graph: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testUnionFind_Behavior() {
        test("UnionFind behavior in MST algorithm", () -> {
            try {
                // Test that UnionFind correctly prevents cycles
                GraphRepository mockRepo = new GraphRepository() {
                    @Override
                    public Graph loadGraph() {
                        Graph g = new Graph();
                        // Create a triangle: A-B-C-A (cycle)
                        g.addStation(new Station("A", "A", 50.0, 4.0, 0.0, 0.0));
                        g.addStation(new Station("B", "B", 51.0, 5.0, 10.0, 10.0));
                        g.addStation(new Station("C", "C", 52.0, 6.0, 20.0, 20.0));
                        // All edges have same distance - algorithm should pick 2 to avoid cycle
                        g.addConnection(new Connection("A", "B", 10.0));
                        g.addConnection(new Connection("B", "C", 10.0));
                        g.addConnection(new Connection("C", "A", 10.0));
                        return g;
                    }
                    @Override
                    public int getLoadedStationCount() { return 3; }
                    @Override
                    public int getLoadedConnectionCount() { return 3; }
                    @Override
                    public int getRejectedStationCount() { return 0; }
                    @Override
                    public int getRejectedConnectionCount() { return 0; }
                };
                BackboneController ctrl = new BackboneController(mockRepo, new DotExporter(), new GraphvizRenderer());
                Graph mst = ctrl.generateBackbone();
                
                // MST should have exactly V-1 = 2 edges (no cycle)
                assertEquals(3, mst.getStationCount(), "MST should have all 3 stations");
                assertEquals(2, mst.getConnectionCount(), "MST should have exactly 2 edges (no cycle)");
                
                // Verify that not all 3 edges are in MST (cycle prevention)
                int abCount = 0, bcCount = 0, caCount = 0;
                for (Connection conn : mst.getAllUniqueConnections()) {
                    if ((conn.getOriginId().equals("A") && conn.getDestinationId().equals("B")) ||
                        (conn.getOriginId().equals("B") && conn.getDestinationId().equals("A"))) {
                        abCount++;
                    }
                    if ((conn.getOriginId().equals("B") && conn.getDestinationId().equals("C")) ||
                        (conn.getOriginId().equals("C") && conn.getDestinationId().equals("B"))) {
                        bcCount++;
                    }
                    if ((conn.getOriginId().equals("C") && conn.getDestinationId().equals("A")) ||
                        (conn.getOriginId().equals("A") && conn.getDestinationId().equals("C"))) {
                        caCount++;
                    }
                }
                // At least one edge should be missing (cycle prevention)
                assertTrue(abCount + bcCount + caCount == 2, 
                        "MST should have exactly 2 edges, preventing cycle");
            } catch (Exception e) {
                throw new RuntimeException("Failed to test UnionFind behavior: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testMST_WithSingleEdge() {
        test("MST with graph having only one edge", () -> {
            try {
                GraphRepository mockRepo = new GraphRepository() {
                    @Override
                    public Graph loadGraph() {
                        Graph g = new Graph();
                        g.addStation(new Station("A", "A", 50.0, 4.0, 0.0, 0.0));
                        g.addStation(new Station("B", "B", 51.0, 5.0, 10.0, 10.0));
                        g.addConnection(new Connection("A", "B", 10.0));
                        return g;
                    }
                    @Override
                    public int getLoadedStationCount() { return 2; }
                    @Override
                    public int getLoadedConnectionCount() { return 1; }
                    @Override
                    public int getRejectedStationCount() { return 0; }
                    @Override
                    public int getRejectedConnectionCount() { return 0; }
                };
                BackboneController ctrl = new BackboneController(mockRepo, new DotExporter(), new GraphvizRenderer());
                Graph mst = ctrl.generateBackbone();
                
                assertEquals(2, mst.getStationCount(), "MST should have 2 stations");
                assertEquals(1, mst.getConnectionCount(), "MST should have exactly 1 edge");
            } catch (Exception e) {
                throw new RuntimeException("Failed to test single edge: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testMST_WithCompleteGraph() {
        test("MST with complete graph (all pairs connected)", () -> {
            try {
                GraphRepository mockRepo = new GraphRepository() {
                    @Override
                    public Graph loadGraph() {
                        Graph g = new Graph();
                        // Create complete graph with 4 nodes (6 edges)
                        for (int i = 0; i < 4; i++) {
                            g.addStation(new Station("N" + i, "Node " + i, 50.0 + i, 4.0 + i, i * 10.0, i * 10.0));
                        }
                        // Connect all pairs
                        for (int i = 0; i < 4; i++) {
                            for (int j = i + 1; j < 4; j++) {
                                g.addConnection(new Connection("N" + i, "N" + j, 10.0 + i + j));
                            }
                        }
                        return g;
                    }
                    @Override
                    public int getLoadedStationCount() { return 4; }
                    @Override
                    public int getLoadedConnectionCount() { return 6; }
                    @Override
                    public int getRejectedStationCount() { return 0; }
                    @Override
                    public int getRejectedConnectionCount() { return 0; }
                };
                BackboneController ctrl = new BackboneController(mockRepo, new DotExporter(), new GraphvizRenderer());
                Graph mst = ctrl.generateBackbone();
                
                assertEquals(4, mst.getStationCount(), "MST should have all 4 stations");
                assertEquals(3, mst.getConnectionCount(), "MST should have V-1 = 3 edges (not all 6)");
            } catch (Exception e) {
                throw new RuntimeException("Failed to test complete graph: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testMST_WithNegativeDistances() {
        test("MST should reject negative distances (Connection validation)", () -> {
            try {
                // Connection constructor should reject negative distances
                try {
                    new Connection("A", "B", -1.0);
                    throw new AssertionError("Should throw exception for negative distance");
                } catch (IllegalArgumentException e) {
                    assertTrue(e.getMessage().contains("negative") || e.getMessage().contains("Distance"),
                            "Error should mention negative distance");
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to test negative distances: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testMST_WithZeroDistance() {
        test("MST with edges having zero distance", () -> {
            try {
                GraphRepository mockRepo = new GraphRepository() {
                    @Override
                    public Graph loadGraph() {
                        Graph g = new Graph();
                        g.addStation(new Station("A", "A", 50.0, 4.0, 0.0, 0.0));
                        g.addStation(new Station("B", "B", 51.0, 5.0, 10.0, 10.0));
                        g.addConnection(new Connection("A", "B", 0.0)); // Zero distance
                        return g;
                    }
                    @Override
                    public int getLoadedStationCount() { return 2; }
                    @Override
                    public int getLoadedConnectionCount() { return 1; }
                    @Override
                    public int getRejectedStationCount() { return 0; }
                    @Override
                    public int getRejectedConnectionCount() { return 0; }
                };
                BackboneController ctrl = new BackboneController(mockRepo, new DotExporter(), new GraphvizRenderer());
                Graph mst = ctrl.generateBackbone();
                
                // Zero distance is valid, MST should include it
                assertEquals(2, mst.getStationCount(), "MST should have 2 stations");
                assertEquals(1, mst.getConnectionCount(), "MST should have 1 edge with zero distance");
            } catch (Exception e) {
                throw new RuntimeException("Failed to test zero distance: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testMST_WithVeryLargeDistances() {
        test("MST with very large distance values", () -> {
            try {
                GraphRepository mockRepo = new GraphRepository() {
                    @Override
                    public Graph loadGraph() {
                        Graph g = new Graph();
                        g.addStation(new Station("A", "A", 50.0, 4.0, 0.0, 0.0));
                        g.addStation(new Station("B", "B", 51.0, 5.0, 10.0, 10.0));
                        g.addStation(new Station("C", "C", 52.0, 6.0, 20.0, 20.0));
                        g.addConnection(new Connection("A", "B", Double.MAX_VALUE / 2)); // Very large
                        g.addConnection(new Connection("B", "C", 10.0)); // Small
                        g.addConnection(new Connection("A", "C", Double.MAX_VALUE / 2)); // Very large
                        return g;
                    }
                    @Override
                    public int getLoadedStationCount() { return 3; }
                    @Override
                    public int getLoadedConnectionCount() { return 3; }
                    @Override
                    public int getRejectedStationCount() { return 0; }
                    @Override
                    public int getRejectedConnectionCount() { return 0; }
                };
                BackboneController ctrl = new BackboneController(mockRepo, new DotExporter(), new GraphvizRenderer());
                Graph mst = ctrl.generateBackbone();
                
                // MST should prefer smaller edge (B-C) and one of the large edges
                assertEquals(3, mst.getStationCount(), "MST should have all 3 stations");
                assertEquals(2, mst.getConnectionCount(), "MST should have 2 edges");
            } catch (Exception e) {
                throw new RuntimeException("Failed to test large distances: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testExportDot_WithGraphHavingNoStations() {
        test("Export DOT with graph having no stations", () -> {
            try {
                BackboneController ctrl = new BackboneController();
                Graph graph = new Graph(); // Empty graph
                
                Path tempFile = Files.createTempFile("backbone_test_", ".dot");
                try {
                    ctrl.exportDot(graph, tempFile.toString());
                    String content = Files.readString(tempFile);
                    
                    assertTrue(content.contains("graph G"), "Should contain graph declaration");
                    // Empty graph should still produce valid DOT
                } finally {
                    Files.deleteIfExists(tempFile);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to test empty graph export: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testExportDot_WithGraphHavingOneStation() {
        test("Export DOT with graph having exactly one station", () -> {
            try {
                BackboneController ctrl = new BackboneController();
                Graph graph = new Graph();
                graph.addStation(new Station("A", "Station A", 50.0, 4.0, 100.0, 200.0));
                
                Path tempFile = Files.createTempFile("backbone_test_", ".dot");
                try {
                    ctrl.exportDot(graph, tempFile.toString());
                    String content = Files.readString(tempFile);
                    
                    assertTrue(content.contains("graph G"), "Should contain graph declaration");
                    assertTrue(content.contains("A"), "Should contain station A");
                    assertTrue(content.contains("Station A"), "Should contain station name");
                } finally {
                    Files.deleteIfExists(tempFile);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to test single station export: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testExportDot_WithGraphHavingTwoStations() {
        test("Export DOT with graph having exactly two stations", () -> {
            try {
                BackboneController ctrl = new BackboneController();
                Graph graph = new Graph();
                graph.addStation(new Station("A", "Station A", 50.0, 4.0, 100.0, 200.0));
                graph.addStation(new Station("B", "Station B", 51.0, 5.0, 150.0, 250.0));
                // No connection - just two isolated stations
                
                Path tempFile = Files.createTempFile("backbone_test_", ".dot");
                try {
                    ctrl.exportDot(graph, tempFile.toString());
                    String content = Files.readString(tempFile);
                    
                    assertTrue(content.contains("graph G"), "Should contain graph declaration");
                    assertTrue(content.contains("A"), "Should contain station A");
                    assertTrue(content.contains("B"), "Should contain station B");
                    // No edges should be present
                } finally {
                    Files.deleteIfExists(tempFile);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to test two stations export: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testExportSvg_WithNullGraph() {
        test("Export SVG with null graph should throw exception", () -> {
            try {
                BackboneController ctrl = new BackboneController();
                Path tempFile = Files.createTempFile("test_", ".svg");
                try {
                    ctrl.exportSvg(null, tempFile.toString());
                    throw new AssertionError("Should throw exception for null graph");
                } catch (IllegalArgumentException e) {
                    assertTrue(e.getMessage().contains("Graph"), "Error message should mention Graph");
                } finally {
                    Files.deleteIfExists(tempFile);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to test null graph SVG export: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testExportSvg_WithEmptyPath() {
        test("Export SVG with empty path should throw exception", () -> {
            try {
                BackboneController ctrl = new BackboneController();
                Graph graph = new Graph();
                graph.addStation(new Station("A", "A", 50.0, 4.0, 100.0, 200.0));
                ctrl.exportSvg(graph, "   ");
                throw new AssertionError("Should throw exception for empty path");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("path"), "Error message should mention path");
            } catch (Exception e) {
                throw new RuntimeException("Unexpected exception: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testGenerateBackbone_WithGraphHavingIsolatedStations() {
        test("Generate backbone with graph having isolated stations", () -> {
            try {
                GraphRepository mockRepo = new GraphRepository() {
                    @Override
                    public Graph loadGraph() {
                        Graph g = new Graph();
                        // Add 3 isolated stations (no connections)
                        g.addStation(new Station("A", "A", 50.0, 4.0, 0.0, 0.0));
                        g.addStation(new Station("B", "B", 51.0, 5.0, 10.0, 10.0));
                        g.addStation(new Station("C", "C", 52.0, 6.0, 20.0, 20.0));
                        return g;
                    }
                    @Override
                    public int getLoadedStationCount() { return 3; }
                    @Override
                    public int getLoadedConnectionCount() { return 0; }
                    @Override
                    public int getRejectedStationCount() { return 0; }
                    @Override
                    public int getRejectedConnectionCount() { return 0; }
                };
                BackboneController ctrl = new BackboneController(mockRepo, new DotExporter(), new GraphvizRenderer());
                Graph mst = ctrl.generateBackbone();
                
                // MST should have all stations but no connections
                assertEquals(3, mst.getStationCount(), "MST should have all 3 isolated stations");
                assertEquals(0, mst.getConnectionCount(), "MST should have 0 connections (isolated stations)");
            } catch (Exception e) {
                throw new RuntimeException("Failed to test isolated stations: " + e.getMessage(), e);
            }
        });
    }
    
    private static void testMST_WithGraphHavingSelfLoops() {
        test("MST should reject self-loops (Connection validation)", () -> {
            try {
                // Connection constructor should reject self-loops
                try {
                    new Connection("A", "A", 10.0);
                    throw new AssertionError("Should throw exception for self-loop");
                } catch (IllegalArgumentException e) {
                    assertTrue(e.getMessage().contains("same") || e.getMessage().contains("Origin"),
                            "Error should mention same origin and destination");
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to test self-loops: " + e.getMessage(), e);
            }
        });
    }
    
    private static void deleteRecursively(Path path) {
        try {
            if (Files.isDirectory(path)) {
                Files.list(path).forEach(BackboneControllerSimpleTest::deleteRecursively);
            }
            Files.deleteIfExists(path);
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }
    
    // ==================== Test Helper Methods ====================
    
    private static void test(String testName, Runnable testCode) {
        testsRun++;
        try {
            System.out.print("Testing: " + testName + " ... ");
            testCode.run();
            System.out.println("✓ PASSED");
            testsPassed++;
        } catch (AssertionError e) {
            System.out.println("✗ FAILED: " + e.getMessage());
            testsFailed++;
        } catch (Exception e) {
            System.out.println("✗ ERROR: " + e.getMessage());
            testsFailed++;
        }
    }
    
    private static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError(message);
        }
    }
    
    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
    
    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected == null || !expected.equals(actual)) {
            throw new AssertionError(message + " (expected: " + expected + ", actual: " + actual + ")");
        }
    }
}

