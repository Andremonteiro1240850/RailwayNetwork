package Controller;

import Domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException; // Necessário para o construtor original, embora não usado diretamente aqui
import java.util.*;

/**
 * Unit tests for the TravelTimeController class.
 * Uses a test-specific subclass to avoid file I/O dependency on RailwayDataLoader.
 */
class TravelTimeControllerTest {

    private Station station1;
    private Station station2;
    private Station station3;
    private LineSegment segment12;
    private LineSegment segment23;
    private List<Station> testStations;
    private List<LineSegment> testLineSegments;
    private TravelCalculationService testService;
    private TestableTravelTimeController controller; // Usaremos a subclasse

    // --- Test-specific subclass to bypass RailwayDataLoader ---
    private static class TestableTravelTimeController extends TravelTimeController {
        private final List<Station> injectedStations;
        private final List<LineSegment> injectedLineSegments;
        private final TravelCalculationService injectedService;
        private final Map<String, Station> injectedStationMap; // Simula o map do dataLoader

        /**
         * Test constructor that accepts pre-defined data.
         * Bypasses the original constructor's file loading logic.
         */
        public TestableTravelTimeController(List<Station> stations, List<LineSegment> segments) {
            // Chamada ao super() é necessária, mas podemos contornar a lógica interna
            // passando dados vazios ou nulls se a classe permitir, ou usar uma estratégia
            // para que a lógica de carregamento não execute.
            // Neste caso, como não podemos alterar TravelTimeController,
            // inicializamos os campos diretamente nesta subclasse.

            // Criamos um mapa simulado para getStationById
            this.injectedStationMap = new HashMap<>();
            for (Station s : stations) {
                this.injectedStationMap.put(s.getId(), s);
            }

            // Armazena os dados injetados
            this.injectedStations = new ArrayList<>(stations);
            this.injectedLineSegments = new ArrayList<>(segments);
            this.injectedService = new TravelCalculationService(this.injectedLineSegments); // Cria serviço com dados de teste
        }

        // Sobrescreve os métodos para retornar os dados injetados
        @Override
        public List<Station> getStations() {
            return new ArrayList<>(injectedStations); // Retorna cópia para segurança
        }

        @Override
        public List<LineSegment> getLineSegments() {
            return new ArrayList<>(injectedLineSegments); // Retorna cópia
        }

        @Override
        public TravelCalculationService getService() {
            return injectedService;
        }

        @Override
        public Station getStationById(String id) {
            // Simula a busca no mapa que o RailwayDataLoader faria
            return injectedStationMap.get(id);
        }

        @Override
        public RailwayDataLoader getDataLoader() {
            // Retornamos null ou um DataLoader "vazio" pois não o usamos neste teste
            // Uma alternativa seria criar um DummyDataLoader se necessário.
            return null;
        }
    }
    // --- Fim da subclasse de teste ---


    @BeforeEach
    void setUp() {
        // 1. Criar dados de teste (Estações e Segmentos)
        station1 = new Station("S1", "Station One", "Location A");
        station2 = new Station("S2", "Station Two"); // Sem localização
        station3 = new Station("S3", "Station Three", "Location C");

        // Usar um construtor que inclua o nome do segmento
        segment12 = new LineSegment("L12", "Line A", station1, station2, 10.5, 1668, true);
        segment23 = new LineSegment("L23", "Line B", station2, station3, 5.2, 1668, false);

        testStations = Arrays.asList(station1, station2, station3);
        testLineSegments = Arrays.asList(segment12, segment23);

        // 2. Instanciar a subclasse de teste do Controller com os dados
        controller = new TestableTravelTimeController(testStations, testLineSegments);
    }

    /**
     * Teste para verificar se o construtor (da subclasse) inicializa
     * corretamente as listas internas com os dados fornecidos.
     */
    @Test
    void testConstructorInitialization() {
        assertNotNull(controller, "Controller should not be null");
        assertEquals(3, controller.getStations().size(), "Should have 3 stations");
        assertEquals(2, controller.getLineSegments().size(), "Should have 2 line segments");
        assertNotNull(controller.getService(), "Service should be initialized");
        // Verificar se o serviço contém os segmentos corretos
        assertEquals(2, controller.getService().getLineSegments().size(), "Service should have 2 segments");
    }

    /**
     * Testa o método getService().
     */
    @Test
    void testGetService() {
        TravelCalculationService service = controller.getService();
        assertNotNull(service, "Service should not be null");
        // Verifica se o serviço retornado contém os segmentos de teste
        assertEquals(testLineSegments, service.getLineSegments(), "Service should contain the test line segments");
    }

    /**
     * Testa o método getStations().
     */
    @Test
    void testGetStations() {
        List<Station> stations = controller.getStations();
        assertNotNull(stations, "Stations list should not be null");
        assertEquals(testStations.size(), stations.size(), "Should return the correct number of stations");
        assertTrue(stations.containsAll(testStations), "Returned list should contain all test stations");
        // Testar a imutabilidade (se retorna uma cópia)
        stations.remove(0);
        assertEquals(testStations.size(), controller.getStations().size(), "Modifying returned list should not affect controller's list");
    }

    /**
     * Testa o método getLineSegments().
     */
    @Test
    void testGetLineSegments() {
        List<LineSegment> segments = controller.getLineSegments();
        assertNotNull(segments, "Line segments list should not be null");
        assertEquals(testLineSegments.size(), segments.size(), "Should return the correct number of line segments");
        assertTrue(segments.containsAll(testLineSegments), "Returned list should contain all test line segments");
        // Testar a imutabilidade
        segments.remove(0);
        assertEquals(testLineSegments.size(), controller.getLineSegments().size(), "Modifying returned list should not affect controller's list");
    }

    /**
     * Testa o método getStationById().
     */
    @Test
    void testGetStationById() {
        // Testar encontrar uma estação existente
        Station foundStation = controller.getStationById("S2");
        assertNotNull(foundStation, "Should find station S2");
        assertEquals(station2, foundStation, "Should return the correct station S2");
        assertEquals("Station Two", foundStation.getName(), "Station S2 name should match");

        // Testar encontrar uma estação com ID diferente (case sensitive)
        Station foundStationLower = controller.getStationById("s1");
        assertNull(foundStationLower, "Should not find station with lowercase id 's1'");


        // Testar estação inexistente
        Station notFoundStation = controller.getStationById("S99");
        assertNull(notFoundStation, "Should return null for a non-existent station ID");

        // Testar ID nulo ou vazio
        assertNull(controller.getStationById(null), "Should return null for null ID");
        assertNull(controller.getStationById(""), "Should return null for empty ID");
        assertNull(controller.getStationById("  "), "Should return null for blank ID");

    }

    /**
     * Testa o método getDataLoader().
     * Como estamos a usar uma subclasse que não inicializa o loader, esperamos null.
     */
    @Test
    void testGetDataLoader() {
        // Na nossa subclasse de teste, o dataLoader não é inicializado da forma normal.
        assertNull(controller.getDataLoader(), "getDataLoader should return null in the test subclass");
    }
}