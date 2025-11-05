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
 * UI for Pick Path Sequencing (USEI04)
 * Simple interface to interact with the sequencing system
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
            System.out.println("\n=== PICK PATH SEQUENCING ===");
            System.out.println("1. Demo: ASC_SWEEP Strategy");
            System.out.println("2. Demo: NEAREST_NEIGHBOUR Strategy");
            System.out.println("3. Process (custom data)");
            System.out.println("4. Process (from US03 trolleys)");
            System.out.println("5. Back to Main Menu");

            System.out.print("Select option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    demonstrateAscSweepStrategy();
                    break;
                case "2":
                    demonstrateNearestNeighbourStrategy();
                    break;
                case "3":
                    processWithCustomData();
                    break;
                case "4":
                    processWithTrolleyData();
                    break;
                case "5":
                    return;
                default:
                    System.out.println("Invalid option! Please try again.");
            }
        }
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
    }

    /**
     * Process with trolley data from US03
     */
    private void processWithTrolleyData() {
        System.out.println("\n=== PROCESSING WITH TROLLEY DATA AS PLANNER ===");

        try {
            // Get trolley data from controller
            List<Trolley> trolleys = controller.getTrolleysFromUS03();

            if (trolleys.isEmpty()) {
                System.out.println("No trolleys found. Please run trolley packing first.");
                return;
            }

            System.out.println("Found " + trolleys.size() + " trolleys:");
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
    }

    /**
     * Read custom stops from user input
     */
    private List<PickStop> readCustomStops() {
        System.out.println("Enter stops (format: aisle,bay,quantity)");
        System.out.println("Example: 1,8,5");
        System.out.println("Type 'end' to finish");

        List<PickStop> stops = new ArrayList<>();
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
     * Converts a trolley to pick stops
     */
    private List<PickStop> trolleyToStops(Trolley trolley) {
        List<PickStop> stops = new ArrayList<>();
        Map<String, Integer> consolidatedStops = new java.util.HashMap<>();

        // Groups identical stops (same aisle/bay) summing quantities
        for (PickingItems item : trolley.getPickingItems()) {
            String key = item.getAisleId() + "," + item.getBayId();
            consolidatedStops.put(key, consolidatedStops.getOrDefault(key, 0) + item.getQty());
        }

        // Converts to PickStops
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
     * Converts all trolleys to pick stops (consolidated)
     */
    private List<PickStop> allTrolleysToStops(List<Trolley> trolleys) {
        List<PickStop> allStops = new ArrayList<>();
        Map<String, Integer> consolidatedStops = new java.util.HashMap<>();

        // Groups stops from all trolleys (same aisle/bay) summing quantities
        for (Trolley trolley : trolleys) {
            for (PickingItems item : trolley.getPickingItems()) {
                String key = item.getAisleId() + "," + item.getBayId();
                consolidatedStops.put(key, consolidatedStops.getOrDefault(key, 0) + item.getQty());
            }
        }

        // Converts to PickStops
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

    // ========== STRATEGY DEMONSTRATION METHODS ==========

    /**
     * Demonstrates how the ASC_SWEEP strategy works step by step
     */
    private void demonstrateAscSweepStrategy() {
        System.out.println("\n=== DEMONSTRATION: ASC_SWEEP STRATEGY ===");

        // Use example data
        List<PickStop> exampleStops = Arrays.asList(
                new PickStop(1, 3, 4, 5),
                new PickStop(2, 1, 8, 3),
                new PickStop(3, 2, 2, 7),
                new PickStop(4, 1, 1, 4),
                new PickStop(5, 3, 1, 2)
        );

        System.out.println("Original stops:");
        displayStops(exampleStops);

        System.out.println("\n--- ASC_SWEEP STEP BY STEP ---");
        System.out.println("1. Sort by aisle in ascending order");
        System.out.println("2. For the same aisle, sort by bay in ascending order");

        List<PickStop> sorted = controller.calculateAscSweep(exampleStops);

        System.out.println("\nSorted stops:");
        displayStops(sorted);

        System.out.println("\n--- DISTANCE FORMULA ---");
        System.out.println("If a1 == a2 (same aisle): distance = |b1 - b2|");
        System.out.println("If a1 != a2 (different aisles): distance = b1 + (|a1 - a2| * 3) + b2");

        System.out.println("\n--- TOTAL DISTANCE CALCULATION STEP BY STEP ---");
        int currentAisle = 0, currentBay = 0, total = 0;

        System.out.println("Starting at entrance (0,0):");

        for (int i = 0; i < sorted.size(); i++) {
            PickStop stop = sorted.get(i);
            int distance = PickPathController.calculateDistance(currentAisle, currentBay,
                    stop.getAisle(), stop.getBay());
            total += distance;

            System.out.println("  " + (i + 1) + ". (" + currentAisle + "," + currentBay + ") → " +
                    stop + " = " + distance + " units");

            // Show detailed calculation
            if (currentAisle == stop.getAisle()) {
                System.out.println("     Same aisle (" + currentAisle + "), distance = |" + currentBay + " - " + stop.getBay() + "| = " + distance);
            } else {
                System.out.println("     Different aisles (" + currentAisle + "→" + stop.getAisle() + "), distance = " +
                        currentBay + " + (|" + currentAisle + "-" + stop.getAisle() + "| * 3) + " + stop.getBay() + " = " + distance);
            }

            currentAisle = stop.getAisle();
            currentBay = stop.getBay();
        }

        System.out.println("\nTotal distance: " + total);

        System.out.println("\n--- FINAL RESULT ---");
        PickPath result = controller.createCalculatedPath(PathStrategy.ASC_SWEEP, exampleStops);
        System.out.println("PickPath created:");
        System.out.println("  Strategy: " + result.getStrategy());
        System.out.println("  Total Distance: " + result.getTotalDistance());
    }

    /**
     * Demonstrates how the NEAREST_NEIGHBOUR strategy works step by step
     */
    private void demonstrateNearestNeighbourStrategy() {
        System.out.println("\n=== DEMONSTRATION: NEAREST_NEIGHBOUR STRATEGY ===");

        // Use example data
        List<PickStop> exampleStops = Arrays.asList(
                new PickStop(1, 3, 4, 5),
                new PickStop(2, 1, 8, 3),
                new PickStop(3, 2, 2, 7),
                new PickStop(4, 1, 1, 4),
                new PickStop(5, 3, 1, 2)
        );

        System.out.println("Original stops:");
        displayStops(exampleStops);

        System.out.println("\n--- DISTANCE FORMULA ---");
        System.out.println("If a1 == a2 (same aisle): distance = |b1 - b2|");
        System.out.println("If a1 != a2 (different aisles): distance = b1 + (|a1 - a2| * 3) + b2");

        System.out.println("\n--- NEAREST_NEIGHBOUR STEP BY STEP ---");
        System.out.println("1. Start at entrance (0,0)");
        System.out.println("2. Find the closest stop from current position");
        System.out.println("3. Move to that stop and remove it from the list");
        System.out.println("4. Repeat until no more stops remain");

        List<PickStop> unvisited = new ArrayList<>(exampleStops);
        List<PickStop> path = new ArrayList<>();
        int currentAisle = 0;
        int currentBay = 0;
        int step = 1;
        int totalDistance = 0;

        while (!unvisited.isEmpty()) {
            System.out.println("\n--- STEP " + step + " ---");
            System.out.println("Current position: (" + currentAisle + "," + currentBay + ")");
            System.out.println("Available stops:");
            displayStops(unvisited);

            // Find the closest stop
            PickStop nearest = findNearestStopDemo(currentAisle, currentBay, unvisited);
            int distance = PickPathController.calculateDistance(currentAisle, currentBay,
                    nearest.getAisle(), nearest.getBay());
            totalDistance += distance;

            System.out.println("Closest stop: " + nearest + " (distance: " + distance + ")");

            // Show detailed distance calculation
            if (currentAisle == nearest.getAisle()) {
                System.out.println("  Same aisle (" + currentAisle + "), distance = |" + currentBay + " - " + nearest.getBay() + "| = " + distance);
            } else {
                System.out.println("  Different aisles (" + currentAisle + "→" + nearest.getAisle() + "), distance = " +
                        currentBay + " + (|" + currentAisle + "-" + nearest.getAisle() + "| * 3) + " + nearest.getBay() + " = " + distance);
            }

            path.add(nearest);
            unvisited.remove(nearest);
            currentAisle = nearest.getAisle();
            currentBay = nearest.getBay();
            step++;
        }

        System.out.println("\n--- FINAL RESULT ---");
        System.out.println("Calculated path:");
        displayStops(path);
        System.out.println("Total distance: " + totalDistance);

        PickPath result = controller.createCalculatedPath(PathStrategy.NEAREST_NEIGHBOUR, exampleStops);
        System.out.println("\nPickPath created:");
        System.out.println("  Strategy: " + result.getStrategy());
        System.out.println("  Total Distance: " + result.getTotalDistance());
    }

    /**
     * Helper method for nearest neighbour demonstration
     */
    private PickStop findNearestStopDemo(int currentAisle, int currentBay, List<PickStop> stops) {
        PickStop nearest = null;
        int minDistance = Integer.MAX_VALUE;

        for (PickStop stop : stops) {
            int distance = PickPathController.calculateDistance(currentAisle, currentBay,
                    stop.getAisle(), stop.getBay());
            if (distance < minDistance) {
                minDistance = distance;
                nearest = stop;
            }
        }
        return nearest;
    }
}
