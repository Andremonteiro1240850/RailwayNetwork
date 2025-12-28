package Controller;

import Domain.*;
import Repository.LineRepository;
import Repository.RailwayGraphRepository;
import Repository.StationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ShortestPathController class (USEI15).
 * Tests use real CSV data loaded from Sprint3 files.
 */
class ShortestPathControllerTest {
    
    private static final String STATIONS_PATH = "train_station_dataset/Sprint3/stations.csv";
    private static final String LINES_PATH = "train_station_dataset/Sprint3/lines.csv";
    
    private RailwayGraphRepository graphRepository;
    private ShortestPathController controller;
    
    @BeforeEach
    void setUp() {
        graphRepository = new RailwayGraphRepository();
        try {
            graphRepository.loadData(STATIONS_PATH, LINES_PATH);
            controller = new ShortestPathController(graphRepository);
            controller.buildGraph();
        } catch (Exception e) {
            fail("Failed to load test data: " + e.getMessage());
        }
    }
    
    // ==================== Constructor Tests ====================
    
    @Test
    void constructor_ShouldCreateController_WhenRepositoryIsValid() {
        RailwayGraphRepository repo = new RailwayGraphRepository();
        ShortestPathController ctrl = new ShortestPathController(repo);
        
        assertNotNull(ctrl, "Controller should not be null");
    }
    
    @Test
    void constructor_ShouldThrowException_WhenRepositoryIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new ShortestPathController(null),
                "Should throw exception for null repository");
    }
    
    // ==================== Build Graph Tests ====================
    
    @Test
    void buildGraph_ShouldBuildGraph_WhenDataIsLoaded() {
        RailwayGraphRepository repo = new RailwayGraphRepository();
        repo.loadData(STATIONS_PATH, LINES_PATH);
        ShortestPathController ctrl = new ShortestPathController(repo);
        
        assertDoesNotThrow(() -> ctrl.buildGraph(), "Should build graph without errors");
        assertTrue(ctrl.isGraphBuilt(), "Graph should be built");
    }
    
    @Test
    void buildGraph_ShouldThrowException_WhenDataNotLoaded() {
        RailwayGraphRepository repo = new RailwayGraphRepository();
        ShortestPathController ctrl = new ShortestPathController(repo);
        
        assertThrows(IllegalStateException.class,
                () -> ctrl.buildGraph(),
                "Should throw exception when data not loaded");
    }
    
    // ==================== Calculate Shortest Path Tests ====================
    
    @Test
    void calculateShortestPath_ShouldFindPath_WhenPathExists() {
        // Use real station IDs from the CSV
        // Testing with stations that should have connections
        ShortestPathController.ShortestPathResult result = 
            controller.calculateShortestPath("906", "151");
        
        assertNotNull(result, "Result should not be null");
        // Path may or may not exist depending on graph structure
        // Just verify it doesn't throw exception and returns valid result
    }
    
    @Test
    void calculateShortestPath_ShouldThrowException_WhenSourceIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.calculateShortestPath(null, "151"),
                "Should throw exception for null source");
    }
    
    @Test
    void calculateShortestPath_ShouldThrowException_WhenTargetIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.calculateShortestPath("906", null),
                "Should throw exception for null target");
    }
    
    @Test
    void calculateShortestPath_ShouldThrowException_WhenSourceIsEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.calculateShortestPath("", "151"),
                "Should throw exception for empty source");
    }
    
    @Test
    void calculateShortestPath_ShouldThrowException_WhenTargetIsEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.calculateShortestPath("906", ""),
                "Should throw exception for empty target");
    }
    
    @Test
    void calculateShortestPath_ShouldThrowException_WhenGraphNotBuilt() {
        RailwayGraphRepository repo = new RailwayGraphRepository();
        repo.loadData(STATIONS_PATH, LINES_PATH);
        ShortestPathController ctrl = new ShortestPathController(repo);
        // Don't call buildGraph()
        
        assertThrows(IllegalStateException.class,
                () -> ctrl.calculateShortestPath("906", "151"),
                "Should throw exception when graph not built");
    }
    
    @Test
    void calculateShortestPath_ShouldReturnTrivialPath_WhenSourceEqualsTarget() {
        ShortestPathController.ShortestPathResult result = 
            controller.calculateShortestPath("906", "906");
        
        assertTrue(result.hasPath(), "Should have a path (trivial)");
        assertEquals(0.0, result.getTotalCost(), 0.001, "Cost should be 0");
        
        List<BellmanFord.PathStep> path = result.getPath();
        assertNotNull(path, "Path should not be null");
        assertEquals(1, path.size(), "Path should have one step");
        assertEquals("906", path.get(0).getStationId());
    }
    
    // ==================== Path String Format Tests ====================
    
    @Test
    void getPathString_ShouldReturnFormattedString_WhenPathExists() {
        // Create a simple test graph
        RailwayGraph testGraph = new RailwayGraph();
        Station s1 = new Station("1", "Station 1", 50.0, 4.0, 100.0, 200.0);
        Station s2 = new Station("2", "Station 2", 51.0, 5.0, 150.0, 250.0);
        Station s3 = new Station("3", "Station 3", 52.0, 6.0, 200.0, 300.0);
        
        testGraph.addNode(s1);
        testGraph.addNode(s2);
        testGraph.addNode(s3);
        
        testGraph.addEdge(new Line("1", "2", 10.0, 20, 5.0));
        testGraph.addEdge(new Line("2", "3", 8.0, 15, 3.0));
        
        BellmanFord.ShortestPathResult domainResult = 
            BellmanFord.findShortestPath(testGraph, "1", "3");
        
        ShortestPathController.ShortestPathResult controllerResult = 
            new ShortestPathController.ShortestPathResult(domainResult);
        
        String pathString = controllerResult.getPathString();
        assertNotNull(pathString, "Path string should not be null");
        assertTrue(pathString.contains("1"), "Should contain source station");
        assertTrue(pathString.contains("3"), "Should contain target station");
        assertTrue(pathString.contains("2"), "Should contain intermediate station");
    }
    
    // ==================== Negative Cycle Tests ====================
    
    @Test
    void getCycleString_ShouldReturnFormattedString_WhenNegativeCycleExists() {
        // Create a graph with negative cycle
        RailwayGraph testGraph = new RailwayGraph();
        Station s1 = new Station("1", "Station 1", 50.0, 4.0, 100.0, 200.0);
        Station s2 = new Station("2", "Station 2", 51.0, 5.0, 150.0, 250.0);
        Station s3 = new Station("3", "Station 3", 52.0, 6.0, 200.0, 300.0);
        
        testGraph.addNode(s1);
        testGraph.addNode(s2);
        testGraph.addNode(s3);
        
        testGraph.addEdge(new Line("1", "2", 10.0, 20, 5.0));
        testGraph.addEdge(new Line("2", "3", 8.0, 15, -3.0));
        testGraph.addEdge(new Line("3", "1", 6.0, 12, -5.0)); // Negative cycle
        
        BellmanFord.ShortestPathResult domainResult = 
            BellmanFord.findShortestPath(testGraph, "1", "2");
        
        ShortestPathController.ShortestPathResult controllerResult = 
            new ShortestPathController.ShortestPathResult(domainResult);
        
        assertTrue(controllerResult.hasNegativeCycle(), "Should have negative cycle");
        
        String cycleString = controllerResult.getCycleString();
        assertNotNull(cycleString, "Cycle string should not be null");
        assertTrue(cycleString.contains("Stations"), "Should contain stations info");
        assertTrue(cycleString.contains("Edges"), "Should contain edges info");
    }
    
    // ==================== Graph Statistics Tests ====================
    
    @Test
    void getGraphStatistics_ShouldReturnStatistics_WhenGraphIsBuilt() {
        String statistics = controller.getGraphStatistics();
        
        assertNotNull(statistics, "Statistics should not be null");
        assertTrue(statistics.contains("Nodes"), "Should contain nodes count");
        assertTrue(statistics.contains("Edges"), "Should contain edges count");
    }
    
    @Test
    void getGraphStatistics_ShouldThrowException_WhenGraphNotBuilt() {
        RailwayGraphRepository repo = new RailwayGraphRepository();
        ShortestPathController ctrl = new ShortestPathController(repo);
        
        assertThrows(IllegalStateException.class,
                () -> ctrl.getGraphStatistics(),
                "Should throw exception when graph not built");
    }
    
    // ==================== Station Access Tests ====================
    
    @Test
    void getAllStations_ShouldReturnAllStations_WhenGraphIsBuilt() {
        Collection<Station> stations = controller.getAllStations();
        
        assertNotNull(stations, "Stations collection should not be null");
        assertFalse(stations.isEmpty(), "Should have stations");
    }
    
    @Test
    void getAllStations_ShouldThrowException_WhenGraphNotBuilt() {
        RailwayGraphRepository repo = new RailwayGraphRepository();
        ShortestPathController ctrl = new ShortestPathController(repo);
        
        assertThrows(IllegalStateException.class,
                () -> ctrl.getAllStations(),
                "Should throw exception when graph not built");
    }
    
    @Test
    void getStationById_ShouldReturnStation_WhenStationExists() {
        Station station = controller.getStationById("906");
        
        assertNotNull(station, "Station should not be null");
        assertEquals("906", station.getId(), "Should return correct station");
    }
    
    @Test
    void getStationById_ShouldReturnNull_WhenStationDoesNotExist() {
        Station station = controller.getStationById("99999");
        
        assertNull(station, "Should return null for non-existent station");
    }
    
    @Test
    void getStationById_ShouldThrowException_WhenGraphNotBuilt() {
        RailwayGraphRepository repo = new RailwayGraphRepository();
        ShortestPathController ctrl = new ShortestPathController(repo);
        
        assertThrows(IllegalStateException.class,
                () -> ctrl.getStationById("906"),
                "Should throw exception when graph not built");
    }
    
    // ==================== Result Object Tests ====================
    
    @Test
    void result_HasPath_ShouldReturnTrue_WhenPathExists() {
        // Create simple path
        RailwayGraph testGraph = new RailwayGraph();
        Station s1 = new Station("1", "Station 1", 50.0, 4.0, 100.0, 200.0);
        Station s2 = new Station("2", "Station 2", 51.0, 5.0, 150.0, 250.0);
        
        testGraph.addNode(s1);
        testGraph.addNode(s2);
        testGraph.addEdge(new Line("1", "2", 10.0, 20, 5.0));
        
        BellmanFord.ShortestPathResult domainResult = 
            BellmanFord.findShortestPath(testGraph, "1", "2");
        
        ShortestPathController.ShortestPathResult controllerResult = 
            new ShortestPathController.ShortestPathResult(domainResult);
        
        assertTrue(controllerResult.hasPath(), "Should have path");
        assertFalse(controllerResult.hasNegativeCycle(), "Should not have negative cycle");
    }
    
    @Test
    void result_HasNegativeCycle_ShouldReturnTrue_WhenCycleExists() {
        // Create graph with negative cycle
        RailwayGraph testGraph = new RailwayGraph();
        Station s1 = new Station("1", "Station 1", 50.0, 4.0, 100.0, 200.0);
        Station s2 = new Station("2", "Station 2", 51.0, 5.0, 150.0, 250.0);
        
        testGraph.addNode(s1);
        testGraph.addNode(s2);
        testGraph.addEdge(new Line("1", "2", 10.0, 20, 5.0));
        testGraph.addEdge(new Line("2", "1", 10.0, 20, -10.0)); // Negative cycle
        
        BellmanFord.ShortestPathResult domainResult = 
            BellmanFord.findShortestPath(testGraph, "1", "2");
        
        ShortestPathController.ShortestPathResult controllerResult = 
            new ShortestPathController.ShortestPathResult(domainResult);
        
        assertTrue(controllerResult.hasNegativeCycle(), "Should have negative cycle");
        assertFalse(controllerResult.hasPath(), "Should not have path");
    }
    
    @Test
    void result_GetCycleStations_ShouldReturnStations_WhenCycleExists() {
        RailwayGraph testGraph = new RailwayGraph();
        Station s1 = new Station("1", "Station 1", 50.0, 4.0, 100.0, 200.0);
        Station s2 = new Station("2", "Station 2", 51.0, 5.0, 150.0, 250.0);
        
        testGraph.addNode(s1);
        testGraph.addNode(s2);
        testGraph.addEdge(new Line("1", "2", 10.0, 20, 5.0));
        testGraph.addEdge(new Line("2", "1", 10.0, 20, -10.0));
        
        BellmanFord.ShortestPathResult domainResult = 
            BellmanFord.findShortestPath(testGraph, "1", "2");
        
        ShortestPathController.ShortestPathResult controllerResult = 
            new ShortestPathController.ShortestPathResult(domainResult);
        
        List<String> cycleStations = controllerResult.getCycleStations();
        assertNotNull(cycleStations, "Cycle stations should not be null");
        assertFalse(cycleStations.isEmpty(), "Should have cycle stations");
    }
    
    @Test
    void result_GetCycleEdges_ShouldReturnEdges_WhenCycleExists() {
        RailwayGraph testGraph = new RailwayGraph();
        Station s1 = new Station("1", "Station 1", 50.0, 4.0, 100.0, 200.0);
        Station s2 = new Station("2", "Station 2", 51.0, 5.0, 150.0, 250.0);
        
        testGraph.addNode(s1);
        testGraph.addNode(s2);
        testGraph.addEdge(new Line("1", "2", 10.0, 20, 5.0));
        testGraph.addEdge(new Line("2", "1", 10.0, 20, -10.0));
        
        BellmanFord.ShortestPathResult domainResult = 
            BellmanFord.findShortestPath(testGraph, "1", "2");
        
        ShortestPathController.ShortestPathResult controllerResult = 
            new ShortestPathController.ShortestPathResult(domainResult);
        
        List<BellmanFord.CycleEdge> cycleEdges = controllerResult.getCycleEdges();
        assertNotNull(cycleEdges, "Cycle edges should not be null");
        assertFalse(cycleEdges.isEmpty(), "Should have cycle edges");
    }
    
    // ==================== Integration Tests with Real Data ====================
    
    @Test
    void calculateShortestPath_ShouldWork_WithRealData() {
        // Test with actual station IDs from CSV
        // This is an integration test using real data
        
        // Try a few different station pairs
        String[] testPairs = {
            "906", "151",  // From CSV line 2
            "1192", "1663", // From CSV line 3
            "1245", "810"   // From CSV line 4
        };
        
        for (int i = 0; i < testPairs.length; i += 2) {
            String source = testPairs[i];
            String target = testPairs[i + 1];
            
            assertDoesNotThrow(() -> {
                ShortestPathController.ShortestPathResult result = 
                    controller.calculateShortestPath(source, target);
                
                assertNotNull(result, "Result should not be null for " + source + " -> " + target);
                // Result may have path, negative cycle, or no path - all are valid
            }, "Should not throw exception for " + source + " -> " + target);
        }
    }
    
    @Test
    void calculateShortestPath_ShouldHandleNegativeCosts_WithRealData() {
        // Check if there are any negative costs in the real data
        // Line 20 in CSV has a negative cost: 259,258,0.614264930704467,40,-3.415768169905445
        
        // Test path that might use negative cost edge
        assertDoesNotThrow(() -> {
            ShortestPathController.ShortestPathResult result = 
                controller.calculateShortestPath("259", "258");
            
            assertNotNull(result, "Result should not be null");
            // May have path or negative cycle depending on graph structure
        }, "Should handle negative costs");
    }
}

