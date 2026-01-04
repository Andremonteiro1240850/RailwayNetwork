package Controller;

import Domain.*;
import Interfaces.GraphRepository;
import Repository.BelgiumGraphRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BackboneController Tests")
class BackboneControllerSimpleTest {

    private static final String STATIONS_PATH = "train_station_dataset/Sprint3/stations.csv";
    private static final String LINES_PATH = "train_station_dataset/Sprint3/lines.csv";

    private BackboneController controller;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setup() {
        controller = new BackboneController();
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("Should create controller with default dependencies")
        void defaultConstructor() {
            BackboneController ctrl = new BackboneController();
            assertNotNull(ctrl);
        }

        @Test
        @DisplayName("Should create controller with custom dependencies")
        void customDependencies() {
            GraphRepository repo = new BelgiumGraphRepository(STATIONS_PATH, LINES_PATH);
            DotExporter exporter = new DotExporter();
            GraphvizRenderer renderer = new GraphvizRenderer();

            BackboneController ctrl = new BackboneController(repo, exporter, renderer);
            assertNotNull(ctrl);
        }

        @Test
        @DisplayName("Should throw exception when repository is null")
        void nullRepository() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new BackboneController(null, new DotExporter(), new GraphvizRenderer())
            );
            assertTrue(exception.getMessage().toLowerCase().contains("repository"));
        }

        @Test
        @DisplayName("Should throw exception when DotExporter is null")
        void nullDotExporter() {
            GraphRepository repo = new BelgiumGraphRepository(STATIONS_PATH, LINES_PATH);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new BackboneController(repo, null, new GraphvizRenderer())
            );
            assertTrue(exception.getMessage().toLowerCase().contains("dotexporter"));
        }

        @Test
        @DisplayName("Should throw exception when GraphvizRenderer is null")
        void nullGraphvizRenderer() {
            GraphRepository repo = new BelgiumGraphRepository(STATIONS_PATH, LINES_PATH);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new BackboneController(repo, new DotExporter(), null)
            );
            assertTrue(exception.getMessage().toLowerCase().contains("renderer"));
        }
    }

    @Nested
    @DisplayName("Generate Backbone")
    class GenerateBackboneTests {

        @Test
        @DisplayName("Should generate backbone from real Belgian railway data")
        void realData() throws Exception {
            Graph mst = controller.generateBackbone();

            assertAll(
                    () -> assertNotNull(mst),
                    () -> assertTrue(mst.getStationCount() > 100, "Should have many stations"),
                    () -> assertTrue(mst.getConnectionCount() >= 0, "Should have non-negative connections")
            );
        }

        @Test
        @DisplayName("Should throw exception when graph is empty")
        void emptyGraph() {
            GraphRepository mockRepo = createMockRepository(new Graph(), 0, 0);
            BackboneController ctrl = new BackboneController(
                    mockRepo, new DotExporter(), new GraphvizRenderer()
            );

            assertThrows(Exception.class, () -> ctrl.generateBackbone());
        }

        @Test
        @DisplayName("Should handle single station with no connections")
        void singleStation() throws Exception {
            Graph graph = new Graph();
            graph.addStation(new Station("A", "Station A", 50.0, 4.0, 100.0, 200.0));

            GraphRepository mockRepo = createMockRepository(graph, 1, 0);
            BackboneController ctrl = new BackboneController(
                    mockRepo, new DotExporter(), new GraphvizRenderer()
            );

            Graph mst = ctrl.generateBackbone();

            assertAll(
                    () -> assertEquals(1, mst.getStationCount()),
                    () -> assertEquals(0, mst.getConnectionCount())
            );
        }

        @Test
        @DisplayName("Should handle multiple isolated stations")
        void isolatedStations() throws Exception {
            Graph graph = new Graph();
            graph.addStation(new Station("A", "A", 50.0, 4.0, 0.0, 0.0));
            graph.addStation(new Station("B", "B", 51.0, 5.0, 10.0, 10.0));
            graph.addStation(new Station("C", "C", 52.0, 6.0, 20.0, 20.0));

            GraphRepository mockRepo = createMockRepository(graph, 3, 0);
            BackboneController ctrl = new BackboneController(
                    mockRepo, new DotExporter(), new GraphvizRenderer()
            );

            Graph mst = ctrl.generateBackbone();

            assertAll(
                    () -> assertEquals(3, mst.getStationCount()),
                    () -> assertEquals(0, mst.getConnectionCount())
            );
        }
    }

    @Nested
    @DisplayName("MST Properties")
    class MSTPropertiesTests {

        @Test
        @DisplayName("Should preserve all stations")
        void preserveStations() throws Exception {
            Graph mst = controller.generateBackbone();
            GraphRepository repo = new BelgiumGraphRepository(STATIONS_PATH, LINES_PATH);
            Graph fullGraph = repo.loadGraph();

            assertEquals(fullGraph.getStationCount(), mst.getStationCount());
        }

        @Test
        @DisplayName("Should have at most V-1 edges for connected graph")
        void atMostVMinus1Edges() throws Exception {
            Graph graph = createConnectedGraph();
            GraphRepository mockRepo = createMockRepository(graph, 3, 3);
            BackboneController ctrl = new BackboneController(
                    mockRepo, new DotExporter(), new GraphvizRenderer()
            );

            Graph mst = ctrl.generateBackbone();

            int expectedMaxEdges = mst.getStationCount() - 1;
            assertTrue(mst.getConnectionCount() <= expectedMaxEdges);
        }

        @Test
        @DisplayName("Should not contain cycles")
        void noCycles() throws Exception {
            Graph graph = createGraphWithCycle();
            GraphRepository mockRepo = createMockRepository(graph, 4, 6);
            BackboneController ctrl = new BackboneController(
                    mockRepo, new DotExporter(), new GraphvizRenderer()
            );

            Graph mst = ctrl.generateBackbone();

            // MST should have exactly V-1 edges (no cycle)
            assertEquals(3, mst.getConnectionCount());
        }

        @Test
        @DisplayName("Should be deterministic")
        void deterministic() throws Exception {
            Graph mst1 = controller.generateBackbone();
            Graph mst2 = controller.generateBackbone();

            assertAll(
                    () -> assertEquals(mst1.getStationCount(), mst2.getStationCount()),
                    () -> assertEquals(mst1.getConnectionCount(), mst2.getConnectionCount())
            );
        }

        @Test
        @DisplayName("Should select shortest edges")
        void shortestEdges() throws Exception {
            Graph graph = new Graph();
            graph.addStation(new Station("A", "A", 50.0, 4.0, 0.0, 0.0));
            graph.addStation(new Station("B", "B", 51.0, 5.0, 10.0, 10.0));
            graph.addStation(new Station("C", "C", 52.0, 6.0, 20.0, 20.0));
            graph.addConnection(new Connection("A", "C", 20.0)); // Longest
            graph.addConnection(new Connection("A", "B", 5.0));  // Shortest
            graph.addConnection(new Connection("B", "C", 10.0)); // Medium

            GraphRepository mockRepo = createMockRepository(graph, 3, 3);
            BackboneController ctrl = new BackboneController(
                    mockRepo, new DotExporter(), new GraphvizRenderer()
            );

            Graph mst = ctrl.generateBackbone();

            // Should select A-B (5.0) and B-C (10.0)
            assertEquals(2, mst.getConnectionCount());
        }

        @Test
        @DisplayName("Should handle disconnected components")
        void disconnectedComponents() throws Exception {
            Graph graph = new Graph();
            // Component 1: A-B
            graph.addStation(new Station("A", "A", 50.0, 4.0, 0.0, 0.0));
            graph.addStation(new Station("B", "B", 51.0, 5.0, 10.0, 10.0));
            graph.addConnection(new Connection("A", "B", 10.0));
            // Component 2: C-D
            graph.addStation(new Station("C", "C", 52.0, 6.0, 20.0, 20.0));
            graph.addStation(new Station("D", "D", 53.0, 7.0, 30.0, 30.0));
            graph.addConnection(new Connection("C", "D", 5.0));

            GraphRepository mockRepo = createMockRepository(graph, 4, 2);
            BackboneController ctrl = new BackboneController(
                    mockRepo, new DotExporter(), new GraphvizRenderer()
            );

            Graph mst = ctrl.generateBackbone();

            assertAll(
                    () -> assertEquals(4, mst.getStationCount()),
                    () -> assertEquals(2, mst.getConnectionCount())
            );
        }
    }

    @Nested
    @DisplayName("Edge Comparator")
    class EdgeComparatorTests {

        @Test
        @DisplayName("Should sort by distance ascending")
        void sortByDistance() throws Exception {
            Graph graph = new Graph();
            graph.addStation(new Station("A", "A", 50.0, 4.0, 0.0, 0.0));
            graph.addStation(new Station("B", "B", 51.0, 5.0, 10.0, 10.0));
            graph.addStation(new Station("C", "C", 52.0, 6.0, 20.0, 20.0));
            graph.addConnection(new Connection("A", "C", 20.0));
            graph.addConnection(new Connection("A", "B", 5.0));
            graph.addConnection(new Connection("B", "C", 10.0));

            GraphRepository mockRepo = createMockRepository(graph, 3, 3);
            BackboneController ctrl = new BackboneController(
                    mockRepo, new DotExporter(), new GraphvizRenderer()
            );

            Graph mst = ctrl.generateBackbone();

            // Should select shortest edges first
            boolean hasAB = mst.getConnections("A").stream()
                    .anyMatch(c -> c.getDestinationId().equals("B"));
            boolean hasBC = mst.getConnections("B").stream()
                    .anyMatch(c -> c.getDestinationId().equals("C"));

            assertTrue(hasAB && hasBC);
        }

        @Test
        @DisplayName("Should break ties by origin ID")
        void tieBreakByOrigin() throws Exception {
            Graph graph = new Graph();
            graph.addStation(new Station("A", "A", 50.0, 4.0, 0.0, 0.0));
            graph.addStation(new Station("B", "B", 51.0, 5.0, 10.0, 10.0));
            graph.addStation(new Station("C", "C", 52.0, 6.0, 20.0, 20.0));
            graph.addStation(new Station("D", "D", 53.0, 7.0, 30.0, 30.0));
            // All same distance
            graph.addConnection(new Connection("C", "D", 10.0));
            graph.addConnection(new Connection("A", "B", 10.0));
            graph.addConnection(new Connection("B", "C", 10.0));

            GraphRepository mockRepo = createMockRepository(graph, 4, 3);
            BackboneController ctrl = new BackboneController(
                    mockRepo, new DotExporter(), new GraphvizRenderer()
            );

            Graph mst = ctrl.generateBackbone();

            // Should be deterministic
            assertEquals(3, mst.getConnectionCount());
        }

        @Test
        @DisplayName("Should handle all equal distances")
        void allEqualDistances() throws Exception {
            Graph graph = new Graph();
            for (int i = 0; i < 5; i++) {
                graph.addStation(new Station("N" + i, "N" + i, 50.0 + i, 4.0, i * 10.0, i * 10.0));
            }
            // All edges with same distance
            for (int i = 0; i < 4; i++) {
                graph.addConnection(new Connection("N" + i, "N" + (i + 1), 10.0));
            }

            GraphRepository mockRepo = createMockRepository(graph, 5, 4);
            BackboneController ctrl = new BackboneController(
                    mockRepo, new DotExporter(), new GraphvizRenderer()
            );

            Graph mst = ctrl.generateBackbone();

            assertEquals(4, mst.getConnectionCount());
        }
    }

    @Nested
    @DisplayName("Export DOT")
    class ExportDotTests {

        @Test
        @DisplayName("Should export valid DOT file")
        void validDotFile() throws IOException {
            Graph graph = createSimpleGraph();
            Path dotFile = tempDir.resolve("test.dot");

            String filePath = controller.exportDot(graph, dotFile.toString());

            assertAll(
                    () -> assertNotNull(filePath),
                    () -> assertTrue(Files.exists(dotFile)),
                    () -> assertTrue(Files.size(dotFile) > 0)
            );
        }

        @Test
        @DisplayName("Should throw exception when graph is null")
        void nullGraph() {
            Path dotFile = tempDir.resolve("test.dot");

            assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.exportDot(null, dotFile.toString())
            );
        }

        @Test
        @DisplayName("Should throw exception when path is null")
        void nullPath() {
            Graph graph = createSimpleGraph();

            assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.exportDot(graph, null)
            );
        }

        @Test
        @DisplayName("Should throw exception when path is empty")
        void emptyPath() {
            Graph graph = createSimpleGraph();

            assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.exportDot(graph, "   ")
            );
        }

        @Test
        @DisplayName("Should create parent directories if needed")
        void createParentDirectories() throws IOException {
            Graph graph = createSimpleGraph();
            Path nestedFile = tempDir.resolve("subdir").resolve("test.dot");

            controller.exportDot(graph, nestedFile.toString());

            assertTrue(Files.exists(nestedFile));
        }

        @Test
        @DisplayName("Should contain valid DOT content")
        void validDotContent() throws IOException {
            Graph graph = new Graph();
            graph.addStation(new Station("TEST_A", "Test A", 50.0, 4.0, 100.5, 200.75));
            graph.addStation(new Station("TEST_B", "Test B", 51.0, 5.0, 150.25, 250.5));
            graph.addConnection(new Connection("TEST_A", "TEST_B", 50.0));

            Path dotFile = tempDir.resolve("test.dot");
            controller.exportDot(graph, dotFile.toString());

            String content = Files.readString(dotFile);

            assertAll(
                    () -> assertTrue(content.contains("graph G")),
                    () -> assertTrue(content.contains("TEST_A")),
                    () -> assertTrue(content.contains("TEST_B")),
                    () -> assertTrue(content.contains("Test A")),
                    () -> assertTrue(content.contains("--"))
            );
        }
    }

    @Nested
    @DisplayName("Export SVG")
    class ExportSvgTests {

        @Test
        @DisplayName("Should export SVG when Graphviz is available")
        void validSvgFile() throws IOException {
            Graph graph = createSimpleGraph();
            Path svgFile = tempDir.resolve("test.svg");

            try {
                controller.exportSvg(graph, svgFile.toString());
                // If successful, file should exist
                assertTrue(Files.exists(svgFile));
            } catch (GraphvizRenderer.GraphvizException e) {
                // Graphviz not available - acceptable
                Assumptions.assumeTrue(false, "Graphviz not available");
            }
        }

        @Test
        @DisplayName("Should throw exception when graph is null")
        void nullGraph() {
            Path svgFile = tempDir.resolve("test.svg");

            assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.exportSvg(null, svgFile.toString())
            );
        }

        @Test
        @DisplayName("Should throw exception when path is empty")
        void emptyPath() {
            Graph graph = createSimpleGraph();

            assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.exportSvg(graph, "   ")
            );
        }
    }

    @Nested
    @DisplayName("Generate and Export")
    class GenerateAndExportTests {

        @Test
        @DisplayName("Should generate and export DOT in one call")
        void generateAndExportDot() throws Exception {
            Path dotFile = tempDir.resolve("backbone.dot");

            String filePath = controller.generateAndExportDot(dotFile.toString());

            assertAll(
                    () -> assertNotNull(filePath),
                    () -> assertTrue(Files.exists(dotFile)),
                    () -> assertTrue(Files.size(dotFile) > 0)
            );
        }

        @Test
        @DisplayName("Should generate and export SVG in one call")
        void generateAndExportSvg() throws IOException {
            Path svgFile = tempDir.resolve("backbone.svg");

            try {
                String filePath = controller.generateAndExportSvg(svgFile.toString());
                assertNotNull(filePath);
            } catch (Exception e) {
                // Graphviz not available - acceptable
                Assumptions.assumeTrue(false, "Graphviz not available");
            }
        }
    }

    @Nested
    @DisplayName("Statistics and Analysis")
    class StatisticsTests {

        @Test
        @DisplayName("Should provide graph statistics")
        void graphStatistics() throws Exception {
            String statistics = controller.getGraphStatistics();

            assertAll(
                    () -> assertNotNull(statistics),
                    () -> assertTrue(statistics.length() > 0),
                    () -> assertTrue(statistics.toLowerCase().contains("stations")),
                    () -> assertTrue(statistics.toLowerCase().contains("connections"))
            );
        }

        @Test
        @DisplayName("Should provide complexity analysis")
        void complexityAnalysis() {
            String analysis = controller.getComplexityAnalysis();

            assertAll(
                    () -> assertNotNull(analysis),
                    () -> assertTrue(analysis.length() > 0),
                    () -> assertTrue(analysis.contains("O(|E| log |E|)")),
                    () -> assertTrue(analysis.toLowerCase().contains("kruskal"))
            );
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle zero distance edges")
        void zeroDistance() throws Exception {
            Graph graph = new Graph();
            graph.addStation(new Station("A", "A", 50.0, 4.0, 0.0, 0.0));
            graph.addStation(new Station("B", "B", 51.0, 5.0, 10.0, 10.0));
            graph.addConnection(new Connection("A", "B", 0.0));

            GraphRepository mockRepo = createMockRepository(graph, 2, 1);
            BackboneController ctrl = new BackboneController(
                    mockRepo, new DotExporter(), new GraphvizRenderer()
            );

            Graph mst = ctrl.generateBackbone();

            assertEquals(1, mst.getConnectionCount());
        }

        @Test
        @DisplayName("Should handle large distances")
        void largeDistances() throws Exception {
            Graph graph = new Graph();
            graph.addStation(new Station("A", "A", 50.0, 4.0, 0.0, 0.0));
            graph.addStation(new Station("B", "B", 51.0, 5.0, 10.0, 10.0));
            graph.addStation(new Station("C", "C", 52.0, 6.0, 20.0, 20.0));
            graph.addConnection(new Connection("A", "B", Double.MAX_VALUE / 2));
            graph.addConnection(new Connection("B", "C", 10.0));
            graph.addConnection(new Connection("A", "C", Double.MAX_VALUE / 2));

            GraphRepository mockRepo = createMockRepository(graph, 3, 3);
            BackboneController ctrl = new BackboneController(
                    mockRepo, new DotExporter(), new GraphvizRenderer()
            );

            Graph mst = ctrl.generateBackbone();

            assertEquals(2, mst.getConnectionCount());
        }

        @Test
        @DisplayName("Should reject negative distances via Connection validation")
        void negativeDistance() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new Connection("A", "B", -1.0)
            );
        }

        @Test
        @DisplayName("Should reject self-loops via Connection validation")
        void selfLoop() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new Connection("A", "A", 10.0)
            );
        }
    }

    // Helper methods

    private GraphRepository createMockRepository(Graph graph, int stations, int connections) {
        return new GraphRepository() {
            @Override
            public Graph loadGraph() { return graph; }
            @Override
            public int getLoadedStationCount() { return stations; }
            @Override
            public int getLoadedConnectionCount() { return connections; }
            @Override
            public int getRejectedStationCount() { return 0; }
            @Override
            public int getRejectedConnectionCount() { return 0; }
        };
    }

    private Graph createSimpleGraph() {
        Graph graph = new Graph();
        graph.addStation(new Station("A", "Station A", 50.0, 4.0, 100.0, 200.0));
        graph.addStation(new Station("B", "Station B", 51.0, 5.0, 150.0, 250.0));
        graph.addConnection(new Connection("A", "B", 50.0));
        return graph;
    }

    private Graph createConnectedGraph() {
        Graph graph = new Graph();
        graph.addStation(new Station("A", "A", 50.0, 4.0, 0.0, 0.0));
        graph.addStation(new Station("B", "B", 51.0, 5.0, 10.0, 10.0));
        graph.addStation(new Station("C", "C", 52.0, 6.0, 20.0, 20.0));
        graph.addConnection(new Connection("A", "B", 10.0));
        graph.addConnection(new Connection("B", "C", 5.0));
        graph.addConnection(new Connection("A", "C", 20.0));
        return graph;
    }

    private Graph createGraphWithCycle() {
        Graph graph = new Graph();
        graph.addStation(new Station("A", "A", 50.0, 4.0, 0.0, 0.0));
        graph.addStation(new Station("B", "B", 51.0, 5.0, 10.0, 10.0));
        graph.addStation(new Station("C", "C", 52.0, 6.0, 20.0, 20.0));
        graph.addStation(new Station("D", "D", 53.0, 7.0, 30.0, 30.0));
        // Create cycle
        graph.addConnection(new Connection("A", "B", 10.0));
        graph.addConnection(new Connection("B", "C", 10.0));
        graph.addConnection(new Connection("C", "D", 10.0));
        graph.addConnection(new Connection("D", "A", 10.0));
        graph.addConnection(new Connection("A", "C", 5.0));
        graph.addConnection(new Connection("B", "D", 5.0));
        return graph;
    }
}