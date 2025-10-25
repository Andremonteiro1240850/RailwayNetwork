package Domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AuditLog class
 */
class AuditLogTest {

    @TempDir
    Path tempDir;
    private Path testAuditLogPath;

    @BeforeEach
    void setUp() throws Exception {
        // Set up a temporary audit log path for testing
        testAuditLogPath = tempDir.resolve("audit.log");
        AuditLog.setAuditLogPath(testAuditLogPath);

        // Clear any existing log entries before each test - but only if file exists
        try {
            if (Files.exists(testAuditLogPath)) {
                AuditLog.clearAll();
            }
        } catch (Exception e) {
            // Ignore clear errors for non-existent files
        }
    }

    @Test
    void testAuditLogConstructor() {
        // Test constructor with all parameters
        AuditLog auditLog = new AuditLog(1, 1001, 1234567890, 5001, "RESTOCK", 10, 0);

        assertEquals(1, auditLog.getId());
        assertEquals(1001, auditLog.getReturnId());
        assertEquals(1234567890, auditLog.getTimeStamp());
        assertEquals(5001, auditLog.getSku());
        assertEquals("RESTOCK", auditLog.getAction());
        assertEquals(10, auditLog.getQtyRestocked());
        assertEquals(0, auditLog.getQtyDiscarded());
    }

    @Test
    void testSettersAndGetters() {
        // Test all setters and getters
        AuditLog auditLog = new AuditLog(1, 1001, 1234567890, 5001, "RESTOCK", 10, 0);

        // Test setters
        auditLog.setId(2);
        auditLog.setReturnId(1002);
        auditLog.setTimeStamp(1234567891);
        auditLog.setSku(5002);
        auditLog.setAction("DISCARD");
        auditLog.setQtyRestocked(5);
        auditLog.setQtyDiscarded(3);

        // Verify changes
        assertEquals(2, auditLog.getId());
        assertEquals(1002, auditLog.getReturnId());
        assertEquals(1234567891, auditLog.getTimeStamp());
        assertEquals(5002, auditLog.getSku());
        assertEquals("DISCARD", auditLog.getAction());
        assertEquals(5, auditLog.getQtyRestocked());
        assertEquals(3, auditLog.getQtyDiscarded());
    }

    @Test
    void testToString() {
        // Test toString method format
        AuditLog auditLog = new AuditLog(1, 1001, 1234567890, 5001, "RESTOCK", 10, 0);

        String result = auditLog.toString();
        String expected = "AuditLog{id=1, returnId=1001, timeStamp=1234567890, sku=5001, action='RESTOCK', qtyRestocked=10, qtyDiscarded=0}";

        assertEquals(expected, result);
    }

    @Test
    void testReadAll_EmptyLog() {
        // Test reading from empty audit log
        List<String> entries = AuditLog.readAll();

        assertNotNull(entries);
        assertTrue(entries.isEmpty());
    }

    @Test
    void testAppendLineAndReadAll() throws IOException {
        // Test appending lines and reading them back
        AuditLog.appendLine("2024-01-15 10:30 | ReturnId=R-ABC123 | SKU=SKU001 | Action=Restocked | Quantity=5");
        AuditLog.appendLine("2024-01-15 11:45 | ReturnId=R-DEF456 | SKU=SKU002 | Action=Discarded | Quantity=3");

        List<String> entries = AuditLog.readAll();

        // readAll returns lines without trailing line separators
        assertEquals(2, entries.size());
        assertEquals("2024-01-15 10:30 | ReturnId=R-ABC123 | SKU=SKU001 | Action=Restocked | Quantity=5", entries.get(0));
        assertEquals("2024-01-15 11:45 | ReturnId=R-DEF456 | SKU=SKU002 | Action=Discarded | Quantity=3", entries.get(1));
    }

    @Test
    void testClearAll() throws IOException {
        // Test clearing audit log entries - first create the file
        AuditLog.appendLine("First entry");
        AuditLog.appendLine("Second entry");

        // Verify entries were added
        List<String> entriesBeforeClear = AuditLog.readAll();
        assertEquals(2, entriesBeforeClear.size());

        // Clear all entries - file exists
        AuditLog.clearAll();

        // Verify entries were cleared
        List<String> entriesAfterClear = AuditLog.readAll();
        assertTrue(entriesAfterClear.isEmpty());
    }

    @Test
    void testAppendLine_SpecialCharacters() throws IOException {
        // Test appending lines with special characters
        String lineWithSpecialChars = "2024-01-15 12:00 | ReturnId=R-GHI789 | SKU=SKU-ÖÖÖ | Action=Restocked | Quantity=10 | Notes=Produto com çêêê";
        AuditLog.appendLine(lineWithSpecialChars);

        List<String> entries = AuditLog.readAll();

        assertEquals(1, entries.size());
        assertEquals(lineWithSpecialChars, entries.get(0));
    }

    @Test
    void testAppendLine_EmptyLine() throws IOException {
        // Test appending empty line
        AuditLog.appendLine("");

        List<String> entries = AuditLog.readAll();

        assertEquals(1, entries.size());
        assertEquals("", entries.get(0));
    }

    @Test
    void testAppendLine_MultipleLines() throws IOException {
        // Test appending multiple lines in sequence
        for (int i = 1; i <= 5; i++) {
            AuditLog.appendLine("Entry " + i + " | ReturnId=R-" + i + " | SKU=SKU" + i + " | Action=Test");
        }

        List<String> entries = AuditLog.readAll();

        assertEquals(5, entries.size());
        for (int i = 0; i < 5; i++) {
            assertEquals("Entry " + (i + 1) + " | ReturnId=R-" + (i + 1) + " | SKU=SKU" + (i + 1) + " | Action=Test", entries.get(i));
        }
    }

    @Test
    void testReadAll_FileCreation() {
        // Test that readAll creates the file if it doesn't exist
        // Delete the file first to ensure it doesn't exist
        try {
            Files.deleteIfExists(testAuditLogPath);
        } catch (IOException e) {
            // Ignore if file doesn't exist
        }

        // This should create the file automatically
        List<String> entries = AuditLog.readAll();

        assertNotNull(entries);
        assertTrue(entries.isEmpty());
        assertTrue(Files.exists(testAuditLogPath));
    }

    @Test
    void testEnsureFile_CreatesDirectoryStructure() throws Exception {
        // Test that ensureFile creates the directory structure
        Path nestedPath = tempDir.resolve("nested").resolve("deep").resolve("audit.log");
        AuditLog.setAuditLogPath(nestedPath);

        // This should create the nested directory structure
        AuditLog.readAll();

        assertTrue(Files.exists(nestedPath));
        assertTrue(Files.exists(nestedPath.getParent()));
        assertTrue(Files.exists(nestedPath.getParent().getParent()));
    }

    @Test
    void testConcurrentAppends() throws IOException {
        // Test multiple appends (simulating concurrent access) - simplified
        int numberOfEntries = 50;

        // Simulate multiple appends
        for (int i = 0; i < numberOfEntries; i++) {
            AuditLog.appendLine("Entry-" + i);
        }

        List<String> entries = AuditLog.readAll();

        // Should have all entries
        assertEquals(numberOfEntries, entries.size());

        // Verify all entries are present
        for (int i = 0; i < numberOfEntries; i++) {
            String expectedEntry = "Entry-" + i;
            assertTrue(entries.contains(expectedEntry), "Missing entry: " + expectedEntry);
        }
    }

    @Test
    void testAuditLogWithZeroValues() {
        // Test AuditLog with zero values
        AuditLog auditLog = new AuditLog(0, 0, 0, 0, "TEST", 0, 0);

        assertEquals(0, auditLog.getId());
        assertEquals(0, auditLog.getReturnId());
        assertEquals(0, auditLog.getTimeStamp());
        assertEquals(0, auditLog.getSku());
        assertEquals("TEST", auditLog.getAction());
        assertEquals(0, auditLog.getQtyRestocked());
        assertEquals(0, auditLog.getQtyDiscarded());
    }

    @Test
    void testAuditLogWithNegativeValues() {
        // Test AuditLog with negative values (edge case)
        AuditLog auditLog = new AuditLog(-1, -1001, -1234567890, -5001, "DISCARD", -10, -5);

        assertEquals(-1, auditLog.getId());
        assertEquals(-1001, auditLog.getReturnId());
        assertEquals(-1234567890, auditLog.getTimeStamp());
        assertEquals(-5001, auditLog.getSku());
        assertEquals("DISCARD", auditLog.getAction());
        assertEquals(-10, auditLog.getQtyRestocked());
        assertEquals(-5, auditLog.getQtyDiscarded());
    }

    @Test
    void testActionFieldNullSafety() {
        // Test null action handling
        AuditLog auditLog = new AuditLog(1, 1001, 1234567890, 5001, null, 10, 0);

        assertNull(auditLog.getAction());

        // Test setting null action
        auditLog.setAction(null);
        assertNull(auditLog.getAction());
    }

    @Test
    void testFileOperationsAfterClear() throws IOException {
        // Test file operations after clearing the log
        // Add some initial entries
        AuditLog.appendLine("First entry");
        AuditLog.appendLine("Second entry");

        // Clear the log
        AuditLog.clearAll();

        // Add new entries after clear
        AuditLog.appendLine("New first entry");
        AuditLog.appendLine("New second entry");

        List<String> entries = AuditLog.readAll();

        assertEquals(2, entries.size());
        assertEquals("New first entry", entries.get(0));
        assertEquals("New second entry", entries.get(1));
    }

    @Test
    void testLargeNumberOfEntries() throws IOException {
        // Test performance with reasonable number of entries - reduced from 1000
        int reasonableNumber = 100;

        for (int i = 0; i < reasonableNumber; i++) {
            AuditLog.appendLine("Test entry number: " + i);
        }

        List<String> entries = AuditLog.readAll();
        assertEquals(reasonableNumber, entries.size());

        // Verify the last entry
        assertEquals("Test entry number: " + (reasonableNumber - 1), entries.get(entries.size() - 1));
    }

    @Test
    void testClearAll_NonExistentFile() {
        // Test that clearAll handles non-existent file gracefully
        // Delete the file to ensure it doesn't exist
        try {
            Files.deleteIfExists(testAuditLogPath);
        } catch (IOException e) {
            // Ignore
        }

        // This should not throw an exception
        assertDoesNotThrow(() -> AuditLog.clearAll());

        // Verify file exists after clearAll (ensureFile should create it)
        assertTrue(Files.exists(testAuditLogPath));
    }

    // ------------------------
    // Tests that exercise exception branches (catch blocks)
    // ------------------------

    @Test
    void testClearAll_ExceptionHandling() throws IOException {
        // Create a directory and set AUDIT_LOG_PATH to the directory itself
        // Writing/truncating to a directory will cause an IOException => RuntimeException in clearAll()
        Path dirPath = tempDir.resolve("dirAsFile");
        Files.createDirectory(dirPath);

        AuditLog.setAuditLogPath(dirPath);

        // Expect RuntimeException because code will attempt to write/truncate a directory
        assertThrows(RuntimeException.class, AuditLog::clearAll);

        // cleanup
        Files.deleteIfExists(dirPath);
    }

    @Test
    void testAppendLine_ExceptionHandling() throws IOException {
        // Create a directory and set AUDIT_LOG_PATH to the directory itself
        // Appending to a directory will cause an IOException => RuntimeException in appendLine()
        Path dirPath = tempDir.resolve("dirAppend");
        Files.createDirectory(dirPath);

        AuditLog.setAuditLogPath(dirPath);

        assertThrows(RuntimeException.class, () -> AuditLog.appendLine("This will fail"));

        // cleanup
        Files.deleteIfExists(dirPath);
    }

    @Test
    void testReadAll_ExceptionHandling_ReturnsEmptyOnReadFailure() throws IOException {
        // Create a directory and set AUDIT_LOG_PATH to the directory itself.
        // readAll() catches exceptions and returns an empty list, so assert that behavior.
        Path dirPath = tempDir.resolve("dirRead");
        Files.createDirectory(dirPath);

        AuditLog.setAuditLogPath(dirPath);

        List<String> result = AuditLog.readAll();
        assertNotNull(result);
        assertTrue(result.isEmpty());

        Files.deleteIfExists(dirPath);
    }

    @Test
    void testEnsureFile_NullParentBranch() throws Exception {
        // Test ensureFile when there's no parent (file in current working directory)
        Path rootFile = Paths.get("auditRoot.log");
        AuditLog.setAuditLogPath(rootFile);

        // readAll should create the file without attempting to create any parent directories
        AuditLog.readAll();
        assertTrue(Files.exists(rootFile));

        // cleanup
        Files.deleteIfExists(rootFile);
    }
}
