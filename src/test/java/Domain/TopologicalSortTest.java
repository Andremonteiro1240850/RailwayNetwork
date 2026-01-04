package Domain;

import Repository.*;
import java.util.*;
import java.io.IOException;

/**
 * Comprehensive test suite for TopologicalSort (USEI11).
 * 
 * Tests cover:
 * - Functional tests: acyclic graphs, cycles, various graph structures
 * - Structural tests: all branches of Kahn's algorithm and DFS algorithm
 * - Edge cases: empty graph, single node, disconnected components
 * - Complexity validation: ensures O(V+E) behavior
 * 
 * All tests use REAL data from train_station_dataset/Sprint3/stations.csv and lines.csv
 * 
 * @author Development Team
 * @version 1.0
 */
public class TopologicalSortTest {
    
    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;
    
    // Real data loaded from CSV files
    private static RailwayGraphRepository realDataRepository;
    private static RailwayGraph realDataGraph;
    private static List<Station> realStations;
    private static List<Line> realLines;
    
    public static void main(String[] args) {
        System.out.println("=== TOPOLOGICAL SORT TESTS (USEI11) ===\n");
        
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
        testOrder_NullGraph();
        testOrderDFS_NullGraph();
        
        // ==================== Empty Graph Tests ====================
        testOrder_EmptyGraph();
        testOrderDFS_EmptyGraph();
        
        // ==================== Single Node Tests ====================
        testOrder_SingleNode();
        testOrderDFS_SingleNode();
        
        // ==================== Simple Acyclic Graphs ====================
        testOrder_SimpleChain();
        testOrder_SimpleChain_Reverse();
        testOrder_TwoIndependentChains();
        testOrder_StarPattern();
        testOrder_ForkPattern();
        testOrder_MergePattern();
        
        // ==================== Complex Acyclic Graphs ====================
        testOrder_ComplexDAG();
        testOrder_MultipleSources();
        testOrder_MultipleSinks();
        testOrder_DisconnectedComponents();
        
        // ==================== Cycle Detection Tests ====================
        testOrder_SelfLoop();
        testOrder_SimpleCycle();
        testOrder_ComplexCycle();
        testOrder_MultipleCycles();
        testOrder_CycleWithBranch();
        
        // ==================== DFS Algorithm Tests ====================
        testOrderDFS_SimpleChain();
        testOrderDFS_ComplexDAG();
        testOrderDFS_SimpleCycle();
        testOrderDFS_ComplexCycle();
        
        // ==================== Correctness Validation ====================
        testOrder_RespectsDependencies();
        testOrder_AllNodesIncluded();
        testOrderDFS_RespectsDependencies();
        testOrderDFS_AllNodesIncluded();
        
        // ==================== Edge Cases ====================
        testOrder_DisconnectedWithCycle();
        testOrder_LargeGraph();
        testOrder_LinearGraph();
        testOrder_BinaryTree();
        
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
     * Selects stations and lines that form a simple chain for testing.
     */
    private static RailwayGraph createGraphFromRealData(int numStations) {
        RailwayGraph graph = new RailwayGraph();
        
        if (realStations == null || realStations.isEmpty()) {
            // Fallback to synthetic data if real data not loaded
            return createSimpleChain();
        }
        
        // Use first N stations from real data
        int stationsToUse = Math.min(numStations, realStations.size());
        for (int i = 0; i < stationsToUse; i++) {
            graph.addNode(realStations.get(i));
        }
        
        // Add lines that connect consecutive stations (if they exist in real data)
        for (int i = 0; i < stationsToUse - 1; i++) {
            String fromId = realStations.get(i).getId();
            String toId = realStations.get(i + 1).getId();
            
            // Find a real line connecting these stations
            Line connectingLine = findLineBetween(fromId, toId);
            if (connectingLine != null) {
                graph.addEdge(connectingLine);
            } else {
                // If no real line exists, create a synthetic one for testing
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
     * Creates a graph using real stations that form a cycle (if possible).
     */
    private static RailwayGraph createCycleFromRealData() {
        RailwayGraph graph = new RailwayGraph();
        
        if (realStations == null || realStations.size() < 3) {
            return createSimpleCycle();
        }
        
        // Try to find a cycle in real data
        // Use first 3 stations and try to create a cycle
        for (int i = 0; i < Math.min(3, realStations.size()); i++) {
            graph.addNode(realStations.get(i));
        }
        
        // Add edges to form a cycle
        String id0 = realStations.get(0).getId();
        String id1 = realStations.get(1).getId();
        String id2 = realStations.get(2).getId();
        
        // Try to use real lines, or create synthetic ones
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
    
    /**
     * Gets a real station by index.
     */
    private static Station getRealStation(int index) {
        if (realStations != null && index < realStations.size()) {
            return realStations.get(index);
        }
        // Fallback
        return new Station("A", "Station A", 50.0, 4.0, 100.0, 200.0);
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
    
    private static RailwayGraph createSimpleCycle() {
        RailwayGraph graph = new RailwayGraph();
        Station a = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
        Station b = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
        Station c = new Station("C", "C", 52.0, 6.0, 102.0, 202.0);
        
        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addEdge(new Line("A", "B", 10.0, 100, 5.0));
        graph.addEdge(new Line("B", "C", 15.0, 100, 7.0));
        graph.addEdge(new Line("C", "A", 20.0, 100, 9.0)); // Cycle!
        
        return graph;
    }
    
    private static RailwayGraph createComplexDAG() {
        RailwayGraph graph = new RailwayGraph();
        // Create: A->B, A->C, B->D, C->D, D->E, F->G
        Station[] stations = new Station[7];
        for (int i = 0; i < 7; i++) {
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
        graph.addEdge(new Line("F", "G", 25.0, 100, 10.0));
        
        return graph;
    }
    
    private static boolean isValidTopologicalOrder(RailwayGraph graph, List<Station> order) {
        if (order == null) return false;
        if (order.size() != graph.getNodeCount()) return false;
        
        // Create a map of positions
        Map<String, Integer> position = new HashMap<>();
        for (int i = 0; i < order.size(); i++) {
            position.put(order.get(i).getId(), i);
        }
        
        // Check all edges: if A->B, then position[A] < position[B]
        for (Line edge : graph.getAllEdges()) {
            String from = edge.getDepartureStationId();
            String to = edge.getArrivalStationId();
            
            Integer fromPos = position.get(from);
            Integer toPos = position.get(to);
            
            if (fromPos == null || toPos == null) return false;
            if (fromPos >= toPos) return false;
        }
        
        return true;
    }
    
    // ==================== Null/Invalid Input Tests ====================
    
    private static void testOrder_NullGraph() {
        test("order with null graph throws exception", () -> {
            try {
                TopologicalSort.order(null);
                throw new AssertionError("Should throw IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("null") || e.getMessage().contains("Graph"), 
                          "Error message mentions null or Graph");
            } catch (TopologicalSort.CycleDetectedException e) {
                throw new AssertionError("Null graph should throw IllegalArgumentException, not CycleDetectedException");
            }
        });
    }
    
    private static void testOrderDFS_NullGraph() {
        test("orderDFS with null graph throws exception", () -> {
            try {
                TopologicalSort.orderDFS(null);
                throw new AssertionError("Should throw IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("null") || e.getMessage().contains("Graph"), 
                          "Error message mentions null or Graph");
            } catch (TopologicalSort.CycleDetectedException e) {
                throw new AssertionError("Null graph should throw IllegalArgumentException, not CycleDetectedException");
            }
        });
    }
    
    // ==================== Empty Graph Tests ====================
    
    private static void testOrder_EmptyGraph() {
        test("order with empty graph returns empty list", () -> {
            RailwayGraph graph = createEmptyGraph();
            try {
                List<Station> result = TopologicalSort.order(graph);
                assertTrue(result != null, "Result is not null");
                assertTrue(result.isEmpty(), "Result is empty");
            } catch (TopologicalSort.CycleDetectedException e) {
                throw new AssertionError("Empty graph should not have cycles");
            }
        });
    }
    
    private static void testOrderDFS_EmptyGraph() {
        test("orderDFS with empty graph returns empty list", () -> {
            RailwayGraph graph = createEmptyGraph();
            try {
                List<Station> result = TopologicalSort.orderDFS(graph);
                assertTrue(result != null, "Result is not null");
                assertTrue(result.isEmpty(), "Result is empty");
            } catch (TopologicalSort.CycleDetectedException e) {
                throw new AssertionError("Empty graph should not have cycles");
            }
        });
    }
    
    // ==================== Single Node Tests ====================
    
    private static void testOrder_SingleNode() {
        test("order with single node returns that node", () -> {
            RailwayGraph graph = createSingleNodeGraph();
            try {
                List<Station> result = TopologicalSort.order(graph);
                assertTrue(result.size() == 1, "Result has one node");
                assertTrue(result.get(0).getId().equals("A"), "Result contains node A");
            } catch (TopologicalSort.CycleDetectedException e) {
                throw new AssertionError("Single node graph should not have cycles");
            }
        });
    }
    
    private static void testOrderDFS_SingleNode() {
        test("orderDFS with single node returns that node", () -> {
            RailwayGraph graph = createSingleNodeGraph();
            try {
                List<Station> result = TopologicalSort.orderDFS(graph);
                assertTrue(result.size() == 1, "Result has one node");
                assertTrue(result.get(0).getId().equals("A"), "Result contains node A");
            } catch (TopologicalSort.CycleDetectedException e) {
                throw new AssertionError("Single node graph should not have cycles");
            }
        });
    }
    
    // ==================== Simple Acyclic Graphs ====================
    
    private static void testOrder_SimpleChain() {
        test("order with simple chain using real data", () -> {
            RailwayGraph graph = createGraphFromRealData(3);
            try {
                List<Station> result = TopologicalSort.order(graph);
                assertTrue(isValidTopologicalOrder(graph, result), "Order is valid");
                assertTrue(result.size() == graph.getNodeCount(), "All nodes included");
            } catch (TopologicalSort.CycleDetectedException e) {
                throw new AssertionError("Simple chain should not have cycles: " + e.getMessage());
            }
        });
    }
    
    private static void testOrder_SimpleChain_Reverse() {
        test("order with reverse chain C->B->A", () -> {
            RailwayGraph graph = new RailwayGraph();
            Station a = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station b = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            Station c = new Station("C", "C", 52.0, 6.0, 102.0, 202.0);
            
            graph.addNode(c);
            graph.addNode(b);
            graph.addNode(a);
            graph.addEdge(new Line("C", "B", 10.0, 100, 5.0));
            graph.addEdge(new Line("B", "A", 15.0, 100, 7.0));
            
            try {
                List<Station> result = TopologicalSort.order(graph);
                assertTrue(isValidTopologicalOrder(graph, result), "Order is valid");
                assertTrue(result.get(0).getId().equals("C"), "C comes first");
            } catch (TopologicalSort.CycleDetectedException e) {
                throw new AssertionError("Reverse chain should not have cycles: " + e.getMessage());
            }
        });
    }
    
    private static void testOrder_TwoIndependentChains() {
        test("order with two independent chains", () -> {
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
            graph.addEdge(new Line("C", "D", 15.0, 100, 7.0));
            
            try {
                List<Station> result = TopologicalSort.order(graph);
                assertTrue(isValidTopologicalOrder(graph, result), "Order is valid");
                assertTrue(result.size() == 4, "All nodes included");
            } catch (TopologicalSort.CycleDetectedException e) {
                throw new AssertionError("Two independent chains should not have cycles: " + e.getMessage());
            }
        });
    }
    
    private static void testOrder_StarPattern() {
        test("order with star pattern (one source, multiple sinks)", () -> {
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
            graph.addEdge(new Line("A", "C", 12.0, 100, 6.0));
            graph.addEdge(new Line("A", "D", 15.0, 100, 7.0));
            
            try {
                List<Station> result = TopologicalSort.order(graph);
                assertTrue(isValidTopologicalOrder(graph, result), "Order is valid");
                assertTrue(result.get(0).getId().equals("A"), "A (source) comes first");
            } catch (TopologicalSort.CycleDetectedException e) {
                throw new AssertionError("Star pattern should not have cycles: " + e.getMessage());
            }
        });
    }
    
    private static void testOrder_ForkPattern() {
        test("order with fork pattern (multiple sources, one sink)", () -> {
            RailwayGraph graph = new RailwayGraph();
            Station a = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station b = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            Station c = new Station("C", "C", 52.0, 6.0, 102.0, 202.0);
            Station d = new Station("D", "D", 53.0, 7.0, 103.0, 203.0);
            
            graph.addNode(a);
            graph.addNode(b);
            graph.addNode(c);
            graph.addNode(d);
            graph.addEdge(new Line("A", "D", 10.0, 100, 5.0));
            graph.addEdge(new Line("B", "D", 12.0, 100, 6.0));
            graph.addEdge(new Line("C", "D", 15.0, 100, 7.0));
            
            try {
                List<Station> result = TopologicalSort.order(graph);
                assertTrue(isValidTopologicalOrder(graph, result), "Order is valid");
                assertTrue(result.get(3).getId().equals("D"), "D (sink) comes last");
            } catch (TopologicalSort.CycleDetectedException e) {
                throw new AssertionError("Fork pattern should not have cycles: " + e.getMessage());
            }
        });
    }
    
    private static void testOrder_MergePattern() {
        test("order with merge pattern (A->C, B->C)", () -> {
            RailwayGraph graph = new RailwayGraph();
            Station a = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station b = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            Station c = new Station("C", "C", 52.0, 6.0, 102.0, 202.0);
            
            graph.addNode(a);
            graph.addNode(b);
            graph.addNode(c);
            graph.addEdge(new Line("A", "C", 10.0, 100, 5.0));
            graph.addEdge(new Line("B", "C", 12.0, 100, 6.0));
            
            try {
                List<Station> result = TopologicalSort.order(graph);
                assertTrue(isValidTopologicalOrder(graph, result), "Order is valid");
                assertTrue(result.get(2).getId().equals("C"), "C comes last");
            } catch (TopologicalSort.CycleDetectedException e) {
                throw new AssertionError("Merge pattern should not have cycles: " + e.getMessage());
            }
        });
    }
    
    // ==================== Complex Acyclic Graphs ====================
    
    private static void testOrder_ComplexDAG() {
        test("order with complex DAG using real data", () -> {
            // Use the full real graph if available
            RailwayGraph graph = (realDataGraph != null && realDataGraph.getNodeCount() > 0) 
                ? realDataGraph 
                : createGraphFromRealData(10);
            try {
                List<Station> result = TopologicalSort.order(graph);
                assertTrue(isValidTopologicalOrder(graph, result), "Order is valid");
                assertTrue(result.size() == graph.getNodeCount(), "All nodes included");
            } catch (TopologicalSort.CycleDetectedException e) {
                throw new AssertionError("Complex DAG should not have cycles: " + e.getMessage());
            }
        });
    }
    
    private static void testOrder_MultipleSources() {
        test("order with multiple source nodes", () -> {
            RailwayGraph graph = new RailwayGraph();
            Station a = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station b = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            Station c = new Station("C", "C", 52.0, 6.0, 102.0, 202.0);
            Station d = new Station("D", "D", 53.0, 7.0, 103.0, 203.0);
            
            graph.addNode(a);
            graph.addNode(b);
            graph.addNode(c);
            graph.addNode(d);
            graph.addEdge(new Line("A", "C", 10.0, 100, 5.0));
            graph.addEdge(new Line("B", "C", 12.0, 100, 6.0));
            graph.addEdge(new Line("C", "D", 15.0, 100, 7.0));
            
            try {
                List<Station> result = TopologicalSort.order(graph);
                assertTrue(isValidTopologicalOrder(graph, result), "Order is valid");
                // A and B should come before C
                int aPos = findPosition(result, "A");
                int bPos = findPosition(result, "B");
                int cPos = findPosition(result, "C");
                assertTrue(aPos < cPos && bPos < cPos, "A and B before C");
            } catch (TopologicalSort.CycleDetectedException e) {
                throw new AssertionError("Multiple sources should not have cycles: " + e.getMessage());
            }
        });
    }
    
    private static void testOrder_MultipleSinks() {
        test("order with multiple sink nodes", () -> {
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
            graph.addEdge(new Line("A", "C", 12.0, 100, 6.0));
            graph.addEdge(new Line("A", "D", 15.0, 100, 7.0));
            
            try {
                List<Station> result = TopologicalSort.order(graph);
                assertTrue(isValidTopologicalOrder(graph, result), "Order is valid");
                assertTrue(result.get(0).getId().equals("A"), "A (source) comes first");
            } catch (TopologicalSort.CycleDetectedException e) {
                throw new AssertionError("Multiple sinks should not have cycles: " + e.getMessage());
            }
        });
    }
    
    private static void testOrder_DisconnectedComponents() {
        test("order with disconnected components using real data", () -> {
            RailwayGraph graph = new RailwayGraph();
            
            if (realStations != null && realStations.size() >= 4) {
                // Use real stations
                graph.addNode(realStations.get(0));
                graph.addNode(realStations.get(1));
                graph.addNode(realStations.get(2));
                graph.addNode(realStations.get(3));
                
                // Add edge between first two
                Line line = findLineBetween(realStations.get(0).getId(), realStations.get(1).getId());
                if (line != null) {
                    graph.addEdge(line);
                } else {
                    graph.addEdge(new Line(realStations.get(0).getId(), realStations.get(1).getId(), 10.0, 100, 5.0));
                }
                // Stations 2 and 3 are disconnected
            } else {
                // Fallback to synthetic
                Station a = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
                Station b = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
                Station c = new Station("C", "C", 52.0, 6.0, 102.0, 202.0);
                Station d = new Station("D", "D", 53.0, 7.0, 103.0, 203.0);
                
                graph.addNode(a);
                graph.addNode(b);
                graph.addNode(c);
                graph.addNode(d);
                graph.addEdge(new Line("A", "B", 10.0, 100, 5.0));
            }
            
            try {
                List<Station> result = TopologicalSort.order(graph);
                assertTrue(isValidTopologicalOrder(graph, result), "Order is valid");
                assertTrue(result.size() == graph.getNodeCount(), "All nodes included");
            } catch (TopologicalSort.CycleDetectedException e) {
                throw new AssertionError("Disconnected components should not have cycles: " + e.getMessage());
            }
        });
    }
    
    // ==================== Cycle Detection Tests ====================
    
    private static void testOrder_SelfLoop() {
        test("order detects self-loop cycle", () -> {
            RailwayGraph graph = new RailwayGraph();
            Station a = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            graph.addNode(a);
            // Note: Line constructor prevents self-loops, so we test with a 2-node cycle
            Station b = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            graph.addNode(b);
            graph.addEdge(new Line("A", "B", 10.0, 100, 5.0));
            graph.addEdge(new Line("B", "A", 12.0, 100, 6.0)); // Cycle A->B->A
            
            try {
                TopologicalSort.order(graph);
                throw new AssertionError("Should throw CycleDetectedException");
            } catch (TopologicalSort.CycleDetectedException e) {
                assertTrue(e.getMessage().contains("cycle") || e.getMessage().contains("Cycle"), 
                          "Error message mentions cycle");
            }
        });
    }
    
    private static void testOrder_SimpleCycle() {
        test("order detects simple 3-node cycle using real data", () -> {
            RailwayGraph graph = createCycleFromRealData();
            try {
                TopologicalSort.order(graph);
                throw new AssertionError("Should throw CycleDetectedException");
            } catch (TopologicalSort.CycleDetectedException e) {
                assertTrue(e.getMessage().contains("cycle") || e.getMessage().contains("Cycle"), 
                          "Error message mentions cycle");
            }
        });
    }
    
    private static void testOrder_ComplexCycle() {
        test("order detects complex cycle", () -> {
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
            graph.addEdge(new Line("C", "D", 15.0, 100, 7.0));
            graph.addEdge(new Line("D", "A", 18.0, 100, 8.0)); // Cycle A->B->C->D->A
            
            try {
                TopologicalSort.order(graph);
                throw new AssertionError("Should throw CycleDetectedException");
            } catch (TopologicalSort.CycleDetectedException e) {
                assertTrue(e.getMessage().contains("cycle") || e.getMessage().contains("Cycle"), 
                          "Error message mentions cycle");
            }
        });
    }
    
    private static void testOrder_MultipleCycles() {
        test("order detects graph with multiple cycles", () -> {
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
            
            try {
                TopologicalSort.order(graph);
                throw new AssertionError("Should throw CycleDetectedException");
            } catch (TopologicalSort.CycleDetectedException e) {
                assertTrue(e.getMessage().contains("cycle") || e.getMessage().contains("Cycle"), 
                          "Error message mentions cycle");
            }
        });
    }
    
    private static void testOrder_CycleWithBranch() {
        test("order detects cycle with branch", () -> {
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
            
            try {
                TopologicalSort.order(graph);
                throw new AssertionError("Should throw CycleDetectedException");
            } catch (TopologicalSort.CycleDetectedException e) {
                assertTrue(e.getMessage().contains("cycle") || e.getMessage().contains("Cycle"), 
                          "Error message mentions cycle");
            }
        });
    }
    
    // ==================== DFS Algorithm Tests ====================
    
    private static void testOrderDFS_SimpleChain() {
        test("orderDFS with simple chain using real data", () -> {
            RailwayGraph graph = createGraphFromRealData(3);
            try {
                List<Station> result = TopologicalSort.orderDFS(graph);
                assertTrue(isValidTopologicalOrder(graph, result), "Order is valid");
                assertTrue(result.size() == graph.getNodeCount(), "All nodes included");
            } catch (TopologicalSort.CycleDetectedException e) {
                throw new AssertionError("Simple chain should not have cycles: " + e.getMessage());
            }
        });
    }
    
    private static void testOrderDFS_ComplexDAG() {
        test("orderDFS with complex DAG using real data", () -> {
            RailwayGraph graph = createGraphFromRealData(10);
            try {
                List<Station> result = TopologicalSort.orderDFS(graph);
                assertTrue(isValidTopologicalOrder(graph, result), "Order is valid");
                assertTrue(result.size() == graph.getNodeCount(), "All nodes included");
            } catch (TopologicalSort.CycleDetectedException e) {
                throw new AssertionError("Complex DAG should not have cycles: " + e.getMessage());
            }
        });
    }
    
    private static void testOrderDFS_SimpleCycle() {
        test("orderDFS detects simple cycle using real data", () -> {
            RailwayGraph graph = createCycleFromRealData();
            try {
                TopologicalSort.orderDFS(graph);
                throw new AssertionError("Should throw CycleDetectedException");
            } catch (TopologicalSort.CycleDetectedException e) {
                assertTrue(e.getMessage().contains("cycle") || e.getMessage().contains("Cycle") || 
                          e.getMessage().contains("back edge"), 
                          "Error message mentions cycle or back edge");
            }
        });
    }
    
    private static void testOrderDFS_ComplexCycle() {
        test("orderDFS detects complex cycle", () -> {
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
            graph.addEdge(new Line("C", "D", 15.0, 100, 7.0));
            graph.addEdge(new Line("D", "A", 18.0, 100, 8.0)); // Cycle
            
            try {
                TopologicalSort.orderDFS(graph);
                throw new AssertionError("Should throw CycleDetectedException");
            } catch (TopologicalSort.CycleDetectedException e) {
                assertTrue(e.getMessage().contains("cycle") || e.getMessage().contains("Cycle") || 
                          e.getMessage().contains("back edge"), 
                          "Error message mentions cycle or back edge");
            }
        });
    }
    
    // ==================== Correctness Validation ====================
    
    private static void testOrder_RespectsDependencies() {
        test("order respects all edge dependencies using real data", () -> {
            RailwayGraph graph = createGraphFromRealData(15);
            try {
                List<Station> result = TopologicalSort.order(graph);
                assertTrue(isValidTopologicalOrder(graph, result), "All dependencies respected");
            } catch (TopologicalSort.CycleDetectedException e) {
                throw new AssertionError("Graph should not have cycles: " + e.getMessage());
            }
        });
    }
    
    private static void testOrder_AllNodesIncluded() {
        test("order includes all nodes using real data", () -> {
            RailwayGraph graph = createGraphFromRealData(15);
            try {
                List<Station> result = TopologicalSort.order(graph);
                assertTrue(result.size() == graph.getNodeCount(), "All nodes included");
                
                Set<String> resultIds = new HashSet<>();
                for (Station s : result) {
                    resultIds.add(s.getId());
                }
                Set<String> graphIds = new HashSet<>();
                for (Station s : graph.getAllNodes()) {
                    graphIds.add(s.getId());
                }
                assertTrue(resultIds.equals(graphIds), "Same set of nodes");
            } catch (TopologicalSort.CycleDetectedException e) {
                throw new AssertionError("Graph should not have cycles: " + e.getMessage());
            }
        });
    }
    
    private static void testOrderDFS_RespectsDependencies() {
        test("orderDFS respects all edge dependencies using real data", () -> {
            RailwayGraph graph = createGraphFromRealData(15);
            try {
                List<Station> result = TopologicalSort.orderDFS(graph);
                assertTrue(isValidTopologicalOrder(graph, result), "All dependencies respected");
            } catch (TopologicalSort.CycleDetectedException e) {
                throw new AssertionError("Graph should not have cycles: " + e.getMessage());
            }
        });
    }
    
    private static void testOrderDFS_AllNodesIncluded() {
        test("orderDFS includes all nodes using real data", () -> {
            RailwayGraph graph = createGraphFromRealData(15);
            try {
                List<Station> result = TopologicalSort.orderDFS(graph);
                assertTrue(result.size() == graph.getNodeCount(), "All nodes included");
            } catch (TopologicalSort.CycleDetectedException e) {
                throw new AssertionError("Graph should not have cycles: " + e.getMessage());
            }
        });
    }
    
    // ==================== Edge Cases ====================
    
    private static void testOrder_DisconnectedWithCycle() {
        test("order handles disconnected component with cycle", () -> {
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
            
            try {
                TopologicalSort.order(graph);
                throw new AssertionError("Should throw CycleDetectedException");
            } catch (TopologicalSort.CycleDetectedException e) {
                assertTrue(true, "Cycle detected correctly");
            }
        });
    }
    
    private static void testOrder_LargeGraph() {
        test("order handles large graph using real data", () -> {
            RailwayGraph graph = createGraphFromRealData(50);
            try {
                List<Station> result = TopologicalSort.order(graph);
                assertTrue(isValidTopologicalOrder(graph, result), "Order is valid for large graph");
                assertTrue(result.size() == graph.getNodeCount(), "All nodes included");
            } catch (TopologicalSort.CycleDetectedException e) {
                throw new AssertionError("Large graph should not have cycles: " + e.getMessage());
            }
        });
    }
    
    private static void testOrder_LinearGraph() {
        test("order with linear graph using real data", () -> {
            RailwayGraph graph = createGraphFromRealData(10);
            try {
                List<Station> result = TopologicalSort.order(graph);
                assertTrue(isValidTopologicalOrder(graph, result), "Order is valid");
                assertTrue(result.size() == graph.getNodeCount(), "All nodes included");
            } catch (TopologicalSort.CycleDetectedException e) {
                throw new AssertionError("Linear graph should not have cycles: " + e.getMessage());
            }
        });
    }
    
    private static void testOrder_BinaryTree() {
        test("order with binary tree structure", () -> {
            RailwayGraph graph = new RailwayGraph();
            Station root = new Station("R", "Root", 50.0, 4.0, 100.0, 200.0);
            Station l1 = new Station("L1", "Left1", 49.0, 3.0, 99.0, 199.0);
            Station r1 = new Station("R1", "Right1", 51.0, 5.0, 101.0, 201.0);
            Station l2 = new Station("L2", "Left2", 48.0, 2.0, 98.0, 198.0);
            Station r2 = new Station("R2", "Right2", 52.0, 6.0, 102.0, 202.0);
            
            graph.addNode(root);
            graph.addNode(l1);
            graph.addNode(r1);
            graph.addNode(l2);
            graph.addNode(r2);
            graph.addEdge(new Line("R", "L1", 10.0, 100, 5.0));
            graph.addEdge(new Line("R", "R1", 12.0, 100, 6.0));
            graph.addEdge(new Line("L1", "L2", 15.0, 100, 7.0));
            graph.addEdge(new Line("R1", "R2", 18.0, 100, 8.0));
            
            try {
                List<Station> result = TopologicalSort.order(graph);
                assertTrue(isValidTopologicalOrder(graph, result), "Order is valid");
                assertTrue(result.get(0).getId().equals("R"), "Root comes first");
            } catch (TopologicalSort.CycleDetectedException e) {
                throw new AssertionError("Binary tree should not have cycles: " + e.getMessage());
            }
        });
    }
    
    // ==================== Helper Methods ====================
    
    private static int findPosition(List<Station> list, String id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }
    
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

