package Domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe Line.
 * Garante a integridade das validações de negócio e formatação de dados.
 */
class LineTest {

    @Test
    @DisplayName("Deve criar uma Line válida e aplicar trim nos IDs")
    void testLineConstructorValid() {
        // Line(departure, arrival, distance, capacity, cost)
        Line line = new Line("  Gare-Midi  ", "  Central  ", 3.5, 100, -5.2);

        assertEquals("Gare-Midi", line.getDepartureStationId());
        assertEquals("Central", line.getArrivalStationId());
        assertEquals(3.5, line.getDistance());
        assertEquals(100, line.getCapacity());
        assertEquals(-5.2, line.getCost());
    }

    @ParameterizedTest
    @DisplayName("Deve lançar IllegalArgumentException para inputs inválidos de texto e lógica")
    @CsvSource({
            "'', 'B', 1.0, 10, 5.0",       // Departure vazio
            "'A', ' ', 1.0, 10, 5.0",      // Arrival vazio
            "'A', 'A', 1.0, 10, 5.0"       // Mesma estação (origem == destino)
    })
    void testLineInvalidStationInputs(String dep, String arr, double dist, int cap, double cost) {
        assertThrows(IllegalArgumentException.class, () -> new Line(dep, arr, dist, cap, cost));
    }

    @ParameterizedTest
    @DisplayName("Deve validar restrições numéricas (distância e capacidade >= 0, custo livre)")
    @CsvSource({
            "'A', 'B', -0.1, 10, 5.0",     // Distância negativa
            "'A', 'B', 10.0, -1, 5.0"      // Capacidade negativa
    })
    void testLineInvalidNumericInputs(String dep, String arr, double dist, int cap, double cost) {
        assertThrows(IllegalArgumentException.class, () -> new Line(dep, arr, dist, cap, cost));
    }

    @Test
    @DisplayName("Deve permitir custo negativo conforme especificação")
    void testNegativeCostAllowed() {
        assertDoesNotThrow(() -> new Line("A", "B", 10.0, 50, -100.0));
    }

    @Test
    @DisplayName("Deve lançar exceção se IDs forem nulos")
    void testNullIds() {
        assertThrows(IllegalArgumentException.class, () -> new Line(null, "B", 1, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new Line("A", null, 1, 1, 1));
    }

    @Test
    @DisplayName("Verifica se o toString está formatado corretamente")
    void testLineToString() {
        Line line = new Line("Gent", "Antwerp", 50.0, 24, 15.5);
        String expected = "Line{departure='Gent', arrival='Antwerp', dist=50.00 km, capacity=24, cost=15.50}";
        assertEquals(expected, line.toString());
    }
}