package Domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BoxTest {

    @Test
    @DisplayName("getBoxId returns the correct boxId")
    void getBoxId() {
        Box box = new Box("BOX001", "SKU001", 10,
                LocalDate.of(2025, 12, 31), LocalDateTime.now());
        assertEquals("BOX001", box.getBoxId());
    }

    @Test
    @DisplayName("getSku returns the correct SKU")
    void getSku() {
        Box box = new Box("BOX002", "SKU123", 5, 
            null, LocalDateTime.now());
        assertEquals("SKU123", box.getSku());
    }

    @Test
    @DisplayName("getQty returns the correct quantity")
    void getQty() {
        Box box = new Box("BOX003", "SKU001", 15, 
            LocalDate.of(2025, 6, 30), LocalDateTime.now());
        assertEquals(15, box.getQty());
    }

    @Test
    @DisplayName("getExpiryDate returns null for non-perishable items")
    void getExpiryDate() {
        Box nonPerishable = new Box("BOX004", "SKU001", 20, 
            null, LocalDateTime.now());
        assertNull(nonPerishable.getExpiryDate());
        
        Box perishable = new Box("BOX005", "SKU002", 25, 
            LocalDate.of(2025, 8, 15), LocalDateTime.now());
        assertEquals(LocalDate.of(2025, 8, 15), perishable.getExpiryDate());
    }

    @Test
    @DisplayName("getReceivedAt returns the correct received timestamp")
    void getReceivedAt() {
        LocalDateTime timestamp = LocalDateTime.of(2025, 1, 15, 10, 30);
        Box box = new Box("BOX006", "SKU001", 12, 
            LocalDate.of(2025, 9, 20), timestamp);
        assertEquals(timestamp, box.getReceivedAt());
    }

    @Test
    @DisplayName("getLocation returns null when box has no location")
    void getLocation() {
        Box boxNoLoc = new Box("BOX007", "SKU001", 8, 
            LocalDate.of(2025, 10, 10), LocalDateTime.now());
        assertNull(boxNoLoc.getLocation());
        
        BayId location = new BayId("W1", 1, 1);
        Box boxWithLoc = new Box("BOX008", "SKU002", 10, 
            LocalDate.of(2025, 11, 11), LocalDateTime.now(), location);
        assertEquals(location, boxWithLoc.getLocation());
    }

    @Test
    @DisplayName("setLocation updates the box location")
    void setLocation() {
        Box box = new Box("BOX009", "SKU001", 5, 
            LocalDate.of(2025, 12, 5), LocalDateTime.now());
        assertNull(box.getLocation());
        
        BayId location = new BayId("W1", 2, 3);
        box.setLocation(location);
        assertEquals(location, box.getLocation());
        
        BayId newLocation = new BayId("W2", 4, 5);
        box.setLocation(newLocation);
        assertEquals(newLocation, box.getLocation());
    }

    @Test
    @DisplayName("reduceQty decreases the quantity by the specified amount")
    void reduceQty() {
        Box box = new Box("BOX010", "SKU001", 100, 
            LocalDate.of(2026, 1, 1), LocalDateTime.now());
        
        box.reduceQty(30);
        assertEquals(70, box.getQty());
        
        box.reduceQty(20);
        assertEquals(50, box.getQty());
        
        box.reduceQty(50);
        assertEquals(0, box.getQty());
    }

    @Test
    @DisplayName("setQty sets the quantity to the specified value")
    void setQty() {
        Box box = new Box("BOX011", "SKU001", 50, 
            LocalDate.of(2025, 6, 15), LocalDateTime.now());
        
        box.setQty(75);
        assertEquals(75, box.getQty());
        
        box.setQty(0);
        assertEquals(0, box.getQty());
        
        box.setQty(200);
        assertEquals(200, box.getQty());
    }

    @Test
    @DisplayName("getAisle returns null for compatibility")
    void getAisle() {
        Box box = new Box("BOX012", "SKU001", 10, 
            null, LocalDateTime.now());
        assertNull(box.getAisle());
    }

    @Test
    @DisplayName("getBay returns null for compatibility")
    void getBay() {
        Box box = new Box("BOX013", "SKU001", 15, 
            LocalDate.of(2025, 7, 10), LocalDateTime.now());
        assertNull(box.getBay());
    }

    @Test
    @DisplayName("FEFO_COMPARATOR orders by expiryDate (earliest first, nulls last)")
    void fefoComparatorExpiryDate() {
        LocalDateTime baseTime = LocalDateTime.of(2025, 1, 1, 10, 0);
        
        // Boxes with different expiry dates
        Box earlyExpiry = new Box("E1", "SKU001", 10, 
            LocalDate.of(2025, 3, 1), baseTime);
        Box lateExpiry = new Box("E2", "SKU001", 10, 
            LocalDate.of(2025, 5, 1), baseTime);
        Box nullExpiry = new Box("E3", "SKU001", 10, 
            null, baseTime);
        
        // Early expiry comes before late expiry
        assertTrue(Box.FEFO_COMPARATOR.compare(earlyExpiry, lateExpiry) < 0);
        assertTrue(Box.FEFO_COMPARATOR.compare(lateExpiry, earlyExpiry) > 0);
        
        // Non-null expiry comes before null expiry
        assertTrue(Box.FEFO_COMPARATOR.compare(earlyExpiry, nullExpiry) < 0);
        assertTrue(Box.FEFO_COMPARATOR.compare(nullExpiry, earlyExpiry) > 0);
    }

    @Test
    @DisplayName("FEFO_COMPARATOR orders by receivedAt when expiryDate is equal")
    void fefoComparatorReceivedAt() {
        LocalDate sameExpiry = LocalDate.of(2025, 4, 1);
        
        Box earlyReceived = new Box("R1", "SKU001", 10, 
            sameExpiry, LocalDateTime.of(2025, 1, 1, 8, 0));
        Box lateReceived = new Box("R2", "SKU001", 10, 
            sameExpiry, LocalDateTime.of(2025, 1, 1, 10, 0));
        
        assertTrue(Box.FEFO_COMPARATOR.compare(earlyReceived, lateReceived) < 0);
        assertTrue(Box.FEFO_COMPARATOR.compare(lateReceived, earlyReceived) > 0);
    }

    @Test
    @DisplayName("FEFO_COMPARATOR orders by boxId when expiry and receivedAt are equal")
    void fefoComparatorBoxId() {
        LocalDate sameExpiry = LocalDate.of(2025, 5, 1);
        LocalDateTime sameReceived = LocalDateTime.of(2025, 1, 1, 9, 0);
        
        Box idA = new Box("A", "SKU001", 10, sameExpiry, sameReceived);
        Box idB = new Box("B", "SKU001", 10, sameExpiry, sameReceived);
        Box idC = new Box("C", "SKU001", 10, sameExpiry, sameReceived);
        
        assertTrue(Box.FEFO_COMPARATOR.compare(idA, idB) < 0);
        assertTrue(Box.FEFO_COMPARATOR.compare(idB, idC) < 0);
        assertTrue(Box.FEFO_COMPARATOR.compare(idA, idC) < 0);
    }

    @Test
    @DisplayName("FEFO_COMPARATOR returns 0 for equal boxes")
    void fefoComparatorEqual() {
        LocalDate expiry = LocalDate.of(2025, 6, 15);
        LocalDateTime received = LocalDateTime.of(2025, 1, 1, 10, 0);
        
        Box box1 = new Box("BOX001", "SKU001", 10, expiry, received);
        Box box2 = new Box("BOX001", "SKU001", 10, expiry, received);
        
        assertEquals(0, Box.FEFO_COMPARATOR.compare(box1, box2));
    }

    @Test
    @DisplayName("testEquals returns true for equal boxes")
    void testEquals() {
        Box box1 = new Box("BOX001", "SKU001", 10, 
            LocalDate.of(2025, 7, 1), LocalDateTime.now());
        Box box2 = new Box("BOX001", "SKU001", 10, 
            LocalDate.of(2025, 7, 1), LocalDateTime.now());
        
        assertEquals(box1, box2);
        assertEquals(box2, box1);
        
        // Same reference
        assertEquals(box1, box1);
    }

    @Test
    @DisplayName("testEquals returns false for different boxIds")
    void testEqualsDifferentId() {
        LocalDateTime time = LocalDateTime.now();
        Box box1 = new Box("BOX001", "SKU001", 10, 
            LocalDate.of(2025, 8, 1), time);
        Box box2 = new Box("BOX002", "SKU001", 10, 
            LocalDate.of(2025, 8, 1), time);
        
        assertNotEquals(box1, box2);
    }

    @Test
    @DisplayName("testEquals returns false for null and different types")
    void testEqualsNullAndType() {
        Box box = new Box("BOX003", "SKU001", 10, 
            null, LocalDateTime.now());
        
        assertNotEquals(box, null);
        assertNotEquals(box, "not a box");
        assertNotEquals(box, new Object());
    }

    @Test
    @DisplayName("testHashCode returns same value for equal boxes")
    void testHashCode() {
        Box box1 = new Box("BOX004", "SKU001", 5, 
            LocalDate.of(2025, 9, 1), LocalDateTime.now());
        Box box2 = new Box("BOX004", "SKU002", 15, 
            LocalDate.of(2026, 1, 1), LocalDateTime.now());
        
        assertEquals(box1.hashCode(), box2.hashCode());
    }

    @Test
    @DisplayName("testHashCode is consistent")
    void testHashCodeConsistent() {
        Box box = new Box("BOX005", "SKU001", 10, 
            LocalDate.of(2025, 10, 1), LocalDateTime.now());
        
        int hash1 = box.hashCode();
        int hash2 = box.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test
    @DisplayName("testToString includes boxId, sku, and qty")
    void testToString() {
        Box box = new Box("BOX006", "SKU123", 25, 
            LocalDate.of(2025, 11, 15), LocalDateTime.now());
        
        String str = box.toString();
        assertTrue(str.contains("BOX006"));
        assertTrue(str.contains("SKU123"));
        assertTrue(str.contains("25"));
    }

    @Test
    @DisplayName("Constructor with location sets all fields correctly")
    void constructorWithLocation() {
        BayId location = new BayId("W1", 3, 5);
        LocalDate expiry = LocalDate.of(2025, 12, 31);
        LocalDateTime received = LocalDateTime.of(2025, 1, 1, 12, 0);
        
        Box box = new Box("BOX007", "SKU001", 20, expiry, received, location);
        
        assertEquals("BOX007", box.getBoxId());
        assertEquals("SKU001", box.getSku());
        assertEquals(20, box.getQty());
        assertEquals(expiry, box.getExpiryDate());
        assertEquals(received, box.getReceivedAt());
        assertEquals(location, box.getLocation());
    }

    @Test
    @DisplayName("Constructor without location sets location to null")
    void constructorWithoutLocation() {
        Box box = new Box("BOX008", "SKU001", 15, 
            LocalDate.of(2025, 8, 10), LocalDateTime.now());
        
        assertNull(box.getLocation());
        assertEquals("BOX008", box.getBoxId());
        assertEquals("SKU001", box.getSku());
        assertEquals(15, box.getQty());
    }

    @Test
    @DisplayName("FEFO_COMPARATOR handles null expiry dates correctly")
    void fefoComparatorNullExpiry() {
        LocalDateTime time = LocalDateTime.of(2025, 1, 1, 10, 0);
        
        Box withExpiry = new Box("E1", "SKU001", 10, 
            LocalDate.of(2025, 3, 1), time);
        Box nullExpiry1 = new Box("N1", "SKU001", 10, 
            null, time);
        Box nullExpiry2 = new Box("N2", "SKU001", 10, 
            null, time);
        
        // With expiry comes before null expiry
        assertTrue(Box.FEFO_COMPARATOR.compare(withExpiry, nullExpiry1) < 0);
        
        // Both null expiry - compare by receivedAt then boxId
        int result = Box.FEFO_COMPARATOR.compare(nullExpiry1, nullExpiry2);
        assertTrue(result != 0); // N1 < N2 alphabetically
    }
}