package Controller;

import Domain.QuarantineInspection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para ItemController (JUnit 5, sem Mockito)
 */
class ItemControllerTest {

    private ItemController itemController;
    private FakeInspectionController fakeInspection;

    @BeforeEach
    void setUp() {
        fakeInspection = new FakeInspectionController();
        itemController = new ItemController(fakeInspection);
    }

    // ==========================
    // Validações de entrada
    // ==========================
    @Test
    void deveRetornarErroQuandoSkuNuloOuVazio() {
        String r1 = itemController.processReturn(null, 10, "REMORSE", LocalDate.now());
        String r2 = itemController.processReturn("   ", 10, "REMORSE", LocalDate.now());

        assertEquals("ERROR: SKU can't be null or empty", r1);
        assertEquals("ERROR: SKU can't be null or empty", r2);
    }

    @Test
    void deveRetornarErroQuandoQuantidadeInvalida() {
        String r1 = itemController.processReturn("SKU1", 0, "DAMAGED", LocalDate.now());
        String r2 = itemController.processReturn("SKU1", -3, "DAMAGED", LocalDate.now());

        assertEquals("ERROR: Quantity must be greater than 0", r1);
        assertEquals("ERROR: Quantity must be greater than 0", r2);
    }

    @Test
    void deveRetornarErroQuandoMotivoNuloOuVazio() {
        String r1 = itemController.processReturn("SKU1", 1, null, LocalDate.now());
        String r2 = itemController.processReturn("SKU1", 1, "  ", LocalDate.now());

        assertEquals("ERROR: Return reason can't be null or empty", r1);
        assertEquals("ERROR: Return reason can't be null or empty", r2);
    }

    @Test
    void deveRetornarErroQuandoMotivoInvalido() {
        String r = itemController.processReturn("SKU1", 1, "INVALIDO", LocalDate.now());
        assertEquals("ERROR: Invalid Motive. Use: REMORSE, DAMAGED, EXPIRED ou CYCLE COUNT", r);
    }

    // ==========================
    // Encaminhamento ao InspectionController
    // ==========================
    @Test
    void deveEncaminharParaInspectionControllerComSucesso() {
        fakeInspection.nextResult = "OK";
        String result = itemController.processReturn("SKU1", 5, "REMORSE", LocalDate.of(2025, 12, 31));

        assertEquals("OK", result);
        assertEquals("SKU1", fakeInspection.lastSku);
        assertEquals(5, fakeInspection.lastQty);
        assertEquals("REMORSE", fakeInspection.lastReason);
        assertEquals(LocalDate.of(2025, 12, 31), fakeInspection.lastExpiry);
    }

    @Test
    void deveEncaminharComValoresDeRestockEDiscard() {
        fakeInspection.nextResult = "PARCIAL";
        String result = itemController.processReturn("SKU2", 10, "DAMAGED", LocalDate.now(), 3, 7);

        assertEquals("PARCIAL", result);
        assertEquals(Integer.valueOf(3), fakeInspection.lastQtyRestocked);
        assertEquals(Integer.valueOf(7), fakeInspection.lastQtyDiscarded);
    }

    @Test
    void deveRetornarErroQuandoInspectionControllerLancaExcecao() {
        fakeInspection.shouldThrow = true;
        fakeInspection.throwMessage = "falha intencional";

        String r = itemController.processReturn("SKU3", 2, "EXPIRED", LocalDate.now());
        assertEquals("ERROR processing return: falha intencional", r);
    }

    // ==========================
    // getReturnsBySku
    // ==========================
    @Test
    void deveRetornarListaDeInspecoesPorSku() {
        List<QuarantineInspection> lista = new ArrayList<>();
        lista.add(new QuarantineInspection());
        lista.add(new QuarantineInspection());
        fakeInspection.nextInspections = lista;

        List<QuarantineInspection> res = itemController.getReturnsBySku("ABC123");
        assertSame(lista, res);
    }

    // ==========================
    // Fake de InspectionController
    // ==========================
    static class FakeInspectionController extends InspectionController {
        String lastSku;
        int lastQty;
        String lastReason;
        LocalDate lastExpiry;
        Integer lastQtyRestocked;
        Integer lastQtyDiscarded;

        String nextResult = "DEFAULT_OK";
        boolean shouldThrow = false;
        String throwMessage = "erro";
        List<QuarantineInspection> nextInspections;

        @Override
        public String processQuarantine(String sku, int quantity, String returnReason,
                                        LocalDate expiryDate, Integer qtyRestocked, Integer qtyDiscarded) {
            if (shouldThrow) throw new RuntimeException(throwMessage);

            this.lastSku = sku;
            this.lastQty = quantity;
            this.lastReason = returnReason;
            this.lastExpiry = expiryDate;
            this.lastQtyRestocked = qtyRestocked;
            this.lastQtyDiscarded = qtyDiscarded;
            return nextResult;
        }

        @Override
        public List<QuarantineInspection> getInspectionsBySku(String sku) {
            return nextInspections;
        }
    }
}
