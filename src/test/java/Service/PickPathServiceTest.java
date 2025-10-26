package Service;

import Domain.ENUM.PathStrategy;
import Domain.PickPath;
import Domain.PickStop;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PickPathServiceTest {

    @Test
    void generateBothPaths_WithValidStops_ReturnsBothStrategies() {
        // Arrange
        List<PickStop> stops = Arrays.asList(
                new PickStop(1, 1, 1, 10),
                new PickStop(2, 2, 2, 20),
                new PickStop(3, 1, 3, 15)
        );

        // Act
        Map<PathStrategy, PickPath> result = PickPathService.generateBothPaths(stops);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(PathStrategy.ASC_SWEEP));
        assertTrue(result.containsKey(PathStrategy.NEAREST_NEIGHBOUR));

        PickPath ascPath = result.get(PathStrategy.ASC_SWEEP);
        PickPath nnPath = result.get(PathStrategy.NEAREST_NEIGHBOUR);

        assertEquals(PathStrategy.ASC_SWEEP, ascPath.getStrategy());
        assertEquals(PathStrategy.NEAREST_NEIGHBOUR, nnPath.getStrategy());
        assertTrue(ascPath.getTotalDistance() >= 0);
        assertTrue(nnPath.getTotalDistance() >= 0);
    }

    @Test
    void generateBothPaths_WithSingleStop_ReturnsValidPaths() {
        // Arrange
        List<PickStop> singleStop = Collections.singletonList(
                new PickStop(1, 1, 1, 10)
        );

        // Act
        Map<PathStrategy, PickPath> result = PickPathService.generateBothPaths(singleStop);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        PickPath ascPath = result.get(PathStrategy.ASC_SWEEP);
        PickPath nnPath = result.get(PathStrategy.NEAREST_NEIGHBOUR);

        assertEquals(1, ascPath.getStops().size());
        assertEquals(1, nnPath.getStops().size());
        assertTrue(ascPath.getTotalDistance() >= 0);
        assertTrue(nnPath.getTotalDistance() >= 0);
    }

    @Test
    void generateBothPaths_WithEmptyList_ReturnsEmptyPaths() {
        // Arrange
        List<PickStop> emptyStops = new ArrayList<>();

        // Act
        Map<PathStrategy, PickPath> result = PickPathService.generateBothPaths(emptyStops);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        PickPath ascPath = result.get(PathStrategy.ASC_SWEEP);
        PickPath nnPath = result.get(PathStrategy.NEAREST_NEIGHBOUR);

        assertEquals(0, ascPath.getStops().size());
        assertEquals(0, nnPath.getStops().size());
        assertEquals(0, ascPath.getTotalDistance());
        assertEquals(0, nnPath.getTotalDistance());
    }

    @Test
    void consolidateStops_WithDuplicates_MergesQuantities() {
        // Arrange
        List<PickStop> stopsWithDuplicates = Arrays.asList(
                new PickStop(1, 1, 1, 10),
                new PickStop(2, 1, 1, 20), // Same aisle/bay
                new PickStop(3, 2, 2, 30),
                new PickStop(4, 1, 1, 15)  // Same aisle/bay
        );

        // Act
        List<PickStop> result = PickPathService.consolidateStops(stopsWithDuplicates);

        // Assert
        assertEquals(2, result.size()); // Should have 2 unique locations

        // Find the consolidated stop for aisle 1, bay 1
        Optional<PickStop> consolidated = result.stream()
                .filter(stop -> stop.getAisle() == 1 && stop.getBay() == 1)
                .findFirst();

        assertTrue(consolidated.isPresent());
        assertEquals(45, consolidated.get().getTotalQty()); // 10 + 20 + 15
    }

    @Test
    void consolidateStops_WithNoDuplicates_ReturnsSameStops() {
        // Arrange
        List<PickStop> uniqueStops = Arrays.asList(
                new PickStop(1, 1, 1, 10),
                new PickStop(2, 1, 2, 20),
                new PickStop(3, 2, 1, 30)
        );

        // Act
        List<PickStop> result = PickPathService.consolidateStops(uniqueStops);

        // Assert
        assertEquals(3, result.size());
        // Note: The order might change due to HashMap, so check by content
        assertTrue(result.stream().anyMatch(stop -> stop.getTotalQty() == 10));
        assertTrue(result.stream().anyMatch(stop -> stop.getTotalQty() == 20));
        assertTrue(result.stream().anyMatch(stop -> stop.getTotalQty() == 30));
    }

    @Test
    void consolidateStops_WithNullInput_ReturnsEmptyList() {
        // Act
        List<PickStop> result = PickPathService.consolidateStops(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void consolidateStops_WithEmptyInput_ReturnsEmptyList() {
        // Arrange
        List<PickStop> emptyList = new ArrayList<>();

        // Act
        List<PickStop> result = PickPathService.consolidateStops(emptyList);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void processPickPathSequencing_CompleteFlow_ReturnsBothPaths() {
        // Arrange
        List<PickStop> inputStops = Arrays.asList(
                new PickStop(1, 1, 1, 10),
                new PickStop(2, 1, 1, 20), // Will be consolidated
                new PickStop(3, 2, 2, 30),
                new PickStop(4, 1, 2, 15)
        );

        // Act
        Map<PathStrategy, PickPath> result = PickPathService.processPickPathSequencing(inputStops);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        PickPath ascPath = result.get(PathStrategy.ASC_SWEEP);
        PickPath nnPath = result.get(PathStrategy.NEAREST_NEIGHBOUR);

        // After consolidation, should have 3 stops instead of 4
        assertEquals(3, ascPath.getStops().size());
        assertEquals(3, nnPath.getStops().size());

        // Both paths should have valid distances
        assertTrue(ascPath.getTotalDistance() > 0);
        assertTrue(nnPath.getTotalDistance() > 0);
    }

    @Test
    void processPickPathSequencing_WithSingleStop_ReturnsValidPaths() {
        // Arrange
        List<PickStop> singleStop = Collections.singletonList(
                new PickStop(1, 3, 4, 25)
        );

        // Act
        Map<PathStrategy, PickPath> result = PickPathService.processPickPathSequencing(singleStop);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        PickPath ascPath = result.get(PathStrategy.ASC_SWEEP);
        assertEquals(1, ascPath.getStops().size());
        assertEquals(3, ascPath.getStops().get(0).getAisle());
        assertEquals(4, ascPath.getStops().get(0).getBay());
        assertEquals(25, ascPath.getStops().get(0).getTotalQty());
    }

    @Test
    void processPickPathSequencing_WithAllDuplicates_ReturnsSingleStopPaths() {
        // Arrange
        List<PickStop> allDuplicates = Arrays.asList(
                new PickStop(1, 1, 1, 10),
                new PickStop(2, 1, 1, 20),
                new PickStop(3, 1, 1, 30)
        );

        // Act
        Map<PathStrategy, PickPath> result = PickPathService.processPickPathSequencing(allDuplicates);

        // Assert
        assertNotNull(result);

        PickPath ascPath = result.get(PathStrategy.ASC_SWEEP);
        assertEquals(1, ascPath.getStops().size());

        // Find the stop with the merged quantity
        int totalQty = ascPath.getStops().get(0).getTotalQty();
        assertEquals(60, totalQty); // 10 + 20 + 30
    }

    @Test
    void processPickPathSequencing_ComplexScenario_ReturnsCorrectResults() {
        // Arrange - Complex scenario with multiple duplicates and different locations
        List<PickStop> complexStops = Arrays.asList(
                new PickStop(1, 1, 1, 5),
                new PickStop(2, 2, 2, 10),
                new PickStop(3, 1, 1, 15), // Duplicate of (1,1)
                new PickStop(4, 3, 3, 20),
                new PickStop(5, 2, 2, 25), // Duplicate of (2,2)
                new PickStop(6, 1, 2, 30)
        );

        // Act
        Map<PathStrategy, PickPath> result = PickPathService.processPickPathSequencing(complexStops);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        PickPath ascPath = result.get(PathStrategy.ASC_SWEEP);
        PickPath nnPath = result.get(PathStrategy.NEAREST_NEIGHBOUR);

        // After consolidation: (1,1)=20, (2,2)=35, (3,3)=20, (1,2)=30 → 4 stops
        assertEquals(4, ascPath.getStops().size());
        assertEquals(4, nnPath.getStops().size());

        // Verify distances are calculated
        assertTrue(ascPath.getTotalDistance() > 0);
        assertTrue(nnPath.getTotalDistance() > 0);

        // Verify strategies are different
        assertEquals(PathStrategy.ASC_SWEEP, ascPath.getStrategy());
        assertEquals(PathStrategy.NEAREST_NEIGHBOUR, nnPath.getStrategy());
    }

    // Additional edge case tests
    @Test
    void generateBothPaths_WithStopsAtSameAisleDifferentBays() {
        // Arrange
        List<PickStop> stops = Arrays.asList(
                new PickStop(1, 1, 5, 10),
                new PickStop(2, 1, 1, 20),
                new PickStop(3, 1, 3, 15)
        );

        // Act
        Map<PathStrategy, PickPath> result = PickPathService.generateBothPaths(stops);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        PickPath ascPath = result.get(PathStrategy.ASC_SWEEP);
        // ASC_SWEEP should order by bay: (1,1), (1,3), (1,5)
        assertEquals(1, ascPath.getStops().get(0).getBay());
        assertEquals(3, ascPath.getStops().get(1).getBay());
        assertEquals(5, ascPath.getStops().get(2).getBay());
    }

    @Test
    void consolidateStops_PreservesNonDuplicateAttributes() {
        // Arrange
        List<PickStop> stops = Arrays.asList(
                new PickStop(5, 1, 1, 10), // This sequence should be preserved
                new PickStop(2, 1, 1, 20)  // This should merge into the first one
        );

        // Act
        List<PickStop> result = PickPathService.consolidateStops(stops);

        // Assert
        assertEquals(1, result.size());
        // The first sequence number encountered should be preserved
        assertEquals(5, result.get(0).getSeq());
        assertEquals(30, result.get(0).getTotalQty());
    }
}