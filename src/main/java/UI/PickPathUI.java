package UI;

import Controller.PickPathController;
import Domain.PickPath;
import Domain.PickStop;
import Domain.PickingItems;
import Domain.Trolley;
import Domain.ENUM.PathStrategy;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * UI para Pick Path Sequencing (USEI04)
 * Interface simples para interagir com o sistema de sequenciação
 */
public class PickPathUI {
    private final PickPathController controller;
    private final Scanner scanner;

    /**
     * Instantiates a new Pick path ui.
     *
     * @param controller the controller
     */
    public PickPathUI(PickPathController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Main menu for USEI04
     */
    public void showMainMenu() {
        while (true) {
            System.out.println("\n=== PICK PATH SEQUENCING (USEI04) ===");
            System.out.println("1. Process (simulated data)");
            System.out.println("2. Process (custom data)");
            System.out.println("3. Process (from US03 trolleys)");
            System.out.println("4. Back to Main Menu");
            
            System.out.print("Select option: ");
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    processWithSimulatedData();
                    break;
                case "2":
                    processWithCustomData();
                    break;
                case "3":
                    processWithTrolleyData();
                    break;
                case "4":
                    return;
                default:
                    System.out.println("Invalid option! Please try again.");
            }
        }
    }
    
    /**
     * Process with simulated data (as it was in Main)
     */
    private void processWithSimulatedData() {
        System.out.println("\n=== PROCESSING WITH SIMULATED DATA ===");
        
        // Simulate input data (when USEI03 is ready, it will come from trolley packing)
        List<PickStop> inputStops = simulateTrolleyPackingOutput();
        
        System.out.println("Input stops (before consolidation):");
        displayStops(inputStops);
        
        try {
            // Process using the controller
            Map<PathStrategy, PickPath> results = controller.processPickPathSequencing(inputStops);
            
            // Show results
            displayPathResults(results);
            
        } catch (Exception e) {
            System.out.println("Error while processing: " + e.getMessage());
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Process with user-provided custom data
     */
    private void processWithCustomData() {
        System.out.println("\n=== PROCESSING WITH CUSTOM DATA ===");
        
        try {
            List<PickStop> inputStops = readCustomStops();
            
            if (inputStops.isEmpty()) {
                System.out.println("No stops were entered.");
                return;
            }
            
            System.out.println("\nInserted stops:");
            displayStops(inputStops);
            
            // Process using the controller
            Map<PathStrategy, PickPath> results = controller.processPickPathSequencing(inputStops);
            
            // Show results
            displayPathResults(results);
            
        } catch (Exception e) {
            System.out.println("Error while processing: " + e.getMessage());
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Process with trolley data from US03
     */
    private void processWithTrolleyData() {
        System.out.println("\n=== PROCESSING WITH TROLLEY DATA FROM US03 ===");
        
        try {
            // Get trolley data from controller
            List<Trolley> trolleys = controller.getTrolleysFromUS03();
            
            if (trolleys.isEmpty()) {
                System.out.println("No trolleys found from US03. Please run trolley packing first.");
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
                return;
            }
            
            System.out.println("Found " + trolleys.size() + " trolleys from US03:");
            for (int i = 0; i < trolleys.size(); i++) {
                Trolley trolley = trolleys.get(i);
                System.out.println("  " + (i + 1) + ". Trolley ID: " + trolley.getId() + 
                                 " (Load: " + trolley.getCurrentLoad() + "/" + trolley.getMaxCapacity() + 
                                 ", Items: " + trolley.getPickingItems().size() + ")");
            }
            
            // Let user select which trolley to process
            System.out.print("\nSelect trolley number to process (or 'all' for all trolleys): ");
            String choice = scanner.nextLine().trim();
            
            List<PickStop> inputStops;
            
            if (choice.equalsIgnoreCase("all")) {
                // Process all trolleys
                inputStops = allTrolleysToStops(trolleys);
                System.out.println("Processing all trolleys combined...");
            } else {
                try {
                    int trolleyIndex = Integer.parseInt(choice) - 1;
                    if (trolleyIndex < 0 || trolleyIndex >= trolleys.size()) {
                        System.out.println("Invalid trolley selection.");
                        return;
                    }
                    
                    Trolley selectedTrolley = trolleys.get(trolleyIndex);
                    inputStops = trolleyToStops(selectedTrolley);
                    System.out.println("Processing trolley ID: " + selectedTrolley.getId());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number or 'all'.");
                    return;
                }
            }
            
            System.out.println("\nInput stops (from trolley data):");
            displayStops(inputStops);
            
            // Process using the controller
            Map<PathStrategy, PickPath> results = controller.processPickPathSequencing(inputStops);
            
            // Show results
            displayPathResults(results);
            
        } catch (Exception e) {
            System.out.println("Error while processing trolley data: " + e.getMessage());
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Read custom stops from user input
     */
    private List<PickStop> readCustomStops() {
        System.out.println("Enter stops (format: aisle,bay,quantity)");
        System.out.println("Example: 1,8,5");
        System.out.println("Type 'end' to finish");
        
        List<PickStop> stops = new java.util.ArrayList<>();
        int seq = 1;
        
        while (true) {
            System.out.print("Stop " + seq + ": ");
            String input = scanner.nextLine().trim();
            
            if (input.equalsIgnoreCase("end")) {
                break;
            }
            
            try {
                String[] parts = input.split(",");
                if (parts.length != 3) {
                    System.out.println("Invalid format. Use: aisle,bay,quantity");
                    continue;
                }
                
                int aisle = Integer.parseInt(parts[0].trim());
                int bay = Integer.parseInt(parts[1].trim());
                int qty = Integer.parseInt(parts[2].trim());
                
                if (aisle < 0 || bay < 0 || qty <= 0) {
                    System.out.println("Invalid values. Aisle and bay >= 0, quantity > 0");
                    continue;
                }
                
                stops.add(new PickStop(seq++, aisle, bay, qty));
                System.out.println("Stop added: (" + aisle + "," + bay + ") qty=" + qty);
                
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format. Try again.");
            }
        }
        
        return stops;
    }
    
    /**
     * Converte um trolley em stops de picking
     */
    private List<PickStop> trolleyToStops(Trolley trolley) {
        List<PickStop> stops = new ArrayList<>();
        Map<String, Integer> consolidatedStops = new java.util.HashMap<>();
        
        // Agrupa stops iguais (mesmo aisle/bay) somando as quantidades
        for (PickingItems item : trolley.getPickingItems()) {
            String key = item.getAisleId() + "," + item.getBayId();
            consolidatedStops.put(key, consolidatedStops.getOrDefault(key, 0) + item.getQty());
        }
        
        // Converte para PickStops
        int seq = 1;
        for (Map.Entry<String, Integer> entry : consolidatedStops.entrySet()) {
            String[] parts = entry.getKey().split(",");
            int aisle = Integer.parseInt(parts[0]);
            int bay = Integer.parseInt(parts[1]);
            int qty = entry.getValue();
            
            stops.add(new PickStop(seq++, aisle, bay, qty));
        }
        
        return stops;
    }
    
    /**
     * Converte todos os trolleys em stops de picking (consolidados)
     */
    private List<PickStop> allTrolleysToStops(List<Trolley> trolleys) {
        List<PickStop> allStops = new ArrayList<>();
        Map<String, Integer> consolidatedStops = new java.util.HashMap<>();
        
        // Agrupa stops de todos os trolleys (mesmo aisle/bay) somando as quantidades
        for (Trolley trolley : trolleys) {
            for (PickingItems item : trolley.getPickingItems()) {
                String key = item.getAisleId() + "," + item.getBayId();
                consolidatedStops.put(key, consolidatedStops.getOrDefault(key, 0) + item.getQty());
            }
        }
        
        // Converte para PickStops
        int seq = 1;
        for (Map.Entry<String, Integer> entry : consolidatedStops.entrySet()) {
            String[] parts = entry.getKey().split(",");
            int aisle = Integer.parseInt(parts[0]);
            int bay = Integer.parseInt(parts[1]);
            int qty = entry.getValue();
            
            allStops.add(new PickStop(seq++, aisle, bay, qty));
        }
        
        return allStops;
    }
    
    /**
     * Simulate trolley packing data (as it was in Main)
     */
    private List<PickStop> simulateTrolleyPackingOutput() {
        return Arrays.asList(
                new PickStop(1, 1, 8, 5),   // Trolley 1
                new PickStop(2, 2, 2, 3),   // Trolley 1
                new PickStop(3, 2, 2, 2),   // Trolley 2 - DUPLICATE (same aisle/bay)
                new PickStop(4, 3, 4, 7),   // Trolley 2
                new PickStop(5, 1, 1, 4),   // Trolley 3
                new PickStop(6, 2, 2, 1)    // Trolley 3 - ANOTHER DUPLICATE
        );
    }
    
    /**
     * Display a list of stops
     */
    private void displayStops(List<PickStop> stops) {
        for (int i = 0; i < stops.size(); i++) {
            PickStop stop = stops.get(i);
            System.out.println("  " + (i + 1) + ". " + stop);
        }
    }
    
    /**
     * Display the results of sequencing strategies
     */
    private void displayPathResults(Map<PathStrategy, PickPath> results) {
        System.out.println("\n--- STRATEGY A - ASC SWEEP ---");
        PickPath ascSweep = results.get(PathStrategy.ASC_SWEEP);
        System.out.println("Total Distance: " + ascSweep.getTotalDistance());
        System.out.println("Path:");
        displayStops(ascSweep.getStops());
        
        System.out.println("\n--- STRATEGY B - NEAREST NEIGHBOUR ---");
        PickPath nearestNeighbour = results.get(PathStrategy.NEAREST_NEIGHBOUR);
        System.out.println("Total Distance: " + nearestNeighbour.getTotalDistance());
        System.out.println("Path:");
        displayStops(nearestNeighbour.getStops());
        
        // Compare strategies
        System.out.println("\n--- COMPARISON ---");
        if (ascSweep.getTotalDistance() < nearestNeighbour.getTotalDistance()) {
            System.out.println("ASC_SWEEP is more efficient (" + ascSweep.getTotalDistance() + " vs " + nearestNeighbour.getTotalDistance() + ")");
        } else if (nearestNeighbour.getTotalDistance() < ascSweep.getTotalDistance()) {
            System.out.println("NEAREST_NEIGHBOUR is more efficient (" + nearestNeighbour.getTotalDistance() + " vs " + ascSweep.getTotalDistance() + ")");
        } else {
            System.out.println("Both strategies have the same total distance");
        }
    }
}
