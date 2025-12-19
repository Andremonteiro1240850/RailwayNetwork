package UI;

import Controller.ShortestPathController;
import Domain.Station;
import Repository.RailwayGraphRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/**
 * Console-based user interface for risk-aware shortest paths operations (USEI15).
 * 
 * Provides menu-based navigation for:
 * - Calculating shortest paths between stations using costs (may be negative)
 * - Detecting negative cycles
 * - Listing all stations
 * - Viewing graph statistics
 * 
 * Handles invalid input gracefully and displays formatted results.
 *
 * @author Development Team
 * @version 1.0
 */
public class ShortestPathUI {
    
    private final ShortestPathController controller;
    private final Scanner scanner;
    
    /**
     * Constructs a ShortestPathUI with default controller.
     */
    public ShortestPathUI() {
        this.controller = new ShortestPathController(
            new RailwayGraphRepository()
        );
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Constructs a ShortestPathUI with custom controller.
     * Useful for dependency injection and testing.
     *
     * @param controller the shortest path controller
     */
    public ShortestPathUI(ShortestPathController controller) {
        if (controller == null) {
            throw new IllegalArgumentException("ShortestPathController cannot be null");
        }
        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Shows the main menu and handles user interaction.
     */
    public void showMainMenu() {
        // Load data and build graph on startup
        try {
            System.out.println("\nLoading graph data...");
            controller.buildGraph();
            System.out.println("✓ Graph loaded successfully!");
        } catch (Exception e) {
            System.out.println("\n✗ ERROR: Failed to load graph data");
            System.out.println("  " + e.getMessage());
            System.out.println("\nPress Enter to exit...");
            scanner.nextLine();
            return;
        }
        
        while (true) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("RISK-AWARE SHORTEST PATHS (USEI15)");
            System.out.println("=".repeat(60));
            System.out.println("1. Calculate Shortest Path");
            System.out.println("2. List All Stations");
            System.out.println("3. View Graph Statistics");
            System.out.println("0. Exit");
            
            System.out.print("\nSelect option: ");
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    calculatePath();
                    break;
                case "2":
                    listStations();
                    break;
                case "3":
                    showGraphStatistics();
                    break;
                case "0":
                    System.out.println("\nExiting... Goodbye!");
                    return;
                default:
                    System.out.println("\nERROR: Invalid option! Please try again.");
            }
        }
    }
    
    /**
     * Calculates and displays the shortest path between two stations.
     */
    private void calculatePath() {
        System.out.println("\n--- Calculate Shortest Path ---");
        
        try {
            // Get source station
            System.out.print("Enter source station ID: ");
            String sourceId = scanner.nextLine().trim();
            
            if (sourceId.isEmpty()) {
                System.out.println("ERROR: Source station ID cannot be empty.");
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
                return;
            }
            
            // Get target station
            System.out.print("Enter target station ID: ");
            String targetId = scanner.nextLine().trim();
            
            if (targetId.isEmpty()) {
                System.out.println("ERROR: Target station ID cannot be empty.");
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
                return;
            }
            
            // Validate stations exist
            Station source = controller.getStationById(sourceId);
            Station target = controller.getStationById(targetId);
            
            if (source == null) {
                System.out.println("ERROR: Source station not found: " + sourceId);
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
                return;
            }
            
            if (target == null) {
                System.out.println("ERROR: Target station not found: " + targetId);
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
                return;
            }
            
            // Display station names
            System.out.println("\nSource: " + source.getName() + " (" + sourceId + ")");
            System.out.println("Target: " + target.getName() + " (" + targetId + ")");
            System.out.println("\nCalculating shortest path...");
            
            // Calculate path
            ShortestPathController.ShortestPathResult result = 
                controller.calculateShortestPath(sourceId, targetId);
            
            // Display results
            System.out.println();
            
            if (result.hasNegativeCycle()) {
                System.out.println("✗ Negative cycle detected!");
                System.out.println();
                String cycleInfo = result.getCycleString();
                if (cycleInfo != null) {
                    System.out.println(cycleInfo);
                } else {
                    System.out.println("  Unable to extract cycle details.");
                }
            } else if (result.hasPath()) {
                System.out.println("✓ Path found:");
                String pathString = result.getPathString();
                if (pathString != null) {
                    System.out.println("  " + pathString);
                }
                double totalCost = result.getTotalCost();
                if (!Double.isNaN(totalCost)) {
                    System.out.println("  Total cost: " + String.format("%.2f", totalCost));
                }
            } else {
                System.out.println("✗ No path exists between the specified stations.");
            }
            
        } catch (IllegalArgumentException e) {
            System.out.println("\n✗ ERROR: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println("\n✗ ERROR: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n✗ ERROR: Failed to calculate shortest path");
            System.out.println("  " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("  Cause: " + e.getCause().getMessage());
            }
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Lists all stations in the graph.
     */
    private void listStations() {
        System.out.println("\n--- List All Stations ---");
        
        try {
            List<Station> stations = new ArrayList<>(controller.getAllStations());
            
            if (stations.isEmpty()) {
                System.out.println("No stations found in the graph.");
            } else {
                // Sort by name for easier reading
                stations.sort(Comparator.comparing(Station::getName));
                
                System.out.println("\nTotal stations: " + stations.size());
                System.out.println("\nID - Name");
                System.out.println("-".repeat(60));
                
                for (Station station : stations) {
                    System.out.println(station.getId() + " - " + station.getName());
                }
            }
            
        } catch (Exception e) {
            System.out.println("\n✗ ERROR: Failed to list stations");
            System.out.println("  " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("  Cause: " + e.getCause().getMessage());
            }
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Shows graph statistics.
     */
    private void showGraphStatistics() {
        System.out.println("\n--- Graph Statistics ---");
        
        try {
            String statistics = controller.getGraphStatistics();
            System.out.println(statistics);
            
        } catch (Exception e) {
            System.out.println("\n✗ ERROR: Failed to load graph statistics");
            System.out.println("  " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("  Cause: " + e.getCause().getMessage());
            }
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
}

