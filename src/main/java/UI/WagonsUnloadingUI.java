package UI;

import Controller.WagonsUnloadingController;
import Domain.Bay;
import Domain.Box;
import java.util.List;
import java.util.Scanner;

public class WagonsUnloadingUI {
    private final WagonsUnloadingController controller;
    private final Scanner scanner;

    public WagonsUnloadingUI(WagonsUnloadingController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Main menu for wagon unloading operations
     */
    public void showMainMenu() {
        while (true) {
            System.out.println("\n=== WAGONS UNLOADING SYSTEM ===");
            System.out.println("1. Unload Wagons (FEFO/FIFO)");
            System.out.println("2. Dispatch Stock");
            System.out.println("3. Relocate Box");
            System.out.println("4. Back to Main Menu");
            
            System.out.print("Select option: ");
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    unloadWagons();
                    break;
                case "2":
                    dispatchStock();
                    break;
                case "3":
                    relocateBox();
                    break;
                case "4":
                    return;
                default:
            System.out.println("Invalid option! Please try again.");
            }
        }
    }


    /**
     * Execute wagon unloading process
     */
    private void unloadWagons() {
        System.out.println("\n=== UNLOADING WAGONS ===");
        
        if (!controller.isDataLoaded()) {
            System.out.println("Data not loaded. Please load data first.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("Starting wagon unloading process...");
        controller.unloadWagons();
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Dispatch stock for a specific SKU
     */
    private void dispatchStock() {
        System.out.println("\n=== DISPATCH STOCK ===");
        
        if (!controller.isDataLoaded()) {
            System.out.println("Data not loaded. Please load data first.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        // Show available SKUs
        List<String> skus = controller.getAvailableSkus();
        if (skus.isEmpty()) {
            System.out.println("No SKUs available.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("Available SKUs:");
        for (int i = 0; i < skus.size(); i++) {
            System.out.println((i + 1) + ". " + skus.get(i));
        }
        
        System.out.print("\nEnter SKU: ");
        String sku = scanner.nextLine().trim();
        
        if (!skus.contains(sku)) {
            System.out.println("Invalid SKU.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.print("Enter quantity to dispatch: ");
        try {
            int quantity = Integer.parseInt(scanner.nextLine().trim());
            
            if (quantity <= 0) {
                System.out.println("Quantity must be positive.");
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
                return;
            }
            
            List<Box> dispatchedBoxes = controller.dispatchStock(sku, quantity);
            
            if (dispatchedBoxes.isEmpty()) {
                System.out.println("No boxes were dispatched.");
            } else {
                System.out.println("Dispatched " + dispatchedBoxes.size() + " boxes:");
                for (Box box : dispatchedBoxes) {
                    System.out.println("  - " + box.getBoxId() + " (qty: " + box.getQty() + ")");
                }
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity format.");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Relocate a box to a different bay
     */
    private void relocateBox() {
        System.out.println("\n=== RELOCATE BOX ===");
        
        if (!controller.isDataLoaded()) {
            System.out.println("Data not loaded. Please load data first.");
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.print("Enter box ID to relocate: ");
        String boxId = scanner.nextLine().trim();
        
        System.out.print("Enter target warehouse ID: ");
        String warehouseId = scanner.nextLine().trim();
        
        System.out.print("Enter target aisle: ");
        try {
            int aisle = Integer.parseInt(scanner.nextLine().trim());
            
            System.out.print("Enter target bay: ");
            int bay = Integer.parseInt(scanner.nextLine().trim());
            
            boolean success = controller.relocateBox(boxId, warehouseId, aisle, bay);
            
            if (success) {
                System.out.println("Box relocated successfully!");
            } else {
                System.out.println("Failed to relocate box. Check if box exists and target bay is available.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid aisle or bay number.");
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

}
