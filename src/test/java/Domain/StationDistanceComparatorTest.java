package Domain;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StationDistanceComparator class (USEI10).
 * Tests the comparator that sorts by distance (ASC) and station name (DESC).
 * Uses real CSV data to test with actual stations.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StationDistanceComparatorTest {

    private static final String CSV_FILE_PATH = "train_station_dataset/train_stations_europe.csv";
    private static List<Station> cachedStations;
    
    private StationDistanceComparator comparator;

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
    }

    @BeforeEach
    void setUp() {
        comparator = new StationDistanceComparator();
    }

    @Test
    void compareShouldSortByDistanceASC() {
        // Create StationDistance objects with different distances
        Station station1 = cachedStations.get(0);
        Station station2 = cachedStations.get(1);
        
        // Use Haversine distance from a target point (Lisbon: 38.7223, -9.1393)
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        
        double dist1 = KDTree.haversineDistance(targetLat, targetLon, 
                station1.getLatitude(), station1.getLongitude());
        double dist2 = KDTree.haversineDistance(targetLat, targetLon, 
                station2.getLatitude(), station2.getLongitude());
        
        KDTree.StationDistance sd1 = new KDTree.StationDistance(station1, dist1);
        KDTree.StationDistance sd2 = new KDTree.StationDistance(station2, dist2);
        
        if (dist1 < dist2) {
            assertTrue(comparator.compare(sd1, sd2) < 0, 
                    "Station with smaller distance should come first");
        } else if (dist1 > dist2) {
            assertTrue(comparator.compare(sd1, sd2) > 0, 
                    "Station with larger distance should come after");
        }
    }

    @Test
    void compareShouldSortByNameDESCWhenDistanceEqual() {
        // Create two stations with same distance (or very close)
        Station station1 = cachedStations.get(0);
        Station station2 = cachedStations.get(1);
        
        // Use same distance for both
        double sameDistance = 10.0;
        
        KDTree.StationDistance sd1 = new KDTree.StationDistance(station1, sameDistance);
        KDTree.StationDistance sd2 = new KDTree.StationDistance(station2, sameDistance);
        
        String name1 = station1.getName();
        String name2 = station2.getName();
        
        int result = comparator.compare(sd1, sd2);
        
        if (name1.equals(name2)) {
            assertEquals(0, result, "Same name and distance should return 0");
        } else {
            // When distances are equal, should sort by name DESC
            int nameCompare = name2.compareTo(name1); // Reverse order (DESC)
            assertEquals(nameCompare, result, 
                    "When distances equal, should sort by name DESC");
        }
    }

    @Test
    void compareShouldReturnZeroForSameStationAndDistance() {
        Station station = cachedStations.get(0);
        double distance = 10.0;
        
        KDTree.StationDistance sd1 = new KDTree.StationDistance(station, distance);
        KDTree.StationDistance sd2 = new KDTree.StationDistance(station, distance);
        
        assertEquals(0, comparator.compare(sd1, sd2), 
                "Same station and distance should return 0");
    }

    @Test
    void compareShouldPrioritizeDistanceOverName() {
        // Create stations where name order would be different from distance order
        Station station1 = cachedStations.get(0);
        Station station2 = cachedStations.get(1);
        
        // Station1 closer, but name comes after station2 alphabetically
        double dist1 = 5.0;  // Closer
        double dist2 = 10.0; // Farther
        
        KDTree.StationDistance sd1 = new KDTree.StationDistance(station1, dist1);
        KDTree.StationDistance sd2 = new KDTree.StationDistance(station2, dist2);
        
        // Distance should take priority (ASC), so sd1 should come before sd2
        assertTrue(comparator.compare(sd1, sd2) < 0, 
                "Distance should take priority over name");
    }

    @Test
    void compareShouldHandleMultipleStationsCorrectly() {
        // Test with multiple stations to verify sorting order
        List<KDTree.StationDistance> list = new ArrayList<>();
        
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        
        // Add first 10 stations with their distances
        for (int i = 0; i < Math.min(10, cachedStations.size()); i++) {
            Station station = cachedStations.get(i);
            double distance = KDTree.haversineDistance(targetLat, targetLon, 
                    station.getLatitude(), station.getLongitude());
            list.add(new KDTree.StationDistance(station, distance));
        }
        
        // Sort using comparator
        list.sort(comparator);
        
        // Verify sorted by distance (ASC)
        for (int i = 0; i < list.size() - 1; i++) {
            double dist1 = list.get(i).getDistance();
            double dist2 = list.get(i + 1).getDistance();
            
            if (dist1 == dist2) {
                // If distances equal, verify name DESC
                String name1 = list.get(i).getStation().getName();
                String name2 = list.get(i + 1).getStation().getName();
                assertTrue(name1.compareTo(name2) >= 0, 
                        "When distances equal, names should be DESC: " + name1 + " >= " + name2);
            } else {
                assertTrue(dist1 < dist2, 
                        "Distances should be ASC: " + dist1 + " < " + dist2);
            }
        }
    }

    @Test
    void compareShouldBeConsistent() {
        // Test that compare(a, b) == -compare(b, a)
        Station station1 = cachedStations.get(0);
        Station station2 = cachedStations.get(1);
        
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        
        double dist1 = KDTree.haversineDistance(targetLat, targetLon, 
                station1.getLatitude(), station1.getLongitude());
        double dist2 = KDTree.haversineDistance(targetLat, targetLon, 
                station2.getLatitude(), station2.getLongitude());
        
        KDTree.StationDistance sd1 = new KDTree.StationDistance(station1, dist1);
        KDTree.StationDistance sd2 = new KDTree.StationDistance(station2, dist2);
        
        int result1 = comparator.compare(sd1, sd2);
        int result2 = comparator.compare(sd2, sd1);
        
        assertEquals(-result1, result2, 
                "compare(a, b) should equal -compare(b, a)");
    }

    @Test
    void compareShouldBeTransitive() {
        // Test transitivity: if a < b and b < c, then a < c
        Station station1 = cachedStations.get(0);
        Station station2 = cachedStations.get(1);
        Station station3 = cachedStations.get(2);
        
        double targetLat = 38.7223;
        double targetLon = -9.1393;
        
        double dist1 = KDTree.haversineDistance(targetLat, targetLon, 
                station1.getLatitude(), station1.getLongitude());
        double dist2 = KDTree.haversineDistance(targetLat, targetLon, 
                station2.getLatitude(), station2.getLongitude());
        double dist3 = KDTree.haversineDistance(targetLat, targetLon, 
                station3.getLatitude(), station3.getLongitude());
        
        KDTree.StationDistance sd1 = new KDTree.StationDistance(station1, dist1);
        KDTree.StationDistance sd2 = new KDTree.StationDistance(station2, dist2);
        KDTree.StationDistance sd3 = new KDTree.StationDistance(station3, dist3);
        
        int cmp12 = comparator.compare(sd1, sd2);
        int cmp23 = comparator.compare(sd2, sd3);
        int cmp13 = comparator.compare(sd1, sd3);
        
        if (cmp12 < 0 && cmp23 < 0) {
            assertTrue(cmp13 < 0, 
                    "Transitivity: if a < b and b < c, then a < c");
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Loads stations from CSV file (64k lines).
     * Called once via @BeforeAll.
     */
    private static List<Station> loadStationsFromCSV() {
        List<Domain.Station> stations = new ArrayList<>();
        
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
}

