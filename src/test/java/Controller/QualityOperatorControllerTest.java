package Controller;

import Domain.Return;
import Domain.QuarantineInspection;
import Domain.WarehouseManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Complete unit tests for QualityOperatorController without Mockito
 */
class QualityOperatorControllerTest {

    private QualityOperatorController controller;
    private TestInspectionController testInspectionController;

    @TempDir
    Path tempDir;

    // Test implementation of InspectionController for testing
    private static class TestInspectionController extends InspectionController {
        public List<QuarantineInspection> inspections = new ArrayList<>();
        public List<String> auditLogs = new ArrayList<>();
        public int processQuarantineCallCount = 0;
        public String lastSku;
        public int lastQuantity;
        public String lastReason;
        public LocalDate lastExpiryDate;

        @Override
        public String processQuarantine(String sku, int quantity, String returnReason, LocalDate expiryDate) {
            processQuarantineCallCount++;
            lastSku = sku;
            lastQuantity = quantity;
            lastReason = returnReason;
            lastExpiryDate = expiryDate;

            // Create a simple inspection record
            QuarantineInspection inspection = new QuarantineInspection();
            inspection.setInspectionId("TEST-INSP-" + processQuarantineCallCount);
            inspection.setReturnId("TEST-RET-" + processQuarantineCallCount);
            inspection.setInspectedAt(LocalDateTime.now());
            inspections.add(inspection);

            auditLogs.add("Processed: " + sku + " Qty: " + quantity + " Reason: " + returnReason);

            return "SUCCESS: Inspection processed for " + sku;
        }

        @Override
        public List<QuarantineInspection> getInspectionsBySku(String sku) {
            return new ArrayList<>(inspections);
        }

        @Override
        public List<String> getAuditLogs() {
            return new ArrayList<>(auditLogs);
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        testInspectionController = new TestInspectionController();
        controller = new QualityOperatorController();

        // Inject our test inspection controller using reflection
        Field inspectionControllerField = QualityOperatorController.class.getDeclaredField("inspectionController");
        inspectionControllerField.setAccessible(true);
        inspectionControllerField.set(controller, testInspectionController);

        // Initialize scanner with empty input
        Field scannerField = QualityOperatorController.class.getDeclaredField("scanner");
        scannerField.setAccessible(true);
        scannerField.set(controller, new Scanner(""));
    }

    @Test
    void testShowLogin() {
        // Test login method returns true
        assertTrue(controller.showLogin());
    }

    @Test
    void testLoadReturnsFromCSV_FileNotFound() {
        // Test when CSV file doesn't exist - should handle gracefully
        assertDoesNotThrow(() -> {
            try {
                invokePrivateMethod(controller, "loadReturnsFromCSV", new Class[]{boolean.class}, new Object[]{Boolean.TRUE});
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testLoadReturnsFromCSV_Success() throws Exception {
        // Create test CSV file with valid data
        Path csvFile = tempDir.resolve("returns.csv");
        String csvContent = "return_id,sku,quantity,reason,timestamp,expiry_date\n" +
                "RET001,SKU001,5,customer-remorse,2024-01-15T10:30:00,2024-12-31\n" +
                "RET002,SKU002,3,damaged,2024-01-14T14:20:00,\n" +
                "RET003,SKU003,10,expired,2024-01-16T09:15:00,2023-12-01\n" +
                "RET004,SKU004,2,cycle-count,2024-01-13T16:45:00,2024-06-30\n";
        Files.writeString(csvFile, csvContent);

        setPrivateField(controller, "scanner", new Scanner(""));

        invokePrivateMethod(controller, "loadReturnsFromCSV", new Class[]{boolean.class}, new Object[]{Boolean.TRUE});

        // Verify items were loaded and sorted correctly
        List<Return> quarantineItems = getQuarantineItems();
        assertNotNull(quarantineItems);
        assertEquals(4, quarantineItems.size());

        // Verify sorting: latest timestamp first (USEI05 requirement)
        assertEquals("RET003", quarantineItems.get(0).getReturnId()); // 2024-01-16 (newest)
        assertEquals("RET001", quarantineItems.get(1).getReturnId()); // 2024-01-15
        assertEquals("RET002", quarantineItems.get(2).getReturnId()); // 2024-01-14
        assertEquals("RET004", quarantineItems.get(3).getReturnId()); // 2024-01-13 (oldest)

        // Verify data was parsed correctly
        Return firstItem = quarantineItems.get(0);
        assertEquals("SKU003", firstItem.getSku());
        assertEquals(10, firstItem.getQty());
        assertEquals("EXPIRED", firstItem.getReason()); // Should be mapped from "expired"
        assertNotNull(firstItem.getExpiryDate());
    }

    @Test
    void testLoadReturnsFromCSV_InvalidData() throws Exception {
        // Create CSV with invalid data that should be skipped
        Path csvFile = tempDir.resolve("returns.csv");
        String csvContent = "return_id,sku,quantity,reason,timestamp,expiry_date\n" +
                "RET001,SKU001,invalid_qty,customer-remorse,2024-01-15T10:30:00,2024-12-31\n" +
                "RET002,SKU002,3,damaged,invalid_timestamp,\n" +
                "RET003,SKU003,7,good-reason,2024-01-16T09:15:00,2024-12-31\n";
        Files.writeString(csvFile, csvContent);

        setPrivateField(controller, "scanner", new Scanner(""));

        invokePrivateMethod(controller, "loadReturnsFromCSV", new Class[]{boolean.class}, new Object[]{Boolean.TRUE});

        // Only the valid line should be loaded
        List<Return> quarantineItems = getQuarantineItems();
        assertEquals(1, quarantineItems.size());
        assertEquals("RET003", quarantineItems.get(0).getReturnId());
    }

    @Test
    void testMapReturnReason() throws Exception {
        // Test all reason mappings from CSV to system reasons
        assertEquals("REMORSE", invokePrivateMethod(controller, "mapReturnReason", new Class[]{String.class}, new Object[]{"customer-remorse"}));
        assertEquals("CYCLE_COUNT", invokePrivateMethod(controller, "mapReturnReason", new Class[]{String.class}, new Object[]{"cycle-count"}));
        assertEquals("DAMAGED", invokePrivateMethod(controller, "mapReturnReason", new Class[]{String.class}, new Object[]{"damaged"}));
        assertEquals("EXPIRED", invokePrivateMethod(controller, "mapReturnReason", new Class[]{String.class}, new Object[]{"expired"}));
        assertEquals("REMORSE", invokePrivateMethod(controller, "mapReturnReason", new Class[]{String.class}, new Object[]{"package-opened"}));
        assertEquals("REMORSE", invokePrivateMethod(controller, "mapReturnReason", new Class[]{String.class}, new Object[]{"wrong-item"}));

        // Test edge cases
        assertEquals("REMORSE", invokePrivateMethod(controller, "mapReturnReason", new Class[]{String.class}, new Object[]{"unknown-reason"}));
        assertEquals("REMORSE", invokePrivateMethod(controller, "mapReturnReason", new Class[]{String.class}, new Object[]{""}));
        assertEquals("REMORSE", invokePrivateMethod(controller, "mapReturnReason", new Class[]{String.class}, new Object[]{"   "}));
    }

    @Test
    void testDetermineProcessingReason_ExpiredProduct() throws Exception {
        // Test that expired products are detected
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Return expiredReturn = new Return("RET001", "SKU001", 5, "REMORSE", LocalDateTime.now(), pastDate);

        String result = (String) invokePrivateMethod(controller, "determineProcessingReason",
                new Class[]{Return.class}, new Object[]{expiredReturn});

        assertEquals("EXPIRED", result);
    }

    @Test
    void testDetermineProcessingReason_NotExpired() throws Exception {
        // Test that non-expired products keep their original reason
        LocalDate futureDate = LocalDate.now().plusDays(30);
        Return validReturn = new Return("RET001", "SKU001", 5, "DAMAGED", LocalDateTime.now(), futureDate);

        String result = (String) invokePrivateMethod(controller, "determineProcessingReason",
                new Class[]{Return.class}, new Object[]{validReturn});

        assertEquals("DAMAGED", result);
    }

    @Test
    void testDetermineProcessingReason_NoExpiryDate() throws Exception {
        // Test products without expiry date
        Return noExpiryReturn = new Return("RET001", "SKU001", 5, "REMORSE", LocalDateTime.now(), null);

        String result = (String) invokePrivateMethod(controller, "determineProcessingReason",
                new Class[]{Return.class}, new Object[]{noExpiryReturn});

        assertEquals("REMORSE", result);
    }

    @Test
    void testDetermineActionFromReason() throws Exception {
        // Test action determination logic
        assertEquals("RESTOCK", invokePrivateMethod(controller, "determineActionFromReason",
                new Class[]{String.class}, new Object[]{"REMORSE"}));
        assertEquals("RESTOCK", invokePrivateMethod(controller, "determineActionFromReason",
                new Class[]{String.class}, new Object[]{"CYCLE_COUNT"}));
        assertEquals("DISCARD", invokePrivateMethod(controller, "determineActionFromReason",
                new Class[]{String.class}, new Object[]{"DAMAGED"}));
        assertEquals("DISCARD", invokePrivateMethod(controller, "determineActionFromReason",
                new Class[]{String.class}, new Object[]{"EXPIRED"}));

        // Default case for unknown reasons
        assertEquals("DISCARD", invokePrivateMethod(controller, "determineActionFromReason",
                new Class[]{String.class}, new Object[]{"UNKNOWN_REASON"}));
    }

    @Test
    void testProcessSingleInspection_EmptyQueue() throws Exception {
        // Test processing when queue is empty
        setQuarantineItems(new ArrayList<>());

        assertDoesNotThrow(() -> {
            try {
                invokePrivateMethod(controller, "processSingleInspection", null, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Verify no inspections were processed
        assertEquals(0, testInspectionController.processQuarantineCallCount);
    }

    @Test
    void testProcessSingleInspection_WithItems() throws Exception {
        // Test processing a single item from queue
        List<Return> items = new ArrayList<>();
        LocalDate futureDate = LocalDate.now().plusDays(30);
        Return testReturn = new Return("RET001", "SKU001", 5, "REMORSE", LocalDateTime.now(), futureDate);
        items.add(testReturn);
        setQuarantineItems(items);

        invokePrivateMethod(controller, "processSingleInspection", null, null);

        // Verify inspection was processed
        assertEquals(1, testInspectionController.processQuarantineCallCount);
        assertEquals("SKU001", testInspectionController.lastSku);
        assertEquals(5, testInspectionController.lastQuantity);
        assertEquals("REMORSE", testInspectionController.lastReason);

        // Verify item was removed from queue
        List<Return> remainingItems = getQuarantineItems();
        assertTrue(remainingItems.isEmpty());
    }

    @Test
    void testProcessSingleInspection_ExpiredProduct() throws Exception {
        // Test that expired products are handled correctly
        List<Return> items = new ArrayList<>();
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Return expiredReturn = new Return("RET001", "SKU001", 5, "REMORSE", LocalDateTime.now(), pastDate);
        items.add(expiredReturn);
        setQuarantineItems(items);

        invokePrivateMethod(controller, "processSingleInspection", null, null);

        // Verify it was processed as EXPIRED (not the original REMORSE reason)
        assertEquals(1, testInspectionController.processQuarantineCallCount);
        assertEquals("EXPIRED", testInspectionController.lastReason);
    }

    @Test
    void testProcessNextInspection_EmptyQueue() throws Exception {
        // Test batch processing with empty queue
        setQuarantineItems(new ArrayList<>());

        assertDoesNotThrow(() -> {
            try {
                invokePrivateMethod(controller, "processNextInspection", null, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertEquals(0, testInspectionController.processQuarantineCallCount);
    }

    @Test
    void testProcessNextInspection_MultipleItems() throws Exception {
        // Test processing all items in queue
        List<Return> items = new ArrayList<>();
        items.add(new Return("RET001", "SKU001", 5, "REMORSE", LocalDateTime.now(), null));
        items.add(new Return("RET002", "SKU002", 3, "DAMAGED", LocalDateTime.now().minusHours(1), null));
        items.add(new Return("RET003", "SKU003", 10, "EXPIRED", LocalDateTime.now().minusHours(2), LocalDate.now().minusDays(1)));

        setQuarantineItems(items);

        invokePrivateMethod(controller, "processNextInspection", null, null);

        // Verify all items were processed
        assertEquals(3, testInspectionController.processQuarantineCallCount);

        // Verify queue is now empty
        List<Return> remainingItems = getQuarantineItems();
        assertTrue(remainingItems.isEmpty());
    }

    @Test
    void testProcessNextInspection_MixedActions() throws Exception {
        // Test processing with different action types
        List<Return> items = new ArrayList<>();
        items.add(new Return("RET001", "SKU001", 5, "REMORSE", LocalDateTime.now(), null)); // Should RESTOCK
        items.add(new Return("RET002", "SKU002", 3, "DAMAGED", LocalDateTime.now(), null)); // Should DISCARD
        items.add(new Return("RET003", "SKU003", 10, "CYCLE_COUNT", LocalDateTime.now(), null)); // Should RESTOCK

        setQuarantineItems(items);

        invokePrivateMethod(controller, "processNextInspection", null, null);

        // Verify all were processed
        assertEquals(3, testInspectionController.processQuarantineCallCount);
    }

    @Test
    void testViewProcessedInspections() throws Exception {
        // Test viewing processed inspections
        // First process some inspections
        testInspectionController.processQuarantine("SKU001", 5, "REMORSE", LocalDate.now());
        testInspectionController.processQuarantine("SKU002", 3, "DAMAGED", null);

        assertDoesNotThrow(() -> {
            try {
                invokePrivateMethod(controller, "viewProcessedInspections", null, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testViewAuditLog() throws Exception {
        // Test viewing audit log
        // First add some audit entries
        testInspectionController.processQuarantine("SKU001", 5, "REMORSE", LocalDate.now());
        testInspectionController.processQuarantine("SKU002", 3, "DAMAGED", null);

        assertDoesNotThrow(() -> {
            try {
                invokePrivateMethod(controller, "viewAuditLog", null, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testViewQuarantineQueue_Empty() throws Exception {
        // Test viewing empty quarantine queue
        setQuarantineItems(new ArrayList<>());

        assertDoesNotThrow(() -> {
            try {
                invokePrivateMethod(controller, "viewQuarantineQueue", null, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testViewQuarantineQueue_WithItems() throws Exception {
        // Test viewing quarantine queue with items
        List<Return> items = new ArrayList<>();
        items.add(new Return("RET001", "SKU001", 5, "REMORSE", LocalDateTime.now(), null));
        items.add(new Return("RET002", "SKU002", 3, "DAMAGED", LocalDateTime.now(), null));

        setQuarantineItems(items);

        assertDoesNotThrow(() -> {
            try {
                invokePrivateMethod(controller, "viewQuarantineQueue", null, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testSortQuarantineQueue() throws Exception {
        // Test the sorting logic (timestamp DESC, returnId ASC)
        List<Return> items = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        Return item1 = new Return("RET001", "SKU001", 1, "REMORSE", now.minusHours(2), null); // Oldest
        Return item2 = new Return("RET002", "SKU002", 1, "REMORSE", now.minusHours(1), null); // Middle
        Return item3 = new Return("RET003", "SKU003", 1, "REMORSE", now, null); // Newest
        Return item4 = new Return("RET004", "SKU004", 1, "REMORSE", now, null); // Same time as item3

        items.add(item1);
        items.add(item2);
        items.add(item3);
        items.add(item4);

        setQuarantineItems(items);

        // Call sort method
        invokePrivateMethod(controller, "sortQuarantineQueue", null, null);

        List<Return> sortedItems = getQuarantineItems();

        // Verify sorting: latest timestamp first, then returnId ASC for ties
        assertEquals("RET003", sortedItems.get(0).getReturnId()); // Newest timestamp
        assertEquals("RET004", sortedItems.get(1).getReturnId()); // Same timestamp, but RET004 > RET003
        assertEquals("RET002", sortedItems.get(2).getReturnId()); // Middle
        assertEquals("RET001", sortedItems.get(3).getReturnId()); // Oldest
    }

    @Test
    void testConstructorWithWarehouseManager() {
        // Test constructor with WarehouseManager parameter
        WarehouseManager warehouseManager = new WarehouseManager(
                new java.util.HashMap<>(),
                new java.util.HashMap<>(),
                new java.util.ArrayList<>()
        );
        QualityOperatorController controllerWithWM = new QualityOperatorController(warehouseManager);
        assertNotNull(controllerWithWM);
    }

    @Test
    void testDefaultConstructor() {
        // Test default constructor
        QualityOperatorController defaultController = new QualityOperatorController();
        assertNotNull(defaultController);
    }

    @Test
    void testQuarantineItemsInitialization() throws Exception {
        // Test that quarantineItems is properly initialized
        List<Return> quarantineItems = getQuarantineItems();
        assertNotNull(quarantineItems);
        assertTrue(quarantineItems.isEmpty());
    }

    // Helper methods
    @SuppressWarnings("unchecked")
    private List<Return> getQuarantineItems() throws Exception {
        Field field = QualityOperatorController.class.getDeclaredField("quarantineItems");
        field.setAccessible(true);
        return (List<Return>) field.get(controller);
    }

    private void setQuarantineItems(List<Return> items) throws Exception {
        Field field = QualityOperatorController.class.getDeclaredField("quarantineItems");
        field.setAccessible(true);
        field.set(controller, items);
    }

    private Object invokePrivateMethod(Object target, String methodName, Class<?>[] paramTypes, Object[] args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}