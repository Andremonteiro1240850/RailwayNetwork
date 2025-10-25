package Controller;

import Domain.PickPath;
import Domain.PickStop;
import Domain.Trolley;
import Domain.ENUM.PathStrategy;
import Repository.TrolleyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PickPathControllerTest {

    private PickPathController controller;

    @BeforeEach
    void init() {
        controller = new PickPathController();
    }

    // -------------------------
    // Sequenciamento de Picking
    // -------------------------
    @Test
    void shouldThrowExceptionWhenProcessingNullStops() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.processPickPathSequencing(null));
        assertEquals("Stop list cannot be null or empty", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenProcessingEmptyStops() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> controller.processPickPathSequencing(Collections.emptyList()));
        assertEquals("Stop list cannot be null or empty", ex.getMessage());
    }

    @Test
    void shouldReturnBothStrategiesForValidStops() {
        List<PickStop> stops = Arrays.asList(
                new PickStop(1, 1, 1, 2),
                new PickStop(2, 1, 2, 1),
                new PickStop(3, 2, 1, 1)
        );

        Map<PathStrategy, PickPath> result = controller.processPickPathSequencing(stops);

        assertNotNull(result);
        assertTrue(result.containsKey(PathStrategy.ASC_SWEEP));
        assertTrue(result.containsKey(PathStrategy.NEAREST_NEIGHBOUR));

        assertEquals(PathStrategy.ASC_SWEEP, result.get(PathStrategy.ASC_SWEEP).getStrategy());
        assertEquals(PathStrategy.NEAREST_NEIGHBOUR, result.get(PathStrategy.NEAREST_NEIGHBOUR).getStrategy());
    }

    // -------------------------
    // Consolidação de Stops
    // -------------------------
    @Test
    void shouldThrowExceptionWhenConsolidatingNullOrEmptyStops() {
        assertThrows(IllegalArgumentException.class, () -> controller.consolidateStops(null));
        assertThrows(IllegalArgumentException.class, () -> controller.consolidateStops(Collections.emptyList()));
    }

    @Test
    void shouldMergeDuplicateStopsAndSumQuantities() {
        List<PickStop> original = Arrays.asList(
                new PickStop(1, 1, 1, 2),
                new PickStop(2, 1, 1, 3),
                new PickStop(3, 2, 2, 1)
        );

        List<PickStop> consolidated = controller.consolidateStops(original);

        assertEquals(2, consolidated.size());

        Optional<PickStop> p11 = consolidated.stream()
                .filter(p -> p.getAisle() == 1 && p.getBay() == 1)
                .findFirst();
        assertTrue(p11.isPresent());
        assertEquals(5, p11.get().getTotalQty());
    }

    // -------------------------
    // Geração de Paths
    // -------------------------
    @Test
    void shouldThrowExceptionWhenGeneratingPathsWithInvalidStops() {
        assertThrows(IllegalArgumentException.class, () -> controller.generateBothPaths(null));
        assertThrows(IllegalArgumentException.class, () -> controller.generateBothPaths(Collections.emptyList()));
    }

    @Test
    void shouldGenerateBothAscSweepAndNearestNeighbourPaths() {
        List<PickStop> stops = Arrays.asList(
                new PickStop(1, 1, 1, 1),
                new PickStop(2, 1, 3, 1),
                new PickStop(3, 2, 1, 1)
        );

        Map<PathStrategy, PickPath> map = controller.generateBothPaths(stops);

        assertNotNull(map);
        assertEquals(2, map.size());
        assertTrue(map.containsKey(PathStrategy.ASC_SWEEP));
        assertTrue(map.containsKey(PathStrategy.NEAREST_NEIGHBOUR));
    }

    // -------------------------
    // Repositório de Trolleys
    // -------------------------
    @Test
    void shouldThrowExceptionWhenGettingTrolleysWithoutRepo() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> controller.getTrolleysFromUS03());
        assertEquals("TrolleyRepository not initialized. Cannot access trolley data from US03.", ex.getMessage());
    }

    @Test
    void shouldReturnSavedTrolleysFromRepository() {
        TrolleyRepository repo = new TrolleyRepository();

        Trolley t1 = new Trolley(100, 0.0, 1, new ArrayList<>(), "HEUR1");
        Trolley t2 = new Trolley(200, 0.0, 2, new ArrayList<>(), "HEUR2");

        repo.saveTrolley(Arrays.asList(t1, t2));

        PickPathController controllerWithRepo = new PickPathController(repo);
        List<Trolley> returned = controllerWithRepo.getTrolleysFromUS03();

        assertNotNull(returned);
        assertEquals(2, returned.size());
        assertTrue(returned.containsAll(Arrays.asList(t1, t2)));
    }

    // -------------------------
    // Cálculo de Distâncias
    // -------------------------
    @Test
    void shouldCalculateDistanceCorrectlyForSameAisle() {
        int d = PickPathController.calculateDistance(2, 10, 2, 4);
        assertEquals(6, d);
    }

    @Test
    void shouldCalculateDistanceCorrectlyForDifferentAisles() {
        int d = PickPathController.calculateDistance(1, 2, 4, 3);
        assertEquals(14, d);
    }

    @Test
    void shouldCalculateTotalDistanceCorrectly() {
        List<PickStop> stops = Arrays.asList(
                new PickStop(1, 1, 1, 1),
                new PickStop(2, 1, 3, 1),
                new PickStop(3, 2, 1, 1)
        );

        int expected = PickPathController.calculateDistance(0,0,1,1)
                + PickPathController.calculateDistance(1,1,1,3)
                + PickPathController.calculateDistance(1,3,2,1);

        assertEquals(expected, controller.calculateTotalDistance(stops));
    }

    // -------------------------
    // Estratégia AscSweep
    // -------------------------
    @Test
    void shouldSortStopsByAisleThenBay() {
        List<PickStop> input = Arrays.asList(
                new PickStop(1, 3, 5, 1),
                new PickStop(2, 1, 7, 1),
                new PickStop(3, 1, 2, 1),
                new PickStop(4, 2, 1, 1)
        );

        List<PickStop> sorted = controller.calculateAscSweep(input);

        assertEquals(1, sorted.get(0).getAisle());
        assertEquals(2, sorted.get(0).getBay());

        assertEquals(1, sorted.get(1).getAisle());
        assertEquals(7, sorted.get(1).getBay());
    }

    // -------------------------
    // Estratégia NearestNeighbour
    // -------------------------
    @Test
    void shouldFollowNearestNeighbourPath() {
        List<PickStop> input = Arrays.asList(
                new PickStop(1, 5, 1, 1),
                new PickStop(2, 1, 2, 1),
                new PickStop(3, 1, 1, 1)
        );

        List<PickStop> path = controller.calculateNearestNeighbour(input);

        assertEquals(3, path.size());
        assertEquals(1, path.get(0).getAisle());
        assertEquals(1, path.get(0).getBay());

        assertEquals(1, path.get(1).getAisle());
        assertEquals(2, path.get(1).getBay());

        assertEquals(5, path.get(2).getAisle());
        assertEquals(1, path.get(2).getBay());
    }

    // -------------------------
    // Criação de PickPath
    // -------------------------
    @Test
    void shouldCreatePickPathWithCorrectStrategyAndDistance() {
        List<PickStop> stops = Arrays.asList(
                new PickStop(1, 1, 1, 1),
                new PickStop(2, 1, 2, 1)
        );

        PickPath ascPath = controller.createCalculatedPath(PathStrategy.ASC_SWEEP, stops);
        assertEquals(PathStrategy.ASC_SWEEP, ascPath.getStrategy());
        assertEquals(controller.calculateTotalDistance(ascPath.getStops()), ascPath.getTotalDistance());

        PickPath nnPath = controller.createCalculatedPath(PathStrategy.NEAREST_NEIGHBOUR, stops);
        assertEquals(PathStrategy.NEAREST_NEIGHBOUR, nnPath.getStrategy());
        assertEquals(controller.calculateTotalDistance(nnPath.getStops()), nnPath.getTotalDistance());
    }
}
