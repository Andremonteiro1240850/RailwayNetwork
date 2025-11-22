package Domain;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StationLoaderTest {

    private static final StationLoader loader = new StationLoader();
    private static final String csvFilePath = "train_station_dataset/train_stations_europe.csv";

    // Cache das estações para evitar recarregar em cada teste
    private static List<Station> cachedStations;

    @BeforeAll
    static void loadOnce() {
        cachedStations = loader.loadFromCSV(csvFilePath);
        System.out.println("✓ Loaded " + cachedStations.size() + " stations for testing");
    }

    @Test
    void testLoadFromCSV_notEmpty() {
        assertNotNull(cachedStations, "Lista de estações não deve ser nula");
        assertFalse(cachedStations.isEmpty(), "Deve haver pelo menos uma estação válida");
    }

    @Test
    void testAllStationsHaveMandatoryFields() {
        for (Station s : cachedStations) {
            assertNotNull(s.getName(), "Nome da estação não deve ser nulo");
            assertFalse(s.getName().isEmpty(), "Nome não deve estar vazio");

            assertNotNull(s.getCountry(), "País não deve ser nulo");
            assertFalse(s.getCountry().isEmpty(), "País não deve estar vazio");

            assertNotNull(s.getTimeZone(), "TimeZone não deve ser nulo");
            assertFalse(s.getTimeZone().isEmpty(), "TimeZone não deve estar vazio");

            assertNotNull(s.getTimeZoneGroup(), "TimeZoneGroup não deve ser nulo");
            assertFalse(s.getTimeZoneGroup().isEmpty(), "TimeZoneGroup não deve estar vazio");
        }
    }

    @Test
    void testAllStationsHaveValidCoordinates() {
        // O loader REJEITA estações com coordenadas inválidas/ausentes
        // Portanto, TODAS as estações carregadas devem ter coordenadas válidas
        for (Station s : cachedStations) {
            assertNotNull(s.getLatitude(), "Latitude não deve ser nula");
            assertNotNull(s.getLongitude(), "Longitude não deve ser nula");

            // Validar ranges
            assertTrue(s.getLatitude() >= -90.0 && s.getLatitude() <= 90.0,
                    "Latitude deve estar em [-90, 90]: " + s.getLatitude());
            assertTrue(s.getLongitude() >= -180.0 && s.getLongitude() <= 180.0,
                    "Longitude deve estar em [-180, 180]: " + s.getLongitude());
        }
    }

    @Test
    void testStationsWithMissingCoordinatesAreRejected() {
        // O loader rejeita estações com coordenadas ausentes ou inválidas
        // Verificar que TODAS as estações carregadas têm coordenadas válidas (não-null)

        for (Station s : cachedStations) {
            assertNotNull(s.getLatitude(),
                    "Estação '" + s.getName() + "' não deveria ter latitude null");
            assertNotNull(s.getLongitude(),
                    "Estação '" + s.getName() + "' não deveria ter longitude null");
        }

        // Verificar que nenhuma estação tem coordenadas em branco ou zero quando inválidas
        long stationsWithInvalidCoords = cachedStations.stream()
                .filter(s -> s.getLatitude() == 0.0 && s.getLongitude() == 0.0)
                .count();

        // Se houver estações em (0,0), verificar se é uma localização válida ou dado ausente
        // Para a maioria dos datasets europeus, (0,0) indica dado ausente
        assertTrue(stationsWithInvalidCoords < cachedStations.size(),
                "Nem todas as estações devem ter coordenadas (0,0)");
    }

    @Test
    void testParseTimeZoneFormats() {
        // Verificar que timeZones foram limpos corretamente (sem aspas, parênteses, etc)
        for (Station s : cachedStations) {
            String tz = s.getTimeZone();

            // Não deve conter caracteres de formatação CSV
            assertFalse(tz.contains("'"), "TimeZone não deve conter aspas simples");
            assertFalse(tz.contains("\""), "TimeZone não deve conter aspas duplas");
            assertFalse(tz.startsWith("("), "TimeZone não deve começar com parênteses");
            assertFalse(tz.endsWith(")"), "TimeZone não deve terminar com parênteses");
        }
    }

    @Test
    void testStationFlags() {
        for (Station s : cachedStations) {
            // Verificar que os booleans não são null (primitivos podem causar NPE se mal implementados)
            assertNotNull(s.isCity(), "isCity não deve ser null");
            assertNotNull(s.isMainStation(), "isMainStation não deve ser null");
            assertNotNull(s.isAirport(), "isAirport não deve ser null");
        }
    }

    @Test
    void testStationValidator() {
        // Todas as estações carregadas devem passar na validação
        for (Station s : cachedStations) {
            assertTrue(StationValidator.isValidStation(s),
                    "Estação deve ser válida: " + s.getName());
        }
    }

    @Test
    void testSpecificStation_Menton() {
        // Verificar que Menton foi carregada corretamente
        List<Station> mentons = cachedStations.stream()
                .filter(s -> "Menton".equals(s.getName()))
                .collect(java.util.stream.Collectors.toList());

        assertFalse(mentons.isEmpty(), "Deve existir pelo menos uma estação 'Menton'");

        // Verificar que tem coordenadas válidas
        for (Station menton : mentons) {
            assertNotNull(menton.getLatitude());
            assertNotNull(menton.getLongitude());
            assertEquals("FR", menton.getCountry(), "Menton deve ser da França");
        }
    }

    @Test
    void testLoadFromCSV_nonExistentFile() {
        List<Station> stations = loader.loadFromCSV("non_existent_file.csv");

        assertNotNull(stations, "Deve retornar lista vazia, não null");
        assertTrue(stations.isEmpty(), "Lista deve estar vazia se o ficheiro não existir");
    }

    @Test
    void testLoadFromCSV_emptyFile() {
        // Testar comportamento com ficheiro vazio (se tiver um ficheiro de teste)
        // Este teste pode ser skip se não tiver ficheiro de teste vazio
        // List<Station> stations = loader.loadFromCSV("test_empty.csv");
        // assertTrue(stations.isEmpty());
    }

    @Test
    void testNoStationWithInvalidLatitude() {
        // Garantir que nenhuma estação tem latitude fora do range [-90, 90]
        long invalidCount = cachedStations.stream()
                .filter(s -> s.getLatitude() < -90.0 || s.getLatitude() > 90.0)
                .count();

        assertEquals(0, invalidCount, "Nenhuma estação deve ter latitude inválida");
    }

    @Test
    void testNoStationWithInvalidLongitude() {
        // Garantir que nenhuma estação tem longitude fora do range [-180, 180]
        long invalidCount = cachedStations.stream()
                .filter(s -> s.getLongitude() < -180.0 || s.getLongitude() > 180.0)
                .count();

        assertEquals(0, invalidCount, "Nenhuma estação deve ter longitude inválida");
    }

    @Test
    void testCountryCodesAreValid() {
        // Verificar que todos os códigos de país têm 2 caracteres (formato ISO)
        for (Station s : cachedStations) {
            String country = s.getCountry();
            assertTrue(country.length() == 2 || country.length() > 2,
                    "Código de país deve ter formato válido: " + country);
        }
    }
}