package Controller;

import Sprint1.Domain.Station;
import Sprint2.Controller.StationController;
import Sprint2.Repository.StationRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StationControllerTest {

    private StationController controller;

    // Repository compartilhado entre TODOS os testes
    private static StationRepository sharedRepository;

    private static final String CSV_FILE_PATH = "train_station_dataset/train_stations_europe.csv";

    @BeforeAll
    static void loadCSVOnce() {
        long start = System.currentTimeMillis();

        // Carrega CSV (354ms)
        List<Station> stations = loadStationsFromCSV();

        // Cria repository UMA ÚNICA VEZ (134s)
        sharedRepository = new StationRepository(stations);

        long totalTime = System.currentTimeMillis() - start;
        System.out.println("✓ Setup completo: " + totalTime + "ms (" + stations.size() + " estações)");
    }

    @BeforeEach
    void setUp() {
        // Reutiliza o repository existente - INSTANTÂNEO!
        controller = new StationController(sharedRepository);
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
    void getStationByTimeZone_ValidTimeZone_ReturnsStations() {
        Iterable<Station> result = controller.getStationByTimeZone("CET");

        List<Station> stations = new ArrayList<>();
        result.forEach(stations::add);

        assertFalse(stations.isEmpty(), "Devem existir estações com timezone CET");
        assertTrue(stations.stream().allMatch(s -> s.getTimeZoneGroup().equals("CET")),
                "Todas as estações devem ter timezone CET");
    }

    @Test
    void getStationByTimeZone_InvalidTimeZone_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            controller.getStationByTimeZone("Invalid/TimeZone");
        });

        assertTrue(exception.getMessage().contains("Invalid Time Zone Group"));
    }

    @Test
    void getStationByCountry_ValidCountry_ReturnsStations() {
        Iterable<Station> result = controller.getStationByCountry("FR");

        List<Station> stations = new ArrayList<>();
        result.forEach(stations::add);

        assertFalse(stations.isEmpty(), "Devem existir estações em França");
        assertTrue(stations.stream().allMatch(s -> s.getCountry().equals("FR")),
                "Todas as estações devem ser de França");
    }

    @Test
    void getStationByCountry_InvalidCountry_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            controller.getStationByCountry("ZZ");
        });

        assertTrue(exception.getMessage().contains("Invalid Country"));
    }

    @Test
    void getStationByLatitude_ValidRange_ReturnsStations() {
        Iterable<Station> result = controller.getStationByLatitude(43.0, 45.0);

        List<Station> stations = new ArrayList<>();
        result.forEach(stations::add);

        assertFalse(stations.isEmpty(), "Devem existir estações no range de latitude");
        assertTrue(stations.stream().allMatch(s -> s.getLatitude() >= 43.0 && s.getLatitude() <= 45.0),
                "Todas as estações devem estar no range especificado");
    }

    @Test
    void getStationByLatitude_InvalidRange_MinLessThanNegative90_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            controller.getStationByLatitude(-100, 50);
        });

        assertTrue(exception.getMessage().contains("Invalid Latitude Range"));
    }

    @Test
    void getStationByLatitude_InvalidRange_MaxGreaterThan90_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            controller.getStationByLatitude(-50, 100);
        });

        assertTrue(exception.getMessage().contains("Invalid Latitude Range"));
    }

    @Test
    void getStationByLatitude_MinGreaterThanMax_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            controller.getStationByLatitude(50, 40);
        });

        assertTrue(exception.getMessage().contains("Invalid Latitude Range"));
    }

    @Test
    void getStationByLongitude_ValidRange_ReturnsStations() {
        Iterable<Station> result = controller.getStationByLongitude(-1.0, 8.0);

        List<Station> stations = new ArrayList<>();
        result.forEach(stations::add);

        assertFalse(stations.isEmpty(), "Devem existir estações no range de longitude");
        assertTrue(stations.stream().allMatch(s -> s.getLongitude() >= -1.0 && s.getLongitude() <= 8.0),
                "Todas as estações devem estar no range especificado");
    }

    @Test
    void getStationByLongitude_InvalidRange_MinLessThanNegative180_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            controller.getStationByLongitude(-200, 50);
        });

        assertTrue(exception.getMessage().contains("Invalid Longitude Range"));
    }

    @Test
    void getStationByLongitude_InvalidRange_MaxGreaterThan180_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            controller.getStationByLongitude(-100, 200);
        });

        assertTrue(exception.getMessage().contains("Invalid Longitude Range"));
    }

    @Test
    void getStationByLongitude_MinGreaterThanMax_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            controller.getStationByLongitude(100, 50);
        });

        assertTrue(exception.getMessage().contains("Invalid Longitude Range"));
    }

    @Test
    void isValidTimeZone_ExistingTimeZone_ReturnsTrue() {
        assertTrue(controller.isValidTimeZone("CET"), "CET deve ser um timezone válido");
    }

    @Test
    void isValidTimeZone_NonExistingTimeZone_ReturnsFalse() {
        assertFalse(controller.isValidTimeZone("Invalid/TimeZone"),
                "TimeZone inválido deve retornar false");
        assertFalse(controller.isValidTimeZone("PST"),
                "TimeZone não existente nos dados deve retornar false");
    }

    @Test
    void isValidCountry_ExistingCountry_ReturnsTrue() {
        assertTrue(controller.isValidCountry("FR"), "FR deve ser um país válido");
    }

    @Test
    void isValidCountry_NonExistingCountry_ReturnsFalse() {
        assertFalse(controller.isValidCountry("ZZ"),
                "País inválido deve retornar false");
        assertFalse(controller.isValidCountry("INVALID"),
                "País não existente nos dados deve retornar false");
    }

    @Test
    void isValidLatitude_ValidRange_ReturnsTrue() {
        assertTrue(controller.isValidLatitude(-90, 90),
                "Range completo de latitude deve ser válido");
        assertTrue(controller.isValidLatitude(0, 45),
                "Range positivo deve ser válido");
        assertTrue(controller.isValidLatitude(-45, 0),
                "Range negativo deve ser válido");
        assertTrue(controller.isValidLatitude(38.5, 41.5),
                "Range específico deve ser válido");
    }

    @Test
    void isValidLatitude_InvalidRange_MinOutOfBounds_ReturnsFalse() {
        assertFalse(controller.isValidLatitude(-100, 50),
                "Min fora do range deve ser inválido");
        assertFalse(controller.isValidLatitude(-91, 90),
                "Min < -90 deve ser inválido");
    }

    @Test
    void isValidLatitude_InvalidRange_MaxOutOfBounds_ReturnsFalse() {
        assertFalse(controller.isValidLatitude(-50, 100),
                "Max fora do range deve ser inválido");
        assertFalse(controller.isValidLatitude(-90, 91),
                "Max > 90 deve ser inválido");
    }

    @Test
    void isValidLatitude_InvalidRange_MinGreaterThanMax_ReturnsFalse() {
        assertFalse(controller.isValidLatitude(50, 40),
                "Min > Max deve ser inválido");
    }

    @Test
    void isValidLatitude_BoundaryValues_ReturnsCorrect() {
        assertTrue(controller.isValidLatitude(-90, -90),
                "Boundary exato em -90 deve ser válido");
        assertTrue(controller.isValidLatitude(90, 90),
                "Boundary exato em 90 deve ser válido");
        assertTrue(controller.isValidLatitude(-90, 90),
                "Range completo deve ser válido");
        assertFalse(controller.isValidLatitude(-90.1, 90),
                "Boundary -90.1 deve ser inválido");
        assertFalse(controller.isValidLatitude(-90, 90.1),
                "Boundary 90.1 deve ser inválido");
    }

    @Test
    void isValidLongitude_ValidRange_ReturnsTrue() {
        assertTrue(controller.isValidLongitude(-180, 180),
                "Range completo de longitude deve ser válido");
        assertTrue(controller.isValidLongitude(0, 90),
                "Range positivo deve ser válido");
        assertTrue(controller.isValidLongitude(-90, 0),
                "Range negativo deve ser válido");
        assertTrue(controller.isValidLongitude(-9.5, -8.5),
                "Range específico deve ser válido");
    }

    @Test
    void isValidLongitude_InvalidRange_MinOutOfBounds_ReturnsFalse() {
        assertFalse(controller.isValidLongitude(-200, 100),
                "Min fora do range deve ser inválido");
        assertFalse(controller.isValidLongitude(-181, 180),
                "Min < -180 deve ser inválido");
    }

    @Test
    void isValidLongitude_InvalidRange_MaxOutOfBounds_ReturnsFalse() {
        assertFalse(controller.isValidLongitude(-100, 200),
                "Max fora do range deve ser inválido");
        assertFalse(controller.isValidLongitude(-180, 181),
                "Max > 180 deve ser inválido");
    }

    @Test
    void isValidLongitude_InvalidRange_MinGreaterThanMax_ReturnsFalse() {
        assertFalse(controller.isValidLongitude(100, 50),
                "Min > Max deve ser inválido");
    }

    @Test
    void isValidLongitude_BoundaryValues_ReturnsCorrect() {
        assertTrue(controller.isValidLongitude(-180, -180),
                "Boundary exato em -180 deve ser válido");
        assertTrue(controller.isValidLongitude(180, 180),
                "Boundary exato em 180 deve ser válido");
        assertTrue(controller.isValidLongitude(-180, 180),
                "Range completo deve ser válido");
        assertFalse(controller.isValidLongitude(-180.1, 180),
                "Boundary -180.1 deve ser inválido");
        assertFalse(controller.isValidLongitude(-180, 180.1),
                "Boundary 180.1 deve ser inválido");
    }

    @Test
    void loadStationsFromCSV_IgnoresInvalidLines() {
        int totalStations = sharedRepository.totalStations();

        assertTrue(totalStations > 0, "Devem existir estações carregadas do CSV");

        Iterable<Station> allStations = sharedRepository.getAllStationsOrderedByLatitude();
        for (Station s : allStations) {
            assertTrue(s.getLatitude() >= -90 && s.getLatitude() <= 90,
                    "Latitude deve estar no range válido");
            assertTrue(s.getLongitude() >= -180 && s.getLongitude() <= 180,
                    "Longitude deve estar no range válido");
        }
    }

    @Test
    void loadStationsFromCSV_ValidData() {
        List<Station> stations = sharedRepository.getStationsByCountry("FR");
        assertFalse(stations.isEmpty(), "Devem existir estações da França");

        // Verifica que estação específica existe e tem dados válidos
        boolean hasMenton = stations.stream()
                .anyMatch(s -> s.getName().equals("Menton"));
        assertTrue(hasMenton, "Deve existir a estação Menton");

        // Verifica que todas as estações carregadas têm coordenadas válidas
        for (Station s : stations) {
            assertNotNull(s.getLatitude(), "Latitude não deve ser nula");
            assertNotNull(s.getLongitude(), "Longitude não deve ser nula");
            assertTrue(s.getLatitude() >= -90 && s.getLatitude() <= 90,
                    "Latitude deve estar no range [-90, 90]");
            assertTrue(s.getLongitude() >= -180 && s.getLongitude() <= 180,
                    "Longitude deve estar no range [-180, 180]");
            assertFalse(s.getName().isEmpty(), "Nome não deve estar vazio");
            assertFalse(s.getCountry().isEmpty(), "País não deve estar vazio");
        }
    }

    @Test
    void getStationByLatitude_EmptyRange_ReturnsEmpty() {
        Iterable<Station> result = controller.getStationByLatitude(-90, -89);

        List<Station> stations = new ArrayList<>();
        result.forEach(stations::add);

        assertTrue(stations.isEmpty(), "Não devem existir estações neste range");
    }

    @Test
    void getStationByLongitude_EmptyRange_ReturnsEmpty() {
        Iterable<Station> result = controller.getStationByLongitude(170, 180);

        List<Station> stations = new ArrayList<>();
        result.forEach(stations::add);

        assertTrue(stations.isEmpty(), "Não devem existir estações neste range");
    }
}