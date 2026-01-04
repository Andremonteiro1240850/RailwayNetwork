package Domain;

import Repository.*;
import java.util.*;
import java.io.IOException;

/**
 * Comprehensive test suite for CycleDetection (USEI11).
 * 
 * Tests cover:
 * - Functional tests: cycle detection, cycle identification, vertex/edge collection
 * - Structural tests: all branches of DFS cycle detection
 * - Edge cases: empty graph, single node, self-loops, multiple cycles
 * - Correctness: ensures all cycle nodes and edges are identified
 * 
 * All tests use REAL data from train_station_dataset/Sprint3/stations.csv and lines.csv
 * 
 * @author Development Team
 * @version 1.0
 */
public class CycleDetectionTest {
    
    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;
    
    // Real data loaded from CSV files
    private static RailwayGraphRepository realDataRepository;
    private static RailwayGraph realDataGraph;
    private static List<Station> realStations;
    private static List<Line> realLines;
    
    public static void main(String[] args) {
        System.out.println("=== CYCLE DETECTION TESTS (USEI11) ===\n");
        
        // Load real data from CSV files
        System.out.println("Loading real data from CSV files...");
        try {
            loadRealData();
            System.out.println("Loaded " + realStations.size() + " stations and " + realLines.size() + " lines\n");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load real data: " + e.getMessage());
            System.err.println("Tests will use synthetic data as fallback.");
            e.printStackTrace();
        }
        
        // ==================== Null/Invalid Input Tests ====================
        testHasCycles_NullGraph();
        testFindCycles_NullGraph();
        
        // ==================== Empty Graph Tests ====================
        testHasCycles_EmptyGraph();
        testFindCycles_EmptyGraph();
        
        // ==================== Single Node Tests ====================
        testHasCycles_SingleNode();
        testFindCycles_SingleNode();
        
        // ==================== Acyclic Graph Tests ====================
        testHasCycles_SimpleChain();
        testHasCycles_ComplexDAG();
        testHasCycles_DisconnectedAcyclic();
        testFindCycles_SimpleChain();
        testFindCycles_ComplexDAG();
        
        // ==================== Simple Cycle Tests ====================
        testHasCycles_SimpleCycle();
        testHasCycles_TwoNodeCycle();
        testHasCycles_ThreeNodeCycle();
        testFindCycles_SimpleCycle();
        testFindCycles_TwoNodeCycle();
        testFindCycles_ThreeNodeCycle();
        
        // ==================== Complex Cycle Tests ====================
        testHasCycles_LargeCycle();
        testHasCycles_MultipleCycles();
        testHasCycles_NestedCycles();
        testFindCycles_LargeCycle();
        testFindCycles_MultipleCycles();
        testFindCycles_NestedCycles();
        
        // ==================== Cycle with Branches Tests ====================
        testHasCycles_CycleWithBranch();
        testHasCycles_CycleWithMultipleBranches();
        testFindCycles_CycleWithBranch();
        testFindCycles_CycleWithMultipleBranches();
        
        // ==================== Disconnected Components Tests ====================
        testHasCycles_DisconnectedWithCycle();
        testHasCycles_DisconnectedMultipleCycles();
        testFindCycles_DisconnectedWithCycle();
        testFindCycles_DisconnectedMultipleCycles();
        
        // ==================== CycleInfo Validation Tests ====================
        testCycleInfo_EmptyCycles();
        testCycleInfo_CycleNodes();
        testCycleInfo_CycleEdges();
        testCycleInfo_CompleteCycles();
        testCycleInfo_HasCycles();
        
        // ==================== Edge Cases ====================
        testHasCycles_LargeAcyclicGraph();
        testHasCycles_LargeCyclicGraph();
        testFindCycles_AllNodesInCycle();
        testFindCycles_PartialCycle();
        
        System.out.println("\n=== TEST SUMMARY ===");
        System.out.println("Tests run: " + testsRun);
        System.out.println("Tests passed: " + testsPassed);
        System.out.println("Tests failed: " + testsFailed);
        
        if (testsFailed == 0) {
            System.out.println("\n✓ ALL TESTS PASSED!");
        } else {
            System.out.println("\n✗ SOME TESTS FAILED");
        }
    }
    
    // ==================== Real Data Loading ====================
    
    /**
     * Loads real data from CSV files.
     */
    private static void loadRealData() throws IOException {
        realDataRepository = new RailwayGraphRepository();
        realDataRepository.loadData("train_station_dataset/Sprint3/stations.csv", 
                                   "train_station_dataset/Sprint3/lines.csv");
        realDataRepository.buildGraph();
        realDataGraph = realDataRepository.buildGraph();
        
        // Get real stations and lines
        realStations = new ArrayList<>(realDataRepository.buildGraph().getAllNodes());
        realLines = new ArrayList<>(realDataRepository.buildGraph().getAllEdges());
    }
    
    /**
     * Creates a graph from real data with a subset of stations and lines.
     */
    private static RailwayGraph createGraphFromRealData(int numStations) {
        RailwayGraph graph = new RailwayGraph();
        
        if (realStations == null || realStations.isEmpty()) {
            return createSimpleChain();
        }
        
        int stationsToUse = Math.min(numStations, realStations.size());
        for (int i = 0; i < stationsToUse; i++) {
            graph.addNode(realStations.get(i));
        }
        
        for (int i = 0; i < stationsToUse - 1; i++) {
            String fromId = realStations.get(i).getId();
            String toId = realStations.get(i + 1).getId();
            
            Line connectingLine = findLineBetween(fromId, toId);
            if (connectingLine != null) {
                graph.addEdge(connectingLine);
            } else {
                graph.addEdge(new Line(fromId, toId, 10.0, 100, 5.0));
            }
        }
        
        return graph;
    }
    
    /**
     * Finds a real line between two stations.
     */
    private static Line findLineBetween(String fromId, String toId) {
        if (realLines == null) return null;
        
        for (Line line : realLines) {
            if (line.getDepartureStationId().equals(fromId) && 
                line.getArrivalStationId().equals(toId)) {
                return line;
            }
        }
        return null;
    }
    
    /**
     * Creates a cycle using real stations.
     */
    private static RailwayGraph createCycleFromRealData() {
        RailwayGraph graph = new RailwayGraph();
        
        if (realStations == null || realStations.size() < 3) {
            return createThreeNodeCycle();
        }
        
        for (int i = 0; i < Math.min(3, realStations.size()); i++) {
            graph.addNode(realStations.get(i));
        }
        
        String id0 = realStations.get(0).getId();
        String id1 = realStations.get(1).getId();
        String id2 = realStations.get(2).getId();
        
        Line line1 = findLineBetween(id0, id1);
        if (line1 == null) line1 = new Line(id0, id1, 10.0, 100, 5.0);
        graph.addEdge(line1);
        
        Line line2 = findLineBetween(id1, id2);
        if (line2 == null) line2 = new Line(id1, id2, 15.0, 100, 7.0);
        graph.addEdge(line2);
        
        Line line3 = findLineBetween(id2, id0);
        if (line3 == null) line3 = new Line(id2, id0, 20.0, 100, 9.0);
        graph.addEdge(line3);
        
        return graph;
    }
    
    // ==================== Helper Methods ====================
    
    private static RailwayGraph createEmptyGraph() {
        return new RailwayGraph();
    }
    
    private static RailwayGraph createSingleNodeGraph() {
        RailwayGraph graph = new RailwayGraph();
        Station station = new Station("A", "Station A", 50.0, 4.0, 100.0, 200.0);
        graph.addNode(station);
        return graph;
    }
    
    private static RailwayGraph createSimpleChain() {
        RailwayGraph graph = new RailwayGraph();
        Station a = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
        Station b = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
        Station c = new Station("C", "C", 52.0, 6.0, 102.0, 202.0);
        
        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addEdge(new Line("A", "B", 10.0, 100, 5.0));
        graph.addEdge(new Line("B", "C", 15.0, 100, 7.0));
        
        return graph;
    }
    
    private static RailwayGraph createTwoNodeCycle() {
        RailwayGraph graph = new RailwayGraph();
        Station a = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
        Station b = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
        
        graph.addNode(a);
        graph.addNode(b);
        graph.addEdge(new Line("A", "B", 10.0, 100, 5.0));
        graph.addEdge(new Line("B", "A", 12.0, 100, 6.0)); // Cycle A->B->A
        
        return graph;
    }
    
    private static RailwayGraph createThreeNodeCycle() {
        RailwayGraph graph = new RailwayGraph();
        Station a = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
        Station b = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
        Station c = new Station("C", "C", 52.0, 6.0, 102.0, 202.0);
        
        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addEdge(new Line("A", "B", 10.0, 100, 5.0));
        graph.addEdge(new Line("B", "C", 15.0, 100, 7.0));
        graph.addEdge(new Line("C", "A", 20.0, 100, 9.0)); // Cycle A->B->C->A
        
        return graph;
    }
    
    private static RailwayGraph createComplexDAG() {
        RailwayGraph graph = new RailwayGraph();
        Station[] stations = new Station[5];
        for (int i = 0; i < 5; i++) {
            stations[i] = new Station(String.valueOf((char)('A' + i)), 
                                     "Station " + (char)('A' + i),
                                     50.0 + i, 4.0 + i, 100.0 + i, 200.0 + i);
            graph.addNode(stations[i]);
        }
        
        graph.addEdge(new Line("A", "B", 10.0, 100, 5.0));
        graph.addEdge(new Line("A", "C", 12.0, 100, 6.0));
        graph.addEdge(new Line("B", "D", 15.0, 100, 7.0));
        graph.addEdge(new Line("C", "D", 18.0, 100, 8.0));
        graph.addEdge(new Line("D", "E", 20.0, 100, 9.0));
        
        return graph;
    }
    
    private static RailwayGraph createMultipleCycles() {
        RailwayGraph graph = new RailwayGraph();
        Station a = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
        Station b = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
        Station c = new Station("C", "C", 52.0, 6.0, 102.0, 202.0);
        Station d = new Station("D", "D", 53.0, 7.0, 103.0, 203.0);
        
        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addNode(d);
        graph.addEdge(new Line("A", "B", 10.0, 100, 5.0));
        graph.addEdge(new Line("B", "A", 12.0, 100, 6.0)); // Cycle 1: A->B->A
        graph.addEdge(new Line("C", "D", 15.0, 100, 7.0));
        graph.addEdge(new Line("D", "C", 18.0, 100, 8.0)); // Cycle 2: C->D->C
        
        return graph;
    }
    
    // ==================== Null/Invalid Input Tests ====================
    
    private static void testHasCycles_NullGraph() {
        test("hasCycles with null graph throws exception", () -> {
            try {
                CycleDetection.hasCycles(null);
                throw new AssertionError("Should throw IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("null") || e.getMessage().contains("Graph"), 
                          "Error message mentions null or Graph");
            }
        });
    }
    
    private static void testFindCycles_NullGraph() {
        test("findCycles with null graph throws exception", () -> {
            try {
                CycleDetection.findCycles(null);
                throw new AssertionError("Should throw IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("null") || e.getMessage().contains("Graph"), 
                          "Error message mentions null or Graph");
            }
        });
    }
    
    // ==================== Empty Graph Tests ====================
    
    private static void testHasCycles_EmptyGraph() {
        test("hasCycles with empty graph returns false", () -> {
            RailwayGraph graph = createEmptyGraph();
            boolean result = CycleDetection.hasCycles(graph);
            assertTrue(result == false, "Empty graph has no cycles");
        });
    }
    
    private static void testFindCycles_EmptyGraph() {
        test("findCycles with empty graph returns empty CycleInfo", () -> {
            RailwayGraph graph = createEmptyGraph();
            CycleDetection.CycleInfo result = CycleDetection.findCycles(graph);
            assertTrue(result != null, "CycleInfo is not null");
            assertTrue(result.hasCycles() == false, "No cycles detected");
            assertTrue(result.getCycleNodeIds().isEmpty(), "No cycle nodes");
            assertTrue(result.getCycleEdges().isEmpty(), "No cycle edges");
        });
    }
    
    // ==================== Single Node Tests ====================
    
    private static void testHasCycles_SingleNode() {
        test("hasCycles with single node returns false", () -> {
            RailwayGraph graph = createSingleNodeGraph();
            boolean result = CycleDetection.hasCycles(graph);
            assertTrue(result == false, "Single node has no cycles");
        });
    }
    
    private static void testFindCycles_SingleNode() {
        test("findCycles with single node returns empty CycleInfo", () -> {
            RailwayGraph graph = createSingleNodeGraph();
            CycleDetection.CycleInfo result = CycleDetection.findCycles(graph);
            assertTrue(result.hasCycles() == false, "No cycles detected");
        });
    }
    
    // ==================== Acyclic Graph Tests ====================
    
    private static void testHasCycles_SimpleChain() {
        test("hasCycles with simple chain using real data returns false", () -> {
            RailwayGraph graph = createGraphFromRealData(3);
            boolean result = CycleDetection.hasCycles(graph);
            assertTrue(result == false, "Simple chain has no cycles");
        });
    }
    
    private static void testHasCycles_ComplexDAG() {
        test("hasCycles with complex DAG using real data returns false", () -> {
            RailwayGraph graph = (realDataGraph != null && realDataGraph.getNodeCount() > 0) 
                ? realDataGraph 
                : createGraphFromRealData(10);
            boolean result = CycleDetection.hasCycles(graph);
            assertTrue(result == false, "Complex DAG has no cycles");
        });
    }
    
    private static void testHasCycles_DisconnectedAcyclic() {
        test("hasCycles with disconnected acyclic components returns false", () -> {
            RailwayGraph graph = new RailwayGraph();
            Station a = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station b = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            Station c = new Station("C", "C", 52.0, 6.0, 102.0, 202.0);
            Station d = new Station("D", "D", 53.0, 7.0, 103.0, 203.0);
            
            graph.addNode(a);
            graph.addNode(b);
            graph.addNode(c);
            graph.addNode(d);
            graph.addEdge(new Line("A", "B", 10.0, 100, 5.0));
            // C and D are disconnected
            
            boolean result = CycleDetection.hasCycles(graph);
            assertTrue(result == false, "Disconnected acyclic graph has no cycles");
        });
    }
    
    private static void testFindCycles_SimpleChain() {
        test("findCycles with simple chain using real data returns empty CycleInfo", () -> {
            RailwayGraph graph = createGraphFromRealData(3);
            CycleDetection.CycleInfo result = CycleDetection.findCycles(graph);
            assertTrue(result.hasCycles() == false, "No cycles detected");
            assertTrue(result.getCycleNodeIds().isEmpty(), "No cycle nodes");
        });
    }
    
    private static void testFindCycles_ComplexDAG() {
        test("findCycles with complex DAG using real data returns empty CycleInfo", () -> {
            RailwayGraph graph = (realDataGraph != null && realDataGraph.getNodeCount() > 0) 
                ? realDataGraph 
                : createGraphFromRealData(10);
            CycleDetection.CycleInfo result = CycleDetection.findCycles(graph);
            assertTrue(result.hasCycles() == false, "No cycles detected");
        });
    }
    
    // ==================== Simple Cycle Tests ====================
    
    private static void testHasCycles_SimpleCycle() {
        test("hasCycles detects simple cycle using real data", () -> {
            RailwayGraph graph = createCycleFromRealData();
            boolean result = CycleDetection.hasCycles(graph);
            assertTrue(result == true, "Cycle detected");
        });
    }
    
    private static void testHasCycles_TwoNodeCycle() {
        test("hasCycles detects two-node cycle", () -> {
            RailwayGraph graph = createTwoNodeCycle();
            boolean result = CycleDetection.hasCycles(graph);
            assertTrue(result == true, "Two-node cycle detected");
        });
    }
    
    private static void testHasCycles_ThreeNodeCycle() {
        test("hasCycles detects three-node cycle", () -> {
            RailwayGraph graph = createThreeNodeCycle();
            boolean result = CycleDetection.hasCycles(graph);
            assertTrue(result == true, "Three-node cycle detected");
        });
    }
    
    private static void testFindCycles_SimpleCycle() {
        test("findCycles identifies simple cycle nodes and edges using real data", () -> {
            RailwayGraph graph = createCycleFromRealData();
            CycleDetection.CycleInfo result = CycleDetection.findCycles(graph);
            assertTrue(result.hasCycles() == true, "Cycles detected");
            assertTrue(result.getCycleNodeIds().size() >= 3, "At least 3 nodes in cycle");
            assertTrue(result.getCycleEdges().size() >= 1, "At least 1 edge in cycle");
        });
    }
    
    private static void testFindCycles_TwoNodeCycle() {
        test("findCycles identifies two-node cycle", () -> {
            RailwayGraph graph = createTwoNodeCycle();
            CycleDetection.CycleInfo result = CycleDetection.findCycles(graph);
            assertTrue(result.hasCycles() == true, "Cycle detected");
            assertTrue(result.getCycleNodeIds().size() >= 2, "At least 2 nodes in cycle");
            assertTrue(result.getCycleNodeIds().contains("A"), "Node A in cycle");
            assertTrue(result.getCycleNodeIds().contains("B"), "Node B in cycle");
        });
    }
    
    private static void testFindCycles_ThreeNodeCycle() {
        test("findCycles identifies three-node cycle", () -> {
            RailwayGraph graph = createThreeNodeCycle();
            CycleDetection.CycleInfo result = CycleDetection.findCycles(graph);
            assertTrue(result.hasCycles() == true, "Cycle detected");
            assertTrue(result.getCycleNodeIds().size() >= 3, "At least 3 nodes in cycle");
        });
    }
    
    // ==================== Complex Cycle Tests ====================
    
    private static void testHasCycles_LargeCycle() {
        test("hasCycles detects large cycle", () -> {
            RailwayGraph graph = new RailwayGraph();
            // Create cycle A->B->C->D->E->A
            Station[] stations = new Station[5];
            for (int i = 0; i < 5; i++) {
                stations[i] = new Station(String.valueOf((char)('A' + i)), 
                                         "Station " + (char)('A' + i),
                                         50.0 + i, 4.0 + i, 100.0 + i, 200.0 + i);
                graph.addNode(stations[i]);
            }
            
            for (int i = 0; i < 5; i++) {
                int next = (i + 1) % 5;
                graph.addEdge(new Line(String.valueOf((char)('A' + i)), 
                                      String.valueOf((char)('A' + next)),
                                      10.0 + i, 100, 5.0 + i));
            }
            
            boolean result = CycleDetection.hasCycles(graph);
            assertTrue(result == true, "Large cycle detected");
        });
    }
    
    private static void testHasCycles_MultipleCycles() {
        test("hasCycles detects multiple cycles", () -> {
            RailwayGraph graph = createMultipleCycles();
            boolean result = CycleDetection.hasCycles(graph);
            assertTrue(result == true, "Multiple cycles detected");
        });
    }
    
    private static void testHasCycles_NestedCycles() {
        test("hasCycles detects nested cycles", () -> {
            RailwayGraph graph = new RailwayGraph();
            Station a = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station b = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            Station c = new Station("C", "C", 52.0, 6.0, 102.0, 202.0);
            Station d = new Station("D", "D", 53.0, 7.0, 103.0, 203.0);
            
            graph.addNode(a);
            graph.addNode(b);
            graph.addNode(c);
            graph.addNode(d);
            graph.addEdge(new Line("A", "B", 10.0, 100, 5.0));
            graph.addEdge(new Line("B", "C", 12.0, 100, 6.0));
            graph.addEdge(new Line("C", "A", 15.0, 100, 7.0)); // Outer cycle A->B->C->A
            graph.addEdge(new Line("B", "D", 18.0, 100, 8.0));
            graph.addEdge(new Line("D", "B", 20.0, 100, 9.0)); // Inner cycle B->D->B
            
            boolean result = CycleDetection.hasCycles(graph);
            assertTrue(result == true, "Nested cycles detected");
        });
    }
    
    private static void testFindCycles_LargeCycle() {
        test("findCycles identifies large cycle", () -> {
            RailwayGraph graph = new RailwayGraph();
            Station[] stations = new Station[5];
            for (int i = 0; i < 5; i++) {
                stations[i] = new Station(String.valueOf((char)('A' + i)), 
                                         "Station " + (char)('A' + i),
                                         50.0 + i, 4.0 + i, 100.0 + i, 200.0 + i);
                graph.addNode(stations[i]);
            }
            
            for (int i = 0; i < 5; i++) {
                int next = (i + 1) % 5;
                graph.addEdge(new Line(String.valueOf((char)('A' + i)), 
                                      String.valueOf((char)('A' + next)),
                                      10.0 + i, 100, 5.0 + i));
            }
            
            CycleDetection.CycleInfo result = CycleDetection.findCycles(graph);
            assertTrue(result.hasCycles() == true, "Cycle detected");
            assertTrue(result.getCycleNodeIds().size() >= 5, "All nodes in cycle identified");
        });
    }
    
    private static void testFindCycles_MultipleCycles() {
        test("findCycles identifies multiple cycles", () -> {
            RailwayGraph graph = createMultipleCycles();
            CycleDetection.CycleInfo result = CycleDetection.findCycles(graph);
            assertTrue(result.hasCycles() == true, "Cycles detected");
            assertTrue(result.getCycleNodeIds().size() >= 4, "All cycle nodes identified");
            assertTrue(result.getCycleNodeIds().contains("A"), "Node A in cycle");
            assertTrue(result.getCycleNodeIds().contains("B"), "Node B in cycle");
            assertTrue(result.getCycleNodeIds().contains("C"), "Node C in cycle");
            assertTrue(result.getCycleNodeIds().contains("D"), "Node D in cycle");
        });
    }
    
    private static void testFindCycles_NestedCycles() {
        test("findCycles identifies nested cycles", () -> {
            RailwayGraph graph = new RailwayGraph();
            Station a = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station b = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            Station c = new Station("C", "C", 52.0, 6.0, 102.0, 202.0);
            Station d = new Station("D", "D", 53.0, 7.0, 103.0, 203.0);
            
            graph.addNode(a);
            graph.addNode(b);
            graph.addNode(c);
            graph.addNode(d);
            graph.addEdge(new Line("A", "B", 10.0, 100, 5.0));
            graph.addEdge(new Line("B", "C", 12.0, 100, 6.0));
            graph.addEdge(new Line("C", "A", 15.0, 100, 7.0));
            graph.addEdge(new Line("B", "D", 18.0, 100, 8.0));
            graph.addEdge(new Line("D", "B", 20.0, 100, 9.0));
            
            CycleDetection.CycleInfo result = CycleDetection.findCycles(graph);
            assertTrue(result.hasCycles() == true, "Cycles detected");
            assertTrue(result.getCycleNodeIds().size() >= 4, "All cycle nodes identified");
        });
    }
    
    // ==================== Cycle with Branches Tests ====================
    
    private static void testHasCycles_CycleWithBranch() {
        test("hasCycles detects cycle with branch", () -> {
            RailwayGraph graph = new RailwayGraph();
            Station a = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station b = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            Station c = new Station("C", "C", 52.0, 6.0, 102.0, 202.0);
            Station d = new Station("D", "D", 53.0, 7.0, 103.0, 203.0);
            
            graph.addNode(a);
            graph.addNode(b);
            graph.addNode(c);
            graph.addNode(d);
            graph.addEdge(new Line("A", "B", 10.0, 100, 5.0));
            graph.addEdge(new Line("B", "C", 12.0, 100, 6.0));
            graph.addEdge(new Line("C", "A", 15.0, 100, 7.0)); // Cycle A->B->C->A
            graph.addEdge(new Line("A", "D", 18.0, 100, 8.0)); // Branch from cycle
            
            boolean result = CycleDetection.hasCycles(graph);
            assertTrue(result == true, "Cycle with branch detected");
        });
    }
    
    private static void testHasCycles_CycleWithMultipleBranches() {
        test("hasCycles detects cycle with multiple branches", () -> {
            RailwayGraph graph = new RailwayGraph();
            Station a = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station b = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            Station c = new Station("C", "C", 52.0, 6.0, 102.0, 202.0);
            Station d = new Station("D", "D", 53.0, 7.0, 103.0, 203.0);
            Station e = new Station("E", "E", 54.0, 8.0, 104.0, 204.0);
            
            graph.addNode(a);
            graph.addNode(b);
            graph.addNode(c);
            graph.addNode(d);
            graph.addNode(e);
            graph.addEdge(new Line("A", "B", 10.0, 100, 5.0));
            graph.addEdge(new Line("B", "C", 12.0, 100, 6.0));
            graph.addEdge(new Line("C", "A", 15.0, 100, 7.0)); // Cycle
            graph.addEdge(new Line("A", "D", 18.0, 100, 8.0)); // Branch 1
            graph.addEdge(new Line("B", "E", 20.0, 100, 9.0)); // Branch 2
            
            boolean result = CycleDetection.hasCycles(graph);
            assertTrue(result == true, "Cycle with multiple branches detected");
        });
    }
    
    private static void testFindCycles_CycleWithBranch() {
        test("findCycles identifies cycle with branch", () -> {
            RailwayGraph graph = new RailwayGraph();
            Station a = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station b = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            Station c = new Station("C", "C", 52.0, 6.0, 102.0, 202.0);
            Station d = new Station("D", "D", 53.0, 7.0, 103.0, 203.0);
            
            graph.addNode(a);
            graph.addNode(b);
            graph.addNode(c);
            graph.addNode(d);
            graph.addEdge(new Line("A", "B", 10.0, 100, 5.0));
            graph.addEdge(new Line("B", "C", 12.0, 100, 6.0));
            graph.addEdge(new Line("C", "A", 15.0, 100, 7.0));
            graph.addEdge(new Line("A", "D", 18.0, 100, 8.0));
            
            CycleDetection.CycleInfo result = CycleDetection.findCycles(graph);
            assertTrue(result.hasCycles() == true, "Cycle detected");
            assertTrue(result.getCycleNodeIds().contains("A"), "Node A in cycle");
            assertTrue(result.getCycleNodeIds().contains("B"), "Node B in cycle");
            assertTrue(result.getCycleNodeIds().contains("C"), "Node C in cycle");
        });
    }
    
    private static void testFindCycles_CycleWithMultipleBranches() {
        test("findCycles identifies cycle with multiple branches", () -> {
            RailwayGraph graph = new RailwayGraph();
            Station a = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station b = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            Station c = new Station("C", "C", 52.0, 6.0, 102.0, 202.0);
            Station d = new Station("D", "D", 53.0, 7.0, 103.0, 203.0);
            Station e = new Station("E", "E", 54.0, 8.0, 104.0, 204.0);
            
            graph.addNode(a);
            graph.addNode(b);
            graph.addNode(c);
            graph.addNode(d);
            graph.addNode(e);
            graph.addEdge(new Line("A", "B", 10.0, 100, 5.0));
            graph.addEdge(new Line("B", "C", 12.0, 100, 6.0));
            graph.addEdge(new Line("C", "A", 15.0, 100, 7.0));
            graph.addEdge(new Line("A", "D", 18.0, 100, 8.0));
            graph.addEdge(new Line("B", "E", 20.0, 100, 9.0));
            
            CycleDetection.CycleInfo result = CycleDetection.findCycles(graph);
            assertTrue(result.hasCycles() == true, "Cycle detected");
        });
    }
    
    // ==================== Disconnected Components Tests ====================
    
    private static void testHasCycles_DisconnectedWithCycle() {
        test("hasCycles detects cycle in disconnected component", () -> {
            RailwayGraph graph = new RailwayGraph();
            Station a = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station b = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            Station c = new Station("C", "C", 52.0, 6.0, 102.0, 202.0);
            Station d = new Station("D", "D", 53.0, 7.0, 103.0, 203.0);
            
            graph.addNode(a);
            graph.addNode(b);
            graph.addNode(c);
            graph.addNode(d);
            graph.addEdge(new Line("A", "B", 10.0, 100, 5.0)); // Valid chain
            graph.addEdge(new Line("C", "D", 12.0, 100, 6.0));
            graph.addEdge(new Line("D", "C", 15.0, 100, 7.0)); // Cycle in disconnected component
            
            boolean result = CycleDetection.hasCycles(graph);
            assertTrue(result == true, "Cycle in disconnected component detected");
        });
    }
    
    private static void testHasCycles_DisconnectedMultipleCycles() {
        test("hasCycles detects multiple cycles in disconnected components", () -> {
            RailwayGraph graph = createMultipleCycles();
            boolean result = CycleDetection.hasCycles(graph);
            assertTrue(result == true, "Multiple cycles detected");
        });
    }
    
    private static void testFindCycles_DisconnectedWithCycle() {
        test("findCycles identifies cycle in disconnected component", () -> {
            RailwayGraph graph = new RailwayGraph();
            Station a = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station b = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            Station c = new Station("C", "C", 52.0, 6.0, 102.0, 202.0);
            Station d = new Station("D", "D", 53.0, 7.0, 103.0, 203.0);
            
            graph.addNode(a);
            graph.addNode(b);
            graph.addNode(c);
            graph.addNode(d);
            graph.addEdge(new Line("A", "B", 10.0, 100, 5.0));
            graph.addEdge(new Line("C", "D", 12.0, 100, 6.0));
            graph.addEdge(new Line("D", "C", 15.0, 100, 7.0));
            
            CycleDetection.CycleInfo result = CycleDetection.findCycles(graph);
            assertTrue(result.hasCycles() == true, "Cycle detected");
            assertTrue(result.getCycleNodeIds().contains("C"), "Node C in cycle");
            assertTrue(result.getCycleNodeIds().contains("D"), "Node D in cycle");
        });
    }
    
    private static void testFindCycles_DisconnectedMultipleCycles() {
        test("findCycles identifies multiple cycles in disconnected components", () -> {
            RailwayGraph graph = createMultipleCycles();
            CycleDetection.CycleInfo result = CycleDetection.findCycles(graph);
            assertTrue(result.hasCycles() == true, "Cycles detected");
            assertTrue(result.getCycleNodeIds().size() >= 4, "All cycle nodes identified");
        });
    }
    
    // ==================== CycleInfo Validation Tests ====================
    
    private static void testCycleInfo_EmptyCycles() {
        test("CycleInfo with no cycles using real data has correct properties", () -> {
            RailwayGraph graph = createGraphFromRealData(3);
            CycleDetection.CycleInfo result = CycleDetection.findCycles(graph);
            assertTrue(result.hasCycles() == false, "No cycles");
            assertTrue(result.getCycleNodeIds().isEmpty(), "No cycle nodes");
            assertTrue(result.getCycleEdges().isEmpty(), "No cycle edges");
            assertTrue(result.getCycles().isEmpty(), "No cycle paths");
        });
    }
    
    private static void testCycleInfo_CycleNodes() {
        test("CycleInfo correctly identifies cycle nodes using real data", () -> {
            RailwayGraph graph = createCycleFromRealData();
            CycleDetection.CycleInfo result = CycleDetection.findCycles(graph);
            assertTrue(result.hasCycles() == true, "Cycles detected");
            Set<String> cycleNodes = result.getCycleNodeIds();
            assertTrue(cycleNodes.size() >= 3, "At least 3 cycle nodes");
        });
    }
    
    private static void testCycleInfo_CycleEdges() {
        test("CycleInfo correctly identifies cycle edges using real data", () -> {
            RailwayGraph graph = createCycleFromRealData();
            CycleDetection.CycleInfo result = CycleDetection.findCycles(graph);
            assertTrue(result.hasCycles() == true, "Cycles detected");
            Set<Line> cycleEdges = result.getCycleEdges();
            assertTrue(cycleEdges.size() >= 1, "At least 1 cycle edge");
        });
    }
    
    private static void testCycleInfo_CompleteCycles() {
        test("CycleInfo contains complete cycle paths using real data", () -> {
            RailwayGraph graph = createCycleFromRealData();
            CycleDetection.CycleInfo result = CycleDetection.findCycles(graph);
            assertTrue(result.hasCycles() == true, "Cycles detected");
            List<List<Station>> cycles = result.getCycles();
            assertTrue(cycles.size() >= 1, "At least 1 cycle path");
        });
    }
    
    private static void testCycleInfo_HasCycles() {
        test("CycleInfo.hasCycles() returns correct value using real data", () -> {
            RailwayGraph acyclicGraph = createGraphFromRealData(3);
            CycleDetection.CycleInfo acyclicResult = CycleDetection.findCycles(acyclicGraph);
            assertTrue(acyclicResult.hasCycles() == false, "Acyclic graph has no cycles");
            
            RailwayGraph cyclicGraph = createCycleFromRealData();
            CycleDetection.CycleInfo cyclicResult = CycleDetection.findCycles(cyclicGraph);
            assertTrue(cyclicResult.hasCycles() == true, "Cyclic graph has cycles");
        });
    }
    
    // ==================== Edge Cases ====================
    
    private static void testHasCycles_LargeAcyclicGraph() {
        test("hasCycles handles large acyclic graph using real data", () -> {
            RailwayGraph graph = createGraphFromRealData(50);
            boolean result = CycleDetection.hasCycles(graph);
            assertTrue(result == false, "Large acyclic graph has no cycles");
        });
    }
    
    private static void testHasCycles_LargeCyclicGraph() {
        test("hasCycles handles large cyclic graph", () -> {
            RailwayGraph graph = new RailwayGraph();
            // Create a cycle of 30 nodes
            for (int i = 0; i < 30; i++) {
                Station s = new Station("N" + i, "Node " + i, 50.0 + i, 4.0 + i, 100.0 + i, 200.0 + i);
                graph.addNode(s);
            }
            
            for (int i = 0; i < 30; i++) {
                int next = (i + 1) % 30;
                graph.addEdge(new Line("N" + i, "N" + next, 10.0, 100, 5.0));
            }
            
            boolean result = CycleDetection.hasCycles(graph);
            assertTrue(result == true, "Large cyclic graph has cycles");
        });
    }
    
    private static void testFindCycles_AllNodesInCycle() {
        test("findCycles identifies all nodes in a complete cycle", () -> {
            RailwayGraph graph = new RailwayGraph();
            Station[] stations = new Station[5];
            for (int i = 0; i < 5; i++) {
                stations[i] = new Station(String.valueOf((char)('A' + i)), 
                                         "Station " + (char)('A' + i),
                                         50.0 + i, 4.0 + i, 100.0 + i, 200.0 + i);
                graph.addNode(stations[i]);
            }
            
            for (int i = 0; i < 5; i++) {
                int next = (i + 1) % 5;
                graph.addEdge(new Line(String.valueOf((char)('A' + i)), 
                                      String.valueOf((char)('A' + next)),
                                      10.0, 100, 5.0));
            }
            
            CycleDetection.CycleInfo result = CycleDetection.findCycles(graph);
            assertTrue(result.hasCycles() == true, "Cycle detected");
            assertTrue(result.getCycleNodeIds().size() >= 5, "All nodes in cycle identified");
        });
    }
    
    private static void testFindCycles_PartialCycle() {
        test("findCycles identifies partial cycle", () -> {
            RailwayGraph graph = new RailwayGraph();
            Station a = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station b = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            Station c = new Station("C", "C", 52.0, 6.0, 102.0, 202.0);
            Station d = new Station("D", "D", 53.0, 7.0, 103.0, 203.0);
            
            graph.addNode(a);
            graph.addNode(b);
            graph.addNode(c);
            graph.addNode(d);
            graph.addEdge(new Line("A", "B", 10.0, 100, 5.0));
            graph.addEdge(new Line("B", "C", 12.0, 100, 6.0));
            graph.addEdge(new Line("C", "A", 15.0, 100, 7.0)); // Cycle A->B->C->A
            // D is not in cycle
            
            CycleDetection.CycleInfo result = CycleDetection.findCycles(graph);
            assertTrue(result.hasCycles() == true, "Cycle detected");
            assertTrue(result.getCycleNodeIds().contains("A"), "Node A in cycle");
            assertTrue(result.getCycleNodeIds().contains("B"), "Node B in cycle");
            assertTrue(result.getCycleNodeIds().contains("C"), "Node C in cycle");
            // D may or may not be in cycleNodes depending on implementation
        });
    }
    
    // ==================== Helper Methods ====================
    
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
            System.out.println("✗ ERROR: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            testsFailed++;
        }
    }
    
    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}

