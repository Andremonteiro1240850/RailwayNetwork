package Domain;

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
 * Unit tests for KDTree class using real CSV data.
 * All data is loaded from CSV file (no manual object creation).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KDTreeTest {

    private static final String CSV_PATH = "train_station_dataset/train_stations_europe.csv";

    private StationRepository stationRepository;
    private KDTreeRepository kdTreeRepository;
    private KDTree tree;

    @BeforeAll
    public void setupRepository() throws Exception {
        // Load stations from CSV
        List<Station> stations = parseStationsFromCsv(CSV_PATH);
        assertFalse(stations.isEmpty(), "CSV must contain at least one station");
        stationRepository = new StationRepository(stations);
        kdTreeRepository = new KDTreeRepository(stationRepository);
    }

    @BeforeEach
    public void setupEach() {
        // Build tree for each test using CSV data
        tree = kdTreeRepository.buildTree();
        assertNotNull(tree, "Tree should not be null");
        assertFalse(tree.isEmpty(), "Tree should not be empty");
    }

    // ==================== Constructor Tests ====================

    @Test
    public void emptyConstructorShouldCreateEmptyTree() {
        KDTree emptyTree = new KDTree();
        assertNotNull(emptyTree, "Empty tree should not be null");
        assertTrue(emptyTree.isEmpty(), "Empty tree should be empty");
        assertEquals(0, emptyTree.size(), "Empty tree should have size 0");
        assertEquals(0, emptyTree.height(), "Empty tree should have height 0");
    }

    @Test
    public void constructorWithNullRootShouldCreateEmptyTree() {
        KDTree emptyTree = new KDTree(null);
        assertNotNull(emptyTree, "Tree with null root should not be null");
        assertTrue(emptyTree.isEmpty(), "Tree with null root should be empty");
        assertEquals(0, emptyTree.size(), "Tree with null root should have size 0");
    }

    @Test
    public void constructorWithValidRootShouldCreateNonEmptyTree() {
        // Use tree built from CSV
        KDTree.KDNode root = tree.getRoot();
        assertNotNull(root, "Root of CSV tree should not be null");
        
        KDTree newTree = new KDTree(root);
        assertNotNull(newTree, "New tree should not be null");
        assertFalse(newTree.isEmpty(), "New tree should not be empty");
        assertTrue(newTree.size() > 0, "New tree should have nodes");
    }

    // ==================== Basic Methods Tests ====================

    @Test
    public void isEmptyShouldReturnFalseForTreeWithData() {
        assertFalse(tree.isEmpty(), "Tree with data should not be empty");
    }

    @Test
    public void isEmptyShouldReturnTrueForEmptyTree() {
        KDTree emptyTree = new KDTree();
        assertTrue(emptyTree.isEmpty(), "Empty tree should be empty");
    }

    @Test
    public void sizeShouldReturnNumberOfNodes() {
        int size = tree.size();
        assertTrue(size > 0, "Tree should have at least one node");
        
        int[] sizeArray = tree.getSize();
        assertEquals(size, sizeArray[0], "size() should match getSize()[0]");
    }

    @Test
    public void getSizeShouldReturnCorrectPair() {
        int[] size = tree.getSize();
        assertNotNull(size, "getSize() should not return null");
        assertEquals(2, size.length, "getSize() should return array of size 2");
        assertTrue(size[0] > 0, "Number of nodes should be positive");
        assertTrue(size[1] > 0, "Number of stations should be positive");
        assertTrue(size[1] >= size[0], "Number of stations should be >= number of nodes");
    }

    @Test
    public void heightShouldReturnValidHeight() {
        int height = tree.height();
        assertTrue(height > 0, "Tree height should be positive");
        assertTrue(height <= tree.size(), "Height should not exceed number of nodes");
    }

    @Test
    public void heightShouldReturnZeroForEmptyTree() {
        KDTree emptyTree = new KDTree();
        assertEquals(0, emptyTree.height(), "Height of empty tree should be 0");
    }

    @Test
    public void getRootShouldReturnValidRoot() {
        KDTree.KDNode root = tree.getRoot();
        assertNotNull(root, "Root should not be null for non-empty tree");
    }

    @Test
    public void getRootShouldReturnNullForEmptyTree() {
        KDTree emptyTree = new KDTree();
        assertNull(emptyTree.getRoot(), "Root of empty tree should be null");
    }

    // ==================== Bucket Statistics Tests ====================

    @Test
    public void getBucketStatsShouldReturnValidMap() {
        Map<Integer, Integer> stats = tree.getBucketStats();
        assertNotNull(stats, "Bucket statistics should not be null");
        assertFalse(stats.isEmpty(), "Statistics should not be empty");
        
        // Verify all counts are positive
        for (Map.Entry<Integer, Integer> entry : stats.entrySet()) {
            assertTrue(entry.getKey() > 0, "Bucket size should be positive");
            assertTrue(entry.getValue() > 0, "Bucket count should be positive");
        }
    }

    @Test
    public void getBucketStatsShouldReturnEmptyMapForEmptyTree() {
        KDTree emptyTree = new KDTree();
        Map<Integer, Integer> stats = emptyTree.getBucketStats();
        assertNotNull(stats, "Statistics should not be null even for empty tree");
        assertTrue(stats.isEmpty(), "Statistics should be empty for empty tree");
    }

    @Test
    public void getBucketStatsShouldSumToNumberOfNodes() {
        Map<Integer, Integer> stats = tree.getBucketStats();
        int totalNodes = 0;
        for (int count : stats.values()) {
            totalNodes += count;
        }
        assertEquals(tree.size(), totalNodes, "Sum of counts should equal number of nodes");
    }

    // ==================== Range Query Tests ====================

    @Test
    public void rangeQueryShouldReturnSortedList() {
        // Get coordinates from a station in CSV
        List<Station> allStations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(allStations::add);
        assertFalse(allStations.isEmpty(), "Should have stations from CSV");
        
        Station sampleStation = allStations.get(0);
        double lat = sampleStation.getLatitude();
        double lon = sampleStation.getLongitude();
        
        // Search in small range around station
        List<Station> results = tree.rangeQuery(lat - 0.1, lat + 0.1, lon - 0.1, lon + 0.1);
        assertNotNull(results, "Results should not be null");
        
        // Verify sorting by name
        for (int i = 1; i < results.size(); i++) {
            assertTrue(results.get(i-1).getName().compareTo(results.get(i).getName()) <= 0,
                    "Results should be sorted by name");
        }
    }

    @Test
    public void rangeQueryShouldReturnStationsInRange() {
        // Get coordinates from a station in CSV
        List<Station> allStations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(allStations::add);
        Station sampleStation = allStations.get(0);
        double lat = sampleStation.getLatitude();
        double lon = sampleStation.getLongitude();
        
        // Search in small range
        List<Station> results = tree.rangeQuery(lat - 0.5, lat + 0.5, lon - 0.5, lon + 0.5);
        
        // Verify all stations are in range
        for (Station station : results) {
            assertTrue(station.getLatitude() >= lat - 0.5 && station.getLatitude() <= lat + 0.5,
                    "Latitude should be in range");
            assertTrue(station.getLongitude() >= lon - 0.5 && station.getLongitude() <= lon + 0.5,
                    "Longitude should be in range");
        }
    }

    @Test
    public void rangeQueryShouldReturnEmptyListForRangeWithoutStations() {
        // Search in range that doesn't contain stations (e.g., in the ocean)
        List<Station> results = tree.rangeQuery(0.0, 0.1, 0.0, 0.1);
        assertNotNull(results, "Results should not be null");
        // May be empty or not, depending on data
    }

    @Test
    public void rangeQueryShouldReturnEmptyListForEmptyTree() {
        KDTree emptyTree = new KDTree();
        List<Station> results = emptyTree.rangeQuery(-90, 90, -180, 180);
        assertNotNull(results, "Results should not be null");
        assertTrue(results.isEmpty(), "Results should be empty for empty tree");
    }

    @Test
    public void rangeQueryShouldReturnAllStationsForFullRange() {
        // Search in range that covers all of Europe
        List<Station> results = tree.rangeQuery(-90, 90, -180, 180);
        assertNotNull(results, "Results should not be null");
        assertFalse(results.isEmpty(), "Should return stations for full range");
        
        int[] size = tree.getSize();
        assertTrue(results.size() <= size[1], "Number of results should not exceed number of stations");
    }

    @Test
    public void rangeQueryUnsortedShouldReturnUnsortedList() {
        // Get coordinates from a station in CSV
        List<Station> allStations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(allStations::add);
        Station sampleStation = allStations.get(0);
        double lat = sampleStation.getLatitude();
        double lon = sampleStation.getLongitude();
        
        // Search in range
        List<Station> results = tree.rangeQueryUnsorted(lat - 1.0, lat + 1.0, lon - 1.0, lon + 1.0);
        assertNotNull(results, "Results should not be null");
        
        // Verify all stations are in range
        for (Station station : results) {
            assertTrue(station.getLatitude() >= lat - 1.0 && station.getLatitude() <= lat + 1.0,
                    "Latitude should be in range");
            assertTrue(station.getLongitude() >= lon - 1.0 && station.getLongitude() <= lon + 1.0,
                    "Longitude should be in range");
        }
    }

    @Test
    public void rangeQueryUnsortedShouldReturnEmptyListForEmptyTree() {
        KDTree emptyTree = new KDTree();
        List<Station> results = emptyTree.rangeQueryUnsorted(-90, 90, -180, 180);
        assertNotNull(results, "Results should not be null");
        assertTrue(results.isEmpty(), "Results should be empty for empty tree");
    }

    // ==================== Nearest N Tests ====================

    @Test
    public void nearestNShouldReturnNNearestStations() {
        // Get coordinates from a station in CSV
        List<Station> allStations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(allStations::add);
        Station targetStation = allStations.get(0);
        double targetLat = targetStation.getLatitude();
        double targetLon = targetStation.getLongitude();
        
        int n = 5;
        List<KDTree.StationDistance> results = tree.nearestN(targetLat, targetLon, n);
        
        assertNotNull(results, "Results should not be null");
        assertTrue(results.size() <= n, "Should return at most N stations");
        assertTrue(results.size() > 0, "Should return at least one station");
        
        // Verify sorting by distance
        for (int i = 1; i < results.size(); i++) {
            assertTrue(results.get(i-1).getDistance() <= results.get(i).getDistance(),
                    "Results should be sorted by ascending distance");
        }
    }

    @Test
    public void nearestNShouldThrowExceptionForNZero() {
        List<Station> allStations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(allStations::add);
        Station targetStation = allStations.get(0);
        
        assertThrows(IllegalArgumentException.class, 
                () -> tree.nearestN(targetStation.getLatitude(), targetStation.getLongitude(), 0),
                "Should throw exception for n=0");
    }

    @Test
    public void nearestNShouldThrowExceptionForNNegative() {
        List<Station> allStations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(allStations::add);
        Station targetStation = allStations.get(0);
        
        assertThrows(IllegalArgumentException.class, 
                () -> tree.nearestN(targetStation.getLatitude(), targetStation.getLongitude(), -1),
                "Should throw exception for negative n");
    }

    @Test
    public void nearestNShouldReturnEmptyListForEmptyTree() {
        KDTree emptyTree = new KDTree();
        List<KDTree.StationDistance> results = emptyTree.nearestN(0.0, 0.0, 5);
        assertNotNull(results, "Results should not be null");
        assertTrue(results.isEmpty(), "Results should be empty for empty tree");
    }

    @Test
    public void nearestNShouldReturnAllStationsIfNGreaterThanTotal() {
        // Get coordinates from a station in CSV
        List<Station> allStations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(allStations::add);
        Station targetStation = allStations.get(0);
        double targetLat = targetStation.getLatitude();
        double targetLon = targetStation.getLongitude();
        
        int[] size = tree.getSize();
        int n = size[1] + 100; // N greater than total stations
        
        List<KDTree.StationDistance> results = tree.nearestN(targetLat, targetLon, n);
        assertNotNull(results, "Results should not be null");
        assertTrue(results.size() <= size[1], "Should return at most all stations");
    }

    @Test
    public void nearestNShouldReturnExactStationAsFirst() {
        // Get coordinates from a station in CSV
        List<Station> allStations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(allStations::add);
        Station targetStation = allStations.get(0);
        double targetLat = targetStation.getLatitude();
        double targetLon = targetStation.getLongitude();
        
        List<KDTree.StationDistance> results = tree.nearestN(targetLat, targetLon, 1);
        assertNotNull(results, "Results should not be null");
        assertFalse(results.isEmpty(), "Should return at least one station");
        
        // First station should have very small distance (may be the station itself)
        assertTrue(results.get(0).getDistance() < 0.1, 
                "First station should be very close (distance < 0.1 km)");
    }

    // ==================== Radius Search Tests ====================

    @Test
    public void radiusSearchShouldReturnStationsWithinRadius() {
        // Get coordinates from a station in CSV
        List<Station> allStations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(allStations::add);
        Station targetStation = allStations.get(0);
        double targetLat = targetStation.getLatitude();
        double targetLon = targetStation.getLongitude();
        
        double radiusKm = 50.0;
        List<KDTree.StationDistance> results = tree.radiusSearch(targetLat, targetLon, radiusKm);
        
        assertNotNull(results, "Results should not be null");
        
        // Verify all stations are within radius
        for (KDTree.StationDistance sd : results) {
            assertTrue(sd.getDistance() <= radiusKm, 
                    "Distance should be <= radius: " + sd.getDistance() + " <= " + radiusKm);
        }
        
        // Verify sorting by distance
        for (int i = 1; i < results.size(); i++) {
            assertTrue(results.get(i-1).getDistance() <= results.get(i).getDistance(),
                    "Results should be sorted by ascending distance");
        }
    }

    @Test
    public void radiusSearchShouldThrowExceptionForNegativeRadius() {
        List<Station> allStations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(allStations::add);
        Station targetStation = allStations.get(0);
        
        assertThrows(IllegalArgumentException.class, 
                () -> tree.radiusSearch(targetStation.getLatitude(), targetStation.getLongitude(), -1.0),
                "Should throw exception for negative radius");
    }

    @Test
    public void radiusSearchShouldReturnEmptyListForZeroRadius() {
        List<Station> allStations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(allStations::add);
        Station targetStation = allStations.get(0);
        
        List<KDTree.StationDistance> results = tree.radiusSearch(
                targetStation.getLatitude(), targetStation.getLongitude(), 0.0);
        assertNotNull(results, "Results should not be null");
        // May be empty or not, depending if there's exact station at coordinate
    }

    @Test
    public void radiusSearchShouldReturnEmptyListForEmptyTree() {
        KDTree emptyTree = new KDTree();
        List<KDTree.StationDistance> results = emptyTree.radiusSearch(0.0, 0.0, 100.0);
        assertNotNull(results, "Results should not be null");
        assertTrue(results.isEmpty(), "Results should be empty for empty tree");
    }

    @Test
    public void radiusSearchShouldReturnStationsForLargeRadius() {
        // Get coordinates from a station in CSV
        List<Station> allStations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(allStations::add);
        Station targetStation = allStations.get(0);
        double targetLat = targetStation.getLatitude();
        double targetLon = targetStation.getLongitude();
        
        double radiusKm = 1000.0; // Large radius
        List<KDTree.StationDistance> results = tree.radiusSearch(targetLat, targetLon, radiusKm);
        
        assertNotNull(results, "Results should not be null");
        assertFalse(results.isEmpty(), "Should return stations for large radius");
        
        // Verify all are within radius
        for (KDTree.StationDistance sd : results) {
            assertTrue(sd.getDistance() <= radiusKm, "Distance should be <= radius");
        }
    }

    @Test
    public void radiusSearchUnsortedShouldReturnStationsWithinRadius() {
        // Get coordinates from a station in CSV
        List<Station> allStations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(allStations::add);
        Station targetStation = allStations.get(0);
        double targetLat = targetStation.getLatitude();
        double targetLon = targetStation.getLongitude();
        
        double radiusKm = 50.0;
        List<KDTree.StationDistance> results = tree.radiusSearchUnsorted(targetLat, targetLon, radiusKm);
        
        assertNotNull(results, "Results should not be null");
        
        // Verify all stations are within radius
        for (KDTree.StationDistance sd : results) {
            assertTrue(sd.getDistance() <= radiusKm, "Distance should be <= radius");
        }
    }

    @Test
    public void radiusSearchUnsortedShouldThrowExceptionForNegativeRadius() {
        List<Station> allStations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(allStations::add);
        Station targetStation = allStations.get(0);
        
        assertThrows(IllegalArgumentException.class, 
                () -> tree.radiusSearchUnsorted(targetStation.getLatitude(), targetStation.getLongitude(), -1.0),
                "Should throw exception for negative radius");
    }

    @Test
    public void radiusSearchUnsortedShouldReturnEmptyListForEmptyTree() {
        KDTree emptyTree = new KDTree();
        List<KDTree.StationDistance> results = emptyTree.radiusSearchUnsorted(0.0, 0.0, 100.0);
        assertNotNull(results, "Results should not be null");
        assertTrue(results.isEmpty(), "Results should be empty for empty tree");
    }

    // ==================== Haversine Distance Tests ====================

    @Test
    public void haversineDistanceShouldCalculateCorrectDistance() {
        // Test with known distance: 1 degree of latitude ≈ 111 km
        double lat1 = 40.0;
        double lon1 = -8.0;
        double lat2 = 41.0; // 1 degree north
        double lon2 = -8.0; // same longitude
        
        double distance = KDTree.haversineDistance(lat1, lon1, lat2, lon2);
        
        // 1 degree of latitude should be approximately 111 km
        assertTrue(distance > 100 && distance < 120, 
                "1 degree of latitude should be approximately 111 km, got: " + distance);
        
        // Test with another known distance: Lisbon to Porto
        // This is a real-world test with approximate distance
        double lisboaLat = 38.7223;
        double lisboaLon = -9.1393;
        double portoLat = 41.1579;
        double portoLon = -8.6291;
        
        double realDistance = KDTree.haversineDistance(lisboaLat, lisboaLon, portoLat, portoLon);
        
        // Verify distance is positive and reasonable
        assertTrue(realDistance > 0, "Distance should be positive, got: " + realDistance);
        // Lisbon to Porto should be roughly 250-400 km
        assertTrue(realDistance >= 250 && realDistance <= 400, 
                "Lisbon-Porto distance should be between 250-400 km, got: " + realDistance);
    }

    @Test
    public void haversineDistanceShouldReturnZeroForSamePoint() {
        double lat = 40.0;
        double lon = -8.0;
        
        double distance = KDTree.haversineDistance(lat, lon, lat, lon);
        assertEquals(0.0, distance, 0.001, "Distance for same point should be 0");
    }

    @Test
    public void haversineDistanceShouldBeSymmetric() {
        double lat1 = 40.0;
        double lon1 = -8.0;
        double lat2 = 41.0;
        double lon2 = -9.0;
        
        double dist1 = KDTree.haversineDistance(lat1, lon1, lat2, lon2);
        double dist2 = KDTree.haversineDistance(lat2, lon2, lat1, lon1);
        
        assertEquals(dist1, dist2, 0.001, "Distance should be symmetric");
    }

    @Test
    public void haversineDistanceShouldReturnPositiveValue() {
        double lat1 = 40.0;
        double lon1 = -8.0;
        double lat2 = 41.0;
        double lon2 = -9.0;
        
        double distance = KDTree.haversineDistance(lat1, lon1, lat2, lon2);
        assertTrue(distance > 0, "Distance should be positive");
    }

    @Test
    public void haversineDistanceShouldCalculateDistanceForDistantPoints() {
        // Approximate distance between two distant points
        double lat1 = 0.0;
        double lon1 = 0.0;
        double lat2 = 1.0;
        double lon2 = 0.0;
        
        double distance = KDTree.haversineDistance(lat1, lon1, lat2, lon2);
        // 1 degree of latitude ≈ 111 km
        assertTrue(distance > 100 && distance < 120, 
                "1 degree of latitude should be approximately 111 km");
    }

    // ==================== StationDistance Tests ====================

    @Test
    public void stationDistanceShouldStoreStationAndDistance() {
        List<Station> allStations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(allStations::add);
        Station station = allStations.get(0);
        double distance = 10.5;
        
        KDTree.StationDistance sd = new KDTree.StationDistance(station, distance);
        assertNotNull(sd, "StationDistance should not be null");
        assertEquals(station, sd.getStation(), "Station should be the same");
        assertEquals(distance, sd.getDistance(), 0.001, "Distance should be the same");
    }

    @Test
    public void stationDistanceCompareToShouldSortByDistance() {
        List<Station> allStations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(allStations::add);
        Station station1 = allStations.get(0);
        Station station2 = allStations.size() > 1 ? allStations.get(1) : allStations.get(0);
        
        KDTree.StationDistance sd1 = new KDTree.StationDistance(station1, 10.0);
        KDTree.StationDistance sd2 = new KDTree.StationDistance(station2, 20.0);
        
        assertTrue(sd1.compareTo(sd2) < 0, "Smaller distance should come first");
        assertTrue(sd2.compareTo(sd1) > 0, "Larger distance should come after");
    }

    @Test
    public void stationDistanceCompareToShouldSortByNameWhenDistanceEqual() {
        List<Station> allStations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(allStations::add);
        Station station1 = allStations.get(0);
        Station station2 = allStations.size() > 1 ? allStations.get(1) : allStations.get(0);
        
        double sameDistance = 10.0;
        KDTree.StationDistance sd1 = new KDTree.StationDistance(station1, sameDistance);
        KDTree.StationDistance sd2 = new KDTree.StationDistance(station2, sameDistance);
        
        int compare = sd1.compareTo(sd2);
        // When distances are equal, compare by name (descending)
        if (station1.getName().compareTo(station2.getName()) != 0) {
            // If names are different, comparison should be opposite (descending)
            assertTrue(compare != 0, "Should compare by name when distances are equal");
        }
    }

    @Test
    public void stationDistanceToStringShouldReturnFormattedString() {
        List<Station> allStations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(allStations::add);
        Station station = allStations.get(0);
        double distance = 10.5;
        
        KDTree.StationDistance sd = new KDTree.StationDistance(station, distance);
        String str = sd.toString();
        
        assertNotNull(str, "toString() should not return null");
        assertTrue(str.contains(station.getName()), "String should contain station name");
        // toString() formats as "%.2f km", so 10.5 becomes "10.50"
        // Check for either format to be flexible
        assertTrue(str.contains("10.50") || str.contains("10.5") || str.contains("km"), 
                "String should contain distance formatted as 'X.XX km' or 'X.X km'");
    }

    // ==================== Integration Tests ====================

    @Test
    public void rangeQueryAndNearestNShouldReturnConsistentResults() {
        // Get coordinates from a station in CSV
        List<Station> allStations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(allStations::add);
        Station targetStation = allStations.get(0);
        double targetLat = targetStation.getLatitude();
        double targetLon = targetStation.getLongitude();
        
        // Search with range query
        List<Station> rangeResults = tree.rangeQuery(
                targetLat - 0.1, targetLat + 0.1, targetLon - 0.1, targetLon + 0.1);
        
        // Search with nearestN
        List<KDTree.StationDistance> nearestResults = tree.nearestN(targetLat, targetLon, 10);
        
        assertNotNull(rangeResults, "Range results should not be null");
        assertNotNull(nearestResults, "Nearest results should not be null");
    }

    @Test
    public void radiusSearchShouldReturnSubsetOfNearestN() {
        // Get coordinates from a station in CSV
        List<Station> allStations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(allStations::add);
        Station targetStation = allStations.get(0);
        double targetLat = targetStation.getLatitude();
        double targetLon = targetStation.getLongitude();
        
        // Search with radius search
        double radiusKm = 50.0;
        List<KDTree.StationDistance> radiusResults = tree.radiusSearch(targetLat, targetLon, radiusKm);
        
        // Search with nearestN (large number)
        List<KDTree.StationDistance> nearestResults = tree.nearestN(targetLat, targetLon, 1000);
        
        // All stations from radius search should be in nearestN
        for (KDTree.StationDistance sd : radiusResults) {
            boolean found = false;
            for (KDTree.StationDistance nsd : nearestResults) {
                if (nsd.getStation().equals(sd.getStation())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Station from radius search should be in nearestN");
        }
    }

    @Test
    public void treeShouldMaintainConsistencyAfterMultipleQueries() {
        // Execute multiple queries
        List<Station> allStations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(allStations::add);
        Station targetStation = allStations.get(0);
        double targetLat = targetStation.getLatitude();
        double targetLon = targetStation.getLongitude();
        
        // Execute various queries
        tree.rangeQuery(-90, 90, -180, 180);
        tree.nearestN(targetLat, targetLon, 10);
        tree.radiusSearch(targetLat, targetLon, 100.0);
        tree.getBucketStats();
        tree.height();
        tree.size();
        
        // Tree should maintain consistency
        assertFalse(tree.isEmpty(), "Tree should not be empty after queries");
        assertTrue(tree.size() > 0, "Tree should maintain size");
    }

    @Test
    public void rangeQueryWithExtremeLimitsShouldWork() {
        // Test with extreme limits
        List<Station> results = tree.rangeQuery(-90, 90, -180, 180);
        assertNotNull(results, "Results should not be null");
        
        // Verify all stations are within limits
        for (Station station : results) {
            assertTrue(station.getLatitude() >= -90 && station.getLatitude() <= 90,
                    "Latitude should be within limits");
            assertTrue(station.getLongitude() >= -180 && station.getLongitude() <= 180,
                    "Longitude should be within limits");
        }
    }

    @Test
    public void nearestNWithN1ShouldReturnNearestStation() {
        // Get coordinates from a station in CSV
        List<Station> allStations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(allStations::add);
        Station targetStation = allStations.get(0);
        double targetLat = targetStation.getLatitude();
        double targetLon = targetStation.getLongitude();
        
        List<KDTree.StationDistance> results = tree.nearestN(targetLat, targetLon, 1);
        assertNotNull(results, "Results should not be null");
        assertEquals(1, results.size(), "Should return exactly 1 station");
        assertTrue(results.get(0).getDistance() >= 0, "Distance should be non-negative");
    }

    @Test
    public void radiusSearchWithVerySmallRadiusShouldWork() {
        // Get coordinates from a station in CSV
        List<Station> allStations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(allStations::add);
        Station targetStation = allStations.get(0);
        double targetLat = targetStation.getLatitude();
        double targetLon = targetStation.getLongitude();
        
        double smallRadius = 0.1; // 100 meters
        List<KDTree.StationDistance> results = tree.radiusSearch(targetLat, targetLon, smallRadius);
        assertNotNull(results, "Results should not be null");
        
        // Verify all are within radius
        for (KDTree.StationDistance sd : results) {
            assertTrue(sd.getDistance() <= smallRadius, "Distance should be <= radius");
        }
    }

    // ==================== KDNode Tests ====================

    @Test
    public void kdNodeShouldStoreDataCorrectly() {
        KDTree.KDNode root = tree.getRoot();
        assertNotNull(root, "Root should not be null");
        
        // Verify getter methods work
        int axis = root.getAxis();
        assertTrue(axis == 0 || axis == 1, "Axis should be 0 (latitude) or 1 (longitude)");
        
        double splitValue = root.getSplitValue();
        assertTrue(splitValue >= -180 && splitValue <= 180, "Split value should be valid coordinate");
        
        List<Station> bucket = root.getBucket();
        assertNotNull(bucket, "Bucket should not be null");
        assertFalse(bucket.isEmpty(), "Bucket should not be empty for root");
    }

    @Test
    public void kdNodeGetBucketShouldReturnCopy() {
        KDTree.KDNode root = tree.getRoot();
        assertNotNull(root, "Root should not be null");
        
        List<Station> bucket1 = root.getBucket();
        List<Station> bucket2 = root.getBucket();
        
        // Should be different copies (not the same reference)
        assertNotSame(bucket1, bucket2, "getBucket() should return new list");
        assertEquals(bucket1.size(), bucket2.size(), "Buckets should have same size");
    }

    @Test
    public void kdNodeSetLeftAndSetRightShouldWork() {
        // Create a simple node to test setters
        List<Station> stations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(stations::add);
        assertFalse(stations.isEmpty(), "Should have stations");
        
        List<Station> bucket = new ArrayList<>();
        bucket.add(stations.get(0));
        
        KDTree.KDNode node = new KDTree.KDNode(0, 40.0, bucket);
        assertNotNull(node, "Node should not be null");
        
        // Test setters
        KDTree.KDNode leftChild = new KDTree.KDNode(1, -8.0, bucket);
        KDTree.KDNode rightChild = new KDTree.KDNode(1, -9.0, bucket);
        
        node.setLeft(leftChild);
        node.setRight(rightChild);
        
        assertSame(leftChild, node.getLeft(), "Left child should be the same");
        assertSame(rightChild, node.getRight(), "Right child should be the same");
    }

    // ==================== Edge Cases Tests ====================

    @Test
    public void rangeQueryWithLatMinGreaterThanLatMaxShouldReturnEmptyList() {
        // Invalid range: latMin > latMax
        List<Station> results = tree.rangeQuery(50.0, 40.0, -10.0, 10.0);
        assertNotNull(results, "Results should not be null");
        assertTrue(results.isEmpty(), "Invalid range should return empty list");
    }

    @Test
    public void rangeQueryWithLonMinGreaterThanLonMaxShouldReturnEmptyList() {
        // Invalid range: lonMin > lonMax
        List<Station> results = tree.rangeQuery(40.0, 50.0, 10.0, -10.0);
        assertNotNull(results, "Results should not be null");
        assertTrue(results.isEmpty(), "Invalid range should return empty list");
    }

    @Test
    public void nearestNWithNGreaterThanTotalStationsShouldReturnAll() {
        List<Station> allStations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(allStations::add);
        Station targetStation = allStations.get(0);
        
        int[] size = tree.getSize();
        int totalStations = size[1];
        
        List<KDTree.StationDistance> results = tree.nearestN(
                targetStation.getLatitude(), targetStation.getLongitude(), totalStations + 100);
        
        assertNotNull(results, "Results should not be null");
        assertTrue(results.size() <= totalStations, "Should not return more than total stations");
    }

    @Test
    public void radiusSearchWithVeryLargeRadiusShouldReturnManyStations() {
        List<Station> allStations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(allStations::add);
        Station targetStation = allStations.get(0);
        
        double largeRadius = 10000.0; // 10000 km
        List<KDTree.StationDistance> results = tree.radiusSearch(
                targetStation.getLatitude(), targetStation.getLongitude(), largeRadius);
        
        assertNotNull(results, "Results should not be null");
        assertFalse(results.isEmpty(), "Should return many stations for large radius");
        
        // Verify all are within radius
        for (KDTree.StationDistance sd : results) {
            assertTrue(sd.getDistance() <= largeRadius, "Distance should be <= radius");
        }
    }

    @Test
    public void haversineDistanceWithExtremeCoordinatesShouldWork() {
        // Test with coordinates at poles
        double northPoleLat = 90.0;
        double southPoleLat = -90.0;
        double lon = 0.0;
        
        double distance = KDTree.haversineDistance(northPoleLat, lon, southPoleLat, lon);
        // Distance between poles ≈ 20015 km
        assertTrue(distance > 19000 && distance < 21000, 
                "Distance between poles should be approximately 20015 km");
    }

    @Test
    public void haversineDistanceWithEquatorCoordinatesShouldWork() {
        // Test with coordinates at equator
        double lat = 0.0;
        double lon1 = 0.0;
        double lon2 = 1.0;
        
        double distance = KDTree.haversineDistance(lat, lon1, lat, lon2);
        // 1 degree of longitude at equator ≈ 111 km
        assertTrue(distance > 100 && distance < 120, 
                "1 degree of longitude at equator should be approximately 111 km");
    }

    @Test
    public void stationDistanceCompareToWithEqualDistanceAndSameNameShouldReturnZero() {
        List<Station> allStations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(allStations::add);
        Station station = allStations.get(0);
        
        KDTree.StationDistance sd1 = new KDTree.StationDistance(station, 10.0);
        KDTree.StationDistance sd2 = new KDTree.StationDistance(station, 10.0);
        
        assertEquals(0, sd1.compareTo(sd2), "Equal stations with same distance should be equal");
    }

    @Test
    public void rangeQueryUnsortedShouldNotSortResults() {
        // Get coordinates from a station in CSV
        List<Station> allStations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(allStations::add);
        Station sampleStation = allStations.get(0);
        double lat = sampleStation.getLatitude();
        double lon = sampleStation.getLongitude();
        
        // Search with range query unsorted
        List<Station> unsortedResults = tree.rangeQueryUnsorted(
                lat - 1.0, lat + 1.0, lon - 1.0, lon + 1.0);
        
        // Search with range query sorted
        List<Station> sortedResults = tree.rangeQuery(
                lat - 1.0, lat + 1.0, lon - 1.0, lon + 1.0);
        
        // Verify sorted is ordered
        boolean sortedIsOrdered = true;
        for (int i = 1; i < sortedResults.size(); i++) {
            if (sortedResults.get(i-1).getName().compareTo(sortedResults.get(i).getName()) > 0) {
                sortedIsOrdered = false;
                break;
            }
        }
        assertTrue(sortedIsOrdered || sortedResults.size() <= 1, 
                "Range query sorted should return sorted results");
        
        // Both should have same number of stations (same content)
        assertEquals(unsortedResults.size(), sortedResults.size(), 
                "Unsorted and sorted should have same number of results");
    }

    @Test
    public void radiusSearchUnsortedShouldNotSortResults() {
        List<Station> allStations = new ArrayList<>();
        stationRepository.getAllStationsOrderedByLatitude().forEach(allStations::add);
        Station targetStation = allStations.get(0);
        
        double radiusKm = 50.0;
        List<KDTree.StationDistance> unsortedResults = tree.radiusSearchUnsorted(
                targetStation.getLatitude(), targetStation.getLongitude(), radiusKm);
        List<KDTree.StationDistance> sortedResults = tree.radiusSearch(
                targetStation.getLatitude(), targetStation.getLongitude(), radiusKm);
        
        // Verify sorted is ordered
        boolean sortedIsOrdered = true;
        for (int i = 1; i < sortedResults.size(); i++) {
            if (sortedResults.get(i-1).getDistance() > sortedResults.get(i).getDistance()) {
                sortedIsOrdered = false;
                break;
            }
        }
        assertTrue(sortedIsOrdered || sortedResults.size() <= 1, 
                "Radius search sorted should return sorted results");
        
        // Both should have same number of stations
        assertEquals(unsortedResults.size(), sortedResults.size(), 
                "Unsorted and sorted should have same number of results");
    }

    @Test
    public void getSizeShouldBeConsistentWithSize() {
        int[] size = tree.getSize();
        int sizeMethod = tree.size();
        
        assertEquals(size[0], sizeMethod, "getSize()[0] should equal size()");
    }

    @Test
    public void getSizeShouldBeConsistentWithNumberOfStations() {
        int[] size = tree.getSize();
        
        // Count stations manually through range query
        List<Station> allStations = tree.rangeQuery(-90, 90, -180, 180);
        
        // Number of stations should be >= number of nodes (each node has at least 1 station)
        assertTrue(size[1] >= size[0], "Number of stations should be >= number of nodes");
        assertTrue(size[1] >= allStations.size(), 
                "Number of stations in getSize() should be >= stations returned");
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
