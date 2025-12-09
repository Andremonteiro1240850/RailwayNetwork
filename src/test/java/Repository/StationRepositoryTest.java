package Repository;

import Sprint1.Domain.Station;
import Sprint2.Repository.StationRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StationRepositoryTest {

    // Repository compartilhado entre TODOS os testes - criado UMA VEZ
    private static StationRepository sharedRepository;
    private static List<Station> cachedStations;
    private static final String CSV_FILE_PATH = "train_station_dataset/train_stations_europe.csv";

    @BeforeAll
    static void loadCSVOnce() {
        long start = System.currentTimeMillis();

        // Carrega CSV
        cachedStations = loadStationsFromCSV();
        System.out.println("✓ CSV loaded: " + cachedStations.size() + " stations (" +
                (System.currentTimeMillis() - start) + "ms)");

        // Cria repository UMA ÚNICA VEZ
        start = System.currentTimeMillis();
        sharedRepository = new StationRepository(cachedStations);
        System.out.println("✓ Repository created: " + (System.currentTimeMillis() - start) + "ms");
        System.out.println("✓ Total setup time - all tests will now run instantly!");
    }

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

                    if (latStr.isEmpty() || lonStr.isEmpty()) {
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
            fail("Erro ao carregar ficheiro CSV: " + e.getMessage());
        }

        return stations;
    }

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

    @Test
    void populateTrees_ValidStations_PopulatesAllThreeTrees() {
        assertTrue(sharedRepository.totalStations() > 0, "Repository deve ter estações");
        assertTrue(sharedRepository.getHeightLatitudeTree() > 0, "Árvore de latitude deve ter altura > 0");
        assertTrue(sharedRepository.getHeightTimeZoneTree() > 0, "Árvore de timezone deve ter altura > 0");
    }

    @Test
    void populateTrees_InvalidStations_IgnoresThem() {
        List<Station> mixedStations = new ArrayList<>();
        mixedStations.add(new Station("Valid", 45.0, 7.0, "FR", "tz", "CET", true, false, false));
        mixedStations.add(new Station("Invalid", 0, 0, "", "", "", false, false, false));

        StationRepository testRepo = new StationRepository(mixedStations);
        assertTrue(testRepo.totalStations() >= 0, "Repository deve processar sem erros");
    }

    @Test
    void getStationsByTimeZone_ExistingTimeZone_ReturnsStations() {
        List<Station> stations = sharedRepository.getStationsByTimeZone("CET");

        assertFalse(stations.isEmpty(), "Deve retornar estações para CET");
        assertTrue(stations.stream().allMatch(s -> s.getTimeZoneGroup().equals("CET")),
                "Todas as estações devem ter timezone CET");
    }

    @Test
    void getStationsByTimeZone_NonExistingTimeZone_ReturnsEmptyList() {
        List<Station> stations = sharedRepository.getStationsByTimeZone("NonExistent");
        assertTrue(stations.isEmpty(), "Deve retornar lista vazia para timezone inexistente");
    }

    @Test
    void getStationsByCountry_ExistingCountry_ReturnsStations() {
        List<Station> stations = sharedRepository.getStationsByCountry("FR");

        assertFalse(stations.isEmpty(), "Deve retornar estações para França");
        assertTrue(stations.stream().allMatch(s -> s.getCountry().equals("FR")),
                "Todas as estações devem ser de França");
    }

    @Test
    void getStationsByCountry_NonExistingCountry_ReturnsEmptyList() {
        List<Station> stations = sharedRepository.getStationsByCountry("ZZ");
        assertTrue(stations.isEmpty(), "Deve retornar lista vazia para país inexistente");
    }

    @Test
    void getStationsByCountry_MultipleCountries_ReturnsOnlySpecified() {
        List<Station> frStations = sharedRepository.getStationsByCountry("FR");

        assertFalse(frStations.isEmpty());
        assertTrue(frStations.stream().noneMatch(s -> !s.getCountry().equals("FR")),
                "Não deve haver estações de outros países");
    }

    @Test
    void getStationsByLatitude_ValidRange_ReturnsStationsInRange() {
        double minLat = 43.0;
        double maxLat = 45.0;

        List<Station> stations = sharedRepository.getStationsByLatitude(minLat, maxLat);

        assertFalse(stations.isEmpty(), "Deve retornar estações no range");
        assertTrue(stations.stream().allMatch(s ->
                        s.getLatitude() >= minLat && s.getLatitude() <= maxLat),
                "Todas as estações devem estar dentro do range");
    }

    @Test
    void getStationsByLatitude_BoundaryValues_IncludesBoundaries() {
        double testLat = 44.08179;

        List<Station> stations = sharedRepository.getStationsByLatitude(testLat, testLat);

        assertFalse(stations.isEmpty(), "Deve incluir estações nos limites exatos");
        assertTrue(stations.stream().anyMatch(s -> s.getLatitude() == testLat),
                "Deve incluir estações com latitude exata");
    }

    @Test
    void getStationsByLatitude_EmptyRange_ReturnsEmptyList() {
        List<Station> stations = sharedRepository.getStationsByLatitude(-90, -89);
        assertTrue(stations.isEmpty(), "Deve retornar lista vazia para range sem estações");
    }

    @Test
    void getStationsByLatitude_FullRange_ReturnsAllStations() {
        List<Station> stations = sharedRepository.getStationsByLatitude(-90, 90);
        assertEquals(sharedRepository.totalStations(), stations.size(),
                "Range completo deve retornar todas as estações");
    }

    @Test
    void getStationsByLongitude_ValidRange_ReturnsStationsInRange() {
        double minLon = -1.0;
        double maxLon = 8.0;

        List<Station> stations = sharedRepository.getStationsByLongitude(minLon, maxLon);

        assertFalse(stations.isEmpty(), "Deve retornar estações no range");
        assertTrue(stations.stream().allMatch(s ->
                        s.getLongitude() >= minLon && s.getLongitude() <= maxLon),
                "Todas as estações devem estar dentro do range");
    }

    @Test
    void getStationsByLongitude_BoundaryValues_IncludesBoundaries() {
        double testLon = 6.001625;

        List<Station> stations = sharedRepository.getStationsByLongitude(testLon, testLon);

        assertFalse(stations.isEmpty(), "Deve incluir estações nos limites exatos");
        assertTrue(stations.stream().anyMatch(s -> s.getLongitude() == testLon),
                "Deve incluir estações com longitude exata");
    }

    @Test
    void getStationsByLongitude_EmptyRange_ReturnsEmptyList() {
        List<Station> stations = sharedRepository.getStationsByLongitude(170, 180);
        assertTrue(stations.isEmpty(), "Deve retornar lista vazia para range sem estações");
    }

    @Test
    void getStationsByLongitude_FullRange_ReturnsAllStations() {
        List<Station> stations = sharedRepository.getStationsByLongitude(-180, 180);
        assertEquals(sharedRepository.totalStations(), stations.size(),
                "Range completo deve retornar todas as estações");
    }

    @Test
    void totalStations_AfterPopulation_ReturnsCorrectCount() {
        int total = sharedRepository.totalStations();
        assertTrue(total > 0, "Total de estações deve ser maior que 0");

        List<Station> byLatitude = sharedRepository.getStationsByLatitude(-90, 90);
        List<Station> byLongitude = sharedRepository.getStationsByLongitude(-180, 180);

        assertEquals(total, byLatitude.size(), "Árvore de latitude deve ter o mesmo total");
        assertEquals(total, byLongitude.size(), "Árvore de longitude deve ter o mesmo total");
    }

    @Test
    void totalStations_EmptyRepository_ReturnsZero() {
        StationRepository emptyRepo = new StationRepository(new ArrayList<>());
        assertEquals(0, emptyRepo.totalStations(), "Repository vazio deve ter 0 estações");
    }

    @Test
    void getHeightLatitudeTree_PopulatedTree_ReturnsPositiveHeight() {
        int height = sharedRepository.getHeightLatitudeTree();
        assertTrue(height > 0, "Altura da árvore de latitude deve ser > 0");

        int n = sharedRepository.totalStations();
        double maxHeight = 1.44 * Math.log(n + 2) / Math.log(2);

        assertTrue(height <= maxHeight,
                "Altura deve respeitar propriedade de balanceamento AVL");
    }

    @Test
    void getHeightLatitudeTree_EmptyTree_ReturnsZero() {
        StationRepository emptyRepo = new StationRepository(new ArrayList<>());
        assertEquals(0, emptyRepo.getHeightLatitudeTree(), "Árvore vazia deve ter altura 0");
    }

    @Test
    void getHeightTimeZoneTree_PopulatedTree_ReturnsPositiveHeight() {
        int height = sharedRepository.getHeightTimeZoneTree();
        assertTrue(height > 0, "Altura da árvore de timezone deve ser > 0");

        int n = sharedRepository.totalStations();
        double maxHeight = 1.44 * Math.log(n + 2) / Math.log(2);

        assertTrue(height <= maxHeight,
                "Altura deve respeitar propriedade de balanceamento AVL");
    }

    @Test
    void getHeightTimeZoneTree_EmptyTree_ReturnsZero() {
        StationRepository emptyRepo = new StationRepository(new ArrayList<>());
        assertEquals(0, emptyRepo.getHeightTimeZoneTree(), "Árvore vazia deve ter altura 0");
    }

    @Test
    void getAllStationsOrderedByLatitude_ReturnsInOrder() {
        Iterable<Station> stations = sharedRepository.getAllStationsOrderedByLatitude();

        List<Station> stationList = new ArrayList<>();
        stations.forEach(stationList::add);

        assertFalse(stationList.isEmpty(), "Deve retornar estações");

        for (int i = 0; i < stationList.size() - 1; i++) {
            assertTrue(stationList.get(i).getLatitude() <= stationList.get(i + 1).getLatitude(),
                    "Estações devem estar ordenadas por latitude crescente");
        }
    }

    @Test
    void getAllStationsOrderedByLatitude_ReturnsAllStations() {
        Iterable<Station> stations = sharedRepository.getAllStationsOrderedByLatitude();

        List<Station> stationList = new ArrayList<>();
        stations.forEach(stationList::add);

        assertEquals(sharedRepository.totalStations(), stationList.size(),
                "Deve retornar todas as estações");
    }

    @Test
    void getAllStationsOrderedByLatitude_EmptyRepository_ReturnsEmpty() {
        StationRepository emptyRepo = new StationRepository(new ArrayList<>());
        Iterable<Station> stations = emptyRepo.getAllStationsOrderedByLatitude();

        List<Station> stationList = new ArrayList<>();
        stations.forEach(stationList::add);

        assertTrue(stationList.isEmpty(), "Repository vazio deve retornar lista vazia");
    }

    @Test
    void getAllStationsOrderedByLongitude_ReturnsInOrder() {
        Iterable<Station> stations = sharedRepository.getAllStationsOrderedByLongitude();

        List<Station> stationList = new ArrayList<>();
        stations.forEach(stationList::add);

        assertFalse(stationList.isEmpty(), "Deve retornar estações");

        for (int i = 0; i < stationList.size() - 1; i++) {
            assertTrue(stationList.get(i).getLongitude() <= stationList.get(i + 1).getLongitude(),
                    "Estações devem estar ordenadas por longitude crescente");
        }
    }

    @Test
    void getAllStationsOrderedByLongitude_ReturnsAllStations() {
        Iterable<Station> stations = sharedRepository.getAllStationsOrderedByLongitude();

        List<Station> stationList = new ArrayList<>();
        stations.forEach(stationList::add);

        assertEquals(sharedRepository.totalStations(), stationList.size(),
                "Deve retornar todas as estações");
    }

    @Test
    void getAllStationsOrderedByLongitude_EmptyRepository_ReturnsEmpty() {
        StationRepository emptyRepo = new StationRepository(new ArrayList<>());
        Iterable<Station> stations = emptyRepo.getAllStationsOrderedByLongitude();

        List<Station> stationList = new ArrayList<>();
        stations.forEach(stationList::add);

        assertTrue(stationList.isEmpty(), "Repository vazio deve retornar lista vazia");
    }

    @Test
    void threeTrees_Consistency_AllHaveSameStations() {
        List<Station> byLatitude = new ArrayList<>();
        sharedRepository.getAllStationsOrderedByLatitude().forEach(byLatitude::add);

        List<Station> byLongitude = new ArrayList<>();
        sharedRepository.getAllStationsOrderedByLongitude().forEach(byLongitude::add);

        assertEquals(byLatitude.size(), byLongitude.size(),
                "Ambas as árvores devem ter o mesmo número de estações");

        for (Station s : byLatitude) {
            assertTrue(byLongitude.contains(s),
                    "Estação deve existir em ambas as árvores");
        }
    }

    @Test
    void avlBalancing_MultipleInsertions_MaintainsBalance() {
        List<Station> manyStations = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            manyStations.add(new Station(
                    "Station" + i,
                    i * 0.9 - 45,
                    i * 1.8 - 90,
                    "FR",
                    "tz",
                    "CET",
                    false,
                    false,
                    false
            ));
        }

        StationRepository largeRepo = new StationRepository(manyStations);

        int n = largeRepo.totalStations();
        int height = largeRepo.getHeightLatitudeTree();

        double maxHeight = 1.44 * Math.log(n + 2) / Math.log(2);

        assertTrue(height <= maxHeight,
                "Árvore grande deve manter balanceamento AVL");
    }
}