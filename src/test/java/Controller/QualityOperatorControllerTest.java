package Controller;

import Domain.AuditLog;
import Domain.QuarantineInspection;
import Domain.Return;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Full test suite for QualityOperatorController.
 * - Uses reflection to access private methods/fields.
 * - Exercises menu, CSV loading (many branches), processing (restock/discard), sort, view, audit.
 */
class QualityOperatorControllerTest {

    private QualityOperatorController controller;

    /**
     * Single reflection helper: invoke private method with varargs for arguments.
     */
    private Object callPrivate(String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method m = QualityOperatorController.class.getDeclaredMethod(methodName, paramTypes);
        m.setAccessible(true);
        return m.invoke(controller, args);
    }

    /**
     * Single reflection helper: read a private field value.
     */
    @SuppressWarnings("unchecked")
    private <T> T readPrivateField(String fieldName, Class<T> clazz) throws Exception {
        Field f = QualityOperatorController.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        return (T) f.get(controller);
    }

    @BeforeEach
    void setUp() throws Exception {
        controller = new QualityOperatorController();

        // Clear audit log to make tests repeatable (production AuditLog has clearAll())
        try {
            AuditLog.clearAll();
        } catch (Exception ignored) {
        }

        // Ensure quarantine list empty
        List<Return> q = readPrivateField("quarantineItems", List.class);
        q.clear();

        // Remove test CSV if present
        try {
            Path csvPath = Paths.get("train_station_dataset", "returns.csv");
            // If it's a directory from previous failed test, delete it
            if (Files.exists(csvPath) && Files.isDirectory(csvPath)) {
                Files.delete(csvPath);
            } else {
                Files.deleteIfExists(csvPath);
            }
        } catch (Exception ignored) {
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        // Remove test CSV if present
        try {
            Path csvPath = Paths.get("train_station_dataset", "returns.csv");
            if (Files.exists(csvPath) && Files.isDirectory(csvPath)) {
                Files.delete(csvPath);
            } else {
                Files.deleteIfExists(csvPath);
            }
        } catch (Exception ignored) {
        }
    }

    // -----------------------
    // Basic public methods
    // -----------------------

    @Test
    @DisplayName("showLogin returns true")
    void showLogin_returnsTrue() {
        assertTrue(controller.showLogin());
    }

    // -----------------------
    // Reason & action logic
    // -----------------------

    @Test
    @DisplayName("mapReturnReason handles known and unknown values")
    void mapReturnReason_values() throws Exception {
        assertEquals("REMORSE", callPrivate("mapReturnReason", new Class<?>[]{String.class}, "customer-remorse"));
        assertEquals("CYCLE_COUNT", callPrivate("mapReturnReason", new Class<?>[]{String.class}, "cycle-count"));
        assertEquals("DAMAGED", callPrivate("mapReturnReason", new Class<?>[]{String.class}, "damaged"));
        assertEquals("EXPIRED", callPrivate("mapReturnReason", new Class<?>[]{String.class}, "expired"));
        assertEquals("REMORSE", callPrivate("mapReturnReason", new Class<?>[]{String.class}, "package-opened"));
        assertEquals("REMORSE", callPrivate("mapReturnReason", new Class<?>[]{String.class}, "wrong-item"));
        assertEquals("REMORSE", callPrivate("mapReturnReason", new Class<?>[]{String.class}, "random-unknown"));
    }

    @Test
    @DisplayName("determineProcessingReason returns EXPIRED when expiry before now")
    void determineProcessingReason_expired() throws Exception {
        Return r = new Return("R1", "SKU1", 1, "REMORSE",
                LocalDateTime.now().minusDays(3), LocalDate.now().minusDays(1));

        Object out = callPrivate("determineProcessingReason", new Class<?>[]{Return.class}, r);
        assertEquals("EXPIRED", out);
    }

    @Test
    @DisplayName("determineProcessingReason keeps reason when not expired")
    void determineProcessingReason_notExpired() throws Exception {
        Return r = new Return("R2", "SKU2", 2, "DAMAGED",
                LocalDateTime.now(), LocalDate.now().plusDays(10));

        Object out = callPrivate("determineProcessingReason", new Class<?>[]{Return.class}, r);
        assertEquals("DAMAGED", out);
    }

    @Test
    @DisplayName("determineActionFromReason covers branches")
    void determineActionFromReason_branches() throws Exception {
        assertEquals("RESTOCK", callPrivate("determineActionFromReason", new Class<?>[]{String.class}, "REMORSE"));
        assertEquals("RESTOCK", callPrivate("determineActionFromReason", new Class<?>[]{String.class}, "CYCLE_COUNT"));
        assertEquals("DISCARD", callPrivate("determineActionFromReason", new Class<?>[]{String.class}, "DAMAGED"));
        assertEquals("DISCARD", callPrivate("determineActionFromReason", new Class<?>[]{String.class}, "EXPIRED"));
        assertEquals("DISCARD", callPrivate("determineActionFromReason", new Class<?>[]{String.class}, "OTHER"));
    }

    // -----------------------
    // Sorting & queue view
    // -----------------------

    @Test
    @DisplayName("sortQuarantineQueue orders by timestamp DESC then returnId ASC")
    void sortQuarantineQueue_ordersCorrectly() throws Exception {
        Return r1 = new Return("R002", "S1", 1, "REMORSE",
                LocalDateTime.of(2025, 10, 24, 10, 0), null);
        Return r2 = new Return("R001", "S2", 1, "REMORSE",
                LocalDateTime.of(2025, 10, 24, 10, 0), null);
        Return r3 = new Return("R003", "S3", 1, "REMORSE",
                LocalDateTime.of(2025, 10, 25, 9, 0), null);

        List<Return> q = readPrivateField("quarantineItems", List.class);
        q.clear();
        q.add(r1);
        q.add(r2);
        q.add(r3);

        callPrivate("sortQuarantineQueue", new Class<?>[]{});

        List<Return> after = readPrivateField("quarantineItems", List.class);
        assertEquals("R003", after.get(0).getReturnId());
        assertEquals("R001", after.get(1).getReturnId());
        assertEquals("R002", after.get(2).getReturnId());
    }

    @Test
    @DisplayName("viewQuarantineQueue empty does not throw")
    void viewQuarantineQueue_empty() throws Exception {
        List<Return> q = readPrivateField("quarantineItems", List.class);
        q.clear();
        callPrivate("viewQuarantineQueue", new Class<?>[]{});
    }

    @Test
    @DisplayName("viewQuarantineQueue with items does not throw")
    void viewQuarantineQueue_withItems() throws Exception {
        List<Return> q = readPrivateField("quarantineItems", List.class);
        q.clear();
        q.add(new Return("R100", "SKU100", 10, "REMORSE", LocalDateTime.now(), LocalDate.now().plusDays(5)));
        q.add(new Return("R101", "SKU101", 1, "DAMAGED", LocalDateTime.now(), null));
        callPrivate("viewQuarantineQueue", new Class<?>[]{});
    }

    // -----------------------
    // CSV loading
    // -----------------------

    @Test
    @DisplayName("loadReturnsFromCSV handles missing file when showDetails=true")
    void loadReturns_missingFile_showDetails() throws Exception {
        Path csvPath = Paths.get("train_station_dataset", "returns.csv");
        Files.deleteIfExists(csvPath);

        // call with Boolean.TRUE
        callPrivate("loadReturnsFromCSV", new Class<?>[]{boolean.class}, Boolean.TRUE);
    }

    @Test
    @DisplayName("loadReturnsFromCSV reads valid CSV and skips bad lines (verbose)")
    void loadReturns_validAndBadLines_showDetails() throws Exception {
        Path dir = Paths.get("train_station_dataset");
        Files.createDirectories(dir);
        Path csv = dir.resolve("returns.csv");

        String header = "returnId,sku,qty,reason,timestamp,expiryDate";
        String valid1 = "R1,SKU1,2,customer-remorse,2025-10-25T10:00,2025-11-01";
        String bad = "BADLINE,one,two";
        String malformed = "R2,SKU2,1,damaged,NOTATIME,";
        String valid2 = "R3,SKU3,5,expired,2025-10-26T12:30,2025-10-01";

        String content = String.join(System.lineSeparator(), header, valid1, bad, malformed, valid2);
        Files.write(csv, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // call verbose loader to hit warnings printing
        callPrivate("loadReturnsFromCSV", new Class<?>[]{boolean.class}, Boolean.TRUE);

        List<Return> q = readPrivateField("quarantineItems", List.class);
        assertTrue(q.size() >= 2);
    }

    @Test
    @DisplayName("loadReturnsFromCSV prints first 5 and '... and N more' when >5 entries (verbose)")
    void loadReturns_moreThanFive_showDetails() throws Exception {
        Path dir = Paths.get("train_station_dataset");
        Files.createDirectories(dir);
        Path csv = dir.resolve("returns.csv");

        String header = "returnId,sku,qty,reason,timestamp,expiryDate";
        StringBuilder sb = new StringBuilder(header);
        // create 6 valid lines to force "... and N more"
        for (int i = 1; i <= 6; i++) {
            sb.append(System.lineSeparator())
                    .append("R").append(i).append(",SKU").append(i).append(",").append(i).append(",customer-remorse,2025-10-2")
                    .append(i).append("T10:00,");
        }
        Files.write(csv, sb.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        callPrivate("loadReturnsFromCSV", new Class<?>[]{boolean.class}, Boolean.TRUE);

        List<Return> q = readPrivateField("quarantineItems", List.class);
        assertEquals(6, q.size());
    }

    @Test
    @DisplayName("loadReturnsFromCSV (no-arg) reads CSV silently when file exists")
    void loadReturns_noArg_readsSilently() throws Exception {
        Path dir = Paths.get("train_station_dataset");
        Files.createDirectories(dir);
        Path csv = dir.resolve("returns.csv");

        String header = "returnId,sku,qty,reason,timestamp,expiryDate";
        String valid = "T1,TSKU,1,customer-remorse,2025-12-01T10:00,";
        String content = header + System.lineSeparator() + valid + System.lineSeparator();
        Files.write(csv, content.getBytes(java.nio.charset.StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // call private no-arg loader
        callPrivate("loadReturnsFromCSV", new Class<?>[]{});

        List<Return> q = readPrivateField("quarantineItems", List.class);
        assertTrue(q.stream().anyMatch(r -> r.getReturnId().equals("T1")));
    }

    @Test
    @DisplayName("loadReturnsFromCSV handles IO exception path (unreadable file) gracefully (verbose)")
    void loadReturns_ioExceptionPath() throws Exception {
        Path parent = Paths.get("train_station_dataset");
        Files.createDirectories(parent);
        Path csvPath = parent.resolve("returns.csv");

        // create a directory where the file should be to cause an IOException when trying to open a reader
        Files.deleteIfExists(csvPath);
        Files.createDirectory(csvPath); // now 'returns.csv' is a directory

        try {
            callPrivate("loadReturnsFromCSV", new Class<?>[]{boolean.class}, Boolean.TRUE);
            // no exception expected; we only assert the method returned gracefully
            assertTrue(true);
        } finally {
            // cleanup: delete the directory we created
            if (Files.exists(csvPath) && Files.isDirectory(csvPath)) {
                Files.delete(csvPath);
            }
        }
    }

    // -----------------------
    // Processing single & multiple
    // -----------------------

    @Test
    @DisplayName("processSingleInspection returns early on empty queue")
    void processSingleInspection_emptyQueue() throws Exception {
        List<Return> q = readPrivateField("quarantineItems", List.class);
        q.clear();
        callPrivate("processSingleInspection", new Class<?>[]{});
    }

    @Test
    @DisplayName("processSingleInspection processes RESTOCK and writes audit")
    void processSingleInspection_processOne_restocks() throws Exception {
        List<Return> q = readPrivateField("quarantineItems", List.class);
        q.clear();
        // REMORSE -> RESTOCK
        q.add(new Return("R20", "SKU-20", 3, "REMORSE", LocalDateTime.now(), null));

        callPrivate("processSingleInspection", new Class<?>[]{});

        List<Return> after = readPrivateField("quarantineItems", List.class);
        assertEquals(0, after.size());

        List<String> lines = AuditLog.readAll();
        assertTrue(lines.stream().anyMatch(s -> s.contains("SKU=SKU-20")));
    }

    @Test
    @DisplayName("processSingleInspection processes DISCARD when expired and prints expiry branch")
    void processSingleInspection_discard_expired() throws Exception {
        List<Return> q = readPrivateField("quarantineItems", List.class);
        q.clear();
        // expiry before now -> determineProcessingReason returns EXPIRED -> action DISCARD
        q.add(new Return("REXP", "SKU-EXP", 1, "REMORSE", LocalDateTime.now().minusDays(5), LocalDate.now().minusDays(1)));

        callPrivate("processSingleInspection", new Class<?>[]{});

        List<Return> after = readPrivateField("quarantineItems", List.class);
        assertEquals(0, after.size());

        List<String> lines = AuditLog.readAll();
        assertTrue(lines.stream().anyMatch(s -> s.contains("SKU=SKU-EXP")));
    }

    @Test
    @DisplayName("processSingleInspection processes DISCARD when reason DAMAGED (non-expired)")
    void processSingleInspection_discard_damaged() throws Exception {
        List<Return> q = readPrivateField("quarantineItems", List.class);
        q.clear();
        // damaged with future expiry -> action DISCARD, but not EXPIRED branch
        q.add(new Return("RDAM", "SKU-DAM", 2, "DAMAGED", LocalDateTime.now(), LocalDate.now().plusDays(10)));

        callPrivate("processSingleInspection", new Class<?>[]{});

        List<Return> after = readPrivateField("quarantineItems", List.class);
        assertEquals(0, after.size());

        List<String> lines = AuditLog.readAll();
        assertTrue(lines.stream().anyMatch(s -> s.contains("SKU=SKU-DAM")));
    }

    @Test
    @DisplayName("processNextInspection returns early on empty queue")
    void processNextInspection_emptyQueue() throws Exception {
        List<Return> q = readPrivateField("quarantineItems", List.class);
        q.clear();
        callPrivate("processNextInspection", new Class<?>[]{});
    }

    @Test
    @DisplayName("processNextInspection processes multiple and writes audit")
    void processNextInspection_processesMultiple() throws Exception {
        List<Return> q = readPrivateField("quarantineItems", List.class);
        q.clear();
        q.add(new Return("RA", "SKU-A", 1, "REMORSE", LocalDateTime.now().minusHours(1), null));
        q.add(new Return("RB", "SKU-B", 2, "DAMAGED", LocalDateTime.now().minusHours(2), null));
        q.add(new Return("RC", "SKU-C", 3, "REMORSE", LocalDateTime.now().minusMinutes(10), LocalDate.now().plusDays(5)));

        Object inspObj = readPrivateField("inspectionController", Object.class);
        assertNotNull(inspObj);
        InspectionController insp = (InspectionController) inspObj;
        List<QuarantineInspection> before = insp.getInspectionsBySku("");

        callPrivate("processNextInspection", new Class<?>[]{});

        List<Return> afterQ = readPrivateField("quarantineItems", List.class);
        assertEquals(0, afterQ.size());

        List<QuarantineInspection> after = insp.getInspectionsBySku("");
        assertTrue(after.size() >= before.size() + 3);

        List<String> lines = AuditLog.readAll();
        assertTrue(lines.stream().anyMatch(s -> s.contains("SKU=SKU-A")));
        assertTrue(lines.stream().anyMatch(s -> s.contains("SKU=SKU-B")));
        assertTrue(lines.stream().anyMatch(s -> s.contains("SKU=SKU-C")));
    }

    // -----------------------
    // View processed inspections & audit log
    // -----------------------

    @Test
    @DisplayName("viewProcessedInspections runs without exception when inspections exist")
    void viewProcessedInspections_runs() throws Exception {
        Object inspObj = readPrivateField("inspectionController", Object.class);
        assertNotNull(inspObj);
        InspectionController insp = (InspectionController) inspObj;
        insp.processQuarantine("SKU-VIEW", 5, "REMORSE", LocalDate.now().plusDays(10));

        callPrivate("viewProcessedInspections", new Class<?>[]{});
    }

    @Test
    @DisplayName("viewAuditLog runs without exception when entries exist")
    void viewAuditLog_runs() throws Exception {
        Object inspObj = readPrivateField("inspectionController", Object.class);
        assertNotNull(inspObj);
        InspectionController insp = (InspectionController) inspObj;
        insp.processQuarantine("SKU-AUD", 2, "DAMAGED", LocalDate.now().plusDays(1));

        callPrivate("viewAuditLog", new Class<?>[]{});
    }

    @Test
    @DisplayName("viewAuditLog prints 'no audit logs available' when empty")
    void viewAuditLog_emptyRuns() throws Exception {
        // ensure audit cleared
        AuditLog.clearAll();
        callPrivate("viewAuditLog", new Class<?>[]{});
    }

    // -----------------------
    // Additional menu tests & default branch
    // -----------------------

    @Test
    @DisplayName("showMainMenu executes menu options and exits cleanly")
    void showMainMenu_runsThroughOptions() throws Exception {
        // prepare input: 1:view queue, 2:process single, 3:process all, 4:view processed, 5:view audit, 6:reload csv(true), 7:exit
        String menuInputs = String.join(System.lineSeparator(),
                "1", "2", "3", "4", "5", "6", "7"
        ) + System.lineSeparator();

        java.io.InputStream is = new java.io.ByteArrayInputStream(menuInputs.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        java.util.Scanner testScanner = new java.util.Scanner(is);
        Field scannerField = QualityOperatorController.class.getDeclaredField("scanner");
        scannerField.setAccessible(true);
        scannerField.set(controller, testScanner);

        // run menu (will exit on "7")
        controller.showMainMenu();

        assertTrue(true); // no exception = success
    }

    @Test
    @DisplayName("showMainMenu handles invalid option (default branch)")
    void showMainMenu_invalidOption() throws Exception {
        // feed an invalid option then exit
        String menuInputs = String.join(System.lineSeparator(), "x", "7") + System.lineSeparator();
        java.io.InputStream is = new java.io.ByteArrayInputStream(menuInputs.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        java.util.Scanner testScanner = new java.util.Scanner(is);
        Field scannerField = QualityOperatorController.class.getDeclaredField("scanner");
        scannerField.setAccessible(true);
        scannerField.set(controller, testScanner);

        controller.showMainMenu(); // should process "x" -> default -> then "7" -> exit

        assertTrue(true);
    }

    // -----------------------
    // Constructor with WarehouseManager test (restock path coverage)
    // -----------------------

    @Test
    @DisplayName("constructor with WarehouseManager triggers restock insert in WarehouseManager.stock")
    void constructorWithWarehouseManager_triggersRestockInsert() throws Exception {
        // create a WarehouseManager using production constructor (empty maps, empty bay list)
        java.util.Map<String, Domain.Item> items = new java.util.HashMap<>();
        java.util.Map<String, java.util.List<Domain.Box>> stock = new java.util.HashMap<>();
        java.util.List<Domain.Bay> bays = new java.util.ArrayList<>();
        Domain.WarehouseManager wm = new Domain.WarehouseManager(items, stock, bays);

        // new controller that uses this WarehouseManager
        QualityOperatorController ctrlWithWM = new QualityOperatorController(wm);

        // inject a return into ctrlWithWM's private quarantineItems
        Field qField = QualityOperatorController.class.getDeclaredField("quarantineItems");
        qField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Return> qList = (List<Return>) qField.get(ctrlWithWM);
        qList.clear();
        qList.add(new Return("W1", "WM-SKU", 2, "REMORSE", LocalDateTime.now(), null));

        // call processSingleInspection via reflection on that instance
        Method m = QualityOperatorController.class.getDeclaredMethod("processSingleInspection");
        m.setAccessible(true);
        m.invoke(ctrlWithWM);

        // ensure removed from that controller's queue
        @SuppressWarnings("unchecked")
        List<Return> after = (List<Return>) qField.get(ctrlWithWM);
        assertEquals(0, after.size());

        // WarehouseManager.stock should contain the restocked SKU
        assertTrue(wm.getStock().containsKey("WM-SKU"));
    }

    // 1) Silent load: CSV has an insufficient-column line but showDetails = false (silent skip)
    @Test
    @DisplayName("loadReturnsFromCSV silent skips lines with insufficient columns")
    void loadReturns_silent_skipsInsufficientColumns() throws Exception {
        Path dir = Paths.get("train_station_dataset");
        Files.createDirectories(dir);
        Path csv = dir.resolve("returns.csv");

        String header = "returnId,sku,qty,reason,timestamp,expiryDate";
        // this line has <6 columns and should be skipped silently
        String insufficient = "BADLINE,one,two";
        String valid = "SIL1,SKU-S,1,customer-remorse,2025-12-01T10:00,";

        String content = String.join(System.lineSeparator(), header, insufficient, valid);
        Files.write(csv, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // call private no-verbose loader (silent branch)
        callPrivate("loadReturnsFromCSV", new Class<?>[]{boolean.class}, Boolean.FALSE);

        List<Return> q = readPrivateField("quarantineItems", List.class);
        // only the valid line should be present
        assertTrue(q.stream().anyMatch(r -> r.getReturnId().equals("SIL1")));
        assertFalse(q.stream().anyMatch(r -> r.getReturnId().equals("BADLINE")));
    }

    // 2) Silent load: CSV has malformed timestamp -> should be skipped silently (no exception thrown)
    @Test
    @DisplayName("loadReturnsFromCSV silent skips malformed timestamp lines")
    void loadReturns_silent_skipsMalformedTimestamp() throws Exception {
        Path dir = Paths.get("train_station_dataset");
        Files.createDirectories(dir);
        Path csv = dir.resolve("returns.csv");

        String header = "returnId,sku,qty,reason,timestamp,expiryDate";
        String malformed = "MAL1,SKU-M,1,damaged,NOT-A-TIMESTAMP,";
        String valid = "MAL2,SKU-M2,2,customer-remorse,2025-12-02T11:00,";

        String content = String.join(System.lineSeparator(), header, malformed, valid);
        Files.write(csv, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // silent load
        callPrivate("loadReturnsFromCSV", new Class<?>[]{boolean.class}, Boolean.FALSE);

        List<Return> q = readPrivateField("quarantineItems", List.class);
        assertTrue(q.stream().anyMatch(r -> r.getReturnId().equals("MAL2")));
        // malformed should not crash, and MAL1 should not be present
        assertFalse(q.stream().anyMatch(r -> r.getReturnId().equals("MAL1")));
    }

    // 3) mapReturnReason handles input with extra spaces and mixed case (default branch coverage)
    @Test
    @DisplayName("mapReturnReason handles spaces and mixed case")
    void mapReturnReason_spacesAndCase() throws Exception {
        assertEquals("REMORSE", callPrivate("mapReturnReason", new Class<?>[]{String.class}, "  Customer-Remorse  "));
        assertEquals("REMORSE", callPrivate("mapReturnReason", new Class<?>[]{String.class}, "Wrong-Item"));
        assertEquals("REMORSE", callPrivate("mapReturnReason", new Class<?>[]{String.class}, "UNKNOWN-REASON"));
    }

    // 4) sortQuarantineQueue extra: identical timestamps and verify returnId tie-breaker again (edge)
    @Test
    @DisplayName("sortQuarantineQueue tie-breaker edge case")
    void sortQuarantineQueue_tieBreakerEdge() throws Exception {
        LocalDateTime ts = LocalDateTime.of(2025, 11, 1, 9, 0);
        Return a = new Return("A10", "S", 1, "REMORSE", ts, null);
        Return b = new Return("A2", "S", 1, "REMORSE", ts, null);
        Return c = new Return("A3", "S", 1, "REMORSE", ts, null);

        List<Return> q = readPrivateField("quarantineItems", List.class);
        q.clear();
        q.add(a);
        q.add(b);
        q.add(c);

        callPrivate("sortQuarantineQueue", new Class<?>[]{});

        List<Return> after = readPrivateField("quarantineItems", List.class);
        // A10 vs A2 vs A3 when compared lexicographically: "A10" > "A2" > "A3"
        // but sort uses thenComparing(Return::getReturnId) ascending, so ensure consistent order
        assertEquals("A10", after.get(0).getReturnId());
    }

}
