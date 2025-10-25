package Controller;

import Domain.AllocationRow;
import Domain.Item;
import Domain.PickingItems;
import Domain.Trolley;
import Repository.TrolleyRepository;
import Repository.WarehouseRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test class for TrolleyController using basic assertions (no external JUnit)
 */
public class TrolleyControllerTest {

    private WarehouseRepository warehouseRepository;
    private TrolleyRepository trolleyRepository;
    private TrolleyController trolleyController;

    private void setUp() {
        warehouseRepository = new WarehouseRepository();
        trolleyRepository = new TrolleyRepository();
        trolleyController = new TrolleyController(warehouseRepository, trolleyRepository);
    }

    // Custom assertion methods
    private void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    private void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError(message + " Expected: " + expected + ", Actual: " + actual);
    }

    private void assertEquals(double expected, double actual, String message) {
        if (Math.abs(expected - actual) > 0.001) {
            throw new AssertionError(message + " Expected: " + expected + ", Actual: " + actual);
        }
    }

    private void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " Expected: " + expected + ", Actual: " + actual);
        }
    }

    private void assertNotNull(Object object, String message) {
        assertTrue(object != null, message);
    }

    private void assertNull(Object object, String message) {
        assertTrue(object == null, message);
    }

    private void assertThrows(Class<? extends Exception> expectedException, Runnable code) {
        try {
            code.run();
            throw new AssertionError("Expected exception: " + expectedException.getSimpleName());
        } catch (Exception e) {
            if (!expectedException.isInstance(e)) {
                throw new AssertionError("Expected " + expectedException.getSimpleName() + " but got " + e.getClass().getSimpleName());
            }
        }
    }

    // Test methods
    public void testConstructor_shouldThrowException_whenWarehouseRepositoryIsNull() {
        setUp();
        assertThrows(IllegalArgumentException.class, () ->
                new TrolleyController(null, trolleyRepository)
        );
        System.out.println("✓ testConstructor_shouldThrowException_whenWarehouseRepositoryIsNull");
    }

    public void testConstructor_shouldThrowException_whenTrolleyRepositoryIsNull() {
        setUp();
        assertThrows(IllegalArgumentException.class, () ->
                new TrolleyController(warehouseRepository, null)
        );
        System.out.println("✓ testConstructor_shouldThrowException_whenTrolleyRepositoryIsNull");
    }

    public void testConstructor_shouldInitializeWithValidRepositories() {
        setUp();
        assertNotNull(trolleyController, "Controller should not be null");
        assertNotNull(trolleyController.getTrolleyList(), "Trolley list should not be null");
        assertTrue(trolleyController.getTrolleyList().isEmpty(), "Trolley list should be empty initially");
        assertNull(trolleyController.getTrolley(), "Current trolley should be null initially");
        System.out.println("✓ testConstructor_shouldInitializeWithValidRepositories");
    }

    public void testCreateTrolley_shouldCreateTrolleyWithCorrectParameters() {
        setUp();
        Trolley trolley = trolleyController.createTrolley(100, "FF");

        assertNotNull(trolley, "Trolley should not be null");
        assertEquals(100, trolley.getMaxCapacity(), "Max capacity should match");
        assertEquals("FF", trolley.getOptionHeuristic(), "Heuristic should match");
        assertEquals(0.0, trolley.getCurrentLoad(), "Current load should be 0");
        assertNotNull(trolley.getPickingItems(), "Picking items list should not be null");
        assertTrue(trolley.getId() >= 1 && trolley.getId() <= 10000, "ID should be in valid range");

        // Verify trolley is stored in controller
        assertEquals(trolley, trolleyController.getTrolley(), "Current trolley should match created trolley");
        assertEquals(1, trolleyController.getTrolleyList().size(), "Trolley list should have one trolley");
        assertTrue(trolleyController.getTrolleyList().contains(trolley), "Trolley list should contain created trolley");
        System.out.println("✓ testCreateTrolley_shouldCreateTrolleyWithCorrectParameters");
    }

    public void testCreateTrolley_shouldAddMultipleTrolleysToList() {
        setUp();
        Trolley trolley1 = trolleyController.createTrolley(100, "FF");
        Trolley trolley2 = trolleyController.createTrolley(200, "FFD");

        assertEquals(trolley2, trolleyController.getTrolley(), "Current trolley should be the last created");
        assertEquals(2, trolleyController.getTrolleyList().size(), "Trolley list should have two trolleys");
        assertTrue(trolleyController.getTrolleyList().contains(trolley1), "Trolley list should contain first trolley");
        assertTrue(trolleyController.getTrolleyList().contains(trolley2), "Trolley list should contain second trolley");
        System.out.println("✓ testCreateTrolley_shouldAddMultipleTrolleysToList");
    }

    public void testReduceLoad_shouldReduceCurrentLoad_whenValidWeight() {
        setUp();
        Trolley trolley = trolleyController.createTrolley(100, "FF");
        trolley.setCurrentLoad(50.0);

        double newLoad = trolleyController.reduceLoad(30.0);

        assertEquals(20.0, newLoad, "New load should be 20.0");
        assertEquals(20.0, trolley.getCurrentLoad(), "Trolley current load should be updated to 20.0");
        System.out.println("✓ testReduceLoad_shouldReduceCurrentLoad_whenValidWeight");
    }

    public void testReduceLoad_shouldThrowException_whenWeightIsZero() {
        setUp();
        Trolley trolley = trolleyController.createTrolley(100, "FF");
        trolley.setCurrentLoad(50.0);

        assertThrows(IllegalArgumentException.class, () ->
                trolleyController.reduceLoad(0.0)
        );
        System.out.println("✓ testReduceLoad_shouldThrowException_whenWeightIsZero");
    }

    public void testReduceLoad_shouldThrowException_whenWeightIsNegative() {
        setUp();
        Trolley trolley = trolleyController.createTrolley(100, "FF");
        trolley.setCurrentLoad(50.0);

        assertThrows(IllegalArgumentException.class, () ->
                trolleyController.reduceLoad(-10.0)
        );
        System.out.println("✓ testReduceLoad_shouldThrowException_whenWeightIsNegative");
    }

    public void testReduceLoad_shouldThrowException_whenWeightExceedsCurrentLoad() {
        setUp();
        Trolley trolley = trolleyController.createTrolley(100, "FF");
        trolley.setCurrentLoad(20.0);

        assertThrows(IllegalArgumentException.class, () ->
                trolleyController.reduceLoad(30.0)
        );
        System.out.println("✓ testReduceLoad_shouldThrowException_whenWeightExceedsCurrentLoad");
    }

    public void testPacking_shouldExecuteFFHeuristic_whenEmptyAllocations() {
        setUp();
        Trolley trolley = trolleyController.createTrolley(100, "FF");

        // No allocations in repository
        trolleyController.packing();

        // Should create no additional trolleys since no allocations
        assertEquals(1, trolleyController.getTrolleyList().size(), "Should only have initial trolley");
        assertEquals(0.0, trolley.getCurrentLoad(), "Current load should remain 0");
        System.out.println("✓ testPacking_shouldExecuteFFHeuristic_whenEmptyAllocations");
    }

    public void testPacking_shouldExecuteFFHeuristic_withSingleItemFittingInTrolley() {
        setUp();
        // Setup test data
        Item item = new Item("SKU001", "Test Item", "Category", "unit", 1.0, 5.0);
        warehouseRepository.saveItems(Arrays.asList(item));

        AllocationRow allocation = new AllocationRow("ORDER001", 1, "SKU001", 2, "BOX001", 1, 1);
        warehouseRepository.saveAllocationRows(Arrays.asList(allocation));

        Trolley trolley = trolleyController.createTrolley(20, "FF");

        trolleyController.packing();

        // Should create one trolley with the item
        assertEquals(1, trolleyController.getTrolleyList().size(), "Should have one trolley");
        Trolley resultTrolley = trolleyController.getTrolleyList().get(0);
        assertEquals(10.0, resultTrolley.getCurrentLoad(), "Current load should be 10.0 (2 items * 5.0 weight)");
        assertEquals(1, resultTrolley.getPickingItems().size(), "Should have one picking item");

        PickingItems pickingItem = resultTrolley.getPickingItems().get(0);
        assertEquals("ORDER001", pickingItem.getOrderId(), "Order ID should match");
        assertEquals("SKU001", pickingItem.getSku(), "SKU should match");
        assertEquals(2, pickingItem.getQty(), "Quantity should match");
        System.out.println("✓ testPacking_shouldExecuteFFHeuristic_withSingleItemFittingInTrolley");
    }

    public void testSetTrolley_shouldUpdateCurrentTrolley() {
        setUp();
        Trolley initialTrolley = trolleyController.createTrolley(100, "FF");
        Trolley newTrolley = new Trolley(200, 50.0, 9999, new ArrayList<>(), "FFD");

        trolleyController.setTrolley(newTrolley);

        assertEquals(newTrolley, trolleyController.getTrolley(), "Current trolley should be updated");
        assertFalse(initialTrolley.equals(trolleyController.getTrolley()), "Current trolley should not be initial trolley");
        System.out.println("✓ testSetTrolley_shouldUpdateCurrentTrolley");
    }

    public void testGetTrolleyList_shouldReturnCopyOfList() {
        setUp();
        trolleyController.createTrolley(100, "FF");
        List<Trolley> firstCall = trolleyController.getTrolleyList();
        List<Trolley> secondCall = trolleyController.getTrolleyList();

        assertFalse(firstCall == secondCall, "Should return different list instances");
        assertEquals(firstCall, secondCall, "List contents should be equal");
        System.out.println("✓ testGetTrolleyList_shouldReturnCopyOfList");
    }

    public void testToString_shouldReturnFormattedString() {
        setUp();
        Trolley trolley = trolleyController.createTrolley(100, "FF");

        String result = trolleyController.toString();

        assertTrue(result.contains("TrolleyController =>"), "Should contain class name");
        assertTrue(result.contains("trolley: " + trolley), "Should contain trolley representation");
        System.out.println("✓ testToString_shouldReturnFormattedString");
    }

    public void testToString_shouldHandleNullTrolley() {
        setUp();
        // Controller with no trolley created.
        String result = trolleyController.toString();

        assertTrue(result.contains("TrolleyController =>"), "Should contain class name");
        assertTrue(result.contains("trolley: null"), "Should indicate null trolley");
        System.out.println("✓ testToString_shouldHandleNullTrolley");
    }

    // Main method to run all tests
    public static void main(String[] args) {
        TrolleyControllerTest test = new TrolleyControllerTest();

        try {
            test.testConstructor_shouldThrowException_whenWarehouseRepositoryIsNull();
            test.testConstructor_shouldThrowException_whenTrolleyRepositoryIsNull();
            test.testConstructor_shouldInitializeWithValidRepositories();
            test.testCreateTrolley_shouldCreateTrolleyWithCorrectParameters();
            test.testCreateTrolley_shouldAddMultipleTrolleysToList();
            test.testReduceLoad_shouldReduceCurrentLoad_whenValidWeight();
            test.testReduceLoad_shouldThrowException_whenWeightIsZero();
            test.testReduceLoad_shouldThrowException_whenWeightIsNegative();
            test.testReduceLoad_shouldThrowException_whenWeightExceedsCurrentLoad();
            test.testPacking_shouldExecuteFFHeuristic_whenEmptyAllocations();
            test.testPacking_shouldExecuteFFHeuristic_withSingleItemFittingInTrolley();
            test.testSetTrolley_shouldUpdateCurrentTrolley();
            test.testGetTrolleyList_shouldReturnCopyOfList();
            test.testToString_shouldReturnFormattedString();
            test.testToString_shouldHandleNullTrolley();

            System.out.println("\n🎉 ALL TESTS PASSED SUCCESSFULLY!");

        } catch (AssertionError e) {
            System.err.println("\n❌ TEST FAILED: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("\n💥 UNEXPECTED ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}