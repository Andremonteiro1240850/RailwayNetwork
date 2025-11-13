package Domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Station class.
 */
class FacilityTest {

    private Facility facilityWithLocation;
    private Facility facilityWithoutLocation;
    private Facility facilityWithTrimmedInput;

    @BeforeEach
    void setUp() {
        facilityWithLocation = new Facility("S1", "Porto São Bento", "Porto");
        facilityWithoutLocation = new Facility("S2", "Porto Campanhã");
        facilityWithTrimmedInput = new Facility(" S3 ", " Ermesinde ", " Valongo ");
    }

    // --- Constructor Tests ---

    @Test
    void testConstructorWithLocation_Valid() {
        assertEquals("S1", facilityWithLocation.getId());
        assertEquals("Porto São Bento", facilityWithLocation.getName());
        assertEquals("Porto", facilityWithLocation.getLocation());
        assertTrue(facilityWithLocation.hasLocation());
    }

    @Test
    void testConstructorWithoutLocation_Valid() {
        assertEquals("S2", facilityWithoutLocation.getId());
        assertEquals("Porto Campanhã", facilityWithoutLocation.getName());
        assertEquals("", facilityWithoutLocation.getLocation(), "Location should be empty string when not provided");
        assertFalse(facilityWithoutLocation.hasLocation());
    }

    @Test
    void testConstructorWithTrimming() {
        assertEquals("S3", facilityWithTrimmedInput.getId(), "ID should be trimmed");
        assertEquals("Ermesinde", facilityWithTrimmedInput.getName(), "Name should be trimmed");
        assertEquals("Valongo", facilityWithTrimmedInput.getLocation(), "Location should be trimmed");
        assertTrue(facilityWithTrimmedInput.hasLocation());
    }

    @Test
    void testConstructor_NullId_ThrowsException() {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> new Facility(null, "Test Name", "Test Location"),
                "Expected constructor to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("Station ID cannot be null or empty"));
    }

    @Test
    void testConstructor_EmptyId_ThrowsException() {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> new Facility("", "Test Name", "Test Location"),
                "Expected constructor to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("Station ID cannot be null or empty"));
    }

    @Test
    void testConstructor_BlankId_ThrowsException() {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> new Facility("   ", "Test Name", "Test Location"),
                "Expected constructor to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("Station ID cannot be null or empty"));
    }


    @Test
    void testConstructor_NullName_ThrowsException() {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> new Facility("TestId", null, "Test Location"),
                "Expected constructor to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("Station name cannot be null or empty"));
    }

    @Test
    void testConstructor_EmptyName_ThrowsException() {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> new Facility("TestId", "", "Test Location"),
                "Expected constructor to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("Station name cannot be null or empty"));
    }

    @Test
    void testConstructor_BlankName_ThrowsException() {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> new Facility("TestId", "  ", "Test Location"),
                "Expected constructor to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("Station name cannot be null or empty"));
    }

    // --- Getter Tests (Partially covered by constructor tests) ---

    @Test
    void testGetId() {
        assertEquals("S1", facilityWithLocation.getId());
    }

    @Test
    void testGetName() {
        assertEquals("Porto São Bento", facilityWithLocation.getName());
    }

    @Test
    void testGetLocation() {
        assertEquals("Porto", facilityWithLocation.getLocation());
        assertEquals("", facilityWithoutLocation.getLocation());
    }

    // --- hasLocation Test ---

    @Test
    void testHasLocation() {
        assertTrue(facilityWithLocation.hasLocation());
        assertFalse(facilityWithoutLocation.hasLocation());
    }

    // --- equals() and hashCode() Tests ---

    @Test
    void testEquals_SameObject() {
        assertTrue(facilityWithLocation.equals(facilityWithLocation), "Station should be equal to itself");
    }

    @Test
    void testEquals_EqualObjects() {
        Facility facility1Copy = new Facility("S1", "Another Name", "Another Location");
        assertTrue(facilityWithLocation.equals(facility1Copy), "Stations with the same ID should be equal");
    }

    @Test
    void testEquals_DifferentObjects() {
        assertFalse(facilityWithLocation.equals(facilityWithoutLocation), "Stations with different IDs should not be equal");
    }

    @Test
    void testEquals_Null() {
        assertFalse(facilityWithLocation.equals(null), "Station should not be equal to null");
    }

    @Test
    void testEquals_DifferentClass() {
        Object other = new Object();
        assertFalse(facilityWithLocation.equals(other), "Station should not be equal to an object of a different class");
    }

    @Test
    void testHashCode_EqualObjects() {
        Facility facility1Copy = new Facility("S1", "Different Name");
        assertEquals(facilityWithLocation.hashCode(), facility1Copy.hashCode(), "Hash codes should be equal for equal objects");
    }

    @Test
    void testHashCode_DifferentObjects() {
        // While not strictly required by the contract, different objects ideally have different hash codes
        // However, the contract only mandates equal objects MUST have equal hash codes.
        // We can assert they are different if we are confident in the implementation.
        assertNotEquals(facilityWithLocation.hashCode(), facilityWithoutLocation.hashCode(), "Hash codes should ideally be different for different objects");
    }

    // --- toString() Test ---

    @Test
    void testToString_WithLocation() {
        String expected = "Porto São Bento (S1) - Porto";
        assertEquals(expected, facilityWithLocation.toString());
    }

    @Test
    void testToString_WithoutLocation() {
        String expected = "Porto Campanhã (S2)";
        assertEquals(expected, facilityWithoutLocation.toString());
    }
}