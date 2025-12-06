package Sprint1.Repository;

import Sprint1.Domain.AllocationRow;
import Sprint1.Domain.EligibilityResult;
import Sprint1.Domain.Item;
import Sprint1.Domain.Bay;
import Sprint1.Domain.Order;
import Sprint1.Domain.Box;
import Sprint1.Domain.Return;

import java.util.*;

/**
 * The type Warehouse repository.
 */
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

    /**
     * Instantiates a new Warehouse repository.
     */
    public WarehouseRepository() {
    }

    /**
     * Save items.
     *
     * @param itemsList the items list
     */
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

    /**
     * Gets items.
     *
     * @return the items
     */
    public List<Item> getItems() {
        return new ArrayList<>(items);
    }

    /**
     * Gets item by sku.
     *
     * @param sku the sku
     * @return the item by sku
     */
    public Item getItemBySku(String sku) {
        return itemsBySku.get(sku);
    }

    /**
     * Item exists boolean.
     *
     * @param sku the sku
     * @return the boolean
     */
    public boolean itemExists(String sku) {
        return itemsBySku.containsKey(sku);
    }

    /**
     * Save stock.
     *
     * @param stockMap the stock map
     */
// --- STOCK ---
    public void saveStock(Map<String, List<Box>> stockMap) {
        this.stock.clear();
        stockMap.forEach((sku, boxes) ->
                this.stock.put(sku, new ArrayList<>(boxes))
        );
    }

    /**
     * Gets stock by sku.
     *
     * @param sku the sku
     * @return the stock by sku
     */
    public List<Box> getStockBySku(String sku) {
        return new ArrayList<>(stock.getOrDefault(sku, new ArrayList<>()));
    }

    /**
     * Gets all stock.
     *
     * @return the all stock
     */
    public Map<String, List<Box>> getAllStock() {
        Map<String, List<Box>> copy = new HashMap<>();
        stock.forEach((key, value) -> copy.put(key, new ArrayList<>(value)));
        return copy;
    }

    /**
     * Save orders.
     *
     * @param ordersList the orders list
     */
// --- ORDERS ---
    public void saveOrders(List<Order> ordersList) {
        this.orders.clear();
        this.orders.addAll(ordersList);

        this.ordersById.clear();
        for (Order order : ordersList) {
            ordersById.put(order.getId(), order);
        }
    }

    /**
     * Gets orders.
     *
     * @return the orders
     */
    public List<Order> getOrders() {
        return new ArrayList<>(orders);
    }

    /**
     * Gets order by id.
     *
     * @param orderId the order id
     * @return the order by id
     */
    public Order getOrderById(String orderId) {
        return ordersById.get(orderId);
    }

    /**
     * Order exists boolean.
     *
     * @param orderId the order id
     * @return the boolean
     */
    public boolean orderExists(String orderId) {
        return ordersById.containsKey(orderId);
    }

    /**
     * Save bays.
     *
     * @param baysList the bays list
     */
// --- BAYS ---
    public void saveBays(List<Bay> baysList) {
        this.bays.clear();
        this.bays.addAll(baysList);
    }

    /**
     * Gets bays.
     *
     * @return the bays
     */
    public List<Bay> getBays() {
        return new ArrayList<>(bays);
    }

    /**
     * Save returns.
     *
     * @param returnsList the returns list
     */
// --- RETURNS ---
    public void saveReturns(List<Return> returnsList) {
        this.returns.clear();
        this.returns.addAll(returnsList);
    }

    /**
     * Gets returns.
     *
     * @return the returns
     */
    public List<Return> getReturns() {
        return new ArrayList<>(returns);
    }

    /**
     * Save eligibility results.
     *
     * @param results the results
     */
// --- ELIGIBILITY RESULTS ---
    public void saveEligibilityResults(List<EligibilityResult> results) {
        this.eligibilityResults.clear();
        this.eligibilityResults.addAll(results);
    }

    /**
     * Gets eligibility results.
     *
     * @return the eligibility results
     */
    public List<EligibilityResult> getEligibilityResults() {
        return new ArrayList<>(eligibilityResults);
    }

    /**
     * Save allocation rows.
     *
     * @param rows the rows
     */
// --- ALLOCATION ROWS ---
    public void saveAllocationRows(List<AllocationRow> rows) {
        this.allocationRows.clear();
        this.allocationRows.addAll(rows);
    }

    /**
     * Gets allocation rows.
     *
     * @return the allocation rows
     */
    public List<AllocationRow> getAllocationRows() {
        return new ArrayList<>(allocationRows);
    }

    /**
     * Clear all data.
     */
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

    /**
     * Gets data summary.
     *
     * @return the data summary
     */
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