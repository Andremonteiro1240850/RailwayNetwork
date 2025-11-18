package UI;

import Controller.RadiusSearchController;
import Domain.KDTree;
import Domain.RadiusSearchSummary;
import Domain.AVL;
import Domain.BST;
import Domain.Station;

import java.util.*;

/**
 * User interface for radius search with density summary (USEI10).
 * Allows users to search for stations within a radius and view summary statistics
 * by country and isCity.
 * 
 * @author Development Team
 * @version 1.0
 */
public class RadiusSearchUI {
    
    private final RadiusSearchController controller;
    private final Scanner scanner;
    
    /**
     * Constructs a new RadiusSearchUI with the specified controller.
     *
     * @param controller the radius search controller
     */
    public RadiusSearchUI(RadiusSearchController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Shows the main menu for radius search operations.
     */
    public void showMainMenu() {
        System.out.println("\n=== RADIUS SEARCH AND DENSITY SUMMARY ===");
        
        List<Station> stations = controller.getAllStations();
        if (stations == null || stations.isEmpty()) {
            System.out.println("No stations loaded. Please check the CSV file.");
            return;
        }
        
        System.out.println("Loading stations...");
        System.out.println("Stations loaded successfully! Total: " + stations.size());
        
        while (true) {
            System.out.println("\n" + StringUtils.repeat("=", 70));
            System.out.println("                    RADIUS SEARCH & DENSITY SUMMARY");
            System.out.println(StringUtils.repeat("=", 70));
            System.out.println();
            System.out.println("  1. Search stations within radius");
            System.out.println("  2. View sample queries");
            System.out.println("  0. Back to main menu");
            System.out.println();
            System.out.print("  Select your option: ");
            
            String userChoice = scanner.nextLine().trim();
            
            switch (userChoice) {
                case "1":
                    performRadiusSearch();
                    break;
                case "2":
                    showSampleQueries();
                    break;
                case "0":
                    System.out.println("\n  Returning to main menu...");
                    return;
                default:
                    System.out.println("\n  [ERROR] Invalid option! Please enter a number between 0 and 2.");
            }
        }
    }
    
    /**
     * Performs a radius search and displays results with summary.
     */
    private void performRadiusSearch() {
        System.out.println("\n" + StringUtils.repeat("=", 70));
        System.out.println("                    RADIUS SEARCH");
        System.out.println(StringUtils.repeat("=", 70));
        System.out.println();
        System.out.println("  Please enter the target point and search radius:");
        System.out.println();
        
        try {
            double targetLat = readDouble("  Target latitude: ", -90, 90);
            double targetLon = readDouble("  Target longitude: ", -180, 180);
            
            System.out.println();
            System.out.print("  Search radius (in kilometers): ");
            String radiusStr = scanner.nextLine().trim();
            double radiusKm;
            try {
                radiusKm = Double.parseDouble(radiusStr);
                if (radiusKm < 0) {
                    System.out.println("\n  [ERROR] Radius must be non-negative!");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("\n  [ERROR] Invalid number format!");
                return;
            }
            
            System.out.println();
            System.out.println("  Searching for stations within radius...");
            
            // Perform search
            RadiusSearchController.RadiusSearchResult result = 
                controller.searchRadiusWithSummary(targetLat, targetLon, radiusKm);
            
            // Display results
            displayResults(result, targetLat, targetLon, radiusKm);
            
        } catch (Exception e) {
            System.out.println("\n  [ERROR] An error occurred: " + e.getMessage());
        }
    }
    
    /**
     * Displays the search results including the AVL tree and summary statistics.
     *
     * @param result the search result containing tree and summary
     * @param targetLat target latitude
     * @param targetLon target longitude
     * @param radiusKm search radius
     */
    private void displayResults(RadiusSearchController.RadiusSearchResult result, 
                                double targetLat, double targetLon, double radiusKm) {
        AVL<KDTree.StationDistance> resultTree = result.getResultTree();
        RadiusSearchSummary summary = result.getSummary();
        
        System.out.println();
        System.out.println("  " + StringUtils.repeat("=", 68));
        System.out.println("                          SEARCH RESULTS");
        System.out.println("  " + StringUtils.repeat("=", 68));
        System.out.println();
        
        System.out.println("  Target Point:");
        System.out.printf("     Latitude:  %.6f\n", targetLat);
        System.out.printf("     Longitude: %.6f\n", targetLon);
        System.out.printf("     Radius:    %.2f km\n", radiusKm);
        
        System.out.println();
        System.out.println("  " + StringUtils.repeat("-", 68));
        System.out.println();
        
        // Display summary
        displaySummary(summary);
        
        System.out.println();
        System.out.println("  " + StringUtils.repeat("-", 68));
        System.out.println();
        
        // Display stations from AVL tree (in-order traversal = sorted by distance ASC, name DESC)
        if (resultTree.isEmpty()) {
            System.out.println("  No stations found within the specified radius.");
        } else {
            System.out.printf("  Found %d station(s) (sorted by distance ASC, name DESC):\n", summary.getTotalStations());
            System.out.println();
            
            int index = 1;
            for (KDTree.StationDistance sd : resultTree.inOrder()) {
                System.out.printf("  %3d. %s\n", index++, formatStationDistance(sd));
            }
        }
        
        System.out.println();
        System.out.println("  " + StringUtils.repeat("=", 68));
    }
    
    /**
     * Displays the summary statistics by country and isCity.
     *
     * @param summary the summary to display
     */
    private void displaySummary(RadiusSearchSummary summary) {
        System.out.println("  DENSITY SUMMARY:");
        System.out.println();
        
        System.out.println("  Total Stations: " + summary.getTotalStations());
        System.out.println();
        
        System.out.println("  Distribution by City Status:");
        System.out.println("     Cities:     " + summary.getStationsByCity());
        System.out.println("     Non-Cities: " + summary.getStationsByNonCity());
        System.out.println();
        
        Map<String, Integer> byCountry = summary.getStationsByCountry();
        if (!byCountry.isEmpty()) {
            System.out.println("  Distribution by Country:");
            // Sort by count (descending) then by country name (ascending)
            List<Map.Entry<String, Integer>> sorted = new ArrayList<>(byCountry.entrySet());
            sorted.sort((a, b) -> {
                int countCompare = b.getValue().compareTo(a.getValue());
                if (countCompare != 0) return countCompare;
                return a.getKey().compareTo(b.getKey());
            });
            
            for (Map.Entry<String, Integer> entry : sorted) {
                System.out.printf("     %s: %d station(s)\n", entry.getKey(), entry.getValue());
            }
        }
    }
    
    /**
     * Formats a station-distance pair for display.
     *
     * @param sd station-distance pair
     * @return formatted string
     */
    private String formatStationDistance(KDTree.StationDistance sd) {
        return String.format("%s | Distance: %.2f km | %s | %s",
            sd.getStation().getName(),
            sd.getDistance(),
            sd.getStation().getCountry(),
            sd.getStation().isCity() ? "City" : "Non-City"
        );
    }
    
    /**
     * Reads a double value from user input with validation.
     *
     * @param prompt the prompt message
     * @param min    minimum allowed value
     * @param max    maximum allowed value
     * @return the validated double value
     */
    private double readDouble(String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            String userInput = scanner.nextLine().trim();
            
            if (userInput.isEmpty()) {
                System.out.println("    [WARNING] Please enter a value. Try again.");
                continue;
            }
            
            try {
                double parsedValue = Double.parseDouble(userInput);
                
                if (parsedValue < min || parsedValue > max) {
                    System.out.printf("    [ERROR] Value must be between %.1f and %.1f. Please try again.\n", min, max);
                    continue;
                }
                
                return parsedValue;
                
            } catch (NumberFormatException e) {
                System.out.println("    [ERROR] Invalid number format. Please enter a valid number (e.g., 38.7 or -9.2).");
            }
        }
    }
    
    /**
     * Shows sample queries for demonstration.
     */
    private void showSampleQueries() {
        System.out.println("\n" + StringUtils.repeat("=", 70));
        System.out.println("                        SAMPLE QUERIES");
        System.out.println(StringUtils.repeat("=", 70));
        System.out.println();
        System.out.println("  Here are some example queries you can try:");
        System.out.println();
        
        System.out.println("  " + StringUtils.repeat("-", 68));
        System.out.println("  1. Search stations within 50 km of Lisbon");
        System.out.println("  " + StringUtils.repeat("-", 68));
        System.out.println("     Latitude:  38.7223");
        System.out.println("     Longitude: -9.1393");
        System.out.println("     Radius:    50 km");
        System.out.println();
        
        System.out.println("  " + StringUtils.repeat("-", 68));
        System.out.println("  2. Search stations within 100 km of Madrid");
        System.out.println("  " + StringUtils.repeat("-", 68));
        System.out.println("     Latitude:  40.4168");
        System.out.println("     Longitude: -3.7038");
        System.out.println("     Radius:    100 km");
        System.out.println();
        
        System.out.println("  " + StringUtils.repeat("-", 68));
        System.out.println("  3. Search stations within 25 km of Porto");
        System.out.println("  " + StringUtils.repeat("-", 68));
        System.out.println("     Latitude:  41.1579");
        System.out.println("     Longitude: -8.6291");
        System.out.println("     Radius:    25 km");
        System.out.println();
        
        System.out.println("  Tip: You can copy these values when using the search option!");
        System.out.println();
        System.out.println(StringUtils.repeat("=", 70));
    }
}

