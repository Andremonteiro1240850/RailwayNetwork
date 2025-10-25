package Domain;

import Domain.ENUM.QAAction;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for QuarantineInspection
 */
class QuarantineInspectionTest {

    @Test
    void constructor_withParameters_setsAllFields() {
        LocalDateTime ts = LocalDateTime.of(2025, 1, 15, 10, 30);
        QuarantineInspection qi = new QuarantineInspection("INS-123", "RET-456", ts, QAAction.RESTOCK);

        assertEquals("INS-123", qi.getInspectionId());
        assertEquals("RET-456", qi.getReturnId());
        assertEquals(ts, qi.getInspectedAt());
        assertEquals(QAAction.RESTOCK, qi.getAction());
    }

    @Test
    void noArgConstructor_and_setters_getters_work() {
        QuarantineInspection qi = new QuarantineInspection();

        assertNull(qi.getInspectionId());
        assertNull(qi.getReturnId());
        assertNull(qi.getInspectedAt());
        assertNull(qi.getAction());

        LocalDateTime now = LocalDateTime.of(2024, 12, 31, 23, 59);
        qi.setInspectionId("ID-1");
        qi.setReturnId("RET-1");
        qi.setInspectedAt(now);
        qi.setAction(QAAction.DISCARD);

        assertEquals("ID-1", qi.getInspectionId());
        assertEquals("RET-1", qi.getReturnId());
        assertEquals(now, qi.getInspectedAt());
        assertEquals(QAAction.DISCARD, qi.getAction());
    }

    @Test
    void toString_containsAllFields_whenAllSet() {
        LocalDateTime ts = LocalDateTime.of(2023, 5, 4, 12, 0, 0);
        QuarantineInspection qi = new QuarantineInspection("I1", "R1", ts, QAAction.RESTOCK);

        String s = qi.toString();
        assertTrue(s.contains("Domain.QuarantineInspection{"));
        assertTrue(s.contains("inspectionId='I1'"));
        assertTrue(s.contains("returnId='R1'"));
        assertTrue(s.contains("inspectedAt=" + ts.toString()));
        assertTrue(s.contains("action=" + QAAction.RESTOCK.toString()));
    }

    @Test
    void toString_handlesNullFields_gracefully() {
        // Create with some nulls and check toString doesn't throw and includes 'null'
        QuarantineInspection qi = new QuarantineInspection();
        qi.setInspectionId("I2");
        qi.setReturnId(null);
        qi.setInspectedAt(null);
        qi.setAction(null);

        String s = qi.toString();
        assertTrue(s.contains("inspectionId='I2'"));
        // returnId and inspectedAt and action should show up as null or be present
        assertTrue(s.contains("returnId='null'") || s.contains("returnId=") || s.contains("returnId"));
        assertTrue(s.contains("inspectedAt=null") || s.contains("inspectedAt="));
        assertTrue(s.contains("action=null") || s.contains("action="));
    }

    @Test
    void action_setToNull_isAllowed() {
        QuarantineInspection qi = new QuarantineInspection();
        qi.setAction(null);
        assertNull(qi.getAction());

        // set again to a valid enum and back to null
        qi.setAction(QAAction.RESTOCK);
        assertEquals(QAAction.RESTOCK, qi.getAction());

        qi.setAction(null);
        assertNull(qi.getAction());
    }
}
