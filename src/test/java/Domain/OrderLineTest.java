package Domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe Value Object OrderLine.
 */
class OrderLineTest {

    private OrderLine line;
    private final int LINE_NO = 1;
    private final String SKU = "SKU-PROD-X";
    private final int REQUESTED_QTY = 100;
    private final String INITIAL_STATUS = "ELIGIBLE";
    private final int INITIAL_ALLOCATED_QTY = 0;

    @BeforeEach
    void setUp() {
        // Inicializa a linha de encomenda antes de cada teste
        line = new OrderLine(LINE_NO, SKU, REQUESTED_QTY);
    }

    // ------------------------------------
    // TESTES DE CONSTRUTOR E GETTERS
    // ------------------------------------

    @Test
    @DisplayName("GET: getLineNo deve retornar o número da linha correto.")
    void getLineNo() {
        assertEquals(LINE_NO, line.getLineNo());
    }

    @Test
    @DisplayName("GET: getSku deve retornar o SKU correto.")
    void getSku() {
        assertEquals(SKU, line.getSku());
    }

    @Test
    @DisplayName("GET: getRequestedQty deve retornar a quantidade pedida correta.")
    void getRequestedQty() {
        assertEquals(REQUESTED_QTY, line.getRequestedQty());
    }

    @Test
    @DisplayName("GET: getAllocatedQty deve retornar 0 na inicialização.")
    void getAllocatedQty() {
        assertEquals(INITIAL_ALLOCATED_QTY, line.getAllocatedQty());
    }

    @Test
    @DisplayName("GET: getStatus deve retornar 'ELIGIBLE' na inicialização.")
    void getStatus() {
        assertEquals(INITIAL_STATUS, line.getStatus());
    }

    // ------------------------------------
    // TESTES DE SETTERS
    // ------------------------------------

    @Test
    @DisplayName("SET: setAllocatedQty deve atualizar a quantidade alocada.")
    void setAllocatedQty() {
        final int NEW_QTY = 50;
        line.setAllocatedQty(NEW_QTY);
        assertEquals(NEW_QTY, line.getAllocatedQty(), "A quantidade alocada deve ser atualizada para " + NEW_QTY);
    }

    @Test
    @DisplayName("SET: setStatus deve atualizar o status da linha.")
    void setStatus() {
        final String NEW_STATUS = "UNDISPATCHABLE";
        line.setStatus(NEW_STATUS);
        assertEquals(NEW_STATUS, line.getStatus(), "O status deve ser atualizado para UNDISPATCHABLE");
    }

    // ------------------------------------
    // TESTES DE UTILIDADE
    // ------------------------------------

    @Test
    @DisplayName("UTIL: toString deve retornar a representação em String completa do objeto, incluindo o estado atual.")
    void testToString() {
        // Estado inicial
        String initialExpected = "OrderLine{" +
                "lineNo=" + LINE_NO +
                ", sku='" + SKU + '\'' +
                ", requestedQty=" + REQUESTED_QTY +
                ", allocatedQty=" + INITIAL_ALLOCATED_QTY +
                ", status='" + INITIAL_STATUS + '\'' +
                '}';
        assertEquals(initialExpected, line.toString(), "O toString inicial está incorreto.");

        // Estado após modificação
        final int MODIFIED_QTY = 75;
        final String MODIFIED_STATUS = "PARTIAL";
        line.setAllocatedQty(MODIFIED_QTY);
        line.setStatus(MODIFIED_STATUS);

        String modifiedExpected = "OrderLine{" +
                "lineNo=" + LINE_NO +
                ", sku='" + SKU + '\'' +
                ", requestedQty=" + REQUESTED_QTY +
                ", allocatedQty=" + MODIFIED_QTY +
                ", status='" + MODIFIED_STATUS + '\'' +
                '}';
        assertEquals(modifiedExpected, line.toString(), "O toString modificado está incorreto.");
    }
}