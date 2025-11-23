package Controller;

import Domain.KDTree;
import Domain.Station;
import Repository.KDTreeRepository;
import Repository.StationRepository;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for KDTreeMetrics class using real CSV data.
 * All data is loaded from CSV file (no manual object creation).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KDTreeMetricsTest {

    private static final String CSV_PATH = "train_station_dataset/train_stations_europe.csv";

    private StationRepository stationRepository;
    private KDTreeRepository kdTreeRepository;

    @BeforeAll
    public void setupRepository() throws Exception {
        // Load stations from CSV
        List<Station> stations = parseStationsFromCsv(CSV_PATH);
        assertFalse(stations.isEmpty(), "CSV must contain at least one station");
        stationRepository = new StationRepository(stations);
        kdTreeRepository = new KDTreeRepository(stationRepository);
    }

    // ==================== Constructor Tests ====================

    @Test
    public void constructorShouldCreateMetricsWithValidValues() {
        // Build tree to get real metrics
        KDTree tree = kdTreeRepository.buildTree();
        assertNotNull(tree, "Tree should not be null");
        
        int[] size = tree.getSize();
        int height = tree.height();
        Map<Integer, Integer> bucketStats = tree.getBucketStats();
        
        KDTreeMetrics metrics = new KDTreeMetrics(size[0], size[1], height, bucketStats);
        assertNotNull(metrics, "Metrics should not be null");
        assertEquals(size[0], metrics.getNumNodes(), "NumNodes should match");
        assertEquals(size[1], metrics.getNumStations(), "NumStations should match");
        assertEquals(height, metrics.getHeight(), "Height should match");
        assertEquals(bucketStats, metrics.getBucketStats(), "BucketStats should match");
    }

    @Test
    public void constructorShouldHandleZeroValues() {
        Map<Integer, Integer> emptyStats = new HashMap<>();
        KDTreeMetrics metrics = new KDTreeMetrics(0, 0, 0, emptyStats);
        assertNotNull(metrics, "Metrics should not be null");
        assertEquals(0, metrics.getNumNodes(), "NumNodes should be 0");
        assertEquals(0, metrics.getNumStations(), "NumStations should be 0");
        assertEquals(0, metrics.getHeight(), "Height should be 0");
        assertNotNull(metrics.getBucketStats(), "BucketStats should not be null");
        assertTrue(metrics.getBucketStats().isEmpty(), "BucketStats should be empty");
    }

    // ==================== Getter Tests ====================

    @Test
    public void getNumNodesShouldReturnCorrectValue() {
        KDTree tree = kdTreeRepository.buildTree();
        int[] size = tree.getSize();
        int height = tree.height();
        Map<Integer, Integer> bucketStats = tree.getBucketStats();
        
        KDTreeMetrics metrics = new KDTreeMetrics(size[0], size[1], height, bucketStats);
        assertEquals(size[0], metrics.getNumNodes(), "getNumNodes() should return correct value");
    }

    @Test
    public void getNumStationsShouldReturnCorrectValue() {
        KDTree tree = kdTreeRepository.buildTree();
        int[] size = tree.getSize();
        int height = tree.height();
        Map<Integer, Integer> bucketStats = tree.getBucketStats();
        
        KDTreeMetrics metrics = new KDTreeMetrics(size[0], size[1], height, bucketStats);
        assertEquals(size[1], metrics.getNumStations(), "getNumStations() should return correct value");
    }

    @Test
    public void getHeightShouldReturnCorrectValue() {
        KDTree tree = kdTreeRepository.buildTree();
        int[] size = tree.getSize();
        int height = tree.height();
        Map<Integer, Integer> bucketStats = tree.getBucketStats();
        
        KDTreeMetrics metrics = new KDTreeMetrics(size[0], size[1], height, bucketStats);
        assertEquals(height, metrics.getHeight(), "getHeight() should return correct value");
    }

    @Test
    public void getBucketStatsShouldReturnCorrectValue() {
        KDTree tree = kdTreeRepository.buildTree();
        int[] size = tree.getSize();
        int height = tree.height();
        Map<Integer, Integer> bucketStats = tree.getBucketStats();
        
        KDTreeMetrics metrics = new KDTreeMetrics(size[0], size[1], height, bucketStats);
        assertEquals(bucketStats, metrics.getBucketStats(), "getBucketStats() should return correct value");
    }

    // ==================== toString() Tests ====================

    @Test
    public void toStringShouldReturnFormattedString() {
        KDTree tree = kdTreeRepository.buildTree();
        int[] size = tree.getSize();
        int height = tree.height();
        Map<Integer, Integer> bucketStats = tree.getBucketStats();
        
        KDTreeMetrics metrics = new KDTreeMetrics(size[0], size[1], height, bucketStats);
        String str = metrics.toString();
        
        assertNotNull(str, "toString() should not return null");
        assertFalse(str.isEmpty(), "toString() should not be empty");
    }

    @Test
    public void toStringShouldContainKDTreeInformation() {
        KDTree tree = kdTreeRepository.buildTree();
        int[] size = tree.getSize();
        int height = tree.height();
        Map<Integer, Integer> bucketStats = tree.getBucketStats();
        
        KDTreeMetrics metrics = new KDTreeMetrics(size[0], size[1], height, bucketStats);
        String str = metrics.toString();
        
        assertTrue(str.contains("KD-Tree"), "Should contain 'KD-Tree'");
        assertTrue(str.contains("Data Structure"), "Should contain data structure information");
    }

    @Test
    public void toStringShouldContainTreeStatistics() {
        KDTree tree = kdTreeRepository.buildTree();
        int[] size = tree.getSize();
        int height = tree.height();
        Map<Integer, Integer> bucketStats = tree.getBucketStats();
        
        KDTreeMetrics metrics = new KDTreeMetrics(size[0], size[1], height, bucketStats);
        String str = metrics.toString();
        
        assertTrue(str.contains("Total Number of Stations"), "Should contain station count");
        assertTrue(str.contains("Total Number of Nodes"), "Should contain node count");
        assertTrue(str.contains("Tree Height"), "Should contain height");
        assertTrue(str.contains(String.valueOf(size[1])), "Should contain actual station count");
        assertTrue(str.contains(String.valueOf(size[0])), "Should contain actual node count");
        assertTrue(str.contains(String.valueOf(height)), "Should contain actual height");
    }

    @Test
    public void toStringShouldHandleEmptyBucketStats() {
        Map<Integer, Integer> emptyStats = new HashMap<>();
        KDTreeMetrics metrics = new KDTreeMetrics(0, 0, 0, emptyStats);
        String str = metrics.toString();
        
        assertNotNull(str, "toString() should not return null");
        assertTrue(str.contains("No buckets found"), 
                "Should indicate no buckets when bucketStats is empty");
    }

    @Test
    public void toStringShouldContainBucketDistribution() {
        KDTree tree = kdTreeRepository.buildTree();
        int[] size = tree.getSize();
        int height = tree.height();
        Map<Integer, Integer> bucketStats = tree.getBucketStats();
        
        KDTreeMetrics metrics = new KDTreeMetrics(size[0], size[1], height, bucketStats);
        String str = metrics.toString();
        
        assertTrue(str.contains("Bucket Size Distribution"), 
                "Should contain bucket distribution header");
        
        if (!bucketStats.isEmpty()) {
            assertTrue(str.contains("Bucket size"), 
                    "Should contain bucket size information");
        }
    }

    @Test
    public void toStringShouldSortBucketStatsByCountThenSize() {
        // Create metrics with multiple bucket sizes to test sorting
        Map<Integer, Integer> bucketStats = new HashMap<>();
        bucketStats.put(1, 10);  // 10 nodes with bucket size 1
        bucketStats.put(2, 5);   // 5 nodes with bucket size 2
        bucketStats.put(3, 10);  // 10 nodes with bucket size 3 (same count as size 1)
        
        KDTreeMetrics metrics = new KDTreeMetrics(25, 30, 5, bucketStats);
        String str = metrics.toString();
        
        assertNotNull(str, "toString() should not return null");
        assertTrue(str.contains("Bucket size"), "Should contain bucket information");
        
        // Verify sorting: should sort by count (descending), then by size (descending)
        // So bucket size 3 (count 10) should come before bucket size 1 (count 10)
        // because 3 > 1 when counts are equal
        int index3 = str.indexOf("Bucket size 3");
        int index1 = str.indexOf("Bucket size 1");
        int index2 = str.indexOf("Bucket size 2");
        
        // All should be present
        assertTrue(index3 >= 0, "Should contain bucket size 3");
        assertTrue(index1 >= 0, "Should contain bucket size 1");
        assertTrue(index2 >= 0, "Should contain bucket size 2");
        
        // Verify sorting order: count 10 should come before count 5
        // When counts are equal (10 and 10), larger size (3) should come before smaller (1)
        if (index3 >= 0 && index1 >= 0) {
            // Both have count 10, so size 3 should come before size 1
            assertTrue(index3 < index1, 
                    "Bucket size 3 (count 10) should come before bucket size 1 (count 10) when counts are equal");
        }
        if (index1 >= 0 && index2 >= 0) {
            // Count 10 should come before count 5
            assertTrue(index1 < index2 || index3 < index2, 
                    "Buckets with count 10 should come before bucket with count 5");
        }
    }

    @Test
    public void toStringShouldHandleSortingWhenCountsAreDifferent() {
        // Test sorting when counts are different (should sort by count descending)
        Map<Integer, Integer> bucketStats = new HashMap<>();
        bucketStats.put(1, 15);  // Highest count
        bucketStats.put(2, 10);  // Medium count
        bucketStats.put(3, 5);   // Lowest count
        
        KDTreeMetrics metrics = new KDTreeMetrics(30, 35, 5, bucketStats);
        String str = metrics.toString();
        
        assertNotNull(str, "toString() should not return null");
        
        int index1 = str.indexOf("Bucket size 1");
        int index2 = str.indexOf("Bucket size 2");
        int index3 = str.indexOf("Bucket size 3");
        
        // All should be present
        assertTrue(index1 >= 0, "Should contain bucket size 1");
        assertTrue(index2 >= 0, "Should contain bucket size 2");
        assertTrue(index3 >= 0, "Should contain bucket size 3");
        
        // Count 15 should come first, then 10, then 5
        assertTrue(index1 < index2, "Bucket with count 15 should come before count 10");
        assertTrue(index2 < index3, "Bucket with count 10 should come before count 5");
    }

    @Test
    public void toStringShouldHandleSortingWhenCountsAreEqual() {
        // Test sorting when counts are equal (should sort by size descending)
        Map<Integer, Integer> bucketStats = new HashMap<>();
        bucketStats.put(5, 10);  // Size 5, count 10
        bucketStats.put(3, 10);  // Size 3, count 10
        bucketStats.put(1, 10);  // Size 1, count 10
        
        KDTreeMetrics metrics = new KDTreeMetrics(30, 35, 5, bucketStats);
        String str = metrics.toString();
        
        assertNotNull(str, "toString() should not return null");
        
        int index5 = str.indexOf("Bucket size 5");
        int index3 = str.indexOf("Bucket size 3");
        int index1 = str.indexOf("Bucket size 1");
        
        // All should be present
        assertTrue(index5 >= 0, "Should contain bucket size 5");
        assertTrue(index3 >= 0, "Should contain bucket size 3");
        assertTrue(index1 >= 0, "Should contain bucket size 1");
        
        // When counts are equal, larger sizes should come first
        assertTrue(index5 < index3, "Size 5 should come before size 3 when counts are equal");
        assertTrue(index3 < index1, "Size 3 should come before size 1 when counts are equal");
    }

    @Test
    public void toStringShouldHandleComplexSortingScenario() {
        // Test complex sorting with various combinations
        Map<Integer, Integer> bucketStats = new HashMap<>();
        bucketStats.put(1, 20);  // Highest count, smallest size
        bucketStats.put(2, 15);  // Medium count, medium size
        bucketStats.put(3, 20);  // Highest count, larger size (same count as size 1)
        bucketStats.put(4, 10);  // Lower count, larger size
        bucketStats.put(5, 15);  // Medium count, largest size (same count as size 2)
        
        KDTreeMetrics metrics = new KDTreeMetrics(80, 90, 6, bucketStats);
        String str = metrics.toString();
        
        assertNotNull(str, "toString() should not return null");
        
        // Verify all are present
        assertTrue(str.contains("Bucket size 1"), "Should contain bucket size 1");
        assertTrue(str.contains("Bucket size 2"), "Should contain bucket size 2");
        assertTrue(str.contains("Bucket size 3"), "Should contain bucket size 3");
        assertTrue(str.contains("Bucket size 4"), "Should contain bucket size 4");
        assertTrue(str.contains("Bucket size 5"), "Should contain bucket size 5");
        
        // Verify counts are present
        assertTrue(str.contains("20"), "Should contain count 20");
        assertTrue(str.contains("15"), "Should contain count 15");
        assertTrue(str.contains("10"), "Should contain count 10");
    }

    @Test
    public void toStringShouldHandleSingleBucketSize() {
        Map<Integer, Integer> singleBucket = new HashMap<>();
        singleBucket.put(1, 5);
        
        KDTreeMetrics metrics = new KDTreeMetrics(5, 5, 3, singleBucket);
        String str = metrics.toString();
        
        assertNotNull(str, "toString() should not return null");
        assertTrue(str.contains("Bucket size 1"), "Should contain bucket size 1");
        assertTrue(str.contains("5"), "Should contain count 5");
    }

    @Test
    public void toStringShouldHandleMultipleBucketSizesWithDifferentCounts() {
        Map<Integer, Integer> bucketStats = new HashMap<>();
        bucketStats.put(1, 20);  // Most common
        bucketStats.put(2, 10);  // Medium
        bucketStats.put(3, 5);   // Least common
        
        KDTreeMetrics metrics = new KDTreeMetrics(35, 40, 6, bucketStats);
        String str = metrics.toString();
        
        assertNotNull(str, "toString() should not return null");
        
        // Verify all bucket sizes are present
        assertTrue(str.contains("Bucket size 1"), "Should contain bucket size 1");
        assertTrue(str.contains("Bucket size 2"), "Should contain bucket size 2");
        assertTrue(str.contains("Bucket size 3"), "Should contain bucket size 3");
        
        // Verify counts are present
        assertTrue(str.contains("20"), "Should contain count 20");
        assertTrue(str.contains("10"), "Should contain count 10");
        assertTrue(str.contains("5"), "Should contain count 5");
    }

    @Test
    public void toStringShouldHandleBucketStatsWithEqualCounts() {
        // Test sorting when counts are equal (should sort by size descending)
        Map<Integer, Integer> bucketStats = new HashMap<>();
        bucketStats.put(1, 5);
        bucketStats.put(2, 5);
        bucketStats.put(3, 5);
        
        KDTreeMetrics metrics = new KDTreeMetrics(15, 20, 4, bucketStats);
        String str = metrics.toString();
        
        assertNotNull(str, "toString() should not return null");
        
        // All should be present
        assertTrue(str.contains("Bucket size 1"), "Should contain bucket size 1");
        assertTrue(str.contains("Bucket size 2"), "Should contain bucket size 2");
        assertTrue(str.contains("Bucket size 3"), "Should contain bucket size 3");
    }

    @Test
    public void toStringShouldFormatAllSections() {
        KDTree tree = kdTreeRepository.buildTree();
        int[] size = tree.getSize();
        int height = tree.height();
        Map<Integer, Integer> bucketStats = tree.getBucketStats();
        
        KDTreeMetrics metrics = new KDTreeMetrics(size[0], size[1], height, bucketStats);
        String str = metrics.toString();
        
        // Verify all major sections are present
        assertTrue(str.contains("KD-Tree Data Structure Information"), 
                "Should contain header");
        assertTrue(str.contains("Data Structure:"), "Should contain data structure section");
        assertTrue(str.contains("Tree Statistics:"), "Should contain statistics section");
        assertTrue(str.contains("Bucket Size Distribution:"), 
                "Should contain bucket distribution section");
    }

    @Test
    public void toStringShouldHandleLargeBucketStats() {
        // Build tree which may have many bucket sizes
        KDTree tree = kdTreeRepository.buildTree();
        int[] size = tree.getSize();
        int height = tree.height();
        Map<Integer, Integer> bucketStats = tree.getBucketStats();
        
        KDTreeMetrics metrics = new KDTreeMetrics(size[0], size[1], height, bucketStats);
        String str = metrics.toString();
        
        assertNotNull(str, "toString() should not return null");
        // Should handle large number of bucket sizes without issues
        assertTrue(str.length() > 0, "String should have content");
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

