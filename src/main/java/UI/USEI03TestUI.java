package UI;

import Controller.PickingPlanController;
import Controller.WarehouseController;
import Domain.*;
import Domain.ENUM.AllocationMode;
import Domain.ENUM.PackingHeuristic;
import Repository.BoxRepository;

import java.util.List;

/**
 * The type USEI03 test ui.
 * Test implementation for USEI03 - Picking Plans
 */
public class USEI03TestUI implements Runnable {
    private final WarehouseController warehouseController;
    private final PickingPlanController pickingPlanController;
    private final BoxRepository boxRepository;

    /**
     * Instantiates a new USEI03 test ui.
     *
     * @param warehouseController   the warehouse controller
     * @param pickingPlanController the picking plan controller
     * @param boxRepository         the box repository
     */
    public USEI03TestUI(WarehouseController warehouseController,
                       PickingPlanController pickingPlanController,
                       BoxRepository boxRepository) {
        this.warehouseController = warehouseController;
        this.pickingPlanController = pickingPlanController;
        this.boxRepository = boxRepository;
    }

    @Override
    public void run() {
        System.out.println("=== USEI03 - PICKING PLANS TEST ===");
        
        // First, run USEI02 to get allocation rows
        System.out.println("Step 1: Running USEI02 to get allocation rows...");
        AllocationResult allocationResult = warehouseController.processOrderEligibilityAndAllocation(
                boxRepository, AllocationMode.STRICT);
        
        System.out.println("Allocation rows created: " + allocationResult.getAllocationRows().size());
        
        if (allocationResult.getAllocationRows().isEmpty()) {
            System.out.println("No allocation rows available. Please ensure USEI02 has been run first.");
            return;
        }
        
        // Test all three heuristics
        testHeuristic(PackingHeuristic.FF, "First Fit");
        testHeuristic(PackingHeuristic.FFD, "First Fit Decreasing");
        testHeuristic(PackingHeuristic.BFD, "Best Fit Decreasing");
    }
    
    private void testHeuristic(PackingHeuristic heuristic, String heuristicName) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TESTING " + heuristicName.toUpperCase());
        System.out.println("=".repeat(60));
        
        int capacity = 50; // 50kg trolley capacity
        boolean allowSplitting = true;
        
        PickingPlan plan = pickingPlanController.createPickingPlan(capacity, heuristic, allowSplitting);
        
        // Display results
        System.out.println("Heuristic: " + plan.getHeuristic());
        System.out.println("Total Trolleys: " + plan.getTotalTrolleys());
        System.out.printf("Average Utilization: %.2f%%\n", plan.getAverageUtilization());
        
        // Display each trolley
        List<Trolley> trolleys = plan.getTrolleys();
        for (int i = 0; i < trolleys.size(); i++) {
            displayTrolleyDetails(trolleys.get(i), i + 1);
        }
    }
    
    private void displayTrolleyDetails(Trolley trolley, int trolleyNumber) {
        double utilization = (trolley.getCurrentLoad() / trolley.getMaxCapacity()) * 100;
        
        System.out.println("\n--- TROLLEY " + trolleyNumber + " ---");
        System.out.printf("Capacity: %d kg | Used: %.2f kg | Utilization: %.1f%%\n", 
                trolley.getMaxCapacity(), trolley.getCurrentLoad(), utilization);
        
        List<PickingItems> items = trolley.getPickingItems();
        if (items.isEmpty()) {
            System.out.println("No items assigned to this trolley.");
        } else {
            System.out.println("Picking Plan:");
            System.out.println("Order ID | Line | Aisle | Bay | Box ID | SKU | Qty");
            System.out.println("-".repeat(50));
            
            for (PickingItems item : items) {
                System.out.printf("%-8s | %-4d | %-5d | %-3d | %-6s | %-3s | %-3d\n",
                        item.getOrderId(),
                        item.getLineNo(),
                        item.getAisleId(),
                        item.getBayId(),
                        item.getBoxId(),
                        item.getSku(),
                        item.getQty()
                );
            }
        }
    }
}
