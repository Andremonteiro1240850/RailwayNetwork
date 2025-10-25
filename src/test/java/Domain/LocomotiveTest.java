package Domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Locomotive class.
 */
class LocomotiveTest {

    private Locomotive electricLoco;
    private Locomotive dieselLoco;
    private Locomotive trimmedLoco;

    @BeforeEach
    void setUp() {
        electricLoco = new Locomotive("L4000", "Electric", 120.0, 1668.0);
        dieselLoco = new Locomotive("L200", "DIESEL", 80.5, 1435.0);
        trimmedLoco = new Locomotive(" L500 ", " electric ", 150.0, 1000.0);
    }

    // --- Constructor Tests ---

    @Test
    void testConstructor_ValidElectric() {
        assertEquals("L4000", electricLoco.getId());
        assertEquals("electric", electricLoco.getType());
        assertEquals(120.0, electricLoco.getMaxSpeedKmH(), 0.001);
        assertEquals(1668.0, electricLoco.getGauge(), 0.001);
        assertTrue(electricLoco.isElectric());
        assertFalse(electricLoco.isDiesel());
    }

    @Test
    void testConstructor_ValidDiesel_CaseInsensitive() {
        assertEquals("L200", dieselLoco.getId());
        assertEquals("diesel", dieselLoco.getType(), "Type should be stored in lowercase");
        assertEquals(80.5, dieselLoco.getMaxSpeedKmH(), 0.001);
        assertEquals(1435.0, dieselLoco.getGauge(), 0.001);
        assertTrue(dieselLoco.isDiesel());
        assertFalse(dieselLoco.isElectric());
    }

    @Test
    void testConstructor_Trimming() {
        assertEquals("L500", trimmedLoco.getId(), "ID should be trimmed");
        assertEquals("electric", trimmedLoco.getType(), "Type should be trimmed and lowercased");
    }

    // --- Invalid Constructor Argument Tests ---

    @Test
    void testConstructor_NullId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Locomotive(null, "Electric", 100.0, 1668.0));
    }

    @Test
    void testConstructor_EmptyType_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Locomotive("LID", "", 100.0, 1668.0));
    }

    @Test
    void testConstructor_ZeroSpeed_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Locomotive("LID", "Diesel", 0.0, 1668.0));
    }

    @Test
    void testConstructor_NegativeGauge_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new Locomotive("LID", "Electric", 100.0, -10.0));
    }

    // --- isElectric() and isDiesel() Tests ---

    @Test
    void testIsElectricAndIsDiesel_OtherType() {
        Locomotive otherLoco = new Locomotive("L99", "Steam", 50.0, 1668.0);
        assertFalse(otherLoco.isElectric());
        assertFalse(otherLoco.isDiesel());
    }

    // --- isGaugeCompatible() Test ---

    @Test
    void testIsGaugeCompatible_ExactMatch() {
        assertTrue(electricLoco.isGaugeCompatible(1668.0));
    }

    @Test
    void testIsGaugeCompatible_WithinTolerance() {
        // 1mm less
        assertTrue(electricLoco.isGaugeCompatible(1667.0));
        // 1mm more
        assertTrue(electricLoco.isGaugeCompatible(1669.0));
        // Half mm difference
        assertTrue(electricLoco.isGaugeCompatible(1668.5));
    }

    @Test
    void testIsGaugeCompatible_OutsideTolerance() {
        // 1.1mm less
        assertFalse(electricLoco.isGaugeCompatible(1666.9));
        // 1.1mm more
        assertFalse(electricLoco.isGaugeCompatible(1669.1));
        // Significantly different
        assertFalse(electricLoco.isGaugeCompatible(1435.0));
    }

    // --- equals() and hashCode() Tests ---

    @Test
    void testEqualsAndHashCode_SameIdDifferentDetails() {
        Locomotive locoCopy = new Locomotive("L4000", "Diesel", 50.0, 1000.0);

        assertTrue(electricLoco.equals(locoCopy), "Locomotives should be equal if IDs match");
        assertEquals(electricLoco.hashCode(), locoCopy.hashCode(), "Hash codes must be equal for equal objects");
    }

    @Test
    void testEquals_DifferentId() {
        assertFalse(electricLoco.equals(dieselLoco));
    }

    @Test
    void testEquals_NullAndDifferentClass() {
        assertFalse(electricLoco.equals(null));
        assertFalse(electricLoco.equals(new Object()));
    }

    // --- toString() Test ---

    @Test
    void testToString() {
        String expected = "L4000 (electric, 120 km/h, 1668mm gauge)";
        assertEquals(expected, electricLoco.toString());

        // Check formatting for speed with decimals
        String expectedDiesel = "L200 (diesel, 81 km/h, 1435mm gauge)"; // 80.5 rounds to 81 with %.0f
        Locomotive dieselRound = new Locomotive("L200", "DIESEL", 80.5, 1435.0);
        assertEquals(expectedDiesel, dieselRound.toString());
    }
}