package UI;

import Controller.PickingPlanController;
import Domain.*;
import Domain.ENUM.PackingHeuristic;

import java.util.List;
import java.util.Scanner;

/**
 * The type Picking plan ui.
 * Implements USEI03 - Picking Plans UI
 */
public class PickingPlanUI implements Runnable {
    private final PickingPlanController pickingPlanController;
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Instantiates a new Picking plan ui.
     *
     * @param pickingPlanController the picking plan controller
     */
    public PickingPlanUI(PickingPlanController pickingPlanController) {
        this.pickingPlanController = pickingPlanController;
    }

    @Override
    public void run() {
        System.out.println("=== PICKING PLANS ===");
        System.out.println("Creating picking plans for allocation rows from USEI02...\n");

        // Get trolley capacity from user
        System.out.print("Enter trolley capacity (kg): ");
        int capacity = scanner.nextInt();

        // Get heuristic choice
        PackingHeuristic heuristic = getHeuristicChoice();

        // Get splitting preference
        System.out.print("Allow splitting items that don't fit? (y/n): ");
        boolean allowSplitting = scanner.next().toLowerCase().startsWith("y");

        // Create picking plan
        PickingPlan plan = pickingPlanController.createPickingPlan(capacity, heuristic, allowSplitting);

        // Display results
        displayPickingPlan(plan);
    }

    /**
     * Get heuristic choice from user
     */
    private PackingHeuristic getHeuristicChoice() {
        System.out.println("\nSelect packing heuristic:");
        System.out.println("1. First Fit (FF)");
        System.out.println("2. First Fit Decreasing (FFD)");
        System.out.println("3. Best Fit Decreasing (BFD)");
        System.out.print("Enter choice (1-3): ");

        int choice = scanner.nextInt();
        return switch (choice) {
            case 1 -> PackingHeuristic.FF;
            case 2 -> PackingHeuristic.FFD;
            case 3 -> PackingHeuristic.BFD;
            default -> {
                System.out.println("Invalid choice, defaulting to First Fit");
                yield PackingHeuristic.FF;
            }
        };
    }

    /**
     * Display the complete picking plan
     */
    private void displayPickingPlan(PickingPlan plan) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("PICKING PLAN RESULTS");
        System.out.println("=".repeat(60));
        System.out.println("Heuristic: " + plan.getHeuristic());
        System.out.println("Total Trolleys: " + plan.getTotalTrolleys());
        System.out.printf("Average Utilization: %.2f%%\n", plan.getAverageUtilization());
        System.out.println("=".repeat(60));

        List<Trolley> trolleys = plan.getTrolleys();
        for (int i = 0; i < trolleys.size(); i++) {
            Trolley trolley = trolleys.get(i);
            displayTrolleyDetails(trolley, i + 1);
        }
    }

    /**
     * Display detailed information for a single trolley
     */
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
