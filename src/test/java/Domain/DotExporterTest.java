package Domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Suite de testes completa para o domínio de Grafos.
 * Cobre: DotExporter, Station e Connection.
 */
class GraphDomainTest {

    private DotExporter exporter;

    @BeforeEach
    void setUp() {
        exporter = new DotExporter();
    }



    @Test
    @DisplayName("DotExporter: Deve exportar grafo completo com ordenação e estilos")
    void testFullExport() {
        Station s1 = new Station("A", "Station A", 0, 0, 10, 10);
        Station s2 = new Station("B", "Station B", 0, 0, 20, 20);
        Connection c1 = new Connection("A", "B", 100.0);

        Graph graph = new StubGraph(Set.of(s1, s2), Set.of(c1));
        String result = exporter.exportToDot(graph);

        assertTrue(result.contains("graph G {"));
        assertTrue(result.contains("A [label=\"Station A\", pos=\"10.00,10.00!\"];"));
        assertTrue(result.contains("A -- B;"));
        assertTrue(result.endsWith("}\n"));
    }

    // --- TESTES STATION ---

    @Test
    @DisplayName("Station: Validação de argumentos do construtor")
    void testStationConstructorValidations() {
        assertThrows(IllegalArgumentException.class, () -> new Station(null, "Name", 0, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Station("ID", "  ", 0, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Station("ID", "Name", 95, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Station("ID", "Name", 0, -190, 0, 0));
    }

    @Test
    @DisplayName("Station: Getters e métodos de objeto")
    void testStationMethods() {
        Station s = new Station(" S1 ", " Porto ", 41.1, -8.6, 100, 200);
        assertEquals("S1", s.getId()); // Testa o trim()
        assertEquals("Porto", s.getName()); // Testa o trim()
        assertEquals(41.1, s.getLatitude());
        assertEquals(-8.6, s.getLongitude());
        assertEquals(100, s.getCoordX());
        assertEquals(200, s.getCoordY());
        assertTrue(s.toString().contains("id='S1'"));
    }

    // --- TESTES CONNECTION ---

    @Test
    @DisplayName("Connection: Deve criar conexão válida e aplicar trim nos IDs")
    void testConnectionValid() {
        Connection conn = new Connection("  A  ", "  B  ", 50.5);
        assertEquals("A", conn.getOriginId());
        assertEquals("B", conn.getDestinationId());
        assertEquals(50.5, conn.getDistance());
    }

    @ParameterizedTest
    @DisplayName("Connection: Deve lançar exceção para inputs inválidos")
    @CsvSource({
            " , B, 10",         // Origem nula
            "A, '', 10",        // Destino vazio
            "A, A, 10",         // Origem igual ao destino
            "A, B, -1"          // Distância negativa
    })
    void testConnectionInvalidInputs(String org, String dest, double dist) {
        assertThrows(IllegalArgumentException.class, () -> new Connection(org, dest, dist));
    }

    @Test
    @DisplayName("Connection: Teste do método reverse")
    void testConnectionReverse() {
        Connection conn = new Connection("A", "B", 100.0);
        Connection rev = conn.reverse();

        assertEquals("B", rev.getOriginId());
        assertEquals("A", rev.getDestinationId());
        assertEquals(conn.getDistance(), rev.getDistance());
    }

    @Test
    @DisplayName("Connection: Equality e HashCode")
    void testConnectionEquality() {
        Connection c1 = new Connection("A", "B", 10.0);
        Connection c2 = new Connection("A", "B", 10.0);
        Connection c3 = new Connection("A", "C", 10.0);

        assertEquals(c1, c2);
        assertNotEquals(c1, c3);
        assertEquals(c1.hashCode(), c2.hashCode());
        assertTrue(c1.toString().contains("origin='A'"));
    }

    // --- STUB PARA GRAPH ---

    private static class StubGraph extends Graph {
        private final List<Station> stations;
        private final Set<Connection> connections;

        public StubGraph(Set<Station> stations, Set<Connection> connections) {
            // Convertemos o Set para List para coincidir com a assinatura esperada pelo DotExporter
            this.stations = new ArrayList<>(stations);
            this.connections = connections;
        }

        @Override
        public List<Station> getStations() {
            return stations;
        }

        @Override
        public Set<Connection> getAllUniqueConnections() {
            return connections;
        }
    }
}