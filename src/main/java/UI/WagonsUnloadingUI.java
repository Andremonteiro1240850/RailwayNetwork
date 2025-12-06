package UI;

import Sprint1.Controller.WagonsUnloadingController;
import Sprint1.Domain.Box;
import Sprint1.Domain.Bay;
import Sprint1.Domain.BayId;
import Sprint1.Repository.BoxRepository;

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
        
        // Show status before unloading
        BoxRepository boxRepo = controller.getBoxRepository();
        BoxRepository.RepositoryStats statsBefore = boxRepo.getStats();
        System.out.println("\nCurrent repository status:");
        System.out.println("  Unassigned boxes: " + statsBefore.totalUnassigned + " (SKUs: " + statsBefore.unassignedSkus + ")");
        System.out.println("  Assigned boxes: " + statsBefore.totalAssigned + " (SKUs: " + statsBefore.assignedSkus + ")");
        
        // Get unassigned boxes to show processing info
        Map<String, List<Box>> unassignedBoxes = boxRepo.getAllUnassignedBoxes();
        
        if (unassignedBoxes.isEmpty()) {
            System.out.println("\nNo unassigned boxes to process.");
            return;
        }
        
        System.out.println("\nStarting wagon unloading process...");
        
        // Show what will be processed
        int totalBoxes = unassignedBoxes.values().stream().mapToInt(List::size).sum();
        System.out.println("Processing " + totalBoxes + " unassigned boxes across " + unassignedBoxes.size() + " SKU(s)...");
        
        // Track assignments for feedback
        Map<String, Integer> assignmentsBySku = new HashMap<>();
        Map<String, Integer> unassignedBySku = new HashMap<>();
        
        for (Map.Entry<String, List<Box>> entry : unassignedBoxes.entrySet()) {
            String sku = entry.getKey();
            List<Box> boxes = entry.getValue();
            assignmentsBySku.put(sku, 0);
            unassignedBySku.put(sku, boxes.size());
            
            System.out.println("\nProcessing " + boxes.size() + " unassigned boxes for SKU: " + sku);
        }
        
        // Execute unloading
        controller.unloadWagons();
        
        // Show results
        BoxRepository.RepositoryStats statsAfter = boxRepo.getStats();
        int assignedCount = statsBefore.totalUnassigned - statsAfter.totalUnassigned;
        
        System.out.println("\n=== UNLOADING COMPLETED ===");
        System.out.println("Boxes assigned: " + assignedCount);
        System.out.println("Remaining unassigned: " + statsAfter.totalUnassigned);
        
        if (statsAfter.totalUnassigned > 0) {
            System.out.println("\nWarning: Some boxes could not be assigned (no available bays).");
        }
        
        // Show detailed assignment information after unloading
        if (statsAfter.totalAssigned > 0) {
            System.out.println("\n=== DETAILED BOX ASSIGNMENTS ===");
            showDetailedAssignments(boxRepo);
        }
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
            System.out.println("Invalid SKU: " + sku + " not found in catalog.");
            return;
        }
        
        System.out.print("Enter quantity to dispatch: ");
        try {
            int quantity = Integer.parseInt(scanner.nextLine().trim());
            
            if (quantity <= 0) {
                System.out.println("Quantity must be positive.");
                return;
            }
            
            System.out.println("\nDispatching " + quantity + " units of SKU " + sku + "...");
            List<Box> dispatchedBoxes = controller.dispatchStock(sku, quantity);
            
            if (dispatchedBoxes.isEmpty()) {
                System.out.println("No boxes were dispatched. Insufficient stock available.");
            } else {
                int totalDispatched = dispatchedBoxes.stream().mapToInt(Box::getQty).sum();
                System.out.println("\nSuccessfully dispatched " + totalDispatched + " units in " + dispatchedBoxes.size() + " box(es):");
                
                for (Box box : dispatchedBoxes) {
                    String location = box.getLocation() != null ? " from " + box.getLocation() : "";
                    String type = box.getBoxId().contains("_PARTIAL") ? " (partial)" : " (full box)";
                    System.out.println("  - " + box.getBoxId() + type + " - qty: " + box.getQty() + location);
                }
                
                if (totalDispatched < quantity) {
                    System.out.println("\nWarning: Requested " + quantity + " units, but only " + totalDispatched + " units were available.");
                }
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity format. Please enter a valid number.");
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
        
        // Check if box exists first
        BoxRepository boxRepo = controller.getBoxRepository();
        Box box = boxRepo.getBoxById(boxId);
        if (box == null) {
            System.out.println("Box " + boxId + " not found.");
            return;
        }
        
        // Show current location
        BayId currentLocation = box.getLocation();
        if (currentLocation == null) {
            System.out.println("Box " + boxId + " is not assigned to any bay.");
            return;
        }
        
        System.out.println("Current location: " + currentLocation);
        System.out.println("Box SKU: " + box.getSku() + " | Quantity: " + box.getQty());
        
        System.out.print("\nEnter target warehouse ID: ");
        String warehouseId = scanner.nextLine().trim();
        
        System.out.print("Enter target aisle: ");
        try {
            int aisle = Integer.parseInt(scanner.nextLine().trim());
            
            System.out.print("Enter target bay: ");
            int bay = Integer.parseInt(scanner.nextLine().trim());
            
            BayId targetBayId = new BayId(warehouseId, aisle, bay);
            
            // Check if target bay exists
            Bay targetBay = controller.getBayMap().get(targetBayId);
            if (targetBay == null) {
                System.out.println("Target bay " + targetBayId + " not found.");
                return;
            }
            
            // Check if target bay is full
            if (targetBay.isFull()) {
                System.out.println("Target bay " + targetBayId + " is full (capacity: " + targetBay.getCapacityBoxes() + ").");
                return;
            }
            
            // Check if target bay can accept this SKU
            if (!targetBay.canAcceptSku(box.getSku())) {
                System.out.println("Target bay " + targetBayId + " is dedicated to SKU " + targetBay.getDedicatedSku() + 
                                 ", cannot accept SKU " + box.getSku() + ".");
                return;
            }
            
            System.out.println("\nRelocating box " + boxId + " to " + targetBayId + "...");
            boolean success = controller.relocateBox(boxId, warehouseId, aisle, bay);
            
            if (success) {
                System.out.println("Box " + boxId + " successfully relocated from " + currentLocation + " to " + targetBayId + "!");
            } else {
                System.out.println("Failed to relocate box. Please check the inputs and try again.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid aisle or bay number. Please enter valid numbers.");
        }
        
    }

    /**
     * Show detailed box assignments by warehouse/aisle/bay
     */
    private void showDetailedAssignments(BoxRepository boxRepo) {
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
