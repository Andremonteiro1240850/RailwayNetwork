package UI;

import Controller.WagonsUnloadingController;
import Domain.Bay;
import Domain.Box;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The type Wagons unloading ui.
 */
public class WagonsUnloadingUI {
    private final WagonsUnloadingController controller;
    private final Scanner scanner;

    /**
     * Instantiates a new Wagons unloading ui.
     *
     * @param controller the controller
     */
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
            return;
        }
        
        System.out.println("Starting wagon unloading process...");
        
        // Show debug information before unloading
        Repository.BoxRepository boxRepo = controller.getBoxRepository();
        Repository.BoxRepository.RepositoryStats stats = boxRepo.getStats();
        System.out.println("Current repository status:");
        System.out.println("  Unassigned boxes: " + stats.totalUnassigned + " (SKUs: " + stats.unassignedSkus + ")");
        System.out.println("  Assigned boxes: " + stats.totalAssigned + " (SKUs: " + stats.assignedSkus + ")");
        
        controller.unloadWagons();
        
        // Show detailed assignment information after unloading
        System.out.println("\nDETAILED BOX ASSIGNMENTS:");
        showDetailedAssignments(boxRepo);
        
    }

    /**
     * Dispatch stock for a specific SKU
     */
    private void dispatchStock() {
        System.out.println("\n=== DISPATCH STOCK ===");
        
        if (!controller.isDataLoaded()) {
            System.out.println("Data not loaded. Please load data first.");
            return;
        }
        
        // Show available SKUs
        List<String> skus = controller.getAvailableSkus();
        if (skus.isEmpty()) {
            System.out.println("No SKUs available.");
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
            return;
        }
        
        System.out.print("Enter quantity to dispatch: ");
        try {
            int quantity = Integer.parseInt(scanner.nextLine().trim());
            
            if (quantity <= 0) {
                System.out.println("Quantity must be positive.");
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
        
    }

    /**
     * Relocate a box to a different bay
     */
    private void relocateBox() {
        System.out.println("\n=== RELOCATE BOX ===");
        
        if (!controller.isDataLoaded()) {
            System.out.println("Data not loaded. Please load data first.");
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
        
    }

    /**
     * Show detailed box assignments by warehouse/aisle/bay
     */
    private void showDetailedAssignments(Repository.BoxRepository boxRepo) {
        Map<String, List<Box>> assignedBoxes = boxRepo.getAllAssignedBoxes();
        
        if (assignedBoxes.isEmpty()) {
            System.out.println("No boxes assigned yet.");
            return;
        }
        
        // Group boxes by location for better display
        Map<String, List<Box>> boxesByLocation = new HashMap<>();
        
        for (Map.Entry<String, List<Box>> entry : assignedBoxes.entrySet()) {
            String sku = entry.getKey();
            List<Box> boxes = entry.getValue();
            
            for (Box box : boxes) {
                if (box.getLocation() != null) {
                    String location = box.getLocation().toString();
                    boxesByLocation.computeIfAbsent(location, k -> new ArrayList<>()).add(box);
                }
            }
        }
        
        // Sort locations for consistent display (W1/A1/B1, W1/A1/B2, etc.)
        List<String> sortedLocations = new ArrayList<>(boxesByLocation.keySet());
        sortedLocations.sort((loc1, loc2) -> {
            // Parse location format: W1/A1/B1
            String[] parts1 = loc1.split("/");
            String[] parts2 = loc2.split("/");
            
            if (parts1.length != 3 || parts2.length != 3) {
                return loc1.compareTo(loc2); // fallback to string comparison
            }
            
            // Compare warehouse
            int warehouseCompare = parts1[0].compareTo(parts2[0]);
            if (warehouseCompare != 0) return warehouseCompare;
            
            // Compare aisle
            int aisleCompare = Integer.compare(
                Integer.parseInt(parts1[1].substring(1)), // A1 -> 1
                Integer.parseInt(parts2[1].substring(1))  // A1 -> 1
            );
            if (aisleCompare != 0) return aisleCompare;
            
            // Compare bay
            return Integer.compare(
                Integer.parseInt(parts1[2].substring(1)), // B1 -> 1
                Integer.parseInt(parts2[2].substring(1))  // B1 -> 1
            );
        });
        
        for (String location : sortedLocations) {
            List<Box> boxes = boxesByLocation.get(location);
            System.out.println("\n" + location + ":");
            
            // Group by SKU within this location
            Map<String, List<Box>> boxesBySku = boxes.stream()
                .collect(Collectors.groupingBy(Box::getSku));
            
            for (Map.Entry<String, List<Box>> skuEntry : boxesBySku.entrySet()) {
                String sku = skuEntry.getKey();
                List<Box> skuBoxes = skuEntry.getValue();
                
                System.out.println("  SKU " + sku + " (" + skuBoxes.size() + " boxes):");
                for (Box box : skuBoxes) {
                    String expiryInfo = box.getExpiryDate() != null ? 
                        " (expires: " + box.getExpiryDate() + ")" : " (non-perishable)";
                    System.out.println("    - " + box.getBoxId() + " (qty: " + box.getQty() + ")" + expiryInfo);
                }
            }
        }
    }

}
