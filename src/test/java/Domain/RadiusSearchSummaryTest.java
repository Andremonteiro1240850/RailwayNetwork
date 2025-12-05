package Domain;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RadiusSearchSummary class (USEI10).
 * Tests the summary statistics aggregation by country and isCity.
 */
class RadiusSearchSummaryTest {

    @Test
    void constructorShouldCreateSummary() {
        Map<String, Integer> byCountry = new HashMap<>();
        byCountry.put("PT", 5);
        byCountry.put("ES", 3);
        
        RadiusSearchSummary summary = new RadiusSearchSummary(byCountry, 4, 4, 8);
        
        assertNotNull(summary, "Summary should not be null");
        assertEquals(8, summary.getTotalStations(), "Total should be 8");
        assertEquals(4, summary.getStationsByCity(), "Cities should be 4");
        assertEquals(4, summary.getStationsByNonCity(), "Non-cities should be 4");
    }

    @Test
    void getStationsByCountryShouldReturnCopy() {
        Map<String, Integer> original = new HashMap<>();
        original.put("PT", 5);
        original.put("ES", 3);
        
        RadiusSearchSummary summary = new RadiusSearchSummary(original, 4, 4, 8);
        Map<String, Integer> returned = summary.getStationsByCountry();
        
        // Modify returned map - should not affect original
        returned.put("FR", 2);
        
        Map<String, Integer> originalAfter = summary.getStationsByCountry();
        assertFalse(originalAfter.containsKey("FR"), 
                "Modifying returned map should not affect internal state");
    }

    @Test
    void getStationsByCountryShouldReturnAllCountries() {
        Map<String, Integer> byCountry = new HashMap<>();
        byCountry.put("PT", 5);
        byCountry.put("ES", 3);
        byCountry.put("FR", 2);
        
        RadiusSearchSummary summary = new RadiusSearchSummary(byCountry, 6, 4, 10);
        Map<String, Integer> returned = summary.getStationsByCountry();
        
        assertEquals(3, returned.size(), "Should return all 3 countries");
        assertEquals(5, returned.get("PT"), "PT should have 5 stations");
        assertEquals(3, returned.get("ES"), "ES should have 3 stations");
        assertEquals(2, returned.get("FR"), "FR should have 2 stations");
    }

    @Test
    void getStationsByCityShouldReturnCorrectCount() {
        Map<String, Integer> byCountry = new HashMap<>();
        RadiusSearchSummary summary = new RadiusSearchSummary(byCountry, 7, 3, 10);
        
        assertEquals(7, summary.getStationsByCity(), "Should return 7 cities");
    }

    @Test
    void getStationsByNonCityShouldReturnCorrectCount() {
        Map<String, Integer> byCountry = new HashMap<>();
        RadiusSearchSummary summary = new RadiusSearchSummary(byCountry, 2, 8, 10);
        
        assertEquals(8, summary.getStationsByNonCity(), "Should return 8 non-cities");
    }

    @Test
    void getTotalStationsShouldReturnCorrectCount() {
        Map<String, Integer> byCountry = new HashMap<>();
        RadiusSearchSummary summary = new RadiusSearchSummary(byCountry, 5, 5, 10);
        
        assertEquals(10, summary.getTotalStations(), "Should return 10 total stations");
    }

    @Test
    void getTotalStationsShouldEqualCitiesPlusNonCities() {
        Map<String, Integer> byCountry = new HashMap<>();
        int cities = 6;
        int nonCities = 4;
        int total = 10;
        
        RadiusSearchSummary summary = new RadiusSearchSummary(byCountry, cities, nonCities, total);
        
        assertEquals(total, summary.getTotalStations(), "Total should be 10");
        assertEquals(cities + nonCities, summary.getTotalStations(), 
                "Total should equal cities + non-cities");
    }

    @Test
    void constructorShouldHandleEmptyMap() {
        Map<String, Integer> empty = new HashMap<>();
        RadiusSearchSummary summary = new RadiusSearchSummary(empty, 0, 0, 0);
        
        assertNotNull(summary, "Summary should not be null");
        assertTrue(summary.getStationsByCountry().isEmpty(), 
                "Empty map should return empty");
        assertEquals(0, summary.getTotalStations(), "Total should be 0");
    }

    @Test
    void constructorShouldHandleSingleCountry() {
        Map<String, Integer> byCountry = new HashMap<>();
        byCountry.put("PT", 10);
        
        RadiusSearchSummary summary = new RadiusSearchSummary(byCountry, 5, 5, 10);
        
        Map<String, Integer> returned = summary.getStationsByCountry();
        assertEquals(1, returned.size(), "Should have one country");
        assertEquals(10, returned.get("PT"), "PT should have 10 stations");
    }

    @Test
    void toStringShouldReturnStringRepresentation() {
        Map<String, Integer> byCountry = new HashMap<>();
        byCountry.put("PT", 5);
        byCountry.put("ES", 3);
        
        RadiusSearchSummary summary = new RadiusSearchSummary(byCountry, 4, 4, 8);
        String str = summary.toString();
        
        assertNotNull(str, "toString should not return null");
        assertFalse(str.isEmpty(), "toString should not return empty string");
        assertTrue(str.contains("total=") || str.contains("8"), 
                "toString should contain total information");
    }

    @Test
    void summaryShouldHandleZeroValues() {
        Map<String, Integer> byCountry = new HashMap<>();
        RadiusSearchSummary summary = new RadiusSearchSummary(byCountry, 0, 0, 0);
        
        assertEquals(0, summary.getTotalStations(), "Total should be 0");
        assertEquals(0, summary.getStationsByCity(), "Cities should be 0");
        assertEquals(0, summary.getStationsByNonCity(), "Non-cities should be 0");
    }

    @Test
    void summaryShouldHandleLargeValues() {
        Map<String, Integer> byCountry = new HashMap<>();
        byCountry.put("PT", 1000);
        byCountry.put("ES", 500);
        
        RadiusSearchSummary summary = new RadiusSearchSummary(byCountry, 800, 700, 1500);
        
        assertEquals(1500, summary.getTotalStations(), "Total should be 1500");
        assertEquals(800, summary.getStationsByCity(), "Cities should be 800");
        assertEquals(700, summary.getStationsByNonCity(), "Non-cities should be 700");
        assertEquals(2, summary.getStationsByCountry().size(), "Should have 2 countries");
    }
}

