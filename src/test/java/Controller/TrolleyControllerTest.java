package Controller;

import Domain.AllocationRow;
import Domain.Item;
import Domain.PickingItems;
import Domain.PickingPlan;
import Domain.Trolley;
import Domain.ENUM.PackingHeuristic;
import Repository.TrolleyRepository;
import Repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes Unitários Expandidos para TrolleyController (JUnit 5, sem Mockito).
 * Foca-se em atingir a máxima cobertura de ramos da função packing() e em
 * validar as regras das heurísticas FF, FFD e BFD.
 */
class TrolleyControllerTest {

    private TrolleyController controller;
    private FakeWarehouseRepository warehouseRepo;
    private TrolleyRepository trolleyRepo;

    @BeforeEach
    void setUp() {
        // Inicializa Fake Repositories para controle total dos dados e persistência
        warehouseRepo = new FakeWarehouseRepository();
        trolleyRepo = new TrolleyRepository(); // Assume que TrolleyRepository é simples e guarda em memória
        controller = new TrolleyController(warehouseRepo, trolleyRepo);
    }

    // ----------------------------------------------------------------
    // Testes de Construtor e Inicialização
    // ----------------------------------------------------------------

    @Test
    void constructor_nullRepositories_shouldThrowException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new TrolleyController(null, new TrolleyRepository()));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new TrolleyController(new FakeWarehouseRepository(), null));
    }

    // ----------------------------------------------------------------
    // Testes de Funcionalidades Base
    // ----------------------------------------------------------------

    @Test
    void shouldCreateTrolleyAndReturnIt() {
        Trolley t = controller.createTrolley(100, "FF");
        assertNotNull(t, "O Trolley deve ser criado e não nulo.");
        assertEquals(100, t.getMaxCapacity(), "A capacidade máxima deve ser a definida.");
        assertEquals("FF", t.getOptionHeuristic(), "A heurística deve ser a definida.");
        assertTrue(t.getId() > 0, "O ID deve ser gerado aleatoriamente e positivo.");
        assertTrue(controller.getTrolleyList().contains(t), "O trolley deve ser adicionado à lista.");
    }

    @Test
    void reduceLoad_validReduction_shouldUpdateLoad() {
        Trolley t = controller.createTrolley(50, "FF");
        t.setCurrentLoad(25.0);
        double newLoad = controller.reduceLoad(10.0);
        assertEquals(15.0, newLoad, "A carga deve ser reduzida corretamente.");
        assertEquals(15.0, t.getCurrentLoad(), "A carga do Trolley deve ser atualizada.");
    }

    @Test
    void reduceLoad_invalidWeight_shouldThrowException() {
        controller.createTrolley(50, "FF");
        controller.getTrolley().setCurrentLoad(10.0);

        // Peso a reduzir negativo ou zero
        assertThrows(IllegalArgumentException.class, () -> controller.reduceLoad(-1.0), "Deve lançar para peso negativo.");
        assertThrows(IllegalArgumentException.class, () -> controller.reduceLoad(0.0), "Deve lançar para peso zero.");

        // Peso a reduzir superior à carga atual
        assertThrows(IllegalArgumentException.class, () -> controller.reduceLoad(10.01), "Deve lançar se peso > carga atual.");
    }

    @Test
    void createPickingPlan_delegatesToController() {
        // Apenas verifica que a delegação para PickingPlanController funciona
        PickingPlan plan = controller.createPickingPlan(10, PackingHeuristic.FF, true);
        assertNotNull(plan, "A criação do PickingPlan deve ser delegada e retornar um plano.");
        // Mais verificações exigiriam mocks ou um PickingPlanController totalmente funcional, mas o foco é no TrolleyController
    }


    // ----------------------------------------------------------------
    // Testes Heurística: FIRST FIT (FF)
    // ----------------------------------------------------------------

    @Test
    void packing_FF_createsMultipleTrolleys_andSaves() {
        int capacity = 20; // Capacidade baixa
        controller.createTrolley(capacity, "FF");
        warehouseRepo.populateAllocationsForSplit(capacity); // Itens que exigem 3 trolleys: [10, 15, 1, 1, 1]

        controller.packing();

        List<Trolley> saved = trolleyRepo.getAllTrolleys();
        // Espera-se 3 trolleys: T1(10), T2(15), T3(1+1+1)
        assertEquals(3, saved.size(), "FF deve criar 3 trolleys para os dados fornecidos.");

        // Verifica a carga e o conteúdo do último trolley (que tem os 3 itens pequenos)
        Trolley ultimo = saved.get(2);
        assertEquals(3.0, ultimo.getCurrentLoad(), 0.001, "A carga do último trolley deve ser 3.0.");
        assertEquals(3, ultimo.getPickingItems().size(), "O último trolley deve ter 3 itens.");
    }

    @Test
    void packing_FF_allFit_createsOneTrolley() {
        controller.createTrolley(100, "FF");
        warehouseRepo.populateAllocationsSmall(10); // Carga total: 5*1 + 10*1 + 5*2 = 25

        controller.packing();

        List<Trolley> saved = trolleyRepo.getAllTrolleys();
        assertEquals(1, saved.size(), "FF deve criar apenas 1 trolley.");
        assertEquals(25.0, saved.get(0).getCurrentLoad(), 0.001, "A carga total deve ser 25.0.");
    }

    // ----------------------------------------------------------------
    // Testes Heurística: FIRST FIT DECREASING (FFD)
    // ----------------------------------------------------------------

    @Test
    void packing_FFD_sortsItems_andCreatesTrolleys() {
        int capacity = 20;
        controller.createTrolley(capacity, "FFD");
        warehouseRepo.populateAllocationsForSplit(capacity); // Itens: [10, 15, 1, 1, 1]. FFD ordena: [15, 10, 1, 1, 1]

        controller.packing();

        List<Trolley> saved = trolleyRepo.getAllTrolleys();
        // Espera-se 2 trolleys (mais eficiente que FF neste caso):
        // T1: 15 (resta 5) -> 1 (resta 4) -> 1 (resta 3) -> 1 (resta 2) = Carga 18
        // T2: 10 = Carga 10
        assertEquals(2, saved.size(), "FFD deve criar 2 trolleys para os dados fornecidos.");

        // Verifica a carga do primeiro trolley
        Trolley t1 = saved.get(0);
        assertEquals(18.0, t1.getCurrentLoad(), 0.001, "A carga do primeiro trolley (FFD) deve ser 18.0.");
        assertEquals(4, t1.getPickingItems().size(), "O primeiro trolley (FFD) deve ter 4 itens.");
    }

    // ----------------------------------------------------------------
    // Testes Heurística: BEST FIT DECREASING (BFD)
    // ----------------------------------------------------------------

    @Test
    void packing_BFD_fillsExistingTrolleyOptimally_andCreatesNew() {
        int capacity = 20;
        controller.createTrolley(capacity, "BFD"); // Inicializa controller e o trolley-template
        warehouseRepo.populateAllocationsForBfdScenario(); // Itens: [15, 20, 10]. Ordena: [20, 15, 10]

        // Pré-condição: Adiciona um trolley existente com 5.0 de carga (espaço restante 15.0)
        Trolley t1_parcial = new Trolley(capacity, 5.0, 100, new ArrayList<>(), "BFD");
        controller.getTrolleyList().add(t1_parcial);

        controller.packing();

        List<Trolley> saved = trolleyRepo.getAllTrolleys();
        assertEquals(3, saved.size(), "BFD deve criar 2 novos trolleys + 1 existente = 3 trolleys.");

        Trolley t1_updated = saved.stream().filter(t -> t.getId() == 100).findFirst().orElse(null);
        assertNotNull(t1_updated);
        assertEquals(20.0, t1_updated.getCurrentLoad(), 0.001, "O trolley pré-existente deve ter o item 15.");
        assertEquals(2, t1_updated.getPickingItems().size(), "O trolley pré-existente deve ter 2 itens (o original + o novo).");
    }

    @Test
    void packing_BFD_newTrolleyWhenNoFitFound() {
        controller.createTrolley(10, "BFD");
        warehouseRepo.populateAllocationsForSplit(20); // Itens: [10, 15, 1, 1, 1]

        controller.packing();
        List<Trolley> saved = trolleyRepo.getAllTrolleys();


        // Teste simples de cobertura: verifica que o processo não falha e que múltiplos trolleys são criados.
        assertTrue(saved.size() >= 2, "BFD deve criar múltiplos trolleys.");
    }

    // ----------------------------------------------------------------
    // Testes de Edge Cases e Cenários Específicos
    // ----------------------------------------------------------------

    @Test
    void packing_unknownHeuristic_doesNothing() {
        controller.createTrolley(50, "FOO"); // Heurística desconhecida
        warehouseRepo.populateAllocationsSmall(10);
        controller.packing();
        assertTrue(trolleyRepo.getAllTrolleys().isEmpty(), "Nenhum trolley deve ser persistido para heurística desconhecida.");
    }

    @Test
    void packing_emptyAllocationRows_noTrolleysCreated() {
        controller.createTrolley(50, "FF");
        // warehouseRepo é limpo por default no setup, mas garantimos que não há itens
        warehouseRepo.clearAllocations();
        controller.packing();
        assertTrue(trolleyRepo.getAllTrolleys().isEmpty(), "Se não há itens, não deve criar trolleys.");
    }

    @Test
    void packing_zeroWeightItems_doNotCrash() {
        controller.createTrolley(10, "FF");
        warehouseRepo.populateAllocationsWithZeroWeight(); // Peso total 0.0
        assertDoesNotThrow(() -> controller.packing(), "Itens com peso zero não devem causar crash.");
        List<Trolley> saved = trolleyRepo.getAllTrolleys();
        // Deve criar 1 trolley com carga 0.0 e o item (a lógica do FF não evita peso 0, apenas checa pesoItem > 0)
        assertEquals(1, saved.size());
        assertEquals(0.0, saved.get(0).getCurrentLoad(), 0.001);
    }

    @Test
    void packing_zeroQtyItems_doNotCrash() {
        controller.createTrolley(10, "FF");
        warehouseRepo.populateAllocationsWithZeroQty(); // Peso total 0.0
        assertDoesNotThrow(() -> controller.packing());
        List<Trolley> saved = trolleyRepo.getAllTrolleys();
        // Deve criar 1 trolley com carga 0.0
        assertEquals(1, saved.size());
        assertEquals(0.0, saved.get(0).getCurrentLoad(), 0.001);
    }

    // ----------------------------------------------------------------
    // Fake Repository: Implementação Controlada
    // ----------------------------------------------------------------

    /**
     * Fake implementation of WarehouseRepository for unit testing.
     * Simulates controlled data for all packing scenarios.
     */
    static class FakeWarehouseRepository extends WarehouseRepository {
        private final List<AllocationRow> rows = new ArrayList<>();
        private final Map<String, Item> items = new HashMap<>();

        public FakeWarehouseRepository() {
            clearAllocations(); // Garante o estado inicial
        }

        public void clearAllocations() {
            rows.clear();
            items.clear();
        }

        /**
         * Popula com itens pequenos (peso 1.0 ou 2.0)
         * Carga total = 25 (para capacidade 100, cabe tudo)
         */
        void populateAllocationsSmall(int capacity) {
            items.put("S1", new Item("S1", "Small1", "Cat", "U", 1.0, 1.0)); // Peso 5.0
            items.put("S2", new Item("S2", "Small2", "Cat", "U", 1.0, 1.0)); // Peso 10.0
            items.put("S3", new Item("S3", "Small3", "Cat", "U", 1.0, 2.0)); // Peso 10.0
            rows.clear();
            rows.add(new AllocationRow("SO1", 1, "S1", 5, "B1", 1, 1));
            rows.add(new AllocationRow("SO2", 1, "S2", 10, "B2", 1, 2));
            rows.add(new AllocationRow("SO3", 1, "S3", 5, "B3", 2, 3));
        }

        /**
         * Popula com itens que forçam a criação de múltiplos trolleys (3 splits esperados)
         * Se capacidade = 20: Itens: [10, 15, 1, 1, 1]
         */
        void populateAllocationsForSplit(int capacity) {
            items.put("M1", new Item("M1", "M", "Cat", "U", 1.0, 10.0)); // Peso 10
            items.put("L1", new Item("L1", "L", "Cat", "U", 1.0, 15.0)); // Peso 15
            items.put("S1", new Item("S1", "S", "Cat", "U", 1.0, 1.0)); // Peso 1
            items.put("S2", new Item("S2", "S", "Cat", "U", 1.0, 1.0)); // Peso 1
            items.put("S3", new Item("S3", "S", "Cat", "U", 1.0, 1.0)); // Peso 1
            rows.clear();
            rows.add(new AllocationRow("O1", 1, "M1", 1, "BOX1", 1, 1));
            rows.add(new AllocationRow("O2", 1, "L1", 1, "BOX2", 1, 2));
            rows.add(new AllocationRow("O3", 1, "S1", 1, "BOX3", 2, 3));
            rows.add(new AllocationRow("O4", 1, "S2", 1, "BOX4", 2, 4));
            rows.add(new AllocationRow("O5", 1, "S3", 1, "BOX5", 3, 5));
        }

        /**
         * Cenário BFD: Itens que podem preencher um espaço 'vazio' existente.
         * Itens: [15, 20, 10]
         */
        void populateAllocationsForBfdScenario() {
            items.put("W1", new Item("W1", "W1", "Cat", "U", 1.0, 15.0));
            items.put("W2", new Item("W2", "W2", "Cat", "U", 1.0, 20.0));
            items.put("W3", new Item("W3", "W3", "Cat", "U", 1.0, 10.0));
            rows.clear();
            rows.add(new AllocationRow("B1", 1, "W1", 1, "B1", 1, 1));
            rows.add(new AllocationRow("B2", 1, "W2", 1, "B2", 1, 2));
            rows.add(new AllocationRow("B3", 1, "W3", 1, "B3", 2, 3));
        }

        void populateAllocationsWithZeroWeight() {
            items.put("Z", new Item("Z", "ZeroWeight", "Cat", "U", 1.0, 0.0));
            rows.clear();
            rows.add(new AllocationRow("Z1", 1, "Z", 5, "ZBOX", 1, 1));
        }

        void populateAllocationsWithZeroQty() {
            items.put("Q", new Item("Q", "ZeroQty", "Cat", "U", 1.0, 2.0));
            rows.clear();
            rows.add(new AllocationRow("Q1", 1, "Q", 0, "QBOX", 1, 1));
        }

        @Override
        public List<AllocationRow> getAllocationRows() {
            return new ArrayList<>(rows);
        }

        @Override
        public Item getItemBySku(String sku) {
            return items.get(sku);
        }
    }
}