package UI;

import Domain.*;
import java.util.*;

/**
 * User interface for calculating estimated travel time between railway stations.
 * This UI implements the CLI interaction for USLP03 - Calculate Estimated Travel Time.
 * 
 * @author Development Team
 * @version 1.0
 */
public class TravelTimeUI {
    
    private final TravelCalculationService service;
    private final Scanner scanner;
    private final List<Station> stations;
    private final List<Locomotive> locomotives;
    
    /**
     * Constructs a new TravelTimeUI with the specified service and mock data.
     * 
     * @param service the travel calculation service
     */
    public TravelTimeUI(TravelCalculationService service) {
        this.service = service;
        this.scanner = new Scanner(System.in);
        this.stations = createMockStations();
        this.locomotives = createMockLocomotives();
    }
    
    /**
     * Shows the main menu for travel time calculation.
     */
    public void showMainMenu() {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("Calculate Estimated Travel Time");
            System.out.println("=".repeat(50));
            System.out.println("1. Calculate Travel Time");
            System.out.println("2. View Available Stations");
            System.out.println("3. View Available Locomotives");
            System.out.println("4. Back to Main Menu");
            
            System.out.print("Select option: ");
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    calculateTravelTime();
                    break;
                case "2":
                    showAvailableStations();
                    break;
                case "3":
                    showAvailableLocomotives();
                    break;
                case "4":
                    return;
                default:
                    System.out.println("❌ Invalid option! Please try again.");
            }
        }
    }
    
    /**
     * Handles the travel time calculation process.
     */
    private void calculateTravelTime() {
        System.out.println("\n" + "-".repeat(50));
        System.out.println("Travel Time Calculation");
        System.out.println("-".repeat(50));
        
        try {
            // Get start station
            Station startStation = selectStation("Enter Start Station ID");
            if (startStation == null) return;
            
            // Get end station
            Station endStation = selectStation("Enter End Station ID");
            if (endStation == null) return;
            
            // Check if stations are the same
            if (startStation.equals(endStation)) {
                System.out.println("❌ Start and end stations cannot be the same!");
                return;
            }
            
            // Get locomotive
            Locomotive locomotive = selectLocomotive();
            if (locomotive == null) return;
            
            // Calculate travel time
            double travelTimeHours = service.calculateTravelTime(startStation, endStation, locomotive);
            
            // Display results
            displayResults(startStation, endStation, locomotive, travelTimeHours);
            
        } catch (IncompatibleGaugeException e) {
            System.out.println("❌ " + e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println("❌ " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ An unexpected error occurred: " + e.getMessage());
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Allows user to select a station by ID.
     * 
     * @param prompt the prompt message
     * @return the selected station or null if cancelled
     */
    private Station selectStation(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            String stationId = scanner.nextLine().trim();
            
            if (stationId.isEmpty()) {
                System.out.println("❌ Station ID cannot be empty!");
                continue;
            }
            
            Station station = findStationById(stationId);
            if (station != null) {
                return station;
            } else {
                System.out.println("❌ Station with ID '" + stationId + "' not found!");
                System.out.print("Do you want to try again? (y/n): ");
                String retry = scanner.nextLine().trim().toLowerCase();
                if (!retry.equals("y") && !retry.equals("yes")) {
                    return null;
                }
            }
        }
    }
    
    /**
     * Allows user to select a locomotive by ID.
     * 
     * @return the selected locomotive or null if cancelled
     */
    private Locomotive selectLocomotive() {
        while (true) {
            System.out.print("Enter Locomotive ID: ");
            String locomotiveId = scanner.nextLine().trim();
            
            if (locomotiveId.isEmpty()) {
                System.out.println("❌ Locomotive ID cannot be empty!");
                continue;
            }
            
            Locomotive locomotive = findLocomotiveById(locomotiveId);
            if (locomotive != null) {
                return locomotive;
            } else {
                System.out.println("❌ Locomotive with ID '" + locomotiveId + "' not found!");
                System.out.print("Do you want to try again? (y/n): ");
                String retry = scanner.nextLine().trim().toLowerCase();
                if (!retry.equals("y") && !retry.equals("yes")) {
                    return null;
                }
            }
        }
    }
    
    /**
     * Displays the calculation results in a formatted manner.
     * 
     * @param startStation the starting station
     * @param endStation the ending station
     * @param locomotive the locomotive used
     * @param travelTimeHours the travel time in hours
     */
    private void displayResults(Station startStation, Station endStation, 
                              Locomotive locomotive, double travelTimeHours) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("RESULT:");
        System.out.println("=".repeat(50));
        
        System.out.printf("From \"%s\" to \"%s\"%n", 
                         startStation.getName(), endStation.getName());
        System.out.printf("Using locomotive \"%s\" (%s, %.0f km/h)%n", 
                         locomotive.getId(), locomotive.getType(), locomotive.getMaxSpeedKmH());
        
        String formattedTime = TravelCalculationService.formatTravelTime(travelTimeHours);
        System.out.printf("Estimated Travel Time: %s%n", formattedTime);
        
        // Additional information
        LineSegment segment = service.findConnectingSegment(startStation, endStation);
        if (segment != null) {
            System.out.printf("Line Segment: %s (%.2f km)%n", 
                             segment.getId(), segment.getLengthKm());
            System.out.printf("Track Gauge: %.0fmm%n", segment.getGauge());
            System.out.printf("Electrified: %s%n", segment.isElectrified() ? "Yes" : "No");
        }
    }
    
    /**
     * Shows all available stations.
     */
    private void showAvailableStations() {
        System.out.println("\n" + "-".repeat(50));
        System.out.println("Available Stations");
        System.out.println("-".repeat(50));
        
        for (Station station : stations) {
            System.out.printf("ID: %s | Name: %s", station.getId(), station.getName());
            if (station.hasLocation()) {
                System.out.printf(" | Location: %s", station.getLocation());
            }
            System.out.println();
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Shows all available locomotives.
     */
    private void showAvailableLocomotives() {
        System.out.println("\n" + "-".repeat(50));
        System.out.println("Available Locomotives");
        System.out.println("-".repeat(50));
        
        for (Locomotive locomotive : locomotives) {
            System.out.printf("ID: %s | Type: %s | Max Speed: %.0f km/h | Gauge: %.0fmm%n",
                             locomotive.getId(), locomotive.getType(), 
                             locomotive.getMaxSpeedKmH(), locomotive.getGauge());
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Finds a station by its ID.
     * 
     * @param id the station ID
     * @return the station or null if not found
     */
    private Station findStationById(String id) {
        return stations.stream()
                .filter(station -> station.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Finds a locomotive by its ID.
     * 
     * @param id the locomotive ID
     * @return the locomotive or null if not found
     */
    private Locomotive findLocomotiveById(String id) {
        return locomotives.stream()
                .filter(locomotive -> locomotive.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Creates mock station data for testing and demonstration.
     * 
     * @return list of mock stations
     */
    private List<Station> createMockStations() {
        return Arrays.asList(
            new Station("ST001", "Lisbon Terminal", "Lisbon"),
            new Station("ST002", "Porto Cargo Hub", "Porto"),
            new Station("ST003", "Coimbra Station", "Coimbra"),
            new Station("ST004", "Aveiro Central", "Aveiro"),
            new Station("ST005", "Braga Station", "Braga"),
            new Station("ST006", "Faro Terminal", "Faro"),
            new Station("ST007", "Évora Station", "Évora"),
            new Station("ST008", "Leiria Hub", "Leiria")
        );
    }
    
    /**
     * Creates mock locomotive data for testing and demonstration.
     * 
     * @return list of mock locomotives
     */
    private List<Locomotive> createMockLocomotives() {
        return Arrays.asList(
            new Locomotive("L001", "electric", 120.0, 1435.0), // Standard gauge electric
            new Locomotive("L002", "diesel", 100.0, 1435.0),    // Standard gauge diesel
            new Locomotive("L003", "electric", 160.0, 1435.0), // High-speed electric
            new Locomotive("L004", "diesel", 80.0, 1435.0),    // Freight diesel
            new Locomotive("L005", "electric", 100.0, 1000.0), // Narrow gauge electric
            new Locomotive("L006", "diesel", 60.0, 1000.0),    // Narrow gauge diesel
            new Locomotive("L007", "electric", 200.0, 1435.0), // Very high-speed electric
            new Locomotive("L008", "diesel", 90.0, 1520.0)     // Russian gauge diesel
        );
    }
}
