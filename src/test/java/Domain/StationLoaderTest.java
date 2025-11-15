package Domain;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StationLoaderTest {

    private final StationLoader loader = new StationLoader();

    private final String csvFilePath = Paths.get("data", "stations.csv").toString();

    @Test
    void testLoadFromCSV_notEmpty() {
        List<Station> stations = loader.loadFromCSV(csvFilePath);

        assertNotNull(stations, "Lista de estações não deve ser nula");
        assertFalse(stations.isEmpty(), "Deve haver pelo menos uma estação válida");
    }

    @Test
    void testAllStationsHaveMandatoryFields() {
        List<Station> stations = loader.loadFromCSV(csvFilePath);

        for (Station s : stations) {
            assertNotNull(s.getName(), "Nome da estação não deve ser nulo");
            assertNotNull(s.getCountry(), "País não deve ser nulo");
            assertNotNull(s.getTimeZone(), "TimeZone não deve ser nulo");
            assertNotNull(s.getTimeZoneGroup(), "TimeZoneGroup não deve ser nulo");
        }
    }

    @Test
    void testStationsWithMissingCoordinates() {
        List<Station> stations = loader.loadFromCSV(csvFilePath);

        // Encontrar estação "Vievola" que tem latitude/longitude ausentes
        Station vievola1 = stations.stream()
                .filter(s -> "Vievola".equals(s.getName()))
                .findFirst()
                .orElse(null);

        assertNotNull(vievola1);
        assertNull(vievola1.getLatitude(), "Latitude ausente deve ser null");
        assertNull(vievola1.getLongitude(), "Longitude ausente deve ser null");
    }

    @Test
    void testParseTimeZoneFormats() {
        List<Station> stations = loader.loadFromCSV(csvFilePath);

        // Todos os timeZones devem ser corretamente limpos
        for (Station s : stations) {
            assertTrue(s.getTimeZone().startsWith("Europe/Paris"), "TimeZoneRaw deve ser limpo corretamente");
        }
    }

    @Test
    void testStationFlags() {
        List<Station> stations = loader.loadFromCSV(csvFilePath);

        for (Station s : stations) {
            assertNotNull(s.isCity());
            assertNotNull(s.isMainStation());
            assertNotNull(s.isAirport());
        }
    }

    @Test
    void testStationValidator() {
        List<Station> stations = loader.loadFromCSV(csvFilePath);

        for (Station s : stations) {
            assertTrue(StationValidator.isValidStation(s), "Todas as estações carregadas devem ser válidas");
        }
    }

    @Test
    void testDuplicateStations() {
        List<Station> stations = loader.loadFromCSV(csvFilePath);

        // Contar quantas vezes "Menton" aparece
        long count = stations.stream()
                .filter(s -> "Menton".equals(s.getName()))
                .count();

        assertEquals(2, count, "Menton aparece duas vezes no CSV");
    }

    @Test
    void testLoadFromCSV_nonExistentFile() {
        List<Station> stations = loader.loadFromCSV("non_existent_file.csv");

        assertNotNull(stations);
        assertTrue(stations.isEmpty(), "Lista deve estar vazia se o ficheiro não existir");
    }
}
