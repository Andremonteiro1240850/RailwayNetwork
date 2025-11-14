package UI;

import Domain.*;
import Controller.DispatchController;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * User interface for train dispatch scheduling.
 * This UI implements the CLI interaction for USPL07 - Train Dispatch Scheduler.
 *
 * @author Development Team
 * @version 1.0
 */
public class SchedulerUI {
    
    private final DispatchController controller;
    private final Scanner scanner;
    private final List<Facility> facilities;
    private final List<Locomotive> locomotives;

    /**
     * Constructs a new SchedulerUI with the specified controller.
     *
     * @param controller the dispatch controller
     */
    public SchedulerUI(DispatchController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
        this.facilities = controller.getStations();
        this.locomotives = createMockLocomotives();
    }

    /**
     * Shows the main menu for train dispatch scheduling.
     */
    public void showMainMenu() {
        while (true) {
            System.out.println("\n" + StringUtils.repeat("=", 60));
            System.out.println("Train Dispatch Scheduler (USPL07)");
            System.out.println(StringUtils.repeat("=", 60));
            System.out.println("1. Dispatch Trains");
            System.out.println("2. View Available Stations");
            System.out.println("3. View Available Locomotives");
            System.out.println("4. Back to Main Menu");
            
            System.out.print("Select option: ");
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    dispatchTrains();
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
     * Handles the train dispatch process.
     */
    private void dispatchTrains() {
        System.out.println("\n" + StringUtils.repeat("-", 60));
        System.out.println("Train Dispatch");
        System.out.println(StringUtils.repeat("-", 60));
        
        try {
            List<Train> trains = collectTrains();
            if (trains.isEmpty()) {
                System.out.println("No trains to dispatch.");
                return;
            }

            System.out.println("\n" + StringUtils.repeat("=", 60));
            System.out.println("DISPATCHING TRAINS...");
            System.out.println(StringUtils.repeat("=", 60));

            // Schedule trains using the controller
            DispatchReport report = controller.scheduleTrains(trains);

            // Display results
            displayDispatchReport(report);

        } catch (Exception e) {
            System.out.println("❌ An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Collects train information from the user.
     *
     * @return the list of trains to dispatch
     */
    private List<Train> collectTrains() {
        List<Train> trains = new ArrayList<>();
        
        System.out.println("Enter train information. Press Enter with empty train ID to finish.");
        
        while (true) {
            System.out.println("\n" + StringUtils.repeat("-", 40));
            System.out.print("Train ID (or press Enter to finish): ");
            String trainId = scanner.nextLine().trim();
            
            if (trainId.isEmpty()) {
                break;
            }

            try {
                Train train = collectTrainInfo(trainId);
                if (train != null) {
                    trains.add(train);
                    System.out.println("✅ Train " + trainId + " added successfully!");
                }
            } catch (Exception e) {
                System.out.println("❌ Error creating train: " + e.getMessage());
                System.out.print("Do you want to try again? (y/n): ");
                String retry = scanner.nextLine().trim().toLowerCase();
                if (!retry.equals("y") && !retry.equals("yes")) {
                    break;
                }
            }
        }

        return trains;
    }

    /**
     * Collects information for a single train.
     *
     * @param trainId the train ID
     * @return the train object, or null if cancelled
     */
    private Train collectTrainInfo(String trainId) {
        // Collect route
        List<Facility> route = collectRoute();
        if (route == null || route.size() < 2) {
            System.out.println("❌ Route must contain at least 2 stations.");
            return null;
        }

        // Collect locomotives
        List<Locomotive> locomotives = collectLocomotives();
        if (locomotives == null || locomotives.isEmpty()) {
            System.out.println("❌ Train must have at least one locomotive.");
            return null;
        }

        // Collect departure time
        LocalDateTime departureTime = collectDepartureTime();
        if (departureTime == null) {
            return null;
        }

        // Collect total weight
        double totalWeight = collectTotalWeight();

        // Collect priority (optional)
        Integer priority = collectPriority();

        return new Train(trainId, route, locomotives, departureTime, totalWeight, priority);
    }

    /**
     * Collects the route (list of stations) from the user.
     *
     * @return the list of stations in the route
     */
    private List<Facility> collectRoute() {
        List<Facility> route = new ArrayList<>();
        
        System.out.println("Enter route stations (at least 2 stations, each pair must be directly connected).");
        System.out.println("Press Enter with empty station ID to finish route.");
        
        while (true) {
            System.out.print("Station ID (or press Enter to finish route): ");
            String stationId = scanner.nextLine().trim();
            
            if (stationId.isEmpty()) {
                if (route.size() < 2) {
                    System.out.println("❌ Route must contain at least 2 stations.");
                    continue;
                }
                break;
            }

            Facility station = controller.getStationById(stationId);
            if (station == null) {
                System.out.println("❌ Station with ID '" + stationId + "' not found!");
                System.out.print("Do you want to try again? (y/n): ");
                String retry = scanner.nextLine().trim().toLowerCase();
                if (!retry.equals("y") && !retry.equals("yes")) {
                    return null;
                }
                continue;
            }

            route.add(station);
            System.out.println("✅ Station " + station.getName() + " (" + stationId + ") added to route.");
        }

        return route;
    }

    /**
     * Collects locomotives from the user.
     *
     * @return the list of locomotives
     */
    private List<Locomotive> collectLocomotives() {
        List<Locomotive> selectedLocomotives = new ArrayList<>();
        
        System.out.println("Enter locomotive IDs (at least 1 locomotive).");
        System.out.println("Press Enter with empty locomotive ID to finish.");
        
        while (true) {
            System.out.print("Locomotive ID (or press Enter to finish): ");
            String locomotiveId = scanner.nextLine().trim();
            
            if (locomotiveId.isEmpty()) {
                if (selectedLocomotives.isEmpty()) {
                    System.out.println("❌ Train must have at least one locomotive.");
                    continue;
                }
                break;
            }

            Locomotive locomotive = findLocomotiveById(locomotiveId);
            if (locomotive == null) {
                System.out.println("❌ Locomotive with ID '" + locomotiveId + "' not found!");
                System.out.print("Do you want to try again? (y/n): ");
                String retry = scanner.nextLine().trim().toLowerCase();
                if (!retry.equals("y") && !retry.equals("yes")) {
                    return null;
                }
                continue;
            }

            selectedLocomotives.add(locomotive);
            System.out.println("✅ Locomotive " + locomotive.getId() + " added.");
        }

        return selectedLocomotives;
    }

    /**
     * Collects the departure time from the user.
     *
     * @return the departure date and time
     */
    private LocalDateTime collectDepartureTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        while (true) {
            System.out.print("Departure Date and Time (format: yyyy-MM-dd HH:mm, e.g., 2025-11-25 08:00): ");
            String dateTimeStr = scanner.nextLine().trim();
            
            if (dateTimeStr.isEmpty()) {
                System.out.println("❌ Departure time cannot be empty!");
                continue;
            }

            try {
                return LocalDateTime.parse(dateTimeStr, formatter);
            } catch (DateTimeParseException e) {
                System.out.println("❌ Invalid date/time format! Please use yyyy-MM-dd HH:mm");
                System.out.print("Do you want to try again? (y/n): ");
                String retry = scanner.nextLine().trim().toLowerCase();
                if (!retry.equals("y") && !retry.equals("yes")) {
                    return null;
                }
            }
        }
    }

    /**
     * Collects the total weight from the user.
     *
     * @return the total weight in kg
     */
    private double collectTotalWeight() {
        while (true) {
            System.out.print("Total Weight (kg): ");
            String weightStr = scanner.nextLine().trim();
            
            if (weightStr.isEmpty()) {
                return 0.0; // Default weight
            }

            try {
                double weight = Double.parseDouble(weightStr);
                if (weight < 0) {
                    System.out.println("❌ Weight cannot be negative!");
                    continue;
                }
                return weight;
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid weight format! Please enter a number.");
                System.out.print("Do you want to try again? (y/n): ");
                String retry = scanner.nextLine().trim().toLowerCase();
                if (!retry.equals("y") && !retry.equals("yes")) {
                    return 0.0;
                }
            }
        }
    }

    /**
     * Collects the priority from the user (optional).
     *
     * @return the priority, or null if not set
     */
    private Integer collectPriority() {
        System.out.print("Priority (optional, press Enter to skip): ");
        String priorityStr = scanner.nextLine().trim();
        
        if (priorityStr.isEmpty()) {
            return null;
        }

        try {
            return Integer.parseInt(priorityStr);
        } catch (NumberFormatException e) {
            System.out.println("⚠️ Invalid priority format. Priority will be set to null.");
            return null;
        }
    }

    /**
     * Displays the dispatch report with schedules, conflicts, and summary.
     *
     * @param report the dispatch report to display
     */
    private void displayDispatchReport(DispatchReport report) {
        System.out.println("\n" + StringUtils.repeat("=", 60));
        System.out.println("DISPATCH REPORT");
        System.out.println(StringUtils.repeat("=", 60));

        // Display summary
        DispatchReport.Summary summary = report.getSummary();
        System.out.println("\n📊 SUMMARY:");
        System.out.println("   Total Trains Dispatched: " + summary.getTotalTrainsDispatched());
        System.out.println("   Number of Conflicts: " + summary.getNumberOfConflicts());
        System.out.println("   Resolvable Conflicts: " + summary.getNumberOfResolvableConflicts());
        System.out.println("   Unsolvable Conflicts: " + summary.getNumberOfUnsolvableConflicts());

        // Display schedules for each train
        System.out.println("\n" + StringUtils.repeat("=", 60));
        System.out.println("TRAIN SCHEDULES");
        System.out.println(StringUtils.repeat("=", 60));

        for (TrainSchedule schedule : report.getTrainSchedules()) {
            displayTrainSchedule(schedule);
        }

        // Display conflicts
        if (!report.getConflicts().isEmpty()) {
            System.out.println("\n" + StringUtils.repeat("=", 60));
            System.out.println("CONFLICTS DETECTED");
            System.out.println(StringUtils.repeat("=", 60));

            for (Conflict conflict : report.getConflicts()) {
                displayConflict(conflict);
            }
        } else {
            System.out.println("\n✅ No conflicts detected!");
        }
    }

    /**
     * Displays a single train schedule.
     *
     * @param schedule the train schedule to display
     */
    private void displayTrainSchedule(TrainSchedule schedule) {
        System.out.println("\n" + StringUtils.repeat("-", 60));
        System.out.println("Train: " + schedule.getTrainId());
        System.out.println("Total Travel Time: " + 
                         TravelCalculationService.formatTravelTime(schedule.getTotalTravelTimeHours()));
        System.out.println(StringUtils.repeat("-", 60));
        System.out.printf("%-15s %-25s %-15s %-10s%n", 
                         "Station ID", "Station Name", "ETA", "Travel Time");
        System.out.println(StringUtils.repeat("-", 60));

        for (TrainSchedule.StationETA eta : schedule.getRouteWithETAs()) {
            String stationId = eta.getStation().getId();
            String stationName = eta.getStation().getName();
            String etaStr = eta.getEta().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            String travelTimeStr = eta.getSegmentId() != null ? 
                TravelCalculationService.formatTravelTime(eta.getTravelTimeFromPreviousHours()) : "DEPARTURE";
            
            System.out.printf("%-15s %-25s %-15s %-10s%n", 
                            stationId, stationName, etaStr, travelTimeStr);
        }
    }

    /**
     * Displays a single conflict.
     *
     * @param conflict the conflict to display
     */
    private void displayConflict(Conflict conflict) {
        System.out.println("\n" + StringUtils.repeat("-", 60));
        System.out.println("⚠️ CONFLICT DETECTED");
        System.out.println(StringUtils.repeat("-", 60));
        System.out.println("Train 1: " + conflict.getTrain1Id());
        System.out.println("Train 2: " + conflict.getTrain2Id());
        System.out.println("Segment: " + conflict.getSegmentId());
        System.out.println("Type: " + conflict.getConflictType());
        System.out.println("Suggested Resolution: " + conflict.getResolution());
        System.out.println("Description: " + conflict.getDescription());
    }

    /**
     * Shows all available stations.
     */
    private void showAvailableStations() {
        System.out.println("\n" + StringUtils.repeat("-", 60));
        System.out.println("Available Stations");
        System.out.println(StringUtils.repeat("-", 60));
        
        if (facilities.isEmpty()) {
            System.out.println("No stations loaded.");
        } else {
            System.out.printf("Total stations: %d%n%n", facilities.size());
            
            for (Facility facility : facilities) {
                System.out.printf("ID: %-3s | Name: %s%n", 
                                 facility.getId(), facility.getName());
            }
        }
    }

    /**
     * Shows all available locomotives.
     */
    private void showAvailableLocomotives() {
        System.out.println("\n" + StringUtils.repeat("-", 60));
        System.out.println("Available Locomotives");
        System.out.println(StringUtils.repeat("-", 60));
        
        for (Locomotive locomotive : locomotives) {
            System.out.printf("ID: %s | Type: %s | Max Speed: %.0f km/h | Gauge: %.0fmm%n",
                             locomotive.getId(), locomotive.getType(), 
                             locomotive.getMaxSpeedKmH(), locomotive.getGauge());
        }
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
     * Creates mock locomotive data for testing and demonstration.
     * This matches the locomotives used in TravelTimeUI.
     *
     * @return list of mock locomotives
     */
    private List<Locomotive> createMockLocomotives() {
        return Arrays.asList(
            new Locomotive("L001", "electric", 120.0, 1668.0), // Standard gauge electric
            new Locomotive("L002", "diesel", 100.0, 1668.0),    // Standard gauge diesel
            new Locomotive("L003", "electric", 160.0, 1668.0), // High-speed electric
            new Locomotive("L004", "diesel", 80.0, 1668.0),    // Freight diesel
            new Locomotive("L005", "electric", 100.0, 1668.0), // Narrow gauge electric
            new Locomotive("L006", "diesel", 60.0, 1668.0),    // Narrow gauge diesel
            new Locomotive("L007", "electric", 200.0, 1668.0), // Very high-speed electric
            new Locomotive("L008", "diesel", 90.0, 1668.0)     // Russian gauge diesel
        );
    }
}

