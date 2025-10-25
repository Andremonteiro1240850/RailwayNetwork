package Repository;

// IMPORTAÇÕES DOS TIPOS DE DOMÍNIO REAIS
import Domain.ENUM.AllocationStatus;
import Domain.Item;
import Domain.Order;
import Domain.Box;
import Domain.Bay;
import Domain.Return;
import Domain.EligibilityResult;
import Domain.AllocationRow;
// Assumindo que BayId é uma classe de domínio que existe
import Domain.BayId;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe {@link WarehouseRepository}.
 * Refatorado para usar as classes de Domínio reais.
 */
class WarehouseRepositoryTest {

    private WarehouseRepository repository;

    // Mock Data (Agora usando classes reais de Domain.*)
    private Item item1;
    private Item item2;
    private Order order1;
    private Order order2;
    private Box boxA;
    private Bay bay1;
    private Return return1;
    private EligibilityResult result1;
    private AllocationRow row1;


    // AS CLASSES DUMMY FORAM REMOVIDAS DAQUI. O TESTE AGORA IMPORTA E USA AS CLASSES REAIS.
    // OrderLine, BayId, AllocationRow, EligibilityResult, Item, Order, Box, Return.


    @BeforeEach
    void setUp() {
        repository = new WarehouseRepository();

        // Inicialização de Dados Mock - AGORA INSTANCIANDO AS CLASSES Domain.*
        // Assumindo que os construtores das classes reais têm as mesmas assinaturas
        // que as classes dummy que estavam no teste.
        item1 = new Item("SKU-1", "Name-1", "Cat-A", "unit", 1.0, 1.0);
        item2 = new Item("SKU-2", "Name-2", "Cat-B", "unit", 2.0, 2.0);

        order1 = new Order("ORD-1", LocalDateTime.now(), 1);
        order2 = new Order("ORD-2", LocalDateTime.now(), 2);

        boxA = new Box("BOX-A", "SKU-1", 10, null, null);
        bay1 = new Bay(new BayId("W1", 1, 1), 100);
        return1 = new Return("RET-1", "SKU-1", 1, "Damage", null, null);
        result1 = new EligibilityResult("ORD-1", 1, "SKU-1", 5, 5, AllocationStatus.ELIGIBLE);
        row1 = new AllocationRow("ORD-1", 1, "SKU-1", 5, "BOX-A", 1, 1);
    }

    // ------------------------------------
    // TESTES DE ITEMS
    // ------------------------------------

    @Test
    @DisplayName("ITEMS: saveItems deve limpar o estado anterior e atualizar listas e mapas.")
    void saveItems() {
        // Arrange
        List<Item> initialItems = new ArrayList<>();
        initialItems.add(item1);
        repository.saveItems(initialItems);

        // Act
        List<Item> newItems = new ArrayList<>();
        newItems.add(item2);
        repository.saveItems(newItems);

        // Assert
        assertEquals(1, repository.getItems().size());
        assertEquals(item2, repository.getItemBySku("SKU-2"));
        assertNull(repository.getItemBySku("SKU-1"), "O item inicial deve ter sido limpo.");
    }

    @Test
    @DisplayName("ITEMS: getItems deve retornar uma cópia defensiva.")
    void getItems_DefensiveCopy() {
        List<Item> itemsToSave = new ArrayList<>(Collections.singletonList(item1));
        repository.saveItems(itemsToSave);
        List<Item> retrievedItems = repository.getItems();
        retrievedItems.clear();

        assertFalse(repository.getItems().isEmpty(), "A lista interna não deve ser afetada.");
    }

    @Test
    @DisplayName("ITEMS: getItemBySku e itemExists devem funcionar corretamente.")
    void getItemBySkuAndExists() {
        List<Item> itemsToSave = new ArrayList<>(Collections.singletonList(item1));
        repository.saveItems(itemsToSave);

        assertEquals(item1, repository.getItemBySku("SKU-1"));
        assertTrue(repository.itemExists("SKU-1"));
        assertNull(repository.getItemBySku("INVALID-SKU"));
        assertFalse(repository.itemExists("INVALID-SKU"));
    }

    // ------------------------------------
    // TESTES DE STOCK
    // ------------------------------------

    @Test
    @DisplayName("STOCK: saveStock deve limpar e salvar o novo mapa, garantindo cópias internas.")
    void saveStock() {
        // Arrange: Stock inicial
        Map<String, List<Box>> initialStock = new HashMap<>();
        List<Box> initialBoxes = new ArrayList<>();
        // Box, Return, e Order precisam ser importados de Domain
        initialBoxes.add(new Box("BX-99", "SKU-99", 1, null, null));
        initialStock.put("SKU-99", initialBoxes);
        repository.saveStock(initialStock);

        // Act: Novo Stock
        List<Box> boxList = new ArrayList<>();
        boxList.add(boxA);
        Map<String, List<Box>> newStock = new HashMap<>();
        newStock.put(item1.getSku(), boxList);
        repository.saveStock(newStock);

        // Assert
        assertEquals(1, repository.getAllStock().size());
        assertTrue(repository.getAllStock().containsKey(item1.getSku()));
        assertFalse(repository.getAllStock().containsKey("SKU-99"), "O stock inicial deve ter sido limpo.");

        // Verifica a cópia defensiva da lista interna:
        Box boxZ = new Box("BX-Z", "SKU-1", 1, null, null);
        boxList.add(boxZ); // Modifica a lista externa passada
        assertEquals(1, repository.getStockBySku(item1.getSku()).size(), "A lista externa não deve afetar a lista interna.");
    }

    @Test
    @DisplayName("STOCK: getStockBySku deve retornar o stock correto ou lista vazia, e cópia defensiva.")
    void getStockBySku() {
        Map<String, List<Box>> stockToSave = new HashMap<>();
        List<Box> boxesForSku = new ArrayList<>(Collections.singletonList(boxA));
        stockToSave.put(item1.getSku(), boxesForSku);
        repository.saveStock(stockToSave);

        // 1. Testa a recuperação
        assertEquals(1, repository.getStockBySku(item1.getSku()).size());
        assertTrue(repository.getStockBySku("UNKNOWN").isEmpty());

        // 2. Testa a cópia defensiva
        List<Box> retrievedStock = repository.getStockBySku(item1.getSku());
        retrievedStock.clear();

        assertEquals(1, repository.getStockBySku(item1.getSku()).size(), "A lista interna não deve ser alterada.");
    }

    @Test
    @DisplayName("STOCK: getAllStock deve retornar uma cópia defensiva do mapa e suas listas.")
    void getAllStock_DefensiveCopy() {
        Map<String, List<Box>> stockToSave = new HashMap<>();
        List<Box> boxes = new ArrayList<>(Collections.singletonList(boxA));
        stockToSave.put(item1.getSku(), boxes);
        repository.saveStock(stockToSave);

        Map<String, List<Box>> retrievedStock = repository.getAllStock();
        retrievedStock.clear(); // Modifica o Mapa retornado

        // Assert
        assertFalse(repository.getAllStock().isEmpty(), "O Mapa interno não deve ser alterado.");
    }

    // ------------------------------------
    // TESTES DE ORDERS
    // ------------------------------------

    @Test
    @DisplayName("ORDERS: saveOrders deve limpar e atualizar listas e mapas auxiliares.")
    void saveOrders() {
        List<Order> orders1 = new ArrayList<>(Collections.singletonList(order1));
        repository.saveOrders(orders1);

        List<Order> orders2 = new ArrayList<>(Collections.singletonList(order2));
        repository.saveOrders(orders2);

        assertEquals(1, repository.getOrders().size());
        assertEquals(order2, repository.getOrderById("ORD-2"));
        assertNull(repository.getOrderById("ORD-1"), "A ordem inicial deve ter sido limpa.");
        assertFalse(repository.orderExists("ORD-1"));
        assertTrue(repository.orderExists("ORD-2"));
    }

    @Test
    @DisplayName("ORDERS: getOrders deve retornar uma cópia defensiva.")
    void getOrders_DefensiveCopy() {
        List<Order> ordersToSave = new ArrayList<>(Collections.singletonList(order1));
        repository.saveOrders(ordersToSave);
        List<Order> retrievedOrders = repository.getOrders();
        retrievedOrders.clear();

        assertFalse(repository.getOrders().isEmpty(), "A lista interna não deve ser afetada.");
    }

    // ------------------------------------
    // TESTES DE BAYS, RETURNS, RESULTS E ALLOCATION ROWS
    // ------------------------------------

    @Test
    @DisplayName("BAYS: saveBays e getBays com cópia defensiva.")
    void saveAndGetBays() {
        List<Bay> baysToSave = new ArrayList<>(Collections.singletonList(bay1));
        repository.saveBays(baysToSave);
        repository.saveBays(new ArrayList<>()); // Limpa

        assertTrue(repository.getBays().isEmpty());

        baysToSave = new ArrayList<>(Collections.singletonList(bay1));
        repository.saveBays(baysToSave);
        List<Bay> retrievedBays = repository.getBays();
        retrievedBays.clear();

        assertFalse(repository.getBays().isEmpty(), "A cópia defensiva falhou.");
    }

    @Test
    @DisplayName("RETURNS: saveReturns e getReturns com cópia defensiva.")
    void saveAndGetReturns() {
        List<Return> returnsToSave = new ArrayList<>(Collections.singletonList(return1));
        repository.saveReturns(returnsToSave);
        repository.saveReturns(new ArrayList<>());

        assertTrue(repository.getReturns().isEmpty());

        returnsToSave = new ArrayList<>(Collections.singletonList(return1));
        repository.saveReturns(returnsToSave);
        List<Return> retrievedReturns = repository.getReturns();
        retrievedReturns.clear();

        assertFalse(repository.getReturns().isEmpty(), "A cópia defensiva falhou.");
    }

    @Test
    @DisplayName("ELIGIBILITY: saveEligibilityResults e getEligibilityResults com cópia defensiva.")
    void saveAndGetEligibilityResults() {
        List<EligibilityResult> resultsToSave = new ArrayList<>(Collections.singletonList(result1));
        repository.saveEligibilityResults(resultsToSave);
        repository.saveEligibilityResults(new ArrayList<>());

        assertTrue(repository.getEligibilityResults().isEmpty());

        resultsToSave = new ArrayList<>(Collections.singletonList(result1));
        repository.saveEligibilityResults(resultsToSave);
        List<EligibilityResult> retrievedResults = repository.getEligibilityResults();
        retrievedResults.clear();

        assertFalse(repository.getEligibilityResults().isEmpty(), "A cópia defensiva falhou.");
    }

    @Test
    @DisplayName("ALLOCATION: saveAllocationRows e getAllocationRows com cópia defensiva.")
    void saveAndGetAllocationRows() {
        List<AllocationRow> rowsToSave = new ArrayList<>(Collections.singletonList(row1));
        repository.saveAllocationRows(rowsToSave);
        repository.saveAllocationRows(new ArrayList<>());

        assertTrue(repository.getAllocationRows().isEmpty());

        rowsToSave = new ArrayList<>(Collections.singletonList(row1));
        repository.saveAllocationRows(rowsToSave);
        List<AllocationRow> retrievedRows = repository.getAllocationRows();
        retrievedRows.clear();

        assertFalse(repository.getAllocationRows().isEmpty(), "A cópia defensiva falhou.");
    }

    // ------------------------------------
    // TESTES DE UTILIDADE
    // ------------------------------------

    @Test
    @DisplayName("UTIL: clearAllData deve limpar todos os repositórios, incluindo mapas auxiliares.")
    void clearAllData() {
        // Arrange: Popula o repositório
        List<Item> itemsToSave = new ArrayList<>(Collections.singletonList(item1));
        repository.saveItems(itemsToSave);
        List<Order> ordersToSave = new ArrayList<>(Collections.singletonList(order1));
        repository.saveOrders(ordersToSave);

        // Act
        repository.clearAllData();

        // Assert: Todos os repositórios devem estar vazios
        assertTrue(repository.getItems().isEmpty());
        assertTrue(repository.getOrders().isEmpty());
        assertNull(repository.getItemBySku("SKU-1"));
        assertFalse(repository.orderExists("ORD-1"));

        // Verifica o resumo (garantindo que os contadores são 0)
        Map<String, Object> summary = repository.getDataSummary();
        assertTrue(summary.values().stream().allMatch(count -> (count instanceof Integer) && (Integer) count == 0), "O sumário deve mostrar contagens zero.");
    }

    @Test
    @DisplayName("UTIL: getDataSummary deve retornar as contagens corretas de todos os repositórios.")
    void getDataSummary() {
        // Arrange: Prepara os dados
        List<Item> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        repository.saveItems(items);

        List<Order> orders = new ArrayList<>(Collections.singletonList(order1));
        repository.saveOrders(orders);

        List<Bay> bays = new ArrayList<>(Collections.singletonList(bay1));
        repository.saveBays(bays);

        List<Box> boxes = new ArrayList<>(Collections.singletonList(boxA));
        Map<String, List<Box>> stock = new HashMap<>();
        stock.put("SKU-1", boxes);
        repository.saveStock(stock);

        List<EligibilityResult> results = new ArrayList<>(Collections.singletonList(result1));
        repository.saveEligibilityResults(results);

        // Act
        Map<String, Object> summary = repository.getDataSummary();

        // Assert
        assertEquals(2, summary.get("items"), "Contagem de items incorreta.");
        assertEquals(1, summary.get("orders"), "Contagem de orders incorreta.");
        assertEquals(1, summary.get("bays"), "Contagem de bays incorreta.");
        assertEquals(1, summary.get("eligibilityResults"), "Contagem de eligibilityResults incorreta.");
        assertEquals(1, summary.get("stockSkus"), "Contagem de SKUs em stock incorreta.");
        assertEquals(0, summary.get("returns"), "Contagem de returns incorreta (deveria ser 0).");
    }
}