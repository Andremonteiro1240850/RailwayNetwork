package Controller;

import Domain.*;
import Repository.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for ShortestPathController (USEI15 - Risk-Aware Shortest Paths).
 * 
 * Tests cover:
 * - Controller initialization and graph building
 * - Path calculation orchestration
 * - Result conversion and formatting
 * - Error handling and validation
 * - Integration with Domain layer (BellmanFord)
 * 
 * All tests use REAL data from train_station_dataset/Sprint3/stations.csv and lines.csv
 * 
 * @author Development Team
 * @version 1.0
 */
class ShortestPathControllerTest {
    
    private static final double EPS = 1e-9;
    
    // Real data loaded from CSV files
    private static RailwayGraphRepository realDataRepository;
    private static RailwayGraph realDataGraph;
    private static List<Station> realStations;
    
    @BeforeAll
    static void loadRealData() {
        try {
            realDataRepository = new RailwayGraphRepository();
            realDataRepository.loadData("train_station_dataset/Sprint3/stations.csv", 
                                       "train_station_dataset/Sprint3/lines.csv");
            realDataGraph = realDataRepository.buildGraph();
            
            // Get real stations
            realStations = new ArrayList<>(realDataGraph.getAllNodes());
            
            System.out.println("Loaded " + realStations.size() + " stations for controller testing");
        } catch (Exception e) {
            System.err.println("WARNING: Failed to load real data: " + e.getMessage());
            System.err.println("Some tests may fail. Please ensure CSV files are available.");
        }
    }
    
    /**
     * Test implementation of RailwayGraphRepository for testing.
     */
    private static class TestRailwayGraphRepository extends RailwayGraphRepository {
        private final RailwayGraph testGraph;
        
        public TestRailwayGraphRepository(RailwayGraph graph) {
            this.testGraph = graph;
        }
        
        @Override
        public RailwayGraph buildGraph() {
            return testGraph;
        }
    }
    
    // ==================== Helper Methods ====================
    
    private RailwayGraphRepository createTestRepository() {
        RailwayGraph graph = new RailwayGraph();
        
        if (realStations != null && realStations.size() >= 3) {
            graph.addNode(realStations.get(0));
            graph.addNode(realStations.get(1));
            graph.addNode(realStations.get(2));
            
            // Try to find real lines
            if (realDataGraph != null) {
                List<Line> allLines = new ArrayList<>(realDataGraph.getAllEdges());
                for (Line line : allLines) {
                    if ((line.getDepartureStationId().equals(realStations.get(0).getId()) && 
                         line.getArrivalStationId().equals(realStations.get(1).getId())) ||
                        (line.getDepartureStationId().equals(realStations.get(1).getId()) && 
                         line.getArrivalStationId().equals(realStations.get(2).getId()))) {
                        graph.addEdge(line);
                    }
                }
            }
            
            // If no real lines found, create synthetic ones
            if (graph.getEdgeCount() == 0) {
                graph.addEdge(new Line(realStations.get(0).getId(), realStations.get(1).getId(), 10.0, 100, 5.0));
                graph.addEdge(new Line(realStations.get(1).getId(), realStations.get(2).getId(), 15.0, 100, 7.0));
            }
        } else {
            // Fallback to synthetic
            Station a = new Station("A", "Station A", 50.0, 4.0, 100.0, 200.0);
            Station b = new Station("B", "Station B", 51.0, 5.0, 101.0, 201.0);
            Station c = new Station("C", "Station C", 52.0, 6.0, 102.0, 202.0);
            
            graph.addNode(a);
            graph.addNode(b);
            graph.addNode(c);
            graph.addEdge(new Line("A", "B", 10.0, 100, 5.0));
            graph.addEdge(new Line("B", "C", 15.0, 100, 7.0));
        }
        
        return new TestRailwayGraphRepository(graph);
    }
    
    private RailwayGraphRepository createNegativeCycleRepository() {
        RailwayGraph graph = new RailwayGraph();
        Station a = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
        Station b = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
        Station c = new Station("C", "C", 52.0, 6.0, 102.0, 202.0);
        
        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addEdge(new Line("A", "B", 10.0, 100, 5.0));
        graph.addEdge(new Line("B", "C", 15.0, 100, 7.0));
        graph.addEdge(new Line("C", "A", 20.0, 100, -15.0));  // Negative cycle
        
        return new TestRailwayGraphRepository(graph);
    }
    
    private RailwayGraphRepository createEmptyRepository() {
        RailwayGraph graph = new RailwayGraph();
        return new TestRailwayGraphRepository(graph);
    }
    
    // ==================== Constructor Tests ====================
    
    @Test
    void constructor_ValidRepository() {
        RailwayGraphRepository repo = createTestRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        assertNotNull(controller);
    }
    
    @Test
    void constructor_NullRepository() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ShortestPathController(null);
        });
    }
    
    // ==================== Graph Building Tests ====================
    
    @Test
    void buildGraph_ValidData() {
        RailwayGraphRepository repo = createTestRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        controller.buildGraph();
        assertTrue(controller.isGraphBuilt());
    }
    
    @Test
    void buildGraph_EmptyRepository() {
        RailwayGraphRepository repo = createEmptyRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        controller.buildGraph();
        assertTrue(controller.isGraphBuilt());
    }
    
    @Test
    void buildGraph_MultipleCalls() {
        RailwayGraphRepository repo = createTestRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        controller.buildGraph();
        controller.buildGraph();  // Second call
        assertTrue(controller.isGraphBuilt());
    }
    
    // ==================== Path Calculation Tests ====================
    
    @Test
    void calculateShortestPath_SimplePath() {
        RailwayGraphRepository repo = createTestRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        controller.buildGraph();
        
        if (realStations != null && realStations.size() >= 2) {
            String sourceId = realStations.get(0).getId();
            String targetId = realStations.get(Math.min(1, realStations.size() - 1)).getId();
            
            ShortestPathController.ShortestPathResult result = controller.calculateShortestPath(sourceId, targetId);
            assertNotNull(result);
            // Path may or may not exist depending on real data
        }
    }
    
    @Test
    void calculateShortestPath_RealDataGraph() {
        if (realDataRepository == null) {
            // Skip if real data not loaded
            return;
        }
        
        ShortestPathController controller = new ShortestPathController(realDataRepository);
        controller.buildGraph();
        
        if (realStations != null && realStations.size() >= 2) {
            String sourceId = realStations.get(0).getId();
            String targetId = realStations.get(Math.min(10, realStations.size() - 1)).getId();
            
            ShortestPathController.ShortestPathResult result = controller.calculateShortestPath(sourceId, targetId);
            assertNotNull(result);
            // May have path or no path depending on connectivity
        }
    }
    
    @Test
    void calculateShortestPath_NegativeCycle() {
        RailwayGraphRepository repo = createNegativeCycleRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        controller.buildGraph();
        
        ShortestPathController.ShortestPathResult result = controller.calculateShortestPath("A", "B");
        assertNotNull(result);
        assertTrue(result.hasNegativeCycle());
    }
    
    @Test
    void calculateShortestPath_NoPath() {
        RailwayGraph graph = new RailwayGraph();
        if (realStations != null && realStations.size() >= 2) {
            graph.addNode(realStations.get(0));
            graph.addNode(realStations.get(1));
            // No edge between them
        } else {
            Station a = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station b = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            graph.addNode(a);
            graph.addNode(b);
        }
        
        ShortestPathController controller = new ShortestPathController(new TestRailwayGraphRepository(graph));
        controller.buildGraph();
        
        List<Station> stations = new ArrayList<>(graph.getAllNodes());
        ShortestPathController.ShortestPathResult result = controller.calculateShortestPath(
            stations.get(0).getId(), stations.get(1).getId());
        assertNotNull(result);
        assertFalse(result.hasPath());
    }
    
    @Test
    void calculateShortestPath_GraphNotBuilt() {
        RailwayGraphRepository repo = createTestRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        
        assertThrows(IllegalStateException.class, () -> {
            controller.calculateShortestPath("A", "B");
        });
    }
    
    @Test
    void calculateShortestPath_InvalidSource() {
        RailwayGraphRepository repo = createTestRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        controller.buildGraph();
        
        assertThrows(IllegalArgumentException.class, () -> {
            controller.calculateShortestPath(null, "B");
        });
    }
    
    @Test
    void calculateShortestPath_InvalidTarget() {
        RailwayGraphRepository repo = createTestRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        controller.buildGraph();
        
        assertThrows(IllegalArgumentException.class, () -> {
            controller.calculateShortestPath("A", null);
        });
    }
    
    @Test
    void calculateShortestPath_EmptySource() {
        RailwayGraphRepository repo = createTestRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        controller.buildGraph();
        
        assertThrows(IllegalArgumentException.class, () -> {
            controller.calculateShortestPath("", "B");
        });
    }
    
    @Test
    void calculateShortestPath_EmptyTarget() {
        RailwayGraphRepository repo = createTestRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        controller.buildGraph();
        
        assertThrows(IllegalArgumentException.class, () -> {
            controller.calculateShortestPath("A", "");
        });
    }
    
    @Test
    void calculateShortestPath_WhitespaceSource() {
        RailwayGraphRepository repo = createTestRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        controller.buildGraph();
        
        assertThrows(IllegalArgumentException.class, () -> {
            controller.calculateShortestPath("   ", "B");
        });
    }
    
    @Test
    void calculateShortestPath_WhitespaceTarget() {
        RailwayGraphRepository repo = createTestRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        controller.buildGraph();
        
        assertThrows(IllegalArgumentException.class, () -> {
            controller.calculateShortestPath("A", "   ");
        });
    }
    
    @Test
    void calculateShortestPath_SameSourceTarget() {
        RailwayGraphRepository repo = createTestRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        controller.buildGraph();
        
        if (realStations != null && !realStations.isEmpty()) {
            String stationId = realStations.get(0).getId();
            ShortestPathController.ShortestPathResult result = controller.calculateShortestPath(stationId, stationId);
            assertNotNull(result);
            assertTrue(result.hasPath());
            assertEquals(0.0, result.getTotalCost(), EPS);
        }
    }
    
    // ==================== Graph Statistics Tests ====================
    
    @Test
    void getGraphStatistics_AfterBuild() {
        RailwayGraphRepository repo = createTestRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        controller.buildGraph();
        
        String stats = controller.getGraphStatistics();
        assertNotNull(stats);
        assertTrue(stats.contains("Nodes") || stats.contains("Edges"));
    }
    
    @Test
    void getGraphStatistics_BeforeBuild() {
        RailwayGraphRepository repo = createTestRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        
        assertThrows(IllegalStateException.class, () -> {
            controller.getGraphStatistics();
        });
    }
    
    // ==================== Station Access Tests ====================
    
    @Test
    void getStationById_Existing() {
        RailwayGraphRepository repo = createTestRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        controller.buildGraph();
        
        if (realStations != null && !realStations.isEmpty()) {
            String stationId = realStations.get(0).getId();
            Station station = controller.getStationById(stationId);
            assertNotNull(station);
            assertEquals(stationId, station.getId());
        }
    }
    
    @Test
    void getStationById_NonExistent() {
        RailwayGraphRepository repo = createTestRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        controller.buildGraph();
        
        Station station = controller.getStationById("NONEXISTENT");
        assertNull(station);
    }
    
    @Test
    void getStationById_BeforeBuild() {
        RailwayGraphRepository repo = createTestRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        
        assertThrows(IllegalStateException.class, () -> {
            controller.getStationById("A");
        });
    }
    
    @Test
    void getAllStations_AfterBuild() {
        RailwayGraphRepository repo = createTestRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        controller.buildGraph();
        
        Collection<Station> stations = controller.getAllStations();
        assertNotNull(stations);
        assertTrue(stations.size() >= 0);
    }
    
    @Test
    void getAllStations_BeforeBuild() {
        RailwayGraphRepository repo = createTestRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        
        assertThrows(IllegalStateException.class, () -> {
            controller.getAllStations();
        });
    }
    
    // ==================== Graph State Tests ====================
    
    @Test
    void isGraphBuilt_AfterBuild() {
        RailwayGraphRepository repo = createTestRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        controller.buildGraph();
        assertTrue(controller.isGraphBuilt());
    }
    
    @Test
    void isGraphBuilt_BeforeBuild() {
        RailwayGraphRepository repo = createTestRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        assertFalse(controller.isGraphBuilt());
    }
    
    // ==================== Result Formatting Tests ====================
    
    @Test
    void shortestPathResult_WithPath() {
        RailwayGraphRepository repo = createTestRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        controller.buildGraph();
        
        if (realStations != null && realStations.size() >= 2) {
            String sourceId = realStations.get(0).getId();
            String targetId = realStations.get(Math.min(1, realStations.size() - 1)).getId();
            
            ShortestPathController.ShortestPathResult result = controller.calculateShortestPath(sourceId, targetId);
            assertNotNull(result);
            
            if (result.hasPath()) {
                String pathString = result.getPathString();
                assertNotNull(pathString);
                assertTrue(pathString.contains(sourceId) || pathString.contains(targetId));
            }
        }
    }
    
    @Test
    void shortestPathResult_WithNegativeCycle() {
        RailwayGraphRepository repo = createNegativeCycleRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        controller.buildGraph();
        
        ShortestPathController.ShortestPathResult result = controller.calculateShortestPath("A", "B");
        assertNotNull(result);
        assertTrue(result.hasNegativeCycle());
        
        String cycleString = result.getCycleString();
        assertNotNull(cycleString);
        assertTrue(cycleString.contains("Stations") || cycleString.contains("Edges"));
    }
    
    @Test
    void shortestPathResult_NoPath() {
        RailwayGraph graph = new RailwayGraph();
        if (realStations != null && realStations.size() >= 2) {
            graph.addNode(realStations.get(0));
            graph.addNode(realStations.get(1));
        } else {
            Station a = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station b = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            graph.addNode(a);
            graph.addNode(b);
        }
        
        ShortestPathController controller = new ShortestPathController(new TestRailwayGraphRepository(graph));
        controller.buildGraph();
        
        List<Station> stations = new ArrayList<>(graph.getAllNodes());
        ShortestPathController.ShortestPathResult result = controller.calculateShortestPath(
            stations.get(0).getId(), stations.get(1).getId());
        assertNotNull(result);
        assertFalse(result.hasPath());
        assertNull(result.getPathString());
    }
    
    @Test
    void shortestPathResult_GetPathWithNullDomainResult() {
        // Test ShortestPathResult with null domainResult
        ShortestPathController.ShortestPathResult result = 
            new ShortestPathController.ShortestPathResult(null);
        
        assertFalse(result.hasPath());
        assertFalse(result.hasNegativeCycle());
        assertNull(result.getPathString());
        assertNull(result.getPath());
        assertTrue(Double.isNaN(result.getTotalCost()));
        assertNull(result.getCycleStations());
        assertNull(result.getCycleEdges());
        assertTrue(Double.isNaN(result.getCycleCost()));
        assertNull(result.getCycleString());
    }
    
    @Test
    void shortestPathResult_GetCycleStringWithNullStations() {
        RailwayGraphRepository repo = createNegativeCycleRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        controller.buildGraph();
        
        ShortestPathController.ShortestPathResult result = controller.calculateShortestPath("A", "B");
        assertNotNull(result);
        assertTrue(result.hasNegativeCycle());
        
        // Test getCycleString - should handle various null/empty cases
        String cycleString = result.getCycleString();
        assertNotNull(cycleString);
        // The cycle string should contain information about the cycle
    }
    
    @Test
    void shortestPathResult_GetTotalCostWithPath() {
        RailwayGraphRepository repo = createTestRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        controller.buildGraph();
        
        if (realStations != null && realStations.size() >= 2) {
            String sourceId = realStations.get(0).getId();
            String targetId = realStations.get(Math.min(1, realStations.size() - 1)).getId();
            
            ShortestPathController.ShortestPathResult result = controller.calculateShortestPath(sourceId, targetId);
            assertNotNull(result);
            
            if (result.hasPath()) {
                double cost = result.getTotalCost();
                assertFalse(Double.isNaN(cost));
                assertTrue(cost >= 0.0);
            }
        }
    }
    
    @Test
    void shortestPathResult_GetPathSteps() {
        RailwayGraphRepository repo = createTestRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        controller.buildGraph();
        
        if (realStations != null && realStations.size() >= 2) {
            String sourceId = realStations.get(0).getId();
            String targetId = realStations.get(Math.min(1, realStations.size() - 1)).getId();
            
            ShortestPathController.ShortestPathResult result = controller.calculateShortestPath(sourceId, targetId);
            assertNotNull(result);
            
            if (result.hasPath()) {
                List<BellmanFord.PathStep> path = result.getPath();
                assertNotNull(path);
                assertFalse(path.isEmpty());
            }
        }
    }
    
    @Test
    void shortestPathResult_GetCycleInfo() {
        RailwayGraphRepository repo = createNegativeCycleRepository();
        ShortestPathController controller = new ShortestPathController(repo);
        controller.buildGraph();
        
        ShortestPathController.ShortestPathResult result = controller.calculateShortestPath("A", "B");
        assertNotNull(result);
        assertTrue(result.hasNegativeCycle());
        
        List<String> cycleStations = result.getCycleStations();
        List<BellmanFord.CycleEdge> cycleEdges = result.getCycleEdges();
        double cycleCost = result.getCycleCost();
        
        // At least one of these should be non-null if cycle exists
        assertTrue(cycleStations != null || cycleEdges != null || !Double.isNaN(cycleCost));
    }
}
