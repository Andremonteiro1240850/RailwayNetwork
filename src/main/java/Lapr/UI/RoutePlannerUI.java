package Lapr.UI;

import Lapr.Controller.RouteController;
import Lapr.Domain.Freight;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * UI for USLP08 - Route Planner.
 * 
 * Allows Freight Manager to:
 * - View pending freights
 * - Define/assign routes (simple or complex) to accomplish pending freights
 * - View route with sequence of stations and cargos to be loaded/unloaded
 * 
 * @author Development Team
 * @version 1.0
 */
public class RoutePlannerUI implements Runnable {
    private final RouteController routeController;
    private final Scanner scanner;
    
    public RoutePlannerUI() {
        this.routeController = new RouteController();
        this.scanner = new Scanner(System.in);
    }
    
    public RoutePlannerUI(RouteController routeController) {
        this.routeController = routeController;
        this.scanner = new Scanner(System.in);
    }
    
    @Override
    public void run() {
        boolean exit = false;
        
        while (!exit) {
            showMenu();
            int option = readOption();
            
            switch (option) {
                case 1:
                    showPendingFreights();
                    break;
                case 2:
                    createSimpleRoute();
                    break;
                case 3:
                    createComplexRoute();
                    break;
                case 4:
                    showRouteDetails();
                    break;
                case 0:
                    exit = true;
                    System.out.println("Exiting Route Planner...");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
    
    /**
     * Shows the main menu.
     */
    private void showMenu() {
        System.out.println("\n=== Route Planner (USLP08) ===");
        System.out.println("1. View Pending Freights");
        System.out.println("2. Create Simple Route (single freight)");
        System.out.println("3. Create Complex Route (multiple freights)");
        System.out.println("4. Show Route Details");
        System.out.println("0. Exit");
        System.out.print("Select an option: ");
    }
    
    /**
     * Reads an option from the user.
     */
    private int readOption() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    /**
     * Shows all pending freights (not assigned to any route).
     */
    private void showPendingFreights() {
        System.out.println("\n=== Pending Freights ===");
        List<Freight> pendingFreights = routeController.getPendingFreights();
        
        if (pendingFreights.isEmpty()) {
            System.out.println("No pending freights found.");
        } else {
            System.out.println("Total pending freights: " + pendingFreights.size());
            System.out.println();
            for (Freight f : pendingFreights) {
                System.out.println("Freight ID: " + f.getId() + 
                                 " | Cargo Type: " + f.getCargoType() + 
                                 " | Wagon ID: " + f.getWagonID() + 
                                 " | Locomotive ID: " + f.getLocomotiveID());
            }
        }
    }
    
    /**
     * Creates a simple route for a single freight.
     */
    private void createSimpleRoute() {
        System.out.println("\n=== Create Simple Route ===");
        
        // Show pending freights
        List<Freight> pendingFreights = routeController.getPendingFreights();
        if (pendingFreights.isEmpty()) {
            System.out.println("No pending freights available. Cannot create route.");
            return;
        }
        
        System.out.println("Available pending freights:");
        for (Freight f : pendingFreights) {
            System.out.println("  " + f.getId() + " - " + f.getCargoType());
        }
        
        // Get freight ID
        System.out.print("Enter Freight ID: ");
        int freightId;
        try {
            freightId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("ERROR: Invalid freight ID.");
            return;
        }
        
        // Verify freight exists
        Freight selectedFreight = null;
        for (Freight f : pendingFreights) {
            if (f.getId() == freightId) {
                selectedFreight = f;
                break;
            }
        }
        
        if (selectedFreight == null) {
            System.out.println("ERROR: Freight ID not found in pending freights.");
            return;
        }
        
        // Get train ID
        System.out.print("Enter Train ID: ");
        int trainId;
        try {
            trainId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("ERROR: Invalid train ID.");
            return;
        }
        
        // Get sequence of stations
        List<Integer> stationIds = readStationSequence();
        if (stationIds.isEmpty()) {
            System.out.println("ERROR: At least 2 stations are required for a route.");
            return;
        }
        
        // Create route
        System.out.println("\nCreating simple route...");
        boolean success = routeController.createSimpleRoute(trainId, stationIds, freightId);
        
        if (success) {
            System.out.println("Simple route created successfully!");
            showRouteSummary(trainId, stationIds, List.of(selectedFreight));
        } else {
            System.out.println("ERROR: Failed to create simple route.");
        }
    }
    
    /**
     * Creates a complex route for multiple freights.
     */
    private void createComplexRoute() {
        System.out.println("\n=== Create Complex Route ===");
        
        // Show pending freights
        List<Freight> pendingFreights = routeController.getPendingFreights();
        if (pendingFreights.isEmpty()) {
            System.out.println("No pending freights available. Cannot create route.");
            return;
        }
        
        System.out.println("Available pending freights:");
        for (Freight f : pendingFreights) {
            System.out.println("  " + f.getId() + " - " + f.getCargoType());
        }
        
        // Get freight IDs
        System.out.print("Enter Freight IDs (comma-separated): ");
        String freightIdsStr = scanner.nextLine().trim();
        List<Integer> freightIds = new ArrayList<>();
        
        try {
            String[] parts = freightIdsStr.split(",");
            for (String part : parts) {
                freightIds.add(Integer.parseInt(part.trim()));
            }
        } catch (NumberFormatException e) {
            System.out.println("ERROR: Invalid freight ID format.");
            return;
        }
        
        if (freightIds.isEmpty()) {
            System.out.println("ERROR: At least one freight ID is required.");
            return;
        }
        
        // Verify freights exist
        List<Freight> selectedFreights = new ArrayList<>();
        for (int freightId : freightIds) {
            boolean found = false;
            for (Freight f : pendingFreights) {
                if (f.getId() == freightId) {
                    selectedFreights.add(f);
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("ERROR: Freight ID " + freightId + " not found in pending freights.");
                return;
            }
        }
        
        // Get train ID
        System.out.print("Enter Train ID: ");
        int trainId;
        try {
            trainId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("ERROR: Invalid train ID.");
            return;
        }
        
        // Get sequence of stations
        List<Integer> stationIds = readStationSequence();
        if (stationIds.isEmpty()) {
            System.out.println("ERROR: At least 2 stations are required for a route.");
            return;
        }
        
        // Create route
        System.out.println("\nCreating complex route...");
        boolean success = routeController.createComplexRoute(trainId, stationIds, freightIds);
        
        if (success) {
            System.out.println("Complex route created successfully!");
            showRouteSummary(trainId, stationIds, selectedFreights);
        } else {
            System.out.println("ERROR: Failed to create complex route.");
        }
    }
    
    /**
     * Reads a sequence of station IDs from the user.
     */
    private List<Integer> readStationSequence() {
        List<Integer> stationIds = new ArrayList<>();
        
        System.out.println("Enter sequence of stations (at least 2 stations):");
        System.out.println("Enter station IDs one by one (press Enter with empty line to finish):");
        
        while (true) {
            System.out.print("Station ID (or press Enter to finish): ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                if (stationIds.size() < 2) {
                    System.out.println("ERROR: At least 2 stations are required.");
                    continue;
                }
                break;
            }
            
            try {
                int stationId = Integer.parseInt(input);
                stationIds.add(stationId);
                System.out.println("Station " + stationId + " added. Total stations: " + stationIds.size());
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Invalid station ID. Please enter a number.");
            }
        }
        
        return stationIds;
    }
    
    /**
     * Shows route details with sequence of stations and cargos.
     */
    private void showRouteDetails() {
        System.out.println("\n=== Route Details ===");
        
        System.out.print("Enter Train ID: ");
        int trainId;
        try {
            trainId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("ERROR: Invalid train ID.");
            return;
        }
        
        // Get routes for this train
        List<Lapr.Domain.Route> routes = routeController.getAllRoutes();
        List<Lapr.Domain.Route> trainRoutes = new ArrayList<>();
        
        for (Lapr.Domain.Route route : routes) {
            if (route.getTrainID() == trainId) {
                trainRoutes.add(route);
            }
        }
        
        if (trainRoutes.isEmpty()) {
            System.out.println("No routes found for Train ID: " + trainId);
            return;
        }
        
        // Sort by order
        trainRoutes.sort((r1, r2) -> Integer.compare(r1.getOrder(), r2.getOrder()));
        
        // Get freights for this train (all freights assigned to this train)
        List<Freight> allFreights = routeController.verifyFreighAssigned();
        List<Freight> trainFreights = new ArrayList<>();
        for (Freight f : allFreights) {
            if (f.getTrainID() == trainId && f.getTrainID() != 0) {
                trainFreights.add(f);
            }
        }
        
        // Show route summary
        System.out.println("\n=== Route Summary for Train ID: " + trainId + " ===");
        System.out.println("\nSequence of Stations:");
        for (int i = 0; i < trainRoutes.size(); i++) {
            Lapr.Domain.Route route = trainRoutes.get(i);
            System.out.println("  " + (i + 1) + ". Station ID: " + route.getFacilityID() + " (Order: " + route.getOrder() + ")");
        }
        
        System.out.println("\nCargos to be Loaded/Unloaded:");
        if (trainFreights.isEmpty()) {
            System.out.println("  No cargos assigned to this route.");
        } else {
            for (Freight f : trainFreights) {
                System.out.println("  Freight ID: " + f.getId() + 
                                 " | Cargo Type: " + f.getCargoType() + 
                                 " | Wagon ID: " + f.getWagonID());
            }
        }
    }
    
    /**
     * Shows a route summary after creation.
     */
    private void showRouteSummary(int trainId, List<Integer> stationIds, List<Freight> freights) {
        System.out.println("\n=== Route Summary ===");
        System.out.println("Train ID: " + trainId);
        System.out.println("\nSequence of Stations:");
        for (int i = 0; i < stationIds.size(); i++) {
            System.out.println("  " + (i + 1) + ". Station ID: " + stationIds.get(i));
        }
        System.out.println("\nCargos to be Loaded/Unloaded:");
        for (Freight f : freights) {
            System.out.println("  Freight ID: " + f.getId() + 
                             " | Cargo Type: " + f.getCargoType() + 
                             " | Wagon ID: " + f.getWagonID());
        }
    }
}
