package UI;

import Controller.KDTreeController;
import Repository.StationRepository;

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
            System.out.println("\n" + "=".repeat(60));
            System.out.println("Balanced KD-Tree Operations");
            System.out.println("=".repeat(60));
            System.out.println("1. Build KD-Tree");
            System.out.println("2. View Tree Metrics");
            System.out.println("3. Generate Report");
            System.out.println("4. Back to Main Menu");
            
            System.out.print("\nSelect option: ");
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    buildTree();
                    break;
                case "2":
                    viewMetrics();
                    break;
                case "3":
                    generateReport();
                    break;
                case "4":
                    return;
                default:
                    System.out.println("❌ Invalid option! Please try again.");
            }
        }
    }
    
    /**
     * Builds the KD-tree.
     */
    private void buildTree() {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("Build Balanced KD-Tree");
        System.out.println("-".repeat(60));
        
        if (controller.isTreeBuilt()) {
            System.out.print("Tree already built. Rebuild? (y/n): ");
            String answer = scanner.nextLine().trim();
            if (!answer.equalsIgnoreCase("y")) {
                return;
            }
        }
        
        System.out.println("\n🔨 Building balanced KD-tree...");
        long startTime = System.currentTimeMillis();
        boolean success = controller.buildTree();
        long endTime = System.currentTimeMillis();
        
        if (success) {
            System.out.println("✓ KD-tree built successfully in " + (endTime - startTime) + " ms.");
            System.out.println("\nTree built! You can now view metrics or generate a report.");
        } else {
            System.out.println("❌ Error building KD-tree. Please check the station repository.");
        }
        
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Displays tree metrics.
     */
    private void viewMetrics() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("KD-Tree Metrics");
        System.out.println("=".repeat(60));
        
        if (!controller.isTreeBuilt()) {
            System.out.println("❌ Tree not built yet. Please build the tree first.");
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        String metrics = controller.getMetricsString();
        System.out.println(metrics);
        
        System.out.println("-".repeat(60));
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Generates a build report.
     */
    private void generateReport() {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("Generate Build Report");
        System.out.println("-".repeat(60));
        
        if (!controller.isTreeBuilt()) {
            System.out.println("❌ Tree not built yet. Please build the tree first.");
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.print("Enter output file path (default: kdtree_report.txt): ");
        String path = scanner.nextLine().trim();
        
        if (path.isEmpty()) {
            path = "kdtree_report.txt";
        }
        
        System.out.println("\n📄 Generating report...");
        boolean success = controller.generateReport(path);
        
        if (success) {
            System.out.println("✓ Report generated successfully: " + path);
        } else {
            System.out.println("❌ Error generating report.");
        }
        
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
}

