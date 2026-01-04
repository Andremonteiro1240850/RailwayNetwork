package Controller;

import Domain.DataLoader;
import Domain.Station;
import Repository.HubRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class HubControllerTest {
    private HubController controller;
    private Station A, B, C, D, E, F;

    private static final double EPS = 1e-9;

    /**
     * Método auxiliar para criar Station com valores dummy.
     */
    private Station st(String id) {
        return new Station(id, "Station " + id, 0.0, 0.0, 0.0, 0.0);
    }

    @BeforeEach
    void setup() {
        // Criar DataLoader "fake" que retorna grafo vazio
        DataLoader loader = new DataLoader() {
            @Override
            public Map<Station, Map<Station, Double>> buildGraph() {
                return new HashMap<>(); // grafo vazio por defeito
            }
        };

        // Criar HubRepository com DataLoader
        HubRepository repo = new HubRepository(loader);

        // Criar HubController
        controller = new HubController(repo);

        // Criar estações dummy
        A = st("A");
        B = st("B");
        C = st("C");
        D = st("D");
        E = st("E");
        F = st("F");
    }
    /* =========================================================
       BETWENNESS CENTRALITY – BRANDES
       ========================================================= */

    @Test
    void betweenness_emptyGraph() {
        assertTrue(controller.calculateBetweennessCentrality(new HashMap<>()).isEmpty());
    }

    @Test
    void betweenness_singleIsolatedNode() {
        Map<Station, Map<Station, Double>> g = Map.of(A, Map.of());
        assertEquals(0.0, controller.calculateBetweennessCentrality(g).get(A), EPS);
    }

    @Test
    void betweenness_twoConnectedNodes() {
        Map<Station, Map<Station, Double>> g = new HashMap<>();
        g.put(A, Map.of(B, 1.0));
        g.put(B, Map.of(A, 1.0));

        var bc = controller.calculateBetweennessCentrality(g);
        assertEquals(0.0, bc.get(A), EPS);
        assertEquals(0.0, bc.get(B), EPS);
    }

    @Test
    void betweenness_lineGraph() {
        Map<Station, Map<Station, Double>> g = new HashMap<>();
        g.put(A, Map.of(B, 1.0));
        g.put(B, Map.of(A, 1.0, C, 1.0));
        g.put(C, Map.of(B, 1.0));

        var bc = controller.calculateBetweennessCentrality(g);
        assertEquals(1.0, bc.get(B), EPS);
    }

    @Test
    void betweenness_starGraph() {
        Map<Station, Map<Station, Double>> g = new HashMap<>();
        g.put(A, Map.of(B,1.0,C,1.0,D,1.0));
        g.put(B, Map.of(A,1.0));
        g.put(C, Map.of(A,1.0));
        g.put(D, Map.of(A,1.0));

        var bc = controller.calculateBetweennessCentrality(g);
        assertTrue(bc.get(A) > 0);
        assertEquals(0.0, bc.get(B), EPS);
    }

    @Test
    void betweenness_equalWeightMultiplePaths() {
        Map<Station, Map<Station, Double>> g = new HashMap<>();
        g.put(A, Map.of(B,1.0,C,1.0));
        g.put(B, Map.of(A,1.0,D,1.0));
        g.put(C, Map.of(A,1.0,D,1.0));
        g.put(D, Map.of(B,1.0,C,1.0));

        var bc = controller.calculateBetweennessCentrality(g);
        assertEquals(bc.get(B), bc.get(C), EPS);
    }

    @Test
    void betweenness_differentWeightPaths() {
        Map<Station, Map<Station, Double>> g = new HashMap<>();
        g.put(A, Map.of(B,1.0,C,5.0));
        g.put(B, Map.of(A,1.0,D,1.0));
        g.put(C, Map.of(A,5.0,D,1.0));
        g.put(D, Map.of(B,1.0,C,1.0));

        var bc = controller.calculateBetweennessCentrality(g);
        assertTrue(bc.get(B) > bc.get(C));
    }

    @Test
    void betweenness_disconnectedGraph() {
        Map<Station, Map<Station, Double>> g = new HashMap<>();
        g.put(A, Map.of(B,1.0));
        g.put(B, Map.of(A,1.0));
        g.put(C, Map.of());

        var bc = controller.calculateBetweennessCentrality(g);
        assertEquals(0.0, bc.get(C), EPS);
    }

    @Test
    void betweenness_cycleGraph() {
        Map<Station, Map<Station, Double>> g = new HashMap<>();
        g.put(A, Map.of(B,1.0,D,1.0));
        g.put(B, Map.of(A,1.0,C,1.0));
        g.put(C, Map.of(B,1.0,D,1.0));
        g.put(D, Map.of(A,1.0,C,1.0));

        var bc = controller.calculateBetweennessCentrality(g);
        assertEquals(bc.get(A), bc.get(B), EPS);
    }
    /* =========================================================
       2. DIJKSTRA + HARMONIC CLOSENESS
       ========================================================= */

    @Test
    void dijkstra_originWithoutNeighbors() {
        Map<Station, Map<Station, Double>> g = Map.of(A, Map.of());
        var r = controller.djikstra(A, g, Set.of(A));
        assertEquals(0.0, r.get(A), EPS);
    }

    @Test
    void dijkstra_noPathToDestination() {
        Map<Station, Map<Station, Double>> g = Map.of(A, Map.of(B,1.0));
        var r = controller.djikstra(A, g, Set.of(A,B,C));
        assertEquals(Double.MAX_VALUE, r.get(C));
    }

    @Test
    void dijkstra_moreHopsLessWeight() {
        Map<Station, Map<Station, Double>> g = new HashMap<>();
        g.put(A, Map.of(B,10.0,C,1.0));
        g.put(C, Map.of(D,1.0));
        g.put(D, Map.of(B,1.0));
        g.put(B, Map.of());

        var r = controller.djikstra(A, g, g.keySet());
        assertEquals(3.0, r.get(B), EPS);
    }

    @Test
    void harmonic_fullyConnectedGraph() {
        Map<Station, Map<Station, Double>> g = new HashMap<>();
        g.put(A, Map.of(B,1.0,C,1.0));
        g.put(B, Map.of(A,1.0,C,1.0));
        g.put(C, Map.of(A,1.0,B,1.0));

        var hc = controller.calculateHarmonicCloseness(g);
        assertEquals(hc.get(A), hc.get(B), EPS);
    }

    @Test
    void harmonic_withUnreachableNodes() {
        Map<Station, Map<Station, Double>> g = Map.of(A, Map.of(B,1.0), B, Map.of());
        var hc = controller.calculateHarmonicCloseness(g);
        assertEquals(1.0, hc.get(A), EPS);
    }

    @Test
    void harmonic_withVerySmallWeights() {
        Map<Station, Map<Station, Double>> g = Map.of(A, Map.of(B,1e-6), B, Map.of(A,1e-6));
        var hc = controller.calculateHarmonicCloseness(g);
        assertTrue(hc.get(A) > 1e5);
    }

    /* =========================================================
       3. STRENGTH + NORMALIZAÇÃO
       ========================================================= */

    @Test
    void strength_variedWeights() {
        Map<Station, Map<Station, Double>> g = Map.of(A, Map.of(B,2.0,C,3.0));
        var s = controller.calculateStrengthNorm(g);
        assertEquals(1.0, s.get(A), EPS);
    }

    @Test
    void strength_allZero() {
        Map<Station, Map<Station, Double>> g = Map.of(A, Map.of());
        assertEquals(0.0, controller.calculateStrengthNorm(g).get(A), EPS);
    }

    @Test
    void strength_singleConnectedNode() {
        Map<Station, Map<Station, Double>> g = Map.of(A, Map.of(B,1.0));
        var s = controller.calculateStrengthNorm(g);
        assertEquals(1.0, s.get(A), EPS);
        assertEquals(0.0, s.get(B), EPS);
    }

    /* =========================================================
       4. DEGREE
       ========================================================= */

    @Test
    void degree_isolatedNode() {
        var d = controller.calculateDegree(Map.of(A, Map.of()));
        assertEquals(0, d.get(A));
    }

    @Test
    void degree_fullyConnectedNode() {
        Map<Station, Map<Station, Double>> g = new HashMap<>();
        g.put(A, Map.of(B,1.0,C,1.0,D,1.0));
        g.put(B, Map.of(A,1.0));
        g.put(C, Map.of(A,1.0));
        g.put(D, Map.of(A,1.0));

        assertEquals(3, controller.calculateDegree(g).get(A));
    }

    /* =========================================================
       5. HUB SCORE
       ========================================================= */

    @Test
    void hubScore_allMetricsPresent() {
        var r = controller.calculateHubScore(
                Map.of(A,1.0),
                Map.of(A,1.0),
                Map.of(A,1.0),
                Set.of(A)
        );
        assertEquals(1.0, r.get(A), EPS);
    }

    @Test
    void hubScore_missingMetricThrows() {
        assertThrows(IllegalStateException.class, () ->
                controller.calculateHubScore(
                        Map.of(A,1.0),
                        Map.of(),
                        Map.of(A,1.0),
                        Set.of(A)
                ));
    }

    @Test
    void hubScore_allZero() {
        var r = controller.calculateHubScore(
                Map.of(A,0.0),
                Map.of(A,0.0),
                Map.of(A,0.0),
                Set.of(A)
        );
        assertEquals(0.0, r.get(A), EPS);
    }

    @Test
    void hubScore_emptyStationSet() {
        assertTrue(controller.calculateHubScore(
                Map.of(), Map.of(), Map.of(), Set.of()
        ).isEmpty());
    }

    /* =========================================================
       6. NORMALIZAÇÕES GERAIS
       ========================================================= */

    @Test
    void normalization_emptyMap() {
        assertTrue(controller.normalizeBetweenness(Map.of()).isEmpty());
    }

    @Test
    void normalization_allZero() {
        var r = controller.normalizeHarmonicCloseness(Map.of(A,0.0));
        assertEquals(0.0, r.get(A), EPS);
    }

    @Test
    void normalization_singlePositive() {
        var r = controller.normalizeBetweenness(Map.of(A,5.0));
        assertEquals(1.0, r.get(A), EPS);
    }

    @Test
    void normalization_scaleExtremes() {
        var r = controller.normalizeBetweenness(Map.of(A,1e9,B,1e-3));
        assertEquals(1.0, r.get(A), EPS);
        assertTrue(r.get(B) < 1e-9);
    }
}
