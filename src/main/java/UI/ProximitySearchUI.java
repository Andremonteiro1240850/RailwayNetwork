package UI;

import Sprint2.Controller.ProximitySearchController;
import Sprint1.Domain.Station;
import Sprint2.Domain.SearchFilters;
import Sprint2.Domain.KDTree;
import Sprint2.Domain.ENUM.Country;

import java.util.List;
import java.util.Scanner;

/**
 * User interface for proximity search operations (Nearest-N search).
 * Allows finding the N nearest stations to a target coordinate with optional filters.
 * 
 * @author Development Team
 * @version 1.0
 */
public class ProximitySearchUI {
    
    private final ProximitySearchController controller;
    private final Scanner scanner;
    
    /**
     * Constructs a new ProximitySearchUI with the specified controller.
     *
     * @param controller the proximity search controller
     */
    public ProximitySearchUI(ProximitySearchController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Shows the main menu for proximity search (Nearest-N search).
     */
    public void showMainMenu() {
        while (true) {
            System.out.println("\n" + StringUtils.repeat("=",70));
            System.out.println("                    PROXIMITY SEARCH");
            System.out.println(StringUtils.repeat("=",70));
            System.out.println();
            System.out.println("  1. Find N nearest stations to a point");
            System.out.println("  2. Find N nearest stations with filters");
            System.out.println("  3. View KD-Tree statistics");
            System.out.println("  0. Back to main menu");
            System.out.println();
            System.out.print("  Select your option: ");
            
            String userChoice = scanner.nextLine().trim();
            
            switch (userChoice) {
                case "1":
                    searchNearestN();
                    break;
                case "2":
                    searchNearestNWithFilters();
                    break;
                case "3":
                    showTreeStatistics();
                    break;
                case "0":
                    System.out.println("\n  Returning to main menu...");
                    return;
                default:
                    System.out.println("\n  [ERROR] Invalid option! Please enter a number between 0 and 3.");
            }
        }
    }
    
    /**
     * Performs a nearest neighbor search.
     * Finds the N nearest stations to a target point.
     */
    private void searchNearestN() {
        System.out.println("\n" + StringUtils.repeat("=",70));
        System.out.println("                    FIND N NEAREST STATIONS");
        System.out.println(StringUtils.repeat("=",70));
        System.out.println();
        System.out.println("  Please enter the target point and number of stations to find:");
        System.out.println();
        
        try {
            double targetLat = readDouble("  Target latitude: ", -90, 90);
            double targetLon = readDouble("  Target longitude: ", -180, 180);
            
            System.out.println();
            System.out.print("  Number of nearest stations to find: ");
            String nStr = scanner.nextLine().trim();
            int n;
            try {
                n = Integer.parseInt(nStr);
                if (n <= 0) {
                    System.out.println("\n  [ERROR] Number must be positive!");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("\n  [ERROR] Invalid number format!");
                return;
            }
            
            System.out.println();
            System.out.println("  Searching for nearest stations...");
            
            List<KDTree.StationDistance> results = controller.findNearestStations(targetLat, targetLon, n);
            
            displayNearestResults(results, targetLat, targetLon, n, null);
            
        } catch (Exception e) {
            System.out.println("\n  [ERROR] An error occurred: " + e.getMessage());
        }
    }
    
    /**
     * Performs a nearest neighbor search with filters.
     */
    private void searchNearestNWithFilters() {
        System.out.println("\n" + StringUtils.repeat("=",70));
        System.out.println("                    FIND N NEAREST STATIONS WITH FILTERS");
        System.out.println(StringUtils.repeat("=",70));
        System.out.println();
        System.out.println("  Please enter the target point and number of stations to find:");
        System.out.println();
        
        try {
            double targetLat = readDouble("  Target latitude: ", -90, 90);
            double targetLon = readDouble("  Target longitude: ", -180, 180);
            
            System.out.println();
            System.out.print("  Number of nearest stations to find: ");
            String nStr = scanner.nextLine().trim();
            int n;
            try {
                n = Integer.parseInt(nStr);
                if (n <= 0) {
                    System.out.println("\n  [ERROR] Number must be positive!");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("\n  [ERROR] Invalid number format!");
                return;
            }
            
            System.out.println();
            SearchFilters filters = readFilters();
            
            System.out.println();
            System.out.println("  Searching for nearest stations...");
            
            List<KDTree.StationDistance> results = controller.findNearestStations(targetLat, targetLon, n, filters);
            
            displayNearestResults(results, targetLat, targetLon, n, filters);
            
        } catch (Exception e) {
            System.out.println("\n  [ERROR] An error occurred: " + e.getMessage());
        }
    }
    
    /**
     * Displays nearest neighbor search results.
     *
     * @param results list of station-distance pairs
     * @param targetLat target latitude
     * @param targetLon target longitude
     * @param n number of stations requested
     * @param filters filters applied (can be null)
     */
    private void displayNearestResults(List<KDTree.StationDistance> results, 
                                      double targetLat, double targetLon, int n, SearchFilters filters) {
        System.out.println();
        System.out.println("  " + StringUtils.repeat("=",68));
        System.out.println("                          SEARCH RESULTS");
        System.out.println("  " + StringUtils.repeat("=",68));
        System.out.println();
        
        System.out.println("  Target Point:");
        System.out.printf("     Latitude:  %.6f\n", targetLat);
        System.out.printf("     Longitude: %.6f\n", targetLon);
        System.out.println("     Requested: " + n + " nearest station(s)");
        
        if (filters != null && filters.hasFilters()) {
            System.out.println();
            System.out.println("  Applied Filters:");
            System.out.println("     " + formatFiltersForDisplay(filters));
        }
        
        System.out.println();
        System.out.println("  " + StringUtils.repeat("-",68));
        System.out.println();
        
        if (results.isEmpty()) {
            System.out.println("  No stations found.");
        } else {
            System.out.printf("  Found %d station(s):\n", results.size());
            System.out.println();
            
            for (int index = 0; index < results.size(); index++) {
                KDTree.StationDistance sd = results.get(index);
                System.out.printf("  %3d. %s\n", index + 1, formatStationDistance(sd));
            }
        }
        
        System.out.println();
        System.out.println("  " + StringUtils.repeat("=",68));
    }
    
    /**
     * Reads filter values from user input.
     * All filters are optional - user can press Enter to skip any filter.
     *
     * @return SearchFilters object with user selections
     */
    private SearchFilters readFilters() {
        System.out.println();
        System.out.println("  " + StringUtils.repeat("-",68));
        System.out.println("  OPTIONAL FILTERS (Press Enter to skip any filter)");
        System.out.println("  " + StringUtils.repeat("-",68));
        System.out.println();
        
        System.out.println("  Country Filter:");
        System.out.print("    Enter country code (PT for Portugal, ES for Spain, or 'all'): ");
        String countryInput = scanner.nextLine().trim();
        Country selectedCountry = null;
        
        if (!countryInput.isEmpty() && !countryInput.equalsIgnoreCase("all")) {
            selectedCountry = Country.fromString(countryInput);
            if (selectedCountry == null) {
                System.out.println("    [WARNING] Invalid country code. This filter will be ignored.");
            } else {
                System.out.println("    [OK] Filter set to: " + selectedCountry);
            }
        }
        
        System.out.println();
        System.out.println("  City Filter:");
        System.out.print("    Show only city stations? (true/false, or press Enter to skip): ");
        String cityInput = scanner.nextLine().trim();
        Boolean cityFilter = null;
        
        if (!cityInput.isEmpty()) {
            if (cityInput.equalsIgnoreCase("true")) {
                cityFilter = true;
                System.out.println("    [OK] Showing only city stations");
            } else if (cityInput.equalsIgnoreCase("false")) {
                cityFilter = false;
                System.out.println("    [OK] Excluding city stations");
            } else {
                System.out.println("    [WARNING] Invalid input. This filter will be ignored.");
            }
        }
        
        System.out.println();
        System.out.println("  Main Station Filter:");
        System.out.print("    Show only main stations? (true/false, or press Enter to skip): ");
        String mainStationInput = scanner.nextLine().trim();
        Boolean mainStationFilter = null;
        
        if (!mainStationInput.isEmpty()) {
            if (mainStationInput.equalsIgnoreCase("true")) {
                mainStationFilter = true;
                System.out.println("    [OK] Showing only main stations");
            } else if (mainStationInput.equalsIgnoreCase("false")) {
                mainStationFilter = false;
                System.out.println("    [OK] Excluding main stations");
            } else {
                System.out.println("    [WARNING] Invalid input. This filter will be ignored.");
            }
        }
        
        return new SearchFilters(cityFilter, mainStationFilter, selectedCountry);
    }
    
    /**
     * Formats filters for display in a user-friendly way.
     *
     * @param filters the filters to format
     * @return formatted string
     */
    private String formatFiltersForDisplay(SearchFilters filters) {
        if (filters == null || !filters.hasFilters()) {
            return "None";
        }
        
        StringBuilder formatted = new StringBuilder();
        boolean isFirst = true;
        
        if (filters.getCountry() != null) {
            formatted.append("Country: ").append(filters.getCountry());
            isFirst = false;
        }
        
        if (filters.getIsCity() != null) {
            if (!isFirst) formatted.append(" | ");
            formatted.append("City: ").append(filters.getIsCity() ? "Yes" : "No");
            isFirst = false;
        }
        
        if (filters.getIsMainStation() != null) {
            if (!isFirst) formatted.append(" | ");
            formatted.append("Main Station: ").append(filters.getIsMainStation() ? "Yes" : "No");
        }
        
        return formatted.toString();
    }
    
    /**
     * Formats a station-distance pair for display.
     *
     * @param sd station-distance pair
     * @return formatted string
     */
    private String formatStationDistance(KDTree.StationDistance sd) {
        Station station = sd.getStation();
        double distance = sd.getDistance();
        
        StringBuilder formatted = new StringBuilder();
        formatted.append(station.getName());
        formatted.append(String.format(" | [%.6f, %.6f]", station.getLatitude(), station.getLongitude()));
        formatted.append(String.format(" | Distance: %.2f km", distance));
        formatted.append(" | ").append(station.getCountry());
        
        if (station.isCity() || station.isMainStation()) {
            formatted.append(" | ");
            if (station.isCity()) {
                formatted.append("City");
            }
            if (station.isCity() && station.isMainStation()) {
                formatted.append(" + ");
            }
            if (station.isMainStation()) {
                formatted.append("Main Station");
            }
        }
        
        return formatted.toString();
    }
    
    /**
     * Shows KD-tree statistics in a user-friendly format.
     * Displays information about the tree structure and dataset size.
     */
    private void showTreeStatistics() {
        System.out.println("\n" + StringUtils.repeat("=",70));
        System.out.println("                    KD-TREE STATISTICS");
        System.out.println(StringUtils.repeat("=",70));
        System.out.println();
        
        String treeStats = controller.getTreeStatistics();
        int totalStations = controller.getStationsCount();
        
        System.out.println("  Tree Structure:");
        System.out.println("     " + treeStats);
        System.out.println();
        System.out.println("  Dataset Information:");
        System.out.println("     Total stations loaded: " + totalStations);
        System.out.println();
        System.out.println("  About KD-Trees:");
        System.out.println("     A KD-tree (2D-tree) is a spatial data structure that");
        System.out.println("     organizes stations by their geographical coordinates.");
        System.out.println("     This allows for efficient nearest neighbor search without");
        System.out.println("     scanning the entire dataset.");
        System.out.println();
        System.out.println(StringUtils.repeat("=",70));
    }
    
    /**
     * Reads a double value from user input with validation.
     * Keeps asking until a valid value is provided.
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
}

