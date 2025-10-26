package Domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe Value Object Imutável {@link PickingPlan}.
 * Valida o construtor, cálculos internos e gestão de coleções.
 */
class PickingPlanTest {

    private static final String HEURISTIC_NAME = "FFD";
    private List<Trolley> mockTrolleys;

    /**
     * Cria 2 trolleys de mock para testes (utilização de 50% e 100%).
     */
    @BeforeEach
    void setUp() {
        mockTrolleys = new ArrayList<>();
        // Trolley 1: 50% de utilização (Capacidade 100, Carga 50)
        mockTrolleys.add(new Trolley(100, 50.0, 1, new ArrayList<>(), HEURISTIC_NAME));
        // Trolley 2: 100% de utilização (Capacidade 200, Carga 200)
        mockTrolleys.add(new Trolley(200, 200.0, 2, new ArrayList<>(), HEURISTIC_NAME));
    }

    private PickingPlan createTestInstance() {
        return new PickingPlan(mockTrolleys, HEURISTIC_NAME);
    }

    @Test
    @DisplayName("HAPPY PATH: Deve inicializar corretamente e calcular a Utilização Média.")
    void constructor_And_AllGetters() {
        // Act
        PickingPlan plan = createTestInstance();

        // Esperado: Utilização em % (50% + 100%) / 2 trolleys = 75.0%
        double expectedUtilization = 75.0;

        // Assert
        assertNotNull(plan);
        assertEquals(HEURISTIC_NAME, plan.getHeuristic());
        assertEquals(2, plan.getTotalTrolleys());
        // Uso de delta para doubles
        assertEquals(expectedUtilization, plan.getAverageUtilization(), 0.001);
    }

    @Test
    @DisplayName("IMUTABILIDADE: getTrolleys deve retornar uma lista imutável.")
    void getTrolleys_ShouldReturnUnmodifiableList() {
        // Arrange
        PickingPlan plan = createTestInstance();

        // Act & Assert: Tentar modificar a lista retornada deve lançar exceção
        assertThrows(UnsupportedOperationException.class, () -> {
            plan.getTrolleys().add(new Trolley(10, 0.0, 3, new ArrayList<>(), ""));
        });
    }

    @Test
    @DisplayName("VALIDAÇÃO: Deve lançar exceção se a lista de Trolleys for nula.")
    void constructor_ShouldThrowException_WhenTrolleysIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PickingPlan(null, HEURISTIC_NAME);
        });
    }

    @Test
    @DisplayName("EDGE CASE: Deve lidar com a lista de Trolleys vazia, resultando em 0% de utilização.")
    void constructor_WithEmptyTrolleys_ShouldResultInZero() {
        // Arrange
        List<Trolley> emptyList = new ArrayList<>();

        // Act
        PickingPlan plan = new PickingPlan(emptyList, HEURISTIC_NAME);

        // Assert
        assertEquals(0, plan.getTotalTrolleys());
        assertEquals(0.0, plan.getAverageUtilization(), 0.001);
    }

    @Test
    @DisplayName("SEGURANÇA: Deve lidar com Trolley de Capacidade Zero (Divisão por Zero).")
    void constructor_WithZeroCapacityTrolley_ShouldHandleDivisionByZero() {
        // Arrange
        mockTrolleys.clear();
        // Trolley com Carga 10 e Capacidade 0 (utilização deve ser 0 para evitar erro)
        mockTrolleys.add(new Trolley(0, 10.0, 3, new ArrayList<>(), HEURISTIC_NAME));
        // Trolley com 100% de utilização (Capacidade 100, Carga 100)
        mockTrolleys.add(new Trolley(100, 100.0, 4, new ArrayList<>(), HEURISTIC_NAME));

        // Act
        PickingPlan plan = new PickingPlan(mockTrolleys, HEURISTIC_NAME);

        // Esperado: (0% (Trolley 1) + 100% (Trolley 2)) / 2 trolleys = 50.0%
        double expectedUtilization = 50.0;

        // Assert
        assertEquals(2, plan.getTotalTrolleys());
        assertEquals(expectedUtilization, plan.getAverageUtilization(), 0.001, "A utilização deve ser calculada corretamente, ignorando o Trolley de capacidade zero.");
    }
}