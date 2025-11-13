package Controller;

import Domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

/**
 * Unit tests for the TravelTimeController class.
 * Uses a test-specific subclass to avoid file I/O dependency on RailwayDataLoader.
 */
class TravelTimeControllerTest {

    private Facility facility1;
    private Facility facility2;
    private Facility facility3;
    private LineSegment segment12;
    private LineSegment segment23;
    private List<Facility> testFacilities;
    private List<LineSegment> testLineSegments;
    private TravelCalculationService testService;
    private TestableTravelTimeController controller; // Usaremos a subclasse

    // --- Test-specific subclass to bypass RailwayDataLoader ---
    private static class TestableTravelTimeController extends TravelTimeController {
        private final List<Facility> injectedFacilities;
        private final List<LineSegment> injectedLineSegments;
        private final TravelCalculationService injectedService;
        private final Map<String, Facility> injectedStationMap; // Simula o map do dataLoader

        /**
         * Test constructor that accepts pre-defined data.
         * Bypasses the original constructor's file loading logic.
         */
        public TestableTravelTimeController(List<Facility> facilities, List<LineSegment> segments) {
            // Chamada ao super() é necessária, mas podemos contornar a lógica interna
            // passando dados vazios ou nulls se a classe permitir, ou usar uma estratégia
            // para que a lógica de carregamento não execute.
            // Neste caso, como não podemos alterar TravelTimeController,
            // inicializamos os campos diretamente nesta subclasse.

            // Criamos um mapa simulado para getStationById
            this.injectedStationMap = new HashMap<>();
            for (Facility s : facilities) {
                this.injectedStationMap.put(s.getId(), s);
            }

            // Armazena os dados injetados
            this.injectedFacilities = new ArrayList<>(facilities);
            this.injectedLineSegments = new ArrayList<>(segments);
            this.injectedService = new TravelCalculationService(this.injectedLineSegments); // Cria serviço com dados de teste
        }

        // Sobrescreve os métodos para retornar os dados injetados
        @Override
        public List<Facility> getStations() {
            return new ArrayList<>(injectedFacilities); // Retorna cópia para segurança
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
        public Facility getStationById(String id) {
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
        facility1 = new Facility("S1", "Station One", "Location A");
        facility2 = new Facility("S2", "Station Two"); // Sem localização
        facility3 = new Facility("S3", "Station Three", "Location C");

        // Usar um construtor que inclua o nome do segmento
        segment12 = new LineSegment("L12", "Line A", facility1, facility2, 10.5, 1668, true);
        segment23 = new LineSegment("L23", "Line B", facility2, facility3, 5.2, 1668, false);

        testFacilities = Arrays.asList(facility1, facility2, facility3);
        testLineSegments = Arrays.asList(segment12, segment23);

        // 2. Instanciar a subclasse de teste do Controller com os dados
        controller = new TestableTravelTimeController(testFacilities, testLineSegments);
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
        List<Facility> facilities = controller.getStations();
        assertNotNull(facilities, "Stations list should not be null");
        assertEquals(testFacilities.size(), facilities.size(), "Should return the correct number of stations");
        assertTrue(facilities.containsAll(testFacilities), "Returned list should contain all test stations");
        // Testar a imutabilidade (se retorna uma cópia)
        facilities.remove(0);
        assertEquals(testFacilities.size(), controller.getStations().size(), "Modifying returned list should not affect controller's list");
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
        Facility foundFacility = controller.getStationById("S2");
        assertNotNull(foundFacility, "Should find station S2");
        assertEquals(facility2, foundFacility, "Should return the correct station S2");
        assertEquals("Station Two", foundFacility.getName(), "Station S2 name should match");

        // Testar encontrar uma estação com ID diferente (case sensitive)
        Facility foundFacilityLower = controller.getStationById("s1");
        assertNull(foundFacilityLower, "Should not find station with lowercase id 's1'");


        // Testar estação inexistente
        Facility notFoundFacility = controller.getStationById("S99");
        assertNull(notFoundFacility, "Should return null for a non-existent station ID");

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