package Domain;

import Domain.ENUM.AllocationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe de Valor {@link EligibilityResult},
 * verificando o comportamento de cada getter individualmente.
 */
class EligibilityResultTest {

    private EligibilityResult result;

    // Dados de teste usados para inicializar o objeto
    private final String ORDER_ID = "ORD-456";
    private final int LINE_NO = 3;
    private final String SKU = "SKU-XYZ";
    private final int REQUESTED_QTY = 50;
    private final int ALLOCATED_QTY = 35;
    private final AllocationStatus STATUS = AllocationStatus.PARTIAL;

    @BeforeEach
    void setUp() {
        // Inicializa o objeto antes de cada teste
        result = new EligibilityResult(
                ORDER_ID,
                LINE_NO,
                SKU,
                REQUESTED_QTY,
                ALLOCATED_QTY,
                STATUS
        );
    }

    @Test
    @DisplayName("GET: Deve retornar o OrderId correto.")
    void getOrderId() {
        assertEquals(ORDER_ID, result.getOrderId());
    }

    @Test
    @DisplayName("GET: Deve retornar o LineNo correto.")
    void getLineNo() {
        assertEquals(LINE_NO, result.getLineNo());
    }

    @Test
    @DisplayName("GET: Deve retornar o SKU correto.")
    void getSku() {
        assertEquals(SKU, result.getSku());
    }

    @Test
    @DisplayName("GET: Deve retornar a RequestedQty correta.")
    void getRequestedQty() {
        assertEquals(REQUESTED_QTY, result.getRequestedQty());
    }

    @Test
    @DisplayName("GET: Deve retornar a AllocatedQty correta.")
    void getAllocatedQty() {
        assertEquals(ALLOCATED_QTY, result.getAllocatedQty());
    }

    @Test
    @DisplayName("GET: Deve retornar o Status correto (AllocationStatus.PARTIAL).")
    void getStatus() {
        assertEquals(STATUS, result.getStatus());
        assertEquals(AllocationStatus.PARTIAL, result.getStatus());
    }
}