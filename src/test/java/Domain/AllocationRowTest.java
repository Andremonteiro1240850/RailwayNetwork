package Domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Classe de testes unitários para a entidade de domínio {@link AllocationRow}.
 * Garante que o construtor e todos os métodos de acesso (Getters) funcionam
 * corretamente, validando a integridade do Value Object.
 */
class AllocationRowTest {

    // Dados de Teste (Utilização de constantes para cumprir o princípio DRY)
    private static final String ORDER_ID_TEST = "ORD-2023-A789";
    private static final int LINE_NO_TEST = 10;
    private static final String SKU_TEST = "PROD-XYZ-999";
    private static final int QTY_TEST = 150;
    private static final String BOX_ID_TEST = "BOX-ALOC-005";
    private static final int AISLE_ID_TEST = 20;
    private static final int BAY_ID_TEST = 8;

    /**
     * Auxiliar para criar uma instância completa e consistente de AllocationRow.
     * @return Uma nova instância de AllocationRow com dados de teste.
     */
    private AllocationRow createTestInstance() {
        return new AllocationRow(
                ORDER_ID_TEST, LINE_NO_TEST, SKU_TEST, QTY_TEST,
                BOX_ID_TEST, AISLE_ID_TEST, BAY_ID_TEST
        );
    }

    /**
     * Testa se o Order ID é corretamente inicializado e retornado.
     */
    @Test
    @DisplayName("Valida getOrderId(): Deve retornar o ID do pedido correto.")
    void getOrderId() {
        // Arrange
        AllocationRow row = createTestInstance();

        // Assert
        assertNotNull(row.getOrderId(), "O Order ID não deve ser nulo.");
        assertEquals(ORDER_ID_TEST, row.getOrderId(), "O Order ID retornado não corresponde ao valor inicializado.");
    }

    /**
     * Testa se o Line Number é corretamente inicializado e retornado.
     */
    @Test
    @DisplayName("Valida getLineNo(): Deve retornar o número da linha correto.")
    void getLineNo() {
        // Arrange
        AllocationRow row = createTestInstance();

        // Assert
        assertEquals(LINE_NO_TEST, row.getLineNo(), "O Line Number retornado não corresponde ao valor inicializado.");
    }

    /**
     * Testa se o SKU (Stock Keeping Unit) é corretamente inicializado e retornado.
     */
    @Test
    @DisplayName("Valida getSku(): Deve retornar o SKU correto do produto.")
    void getSku() {
        // Arrange
        AllocationRow row = createTestInstance();

        // Assert
        assertNotNull(row.getSku(), "O SKU não deve ser nulo.");
        assertEquals(SKU_TEST, row.getSku(), "O SKU retornado não corresponde ao valor inicializado.");
    }

    /**
     * Testa se a Quantidade (Qty) é corretamente inicializada e retornada.
     */
    @Test
    @DisplayName("Valida getQty(): Deve retornar a quantidade alocada correta.")
    void getQty() {
        // Arrange
        AllocationRow row = createTestInstance();

        // Assert
        assertEquals(QTY_TEST, row.getQty(), "A Quantidade (Qty) retornada não corresponde ao valor inicializado.");
    }

    /**
     * Testa se o Box ID é corretamente inicializado e retornado.
     */
    @Test
    @DisplayName("Valida getBoxId(): Deve retornar o ID da caixa correto.")
    void getBoxId() {
        // Arrange
        AllocationRow row = createTestInstance();

        // Assert
        assertNotNull(row.getBoxId(), "O Box ID não deve ser nulo.");
        assertEquals(BOX_ID_TEST, row.getBoxId(), "O Box ID retornado não corresponde ao valor inicializado.");
    }

    /**
     * Testa se o Aisle ID (Corredor) é corretamente inicializado e retornado.
     */
    @Test
    @DisplayName("Valida getAisle(): Deve retornar o ID do corredor (Aisle) correto.")
    void getAisle() {
        // Arrange
        AllocationRow row = createTestInstance();

        // Assert
        assertEquals(AISLE_ID_TEST, row.getAisle(), "O Aisle ID retornado não corresponde ao valor inicializado.");
    }

    /**
     * Testa se o Bay ID (Baia/Prateleira) é corretamente inicializado e retornado.
     */
    @Test
    @DisplayName("Valida getBay(): Deve retornar o ID da baia (Bay) correto.")
    void getBay() {
        // Arrange
        AllocationRow row = createTestInstance();

        // Assert
        assertEquals(BAY_ID_TEST, row.getBay(), "O Bay ID retornado não corresponde ao valor inicializado.");
    }
}