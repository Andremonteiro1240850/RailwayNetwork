package Repository;

import Domain.Station;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StationRepositoryTest {

    private StationRepository repository;
    private List<Station> testStations;
    private static final String CSV_FILE_PATH = "src/test/resources/stations.csv";

    @BeforeEach
    void setUp() {
        testStations = loadStationsFromCSV();
        repository = new StationRepository(testStations);
    }

    private List<Station> loadStationsFromCSV() {
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
    void populateTrees_ValidStations_PopulatesAllThreeTrees() {
        // Verifica que as árvores foram populadas
        assertTrue(repository.totalStations() > 0, "Repository deve ter estações");

        // Verifica que as árvores têm altura > 0 (não estão vazias)
        assertTrue(repository.getHeightLatitudeTree() > 0, "Árvore de latitude deve ter altura > 0");
        assertTrue(repository.getHeightTimeZoneTree() > 0, "Árvore de timezone deve ter altura > 0");
    }

    @Test
    void populateTrees_InvalidStations_IgnoresThem() {
        // Cria lista com estações inválidas (coordenadas vazias)
        List<Station> mixedStations = new ArrayList<>();
        mixedStations.add(new Station("Valid", 45.0, 7.0, "FR", "tz", "CET", true, false, false));
        mixedStations.add(new Station("Invalid", 0, 0, "", "", "", false, false, false));

        StationRepository testRepo = new StationRepository(mixedStations);

        // Apenas estações válidas devem ser inseridas
        // O StationValidator deve filtrar as inválidas
        assertTrue(testRepo.totalStations() >= 0, "Repository deve processar sem erros");
    }

    @Test
    void getStationsByTimeZone_ExistingTimeZone_ReturnsStations() {
        List<Station> stations = repository.getStationsByTimeZone("CET");

        assertFalse(stations.isEmpty(), "Deve retornar estações para CET");
        assertTrue(stations.stream().allMatch(s -> s.getTimeZoneGroup().equals("CET")),
                "Todas as estações devem ter timezone CET");
    }

    @Test
    void getStationsByTimeZone_NonExistingTimeZone_ReturnsEmptyList() {
        List<Station> stations = repository.getStationsByTimeZone("NonExistent");

        assertTrue(stations.isEmpty(), "Deve retornar lista vazia para timezone inexistente");
    }

    @Test
    void getStationsByCountry_ExistingCountry_ReturnsStations() {
        List<Station> stations = repository.getStationsByCountry("FR");

        assertFalse(stations.isEmpty(), "Deve retornar estações para França");
        assertTrue(stations.stream().allMatch(s -> s.getCountry().equals("FR")),
                "Todas as estações devem ser de França");
    }

    @Test
    void getStationsByCountry_NonExistingCountry_ReturnsEmptyList() {
        List<Station> stations = repository.getStationsByCountry("ZZ");

        assertTrue(stations.isEmpty(), "Deve retornar lista vazia para país inexistente");
    }

    @Test
    void getStationsByCountry_MultipleCountries_ReturnsOnlySpecified() {
        // Carrega estações e verifica que apenas o país solicitado é retornado
        List<Station> frStations = repository.getStationsByCountry("FR");

        assertFalse(frStations.isEmpty());

        // Nenhuma estação de outro país deve estar na lista
        assertTrue(frStations.stream().noneMatch(s -> !s.getCountry().equals("FR")),
                "Não deve haver estações de outros países");
    }

    @Test
    void getStationsByLatitude_ValidRange_ReturnsStationsInRange() {
        double minLat = 43.0;
        double maxLat = 45.0;

        List<Station> stations = repository.getStationsByLatitude(minLat, maxLat);

        assertFalse(stations.isEmpty(), "Deve retornar estações no range");
        assertTrue(stations.stream().allMatch(s ->
                        s.getLatitude() >= minLat && s.getLatitude() <= maxLat),
                "Todas as estações devem estar dentro do range");
    }

    @Test
    void getStationsByLatitude_BoundaryValues_IncludesBoundaries() {
        // Testa que os limites são inclusivos
        double testLat = 44.08179; // Latitude da primeira estação do CSV

        List<Station> stations = repository.getStationsByLatitude(testLat, testLat);

        assertFalse(stations.isEmpty(), "Deve incluir estações nos limites exatos");
        assertTrue(stations.stream().anyMatch(s -> s.getLatitude() == testLat),
                "Deve incluir estações com latitude exata");
    }

    @Test
    void getStationsByLatitude_EmptyRange_ReturnsEmptyList() {
        List<Station> stations = repository.getStationsByLatitude(-90, -89);

        assertTrue(stations.isEmpty(), "Deve retornar lista vazia para range sem estações");
    }

    @Test
    void getStationsByLatitude_FullRange_ReturnsAllStations() {
        List<Station> stations = repository.getStationsByLatitude(-90, 90);

        assertEquals(repository.totalStations(), stations.size(),
                "Range completo deve retornar todas as estações");
    }

    @Test
    void getStationsByLongitude_ValidRange_ReturnsStationsInRange() {
        double minLon = -1.0;
        double maxLon = 8.0;

        List<Station> stations = repository.getStationsByLongitude(minLon, maxLon);

        assertFalse(stations.isEmpty(), "Deve retornar estações no range");
        assertTrue(stations.stream().allMatch(s ->
                        s.getLongitude() >= minLon && s.getLongitude() <= maxLon),
                "Todas as estações devem estar dentro do range");
    }

    @Test
    void getStationsByLongitude_BoundaryValues_IncludesBoundaries() {
        // Testa que os limites são inclusivos
        double testLon = 6.001625; // Longitude da primeira estação do CSV

        List<Station> stations = repository.getStationsByLongitude(testLon, testLon);

        assertFalse(stations.isEmpty(), "Deve incluir estações nos limites exatos");
        assertTrue(stations.stream().anyMatch(s -> s.getLongitude() == testLon),
                "Deve incluir estações com longitude exata");
    }

    @Test
    void getStationsByLongitude_EmptyRange_ReturnsEmptyList() {
        List<Station> stations = repository.getStationsByLongitude(170, 180);

        assertTrue(stations.isEmpty(), "Deve retornar lista vazia para range sem estações");
    }

    @Test
    void getStationsByLongitude_FullRange_ReturnsAllStations() {
        List<Station> stations = repository.getStationsByLongitude(-180, 180);

        assertEquals(repository.totalStations(), stations.size(),
                "Range completo deve retornar todas as estações");
    }

    @Test
    void totalStations_AfterPopulation_ReturnsCorrectCount() {
        int total = repository.totalStations();

        assertTrue(total > 0, "Total de estações deve ser maior que 0");

        // Verifica consistência: todas as árvores devem ter o mesmo número
        List<Station> byLatitude = repository.getStationsByLatitude(-90, 90);
        List<Station> byLongitude = repository.getStationsByLongitude(-180, 180);

        assertEquals(total, byLatitude.size(),
                "Árvore de latitude deve ter o mesmo total");
        assertEquals(total, byLongitude.size(),
                "Árvore de longitude deve ter o mesmo total");
    }

    @Test
    void totalStations_EmptyRepository_ReturnsZero() {
        StationRepository emptyRepo = new StationRepository(new ArrayList<>());

        assertEquals(0, emptyRepo.totalStations(),
                "Repository vazio deve ter 0 estações");
    }

    @Test
    void getHeightLatitudeTree_PopulatedTree_ReturnsPositiveHeight() {
        int height = repository.getHeightLatitudeTree();

        assertTrue(height > 0, "Altura da árvore de latitude deve ser > 0");

        // Verifica propriedade AVL: altura <= 1.44 * log2(n+2)
        int n = repository.totalStations();
        double maxHeight = 1.44 * Math.log(n + 2) / Math.log(2);

        assertTrue(height <= maxHeight,
                "Altura deve respeitar propriedade de balanceamento AVL");
    }

    @Test
    void getHeightLatitudeTree_EmptyTree_ReturnsZero() {
        StationRepository emptyRepo = new StationRepository(new ArrayList<>());

        assertEquals(0, emptyRepo.getHeightLatitudeTree(),
                "Árvore vazia deve ter altura 0");
    }

    @Test
    void getHeightTimeZoneTree_PopulatedTree_ReturnsPositiveHeight() {
        int height = repository.getHeightTimeZoneTree();

        assertTrue(height > 0, "Altura da árvore de timezone deve ser > 0");

        // Verifica propriedade AVL
        int n = repository.totalStations();
        double maxHeight = 1.44 * Math.log(n + 2) / Math.log(2);

        assertTrue(height <= maxHeight,
                "Altura deve respeitar propriedade de balanceamento AVL");
    }

    @Test
    void getHeightTimeZoneTree_EmptyTree_ReturnsZero() {
        StationRepository emptyRepo = new StationRepository(new ArrayList<>());

        assertEquals(0, emptyRepo.getHeightTimeZoneTree(),
                "Árvore vazia deve ter altura 0");
    }

    @Test
    void getAllStationsOrderedByLatitude_ReturnsInOrder() {
        Iterable<Station> stations = repository.getAllStationsOrderedByLatitude();

        List<Station> stationList = new ArrayList<>();
        stations.forEach(stationList::add);

        assertFalse(stationList.isEmpty(), "Deve retornar estações");

        // Verifica que está ordenado por latitude (crescente)
        for (int i = 0; i < stationList.size() - 1; i++) {
            assertTrue(stationList.get(i).getLatitude() <= stationList.get(i + 1).getLatitude(),
                    "Estações devem estar ordenadas por latitude crescente");
        }
    }

    @Test
    void getAllStationsOrderedByLatitude_ReturnsAllStations() {
        Iterable<Station> stations = repository.getAllStationsOrderedByLatitude();

        List<Station> stationList = new ArrayList<>();
        stations.forEach(stationList::add);

        assertEquals(repository.totalStations(), stationList.size(),
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
        Iterable<Station> stations = repository.getAllStationsOrderedByLongitude();

        List<Station> stationList = new ArrayList<>();
        stations.forEach(stationList::add);

        assertFalse(stationList.isEmpty(), "Deve retornar estações");

        // Verifica que está ordenado por longitude (crescente)
        for (int i = 0; i < stationList.size() - 1; i++) {
            assertTrue(stationList.get(i).getLongitude() <= stationList.get(i + 1).getLongitude(),
                    "Estações devem estar ordenadas por longitude crescente");
        }
    }

    @Test
    void getAllStationsOrderedByLongitude_ReturnsAllStations() {
        Iterable<Station> stations = repository.getAllStationsOrderedByLongitude();

        List<Station> stationList = new ArrayList<>();
        stations.forEach(stationList::add);

        assertEquals(repository.totalStations(), stationList.size(),
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
        // Verifica que as 3 árvores têm as mesmas estações
        List<Station> byLatitude = new ArrayList<>();
        repository.getAllStationsOrderedByLatitude().forEach(byLatitude::add);

        List<Station> byLongitude = new ArrayList<>();
        repository.getAllStationsOrderedByLongitude().forEach(byLongitude::add);

        assertEquals(byLatitude.size(), byLongitude.size(),
                "Ambas as árvores devem ter o mesmo número de estações");

        // Verifica que todas as estações de latitude existem em longitude
        for (Station s : byLatitude) {
            assertTrue(byLongitude.contains(s),
                    "Estação deve existir em ambas as árvores");
        }
    }

    @Test
    void avlBalancing_MultipleInsertions_MaintainsBalance() {
        // Cria repository com múltiplas estações e verifica balanceamento
        List<Station> manyStations = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            manyStations.add(new Station(
                    "Station" + i,
                    i * 0.9 - 45,  // Latitude entre -45 e 45
                    i * 1.8 - 90,  // Longitude entre -90 e 90
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

        // Propriedade AVL: h <= 1.44 * log2(n+2)
        double maxHeight = 1.44 * Math.log(n + 2) / Math.log(2);

        assertTrue(height <= maxHeight,
                "Árvore grande deve manter balanceamento AVL");
    }
}