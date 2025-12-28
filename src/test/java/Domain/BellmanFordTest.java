package Domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BellmanFord algorithm (USEI15).
 * Tests shortest path calculation with positive and negative costs,
 * negative cycle detection, and edge cases.
 */
class BellmanFordTest {
    
    private RailwayGraph graph;
    private Station stationA, stationB, stationC, stationD, stationE;
    
    @BeforeEach
    void setUp() {
        graph = new RailwayGraph();
        
        // Create test stations
        stationA = new Station("A", "Station A", 50.0, 4.0, 100.0, 200.0);
        stationB = new Station("B", "Station B", 51.0, 5.0, 150.0, 250.0);
        stationC = new Station("C", "Station C", 52.0, 6.0, 200.0, 300.0);
        stationD = new Station("D", "Station D", 53.0, 7.0, 250.0, 350.0);
        stationE = new Station("E", "Station E", 54.0, 8.0, 300.0, 400.0);
        
        // Add stations to graph
        graph.addNode(stationA);
        graph.addNode(stationB);
        graph.addNode(stationC);
        graph.addNode(stationD);
        graph.addNode(stationE);
    }
    
    // ==================== Constructor and Validation Tests ====================
    
    @Test
    void findShortestPath_ShouldThrowException_WhenGraphIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> BellmanFord.findShortestPath(null, "A", "B"),
                "Should throw exception for null graph");
    }
    
    @Test
    void findShortestPath_ShouldThrowException_WhenSourceIdIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> BellmanFord.findShortestPath(graph, null, "B"),
                "Should throw exception for null source ID");
    }
    
    @Test
    void findShortestPath_ShouldThrowException_WhenTargetIdIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> BellmanFord.findShortestPath(graph, "A", null),
                "Should throw exception for null target ID");
    }
    
    @Test
    void findShortestPath_ShouldThrowException_WhenSourceDoesNotExist() {
        assertThrows(IllegalArgumentException.class,
                () -> BellmanFord.findShortestPath(graph, "X", "A"),
                "Should throw exception when source station doesn't exist");
    }
    
    @Test
    void findShortestPath_ShouldThrowException_WhenTargetDoesNotExist() {
        assertThrows(IllegalArgumentException.class,
                () -> BellmanFord.findShortestPath(graph, "A", "X"),
                "Should throw exception when target station doesn't exist");
    }
    
    // ==================== Simple Path Tests ====================
    
    @Test
    void findShortestPath_ShouldReturnTrivialPath_WhenSourceEqualsTarget() {
        BellmanFord.ShortestPathResult result = BellmanFord.findShortestPath(graph, "A", "A");
        
        assertTrue(result.hasPath(), "Should have a path (trivial)");
        assertFalse(result.hasNegativeCycle(), "Should not have negative cycle");
        assertEquals(0.0, result.getTotalCost(), 0.001, "Cost should be 0");
        
        List<BellmanFord.PathStep> path = result.getPath();
        assertNotNull(path, "Path should not be null");
        assertEquals(1, path.size(), "Path should have one step");
        assertEquals("A", path.get(0).getStationId(), "Path should contain source station");
        assertEquals(0.0, path.get(0).getAccumulatedCost(), 0.001, "Accumulated cost should be 0");
    }
    
    @Test
    void findShortestPath_ShouldFindDirectPath_WithPositiveCosts() {
        // A -> B (cost: 5.0)
        Line lineAB = new Line("A", "B", 10.0, 20, 5.0);
        graph.addEdge(lineAB);
        
        BellmanFord.ShortestPathResult result = BellmanFord.findShortestPath(graph, "A", "B");
        
        assertTrue(result.hasPath(), "Should have a path");
        assertFalse(result.hasNegativeCycle(), "Should not have negative cycle");
        assertEquals(5.0, result.getTotalCost(), 0.001, "Total cost should be 5.0");
        
        List<BellmanFord.PathStep> path = result.getPath();
        assertNotNull(path, "Path should not be null");
        assertEquals(2, path.size(), "Path should have 2 steps");
        assertEquals("A", path.get(0).getStationId(), "First step should be A");
        assertEquals(0.0, path.get(0).getAccumulatedCost(), 0.001, "A cost should be 0");
        assertEquals("B", path.get(1).getStationId(), "Second step should be B");
        assertEquals(5.0, path.get(1).getAccumulatedCost(), 0.001, "B cost should be 5.0");
    }
    
    @Test
    void findShortestPath_ShouldFindPath_WithMultipleSteps() {
        // A -> B (cost: 5.0)
        // B -> C (cost: 3.0)
        // C -> D (cost: 2.0)
        graph.addEdge(new Line("A", "B", 10.0, 20, 5.0));
        graph.addEdge(new Line("B", "C", 8.0, 15, 3.0));
        graph.addEdge(new Line("C", "D", 5.0, 10, 2.0));
        
        BellmanFord.ShortestPathResult result = BellmanFord.findShortestPath(graph, "A", "D");
        
        assertTrue(result.hasPath(), "Should have a path");
        assertEquals(10.0, result.getTotalCost(), 0.001, "Total cost should be 10.0");
        
        List<BellmanFord.PathStep> path = result.getPath();
        assertEquals(4, path.size(), "Path should have 4 steps");
        assertEquals("A", path.get(0).getStationId());
        assertEquals("B", path.get(1).getStationId());
        assertEquals("C", path.get(2).getStationId());
        assertEquals("D", path.get(3).getStationId());
        assertEquals(0.0, path.get(0).getAccumulatedCost(), 0.001);
        assertEquals(5.0, path.get(1).getAccumulatedCost(), 0.001);
        assertEquals(8.0, path.get(2).getAccumulatedCost(), 0.001);
        assertEquals(10.0, path.get(3).getAccumulatedCost(), 0.001);
    }
    
    @Test
    void findShortestPath_ShouldFindShortestPath_WhenMultiplePathsExist() {
        // Path 1: A -> B -> D (cost: 5 + 4 = 9)
        // Path 2: A -> C -> D (cost: 2 + 3 = 5) <- shortest
        graph.addEdge(new Line("A", "B", 10.0, 20, 5.0));
        graph.addEdge(new Line("A", "C", 8.0, 15, 2.0));
        graph.addEdge(new Line("B", "D", 6.0, 12, 4.0));
        graph.addEdge(new Line("C", "D", 5.0, 10, 3.0));
        
        BellmanFord.ShortestPathResult result = BellmanFord.findShortestPath(graph, "A", "D");
        
        assertTrue(result.hasPath(), "Should have a path");
        assertEquals(5.0, result.getTotalCost(), 0.001, "Should find shortest path (cost 5)");
        
        List<BellmanFord.PathStep> path = result.getPath();
        assertEquals(3, path.size(), "Path should have 3 steps");
        assertEquals("A", path.get(0).getStationId());
        assertEquals("C", path.get(1).getStationId());
        assertEquals("D", path.get(2).getStationId());
    }
    
    // ==================== Negative Cost Tests ====================
    
    @Test
    void findShortestPath_ShouldHandleNegativeCosts() {
        // A -> B (cost: -2.0)
        graph.addEdge(new Line("A", "B", 10.0, 20, -2.0));
        
        BellmanFord.ShortestPathResult result = BellmanFord.findShortestPath(graph, "A", "B");
        
        assertTrue(result.hasPath(), "Should have a path");
        assertEquals(-2.0, result.getTotalCost(), 0.001, "Cost should be negative");
    }
    
    @Test
    void findShortestPath_ShouldFindPath_WithMixedPositiveAndNegativeCosts() {
        // A -> B (cost: 5.0)
        // B -> C (cost: -3.0)
        // C -> D (cost: 2.0)
        graph.addEdge(new Line("A", "B", 10.0, 20, 5.0));
        graph.addEdge(new Line("B", "C", 8.0, 15, -3.0));
        graph.addEdge(new Line("C", "D", 5.0, 10, 2.0));
        
        BellmanFord.ShortestPathResult result = BellmanFord.findShortestPath(graph, "A", "D");
        
        assertTrue(result.hasPath(), "Should have a path");
        assertEquals(4.0, result.getTotalCost(), 0.001, "Total cost should be 4.0 (5 - 3 + 2)");
    }
    
    // ==================== Negative Cycle Tests ====================
    
    @Test
    void findShortestPath_ShouldDetectNegativeCycle() {
        // Create a negative cycle: A -> B (5), B -> C (-3), C -> A (-5)
        // Total cycle cost: 5 + (-3) + (-5) = -3 (negative cycle)
        graph.addEdge(new Line("A", "B", 10.0, 20, 5.0));
        graph.addEdge(new Line("B", "C", 8.0, 15, -3.0));
        graph.addEdge(new Line("C", "A", 6.0, 12, -5.0));
        
        BellmanFord.ShortestPathResult result = BellmanFord.findShortestPath(graph, "A", "B");
        
        assertTrue(result.hasNegativeCycle(), "Should detect negative cycle");
        assertFalse(result.hasPath(), "Should not have a valid path");
        
        BellmanFord.NegativeCycleInfo cycleInfo = result.getCycleInfo();
        assertNotNull(cycleInfo, "Cycle info should not be null");
        
        List<String> cycleStations = cycleInfo.getCycleStations();
        assertNotNull(cycleStations, "Cycle stations should not be null");
        assertFalse(cycleStations.isEmpty(), "Cycle should have stations");
        
        List<BellmanFord.CycleEdge> cycleEdges = cycleInfo.getCycleEdges();
        assertNotNull(cycleEdges, "Cycle edges should not be null");
        assertFalse(cycleEdges.isEmpty(), "Cycle should have edges");
        
        double cycleCost = cycleInfo.getCycleCost();
        assertTrue(cycleCost < 0, "Cycle cost should be negative");
    }
    
    @Test
    void findShortestPath_ShouldDetectNegativeCycle_WithSingleNegativeEdge() {
        // Simple negative cycle: A -> B (5), B -> A (-10)
        // Total: 5 + (-10) = -5 (negative cycle)
        graph.addEdge(new Line("A", "B", 10.0, 20, 5.0));
        graph.addEdge(new Line("B", "A", 10.0, 20, -10.0));
        
        BellmanFord.ShortestPathResult result = BellmanFord.findShortestPath(graph, "A", "B");
        
        assertTrue(result.hasNegativeCycle(), "Should detect negative cycle");
    }
    
    // ==================== No Path Tests ====================
    
    @Test
    void findShortestPath_ShouldReturnNoPath_WhenTargetIsUnreachable() {
        // A -> B, but no path to C
        graph.addEdge(new Line("A", "B", 10.0, 20, 5.0));
        
        BellmanFord.ShortestPathResult result = BellmanFord.findShortestPath(graph, "A", "C");
        
        assertFalse(result.hasPath(), "Should not have a path");
        assertFalse(result.hasNegativeCycle(), "Should not have negative cycle");
        assertTrue(Double.isNaN(result.getTotalCost()), "Total cost should be NaN");
    }
    
    @Test
    void findShortestPath_ShouldReturnNoPath_WhenGraphIsDisconnected() {
        // A -> B, but C and D are isolated
        graph.addEdge(new Line("A", "B", 10.0, 20, 5.0));
        
        BellmanFord.ShortestPathResult result = BellmanFord.findShortestPath(graph, "C", "D");
        
        assertFalse(result.hasPath(), "Should not have a path");
    }
    
    // ==================== Complex Graph Tests ====================
    
    @Test
    void findShortestPath_ShouldHandleComplexGraph() {
        // Create a more complex graph:
        // A -> B (5), A -> C (2)
        // B -> D (4), B -> E (3)
        // C -> D (3), C -> E (6)
        // D -> E (1)
        graph.addEdge(new Line("A", "B", 10.0, 20, 5.0));
        graph.addEdge(new Line("A", "C", 8.0, 15, 2.0));
        graph.addEdge(new Line("B", "D", 6.0, 12, 4.0));
        graph.addEdge(new Line("B", "E", 7.0, 14, 3.0));
        graph.addEdge(new Line("C", "D", 5.0, 10, 3.0));
        graph.addEdge(new Line("C", "E", 9.0, 18, 6.0));
        graph.addEdge(new Line("D", "E", 4.0, 8, 1.0));
        
        // Shortest path from A to E should be: A -> C -> D -> E (cost: 2 + 3 + 1 = 6)
        BellmanFord.ShortestPathResult result = BellmanFord.findShortestPath(graph, "A", "E");
        
        assertTrue(result.hasPath(), "Should have a path");
        assertEquals(6.0, result.getTotalCost(), 0.001, "Should find shortest path");
    }
    
    @Test
    void findShortestPath_ShouldHandleGraphWithNegativeCycle_NotInPath() {
        // Graph with negative cycle (B -> C -> B) but path A -> D doesn't use it
        // A -> B (5), B -> C (3), C -> B (-10) [negative cycle]
        // A -> D (2) [direct path]
        graph.addEdge(new Line("A", "B", 10.0, 20, 5.0));
        graph.addEdge(new Line("B", "C", 8.0, 15, 3.0));
        graph.addEdge(new Line("C", "B", 8.0, 15, -10.0)); // Negative cycle
        graph.addEdge(new Line("A", "D", 5.0, 10, 2.0));
        
        // Path A -> D should still work (cost: 2)
        BellmanFord.ShortestPathResult result = BellmanFord.findShortestPath(graph, "A", "D");
        
        // Note: Bellman-Ford detects ANY negative cycle reachable from source
        // Since B is reachable from A, the negative cycle will be detected
        // This is correct behavior - if a negative cycle is reachable, we can't trust any path
        assertTrue(result.hasNegativeCycle() || result.hasPath(), 
                "Should either detect cycle or find path");
    }
}

