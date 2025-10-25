package Domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BayIdTest {

    // ---------- Getters ----------
    @Test
    @DisplayName("Getters return constructor values")
    void getters() {
        BayId id = new BayId("WH1", 2, 7);
        assertEquals("WH1", id.getWarehouseId());
        assertEquals(2, id.getAisle());
        assertEquals(7, id.getBay());
    }

    // ---------- compareTo ----------
    @Test
    @DisplayName("compareTo: orders by warehouseId, then aisle, then bay")
    void compareToOrdering() {
        BayId a = new BayId("A", 1, 1);
        BayId same = new BayId("A", 1, 1);
        BayId bWare = new BayId("B", 1, 1);
        BayId aAisleHigher = new BayId("A", 2, 1);
        BayId aBayHigher = new BayId("A", 1, 2);

        // self
        assertEquals(0, a.compareTo(a));

        // equality -> compareTo == 0
        assertEquals(0, a.compareTo(same));
        assertEquals(0, same.compareTo(a));

        // warehouse decides first
        assertTrue(a.compareTo(bWare) < 0);
        assertTrue(bWare.compareTo(a) > 0);

        // aisle decides if warehouse equal
        assertTrue(a.compareTo(aAisleHigher) < 0);
        assertTrue(aAisleHigher.compareTo(a) > 0);

        // bay decides if warehouse & aisle equal
        assertTrue(a.compareTo(aBayHigher) < 0);
        assertTrue(aBayHigher.compareTo(a) > 0);

        // transitivity
        BayId x = new BayId("A", 1, 1);
        BayId y = new BayId("A", 1, 2);
        BayId z = new BayId("A", 2, 1);
        assertTrue(x.compareTo(y) < 0);
        assertTrue(y.compareTo(z) < 0);
        assertTrue(x.compareTo(z) < 0);
    }

    @Test
    @DisplayName("compareTo throws NPE if this.warehouseId is null")
    void compareToThrowsWhenThisWarehouseIdNull() {
        BayId nullThis = new BayId(null, 1, 1);
        BayId other = new BayId("WH1", 1, 1);
        assertThrows(NullPointerException.class, () -> nullThis.compareTo(other));
    }

    @Test
    @DisplayName("compareTo throws NPE if other.warehouseId is null")
    void compareToThrowsWhenOtherWarehouseIdNull() {
        BayId thisOk = new BayId("WH1", 1, 1);
        BayId otherNull = new BayId(null, 1, 1);
        assertThrows(NullPointerException.class, () -> thisOk.compareTo(otherNull));
    }

    @Test
    @DisplayName("compareTo handles edge cases: zeros, negatives, large values")
    void compareToEdgeCases() {
        // Zeros
        BayId zeroBay = new BayId("WH1", 0, 0);
        BayId oneBay = new BayId("WH1", 0, 1);
        assertTrue(zeroBay.compareTo(oneBay) < 0);

        // Negatives
        BayId neg1 = new BayId("WH1", -1, 5);
        BayId pos1 = new BayId("WH1", 1, 5);
        assertTrue(neg1.compareTo(pos1) < 0);

        // Large values
        BayId large = new BayId("WH1", 1000, 2000);
        BayId small = new BayId("WH1", 999, 1999);
        assertTrue(small.compareTo(large) < 0);
    }

    @Test
    @DisplayName("compareTo handles all possible branches in the logic")
    void compareToAllBranches() {
        // Case 1: warehouseId differs
        BayId id1 = new BayId("A", 5, 10);
        BayId id2 = new BayId("B", 5, 10);
        assertTrue(id1.compareTo(id2) < 0);
        assertTrue(id2.compareTo(id1) > 0);

        // Case 2: warehouseId equal, aisle differs
        BayId id3 = new BayId("A", 5, 10);
        BayId id4 = new BayId("A", 6, 10);
        assertTrue(id3.compareTo(id4) < 0);
        assertTrue(id4.compareTo(id3) > 0);

        // Case 3: warehouseId and aisle equal, bay differs
        BayId id5 = new BayId("A", 5, 10);
        BayId id6 = new BayId("A", 5, 11);
        assertTrue(id5.compareTo(id6) < 0);
        assertTrue(id6.compareTo(id5) > 0);
    }

    // ---------- equals ----------
    @Test
    @DisplayName("equals: reflexive, symmetric, transitive, null and type checks")
    void testEquals() {
        BayId i1 = new BayId("WH1", 3, 5);
        BayId i2 = new BayId("WH1", 3, 5);
        BayId i3 = new BayId("WH1", 3, 5);

        // reflexive
        assertEquals(i1, i1);

        // symmetric
        assertEquals(i1, i2);
        assertEquals(i2, i1);

        // transitive
        assertEquals(i1, i2);
        assertEquals(i2, i3);
        assertEquals(i1, i3);

        // consistent
        assertEquals(i1, i2);
        assertEquals(i1, i2);

        // null & different type
        assertNotEquals(i1, null);
        assertNotEquals(i1, "not-a-bayid");

        // field changes break equality
        assertNotEquals(i1, new BayId("WH2", 3, 5));
        assertNotEquals(i1, new BayId("WH1", 4, 5));
        assertNotEquals(i1, new BayId("WH1", 3, 6));

        // equals implies compareTo == 0
        assertEquals(0, i1.compareTo(i2));
    }

    @Test
    @DisplayName("equals handles null warehouseId correctly")
    void equalsWithNullWarehouseId() {
        BayId nullWh1 = new BayId(null, 1, 1);
        BayId nullWh2 = new BayId(null, 1, 1);
        BayId notNullWh = new BayId("WH1", 1, 1);

        assertEquals(nullWh1, nullWh2);
        assertNotEquals(nullWh1, notNullWh);
        assertNotEquals(notNullWh, nullWh1);
    }

    @Test
    @DisplayName("equals handles edge cases: negative values, zeros")
    void equalsEdgeCases() {
        BayId id1 = new BayId("WH1", -1, -5);
        BayId id2 = new BayId("WH1", -1, -5);
        BayId id3 = new BayId("WH1", -1, -6);

        assertEquals(id1, id2);
        assertNotEquals(id1, id3);

        BayId id4 = new BayId("WH1", 0, 0);
        BayId id5 = new BayId("WH1", 0, 0);
        assertEquals(id4, id5);
    }

    @Test
    @DisplayName("equals handles same reference and different type branches")
    void equalsBranches() {
        BayId id = new BayId("WH1", 1, 1);
        // Mesma referência
        assertTrue(id.equals(id));
        // Tipo diferente
        assertFalse(id.equals(new Object()));
        assertFalse(id.equals("string"));
        assertFalse(id.equals(123));
    }

    // ---------- hashCode ----------
    @Test
    @DisplayName("hashCode consistent with equals and stable")
    void testHashCode() {
        BayId a = new BayId("WH1", 3, 5);
        BayId b = new BayId("WH1", 3, 5);
        BayId c = new BayId("WH1", 3, 6);

        // Equal objects have same hashcode
        assertEquals(a.hashCode(), b.hashCode());
        
        // Not equal objects can have different hashcodes (but not required)
        // We just verify that hashCode is consistent for the same object
        int hashA = a.hashCode();
        int hashA2 = a.hashCode();
        assertEquals(hashA, hashA2);
        
        // Test that hashCode is not always the same (different values should produce different hashes typically)
        assertNotEquals(a.hashCode(), c.hashCode());
    }

    @Test
    @DisplayName("hashCode handles null warehouseId")
    void testHashCodeNullWarehouseId() {
        BayId id1 = new BayId(null, 1, 1);
        BayId id2 = new BayId(null, 1, 1);
        
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    @DisplayName("hashCode handles edge cases")
    void testHashCodeEdgeCases() {
        BayId id1 = new BayId("WH1", 0, 0);
        BayId id2 = new BayId("WH1", -5, -10);
        
        int hash1 = id1.hashCode();
        int hash2 = id1.hashCode();
        assertEquals(hash1, hash2); // Consistent
        
        assertNotEquals(id1.hashCode(), id2.hashCode()); // Different values
    }

    @Test
    @DisplayName("Getter methods are all invoked explicitly for coverage")
    void gettersExplicitly() {
        BayId id = new BayId("WH9", 99, 88);
        // Invocar todos os getters (alguns já cobertos, mas reforça cobertura)
        assertEquals("WH9", id.getWarehouseId());
        assertEquals(99, id.getAisle());
        assertEquals(88, id.getBay());
    }

    // ---------- toString ----------
    @Test
    @DisplayName("toString returns correct format")
    void testToString() {
        BayId id = new BayId("WH1", 5, 10);
        String result = id.toString();
        assertEquals("WH1/A5/B10", result);
    }

    @Test
    @DisplayName("toString handles null warehouseId")
    void testToStringNullWarehouseId() {
        BayId id = new BayId(null, 2, 3);
        String result = id.toString();
        assertEquals("null/A2/B3", result);
    }

    @Test
    @DisplayName("toString handles edge cases: zeros, negatives")
    void testToStringEdgeCases() {
        BayId id1 = new BayId("WH1", 0, 0);
        assertEquals("WH1/A0/B0", id1.toString());

        BayId id2 = new BayId("WH2", -1, -5);
        assertEquals("WH2/A-1/B-5", id2.toString());

        BayId id3 = new BayId("WH3", 100, 200);
        assertEquals("WH3/A100/B200", id3.toString());
    }

    @Test
    @DisplayName("toString is consistent for same BayId")
    void testToStringConsistency() {
        BayId id = new BayId("WH1", 5, 10);
        String str1 = id.toString();
        String str2 = id.toString();
        assertEquals(str1, str2);
    }

    // ---------- Constructor ----------
    @Test
    @DisplayName("Constructor initializes all fields correctly")
    void constructor() {
        BayId id = new BayId("WH_TEST", 42, 99);
        assertEquals("WH_TEST", id.getWarehouseId());
        assertEquals(42, id.getAisle());
        assertEquals(99, id.getBay());
    }

    @Test
    @DisplayName("Constructor handles null warehouseId")
    void constructorNullWarehouseId() {
        BayId id = new BayId(null, 1, 2);
        assertNull(id.getWarehouseId());
        assertEquals(1, id.getAisle());
        assertEquals(2, id.getBay());
    }

    @Test
    @DisplayName("Constructor handles empty string warehouseId")
    void constructorEmptyStringWarehouseId() {
        BayId id = new BayId("", 1, 2);
        assertEquals("", id.getWarehouseId());
        assertEquals(1, id.getAisle());
        assertEquals(2, id.getBay());
    }

    // ---------- Integration tests ----------
    @Test
    @DisplayName("Full contract: equals, hashCode, compareTo")
    void fullContract() {
        BayId id1 = new BayId("WH1", 1, 1);
        BayId id2 = new BayId("WH1", 1, 1);
        BayId id3 = new BayId("WH1", 1, 2);

        // equals and hashCode consistency
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());

        // equals and compareTo consistency
        assertEquals(0, id1.compareTo(id2));

        // Not equal objects
        assertNotEquals(id1, id3);
        assertNotEquals(0, id1.compareTo(id3));
    }
}
