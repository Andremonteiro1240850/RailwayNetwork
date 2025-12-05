package Controller;

import Domain.KDTree;
import Domain.SearchFilters;
import Domain.Station;
import Domain.ENUM.Country;
import Repository.KDTreeRepository;
import Repository.StationRepository;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProximitySearchController class using real CSV data.
 * All data is loaded from CSV file (no manual object creation).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProximitySearchControllerTest {

    private static final String CSV_PATH = "train_station_dataset/train_stations_europe.csv";

    private List<Station> allStations;
    private StationRepository stationRepository;
    private KDTreeController kdTreeController;

    @BeforeAll
    public void setupRepository() throws Exception {
        // Load stations from CSV
        allStations = parseStationsFromCsv(CSV_PATH);
        assertFalse(allStations.isEmpty(), "CSV must contain at least one station");
        stationRepository = new StationRepository(allStations);
        
        // Build KD-tree for use in tests
        kdTreeController = new KDTreeController(stationRepository);
        kdTreeController.buildTree();
        assertTrue(kdTreeController.isTreeBuilt(), "KD-tree must be built for tests");
    }

    // ==================== Constructor Tests ====================

    @Test
    public void constructorWithListShouldCreateController() {
        ProximitySearchController controller = new ProximitySearchController(allStations);
        assertNotNull(controller, "Controller should not be null");
        assertNotNull(controller.getKDTree(), "KD-tree should not be null");
        assertFalse(controller.getKDTree().isEmpty(), "KD-tree should not be empty");
    }

    @Test
    public void constructorWithListShouldBuildTreeFromStations() {
        ProximitySearchController controller = new ProximitySearchController(allStations);
        KDTree tree = controller.getKDTree();
        assertNotNull(tree, "Tree should be built");
        assertTrue(tree.size() > 0, "Tree should have nodes");
    }

    @Test
    public void constructorWithKDTreeAndListShouldCreateController() {
        KDTree tree = kdTreeController.getKDTree();
        ProximitySearchController controller = new ProximitySearchController(tree, allStations);
        assertNotNull(controller, "Controller should not be null");
        assertSame(tree, controller.getKDTree(), "Should use provided tree");
    }

    @Test
    public void constructorWithKDTreeAndRepositoryShouldCreateController() {
        KDTree tree = kdTreeController.getKDTree();
        ProximitySearchController controller = new ProximitySearchController(tree, stationRepository);
        assertNotNull(controller, "Controller should not be null");
        assertSame(tree, controller.getKDTree(), "Should use provided tree");
        assertEquals(allStations.size(), controller.getStationsCount(), "Should have all stations");
    }

    @Test
    public void constructorWithKDTreeAndRepositoryShouldThrowExceptionForNullTree() {
        KDTree nullTree = null;
        assertThrows(IllegalArgumentException.class,
                () -> new ProximitySearchController(nullTree, stationRepository),
                "Should throw exception for null KDTree");
    }

    @Test
    public void constructorWithKDTreeAndRepositoryShouldThrowExceptionForNullRepository() {
        KDTree tree = kdTreeController.getKDTree();
        StationRepository nullRepo = null;
        assertThrows(IllegalArgumentException.class,
                () -> new ProximitySearchController(tree, nullRepo),
                "Should throw exception for null StationRepository");
    }

    @Test
    public void constructorWithKDTreeControllerShouldCreateController() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        assertNotNull(controller, "Controller should not be null");
        assertNotNull(controller.getKDTree(), "KD-tree should not be null");
        assertEquals(allStations.size(), controller.getStationsCount(), "Should have all stations");
    }

    @Test
    public void constructorWithKDTreeControllerShouldThrowExceptionForNullController() {
        KDTreeController nullController = null;
        assertThrows(IllegalArgumentException.class,
                () -> new ProximitySearchController(nullController, stationRepository),
                "Should throw exception for null KDTreeController");
    }

    @Test
    public void constructorWithKDTreeControllerShouldThrowExceptionForNullRepository() {
        assertThrows(IllegalArgumentException.class,
                () -> new ProximitySearchController(kdTreeController, null),
                "Should throw exception for null StationRepository");
    }

    @Test
    public void constructorWithKDTreeControllerShouldThrowExceptionWhenTreeNotBuilt() {
        KDTreeController emptyController = new KDTreeController(stationRepository);
        assertThrows(IllegalStateException.class,
                () -> new ProximitySearchController(emptyController, stationRepository),
                "Should throw exception when tree is not built");
    }

    // ==================== findNearestStations() Tests (without filters) ====================

    @Test
    public void findNearestStationsShouldReturnList() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        // Use coordinates from CSV (Lisbon: 38.7223, -9.1393)
        List<KDTree.StationDistance> results = controller.findNearestStations(38.7223, -9.1393, 5);
        
        assertNotNull(results, "Results should not be null");
        assertFalse(results.isEmpty(), "Should find at least one station");
    }

    @Test
    public void findNearestStationsShouldReturnCorrectNumberOfResults() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        int n = 10;
        List<KDTree.StationDistance> results = controller.findNearestStations(38.7223, -9.1393, n);
        
        assertTrue(results.size() <= n, "Should return at most n stations");
        assertTrue(results.size() > 0, "Should return at least one station");
    }

    @Test
    public void findNearestStationsShouldReturnResultsSortedByDistance() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        List<KDTree.StationDistance> results = controller.findNearestStations(38.7223, -9.1393, 10);
        
        for (int i = 0; i < results.size() - 1; i++) {
            double distance1 = results.get(i).getDistance();
            double distance2 = results.get(i + 1).getDistance();
            assertTrue(distance1 <= distance2, "Results should be sorted by distance (ascending)");
        }
    }

    @Test
    public void findNearestStationsShouldReturnStationsWithValidDistances() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        List<KDTree.StationDistance> results = controller.findNearestStations(38.7223, -9.1393, 5);
        
        for (KDTree.StationDistance sd : results) {
            assertNotNull(sd.getStation(), "Station should not be null");
            assertTrue(sd.getDistance() >= 0, "Distance should be non-negative");
        }
    }

    @Test
    public void findNearestStationsShouldHandleLargeN() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        // Request more stations than exist
        int n = allStations.size() + 100;
        List<KDTree.StationDistance> results = controller.findNearestStations(38.7223, -9.1393, n);
        
        assertNotNull(results, "Results should not be null");
        assertTrue(results.size() <= allStations.size(), "Should return at most all stations");
    }

    @Test
    public void findNearestStationsShouldHandleNEqualsOne() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        List<KDTree.StationDistance> results = controller.findNearestStations(38.7223, -9.1393, 1);
        
        assertEquals(1, results.size(), "Should return exactly one station");
    }

    @Test
    public void findNearestStationsShouldWorkWithDifferentCoordinates() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        // Test with different coordinates (Madrid: 40.4168, -3.7038)
        List<KDTree.StationDistance> results = controller.findNearestStations(40.4168, -3.7038, 5);
        
        assertNotNull(results, "Results should not be null");
        assertFalse(results.isEmpty(), "Should find stations near Madrid");
    }

    @Test
    public void findNearestStationsShouldThrowExceptionForInvalidLatitude() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        assertThrows(IllegalArgumentException.class,
                () -> controller.findNearestStations(91.0, -9.1393, 5),
                "Should throw exception for latitude > 90");
        
        assertThrows(IllegalArgumentException.class,
                () -> controller.findNearestStations(-91.0, -9.1393, 5),
                "Should throw exception for latitude < -90");
    }

    @Test
    public void findNearestStationsShouldThrowExceptionForInvalidLongitude() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        assertThrows(IllegalArgumentException.class,
                () -> controller.findNearestStations(38.7223, 181.0, 5),
                "Should throw exception for longitude > 180");
        
        assertThrows(IllegalArgumentException.class,
                () -> controller.findNearestStations(38.7223, -181.0, 5),
                "Should throw exception for longitude < -180");
    }

    @Test
    public void findNearestStationsShouldThrowExceptionForInvalidN() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        assertThrows(IllegalArgumentException.class,
                () -> controller.findNearestStations(38.7223, -9.1393, 0),
                "Should throw exception for n = 0");
        
        assertThrows(IllegalArgumentException.class,
                () -> controller.findNearestStations(38.7223, -9.1393, -1),
                "Should throw exception for n < 0");
    }

    @Test
    public void findNearestStationsShouldWorkWithBoundaryLatitude() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        // Test boundary values
        List<KDTree.StationDistance> results1 = controller.findNearestStations(90.0, 0.0, 5);
        List<KDTree.StationDistance> results2 = controller.findNearestStations(-90.0, 0.0, 5);
        
        assertNotNull(results1, "Results should not be null for latitude = 90");
        assertNotNull(results2, "Results should not be null for latitude = -90");
    }

    @Test
    public void findNearestStationsShouldWorkWithBoundaryLongitude() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        // Test boundary values
        List<KDTree.StationDistance> results1 = controller.findNearestStations(0.0, 180.0, 5);
        List<KDTree.StationDistance> results2 = controller.findNearestStations(0.0, -180.0, 5);
        
        assertNotNull(results1, "Results should not be null for longitude = 180");
        assertNotNull(results2, "Results should not be null for longitude = -180");
    }

    // ==================== findNearestStations() Tests (with filters) ====================

    @Test
    public void findNearestStationsWithFiltersShouldReturnFilteredResults() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        SearchFilters filters = new SearchFilters(null, null, Country.PT);
        List<KDTree.StationDistance> results = controller.findNearestStations(38.7223, -9.1393, 10, filters);
        
        assertNotNull(results, "Results should not be null");
        for (KDTree.StationDistance sd : results) {
            assertEquals(Country.PT.toString(), sd.getStation().getCountry(), 
                    "All results should be from Portugal");
        }
    }

    @Test
    public void findNearestStationsWithCityFilterShouldReturnOnlyCities() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        SearchFilters filters = new SearchFilters(true, null, null);
        List<KDTree.StationDistance> results = controller.findNearestStations(38.7223, -9.1393, 10, filters);
        
        assertNotNull(results, "Results should not be null");
        for (KDTree.StationDistance sd : results) {
            assertTrue(sd.getStation().isCity(), "All results should be cities");
        }
    }

    @Test
    public void findNearestStationsWithMainStationFilterShouldReturnOnlyMainStations() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        SearchFilters filters = new SearchFilters(null, true, null);
        List<KDTree.StationDistance> results = controller.findNearestStations(38.7223, -9.1393, 10, filters);
        
        assertNotNull(results, "Results should not be null");
        for (KDTree.StationDistance sd : results) {
            assertTrue(sd.getStation().isMainStation(), "All results should be main stations");
        }
    }

    @Test
    public void findNearestStationsWithMultipleFiltersShouldReturnMatchingResults() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        SearchFilters filters = new SearchFilters(true, true, Country.PT);
        List<KDTree.StationDistance> results = controller.findNearestStations(38.7223, -9.1393, 10, filters);
        
        assertNotNull(results, "Results should not be null");
        for (KDTree.StationDistance sd : results) {
            assertTrue(sd.getStation().isCity(), "Should be a city");
            assertTrue(sd.getStation().isMainStation(), "Should be a main station");
            assertEquals(Country.PT.toString(), sd.getStation().getCountry(), "Should be from Portugal");
        }
    }

    @Test
    public void findNearestStationsWithFiltersShouldReturnEmptyListWhenNoMatches() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        // Use coordinates far from any stations and filter that won't match
        SearchFilters filters = new SearchFilters(false, false, Country.ES);
        List<KDTree.StationDistance> results = controller.findNearestStations(0.0, 0.0, 10, filters);
        
        // May be empty if no stations match the filter
        assertNotNull(results, "Results should not be null (may be empty)");
    }

    @Test
    public void findNearestStationsWithNullFiltersShouldWorkAsNoFilters() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        List<KDTree.StationDistance> results1 = controller.findNearestStations(38.7223, -9.1393, 5, null);
        List<KDTree.StationDistance> results2 = controller.findNearestStations(38.7223, -9.1393, 5);
        
        assertNotNull(results1, "Results with null filters should not be null");
        assertNotNull(results2, "Results without filters should not be null");
        assertEquals(results1.size(), results2.size(), "Results should be same with null filters");
    }

    @Test
    public void findNearestStationsWithEmptyFiltersShouldWorkAsNoFilters() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        SearchFilters emptyFilters = new SearchFilters();
        List<KDTree.StationDistance> results1 = controller.findNearestStations(38.7223, -9.1393, 5, emptyFilters);
        List<KDTree.StationDistance> results2 = controller.findNearestStations(38.7223, -9.1393, 5);
        
        assertNotNull(results1, "Results with empty filters should not be null");
        assertNotNull(results2, "Results without filters should not be null");
        assertEquals(results1.size(), results2.size(), "Results should be same with empty filters");
    }

    @Test
    public void findNearestStationsWithFiltersShouldMaintainDistanceOrder() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        SearchFilters filters = new SearchFilters(null, null, Country.PT);
        List<KDTree.StationDistance> results = controller.findNearestStations(38.7223, -9.1393, 10, filters);
        
        for (int i = 0; i < results.size() - 1; i++) {
            double distance1 = results.get(i).getDistance();
            double distance2 = results.get(i + 1).getDistance();
            assertTrue(distance1 <= distance2, "Filtered results should still be sorted by distance");
        }
    }

    @Test
    public void findNearestStationsWithCountryFilterShouldWorkForDifferentCountries() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        // Test with Spain filter
        SearchFilters filtersES = new SearchFilters(null, null, Country.ES);
        List<KDTree.StationDistance> resultsES = controller.findNearestStations(40.4168, -3.7038, 10, filtersES);
        
        assertNotNull(resultsES, "Results for Spain should not be null");
        for (KDTree.StationDistance sd : resultsES) {
            assertEquals(Country.ES.toString(), sd.getStation().getCountry(), 
                    "All results should be from Spain");
        }
    }

    @Test
    public void findNearestStationsWithCityFilterFalseShouldExcludeCities() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        SearchFilters filters = new SearchFilters(false, null, null);
        List<KDTree.StationDistance> results = controller.findNearestStations(38.7223, -9.1393, 10, filters);
        
        assertNotNull(results, "Results should not be null");
        for (KDTree.StationDistance sd : results) {
            assertFalse(sd.getStation().isCity(), "All results should NOT be cities");
        }
    }

    @Test
    public void findNearestStationsWithMainStationFilterFalseShouldExcludeMainStations() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        SearchFilters filters = new SearchFilters(null, false, null);
        List<KDTree.StationDistance> results = controller.findNearestStations(38.7223, -9.1393, 10, filters);
        
        assertNotNull(results, "Results should not be null");
        for (KDTree.StationDistance sd : results) {
            assertFalse(sd.getStation().isMainStation(), "All results should NOT be main stations");
        }
    }

    // ==================== getTreeStatistics() Tests ====================

    @Test
    public void getTreeStatisticsShouldReturnFormattedString() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        String stats = controller.getTreeStatistics();
        assertNotNull(stats, "Statistics should not be null");
        assertFalse(stats.isEmpty(), "Statistics should not be empty");
        assertTrue(stats.contains("Size"), "Should contain 'Size'");
        assertTrue(stats.contains("nodes"), "Should contain 'nodes'");
        assertTrue(stats.contains("Height"), "Should contain 'Height'");
    }

    @Test
    public void getTreeStatisticsShouldContainValidNumbers() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        String stats = controller.getTreeStatistics();
        KDTree tree = controller.getKDTree();
        
        assertTrue(stats.contains(String.valueOf(tree.size())), 
                "Statistics should contain tree size");
        assertTrue(stats.contains(String.valueOf(tree.height())), 
                "Statistics should contain tree height");
    }

    @Test
    public void getTreeStatisticsShouldBeConsistent() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        String stats1 = controller.getTreeStatistics();
        String stats2 = controller.getTreeStatistics();
        
        assertEquals(stats1, stats2, "Statistics should be consistent");
    }

    // ==================== getKDTree() Tests ====================

    @Test
    public void getKDTreeShouldReturnNonNullTree() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        KDTree tree = controller.getKDTree();
        assertNotNull(tree, "Tree should not be null");
    }

    @Test
    public void getKDTreeShouldReturnSameInstance() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        KDTree tree1 = controller.getKDTree();
        KDTree tree2 = controller.getKDTree();
        
        assertSame(tree1, tree2, "getKDTree() should return same instance");
    }

    @Test
    public void getKDTreeShouldReturnNonEmptyTree() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        KDTree tree = controller.getKDTree();
        assertFalse(tree.isEmpty(), "Tree should not be empty");
        assertTrue(tree.size() > 0, "Tree should have nodes");
    }

    // ==================== getAllStations() Tests ====================

    @Test
    public void getAllStationsShouldReturnList() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        List<Station> stations = controller.getAllStations();
        assertNotNull(stations, "Stations list should not be null");
        assertFalse(stations.isEmpty(), "Stations list should not be empty");
    }

    @Test
    public void getAllStationsShouldReturnAllStations() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        List<Station> stations = controller.getAllStations();
        assertEquals(allStations.size(), stations.size(), 
                "Should return all stations from repository");
    }

    @Test
    public void getAllStationsShouldReturnNewListInstance() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        List<Station> stations1 = controller.getAllStations();
        List<Station> stations2 = controller.getAllStations();
        
        assertNotSame(stations1, stations2, "Should return new list instance each time");
        assertEquals(stations1.size(), stations2.size(), "Lists should have same size");
    }

    @Test
    public void getAllStationsShouldContainValidStations() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        List<Station> stations = controller.getAllStations();
        for (Station station : stations) {
            assertNotNull(station, "Station should not be null");
            assertNotNull(station.getName(), "Station name should not be null");
            assertTrue(station.getLatitude() >= -90 && station.getLatitude() <= 90, 
                    "Latitude should be valid");
            assertTrue(station.getLongitude() >= -180 && station.getLongitude() <= 180, 
                    "Longitude should be valid");
        }
    }

    // ==================== getStationsCount() Tests ====================

    @Test
    public void getStationsCountShouldReturnCorrectCount() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        int count = controller.getStationsCount();
        assertEquals(allStations.size(), count, "Count should match all stations");
    }

    @Test
    public void getStationsCountShouldBeConsistent() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        int count1 = controller.getStationsCount();
        int count2 = controller.getStationsCount();
        
        assertEquals(count1, count2, "Count should be consistent");
    }

    @Test
    public void getStationsCountShouldMatchGetAllStationsSize() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        int count = controller.getStationsCount();
        List<Station> stations = controller.getAllStations();
        
        assertEquals(count, stations.size(), "Count should match list size");
    }

    @Test
    public void getStationsCountShouldBePositive() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        int count = controller.getStationsCount();
        assertTrue(count > 0, "Count should be positive");
    }

    // ==================== Integration Tests ====================

    @Test
    public void controllerShouldWorkWithAllConstructors() {
        // Test constructor with List
        ProximitySearchController controller1 = new ProximitySearchController(allStations);
        assertNotNull(controller1.getKDTree());
        
        // Test constructor with KDTree and List
        KDTree tree = kdTreeController.getKDTree();
        ProximitySearchController controller2 = new ProximitySearchController(tree, allStations);
        assertSame(tree, controller2.getKDTree());
        
        // Test constructor with KDTree and Repository
        ProximitySearchController controller3 = new ProximitySearchController(tree, stationRepository);
        assertSame(tree, controller3.getKDTree());
        
        // Test constructor with KDTreeController
        ProximitySearchController controller4 = new ProximitySearchController(kdTreeController, stationRepository);
        assertNotNull(controller4.getKDTree());
    }

    @Test
    public void findNearestStationsShouldWorkWithAllConstructors() {
        // Test with constructor from List
        ProximitySearchController controller1 = new ProximitySearchController(allStations);
        List<KDTree.StationDistance> results1 = controller1.findNearestStations(38.7223, -9.1393, 5);
        assertNotNull(results1);
        
        // Test with constructor from KDTreeController
        ProximitySearchController controller2 = new ProximitySearchController(kdTreeController, stationRepository);
        List<KDTree.StationDistance> results2 = controller2.findNearestStations(38.7223, -9.1393, 5);
        assertNotNull(results2);
    }

    @Test
    public void findNearestStationsShouldHandleEdgeCases() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        // Test with very small n
        List<KDTree.StationDistance> results1 = controller.findNearestStations(38.7223, -9.1393, 1);
        assertEquals(1, results1.size());
        
        // Test with n larger than available stations
        int largeN = allStations.size() + 50;
        List<KDTree.StationDistance> results2 = controller.findNearestStations(38.7223, -9.1393, largeN);
        assertTrue(results2.size() <= allStations.size());
        
        // Test with filters that may return empty
        SearchFilters strictFilters = new SearchFilters(false, false, Country.ES);
        List<KDTree.StationDistance> results3 = controller.findNearestStations(38.7223, -9.1393, 10, strictFilters);
        assertNotNull(results3); // May be empty but should not be null
    }

    @Test
    public void findNearestStationsShouldReturnConsistentResults() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        List<KDTree.StationDistance> results1 = controller.findNearestStations(38.7223, -9.1393, 5);
        List<KDTree.StationDistance> results2 = controller.findNearestStations(38.7223, -9.1393, 5);
        
        assertEquals(results1.size(), results2.size(), "Results should be consistent");
        if (!results1.isEmpty() && !results2.isEmpty()) {
            assertEquals(results1.get(0).getStation().getName(), 
                    results2.get(0).getStation().getName(), 
                    "Nearest station should be the same");
        }
    }

    @Test
    public void findNearestStationsWithFiltersShouldHandleComplexFilters() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        // Test with all filters combined
        SearchFilters complexFilters = new SearchFilters(true, true, Country.PT);
        List<KDTree.StationDistance> results = controller.findNearestStations(38.7223, -9.1393, 20, complexFilters);
        
        assertNotNull(results, "Results should not be null");
        for (KDTree.StationDistance sd : results) {
            assertTrue(sd.getStation().isCity(), "Should be city");
            assertTrue(sd.getStation().isMainStation(), "Should be main station");
            assertEquals(Country.PT.toString(), sd.getStation().getCountry(), "Should be from Portugal");
        }
    }

    @Test
    public void allMethodsShouldWorkTogether() {
        ProximitySearchController controller = new ProximitySearchController(kdTreeController, stationRepository);
        
        // Test all methods in sequence
        KDTree tree = controller.getKDTree();
        assertNotNull(tree);
        
        List<Station> stations = controller.getAllStations();
        assertNotNull(stations);
        
        int count = controller.getStationsCount();
        assertEquals(stations.size(), count);
        
        String stats = controller.getTreeStatistics();
        assertNotNull(stats);
        
        List<KDTree.StationDistance> results = controller.findNearestStations(38.7223, -9.1393, 5);
        assertNotNull(results);
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

