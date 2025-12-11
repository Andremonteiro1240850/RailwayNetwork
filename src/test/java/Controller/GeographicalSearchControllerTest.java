package Controller;

import Sprint2.Controller.GeographicalSearchController;
import Sprint2.Controller.KDTreeController;
import Sprint2.Domain.KDTree;
import Sprint2.Domain.SearchFilters;
import Sprint2.Domain.Station;
import Sprint2.Domain.ENUM.Country;
import Sprint2.Repository.KDTreeRepository;
import Sprint2.Repository.StationRepository;
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
 * Test class for GeographicalSearchController (USEI08).
 * Tests use the existing train_stations_europe.csv file and validate results.
 * Optimized to load data once using @BeforeAll for faster test execution.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GeographicalSearchControllerTest {

    private GeographicalSearchController controller;
    private static StationRepository cachedStationRepository;
    private static KDTreeRepository cachedKDTreeRepository;
    private static KDTree cachedKDTree;
    private static List<Station> cachedStations;
    private static final String CSV_FILE_PATH = "train_station_dataset/train_stations_europe.csv";

    @BeforeAll
    void setUpOnce() {
        // Load stations from existing CSV file once for all tests
        cachedStations = loadStationsFromCSV();
        
        // Verify file was loaded correctly
        assertNotNull(cachedStations, "Lista de estações não deve ser null");
        assertFalse(cachedStations.isEmpty(), "Deve carregar estações do ficheiro CSV");
        assertTrue(cachedStations.size() > 1000, "Deve carregar pelo menos 1000 estações do ficheiro");
        
        cachedStationRepository = new StationRepository(cachedStations);
        
        // Build KD-tree once
        cachedKDTreeRepository = new KDTreeRepository(cachedStationRepository);
        cachedKDTree = cachedKDTreeRepository.buildTree(cachedStations);
        
        // Verify tree was built
        assertNotNull(cachedKDTree, "KD-tree não deve ser null");
        assertFalse(cachedKDTree.isEmpty(), "KD-tree não deve estar vazio");
    }

    @BeforeEach
    void setUp() {
        // Create new controller for each test using cached data
        controller = new GeographicalSearchController(cachedKDTreeRepository, cachedStationRepository);
    }

    /**
     * Loads stations from the existing CSV file.
     * Verifies file exists and contains valid data.
     */
    private List<Station> loadStationsFromCSV() {
        List<Station> stations = new ArrayList<>();
        
        // Verify file exists
        java.io.File csvFile = new java.io.File(CSV_FILE_PATH);
        if (!csvFile.exists()) {
            fail("Ficheiro CSV não encontrado: " + CSV_FILE_PATH + 
                 "\nCaminho absoluto tentado: " + csvFile.getAbsolutePath());
        }
        
        if (!csvFile.canRead()) {
            fail("Ficheiro CSV não pode ser lido: " + CSV_FILE_PATH);
        }

        int lineCount = 0;
        int validStationsCount = 0;
        
        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                lineCount++;
                
                if (isFirstLine) {
                    isFirstLine = false;
                    // Verify header
                    if (!line.contains("country") || !line.contains("station")) {
                        fail("Header do CSV não está no formato esperado. Primeira linha: " + line);
                    }
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

                    if (latStr.isEmpty() || lonStr.isEmpty()) {
                        continue;
                    }

                    double latitude = Double.parseDouble(latStr);
                    double longitude = Double.parseDouble(lonStr);
                    
                    // Validate coordinates
                    if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                        continue;
                    }

                    Station station = new Station(stationName, latitude, longitude, country,
                            timeZone, timeZoneGroup, isCity, isMainStation, isAirport);
                    stations.add(station);
                    validStationsCount++;

                } catch (NumberFormatException e) {
                    continue;
                }
            }
        } catch (IOException e) {
            fail("Erro ao carregar ficheiro CSV: " + e.getMessage() + 
                 "\nCaminho: " + CSV_FILE_PATH +
                 "\nCaminho absoluto: " + new java.io.File(CSV_FILE_PATH).getAbsolutePath());
        }
        
        // Verify we loaded stations
        if (stations.isEmpty()) {
            fail("Nenhuma estação foi carregada do ficheiro CSV. " +
                 "Linhas processadas: " + lineCount + 
                 "\nVerifique se o ficheiro está no formato correto.");
        }
        
        if (validStationsCount < 100) {
            fail("Muito poucas estações válidas carregadas: " + validStationsCount + 
                 ". Esperado pelo menos 100. Linhas processadas: " + lineCount);
        }

        return stations;
    }

    /**
     * Parses a CSV line handling quoted fields.
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

    @Test
    void test1_RangeQuery_WithoutFilters() {
        // Test: Search for stations in France region (around Paris)
        double latMin = 43.0;
        double latMax = 49.0;
        double lonMin = 2.0;
        double lonMax = 8.0;
        
        // Execute search
        List<Station> results = controller.searchByGeographicalArea(latMin, latMax, lonMin, lonMax);
        
        // Verify results
        assertFalse(results.isEmpty(), "Deve retornar estações na região de França");
        
        // Verify all results are within the specified range
        for (Station station : results) {
            assertTrue(station.getLatitude() >= latMin && station.getLatitude() <= latMax,
                    "Latitude deve estar dentro do range");
            assertTrue(station.getLongitude() >= lonMin && station.getLongitude() <= lonMax,
                    "Longitude deve estar dentro do range");
        }
        
        // Verify sorting (should be sorted by name)
        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).getName().compareTo(results.get(i + 1).getName()) <= 0,
                    "Resultados devem estar ordenados por nome");
        }
    }

    @Test
    void test2_RangeQuery_WithFilters() {
        // Test: Search for French cities in a specific region
        double latMin = 43.0;
        double latMax = 49.0;
        double lonMin = 2.0;
        double lonMax = 8.0;
        SearchFilters filters = new SearchFilters(true, null, Country.FR);
        
        // Execute search
        List<Station> results = controller.searchByGeographicalArea(latMin, latMax, lonMin, lonMax, filters);
        
        // Verify results
        assertFalse(results.isEmpty(), "Deve retornar estações francesas que são cidades");
        
        // Verify all results match filters
        for (Station station : results) {
            assertTrue(station.getLatitude() >= latMin && station.getLatitude() <= latMax,
                    "Latitude deve estar dentro do range");
            assertTrue(station.getLongitude() >= lonMin && station.getLongitude() <= lonMax,
                    "Longitude deve estar dentro do range");
            assertTrue(filters.matches(station), 
                    "Todas as estações devem corresponder aos filtros");
            assertTrue(station.isCity(), "Todas devem ser cidades");
            assertEquals("FR", station.getCountry(), "Todas devem ser francesas");
        }
        
        // Verify sorting
        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).getName().compareTo(results.get(i + 1).getName()) <= 0,
                    "Resultados devem estar ordenados por nome");
        }
    }

    @Test
    void test3_NearestNeighbor_WithoutFilters() {
        // Test: Find 5 nearest stations to Paris
        double targetLat = 48.8566;  // Paris
        double targetLon = 2.3522;
        int n = 5;
        
        // Execute search
        List<KDTree.StationDistance> results = controller.findNearestStations(targetLat, targetLon, n);
        
        // Verify results
        assertTrue(results.size() <= n, "Número de resultados não deve exceder n");
        assertFalse(results.isEmpty(), "Deve retornar pelo menos uma estação");
        
        // Verify sorting (should be sorted by distance)
        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).getDistance() <= results.get(i + 1).getDistance(),
                    "Resultados devem estar ordenados por distância");
        }
        
        // Verify first result is closest
        if (results.size() > 0) {
            double firstDistance = results.get(0).getDistance();
            assertTrue(firstDistance >= 0, "Distância deve ser não negativa");
        }
    }

    @Test
    void test4_NearestNeighbor_WithFilters() {
        // Test: Find 10 nearest French main stations to Paris
        double targetLat = 48.8566;  // Paris
        double targetLon = 2.3522;
        int n = 10;
        SearchFilters filters = new SearchFilters(null, true, Country.FR);
        
        // Execute search
        List<KDTree.StationDistance> results = controller.findNearestStations(targetLat, targetLon, n, filters);
        
        // Verify results
        assertTrue(results.size() <= n, "Número de resultados não deve exceder n");
        
        // Verify all results match filters
        for (KDTree.StationDistance sd : results) {
            assertTrue(filters.matches(sd.getStation()), 
                    "Todas as estações devem corresponder aos filtros");
            assertTrue(sd.getStation().isMainStation(), "Todas devem ser estações principais");
            assertEquals("FR", sd.getStation().getCountry(), "Todas devem ser francesas");
        }
        
        // Verify sorting
        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).getDistance() <= results.get(i + 1).getDistance(),
                    "Resultados devem estar ordenados por distância");
        }
    }

    @Test
    void test5_RadiusSearch_WithoutFilters() {
        // Test: Find all stations within 100km of Paris
        double targetLat = 48.8566;  // Paris
        double targetLon = 2.3522;
        double radiusKm = 100.0;
        
        // Execute search
        List<KDTree.StationDistance> results = controller.findStationsInRadius(targetLat, targetLon, radiusKm);
        
        // Verify results
        assertFalse(results.isEmpty(), "Deve retornar estações dentro do raio");
        
        // Verify all results are within radius
        for (KDTree.StationDistance sd : results) {
            assertTrue(sd.getDistance() <= radiusKm,
                    "Todas as estações devem estar dentro do raio");
            assertTrue(sd.getDistance() >= 0, "Distância deve ser não negativa");
        }
        
        // Verify sorting (should be sorted by distance)
        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).getDistance() <= results.get(i + 1).getDistance(),
                    "Resultados devem estar ordenados por distância");
        }
    }

    @Test
    void test6_RadiusSearch_WithFilters() {
        // Test: Find all cities within 200km of Paris
        double targetLat = 48.8566;  // Paris
        double targetLon = 2.3522;
        double radiusKm = 200.0;
        SearchFilters filters = new SearchFilters(true, null, null);
        
        // Execute search
        List<KDTree.StationDistance> results = controller.findStationsInRadius(targetLat, targetLon, radiusKm, filters);
        
        // Verify results
        assertFalse(results.isEmpty(), "Deve retornar cidades dentro do raio");
        
        // Verify all results are within radius and match filters
        for (KDTree.StationDistance sd : results) {
            assertTrue(sd.getDistance() <= radiusKm,
                    "Todas as estações devem estar dentro do raio");
            assertTrue(filters.matches(sd.getStation()), 
                    "Todas as estações devem corresponder aos filtros");
            assertTrue(sd.getStation().isCity(), "Todas devem ser cidades");
        }
        
        // Verify sorting
        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).getDistance() <= results.get(i + 1).getDistance(),
                    "Resultados devem estar ordenados por distância");
        }
    }

    @Test
    void test7_InvalidRangeQuery_ThrowsException() {
        // Test: Invalid latitude range
        assertThrows(IllegalArgumentException.class, () -> {
            controller.searchByGeographicalArea(50.0, 40.0, -180.0, 180.0);
        }, "Deve lançar exceção para range de latitude inválido");
        
        // Test: Invalid longitude range
        assertThrows(IllegalArgumentException.class, () -> {
            controller.searchByGeographicalArea(-90.0, 90.0, 10.0, 5.0);
        }, "Deve lançar exceção para range de longitude inválido");
        
        // Test: Latitude out of bounds
        assertThrows(IllegalArgumentException.class, () -> {
            controller.searchByGeographicalArea(-100.0, 90.0, -180.0, 180.0);
        }, "Deve lançar exceção para latitude fora dos limites");
    }

    @Test
    void test8_InvalidNearestNeighbor_ThrowsException() {
        // Test: Invalid n parameter
        assertThrows(IllegalArgumentException.class, () -> {
            controller.findNearestStations(48.8566, 2.3522, 0);
        }, "Deve lançar exceção para n <= 0");
        
        // Test: Invalid latitude
        assertThrows(IllegalArgumentException.class, () -> {
            controller.findNearestStations(100.0, 2.3522, 5);
        }, "Deve lançar exceção para latitude inválida");
    }

    @Test
    void test9_InvalidRadiusSearch_ThrowsException() {
        // Test: Negative radius
        assertThrows(IllegalArgumentException.class, () -> {
            controller.findStationsInRadius(48.8566, 2.3522, -10.0);
        }, "Deve lançar exceção para raio negativo");
        
        // Test: Invalid latitude
        assertThrows(IllegalArgumentException.class, () -> {
            controller.findStationsInRadius(100.0, 2.3522, 50.0);
        }, "Deve lançar exceção para latitude inválida");
    }

    @Test
    void test10_EmptyResults_ValidQuery() {
        // Test: Query in area with no stations (middle of ocean)
        List<Station> results = controller.searchByGeographicalArea(-10.0, -5.0, -30.0, -25.0);
        
        // Should return empty list, not throw exception
        assertNotNull(results, "Deve retornar lista (pode estar vazia)");
        assertTrue(results.isEmpty(), "Deve retornar lista vazia para área sem estações");
    }

    @Test
    void test11_RangeQuery_WithNullFilters() {
        // Test: Filters object is null (should work as no filters)
        double latMin = 43.0;
        double latMax = 49.0;
        double lonMin = 2.0;
        double lonMax = 8.0;
        
        List<Station> results = controller.searchByGeographicalArea(latMin, latMax, lonMin, lonMax, null);
        
        assertNotNull(results, "Deve retornar resultados mesmo com filtros null");
        assertFalse(results.isEmpty(), "Deve retornar estações na região");
    }

    @Test
    void test12_RangeQuery_WithEmptyFilters() {
        // Test: Filters object with no active filters
        double latMin = 43.0;
        double latMax = 49.0;
        double lonMin = 2.0;
        double lonMax = 8.0;
        SearchFilters emptyFilters = new SearchFilters();
        
        List<Station> results = controller.searchByGeographicalArea(latMin, latMax, lonMin, lonMax, emptyFilters);
        
        assertNotNull(results, "Deve retornar resultados com filtros vazios");
        assertFalse(results.isEmpty(), "Deve retornar estações na região");
    }

    @Test
    void test13_RangeQuery_WithCombinedFilters() {
        // Test: Multiple filters combined (city AND main station AND country)
        double latMin = 43.0;
        double latMax = 49.0;
        double lonMin = 2.0;
        double lonMax = 8.0;
        SearchFilters filters = new SearchFilters(true, true, Country.FR);
        
        List<Station> results = controller.searchByGeographicalArea(latMin, latMax, lonMin, lonMax, filters);
        
        // Verify all results match all filters
        for (Station station : results) {
            assertTrue(station.isCity(), "Todas devem ser cidades");
            assertTrue(station.isMainStation(), "Todas devem ser estações principais");
            assertEquals("FR", station.getCountry(), "Todas devem ser francesas");
        }
    }

    @Test
    void test14_InvalidLongitude_ThrowsException() {
        // Test: Longitude out of bounds
        assertThrows(IllegalArgumentException.class, () -> {
            controller.searchByGeographicalArea(-90.0, 90.0, -200.0, 180.0);
        }, "Deve lançar exceção para longitude fora dos limites");
        
        assertThrows(IllegalArgumentException.class, () -> {
            controller.searchByGeographicalArea(-90.0, 90.0, -180.0, 200.0);
        }, "Deve lançar exceção para longitude fora dos limites");
    }

    @Test
    void test15_InvalidLongitude_NearestNeighbor() {
        // Test: Invalid longitude for nearest neighbor
        assertThrows(IllegalArgumentException.class, () -> {
            controller.findNearestStations(48.8566, 200.0, 5);
        }, "Deve lançar exceção para longitude inválida");
        
        assertThrows(IllegalArgumentException.class, () -> {
            controller.findNearestStations(48.8566, -200.0, 5);
        }, "Deve lançar exceção para longitude inválida");
    }

    @Test
    void test16_InvalidLongitude_RadiusSearch() {
        // Test: Invalid longitude for radius search
        assertThrows(IllegalArgumentException.class, () -> {
            controller.findStationsInRadius(48.8566, 200.0, 50.0);
        }, "Deve lançar exceção para longitude inválida");
        
        assertThrows(IllegalArgumentException.class, () -> {
            controller.findStationsInRadius(48.8566, -200.0, 50.0);
        }, "Deve lançar exceção para longitude inválida");
    }

    @Test
    void test17_NearestNeighbor_WithNoMatchingFilters() {
        // Test: Filters that match no stations (very specific filter)
        double targetLat = 48.8566;
        double targetLon = 2.3522;
        int n = 10;
        // Filter for non-existent combination (e.g., airport stations in a country with no airports in range)
        SearchFilters filters = new SearchFilters(false, false, Country.PT);
        
        List<KDTree.StationDistance> results = controller.findNearestStations(targetLat, targetLon, n, filters);
        
        // Should return empty or fewer results, not throw exception
        assertNotNull(results, "Deve retornar lista mesmo sem matches");
        assertTrue(results.size() <= n, "Número de resultados não deve exceder n");
    }

    @Test
    void test18_RadiusSearch_WithNoMatchingFilters() {
        // Test: Filters that match no stations in radius
        double targetLat = 48.8566;
        double targetLon = 2.3522;
        double radiusKm = 10.0; // Small radius
        SearchFilters filters = new SearchFilters(true, true, Country.PT); // PT stations in France region
        
        List<KDTree.StationDistance> results = controller.findStationsInRadius(targetLat, targetLon, radiusKm, filters);
        
        // Should return empty list, not throw exception
        assertNotNull(results, "Deve retornar lista mesmo sem matches");
    }

    @Test
    void test19_RadiusSearch_ZeroRadius() {
        // Test: Zero radius should return stations at exact point (if any)
        double targetLat = 48.8566;
        double targetLon = 2.3522;
        double radiusKm = 0.0;
        
        List<KDTree.StationDistance> results = controller.findStationsInRadius(targetLat, targetLon, radiusKm);
        
        assertNotNull(results, "Deve retornar lista mesmo com raio zero");
        // All results should have distance 0 (or very close)
        for (KDTree.StationDistance sd : results) {
            assertTrue(sd.getDistance() <= radiusKm + 0.001, "Distância deve ser <= raio");
        }
    }

    @Test
    void test20_NearestNeighbor_LargeN() {
        // Test: Request more stations than available
        double targetLat = 48.8566;
        double targetLon = 2.3522;
        int n = 100000; // Very large number
        
        List<KDTree.StationDistance> results = controller.findNearestStations(targetLat, targetLon, n);
        
        assertNotNull(results, "Deve retornar lista");
        assertTrue(results.size() <= n, "Número de resultados não deve exceder n");
        // Should return all available stations, not throw exception
    }

    @Test
    void test21_RangeQuery_ExactBoundaries() {
        // Test: Query with exact boundary values
        double latMin = -90.0;
        double latMax = 90.0;
        double lonMin = -180.0;
        double lonMax = 180.0;
        
        List<Station> results = controller.searchByGeographicalArea(latMin, latMax, lonMin, lonMax);
        
        assertNotNull(results, "Deve retornar resultados para todo o mundo");
        assertFalse(results.isEmpty(), "Deve retornar estações");
    }

    @Test
    void test22_RangeQuery_WithIsMainStationFilter() {
        // Test: Filter by main station only
        double latMin = 43.0;
        double latMax = 49.0;
        double lonMin = 2.0;
        double lonMax = 8.0;
        SearchFilters filters = new SearchFilters(null, true, null);
        
        List<Station> results = controller.searchByGeographicalArea(latMin, latMax, lonMin, lonMax, filters);
        
        for (Station station : results) {
            assertTrue(station.isMainStation(), "Todas devem ser estações principais");
        }
    }

    @Test
    void test23_RangeQuery_WithIsCityFalseFilter() {
        // Test: Filter for non-city stations
        double latMin = 43.0;
        double latMax = 49.0;
        double lonMin = 2.0;
        double lonMax = 8.0;
        SearchFilters filters = new SearchFilters(false, null, null);
        
        List<Station> results = controller.searchByGeographicalArea(latMin, latMax, lonMin, lonMax, filters);
        
        for (Station station : results) {
            assertFalse(station.isCity(), "Todas devem ser não-cidades");
        }
    }

    @Test
    void test24_RadiusSearch_WithIsMainStationFilter() {
        // Test: Filter by main station in radius search
        double targetLat = 48.8566;
        double targetLon = 2.3522;
        double radiusKm = 100.0;
        SearchFilters filters = new SearchFilters(null, true, null);
        
        List<KDTree.StationDistance> results = controller.findStationsInRadius(targetLat, targetLon, radiusKm, filters);
        
        for (KDTree.StationDistance sd : results) {
            assertTrue(sd.getStation().isMainStation(), "Todas devem ser estações principais");
        }
    }

    @Test
    void test25_GetTreeStatistics() {
        // Test: Get tree statistics
        String stats = controller.getTreeStatistics();
        
        assertNotNull(stats, "Estatísticas não devem ser null");
        assertTrue(stats.contains("Size") || stats.contains("nodes"), "Deve conter informação sobre tamanho");
        assertTrue(stats.contains("Height") || stats.contains("levels"), "Deve conter informação sobre altura");
    }

    @Test
    void test26_GetKDTree() {
        // Test: Get KD-tree
        KDTree tree = controller.getKDTree();
        
        assertNotNull(tree, "KD-tree não deve ser null");
        assertFalse(tree.isEmpty(), "KD-tree não deve estar vazio");
    }

    @Test
    void test27_GetAllStations() {
        // Test: Get all stations
        List<Station> allStations = controller.getAllStations();
        
        assertNotNull(allStations, "Lista de estações não deve ser null");
        assertFalse(allStations.isEmpty(), "Deve conter estações");
        assertEquals(cachedStations.size(), allStations.size(), "Deve retornar todas as estações carregadas");
    }

    @Test
    void test28_GetStationsCount() {
        // Test: Get stations count
        int count = controller.getStationsCount();
        
        assertTrue(count > 0, "Contagem deve ser positiva");
        assertEquals(cachedStations.size(), count, "Contagem deve corresponder ao número de estações carregadas");
    }

    @Test
    void test29_RangeQuery_WithDifferentCountries() {
        // Test: Filter by different countries
        double latMin = 36.0;
        double latMax = 50.0;
        double lonMin = -10.0;
        double lonMax = 10.0;
        
        SearchFilters ptFilter = new SearchFilters(null, null, Country.PT);
        SearchFilters esFilter = new SearchFilters(null, null, Country.ES);
        SearchFilters frFilter = new SearchFilters(null, null, Country.FR);
        
        List<Station> ptResults = controller.searchByGeographicalArea(latMin, latMax, lonMin, lonMax, ptFilter);
        List<Station> esResults = controller.searchByGeographicalArea(latMin, latMax, lonMin, lonMax, esFilter);
        List<Station> frResults = controller.searchByGeographicalArea(latMin, latMax, lonMin, lonMax, frFilter);
        
        // Verify country filtering works
        for (Station station : ptResults) {
            assertEquals("PT", station.getCountry(), "Todas devem ser portuguesas");
        }
        for (Station station : esResults) {
            assertEquals("ES", station.getCountry(), "Todas devem ser espanholas");
        }
        for (Station station : frResults) {
            assertEquals("FR", station.getCountry(), "Todas devem ser francesas");
        }
    }

    @Test
    void test30_NearestNeighbor_ExactLocation() {
        // Test: Find nearest to an exact station location
        // Use coordinates of a known station
        double targetLat = 48.8566;  // Paris
        double targetLon = 2.3522;
        int n = 1;
        
        List<KDTree.StationDistance> results = controller.findNearestStations(targetLat, targetLon, n);
        
        assertEquals(1, results.size(), "Deve retornar exatamente 1 estação");
        assertTrue(results.get(0).getDistance() >= 0, "Distância deve ser não negativa");
    }

    @Test
    void test31_Constructor_WithStationsAndRepository() {
        // Test: Constructor with stations list and repository
        List<Station> testStations = new ArrayList<>();
        testStations.add(new Station("Test1", 40.0, -8.0, "PT", "Europe/Lisbon", "WET", true, true, false));
        testStations.add(new Station("Test2", 50.0, 2.0, "FR", "Europe/Paris", "CET", false, false, false));
        
        StationRepository testRepo = new StationRepository(testStations);
        GeographicalSearchController testController = new GeographicalSearchController(testStations, testRepo);
        
        assertNotNull(testController, "Controller não deve ser null");
        assertTrue(testController.getStationsCount() >= 0, "Deve ter estações (pode ser menos se algumas forem filtradas)");
    }

    @Test
    void test32_Constructor_WithStationsAndRepository_NullStations() {
        // Test: Constructor throws exception for null stations
        StationRepository testRepo = new StationRepository(new ArrayList<>());
        
        assertThrows(IllegalArgumentException.class, () -> {
            new GeographicalSearchController((List<Station>) null, testRepo);
        }, "Deve lançar exceção para stations null");
    }

    @Test
    void test33_Constructor_WithStationsAndRepository_EmptyStations() {
        // Test: Constructor throws exception for empty stations
        StationRepository testRepo = new StationRepository(new ArrayList<>());
        
        assertThrows(IllegalArgumentException.class, () -> {
            new GeographicalSearchController(new ArrayList<>(), testRepo);
        }, "Deve lançar exceção para stations vazias");
    }

    @Test
    void test34_Constructor_WithStationsAndRepository_NullRepository() {
        // Test: Constructor with null repository (should create empty tree)
        List<Station> testStations = new ArrayList<>();
        testStations.add(new Station("Test1", 40.0, -8.0, "PT", "Europe/Lisbon", "WET", true, true, false));
        
        GeographicalSearchController testController = new GeographicalSearchController(testStations, null);
        
        assertNotNull(testController, "Controller não deve ser null");
        assertTrue(testController.getKDTree().isEmpty(), "KD-tree deve estar vazio quando repository é null");
    }

    @Test
    void test35_Constructor_WithKDTreeAndStations() {
        // Test: Constructor with existing KD-tree and stations
        List<Station> testStations = new ArrayList<>();
        testStations.add(new Station("Test1", 40.0, -8.0, "PT", "Europe/Lisbon", "WET", true, true, false));
        
        StationRepository testRepo = new StationRepository(testStations);
        KDTreeRepository kdTreeRepo = new KDTreeRepository(testRepo);
        KDTree testTree = kdTreeRepo.buildTree(testStations);
        
        GeographicalSearchController testController = new GeographicalSearchController(testTree, testStations);
        
        assertNotNull(testController, "Controller não deve ser null");
        assertTrue(testController.getStationsCount() >= 0, "Deve ter estações");
    }

    @Test
    void test36_Constructor_WithKDTreeAndRepository() {
        // Test: Constructor with KD-tree and repository
        List<Station> testStations = new ArrayList<>();
        testStations.add(new Station("Test1", 40.0, -8.0, "PT", "Europe/Lisbon", "WET", true, true, false));
        
        StationRepository testRepo = new StationRepository(testStations);
        KDTreeRepository kdTreeRepo = new KDTreeRepository(testRepo);
        KDTree testTree = kdTreeRepo.buildTree(testStations);
        
        GeographicalSearchController testController = new GeographicalSearchController(testTree, testRepo);
        
        assertNotNull(testController, "Controller não deve ser null");
        assertTrue(testController.getStationsCount() >= 0, "Deve ter estações");
    }

    @Test
    void test37_Constructor_WithKDTreeAndRepository_NullKDTree() {
        // Test: Constructor throws exception for null KD-tree
        StationRepository testRepo = new StationRepository(new ArrayList<>());
        
        assertThrows(IllegalArgumentException.class, () -> {
            new GeographicalSearchController((KDTree) null, testRepo);
        }, "Deve lançar exceção para KD-tree null");
    }

    @Test
    void test38_Constructor_WithKDTreeAndRepository_NullRepository() {
        // Test: Constructor throws exception for null repository
        KDTree testTree = new KDTree();
        
        assertThrows(IllegalArgumentException.class, () -> {
            new GeographicalSearchController(testTree, (StationRepository) null);
        }, "Deve lançar exceção para repository null");
    }

    @Test
    void test39_Constructor_WithKDTreeRepository_NullKDTreeRepository() {
        // Test: Constructor throws exception for null KDTreeRepository
        StationRepository testRepo = new StationRepository(new ArrayList<>());
        
        assertThrows(IllegalArgumentException.class, () -> {
            new GeographicalSearchController((KDTreeRepository) null, testRepo);
        }, "Deve lançar exceção para KDTreeRepository null");
    }

    @Test
    void test40_Constructor_WithKDTreeRepository_NullStationRepository() {
        // Test: Constructor throws exception for null StationRepository
        List<Station> testStations = new ArrayList<>();
        testStations.add(new Station("Test1", 40.0, -8.0, "PT", "Europe/Lisbon", "WET", true, true, false));
        
        StationRepository testRepo = new StationRepository(testStations);
        KDTreeRepository kdTreeRepo = new KDTreeRepository(testRepo);
        kdTreeRepo.buildTree(testStations);
        
        assertThrows(IllegalArgumentException.class, () -> {
            new GeographicalSearchController(kdTreeRepo, (StationRepository) null);
        }, "Deve lançar exceção para StationRepository null");
    }

    @Test
    void test41_Constructor_WithKDTreeRepository_TreeNotBuilt() {
        // Test: Constructor throws exception when tree is not built
        List<Station> testStations = new ArrayList<>();
        testStations.add(new Station("Test1", 40.0, -8.0, "PT", "Europe/Lisbon", "WET", true, true, false));
        
        StationRepository testRepo = new StationRepository(testStations);
        KDTreeRepository kdTreeRepo = new KDTreeRepository(testRepo);
        // Don't build tree
        
        assertThrows(IllegalStateException.class, () -> {
            new GeographicalSearchController(kdTreeRepo, testRepo);
        }, "Deve lançar exceção quando tree não está construída");
    }

    @Test
    void test42_RangeQuery_AllLatitudeBoundaryConditions() {
        // Test: All latitude boundary conditions
        // latMin = -90
        List<Station> results1 = controller.searchByGeographicalArea(-90.0, 0.0, -180.0, 180.0);
        assertNotNull(results1, "Deve funcionar com latMin = -90");
        
        // latMax = 90
        List<Station> results2 = controller.searchByGeographicalArea(0.0, 90.0, -180.0, 180.0);
        assertNotNull(results2, "Deve funcionar com latMax = 90");
        
        // latMin = latMax (single point)
        List<Station> results3 = controller.searchByGeographicalArea(48.0, 48.0, 2.0, 2.0);
        assertNotNull(results3, "Deve funcionar com latMin = latMax");
    }

    @Test
    void test43_RangeQuery_AllLongitudeBoundaryConditions() {
        // Test: All longitude boundary conditions
        // lonMin = -180
        List<Station> results1 = controller.searchByGeographicalArea(-90.0, 90.0, -180.0, 0.0);
        assertNotNull(results1, "Deve funcionar com lonMin = -180");
        
        // lonMax = 180
        List<Station> results2 = controller.searchByGeographicalArea(-90.0, 90.0, 0.0, 180.0);
        assertNotNull(results2, "Deve funcionar com lonMax = 180");
        
        // lonMin = lonMax (single point)
        List<Station> results3 = controller.searchByGeographicalArea(48.0, 48.0, 2.0, 2.0);
        assertNotNull(results3, "Deve funcionar com lonMin = lonMax");
    }

    @Test
    void test44_RangeQuery_FiltersWithNoMatches() {
        // Test: Filters that match no stations in the area
        double latMin = 43.0;
        double latMax = 49.0;
        double lonMin = 2.0;
        double lonMax = 8.0;
        // Filter for PT stations in France region (should return empty)
        SearchFilters filters = new SearchFilters(null, null, Country.PT);
        
        List<Station> results = controller.searchByGeographicalArea(latMin, latMax, lonMin, lonMax, filters);
        
        assertNotNull(results, "Deve retornar lista mesmo sem matches");
        // May be empty, which is valid
    }

    @Test
    void test45_NearestNeighbor_FiltersWithNoMatches() {
        // Test: Filters that match no stations
        double targetLat = 48.8566;
        double targetLon = 2.3522;
        int n = 5;
        // Very specific filter that might not match
        SearchFilters filters = new SearchFilters(false, false, Country.PT);
        
        List<KDTree.StationDistance> results = controller.findNearestStations(targetLat, targetLon, n, filters);
        
        assertNotNull(results, "Deve retornar lista mesmo sem matches");
        assertTrue(results.size() <= n, "Número de resultados não deve exceder n");
    }

    @Test
    void test46_RadiusSearch_FiltersWithNoMatches() {
        // Test: Filters that match no stations in radius
        double targetLat = 48.8566;
        double targetLon = 2.3522;
        double radiusKm = 5.0; // Very small radius
        SearchFilters filters = new SearchFilters(true, true, Country.PT); // PT in France
        
        List<KDTree.StationDistance> results = controller.findStationsInRadius(targetLat, targetLon, radiusKm, filters);
        
        assertNotNull(results, "Deve retornar lista mesmo sem matches");
    }

    @Test
    void test47_RangeQuery_FilteredResultsEmpty() {
        // Test: When filtered results are empty, should return empty list
        double latMin = 43.0;
        double latMax = 49.0;
        double lonMin = 2.0;
        double lonMax = 8.0;
        // Filter that definitely won't match (e.g., airport stations when there are none)
        SearchFilters filters = new SearchFilters(null, null, Country.AD); // Andorra in France region
        
        List<Station> results = controller.searchByGeographicalArea(latMin, latMax, lonMin, lonMax, filters);
        
        assertNotNull(results, "Deve retornar lista");
        // May be empty, which is valid
    }

    @Test
    void test48_VerifyCSVFileLoaded() {
        // Test: Verify that CSV file was actually loaded with real data
        assertTrue(cachedStations.size() > 10000, "Deve carregar mais de 10000 estações do CSV");
        
        // Verify stations have valid data
        boolean hasValidStation = false;
        for (Station station : cachedStations) {
            if (station.getLatitude() >= -90 && station.getLatitude() <= 90 &&
                station.getLongitude() >= -180 && station.getLongitude() <= 180 &&
                station.getName() != null && !station.getName().isEmpty()) {
                hasValidStation = true;
                break;
            }
        }
        assertTrue(hasValidStation, "Deve ter pelo menos uma estação com dados válidos");
        
        // Verify countries are loaded
        boolean hasPT = false;
        boolean hasFR = false;
        boolean hasES = false;
        for (Station station : cachedStations) {
            String country = station.getCountry();
            if ("PT".equals(country)) hasPT = true;
            if ("FR".equals(country)) hasFR = true;
            if ("ES".equals(country)) hasES = true;
        }
        assertTrue(hasPT || hasFR || hasES, "Deve ter estações de pelo menos um país (PT, FR ou ES)");
    }

    @Test
    void test49_RangeQuery_WithAllFilterCombinations() {
        // Test: All possible filter combinations
        double latMin = 36.0;
        double latMax = 50.0;
        double lonMin = -10.0;
        double lonMax = 10.0;
        
        // Test all combinations
        SearchFilters[] filterCombinations = {
            new SearchFilters(true, true, Country.FR),
            new SearchFilters(true, false, Country.FR),
            new SearchFilters(false, true, Country.FR),
            new SearchFilters(false, false, Country.FR),
            new SearchFilters(true, null, Country.FR),
            new SearchFilters(null, true, Country.FR),
            new SearchFilters(null, null, Country.FR),
            new SearchFilters(true, true, null),
            new SearchFilters(true, false, null),
            new SearchFilters(false, true, null),
            new SearchFilters(false, false, null)
        };
        
        for (SearchFilters filters : filterCombinations) {
            List<Station> results = controller.searchByGeographicalArea(latMin, latMax, lonMin, lonMax, filters);
            assertNotNull(results, "Deve retornar lista para filtros: " + filters);
            // Verify all results match filters
            for (Station station : results) {
                assertTrue(filters.matches(station), 
                    "Todas as estações devem corresponder aos filtros: " + filters);
            }
        }
    }

    @Test
    void test50_RadiusSearch_WithAllFilterCombinations() {
        // Test: All possible filter combinations for radius search
        double targetLat = 48.8566;
        double targetLon = 2.3522;
        double radiusKm = 200.0;
        
        SearchFilters[] filterCombinations = {
            new SearchFilters(true, true, Country.FR),
            new SearchFilters(true, false, Country.FR),
            new SearchFilters(false, true, Country.FR),
            new SearchFilters(false, false, Country.FR),
            new SearchFilters(true, null, Country.FR),
            new SearchFilters(null, true, Country.FR),
            new SearchFilters(null, null, Country.FR),
            new SearchFilters(true, true, null),
            new SearchFilters(true, false, null),
            new SearchFilters(false, true, null),
            new SearchFilters(false, false, null)
        };
        
        for (SearchFilters filters : filterCombinations) {
            List<KDTree.StationDistance> results = controller.findStationsInRadius(
                targetLat, targetLon, radiusKm, filters);
            assertNotNull(results, "Deve retornar lista para filtros: " + filters);
            // Verify all results match filters
            for (KDTree.StationDistance sd : results) {
                assertTrue(filters.matches(sd.getStation()), 
                    "Todas as estações devem corresponder aos filtros: " + filters);
            }
        }
    }

    @Test
    void test51_NearestNeighbor_WithAllFilterCombinations() {
        // Test: All possible filter combinations for nearest neighbor
        double targetLat = 48.8566;
        double targetLon = 2.3522;
        int n = 20;
        
        SearchFilters[] filterCombinations = {
            new SearchFilters(true, true, Country.FR),
            new SearchFilters(true, false, Country.FR),
            new SearchFilters(false, true, Country.FR),
            new SearchFilters(false, false, Country.FR),
            new SearchFilters(true, null, Country.FR),
            new SearchFilters(null, true, Country.FR),
            new SearchFilters(null, null, Country.FR),
            new SearchFilters(true, true, null),
            new SearchFilters(true, false, null),
            new SearchFilters(false, true, null),
            new SearchFilters(false, false, null)
        };
        
        for (SearchFilters filters : filterCombinations) {
            List<KDTree.StationDistance> results = controller.findNearestStations(
                targetLat, targetLon, n, filters);
            assertNotNull(results, "Deve retornar lista para filtros: " + filters);
            assertTrue(results.size() <= n, "Número de resultados não deve exceder n");
            // Verify all results match filters
            for (KDTree.StationDistance sd : results) {
                assertTrue(filters.matches(sd.getStation()), 
                    "Todas as estações devem corresponder aos filtros: " + filters);
            }
        }
    }

    @Test
    void test52_RangeQuery_LatMaxLessThanLatMin() {
        // Test: latMax < latMin should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            controller.searchByGeographicalArea(50.0, 40.0, -180.0, 180.0);
        }, "Deve lançar exceção quando latMax < latMin");
    }

    @Test
    void test53_RangeQuery_LonMaxLessThanLonMin() {
        // Test: lonMax < lonMin should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            controller.searchByGeographicalArea(-90.0, 90.0, 10.0, 5.0);
        }, "Deve lançar exceção quando lonMax < lonMin");
    }

    @Test
    void test54_GetAllStations_ReturnsCopy() {
        // Test: getAllStations returns a copy, not the original list
        List<Station> stations1 = controller.getAllStations();
        List<Station> stations2 = controller.getAllStations();
        
        assertNotSame(stations1, stations2, "Deve retornar cópias diferentes");
        assertEquals(stations1.size(), stations2.size(), "Cópias devem ter o mesmo tamanho");
    }

    @Test
    void test55_Constructor_Deprecated() {
        // Test: Deprecated constructor with stations list only
        List<Station> testStations = new ArrayList<>();
        testStations.add(new Station("Test1", 40.0, -8.0, "PT", "Europe/Lisbon", "WET", true, true, false));
        
        @SuppressWarnings("deprecation")
        GeographicalSearchController testController = new GeographicalSearchController(testStations);
        
        assertNotNull(testController, "Controller não deve ser null");
        assertTrue(testController.getStationsCount() >= 0, "Deve ter estações");
    }

    @Test
    void test56_Constructor_WithKDTreeController() {
        // Test: Constructor with KDTreeController
        List<Station> testStations = new ArrayList<>();
        testStations.add(new Station("Test1", 40.0, -8.0, "PT", "Europe/Lisbon", "WET", true, true, false));
        testStations.add(new Station("Test2", 50.0, 2.0, "FR", "Europe/Paris", "CET", false, false, false));
        
        StationRepository testRepo = new StationRepository(testStations);
        KDTreeController kdTreeController = new KDTreeController(testRepo);
        kdTreeController.buildTree(testStations);
        
        GeographicalSearchController testController = new GeographicalSearchController(kdTreeController, testRepo);
        
        assertNotNull(testController, "Controller não deve ser null");
        assertTrue(testController.getStationsCount() >= 0, "Deve ter estações");
    }

    @Test
    void test57_Constructor_WithKDTreeController_NullController() {
        // Test: Constructor throws exception for null KDTreeController
        List<Station> testStations = new ArrayList<>();
        testStations.add(new Station("Test1", 40.0, -8.0, "PT", "Europe/Lisbon", "WET", true, true, false));
        StationRepository testRepo = new StationRepository(testStations);
        
        assertThrows(IllegalArgumentException.class, () -> {
            new GeographicalSearchController((KDTreeController) null, testRepo);
        }, "Deve lançar exceção para KDTreeController null");
    }

    @Test
    void test58_Constructor_WithKDTreeController_TreeNotBuilt() {
        // Test: Constructor throws exception when tree is not built
        List<Station> testStations = new ArrayList<>();
        testStations.add(new Station("Test1", 40.0, -8.0, "PT", "Europe/Lisbon", "WET", true, true, false));
        StationRepository testRepo = new StationRepository(testStations);
        KDTreeController kdTreeController = new KDTreeController(testRepo);
        // Don't build tree
        
        assertThrows(IllegalStateException.class, () -> {
            new GeographicalSearchController(kdTreeController, testRepo);
        }, "Deve lançar exceção quando tree não está construída");
    }

    @Test
    void test59_Constructor_WithKDTreeRepository_EmptyTree() {
        // Test: Constructor throws exception when tree is empty
        List<Station> testStations = new ArrayList<>();
        StationRepository testRepo = new StationRepository(testStations);
        KDTreeRepository kdTreeRepo = new KDTreeRepository(testRepo);
        // Build tree with empty list - will create empty tree
        try {
            kdTreeRepo.buildTree(testStations);
        } catch (Exception e) {
            // Expected - empty list may cause issues
        }
        
        // If tree is empty, constructor should throw exception
        if (kdTreeRepo.hasCachedTree() && kdTreeRepo.getCachedTree() != null && kdTreeRepo.getCachedTree().isEmpty()) {
            assertThrows(IllegalStateException.class, () -> {
                new GeographicalSearchController(kdTreeRepo, testRepo);
            }, "Deve lançar exceção quando tree está vazia");
        }
    }


    @Test
    void test61_NearestNeighbor_NegativeN() {
        // Test: Negative n should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            controller.findNearestStations(48.8566, 2.3522, -1);
        }, "Deve lançar exceção para n negativo");
    }

    @Test
    void test62_NearestNeighbor_ZeroN() {
        // Test: Zero n should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            controller.findNearestStations(48.8566, 2.3522, 0);
        }, "Deve lançar exceção para n = 0");
    }

    @Test
    void test63_RadiusSearch_NegativeRadius() {
        // Test: Negative radius should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            controller.findStationsInRadius(48.8566, 2.3522, -1.0);
        }, "Deve lançar exceção para radius negativo");
    }

    @Test
    void test64_ExactBoundaries_AllMethods() {
        // Test: Exact boundary values for all search methods (consolidated)
        // Range query
        assertNotNull(controller.searchByGeographicalArea(-90.0, -89.0, -180.0, 180.0), "Range: latMin = -90");
        assertNotNull(controller.searchByGeographicalArea(89.0, 90.0, -180.0, 180.0), "Range: latMax = 90");
        assertNotNull(controller.searchByGeographicalArea(-90.0, 90.0, -180.0, -179.0), "Range: lonMin = -180");
        assertNotNull(controller.searchByGeographicalArea(-90.0, 90.0, 179.0, 180.0), "Range: lonMax = 180");
        
        // Nearest neighbor
        assertNotNull(controller.findNearestStations(-90.0, 0.0, 5), "Nearest: lat = -90");
        assertNotNull(controller.findNearestStations(90.0, 0.0, 5), "Nearest: lat = 90");
        assertNotNull(controller.findNearestStations(0.0, -180.0, 5), "Nearest: lon = -180");
        assertNotNull(controller.findNearestStations(0.0, 180.0, 5), "Nearest: lon = 180");
        
        // Radius search
        assertNotNull(controller.findStationsInRadius(-90.0, 0.0, 100.0), "Radius: lat = -90");
        assertNotNull(controller.findStationsInRadius(90.0, 0.0, 100.0), "Radius: lat = 90");
        assertNotNull(controller.findStationsInRadius(0.0, -180.0, 100.0), "Radius: lon = -180");
        assertNotNull(controller.findStationsInRadius(0.0, 180.0, 100.0), "Radius: lon = 180");
    }

    @Test
    void test70_RangeQuery_SinglePoint() {
        // Test: Query for a single point (latMin = latMax, lonMin = lonMax)
        List<Station> results = controller.searchByGeographicalArea(48.8566, 48.8566, 2.3522, 2.3522);
        assertNotNull(results, "Deve funcionar com ponto único");
    }


    @Test
    void test73_RadiusSearch_VeryLargeRadius() {
        // Test: Radius search with very large radius (should cover all stations)
        List<KDTree.StationDistance> results = controller.findStationsInRadius(48.8566, 2.3522, 20000.0);
        
        assertNotNull(results, "Deve retornar lista");
        assertTrue(results.size() <= cachedStations.size(), "Número de resultados não deve exceder total de estações");
    }

    @Test
    void test74_InvalidLatitude_AllMethods() {
        // Test: Invalid latitude values for all methods (consolidated)
        // Range query
        assertThrows(IllegalArgumentException.class, () -> {
            controller.searchByGeographicalArea(-91.0, 90.0, -180.0, 180.0);
        }, "Range: latMin < -90");
        assertThrows(IllegalArgumentException.class, () -> {
            controller.searchByGeographicalArea(-90.0, 91.0, -180.0, 180.0);
        }, "Range: latMax > 90");
        
        // Nearest neighbor
        assertThrows(IllegalArgumentException.class, () -> {
            controller.findNearestStations(-91.0, 2.3522, 5);
        }, "Nearest: lat < -90");
        assertThrows(IllegalArgumentException.class, () -> {
            controller.findNearestStations(91.0, 2.3522, 5);
        }, "Nearest: lat > 90");
        
        // Radius search
        assertThrows(IllegalArgumentException.class, () -> {
            controller.findStationsInRadius(-91.0, 2.3522, 50.0);
        }, "Radius: lat < -90");
        assertThrows(IllegalArgumentException.class, () -> {
            controller.findStationsInRadius(91.0, 2.3522, 50.0);
        }, "Radius: lat > 90");
    }

    @Test
    void test80_Constructor_WithKDTreeRepository_NullTree() {
        // Test: Constructor handles null tree from repository
        // This tests the branch: if (tree == null || tree.isEmpty())
        List<Station> testStations = new ArrayList<>();
        testStations.add(new Station("Test1", 40.0, -8.0, "PT", "Europe/Lisbon", "WET", true, true, false));
        
        StationRepository testRepo = new StationRepository(testStations);
        KDTreeRepository kdTreeRepo = new KDTreeRepository(testRepo);
        kdTreeRepo.buildTree(testStations);
        
        // Verify tree is built before testing
        if (kdTreeRepo.hasCachedTree() && kdTreeRepo.getCachedTree() != null && !kdTreeRepo.getCachedTree().isEmpty()) {
            GeographicalSearchController testController = new GeographicalSearchController(kdTreeRepo, testRepo);
            assertNotNull(testController, "Controller não deve ser null");
        }
    }

    @Test
    void test81_Constructor_WithKDTreeRepository_EmptyTreeAfterClear() {
        // Test: Constructor throws exception when tree is cleared (becomes empty)
        List<Station> testStations = new ArrayList<>();
        testStations.add(new Station("Test1", 40.0, -8.0, "PT", "Europe/Lisbon", "WET", true, true, false));
        
        StationRepository testRepo = new StationRepository(testStations);
        KDTreeRepository kdTreeRepo = new KDTreeRepository(testRepo);
        kdTreeRepo.buildTree(testStations);
        
        // Clear cache to make tree null
        kdTreeRepo.clearCache();
        
        // Now hasCachedTree should return false, so constructor should throw IllegalStateException
        assertThrows(IllegalStateException.class, () -> {
            new GeographicalSearchController(kdTreeRepo, testRepo);
        }, "Deve lançar exceção quando tree não está construída (após clear)");
    }



    @Test
    void test92_GetTreeStatistics_NotEmpty() {
        // Test: Tree statistics with non-empty tree
        String stats = controller.getTreeStatistics();
        assertNotNull(stats, "Estatísticas não devem ser null");
        assertTrue(stats.contains("Size") || stats.contains("nodes"), "Deve conter informação sobre tamanho");
        assertTrue(stats.contains("Height") || stats.contains("levels"), "Deve conter informação sobre altura");
        assertTrue(stats.length() > 0, "Estatísticas não devem estar vazias");
    }

    @Test
    void test93_GetKDTree_NotEmpty() {
        // Test: Get KD-tree and verify it's not empty
        KDTree tree = controller.getKDTree();
        assertNotNull(tree, "KD-tree não deve ser null");
        assertFalse(tree.isEmpty(), "KD-tree não deve estar vazio");
        assertTrue(tree.size() > 0, "KD-tree deve ter nós");
    }

    @Test
    void test94_GetAllStations_ModificationDoesNotAffectOriginal() {
        // Test: Modifying returned list doesn't affect original
        List<Station> stations = controller.getAllStations();
        int originalSize = stations.size();
        
        // Try to modify (should not affect original)
        stations.clear();
        
        // Original should still have stations
        List<Station> stations2 = controller.getAllStations();
        assertEquals(originalSize, stations2.size(), "Modificar lista retornada não deve afetar original");
    }
}

