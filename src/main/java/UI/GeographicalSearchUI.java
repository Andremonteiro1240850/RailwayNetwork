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
     */
    public void showMainMenu() {
        while (true) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("Geographical Search - Search by Area (USEI08)");
            System.out.println("=".repeat(60));
            System.out.println("1. Search by Geographical Area");
            System.out.println("2. Search with Filters");
            System.out.println("3. View Tree Statistics");
            System.out.println("4. Sample Queries");
            System.out.println("5. Back to Main Menu");
            
            System.out.print("\nSelect option: ");
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
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
                    return;
                default:
                    System.out.println("❌ Invalid option! Please try again.");
            }
        }
    }
    
    /**
     * Performs a search by geographical area without filters.
     */
    private void searchByArea() {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("Search by Geographical Area");
        System.out.println("-".repeat(60));
        
        try {
            double latMin = readDouble("Enter minimum latitude [-90 to 90]: ", -90, 90);
            double latMax = readDouble("Enter maximum latitude [-90 to 90]: ", -90, 90);
            double lonMin = readDouble("Enter minimum longitude [-180 to 180]: ", -180, 180);
            double lonMax = readDouble("Enter maximum longitude [-180 to 180]: ", -180, 180);
            
            if (latMin > latMax) {
                System.out.println("❌ Error: Minimum latitude cannot be greater than maximum latitude!");
                return;
            }
            
            if (lonMin > lonMax) {
                System.out.println("❌ Error: Minimum longitude cannot be greater than maximum longitude!");
                return;
            }
            
            System.out.println("\n🔍 Searching...");
            List<Station> results = controller.searchByGeographicalArea(latMin, latMax, lonMin, lonMax);
            
            displayResults(results, latMin, latMax, lonMin, lonMax, null);
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
    
    /**
     * Performs a search by geographical area with filters.
     */
    private void searchWithFilters() {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("Search by Geographical Area with Filters");
        System.out.println("-".repeat(60));
        
        try {
            double latMin = readDouble("Enter minimum latitude [-90 to 90]: ", -90, 90);
            double latMax = readDouble("Enter maximum latitude [-90 to 90]: ", -90, 90);
            double lonMin = readDouble("Enter minimum longitude [-180 to 180]: ", -180, 180);
            double lonMax = readDouble("Enter maximum longitude [-180 to 180]: ", -180, 180);
            
            if (latMin > latMax || lonMin > lonMax) {
                System.out.println("❌ Error: Invalid range!");
                return;
            }
            
            // Get filters
            SearchFilters filters = readFilters();
            
            System.out.println("\n🔍 Searching with filters...");
            List<Station> results = controller.searchByGeographicalArea(latMin, latMax, lonMin, lonMax, filters);
            
            displayResults(results, latMin, latMax, lonMin, lonMax, filters);
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
    
    /**
     * Reads filter values from user input.
     *
     * @return SearchFilters object with user selections
     */
    private SearchFilters readFilters() {
        System.out.println("\n--- Optional Filters (press Enter to skip) ---");
        
        // Country filter
        System.out.print("Country filter (PT/ES/all): ");
        String countryStr = scanner.nextLine().trim();
        Country country = null;
        if (!countryStr.isEmpty() && !countryStr.equalsIgnoreCase("all")) {
            country = Country.fromString(countryStr);
            if (country == null) {
                System.out.println("⚠️  Invalid country, ignoring filter.");
            }
        }
        
        // isCity filter
        Boolean isCity = null;
        System.out.print("Filter by city? (true/false/skip): ");
        String cityStr = scanner.nextLine().trim();
        if (!cityStr.isEmpty()) {
            if (cityStr.equalsIgnoreCase("true")) {
                isCity = true;
            } else if (cityStr.equalsIgnoreCase("false")) {
                isCity = false;
            }
        }
        
        // isMainStation filter
        Boolean isMainStation = null;
        System.out.print("Filter by main station? (true/false/skip): ");
        String mainStr = scanner.nextLine().trim();
        if (!mainStr.isEmpty()) {
            if (mainStr.equalsIgnoreCase("true")) {
                isMainStation = true;
            } else if (mainStr.equalsIgnoreCase("false")) {
                isMainStation = false;
            }
        }
        
        return new SearchFilters(isCity, isMainStation, country);
    }
    
    /**
     * Displays search results.
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
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Search Results");
        System.out.println("=".repeat(60));
        System.out.printf("Region: lat ∈ [%.6f, %.6f], lon ∈ [%.6f, %.6f]\n", 
                         latMin, latMax, lonMin, lonMax);
        
        if (filters != null && filters.hasFilters()) {
            System.out.println("Filters: " + filters);
        }
        
        System.out.println("\nFound " + results.size() + " station(s):\n");
        
        if (results.isEmpty()) {
            System.out.println("No stations found in the specified region.");
        } else {
            for (int i = 0; i < results.size(); i++) {
                Station station = results.get(i);
                System.out.printf("%3d. %s\n", i + 1, formatStation(station));
            }
        }
        
        System.out.println("\n" + "-".repeat(60));
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Formats a station for display.
     *
     * @param station the station to format
     * @return formatted string
     */
    private String formatStation(Station station) {
        StringBuilder sb = new StringBuilder();
        sb.append(station.getName());
        sb.append(String.format(" - [%.6f, %.6f]", station.getLatitude(), station.getLongitude()));
        sb.append(" - ").append(station.getCountry());
        
        if (station.isCity()) {
            sb.append(" [City]");
        }
        
        if (station.isMainStation()) {
            sb.append(" [Main]");
        }
        
        return sb.toString();
    }
    
    /**
     * Shows tree statistics.
     */
    private void showTreeStatistics() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("KD-Tree Statistics");
        System.out.println("=".repeat(60));
        System.out.println(controller.getTreeStatistics());
        System.out.println("Total stations: " + controller.getStationsCount());
        System.out.println("\n" + "-".repeat(60));
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Shows sample queries for demonstration.
     */
    private void showSampleQueries() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Sample Queries");
        System.out.println("=".repeat(60));
        
        System.out.println("\nThese are example queries you can try:\n");
        
        // Sample query 1: Portugal region
        System.out.println("1. Search in Portugal region:");
        System.out.println("   Latitude: [36.8, 42.2]");
        System.out.println("   Longitude: [-9.5, -6.2]");
        System.out.println("   Filters: Country=PT");
        
        // Sample query 2: Spain region
        System.out.println("\n2. Search in Spain region:");
        System.out.println("   Latitude: [36.0, 43.8]");
        System.out.println("   Longitude: [-9.3, 4.3]");
        System.out.println("   Filters: Country=ES, isCity=true");
        
        // Sample query 3: Main stations only
        System.out.println("\n3. Search main stations in Iberian Peninsula:");
        System.out.println("   Latitude: [36.0, 43.8]");
        System.out.println("   Longitude: [-9.5, 4.3]");
        System.out.println("   Filters: isMainStation=true");
        
        // Sample query 4: City stations
        System.out.println("\n4. Search city stations in Portugal:");
        System.out.println("   Latitude: [36.8, 42.2]");
        System.out.println("   Longitude: [-9.5, -6.2]");
        System.out.println("   Filters: Country=PT, isCity=true");
        
        // Sample query 5: Small region
        System.out.println("\n5. Search in Lisbon area:");
        System.out.println("   Latitude: [38.6, 38.9]");
        System.out.println("   Longitude: [-9.3, -9.0]");
        System.out.println("   Filters: None");
        
        System.out.println("\n" + "-".repeat(60));
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
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
            String input = scanner.nextLine().trim();
            
            try {
                double value = Double.parseDouble(input);
                if (value < min || value > max) {
                    System.out.printf("❌ Value must be between %.1f and %.1f. Please try again.\n", min, max);
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid number format. Please try again.");
            }
        }
    }
}

