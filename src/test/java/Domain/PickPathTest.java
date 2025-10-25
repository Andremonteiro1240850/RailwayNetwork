package Domain;

import Domain.ENUM.PathStrategy;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PickPathTest {

    @Test
    void getStrategy_ReturnsCorrectStrategy() {
        List<PickStop> stops = Arrays.asList(
                new PickStop(1, 1, 1, 2),
                new PickStop(2, 2, 3, 5),
                new PickStop(3, 3, 2, 1)
        );
        PickPath path = new PickPath(PathStrategy.ASC_SWEEP, stops, 20);

        assertEquals(PathStrategy.ASC_SWEEP, path.getStrategy());
    }

    @Test
    void getTotalDistance_ReturnsCorrectDistance() {
        List<PickStop> stops = Arrays.asList(
                new PickStop(1, 0, 0, 1),
                new PickStop(2, 1, 2, 3),
                new PickStop(3, 2, 1, 2)
        );
        PickPath path = new PickPath(PathStrategy.NEAREST_NEIGHBOUR, stops, 25);

        assertEquals(25, path.getTotalDistance());
    }

    @Test
    void getStops_ReturnsCopyNotOriginalList() {
        PickStop stop1 = new PickStop(1, 1, 1, 2);
        PickStop stop2 = new PickStop(2, 2, 2, 3);
        PickStop stop3 = new PickStop(3, 3, 3, 1);

        List<PickStop> originalStops = new ArrayList<>(Arrays.asList(stop1, stop2, stop3));
        PickPath path = new PickPath(PathStrategy.ASC_SWEEP, originalStops, 30);

        List<PickStop> returnedStops = path.getStops();
        assertEquals(3, returnedStops.size());
        assertTrue(returnedStops.containsAll(originalStops));

        // Modifying returned list should not affect internal list
        returnedStops.clear();
        assertEquals(3, path.getStops().size());

        // Modifying original list should not affect PickPath
        originalStops.clear();
        assertEquals(3, path.getStops().size());
    }

    @Test
    void testToString_ContainsAllRelevantInformation() {
        List<PickStop> stops = Arrays.asList(
                new PickStop(1, 1, 1, 2),
                new PickStop(2, 2, 3, 5)
        );
        PickPath path = new PickPath(PathStrategy.NEAREST_NEIGHBOUR, stops, 18);

        String result = path.toString();
        assertTrue(result.contains("NEAREST_NEIGHBOUR"));
        assertTrue(result.contains("18"));
        assertTrue(result.contains("(1,1) Qty:2"));
        assertTrue(result.contains("(2,3) Qty:5"));
    }

    @Test
    void largeList_GetStopsAndDistance_ReturnsCorrectValues() {
        List<PickStop> stops = new ArrayList<>();
        int totalQty = 1;
        int expectedDistance = 0;
        int currentAisle = 0, currentBay = 0;

        // Create 10 stops at different positions
        for (int i = 1; i <= 10; i++) {
            PickStop stop = new PickStop(i, i % 4, i % 5, totalQty);
            stops.add(stop);

            // Calculate distance manually for validation
            int distance;
            if (currentAisle == stop.getAisle()) {
                distance = Math.abs(currentBay - stop.getBay());
            } else {
                distance = currentBay + Math.abs(currentAisle - stop.getAisle()) * 3 + stop.getBay();
            }
            expectedDistance += distance;
            currentAisle = stop.getAisle();
            currentBay = stop.getBay();
        }

        PickPath path = new PickPath(PathStrategy.ASC_SWEEP, stops, expectedDistance);

        assertEquals(10, path.getStops().size());
        assertEquals(expectedDistance, path.getTotalDistance());
    }

    @Test
    void constructor_WithNullStrategy_ThrowsException() {
        List<PickStop> stops = Arrays.asList(
                new PickStop(1, 1, 1, 2)
        );

        assertThrows(IllegalArgumentException.class, () -> {
            new PickPath(null, stops, 10);
        });
    }

    @Test
    void constructor_WithNullStops_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PickPath(PathStrategy.ASC_SWEEP, null, 10);
        });
    }

    @Test
    void constructor_WithEmptyStops_CreatesValidObject() {
        List<PickStop> emptyStops = new ArrayList<>();
        PickPath path = new PickPath(PathStrategy.ASC_SWEEP, emptyStops, 0);

        assertEquals(0, path.getStops().size());
        assertEquals(0, path.getTotalDistance());
    }

    @Test
    void constructor_WithNegativeDistance_ThrowsException() {
        List<PickStop> stops = Arrays.asList(
                new PickStop(1, 1, 1, 2)
        );

        assertThrows(IllegalArgumentException.class, () -> {
            new PickPath(PathStrategy.ASC_SWEEP, stops, -5);
        });
    }
}