package Domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de testes completa para a classe Graph.
 * Testa a integridade da estrutura de dados, vizinhança e invariantes do domínio.
 */
class GraphTest {

    private Graph graph;
    private Station s1;
    private Station s2;
    private Station s3;

    @BeforeEach
    void setUp() {
        graph = new Graph();
        s1 = new Station("S1", "Porto", 41.1, -8.6, 100, 100);
        s2 = new Station("S2", "Lisboa", 38.7, -9.1, 200, 200);
        s3 = new Station("S3", "Coimbra", 40.2, -8.4, 150, 150);
    }

    // --- TESTES DE ESTAÇÕES (NÓS) ---

    @Test
    @DisplayName("Graph: Deve adicionar estações e ignorar IDs duplicados")
    void testAddStation() {
        graph.addStation(s1);
        graph.addStation(s1); // Tentativa de duplicado

        assertEquals(1, graph.getStationCount(), "Não deve contar a mesma estação duas vezes.");
        assertTrue(graph.containsStation("S1"));
        assertNotNull(graph.getStation("S1"));
    }

    @Test
    @DisplayName("Graph: Deve permitir pesquisar estação com espaços no ID")
    void testGetStationWithSpaces() {
        graph.addStation(s1);
        // O método getStation deve fazer trim() internamente
        assertNotNull(graph.getStation("  S1  "));
    }

    @Test
    @DisplayName("Graph: Deve lançar exceção ao adicionar estação nula")
    void testAddStationNull() {
        assertThrows(IllegalArgumentException.class, () -> graph.addStation(null));
    }

    // --- TESTES DE CONEXÕES (ARESTAS) ---

    @Test
    @DisplayName("Graph: Deve adicionar conexão e garantir que é bidirecional")
    void testAddConnectionBidirectional() {
        graph.addStation(s1);
        graph.addStation(s2);
        Connection conn = new Connection("S1", "S2", 300.0);

        graph.addConnection(conn);

        // Num grafo não-direcionado, S1 é vizinho de S2 e vice-versa
        assertEquals(1, graph.getConnectionCount(), "Deve haver apenas 1 conexão única.");
        assertTrue(graph.getNeighbors("S1").contains("S2"), "S2 deve ser vizinho de S1");
        assertTrue(graph.getNeighbors("S2").contains("S1"), "S1 deve ser vizinho de S2");
    }

    @Test
    @DisplayName("Graph: Deve falhar ao adicionar conexão se uma estação não existir")
    void testAddConnectionMissingStations() {
        graph.addStation(s1);
        // S3 não foi adicionada ao grafo ainda
        Connection conn = new Connection("S1", "S3", 120.0);

        assertThrows(IllegalArgumentException.class, () -> graph.addConnection(conn));
    }

    @Test
    @DisplayName("Graph: Deve filtrar corretamente conexões únicas")
    void testGetAllUniqueConnections() {
        graph.addStation(s1);
        graph.addStation(s2);
        graph.addStation(s3);

        graph.addConnection(new Connection("S1", "S2", 10.0));
        graph.addConnection(new Connection("S2", "S3", 20.0));

        Set<Connection> unique = graph.getAllUniqueConnections();

        // Embora internamente existam S1->S2, S2->S1, S2->S3, S3->S2...
        // O conjunto de conexões únicas deve ter apenas 2 elementos.
        assertEquals(2, unique.size());
    }

    // --- TESTES DE VIZINHANÇA E CONSULTA ---

    @Test
    @DisplayName("Graph: Deve retornar lista vazia para vizinhos de IDs inválidos")
    void testGetNeighborsInvalid() {
        graph.addStation(s1);
        assertTrue(graph.getNeighbors(null).isEmpty());
        assertTrue(graph.getNeighbors("NON_EXISTENT").isEmpty());
    }

    @Test
    @DisplayName("Graph: Deve listar todas as conexões de uma estação específica")
    void testGetConnectionsForStation() {
        graph.addStation(s1);
        graph.addStation(s2);
        graph.addStation(s3);
        graph.addConnection(new Connection("S1", "S2", 100.0));
        graph.addConnection(new Connection("S1", "S3", 50.0));

        List<Connection> s1Conns = graph.getConnections("S1");
        assertEquals(2, s1Conns.size(), "S1 deve ter duas conexões a sair/entrar.");
    }

    // --- TESTES DE INTEGRIDADE E IMUTABILIDADE ---

    @Test
    @DisplayName("Graph: A lista de estações retornada deve ser imutável")
    void testImmutabilityOfStations() {
        graph.addStation(s1);
        List<Station> stations = graph.getStations();

        // Tentar modificar a lista externa deve lançar exceção
        assertThrows(UnsupportedOperationException.class, () -> stations.add(s2));
    }

    @Test
    @DisplayName("Graph: ToString deve retornar formato esperado")
    void testToString() {
        graph.addStation(s1);
        graph.addStation(s2);
        graph.addConnection(new Connection("S1", "S2", 10.0));

        String expected = "Graph{stations=2, connections=1}";
        assertEquals(expected, graph.toString());
    }
}