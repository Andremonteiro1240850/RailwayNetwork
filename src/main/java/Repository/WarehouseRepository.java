package Repository;

import Domain.AllocationRow;
import Domain.EligibilityResult;
import Domain.Item;
import Domain.Bay;
import Domain.Order;
import Domain.Box;
import Domain.Return;

import java.util.*;

public class WarehouseRepository {
    private final List<EligibilityResult> eligibilityResults = new ArrayList<>();
    private final List<AllocationRow> allocationRows = new ArrayList<>();
    private final List<Item> items = new ArrayList<>();
    private final List<Bay> bays = new ArrayList<>();
    private final List<Order> orders = new ArrayList<>();
    private final List<Return> returns = new ArrayList<>();
    private final Map<String, List<Box>> stock = new HashMap<>();


    private final Map<String, Item> itemsBySku = new HashMap<>();
    private final Map<String, Order> ordersById = new HashMap<>();

    public WarehouseRepository() {
    }

    // --- ITEMS ---
    public void saveItems(List<Item> itemsList) {
        this.items.clear();
        this.items.addAll(itemsList);

        // Atualizar mapa de busca rápida
        this.itemsBySku.clear();
        for (Item item : itemsList) {
            itemsBySku.put(item.getSku(), item);
        }
    }

    public List<Item> getItems() {
        return new ArrayList<>(items);
    }

    public Item getItemBySku(String sku) {
        return itemsBySku.get(sku);
    }

    public boolean itemExists(String sku) {
        return itemsBySku.containsKey(sku);
    }

    // --- STOCK ---
    public void saveStock(Map<String, List<Box>> stockMap) {
        this.stock.clear();
        stockMap.forEach((sku, boxes) ->
                this.stock.put(sku, new ArrayList<>(boxes))
        );
    }

    public List<Box> getStockBySku(String sku) {
        return new ArrayList<>(stock.getOrDefault(sku, new ArrayList<>()));
    }

    public Map<String, List<Box>> getAllStock() {
        Map<String, List<Box>> copy = new HashMap<>();
        stock.forEach((key, value) -> copy.put(key, new ArrayList<>(value)));
        return copy;
    }

    // --- ORDERS ---
    public void saveOrders(List<Order> ordersList) {
        this.orders.clear();
        this.orders.addAll(ordersList);

        this.ordersById.clear();
        for (Order order : ordersList) {
            ordersById.put(order.getId(), order);
        }
    }

    public List<Order> getOrders() {
        return new ArrayList<>(orders);
    }

    public Order getOrderById(String orderId) {
        return ordersById.get(orderId);
    }

    public boolean orderExists(String orderId) {
        return ordersById.containsKey(orderId);
    }

    // --- BAYS ---
    public void saveBays(List<Bay> baysList) {
        this.bays.clear();
        this.bays.addAll(baysList);
    }

    public List<Bay> getBays() {
        return new ArrayList<>(bays);
    }

    // --- RETURNS ---
    public void saveReturns(List<Return> returnsList) {
        this.returns.clear();
        this.returns.addAll(returnsList);
    }

    public List<Return> getReturns() {
        return new ArrayList<>(returns);
    }

    // --- ELIGIBILITY RESULTS ---
    public void saveEligibilityResults(List<EligibilityResult> results) {
        this.eligibilityResults.clear();
        this.eligibilityResults.addAll(results);
    }

    public List<EligibilityResult> getEligibilityResults() {
        return new ArrayList<>(eligibilityResults);
    }

    // --- ALLOCATION ROWS ---
    public void saveAllocationRows(List<AllocationRow> rows) {
        this.allocationRows.clear();
        this.allocationRows.addAll(rows);
    }

    public List<AllocationRow> getAllocationRows() {
        return new ArrayList<>(allocationRows);
    }

    // --- MÉTODOS DE UTILIDADE ---
    public void clearAllData() {
        items.clear();
        itemsBySku.clear();
        stock.clear();
        orders.clear();
        ordersById.clear();
        bays.clear();
        returns.clear();
        eligibilityResults.clear();
        allocationRows.clear();
    }

    public Map<String, Object> getDataSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("items", items.size());
        summary.put("orders", orders.size());
        summary.put("bays", bays.size());
        summary.put("returns", returns.size());
        summary.put("eligibilityResults", eligibilityResults.size());
        summary.put("allocationRows", allocationRows.size());
        summary.put("stockSkus", stock.size());

        return summary;
    }
}