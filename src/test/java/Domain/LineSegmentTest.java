package Domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the LineSegment class.
 */
class LineSegmentTest {

    private Facility facilityA;
    private Facility facilityB;
    private Facility facilityC;
    private LineSegment segmentAB_named;
    private LineSegment segmentBC_unnamed; // Using the older constructor

    @BeforeEach
    void setUp() {
        facilityA = new Facility("SA", "Station A");
        facilityB = new Facility("SB", "Station B");
        facilityC = new Facility("SC", "Station C");

        segmentAB_named = new LineSegment("L-AB", "Main Line", facilityA, facilityB, 25.5, 1668, true);
        // Using the constructor without name for testing purposes
        segmentBC_unnamed = new LineSegment("L-BC", facilityB, facilityC, 15.0, 1435, false);
    }

    // --- Constructor Tests ---

    @Test
    void testConstructorWithName_Valid() {
        assertEquals("L-AB", segmentAB_named.getId());
        assertEquals("Main Line", segmentAB_named.getName());
        assertEquals(facilityA, segmentAB_named.getStartStation());
        assertEquals(facilityB, segmentAB_named.getEndStation());
        assertEquals(25.5, segmentAB_named.getLengthKm(), 0.001);
        assertEquals(1668, segmentAB_named.getGauge(), 0.001);
        assertTrue(segmentAB_named.isElectrified());
    }

    @Test
    void testConstructorWithoutName_Valid() {
        assertEquals("L-BC", segmentBC_unnamed.getId());
        assertEquals("", segmentBC_unnamed.getName(), "Name should be empty string when using the older constructor");
        assertEquals(facilityB, segmentBC_unnamed.getStartStation());
        assertEquals(facilityC, segmentBC_unnamed.getEndStation());
        assertEquals(15.0, segmentBC_unnamed.getLengthKm(), 0.001);
        assertEquals(1435, segmentBC_unnamed.getGauge(), 0.001);
        assertFalse(segmentBC_unnamed.isElectrified());
    }

    @Test
    void testConstructorWithTrimming() {
        LineSegment segmentTrim = new LineSegment(" L-XY ", " Side Line ", facilityA, facilityB, 10, 1668, false);
        assertEquals("L-XY", segmentTrim.getId());
        assertEquals("Side Line", segmentTrim.getName());
    }

    // --- Invalid Constructor Argument Tests ---
    @Test
    void testConstructor_NullId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new LineSegment(null, "Test Line", facilityA, facilityB, 10, 1668, true));
    }

    @Test
    void testConstructor_EmptyId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new LineSegment("", "Test Line", facilityA, facilityB, 10, 1668, true));
    }
    @Test
    void testConstructor_BlankId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new LineSegment("  ", "Test Line", facilityA, facilityB, 10, 1668, true));
    }


    @Test
    void testConstructor_NullStartStation_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new LineSegment("L-Test", "Test Line", null, facilityB, 10, 1668, true));
    }

    @Test
    void testConstructor_NullEndStation_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new LineSegment("L-Test", "Test Line", facilityA, null, 10, 1668, true));
    }

    @Test
    void testConstructor_SameStartEndStation_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new LineSegment("L-Test", "Test Line", facilityA, facilityA, 10, 1668, true));
    }

    @Test
    void testConstructor_ZeroLength_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new LineSegment("L-Test", "Test Line", facilityA, facilityB, 0, 1668, true));
    }

    @Test
    void testConstructor_NegativeLength_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new LineSegment("L-Test", "Test Line", facilityA, facilityB, -10.5, 1668, true));
    }

    @Test
    void testConstructor_ZeroGauge_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new LineSegment("L-Test", "Test Line", facilityA, facilityB, 10, 0, true));
    }


    @Test
    void testConstructor_NegativeGauge_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new LineSegment("L-Test", "Test Line", facilityA, facilityB, 10, -1668, true));
    }


    // --- Getter Tests ---
    // (Most getters implicitly tested by constructor tests)
    @Test
    void testGetters() {
        assertEquals("L-AB", segmentAB_named.getId());
        assertEquals("Main Line", segmentAB_named.getName());
        assertEquals(facilityA, segmentAB_named.getStartStation());
        assertEquals(facilityB, segmentAB_named.getEndStation());
        assertEquals(25.5, segmentAB_named.getLengthKm(), 0.001);
        assertEquals(1668, segmentAB_named.getGauge(), 0.001);
        assertTrue(segmentAB_named.isElectrified());
    }


    // --- connectsStations() Test ---

    @Test
    void testConnectsStations_CorrectOrder() {
        assertTrue(segmentAB_named.connectsStations(facilityA, facilityB));
    }

    @Test
    void testConnectsStations_ReverseOrder() {
        assertTrue(segmentAB_named.connectsStations(facilityB, facilityA));
    }

    @Test
    void testConnectsStations_OneWrongStation() {
        assertFalse(segmentAB_named.connectsStations(facilityA, facilityC));
        assertFalse(segmentAB_named.connectsStations(facilityC, facilityB));
    }

    @Test
    void testConnectsStations_BothWrongStations() {
        Facility facilityD = new Facility("SD", "Station D");
        assertFalse(segmentAB_named.connectsStations(facilityC, facilityD));
    }

    @Test
    void testConnectsStations_NullInput() {
        assertFalse(segmentAB_named.connectsStations(null, facilityB));
        assertFalse(segmentAB_named.connectsStations(facilityA, null));
        assertFalse(segmentAB_named.connectsStations(null, null));
    }

    // --- isGaugeCompatible() Test ---

    @Test
    void testIsGaugeCompatible_ExactMatch() {
        assertTrue(segmentAB_named.isGaugeCompatible(1668));
        assertTrue(segmentBC_unnamed.isGaugeCompatible(1435));
    }

    @Test
    void testIsGaugeCompatible_WithinTolerance() {
        assertTrue(segmentAB_named.isGaugeCompatible(1667.5));
        assertTrue(segmentAB_named.isGaugeCompatible(1668.9));
        // Check exactly 1mm difference
        assertTrue(segmentAB_named.isGaugeCompatible(1667.0));
        assertTrue(segmentAB_named.isGaugeCompatible(1669.0));
    }

    @Test
    void testIsGaugeCompatible_OutsideTolerance() {
        assertFalse(segmentAB_named.isGaugeCompatible(1666.9));
        assertFalse(segmentAB_named.isGaugeCompatible(1669.1));
        assertFalse(segmentAB_named.isGaugeCompatible(1435)); // Significantly different
    }

    // --- getOtherStation() Test ---

    @Test
    void testGetOtherStation_GivenStart() {
        assertEquals(facilityB, segmentAB_named.getOtherStation(facilityA));
    }

    @Test
    void testGetOtherStation_GivenEnd() {
        assertEquals(facilityA, segmentAB_named.getOtherStation(facilityB));
    }

    @Test
    void testGetOtherStation_GivenUnrelatedStation() {
        assertNull(segmentAB_named.getOtherStation(facilityC), "Should return null if station is not part of the segment");
    }

    @Test
    void testGetOtherStation_GivenNull() {
        assertNull(segmentAB_named.getOtherStation(null), "Should return null if input station is null");
    }

    // --- equals() and hashCode() Tests ---

    @Test
    void testEquals_SameObject() {
        assertTrue(segmentAB_named.equals(segmentAB_named));
    }

    @Test
    void testEquals_EqualObjects() {
        LineSegment segmentAB_copy = new LineSegment("L-AB", "Different Name", facilityA, facilityB, 50, 1000, false);
        assertTrue(segmentAB_named.equals(segmentAB_copy), "Segments with same ID should be equal");
    }

    @Test
    void testEquals_DifferentObjects() {
        assertFalse(segmentAB_named.equals(segmentBC_unnamed), "Segments with different IDs should not be equal");
    }

    @Test
    void testEquals_Null() {
        assertFalse(segmentAB_named.equals(null));
    }

    @Test
    void testEquals_DifferentClass() {
        assertFalse(segmentAB_named.equals(facilityA));
    }


    @Test
    void testHashCode_EqualObjects() {
        LineSegment segmentAB_copy = new LineSegment("L-AB", "Another Line", facilityA, facilityB, 100, 1000, true);
        assertEquals(segmentAB_named.hashCode(), segmentAB_copy.hashCode(), "Hash codes should be equal for equal objects");
    }

    // --- toString() Test ---

    @Test
    void testToString_NamedSegment() {
        String expected = "L-AB (Main Line): Station A -> Station B (25.50 km, 1668mm gauge, electrified)";
        assertEquals(expected, segmentAB_named.toString());
    }

    @Test
    void testToString_UnnamedSegment() {
        String expected = "L-BC: Station B -> Station C (15.00 km, 1435mm gauge, non-electrified)";
        assertEquals(expected, segmentBC_unnamed.toString());
    }
}