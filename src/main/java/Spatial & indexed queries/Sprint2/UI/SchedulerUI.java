package Sprint2.UI;

import Sprint1.UI.StringUtils;
import Sprint2.Controller.DispatchController;
import Sprint2.Domain.DispatchReport;
import Sprint2.Domain.KDTree;
import Sprint2.Domain.LineSegment;
import Sprint2.Domain.Train;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * User interface for train dispatch scheduling.
 * This UI implements the CLI interaction for Train Dispatch Scheduler.
 *
 * @author Development Team
 * @version 1.0
 */
public class SchedulerUI {
    
    private final DispatchController controller;
    private final Scanner scanner;
    private final List<Facility> facilities;
    private final List<Locomotive> locomotives;
    private final List<Wagon> wagons;
    private final RailwayDataLoader dataLoader;

    /**
     * Constructs a new SchedulerUI with the specified controller.
     *
     * @param controller the dispatch controller
     */
    public SchedulerUI(DispatchController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
        this.facilities = controller.getStations();
        this.dataLoader = new RailwayDataLoader();
        
        // Load locomotives and wagons from CSV
        List<Locomotive> loadedLocomotives;
        List<Wagon> loadedWagons;
        
        try {
            loadedLocomotives = dataLoader.loadLocomotivesFromCSV("train_station_dataset/locomotives.csv");
            loadedWagons = dataLoader.loadWagonsFromCSV("train_station_dataset/wagons.csv");
            
            if (loadedLocomotives.isEmpty()) {
                System.out.println("WARNING: No locomotives loaded from CSV, using mock data.");
                loadedLocomotives = createMockLocomotives();
            }
        } catch (Exception e) {
            System.out.println("WARNING: Error loading data from CSV: " + e.getMessage());
            System.out.println("Using mock locomotives.");
            loadedLocomotives = createMockLocomotives();
            loadedWagons = new ArrayList<>();
        }
        
        this.locomotives = loadedLocomotives;
        this.wagons = loadedWagons;
    }

    /**
     * Shows the main menu for train dispatch scheduling.
     */
    public void showMainMenu() {
        while (true) {
            System.out.println("\n" + StringUtils.repeat("=", 60));
            System.out.println("Train Dispatch Scheduler");
            System.out.println(StringUtils.repeat("=", 60));
            System.out.println("1. Dispatch Trains");
            System.out.println("2. View Available Stations");
            System.out.println("3. View Available Locomotives");
            System.out.println("4. View Available Wagons");
            System.out.println("5. View Line Scheme");
            System.out.println("0. Back to Main Menu");
            
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
                    showAvailableWagons();
                    break;
                case "5":
                    showLineScheme();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("ERROR: Invalid option!");
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
            System.out.println("\nERROR: An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Collects train information from the user.
     * First asks how many trains to create, then collects information for each train.
     *
     * @return the list of trains to dispatch
     */
    private List<Train> collectTrains() {
        List<Train> trains = new ArrayList<>();
        
        // Ask how many trains to create
        int numTrains = 0;
        while (true) {
            System.out.println("\nHow many trains do you want to create?");
            System.out.print("Number of trains: ");
            String numStr = scanner.nextLine().trim();
            
            try {
                numTrains = Integer.parseInt(numStr);
                if (numTrains < 1) {
                    System.out.println("ERROR: Must create at least 1 train.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Invalid number. Please enter a positive integer.");
            }
        }
        
        System.out.println("\n" + StringUtils.repeat("=", 60));
        System.out.println("CREATING " + numTrains + " TRAIN(S)");
        System.out.println(StringUtils.repeat("=", 60));
        
        // Collect information for each train
        for (int i = 1; i <= numTrains; i++) {
            System.out.println("\n" + StringUtils.repeat("-", 60));
            System.out.println("TRAIN " + i + " of " + numTrains);
            System.out.println(StringUtils.repeat("-", 60));
            
            while (true) {
                System.out.print("Train ID: ");
                String trainId = scanner.nextLine().trim();
                
                if (trainId.isEmpty()) {
                    System.out.println("ERROR: Train ID cannot be empty.");
                    continue;
                }

                try {
                    Train train = collectTrainInfo(trainId, trains);
                    if (train != null) {
                        trains.add(train);
                        System.out.println("\n[SUCCESS] Train " + trainId + " created successfully!");
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("\nERROR: Error creating train: " + e.getMessage());
                    System.out.println("Skipping this train. Continuing with next train...");
                    break;
                }
            }
        }

        return trains;
    }

    /**
     * Collects information for a single train.
     * User manually selects locomotives and wagons from CSV, and enters the route.
     * Validates that locomotives are not being used by other trains at the same time.
     *
     * @param trainId the train ID
     * @param existingTrains the list of trains already created (for locomotive validation)
     * @return the train object, or null if cancelled
     */
    private Train collectTrainInfo(String trainId, List<Train> existingTrains) {
        // Collect route (returns both full route and selected stations)
        RouteData routeData = collectRoute();
        if (routeData == null || routeData.fullRoute.size() < 2) {
            System.out.println("\nERROR: Route must contain at least 2 stations.");
            return null;
        }
        
        List<Facility> route = routeData.fullRoute;
        List<Facility> selectedStations = routeData.selectedStations;

        // Collect departure time first (needed for locomotive validation)
        LocalDateTime departureTime = collectDepartureTime();
        if (departureTime == null) {
            return null;
        }

        // Use wrapper array to allow modification of departure time during locomotive selection
        LocalDateTime[] departureTimeWrapper = new LocalDateTime[]{departureTime};

        // Collect locomotives from CSV with validation
        List<Locomotive> selectedLocomotives = collectLocomotivesWithValidation(existingTrains, route.get(0), departureTimeWrapper);
        if (selectedLocomotives == null || selectedLocomotives.isEmpty()) {
            System.out.println("\nERROR: Train must have at least one locomotive.");
            return null;
        }

        // Update departure time in case it was changed during locomotive selection
        departureTime = departureTimeWrapper[0];

        // Collect wagons from CSV with validation
        List<Wagon> selectedWagons = collectWagonsWithValidation(existingTrains);
        if (selectedWagons == null) {
            return null;
        }

        // Calculate total weight from wagons (empty or loaded)
        double totalWeight = calculateTotalWeight(selectedWagons);

        return new Train(trainId, route, selectedStations, selectedLocomotives, selectedWagons, departureTime, totalWeight);
    }
    
    /**
     * Helper class to store both full route and selected stations.
     */
    private static class RouteData {
        final List<Facility> fullRoute;
        final List<Facility> selectedStations;
        
        RouteData(List<Facility> fullRoute, List<Facility> selectedStations) {
            this.fullRoute = fullRoute;
            this.selectedStations = selectedStations;
        }
    }

    /**
     * Collects the route (list of stations) from the user.
     *
     * @return RouteData containing both full route (with intermediates) and selected stations (user choices only)
     */
    private RouteData collectRoute() {
        List<Facility> route = new ArrayList<>(); // Full route with intermediate stations
        List<Facility> selectedStations = new ArrayList<>(); // Only stations selected by user
        
        // Show line scheme before collecting route
        displayLineScheme();
        
        System.out.println("\nEnter route stations (at least 2 stations).");
        System.out.println("The system will automatically find intermediate stations if needed.");
        System.out.println("Press Enter with empty station ID to finish route.");
        System.out.println();
        
        while (true) {
            System.out.print("Station ID (or press Enter to finish route): ");
            String stationId = scanner.nextLine().trim();
            
            if (stationId.isEmpty()) {
                if (selectedStations.size() < 2) {
                    System.out.println("ERROR: Route must contain at least 2 stations.");
                    System.out.println();
                    continue;
                }
                break;
            }

            Facility station = controller.getStationById(stationId);
            if (station == null) {
                System.out.println("ERROR: Station with ID '" + stationId + "' does not exist in facility.csv!");
                System.out.println("Please enter another station ID.");
                System.out.println();
                continue;
            }

            // Add to selected stations (user choice)
            selectedStations.add(station);

            // If this is not the first station, check if we need to find a path
            if (route.isEmpty()) {
                // First station
                route.add(station);
                System.out.println("Station " + station.getName() + " (" + stationId + ") added to route.");
            } else {
                Facility previousStation = route.get(route.size() - 1);
                TravelCalculationService travelService = controller.getTravelTimeController().getService();
                
                // Check if directly connected
                if (!travelService.areStationsConnected(previousStation, station)) {
                    // Try to find a path
                    List<Facility> path = travelService.findPath(previousStation, station);
                    if (path == null || path.size() < 2) {
                        System.out.println("ERROR: No path found between " + previousStation.getId() + 
                                         " and " + station.getId() + ".");
                        selectedStations.remove(selectedStations.size() - 1); // Remove from selected
                        System.out.println("Please enter another station ID.");
                        System.out.println();
                        continue;
                    }
                    
                    // Add intermediate stations (skip first as it's already in route)
                    System.out.println("Path found: " + path.stream()
                        .map(f -> f.getId())
                        .reduce((a, b) -> a + " -> " + b)
                        .orElse(""));
                    for (int i = 1; i < path.size(); i++) {
                        route.add(path.get(i));
                        if (i == path.size() - 1) {
                            System.out.println("Station " + path.get(i).getName() + " (" + path.get(i).getId() + ") added to route.");
                        } else {
                            System.out.println("Intermediate station " + path.get(i).getName() + " (" + path.get(i).getId() + ") added to route.");
                        }
                    }
                } else {
                    route.add(station);
                    System.out.println("Station " + station.getName() + " (" + stationId + ") added to route.");
                }
            }
            System.out.println();
        }

        return new RouteData(route, selectedStations);
    }

    /**
     * Collects locomotives from the user (from CSV).
     *
     * @return the list of locomotives
     */
    private List<Locomotive> collectLocomotives() {
        List<Locomotive> selectedLocomotives = new ArrayList<>();
        
        // Ask how many locomotives
        int numLocomotives = 0;
        while (true) {
            System.out.println("\nHow many locomotives do you want? (at least 1)");
            System.out.print("Number of locomotives: ");
            String numStr = scanner.nextLine().trim();
            
            try {
                numLocomotives = Integer.parseInt(numStr);
                if (numLocomotives < 1) {
                    System.out.println("ERROR: Must have at least 1 locomotive.");
                    System.out.println();
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Invalid number. Please enter a positive integer.");
                System.out.println();
            }
        }
        
        System.out.println();
        if (!locomotives.isEmpty()) {
            System.out.println("Available Locomotives:");
            for (Locomotive loco : locomotives) {
                System.out.printf("  - %s (%s, %.0f km/h, %.0fmm gauge)%n",
                    loco.getId(), loco.getType(), loco.getMaxSpeedKmH(), loco.getGauge());
            }
            System.out.println();
        }
        
        for (int i = 0; i < numLocomotives; i++) {
            System.out.print("Locomotive " + (i + 1) + " ID: ");
            String locomotiveId = scanner.nextLine().trim();
            
            if (locomotiveId.isEmpty()) {
                System.out.println("ERROR: Locomotive ID cannot be empty.");
                i--; // Retry this iteration
                System.out.println();
                continue;
            }

            Locomotive locomotive = findLocomotiveById(locomotiveId);
            if (locomotive == null) {
                System.out.println("ERROR: Locomotive with ID '" + locomotiveId + "' not found!");
                System.out.print("Do you want to try again? (y/n): ");
                String retry = scanner.nextLine().trim().toLowerCase();
                if (!retry.equals("y") && !retry.equals("yes")) {
                    return null;
                }
                i--; // Retry this iteration
                System.out.println();
                continue;
            }

            selectedLocomotives.add(locomotive);
            System.out.println("Locomotive " + locomotive.getId() + " added.");
            System.out.println();
        }

        return selectedLocomotives;
    }

    /**
     * Collects locomotives from the user with validation to ensure they are not being used by other trains.
     *
     * @param existingTrains the list of trains already created
     * @param departureStation the departure station of the new train
     * @param departureTimeWrapper array with one element containing the departure time (can be modified if user chooses to change it)
     * @return the list of selected locomotives, or null if cancelled or if user wants to change departure time
     */
    private List<Locomotive> collectLocomotivesWithValidation(List<Train> existingTrains, Facility departureStation, LocalDateTime[] departureTimeWrapper) {
        List<Locomotive> selectedLocomotives = new ArrayList<>();
        
        // Ask how many locomotives
        int numLocomotives = 0;
        while (true) {
            System.out.println("\nHow many locomotives do you want? (at least 1)");
            System.out.print("Number of locomotives: ");
            String numStr = scanner.nextLine().trim();
            
            try {
                numLocomotives = Integer.parseInt(numStr);
                if (numLocomotives < 1) {
                    System.out.println("ERROR: Must have at least 1 locomotive.");
                    System.out.println();
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Invalid number. Please enter a positive integer.");
                System.out.println();
            }
        }
        
        System.out.println();
        if (!locomotives.isEmpty()) {
            System.out.println("Available Locomotives:");
            for (Locomotive loco : locomotives) {
                System.out.printf("  - %s (%s, %.0f km/h, %.0fmm gauge)%n",
                    loco.getId(), loco.getType(), loco.getMaxSpeedKmH(), loco.getGauge());
            }
            System.out.println();
        }
        
        for (int i = 0; i < numLocomotives; i++) {
            while (true) {
                System.out.print("Locomotive " + (i + 1) + " ID: ");
                String locomotiveId = scanner.nextLine().trim();
                
                if (locomotiveId.isEmpty()) {
                    System.out.println("ERROR: Locomotive ID cannot be empty.");
                    System.out.println();
                    continue;
                }

                Locomotive locomotive = findLocomotiveById(locomotiveId);
                if (locomotive == null) {
                    System.out.println("ERROR: Locomotive with ID '" + locomotiveId + "' not found!");
                    System.out.println("Please enter another locomotive ID.");
                    System.out.println();
                    continue;
                }

                // Validate locomotive availability
                String validationError = validateLocomotiveAvailability(locomotive, existingTrains, departureStation, departureTimeWrapper[0]);
                if (validationError != null) {
                    System.out.println("ERROR: " + validationError);
                    System.out.println();
                    System.out.println("What would you like to do?");
                    System.out.println("1. Change departure time");
                    System.out.println("2. Choose a different locomotive");
                    System.out.print("Select option (1 or 2): ");
                    String choice = scanner.nextLine().trim();
                    
                    if (choice.equals("1")) {
                        // User wants to change departure time
                        LocalDateTime newDepartureTime = collectDepartureTime();
                        if (newDepartureTime == null) {
                            return null; // User cancelled
                        }
                        departureTimeWrapper[0] = newDepartureTime;
                        // Re-validate with new departure time
                        validationError = validateLocomotiveAvailability(locomotive, existingTrains, departureStation, departureTimeWrapper[0]);
                        if (validationError != null) {
                            System.out.println("ERROR: " + validationError);
                            System.out.println("The locomotive is still not available at the new departure time.");
                            System.out.println("Please enter a different departure time.");
                            System.out.println();
                            continue;
                        }
                        // Locomotive is now available with new departure time
                    } else if (choice.equals("2")) {
                        // User wants to choose a different locomotive
                        System.out.println();
                        continue;
                    } else {
                        System.out.println("ERROR: Invalid option. Please choose 1 or 2.");
                        System.out.println();
                        continue;
                    }
                }

                selectedLocomotives.add(locomotive);
                System.out.println("Locomotive " + locomotive.getId() + " added.");
                System.out.println();
                break;
            }
        }

        return selectedLocomotives;
    }

    /**
     * Validates if a locomotive is available for use at the specified departure time and station.
     *
     * @param locomotive the locomotive to validate
     * @param existingTrains the list of trains already created
     * @param departureStation the departure station of the new train
     * @param departureTime the departure time of the new train
     * @return null if available, error message if not available
     */
    private String validateLocomotiveAvailability(Locomotive locomotive, List<Train> existingTrains, 
                                                   Facility departureStation, LocalDateTime departureTime) {
        if (existingTrains == null || existingTrains.isEmpty()) {
            return null; // No existing trains, locomotive is available
        }

        TravelCalculationService travelService = controller.getTravelTimeController().getService();

        for (Train existingTrain : existingTrains) {
            // Check if this locomotive is used in the existing train
            if (!existingTrain.getLocomotives().contains(locomotive)) {
                continue; // Locomotive not used in this train
            }

            // Calculate when the locomotive will be available
            LocalDateTime locomotiveAvailableTime = calculateLocomotiveAvailableTime(
                locomotive, existingTrain, departureStation, travelService);

            // Check if departure time is after locomotive becomes available
            if (departureTime.isBefore(locomotiveAvailableTime)) {
                return String.format(
                    "Locomotive %s is already assigned to train %s. " +
                    "It will be available at %s (after completing its journey and traveling to %s). " +
                    "Please choose a departure time after %s or select a different locomotive.",
                    locomotive.getId(),
                    existingTrain.getTrainId(),
                    locomotiveAvailableTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    departureStation.getId(),
                    locomotiveAvailableTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                );
            }
        }

        return null; // Locomotive is available
    }

    /**
     * Calculates when a locomotive will be available at a specific station.
     * This considers: completion time of current journey + travel time to new departure station.
     *
     * @param locomotive the locomotive
     * @param train the train currently using the locomotive
     * @param targetStation the station where the locomotive needs to be available
     * @param travelService the travel calculation service
     * @return the time when the locomotive will be available at the target station
     */
    private LocalDateTime calculateLocomotiveAvailableTime(Locomotive locomotive, Train train,
                                                           Facility targetStation, TravelCalculationService travelService) {
        // Get the train's route
        List<Facility> route = train.getRoute();
        if (route.isEmpty()) {
            return train.getDeparturePlannedDateTime(); // Should not happen, but safety check
        }

        // Calculate total travel time for the train
        double totalTravelTimeHours = 0.0;
        for (int i = 0; i < route.size() - 1; i++) {
            try {
                double segmentTime = travelService.calculateTravelTime(route.get(i), route.get(i + 1), locomotive);
                totalTravelTimeHours += segmentTime;
            } catch (Exception e) {
                // If calculation fails, use a simple estimate
                // This shouldn't happen if routes are valid, but handle gracefully
                return train.getDeparturePlannedDateTime().plusHours(24); // Conservative estimate
            }
        }

        // Time when train completes its journey (arrives at final station)
        LocalDateTime journeyEndTime = train.getDeparturePlannedDateTime().plusMinutes((long)(totalTravelTimeHours * 60));

        // Get the final station of the train's route
        Facility finalStation = route.get(route.size() - 1);

        // If target station is the same as final station, locomotive is available immediately after journey
        if (finalStation.getId().equals(targetStation.getId())) {
            return journeyEndTime;
        }

        // Calculate travel time from final station to target station
        double travelTimeToTargetHours = 0.0;
        try {
            travelTimeToTargetHours = travelService.calculateTravelTime(finalStation, targetStation, locomotive);
        } catch (Exception e) {
            // If stations are not directly connected, try to find a path through the route
            // If the target station is in the train's route, calculate time to return
            int targetIndex = -1;
            for (int i = 0; i < route.size(); i++) {
                if (route.get(i).getId().equals(targetStation.getId())) {
                    targetIndex = i;
                    break;
                }
            }
            
            if (targetIndex >= 0 && targetIndex < route.size() - 1) {
                // Target station is in the route, calculate return time
                // Calculate time from final station back to target station (reverse path)
                for (int i = route.size() - 1; i > targetIndex; i--) {
                    try {
                        double segmentTime = travelService.calculateTravelTime(route.get(i), route.get(i - 1), locomotive);
                        travelTimeToTargetHours += segmentTime;
                    } catch (Exception ex) {
                        // If we can't calculate return path, assume locomotive is already at target
                        // (it passed through there during the journey)
                        travelTimeToTargetHours = 0.0;
                        break;
                    }
                }
            } else {
                // Target station not in route and not directly connected
                // Assume locomotive needs to return to departure station (first station of route)
                // This is a reasonable assumption for most cases
                if (route.get(0).getId().equals(targetStation.getId())) {
                    // Target is the departure station, calculate return time
                    for (int i = route.size() - 1; i > 0; i--) {
                        try {
                            double segmentTime = travelService.calculateTravelTime(route.get(i), route.get(i - 1), locomotive);
                            travelTimeToTargetHours += segmentTime;
                        } catch (Exception ex) {
                            // Can't calculate return path, use 0 (shouldn't happen for valid routes)
                            travelTimeToTargetHours = 0.0;
                            break;
                        }
                    }
                } else {
                    // Target station is completely different and not connected
                    // This shouldn't happen in normal operation, but if it does, 
                    // assume no additional travel time needed (locomotive stays at final station)
                    travelTimeToTargetHours = 0.0;
                }
            }
        }

        // Locomotive is available after completing journey + traveling to target station
        return journeyEndTime.plusMinutes((long)(travelTimeToTargetHours * 60));
    }

    /**
     * Validates if a wagon is available for use.
     * A wagon cannot be assigned to multiple trains - once assigned to a train, it cannot be assigned to another.
     *
     * @param wagon the wagon to validate
     * @param existingTrains the list of trains already created
     * @return null if available, error message if not available
     */
    private String validateWagonAvailability(Wagon wagon, List<Train> existingTrains) {
        if (existingTrains == null || existingTrains.isEmpty()) {
            return null; // No existing trains, wagon is available
        }

        for (Train existingTrain : existingTrains) {
            // Check if this wagon is already assigned to an existing train
            if (existingTrain.getWagons().contains(wagon)) {
                return String.format(
                    "Wagon %s is already assigned to train %s. " +
                    "A wagon cannot be assigned to multiple trains. Please select a different wagon.",
                    wagon.getWagonId(),
                    existingTrain.getTrainId()
                );
            }
        }

        return null; // Wagon is available
    }


    /**
     * Collects the departure time from the user.
     *
     * @return the departure date and time
     */
    private LocalDateTime collectDepartureTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        System.out.println();
        while (true) {
            System.out.print("Departure Date and Time (format: yyyy-MM-dd HH:mm, e.g., 2025-11-25 08:00): ");
            String dateTimeStr = scanner.nextLine().trim();
            
            if (dateTimeStr.isEmpty()) {
                System.out.println("ERROR: Departure time cannot be empty!");
                System.out.println();
                continue;
            }

            try {
                return LocalDateTime.parse(dateTimeStr, formatter);
            } catch (DateTimeParseException e) {
                System.out.println("ERROR: Invalid date/time format! Please use yyyy-MM-dd HH:mm");
                System.out.print("Do you want to try again? (y/n): ");
                String retry = scanner.nextLine().trim().toLowerCase();
                if (!retry.equals("y") && !retry.equals("yes")) {
                    return null;
                }
                System.out.println();
            }
        }
    }

    /**
     * Collects wagons from the user (from CSV) with validation to ensure they are not already assigned to other trains.
     * A wagon cannot be assigned to multiple trains.
     *
     * @param existingTrains the list of trains already created
     * @return the list of selected wagons (can be empty), or null if cancelled
     */
    private List<Wagon> collectWagonsWithValidation(List<Train> existingTrains) {
        List<Wagon> selectedWagons = new ArrayList<>();
        
        // Ask how many wagons
        int numWagons = 0;
        while (true) {
            System.out.println("\nHow many wagons do you want? (can be 0)");
            System.out.print("Number of wagons: ");
            String numStr = scanner.nextLine().trim();
            
            try {
                numWagons = Integer.parseInt(numStr);
                if (numWagons < 0) {
                    System.out.println("ERROR: Number cannot be negative.");
                    System.out.println();
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Invalid number. Please enter a non-negative integer.");
                System.out.println();
            }
        }
        
        if (numWagons == 0) {
            System.out.println("No wagons selected.");
            System.out.println();
            return selectedWagons;
        }
        
        System.out.println();
        if (!wagons.isEmpty()) {
            System.out.println("Available Wagons:");
            for (Wagon wagon : wagons) {
                System.out.printf("  - %s (empty: %.0f kg, max load: %.0f kg)%n",
                    wagon.getWagonId(), wagon.getEmptyWeightKg(), wagon.getMaxLoadWeightKg());
            }
            System.out.println();
        }
        
        for (int i = 0; i < numWagons; i++) {
            while (true) {
                System.out.print("Wagon " + (i + 1) + " ID: ");
                String wagonId = scanner.nextLine().trim();
                
                if (wagonId.isEmpty()) {
                    System.out.println("ERROR: Wagon ID cannot be empty.");
                    System.out.println();
                    continue;
                }

                Wagon wagon = findWagonById(wagonId);
                if (wagon == null) {
                    System.out.println("ERROR: Wagon with ID '" + wagonId + "' not found in CSV!");
                    System.out.println("Please enter another wagon ID.");
                    System.out.println();
                    continue;
                }

                // Validate wagon availability
                String validationError = validateWagonAvailability(wagon, existingTrains);
                if (validationError != null) {
                    System.out.println("ERROR: " + validationError);
                    System.out.println();
                    continue; // Ask for a new wagon ID directly
                }

                selectedWagons.add(wagon);
                System.out.println("Wagon " + wagon.getWagonId() + " added.");
                System.out.println();
                break;
            }
        }

        return selectedWagons;
    }

    /**
     * Collects wagons from the user (from CSV).
     *
     * @return the list of selected wagons (can be empty)
     */
    private List<Wagon> collectWagons() {
        List<Wagon> selectedWagons = new ArrayList<>();
        
        // Ask how many wagons
        int numWagons = 0;
        while (true) {
            System.out.println("\nHow many wagons do you want? (can be 0)");
            System.out.print("Number of wagons: ");
            String numStr = scanner.nextLine().trim();
            
            try {
                numWagons = Integer.parseInt(numStr);
                if (numWagons < 0) {
                    System.out.println("ERROR: Number cannot be negative.");
                    System.out.println();
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Invalid number. Please enter a non-negative integer.");
                System.out.println();
            }
        }
        
        if (numWagons == 0) {
            System.out.println("No wagons selected.");
            System.out.println();
            return selectedWagons;
        }
        
        System.out.println();
        if (!wagons.isEmpty()) {
            System.out.println("Available Wagons:");
            for (Wagon wagon : wagons) {
                System.out.printf("  - %s (empty: %.0f kg, max load: %.0f kg)%n",
                    wagon.getWagonId(), wagon.getEmptyWeightKg(), wagon.getMaxLoadWeightKg());
            }
            System.out.println();
        }
        
        for (int i = 0; i < numWagons; i++) {
            System.out.print("Wagon " + (i + 1) + " ID: ");
            String wagonId = scanner.nextLine().trim();
            
            if (wagonId.isEmpty()) {
                System.out.println("ERROR: Wagon ID cannot be empty.");
                i--; // Retry this iteration
                System.out.println();
                continue;
            }

            Wagon wagon = findWagonById(wagonId);
            if (wagon == null) {
                System.out.println("ERROR: Wagon with ID '" + wagonId + "' not found in CSV!");
                System.out.println("Please enter another wagon ID.");
                i--; // Retry this iteration
                System.out.println();
                continue;
            }

            selectedWagons.add(wagon);
            System.out.println("Wagon " + wagon.getWagonId() + " added.");
            System.out.println();
        }

        return selectedWagons;
    }

    /**
     * Calculates the total weight from selected wagons.
     * Asks user if wagons are loaded or empty.
     *
     * @param selectedWagons the list of selected wagons
     * @return the total weight in kg
     */
    private double calculateTotalWeight(List<Wagon> selectedWagons) {
        if (selectedWagons.isEmpty()) {
            return 0.0;
        }
        
        // Calculate empty weight
        double emptyWeight = selectedWagons.stream()
            .mapToDouble(Wagon::getEmptyWeightKg)
            .sum();
        
        System.out.println("\nWagon Load Status:");
        System.out.println("   Empty weight: " + emptyWeight + " kg");
        System.out.print("Are wagons loaded? (y/n, default: n): ");
        String loadedStr = scanner.nextLine().trim().toLowerCase();
        boolean wagonsLoaded = loadedStr.equals("y") || loadedStr.equals("yes");
        
        double totalWeight = emptyWeight;
        if (wagonsLoaded) {
            // Calculate max load weight
            double maxLoadWeight = selectedWagons.stream()
                .mapToDouble(Wagon::getMaxLoadWeightKg)
                .sum();
            totalWeight += maxLoadWeight;
            System.out.println("   Using fully loaded weight: " + totalWeight + " kg");
        } else {
            System.out.println("   Using empty weight: " + totalWeight + " kg");
        }
        System.out.println();
        
        return totalWeight;
    }

    /**
     * Finds a wagon by its ID.
     *
     * @param id the wagon ID
     * @return the wagon or null if not found
     */
    private Wagon findWagonById(String id) {
        return wagons.stream()
                .filter(wagon -> wagon.getWagonId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Displays the dispatch report with schedules, conflicts, and summary.
     * Format: Train -> Route -> Departure time -> Estimated times per point -> Crossing operations
     *
     * @param report the dispatch report to display
     */
    private void displayDispatchReport(DispatchReport report) {
        System.out.println("\n" + StringUtils.repeat("=", 80));
        System.out.println("DISPATCH REPORT");
        System.out.println(StringUtils.repeat("=", 80));

        // Display summary
        DispatchReport.Summary summary = report.getSummary();
        System.out.println();
        System.out.println("SUMMARY:");
        System.out.println("   Total Trains Dispatched: " + summary.getTotalTrainsDispatched());
        System.out.println("   Number of Conflicts: " + summary.getNumberOfConflicts());
        System.out.println("   Resolvable Conflicts: " + summary.getNumberOfResolvableConflicts());
        System.out.println("   Unsolvable Conflicts: " + summary.getNumberOfUnsolvableConflicts());
        System.out.println();

        // Display schedules for each train
        System.out.println("\n" + StringUtils.repeat("=", 80));
        System.out.println("PLANNED STATE OF EACH TRAIN");
        System.out.println(StringUtils.repeat("=", 80));

        for (DispatchReport.TrainSchedule schedule : report.getTrainSchedules()) {
            displayTrainSchedule(schedule);
        }

        // Display conflicts in simple lines
        if (!report.getConflicts().isEmpty()) {
            System.out.println("\n" + StringUtils.repeat("=", 80));
            System.out.println("CROSSING/CONFLICT ALERTS");
            System.out.println(StringUtils.repeat("=", 80));

            for (DispatchReport.Conflict conflict : report.getConflicts()) {
                displayConflictSimple(conflict, report);
            }
        } else {
            System.out.println("\n[INFO] No conflicts detected!");
        }
    }

    /**
     * Displays a single train schedule.
     * Format: Train -> Route -> Departure time -> Estimated times per point
     *
     * @param schedule the train schedule to display
     */
    private void displayTrainSchedule(DispatchReport.TrainSchedule schedule) {
        List<DispatchReport.TrainSchedule.StationETA> routeWithETAs = schedule.getRouteWithETAs();
        List<Facility> selectedStations = schedule.getSelectedStations();
        if (routeWithETAs.isEmpty()) {
            return;
        }

        // Get departure time (first station ETA)
        LocalDateTime departureTime = routeWithETAs.get(0).getEta();
        String departureTimeStr = departureTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        // Build route string using only selected stations
        StringBuilder routeStr = new StringBuilder();
        for (int i = 0; i < selectedStations.size(); i++) {
            if (i > 0) routeStr.append(" -> ");
            routeStr.append(selectedStations.get(i).getId());
        }

        // Table width: 12 + 1 + 30 + 1 + 20 + 1 + 15 + 1 + 15 = 96 characters
        int tableWidth = 96;
        System.out.println("\n" + StringUtils.repeat("-", tableWidth));
        System.out.println("TRAIN: " + schedule.getTrainId());
        System.out.println("ROUTE: " + routeStr.toString());
        System.out.println("DEPARTURE TIME: " + departureTimeStr);
        System.out.println("TOTAL TRAVEL TIME: " + 
                         TravelCalculationService.formatTravelTime(schedule.getTotalTravelTimeHours()));
        System.out.println(StringUtils.repeat("-", tableWidth));
        System.out.println();
        System.out.println("ESTIMATED PASSAGE TIMES PER POINT:");
        System.out.println(StringUtils.repeat("-", tableWidth));
        System.out.printf("%-12s %-30s %-20s %-15s %-15s%n", 
                         "Point", "Station Name", "Estimated Time", "Travel Time", "Distance");
        System.out.println(StringUtils.repeat("-", tableWidth));

        // Create a map of station ID to ETA for quick lookup
        java.util.Map<String, DispatchReport.TrainSchedule.StationETA> etaMap = new java.util.HashMap<>();
        for (DispatchReport.TrainSchedule.StationETA eta : routeWithETAs) {
            etaMap.put(eta.getStation().getId(), eta);
        }
        
        // Display only selected stations, calculating cumulative travel time and distance
        LocalDateTime previousEta = null;
        Facility previousStation = null;
        for (int i = 0; i < selectedStations.size(); i++) {
            Facility station = selectedStations.get(i);
            DispatchReport.TrainSchedule.StationETA eta = etaMap.get(station.getId());
            
            if (eta == null) {
                continue; // Skip if not found (shouldn't happen)
            }
            
            String stationId = station.getId();
            String stationName = station.getName();
            String etaStr = eta.getEta().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            
            String travelTimeStr;
            if (i == 0) {
                travelTimeStr = "DEPARTURE";
            } else {
                // Calculate travel time from previous selected station
                if (previousEta != null) {
                    long minutes = java.time.Duration.between(previousEta, eta.getEta()).toMinutes();
                    int hours = (int) (minutes / 60);
                    int mins = (int) (minutes % 60);
                    if (hours > 0) {
                        travelTimeStr = String.format("%dh %dm", hours, mins);
                    } else {
                        travelTimeStr = String.format("%dm", mins);
                    }
                } else {
                    travelTimeStr = "DEPARTURE";
                }
            }
            
            // Calculate distance from previous station (summing all segments including intermediates)
            String distanceStr;
            if (i == 0) {
                distanceStr = "-";
            } else if (previousStation != null) {
                double totalDistanceKm = calculateDistanceBetweenStations(
                    previousStation, station, routeWithETAs);
                
                distanceStr = totalDistanceKm > 0 ? formatDistance(totalDistanceKm) : "N/A";
            } else {
                distanceStr = "-";
            }
            
            System.out.printf("%-12s %-30s %-20s %-15s %-15s%n", 
                            stationId, stationName, etaStr, travelTimeStr, distanceStr);
            
            previousEta = eta.getEta();
            previousStation = station;
        }
        
        System.out.println(StringUtils.repeat("-", tableWidth));
        System.out.println("TRAIN " + schedule.getTrainId());
        
        double totalRouteDistanceKm = calculateTotalRouteDistance(routeWithETAs);
        if (totalRouteDistanceKm > 0) {
            System.out.println("TOTAL DISTANCE: " + formatDistance(totalRouteDistanceKm));
        } else {
            System.out.println("TOTAL DISTANCE: N/A");
        }
    }

    /**
     * Calculates the total distance between two stations by summing all segment distances
     * in the route, including intermediate stations.
     *
     * @param startStation the starting station (selected)
     * @param endStation the ending station (selected)
     * @param routeWithETAs the full route with all stations (including intermediates)
     * @return total distance in kilometers, or 0 if not found
     */
    private double calculateDistanceBetweenStations(
            Facility startStation, Facility endStation,
            List<DispatchReport.TrainSchedule.StationETA> routeWithETAs) {
        
        // Find the indices of start and end stations in the full route
        int startIndex = -1;
        int endIndex = -1;
        
        for (int i = 0; i < routeWithETAs.size(); i++) {
            Facility station = routeWithETAs.get(i).getStation();
            if (station.getId().equals(startStation.getId())) {
                startIndex = i;
            }
            if (station.getId().equals(endStation.getId())) {
                endIndex = i;
            }
        }
        
        // If stations not found in route, return 0
        if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
            return 0.0;
        }
        
        // Sum distances of all segments between start and end stations
        double totalDistanceKm = 0.0;
        TravelCalculationService service = controller.getTravelTimeController().getService();
        
        for (int i = startIndex; i < endIndex; i++) {
            Facility currentStation = routeWithETAs.get(i).getStation();
            Facility nextStation = routeWithETAs.get(i + 1).getStation();
            
            try {
                LineSegment segment = service.findConnectingSegment(currentStation, nextStation);
                if (segment != null) {
                    totalDistanceKm += segment.getLengthKm();
                    continue;
                }
            } catch (Exception e) {
                // Fall through to haversine
            }
            
            // If segment not found, try haversine distance (straight-line)
            if (currentStation.getLatitude() != null && currentStation.getLongitude() != null &&
                nextStation.getLatitude() != null && nextStation.getLongitude() != null) {
                double haversineDist = KDTree.haversineDistance(
                    currentStation.getLatitude(), currentStation.getLongitude(),
                    nextStation.getLatitude(), nextStation.getLongitude()
                );
                totalDistanceKm += haversineDist;
            }
        }
        
        return totalDistanceKm;
    }

    /**
     * Calculates the total distance of the complete route (all segments).
     *
     * @param routeWithETAs full route with all stations
     * @return total distance in kilometers
     */
    private double calculateTotalRouteDistance(List<DispatchReport.TrainSchedule.StationETA> routeWithETAs) {
        if (routeWithETAs == null || routeWithETAs.size() < 2) {
            return 0.0;
        }
        
        double totalDistanceKm = 0.0;
        TravelCalculationService service = controller.getTravelTimeController().getService();
        
        for (int i = 0; i < routeWithETAs.size() - 1; i++) {
            Facility currentStation = routeWithETAs.get(i).getStation();
            Facility nextStation = routeWithETAs.get(i + 1).getStation();
            
            try {
                LineSegment segment = service.findConnectingSegment(currentStation, nextStation);
                if (segment != null) {
                    totalDistanceKm += segment.getLengthKm();
                    continue;
                }
            } catch (Exception e) {
                // Fall through to haversine
            }
            
            if (currentStation.getLatitude() != null && currentStation.getLongitude() != null &&
                nextStation.getLatitude() != null && nextStation.getLongitude() != null) {
                double haversineDist = KDTree.haversineDistance(
                    currentStation.getLatitude(), currentStation.getLongitude(),
                    nextStation.getLatitude(), nextStation.getLongitude()
                );
                totalDistanceKm += haversineDist;
            }
        }
        
        return totalDistanceKm;
    }

    /**
     * Formats a distance value in kilometers for display.
     *
     * @param distanceKm the distance in kilometers
     * @return formatted string
     */
    private String formatDistance(double distanceKm) {
        if (distanceKm <= 0) {
            return "N/A";
        }
        if (distanceKm < 1.0) {
            return String.format("%.0f m", distanceKm * 1000);
        }
        return String.format("%.1f km", distanceKm);
    }

    /**
     * Displays a conflict in a simple line format.
     *
     * @param conflict the conflict to display
     * @param report the dispatch report to get station information from
     */
    private void displayConflictSimple(DispatchReport.Conflict conflict, DispatchReport report) {
        System.out.println();
        System.out.println("[CONFLICT] Train " + conflict.getTrain1Id() + " vs Train " + conflict.getTrain2Id());
        
        // Find station name from segment ID
        String stationName = conflict.getSegmentId(); // Default to segment ID if not found
        for (DispatchReport.TrainSchedule schedule : report.getTrainSchedules()) {
            for (DispatchReport.TrainSchedule.SegmentTime segmentTime : schedule.getSegmentTimes()) {
                if (segmentTime.getSegmentId().equals(conflict.getSegmentId())) {
                    // Use the end station name (where the conflict occurs)
                    stationName = segmentTime.getEndStation().getName();
                    break;
                }
            }
            if (!stationName.equals(conflict.getSegmentId())) {
                break; // Found the station name
            }
        }
        
        System.out.println("   Station: " + stationName);
        System.out.println("   Type: " + conflict.getConflictType());
        System.out.println();
        System.out.println("   Explanation: Both trains are scheduled to use the same segment (single-track line)");
        System.out.println("   at overlapping times. This creates a conflict because only one train can use");
        System.out.println("   the segment at a time on a single-track railway.");
        
        // Add resolution info if available
        if (conflict.hasCrossingDetails()) {
            System.out.println();
            if (conflict.getCrossingStationId() != null) {
                System.out.println("   Crossing Station: " + conflict.getCrossingStationId());
            }
        }
        
        // Always show waiting train and wait time if available
        if (conflict.getWaitingTrainId() != null || conflict.getWaitTimeMinutes() != null) {
            if (!conflict.hasCrossingDetails()) {
                System.out.println();
            }
            if (conflict.getWaitingTrainId() != null) {
                System.out.println("   Waiting Train: " + conflict.getWaitingTrainId());
            }
            if (conflict.getWaitTimeMinutes() != null) {
                int hours = (int) (conflict.getWaitTimeMinutes() / 60);
                int minutes = (int) (conflict.getWaitTimeMinutes() % 60);
                if (hours > 0) {
                    System.out.printf("   Wait Time: %dh %dm%n", hours, minutes);
                } else {
                    System.out.printf("   Wait Time: %dm%n", minutes);
                }
            }
        }
        
        // Show operation details if available
        if (conflict.hasCrossingDetails()) {
            System.out.println();
            if (conflict.getResolution() == DispatchReport.Conflict.ResolutionType.CROSS_AT_STATION_WITH_SIDING) {
                // Find the other train and its destination station
                String otherTrainId = conflict.getTrain1Id().equals(conflict.getWaitingTrainId()) 
                    ? conflict.getTrain2Id() 
                    : conflict.getTrain1Id();
                String otherTrainDestinationStationName = "the destination station"; // Default
                
                // Find the destination station for the other train
                for (DispatchReport.TrainSchedule schedule : report.getTrainSchedules()) {
                    if (schedule.getTrainId().equals(otherTrainId)) {
                        // Find the segment where the conflict occurs for the other train
                        for (DispatchReport.TrainSchedule.SegmentTime segmentTime : schedule.getSegmentTimes()) {
                            if (segmentTime.getSegmentId().equals(conflict.getSegmentId())) {
                                // The destination station is the end station of this segment
                                otherTrainDestinationStationName = segmentTime.getEndStation().getName();
                                break;
                            }
                        }
                        break;
                    }
                }
                
                System.out.println("   Resolution: Train " + conflict.getWaitingTrainId() + 
                                 " must enter the siding at station " + conflict.getCrossingStationId() +
                                 " until Train " + otherTrainId + 
                                 " arrives at station " + otherTrainDestinationStationName + ".");
            } else if (conflict.getResolution() == DispatchReport.Conflict.ResolutionType.WAIT_AT_PREVIOUS_STATION) {
                // Find the previous station name for the waiting train and the destination station for the other train
                String previousStationName = "the previous station"; // Default
                String otherTrainId = null;
                String otherTrainDestinationStationName = "the destination station"; // Default
                
                if (conflict.getWaitingTrainId() != null) {
                    // Find the other train (the one that is not waiting)
                    otherTrainId = conflict.getTrain1Id().equals(conflict.getWaitingTrainId()) 
                        ? conflict.getTrain2Id() 
                        : conflict.getTrain1Id();
                    
                    // Find the waiting train's previous station and the other train's destination station
                    for (DispatchReport.TrainSchedule schedule : report.getTrainSchedules()) {
                        if (schedule.getTrainId().equals(conflict.getWaitingTrainId())) {
                            // Find the segment where the conflict occurs for the waiting train
                            for (DispatchReport.TrainSchedule.SegmentTime segmentTime : schedule.getSegmentTimes()) {
                                if (segmentTime.getSegmentId().equals(conflict.getSegmentId())) {
                                    // The previous station is the start station of this segment
                                    previousStationName = segmentTime.getStartStation().getName();
                                    break;
                                }
                            }
                        } else if (schedule.getTrainId().equals(otherTrainId)) {
                            // Find the segment where the conflict occurs for the other train
                            for (DispatchReport.TrainSchedule.SegmentTime segmentTime : schedule.getSegmentTimes()) {
                                if (segmentTime.getSegmentId().equals(conflict.getSegmentId())) {
                                    // The destination station is the end station of this segment
                                    otherTrainDestinationStationName = segmentTime.getEndStation().getName();
                                    break;
                                }
                            }
                        }
                    }
                }
                
                System.out.println("   Resolution: Train " + conflict.getWaitingTrainId() + 
                                 " must wait at station " + previousStationName + 
                                 " until Train " + otherTrainId + 
                                 " arrives at station " + otherTrainDestinationStationName + ".");
            } else if (conflict.getResolution() == DispatchReport.Conflict.ResolutionType.UNSOLVABLE) {
                System.out.println("   Resolution: UNSOLVABLE - Both trains arrive simultaneously at the segment.");
                System.out.println("   Manual intervention required to reschedule one of the trains.");
            }
        }
        System.out.println();
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
        
        if (locomotives.isEmpty()) {
            System.out.println("No locomotives loaded.");
        } else {
            System.out.printf("Total locomotives: %d%n%n", locomotives.size());
            for (Locomotive locomotive : locomotives) {
                System.out.printf("ID: %s | Type: %s | Max Speed: %.0f km/h | Gauge: %.0fmm%n",
                                 locomotive.getId(), locomotive.getType(),
                                 locomotive.getMaxSpeedKmH(), locomotive.getGauge());
            }
        }
    }

    /**
     * Shows all available wagons.
     */
    private void showAvailableWagons() {
        System.out.println("\n" + StringUtils.repeat("-", 60));
        System.out.println("Available Wagons");
        System.out.println(StringUtils.repeat("-", 60));
        
        if (wagons.isEmpty()) {
            System.out.println("No wagons loaded from CSV.");
        } else {
            System.out.printf("Total wagons: %d%n%n", wagons.size());
            for (Wagon wagon : wagons) {
                System.out.printf("ID: %s | Empty Weight: %.0f kg | Max Load: %.0f kg | Total (loaded): %.0f kg%n",
                                 wagon.getWagonId(), wagon.getEmptyWeightKg(), 
                                 wagon.getMaxLoadWeightKg(), wagon.getFullyLoadedWeightKg());
            }
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
     * Displays the line scheme table (used internally, without header).
     * The data is reloaded from the file each time to ensure it's always up to date.
     */
    private void displayLineScheme() {
        try {
            // Reload line segments from CSV to ensure data is up to date
            RailwayDataLoader loader = new RailwayDataLoader();
            loader.loadStations("train_station_dataset/facility.csv");
            loader.loadLineSegments("train_station_dataset/line.csv");
            
            List<LineSegment> lineSegments = loader.getLineSegments();
            List<Facility> stations = loader.getStations();
            
            if (lineSegments.isEmpty()) {
                return;
            }
            
            // Display line segments in order
            // Calculate total table width: 5 + 1 + 40 + 1 + 12 + 1 + 12 + 1 + 12 + 1 + 10 = 96
            int tableWidth = 5 + 1 + 40 + 1 + 12 + 1 + 12 + 1 + 12 + 1 + 10;
            System.out.println(StringUtils.repeat("-", tableWidth));
            System.out.println("LINE SEGMENTS:");
            System.out.println(StringUtils.repeat("-", tableWidth));
            System.out.printf("%-5s %-40s %-12s %-12s %-12s %-10s%n", 
                            "ID", "Name", "Start ID", "End ID", "Length (km)", "Gauge (mm)");
            System.out.println(StringUtils.repeat("-", tableWidth));
            
            for (LineSegment segment : lineSegments) {
                String name = segment.getName();
                if (name.length() > 40) {
                    name = name.substring(0, 37) + "...";
                }
                System.out.printf("%-5s %-40s %-12s %-12s %-12.2f %-10.0f%n",
                                segment.getId(),
                                name,
                                segment.getStartStation().getId(),
                                segment.getEndStation().getId(),
                                segment.getLengthKm(),
                                segment.getGauge());
            }
            
            System.out.println(StringUtils.repeat("-", tableWidth));
            System.out.println();
            
        } catch (Exception e) {
            // Silently fail - don't show error when displaying scheme before route selection
        }
    }

    /**
     * Shows the line scheme (network diagram) loaded from line.csv.
     * The data is reloaded from the file each time to ensure it's always up to date.
     */
    private void showLineScheme() {
        System.out.println("\n" + StringUtils.repeat("=", 80));
        System.out.println("RAILWAY LINE SCHEME");
        System.out.println(StringUtils.repeat("=", 80));
        System.out.println("(Data loaded from line.csv)");
        System.out.println();
        
        try {
            // Reload line segments from CSV to ensure data is up to date
            RailwayDataLoader loader = new RailwayDataLoader();
            loader.loadStations("train_station_dataset/facility.csv");
            loader.loadLineSegments("train_station_dataset/line.csv");
            
            List<LineSegment> lineSegments = loader.getLineSegments();
            List<Facility> stations = loader.getStations();
            
            if (lineSegments.isEmpty()) {
                System.out.println("No line segments found in line.csv.");
                System.out.println();
                return;
            }
            
            System.out.println("Total line segments: " + lineSegments.size());
            System.out.println("Total stations: " + stations.size());
            System.out.println();
            
            // Display line segments in order
            // Calculate total table width: 5 + 1 + 40 + 1 + 12 + 1 + 12 + 1 + 12 + 1 + 10 = 96
            int tableWidth = 5 + 1 + 40 + 1 + 12 + 1 + 12 + 1 + 12 + 1 + 10;
            System.out.println(StringUtils.repeat("-", tableWidth));
            System.out.println("LINE SEGMENTS:");
            System.out.println(StringUtils.repeat("-", tableWidth));
            System.out.printf("%-5s %-40s %-12s %-12s %-12s %-10s%n", 
                            "ID", "Name", "Start ID", "End ID", "Length (km)", "Gauge (mm)");
            System.out.println(StringUtils.repeat("-", tableWidth));
            
            for (LineSegment segment : lineSegments) {
                String name = segment.getName();
                if (name.length() > 40) {
                    name = name.substring(0, 37) + "...";
                }
                System.out.printf("%-5s %-40s %-12s %-12s %-12.2f %-10.0f%n",
                                segment.getId(),
                                name,
                                segment.getStartStation().getId(),
                                segment.getEndStation().getId(),
                                segment.getLengthKm(),
                                segment.getGauge());
            }
            
            System.out.println(StringUtils.repeat("-", tableWidth));
            
        } catch (Exception e) {
            System.out.println("ERROR: Failed to load line scheme from line.csv: " + e.getMessage());
            System.out.println("Please ensure the file exists and is properly formatted.");
            System.out.println();
        }
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

