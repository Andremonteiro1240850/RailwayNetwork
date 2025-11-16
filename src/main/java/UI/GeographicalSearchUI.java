package UI;

import Controller.GeographicalSearchController;
import Domain.Station;
import Domain.SearchFilters;
import Domain.KDTree;
import Domain.ENUM.Country;

import java.util.List;
import java.util.Scanner;

/**
 * User interface for geographical search operations.
 * Allows searching for railway stations within a geographical rectangle,
 * finding nearest neighbors, and radius search with optional filters (isCity, isMainStation, country).
 * 
 * @author Development Team
 * @version 2.0
 */
public class GeographicalSearchUI {
    
    private final GeographicalSearchController controller;
    private final Scanner scanner;
    
    /**
     * Constructs a new GeographicalSearchUI with the specified controller.
     *
     * @param controller the geographical search controller
     */
    public GeographicalSearchUI(GeographicalSearchController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }
    
    /**
     * Shows the main menu for geographical search (legacy method, kept for compatibility).
     * New menus should use showAreaSearchMenu(), showNearestNSearchMenu(), or showRadiusSearchMenu().
     */
    public void showMainMenu() {
        while (true) {
            System.out.println("\n" + StringUtils.repeat("=", 70));
            System.out.println("          GEOGRAPHICAL SEARCH");
            System.out.println(StringUtils.repeat("=", 70));
            System.out.println();
            System.out.println("  What would you like to do?");
            System.out.println();
            System.out.println("  1. Search stations in a geographical area");
            System.out.println("  2. Search stations with filters");
            System.out.println("  3. Find N nearest stations to a point");
            System.out.println("  4. Find stations within radius");
            System.out.println("  5. View KD-Tree statistics");
            System.out.println("  6. View sample queries");
            System.out.println("  7. Back to main menu");
            System.out.println();
            System.out.print("  Select your option: ");
            
            String userChoice = scanner.nextLine().trim();
            
            switch (userChoice) {
                case "1":
                    searchByArea();
                    break;
                case "2":
                    searchWithFilters();
                    break;
                case "3":
                    searchNearestN();
                    break;
                case "4":
                    searchRadius();
                    break;
                case "5":
                    showTreeStatistics();
                    break;
                case "6":
                    showSampleQueries();
                    break;
                case "7":
                    System.out.println("\n  Returning to main menu...");
                    return;
                default:
                    System.out.println("\n  [ERROR] Invalid option! Please enter a number between 1 and 7.");
            }
        }
    }
    
    /**
     * Performs a search by geographical area without filters.
     * Guides the user through entering latitude and longitude boundaries.
     */
    private void searchByArea() {
        System.out.println("\n" + StringUtils.repeat("=",70));
        System.out.println("                    SEARCH BY GEOGRAPHICAL AREA");
        System.out.println(StringUtils.repeat("=",70));
        System.out.println();
        System.out.println("  Please enter the geographical boundaries for your search:");
        System.out.println("  (Latitude: -90 to 90, Longitude: -180 to 180)");
        System.out.println();
        
        try {
            System.out.println("  Latitude Range:");
            double minimumLatitude = readDouble("    Minimum latitude: ", -90, 90);
            double maximumLatitude = readDouble("    Maximum latitude: ", -90, 90);
            
            if (minimumLatitude > maximumLatitude) {
                System.out.println("\n  [ERROR] Minimum latitude (" + minimumLatitude + 
                                 ") cannot be greater than maximum latitude (" + maximumLatitude + ")!");
                return;
            }
            
            System.out.println();
            System.out.println("  Longitude Range:");
            double minimumLongitude = readDouble("    Minimum longitude: ", -180, 180);
            double maximumLongitude = readDouble("    Maximum longitude: ", -180, 180);
            
            if (minimumLongitude > maximumLongitude) {
                System.out.println("\n  [ERROR] Minimum longitude (" + minimumLongitude + 
                                 ") cannot be greater than maximum longitude (" + maximumLongitude + ")!");
                return;
            }
            
            System.out.println();
            System.out.println("  Searching for stations in the specified area...");
            System.out.println("  (This may take a moment depending on the dataset size)");
            
            List<Station> searchResults = controller.searchByGeographicalArea(
                minimumLatitude, maximumLatitude, minimumLongitude, maximumLongitude);
            
            displayResults(searchResults, minimumLatitude, maximumLatitude, 
                          minimumLongitude, maximumLongitude, null);
            
        } catch (Exception e) {
            System.out.println("\n  [ERROR] An error occurred: " + e.getMessage());
            System.out.println("  Please try again with valid coordinates.");
        }
    }
    
    /**
     * Performs a search by geographical area with optional filters.
     * Allows users to refine their search by country, city status, or main station status.
     */
    private void searchWithFilters() {
        System.out.println("\n" + StringUtils.repeat("=",70));
        System.out.println("              SEARCH WITH FILTERS");
        System.out.println(StringUtils.repeat("=",70));
        System.out.println();
        System.out.println("  First, define the geographical area:");
        System.out.println();
        
        try {
            System.out.println("  Latitude Range:");
            double minimumLatitude = readDouble("    Minimum latitude: ", -90, 90);
            double maximumLatitude = readDouble("    Maximum latitude: ", -90, 90);
            
            if (minimumLatitude > maximumLatitude) {
                System.out.println("\n  [ERROR] Invalid latitude range!");
                return;
            }
            
            System.out.println();
            System.out.println("  Longitude Range:");
            double minimumLongitude = readDouble("    Minimum longitude: ", -180, 180);
            double maximumLongitude = readDouble("    Maximum longitude: ", -180, 180);
            
            if (minimumLongitude > maximumLongitude) {
                System.out.println("\n  [ERROR] Invalid longitude range!");
                return;
            }
            
            SearchFilters userFilters = readFilters();
            
            System.out.println();
            System.out.println("  Searching with your specified filters...");
            
            List<Station> searchResults = controller.searchByGeographicalArea(
                minimumLatitude, maximumLatitude, minimumLongitude, maximumLongitude, userFilters);
            
            displayResults(searchResults, minimumLatitude, maximumLatitude, 
                          minimumLongitude, maximumLongitude, userFilters);
            
        } catch (Exception e) {
            System.out.println("\n  [ERROR] An error occurred: " + e.getMessage());
            System.out.println("  Please try again with valid input.");
        }
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
     * Displays search results in a user-friendly format.
     *
     * @param results list of stations found
     * @param latMin minimum latitude
     * @param latMax maximum latitude
     * @param lonMin minimum longitude
     * @param lonMax maximum longitude
     * @param filters filters applied (can be null)
     */
    private void displayResults(List<Station> results, double latMin, double latMax, 
                               double lonMin, double lonMax, SearchFilters filters) {
        System.out.println();
        System.out.println("  " + StringUtils.repeat("=",68));
        System.out.println("                          SEARCH RESULTS");
        System.out.println("  " + StringUtils.repeat("=",68));
        System.out.println();
        
        System.out.println("  Search Region:");
        System.out.printf("     Latitude:  [%.6f, %.6f]\n", latMin, latMax);
        System.out.printf("     Longitude: [%.6f, %.6f]\n", lonMin, lonMax);
        
        if (filters != null && filters.hasFilters()) {
            System.out.println();
            System.out.println("  Applied Filters:");
            System.out.println("     " + formatFiltersForDisplay(filters));
        }
        
        System.out.println();
        System.out.println("  " + StringUtils.repeat("-",68));
        System.out.println();
        
        int numberOfResults = results.size();
        if (numberOfResults == 0) {
            System.out.println("  No stations found in the specified region.");
            System.out.println();
            System.out.println("  Tip: Try expanding your search area or removing filters.");
        } else {
            System.out.printf("  Found %d station(s):\n", numberOfResults);
            System.out.println();
            
            for (int index = 0; index < results.size(); index++) {
                Station currentStation = results.get(index);
                System.out.printf("  %3d. %s\n", index + 1, formatStation(currentStation));
            }
        }
        
        System.out.println();
        System.out.println("  " + StringUtils.repeat("=",68));
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
     * Formats a station for display in a user-friendly way.
     *
     * @param station the station to format
     * @return formatted string with station details
     */
    private String formatStation(Station station) {
        StringBuilder formatted = new StringBuilder();
        
        formatted.append(station.getName());
        
        formatted.append(String.format(" | [%.6f, %.6f]", 
            station.getLatitude(), station.getLongitude()));
        
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
        System.out.println("     This allows for efficient range queries without scanning");
        System.out.println("     the entire dataset.");
        System.out.println();
        System.out.println(StringUtils.repeat("=",70));
    }
    
    /**
     * Shows sample queries for demonstration.
     * Provides users with example queries they can try to understand the system.
     */
    private void showSampleQueries() {
        System.out.println("\n" + StringUtils.repeat("=",70));
        System.out.println("                        SAMPLE QUERIES");
        System.out.println(StringUtils.repeat("=",70));
        System.out.println();
        System.out.println("  Here are some example queries you can try:");
        System.out.println();
        
        System.out.println("  " + StringUtils.repeat("-",68));
        System.out.println("  1. Search all stations in Portugal");
        System.out.println("  " + StringUtils.repeat("-",68));
        System.out.println("     Latitude:  [36.8, 42.2]");
        System.out.println("     Longitude: [-9.5, -6.2]");
        System.out.println("     Filter: Country = PT");
        System.out.println();
        
        System.out.println("  " + StringUtils.repeat("-",68));
        System.out.println("  2. Search city stations in Spain");
        System.out.println("  " + StringUtils.repeat("-",68));
        System.out.println("     Latitude:  [36.0, 43.8]");
        System.out.println("     Longitude: [-9.3, 4.3]");
        System.out.println("     Filters: Country = ES, City = Yes");
        System.out.println();
        
        System.out.println("  " + StringUtils.repeat("-",68));
        System.out.println("  3. Search main stations in Iberian Peninsula");
        System.out.println("  " + StringUtils.repeat("-",68));
        System.out.println("     Latitude:  [36.0, 43.8]");
        System.out.println("     Longitude: [-9.5, 4.3]");
        System.out.println("     Filter: Main Station = Yes");
        System.out.println();
        
        System.out.println("  " + StringUtils.repeat("-",68));
        System.out.println("  4. Search city stations in Portugal");
        System.out.println("  " + StringUtils.repeat("-",68));
        System.out.println("     Latitude:  [36.8, 42.2]");
        System.out.println("     Longitude: [-9.5, -6.2]");
        System.out.println("     Filters: Country = PT, City = Yes");
        System.out.println();
        
        System.out.println("  " + StringUtils.repeat("-",68));
        System.out.println("  5. Search all stations in Lisbon area");
        System.out.println("  " + StringUtils.repeat("-",68));
        System.out.println("     Latitude:  [38.6, 38.9]");
        System.out.println("     Longitude: [-9.3, -9.0]");
        System.out.println("     Filters: None (shows all stations in the area)");
        System.out.println();
        
        System.out.println("  Tip: You can copy these values when using the search options!");
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
     * Performs a radius search.
     * Finds all stations within a specified radius of a target point.
     */
    private void searchRadius() {
        System.out.println("\n" + StringUtils.repeat("=",70));
        System.out.println("                    FIND STATIONS IN RADIUS");
        System.out.println(StringUtils.repeat("=",70));
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
            
            List<KDTree.StationDistance> results = controller.findStationsInRadius(targetLat, targetLon, radiusKm);
            
            displayRadiusResults(results, targetLat, targetLon, radiusKm, null);
            
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
     * Displays radius search results.
     *
     * @param results list of station-distance pairs
     * @param targetLat target latitude
     * @param targetLon target longitude
     * @param radiusKm search radius
     * @param filters filters applied (can be null)
     */
    private void displayRadiusResults(List<KDTree.StationDistance> results, 
                                     double targetLat, double targetLon, double radiusKm, SearchFilters filters) {
        System.out.println();
        System.out.println("  " + StringUtils.repeat("=",68));
        System.out.println("                          SEARCH RESULTS");
        System.out.println("  " + StringUtils.repeat("=",68));
        System.out.println();
        
        System.out.println("  Target Point:");
        System.out.printf("     Latitude:  %.6f\n", targetLat);
        System.out.printf("     Longitude: %.6f\n", targetLon);
        System.out.printf("     Radius:    %.2f km\n", radiusKm);
        
        if (filters != null && filters.hasFilters()) {
            System.out.println();
            System.out.println("  Applied Filters:");
            System.out.println("     " + formatFiltersForDisplay(filters));
        }
        
        System.out.println();
        System.out.println("  " + StringUtils.repeat("-",68));
        System.out.println();
        
        if (results.isEmpty()) {
            System.out.println("  No stations found within the specified radius.");
        } else {
            System.out.printf("  Found %d station(s) within %.2f km:\n", results.size(), radiusKm);
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
     * Shows menu for Area Search only.
     */
    public void showAreaSearchMenu() {
        while (true) {
            System.out.println("\n" + StringUtils.repeat("=",70));
            System.out.println("                    AREA SEARCH");
            System.out.println(StringUtils.repeat("=",70));
            System.out.println();
            System.out.println("  1. Search stations in a geographical area");
            System.out.println("  2. Search stations with filters (country, city, main station)");
            System.out.println("  3. View KD-Tree statistics");
            System.out.println("  4. View sample queries");
            System.out.println("  0. Back to main menu");
            System.out.println();
            System.out.print("  Select your option: ");
            
            String userChoice = scanner.nextLine().trim();
            
            switch (userChoice) {
                case "1":
                    searchByArea();
                    break;
                case "2":
                    searchWithFilters();
                    break;
                case "3":
                    showTreeStatistics();
                    break;
                case "4":
                    showSampleQueries();
                    break;
                case "0":
                    System.out.println("\n  Returning to main menu...");
                    return;
                default:
                    System.out.println("\n  [ERROR] Invalid option! Please enter a number between 0 and 4.");
            }
        }
    }
    
    /**
     * Shows menu for Nearest-N Search only.
     */
    public void showNearestNSearchMenu() {
        while (true) {
            System.out.println("\n" + StringUtils.repeat("=",70));
            System.out.println("                    NEAREST-N SEARCH");
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
     * Shows menu for Radius Search & Summary.
     */
    public void showRadiusSearchMenu() {
        while (true) {
            System.out.println("\n" + StringUtils.repeat("=",70));
            System.out.println("                    RADIUS SEARCH & SUMMARY");
            System.out.println(StringUtils.repeat("=",70));
            System.out.println();
            System.out.println("  1. Find stations within radius");
            System.out.println("  2. Find stations within radius with filters");
            System.out.println("  3. View search summary and statistics");
            System.out.println("  4. View KD-Tree statistics");
            System.out.println("  0. Back to main menu");
            System.out.println();
            System.out.print("  Select your option: ");
            
            String userChoice = scanner.nextLine().trim();
            
            switch (userChoice) {
                case "1":
                    searchRadius();
                    break;
                case "2":
                    searchRadiusWithFilters();
                    break;
                case "3":
                    showSearchSummary();
                    break;
                case "4":
                    showTreeStatistics();
                    break;
                case "0":
                    System.out.println("\n  Returning to main menu...");
                    return;
                default:
                    System.out.println("\n  [ERROR] Invalid option! Please enter a number between 0 and 4.");
            }
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
     * Performs a radius search with filters.
     */
    private void searchRadiusWithFilters() {
        System.out.println("\n" + StringUtils.repeat("=",70));
        System.out.println("                    FIND STATIONS IN RADIUS WITH FILTERS");
        System.out.println(StringUtils.repeat("=",70));
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
            SearchFilters filters = readFilters();
            
            System.out.println();
            System.out.println("  Searching for stations within radius...");
            
            List<KDTree.StationDistance> results = controller.findStationsInRadius(targetLat, targetLon, radiusKm, filters);
            
            displayRadiusResults(results, targetLat, targetLon, radiusKm, filters);
            
        } catch (Exception e) {
            System.out.println("\n  [ERROR] An error occurred: " + e.getMessage());
        }
    }
    
    /**
     * Shows search summary and statistics.
     */
    private void showSearchSummary() {
        System.out.println("\n" + StringUtils.repeat("=",70));
        System.out.println("                    SEARCH SUMMARY & STATISTICS");
        System.out.println(StringUtils.repeat("=",70));
        System.out.println();
        
        String treeStats = controller.getTreeStatistics();
        int totalStations = controller.getStationsCount();
        
        System.out.println("  KD-Tree Structure:");
        System.out.println("     " + treeStats);
        System.out.println();
        System.out.println("  Dataset Information:");
        System.out.println("     Total stations loaded: " + totalStations);
        System.out.println();
        System.out.println("  Available Search Operations:");
        System.out.println("     • Area Search: Search stations within a geographical rectangle");
        System.out.println("     • Nearest-N Search: Find the N nearest stations to a point");
        System.out.println("     • Radius Search: Find all stations within a specified radius");
        System.out.println();
        System.out.println("  All search operations support optional filters:");
        System.out.println("     • Country filter (PT, ES)");
        System.out.println("     • City status filter");
        System.out.println("     • Main station status filter");
        System.out.println();
        System.out.println(StringUtils.repeat("=",70));
    }
    
}

