package Domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BayTest {

    // ---------- Helpers ----------
    private static BayId id(String wh, int a, int b) {
        return new BayId(wh, a, b);
    }

    private static Box box(String boxId, String sku, int qty,
                           LocalDate expiry, LocalDateTime received, BayId loc) {
        return new Box(boxId, sku, qty, expiry, received, loc);
    }

    private static Box boxNoLoc(String boxId, String sku, int qty,
                                LocalDate expiry, LocalDateTime received) {
        return new Box(boxId, sku, qty, expiry, received);
    }


    @Test
    void getId() {
        BayId bid = id("W1", 1, 1);
        Bay bay = new Bay(bid, 10);
        assertSame(bid, bay.getId());
    }

    @Test
    void getCapacityBoxes() {
        Bay bay = new Bay(id("W1", 1, 1), 7);
        assertEquals(7, bay.getCapacityBoxes());
    }


    @Test
    void insertBox() {
        Bay bay = new Bay(id("W1", 1, 1), 10);
        Box b1 = boxNoLoc("X1", "SKU-A", 5,
                LocalDate.of(2026, 1, 10), LocalDateTime.of(2025, 1, 1, 10, 0));

        // Before insert: no dedicated SKU, can accept any
        assertNull(bay.getDedicatedSku());
        assertTrue(bay.canAcceptSku("SKU-A"));
        assertTrue(bay.canAcceptSku("SKU-B"));
        assertTrue(bay.isEmpty());

        bay.insertBox(b1);

        // After first insert: dedicated SKU becomes SKU-A
        assertEquals("SKU-A", bay.getDedicatedSku());
        assertTrue(bay.isDedicatedToSku("SKU-A"));
        assertTrue(bay.canAcceptSku("SKU-A"));
        assertFalse(bay.canAcceptSku("SKU-B"));
        assertFalse(bay.isEmpty());
        assertTrue(bay.hasSku("SKU-A"));
        assertEquals(1, bay.getBoxCountForSku("SKU-A"));
        assertEquals(5, bay.getTotalQuantityForSku("SKU-A"));

        // Trying to insert a different SKU should fail
        Box wrongSku = boxNoLoc("X2", "SKU-B", 3,
                LocalDate.of(2026, 1, 11), LocalDateTime.of(2025, 1, 1, 11, 0));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> bay.insertBox(wrongSku));
        assertTrue(ex.getMessage().contains("is dedicated to SKU"));
    }


    @Test
    void peekAndPollFrontFefo() {
        Bay bay = new Bay(id("W1", 1, 2), 10);

        // Three boxes of same SKU:
        // b2 should come first (same expiry as b1 but received earlier)
        // b1 second
        // b3 last (null expiry comes after dated expiries)
        Box b1 = boxNoLoc("B1", "SKU-A", 2,
                LocalDate.of(2026, 1, 10), LocalDateTime.of(2025, 1, 1, 10, 0));
        Box b2 = boxNoLoc("B0", "SKU-A", 4,
                LocalDate.of(2026, 1, 10), LocalDateTime.of(2025, 1, 1, 9, 0)); // earlier received
        Box b3 = boxNoLoc("B2", "SKU-A", 1,
                null, LocalDateTime.of(2025, 1, 1, 8, 0)); // null expiry → after others

        bay.insertBox(b1);
        bay.insertBox(b2);
        bay.insertBox(b3);

        // peek does not remove
        assertEquals("B0", bay.peekFront("SKU-A").getBoxId());
        assertEquals(3, bay.getBoxCountForSku("SKU-A"));

        // poll removes in order
        assertEquals("B0", bay.pollFront("SKU-A").getBoxId()); // b2
        assertEquals("B1", bay.peekFront("SKU-A").getBoxId()); // now b1 on top
        assertEquals("B1", bay.pollFront("SKU-A").getBoxId()); // b1
        assertEquals("B2", bay.pollFront("SKU-A").getBoxId()); // b3 (null expiry last)

        // Now empty for that SKU
        assertNull(bay.peekFront("SKU-A"));
        assertNull(bay.pollFront("SKU-A"));
        assertFalse(bay.hasSku("SKU-A"));

        // Behavior note: dedicatedSku is NOT cleared by pollFront()
        assertTrue(bay.isEmpty());
        assertEquals("SKU-A", bay.getDedicatedSku());
        // available SKUs still contains the key if the internal map kept an empty set
        List<String> skus = bay.getAvailableSkus();
        // either it still lists SKU-A or not: both are possible depending on map state; assert consistency with hasSku()
        if (skus.contains("SKU-A")) {
            assertFalse(bay.hasSku("SKU-A"));
        }
    }


    @Test
    void removeBox() {
        Bay bay = new Bay(id("W1", 1, 3), 10);
        Box b1 = boxNoLoc("R1", "SKU-X", 1,
                LocalDate.of(2026, 6, 1), LocalDateTime.of(2025, 1, 2, 12, 0));
        Box b2 = boxNoLoc("R2", "SKU-X", 2,
                LocalDate.of(2026, 7, 1), LocalDateTime.of(2025, 1, 2, 13, 0));

        bay.insertBox(b1);
        bay.insertBox(b2);

        assertEquals("SKU-X", bay.getDedicatedSku());
        assertEquals(2, bay.getBoxCountForSku("SKU-X"));
        assertEquals(3, bay.getTotalQuantityForSku("SKU-X"));
        assertTrue(bay.hasSku("SKU-X"));

        // Remove one box
        bay.removeBox(b1);
        assertEquals(1, bay.getBoxCountForSku("SKU-X"));
        assertEquals(2, bay.getTotalQuantityForSku("SKU-X"));
        assertEquals("SKU-X", bay.getDedicatedSku());
        assertTrue(bay.hasSku("SKU-X"));

        // Remove last box — should drop SKU entry and clear dedicated SKU
        bay.removeBox(b2);
        assertEquals(0, bay.getBoxCountForSku("SKU-X"));
        assertEquals(0, bay.getTotalQuantityForSku("SKU-X"));
        assertFalse(bay.hasSku("SKU-X"));
        assertTrue(bay.isEmpty());
        assertNull(bay.getDedicatedSku());
        assertFalse(bay.getAvailableSkus().contains("SKU-X"));
    }


    @Test

    void isFull() {
        Bay bay = new Bay(id("W1", 2, 1), 2);
        Box b1 = boxNoLoc("C1", "SKU-C", 1,
                LocalDate.of(2026, 1, 1), LocalDateTime.of(2025, 1, 1, 9, 0));
        Box b2 = boxNoLoc("C2", "SKU-C", 1,
                LocalDate.of(2026, 2, 1), LocalDateTime.of(2025, 1, 1, 10, 0));
        Box b3 = boxNoLoc("C3", "SKU-C", 1,
                LocalDate.of(2026, 3, 1), LocalDateTime.of(2025, 1, 1, 11, 0));

        bay.insertBox(b1);
        bay.insertBox(b2);
        assertTrue(bay.isFull());
        assertEquals(2, bay.totalBoxes());

        // Insert beyond capacity (class allows it)
        bay.insertBox(b3);
        assertTrue(bay.isFull());
        assertEquals(3, bay.totalBoxes());
    }

    @Test

    void totalBoxesAndGetAllBoxes() {
        Bay bay = new Bay(id("W1", 2, 2), 10);
        Box a1 = boxNoLoc("A1", "SKU-A", 5,
                LocalDate.of(2026, 1, 1), LocalDateTime.of(2025, 1, 1, 9, 0));
        Box a2 = boxNoLoc("A2", "SKU-A", 6,
                LocalDate.of(2026, 1, 2), LocalDateTime.of(2025, 1, 1, 10, 0));

        bay.insertBox(a1);
        bay.insertBox(a2);

        Collection<Box> all = bay.getAllBoxes();
        assertEquals(2, all.size());
        assertTrue(all.contains(a1));
        assertTrue(all.contains(a2));
        assertEquals(2, bay.totalBoxes());
    }


    @Test
    void perSkuMetrics() {
        Bay bay = new Bay(id("W1", 3, 3), 10);
        assertEquals(0, bay.getBoxCountForSku("SKU-Z"));
        assertEquals(0, bay.getTotalQuantityForSku("SKU-Z"));
        assertFalse(bay.hasSku("SKU-Z"));

        Box z1 = boxNoLoc("Z1", "SKU-Z", 7,
                null, LocalDateTime.of(2025, 1, 3, 8, 0));
        bay.insertBox(z1);

        assertEquals(1, bay.getBoxCountForSku("SKU-Z"));
        assertEquals(7, bay.getTotalQuantityForSku("SKU-Z"));
        assertTrue(bay.hasSku("SKU-Z"));
        assertTrue(bay.getAvailableSkus().contains("SKU-Z"));
    }


    @Test
    void isEmptyAndAcceptance() {
        Bay bay = new Bay(id("W1", 4, 4), 10);
        assertTrue(bay.isEmpty());
        assertTrue(bay.canAcceptSku("ANY"));
        assertFalse(bay.isDedicatedToSku("ANY"));

        Box x = boxNoLoc("X", "SKU-X", 1,
                LocalDate.of(2026, 1, 1), LocalDateTime.of(2025, 1, 1, 12, 0));
        bay.insertBox(x);

        assertFalse(bay.isEmpty());
        assertTrue(bay.isDedicatedToSku("SKU-X"));
        assertFalse(bay.isDedicatedToSku("SKU-Y"));
        assertTrue(bay.canAcceptSku("SKU-X"));
        assertFalse(bay.canAcceptSku("SKU-Y"));
    }


    @Test
    void testToString() {
        Bay bay = new Bay(id("W9", 9, 9), 10);
        Box b = boxNoLoc("T1", "SKU-T", 3,
                LocalDate.of(2027, 1, 1), LocalDateTime.of(2025, 2, 1, 8, 0));
        bay.insertBox(b);

        String s = bay.toString();
        assertTrue(s.startsWith("Bay{"));
        assertTrue(s.contains("W9/A9/B9"));
        assertTrue(s.contains("boxes=1"));
        assertTrue(s.contains("dedicatedSku=SKU-T"));
    }


    @Test
    void peekPollUnknownSku() {
        Bay bay = new Bay(id("W1", 5, 5), 5);
        assertNull(bay.peekFront("NOPE"));
        assertNull(bay.pollFront("NOPE"));
    }
}
