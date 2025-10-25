package Domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Domain.Return class
 */
class ReturnTest {

    @Test
    void constructor_setsAllFieldsCorrectly() {
        LocalDateTime ts = LocalDateTime.of(2025, 6, 1, 9, 15);
        LocalDate expiry = LocalDate.of(2025, 12, 31);

        Return r = new Return("RET-001", "SKU-ABC", 10, "DAMAGED", ts, expiry);

        assertEquals("RET-001", r.getReturnId());
        assertEquals("SKU-ABC", r.getSku());
        assertEquals(10, r.getQty());
        assertEquals("DAMAGED", r.getReason());
        assertEquals(ts, r.getTimestamp());
        assertEquals(expiry, r.getExpiryDate());
    }

    @Test
    void getters_reflectInitialValues() {
        LocalDateTime ts = LocalDateTime.now();
        Return r = new Return("X", "Y", 1, "REMORSE", ts, null);

        assertEquals("X", r.getReturnId());
        assertEquals("Y", r.getSku());
        assertEquals(1, r.getQty());
        assertEquals("REMORSE", r.getReason());
        assertEquals(ts, r.getTimestamp());
        assertNull(r.getExpiryDate());
    }

    @Test
    void setters_updateFieldsCorrectly() {
        LocalDateTime ts = LocalDateTime.now();
        Return r = new Return("R2", "SKU2", 5, "EXPIRED", ts, null);

        // change qty
        r.setQty(20);
        assertEquals(20, r.getQty());

        // change reason
        r.setReason("DAMAGED");
        assertEquals("DAMAGED", r.getReason());

        // set expiry date
        LocalDate newExpiry = LocalDate.of(2026, 1, 1);
        r.setExpiryDate(newExpiry);
        assertEquals(newExpiry, r.getExpiryDate());
    }

    @Test
    void toString_containsAllRelevantInformation() {
        LocalDateTime ts = LocalDateTime.of(2024, 11, 11, 11, 11);
        LocalDate expiry = LocalDate.of(2025, 1, 1);
        Return r = new Return("RID", "SKUZ", 3, "CYCLE_COUNT", ts, expiry);

        String s = r.toString();

        assertTrue(s.contains("returnId='RID'") || s.contains("RID"));
        assertTrue(s.contains("sku='SKUZ'") || s.contains("SKUZ"));
        assertTrue(s.contains("qty=3"));
        assertTrue(s.contains("reason='CYCLE_COUNT'") || s.contains("CYCLE_COUNT"));
        assertTrue(s.contains("timestamp=" + ts.toString()));
        assertTrue(s.contains("expiryDate=" + expiry.toString()) || s.contains("expiryDate="));
    }

    @Test
    void expiry_canBeNull_and_toStringHandlesIt() {
        LocalDateTime ts = LocalDateTime.now();
        Return r = new Return("RID2", "SKU2", 2, "REMORSE", ts, null);

        assertNull(r.getExpiryDate());

        String s = r.toString();
        // ensure no exceptions and string contains the field name
        assertTrue(s.contains("expiryDate="));
    }

    @Test
    void qty_canBeNegative_and_setterAcceptsIt() {
        LocalDateTime ts = LocalDateTime.now();
        Return r = new Return("RID3", "SKU3", -5, "TEST", ts, null);

        // initial negative qty preserved
        assertEquals(-5, r.getQty());

        // setter to another negative
        r.setQty(-10);
        assertEquals(-10, r.getQty());
    }
}
