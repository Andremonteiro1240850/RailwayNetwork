package Domain;

import Domain.ENUM.PathStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe PickPath
 * Testa construtor, getters, toString e comportamento com diferentes estratégias
 */
@DisplayName("PickPath Tests")
class PickPathTest {

    private List<PickStop> sampleStops;
    private PickPath pickPath;

    @BeforeEach
    void setUp() {
        sampleStops = Arrays.asList(
                new PickStop(1, 1, 1, 5),
                new PickStop(2, 2, 2, 3),
                new PickStop(3, 3, 3, 7)
        );
        pickPath = new PickPath(PathStrategy.ASC_SWEEP, sampleStops, 15);
    }

    // ========== TESTES DO CONSTRUTOR ==========

    @Test
    @DisplayName("Constructor should create PickPath with correct values")
    void constructor_shouldCreatePickPathWithCorrectValues() {
        // Arrange
        List<PickStop> stops = Arrays.asList(
                new PickStop(1, 1, 1, 5),
                new PickStop(2, 2, 2, 3)
        );

        // Act
        PickPath path = new PickPath(PathStrategy.NEAREST_NEIGHBOUR, stops, 20);

        // Assert
        assertEquals(PathStrategy.NEAREST_NEIGHBOUR, path.getStrategy());
        assertEquals(20, path.getTotalDistance());
        assertEquals(2, path.getStops().size());
        assertTrue(path.getStops().containsAll(stops));
    }

    @Test
    @DisplayName("Constructor should create defensive copy of stops list")
    void constructor_shouldCreateDefensiveCopyOfStopsList() {
        // Arrange
        List<PickStop> originalStops = new ArrayList<>(sampleStops);

        // Act
        PickPath path = new PickPath(PathStrategy.ASC_SWEEP, originalStops, 10);
        originalStops.add(new PickStop(4, 4, 4, 1)); // Modify original list

        // Assert
        assertEquals(3, path.getStops().size()); // Should not be affected by original list modification
        assertFalse(path.getStops().contains(new PickStop(4, 4, 4, 1)));
    }

    @Test
    @DisplayName("Constructor should handle empty stops list")
    void constructor_shouldHandleEmptyStopsList() {
        // Arrange
        List<PickStop> emptyStops = new ArrayList<>();

        // Act
        PickPath path = new PickPath(PathStrategy.ASC_SWEEP, emptyStops, 0);

        // Assert
        assertEquals(PathStrategy.ASC_SWEEP, path.getStrategy());
        assertEquals(0, path.getTotalDistance());
        assertTrue(path.getStops().isEmpty());
    }

    @Test
    @DisplayName("Constructor should handle null strategy")
    void constructor_shouldHandleNullStrategy() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            new PickPath(null, sampleStops, 10);
        });
    }

    @Test
    @DisplayName("Constructor should handle null stops list")
    void constructor_shouldHandleNullStopsList() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            new PickPath(PathStrategy.ASC_SWEEP, null, 10);
        });
    }

    @Test
    @DisplayName("Constructor should handle negative total distance")
    void constructor_shouldHandleNegativeTotalDistance() {
        // Act
        PickPath path = new PickPath(PathStrategy.ASC_SWEEP, sampleStops, -5);

        // Assert
        assertEquals(-5, path.getTotalDistance());
    }

    @Test
    @DisplayName("Constructor should handle zero total distance")
    void constructor_shouldHandleZeroTotalDistance() {
        // Act
        PickPath path = new PickPath(PathStrategy.ASC_SWEEP, sampleStops, 0);

        // Assert
        assertEquals(0, path.getTotalDistance());
    }

    @Test
    @DisplayName("Constructor should handle large total distance")
    void constructor_shouldHandleLargeTotalDistance() {
        // Act
        PickPath path = new PickPath(PathStrategy.ASC_SWEEP, sampleStops, Integer.MAX_VALUE);

        // Assert
        assertEquals(Integer.MAX_VALUE, path.getTotalDistance());
    }

    // ========== TESTES DOS GETTERS ==========

    @Test
    @DisplayName("getStrategy should return correct strategy")
    void getStrategy_shouldReturnCorrectStrategy() {
        // Act & Assert
        assertEquals(PathStrategy.ASC_SWEEP, pickPath.getStrategy());
    }

    @Test
    @DisplayName("getTotalDistance should return correct total distance")
    void getTotalDistance_shouldReturnCorrectTotalDistance() {
        // Act & Assert
        assertEquals(15, pickPath.getTotalDistance());
    }

    @Test
    @DisplayName("getStops should return defensive copy of stops")
    void getStops_shouldReturnDefensiveCopyOfStops() {
        // Act
        List<PickStop> returnedStops = pickPath.getStops();
        returnedStops.add(new PickStop(4, 4, 4, 1)); // Modify returned list

        // Assert
        assertEquals(3, pickPath.getStops().size()); // Original should not be affected
        assertFalse(pickPath.getStops().contains(new PickStop(4, 4, 4, 1)));
    }

    @Test
    @DisplayName("getStops should return same content as original")
    void getStops_shouldReturnSameContentAsOriginal() {
        // Act
        List<PickStop> returnedStops = pickPath.getStops();

        // Assert
        assertEquals(sampleStops.size(), returnedStops.size());
        assertTrue(returnedStops.containsAll(sampleStops));
        assertTrue(sampleStops.containsAll(returnedStops));
    }

    @Test
    @DisplayName("getStops should return new list instance each time")
    void getStops_shouldReturnNewListInstanceEachTime() {
        // Act
        List<PickStop> stops1 = pickPath.getStops();
        List<PickStop> stops2 = pickPath.getStops();

        // Assert
        assertNotSame(stops1, stops2);
        assertEquals(stops1, stops2);
    }

    // ========== TESTES COM DIFERENTES ESTRATÉGIAS ==========

    @ParameterizedTest
    @EnumSource(PathStrategy.class)
    @DisplayName("Constructor should work with all strategy types")
    void constructor_shouldWorkWithAllStrategyTypes(PathStrategy strategy) {
        // Act
        PickPath path = new PickPath(strategy, sampleStops, 10);

        // Assert
        assertEquals(strategy, path.getStrategy());
        assertEquals(10, path.getTotalDistance());
        assertEquals(sampleStops.size(), path.getStops().size());
    }

    @Test
    @DisplayName("PickPath with ASC_SWEEP strategy should work correctly")
    void pickPathWithAscSweepStrategy_shouldWorkCorrectly() {
        // Arrange
        List<PickStop> stops = Arrays.asList(
                new PickStop(1, 3, 1, 5),
                new PickStop(2, 1, 3, 3),
                new PickStop(3, 2, 2, 7)
        );

        // Act
        PickPath path = new PickPath(PathStrategy.ASC_SWEEP, stops, 25);

        // Assert
        assertEquals(PathStrategy.ASC_SWEEP, path.getStrategy());
        assertEquals(25, path.getTotalDistance());
        assertEquals(3, path.getStops().size());
    }

    @Test
    @DisplayName("PickPath with NEAREST_NEIGHBOUR strategy should work correctly")
    void pickPathWithNearestNeighbourStrategy_shouldWorkCorrectly() {
        // Arrange
        List<PickStop> stops = Arrays.asList(
                new PickStop(1, 1, 1, 5),
                new PickStop(2, 2, 2, 3),
                new PickStop(3, 3, 3, 7)
        );

        // Act
        PickPath path = new PickPath(PathStrategy.NEAREST_NEIGHBOUR, stops, 30);

        // Assert
        assertEquals(PathStrategy.NEAREST_NEIGHBOUR, path.getStrategy());
        assertEquals(30, path.getTotalDistance());
        assertEquals(3, path.getStops().size());
    }

    // ========== TESTES DO TOSTRING ==========

    @Test
    @DisplayName("toString should return correct format")
    void toString_shouldReturnCorrectFormat() {
        // Act
        String result = pickPath.toString();

        // Assert
        assertTrue(result.contains("PickPath{"));
        assertTrue(result.contains("strategy=ASC_SWEEP"));
        assertTrue(result.contains("totalDistance=15"));
        assertTrue(result.contains("stops="));
    }

    @Test
    @DisplayName("toString should include all stops information")
    void toString_shouldIncludeAllStopsInformation() {
        // Act
        String result = pickPath.toString();

        // Assert
        // Check that all stops are represented in the string
        for (PickStop stop : sampleStops) {
            assertTrue(result.contains(stop.toString()));
        }
    }

    @Test
    @DisplayName("toString should handle empty stops list")
    void toString_shouldHandleEmptyStopsList() {
        // Arrange
        PickPath emptyPath = new PickPath(PathStrategy.ASC_SWEEP, new ArrayList<>(), 0);

        // Act
        String result = emptyPath.toString();

        // Assert
        assertTrue(result.contains("PickPath{"));
        assertTrue(result.contains("strategy=ASC_SWEEP"));
        assertTrue(result.contains("totalDistance=0"));
        assertTrue(result.contains("stops=[]"));
    }

    @Test
    @DisplayName("toString should handle single stop")
    void toString_shouldHandleSingleStop() {
        // Arrange
        List<PickStop> singleStop = Arrays.asList(new PickStop(1, 1, 1, 5));
        PickPath singlePath = new PickPath(PathStrategy.NEAREST_NEIGHBOUR, singleStop, 10);

        // Act
        String result = singlePath.toString();

        // Assert
        assertTrue(result.contains("PickPath{"));
        assertTrue(result.contains("strategy=NEAREST_NEIGHBOUR"));
        assertTrue(result.contains("totalDistance=10"));
        assertTrue(result.contains("(1,1) Qty:5"));
    }

    // ========== TESTES DE COMPORTAMENTO COM MÚLTIPLAS INSTÂNCIAS ==========

    @Test
    @DisplayName("Multiple PickPath instances should be independent")
    void multiplePickPathInstances_shouldBeIndependent() {
        // Arrange
        List<PickStop> stops1 = Arrays.asList(new PickStop(1, 1, 1, 5));
        List<PickStop> stops2 = Arrays.asList(new PickStop(2, 2, 2, 3));
        PickPath path1 = new PickPath(PathStrategy.ASC_SWEEP, stops1, 10);
        PickPath path2 = new PickPath(PathStrategy.NEAREST_NEIGHBOUR, stops2, 20);

        // Act
        List<PickStop> returnedStops1 = path1.getStops();
        List<PickStop> returnedStops2 = path2.getStops();

        // Assert
        assertNotSame(returnedStops1, returnedStops2);
        assertNotEquals(returnedStops1, returnedStops2);
        assertEquals(1, returnedStops1.size());
        assertEquals(1, returnedStops2.size());
    }

    @Test
    @DisplayName("PickPath should maintain immutability of stops")
    void pickPath_shouldMaintainImmutabilityOfStops() {
        // Arrange
        List<PickStop> originalStops = new ArrayList<>(sampleStops);
        PickPath path = new PickPath(PathStrategy.ASC_SWEEP, originalStops, 15);

        // Act
        List<PickStop> returnedStops = path.getStops();
        returnedStops.clear(); // Try to modify the returned list

        // Assert
        assertEquals(3, path.getStops().size()); // Original should not be affected
        assertTrue(path.getStops().containsAll(sampleStops));
    }

    // ========== TESTES DE CASOS LIMITE ==========

    @Test
    @DisplayName("PickPath should handle very large number of stops")
    void pickPath_shouldHandleVeryLargeNumberOfStops() {
        // Arrange
        List<PickStop> manyStops = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            manyStops.add(new PickStop(i, i % 10, i % 20, i % 5 + 1));
        }

        // Act
        PickPath path = new PickPath(PathStrategy.ASC_SWEEP, manyStops, 50000);

        // Assert
        assertEquals(1000, path.getStops().size());
        assertEquals(50000, path.getTotalDistance());
        assertEquals(PathStrategy.ASC_SWEEP, path.getStrategy());
    }

    @Test
    @DisplayName("PickPath should handle stops with extreme values")
    void pickPath_shouldHandleStopsWithExtremeValues() {
        // Arrange
        List<PickStop> extremeStops = Arrays.asList(
                new PickStop(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE),
                new PickStop(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE),
                new PickStop(0, 0, 0, 0)
        );

        // Act
        PickPath path = new PickPath(PathStrategy.NEAREST_NEIGHBOUR, extremeStops, Integer.MAX_VALUE);

        // Assert
        assertEquals(3, path.getStops().size());
        assertEquals(Integer.MAX_VALUE, path.getTotalDistance());
        assertTrue(path.getStops().containsAll(extremeStops));
    }
}
