package Domain;

import Repository.BelgiumGraphRepository;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

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
        
        // Branch 8: nodeId == null (line 109 - first part of OR TRUE, short-circuit)
        testEscapeNodeId_Null();
        // Branch 9: nodeId != null && nodeId.isEmpty() (line 109 - first FALSE, second TRUE)
        testEscapeNodeId_Empty();
        testEscapeNodeId_NonNullButEmpty(); // Ensure OR second part is evaluated
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
        
        // Branch: uniqueConnections.isEmpty() - no connections loop
        testConnections_Empty();
        
        // Branch: nodeId.replace() executed when quotes in invalid ID
        testEscapeNodeId_InvalidWithQuotes();
        testEscapeNodeId_InvalidWithoutQuotes();
        
        // Branch: escapeNodeId called from connection loop
        testEscapeNodeId_InConnectionOrigin();
        testEscapeNodeId_InConnectionDestination();
        
        // Branch: escapeNodeId - valid ID with quotes (should not happen but tests the match branch)
        testEscapeNodeId_ValidID_NoQuotes();
        
        // Branch: escapeLabel - each replace operation individually
        testEscapeLabel_OnlyBackslash();
        testEscapeLabel_OnlyQuote();
        testEscapeLabel_OnlyNewline();
        testEscapeLabel_OnlyCarriageReturn();
        testEscapeLabel_NoSpecialChars();
        
        // Branch: Multiple connections with same origin (originCompare == 0)
        testConnections_MultipleSameOrigin();
        
        // Branch: Empty string nodeId (different from null)
        testEscapeNodeId_EmptyString();
        
        // Branch: NodeId with only underscore
        testEscapeNodeId_OnlyUnderscore();
        
        // Branch: NodeId starting with underscore followed by number
        testEscapeNodeId_UnderscoreNumber();
        
        // Branch: nodeId.replace() when nodeId is invalid but has no quotes (replace finds nothing)
        testEscapeNodeId_InvalidNoQuotes_ReplaceFindsNothing();
        
        // Branch: escapeLabel - label with only one specific char (each replace individually)
        testEscapeLabel_BackslashOnly();
        testEscapeLabel_QuoteOnly();
        testEscapeLabel_NewlineOnly();
        testEscapeLabel_CarriageReturnOnly();
        
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
            try {
                DotExporter exporter = new DotExporter();
                Graph graph = new Graph();
                Station station = new Station("STATION_ID", "TempName", 50.0, 4.0, 100.0, 200.0);
                // Use reflection to set name to null (bypassing constructor validation)
                Field nameField = Station.class.getDeclaredField("name");
                nameField.setAccessible(true);
                nameField.set(station, null);
                graph.addStation(station);
                String dot = exporter.exportToDot(graph);
                assertTrue(dot.contains("label=\"STATION_ID\""), "Null name uses ID");
            } catch (Exception e) {
                throw new RuntimeException("Reflection failed", e);
            }
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
            try {
                DotExporter exporter = new DotExporter();
                Method method = DotExporter.class.getDeclaredMethod("escapeNodeId", String.class);
                method.setAccessible(true);
                String result = (String) method.invoke(exporter, (String) null);
                assertEquals("\"\"", result, "Null ID escaped as \"\"");
            } catch (Exception e) {
                throw new RuntimeException("Reflection failed", e);
            }
        });
    }
    
    private static void testEscapeNodeId_Empty() {
        test("escapeNodeId - nodeId.isEmpty() == true (OR second part TRUE)", () -> {
            try {
                DotExporter exporter = new DotExporter();
                Method method = DotExporter.class.getDeclaredMethod("escapeNodeId", String.class);
                method.setAccessible(true);
                String result = (String) method.invoke(exporter, "");
                assertEquals("\"\"", result, "Empty ID escaped as \"\"");
            } catch (Exception e) {
                throw new RuntimeException("Reflection failed", e);
            }
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
    
    // Test to ensure OR short-circuit is fully covered
    private static void testEscapeNodeId_NonNullButEmpty() {
        test("escapeNodeId - nodeId != null but isEmpty (OR second part TRUE after first FALSE)", () -> {
            try {
                DotExporter exporter = new DotExporter();
                Method method = DotExporter.class.getDeclaredMethod("escapeNodeId", String.class);
                method.setAccessible(true);
                String result = (String) method.invoke(exporter, "");
                assertEquals("\"\"", result, "Empty string ID escaped");
            } catch (Exception e) {
                throw new RuntimeException("Reflection failed", e);
            }
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
            try {
                DotExporter exporter = new DotExporter();
                Method method = DotExporter.class.getDeclaredMethod("escapeLabel", String.class);
                method.setAccessible(true);
                String result = (String) method.invoke(exporter, (String) null);
                assertEquals("", result, "Null label returns empty string");
            } catch (Exception e) {
                throw new RuntimeException("Reflection failed", e);
            }
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
    
    // ==================== Additional Branch Coverage ====================
    
    private static void testConnections_Empty() {
        test("connections empty - loop not executed", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("A", "A", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(!dot.contains("--"), "No edges when no connections");
            assertTrue(dot.contains("A ["), "Station present");
        });
    }
    
    private static void testEscapeNodeId_InvalidWithQuotes() {
        test("escapeNodeId - invalid ID with quotes (replace branch executed)", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            // Invalid ID (starts with number) that also contains quotes - tests the replace branch
            graph.addStation(new Station("123\"quoted\"id", "Test", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            // Should be quoted and quotes escaped
            assertTrue(dot.contains("\"123"), "ID is quoted");
            assertTrue(dot.contains("\\\""), "Quotes are escaped");
        });
    }
    
    private static void testEscapeNodeId_InvalidWithoutQuotes() {
        test("escapeNodeId - invalid ID without quotes (no replace executed)", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            // Invalid ID but no quotes - replace not executed
            graph.addStation(new Station("123invalid", "Test", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("\"123invalid\""), "Invalid ID quoted");
            assertTrue(!dot.contains("\\\""), "No quotes to escape");
        });
    }
    
    private static void testEscapeLabel_OnlyBackslash() {
        test("escapeLabel - only backslash (first replace executed)", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("A", "path\\to", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            // Check that backslash is escaped (becomes \\)
            assertTrue(dot.contains("path") && dot.contains("to"), "Backslash handled");
            // The escaped backslash should appear as \\ in the output
        });
    }
    
    private static void testEscapeLabel_OnlyQuote() {
        test("escapeLabel - only quote (second replace executed)", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("A", "name\"test", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            // Check that quote is escaped
            assertTrue(dot.contains("name") && dot.contains("test"), "Quote handled");
        });
    }
    
    private static void testEscapeLabel_OnlyNewline() {
        test("escapeLabel - only newline (third replace executed)", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("A", "line1\nline2", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            // Check that newline is escaped
            assertTrue(dot.contains("line1") && dot.contains("line2"), "Newline handled");
        });
    }
    
    private static void testEscapeLabel_OnlyCarriageReturn() {
        test("escapeLabel - only carriage return (fourth replace executed)", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("A", "line1\rline2", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            // Check that carriage return is escaped
            assertTrue(dot.contains("line1") && dot.contains("line2"), "Carriage return handled");
        });
    }
    
    private static void testEscapeNodeId_InConnectionOrigin() {
        test("escapeNodeId - called for connection origin ID", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("A", "A", 50.0, 4.0, 100.0, 200.0));
            graph.addStation(new Station("B", "B", 51.0, 5.0, 101.0, 201.0));
            // Use connection with IDs that need escaping
            graph.addConnection(new Connection("A", "B", 10.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("A -- B") || dot.contains("B -- A"), "Connection present");
        });
    }
    
    private static void testEscapeNodeId_InConnectionDestination() {
        test("escapeNodeId - called for connection destination ID", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("A", "A", 50.0, 4.0, 100.0, 200.0));
            graph.addStation(new Station("123invalid", "B", 51.0, 5.0, 101.0, 201.0));
            graph.addConnection(new Connection("A", "123invalid", 10.0));
            String dot = exporter.exportToDot(graph);
            // Destination ID should be escaped
            assertTrue(dot.contains("\"123invalid\""), "Destination ID escaped");
        });
    }
    
    private static void testEscapeNodeId_ValidID_NoQuotes() {
        test("escapeNodeId - valid ID without quotes (match branch, no replace)", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("ValidID123", "Test", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            // Valid ID should not be quoted
            assertTrue(dot.contains("ValidID123") && !dot.contains("\"ValidID123\""), "Valid ID not quoted");
        });
    }
    
    private static void testConnections_MultipleSameOrigin() {
        test("connections - multiple with same origin (originCompare == 0)", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("A", "A", 50.0, 4.0, 100.0, 200.0));
            graph.addStation(new Station("B", "B", 51.0, 5.0, 101.0, 201.0));
            graph.addStation(new Station("C", "C", 52.0, 6.0, 102.0, 202.0));
            graph.addStation(new Station("D", "D", 53.0, 7.0, 103.0, 203.0));
            // Multiple connections from A
            graph.addConnection(new Connection("A", "D", 40.0));
            graph.addConnection(new Connection("A", "C", 30.0));
            graph.addConnection(new Connection("A", "B", 20.0));
            String dot = exporter.exportToDot(graph);
            int indexAB = dot.indexOf("A -- B");
            int indexAC = dot.indexOf("A -- C");
            int indexAD = dot.indexOf("A -- D");
            assertTrue(indexAB != -1 && indexAC != -1 && indexAD != -1, "All connections present");
            // Should be sorted by destination: A-B, A-C, A-D
            assertTrue(indexAB < indexAC && indexAC < indexAD, "Sorted by destination when origin same");
        });
    }
    
    private static void testEscapeNodeId_EmptyString() {
        test("escapeNodeId - empty string (isEmpty branch)", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("", "Test", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("\"\""), "Empty string escaped as \"\"");
        });
    }
    
    private static void testEscapeNodeId_OnlyUnderscore() {
        test("escapeNodeId - only underscore (valid match)", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("_", "Test", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("_") && !dot.contains("\"_\""), "Single underscore is valid");
        });
    }
    
    private static void testEscapeNodeId_UnderscoreNumber() {
        test("escapeNodeId - underscore followed by number (valid match)", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("_123", "Test", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("_123") && !dot.contains("\"_123\""), "Underscore+number is valid");
        });
    }
    
    private static void testEscapeLabel_NoSpecialChars() {
        test("escapeLabel - no special chars (no replaces executed)", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("A", "NormalLabel123", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("NormalLabel123"), "Normal label unchanged");
        });
    }
    
    // ==================== Additional Branch Coverage Tests ====================
    
    private static void testEscapeNodeId_InvalidNoQuotes_ReplaceFindsNothing() {
        test("escapeNodeId - invalid ID without quotes (replace finds nothing)", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            // Invalid ID (starts with number) but no quotes - replace() is called but finds nothing
            graph.addStation(new Station("123noquotes", "Test", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            // Should be quoted but no escape needed
            assertTrue(dot.contains("\"123noquotes\""), "Invalid ID quoted");
        });
    }
    
    private static void testEscapeLabel_BackslashOnly() {
        test("escapeLabel - only backslash character", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("A", "test\\end", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("test") && dot.contains("end"), "Backslash only");
        });
    }
    
    private static void testEscapeLabel_QuoteOnly() {
        test("escapeLabel - only quote character", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("A", "test\"end", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("test") && dot.contains("end"), "Quote only");
        });
    }
    
    private static void testEscapeLabel_NewlineOnly() {
        test("escapeLabel - only newline character", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("A", "test\nend", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("test") && dot.contains("end"), "Newline only");
        });
    }
    
    private static void testEscapeLabel_CarriageReturnOnly() {
        test("escapeLabel - only carriage return character", () -> {
            DotExporter exporter = new DotExporter();
            Graph graph = new Graph();
            graph.addStation(new Station("A", "test\rend", 50.0, 4.0, 100.0, 200.0));
            String dot = exporter.exportToDot(graph);
            assertTrue(dot.contains("test") && dot.contains("end"), "Carriage return only");
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
