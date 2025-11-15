package UI;

import Controller.GeographicalSearchController;
import Domain.Station;
import Domain.SearchFilters;
import Domain.ENUM.Country;

import java.util.List;
import java.util.Scanner;

/**
 * User interface for geographical search operations (USEI08).
 * Allows searching for railway stations within a geographical rectangle
 * with optional filters (isCity, isMainStation, country).
 * 
 * @author Development Team
 * @version 1.0
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
     * Shows the main menu for geographical search.
     * Provides a user-friendly interface for searching railway stations by geographical area.
     */
    public void showMainMenu() {
        while (true) {
            System.out.println("\n" + "=".repeat(70));
            System.out.println("          GEOGRAPHICAL SEARCH - SEARCH BY AREA (USEI08)");
            System.out.println("=".repeat(70));
            System.out.println();
            System.out.println("  What would you like to do?");
            System.out.println();
            System.out.println("  1. Search stations in a geographical area");
            System.out.println("  2. Search stations with filters (country, city, main station)");
            System.out.println("  3. View KD-Tree statistics");
            System.out.println("  4. View sample queries");
            System.out.println("  5. Back to main menu");
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
                case "5":
                    System.out.println("\n  Returning to main menu...");
                    return;
                default:
                    System.out.println("\n  [ERROR] Invalid option! Please enter a number between 1 and 5.");
                    pauseForUser();
            }
        }
    }
    
    /**
     * Performs a search by geographical area without filters.
     * Guides the user through entering latitude and longitude boundaries.
     */
    private void searchByArea() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("                    SEARCH BY GEOGRAPHICAL AREA");
        System.out.println("=".repeat(70));
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
                pauseForUser();
                return;
            }
            
            System.out.println();
            System.out.println("  Longitude Range:");
            double minimumLongitude = readDouble("    Minimum longitude: ", -180, 180);
            double maximumLongitude = readDouble("    Maximum longitude: ", -180, 180);
            
            if (minimumLongitude > maximumLongitude) {
                System.out.println("\n  [ERROR] Minimum longitude (" + minimumLongitude + 
                                 ") cannot be greater than maximum longitude (" + maximumLongitude + ")!");
                pauseForUser();
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
            pauseForUser();
        }
    }
    
    /**
     * Performs a search by geographical area with optional filters.
     * Allows users to refine their search by country, city status, or main station status.
     */
    private void searchWithFilters() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("              SEARCH WITH FILTERS");
        System.out.println("=".repeat(70));
        System.out.println();
        System.out.println("  First, define the geographical area:");
        System.out.println();
        
        try {
            System.out.println("  Latitude Range:");
            double minimumLatitude = readDouble("    Minimum latitude: ", -90, 90);
            double maximumLatitude = readDouble("    Maximum latitude: ", -90, 90);
            
            if (minimumLatitude > maximumLatitude) {
                System.out.println("\n  [ERROR] Invalid latitude range!");
                pauseForUser();
                return;
            }
            
            System.out.println();
            System.out.println("  Longitude Range:");
            double minimumLongitude = readDouble("    Minimum longitude: ", -180, 180);
            double maximumLongitude = readDouble("    Maximum longitude: ", -180, 180);
            
            if (minimumLongitude > maximumLongitude) {
                System.out.println("\n  [ERROR] Invalid longitude range!");
                pauseForUser();
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
            pauseForUser();
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
        System.out.println("  " + "-".repeat(68));
        System.out.println("  OPTIONAL FILTERS (Press Enter to skip any filter)");
        System.out.println("  " + "-".repeat(68));
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
        System.out.println("  " + "=".repeat(68));
        System.out.println("                          SEARCH RESULTS");
        System.out.println("  " + "=".repeat(68));
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
        System.out.println("  " + "-".repeat(68));
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
        System.out.println("  " + "=".repeat(68));
        pauseForUser();
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
        System.out.println("\n" + "=".repeat(70));
        System.out.println("                    KD-TREE STATISTICS");
        System.out.println("=".repeat(70));
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
        System.out.println("=".repeat(70));
        pauseForUser();
    }
    
    /**
     * Shows sample queries for demonstration.
     * Provides users with example queries they can try to understand the system.
     */
    private void showSampleQueries() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("                        SAMPLE QUERIES");
        System.out.println("=".repeat(70));
        System.out.println();
        System.out.println("  Here are some example queries you can try:");
        System.out.println();
        
        System.out.println("  " + "-".repeat(68));
        System.out.println("  1. Search all stations in Portugal");
        System.out.println("  " + "-".repeat(68));
        System.out.println("     Latitude:  [36.8, 42.2]");
        System.out.println("     Longitude: [-9.5, -6.2]");
        System.out.println("     Filter: Country = PT");
        System.out.println();
        
        System.out.println("  " + "-".repeat(68));
        System.out.println("  2. Search city stations in Spain");
        System.out.println("  " + "-".repeat(68));
        System.out.println("     Latitude:  [36.0, 43.8]");
        System.out.println("     Longitude: [-9.3, 4.3]");
        System.out.println("     Filters: Country = ES, City = Yes");
        System.out.println();
        
        System.out.println("  " + "-".repeat(68));
        System.out.println("  3. Search main stations in Iberian Peninsula");
        System.out.println("  " + "-".repeat(68));
        System.out.println("     Latitude:  [36.0, 43.8]");
        System.out.println("     Longitude: [-9.5, 4.3]");
        System.out.println("     Filter: Main Station = Yes");
        System.out.println();
        
        System.out.println("  " + "-".repeat(68));
        System.out.println("  4. Search city stations in Portugal");
        System.out.println("  " + "-".repeat(68));
        System.out.println("     Latitude:  [36.8, 42.2]");
        System.out.println("     Longitude: [-9.5, -6.2]");
        System.out.println("     Filters: Country = PT, City = Yes");
        System.out.println();
        
        System.out.println("  " + "-".repeat(68));
        System.out.println("  5. Search all stations in Lisbon area");
        System.out.println("  " + "-".repeat(68));
        System.out.println("     Latitude:  [38.6, 38.9]");
        System.out.println("     Longitude: [-9.3, -9.0]");
        System.out.println("     Filters: None (shows all stations in the area)");
        System.out.println();
        
        System.out.println("  Tip: You can copy these values when using the search options!");
        System.out.println();
        System.out.println("=".repeat(70));
        pauseForUser();
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
     * Pauses execution and waits for user to press Enter.
     * Provides a consistent way to pause between operations.
     */
    private void pauseForUser() {
        System.out.print("  Press Enter to continue...");
        scanner.nextLine();
    }
}

