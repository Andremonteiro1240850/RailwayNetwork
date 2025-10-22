package Domain;

import Repository.WarehouseRepository;
import java.util.*;

/**
 * The type Warehouse.
 */
public class Warehouse {
    private final DataLoader dataLoader;
    private final WarehouseRepository warehouseRepository;

    /**
     * Instantiates a new Warehouse.
     *
     * @param warehouseRepository the warehouse repository
     */
    public Warehouse(WarehouseRepository warehouseRepository) {
        this.dataLoader = new DataLoader();
        this.warehouseRepository = warehouseRepository;
    }

    /**
     * Load items.
     *
     * @param filePath the file path
     */
    public void loadItems(String filePath) {
        dataLoader.loadItems(filePath);
        // Converter Map para List e guardar no Repository
        List<Item> itemsList = new ArrayList<>(dataLoader.items.values());
        warehouseRepository.saveItems(itemsList);
    }

    /**
     * Load wagons.
     *
     * @param filePath the file path
     */
    public void loadWagons(String filePath) {
        dataLoader.loadWagons(filePath);
        warehouseRepository.saveStock(dataLoader.stock);
    }

    /**
     * Load orders.
     *
     * @param ordersFile the orders file
     * @param linesFile  the lines file
     */
    public void loadOrders(String ordersFile, String linesFile) {
        dataLoader.loadOrders(ordersFile, linesFile);
        List<Order> ordersList = new ArrayList<>(dataLoader.orders.values());
        warehouseRepository.saveOrders(ordersList);
    }

    /**
     * Load bays.
     *
     * @param filePath the file path
     */
    public void loadBays(String filePath) {
        dataLoader.loadBays(filePath);
        warehouseRepository.saveBays(dataLoader.bays);
    }

    /**
     * Load returns.
     *
     * @param filePath the file path
     */
    public void loadReturns(String filePath) {
        dataLoader.loadReturns(filePath);
        warehouseRepository.saveReturns(dataLoader.returns);
    }

    /**
     * Gets items.
     *
     * @return the items
     */
    public List<Item> getItems() {
        return warehouseRepository.getItems();
    }

    /**
     * Gets stock.
     *
     * @return the stock
     */
    public Map<String, List<Box>> getStock() {
        return warehouseRepository.getAllStock();
    }

    /**
     * Gets orders.
     *
     * @return the orders
     */
    public List<Order> getOrders() {
        return warehouseRepository.getOrders();
    }

    /**
     * Gets bays.
     *
     * @return the bays
     */
    public List<Bay> getBays() {
        return warehouseRepository.getBays();
    }

    /**
     * Gets returns.
     *
     * @return the returns
     */
    public List<Return> getReturns() {
        return warehouseRepository.getReturns();
    }

    /**
     * Save eligibility results.
     *
     * @param results the results
     */
    public void saveEligibilityResults(List<EligibilityResult> results) {
        warehouseRepository.saveEligibilityResults(results);
    }

    /**
     * Save allocation rows.
     *
     * @param allocations the allocations
     */
    public void saveAllocationRows(List<AllocationRow> allocations) {
        warehouseRepository.saveAllocationRows(allocations);
    }

    @Override
    public String toString() {
        return "Warehouse: " +
                "dataLoader => " + dataLoader +
                ", warehouseRepository => " + warehouseRepository +
                '.';
    }
}