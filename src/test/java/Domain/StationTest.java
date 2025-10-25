package Domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Station class.
 */
class StationTest {

    private Station stationWithLocation;
    private Station stationWithoutLocation;
    private Station stationWithTrimmedInput;

    @BeforeEach
    void setUp() {
        stationWithLocation = new Station("S1", "Porto São Bento", "Porto");
        stationWithoutLocation = new Station("S2", "Porto Campanhã");
        stationWithTrimmedInput = new Station(" S3 ", " Ermesinde ", " Valongo ");
    }

    // --- Constructor Tests ---

    @Test
    void testConstructorWithLocation_Valid() {
        assertEquals("S1", stationWithLocation.getId());
        assertEquals("Porto São Bento", stationWithLocation.getName());
        assertEquals("Porto", stationWithLocation.getLocation());
        assertTrue(stationWithLocation.hasLocation());
    }

    @Test
    void testConstructorWithoutLocation_Valid() {
        assertEquals("S2", stationWithoutLocation.getId());
        assertEquals("Porto Campanhã", stationWithoutLocation.getName());
        assertEquals("", stationWithoutLocation.getLocation(), "Location should be empty string when not provided");
        assertFalse(stationWithoutLocation.hasLocation());
    }

    @Test
    void testConstructorWithTrimming() {
        assertEquals("S3", stationWithTrimmedInput.getId(), "ID should be trimmed");
        assertEquals("Ermesinde", stationWithTrimmedInput.getName(), "Name should be trimmed");
        assertEquals("Valongo", stationWithTrimmedInput.getLocation(), "Location should be trimmed");
        assertTrue(stationWithTrimmedInput.hasLocation());
    }

    @Test
    void testConstructor_NullId_ThrowsException() {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> new Station(null, "Test Name", "Test Location"),
                "Expected constructor to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("Station ID cannot be null or empty"));
    }

    @Test
    void testConstructor_EmptyId_ThrowsException() {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> new Station("", "Test Name", "Test Location"),
                "Expected constructor to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("Station ID cannot be null or empty"));
    }

    @Test
    void testConstructor_BlankId_ThrowsException() {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> new Station("   ", "Test Name", "Test Location"),
                "Expected constructor to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("Station ID cannot be null or empty"));
    }


    @Test
    void testConstructor_NullName_ThrowsException() {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> new Station("TestId", null, "Test Location"),
                "Expected constructor to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("Station name cannot be null or empty"));
    }

    @Test
    void testConstructor_EmptyName_ThrowsException() {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> new Station("TestId", "", "Test Location"),
                "Expected constructor to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("Station name cannot be null or empty"));
    }

    @Test
    void testConstructor_BlankName_ThrowsException() {
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> new Station("TestId", "  ", "Test Location"),
                "Expected constructor to throw, but it didn't"
        );
        assertTrue(thrown.getMessage().contains("Station name cannot be null or empty"));
    }

    // --- Getter Tests (Partially covered by constructor tests) ---

    @Test
    void testGetId() {
        assertEquals("S1", stationWithLocation.getId());
    }

    @Test
    void testGetName() {
        assertEquals("Porto São Bento", stationWithLocation.getName());
    }

    @Test
    void testGetLocation() {
        assertEquals("Porto", stationWithLocation.getLocation());
        assertEquals("", stationWithoutLocation.getLocation());
    }

    // --- hasLocation Test ---

    @Test
    void testHasLocation() {
        assertTrue(stationWithLocation.hasLocation());
        assertFalse(stationWithoutLocation.hasLocation());
    }

    // --- equals() and hashCode() Tests ---

    @Test
    void testEquals_SameObject() {
        assertTrue(stationWithLocation.equals(stationWithLocation), "Station should be equal to itself");
    }

    @Test
    void testEquals_EqualObjects() {
        Station station1Copy = new Station("S1", "Another Name", "Another Location");
        assertTrue(stationWithLocation.equals(station1Copy), "Stations with the same ID should be equal");
    }

    @Test
    void testEquals_DifferentObjects() {
        assertFalse(stationWithLocation.equals(stationWithoutLocation), "Stations with different IDs should not be equal");
    }

    @Test
    void testEquals_Null() {
        assertFalse(stationWithLocation.equals(null), "Station should not be equal to null");
    }

    @Test
    void testEquals_DifferentClass() {
        Object other = new Object();
        assertFalse(stationWithLocation.equals(other), "Station should not be equal to an object of a different class");
    }

    @Test
    void testHashCode_EqualObjects() {
        Station station1Copy = new Station("S1", "Different Name");
        assertEquals(stationWithLocation.hashCode(), station1Copy.hashCode(), "Hash codes should be equal for equal objects");
    }

    @Test
    void testHashCode_DifferentObjects() {
        // While not strictly required by the contract, different objects ideally have different hash codes
        // However, the contract only mandates equal objects MUST have equal hash codes.
        // We can assert they are different if we are confident in the implementation.
        assertNotEquals(stationWithLocation.hashCode(), stationWithoutLocation.hashCode(), "Hash codes should ideally be different for different objects");
    }

    // --- toString() Test ---

    @Test
    void testToString_WithLocation() {
        String expected = "Porto São Bento (S1) - Porto";
        assertEquals(expected, stationWithLocation.toString());
    }

    @Test
    void testToString_WithoutLocation() {
        String expected = "Porto Campanhã (S2)";
        assertEquals(expected, stationWithoutLocation.toString());
    }
}