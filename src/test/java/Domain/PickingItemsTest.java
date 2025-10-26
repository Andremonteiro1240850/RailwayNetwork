package Domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe mutável {@link PickingItems}.
 * Valida a correta inicialização via construtor (Getters) e
 * a capacidade de modificar o estado (Setters) e a representação (toString).
 */
class PickingItemsTest {

    // Dados de Teste - Iniciais (ARRANGE)
    private static final String INITIAL_ORDER_ID = "ORD-P-1234";
    private static final int INITIAL_LINE_NO = 1;
    private static final int INITIAL_AISLE_ID = 5;
    private static final int INITIAL_BAY_ID = 10;
    private static final String INITIAL_BOX_ID = "B-SMALL-01";
    private static final String INITIAL_SKU = "ITEM-A01";
    private static final int INITIAL_QTY = 50;

    // Dados de Teste - Novos Valores (ACT para Setters)
    private static final String NEW_ORDER_ID = "ORD-P-5678";
    private static final int NEW_LINE_NO = 2;
    private static final int NEW_AISLE_ID = 6;
    private static final int NEW_BAY_ID = 11;
    private static final String NEW_BOX_ID = "B-LARGE-02";
    private static final String NEW_SKU = "ITEM-B02";
    private static final int NEW_QTY = 100;

    /**
     * Auxiliar para criar uma instância consistente de PickingItems.
     */
    private PickingItems createInitialInstance() {
        return new PickingItems(
                INITIAL_ORDER_ID, INITIAL_LINE_NO, INITIAL_AISLE_ID, INITIAL_BAY_ID,
                INITIAL_BOX_ID, INITIAL_SKU, INITIAL_QTY
        );
    }

    // ------------------------------------
    // TESTES DE GETTERS
    // ------------------------------------

    @Test
    @DisplayName("GET: Deve retornar o Order ID inicial correto.")
    void getOrderId() {
        // Arrange
        PickingItems item = createInitialInstance();
        // Assert
        assertEquals(INITIAL_ORDER_ID, item.getOrderId());
    }

    @Test
    @DisplayName("GET: Deve retornar o Line Number inicial correto.")
    void getLineNo() {
        // Arrange
        PickingItems item = createInitialInstance();
        // Assert
        assertEquals(INITIAL_LINE_NO, item.getLineNo());
    }

    @Test
    @DisplayName("GET: Deve retornar o Aisle ID inicial correto.")
    void getAisleId() {
        // Arrange
        PickingItems item = createInitialInstance();
        // Assert
        assertEquals(INITIAL_AISLE_ID, item.getAisleId());
    }

    @Test
    @DisplayName("GET: Deve retornar o Bay ID inicial correto.")
    void getBayId() {
        // Arrange
        PickingItems item = createInitialInstance();
        // Assert
        assertEquals(INITIAL_BAY_ID, item.getBayId());
    }

    @Test
    @DisplayName("GET: Deve retornar o Box ID inicial correto.")
    void getBoxId() {
        // Arrange
        PickingItems item = createInitialInstance();
        // Assert
        assertEquals(INITIAL_BOX_ID, item.getBoxId());
    }

    @Test
    @DisplayName("GET: Deve retornar o SKU inicial correto.")
    void getSku() {
        // Arrange
        PickingItems item = createInitialInstance();
        // Assert
        assertEquals(INITIAL_SKU, item.getSku());
    }

    @Test
    @DisplayName("GET: Deve retornar a Quantidade (Qty) inicial correta.")
    void getQty() {
        // Arrange
        PickingItems item = createInitialInstance();
        // Assert
        assertEquals(INITIAL_QTY, item.getQty());
    }

    // ------------------------------------
    // TESTES DE SETTERS
    // ------------------------------------

    @Test
    @DisplayName("SET: Deve alterar o Order ID corretamente.")
    void setOrderId() {
        // Arrange
        PickingItems item = createInitialInstance();
        // Act
        item.setOrderId(NEW_ORDER_ID);
        // Assert: A alteração deve ser refletida imediatamente no getter.
        assertEquals(NEW_ORDER_ID, item.getOrderId());
    }

    @Test
    @DisplayName("SET: Deve alterar o Line Number corretamente.")
    void setLineNo() {
        // Arrange
        PickingItems item = createInitialInstance();
        // Act
        item.setLineNo(NEW_LINE_NO);
        // Assert
        assertEquals(NEW_LINE_NO, item.getLineNo());
    }

    @Test
    @DisplayName("SET: Deve alterar o Aisle ID corretamente.")
    void setAisleId() {
        // Arrange
        PickingItems item = createInitialInstance();
        // Act
        item.setAisleId(NEW_AISLE_ID);
        // Assert
        assertEquals(NEW_AISLE_ID, item.getAisleId());
    }

    @Test
    @DisplayName("SET: Deve alterar o Bay ID corretamente.")
    void setBayId() {
        // Arrange
        PickingItems item = createInitialInstance();
        // Act
        item.setBayId(NEW_BAY_ID);
        // Assert
        assertEquals(NEW_BAY_ID, item.getBayId());
    }

    @Test
    @DisplayName("SET: Deve alterar o Box ID corretamente.")
    void setBoxId() {
        // Arrange
        PickingItems item = createInitialInstance();
        // Act
        item.setBoxId(NEW_BOX_ID);
        // Assert
        assertEquals(NEW_BOX_ID, item.getBoxId());
    }

    @Test
    @DisplayName("SET: Deve alterar o SKU corretamente.")
    void setSku() {
        // Arrange
        PickingItems item = createInitialInstance();
        // Act
        item.setSku(NEW_SKU);
        // Assert
        assertEquals(NEW_SKU, item.getSku());
    }

    @Test
    @DisplayName("SET: Deve alterar a Quantidade (Qty) corretamente.")
    void setQty() {
        // Arrange
        PickingItems item = createInitialInstance();
        // Act
        item.setQty(NEW_QTY);
        // Assert
        assertEquals(NEW_QTY, item.getQty());
    }

    // ------------------------------------
    // TESTE DE UTILIDADE
    // ------------------------------------

    @Test
    @DisplayName("TOSTRING: Deve gerar uma representação em String que contenha todos os campos.")
    void testToString() {
        // Arrange
        PickingItems item = createInitialInstance();
        // Act
        String result = item.toString();

        // Assert: Verifica se a string contém substrings de todos os campos
        // Isso garante que o método toString está a incluir todas as propriedades.
        assertTrue(result.contains("PickingItems{"));
        assertTrue(result.contains("orderId='" + INITIAL_ORDER_ID + "'"));
        assertTrue(result.contains("lineNo=" + INITIAL_LINE_NO));
        assertTrue(result.contains("aisleId=" + INITIAL_AISLE_ID));
        assertTrue(result.contains("bayId=" + INITIAL_BAY_ID));
        assertTrue(result.contains("boxId='" + INITIAL_BOX_ID + "'"));
        assertTrue(result.contains("sku='" + INITIAL_SKU + "'"));
        assertTrue(result.contains("qty=" + INITIAL_QTY));
    }
}