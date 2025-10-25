package Domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe Agregada {@link Order}.
 * Refeito para usar a classe OrderLine real.
 */
class OrderTest {

    private Order order;
    private final String ID = "ORD-001";
    private final LocalDateTime DUE_DATE = LocalDateTime.of(2025, 12, 31, 10, 0);
    private final int PRIORITY = 5;

    // Usando a classe OrderLine real
    private OrderLine line1;
    private OrderLine line2;

    @BeforeEach
    void setUp() {
        // Inicialização de OrderLines
        line1 = new OrderLine(1, "SKU-A", 10);
        line2 = new OrderLine(2, "SKU-B", 5);

        // Inicializa a Ordem
        // Assumindo que o construtor da Order aceita ID, Data Limite e Prioridade
        order = new Order(ID, DUE_DATE, PRIORITY);
    }

    // ------------------------------------
    // TESTES DE GETTERS
    // ------------------------------------

    @Test
    @DisplayName("GET: Deve retornar o ID da Ordem correto.")
    void getId() {
        assertEquals(ID, order.getId());
    }

    @Test
    @DisplayName("GET: getLines deve retornar a lista de linhas de encomenda (inicialmente vazia).")
    void getLines() {
        assertTrue(order.getLines().isEmpty());
    }

    // ------------------------------------
    // TESTES DE SETTERS E MANIPULAÇÃO DE LINHAS
    // ------------------------------------

    @Test
    @DisplayName("SET: setLines deve substituir a lista interna de linhas de encomenda.")
    void setLines() {
        // Arrange
        order.addLine(line1);
        List<OrderLine> newList = new ArrayList<>();
        newList.add(line2);

        // Act
        // Assume-se que Order tem um método setLines(List<OrderLine> lines)
        order.setLines(newList);

        // Assert
        assertEquals(1, order.getLines().size());
        assertTrue(order.getLines().contains(line2));
        assertFalse(order.getLines().contains(line1), "A linha original deve ter sido removida.");
    }

    @Test
    @DisplayName("ADD: addLine deve adicionar uma linha à lista interna.")
    void addLine() {
        // Act
        order.addLine(line1);
        order.addLine(line2);

        // Assert
        assertEquals(2, order.getLines().size());
        assertTrue(order.getLines().contains(line1));
        assertTrue(order.getLines().contains(line2));
    }

    // ------------------------------------
    // TESTES DE COMPARABLE
    // ------------------------------------

    @Test
    @DisplayName("COMPARE: Deve implementar a ordenação correta (Prioridade > Data Limite > ID) [ASC].")
    void compareTo() {
        // Arrange
        LocalDateTime DUE_FUTURE = DUE_DATE.plusDays(1);
        LocalDateTime DUE_PAST = DUE_DATE.minusDays(1);

        // 1. Ordem por Prioridade (ASC)
        Order p2 = new Order("A", DUE_DATE, 2); // Mais alta prioridade (menor número)
        Order p6 = new Order("B", DUE_DATE, 6); // Menor prioridade (maior número)
        // P5 (atual) deve vir DEPOIS de P2 (prioridade maior).
        assertTrue(order.compareTo(p2) > 0, "P5 deve ser maior que P2.");
        // P5 (atual) deve vir ANTES de P6 (prioridade menor).
        assertTrue(order.compareTo(p6) < 0, "P5 deve ser menor que P6.");

        // 2. Ordem por Data Limite (ASC) quando Prioridade é igual
        Order dPast = new Order("A", DUE_PAST, PRIORITY); // Data mais antiga (vem primeiro)
        Order dFuture = new Order("B", DUE_FUTURE, PRIORITY); // Data mais recente (vem depois)
        // Data atual (D) deve vir DEPOIS de data passada (D-1).
        assertTrue(order.compareTo(dPast) > 0, "Data D deve ser maior que data D-1.");
        // Data atual (D) deve vir ANTES de data futura (D+1).
        assertTrue(order.compareTo(dFuture) < 0, "Data D deve ser menor que data D+1.");

        // 3. Ordem por ID (ASC) quando Prioridade e Data Limite são iguais
        Order idLower = new Order("ORD-000", DUE_DATE, PRIORITY); // ID alfabeticamente menor (vem primeiro)
        Order idEqual = new Order("ORD-001", DUE_DATE, PRIORITY); // ID igual
        // ID '001' deve vir DEPOIS de ID '000'.
        assertTrue(order.compareTo(idLower) > 0, "ID maior deve ser maior que ID menor.");
        assertEquals(0, order.compareTo(idEqual), "IDs iguais devem retornar 0.");
    }
}