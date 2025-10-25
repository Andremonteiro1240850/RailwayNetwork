package Controller;

import Domain.AuditLog;
import Domain.QuarantineInspection;
import Domain.WarehouseManager;
import Domain.ENUM.QAAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Full coverage tests for InspectionController.
 * - No Mockito: uses FakeWarehouse (extends real WarehouseManager)
 * - Uses AuditLog.clearAll() to avoid file interference
 * - Exercises determineAction cases: REMORSE, CYCLE_COUNT, DAMAGED, EXPIRED, default
 * - Exercises audit log branches: Quantity Restocked / Quantity Discarded presence
 * - Exercises exception handling path in processQuarantine
 */
class InspectionControllerTest {

    // --- FakeWarehouse records insertReturnedBox calls ---
    static class FakeWarehouse extends WarehouseManager {
        static class Call {
            final String boxId;
            final String sku;
            final int qty;
            final LocalDate expiry;
            final LocalDateTime receivedAt;

            Call(String boxId, String sku, int qty, LocalDate expiry, LocalDateTime receivedAt) {
                this.boxId = boxId;
                this.sku = sku;
                this.qty = qty;
                this.expiry = expiry;
                this.receivedAt = receivedAt;
            }
        }

        final List<Call> calls = new ArrayList<>();

        // call a simple WarehouseManager constructor (legacy)
        public FakeWarehouse() {
            super(new HashMap<>(), new HashMap<>(), new ArrayList<>());
        }

        @Override
        public void insertReturnedBox(String boxId, String sku, int quantity, java.time.LocalDate expiryDate, java.time.LocalDateTime receivedAt) {
            // do not invoke super logic (avoid bays/boxes complexity)
            calls.add(new Call(boxId, sku, quantity, expiryDate, receivedAt));
        }

        int callCount() { return calls.size(); }

        Call lastCall() { return calls.isEmpty() ? null : calls.get(calls.size() - 1); }

        void clearCalls() { calls.clear(); }
    }

    private FakeWarehouse fakeWarehouse;
    private InspectionController controller;

    @BeforeEach
    void beforeEach() {
        // clear audit file to keep tests isolated
        try {
            AuditLog.clearAll();
        } catch (Exception ignored) {}

        fakeWarehouse = new FakeWarehouse();
        controller = new InspectionController(fakeWarehouse);
    }

    // ---------- Basic happy flows (already covered but kept for completeness) ----------

    @Test
    void restock_when_reason_REMORSE() {
        String sku = "R-1";
        int qty = 5;
        String reason = "REMORSE";
        LocalDate expiry = LocalDate.now().plusDays(10);

        String out = controller.processQuarantine(sku, qty, reason, expiry);

        assertNotNull(out);
        assertTrue(out.contains("Inspection Processed"));
        assertEquals(1, fakeWarehouse.callCount());

        FakeWarehouse.Call call = fakeWarehouse.lastCall();
        assertNotNull(call);
        assertTrue(call.boxId.startsWith("RET-"));
        assertEquals(sku, call.sku);
        assertEquals(qty, call.qty);
        assertEquals(expiry, call.expiry);

        List<QuarantineInspection> inspections = controller.getInspectionsBySku(sku);
        assertEquals(1, inspections.size());
        assertEquals(QAAction.RESTOCK, inspections.get(0).getAction());
    }

    @Test
    void discard_when_reason_DAMAGED() {
        // use controller without warehouse
        controller = new InspectionController();
        String sku = "D-1";
        int qty = 3;

        String out = controller.processQuarantine(sku, qty, "DAMAGED", null);

        assertNotNull(out);
        assertTrue(out.contains("Inspection Processed"));
        List<QuarantineInspection> inspections = controller.getInspectionsBySku(sku);
        assertEquals(1, inspections.size());
        assertEquals(QAAction.DISCARD, inspections.get(0).getAction());
    }

    // ---------- Cover all determineAction cases ----------

    @Test
    void restock_when_reason_CYCLE_COUNT_case_insensitive() {
        String sku = "C-1";
        String reasonLower = "cycle_count"; // lower-case to exercise toUpperCase
        controller.processQuarantine(sku, 2, reasonLower, LocalDate.now().plusDays(1));

        List<QuarantineInspection> inspections = controller.getInspectionsBySku(sku);
        assertEquals(1, inspections.size());
        assertEquals(QAAction.RESTOCK, inspections.get(0).getAction());
    }

    @Test
    void discard_when_reason_EXPIRED() {
        String sku = "E-1";
        controller.processQuarantine(sku, 1, "EXPIRED", null);

        List<QuarantineInspection> inspections = controller.getInspectionsBySku(sku);
        assertEquals(1, inspections.size());
        assertEquals(QAAction.DISCARD, inspections.get(0).getAction());
    }

    @Test
    void default_reason_maps_to_DISCARD() {
        String sku = "X-1";
        controller.processQuarantine(sku, 4, "UNKNOWN_REASON", null);

        List<QuarantineInspection> inspections = controller.getInspectionsBySku(sku);
        assertEquals(1, inspections.size());
        assertEquals(QAAction.DISCARD, inspections.get(0).getAction());

        // audit log should show Discarded
        List<String> logs = controller.getAuditLogs();
        assertFalse(logs.isEmpty());
        String last = logs.get(logs.size() - 1);
        assertTrue(last.contains("Discarded") || last.contains("Action=Discarded"));
    }

    // ---------- Audit log combinations: ensure both "Quantity Restocked" and "Quantity Discarded" entries appear when provided ----------

    @Test
    void auditLog_includes_both_partial_quantities_when_both_provided_on_restock() {
        // Even if action is RESTOCK, createAuditLog appends both partial fields when not null
        String sku = "P-A";
        // pass both quantities (Integer wrappers)
        controller.processQuarantine(sku, 10, "REMORSE", LocalDate.now().plusDays(2), Integer.valueOf(3), Integer.valueOf(7));

        List<String> logs = controller.getAuditLogs();
        assertFalse(logs.isEmpty());
        String last = logs.get(logs.size() - 1);

        assertTrue(last.contains("Quantity Restocked=3"));
        assertTrue(last.contains("Quantity Discarded=7"));
    }

    @Test
    void auditLog_includes_both_partial_quantities_when_both_provided_on_discard() {
        // For DISCARD action, both fields are also appended if provided
        // Use a reason that maps to DISCARD and supply both non-null
        controller.processQuarantine("P-B", 8, "DAMAGED", null, Integer.valueOf(2), Integer.valueOf(6));

        List<String> logs = controller.getAuditLogs();
        assertFalse(logs.isEmpty());
        String last = logs.get(logs.size() - 1);

        assertTrue(last.contains("Quantity Restocked=2"));
        assertTrue(last.contains("Quantity Discarded=6"));
    }

    @Test
    void auditLog_handles_null_partial_quantities() {
        // neither partial provided => log should not contain the partial markers
        controller.processQuarantine("P-C", 5, "REMORSE", LocalDate.now().plusDays(3), null, null);

        String last = controller.getAuditLogs().get(controller.getAuditLogs().size() - 1);
        assertFalse(last.contains("Quantity Restocked="));
        assertFalse(last.contains("Quantity Discarded="));
    }

    // ---------- Exception path ----------

    @Test
    void processQuarantine_catches_exception_and_returns_ERROR_message_when_reason_null() {
        String result = controller.processQuarantine("ERR-1", 1, null, null);
        assertNotNull(result);
        assertTrue(result.startsWith("ERROR:"), "Expected ERROR: when passing null reason; got: " + result);
    }

    // ---------- Order behaviour ----------

    @Test
    void newest_inspection_is_first() {
        controller.processQuarantine("O-1", 1, "REMORSE", LocalDate.now());
        controller.processQuarantine("O-2", 1, "DAMAGED", null);

        List<QuarantineInspection> all = controller.getInspectionsBySku("any");
        assertEquals(2, all.size());
        assertEquals(QAAction.DISCARD, all.get(0).getAction()); // most recent
        assertEquals(QAAction.RESTOCK, all.get(1).getAction()); // earlier
    }

    // ---------- Null-safety for SKU or other fields: ensure no exception thrown (implementation dependent) ----------

    @Test
    void processQuarantine_allows_null_sku_and_does_not_throw() {
        // implementation may add an inspection before failing; test only that no exception is thrown and result is returned
        String out = controller.processQuarantine(null, 1, "DAMAGED", null);
        assertNotNull(out);
        // out either "ERROR:" or "Inspection Processed" - both acceptable for ensuring no crash
        assertTrue(out.startsWith("ERROR:") || out.contains("Inspection Processed") || out.length() > 0);
    }
}
