package Domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe PickStop
 * Testa construtores, getters/setters, equals/hashCode, toString e método de consolidação
 */
@DisplayName("PickStop Tests")
class PickStopTest {

    private PickStop pickStop;

    @BeforeEach
    void setUp() {
        pickStop = new PickStop(1, 2, 3, 10);
    }

    // ========== TESTES DO CONSTRUTOR ==========

    @Test
    @DisplayName("Constructor should create PickStop with correct values")
    void constructor_shouldCreatePickStopWithCorrectValues() {
        // Arrange & Act
        PickStop stop = new PickStop(1, 2, 3, 10);

        // Assert
        assertEquals(1, stop.getSeq());
        assertEquals(2, stop.getAisle());
        assertEquals(3, stop.getBay());
        assertEquals(10, stop.getTotalQty());
    }

    @Test
    @DisplayName("Constructor should handle zero values correctly")
    void constructor_shouldHandleZeroValuesCorrectly() {
        // Arrange & Act
        PickStop stop = new PickStop(0, 0, 0, 0);

        // Assert
        assertEquals(0, stop.getSeq());
        assertEquals(0, stop.getAisle());
        assertEquals(0, stop.getBay());
        assertEquals(0, stop.getTotalQty());
    }

    @Test
    @DisplayName("Constructor should handle negative values correctly")
    void constructor_shouldHandleNegativeValuesCorrectly() {
        // Arrange & Act
        PickStop stop = new PickStop(-1, -2, -3, -10);

        // Assert
        assertEquals(-1, stop.getSeq());
        assertEquals(-2, stop.getAisle());
        assertEquals(-3, stop.getBay());
        assertEquals(-10, stop.getTotalQty());
    }

    // ========== TESTES DOS GETTERS ==========

    @Test
    @DisplayName("getSeq should return correct sequence number")
    void getSeq_shouldReturnCorrectSequenceNumber() {
        // Act & Assert
        assertEquals(1, pickStop.getSeq());
    }

    @Test
    @DisplayName("getAisle should return correct aisle number")
    void getAisle_shouldReturnCorrectAisleNumber() {
        // Act & Assert
        assertEquals(2, pickStop.getAisle());
    }

    @Test
    @DisplayName("getBay should return correct bay number")
    void getBay_shouldReturnCorrectBayNumber() {
        // Act & Assert
        assertEquals(3, pickStop.getBay());
    }

    @Test
    @DisplayName("getTotalQty should return correct total quantity")
    void getTotalQty_shouldReturnCorrectTotalQuantity() {
        // Act & Assert
        assertEquals(10, pickStop.getTotalQty());
    }

    // ========== TESTES DOS SETTERS ==========

    @Test
    @DisplayName("setSeq should update sequence number")
    void setSeq_shouldUpdateSequenceNumber() {
        // Act
        pickStop.setSeq(5);

        // Assert
        assertEquals(5, pickStop.getSeq());
    }

    @Test
    @DisplayName("setAisle should update aisle number")
    void setAisle_shouldUpdateAisleNumber() {
        // Act
        pickStop.setAisle(7);

        // Assert
        assertEquals(7, pickStop.getAisle());
    }

    @Test
    @DisplayName("setBay should update bay number")
    void setBay_shouldUpdateBayNumber() {
        // Act
        pickStop.setBay(9);

        // Assert
        assertEquals(9, pickStop.getBay());
    }

    @Test
    @DisplayName("setTotalQty should update total quantity")
    void setTotalQty_shouldUpdateTotalQuantity() {
        // Act
        pickStop.setTotalQty(15);

        // Assert
        assertEquals(15, pickStop.getTotalQty());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 100, -5, Integer.MAX_VALUE, Integer.MIN_VALUE})
    @DisplayName("setSeq should handle various integer values")
    void setSeq_shouldHandleVariousIntegerValues(int value) {
        // Act
        pickStop.setSeq(value);

        // Assert
        assertEquals(value, pickStop.getSeq());
    }

    // ========== TESTES DO EQUALS ==========

    @Test
    @DisplayName("equals should return true for same object")
    void equals_shouldReturnTrueForSameObject() {
        // Act & Assert
        assertTrue(pickStop.equals(pickStop));
    }

    @Test
    @DisplayName("equals should return true for objects with same aisle and bay")
    void equals_shouldReturnTrueForObjectsWithSameAisleAndBay() {
        // Arrange
        PickStop otherStop = new PickStop(5, 2, 3, 20); // Different seq and qty, same aisle and bay

        // Act & Assert
        assertTrue(pickStop.equals(otherStop));
    }

    @Test
    @DisplayName("equals should return false for objects with different aisle")
    void equals_shouldReturnFalseForObjectsWithDifferentAisle() {
        // Arrange
        PickStop otherStop = new PickStop(1, 5, 3, 10); // Different aisle

        // Act & Assert
        assertFalse(pickStop.equals(otherStop));
    }

    @Test
    @DisplayName("equals should return false for objects with different bay")
    void equals_shouldReturnFalseForObjectsWithDifferentBay() {
        // Arrange
        PickStop otherStop = new PickStop(1, 2, 5, 10); // Different bay

        // Act & Assert
        assertFalse(pickStop.equals(otherStop));
    }

    @Test
    @DisplayName("equals should return false for null object")
    void equals_shouldReturnFalseForNullObject() {
        // Act & Assert
        assertFalse(pickStop.equals(null));
    }

    @Test
    @DisplayName("equals should return false for different class")
    void equals_shouldReturnFalseForDifferentClass() {
        // Arrange
        String otherObject = "not a PickStop";

        // Act & Assert
        assertFalse(pickStop.equals(otherObject));
    }

    @Test
    @DisplayName("equals should be symmetric")
    void equals_shouldBeSymmetric() {
        // Arrange
        PickStop otherStop = new PickStop(5, 2, 3, 20);

        // Act & Assert
        assertEquals(pickStop.equals(otherStop), otherStop.equals(pickStop));
    }

    @Test
    @DisplayName("equals should be transitive")
    void equals_shouldBeTransitive() {
        // Arrange
        PickStop stop1 = new PickStop(1, 2, 3, 10);
        PickStop stop2 = new PickStop(5, 2, 3, 20);
        PickStop stop3 = new PickStop(10, 2, 3, 30);

        // Act & Assert
        assertTrue(stop1.equals(stop2));
        assertTrue(stop2.equals(stop3));
        assertTrue(stop1.equals(stop3));
    }

    // ========== TESTES DO HASHCODE ==========

    @Test
    @DisplayName("hashCode should be consistent")
    void hashCode_shouldBeConsistent() {
        // Act
        int hashCode1 = pickStop.hashCode();
        int hashCode2 = pickStop.hashCode();

        // Assert
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    @DisplayName("hashCode should be equal for objects with same aisle and bay")
    void hashCode_shouldBeEqualForObjectsWithSameAisleAndBay() {
        // Arrange
        PickStop otherStop = new PickStop(5, 2, 3, 20);

        // Act & Assert
        assertEquals(pickStop.hashCode(), otherStop.hashCode());
    }

    @Test
    @DisplayName("hashCode should be different for objects with different aisle or bay")
    void hashCode_shouldBeDifferentForObjectsWithDifferentAisleOrBay() {
        // Arrange
        PickStop differentAisle = new PickStop(1, 5, 3, 10);
        PickStop differentBay = new PickStop(1, 2, 5, 10);

        // Act & Assert
        assertNotEquals(pickStop.hashCode(), differentAisle.hashCode());
        assertNotEquals(pickStop.hashCode(), differentBay.hashCode());
    }

    // ========== TESTES DO TOSTRING ==========

    @Test
    @DisplayName("toString should return correct format")
    void toString_shouldReturnCorrectFormat() {
        // Act
        String result = pickStop.toString();

        // Assert
        assertEquals("(2,3) Qty:10", result);
    }

    @Test
    @DisplayName("toString should handle zero values correctly")
    void toString_shouldHandleZeroValuesCorrectly() {
        // Arrange
        PickStop zeroStop = new PickStop(0, 0, 0, 0);

        // Act
        String result = zeroStop.toString();

        // Assert
        assertEquals("(0,0) Qty:0", result);
    }

    @Test
    @DisplayName("toString should handle negative values correctly")
    void toString_shouldHandleNegativeValuesCorrectly() {
        // Arrange
        PickStop negativeStop = new PickStop(1, -2, -3, -10);

        // Act
        String result = negativeStop.toString();

        // Assert
        assertEquals("(-2,-3) Qty:-10", result);
    }

    // ========== TESTES DO MÉTODO CONSOLIDATE STOPS ==========

    @Test
    @DisplayName("consolidateStops should return empty list for null input")
    void consolidateStops_shouldReturnEmptyListForNullInput() {
        // Act
        List<PickStop> result = PickStop.consolidateStops(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("consolidateStops should return empty list for empty input")
    void consolidateStops_shouldReturnEmptyListForEmptyInput() {
        // Arrange
        List<PickStop> emptyList = new ArrayList<>();

        // Act
        List<PickStop> result = PickStop.consolidateStops(emptyList);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("consolidateStops should return same list for no duplicates")
    void consolidateStops_shouldReturnSameListForNoDuplicates() {
        // Arrange
        List<PickStop> originalStops = Arrays.asList(
                new PickStop(1, 1, 1, 5),
                new PickStop(2, 1, 2, 3),
                new PickStop(3, 2, 1, 7)
        );

        // Act
        List<PickStop> result = PickStop.consolidateStops(originalStops);

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.containsAll(originalStops));
    }

    @Test
    @DisplayName("consolidateStops should merge stops with same aisle and bay")
    void consolidateStops_shouldMergeStopsWithSameAisleAndBay() {
        // Arrange
        List<PickStop> originalStops = Arrays.asList(
                new PickStop(1, 1, 1, 5),
                new PickStop(2, 1, 1, 3), // Same aisle and bay as first
                new PickStop(3, 2, 1, 7)
        );

        // Act
        List<PickStop> result = PickStop.consolidateStops(originalStops);

        // Assert
        assertEquals(2, result.size());
        
        // Find the consolidated stop
        PickStop consolidatedStop = result.stream()
                .filter(stop -> stop.getAisle() == 1 && stop.getBay() == 1)
                .findFirst()
                .orElse(null);
        
        assertNotNull(consolidatedStop);
        assertEquals(8, consolidatedStop.getTotalQty()); // 5 + 3
    }

    @Test
    @DisplayName("consolidateStops should handle multiple duplicates correctly")
    void consolidateStops_shouldHandleMultipleDuplicatesCorrectly() {
        // Arrange
        List<PickStop> originalStops = Arrays.asList(
                new PickStop(1, 1, 1, 5),
                new PickStop(2, 1, 1, 3), // First duplicate
                new PickStop(3, 1, 1, 2), // Second duplicate
                new PickStop(4, 2, 2, 7),
                new PickStop(5, 2, 2, 1)  // Another duplicate
        );

        // Act
        List<PickStop> result = PickStop.consolidateStops(originalStops);

        // Assert
        assertEquals(2, result.size());
        
        // Check first consolidated stop (1,1)
        PickStop stop1 = result.stream()
                .filter(stop -> stop.getAisle() == 1 && stop.getBay() == 1)
                .findFirst()
                .orElse(null);
        assertNotNull(stop1);
        assertEquals(10, stop1.getTotalQty()); // 5 + 3 + 2
        
        // Check second consolidated stop (2,2)
        PickStop stop2 = result.stream()
                .filter(stop -> stop.getAisle() == 2 && stop.getBay() == 2)
                .findFirst()
                .orElse(null);
        assertNotNull(stop2);
        assertEquals(8, stop2.getTotalQty()); // 7 + 1
    }

    @Test
    @DisplayName("consolidateStops should preserve original stops order for first occurrence")
    void consolidateStops_shouldPreserveOriginalStopsOrderForFirstOccurrence() {
        // Arrange
        List<PickStop> originalStops = Arrays.asList(
                new PickStop(1, 1, 1, 5),
                new PickStop(2, 1, 1, 3), // Duplicate
                new PickStop(3, 2, 2, 7)
        );

        // Act
        List<PickStop> result = PickStop.consolidateStops(originalStops);

        // Assert
        assertEquals(2, result.size());
        
        // The first occurrence should be preserved
        PickStop firstStop = result.stream()
                .filter(stop -> stop.getAisle() == 1 && stop.getBay() == 1)
                .findFirst()
                .orElse(null);
        assertNotNull(firstStop);
        assertEquals(1, firstStop.getSeq()); // Original sequence number
    }

    @Test
    @DisplayName("consolidateStops should handle zero quantities correctly")
    void consolidateStops_shouldHandleZeroQuantitiesCorrectly() {
        // Arrange
        List<PickStop> originalStops = Arrays.asList(
                new PickStop(1, 1, 1, 0),
                new PickStop(2, 1, 1, 5),
                new PickStop(3, 2, 2, 0)
        );

        // Act
        List<PickStop> result = PickStop.consolidateStops(originalStops);

        // Assert
        assertEquals(2, result.size());
        
        PickStop consolidatedStop = result.stream()
                .filter(stop -> stop.getAisle() == 1 && stop.getBay() == 1)
                .findFirst()
                .orElse(null);
        assertNotNull(consolidatedStop);
        assertEquals(5, consolidatedStop.getTotalQty()); // 0 + 5
    }

    @Test
    @DisplayName("consolidateStops should handle negative quantities correctly")
    void consolidateStops_shouldHandleNegativeQuantitiesCorrectly() {
        // Arrange
        List<PickStop> originalStops = Arrays.asList(
                new PickStop(1, 1, 1, 5),
                new PickStop(2, 1, 1, -3), // Negative quantity
                new PickStop(3, 2, 2, 7)
        );

        // Act
        List<PickStop> result = PickStop.consolidateStops(originalStops);

        // Assert
        assertEquals(2, result.size());
        
        PickStop consolidatedStop = result.stream()
                .filter(stop -> stop.getAisle() == 1 && stop.getBay() == 1)
                .findFirst()
                .orElse(null);
        assertNotNull(consolidatedStop);
        assertEquals(2, consolidatedStop.getTotalQty()); // 5 + (-3)
    }

    @Test
    @DisplayName("consolidateStops should not modify original list")
    void consolidateStops_shouldNotModifyOriginalList() {
        // Arrange
        List<PickStop> originalStops = Arrays.asList(
                new PickStop(1, 1, 1, 5),
                new PickStop(2, 1, 1, 3),
                new PickStop(3, 2, 2, 7)
        );
        List<PickStop> originalCopy = new ArrayList<>(originalStops);

        // Act
        PickStop.consolidateStops(originalStops);

        // Assert
        assertEquals(originalCopy, originalStops);
    }
}
