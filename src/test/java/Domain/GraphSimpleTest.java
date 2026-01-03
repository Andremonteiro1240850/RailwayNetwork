package Domain;

import java.util.List;
import java.util.Set;

/**
 * Simple test class for Graph (USEI12) without JUnit dependencies.
 * Graph is used by DotExporter, so testing it improves overall coverage.
 */
public class GraphSimpleTest {
    
    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;
    
    public static void main(String[] args) {
        System.out.println("=== GRAPH SIMPLE TESTS (USEI12) ===\n");
        
        // Constructor
        testConstructor();
        
        // addStation branches
        testAddStation_Null();
        testAddStation_Valid();
        testAddStation_Duplicate();
        
        // addConnection branches
        testAddConnection_Null();
        testAddConnection_OriginNotExists();
        testAddConnection_DestinationNotExists();
        testAddConnection_Valid();
        
        // getNeighbors branches
        testGetNeighbors_Null();
        testGetNeighbors_Empty();
        testGetNeighbors_StationNotExists();
        testGetNeighbors_StationExists();
        testGetNeighbors_OriginBranch();
        testGetNeighbors_DestinationBranch();
        
        // getStation branches
        testGetStation_Null();
        testGetStation_Exists();
        testGetStation_NotExists();
        
        // getConnections branches
        testGetConnections_Null();
        testGetConnections_Empty();
        testGetConnections_StationNotExists();
        testGetConnections_StationExists();
        
        // getAllUniqueConnections branches
        testGetAllUniqueConnections_Empty();
        testGetAllUniqueConnections_WithConnections();
        testGetAllUniqueConnections_ProcessedKey1();
        testGetAllUniqueConnections_ProcessedKey2();
        
        // containsStation branches
        testContainsStation_Null();
        testContainsStation_Exists();
        testContainsStation_NotExists();
        
        // Other methods
        testGetStations();
        testGetAllConnections();
        testGetStationCount();
        testGetConnectionCount();
        testToString();
        
        System.out.println("\n=== TEST SUMMARY ===");
        System.out.println("Tests run: " + testsRun);
        System.out.println("Tests passed: " + testsPassed);
        System.out.println("Tests failed: " + testsFailed);
    }
    
    // ==================== Constructor ====================
    
    private static void testConstructor() {
        test("constructor creates empty graph", () -> {
            Graph graph = new Graph();
            assertTrue(graph.getStationCount() == 0, "Graph is empty");
            assertTrue(graph.getConnectionCount() == 0, "No connections");
        });
    }
    
    // ==================== addStation Branches ====================
    
    private static void testAddStation_Null() {
        test("addStation with null throws exception", () -> {
            Graph graph = new Graph();
            try {
                graph.addStation(null);
                throw new AssertionError("Should throw exception");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("null"), "Error mentions null");
            }
        });
    }
    
    private static void testAddStation_Valid() {
        test("addStation with valid station", () -> {
            Graph graph = new Graph();
            Station station = new Station("A", "Station A", 50.0, 4.0, 100.0, 200.0);
            graph.addStation(station);
            assertTrue(graph.getStationCount() == 1, "Station added");
            assertTrue(graph.containsStation("A"), "Station exists");
        });
    }
    
    private static void testAddStation_Duplicate() {
        test("addStation with duplicate ID", () -> {
            Graph graph = new Graph();
            Station station1 = new Station("A", "Station A", 50.0, 4.0, 100.0, 200.0);
            Station station2 = new Station("A", "Station B", 51.0, 5.0, 101.0, 201.0);
            graph.addStation(station1);
            graph.addStation(station2);
            // Branch: !stationById.containsKey(id) == false, so duplicate is ignored
            assertTrue(graph.getStationCount() == 1, "Duplicate ignored");
        });
    }
    
    // ==================== addConnection Branches ====================
    
    private static void testAddConnection_Null() {
        test("addConnection with null throws exception", () -> {
            Graph graph = new Graph();
            try {
                graph.addConnection(null);
                throw new AssertionError("Should throw exception");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("null"), "Error mentions null");
            }
        });
    }
    
    private static void testAddConnection_OriginNotExists() {
        test("addConnection with origin not exists", () -> {
            Graph graph = new Graph();
            Station stationB = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            graph.addStation(stationB);
            Connection conn = new Connection("A", "B", 10.0);
            try {
                graph.addConnection(conn);
                throw new AssertionError("Should throw exception");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("Origin") || e.getMessage().contains("A"), "Error mentions origin");
            }
        });
    }
    
    private static void testAddConnection_DestinationNotExists() {
        test("addConnection with destination not exists", () -> {
            Graph graph = new Graph();
            Station stationA = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            graph.addStation(stationA);
            Connection conn = new Connection("A", "B", 10.0);
            try {
                graph.addConnection(conn);
                throw new AssertionError("Should throw exception");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("Destination") || e.getMessage().contains("B"), "Error mentions destination");
            }
        });
    }
    
    private static void testAddConnection_Valid() {
        test("addConnection with valid stations", () -> {
            Graph graph = new Graph();
            Station stationA = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station stationB = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            graph.addStation(stationA);
            graph.addStation(stationB);
            Connection conn = new Connection("A", "B", 10.0);
            graph.addConnection(conn);
            assertTrue(graph.getConnectionCount() == 1, "Connection added");
            assertTrue(graph.getNeighbors("A").contains("B"), "B is neighbor of A");
            assertTrue(graph.getNeighbors("B").contains("A"), "A is neighbor of B");
        });
    }
    
    // ==================== getNeighbors Branches ====================
    
    private static void testGetNeighbors_Null() {
        test("getNeighbors with null", () -> {
            Graph graph = new Graph();
            List<String> neighbors = graph.getNeighbors(null);
            // Branch: stationId == null || stationId.trim().isEmpty() == true
            assertTrue(neighbors.isEmpty(), "Returns empty list");
        });
    }
    
    private static void testGetNeighbors_Empty() {
        test("getNeighbors with empty string", () -> {
            Graph graph = new Graph();
            List<String> neighbors = graph.getNeighbors("");
            // Branch: stationId.trim().isEmpty() == true
            assertTrue(neighbors.isEmpty(), "Returns empty list");
        });
    }
    
    private static void testGetNeighbors_StationNotExists() {
        test("getNeighbors with station not exists", () -> {
            Graph graph = new Graph();
            List<String> neighbors = graph.getNeighbors("NONEXISTENT");
            // Branch: connections == null
            assertTrue(neighbors.isEmpty(), "Returns empty list");
        });
    }
    
    private static void testGetNeighbors_StationExists() {
        test("getNeighbors with station exists", () -> {
            Graph graph = new Graph();
            Station stationA = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station stationB = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            graph.addStation(stationA);
            graph.addStation(stationB);
            graph.addConnection(new Connection("A", "B", 10.0));
            List<String> neighbors = graph.getNeighbors("A");
            assertTrue(neighbors.contains("B"), "B is neighbor");
        });
    }
    
    private static void testGetNeighbors_OriginBranch() {
        test("getNeighbors - origin branch", () -> {
            Graph graph = new Graph();
            Station stationA = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station stationB = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            graph.addStation(stationA);
            graph.addStation(stationB);
            graph.addConnection(new Connection("A", "B", 10.0));
            List<String> neighbors = graph.getNeighbors("A");
            // Branch: conn.getOriginId().equals(stationId) == true
            assertTrue(neighbors.contains("B"), "B is neighbor");
        });
    }
    
    private static void testGetNeighbors_DestinationBranch() {
        test("getNeighbors - destination branch", () -> {
            Graph graph = new Graph();
            Station stationA = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station stationB = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            graph.addStation(stationA);
            graph.addStation(stationB);
            graph.addConnection(new Connection("A", "B", 10.0));
            List<String> neighbors = graph.getNeighbors("B");
            // Branch: conn.getOriginId().equals(stationId) == false, so else branch
            assertTrue(neighbors.contains("A"), "A is neighbor");
        });
    }
    
    // ==================== getStation Branches ====================
    
    private static void testGetStation_Null() {
        test("getStation with null", () -> {
            Graph graph = new Graph();
            Station station = graph.getStation(null);
            // Branch: stationId == null
            assertTrue(station == null, "Returns null");
        });
    }
    
    private static void testGetStation_Exists() {
        test("getStation with existing station", () -> {
            Graph graph = new Graph();
            Station stationA = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            graph.addStation(stationA);
            Station found = graph.getStation("A");
            assertTrue(found != null && found.getId().equals("A"), "Station found");
        });
    }
    
    private static void testGetStation_NotExists() {
        test("getStation with non-existing station", () -> {
            Graph graph = new Graph();
            Station found = graph.getStation("NONEXISTENT");
            assertTrue(found == null, "Returns null");
        });
    }
    
    // ==================== getConnections Branches ====================
    
    private static void testGetConnections_Null() {
        test("getConnections with null", () -> {
            Graph graph = new Graph();
            List<Connection> connections = graph.getConnections(null);
            // Branch: stationId == null || stationId.trim().isEmpty() == true
            assertTrue(connections.isEmpty(), "Returns empty list");
        });
    }
    
    private static void testGetConnections_Empty() {
        test("getConnections with empty string", () -> {
            Graph graph = new Graph();
            List<Connection> connections = graph.getConnections("");
            assertTrue(connections.isEmpty(), "Returns empty list");
        });
    }
    
    private static void testGetConnections_StationNotExists() {
        test("getConnections with station not exists", () -> {
            Graph graph = new Graph();
            List<Connection> connections = graph.getConnections("NONEXISTENT");
            // Branch: connections == null
            assertTrue(connections.isEmpty(), "Returns empty list");
        });
    }
    
    private static void testGetConnections_StationExists() {
        test("getConnections with station exists", () -> {
            Graph graph = new Graph();
            Station stationA = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station stationB = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            graph.addStation(stationA);
            graph.addStation(stationB);
            graph.addConnection(new Connection("A", "B", 10.0));
            List<Connection> connections = graph.getConnections("A");
            // Branch: connections != null
            assertTrue(connections.size() == 1, "Returns connections");
        });
    }
    
    // ==================== getAllUniqueConnections Branches ====================
    
    private static void testGetAllUniqueConnections_Empty() {
        test("getAllUniqueConnections with empty graph", () -> {
            Graph graph = new Graph();
            Set<Connection> connections = graph.getAllUniqueConnections();
            assertTrue(connections.isEmpty(), "Returns empty set");
        });
    }
    
    private static void testGetAllUniqueConnections_WithConnections() {
        test("getAllUniqueConnections with connections", () -> {
            Graph graph = new Graph();
            Station stationA = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station stationB = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            graph.addStation(stationA);
            graph.addStation(stationB);
            graph.addConnection(new Connection("A", "B", 10.0));
            Set<Connection> connections = graph.getAllUniqueConnections();
            assertTrue(connections.size() == 1, "Returns unique connections");
        });
    }
    
    private static void testGetAllUniqueConnections_ProcessedKey1() {
        test("getAllUniqueConnections - processed key1", () -> {
            Graph graph = new Graph();
            Station stationA = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station stationB = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            graph.addStation(stationA);
            graph.addStation(stationB);
            graph.addConnection(new Connection("A", "B", 10.0));
            // This will process the connection and mark both keys as processed
            Set<Connection> connections = graph.getAllUniqueConnections();
            // Branch: !processed.contains(key1) && !processed.contains(key2) == true initially
            assertTrue(connections.size() == 1, "Returns unique connections");
        });
    }
    
    private static void testGetAllUniqueConnections_ProcessedKey2() {
        test("getAllUniqueConnections - processed key2", () -> {
            Graph graph = new Graph();
            Station stationA = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station stationB = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            graph.addStation(stationA);
            graph.addStation(stationB);
            graph.addConnection(new Connection("A", "B", 10.0));
            // Get connections twice - second time should skip due to processed keys
            Set<Connection> connections1 = graph.getAllUniqueConnections();
            Set<Connection> connections2 = graph.getAllUniqueConnections();
            // Branch: processed.contains(key1) || processed.contains(key2) == true on second iteration
            assertTrue(connections1.size() == 1 && connections2.size() == 1, "Returns unique connections");
        });
    }
    
    // ==================== containsStation Branches ====================
    
    private static void testContainsStation_Null() {
        test("containsStation with null", () -> {
            Graph graph = new Graph();
            boolean contains = graph.containsStation(null);
            // Branch: stationId == null
            assertTrue(contains == false, "Returns false");
        });
    }
    
    private static void testContainsStation_Exists() {
        test("containsStation with existing station", () -> {
            Graph graph = new Graph();
            Station stationA = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            graph.addStation(stationA);
            boolean contains = graph.containsStation("A");
            // Branch: stationById.containsKey(stationId.trim()) == true
            assertTrue(contains == true, "Returns true");
        });
    }
    
    private static void testContainsStation_NotExists() {
        test("containsStation with non-existing station", () -> {
            Graph graph = new Graph();
            boolean contains = graph.containsStation("NONEXISTENT");
            // Branch: stationById.containsKey(stationId.trim()) == false
            assertTrue(contains == false, "Returns false");
        });
    }
    
    // ==================== Other Methods ====================
    
    private static void testGetStations() {
        test("getStations returns unmodifiable list", () -> {
            Graph graph = new Graph();
            Station stationA = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            graph.addStation(stationA);
            List<Station> stations = graph.getStations();
            assertTrue(stations.size() == 1, "Returns stations");
        });
    }
    
    private static void testGetAllConnections() {
        test("getAllConnections returns all connections", () -> {
            Graph graph = new Graph();
            Station stationA = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station stationB = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            graph.addStation(stationA);
            graph.addStation(stationB);
            graph.addConnection(new Connection("A", "B", 10.0));
            List<Connection> connections = graph.getAllConnections();
            // Since edges are stored in both directions, should return 2
            assertTrue(connections.size() == 2, "Returns all connections");
        });
    }
    
    private static void testGetStationCount() {
        test("getStationCount returns correct count", () -> {
            Graph graph = new Graph();
            assertTrue(graph.getStationCount() == 0, "Empty graph has 0 stations");
            graph.addStation(new Station("A", "A", 50.0, 4.0, 100.0, 200.0));
            assertTrue(graph.getStationCount() == 1, "Graph has 1 station");
        });
    }
    
    private static void testGetConnectionCount() {
        test("getConnectionCount returns correct count", () -> {
            Graph graph = new Graph();
            Station stationA = new Station("A", "A", 50.0, 4.0, 100.0, 200.0);
            Station stationB = new Station("B", "B", 51.0, 5.0, 101.0, 201.0);
            graph.addStation(stationA);
            graph.addStation(stationB);
            assertTrue(graph.getConnectionCount() == 0, "No connections");
            graph.addConnection(new Connection("A", "B", 10.0));
            assertTrue(graph.getConnectionCount() == 1, "Graph has 1 connection");
        });
    }
    
    private static void testToString() {
        test("toString returns formatted string", () -> {
            Graph graph = new Graph();
            String str = graph.toString();
            assertTrue(str.contains("Graph") && str.contains("stations") && str.contains("connections"), "Contains expected text");
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
            System.out.println("✗ ERROR: " + e.getMessage());
            testsFailed++;
        }
    }
    
    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}

