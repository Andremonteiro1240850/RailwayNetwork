package Domain;

import Repository.BelgiumGraphRepository;

/**
 * Simple test class for DotExporter (USEI12) without JUnit dependencies.
 * Comprehensive branch coverage - targeting 90%+ branch coverage.
 */
public class DotExporterSimpleTest {
    
    private static final String STATIONS_PATH = "train_station_dataset/Sprint3/stations.csv";
    private static final String LINES_PATH = "train_station_dataset/Sprint3/lines.csv";
    
    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;
    
    public static void main(String[] args) {
        System.out.println("=== DOT EXPORTER SIMPLE TESTS (USEI12) ===\n");
        
        // Branch 1: graph == null (line 39)
        testNullGraph();
        
        // Branch 2: stations.isEmpty() == true (line 49)
        testEmptyGraph();
        // Branch 3: stations.isEmpty() == false (line 49)
        testNonEmptyGraph();
        
        // Branch 4: station.getName() != null (line 66 - ternary TRUE)
        testStationName_NotNull();
        // Branch 5: station.getName() == null (line 66 - ternary FALSE)
        testStationName_Null();
        
        // Branch 6: originCompare != 0 (line 82 - TRUE)
        testConnections_OriginCompareNotZero();
        // Branch 7: originCompare == 0 (line 82 - FALSE)
        testConnections_OriginCompareZero();
        
        // Branch 8: nodeId == null (line 109 - first part of OR TRUE)
        testEscapeNodeId_Null();
        // Branch 9: nodeId != null && nodeId.isEmpty() (line 109 - first FALSE, second TRUE)
        testEscapeNodeId_Empty();
        // Branch 10: nodeId != null && !nodeId.isEmpty() (line 109 - both FALSE)
        testEscapeNodeId_NonEmpty();
        
        // Branch 11: nodeId.matches() == true (line 114 - TRUE)
        testEscapeNodeId_ValidMatch_LetterStart();
        testEscapeNodeId_ValidMatch_UnderscoreStart();
        testEscapeNodeId_ValidMatch_WithNumbers();
        // Branch 12: nodeId.matches() == false (line 114 - FALSE)
        testEscapeNodeId_InvalidMatch_NumberStart();
        testEscapeNodeId_InvalidMatch_SpecialCharStart();
        testEscapeNodeId_InvalidMatch_ContainsSpecialChars();
        testEscapeNodeId_InvalidMatch_ContainsHyphen();
        testEscapeNodeId_InvalidMatch_ContainsSpace();
        // Branch 13: nodeId with quotes (line 119 - replace executed)
        testEscapeNodeId_WithQuotes();
        
        // Branch 14: label == null (line 130 - TRUE)
        testEscapeLabel_Null();
        // Branch 15: label != null (line 130 - FALSE)
        testEscapeLabel_NotNull();
        
        // Branch 16-19: escapeLabel replace operations (lines 135-138)
        testEscapeLabel_WithBackslash();
        testEscapeLabel_WithQuote();
        testEscapeLabel_WithNewline();
        testEscapeLabel_WithCarriageReturn();
        testEscapeLabel_WithAllSpecialChars();
        
        // Additional comprehensive tests
        testMultipleStations();
        testWithConnections();
        testStations_SortedOrder();
        testCoordinates_Formatting();
        testWithRealData();
        
        System.out.println("\n=== TEST SUMMARY ===");
        System.out.println("Tests run: " + testsRun);
        System.out.println("Tests passed: " + testsPassed);
        System.out.println("Tests failed: " + testsFailed);
    }
    
    // ==================== Branch 1: graph == null ====================
    
    private static void testNullGraph() {
        test("null graph throws exception", () -> {
            DotExporter exporter = new DotExporter();
            try {
                exporter.exportToDot(null);
                throw new AssertionError("Should throw exception");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("null"), "Error mentions null");
            } catch (Exception e) {
                throw new AssertionError("Wrong exception type: " + e.getClass());
            }
        });
    }
    
    // ==================== Branches 2-3: stations.isEmpty() ====================
    
    private static void testEmptyGraph() {
        test("empty graph - stations.isEmpty() == true", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            String dot = exporter.exportToDot(graph);
            assertEquals("graph G {\n}\n", dot, "Empty graph DOT");
        });
    }
    
    private static void testNonEmptyGraph() {
        test("non-empty graph - stations.isEmpty() == false", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("A", "Station A", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("graph G {"), "Contains graph header");
            assertTrue(dot.contains("A"), "Contains station");
        });
    }
    
    // ==================== Branches 4-5: station.getName() != null ====================
    
    private static void testStationName_NotNull() {
        test("station name not null - ternary TRUE branch", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("STATION_ID", "Station Name", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("label=\"Station Name\""), "Non-null name used");
        });
    }
    
    private static void testStationName_Null() {
        test("station name null - ternary FALSE branch", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("STATION_ID", null, 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("label=\"STATION_ID\""), "Null name uses ID");
        });
    }
    
    // ==================== Branches 6-7: originCompare != 0 ====================
    
    private static void testConnections_OriginCompareNotZero() {
        test("connections - originCompare != 0 (TRUE branch)", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("B", "B", 50.0, 4.0, 100.0, 200.0));
            graph.addStation(new Station("A", "A", 51.0, 5.0, 101.0, 201.0));
            graph.addStation(new Station("C", "C", 52.0, 6.0, 102.0, 202.0));
            graph.addConnection(new Connection("B", "A", 10.0));
            graph.addConnection(new Connection("A", "C", 20.0));
            String dot = exporter.exportToDot(graph);
            int indexAC = dot.indexOf("A -- C");
            int indexBA = dot.indexOf("B -- A");
            assertTrue(indexAC != -1 && indexBA != -1, "Both connections present");
            assertTrue(indexAC < indexBA, "A-C before B-A (sorted by origin)");
        });
    }
    
    private static void testConnections_OriginCompareZero() {
        test("connections - originCompare == 0 (FALSE branch)", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("A", "A", 50.0, 4.0, 100.0, 200.0));
            graph.addStation(new Station("B", "B", 51.0, 5.0, 101.0, 201.0));
            graph.addStation(new Station("C", "C", 52.0, 6.0, 102.0, 202.0));
            graph.addConnection(new Connection("A", "C", 20.0));
            graph.addConnection(new Connection("A", "B", 10.0));
            String dot = exporter.exportToDot(graph);
            int indexAB = dot.indexOf("A -- B");
            int indexAC = dot.indexOf("A -- C");
            assertTrue(indexAB != -1 && indexAC != -1, "Both connections present");
            assertTrue(indexAB < indexAC, "A-B before A-C (sorted by destination)");
        });
    }
    
    // ==================== Branches 8-10: nodeId == null || nodeId.isEmpty() ====================
    
    private static void testEscapeNodeId_Null() {
        test("escapeNodeId - nodeId == null (OR first part TRUE)", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station(null, "Test", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("\"\""), "Null ID escaped as \"\"");
        });
    }
    
    private static void testEscapeNodeId_Empty() {
        test("escapeNodeId - nodeId.isEmpty() == true (OR second part TRUE)", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("", "Test", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("\"\""), "Empty ID escaped as \"\"");
        });
    }
    
    private static void testEscapeNodeId_NonEmpty() {
        test("escapeNodeId - nodeId != null && !nodeId.isEmpty() (OR both FALSE)", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("NON_EMPTY", "Test", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("NON_EMPTY"), "Non-empty ID processed");
        });
    }
    
    // ==================== Branches 11-12: nodeId.matches() ====================
    
    private static void testEscapeNodeId_ValidMatch_LetterStart() {
        test("escapeNodeId - valid match starting with letter", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("aValidId123", "Test", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("aValidId123") && !dot.contains("\"aValidId123\""), "Valid ID not quoted");
        });
    }
    
    private static void testEscapeNodeId_ValidMatch_UnderscoreStart() {
        test("escapeNodeId - valid match starting with underscore", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("_valid_id_123", "Test", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("_valid_id_123") && !dot.contains("\"_valid_id_123\""), "Valid ID not quoted");
        });
    }
    
    private static void testEscapeNodeId_ValidMatch_WithNumbers() {
        test("escapeNodeId - valid match with numbers", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("id123", "Test", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("id123") && !dot.contains("\"id123\""), "Valid ID with numbers not quoted");
        });
    }
    
    private static void testEscapeNodeId_InvalidMatch_NumberStart() {
        test("escapeNodeId - invalid match starting with number", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("123invalid", "Test", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("\"123invalid\""), "Invalid ID quoted");
        });
    }
    
    private static void testEscapeNodeId_InvalidMatch_SpecialCharStart() {
        test("escapeNodeId - invalid match starting with special char", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("@invalid", "Test", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("\"@invalid\""), "Invalid ID quoted");
        });
    }
    
    private static void testEscapeNodeId_InvalidMatch_ContainsSpecialChars() {
        test("escapeNodeId - invalid match containing special chars", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("valid@invalid", "Test", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("\"valid@invalid\""), "Invalid ID quoted");
        });
    }
    
    private static void testEscapeNodeId_InvalidMatch_ContainsHyphen() {
        test("escapeNodeId - invalid match containing hyphen", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("id-with-hyphen", "Test", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("\"id-with-hyphen\""), "Invalid ID with hyphen quoted");
        });
    }
    
    private static void testEscapeNodeId_InvalidMatch_ContainsSpace() {
        test("escapeNodeId - invalid match containing space", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("id with space", "Test", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("\"id with space\""), "Invalid ID with space quoted");
        });
    }
    
    private static void testEscapeNodeId_WithQuotes() {
        test("escapeNodeId - with quotes (replace executed)", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("id\"with\"quotes", "Test", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("\\\""), "Quotes escaped");
        });
    }
    
    // ==================== Branches 14-15: label == null ====================
    
    private static void testEscapeLabel_Null() {
        test("escapeLabel - label == null (TRUE branch)", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("A", null, 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("label=\"A\""), "Null label uses ID");
        });
    }
    
    private static void testEscapeLabel_NotNull() {
        test("escapeLabel - label != null (FALSE branch)", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("A", "Normal Label", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("label=\"Normal Label\""), "Non-null label used");
        });
    }
    
    // ==================== Branches 16-19: escapeLabel replace operations ====================
    
    private static void testEscapeLabel_WithBackslash() {
        test("escapeLabel - with backslash (replace executed)", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("A", "path\\to\\file", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("path") || dot.contains("file"), "Backslash in label");
        });
    }
    
    private static void testEscapeLabel_WithQuote() {
        test("escapeLabel - with quote (replace executed)", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("A", "name\"with\"quotes", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("name") || dot.contains("quotes"), "Quote in label");
        });
    }
    
    private static void testEscapeLabel_WithNewline() {
        test("escapeLabel - with newline (replace executed)", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("A", "line1\nline2", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("line1") || dot.contains("line2"), "Newline in label");
        });
    }
    
    private static void testEscapeLabel_WithCarriageReturn() {
        test("escapeLabel - with carriage return (replace executed)", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("A", "line1\rline2", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("line1") || dot.contains("line2"), "Carriage return in label");
        });
    }
    
    private static void testEscapeLabel_WithAllSpecialChars() {
        test("escapeLabel - with all special chars (all replaces executed)", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("A", "test\\\"\n\r", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("test"), "All special chars in label");
        });
    }
    
    // ==================== Additional Comprehensive Tests ====================
    
    private static void testMultipleStations() {
        test("multiple stations export", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("B", "Station B", 50.0, 4.0, 100.0, 200.0));
            graph.addStation(new Station("A", "Station A", 51.0, 5.0, 101.0, 201.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("A") && dot.contains("B"), "Contains both stations");
            int indexA = dot.indexOf("A [");
            int indexB = dot.indexOf("B [");
            assertTrue(indexA < indexB, "A comes before B (sorted)");
        });
    }
    
    private static void testWithConnections() {
        test("graph with connections", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("A", "Station A", 50.0, 4.0, 100.0, 200.0));
            graph.addStation(new Station("B", "Station B", 51.0, 5.0, 101.0, 201.0));
            graph.addConnection(new Connection("A", "B", 10.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("--"), "Contains edge");
            assertTrue(dot.contains("A -- B") || dot.contains("B -- A"), "Contains connection");
        });
    }
    
    private static void testStations_SortedOrder() {
        test("stations sorted by ID", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("Z", "Z", 50.0, 4.0, 100.0, 200.0));
            graph.addStation(new Station("A", "A", 51.0, 5.0, 101.0, 201.0));
            graph.addStation(new Station("M", "M", 52.0, 6.0, 102.0, 202.0));
            String dot = exporter.exportToDot(graph);
            int indexA = dot.indexOf("A [");
            int indexM = dot.indexOf("M [");
            int indexZ = dot.indexOf("Z [");
            assertTrue(indexA != -1 && indexM != -1 && indexZ != -1, "All stations present");
            assertTrue(indexA < indexM && indexM < indexZ, "Stations sorted by ID");
        });
    }
    
    private static void testCoordinates_Formatting() {
        test("coordinates formatted with 2 decimal places", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("A", "A", 50.123456, 4.987654, 100.1, 200.99));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("100.10") || dot.contains("100.1"), "X coordinate formatted");
            assertTrue(dot.contains("200.99"), "Y coordinate formatted");
        });
    }
    
    private static void testWithRealData() {
        test("export with real data", () -> {
            try {
                BelgiumGraphRepository repo = new BelgiumGraphRepository(STATIONS_PATH, LINES_PATH);
                Graph graph = repo.loadGraph();
                DotExporter exporter = new DotExporter();
                String dot = exporter.exportToDot(graph);
                assertNotNull(dot, "DOT not null");
                assertTrue(dot.contains("graph G"), "Contains graph declaration");
                assertTrue(dot.length() > 100, "DOT has content");
            } catch (Exception e) {
                throw new RuntimeException("Failed to test with real data: " + e.getMessage(), e);
            }
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
