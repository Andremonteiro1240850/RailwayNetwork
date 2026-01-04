package Domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de testes completa para a classe Lin.e.
 * Cobre validações de argumentos, integridade de dados e formatação..
 */
class LineTest {

    // --- TESTES DE CONSTRUTOR E SUCESSO ---

    @Test
    @DisplayName("Line: Deve criar uma Line válida e aplicar trim nos IDs")
    void testLineConstructorValid() {
        // Exemplo: " Gare-Midi " deve tornar-se "Gare-Midi"
        Line line = new Line("  Gare-Midi  ", "  Central  ", 3.5, 100, -5.2);

        assertEquals("Gare-Midi", line.getDepartureStationId());
        assertEquals("Central", line.getArrivalStationId());
        assertEquals(3.5, line.getDistance());
        assertEquals(100, line.getCapacity());
        assertEquals(-5.2, line.getCost());
    }

    @Test
    @DisplayName("Line: Deve permitir custo negativo conforme especificação")
    void testNegativeCostAllowed() {
        // Em alguns algoritmos de grafos, custos negativos são permitidos (ex: ganhos ou subsídios)
        assertDoesNotThrow(() -> new Line("A", "B", 10.0, 50, -100.0));
    }

    // --- TESTES DE VALIDAÇÃO (ERROS) ---

    @ParameterizedTest
    @DisplayName("Line: Deve lançar exceção para IDs de estação inválidos")
    @CsvSource({
            "'', 'B', 1.0, 10, 5.0",       // Departure vazio
            "'A', ' ', 1.0, 10, 5.0",      // Arrival com espaços
            "'A', 'A', 1.0, 10, 5.0"       // Auto-loop (origem igual ao destino)
    })
    void testLineInvalidStationInputs(String dep, String arr, double dist, int cap, double cost) {
        assertThrows(IllegalArgumentException.class, () -> new Line(dep, arr, dist, cap, cost));
    }

    @ParameterizedTest
    @DisplayName("Line: Deve validar restrições numéricas (distância e capacidade >= 0)")
    @CsvSource({
            "'A', 'B', -0.1, 10, 5.0",     // Distância negativa
            "'A', 'B', 10.0, -1, 5.0"      // Capacidade negativa
    })
    void testLineInvalidNumericInputs(String dep, String arr, double dist, int cap, double cost) {
        assertThrows(IllegalArgumentException.class, () -> new Line(dep, arr, dist, cap, cost));
    }

    @Test
    @DisplayName("Line: Deve lançar exceção se IDs forem nulos")
    void testNullIds() {
        assertThrows(IllegalArgumentException.class, () -> new Line(null, "B", 1, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new Line("A", null, 1, 1, 1));
    }

    // --- TESTES DE COMPORTAMENTO E FORMATAÇÃO ---

    @Test
    @DisplayName("Line: Verifica se o toString está formatado corretamente (Locale US)")
    void testLineToString() {
        // Testamos se a distância e o custo aparecem com 2 casas decimais e separador ponto
        Line line = new Line("Gent", "Antwerp", 50.0, 24, 15.5);
        String expected = "Line{departure='Gent', arrival='Antwerp', dist=50.00 km, capacity=24, cost=15.50}";

        assertEquals(expected, line.toString(), "O toString deve seguir o padrão US com ponto decimal.");
    }

    @Test
    @DisplayName("Line: Verificação de imutabilidade")
    void testImmutability() {
        Line line = new Line("A", "B", 10, 5, 2);
        // Como a classe não tem setters e os campos são 'final',
        // a imutabilidade é garantida estruturalmente.
        // Testamos os getters para garantir que devolvem os valores originais.
        assertEquals(10.0, line.getDistance());
        assertEquals(5, line.getCapacity());
    }
}