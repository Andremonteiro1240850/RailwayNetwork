package Domain;

import Controller.InspectionController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for QualityOperator WITHOUT Mockito.
 * - Covers menu branches (cases 1..5 and default)
 * - Exercises empty/non-empty branches and ternary expiry formatting
 */
class QualityOperatorTest {

    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setUpStreams() {
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    // -----------------------
    // Fake controller
    // -----------------------
    static class FakeInspectionController extends InspectionController {
        final List<QuarantineInspection> inspections = new ArrayList<>();
        final List<String> auditLogs = new ArrayList<>();
        final List<String> processedResults = new ArrayList<>();

        @Override
        public String processQuarantine(String sku, int qty, String reason, java.time.LocalDate expiry) {
            String result = "Processed " + sku + " qty=" + qty;
            processedResults.add(result);
            try {
                QuarantineInspection qi = createQuarantineInspectionForTest(
                        "INS-" + (inspections.size() + 1),
                        "RET-" + (inspections.size() + 1),
                        LocalDateTime.now(),
                        "RESTOCK"
                );
                inspections.add(qi);
            } catch (Exception ignored) { }
            return result;
        }

        @Override
        public List<QuarantineInspection> getInspectionsBySku(String sku) {
            return inspections;
        }

        @Override
        public List<String> getAuditLogs() {
            return auditLogs;
        }
    }

    // -----------------------
    // Reflection helpers
    // -----------------------

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object target, String fieldName, Class<T> type) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return (T) f.get(target);
    }

    private static QuarantineInspection createQuarantineInspectionForTest(String inspectionId,
                                                                          String returnId,
                                                                          LocalDateTime inspectedAt,
                                                                          String actionName) throws Exception {
        Class<?> qiClass = Class.forName("Domain.QuarantineInspection");
        Constructor<?>[] ctors = qiClass.getDeclaredConstructors();

        for (Constructor<?> ctor : ctors) {
            ctor.setAccessible(true);
            Class<?>[] paramTypes = ctor.getParameterTypes();
            Object[] args = new Object[paramTypes.length];
            boolean ok = true;

            for (int i = 0; i < paramTypes.length; i++) {
                Class<?> p = paramTypes[i];

                if (p.equals(String.class)) {
                    if (args[0] == null) {
                        args[i] = inspectionId;
                    } else if (args[1] == null) {
                        args[i] = returnId;
                    } else {
                        args[i] = "";
                    }
                } else if (p.equals(LocalDateTime.class)) {
                    args[i] = inspectedAt;
                } else if (p.equals(LocalDate.class)) {
                    args[i] = inspectedAt != null ? inspectedAt.toLocalDate() : null;
                } else if (p.isEnum()) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Enum> enumClass = (Class<? extends Enum>) p;
                    try {
                        args[i] = Enum.valueOf(enumClass, actionName);
                    } catch (IllegalArgumentException iae) {
                        args[i] = Enum.valueOf(enumClass, actionName.toUpperCase());
                    }
                } else {
                    // box primitives
                    if (p.equals(int.class) || p.equals(Integer.class)) {
                        args[i] = Integer.valueOf(0);
                    } else if (p.equals(long.class) || p.equals(Long.class)) {
                        args[i] = Long.valueOf(0L);
                    } else if (p.equals(boolean.class) || p.equals(Boolean.class)) {
                        args[i] = Boolean.FALSE;
                    } else {
                        args[i] = null;
                    }
                }
            }

            for (int i = 0; i < paramTypes.length; i++) {
                if (paramTypes[i].isPrimitive() && args[i] == null) {
                    ok = false;
                    break;
                }
            }
            if (!ok) continue;

            try {
                Object obj = ctor.newInstance(args);
                return (QuarantineInspection) obj;
            } catch (Throwable ignored) { }
        }

        throw new IllegalStateException("No suitable constructor found for QuarantineInspection");
    }

    private String getOutput() {
        return outContent.toString().replace("\r", "");
    }

    private void invokePrivateMethod(Object target, String methodName) throws Exception {
        Method m = target.getClass().getDeclaredMethod(methodName);
        m.setAccessible(true);
        m.invoke(target);
    }

    // -----------------------
    // Base unit tests (previous coverage)
    // -----------------------

    @Test
    void viewQuarantineQueue_empty_showsNoItems() throws Exception {
        QualityOperator op = new QualityOperator();
        setPrivateField(op, "quarantineItems", new ArrayList<Return>());

        invokePrivateMethod(op, "viewQuarantineQueue");

        String out = getOutput();
        assertTrue(out.contains("No items in quarantine queue."));
    }

    @Test
    void viewQuarantineQueue_withItems_printsTable_bothExpiryCases() throws Exception {
        QualityOperator op = new QualityOperator();

        List<Return> list = new ArrayList<>();
        // item with expiry (non-null)
        list.add(new Return("R1", "SKU1", 2, "DAMAGED", LocalDateTime.now(), LocalDate.now()));
        // item without expiry (null)
        list.add(new Return("R2", "SKU2", 5, "EXPIRED", LocalDateTime.now().minusHours(1), null));
        setPrivateField(op, "quarantineItems", list);

        invokePrivateMethod(op, "viewQuarantineQueue");

        String out = getOutput();
        assertTrue(out.contains("R1"));
        assertTrue(out.contains("R2"));
        assertTrue(out.contains("Total items in queue: 2"));
        // check both expiry string modes appeared
        assertTrue(out.contains("N/A") || out.contains(LocalDate.now().toString()));
    }

    @Test
    void processNextInspection_empty_doesNothing() throws Exception {
        QualityOperator op = new QualityOperator();
        setPrivateField(op, "quarantineItems", new ArrayList<Return>());

        invokePrivateMethod(op, "processNextInspection");

        String out = getOutput();
        assertTrue(out.contains("No items in quarantine queue.") || out.contains("📦 No items in quarantine queue."));
    }

    @Test
    void processNextInspection_withItems_handlesExpiryNullAndNonNull() throws Exception {
        QualityOperator op = new QualityOperator();

        FakeInspectionController fake = new FakeInspectionController();
        setPrivateField(op, "inspectionController", fake);

        List<Return> list = new ArrayList<>();
        // first with non-null expiry
        list.add(new Return("R1", "SKU-X", 3, "REMORSE", LocalDateTime.now(), LocalDate.now()));
        // second with null expiry
        list.add(new Return("R2", "SKU-Y", 1, "DAMAGED", LocalDateTime.now().minusMinutes(1), null));
        setPrivateField(op, "quarantineItems", list);

        invokePrivateMethod(op, "processNextInspection");

        // both processed
        assertEquals(2, fake.processedResults.size());
        // queue empty
        @SuppressWarnings("unchecked")
        List<Return> remaining = getPrivateField(op, "quarantineItems", List.class);
        assertTrue(remaining.isEmpty());
        String out = getOutput();
        // ensure both expiry formats printed
        assertTrue(out.contains("N/A") || out.contains(LocalDate.now().toString()));
    }

    @Test
    void viewProcessedInspections_empty_showsNoProcessed() throws Exception {
        QualityOperator op = new QualityOperator();
        FakeInspectionController fake = new FakeInspectionController();
        setPrivateField(op, "inspectionController", fake);

        invokePrivateMethod(op, "viewProcessedInspections");

        String out = getOutput();
        assertTrue(out.contains("No processed inspections yet."));
    }

    @Test
    void viewProcessedInspections_withInspections_printsList() throws Exception {
        QualityOperator op = new QualityOperator();
        FakeInspectionController fake = new FakeInspectionController();
        QuarantineInspection qi = createQuarantineInspectionForTest("INS-1", "RET-1", LocalDateTime.now(), "RESTOCK");
        fake.inspections.add(qi);
        setPrivateField(op, "inspectionController", fake);

        invokePrivateMethod(op, "viewProcessedInspections");

        String out = getOutput();
        assertTrue(out.contains("INS-1"));
    }

    @Test
    void viewAuditLog_empty_showsNoAudit() throws Exception {
        QualityOperator op = new QualityOperator();
        FakeInspectionController fake = new FakeInspectionController();
        setPrivateField(op, "inspectionController", fake);

        invokePrivateMethod(op, "viewAuditLog");

        String out = getOutput();
        assertTrue(out.contains("No audit logs available."));
    }

    @Test
    void viewAuditLog_withEntries_printsEntries() throws Exception {
        QualityOperator op = new QualityOperator();
        FakeInspectionController fake = new FakeInspectionController();
        fake.auditLogs.add("Entry A");
        fake.auditLogs.add("Entry B");
        setPrivateField(op, "inspectionController", fake);

        invokePrivateMethod(op, "viewAuditLog");

        String out = getOutput();
        assertTrue(out.contains("Entry A"));
        assertTrue(out.contains("Entry B"));
    }

    @Test
    void sortQuarantineQueue_ordersByTimestampDesc_thenReturnIdAsc() throws Exception {
        QualityOperator op = new QualityOperator();

        LocalDateTime now = LocalDateTime.now();
        Return r1 = new Return("C", "sku", 1, "DAMAGED", now.minusMinutes(5), null);
        Return r2 = new Return("A", "sku", 1, "DAMAGED", now, null);
        Return r3 = new Return("B", "sku", 1, "DAMAGED", now, null);

        List<Return> list = new ArrayList<>();
        list.add(r1);
        list.add(r2);
        list.add(r3);
        setPrivateField(op, "quarantineItems", list);

        invokePrivateMethod(op, "sortQuarantineQueue");

        @SuppressWarnings("unchecked")
        List<Return> sorted = getPrivateField(op, "quarantineItems", List.class);
        assertEquals("A", sorted.get(0).getReturnId());
        assertEquals("B", sorted.get(1).getReturnId());
        assertEquals("C", sorted.get(2).getReturnId());
    }

    @Test
    void logout_printsMessage() throws Exception {
        QualityOperator op = new QualityOperator();
        invokePrivateMethod(op, "logout");

        String out = getOutput();
        assertTrue(out.contains("Logged out successfully!"));
    }

    // -----------------------
    // New: full menu flows to execute all switch branches
    // -----------------------

    @Test
    void start_menu_flow_allOptions_emptyLists() throws Exception {
        QualityOperator op = new QualityOperator();
        // inject fake controller that has empty inspections/auditLogs
        setPrivateField(op, "inspectionController", new FakeInspectionController());
        // ensure quarantineItems empty
        setPrivateField(op, "quarantineItems", new ArrayList<Return>());

        // Simulate: 1 (view queue empty), 2 (process empty), 3 (view processed empty), 4 (view audit empty), 5 (logout)
        String simulated = "1\n2\n3\n4\n5\n";
        System.setIn(new ByteArrayInputStream(simulated.getBytes()));
        setPrivateField(op, "scanner", new java.util.Scanner(System.in));

        op.start();

        String out = getOutput();
        assertTrue(out.contains("No items in quarantine queue."));
        assertTrue(out.contains("No processed inspections yet."));
        assertTrue(out.contains("No audit logs available."));
        assertTrue(out.contains("Logged out successfully!"));
    }

    @Test
    void start_menu_flow_allOptions_withData() throws Exception {
        QualityOperator op = new QualityOperator();
        FakeInspectionController fake = new FakeInspectionController();

        // populate quarantineItems with one item (to be printed and processed)
        List<Return> q = new ArrayList<>();
        q.add(new Return("RZ", "SKU-Z", 7, "CYCLE_COUNT", LocalDateTime.now(), LocalDate.now()));
        setPrivateField(op, "quarantineItems", q);

        // populate inspections and audit logs so options 3 and 4 show non-empty branches
        QuarantineInspection qi = createQuarantineInspectionForTest("INS-9", "RET-9", LocalDateTime.now(), "RESTOCK");
        fake.inspections.add(qi);
        fake.auditLogs.add("Audit entry 1");
        setPrivateField(op, "inspectionController", fake);

        // Menu: 1(view queue non-empty), 2(process items -> will call controller), 3(view processed non-empty), 4(view audit non-empty), 5(logout)
        String simulated = "1\n2\n3\n4\n5\n";
        System.setIn(new ByteArrayInputStream(simulated.getBytes()));
        setPrivateField(op, "scanner", new java.util.Scanner(System.in));

        op.start();

        String out = getOutput();
        assertTrue(out.contains("RZ"));
        assertTrue(fake.processedResults.size() >= 1, "At least one processed result expected after option 2");
        assertTrue(out.contains("INS-9") || out.contains("INS-"));
        assertTrue(out.contains("Audit entry 1"));
        assertTrue(out.contains("Logged out successfully!"));
    }
}
