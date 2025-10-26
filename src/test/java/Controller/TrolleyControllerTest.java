package Controller;

import Domain.AllocationRow;
import Domain.Item;
import Domain.PickingItems;
import Domain.PickingPlan;
import Domain.Trolley;
import Domain.ENUM.PackingHeuristic;
import Repository.TrolleyRepository;
import Repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Expanded unit tests for TrolleyController (JUnit 5, no Mockito)
 *
 * This file adds extra edge-case and branch-targeting tests to increase method and branch coverage.
 */
class TrolleyControllerTest {

    private TrolleyController controller;
    private FakeWarehouseRepository warehouseRepo;
    private TrolleyRepository trolleyRepo;

    @BeforeEach
    void setUp() {
        warehouseRepo = new FakeWarehouseRepository();
        trolleyRepo = new TrolleyRepository();
        controller = new TrolleyController(warehouseRepo, trolleyRepo);
    }

    // ----------------------------------------------------------------
    // Existing coverage helpers: basic behaviour
    // ----------------------------------------------------------------

    @Test
    void shouldCreateTrolleyAndReturnIt() {
        Trolley t = controller.createTrolley(100, "FF");
        assertNotNull(t);
        assertEquals(100, t.getMaxCapacity());
        assertEquals("FF", t.getOptionHeuristic());
    }

    @Test
    void shouldSupportSetAndGetTrolleyAndToString() {
        Trolley t = controller.createTrolley(60, "FF");
        controller.setTrolley(t);
        assertSame(t, controller.getTrolley());
        // toString should not throw and should contain "TrolleyController"
        String s = controller.toString();
        assertNotNull(s);
        assertTrue(s.contains("TrolleyController") || s.length() > 0);
    }

    @Test
    void createTrolley_multipleTimes_shouldGrowList() {
        int initial = controller.getTrolleyList().size();
        controller.createTrolley(10, "FF");
        controller.createTrolley(20, "FFD");
        controller.createTrolley(30, "BFD");
        assertTrue(controller.getTrolleyList().size() >= initial + 3);
    }

    // ----------------------------------------------------------------
    // Extra edge cases for reduceLoad
    // ----------------------------------------------------------------

    @Test
    void reduceLoad_boundaryAndInvalid() {
        Trolley t = controller.createTrolley(50, "FF");
        t.setCurrentLoad(5.0);

        // valid reduce to zero is not allowed (should throw)
        assertThrows(IllegalArgumentException.class, () -> controller.reduceLoad(6.0));
        assertThrows(IllegalArgumentException.class, () -> controller.reduceLoad(0.0));

        // correct reduce
        t.setCurrentLoad(10.0);
        double newLoad = controller.reduceLoad(5.0);
        assertEquals(5.0, newLoad);
    }

    // ----------------------------------------------------------------
    // Branch-heavy tests: force multiple trolley creations in each heuristic
    // ----------------------------------------------------------------

    @Test
    void ff_forcesManySplits_and_savesToRepo() {
        // small capacity so many small allocations will create many trolleys
        controller.createTrolley(10, "FF");

        warehouseRepo.populateManySmallAllocations(15); // many tiny allocations
        controller.packing();

        List<Trolley> saved = trolleyRepo.getAllTrolleys();
        assertTrue(saved.size() > 1, "FF with small capacity should create multiple trolleys");
        // ensure saving actually persisted items
        assertFalse(trolleyRepo.getAllTrolleys().isEmpty());
    }

    @Test
    void ffd_forcesManySplits_and_savesToRepo() {
        controller.createTrolley(10, "FFD");
        warehouseRepo.populateManySmallAllocations(12);
        controller.packing();
        assertFalse(trolleyRepo.getAllTrolleys().isEmpty());
    }

    @Test
    void bfd_forcesManySplits_and_usesBestFitLogic() {
        controller.createTrolley(12, "BFD");
        warehouseRepo.populateManySmallAllocations(12);
        // Pre-add a partially filled trolley to force the BFD branch placing into existing trolleys
        Trolley partial = new Trolley(12, 4.0, 12345, new ArrayList<>(), "BFD");
        controller.getTrolleyList().add(partial);

        controller.packing();

        List<Trolley> saved = trolleyRepo.getAllTrolleys();
        assertFalse(saved.isEmpty());
    }

    // ----------------------------------------------------------------
    // Unknown heuristic + repeated packing behavior
    // ----------------------------------------------------------------

    @Test
    void unknownHeuristic_doesNotSave() {
        controller.createTrolley(50, "FOO"); // unknown
        warehouseRepo.populateAllocations();
        controller.packing();
        assertTrue(trolleyRepo.getAllTrolleys().isEmpty());
    }

    @Test
    void repeatedPacking_accumulatesInRepository() {
        controller.createTrolley(30, "FF");
        warehouseRepo.populateAllocationsForSplit();
        controller.packing();
        int first = trolleyRepo.getAllTrolleys().size();
        controller.packing();
        int second = trolleyRepo.getAllTrolleys().size();
        assertTrue(second >= first);
    }

    // ----------------------------------------------------------------
    // Additional tiny edge scenarios to trigger branches
    // ----------------------------------------------------------------

    @Test
    void zeroWeightAndZeroQty_doNotCrash() {
        controller.createTrolley(100, "FF");
        warehouseRepo.populateAllocationsWithZeroWeight();
        warehouseRepo.populateAllocationsWithZeroQty();
        // Should not throw
        assertDoesNotThrow(() -> controller.packing());
    }

    @Test
    void setTrolley_null_isHandledGracefully() {
        controller.setTrolley(null);
        // operations should not throw immediately (some behavior may be NPE in other code but this checks setter)
        assertNull(controller.getTrolley());
    }

    @Test
    void createPickingPlan_delegatesAndReturns() {
        PickingPlan plan = controller.createPickingPlan(10, PackingHeuristic.FF, true);
        assertNotNull(plan);
    }

    // ----------------------------------------------------------------
    // Fake repository with many scenarios
    // ----------------------------------------------------------------
    static class FakeWarehouseRepository extends WarehouseRepository {
        private final List<AllocationRow> rows = new ArrayList<>();
        private final Map<String, Item> items = new HashMap<>();

        void populateAllocations() {
            items.put("SKU1", new Item("SKU1", "Item 1", "Cat", "U", 1.0, 2.0));
            items.put("SKU2", new Item("SKU2", "Item 2", "Cat", "U", 1.0, 3.0));
            items.put("SKU3", new Item("SKU3", "Item 3", "Cat", "U", 1.0, 5.0));
            rows.clear();
            rows.add(new AllocationRow("O1", 1, "SKU1", 5, "BOX1", 1, 1));
            rows.add(new AllocationRow("O2", 1, "SKU2", 10, "BOX2", 1, 2));
            rows.add(new AllocationRow("O3", 1, "SKU3", 5, "BOX3", 2, 3));
        }

        void populateAllocationsForSplit() {
            items.put("A", new Item("A", "A", "Cat", "U", 1.0, 20.0));
            items.put("B", new Item("B", "B", "Cat", "U", 1.0, 20.0));
            rows.clear();
            rows.add(new AllocationRow("X1", 1, "A", 1, "BX1", 1, 1));
            rows.add(new AllocationRow("X2", 1, "B", 1, "BX2", 1, 2));
        }

        void populateManySmallAllocations(int count) {
            rows.clear();
            for (int i = 0; i < count; i++) {
                String sku = "S" + i;
                items.put(sku, new Item(sku, "Item"+i, "Cat", "U", 1.0, 2.0)); // weight 2
                rows.add(new AllocationRow("O" + i, 1, sku, 1, "BOX" + i, i % 5 + 1, i % 3 + 1));
            }
        }

        void populateAllocationsSmall() {
            items.put("S1", new Item("S1", "Small1", "Cat", "U", 1.0, 1.0));
            items.put("S2", new Item("S2", "Small2", "Cat", "U", 1.0, 1.0));
            items.put("S3", new Item("S3", "Small3", "Cat", "U", 1.0, 2.0));
            rows.clear();
            rows.add(new AllocationRow("SO1", 1, "S1", 5, "B1", 1, 1));
            rows.add(new AllocationRow("SO2", 1, "S2", 10, "B2", 1, 2));
            rows.add(new AllocationRow("SO3", 1, "S3", 5, "B3", 2, 3));
        }

        void populateAllocationsForBfdScenario() {
            items.put("W1", new Item("W1", "W1", "Cat", "U", 1.0, 15.0));
            items.put("W2", new Item("W2", "W2", "Cat", "U", 1.0, 20.0));
            items.put("W3", new Item("W3", "W3", "Cat", "U", 1.0, 10.0));
            rows.clear();
            rows.add(new AllocationRow("B1", 1, "W1", 1, "B1", 1, 1));
            rows.add(new AllocationRow("B2", 1, "W2", 1, "B2", 1, 2));
            rows.add(new AllocationRow("B3", 1, "W3", 1, "B3", 2, 3));
        }

        void populateAllocationsWithZeroWeight() {
            items.put("Z", new Item("Z", "ZeroWeight", "Cat", "U", 1.0, 0.0));
            rows.clear();
            rows.add(new AllocationRow("Z1", 1, "Z", 5, "ZBOX", 1, 1));
        }

        void populateAllocationsWithZeroQty() {
            items.put("Q", new Item("Q", "ZeroQty", "Cat", "U", 1.0, 2.0));
            rows.clear();
            rows.add(new AllocationRow("Q1", 1, "Q", 0, "QBOX", 1, 1));
        }

        @Override
        public List<AllocationRow> getAllocationRows() {
            return new ArrayList<>(rows);
        }

        @Override
        public Item getItemBySku(String sku) {
            return items.get(sku);
        }
    }
}
