package Domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de testes para a classe Graph.
 * Foco: Adição de nós/arestas, vizinhança e integridade da estrutura.
 */
class GraphTest {

    private Graph graph;
    private Station s1;
    private Station s2;

    @BeforeEach
    void setUp() {
        graph = new Graph();
        s1 = new Station("S1", "Station 1", 40, -8, 10, 10);
        s2 = new Station("S2", "Station 2", 41, -7, 20, 20);
    }

    @Test
    @DisplayName("Deve adicionar estações e ignorar duplicados")
    void testAddStation() {
        graph.addStation(s1);
        graph.addStation(s1); // Duplicado

        assertEquals(1, graph.getStationCount());
        assertTrue(graph.containsStation("S1"));
        assertNotNull(graph.getStation("S1"));
    }

    @Test
    @DisplayName("Deve lançar exceção ao adicionar estação nula")
    void testAddStationNull() {
        assertThrows(IllegalArgumentException.class, () -> graph.addStation(null));
    }

    @Test
    @DisplayName("Deve adicionar conexões bidirecionais corretamente")
    void testAddConnection() {
        graph.addStation(s1);
        graph.addStation(s2);
        Connection conn = new Connection("S1", "S2", 10.0);

        graph.addConnection(conn);

        assertEquals(1, graph.getConnectionCount());
        assertEquals(2, graph.getAllConnections().size()); // S1->S2 e S2->S1

        // Verificar vizinhos
        assertTrue(graph.getNeighbors("S1").contains("S2"));
        assertTrue(graph.getNeighbors("S2").contains("S1"));
    }

    @Test
    @DisplayName("Deve falhar ao adicionar conexão se estações não existirem")
    void testAddConnectionMissingStations() {
        graph.addStation(s1);
        Connection conn = new Connection("S1", "S3", 10.0); // S3 não existe

        assertThrows(IllegalArgumentException.class, () -> graph.addConnection(conn));
    }

    @Test
    @DisplayName("Deve retornar lista vazia para vizinhos de estações inválidas ou inexistentes")
    void testGetNeighborsInvalid() {
        graph.addStation(s1);

        assertTrue(graph.getNeighbors(null).isEmpty());
        assertTrue(graph.getNeighbors("   ").isEmpty());
        assertTrue(graph.getNeighbors("NON_EXISTENT").isEmpty());
    }

    @Test
    @DisplayName("Deve filtrar conexões únicas corretamente (getAllUniqueConnections)")
    void testUniqueConnections() {
        graph.addStation(s1);
        graph.addStation(s2);
        graph.addConnection(new Connection("S1", "S2", 10.0));

        Set<Connection> unique = graph.getAllUniqueConnections();
        assertEquals(1, unique.size());

        // Verifica se o objeto retornado é o original (ou o seu reverso)
        Connection c = unique.iterator().next();
        assertTrue(c.getOriginId().equals("S1") || c.getOriginId().equals("S2"));
    }

    @Test
    @DisplayName("Deve testar getters de conveniência e toString")
    void testGettersAndToString() {
        graph.addStation(s1);
        graph.addStation(s2);
        graph.addConnection(new Connection("S1", "S2", 5.0));

        assertEquals(2, graph.getStationCount());
        assertEquals(1, graph.getConnectionCount());
        assertEquals("Graph{stations=2, connections=1}", graph.toString());

        // getConnections para uma estação específica
        assertEquals(1, graph.getConnections("S1").size());
        assertEquals(0, graph.getConnections("S3").size());
        assertEquals(0, graph.getConnections(null).size());
    }

    @Test
    @DisplayName("Deve validar getStation com IDs nulos ou com espaços")
    void testGetStationValidation() {
        graph.addStation(s1);
        assertNull(graph.getStation(null));
        assertNotNull(graph.getStation(" S1 ")); // Deve fazer trim
    }

    @Test
    @DisplayName("As listas retornadas devem ser imutáveis")
    void testImmutability() {
        graph.addStation(s1);
        List<Station> stations = graph.getStations();
        assertThrows(UnsupportedOperationException.class, () -> stations.add(s2));
    }
}