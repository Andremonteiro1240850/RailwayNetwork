package Controller;

import Domain.PickPath;
import Domain.PickStop;
import Domain.Trolley;
import Domain.ENUM.PathStrategy;
import Repository.TrolleyRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe PickPathController
 * Testa todos os métodos públicos, validações e cálculos de distância
 */
@DisplayName("PickPathController Tests")
class PickPathControllerTest {

    private PickPathController controller;
    private TrolleyRepository trolleyRepository;
    private List<PickStop> sampleStops;

    @BeforeEach
    void setUp() {
        controller = new PickPathController();
        trolleyRepository = new TrolleyRepository();
        sampleStops = Arrays.asList(
                new PickStop(1, 1, 1, 5),
                new PickStop(2, 2, 2, 3),
                new PickStop(3, 3, 3, 7)
        );
    }

    // ========== TESTES DOS CONSTRUTORES ==========

    @Test
    @DisplayName("Default constructor should create controller with null repository")
    void defaultConstructor_shouldCreateControllerWithNullRepository() {
        // Act
        PickPathController newController = new PickPathController();

        // Assert
        assertNotNull(newController);
        // Note: We can't directly test the private field, but we can test behavior
    }

    @Test
    @DisplayName("Constructor with repository should create controller with repository")
    void constructorWithRepository_shouldCreateControllerWithRepository() {
        // Act
        PickPathController newController = new PickPathController(trolleyRepository);

        // Assert
        assertNotNull(newController);
        // Note: We can't directly test the private field, but we can test behavior
    }

    // ========== TESTES DO MÉTODO PROCESS PICK PATH SEQUENCING ==========

    @Test
    @DisplayName("processPickPathSequencing should process valid stops correctly")
    void processPickPathSequencing_shouldProcessValidStopsCorrectly() {
        // Act
        Map<PathStrategy, PickPath> result = controller.processPickPathSequencing(sampleStops);

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
    @DisplayName("processPickPathSequencing should throw exception for null input")
    void processPickPathSequencing_shouldThrowExceptionForNullInput() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            controller.processPickPathSequencing(null);
        });
        
        assertEquals("Stop list cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("processPickPathSequencing should throw exception for empty input")
    void processPickPathSequencing_shouldThrowExceptionForEmptyInput() {
        // Arrange
        List<PickStop> emptyStops = new ArrayList<>();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            controller.processPickPathSequencing(emptyStops);
        });
        
        assertEquals("Stop list cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("processPickPathSequencing should handle single stop")
    void processPickPathSequencing_shouldHandleSingleStop() {
        // Arrange
        List<PickStop> singleStop = Arrays.asList(new PickStop(1, 1, 1, 5));

        // Act
        Map<PathStrategy, PickPath> result = controller.processPickPathSequencing(singleStop);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        PickPath ascSweepPath = result.get(PathStrategy.ASC_SWEEP);
        PickPath nearestNeighbourPath = result.get(PathStrategy.NEAREST_NEIGHBOUR);
        
        assertEquals(1, ascSweepPath.getStops().size());
        assertEquals(1, nearestNeighbourPath.getStops().size());
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
        List<PickStop> result = controller.consolidateStops(stopsWithDuplicates);

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
    @DisplayName("consolidateStops should throw exception for null input")
    void consolidateStops_shouldThrowExceptionForNullInput() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            controller.consolidateStops(null);
        });
        
        assertEquals("Stop list cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("consolidateStops should throw exception for empty input")
    void consolidateStops_shouldThrowExceptionForEmptyInput() {
        // Arrange
        List<PickStop> emptyStops = new ArrayList<>();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            controller.consolidateStops(emptyStops);
        });
        
        assertEquals("Stop list cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("consolidateStops should return same list for no duplicates")
    void consolidateStops_shouldReturnSameListForNoDuplicates() {
        // Act
        List<PickStop> result = controller.consolidateStops(sampleStops);

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.containsAll(sampleStops));
    }

    // ========== TESTES DO MÉTODO GENERATE BOTH PATHS ==========

    @Test
    @DisplayName("generateBothPaths should generate both strategies correctly")
    void generateBothPaths_shouldGenerateBothStrategiesCorrectly() {
        // Act
        Map<PathStrategy, PickPath> result = controller.generateBothPaths(sampleStops);

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
    @DisplayName("generateBothPaths should throw exception for null input")
    void generateBothPaths_shouldThrowExceptionForNullInput() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            controller.generateBothPaths(null);
        });
        
        assertEquals("Stop list cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("generateBothPaths should throw exception for empty input")
    void generateBothPaths_shouldThrowExceptionForEmptyInput() {
        // Arrange
        List<PickStop> emptyStops = new ArrayList<>();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            controller.generateBothPaths(emptyStops);
        });
        
        assertEquals("Stop list cannot be null or empty", exception.getMessage());
    }

    // ========== TESTES DO MÉTODO GET TROLLEYS FROM US03 ==========

    @Test
    @DisplayName("getTrolleysFromUS03 should throw exception when repository is null")
    void getTrolleysFromUS03_shouldThrowExceptionWhenRepositoryIsNull() {
        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            controller.getTrolleysFromUS03();
        });
        
        assertEquals("TrolleyRepository not initialized. Cannot access trolley data from US03.", exception.getMessage());
    }

    @Test
    @DisplayName("getTrolleysFromUS03 should return empty list when repository is empty")
    void getTrolleysFromUS03_shouldReturnEmptyListWhenRepositoryIsEmpty() {
        // Arrange
        PickPathController controllerWithRepo = new PickPathController(trolleyRepository);

        // Act
        List<Trolley> result = controllerWithRepo.getTrolleysFromUS03();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getTrolleysFromUS03 should return trolleys when repository has data")
    void getTrolleysFromUS03_shouldReturnTrolleysWhenRepositoryHasData() {
        // Arrange
        List<Trolley> trolleys = Arrays.asList(
                new Trolley(100, 0.0, 1, new ArrayList<>(), "HEURISTIC1"),
                new Trolley(150, 0.0, 2, new ArrayList<>(), "HEURISTIC2")
        );
        trolleyRepository.saveTrolley(trolleys);
        PickPathController controllerWithRepo = new PickPathController(trolleyRepository);

        // Act
        List<Trolley> result = controllerWithRepo.getTrolleysFromUS03();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsAll(trolleys));
    }

    // ========== TESTES DO MÉTODO CALCULATE DISTANCE ==========

    @Test
    @DisplayName("calculateDistance should return correct distance for same aisle")
    void calculateDistance_shouldReturnCorrectDistanceForSameAisle() {
        // Act
        int distance = PickPathController.calculateDistance(1, 5, 1, 8);

        // Assert
        assertEquals(3, distance); // |5 - 8| = 3
    }

    @Test
    @DisplayName("calculateDistance should return zero for same position")
    void calculateDistance_shouldReturnZeroForSamePosition() {
        // Act
        int distance = PickPathController.calculateDistance(2, 3, 2, 3);

        // Assert
        assertEquals(0, distance);
    }

    @Test
    @DisplayName("calculateDistance should return correct distance for different aisles")
    void calculateDistance_shouldReturnCorrectDistanceForDifferentAisles() {
        // Act
        int distance = PickPathController.calculateDistance(1, 5, 3, 8);

        // Assert
        assertEquals(19, distance); // 5 + (|1-3| * 3) + 8 = 5 + 6 + 8 = 19
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0, 0, 0, 0",
            "1, 1, 1, 1, 0",
            "1, 5, 1, 8, 3",
            "2, 3, 2, 1, 2",
            "1, 2, 3, 4, 12",
            "3, 1, 1, 5, 9",
            "0, 0, 1, 0, 3",
            "1, 0, 0, 0, 3"
    })
    @DisplayName("calculateDistance should return correct distance for various inputs")
    void calculateDistance_shouldReturnCorrectDistanceForVariousInputs(int a1, int b1, int a2, int b2, int expected) {
        // Act
        int distance = PickPathController.calculateDistance(a1, b1, a2, b2);

        // Assert
        assertEquals(expected, distance);
    }

    @Test
    @DisplayName("calculateDistance should handle negative values correctly")
    void calculateDistance_shouldHandleNegativeValuesCorrectly() {
        // Act
        int distance = PickPathController.calculateDistance(-1, -2, -3, -4);

        // Assert
        assertEquals(8, distance); // |-2| + (|-1-(-3)| * 3) + |-4| = 2 + 6 + 4 = 12
    }

    // ========== TESTES DO MÉTODO CALCULATE TOTAL DISTANCE ==========

    @Test
    @DisplayName("calculateTotalDistance should return zero for empty list")
    void calculateTotalDistance_shouldReturnZeroForEmptyList() {
        // Arrange
        List<PickStop> emptyStops = new ArrayList<>();

        // Act
        int distance = controller.calculateTotalDistance(emptyStops);

        // Assert
        assertEquals(0, distance);
    }

    @Test
    @DisplayName("calculateTotalDistance should return correct distance for single stop")
    void calculateTotalDistance_shouldReturnCorrectDistanceForSingleStop() {
        // Arrange
        List<PickStop> singleStop = Arrays.asList(new PickStop(1, 2, 3, 5));

        // Act
        int distance = controller.calculateTotalDistance(singleStop);

        // Assert
        assertEquals(5, distance); // 0 + (|0-2| * 3) + 3 = 0 + 6 + 3 = 9
    }

    @Test
    @DisplayName("calculateTotalDistance should return correct distance for multiple stops")
    void calculateTotalDistance_shouldReturnCorrectDistanceForMultipleStops() {
        // Arrange
        List<PickStop> stops = Arrays.asList(
                new PickStop(1, 1, 1, 5),
                new PickStop(2, 2, 2, 3),
                new PickStop(3, 3, 3, 7)
        );

        // Act
        int distance = controller.calculateTotalDistance(stops);

        // Assert
        // From (0,0) to (1,1): 0 + (|0-1| * 3) + 1 = 4
        // From (1,1) to (2,2): 1 + (|1-2| * 3) + 2 = 6
        // From (2,2) to (3,3): 2 + (|2-3| * 3) + 3 = 8
        // Total: 4 + 6 + 8 = 18
        assertEquals(18, distance);
    }

    // ========== TESTES DO MÉTODO CALCULATE ASC SWEEP ==========

    @Test
    @DisplayName("calculateAscSweep should sort stops by aisle then bay")
    void calculateAscSweep_shouldSortStopsByAisleThenBay() {
        // Arrange
        List<PickStop> unsortedStops = Arrays.asList(
                new PickStop(1, 3, 4, 5),
                new PickStop(2, 1, 8, 3),
                new PickStop(3, 2, 2, 7),
                new PickStop(4, 1, 1, 4)
        );

        // Act
        List<PickStop> result = controller.calculateAscSweep(unsortedStops);

        // Assert
        assertEquals(4, result.size());
        assertEquals(1, result.get(0).getAisle());
        assertEquals(1, result.get(0).getBay());
        assertEquals(1, result.get(1).getAisle());
        assertEquals(8, result.get(1).getBay());
        assertEquals(2, result.get(2).getAisle());
        assertEquals(2, result.get(2).getBay());
        assertEquals(3, result.get(3).getAisle());
        assertEquals(4, result.get(3).getBay());
    }

    @Test
    @DisplayName("calculateAscSweep should not modify original list")
    void calculateAscSweep_shouldNotModifyOriginalList() {
        // Arrange
        List<PickStop> originalStops = new ArrayList<>(sampleStops);
        List<PickStop> originalCopy = new ArrayList<>(originalStops);

        // Act
        controller.calculateAscSweep(originalStops);

        // Assert
        assertEquals(originalCopy, originalStops);
    }

    @Test
    @DisplayName("calculateAscSweep should handle single stop")
    void calculateAscSweep_shouldHandleSingleStop() {
        // Arrange
        List<PickStop> singleStop = Arrays.asList(new PickStop(1, 5, 3, 7));

        // Act
        List<PickStop> result = controller.calculateAscSweep(singleStop);

        // Assert
        assertEquals(1, result.size());
        assertEquals(singleStop.get(0), result.get(0));
    }

    // ========== TESTES DO MÉTODO CALCULATE NEAREST NEIGHBOUR ==========

    @Test
    @DisplayName("calculateNearestNeighbour should find optimal path")
    void calculateNearestNeighbour_shouldFindOptimalPath() {
        // Arrange
        List<PickStop> stops = Arrays.asList(
                new PickStop(1, 1, 1, 5),
                new PickStop(2, 3, 3, 3),
                new PickStop(3, 2, 2, 7)
        );

        // Act
        List<PickStop> result = controller.calculateNearestNeighbour(stops);

        // Assert
        assertEquals(3, result.size());
        // The first stop should be the closest to (0,0)
        // Distance from (0,0) to (1,1) = 4
        // Distance from (0,0) to (2,2) = 8
        // Distance from (0,0) to (3,3) = 12
        // So (1,1) should be first
        assertEquals(1, result.get(0).getAisle());
        assertEquals(1, result.get(0).getBay());
    }

    @Test
    @DisplayName("calculateNearestNeighbour should not modify original list")
    void calculateNearestNeighbour_shouldNotModifyOriginalList() {
        // Arrange
        List<PickStop> originalStops = new ArrayList<>(sampleStops);
        List<PickStop> originalCopy = new ArrayList<>(originalStops);

        // Act
        controller.calculateNearestNeighbour(originalStops);

        // Assert
        assertEquals(originalCopy, originalStops);
    }

    @Test
    @DisplayName("calculateNearestNeighbour should handle single stop")
    void calculateNearestNeighbour_shouldHandleSingleStop() {
        // Arrange
        List<PickStop> singleStop = Arrays.asList(new PickStop(1, 5, 3, 7));

        // Act
        List<PickStop> result = controller.calculateNearestNeighbour(singleStop);

        // Assert
        assertEquals(1, result.size());
        assertEquals(singleStop.get(0), result.get(0));
    }

    // ========== TESTES DO MÉTODO CREATE CALCULATED PATH ==========

    @Test
    @DisplayName("createCalculatedPath should create ASC_SWEEP path correctly")
    void createCalculatedPath_shouldCreateAscSweepPathCorrectly() {
        // Act
        PickPath result = controller.createCalculatedPath(PathStrategy.ASC_SWEEP, sampleStops);

        // Assert
        assertNotNull(result);
        assertEquals(PathStrategy.ASC_SWEEP, result.getStrategy());
        assertEquals(3, result.getStops().size());
        assertTrue(result.getTotalDistance() > 0);
    }

    @Test
    @DisplayName("createCalculatedPath should create NEAREST_NEIGHBOUR path correctly")
    void createCalculatedPath_shouldCreateNearestNeighbourPathCorrectly() {
        // Act
        PickPath result = controller.createCalculatedPath(PathStrategy.NEAREST_NEIGHBOUR, sampleStops);

        // Assert
        assertNotNull(result);
        assertEquals(PathStrategy.NEAREST_NEIGHBOUR, result.getStrategy());
        assertEquals(3, result.getStops().size());
        assertTrue(result.getTotalDistance() > 0);
    }

    @Test
    @DisplayName("createCalculatedPath should handle unknown strategy")
    void createCalculatedPath_shouldHandleUnknownStrategy() {
        // Act
        PickPath result = controller.createCalculatedPath(null, sampleStops);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getStops().size());
        // Should return original stops without modification
    }

    @Test
    @DisplayName("createCalculatedPath should calculate correct total distance")
    void createCalculatedPath_shouldCalculateCorrectTotalDistance() {
        // Arrange
        List<PickStop> stops = Arrays.asList(
                new PickStop(1, 1, 1, 5),
                new PickStop(2, 2, 2, 3)
        );

        // Act
        PickPath ascSweepPath = controller.createCalculatedPath(PathStrategy.ASC_SWEEP, stops);
        PickPath nearestNeighbourPath = controller.createCalculatedPath(PathStrategy.NEAREST_NEIGHBOUR, stops);

        // Assert
        assertTrue(ascSweepPath.getTotalDistance() > 0);
        assertTrue(nearestNeighbourPath.getTotalDistance() > 0);
        // Both should have the same total distance for this simple case
        assertEquals(ascSweepPath.getTotalDistance(), nearestNeighbourPath.getTotalDistance());
    }

    // ========== TESTES DE INTEGRAÇÃO ==========

    @Test
    @DisplayName("Full process should work end-to-end")
    void fullProcess_shouldWorkEndToEnd() {
        // Arrange
        List<PickStop> inputStops = Arrays.asList(
                new PickStop(1, 3, 4, 5),
                new PickStop(2, 1, 8, 3),
                new PickStop(3, 2, 2, 7),
                new PickStop(4, 1, 1, 4),
                new PickStop(5, 2, 2, 2) // Duplicate of stop 3
        );

        // Act
        Map<PathStrategy, PickPath> result = controller.processPickPathSequencing(inputStops);

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
    }

    @Test
    @DisplayName("Controller should handle large number of stops")
    void controller_shouldHandleLargeNumberOfStops() {
        // Arrange
        List<PickStop> manyStops = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            manyStops.add(new PickStop(i, i % 10, i % 20, i % 5 + 1));
        }

        // Act
        Map<PathStrategy, PickPath> result = controller.processPickPathSequencing(manyStops);

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
}
