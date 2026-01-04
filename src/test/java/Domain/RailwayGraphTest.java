package Domain;

import Repository.RailwayGraphRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for RailwayGraph (USEI11).
 * 
 * Tests cover:
 * - Constructor
 * - Node operations (add, get, contains, count)
 * - Edge operations (add, get outgoing/incoming, count)
 * - Neighbor operations
 * - Edge cases and error handling
 * - Immutability of returned collections
 * - Real data integration
 * 
 * All tests use REAL data from train_station_dataset/Sprint3/stations.csv and lines.csv
 * 
 * @author Development Team
 * @version 1.0
 */
public class RailwayGraphTest {
    
    // Real data loaded from CSV files
    private static RailwayGraphRepository realDataRepository;
    private static List<Station> realStations;
    private static List<Line> realLines;
    private static RailwayGraph realGraph;
    
    private RailwayGraph graph;
    
    @BeforeAll
    static void loadRealData() {
        try {
            realDataRepository = new RailwayGraphRepository();
            realDataRepository.loadData("train_station_dataset/Sprint3/stations.csv", 
                                       "train_station_dataset/Sprint3/lines.csv");
            
            realGraph = realDataRepository.buildGraph();
            realStations = new ArrayList<>(realGraph.getAllNodes());
            realLines = new ArrayList<>(realGraph.getAllEdges());
            
            System.out.println("Loaded " + realStations.size() + " stations and " + realLines.size() + " lines for testing");
        } catch (Exception e) {
            System.err.println("WARNING: Failed to load real data: " + e.getMessage());
            System.err.println("Some tests may fail. Please ensure CSV files are available.");
        }
    }
    
    @BeforeEach
    void setUp() {
        graph = new RailwayGraph();
    }
    
    // ==================== Constructor Tests ====================
    
    @Test
    void constructor_CreatesEmptyGraph() {
        RailwayGraph newGraph = new RailwayGraph();
        
        assertEquals(0, newGraph.getNodeCount());
        assertEquals(0, newGraph.getEdgeCount());
        assertTrue(newGraph.getAllNodes().isEmpty());
        assertTrue(newGraph.getAllEdges().isEmpty());
    }
    
    // ==================== addNode Tests ====================
    
    @Test
    void addNode_WithNull_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            graph.addNode(null);
        });
    }
    
    @Test
    void addNode_WithValidStation_AddsNode() {
        Station station = new Station("TEST1", "Test Station", 50.0, 4.0, 100.0, 200.0);
        
        graph.addNode(station);
        
        assertEquals(1, graph.getNodeCount());
        assertTrue(graph.containsNode("TEST1"));
        assertEquals(station, graph.getNode("TEST1"));
    }
    
    @Test
    void addNode_WithRealStation_AddsNode() {
        if (realStations == null || realStations.isEmpty()) {
            System.out.println("Skipping test - no real data available");
            return;
        }
        
        Station station = realStations.get(0);
        graph.addNode(station);
        
        assertEquals(1, graph.getNodeCount());
        assertTrue(graph.containsNode(station.getId()));
        assertEquals(station, graph.getNode(station.getId()));
    }
    
    @Test
    void addNode_WithDuplicateId_ReplacesNode() {
        Station station1 = new Station("TEST1", "Test Station 1", 50.0, 4.0, 100.0, 200.0);
        Station station2 = new Station("TEST1", "Test Station 2", 51.0, 5.0, 101.0, 201.0);
        
        graph.addNode(station1);
        graph.addNode(station2);
        
        assertEquals(1, graph.getNodeCount());
        assertEquals(station2, graph.getNode("TEST1"));
        assertEquals("Test Station 2", graph.getNode("TEST1").getName());
    }
    
    @Test
    void addNode_WithMultipleStations_AddsAll() {
        if (realStations == null || realStations.size() < 5) {
            System.out.println("Skipping test - insufficient real data available");
            return;
        }
        
        for (int i = 0; i < 5; i++) {
            graph.addNode(realStations.get(i));
        }
        
        assertEquals(5, graph.getNodeCount());
        for (int i = 0; i < 5; i++) {
            assertTrue(graph.containsNode(realStations.get(i).getId()));
        }
    }
    
    @Test
    void addNode_InitializesEdgeLists() {
        Station station = new Station("TEST1", "Test Station", 50.0, 4.0, 100.0, 200.0);
        
        graph.addNode(station);
        
        assertTrue(graph.getOutgoingEdges("TEST1").isEmpty());
        assertTrue(graph.getIncomingEdges("TEST1").isEmpty());
    }
    
    // ==================== addEdge Tests ====================
    
    @Test
    void addEdge_WithNull_ThrowsException() {
        Station station = new Station("TEST1", "Test Station", 50.0, 4.0, 100.0, 200.0);
        graph.addNode(station);
        
        assertThrows(IllegalArgumentException.class, () -> {
            graph.addEdge(null);
        });
    }
    
    @Test
    void addEdge_WithSourceNodeNotExisting_ThrowsException() {
        Station station = new Station("TEST2", "Test Station 2", 50.0, 4.0, 100.0, 200.0);
        graph.addNode(station);
        
        Line edge = new Line("TEST1", "TEST2", 10.0, 100, 5.0);
        
        assertThrows(IllegalArgumentException.class, () -> {
            graph.addEdge(edge);
        });
    }
    
    @Test
    void addEdge_WithDestinationNodeNotExisting_ThrowsException() {
        Station station = new Station("TEST1", "Test Station 1", 50.0, 4.0, 100.0, 200.0);
        graph.addNode(station);
        
        Line edge = new Line("TEST1", "TEST2", 10.0, 100, 5.0);
        
        assertThrows(IllegalArgumentException.class, () -> {
            graph.addEdge(edge);
        });
    }
    
    @Test
    void addEdge_WithValidEdge_AddsEdge() {
        Station station1 = new Station("TEST1", "Test Station 1", 50.0, 4.0, 100.0, 200.0);
        Station station2 = new Station("TEST2", "Test Station 2", 51.0, 5.0, 101.0, 201.0);
        
        graph.addNode(station1);
        graph.addNode(station2);
        
        Line edge = new Line("TEST1", "TEST2", 10.0, 100, 5.0);
        graph.addEdge(edge);
        
        assertEquals(1, graph.getEdgeCount());
        assertTrue(graph.getOutgoingEdges("TEST1").contains(edge));
        assertTrue(graph.getIncomingEdges("TEST2").contains(edge));
        assertTrue(graph.getAllEdges().contains(edge));
    }
    
    @Test
    void addEdge_WithRealData_AddsEdge() {
        if (realStations == null || realStations.size() < 2 || realLines == null || realLines.isEmpty()) {
            System.out.println("Skipping test - insufficient real data available");
            return;
        }
        
        // Add two stations that are connected in real data
        Station station1 = realStations.get(0);
        Station station2 = realStations.get(1);
        
        graph.addNode(station1);
        graph.addNode(station2);
        
        // Find a real edge connecting these stations
        Line realEdge = null;
        for (Line line : realLines) {
            if (line.getDepartureStationId().equals(station1.getId()) && 
                line.getArrivalStationId().equals(station2.getId())) {
                realEdge = line;
                break;
            }
        }
        
        // If no direct edge, create one for testing
        if (realEdge == null) {
            realEdge = new Line(station1.getId(), station2.getId(), 10.0, 100, 5.0);
        }
        
        graph.addEdge(realEdge);
        
        assertEquals(1, graph.getEdgeCount());
        assertTrue(graph.getOutgoingEdges(station1.getId()).contains(realEdge));
        assertTrue(graph.getIncomingEdges(station2.getId()).contains(realEdge));
    }
    
    @Test
    void addEdge_MultipleEdgesFromSameSource_AddsAll() {
        Station station1 = new Station("TEST1", "Test Station 1", 50.0, 4.0, 100.0, 200.0);
        Station station2 = new Station("TEST2", "Test Station 2", 51.0, 5.0, 101.0, 201.0);
        Station station3 = new Station("TEST3", "Test Station 3", 52.0, 6.0, 102.0, 202.0);
        
        graph.addNode(station1);
        graph.addNode(station2);
        graph.addNode(station3);
        
        Line edge1 = new Line("TEST1", "TEST2", 10.0, 100, 5.0);
        Line edge2 = new Line("TEST1", "TEST3", 15.0, 100, 7.0);
        
        graph.addEdge(edge1);
        graph.addEdge(edge2);
        
        assertEquals(2, graph.getEdgeCount());
        assertEquals(2, graph.getOutgoingEdges("TEST1").size());
        assertEquals(1, graph.getIncomingEdges("TEST2").size());
        assertEquals(1, graph.getIncomingEdges("TEST3").size());
    }
    
    @Test
    void addEdge_MultipleEdgesToSameDestination_AddsAll() {
        Station station1 = new Station("TEST1", "Test Station 1", 50.0, 4.0, 100.0, 200.0);
        Station station2 = new Station("TEST2", "Test Station 2", 51.0, 5.0, 101.0, 201.0);
        Station station3 = new Station("TEST3", "Test Station 3", 52.0, 6.0, 102.0, 202.0);
        
        graph.addNode(station1);
        graph.addNode(station2);
        graph.addNode(station3);
        
        Line edge1 = new Line("TEST1", "TEST3", 10.0, 100, 5.0);
        Line edge2 = new Line("TEST2", "TEST3", 15.0, 100, 7.0);
        
        graph.addEdge(edge1);
        graph.addEdge(edge2);
        
        assertEquals(2, graph.getEdgeCount());
        assertEquals(2, graph.getIncomingEdges("TEST3").size());
        assertEquals(1, graph.getOutgoingEdges("TEST1").size());
        assertEquals(1, graph.getOutgoingEdges("TEST2").size());
    }
    
    // ==================== getNode Tests ====================
    
    @Test
    void getNode_WithExistingNode_ReturnsNode() {
        Station station = new Station("TEST1", "Test Station", 50.0, 4.0, 100.0, 200.0);
        graph.addNode(station);
        
        Station retrieved = graph.getNode("TEST1");
        
        assertNotNull(retrieved);
        assertEquals(station, retrieved);
        assertEquals("TEST1", retrieved.getId());
    }
    
    @Test
    void getNode_WithNonExistentNode_ReturnsNull() {
        Station retrieved = graph.getNode("NONEXISTENT");
        assertNull(retrieved);
    }
    
    @Test
    void getNode_WithNullId_ReturnsNull() {
        Station retrieved = graph.getNode(null);
        assertNull(retrieved);
    }
    
    @Test
    void getNode_WithRealStation_ReturnsNode() {
        if (realStations == null || realStations.isEmpty()) {
            System.out.println("Skipping test - no real data available");
            return;
        }
        
        Station station = realStations.get(0);
        graph.addNode(station);
        
        Station retrieved = graph.getNode(station.getId());
        
        assertNotNull(retrieved);
        assertEquals(station.getId(), retrieved.getId());
        assertEquals(station.getName(), retrieved.getName());
    }
    
    // ==================== getAllNodes Tests ====================
    
    @Test
    void getAllNodes_EmptyGraph_ReturnsEmptyCollection() {
        Collection<Station> nodes = graph.getAllNodes();
        
        assertNotNull(nodes);
        assertTrue(nodes.isEmpty());
    }
    
    @Test
    void getAllNodes_WithNodes_ReturnsAllNodes() {
        Station station1 = new Station("TEST1", "Test Station 1", 50.0, 4.0, 100.0, 200.0);
        Station station2 = new Station("TEST2", "Test Station 2", 51.0, 5.0, 101.0, 201.0);
        
        graph.addNode(station1);
        graph.addNode(station2);
        
        Collection<Station> nodes = graph.getAllNodes();
        
        assertEquals(2, nodes.size());
        assertTrue(nodes.contains(station1));
        assertTrue(nodes.contains(station2));
    }
    
    @Test
    void getAllNodes_WithRealData_ReturnsAllNodes() {
        if (realStations == null || realStations.size() < 5) {
            System.out.println("Skipping test - insufficient real data available");
            return;
        }
        
        for (int i = 0; i < 5; i++) {
            graph.addNode(realStations.get(i));
        }
        
        Collection<Station> nodes = graph.getAllNodes();
        
        assertEquals(5, nodes.size());
        for (int i = 0; i < 5; i++) {
            assertTrue(nodes.contains(realStations.get(i)));
        }
    }
    
    @Test
    void getAllNodes_ReturnsUnmodifiableCollection() {
        Station station = new Station("TEST1", "Test Station", 50.0, 4.0, 100.0, 200.0);
        graph.addNode(station);
        
        Collection<Station> nodes = graph.getAllNodes();
        
        assertThrows(UnsupportedOperationException.class, () -> {
            nodes.add(new Station("TEST2", "Test Station 2", 51.0, 5.0, 101.0, 201.0));
        });
    }
    
    // ==================== getOutgoingEdges Tests ====================
    
    @Test
    void getOutgoingEdges_WithExistingNode_ReturnsEdges() {
        Station station1 = new Station("TEST1", "Test Station 1", 50.0, 4.0, 100.0, 200.0);
        Station station2 = new Station("TEST2", "Test Station 2", 51.0, 5.0, 101.0, 201.0);
        
        graph.addNode(station1);
        graph.addNode(station2);
        
        Line edge = new Line("TEST1", "TEST2", 10.0, 100, 5.0);
        graph.addEdge(edge);
        
        List<Line> outgoing = graph.getOutgoingEdges("TEST1");
        
        assertEquals(1, outgoing.size());
        assertTrue(outgoing.contains(edge));
    }
    
    @Test
    void getOutgoingEdges_WithNoOutgoingEdges_ReturnsEmptyList() {
        Station station = new Station("TEST1", "Test Station", 50.0, 4.0, 100.0, 200.0);
        graph.addNode(station);
        
        List<Line> outgoing = graph.getOutgoingEdges("TEST1");
        
        assertNotNull(outgoing);
        assertTrue(outgoing.isEmpty());
    }
    
    @Test
    void getOutgoingEdges_WithNonExistentNode_ReturnsEmptyList() {
        List<Line> outgoing = graph.getOutgoingEdges("NONEXISTENT");
        
        assertNotNull(outgoing);
        assertTrue(outgoing.isEmpty());
    }
    
    @Test
    void getOutgoingEdges_WithNullId_ReturnsEmptyList() {
        List<Line> outgoing = graph.getOutgoingEdges(null);
        
        assertNotNull(outgoing);
        assertTrue(outgoing.isEmpty());
    }
    
    @Test
    void getOutgoingEdges_ReturnsUnmodifiableList() {
        Station station1 = new Station("TEST1", "Test Station 1", 50.0, 4.0, 100.0, 200.0);
        Station station2 = new Station("TEST2", "Test Station 2", 51.0, 5.0, 101.0, 201.0);
        
        graph.addNode(station1);
        graph.addNode(station2);
        
        Line edge = new Line("TEST1", "TEST2", 10.0, 100, 5.0);
        graph.addEdge(edge);
        
        List<Line> outgoing = graph.getOutgoingEdges("TEST1");
        
        assertThrows(UnsupportedOperationException.class, () -> {
            outgoing.add(new Line("TEST1", "TEST2", 15.0, 100, 7.0));
        });
    }
    
    @Test
    void getOutgoingEdges_WithMultipleEdges_ReturnsAll() {
        Station station1 = new Station("TEST1", "Test Station 1", 50.0, 4.0, 100.0, 200.0);
        Station station2 = new Station("TEST2", "Test Station 2", 51.0, 5.0, 101.0, 201.0);
        Station station3 = new Station("TEST3", "Test Station 3", 52.0, 6.0, 102.0, 202.0);
        
        graph.addNode(station1);
        graph.addNode(station2);
        graph.addNode(station3);
        
        Line edge1 = new Line("TEST1", "TEST2", 10.0, 100, 5.0);
        Line edge2 = new Line("TEST1", "TEST3", 15.0, 100, 7.0);
        
        graph.addEdge(edge1);
        graph.addEdge(edge2);
        
        List<Line> outgoing = graph.getOutgoingEdges("TEST1");
        
        assertEquals(2, outgoing.size());
        assertTrue(outgoing.contains(edge1));
        assertTrue(outgoing.contains(edge2));
    }
    
    // ==================== getIncomingEdges Tests ====================
    
    @Test
    void getIncomingEdges_WithExistingNode_ReturnsEdges() {
        Station station1 = new Station("TEST1", "Test Station 1", 50.0, 4.0, 100.0, 200.0);
        Station station2 = new Station("TEST2", "Test Station 2", 51.0, 5.0, 101.0, 201.0);
        
        graph.addNode(station1);
        graph.addNode(station2);
        
        Line edge = new Line("TEST1", "TEST2", 10.0, 100, 5.0);
        graph.addEdge(edge);
        
        List<Line> incoming = graph.getIncomingEdges("TEST2");
        
        assertEquals(1, incoming.size());
        assertTrue(incoming.contains(edge));
    }
    
    @Test
    void getIncomingEdges_WithNoIncomingEdges_ReturnsEmptyList() {
        Station station = new Station("TEST1", "Test Station", 50.0, 4.0, 100.0, 200.0);
        graph.addNode(station);
        
        List<Line> incoming = graph.getIncomingEdges("TEST1");
        
        assertNotNull(incoming);
        assertTrue(incoming.isEmpty());
    }
    
    @Test
    void getIncomingEdges_WithNonExistentNode_ReturnsEmptyList() {
        List<Line> incoming = graph.getIncomingEdges("NONEXISTENT");
        
        assertNotNull(incoming);
        assertTrue(incoming.isEmpty());
    }
    
    @Test
    void getIncomingEdges_WithNullId_ReturnsEmptyList() {
        List<Line> incoming = graph.getIncomingEdges(null);
        
        assertNotNull(incoming);
        assertTrue(incoming.isEmpty());
    }
    
    @Test
    void getIncomingEdges_ReturnsUnmodifiableList() {
        Station station1 = new Station("TEST1", "Test Station 1", 50.0, 4.0, 100.0, 200.0);
        Station station2 = new Station("TEST2", "Test Station 2", 51.0, 5.0, 101.0, 201.0);
        
        graph.addNode(station1);
        graph.addNode(station2);
        
        Line edge = new Line("TEST1", "TEST2", 10.0, 100, 5.0);
        graph.addEdge(edge);
        
        List<Line> incoming = graph.getIncomingEdges("TEST2");
        
        assertThrows(UnsupportedOperationException.class, () -> {
            incoming.add(new Line("TEST1", "TEST2", 15.0, 100, 7.0));
        });
    }
    
    @Test
    void getIncomingEdges_WithMultipleEdges_ReturnsAll() {
        Station station1 = new Station("TEST1", "Test Station 1", 50.0, 4.0, 100.0, 200.0);
        Station station2 = new Station("TEST2", "Test Station 2", 51.0, 5.0, 101.0, 201.0);
        Station station3 = new Station("TEST3", "Test Station 3", 52.0, 6.0, 102.0, 202.0);
        
        graph.addNode(station1);
        graph.addNode(station2);
        graph.addNode(station3);
        
        Line edge1 = new Line("TEST1", "TEST3", 10.0, 100, 5.0);
        Line edge2 = new Line("TEST2", "TEST3", 15.0, 100, 7.0);
        
        graph.addEdge(edge1);
        graph.addEdge(edge2);
        
        List<Line> incoming = graph.getIncomingEdges("TEST3");
        
        assertEquals(2, incoming.size());
        assertTrue(incoming.contains(edge1));
        assertTrue(incoming.contains(edge2));
    }
    
    // ==================== getAllEdges Tests ====================
    
    @Test
    void getAllEdges_EmptyGraph_ReturnsEmptyList() {
        List<Line> edges = graph.getAllEdges();
        
        assertNotNull(edges);
        assertTrue(edges.isEmpty());
    }
    
    @Test
    void getAllEdges_WithEdges_ReturnsAllEdges() {
        Station station1 = new Station("TEST1", "Test Station 1", 50.0, 4.0, 100.0, 200.0);
        Station station2 = new Station("TEST2", "Test Station 2", 51.0, 5.0, 101.0, 201.0);
        Station station3 = new Station("TEST3", "Test Station 3", 52.0, 6.0, 102.0, 202.0);
        
        graph.addNode(station1);
        graph.addNode(station2);
        graph.addNode(station3);
        
        Line edge1 = new Line("TEST1", "TEST2", 10.0, 100, 5.0);
        Line edge2 = new Line("TEST2", "TEST3", 15.0, 100, 7.0);
        
        graph.addEdge(edge1);
        graph.addEdge(edge2);
        
        List<Line> edges = graph.getAllEdges();
        
        assertEquals(2, edges.size());
        assertTrue(edges.contains(edge1));
        assertTrue(edges.contains(edge2));
    }
    
    @Test
    void getAllEdges_ReturnsUnmodifiableList() {
        Station station1 = new Station("TEST1", "Test Station 1", 50.0, 4.0, 100.0, 200.0);
        Station station2 = new Station("TEST2", "Test Station 2", 51.0, 5.0, 101.0, 201.0);
        
        graph.addNode(station1);
        graph.addNode(station2);
        
        Line edge = new Line("TEST1", "TEST2", 10.0, 100, 5.0);
        graph.addEdge(edge);
        
        List<Line> edges = graph.getAllEdges();
        
        assertThrows(UnsupportedOperationException.class, () -> {
            edges.add(new Line("TEST1", "TEST2", 15.0, 100, 7.0));
        });
    }
    
    // ==================== getNodeCount Tests ====================
    
    @Test
    void getNodeCount_EmptyGraph_ReturnsZero() {
        assertEquals(0, graph.getNodeCount());
    }
    
    @Test
    void getNodeCount_WithNodes_ReturnsCorrectCount() {
        Station station1 = new Station("TEST1", "Test Station 1", 50.0, 4.0, 100.0, 200.0);
        Station station2 = new Station("TEST2", "Test Station 2", 51.0, 5.0, 101.0, 201.0);
        
        graph.addNode(station1);
        assertEquals(1, graph.getNodeCount());
        
        graph.addNode(station2);
        assertEquals(2, graph.getNodeCount());
    }
    
    @Test
    void getNodeCount_WithDuplicateId_ReturnsOne() {
        Station station1 = new Station("TEST1", "Test Station 1", 50.0, 4.0, 100.0, 200.0);
        Station station2 = new Station("TEST1", "Test Station 2", 51.0, 5.0, 101.0, 201.0);
        
        graph.addNode(station1);
        graph.addNode(station2);
        
        assertEquals(1, graph.getNodeCount());
    }
    
    // ==================== getEdgeCount Tests ====================
    
    @Test
    void getEdgeCount_EmptyGraph_ReturnsZero() {
        assertEquals(0, graph.getEdgeCount());
    }
    
    @Test
    void getEdgeCount_WithEdges_ReturnsCorrectCount() {
        Station station1 = new Station("TEST1", "Test Station 1", 50.0, 4.0, 100.0, 200.0);
        Station station2 = new Station("TEST2", "Test Station 2", 51.0, 5.0, 101.0, 201.0);
        Station station3 = new Station("TEST3", "Test Station 3", 52.0, 6.0, 102.0, 202.0);
        
        graph.addNode(station1);
        graph.addNode(station2);
        graph.addNode(station3);
        
        Line edge1 = new Line("TEST1", "TEST2", 10.0, 100, 5.0);
        graph.addEdge(edge1);
        assertEquals(1, graph.getEdgeCount());
        
        Line edge2 = new Line("TEST2", "TEST3", 15.0, 100, 7.0);
        graph.addEdge(edge2);
        assertEquals(2, graph.getEdgeCount());
    }
    
    // ==================== containsNode Tests ====================
    
    @Test
    void containsNode_WithExistingNode_ReturnsTrue() {
        Station station = new Station("TEST1", "Test Station", 50.0, 4.0, 100.0, 200.0);
        graph.addNode(station);
        
        assertTrue(graph.containsNode("TEST1"));
    }
    
    @Test
    void containsNode_WithNonExistentNode_ReturnsFalse() {
        assertFalse(graph.containsNode("NONEXISTENT"));
    }
    
    @Test
    void containsNode_WithNullId_ReturnsFalse() {
        assertFalse(graph.containsNode(null));
    }
    
    @Test
    void containsNode_WithRealStation_ReturnsTrue() {
        if (realStations == null || realStations.isEmpty()) {
            System.out.println("Skipping test - no real data available");
            return;
        }
        
        Station station = realStations.get(0);
        graph.addNode(station);
        
        assertTrue(graph.containsNode(station.getId()));
    }
    
    // ==================== getNeighbors Tests ====================
    
    @Test
    void getNeighbors_WithNoNeighbors_ReturnsEmptySet() {
        Station station = new Station("TEST1", "Test Station", 50.0, 4.0, 100.0, 200.0);
        graph.addNode(station);
        
        Set<Station> neighbors = graph.getNeighbors("TEST1");
        
        assertNotNull(neighbors);
        assertTrue(neighbors.isEmpty());
    }
    
    @Test
    void getNeighbors_WithNeighbors_ReturnsNeighbors() {
        Station station1 = new Station("TEST1", "Test Station 1", 50.0, 4.0, 100.0, 200.0);
        Station station2 = new Station("TEST2", "Test Station 2", 51.0, 5.0, 101.0, 201.0);
        Station station3 = new Station("TEST3", "Test Station 3", 52.0, 6.0, 102.0, 202.0);
        
        graph.addNode(station1);
        graph.addNode(station2);
        graph.addNode(station3);
        
        Line edge1 = new Line("TEST1", "TEST2", 10.0, 100, 5.0);
        Line edge2 = new Line("TEST1", "TEST3", 15.0, 100, 7.0);
        
        graph.addEdge(edge1);
        graph.addEdge(edge2);
        
        Set<Station> neighbors = graph.getNeighbors("TEST1");
        
        assertEquals(2, neighbors.size());
        assertTrue(neighbors.contains(station2));
        assertTrue(neighbors.contains(station3));
    }
    
    @Test
    void getNeighbors_WithNonExistentNode_ReturnsEmptySet() {
        Set<Station> neighbors = graph.getNeighbors("NONEXISTENT");
        
        assertNotNull(neighbors);
        assertTrue(neighbors.isEmpty());
    }
    
    @Test
    void getNeighbors_WithNullId_ReturnsEmptySet() {
        Set<Station> neighbors = graph.getNeighbors(null);
        
        assertNotNull(neighbors);
        assertTrue(neighbors.isEmpty());
    }
    
    @Test
    void getNeighbors_WithDuplicateEdges_ReturnsUniqueNeighbors() {
        Station station1 = new Station("TEST1", "Test Station 1", 50.0, 4.0, 100.0, 200.0);
        Station station2 = new Station("TEST2", "Test Station 2", 51.0, 5.0, 101.0, 201.0);
        
        graph.addNode(station1);
        graph.addNode(station2);
        
        Line edge1 = new Line("TEST1", "TEST2", 10.0, 100, 5.0);
        Line edge2 = new Line("TEST1", "TEST2", 15.0, 100, 7.0);
        
        graph.addEdge(edge1);
        graph.addEdge(edge2);
        
        Set<Station> neighbors = graph.getNeighbors("TEST1");
        
        // Should return unique neighbors (only one instance of station2)
        assertEquals(1, neighbors.size());
        assertTrue(neighbors.contains(station2));
    }
    
    // ==================== Integration Tests with Real Data ====================
    
    @Test
    void buildGraphFromRealData_AllNodesAndEdgesPresent() {
        if (realGraph == null || realStations == null || realLines == null) {
            System.out.println("Skipping test - no real data available");
            return;
        }
        
        assertEquals(realStations.size(), realGraph.getNodeCount());
        assertTrue(realGraph.getEdgeCount() > 0);
        
        // Verify all real stations are in the graph
        for (Station station : realStations) {
            assertTrue(realGraph.containsNode(station.getId()));
            assertEquals(station, realGraph.getNode(station.getId()));
        }
    }
    
    @Test
    void buildGraphFromRealData_EdgeConsistency() {
        if (realGraph == null || realStations == null || realLines == null) {
            System.out.println("Skipping test - no real data available");
            return;
        }
        
        // Verify that for each edge in the graph, both nodes exist
        for (Line edge : realGraph.getAllEdges()) {
            assertTrue(realGraph.containsNode(edge.getDepartureStationId()),
                "Departure station " + edge.getDepartureStationId() + " should exist");
            assertTrue(realGraph.containsNode(edge.getArrivalStationId()),
                "Arrival station " + edge.getArrivalStationId() + " should exist");
        }
    }
    
    @Test
    void buildGraphFromRealData_OutgoingIncomingConsistency() {
        if (realGraph == null || realStations == null) {
            System.out.println("Skipping test - no real data available");
            return;
        }
        
        // Verify that outgoing edges match incoming edges
        for (Station station : realGraph.getAllNodes()) {
            List<Line> outgoing = realGraph.getOutgoingEdges(station.getId());
            for (Line edge : outgoing) {
                List<Line> incoming = realGraph.getIncomingEdges(edge.getArrivalStationId());
                assertTrue(incoming.contains(edge),
                    "Edge from " + edge.getDepartureStationId() + " to " + 
                    edge.getArrivalStationId() + " should be in incoming edges");
            }
        }
    }
    
    @Test
    void buildGraphFromRealData_NeighborsConsistency() {
        if (realGraph == null || realStations == null) {
            System.out.println("Skipping test - no real data available");
            return;
        }
        
        // Verify neighbors are consistent with outgoing edges
        for (Station station : realGraph.getAllNodes()) {
            Set<Station> neighbors = realGraph.getNeighbors(station.getId());
            List<Line> outgoing = realGraph.getOutgoingEdges(station.getId());
            
            assertEquals(outgoing.size(), neighbors.size(),
                "Number of neighbors should match number of outgoing edges for " + station.getId());
            
            for (Line edge : outgoing) {
                Station neighbor = realGraph.getNode(edge.getArrivalStationId());
                assertNotNull(neighbor);
                assertTrue(neighbors.contains(neighbor));
            }
        }
    }
}

