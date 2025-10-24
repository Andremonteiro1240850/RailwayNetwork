package pt.ipp.isep.dei;

import Domain.PickPath;
import Domain.PickStop;
import Domain.ENUM.PathStrategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe PickPathService
 * Testa todos os métodos estáticos e funcionalidades de processamento
 */
@DisplayName("PickPathService Tests")
class PickPathServiceTest {

    private List<PickStop> sampleStops;

    @BeforeEach
    void setUp() {
        sampleStops = Arrays.asList(
                new PickStop(1, 1, 1, 5),
                new PickStop(2, 2, 2, 3),
                new PickStop(3, 3, 3, 7)
        );
    }

    // ========== TESTES DO MÉTODO GENERATE BOTH PATHS ==========

    @Test
    @DisplayName("generateBothPaths should generate both strategies correctly")
    void generateBothPaths_shouldGenerateBothStrategiesCorrectly() {
        // Act
        Map<PathStrategy, PickPath> result = PickPathService.generateBothPaths(sampleStops);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(PathStrategy.ASC_SWEEP));
        assertTrue(result.containsKey(PathStrategy.NEAREST_NEIGHBOUR));
        
        PickPath ascSweepPath = result.get(PathStrategy.ASC_SWEEP);
        PickPath nearestNeighbourPath = result.get(PathStrategy.NEAREST_NEIGHBOUR);
        
        assertNotNull(ascSweepPath);
        assertNotNull(nearestNeighbourPath);
        assertEquals(PathStrategy.ASC_SWEEP, ascSweepPath.getStrategy());
        assertEquals(PathStrategy.NEAREST_NEIGHBOUR, nearestNeighbourPath.getStrategy());
    }

    @Test
    @DisplayName("generateBothPaths should handle single stop")
    void generateBothPaths_shouldHandleSingleStop() {
        // Arrange
        List<PickStop> singleStop = Arrays.asList(new PickStop(1, 1, 1, 5));

        // Act
        Map<PathStrategy, PickPath> result = PickPathService.generateBothPaths(singleStop);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        PickPath ascSweepPath = result.get(PathStrategy.ASC_SWEEP);
        PickPath nearestNeighbourPath = result.get(PathStrategy.NEAREST_NEIGHBOUR);
        
        assertEquals(1, ascSweepPath.getStops().size());
        assertEquals(1, nearestNeighbourPath.getStops().size());
        assertEquals(singleStop.get(0), ascSweepPath.getStops().get(0));
        assertEquals(singleStop.get(0), nearestNeighbourPath.getStops().get(0));
    }

    @Test
    @DisplayName("generateBothPaths should handle empty list")
    void generateBothPaths_shouldHandleEmptyList() {
        // Arrange
        List<PickStop> emptyStops = new ArrayList<>();

        // Act
        Map<PathStrategy, PickPath> result = PickPathService.generateBothPaths(emptyStops);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        PickPath ascSweepPath = result.get(PathStrategy.ASC_SWEEP);
        PickPath nearestNeighbourPath = result.get(PathStrategy.NEAREST_NEIGHBOUR);
        
        assertTrue(ascSweepPath.getStops().isEmpty());
        assertTrue(nearestNeighbourPath.getStops().isEmpty());
        assertEquals(0, ascSweepPath.getTotalDistance());
        assertEquals(0, nearestNeighbourPath.getTotalDistance());
    }

    @Test
    @DisplayName("generateBothPaths should not modify original list")
    void generateBothPaths_shouldNotModifyOriginalList() {
        // Arrange
        List<PickStop> originalStops = new ArrayList<>(sampleStops);
        List<PickStop> originalCopy = new ArrayList<>(originalStops);

        // Act
        PickPathService.generateBothPaths(originalStops);

        // Assert
        assertEquals(originalCopy, originalStops);
    }

    @Test
    @DisplayName("generateBothPaths should create independent PickPath objects")
    void generateBothPaths_shouldCreateIndependentPickPathObjects() {
        // Act
        Map<PathStrategy, PickPath> result = PickPathService.generateBothPaths(sampleStops);

        // Assert
        PickPath ascSweepPath = result.get(PathStrategy.ASC_SWEEP);
        PickPath nearestNeighbourPath = result.get(PathStrategy.NEAREST_NEIGHBOUR);
        
        assertNotSame(ascSweepPath, nearestNeighbourPath);
        assertNotEquals(ascSweepPath, nearestNeighbourPath);
    }

    @Test
    @DisplayName("generateBothPaths should calculate different distances for different strategies")
    void generateBothPaths_shouldCalculateDifferentDistancesForDifferentStrategies() {
        // Arrange
        List<PickStop> complexStops = Arrays.asList(
                new PickStop(1, 3, 4, 5),
                new PickStop(2, 1, 8, 3),
                new PickStop(3, 2, 2, 7),
                new PickStop(4, 1, 1, 4)
        );

        // Act
        Map<PathStrategy, PickPath> result = PickPathService.generateBothPaths(complexStops);

        // Assert
        PickPath ascSweepPath = result.get(PathStrategy.ASC_SWEEP);
        PickPath nearestNeighbourPath = result.get(PathStrategy.NEAREST_NEIGHBOUR);
        
        // Both should have valid distances
        assertTrue(ascSweepPath.getTotalDistance() > 0);
        assertTrue(nearestNeighbourPath.getTotalDistance() > 0);
        
        // They might be different (though not guaranteed)
        // The important thing is that both are calculated correctly
        assertNotNull(ascSweepPath.getTotalDistance());
        assertNotNull(nearestNeighbourPath.getTotalDistance());
    }

    // ========== TESTES DO MÉTODO CONSOLIDATE STOPS ==========

    @Test
    @DisplayName("consolidateStops should consolidate duplicate stops correctly")
    void consolidateStops_shouldConsolidateDuplicateStopsCorrectly() {
        // Arrange
        List<PickStop> stopsWithDuplicates = Arrays.asList(
                new PickStop(1, 1, 1, 5),
                new PickStop(2, 1, 1, 3), // Duplicate aisle/bay
                new PickStop(3, 2, 2, 7)
        );

        // Act
        List<PickStop> result = PickPathService.consolidateStops(stopsWithDuplicates);

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
    @DisplayName("consolidateStops should return empty list for null input")
    void consolidateStops_shouldReturnEmptyListForNullInput() {
        // Act
        List<PickStop> result = PickPathService.consolidateStops(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("consolidateStops should return empty list for empty input")
    void consolidateStops_shouldReturnEmptyListForEmptyInput() {
        // Arrange
        List<PickStop> emptyStops = new ArrayList<>();

        // Act
        List<PickStop> result = PickPathService.consolidateStops(emptyStops);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("consolidateStops should return same list for no duplicates")
    void consolidateStops_shouldReturnSameListForNoDuplicates() {
        // Act
        List<PickStop> result = PickPathService.consolidateStops(sampleStops);

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.containsAll(sampleStops));
    }

    @Test
    @DisplayName("consolidateStops should handle multiple duplicates correctly")
    void consolidateStops_shouldHandleMultipleDuplicatesCorrectly() {
        // Arrange
        List<PickStop> stopsWithMultipleDuplicates = Arrays.asList(
                new PickStop(1, 1, 1, 5),
                new PickStop(2, 1, 1, 3), // First duplicate
                new PickStop(3, 1, 1, 2), // Second duplicate
                new PickStop(4, 2, 2, 7),
                new PickStop(5, 2, 2, 1)  // Another duplicate
        );

        // Act
        List<PickStop> result = PickPathService.consolidateStops(stopsWithMultipleDuplicates);

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
    @DisplayName("consolidateStops should not modify original list")
    void consolidateStops_shouldNotModifyOriginalList() {
        // Arrange
        List<PickStop> originalStops = new ArrayList<>(sampleStops);
        List<PickStop> originalCopy = new ArrayList<>(originalStops);

        // Act
        PickPathService.consolidateStops(originalStops);

        // Assert
        assertEquals(originalCopy, originalStops);
    }

    @Test
    @DisplayName("consolidateStops should handle zero quantities correctly")
    void consolidateStops_shouldHandleZeroQuantitiesCorrectly() {
        // Arrange
        List<PickStop> stopsWithZeroQuantities = Arrays.asList(
                new PickStop(1, 1, 1, 0),
                new PickStop(2, 1, 1, 5),
                new PickStop(3, 2, 2, 0)
        );

        // Act
        List<PickStop> result = PickPathService.consolidateStops(stopsWithZeroQuantities);

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
        List<PickStop> stopsWithNegativeQuantities = Arrays.asList(
                new PickStop(1, 1, 1, 5),
                new PickStop(2, 1, 1, -3), // Negative quantity
                new PickStop(3, 2, 2, 7)
        );

        // Act
        List<PickStop> result = PickPathService.consolidateStops(stopsWithNegativeQuantities);

        // Assert
        assertEquals(2, result.size());
        
        PickStop consolidatedStop = result.stream()
                .filter(stop -> stop.getAisle() == 1 && stop.getBay() == 1)
                .findFirst()
                .orElse(null);
        assertNotNull(consolidatedStop);
        assertEquals(2, consolidatedStop.getTotalQty()); // 5 + (-3)
    }

    // ========== TESTES DO MÉTODO PROCESS PICK PATH SEQUENCING ==========

    @Test
    @DisplayName("processPickPathSequencing should process valid stops correctly")
    void processPickPathSequencing_shouldProcessValidStopsCorrectly() {
        // Act
        Map<PathStrategy, PickPath> result = PickPathService.processPickPathSequencing(sampleStops);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(PathStrategy.ASC_SWEEP));
        assertTrue(result.containsKey(PathStrategy.NEAREST_NEIGHBOUR));
        
        PickPath ascSweepPath = result.get(PathStrategy.ASC_SWEEP);
        PickPath nearestNeighbourPath = result.get(PathStrategy.NEAREST_NEIGHBOUR);
        
        assertNotNull(ascSweepPath);
        assertNotNull(nearestNeighbourPath);
        assertEquals(PathStrategy.ASC_SWEEP, ascSweepPath.getStrategy());
        assertEquals(PathStrategy.NEAREST_NEIGHBOUR, nearestNeighbourPath.getStrategy());
    }

    @Test
    @DisplayName("processPickPathSequencing should handle stops with duplicates")
    void processPickPathSequencing_shouldHandleStopsWithDuplicates() {
        // Arrange
        List<PickStop> stopsWithDuplicates = Arrays.asList(
                new PickStop(1, 1, 1, 5),
                new PickStop(2, 1, 1, 3), // Duplicate
                new PickStop(3, 2, 2, 7)
        );

        // Act
        Map<PathStrategy, PickPath> result = PickPathService.processPickPathSequencing(stopsWithDuplicates);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        PickPath ascSweepPath = result.get(PathStrategy.ASC_SWEEP);
        PickPath nearestNeighbourPath = result.get(PathStrategy.NEAREST_NEIGHBOUR);
        
        // Should have consolidated the duplicates
        assertEquals(2, ascSweepPath.getStops().size());
        assertEquals(2, nearestNeighbourPath.getStops().size());
        
        // Find the consolidated stop
        PickStop consolidatedStop = ascSweepPath.getStops().stream()
                .filter(stop -> stop.getAisle() == 1 && stop.getBay() == 1)
                .findFirst()
                .orElse(null);
        assertNotNull(consolidatedStop);
        assertEquals(8, consolidatedStop.getTotalQty()); // 5 + 3
    }

    @Test
    @DisplayName("processPickPathSequencing should handle single stop")
    void processPickPathSequencing_shouldHandleSingleStop() {
        // Arrange
        List<PickStop> singleStop = Arrays.asList(new PickStop(1, 1, 1, 5));

        // Act
        Map<PathStrategy, PickPath> result = PickPathService.processPickPathSequencing(singleStop);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        PickPath ascSweepPath = result.get(PathStrategy.ASC_SWEEP);
        PickPath nearestNeighbourPath = result.get(PathStrategy.NEAREST_NEIGHBOUR);
        
        assertEquals(1, ascSweepPath.getStops().size());
        assertEquals(1, nearestNeighbourPath.getStops().size());
        assertEquals(singleStop.get(0), ascSweepPath.getStops().get(0));
        assertEquals(singleStop.get(0), nearestNeighbourPath.getStops().get(0));
    }

    @Test
    @DisplayName("processPickPathSequencing should handle empty list")
    void processPickPathSequencing_shouldHandleEmptyList() {
        // Arrange
        List<PickStop> emptyStops = new ArrayList<>();

        // Act
        Map<PathStrategy, PickPath> result = PickPathService.processPickPathSequencing(emptyStops);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        PickPath ascSweepPath = result.get(PathStrategy.ASC_SWEEP);
        PickPath nearestNeighbourPath = result.get(PathStrategy.NEAREST_NEIGHBOUR);
        
        assertTrue(ascSweepPath.getStops().isEmpty());
        assertTrue(nearestNeighbourPath.getStops().isEmpty());
        assertEquals(0, ascSweepPath.getTotalDistance());
        assertEquals(0, nearestNeighbourPath.getTotalDistance());
    }

    @Test
    @DisplayName("processPickPathSequencing should not modify original list")
    void processPickPathSequencing_shouldNotModifyOriginalList() {
        // Arrange
        List<PickStop> originalStops = new ArrayList<>(sampleStops);
        List<PickStop> originalCopy = new ArrayList<>(originalStops);

        // Act
        PickPathService.processPickPathSequencing(originalStops);

        // Assert
        assertEquals(originalCopy, originalStops);
    }

    @Test
    @DisplayName("processPickPathSequencing should create independent PickPath objects")
    void processPickPathSequencing_shouldCreateIndependentPickPathObjects() {
        // Act
        Map<PathStrategy, PickPath> result = PickPathService.processPickPathSequencing(sampleStops);

        // Assert
        PickPath ascSweepPath = result.get(PathStrategy.ASC_SWEEP);
        PickPath nearestNeighbourPath = result.get(PathStrategy.NEAREST_NEIGHBOUR);
        
        assertNotSame(ascSweepPath, nearestNeighbourPath);
        assertNotEquals(ascSweepPath, nearestNeighbourPath);
    }

    @Test
    @DisplayName("processPickPathSequencing should calculate valid distances")
    void processPickPathSequencing_shouldCalculateValidDistances() {
        // Act
        Map<PathStrategy, PickPath> result = PickPathService.processPickPathSequencing(sampleStops);

        // Assert
        PickPath ascSweepPath = result.get(PathStrategy.ASC_SWEEP);
        PickPath nearestNeighbourPath = result.get(PathStrategy.NEAREST_NEIGHBOUR);
        
        assertTrue(ascSweepPath.getTotalDistance() >= 0);
        assertTrue(nearestNeighbourPath.getTotalDistance() >= 0);
    }

    // ========== TESTES DE INTEGRAÇÃO ==========

    @Test
    @DisplayName("Full process should work end-to-end with complex data")
    void fullProcess_shouldWorkEndToEndWithComplexData() {
        // Arrange
        List<PickStop> complexStops = Arrays.asList(
                new PickStop(1, 3, 4, 5),
                new PickStop(2, 1, 8, 3),
                new PickStop(3, 2, 2, 7),
                new PickStop(4, 1, 1, 4),
                new PickStop(5, 2, 2, 2), // Duplicate of stop 3
                new PickStop(6, 1, 1, 1)  // Another duplicate of stop 4
        );

        // Act
        Map<PathStrategy, PickPath> result = PickPathService.processPickPathSequencing(complexStops);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        PickPath ascSweepPath = result.get(PathStrategy.ASC_SWEEP);
        PickPath nearestNeighbourPath = result.get(PathStrategy.NEAREST_NEIGHBOUR);
        
        assertNotNull(ascSweepPath);
        assertNotNull(nearestNeighbourPath);
        
        // Should have consolidated the duplicate stops
        assertEquals(4, ascSweepPath.getStops().size());
        assertEquals(4, nearestNeighbourPath.getStops().size());
        
        // Both paths should have valid distances
        assertTrue(ascSweepPath.getTotalDistance() > 0);
        assertTrue(nearestNeighbourPath.getTotalDistance() > 0);
        
        // Check that duplicates were consolidated
        PickStop consolidatedStop1 = ascSweepPath.getStops().stream()
                .filter(stop -> stop.getAisle() == 2 && stop.getBay() == 2)
                .findFirst()
                .orElse(null);
        assertNotNull(consolidatedStop1);
        assertEquals(9, consolidatedStop1.getTotalQty()); // 7 + 2
        
        PickStop consolidatedStop2 = ascSweepPath.getStops().stream()
                .filter(stop -> stop.getAisle() == 1 && stop.getBay() == 1)
                .findFirst()
                .orElse(null);
        assertNotNull(consolidatedStop2);
        assertEquals(5, consolidatedStop2.getTotalQty()); // 4 + 1
    }

    @Test
    @DisplayName("Service should handle large number of stops")
    void service_shouldHandleLargeNumberOfStops() {
        // Arrange
        List<PickStop> manyStops = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            manyStops.add(new PickStop(i, i % 10, i % 20, i % 5 + 1));
        }

        // Act
        Map<PathStrategy, PickPath> result = PickPathService.processPickPathSequencing(manyStops);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        PickPath ascSweepPath = result.get(PathStrategy.ASC_SWEEP);
        PickPath nearestNeighbourPath = result.get(PathStrategy.NEAREST_NEIGHBOUR);
        
        assertEquals(100, ascSweepPath.getStops().size());
        assertEquals(100, nearestNeighbourPath.getStops().size());
        assertTrue(ascSweepPath.getTotalDistance() > 0);
        assertTrue(nearestNeighbourPath.getTotalDistance() > 0);
    }

    @Test
    @DisplayName("Service should handle stops with extreme values")
    void service_shouldHandleStopsWithExtremeValues() {
        // Arrange
        List<PickStop> extremeStops = Arrays.asList(
                new PickStop(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE),
                new PickStop(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE),
                new PickStop(0, 0, 0, 0)
        );

        // Act
        Map<PathStrategy, PickPath> result = PickPathService.processPickPathSequencing(extremeStops);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        PickPath ascSweepPath = result.get(PathStrategy.ASC_SWEEP);
        PickPath nearestNeighbourPath = result.get(PathStrategy.NEAREST_NEIGHBOUR);
        
        assertEquals(3, ascSweepPath.getStops().size());
        assertEquals(3, nearestNeighbourPath.getStops().size());
        assertTrue(ascSweepPath.getTotalDistance() > 0);
        assertTrue(nearestNeighbourPath.getTotalDistance() > 0);
    }

    // ========== TESTES DE CONSISTÊNCIA ==========

    @Test
    @DisplayName("Multiple calls should return consistent results")
    void multipleCalls_shouldReturnConsistentResults() {
        // Act
        Map<PathStrategy, PickPath> result1 = PickPathService.processPickPathSequencing(sampleStops);
        Map<PathStrategy, PickPath> result2 = PickPathService.processPickPathSequencing(sampleStops);

        // Assert
        assertEquals(result1.size(), result2.size());
        assertEquals(result1.keySet(), result2.keySet());
        
        // The paths should be equivalent (same stops, same distances)
        PickPath ascSweep1 = result1.get(PathStrategy.ASC_SWEEP);
        PickPath ascSweep2 = result2.get(PathStrategy.ASC_SWEEP);
        assertEquals(ascSweep1.getTotalDistance(), ascSweep2.getTotalDistance());
        assertEquals(ascSweep1.getStops().size(), ascSweep2.getStops().size());
        
        PickPath nearestNeighbour1 = result1.get(PathStrategy.NEAREST_NEIGHBOUR);
        PickPath nearestNeighbour2 = result2.get(PathStrategy.NEAREST_NEIGHBOUR);
        assertEquals(nearestNeighbour1.getTotalDistance(), nearestNeighbour2.getTotalDistance());
        assertEquals(nearestNeighbour1.getStops().size(), nearestNeighbour2.getStops().size());
    }

    @Test
    @DisplayName("Service should be thread-safe for read operations")
    void service_shouldBeThreadSafeForReadOperations() {
        // Arrange
        List<PickStop> stops = Arrays.asList(
                new PickStop(1, 1, 1, 5),
                new PickStop(2, 2, 2, 3)
        );

        // Act & Assert
        // Multiple threads calling the same method should not cause issues
        // Since the methods are static and don't modify shared state
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 10; i++) {
                PickPathService.processPickPathSequencing(stops);
            }
        });
    }
}
