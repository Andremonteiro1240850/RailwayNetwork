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
 * Suite de testes robusta para o domínio de Grafos.
 * Cobre casos de sucesso, falha, caracteres especiais e formatação.
 */
class GraphDomainTest {

    private DotExporter exporter;

    @BeforeEach
    void setUp() {
        exporter = new DotExporter();
    }

    // --- TESTES DOTEXPORTER ---

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

    @Test
    @DisplayName("DotExporter: Deve garantir ponto decimal nas coordenadas (Locale US)")
    void testCoordinateFormatting() {
        // Testando se coordenadas com decimais longos são arredondadas e usam '.'
        Station s1 = new Station("A", "A", 0, 0, 10.5555, 20.7777);
        Graph graph = new StubGraph(Set.of(s1), Collections.emptySet());

        String result = exporter.exportToDot(graph);

        // Esperado: 10.56 e 20.78 (arredondamento de duas casas)
        assertTrue(result.contains("pos=\"10.56,20.78!\""));
    }

    @Test
    @DisplayName("DotExporter: Deve tratar nomes com aspas e IDs com caracteres especiais")
    void testSpecialCharactersEscaping() {
        // IDs com hífen precisam ser escapados com aspas no DOT pelo escapeNodeId
        Station s1 = new Station("S-01", "Gare \"Central\"", 0, 0, 10, 10);
        Graph graph = new StubGraph(Set.of(s1), Collections.emptySet());

        String result = exporter.exportToDot(graph);

        // Verifica se o ID S-01 foi colocado entre aspas e a aspa interna escapada
        assertTrue(result.contains("\"S-01\""));
        assertTrue(result.contains("label=\"Gare \\\"Central\\\"\""));
    }

    @Test
    @DisplayName("DotExporter: Deve retornar estrutura básica para grafo vazio")
    void testEmptyGraphExport() {
        Graph graph = new StubGraph(Collections.emptySet(), Collections.emptySet());
        String result = exporter.exportToDot(graph);
        assertEquals("graph G {\n}\n", result);
    }

    // --- TESTES STATION ---

    @Test
    @DisplayName("Station: Validação de argumentos do construtor")
    void testStationConstructorValidations() {
        assertThrows(IllegalArgumentException.class, () -> new Station(null, "Name", 0, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Station("ID", "  ", 0, 0, 0, 0));
        // Testando limites geográficos (exemplo: Latitude > 90)
        assertThrows(IllegalArgumentException.class, () -> new Station("ID", "Name", 95, 0, 0, 0));
    }

    @Test
    @DisplayName("Station: Getters e limpeza de strings (trim)")
    void testStationMethods() {
        Station s = new Station(" S1 ", " Porto ", 41.1, -8.6, 100, 200);
        assertEquals("S1", s.getId());
        assertEquals("Porto", s.getName());
        assertTrue(s.toString().contains("id='S1'"));
    }

    // --- TESTES CONNECTION ---

    @Test
    @DisplayName("Connection: Criação válida e imutabilidade dos IDs")
    void testConnectionValid() {
        Connection conn = new Connection("  A  ", "  B  ", 50.5);
        assertEquals("A", conn.getOriginId());
        assertEquals("B", conn.getDestinationId());
        assertEquals(50.5, conn.getDistance());
    }

    @ParameterizedTest
    @DisplayName("Connection: Lançar exceção para entradas inválidas")
    @CsvSource({
            " , B, 10",         // Origem nula
            "A, '', 10",        // Destino vazio
            "A, A, 10",         // Auto-loop (origem = destino)
            "A, B, -5.0"        // Distância negativa
    })
    void testConnectionInvalidInputs(String org, String dest, double dist) {
        assertThrows(IllegalArgumentException.class, () -> new Connection(org, dest, dist));
    }

    @Test
    @DisplayName("Connection: Inversão de sentido (reverse)")
    void testConnectionReverse() {
        Connection conn = new Connection("Lisboa", "Porto", 300.0);
        Connection rev = conn.reverse();

        assertEquals("Porto", rev.getOriginId());
        assertEquals("Lisboa", rev.getDestinationId());
        assertEquals(300.0, rev.getDistance());
    }

    @Test
    @DisplayName("Connection: Igualdade lógica (Equals/HashCode)")
    void testConnectionEquality() {
        Connection c1 = new Connection("A", "B", 10.0);
        Connection c2 = new Connection("A", "B", 10.0);
        Connection c3 = new Connection("B", "A", 10.0); // Sentido oposto

        assertEquals(c1, c2);
        assertNotEquals(c1, c3); // Em grafos direcionados/objetos distintos são diferentes
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    // --- STUB PARA GRAPH ---
    // Necessário para testar o Exporter sem depender da implementação real de Graph
    private static class StubGraph extends Graph {
        private final List<Station> stations;
        private final Set<Connection> connections;

        public StubGraph(Set<Station> stations, Set<Connection> connections) {
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