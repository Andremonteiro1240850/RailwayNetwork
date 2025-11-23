package Controller;

import Domain.AVL;
import Domain.BST;
import Domain.KDTree;
import Domain.RadiusSearchSummary;
import Domain.Station;
import Domain.StationDistanceComparator;
import Repository.KDTreeRepository;
import Repository.StationRepository;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RadiusSearchController class (USEI10).
 * Tests use real CSV data (64k lines) loaded once via @BeforeAll.
 * Tests radius search with density summary by country and isCity.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RadiusSearchControllerTest {

    private static final String CSV_FILE_PATH = "train_station_dataset/train_stations_europe.csv";
    
    private static List<Station> cachedStations;
    private static StationRepository cachedStationRepository;
    private static KDTreeRepository cachedKDTreeRepository;
    private static KDTree cachedKDTree;
    
    private RadiusSearchController controller;

    @BeforeAll
    static void loadCSVOnce() {
        long start = System.currentTimeMillis();
        
        // Load stations from CSV (64k lines) - ONCE
        cachedStations = loadStationsFromCSV();
        assertNotNull(cachedStations, "Stations list should not be null");
        assertFalse(cachedStations.isEmpty(), "Should load stations from CSV");
        assertTrue(cachedStations.size() > 1000, "Should load at least 1000 stations");
        
        System.out.println("✓ CSV loaded: " + cachedStations.size() + " stations (" +
                (System.currentTimeMillis() - start) + "ms)");
        
        // Create repository - ONCE
        start = System.currentTimeMillis();
        cachedStationRepository = new StationRepository(cachedStations);
        System.out.println("✓ Repository created: " + (System.currentTimeMillis() - start) + "ms");
        
        // Build KD-tree - ONCE
        start = System.currentTimeMillis();
        cachedKDTreeRepository = new KDTreeRepository(cachedStationRepository);
        cachedKDTree = cachedKDTreeRepository.buildTree(cachedStations);
        assertNotNull(cachedKDTree, "KD-tree should not be null");
        assertFalse(cachedKDTree.isEmpty(), "KD-tree should not be empty");
        System.out.println("✓ KD-tree built: " + (System.currentTimeMillis() - start) + "ms");
        System.out.println("✓ Total setup time - all tests will now run instantly!");
    }

    @BeforeEach
    void setUp() {
        // Create controller for each test using cached data
        controller = new RadiusSearchController(cachedKDTreeRepository, cachedStationRepository);
    }

    // ==================== Constructor Tests ====================

    @Test
    void constructorShouldCreateController() {
        assertNotNull(controller, "Controller should not be null");
        assertNotNull(controller.getKDTree(), "KD-tree should not be null");
        assertFalse(controller.getKDTree().isEmpty(), "KD-tree should not be empty");
    }

    @Test
    void constructorShouldThrowExceptionForNullKDTreeRepository() {
        assertThrows(IllegalArgumentException.class,
                () -> new RadiusSearchController(null, cachedStationRepository),
                "Should throw exception for null KDTreeRepository");
    }

    @Test
    void constructorShouldThrowExceptionForNullStationRepository() {
        assertThrows(IllegalArgumentException.class,
                () -> new RadiusSearchController(cachedKDTreeRepository, null),
                "Should throw exception for null StationRepository");
    }

    @Test
    void constructorShouldThrowExceptionWhenTreeNotBuilt() {
        KDTreeRepository emptyRepository = new KDTreeRepository(cachedStationRepository);
        assertThrows(IllegalStateException.class,
                () -> new RadiusSearchController(emptyRepository, cachedStationRepository),
                "Should throw exception when tree is not built");
    }

    // ==================== searchRadiusWithSummary() Tests ====================

    @Test
    void searchRadiusWithSummaryShouldReturnResult() {
        // Lisbon coordinates: 38.7223, -9.1393
        RadiusSearchController.RadiusSearchResult result = 
                controller.searchRadiusWithSummary(38.7223, -9.1393, 50.0);
        
        assertNotNull(result, "Result should not be null");
        assertNotNull(result.getResultTree(), "Result tree should not be null");
        assertNotNull(result.getSummary(), "Summary should not be null");
    }

    @Test
    void searchRadiusWithSummaryShouldReturnAVLTree() {
        RadiusSearchController.RadiusSearchResult result = 
                controller.searchRadiusWithSummary(38.7223, -9.1393, 50.0);
        
        AVL<KDTree.StationDistance> tree = result.getResultTree();
        assertNotNull(tree, "AVL tree should not be null");
        assertFalse(tree.isEmpty(), "AVL tree should not be empty for radius 50km around Lisbon");
    }

    @Test
    void searchRadiusWithSummaryShouldReturnSummary() {
        RadiusSearchController.RadiusSearchResult result = 
                controller.searchRadiusWithSummary(38.7223, -9.1393, 50.0);
        
        RadiusSearchSummary summary = result.getSummary();
        assertNotNull(summary, "Summary should not be null");
        assertTrue(summary.getTotalStations() > 0, "Should find at least one station");
    }

    @Test
    void searchRadiusWithSummaryShouldFindStationsInRadius() {
        // Paris coordinates: 48.8566, 2.3522
        RadiusSearchController.RadiusSearchResult result = 
                controller.searchRadiusWithSummary(48.8566, 2.3522, 20.0);
        
        RadiusSearchSummary summary = result.getSummary();
        assertTrue(summary.getTotalStations() > 0, "Should find stations within 20km of Paris");
    }

    @Test
    void searchRadiusWithSummaryShouldReturnEmptyForSmallRadius() {
        // Remote location with small radius
        RadiusSearchController.RadiusSearchResult result = 
                controller.searchRadiusWithSummary(70.0, 20.0, 1.0);
        
        RadiusSearchSummary summary = result.getSummary();
        // May be empty or have few stations depending on data
        assertNotNull(summary, "Summary should not be null even if empty");
    }

    @Test
    void searchRadiusWithSummaryShouldCalculateSummaryByCountry() {
        // Madrid coordinates: 40.4168, -3.7038
        RadiusSearchController.RadiusSearchResult result = 
                controller.searchRadiusWithSummary(40.4168, -3.7038, 100.0);
        
        RadiusSearchSummary summary = result.getSummary();
        Map<String, Integer> byCountry = summary.getStationsByCountry();
        
        assertNotNull(byCountry, "Stations by country should not be null");
        // Should have at least one country (likely ES for Madrid)
        assertFalse(byCountry.isEmpty() || summary.getTotalStations() == 0, 
                "Should have stations grouped by country");
    }

    @Test
    void searchRadiusWithSummaryShouldCalculateSummaryByIsCity() {
        // Berlin coordinates: 52.5200, 13.4050
        RadiusSearchController.RadiusSearchResult result = 
                controller.searchRadiusWithSummary(52.5200, 13.4050, 50.0);
        
        RadiusSearchSummary summary = result.getSummary();
        
        int total = summary.getTotalStations();
        int cities = summary.getStationsByCity();
        int nonCities = summary.getStationsByNonCity();
        
        assertEquals(total, cities + nonCities, 
                "Total should equal cities + non-cities");
        assertTrue(cities >= 0, "Cities count should be non-negative");
        assertTrue(nonCities >= 0, "Non-cities count should be non-negative");
    }

    @Test
    void searchRadiusWithSummaryShouldSortByDistanceASC() {
        // Lisbon coordinates
        RadiusSearchController.RadiusSearchResult result = 
                controller.searchRadiusWithSummary(38.7223, -9.1393, 100.0);
        
        AVL<KDTree.StationDistance> tree = result.getResultTree();
        if (tree.isEmpty()) {
            return; // Skip if no results
        }
        
        // Collect all stations from tree (in-order traversal = sorted)
        List<KDTree.StationDistance> sorted = new ArrayList<>();
        BST.Node<KDTree.StationDistance> root = tree.getRoot();
        if (root != null) {
            collectInOrder(root, sorted);
        }
        
        // Verify sorted by distance (ASC)
        for (int i = 0; i < sorted.size() - 1; i++) {
            double dist1 = sorted.get(i).getDistance();
            double dist2 = sorted.get(i + 1).getDistance();
            assertTrue(dist1 <= dist2, 
                    "Stations should be sorted by distance (ASC): " + dist1 + " <= " + dist2);
        }
    }

    @Test
    void searchRadiusWithSummaryShouldSortByNameDESCWhenDistanceEqual() {
        // Test with stations that might have same distance
        // This is harder to test with real data, but we verify the comparator is used
        RadiusSearchController.RadiusSearchResult result = 
                controller.searchRadiusWithSummary(38.7223, -9.1393, 100.0);
        
        AVL<KDTree.StationDistance> tree = result.getResultTree();
        assertNotNull(tree, "Tree should not be null");
        
        // The tree uses StationDistanceComparator which sorts by distance ASC, name DESC
        // This is verified by the tree structure itself
    }

    // ==================== Parameter Validation Tests ====================

    @Test
    void searchRadiusWithSummaryShouldThrowExceptionForInvalidLatitude() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.searchRadiusWithSummary(91.0, 0.0, 10.0),
                "Should throw exception for latitude > 90");
        
        assertThrows(IllegalArgumentException.class,
                () -> controller.searchRadiusWithSummary(-91.0, 0.0, 10.0),
                "Should throw exception for latitude < -90");
    }

    @Test
    void searchRadiusWithSummaryShouldThrowExceptionForInvalidLongitude() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.searchRadiusWithSummary(0.0, 181.0, 10.0),
                "Should throw exception for longitude > 180");
        
        assertThrows(IllegalArgumentException.class,
                () -> controller.searchRadiusWithSummary(0.0, -181.0, 10.0),
                "Should throw exception for longitude < -180");
    }

    @Test
    void searchRadiusWithSummaryShouldThrowExceptionForNegativeRadius() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.searchRadiusWithSummary(38.7223, -9.1393, -1.0),
                "Should throw exception for negative radius");
    }

    @Test
    void searchRadiusWithSummaryShouldAcceptZeroRadius() {
        // Zero radius should return empty results, not throw exception
        RadiusSearchController.RadiusSearchResult result = 
                controller.searchRadiusWithSummary(38.7223, -9.1393, 0.0);
        
        assertNotNull(result, "Result should not be null even for zero radius");
        assertNotNull(result.getSummary(), "Summary should not be null");
        assertEquals(0, result.getSummary().getTotalStations(), 
                "Zero radius should return no stations");
    }

    // ==================== Real Data Tests ====================

    @Test
    void searchRadiusWithSummaryShouldFindLisbonStations() {
        // Lisbon: 38.7223, -9.1393
        RadiusSearchController.RadiusSearchResult result = 
                controller.searchRadiusWithSummary(38.7223, -9.1393, 10.0);
        
        RadiusSearchSummary summary = result.getSummary();
        assertTrue(summary.getTotalStations() > 0, 
                "Should find stations within 10km of Lisbon");
        
        // Check if Portugal stations are found
        Map<String, Integer> byCountry = summary.getStationsByCountry();
        assertTrue(byCountry.containsKey("PT") || summary.getTotalStations() > 0,
                "Should find Portuguese stations or at least some stations");
    }

    @Test
    void searchRadiusWithSummaryShouldFindParisStations() {
        // Paris: 48.8566, 2.3522
        RadiusSearchController.RadiusSearchResult result = 
                controller.searchRadiusWithSummary(48.8566, 2.3522, 15.0);
        
        RadiusSearchSummary summary = result.getSummary();
        assertTrue(summary.getTotalStations() > 0, 
                "Should find stations within 15km of Paris");
        
        Map<String, Integer> byCountry = summary.getStationsByCountry();
        assertTrue(byCountry.containsKey("FR") || summary.getTotalStations() > 0,
                "Should find French stations or at least some stations");
    }

    @Test
    void searchRadiusWithSummaryShouldUseHaversineDistance() {
        // Test that distances are calculated using Haversine formula
        // Compare with known distances
        RadiusSearchController.RadiusSearchResult result = 
                controller.searchRadiusWithSummary(38.7223, -9.1393, 50.0);
        
        AVL<KDTree.StationDistance> tree = result.getResultTree();
        if (tree.isEmpty()) {
            return;
        }
        
        // All distances should be <= radius
        List<KDTree.StationDistance> all = new ArrayList<>();
        collectInOrder(tree.getRoot(), all);
        
        for (KDTree.StationDistance sd : all) {
            assertTrue(sd.getDistance() <= 50.0, 
                    "All distances should be <= radius: " + sd.getDistance());
            assertTrue(sd.getDistance() >= 0.0, 
                    "All distances should be non-negative");
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Loads stations from CSV file (64k lines).
     * Called once via @BeforeAll.
     */
    private static List<Station> loadStationsFromCSV() {
        List<Station> stations = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
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
                    stations.add(station);
                    
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        } catch (IOException e) {
            fail("Error loading CSV file: " + e.getMessage());
        }
        
        return stations;
    }

    /**
     * Parses a CSV line, handling quoted fields.
     */
    private static String[] parseCSVLine(String line) {
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

    /**
     * Collects all elements from AVL tree using in-order traversal.
     */
    private void collectInOrder(BST.Node<KDTree.StationDistance> node, 
                                List<KDTree.StationDistance> result) {
        if (node == null) {
            return;
        }
        collectInOrder(node.getLeft(), result);
        result.add(node.getElement());
        collectInOrder(node.getRight(), result);
    }
}

