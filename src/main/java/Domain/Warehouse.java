package Domain;

import Repository.WarehouseRepository;
import java.util.*;

public class Warehouse {
    private final DataLoader dataLoader;
    private final WarehouseRepository warehouseRepository;

    public Warehouse(WarehouseRepository warehouseRepository) {
        this.dataLoader = new DataLoader();
        this.warehouseRepository = warehouseRepository;
    }

    public void loadItems(String filePath) {
        dataLoader.loadItems(filePath);
        // Converter Map para List e guardar no Repository
        List<Item> itemsList = new ArrayList<>(dataLoader.items.values());
        warehouseRepository.saveItems(itemsList);
    }

    public void loadWagons(String filePath) {
        dataLoader.loadWagons(filePath);
        warehouseRepository.saveStock(dataLoader.stock);
    }

    public void loadOrders(String ordersFile, String linesFile) {
        dataLoader.loadOrders(ordersFile, linesFile);
        List<Order> ordersList = new ArrayList<>(dataLoader.orders.values());
        warehouseRepository.saveOrders(ordersList);
    }

    public void loadBays(String filePath) {
        dataLoader.loadBays(filePath);
        warehouseRepository.saveBays(dataLoader.bays);
    }

    public void loadReturns(String filePath) {
        dataLoader.loadReturns(filePath);
        warehouseRepository.saveReturns(dataLoader.returns);
    }

    public List<Item> getItems() {
        return warehouseRepository.getItems();
    }

    public Map<String, List<Box>> getStock() {
        return warehouseRepository.getAllStock();
    }

    public List<Order> getOrders() {
        return warehouseRepository.getOrders();
    }

    public List<Bay> getBays() {
        return warehouseRepository.getBays();
    }

    public List<Return> getReturns() {
        return warehouseRepository.getReturns();
    }

    public void saveEligibilityResults(List<EligibilityResult> results) {
        warehouseRepository.saveEligibilityResults(results);
    }

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