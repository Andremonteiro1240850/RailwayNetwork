package UI;

import Controller.PickPathController;
import Domain.PickPath;
import Domain.PickStop;
import Domain.PickingItems;
import Domain.Trolley;
import Domain.ENUM.PathStrategy;
import Repository.TrolleyRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.ByteArrayInputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe PickPathUI
 * Testa métodos públicos e funcionalidades básicas
 * Nota: Alguns métodos privados são testados indiretamente através de métodos públicos
 */
@DisplayName("PickPathUI Tests")
class PickPathUITest {

    private PickPathController controller;
    private PickPathUI pickPathUI;
    private TrolleyRepository trolleyRepository;

    @BeforeEach
    void setUp() {
        trolleyRepository = new TrolleyRepository();
        controller = new PickPathController(trolleyRepository);
        pickPathUI = new PickPathUI(controller);
    }

    // ========== TESTES DO CONSTRUTOR ==========

    @Test
    @DisplayName("Constructor should create PickPathUI with controller")
    void constructor_shouldCreatePickPathUIWithController() {
        // Act
        PickPathUI newUI = new PickPathUI(controller);

        // Assert
        assertNotNull(newUI);
    }

    @Test
    @DisplayName("Constructor should handle null controller")
    void constructor_shouldHandleNullController() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            new PickPathUI(null);
        });
    }

    // ========== TESTES DOS MÉTODOS PÚBLICOS ==========

    @Test
    @DisplayName("showMainMenu should not throw exception with exit option")
    void showMainMenu_shouldNotThrowExceptionWithExitOption() {
        // Arrange
        String input = "5\n"; // Exit option
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // Act & Assert
        assertDoesNotThrow(() -> {
            pickPathUI.showMainMenu();
        });
    }

    @Test
    @DisplayName("showMainMenu should handle invalid input gracefully")
    void showMainMenu_shouldHandleInvalidInputGracefully() {
        // Arrange
        String input = "invalid\n5\n"; // Invalid input then exit
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // Act & Assert
        assertDoesNotThrow(() -> {
            pickPathUI.showMainMenu();
        });
    }

    // ========== TESTES DE INTEGRAÇÃO COM CONTROLLER ==========

    @Test
    @DisplayName("UI should work with controller for basic operations")
    void ui_shouldWorkWithControllerForBasicOperations() {
        // Arrange
        List<PickStop> sampleStops = Arrays.asList(
                new PickStop(1, 1, 1, 5),
                new PickStop(2, 2, 2, 3)
        );

        // Act & Assert
        assertDoesNotThrow(() -> {
            Map<PathStrategy, PickPath> results = controller.processPickPathSequencing(sampleStops);
            assertNotNull(results);
            assertEquals(2, results.size());
            assertTrue(results.containsKey(PathStrategy.ASC_SWEEP));
            assertTrue(results.containsKey(PathStrategy.NEAREST_NEIGHBOUR));
        });
    }

    @Test
    @DisplayName("UI should handle trolley data correctly")
    void ui_shouldHandleTrolleyDataCorrectly() {
        // Arrange
        List<PickingItems> items = Arrays.asList(
                new PickingItems("O1", 1, 1, 1, "B1", "SKU1", 5),
                new PickingItems("O2", 2, 2, 2, "B2", "SKU2", 3)
        );
        List<Trolley> trolleys = Arrays.asList(
                new Trolley(100, 0.0, 1, items, "HEURISTIC1")
        );
        trolleyRepository.saveTrolley(trolleys);

        // Act & Assert
        assertDoesNotThrow(() -> {
            List<Trolley> retrievedTrolleys = controller.getTrolleysFromUS03();
            assertNotNull(retrievedTrolleys);
            assertEquals(1, retrievedTrolleys.size());
            assertEquals(1, retrievedTrolleys.get(0).getId());
        });
    }

    // ========== TESTES DE CASOS LIMITE ==========

    @Test
    @DisplayName("UI should handle empty trolley repository")
    void ui_shouldHandleEmptyTrolleyRepository() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            List<Trolley> trolleys = controller.getTrolleysFromUS03();
            assertNotNull(trolleys);
            assertTrue(trolleys.isEmpty());
        });
    }

    @Test
    @DisplayName("UI should handle null input gracefully")
    void ui_shouldHandleNullInputGracefully() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            controller.processPickPathSequencing(null);
        });
    }

    @Test
    @DisplayName("UI should handle empty input gracefully")
    void ui_shouldHandleEmptyInputGracefully() {
        // Arrange
        List<PickStop> emptyStops = new ArrayList<>();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            controller.processPickPathSequencing(emptyStops);
        });
    }

    // ========== TESTES DE PERFORMANCE ==========

    @Test
    @DisplayName("UI should handle large number of stops")
    void ui_shouldHandleLargeNumberOfStops() {
        // Arrange
        List<PickStop> manyStops = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            manyStops.add(new PickStop(i, i % 10, i % 20, i % 5 + 1));
        }

        // Act & Assert
        assertDoesNotThrow(() -> {
            Map<PathStrategy, PickPath> results = controller.processPickPathSequencing(manyStops);
            assertNotNull(results);
            assertEquals(2, results.size());
            
            PickPath ascSweepPath = results.get(PathStrategy.ASC_SWEEP);
            PickPath nearestNeighbourPath = results.get(PathStrategy.NEAREST_NEIGHBOUR);
            
            assertNotNull(ascSweepPath);
            assertNotNull(nearestNeighbourPath);
            assertEquals(100, ascSweepPath.getStops().size());
            assertEquals(100, nearestNeighbourPath.getStops().size());
        });
    }

    // ========== TESTES DE VALIDAÇÃO ==========

    @Test
    @DisplayName("UI should validate controller operations")
    void ui_shouldValidateControllerOperations() {
        // Arrange
        List<PickStop> stops = Arrays.asList(
                new PickStop(1, 1, 1, 5),
                new PickStop(2, 1, 1, 3), // Duplicate
                new PickStop(3, 2, 2, 7)
        );

        // Act
        List<PickStop> consolidated = controller.consolidateStops(stops);
        Map<PathStrategy, PickPath> results = controller.processPickPathSequencing(stops);

        // Assert
        assertEquals(2, consolidated.size()); // Should consolidate duplicates
        
        PickStop consolidatedStop = consolidated.stream()
                .filter(stop -> stop.getAisle() == 1 && stop.getBay() == 1)
                .findFirst()
                .orElse(null);
        
        assertNotNull(consolidatedStop);
        assertEquals(8, consolidatedStop.getTotalQty()); // 5 + 3
        
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("UI should handle different path strategies correctly")
    void ui_shouldHandleDifferentPathStrategiesCorrectly() {
        // Arrange
        List<PickStop> stops = Arrays.asList(
                new PickStop(1, 3, 4, 5),
                new PickStop(2, 1, 8, 3),
                new PickStop(3, 2, 2, 7)
        );

        // Act
        PickPath ascSweepPath = controller.createCalculatedPath(PathStrategy.ASC_SWEEP, stops);
        PickPath nearestNeighbourPath = controller.createCalculatedPath(PathStrategy.NEAREST_NEIGHBOUR, stops);

        // Assert
        assertNotNull(ascSweepPath);
        assertNotNull(nearestNeighbourPath);
        
        assertEquals(PathStrategy.ASC_SWEEP, ascSweepPath.getStrategy());
        assertEquals(PathStrategy.NEAREST_NEIGHBOUR, nearestNeighbourPath.getStrategy());
        
        assertEquals(3, ascSweepPath.getStops().size());
        assertEquals(3, nearestNeighbourPath.getStops().size());
        
        assertTrue(ascSweepPath.getTotalDistance() > 0);
        assertTrue(nearestNeighbourPath.getTotalDistance() > 0);
    }

    // ========== TESTES DE DISTÂNCIA ==========

    @Test
    @DisplayName("UI should calculate distances correctly")
    void ui_shouldCalculateDistancesCorrectly() {
        // Test same aisle
        int distance1 = PickPathController.calculateDistance(1, 5, 1, 8);
        assertEquals(3, distance1); // |5 - 8| = 3

        // Test different aisles
        int distance2 = PickPathController.calculateDistance(1, 5, 3, 8);
        assertEquals(19, distance2); // 5 + (|1-3| * 3) + 8 = 5 + 6 + 8 = 19

        // Test same position
        int distance3 = PickPathController.calculateDistance(2, 3, 2, 3);
        assertEquals(0, distance3);
    }

    @Test
    @DisplayName("UI should calculate total distance correctly")
    void ui_shouldCalculateTotalDistanceCorrectly() {
        // Arrange
        List<PickStop> stops = Arrays.asList(
                new PickStop(1, 1, 1, 5),
                new PickStop(2, 2, 2, 3)
        );

        // Act
        int totalDistance = controller.calculateTotalDistance(stops);

        // Assert
        assertTrue(totalDistance > 0);
        // From (0,0) to (1,1): 0 + (|0-1| * 3) + 1 = 4
        // From (1,1) to (2,2): 1 + (|1-2| * 3) + 2 = 6
        // Total: 4 + 6 = 10
        assertEquals(10, totalDistance);
    }

    // ========== TESTES DE ROBUSTEZ ==========

    @Test
    @DisplayName("UI should handle extreme values")
    void ui_shouldHandleExtremeValues() {
        // Arrange
        List<PickStop> extremeStops = Arrays.asList(
                new PickStop(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE),
                new PickStop(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE)
        );

        // Act & Assert
        assertDoesNotThrow(() -> {
            Map<PathStrategy, PickPath> results = controller.processPickPathSequencing(extremeStops);
            assertNotNull(results);
            assertEquals(2, results.size());
        });
    }

    @Test
    @DisplayName("UI should handle zero values")
    void ui_shouldHandleZeroValues() {
        // Arrange
        List<PickStop> zeroStops = Arrays.asList(
                new PickStop(0, 0, 0, 0)
        );

        // Act & Assert
        assertDoesNotThrow(() -> {
            Map<PathStrategy, PickPath> results = controller.processPickPathSequencing(zeroStops);
            assertNotNull(results);
            assertEquals(2, results.size());
        });
    }
}