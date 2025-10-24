package Domain.ENUM;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para o enum PathStrategy
 * Testa valores do enum, métodos herdados e comportamento
 */
@DisplayName("PathStrategy Enum Tests")
class PathStrategyTest {

    // ========== TESTES DOS VALORES DO ENUM ==========

    @Test
    @DisplayName("PathStrategy should have exactly two values")
    void pathStrategy_shouldHaveExactlyTwoValues() {
        // Act
        PathStrategy[] values = PathStrategy.values();

        // Assert
        assertEquals(2, values.length);
    }

    @Test
    @DisplayName("PathStrategy should contain ASC_SWEEP value")
    void pathStrategy_shouldContainAscSweepValue() {
        // Act
        PathStrategy[] values = PathStrategy.values();

        // Assert
        assertTrue(java.util.Arrays.asList(values).contains(PathStrategy.ASC_SWEEP));
    }

    @Test
    @DisplayName("PathStrategy should contain NEAREST_NEIGHBOUR value")
    void pathStrategy_shouldContainNearestNeighbourValue() {
        // Act
        PathStrategy[] values = PathStrategy.values();

        // Assert
        assertTrue(java.util.Arrays.asList(values).contains(PathStrategy.NEAREST_NEIGHBOUR));
    }

    @Test
    @DisplayName("ASC_SWEEP should be the first value")
    void ascSweep_shouldBeTheFirstValue() {
        // Act
        PathStrategy[] values = PathStrategy.values();

        // Assert
        assertEquals(PathStrategy.ASC_SWEEP, values[0]);
    }

    @Test
    @DisplayName("NEAREST_NEIGHBOUR should be the second value")
    void nearestNeighbour_shouldBeTheSecondValue() {
        // Act
        PathStrategy[] values = PathStrategy.values();

        // Assert
        assertEquals(PathStrategy.NEAREST_NEIGHBOUR, values[1]);
    }

    // ========== TESTES DO MÉTODO VALUEOF ==========

    @Test
    @DisplayName("valueOf should return ASC_SWEEP for valid string")
    void valueOf_shouldReturnAscSweepForValidString() {
        // Act
        PathStrategy result = PathStrategy.valueOf("ASC_SWEEP");

        // Assert
        assertEquals(PathStrategy.ASC_SWEEP, result);
    }

    @Test
    @DisplayName("valueOf should return NEAREST_NEIGHBOUR for valid string")
    void valueOf_shouldReturnNearestNeighbourForValidString() {
        // Act
        PathStrategy result = PathStrategy.valueOf("NEAREST_NEIGHBOUR");

        // Assert
        assertEquals(PathStrategy.NEAREST_NEIGHBOUR, result);
    }

    @Test
    @DisplayName("valueOf should throw IllegalArgumentException for invalid string")
    void valueOf_shouldThrowIllegalArgumentExceptionForInvalidString() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            PathStrategy.valueOf("INVALID_STRATEGY");
        });
    }

    @Test
    @DisplayName("valueOf should throw IllegalArgumentException for null string")
    void valueOf_shouldThrowIllegalArgumentExceptionForNullString() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            PathStrategy.valueOf(null);
        });
    }

    @Test
    @DisplayName("valueOf should throw IllegalArgumentException for empty string")
    void valueOf_shouldThrowIllegalArgumentExceptionForEmptyString() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            PathStrategy.valueOf("");
        });
    }

    @Test
    @DisplayName("valueOf should be case sensitive")
    void valueOf_shouldBeCaseSensitive() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            PathStrategy.valueOf("asc_sweep");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            PathStrategy.valueOf("nearest_neighbour");
        });
    }

    // ========== TESTES DO MÉTODO ORDINAL ==========

    @Test
    @DisplayName("ASC_SWEEP should have ordinal 0")
    void ascSweep_shouldHaveOrdinal0() {
        // Act & Assert
        assertEquals(0, PathStrategy.ASC_SWEEP.ordinal());
    }

    @Test
    @DisplayName("NEAREST_NEIGHBOUR should have ordinal 1")
    void nearestNeighbour_shouldHaveOrdinal1() {
        // Act & Assert
        assertEquals(1, PathStrategy.NEAREST_NEIGHBOUR.ordinal());
    }

    @Test
    @DisplayName("Ordinals should be unique")
    void ordinals_shouldBeUnique() {
        // Act
        int ascSweepOrdinal = PathStrategy.ASC_SWEEP.ordinal();
        int nearestNeighbourOrdinal = PathStrategy.NEAREST_NEIGHBOUR.ordinal();

        // Assert
        assertNotEquals(ascSweepOrdinal, nearestNeighbourOrdinal);
    }

    // ========== TESTES DO MÉTODO NAME ==========

    @Test
    @DisplayName("ASC_SWEEP should have correct name")
    void ascSweep_shouldHaveCorrectName() {
        // Act & Assert
        assertEquals("ASC_SWEEP", PathStrategy.ASC_SWEEP.name());
    }

    @Test
    @DisplayName("NEAREST_NEIGHBOUR should have correct name")
    void nearestNeighbour_shouldHaveCorrectName() {
        // Act & Assert
        assertEquals("NEAREST_NEIGHBOUR", PathStrategy.NEAREST_NEIGHBOUR.name());
    }

    @Test
    @DisplayName("Names should be unique")
    void names_shouldBeUnique() {
        // Act
        String ascSweepName = PathStrategy.ASC_SWEEP.name();
        String nearestNeighbourName = PathStrategy.NEAREST_NEIGHBOUR.name();

        // Assert
        assertNotEquals(ascSweepName, nearestNeighbourName);
    }

    // ========== TESTES DO MÉTODO TOSTRING ==========

    @Test
    @DisplayName("toString should return correct string for ASC_SWEEP")
    void toString_shouldReturnCorrectStringForAscSweep() {
        // Act
        String result = PathStrategy.ASC_SWEEP.toString();

        // Assert
        assertEquals("ASC_SWEEP", result);
    }

    @Test
    @DisplayName("toString should return correct string for NEAREST_NEIGHBOUR")
    void toString_shouldReturnCorrectStringForNearestNeighbour() {
        // Act
        String result = PathStrategy.NEAREST_NEIGHBOUR.toString();

        // Assert
        assertEquals("NEAREST_NEIGHBOUR", result);
    }

    @Test
    @DisplayName("toString should be same as name")
    void toString_shouldBeSameAsName() {
        // Act & Assert
        assertEquals(PathStrategy.ASC_SWEEP.name(), PathStrategy.ASC_SWEEP.toString());
        assertEquals(PathStrategy.NEAREST_NEIGHBOUR.name(), PathStrategy.NEAREST_NEIGHBOUR.toString());
    }

    // ========== TESTES DO MÉTODO COMPARETO ==========

    @Test
    @DisplayName("ASC_SWEEP should compare less than NEAREST_NEIGHBOUR")
    void ascSweep_shouldCompareLessThanNearestNeighbour() {
        // Act
        int result = PathStrategy.ASC_SWEEP.compareTo(PathStrategy.NEAREST_NEIGHBOUR);

        // Assert
        assertTrue(result < 0);
    }

    @Test
    @DisplayName("NEAREST_NEIGHBOUR should compare greater than ASC_SWEEP")
    void nearestNeighbour_shouldCompareGreaterThanAscSweep() {
        // Act
        int result = PathStrategy.NEAREST_NEIGHBOUR.compareTo(PathStrategy.ASC_SWEEP);

        // Assert
        assertTrue(result > 0);
    }

    @Test
    @DisplayName("Same enum values should compare equal")
    void sameEnumValues_shouldCompareEqual() {
        // Act
        int result = PathStrategy.ASC_SWEEP.compareTo(PathStrategy.ASC_SWEEP);

        // Assert
        assertEquals(0, result);
    }

    @Test
    @DisplayName("CompareTo should be consistent with equals")
    void compareTo_shouldBeConsistentWithEquals() {
        // Act & Assert
        assertTrue(PathStrategy.ASC_SWEEP.compareTo(PathStrategy.ASC_SWEEP) == 0);
        assertTrue(PathStrategy.ASC_SWEEP.equals(PathStrategy.ASC_SWEEP));

        assertTrue(PathStrategy.ASC_SWEEP.compareTo(PathStrategy.NEAREST_NEIGHBOUR) != 0);
        assertFalse(PathStrategy.ASC_SWEEP.equals(PathStrategy.NEAREST_NEIGHBOUR));
    }

    // ========== TESTES DO MÉTODO EQUALS ==========

    @Test
    @DisplayName("Same enum values should be equal")
    void sameEnumValues_shouldBeEqual() {
        // Act & Assert
        assertEquals(PathStrategy.ASC_SWEEP, PathStrategy.ASC_SWEEP);
        assertEquals(PathStrategy.NEAREST_NEIGHBOUR, PathStrategy.NEAREST_NEIGHBOUR);
    }

    @Test
    @DisplayName("Different enum values should not be equal")
    void differentEnumValues_shouldNotBeEqual() {
        // Act & Assert
        assertNotEquals(PathStrategy.ASC_SWEEP, PathStrategy.NEAREST_NEIGHBOUR);
        assertNotEquals(PathStrategy.NEAREST_NEIGHBOUR, PathStrategy.ASC_SWEEP);
    }

    @Test
    @DisplayName("Enum should not be equal to null")
    void enum_shouldNotBeEqualToString() {
        // Act & Assert
        assertNotEquals(PathStrategy.ASC_SWEEP, null);
        assertNotEquals(PathStrategy.NEAREST_NEIGHBOUR, null);
    }

    @Test
    @DisplayName("Enum should not be equal to different type")
    void enum_shouldNotBeEqualToDifferentType() {
        // Act & Assert
        assertNotEquals(PathStrategy.ASC_SWEEP, "ASC_SWEEP");
        assertNotEquals(PathStrategy.NEAREST_NEIGHBOUR, 1);
    }

    // ========== TESTES DO MÉTODO HASHCODE ==========

    @Test
    @DisplayName("Same enum values should have same hashCode")
    void sameEnumValues_shouldHaveSameHashCode() {
        // Act
        int hashCode1 = PathStrategy.ASC_SWEEP.hashCode();
        int hashCode2 = PathStrategy.ASC_SWEEP.hashCode();

        // Assert
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    @DisplayName("Different enum values should have different hashCode")
    void differentEnumValues_shouldHaveDifferentHashCode() {
        // Act
        int ascSweepHashCode = PathStrategy.ASC_SWEEP.hashCode();
        int nearestNeighbourHashCode = PathStrategy.NEAREST_NEIGHBOUR.hashCode();

        // Assert
        assertNotEquals(ascSweepHashCode, nearestNeighbourHashCode);
    }

    @Test
    @DisplayName("HashCode should be consistent")
    void hashCode_shouldBeConsistent() {
        // Act
        int hashCode1 = PathStrategy.ASC_SWEEP.hashCode();
        int hashCode2 = PathStrategy.ASC_SWEEP.hashCode();

        // Assert
        assertEquals(hashCode1, hashCode2);
    }

    // ========== TESTES COM PARAMETRIZAÇÃO ==========

    @ParameterizedTest
    @EnumSource(PathStrategy.class)
    @DisplayName("All enum values should have valid name")
    void allEnumValues_shouldHaveValidName(PathStrategy strategy) {
        // Act
        String name = strategy.name();

        // Assert
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    @ParameterizedTest
    @EnumSource(PathStrategy.class)
    @DisplayName("All enum values should have valid toString")
    void allEnumValues_shouldHaveValidToString(PathStrategy strategy) {
        // Act
        String toString = strategy.toString();

        // Assert
        assertNotNull(toString);
        assertFalse(toString.isEmpty());
        assertEquals(strategy.name(), toString);
    }

    @ParameterizedTest
    @EnumSource(PathStrategy.class)
    @DisplayName("All enum values should have valid ordinal")
    void allEnumValues_shouldHaveValidOrdinal(PathStrategy strategy) {
        // Act
        int ordinal = strategy.ordinal();

        // Assert
        assertTrue(ordinal >= 0);
        assertTrue(ordinal < PathStrategy.values().length);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ASC_SWEEP", "NEAREST_NEIGHBOUR"})
    @DisplayName("Valid string values should be convertible to enum")
    void validStringValues_shouldBeConvertibleToEnum(String value) {
        // Act
        PathStrategy strategy = PathStrategy.valueOf(value);

        // Assert
        assertNotNull(strategy);
        assertEquals(value, strategy.name());
    }

    // ========== TESTES DE COMPORTAMENTO EM COLEÇÕES ==========

    @Test
    @DisplayName("Enum values should work correctly in sets")
    void enumValues_shouldWorkCorrectlyInSets() {
        // Arrange
        java.util.Set<PathStrategy> strategySet = new java.util.HashSet<>();

        // Act
        strategySet.add(PathStrategy.ASC_SWEEP);
        strategySet.add(PathStrategy.NEAREST_NEIGHBOUR);
        strategySet.add(PathStrategy.ASC_SWEEP); // Duplicate

        // Assert
        assertEquals(2, strategySet.size());
        assertTrue(strategySet.contains(PathStrategy.ASC_SWEEP));
        assertTrue(strategySet.contains(PathStrategy.NEAREST_NEIGHBOUR));
    }

    @Test
    @DisplayName("Enum values should work correctly in maps")
    void enumValues_shouldWorkCorrectlyInMaps() {
        // Arrange
        java.util.Map<PathStrategy, String> strategyMap = new java.util.HashMap<>();

        // Act
        strategyMap.put(PathStrategy.ASC_SWEEP, "Ascending Sweep Strategy");
        strategyMap.put(PathStrategy.NEAREST_NEIGHBOUR, "Nearest Neighbour Strategy");

        // Assert
        assertEquals(2, strategyMap.size());
        assertEquals("Ascending Sweep Strategy", strategyMap.get(PathStrategy.ASC_SWEEP));
        assertEquals("Nearest Neighbour Strategy", strategyMap.get(PathStrategy.NEAREST_NEIGHBOUR));
    }

    @Test
    @DisplayName("Enum values should work correctly in lists")
    void enumValues_shouldWorkCorrectlyInLists() {
        // Arrange
        java.util.List<PathStrategy> strategyList = new java.util.ArrayList<>();

        // Act
        strategyList.add(PathStrategy.ASC_SWEEP);
        strategyList.add(PathStrategy.NEAREST_NEIGHBOUR);

        // Assert
        assertEquals(2, strategyList.size());
        assertEquals(PathStrategy.ASC_SWEEP, strategyList.get(0));
        assertEquals(PathStrategy.NEAREST_NEIGHBOUR, strategyList.get(1));
    }

    // ========== TESTES DE REFLEXÃO ==========

    @Test
    @DisplayName("Enum should be instance of PathStrategy")
    void enum_shouldBeInstanceOfPathStrategy() {
        // Act & Assert
        assertTrue(PathStrategy.ASC_SWEEP instanceof PathStrategy);
        assertTrue(PathStrategy.NEAREST_NEIGHBOUR instanceof PathStrategy);
    }

    @Test
    @DisplayName("Enum should have correct class")
    void enum_shouldHaveCorrectClass() {
        // Act & Assert
        assertEquals(PathStrategy.class, PathStrategy.ASC_SWEEP.getClass());
        assertEquals(PathStrategy.class, PathStrategy.NEAREST_NEIGHBOUR.getClass());
    }
}
