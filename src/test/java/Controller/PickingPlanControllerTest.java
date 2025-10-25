package Controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import Domain.*;
import Domain.ENUM.PackingHeuristic;
import Repository.WarehouseRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste exaustivo ao PickingPlanController.
 * NÃO usa Mockito — cria repositórios reais.
 * Cobre vários modos de heurística (FF, FFD, BFD) e casos extremos.
 */
class PickingPlanControllerTest {

    private WarehouseRepository warehouseRepo;
    private PickingPlanController controller;

    @BeforeEach
    void setUp() {
        warehouseRepo = new WarehouseRepository();
        controller = new PickingPlanController(warehouseRepo);
    }

    @Test
    void testCreatePickingPlanWithNoAllocations() {
        // Nenhuma linha de alocação guardada
        warehouseRepo.saveAllocationRows(Collections.emptyList());

        PickingPlan plan = controller.createPickingPlan(100, PackingHeuristic.FF, false);
        assertNotNull(plan);
        assertTrue(plan.getTrolleys().isEmpty(), "Sem alocações -> sem trolleys");
    }

    @Test
    void testCreatePickingPlanSimple_FF() {
        // Preparar um item e respetiva linha de alocação
        saveItemToRepository("SKU1", 2.0);
        AllocationRow row = new AllocationRow("ORDER1", 1, "SKU1", 5, "BOX1", 1, 1);
        warehouseRepo.saveAllocationRows(Collections.singletonList(row));

        PickingPlan plan = controller.createPickingPlan(20, PackingHeuristic.FF, false);

        assertNotNull(plan);
        assertEquals(1, plan.getTrolleys().size(), "Deve caber num trolley");
        assertTrue(plan.getTrolleys().get(0).getCurrentLoad() > 0);
    }

    @Test
    void testCreatePickingPlan_FFD_withMultipleItems() {
        saveItemToRepository("HEAVY", 10.0);
        saveItemToRepository("LIGHT", 1.0);

        AllocationRow r1 = new AllocationRow("ORD1", 1, "HEAVY", 2, "BOX1", 1, 1);
        AllocationRow r2 = new AllocationRow("ORD1", 2, "LIGHT", 10, "BOX2", 1, 1);
        warehouseRepo.saveAllocationRows(Arrays.asList(r1, r2));

        PickingPlan plan = controller.createPickingPlan(25, PackingHeuristic.FFD, false);
        assertNotNull(plan);
        assertFalse(plan.getTrolleys().isEmpty());
    }

    @Test
    void testCreatePickingPlan_BFD_withSplitAllowed() {
        saveItemToRepository("A", 8.0);
        saveItemToRepository("B", 3.0);

        AllocationRow a1 = new AllocationRow("O1", 1, "A", 4, "B1", 1, 1); // peso 32
        AllocationRow a2 = new AllocationRow("O1", 2, "B", 4, "B2", 1, 1); // peso 12
        warehouseRepo.saveAllocationRows(Arrays.asList(a1, a2));

        // Capacidade menor que total para forçar divisão
        PickingPlan planSplit = controller.createPickingPlan(20, PackingHeuristic.BFD, true);
        PickingPlan planNoSplit = controller.createPickingPlan(20, PackingHeuristic.BFD, false);

        assertNotNull(planSplit);
        assertNotNull(planNoSplit);
        assertTrue(planSplit.getTrolleys().size() >= 1);
        assertTrue(planNoSplit.getTrolleys().size() >= 1);
    }

    @Test
    void testCreatePickingPlanInvalidCapacity() {
        assertThrows(IllegalArgumentException.class, () -> controller.createPickingPlan(0, PackingHeuristic.FF, false));
        assertThrows(IllegalArgumentException.class, () -> controller.createPickingPlan(-10, PackingHeuristic.FF, false));
    }

    // ==============================================================
    // Método auxiliar seguro para criar Item no repositório
    // ==============================================================
    private void saveItemToRepository(String sku, double weight) {
        try {
            // Valores placeholder razoáveis — altera se quiseres outros
            String name = sku;                 // usa o sku como nome por simplicidade
            String category = "TEST_CATEGORY";
            String unit = "UNIT";
            double volume = 0.0;               // se não tens volume relevante, usa 0.0

            // Cria o Item com o construtor correto
            Item item = new Item(sku, name, category, unit, volume, weight);

            // Guarda no repositório (ajusta o método se o teu repo usar outro nome)
            warehouseRepo.saveItems(Collections.singletonList(item));
        } catch (Exception e) {
            fail("Não foi possível criar o item '" + sku + "': " + e.getMessage());
        }
    }
}
