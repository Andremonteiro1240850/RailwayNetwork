package Controller;

import Domain.AuditLog;
import Domain.QuarantineInspection;
import Domain.Return;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
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
 * - Uses reflection to access private methods/fields where needed.
 * - Exercises CSV loading, processing (restock/discard), sorting logic.
 * - Tests business logic only (UI methods moved to QualityOperatorUI).
 *
 * Tests run by writing test CSV content into train_station_dataset/returns.csv,
 * but a backup of any existing file is created and restored after each test so no real data is lost.
 */
class QualityOperatorControllerTest {

    private QualityOperatorController controller;

    private static final Path TEST_DIR = Paths.get("train_station_dataset");
    private static final Path ACTUAL_CSV = TEST_DIR.resolve("returns.csv");
    private static final Path BACKUP_CSV = TEST_DIR.resolve("returns_backup_for_tests.csv");

    // -----------------------
    // Reflection helpers
    // -----------------------

    private Object callPrivate(String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method m = QualityOperatorController.class.getDeclaredMethod(methodName, paramTypes);
        m.setAccessible(true);
        return m.invoke(controller, args);
    }

    @SuppressWarnings("unchecked")
    private <T> T readPrivateField(String fieldName, Class<T> clazz) throws Exception {
        Field f = QualityOperatorController.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        return (T) f.get(controller);
    }

    // -----------------------
    // File helpers (backup/restore)
    // -----------------------

    private void backupOriginalCsvIfExists() throws IOException {
        Files.createDirectories(TEST_DIR);
        if (Files.exists(ACTUAL_CSV) && !Files.isDirectory(ACTUAL_CSV)) {
            Files.copy(ACTUAL_CSV, BACKUP_CSV, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void restoreOriginalCsvOrDelete() throws IOException {
        if (Files.exists(BACKUP_CSV)) {
            Files.move(BACKUP_CSV, ACTUAL_CSV, StandardCopyOption.REPLACE_EXISTING);
        } else {
            Files.deleteIfExists(ACTUAL_CSV);
        }
    }

    private void writeCsvToActual(String content) throws IOException {
        Files.createDirectories(TEST_DIR);
        Files.write(ACTUAL_CSV, content.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    // -----------------------
    // Setup / Teardown
    // -----------------------

    @BeforeEach
    void setUp() throws Exception {
        controller = new QualityOperatorController();

        // clear audit
        try {
            AuditLog.clearAll();
        } catch (Exception ignored) {}

        // clear quarantine list
        List<Return> q = readPrivateField("quarantineItems", List.class);
        q.clear();

        // backup any existing CSV and ensure a header exists
        backupOriginalCsvIfExists();
        String header = "returnId,sku,qty,reason,timestamp,expiryDate\n";
        writeCsvToActual(header);
    }

    @AfterEach
    void tearDown() throws Exception {
        // restore original CSV (or remove test CSV)
        try {
            restoreOriginalCsvOrDelete();
        } catch (Exception ignored) {}
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
    // Sorting & queue management
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
    @DisplayName("getQuarantineItems returns defensive copy")
    void getQuarantineItems_returnsDefensiveCopy() throws Exception {
        List<Return> q = readPrivateField("quarantineItems", List.class);
        q.clear();
        q.add(new Return("R1", "SKU1", 1, "REMORSE", LocalDateTime.now(), null));

        List<Return> publicList = controller.getQuarantineItems();
        assertEquals(1, publicList.size());
        
        // Modifying public list should not affect internal list
        publicList.clear();
        assertEquals(1, readPrivateField("quarantineItems", List.class).size());
    }

    // -----------------------
    // CSV loading
    // -----------------------

    @Test
    @DisplayName("loadReturnsFromCSV returns failure when file missing")
    void loadReturns_missingFile() {
        // remove the file to simulate missing file
        try {
            Files.deleteIfExists(ACTUAL_CSV);
        } catch (IOException ignored) {}
        
        QualityOperatorController.LoadResult result = controller.loadReturnsFromCSV();
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("not found"));
    }

    @Test
    @DisplayName("loadReturnsFromCSV reads valid CSV and skips bad lines")
    void loadReturns_validAndBadLines() throws Exception {
        String header = "returnId,sku,qty,reason,timestamp,expiryDate";
        String valid1 = "R1,SKU1,2,customer-remorse,2025-10-25T10:00,2025-11-01";
        String bad = "BADLINE,one,two";
        String malformed = "R2,SKU2,1,damaged,NOTATIME,";
        String valid2 = "R3,SKU3,5,expired,2025-10-26T12:30,2025-10-01";

        String content = String.join(System.lineSeparator(), header, valid1, bad, malformed, valid2);
        writeCsvToActual(content);

        QualityOperatorController.LoadResult result = controller.loadReturnsFromCSV(true);
        assertTrue(result.isSuccess());
        assertEquals(2, result.getLoadedCount());
        assertFalse(result.getWarnings().isEmpty());

        List<Return> q = controller.getQuarantineItems();
        assertEquals(2, q.size());
    }

    @Test
    @DisplayName("loadReturnsFromCSV includes warnings for bad lines")
    void loadReturns_includesWarnings() throws Exception {
        String header = "returnId,sku,qty,reason,timestamp,expiryDate";
        String bad = "BADLINE,one,two";
        String valid = "R1,SKU1,1,customer-remorse,2025-10-25T10:00,";

        String content = String.join(System.lineSeparator(), header, bad, valid);
        writeCsvToActual(content);

        QualityOperatorController.LoadResult result = controller.loadReturnsFromCSV(true);
        assertTrue(result.isSuccess());
        assertFalse(result.getWarnings().isEmpty());
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("insufficient columns")));
    }

    @Test
    @DisplayName("loadReturnsFromCSV (no-arg) reads CSV successfully")
    void loadReturns_noArg_readsSuccessfully() throws Exception {
        String header = "returnId,sku,qty,reason,timestamp,expiryDate";
        String valid = "T1,TSKU,1,customer-remorse,2025-12-01T10:00,";
        String content = header + System.lineSeparator() + valid + System.lineSeparator();
        writeCsvToActual(content);

        QualityOperatorController.LoadResult result = controller.loadReturnsFromCSV();
        assertTrue(result.isSuccess());
        assertEquals(1, result.getLoadedCount());

        List<Return> q = controller.getQuarantineItems();
        assertTrue(q.stream().anyMatch(r -> r.getReturnId().equals("T1")));
    }

    @Test
    @DisplayName("loadReturnsFromCSV handles IO exception gracefully")
    void loadReturns_ioExceptionPath() throws Exception {
        // ensure clean and create a directory at the file path to cause IOException
        Files.deleteIfExists(ACTUAL_CSV);
        Files.createDirectories(TEST_DIR);
        Files.createDirectory(ACTUAL_CSV); // ACTUAL_CSV is now a directory

        try {
            QualityOperatorController.LoadResult result = controller.loadReturnsFromCSV(true);
            assertFalse(result.isSuccess());
            assertNotNull(result.getErrorMessage());
        } finally {
            if (Files.exists(ACTUAL_CSV) && Files.isDirectory(ACTUAL_CSV)) {
                Files.delete(ACTUAL_CSV);
            }
        }
    }

    @Test
    @DisplayName("loadReturnsFromCSV silent skips lines with insufficient columns")
    void loadReturns_silent_skipsInsufficientColumns() throws Exception {
        String header = "returnId,sku,qty,reason,timestamp,expiryDate";
        String insufficient = "BADLINE,one,two";
        String valid = "SIL1,SKU-S,1,customer-remorse,2025-12-01T10:00,";

        String content = String.join(System.lineSeparator(), header, insufficient, valid);
        writeCsvToActual(content);

        QualityOperatorController.LoadResult result = controller.loadReturnsFromCSV(false);
        assertTrue(result.isSuccess());
        List<Return> q = controller.getQuarantineItems();
        assertTrue(q.stream().anyMatch(r -> r.getReturnId().equals("SIL1")));
        assertFalse(q.stream().anyMatch(r -> r.getReturnId().equals("BADLINE")));
    }

    @Test
    @DisplayName("loadReturnsFromCSV silent skips malformed timestamp lines")
    void loadReturns_silent_skipsMalformedTimestamp() throws Exception {
        String header = "returnId,sku,qty,reason,timestamp,expiryDate";
        String malformed = "MAL1,SKU-M,1,damaged,NOT-A-TIMESTAMP,";
        String valid = "MAL2,SKU-M2,2,customer-remorse,2025-12-02T11:00,";

        String content = String.join(System.lineSeparator(), header, malformed, valid);
        writeCsvToActual(content);

        QualityOperatorController.LoadResult result = controller.loadReturnsFromCSV(false);
        assertTrue(result.isSuccess());
        List<Return> q = controller.getQuarantineItems();
        assertTrue(q.stream().anyMatch(r -> r.getReturnId().equals("MAL2")));
        assertFalse(q.stream().anyMatch(r -> r.getReturnId().equals("MAL1")));
    }

    @Test
    @DisplayName("mapReturnReason handles spaces and mixed case")
    void mapReturnReason_spacesAndCase() throws Exception {
        assertEquals("REMORSE", callPrivate("mapReturnReason", new Class<?>[]{String.class}, "  Customer-Remorse  "));
        assertEquals("REMORSE", callPrivate("mapReturnReason", new Class<?>[]{String.class}, "Wrong-Item"));
        assertEquals("REMORSE", callPrivate("mapReturnReason", new Class<?>[]{String.class}, "UNKNOWN-REASON"));
    }

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
        // Sort uses thenComparing(Return::getReturnId) ascending
        // Lexicographically: A10 < A2 < A3 (string comparison)
        assertEquals("A10", after.get(0).getReturnId());
    }

    // -----------------------
    // Processing single & multiple
    // -----------------------

    @Test
    @DisplayName("processSingleInspection returns null on empty queue")
    void processSingleInspection_emptyQueue() {
        QualityOperatorController.ProcessingResult result = controller.processSingleInspection();
        assertNull(result);
    }

    @Test
    @DisplayName("processSingleInspection processes RESTOCK and writes audit")
    void processSingleInspection_processOne_restocks() throws Exception {
        List<Return> q = readPrivateField("quarantineItems", List.class);
        q.clear();
        // REMORSE -> RESTOCK
        Return r = new Return("R20", "SKU-20", 3, "REMORSE", LocalDateTime.now(), null);
        q.add(r);

        QualityOperatorController.ProcessingResult result = controller.processSingleInspection();
        assertNotNull(result);
        assertEquals("RESTOCK", result.getAction());
        assertEquals("REMORSE", result.getReason());
        assertEquals(r, result.getItem());

        List<Return> after = readPrivateField("quarantineItems", List.class);
        assertEquals(0, after.size());

        List<String> lines = AuditLog.readAll();
        assertTrue(lines.stream().anyMatch(s -> s.contains("SKU=SKU-20")));
    }

    @Test
    @DisplayName("processSingleInspection processes DISCARD when expired and writes audit")
    void processSingleInspection_discard_expired() throws Exception {
        List<Return> q = readPrivateField("quarantineItems", List.class);
        q.clear();
        Return r = new Return("REXP", "SKU-EXP", 1, "REMORSE", LocalDateTime.now().minusDays(5), LocalDate.now().minusDays(1));
        q.add(r);

        QualityOperatorController.ProcessingResult result = controller.processSingleInspection();
        assertNotNull(result);
        assertEquals("DISCARD", result.getAction());
        assertEquals("EXPIRED", result.getReason());

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
        Return r = new Return("RDAM", "SKU-DAM", 2, "DAMAGED", LocalDateTime.now(), LocalDate.now().plusDays(10));
        q.add(r);

        QualityOperatorController.ProcessingResult result = controller.processSingleInspection();
        assertNotNull(result);
        assertEquals("DISCARD", result.getAction());
        assertEquals("DAMAGED", result.getReason());

        List<Return> after = readPrivateField("quarantineItems", List.class);
        assertEquals(0, after.size());

        List<String> lines = AuditLog.readAll();
        assertTrue(lines.stream().anyMatch(s -> s.contains("SKU=SKU-DAM")));
    }

    @Test
    @DisplayName("processAllInspections returns summary with correct counts")
    void processAllInspections_returnsSummary() throws Exception {
        List<Return> q = readPrivateField("quarantineItems", List.class);
        q.clear();
        q.add(new Return("RA", "SKU-A", 1, "REMORSE", LocalDateTime.now().minusHours(1), null));
        q.add(new Return("RB", "SKU-B", 2, "DAMAGED", LocalDateTime.now().minusHours(2), null));
        q.add(new Return("RC", "SKU-C", 3, "REMORSE", LocalDateTime.now().minusMinutes(10), LocalDate.now().plusDays(5)));

        QualityOperatorController.ProcessingSummary summary = controller.processAllInspections();
        
        assertNotNull(summary);
        assertEquals(3, summary.getProcessedCount());
        assertEquals(1, summary.getDiscardedCount()); // RB is DAMAGED
        assertEquals(2, summary.getRestockedCount()); // RA and RC are REMORSE
        assertEquals(3, summary.getResults().size());

        List<Return> afterQ = readPrivateField("quarantineItems", List.class);
        assertEquals(0, afterQ.size());

        List<String> lines = AuditLog.readAll();
        assertTrue(lines.stream().anyMatch(s -> s.contains("SKU=SKU-A")));
        assertTrue(lines.stream().anyMatch(s -> s.contains("SKU=SKU-B")));
        assertTrue(lines.stream().anyMatch(s -> s.contains("SKU=SKU-C")));
    }

    @Test
    @DisplayName("processAllInspections returns empty summary on empty queue")
    void processAllInspections_emptyQueue() {
        QualityOperatorController.ProcessingSummary summary = controller.processAllInspections();
        assertNotNull(summary);
        assertEquals(0, summary.getProcessedCount());
        assertEquals(0, summary.getDiscardedCount());
        assertEquals(0, summary.getRestockedCount());
        assertTrue(summary.getResults().isEmpty());
    }

    // -----------------------
    // View processed inspections & audit log
    // -----------------------

    @Test
    @DisplayName("getProcessedInspections returns list from InspectionController")
    void getProcessedInspections_returnsList() throws Exception {
        // Process an inspection first
        List<Return> q = readPrivateField("quarantineItems", List.class);
        q.clear();
        q.add(new Return("R1", "SKU-VIEW", 5, "REMORSE", LocalDateTime.now(), LocalDate.now().plusDays(10)));
        controller.processSingleInspection();

        List<QuarantineInspection> inspections = controller.getProcessedInspections();
        assertNotNull(inspections);
        assertFalse(inspections.isEmpty());
    }

    @Test
    @DisplayName("getAuditLogs returns list from InspectionController")
    void getAuditLogs_returnsList() throws Exception {
        // Process an inspection first
        List<Return> q = readPrivateField("quarantineItems", List.class);
        q.clear();
        q.add(new Return("R1", "SKU-AUD", 2, "DAMAGED", LocalDateTime.now(), LocalDate.now().plusDays(1)));
        controller.processSingleInspection();

        List<String> logs = controller.getAuditLogs();
        assertNotNull(logs);
        assertFalse(logs.isEmpty());
    }

    // -----------------------
    // Constructor with WarehouseManager test
    // -----------------------

    @Test
    @DisplayName("constructor with WarehouseManager triggers restock insert in WarehouseManager.stock")
    void constructorWithWarehouseManager_triggersRestockInsert() throws Exception {
        java.util.Map<String, Domain.Item> items = new java.util.HashMap<>();
        java.util.Map<String, java.util.List<Domain.Box>> stock = new java.util.HashMap<>();
        java.util.List<Domain.Bay> bays = new java.util.ArrayList<>();
        Domain.WarehouseManager wm = new Domain.WarehouseManager(items, stock, bays);

        QualityOperatorController ctrlWithWM = new QualityOperatorController(wm);

        Field qField = QualityOperatorController.class.getDeclaredField("quarantineItems");
        qField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Return> qList = (List<Return>) qField.get(ctrlWithWM);
        qList.clear();
        qList.add(new Return("W1", "WM-SKU", 2, "REMORSE", LocalDateTime.now(), null));

        QualityOperatorController.ProcessingResult result = ctrlWithWM.processSingleInspection();
        assertNotNull(result);
        assertEquals("RESTOCK", result.getAction());

        @SuppressWarnings("unchecked")
        List<Return> after = (List<Return>) qField.get(ctrlWithWM);
        assertEquals(0, after.size());

        assertTrue(wm.getStock().containsKey("WM-SKU"));
    }

}
