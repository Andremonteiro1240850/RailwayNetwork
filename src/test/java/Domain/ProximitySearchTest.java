package Domain;

import Sprint2.Controller.ProximitySearchController;
import Sprint2.Domain.Station;
import Sprint2.Domain.ENUM.Country;
import Sprint2.Domain.KDTree;
import Sprint2.Domain.ProximitySearch;
import Sprint2.Domain.SearchFilters;
import Sprint2.Repository.StationRepository;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProximitySearch class using real CSV data.
 * All data is loaded from CSV file (no manual object creation).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProximitySearchTest {

    private static final String CSV_PATH = "train_station_dataset/train_stations_europe.csv";

    private List<Station> allStations;
    private StationRepository stationRepository;
    private ProximitySearchController controller;

    @BeforeAll
    public void setupRepository() throws Exception {
        // Load stations from CSV
        allStations = parseStationsFromCsv(CSV_PATH);
        assertFalse(allStations.isEmpty(), "CSV must contain at least one station");
        stationRepository = new StationRepository(allStations);
        
        // Build controller for creating ProximitySearch objects
        controller = new ProximitySearchController(allStations);
        assertNotNull(controller.getKDTree(), "KD-tree must be built for tests");
    }

    // ==================== Constructor Tests ====================

    @Test
    public void constructorWithFiltersShouldCreateProximitySearch() {
        // Use coordinates from CSV (Lisbon: 38.7223, -9.1393)
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        assertFalse(nearestStations.isEmpty(), "Should find stations from CSV");
        
        SearchFilters filters = new SearchFilters(null, null, Country.PT);
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations, filters);
        
        assertNotNull(search, "ProximitySearch should not be null");
        assertEquals(targetLat, search.getTargetLatitude(), 0.0001, "Should store target latitude");
        assertEquals(targetLon, search.getTargetLongitude(), 0.0001, "Should store target longitude");
        assertEquals(n, search.getRequestedN(), "Should store requested N");
        assertEquals(nearestStations.size(), search.getFoundCount(), "Should store found count");
        assertNotNull(search.getAppliedFilters(), "Should store filters");
    }

    @Test
    public void constructorWithoutFiltersShouldCreateProximitySearch() {
        // Use coordinates from CSV (Porto: 41.1579, -8.6291)
        double targetLat = 41.1579;
        double targetLon = -8.6291;
        int n = 10;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        assertFalse(nearestStations.isEmpty(), "Should find stations from CSV");
        
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations);
        
        assertNotNull(search, "ProximitySearch should not be null");
        assertEquals(targetLat, search.getTargetLatitude(), 0.0001, "Should store target latitude");
        assertEquals(targetLon, search.getTargetLongitude(), 0.0001, "Should store target longitude");
        assertEquals(n, search.getRequestedN(), "Should store requested N");
        assertNull(search.getAppliedFilters(), "Should have null filters");
    }

    @Test
    public void constructorShouldCreateDefensiveCopyOfNearestStations() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        int originalSize = nearestStations.size();
        
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations);
        
        // Modify original list
        nearestStations.clear();
        
        // ProximitySearch should still have the stations
        assertEquals(originalSize, search.getFoundCount(), "Should maintain defensive copy");
        assertFalse(search.getNearestStations().isEmpty(), "Should maintain defensive copy");
    }

    @Test
    public void constructorWithNullFiltersShouldCreateProximitySearch() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        SearchFilters nullFilters = null;
        
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations, nullFilters);
        
        assertNull(search.getAppliedFilters(), "Should accept null filters");
        assertFalse(search.hasFilters(), "Should not have filters when null");
    }

    @Test
    public void constructorWithEmptyNearestStationsShouldCreateProximitySearch() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        List<KDTree.StationDistance> emptyList = new ArrayList<>();
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, emptyList);
        
        assertNotNull(search, "Should accept empty list");
        assertEquals(0, search.getFoundCount(), "Should have zero found count");
        assertEquals(-1.0, search.getNearestDistance(), 0.001, "Should return -1 for nearest distance");
        assertEquals(-1.0, search.getFarthestDistance(), 0.001, "Should return -1 for farthest distance");
    }

    // ==================== Getter Tests ====================

    @Test
    public void getTargetLatitudeShouldReturnCorrectValue() {
        double targetLat = 40.4168; // Madrid
        double targetLon = -3.7038;
        int n = 3;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations);
        
        assertEquals(targetLat, search.getTargetLatitude(), 0.0001, "Should return correct latitude");
    }

    @Test
    public void getTargetLongitudeShouldReturnCorrectValue() {
        double targetLat = 40.4168;
        double targetLon = -3.7038; // Madrid
        int n = 3;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations);
        
        assertEquals(targetLon, search.getTargetLongitude(), 0.0001, "Should return correct longitude");
    }

    @Test
    public void getRequestedNShouldReturnCorrectValue() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 15;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations);
        
        assertEquals(n, search.getRequestedN(), "Should return correct requested N");
    }

    @Test
    public void getNearestStationsShouldReturnDefensiveCopy() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations);
        
        List<KDTree.StationDistance> returnedList = search.getNearestStations();
        assertNotNull(returnedList, "Should return non-null list");
        assertEquals(nearestStations.size(), returnedList.size(), "Should return same size");
        
        // Modify returned list
        returnedList.clear();
        
        // Original should still be intact
        assertEquals(nearestStations.size(), search.getFoundCount(), "Should maintain defensive copy");
        assertFalse(search.getNearestStations().isEmpty(), "Should maintain defensive copy");
    }

    @Test
    public void getFoundCountShouldReturnCorrectCount() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 7;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations);
        
        assertEquals(nearestStations.size(), search.getFoundCount(), "Should return correct found count");
        assertTrue(search.getFoundCount() > 0, "Should have found at least one station");
    }

    @Test
    public void getAppliedFiltersShouldReturnCorrectFilters() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        SearchFilters filters = new SearchFilters(true, null, Country.PT);
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations, filters);
        
        assertNotNull(search.getAppliedFilters(), "Should return filters");
        assertEquals(filters, search.getAppliedFilters(), "Should return same filters");
    }

    @Test
    public void getAppliedFiltersShouldReturnNullWhenNoFilters() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations);
        
        assertNull(search.getAppliedFilters(), "Should return null when no filters");
    }

    // ==================== hasFilters() Tests ====================

    @Test
    public void hasFiltersShouldReturnFalseWhenNoFilters() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations);
        
        assertFalse(search.hasFilters(), "Should return false when no filters");
    }

    @Test
    public void hasFiltersShouldReturnFalseWhenNullFilters() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        SearchFilters nullFilters = null;
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations, nullFilters);
        
        assertFalse(search.hasFilters(), "Should return false when filters are null");
    }

    @Test
    public void hasFiltersShouldReturnFalseWhenFiltersHaveNoActiveFilters() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        SearchFilters emptyFilters = new SearchFilters(); // All null
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations, emptyFilters);
        
        assertFalse(search.hasFilters(), "Should return false when filters have no active filters");
    }

    @Test
    public void hasFiltersShouldReturnTrueWhenCountryFilterApplied() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        SearchFilters filters = new SearchFilters(null, null, Country.PT);
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations, filters);
        
        assertTrue(search.hasFilters(), "Should return true when country filter is applied");
    }

    @Test
    public void hasFiltersShouldReturnTrueWhenCityFilterApplied() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        SearchFilters filters = new SearchFilters(true, null, null);
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations, filters);
        
        assertTrue(search.hasFilters(), "Should return true when city filter is applied");
    }

    @Test
    public void hasFiltersShouldReturnTrueWhenMainStationFilterApplied() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        SearchFilters filters = new SearchFilters(null, true, null);
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations, filters);
        
        assertTrue(search.hasFilters(), "Should return true when main station filter is applied");
    }

    @Test
    public void hasFiltersShouldReturnTrueWhenMultipleFiltersApplied() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        SearchFilters filters = new SearchFilters(true, true, Country.PT);
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations, filters);
        
        assertTrue(search.hasFilters(), "Should return true when multiple filters are applied");
    }

    // ==================== getNearestDistance() Tests ====================

    @Test
    public void getNearestDistanceShouldReturnFirstStationDistance() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        assertFalse(nearestStations.isEmpty(), "Should find stations");
        
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations);
        
        double expectedDistance = nearestStations.get(0).getDistance();
        assertEquals(expectedDistance, search.getNearestDistance(), 0.01, "Should return first station distance");
        assertTrue(search.getNearestDistance() >= 0, "Distance should be non-negative");
    }

    @Test
    public void getNearestDistanceShouldReturnMinusOneWhenEmpty() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        List<KDTree.StationDistance> emptyList = new ArrayList<>();
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, emptyList);
        
        assertEquals(-1.0, search.getNearestDistance(), 0.001, "Should return -1 when empty");
    }

    @Test
    public void getNearestDistanceShouldReturnZeroForExactMatch() {
        // Find a station from CSV and use its exact coordinates
        Station firstStation = allStations.get(0);
        double targetLat = firstStation.getLatitude();
        double targetLon = firstStation.getLongitude();
        int n = 1;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        assertFalse(nearestStations.isEmpty(), "Should find the station");
        
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations);
        
        // The distance should be very close to 0 (or exactly 0)
        assertTrue(search.getNearestDistance() >= 0, "Distance should be non-negative");
        assertTrue(search.getNearestDistance() < 0.1, "Distance should be very small for exact match");
    }

    // ==================== getFarthestDistance() Tests ====================

    @Test
    public void getFarthestDistanceShouldReturnLastStationDistance() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 10;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        assertTrue(nearestStations.size() > 1, "Should find multiple stations");
        
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations);
        
        double expectedDistance = nearestStations.get(nearestStations.size() - 1).getDistance();
        assertEquals(expectedDistance, search.getFarthestDistance(), 0.01, "Should return last station distance");
        assertTrue(search.getFarthestDistance() >= search.getNearestDistance(), "Farthest should be >= nearest");
    }

    @Test
    public void getFarthestDistanceShouldReturnMinusOneWhenEmpty() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        List<KDTree.StationDistance> emptyList = new ArrayList<>();
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, emptyList);
        
        assertEquals(-1.0, search.getFarthestDistance(), 0.001, "Should return -1 when empty");
    }

    @Test
    public void getFarthestDistanceShouldEqualNearestWhenOnlyOneStation() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 1;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        assertFalse(nearestStations.isEmpty(), "Should find at least one station");
        
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations);
        
        assertEquals(search.getNearestDistance(), search.getFarthestDistance(), 0.01, 
                "Farthest should equal nearest when only one station");
    }

    @Test
    public void getFarthestDistanceShouldBeGreaterThanNearestWhenMultipleStations() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        assertTrue(nearestStations.size() > 1, "Should find multiple stations");
        
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations);
        
        assertTrue(search.getFarthestDistance() > search.getNearestDistance(), 
                "Farthest should be greater than nearest when multiple stations");
    }

    // ==================== foundAllRequested() Tests ====================

    @Test
    public void foundAllRequestedShouldReturnTrueWhenFoundEqualsRequested() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        // Request exactly what we found
        int actualFound = nearestStations.size();
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, actualFound, nearestStations);
        
        assertTrue(search.foundAllRequested(), "Should return true when found equals requested");
    }

    @Test
    public void foundAllRequestedShouldReturnFalseWhenFoundLessThanRequested() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        int actualFound = nearestStations.size();
        // Request more than found
        int requested = actualFound + 10;
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, requested, nearestStations);
        
        assertFalse(search.foundAllRequested(), "Should return false when found less than requested");
    }

    @Test
    public void foundAllRequestedShouldReturnFalseWhenEmpty() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        List<KDTree.StationDistance> emptyList = new ArrayList<>();
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, emptyList);
        
        assertFalse(search.foundAllRequested(), "Should return false when empty");
    }

    @Test
    public void foundAllRequestedShouldReturnTrueWhenRequestedOneAndFoundOne() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 1;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        assertFalse(nearestStations.isEmpty(), "Should find at least one station");
        
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations);
        
        assertTrue(search.foundAllRequested(), "Should return true when requested 1 and found 1");
    }

    // ==================== toString() Tests ====================

    @Test
    public void toStringShouldIncludeTargetCoordinates() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations);
        
        String result = search.toString();
        assertNotNull(result, "toString should not return null");
        assertTrue(result.contains("38.7223"), "Should include latitude");
        assertTrue(result.contains("-9.1393"), "Should include longitude");
    }

    @Test
    public void toStringShouldIncludeRequestedAndFoundCount() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 7;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations);
        
        String result = search.toString();
        assertTrue(result.contains("requested=" + n), "Should include requested count");
        assertTrue(result.contains("found=" + nearestStations.size()), "Should include found count");
    }

    @Test
    public void toStringShouldIncludeFiltersWhenPresent() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        SearchFilters filters = new SearchFilters(null, null, Country.PT);
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations, filters);
        
        String result = search.toString();
        assertTrue(result.contains("filters="), "Should include filters when present");
    }

    @Test
    public void toStringShouldNotIncludeFiltersWhenAbsent() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations);
        
        String result = search.toString();
        assertFalse(result.contains("filters="), "Should not include filters when absent");
    }

    @Test
    public void toStringShouldIncludeDistancesWhenStationsFound() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        assertFalse(nearestStations.isEmpty(), "Should find stations");
        
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations);
        
        String result = search.toString();
        assertTrue(result.contains("nearestDistance="), "Should include nearest distance");
        assertTrue(result.contains("farthestDistance="), "Should include farthest distance");
        assertTrue(result.contains("km"), "Should include km unit");
    }

    @Test
    public void toStringShouldNotIncludeDistancesWhenNoStationsFound() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        List<KDTree.StationDistance> emptyList = new ArrayList<>();
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, emptyList);
        
        String result = search.toString();
        assertFalse(result.contains("nearestDistance="), "Should not include nearest distance when empty");
        assertFalse(result.contains("farthestDistance="), "Should not include farthest distance when empty");
    }

    @Test
    public void toStringShouldFormatDistancesCorrectly() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        assertFalse(nearestStations.isEmpty(), "Should find stations");
        
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations);
        
        String result = search.toString();
        // Check that distances are formatted with 2 decimal places
        // The format is "%.2f km", which produces numbers like "0.50 km", "10.50 km", "123.45 km"
        assertTrue(result.contains("nearestDistance="), "Should contain nearestDistance. Got: " + result);
        assertTrue(result.contains("farthestDistance="), "Should contain farthestDistance. Got: " + result);
        assertTrue(result.contains(" km"), "Should contain km unit. Got: " + result);
        
        // Get the actual distance values and format them the same way toString() does
        double nearestDistance = search.getNearestDistance();
        double farthestDistance = search.getFarthestDistance();
        
        // Format using the same format as toString() uses: String.format("%.2f km", distance)
        // This format always produces 2 decimal places (e.g., "10.50 km", "0.05 km")
        String expectedNearestFormat = String.format("%.2f km", nearestDistance);
        String expectedFarthestFormat = String.format("%.2f km", farthestDistance);
        
        // Verify the formatted strings are present in the result
        // Since we're using the exact same format (%.2f), this verifies the format is correct
        assertTrue(result.contains(expectedNearestFormat), 
                "Result should contain nearest distance formatted as '" + expectedNearestFormat + "'. Full result: " + result);
        assertTrue(result.contains(expectedFarthestFormat), 
                "Result should contain farthest distance formatted as '" + expectedFarthestFormat + "'. Full result: " + result);
    }

    @Test
    public void toStringShouldHaveCorrectStructure() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations);
        
        String result = search.toString();
        assertTrue(result.startsWith("ProximitySearch{"), "Should start with ProximitySearch{");
        assertTrue(result.endsWith("}"), "Should end with }");
    }

    // ==================== Integration Tests ====================

    @Test
    public void integrationTestWithAllFeatures() {
        // Use coordinates from CSV (Madrid: 40.4168, -3.7038)
        double targetLat = 40.4168;
        double targetLon = -3.7038;
        int n = 8;
        
        List<KDTree.StationDistance> nearestStations = controller.findNearestStations(targetLat, targetLon, n);
        SearchFilters filters = new SearchFilters(true, false, Country.ES);
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, nearestStations, filters);
        
        // Test all getters
        assertEquals(targetLat, search.getTargetLatitude(), 0.0001);
        assertEquals(targetLon, search.getTargetLongitude(), 0.0001);
        assertEquals(n, search.getRequestedN());
        assertEquals(nearestStations.size(), search.getFoundCount());
        assertNotNull(search.getAppliedFilters());
        
        // Test filter methods
        assertTrue(search.hasFilters());
        
        // Test distance methods
        if (!nearestStations.isEmpty()) {
            assertTrue(search.getNearestDistance() >= 0);
            assertTrue(search.getFarthestDistance() >= search.getNearestDistance());
        }
        
        // Test foundAllRequested
        boolean allFound = search.getFoundCount() == search.getRequestedN();
        assertEquals(allFound, search.foundAllRequested());
        
        // Test toString
        String result = search.toString();
        assertNotNull(result);
        assertTrue(result.contains(String.valueOf(targetLat)));
        assertTrue(result.contains(String.valueOf(targetLon)));
    }

    @Test
    public void integrationTestWithNoFiltersAndEmptyResults() {
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        int n = 5;
        
        // Use an empty list directly to test empty results scenario
        List<KDTree.StationDistance> emptyList = new ArrayList<>();
        ProximitySearch search = new ProximitySearch(targetLat, targetLon, n, emptyList);
        
        // Test with empty results
        assertEquals(0, search.getFoundCount(), "Should have zero found count");
        assertEquals(-1.0, search.getNearestDistance(), 0.001, "Should return -1 for nearest distance");
        assertEquals(-1.0, search.getFarthestDistance(), 0.001, "Should return -1 for farthest distance");
        assertFalse(search.foundAllRequested(), "Should not have found all requested");
        assertFalse(search.hasFilters(), "Should not have filters");
        assertNull(search.getAppliedFilters(), "Should have null filters");
        
        String result = search.toString();
        assertNotNull(result, "toString should not return null");
        assertFalse(result.contains("nearestDistance="), "Should not include nearest distance when empty");
        assertFalse(result.contains("farthestDistance="), "Should not include farthest distance when empty");
    }

    // ==================== Helper Methods ====================

    /**
     * Loads stations from CSV file.
     */
    private List<Station> parseStationsFromCsv(String csvPath) throws IOException {
        List<Station> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = parseCSVLine(line);
                
                if (values.length < 9) {
                    continue;
                }

                try {
                    String country = values[0].trim();
                    String timeZone = values[1].trim();
                    String timeZoneGroup = values[2].trim();
                    String stationName = values[3].trim();
                    String latStr = values[4].trim();
                    String lonStr = values[5].trim();
                    boolean isCity = Boolean.parseBoolean(values[6].trim());
                    boolean isMainStation = Boolean.parseBoolean(values[7].trim());
                    boolean isAirport = Boolean.parseBoolean(values[8].trim());

                    if (latStr.isEmpty() || lonStr.isEmpty() || stationName.isEmpty()) {
                        continue;
                    }

                    double latitude = Double.parseDouble(latStr);
                    double longitude = Double.parseDouble(lonStr);

                    Station station = new Station(stationName, latitude, longitude, country,
                            timeZone, timeZoneGroup, isCity, isMainStation, isAirport);
                    result.add(station);

                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        return result;
    }

    /**
     * Parses a CSV line, handling quoted fields.
     */
    private String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentField = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        result.add(currentField.toString());

        return result.toArray(new String[0]);
    }
}

