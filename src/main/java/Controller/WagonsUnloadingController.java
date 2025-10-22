package Controller;

import Domain.*;
import java.util.*;

public class WagonsUnloadingController {
    private WarehouseManager warehouseManager;
    private DataLoader dataLoader;
    private boolean dataLoaded = false;

    public WagonsUnloadingController(DataLoader dataLoader) {
        this.dataLoader = dataLoader;
        this.dataLoaded = true;
        
        // Initialize warehouse manager with loaded data and BoxRepository
        this.warehouseManager = new WarehouseManager(
            dataLoader.items,
            dataLoader.stock,
            dataLoader.bays,
            dataLoader.boxRepository
        );
    }


    /**
     * Execute wagon unloading process
     */
    public void unloadWagons() {
        if (!dataLoaded) {
            System.out.println("Data not loaded. Please load data first.");
            return;
        }
        
        warehouseManager.unloadWagons();
    }

    /**
     * Dispatch stock for a specific SKU
     */
    public List<Box> dispatchStock(String sku, int quantity) {
        if (!dataLoaded) {
            System.out.println("Data not loaded. Please load data first.");
            return new ArrayList<>();
        }
        
        if (!warehouseManager.getItems().containsKey(sku)) {
            System.out.println("SKU " + sku + " not found in catalog.");
            return new ArrayList<>();
        }
        
        if (quantity <= 0) {
            System.out.println("Quantity must be positive.");
            return new ArrayList<>();
        }
        
        return warehouseManager.dispatchStock(sku, quantity);
    }

    /**
     * Relocate a box to a different bay
     */
    public boolean relocateBox(String boxId, String warehouseId, int aisle, int bay) {
        if (!dataLoaded) {
            System.out.println("Data not loaded. Please load data first.");
            return false;
        }
        
        BayId newBayId = new BayId(warehouseId, aisle, bay);
        return warehouseManager.relocateBox(boxId, newBayId);
    }

    /**
     * Get available SKUs
     */
    public List<String> getAvailableSkus() {
        if (!dataLoaded) {
            return new ArrayList<>();
        }
        
        return new ArrayList<>(warehouseManager.getItems().keySet());
    }

    /**
     * Check if data is loaded
     */
    public boolean isDataLoaded() {
        return dataLoaded;
    }

    /**
     * Get warehouse manager (for advanced operations)
     */
    public WarehouseManager getWarehouseManager() {
        return warehouseManager;
    }
    
    /**
     * Get box repository (for box management operations)
     */
    public Repository.BoxRepository getBoxRepository() {
        return dataLoader.boxRepository;
    }
}
