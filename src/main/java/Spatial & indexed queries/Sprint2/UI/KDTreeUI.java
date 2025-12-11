package Sprint2.UI;

import Sprint1.UI.StringUtils;
import Sprint2.Controller.KDTreeController;
import Sprint2.Controller.KDTreeMetrics;

import java.util.Scanner;

/**
 * User interface for KD-tree operations.
 * Allows building balanced KD-trees and viewing metrics.
 * 
 * @author Development Team
 * @version 1.0
 */
public class KDTreeUI {
    
    private final KDTreeController controller;
    private final Scanner scanner;
    
    /**
     * Constructs a new KDTreeUI with the specified controller.
     *
     * @param controller the KD-tree controller
     */
    public KDTreeUI(KDTreeController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Shows the main menu for KD-tree operations.
     */
    public void showMainMenu() {
        while (true) {
            System.out.println("\n" + StringUtils.repeat("=", 60));
            System.out.println("Balanced KD-Tree Operations");
            System.out.println(StringUtils.repeat("=", 60));
            System.out.println("1. Build KD-Tree");
            System.out.println("2. View Tree Metrics");
            System.out.println("0. Back to Main Menu");
            
            System.out.print("\nSelect option: ");
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    buildTree();
                    break;
                case "2":
                    viewMetrics();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid option! Please try again.");
            }
        }
    }
    
    /**
     * Builds the KD-tree.
     */
    private void buildTree() {
        System.out.println("\n" + StringUtils.repeat("-", 60));
        System.out.println("Build Balanced KD-Tree");
        System.out.println(StringUtils.repeat("-", 60));
        
        if (controller.isTreeBuilt()) {
            System.out.print("Tree already built. Rebuild? (y/n): ");
            String answer = scanner.nextLine().trim();
            if (!answer.equalsIgnoreCase("y")) {
                return;
            }
        }
        
        System.out.println("\nBuilding balanced KD-tree...");
        long startTime = System.currentTimeMillis();
        boolean success = controller.buildTree();
        long endTime = System.currentTimeMillis();
        
        if (success) {
            System.out.println("KD-tree built successfully in " + (endTime - startTime) + " ms.");
            
            // Show summary immediately after build
            KDTreeMetrics metrics = controller.getAllMetrics();
            if (metrics != null) {
                System.out.println("\n" + StringUtils.repeat("=", 60));
                System.out.println("KD-Tree Build Summary");
                System.out.println(StringUtils.repeat("=", 60));
                System.out.println("Total Number of Stations: " + metrics.getNumStations());
                System.out.println("Total Number of Nodes: " + metrics.getNumNodes());
                System.out.println("Tree Height: " + metrics.getHeight());
                System.out.println("\nBucket Size Distribution:");
                metrics.getBucketStats().entrySet().stream()
                    .sorted((a, b) -> {
                        int cmp = b.getValue().compareTo(a.getValue());
                        return cmp != 0 ? cmp : b.getKey().compareTo(a.getKey());
                    })
                    .forEach(entry -> System.out.println("  Bucket size " + entry.getKey() + ": " + entry.getValue() + " node(s)"));
                
                System.out.println("\nYou can view detailed metrics.");
            }
        } else {
            System.out.println("Error building KD-tree. Please check the station repository.");
        }
    }
    
    /**
     * Displays tree metrics.
     */
    private void viewMetrics() {
        System.out.println("\n" + StringUtils.repeat("=", 60));
        System.out.println("KD-Tree Metrics");
        System.out.println(StringUtils.repeat("=", 60));
        
        if (!controller.isTreeBuilt()) {
            System.out.println("Tree not built yet. Please build the tree first.");
            return;
        }
        
        String metrics = controller.getMetricsString();
        System.out.println(metrics);
        
        System.out.println(StringUtils.repeat("-", 60));
    }
    
}

