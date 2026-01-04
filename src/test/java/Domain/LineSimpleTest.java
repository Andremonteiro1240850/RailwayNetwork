package Domain;

/**
 * Simple test class for Line (Domain class) without JUnit dependencies.
 * Tests all validation branches in the constructor.
 */
public class LineSimpleTest {
    
    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;
    
    public static void main(String[] args) {
        System.out.println("=== LINE SIMPLE TESTS (DOMAIN) ===\n");
        
        // Constructor validation branches
        testConstructor_DepartureStationId_Null();
        testConstructor_DepartureStationId_Empty();
        testConstructor_DepartureStationId_TrimmedEmpty();
        testConstructor_ArrivalStationId_Null();
        testConstructor_ArrivalStationId_Empty();
        testConstructor_ArrivalStationId_TrimmedEmpty();
        testConstructor_SameDepartureAndArrival();
        testConstructor_NegativeDistance();
        testConstructor_NegativeCapacity();
        testConstructor_Valid();
        
        // Getters
        testGetDepartureStationId();
        testGetArrivalStationId();
        testGetDistance();
        testGetCapacity();
        testGetCost();
        testGetCost_Negative();
        
        // toString
        testToString();
        
        System.out.println("\n=== TEST SUMMARY ===");
        System.out.println("Tests run: " + testsRun);
        System.out.println("Tests passed: " + testsPassed);
        System.out.println("Tests failed: " + testsFailed);
    }
    
    // ==================== Constructor Validation Branches ====================
    
    private static void testConstructor_DepartureStationId_Null() {
        test("constructor with null departureStationId throws exception", () -> {
            try {
                new Line(null, "B", 10.0, 100, 5.0);
                throw new AssertionError("Should throw exception");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("Departure") || e.getMessage().contains("null"), "Error mentions departure or null");
            }
        });
    }
    
    private static void testConstructor_DepartureStationId_Empty() {
        test("constructor with empty departureStationId throws exception", () -> {
            try {
                new Line("", "B", 10.0, 100, 5.0);
                throw new AssertionError("Should throw exception");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("Departure") || e.getMessage().contains("empty"), "Error mentions departure or empty");
            }
        });
    }
    
    private static void testConstructor_DepartureStationId_TrimmedEmpty() {
        test("constructor with trimmed empty departureStationId throws exception", () -> {
            try {
                new Line("   ", "B", 10.0, 100, 5.0);
                throw new AssertionError("Should throw exception");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("Departure") || e.getMessage().contains("empty"), "Error mentions departure or empty");
            }
        });
    }
    
    private static void testConstructor_ArrivalStationId_Null() {
        test("constructor with null arrivalStationId throws exception", () -> {
            try {
                new Line("A", null, 10.0, 100, 5.0);
                throw new AssertionError("Should throw exception");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("Arrival") || e.getMessage().contains("null"), "Error mentions arrival or null");
            }
        });
    }
    
    private static void testConstructor_ArrivalStationId_Empty() {
        test("constructor with empty arrivalStationId throws exception", () -> {
            try {
                new Line("A", "", 10.0, 100, 5.0);
                throw new AssertionError("Should throw exception");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("Arrival") || e.getMessage().contains("empty"), "Error mentions arrival or empty");
            }
        });
    }
    
    private static void testConstructor_ArrivalStationId_TrimmedEmpty() {
        test("constructor with trimmed empty arrivalStationId throws exception", () -> {
            try {
                new Line("A", "   ", 10.0, 100, 5.0);
                throw new AssertionError("Should throw exception");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("Arrival") || e.getMessage().contains("empty"), "Error mentions arrival or empty");
            }
        });
    }
    
    private static void testConstructor_SameDepartureAndArrival() {
        test("constructor with same departure and arrival throws exception", () -> {
            try {
                new Line("A", "A", 10.0, 100, 5.0);
                throw new AssertionError("Should throw exception");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("same") || e.getMessage().contains("Departure and arrival"), "Error mentions same stations");
            }
        });
    }
    
    private static void testConstructor_NegativeDistance() {
        test("constructor with negative distance throws exception", () -> {
            try {
                new Line("A", "B", -10.0, 100, 5.0);
                throw new AssertionError("Should throw exception");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("Distance") || e.getMessage().contains("negative"), "Error mentions distance or negative");
            }
        });
    }
    
    private static void testConstructor_NegativeCapacity() {
        test("constructor with negative capacity throws exception", () -> {
            try {
                new Line("A", "B", 10.0, -100, 5.0);
                throw new AssertionError("Should throw exception");
            } catch (IllegalArgumentException e) {
                assertTrue(e.getMessage().contains("Capacity") || e.getMessage().contains("negative"), "Error mentions capacity or negative");
            }
        });
    }
    
    private static void testConstructor_Valid() {
        test("constructor with valid parameters", () -> {
            Line line = new Line("A", "B", 10.0, 100, 5.0);
            assertTrue(line != null, "Line created");
            assertTrue(line.getDepartureStationId().equals("A"), "Departure ID is A");
            assertTrue(line.getArrivalStationId().equals("B"), "Arrival ID is B");
        });
    }
    
    // ==================== Getters ====================
    
    private static void testGetDepartureStationId() {
        test("getDepartureStationId returns correct value", () -> {
            Line line = new Line("StationA", "StationB", 10.0, 100, 5.0);
            assertTrue(line.getDepartureStationId().equals("StationA"), "Departure ID matches");
        });
    }
    
    private static void testGetArrivalStationId() {
        test("getArrivalStationId returns correct value", () -> {
            Line line = new Line("StationA", "StationB", 10.0, 100, 5.0);
            assertTrue(line.getArrivalStationId().equals("StationB"), "Arrival ID matches");
        });
    }
    
    private static void testGetDistance() {
        test("getDistance returns correct value", () -> {
            Line line = new Line("A", "B", 15.5, 100, 5.0);
            assertTrue(line.getDistance() == 15.5, "Distance matches");
        });
    }
    
    private static void testGetCapacity() {
        test("getCapacity returns correct value", () -> {
            Line line = new Line("A", "B", 10.0, 200, 5.0);
            assertTrue(line.getCapacity() == 200, "Capacity matches");
        });
    }
    
    private static void testGetCost() {
        test("getCost returns correct value", () -> {
            Line line = new Line("A", "B", 10.0, 100, 7.5);
            assertTrue(line.getCost() == 7.5, "Cost matches");
        });
    }
    
    private static void testGetCost_Negative() {
        test("getCost can return negative value", () -> {
            Line line = new Line("A", "B", 10.0, 100, -5.0);
            assertTrue(line.getCost() == -5.0, "Negative cost is allowed");
        });
    }
    
    // ==================== toString ====================
    
    private static void testToString() {
        test("toString returns formatted string", () -> {
            Line line = new Line("A", "B", 10.5, 100, 5.0);
            String str = line.toString();
            assertTrue(str != null && str.length() > 0, "String not null");
            assertTrue(str.contains("A"), "Contains departure");
            assertTrue(str.contains("B"), "Contains arrival");
            assertTrue(str.contains("10.50"), "Contains formatted distance");
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

