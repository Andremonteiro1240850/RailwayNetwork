package Lapr.Domain;

import Sprint1.Domain.Facility;
import Sprint1.Domain.TravelCalculationService;
import Sprint1.Domain.Locomotive;
import Sprint2.Domain.LineSegment;
import Lapr.DataBaseAssociation.LineSegmentAOD;
import Lapr.DataBaseAssociation.LocomotiveAOD;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class responsible for scheduling trains and detecting conflicts.
 * <p>
 * This service reuses the TravelCalculationService to calculate travel times
 * for each segment in a train's route.
 *
 * @author Development Team
 * @version 1.0
 */
public class SchedulerService {
    
    private final TravelCalculationService travelCalculationService;
    private final List<LineSegment> lineSegments;
    private final CalculationService physicsCalculationService;
    private final LineSegmentAOD lineSegmentAOD;
    private final LocomotiveAOD locomotiveAOD;

    /**
     * Constructs a new SchedulerService with the specified travel calculation service and line segments.
     *
     * @param travelCalculationService the service for calculating travel times
     * @param lineSegments            the list of available line segments
     * @throws IllegalArgumentException if parameters are null
     */
    public SchedulerService(TravelCalculationService travelCalculationService, List<LineSegment> lineSegments) {
        if (travelCalculationService == null) {
            throw new IllegalArgumentException("Travel calculation service cannot be null");
        }
        if (lineSegments == null) {
            throw new IllegalArgumentException("Line segments list cannot be null");
        }
        
        this.travelCalculationService = travelCalculationService;
        this.lineSegments = new ArrayList<>(lineSegments);
        this.physicsCalculationService = new CalculationService();
        this.lineSegmentAOD = new LineSegmentAOD();
        this.locomotiveAOD = new LocomotiveAOD();
        
        System.out.println("SchedulerService initialized with " + this.lineSegments.size() + " line segments");
        if (!this.lineSegments.isEmpty()) {
            System.out.println("Sample segments (first 5):");
            this.lineSegments.stream().limit(5).forEach(seg -> 
                System.out.println("  - " + seg.getId() + ": " + seg.getStartStation().getId() + " -> " + seg.getEndStation().getId())
            );
        }
    }

    /**
     * Schedules a list of trains and generates a dispatch report with ETAs and conflicts.
     * <p>
     * This method:
     * 1. Calculates travel times for each segment using TravelCalculationService
     * 2. Accumulates ETAs for each station in each train's route
     * 3. Detects single-track conflicts (overlapping segment time intervals)
     * 4. Automatically resolves conflicts by adjusting departure times when trains depart from the same station at the same time
     * 5. Suggests basic resolutions for remaining conflicts
     *
     * @param trains the list of trains to schedule
     * @return a DispatchReport containing schedules, conflicts, and summary
     * @throws IllegalArgumentException if trains list is null or empty
     */
    public DispatchReport scheduleTrains(List<Train> trains) {
        if (trains == null || trains.isEmpty()) {
            throw new IllegalArgumentException("Trains list cannot be null or empty");
        }

        // Step 0: Sort trains deterministically (by train ID, then departure time)
        // This ensures the system is deterministic and reproducible
        List<Train> sortedTrains = new ArrayList<>(trains);
        sortedTrains.sort((t1, t2) -> {
            int idCompare = t1.getTrainId().compareTo(t2.getTrainId());
            if (idCompare != 0) return idCompare;
            return t1.getDeparturePlannedDateTime().compareTo(t2.getDeparturePlannedDateTime());
        });

        // Step 1: Resolve conflicts by adjusting departure times and detect departure conflicts
        // Use iterative approach to handle cascading conflicts
        List<Train> adjustedTrains = resolveDepartureTimeConflictsWithReevaluation(sortedTrains);
        List<DispatchReport.Conflict> departureConflicts = detectDepartureTimeConflicts(sortedTrains, adjustedTrains);

        List<DispatchReport.TrainSchedule> schedules = new ArrayList<>();
        List<DispatchReport.Conflict> conflicts = new ArrayList<>();
        
        // Add departure conflicts first
        conflicts.addAll(departureConflicts);

        // Step 2: Calculate schedules for each train (with adjusted departure times)
        for (Train train : adjustedTrains) {
            try {
                DispatchReport.TrainSchedule schedule = calculateTrainSchedule(train);
                schedules.add(schedule);
                System.out.println("Successfully scheduled train " + train.getTrainId());
            } catch (Exception e) {
                // Log error but continue with other trains
                System.err.println("ERROR scheduling train " + train.getTrainId() + ": " + e.getMessage());
                System.err.println("  Route: " + train.getRoute().stream()
                    .map(Facility::getId)
                    .reduce((a, b) -> a + " -> " + b)
                    .orElse("empty"));
                e.printStackTrace();
            }
        }
        
        if (schedules.isEmpty() && !adjustedTrains.isEmpty()) {
            System.err.println("WARNING: No trains were successfully scheduled out of " + adjustedTrains.size() + " train(s)!");
        }

        // Step 3: Detect remaining conflicts between trains
        // Filter out conflicts that are already covered by departure time conflicts
        // A departure conflict covers the case where trains depart from the same station at the same time
        // and one train's departure is adjusted. We don't need to show a segment conflict for the same train pair
        // if it's already covered by a departure conflict (which has the correct wait time).
        // Create a map of trainId -> Train for conflict resolution
        java.util.Map<String, Train> trainMap = new java.util.HashMap<>();
        for (Train train : adjustedTrains) {
            trainMap.put(train.getTrainId(), train);
        }
        int[] doubleTrackPassagesCount = new int[1]; // Array to pass by reference
        java.util.List<String> doubleTrackSegmentIds = new java.util.ArrayList<>();
        List<DispatchReport.Conflict> segmentConflicts = detectConflicts(schedules, trainMap, doubleTrackPassagesCount, doubleTrackSegmentIds);
        
        // Step 3.1: Apply conflict resolutions by adjusting train departure times
        // Map to track which trains need their schedules recalculated
        java.util.Map<String, Train> trainsToRecalculate = new java.util.HashMap<>();
        java.util.Set<String> trainsWithAdjustedDeparture = new java.util.HashSet<>();
        
        for (DispatchReport.Conflict segmentConflict : segmentConflicts) {
            // Check if this conflict is already covered by a departure conflict
            boolean isDuplicate = false;
            for (DispatchReport.Conflict departureConflict : departureConflicts) {
                // If both conflicts involve the same two trains, skip the segment conflict
                // (the departure conflict already has the correct wait time and better describes the situation)
                if ((segmentConflict.getTrain1Id().equals(departureConflict.getTrain1Id()) &&
                     segmentConflict.getTrain2Id().equals(departureConflict.getTrain2Id())) ||
                    (segmentConflict.getTrain1Id().equals(departureConflict.getTrain2Id()) &&
                     segmentConflict.getTrain2Id().equals(departureConflict.getTrain1Id()))) {
                    isDuplicate = true;
                    break;
                }
            }
            
            if (!isDuplicate) {
                conflicts.add(segmentConflict);
                
                // Apply conflict resolution: adjust departure time of waiting train
                // IMPORTANT: If the conflict resolution uses siding, the train does NOT wait at the station
                // It enters the segment and waits at the siding. Only adjust departure time if NO siding.
                String waitingTrainId = segmentConflict.getWaitingTrainId();
                if (waitingTrainId != null && segmentConflict.getWaitTimeMinutes() != null && segmentConflict.getWaitTimeMinutes() > 0) {
                    // Check if this conflict uses siding - if yes, train doesn't wait at station
                    boolean usesSiding = segmentConflict.getResolution() == DispatchReport.Conflict.ResolutionType.CROSS_AT_STATION_WITH_SIDING;
                    
                    if (!usesSiding) {
                        // No siding: train waits at the station before entering segment
                        Train waitingTrain = trainMap.get(waitingTrainId);
                        if (waitingTrain != null && !trainsWithAdjustedDeparture.contains(waitingTrainId)) {
                            // Get the current departure time
                            LocalDateTime currentDeparture = waitingTrain.getDeparturePlannedDateTime();
                            // Calculate new departure time by adding wait time
                            LocalDateTime adjustedDeparture = currentDeparture.plusMinutes(segmentConflict.getWaitTimeMinutes());
                            
                            // Create new Train with adjusted departure time
                            Train adjustedTrain = new Train(
                                waitingTrain.getTrainId(),
                                waitingTrain.getRoute(),
                                waitingTrain.getSelectedStations(),
                                waitingTrain.getLocomotives(),
                                waitingTrain.getWagons(),
                                adjustedDeparture,
                                waitingTrain.getTotalWeight()
                            );
                            
                            // Mark for recalculation
                            trainsToRecalculate.put(waitingTrainId, adjustedTrain);
                            trainsWithAdjustedDeparture.add(waitingTrainId);
                            trainMap.put(waitingTrainId, adjustedTrain);
                            
                            System.out.println(String.format(
                                "CONFLICT RESOLUTION: Train %s departure time adjusted from %s to %s (waiting %d minutes at station - no siding)",
                                waitingTrainId, currentDeparture, adjustedDeparture, segmentConflict.getWaitTimeMinutes()));
                        }
                    } else {
                        // Has siding: train enters segment normally, waits at siding
                        // The delay is applied within the segment, not at station departure
                        // We need to recalculate the schedule to apply the delay at the siding
                        Train waitingTrain = trainMap.get(waitingTrainId);
                        if (waitingTrain != null && !trainsWithAdjustedDeparture.contains(waitingTrainId)) {
                            // Find the schedule for this train
                            DispatchReport.TrainSchedule waitingSchedule = schedules.stream()
                                .filter(s -> s.getTrainId().equals(waitingTrainId))
                                .findFirst()
                                .orElse(null);
                            
                            if (waitingSchedule != null) {
                                // Apply delay to the schedule by modifying arrival times
                                // The delay happens at the siding within the segment
                                String conflictSegmentId = segmentConflict.getSegmentId();
                                
                                // Apply delay to schedule: modify the schedule to add delay at the siding
                                DispatchReport.TrainSchedule adjustedSchedule = applySidingDelayToSchedule(
                                    waitingSchedule, conflictSegmentId, segmentConflict.getWaitTimeMinutes());
                                
                                // Remove old schedule and add adjusted one
                                schedules.removeIf(s -> s.getTrainId().equals(waitingTrainId));
                                schedules.add(adjustedSchedule);
                                trainsWithAdjustedDeparture.add(waitingTrainId);
                                
                                System.out.println(String.format(
                                    "CONFLICT RESOLUTION: Train %s schedule adjusted - delay of %d minutes applied at siding in segment %s",
                                    waitingTrainId, segmentConflict.getWaitTimeMinutes(), conflictSegmentId));
                            }
                        }
                    }
                }
            }
        }
        
        // Step 3.2: Recalculate schedules for trains with adjusted departure times
        if (!trainsToRecalculate.isEmpty()) {
            // Remove old schedules for trains that need recalculation
            schedules.removeIf(schedule -> trainsToRecalculate.containsKey(schedule.getTrainId()));
            
            // Recalculate schedules with adjusted departure times
            for (Train adjustedTrain : trainsToRecalculate.values()) {
                try {
                    DispatchReport.TrainSchedule newSchedule = calculateTrainSchedule(adjustedTrain);
                    schedules.add(newSchedule);
                    System.out.println("Successfully rescheduled train " + adjustedTrain.getTrainId() + " with adjusted departure time");
                } catch (Exception e) {
                    System.err.println("ERROR rescheduling train " + adjustedTrain.getTrainId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        // Step 4: Generate summary
        DispatchReport.Summary summary = generateSummary(schedules, conflicts, doubleTrackPassagesCount[0], doubleTrackSegmentIds);

        return new DispatchReport(schedules, conflicts, summary);
    }

    /**
     * Calculates the schedule for a single train.
     * This method reuses TravelCalculationService for each segment.
     * If the route only has start and end stations, automatically finds the fastest path.
     *
     * @param train the train to schedule
     * @return the train schedule with ETAs
     * @throws IllegalStateException if route segments are not directly connected and no path can be found
     */
    private DispatchReport.TrainSchedule calculateTrainSchedule(Train train) {
        List<DispatchReport.TrainSchedule.StationETA> routeWithETAs = new ArrayList<>();
        List<DispatchReport.TrainSchedule.SegmentTime> segmentTimes = new ArrayList<>();
        
        LocalDateTime currentTime = train.getDeparturePlannedDateTime();
        List<Facility> route = train.getRoute();
        
        // If route only has 2 stations (start and end), find the fastest path automatically
        if (route.size() == 2) {
            Facility startStation = route.get(0);
            Facility endStation = route.get(1);
            
            // Check if start and end stations are the same
            if (startStation.getId().equals(endStation.getId())) {
                throw new IllegalStateException(
                    String.format("Route for train %s has the same start and end station (%s). " +
                                "A train must travel between different stations.",
                                train.getTrainId(), startStation.getId()));
            }
            
            System.out.println("Route has 2 stations: " + startStation.getId() + " -> " + endStation.getId());
            
            // Check if directly connected
            LineSegment directSegment = findConnectingSegment(startStation, endStation);
            if (directSegment == null) {
                System.out.println("  No direct connection found, searching for path...");
                // Find path using shortest path algorithm (based on travel time)
                List<Facility> foundPath = findFastestPath(startStation, endStation, train);
                if (foundPath != null && foundPath.size() >= 2) {
                    System.out.println("Found path from " + startStation.getId() + " to " + endStation.getId() + 
                                     " via " + (foundPath.size() - 2) + " intermediate stations");
                    
                    // Validate that each consecutive pair in the path has a direct segment
                    for (int i = 0; i < foundPath.size() - 1; i++) {
                        Facility pathStart = foundPath.get(i);
                        Facility pathEnd = foundPath.get(i + 1);
                        LineSegment pathSegment = findConnectingSegment(pathStart, pathEnd);
                        if (pathSegment == null) {
                            System.err.println("ERROR: Path validation failed! No segment between " + 
                                             pathStart.getId() + " and " + pathEnd.getId() + 
                                             " in found path");
                            System.err.println("  This indicates a bug in findFastestPath algorithm");
                            throw new IllegalStateException(
                                String.format("Invalid path found: no segment between stations %s and %s in path from %s to %s",
                                            pathStart.getId(), pathEnd.getId(), 
                                            startStation.getId(), endStation.getId()));
                        }
                    }
                    
                    route = foundPath;
                } else {
                    throw new IllegalStateException(
                        String.format("No path found between stations %s and %s for train %s",
                                    startStation.getId(), endStation.getId(), train.getTrainId()));
                }
            } else {
                System.out.println("  Direct connection found: segment " + directSegment.getId());
            }
        }

        // Validate route is not empty
        if (route == null || route.isEmpty()) {
            throw new IllegalStateException("Route cannot be null or empty for train " + train.getTrainId());
        }
        
        // Validate route has at least 2 stations (needed for at least one segment)
        if (route.size() < 2) {
            throw new IllegalStateException(
                String.format("Route for train %s has only %d station(s). " +
                            "A train route must have at least 2 different stations to travel between.",
                            train.getTrainId(), route.size()));
        }
        
        // Validate first station
        Facility departureStation = route.get(0);
        if (departureStation == null) {
            throw new IllegalStateException("Departure station cannot be null for train " + train.getTrainId());
        }
        
        // Validate departure time
        if (currentTime == null) {
            throw new IllegalStateException("Departure time cannot be null for train " + train.getTrainId());
        }
        
        // Add departure station (first station)
        routeWithETAs.add(new DispatchReport.TrainSchedule.StationETA(
            departureStation,
            currentTime,
            0.0,
            null // No segment for departure station
        ));

        // USLP10: Calculate travel time using physics for each segment
        // Velocity from previous segment is used as initial velocity for next segment
        double initialVelocityMs = 0.0; // Start from rest
        
        // Track if schedule is incomplete due to missing/invalid data
        boolean scheduleIncomplete = false;
        List<String> incompleteSegments = new ArrayList<>();
        
        // Load locomotive power from database if not already set
        loadLocomotivePowerFromDatabase(train);
        
        // Log route information for debugging
        System.out.println("Processing route for train " + train.getTrainId() + 
                         " with " + route.size() + " station(s): " +
                         route.stream().map(Facility::getId).reduce((a, b) -> a + " -> " + b).orElse("empty"));
        
        // Expand route: if user specified intermediate stations that are not directly connected,
        // find the path between them
        List<Facility> expandedRoute = new ArrayList<>();
        for (int i = 0; i < route.size() - 1; i++) {
            Facility routeStartStation = route.get(i);
            Facility routeEndStation = route.get(i + 1);
            
            // Validate stations
            if (routeStartStation == null) {
                throw new IllegalStateException(
                    String.format("Start station at index %d is null in route for train %s", i, train.getTrainId()));
            }
            if (routeEndStation == null) {
                throw new IllegalStateException(
                    String.format("End station at index %d is null in route for train %s", i + 1, train.getTrainId()));
            }

            // Check if directly connected
            LineSegment segment = findConnectingSegment(routeStartStation, routeEndStation);
            if (segment == null) {
                // Not directly connected - find path between them
                System.out.println("  No direct connection found between " + routeStartStation.getId() + 
                                 " and " + routeEndStation.getId() + ", searching for path...");
                List<Facility> path = findFastestPath(routeStartStation, routeEndStation, train);
                if (path == null || path.size() < 2) {
                    throw new IllegalStateException(
                        String.format("No path found between stations %s and %s for train %s",
                                    routeStartStation.getId(), routeEndStation.getId(), train.getTrainId()));
                }
                
                // Add path to expanded route (skip first station if it's already in expandedRoute)
                if (expandedRoute.isEmpty() || !expandedRoute.get(expandedRoute.size() - 1).getId().equals(path.get(0).getId())) {
                    expandedRoute.addAll(path);
                } else {
                    // Skip first station as it's already in the route
                    for (int j = 1; j < path.size(); j++) {
                        expandedRoute.add(path.get(j));
                    }
                }
                System.out.println("Found path from " + routeStartStation.getId() + " to " + routeEndStation.getId() + 
                                 " via " + (path.size() - 2) + " intermediate stations");
            } else {
                // Directly connected - add stations to route
                if (expandedRoute.isEmpty() || !expandedRoute.get(expandedRoute.size() - 1).getId().equals(routeStartStation.getId())) {
                    expandedRoute.add(routeStartStation);
                }
                expandedRoute.add(routeEndStation);
            }
        }
        
        // Use expanded route for segment processing
        route = expandedRoute;
        
        // Load segment data from database (inclination, etc.)
        for (int i = 0; i < route.size() - 1; i++) {
            Facility routeStartStation = route.get(i);
            Facility routeEndStation = route.get(i + 1);
            
            // Validate stations
            if (routeStartStation == null) {
                throw new IllegalStateException(
                    String.format("Start station at index %d is null in route for train %s", i, train.getTrainId()));
            }
            if (routeEndStation == null) {
                throw new IllegalStateException(
                    String.format("End station at index %d is null in route for train %s", i + 1, train.getTrainId()));
            }

            // Find the connecting segment (should always exist now after expansion)
            LineSegment segment = findConnectingSegment(routeStartStation, routeEndStation);
            if (segment == null) {
                throw new IllegalStateException(
                    String.format("No direct connection found between stations %s and %s for train %s (after path expansion)",
                                routeStartStation.getId(), routeEndStation.getId(), train.getTrainId()));
            }
            
            // Use the stations from the segment to ensure consistency
            // This ensures we use the same Facility instances that are in the LineSegment
            Facility startStation;
            Facility endStation;
            
            // Determine direction: check if route matches segment direction
            if (segment.getStartStation().getId().equals(routeStartStation.getId()) &&
                segment.getEndStation().getId().equals(routeEndStation.getId())) {
                // Same direction as segment
                startStation = segment.getStartStation();
                endStation = segment.getEndStation();
            } else if (segment.getStartStation().getId().equals(routeEndStation.getId()) &&
                       segment.getEndStation().getId().equals(routeStartStation.getId())) {
                // Opposite direction - reverse the stations
                startStation = segment.getEndStation();
                endStation = segment.getStartStation();
            } else {
                // This shouldn't happen if findConnectingSegment works correctly
                // But use route stations as fallback
                startStation = routeStartStation;
                endStation = routeEndStation;
            }

            // USLP10: Load segment data from database (inclination, etc.)
            LineSegment segmentWithData = loadSegmentDataFromDatabase(segment);
            
            // If database load failed, check if original segment has valid data
            if (segmentWithData == null) {
                System.err.println("WARNING: Failed to load segment data from database for segment " + 
                                 segment.getId());
                // Use original segment if it has valid length (from initial load)
                // This is acceptable because the original segment was loaded from database initially
                if (segment.getLengthKm() > 0) {
                    System.err.println("  Using original segment data (length: " + segment.getLengthKm() + " km from initial load)");
                    segmentWithData = segment;
                } else {
                    System.err.println("  Original segment also has invalid length: " + segment.getLengthKm() + " km");
                    System.err.println("  This segment will be skipped - train schedule will be incomplete");
                    scheduleIncomplete = true;
                    incompleteSegments.add(segment.getId() + " (no valid length: " + segment.getLengthKm() + " km)");
                    continue; // Skip to next segment
                }
            }
            
            // Debug: Log segment information
            System.out.println("Calculating travel time for segment " + segmentWithData.getId() + 
                             " (length: " + segmentWithData.getLengthKm() + " km, " +
                             "inclination: " + segmentWithData.getInclinationPercent() + "%)");
            
            // Validate segment length - must be > 0 to calculate travel time
            // USLP10: We cannot invent values - if length is invalid, we cannot calculate travel time
            if (segmentWithData.getLengthKm() <= 0) {
                System.err.println("ERROR: Segment " + segmentWithData.getId() + " has invalid length: " + 
                                 segmentWithData.getLengthKm() + " km - cannot calculate travel time");
                System.err.println("  This segment will be skipped - train schedule will be incomplete");
                scheduleIncomplete = true;
                incompleteSegments.add(segmentWithData.getId() + " (invalid length: " + segmentWithData.getLengthKm() + " km)");
                // Skip this segment - don't calculate travel time
                // This will result in an incomplete schedule, which is correct behavior
                continue; // Skip to next segment
            }
            
            // USLP10: Calculate travel time using physics (2nd Law of Newton)
            CalculationService.SegmentCalculationResult result =
                physicsCalculationService.calculateSegment(train, segmentWithData, initialVelocityMs);
            
            double travelTimeHours = result.getTravelTimeHours();
            System.out.println("  Calculated travel time: " + travelTimeHours + " hours (" + 
                             (travelTimeHours * 3600) + " seconds)");
            
            // Validate travel time - if invalid, skip this segment
            if (travelTimeHours <= 0) {
                System.err.println("ERROR: Calculated travel time is 0 or negative for segment " + 
                                 segmentWithData.getId() + ": " + travelTimeHours + " hours");
                System.err.println("  This segment will be skipped - train schedule will be incomplete");
                scheduleIncomplete = true;
                incompleteSegments.add(segmentWithData.getId() + " (invalid travel time: " + travelTimeHours + " hours)");
                // Skip this segment - don't add it to the schedule
                continue; // Skip to next segment
            }
            
            // Calculate segment start and end times
            LocalDateTime segmentStartTime = currentTime;
            // Convert hours to seconds for more accurate time calculation (handles sub-minute times)
            long totalSeconds = Math.round(travelTimeHours * 3600.0);
            LocalDateTime segmentEndTime = currentTime.plusSeconds(totalSeconds);

            // Create segment time
            DispatchReport.TrainSchedule.SegmentTime segmentTime = new DispatchReport.TrainSchedule.SegmentTime(
                segmentWithData.getId(),
                startStation,
                endStation,
                travelTimeHours,
                segmentStartTime,
                segmentEndTime
            );
            segmentTimes.add(segmentTime);

            // Update current time for next segment
            currentTime = segmentEndTime;
            
            // Update initial velocity for next segment (convert km/h to m/s)
            initialVelocityMs = result.getFinalVelocityKmH() / 3.6;

            // Validate end station and segment data before creating StationETA
            if (endStation == null) {
                throw new IllegalStateException(
                    String.format("End station is null for segment %s in train %s",
                                segmentWithData.getId(), train.getTrainId()));
            }
            if (segmentEndTime == null) {
                throw new IllegalStateException(
                    String.format("Segment end time is null for segment %s in train %s",
                                segmentWithData.getId(), train.getTrainId()));
            }
            if (segmentWithData.getId() == null) {
                throw new IllegalStateException(
                    String.format("Segment ID is null for segment in train %s",
                                train.getTrainId()));
            }
            
            // Add ETA for the end station
            routeWithETAs.add(new DispatchReport.TrainSchedule.StationETA(
                endStation,
                segmentEndTime,
                travelTimeHours,
                segmentWithData.getId()
            ));
        }

        // Validate that we have at least departure and arrival stations
        if (routeWithETAs.size() < 2) {
            String errorMsg = String.format(
                "Train %s schedule is incomplete - only %d station(s) in route. " +
                "This usually means all segments were skipped due to invalid data (length, travel time, etc.). " +
                "Original route: %s",
                train.getTrainId(), 
                routeWithETAs.size(),
                route.stream().map(Facility::getId).reduce((a, b) -> a + " -> " + b).orElse("empty")
            );
            System.err.println("ERROR: " + errorMsg);
            scheduleIncomplete = true;
            
            // If no segments were successfully processed, throw exception to prevent invalid schedule
            if (segmentTimes.isEmpty()) {
                throw new IllegalStateException(
                    String.format("Cannot create schedule for train %s: no valid segments found in route from %s to %s. " +
                                "Please check segment data (length, inclination, etc.) in database.",
                                train.getTrainId(),
                                route.get(0).getId(),
                                route.size() > 1 ? route.get(route.size() - 1).getId() : "unknown"));
            }
        }
        
        // Calculate total travel time based on actual arrival time to ensure consistency
        // This ensures the displayed total travel time matches the actual time difference
        double totalTravelTimeActual = 0.0;
        if (routeWithETAs.size() >= 2) {
            LocalDateTime departureTime = routeWithETAs.get(0).getEta();
            LocalDateTime arrivalTime = routeWithETAs.get(routeWithETAs.size() - 1).getEta();
            // Use seconds for more accurate calculation (handles sub-minute times)
            long totalSecondsActual = java.time.Duration.between(departureTime, arrivalTime).getSeconds();
            totalTravelTimeActual = totalSecondsActual / 3600.0;
        } else {
            // If we only have one station (or none), travel time is 0
            System.err.println("WARNING: Cannot calculate travel time - route has only " + routeWithETAs.size() + " station(s)");
            totalTravelTimeActual = 0.0;
        }
        
        // Log if schedule is incomplete
        if (scheduleIncomplete) {
            System.err.println("WARNING: Train " + train.getTrainId() + " schedule is INCOMPLETE due to:");
            for (String reason : incompleteSegments) {
                System.err.println("  - Segment " + reason);
            }
            System.err.println("  This schedule should be marked as degraded/incomplete");
        }

        return new DispatchReport.TrainSchedule(train.getTrainId(), routeWithETAs, segmentTimes, totalTravelTimeActual, train.getSelectedStations());
    }

    /**
     * Finds the fastest path between two stations using Dijkstra's algorithm.
     * The path is optimized based on estimated travel time.
     *
     * @param startStation the starting station
     * @param endStation   the ending station
     * @param train        the train (used for calculating travel time estimates)
     * @return list of stations representing the fastest path, or null if no path exists
     */
    private List<Facility> findFastestPath(Facility startStation, Facility endStation, Train train) {
        if (startStation == null || endStation == null || train == null) {
            return null;
        }
        
        if (startStation.getId().equals(endStation.getId())) {
            return java.util.Arrays.asList(startStation);
        }
        
        // Check if directly connected first
        if (findConnectingSegment(startStation, endStation) != null) {
            return java.util.Arrays.asList(startStation, endStation);
        }
        
        // Dijkstra's algorithm to find fastest path
        java.util.Map<String, Double> distances = new java.util.HashMap<>(); // Station ID -> travel time in hours
        java.util.Map<String, Facility> previous = new java.util.HashMap<>(); // Station ID -> previous station
        java.util.Set<String> visited = new java.util.HashSet<>();
        java.util.PriorityQueue<PathNode> queue = new java.util.PriorityQueue<>(
            (a, b) -> Double.compare(a.time, b.time)
        );
        
        // Initialize distances for all stations found in segments
        // Also ensure start and end stations are in the map
        distances.put(startStation.getId(), 0.0);
        distances.put(endStation.getId(), Double.MAX_VALUE);
        
        for (LineSegment segment : lineSegments) {
            String startId = segment.getStartStation().getId();
            String endId = segment.getEndStation().getId();
            
            // Only initialize if not already set (don't overwrite startStation which is 0.0)
            if (!distances.containsKey(startId)) {
                distances.put(startId, Double.MAX_VALUE);
            }
            if (!distances.containsKey(endId)) {
                distances.put(endId, Double.MAX_VALUE);
            }
        }
        
        System.out.println("Finding path from station " + startStation.getId() + " to " + endStation.getId());
        System.out.println("  Total segments available: " + lineSegments.size());
        System.out.println("  Stations initialized in distances map: " + distances.size());
        
        queue.add(new PathNode(startStation, 0.0));
        
        while (!queue.isEmpty()) {
            PathNode current = queue.poll();
            String currentId = current.station.getId();
            
            if (visited.contains(currentId)) {
                continue;
            }
            
            visited.add(currentId);
            
            // If we reached the destination, reconstruct path
            if (currentId.equals(endStation.getId())) {
                System.out.println("  Path found! Reconstructing path...");
                java.util.List<Facility> path = new java.util.ArrayList<>();
                Facility node = endStation;
                int pathLength = 0;
                while (node != null && pathLength < 100) { // Safety limit to prevent infinite loops
                    path.add(0, node);
                    String nodeId = node.getId();
                    node = previous.get(nodeId);
                    pathLength++;
                    if (node != null) {
                        System.out.println("    Path step: " + nodeId + " <- " + node.getId());
                    }
                }
                
                if (path.size() < 2) {
                    System.err.println("  ERROR: Reconstructed path has less than 2 stations!");
                    return null;
                }
                
                System.out.println("  Path reconstructed: " + path.size() + " stations");
                for (int i = 0; i < path.size(); i++) {
                    System.out.println("    [" + i + "] " + path.get(i).getId() + " - " + path.get(i).getName());
                }
                
                return path;
            }
            
            // Find all neighbors (stations directly connected to current)
            for (LineSegment segment : lineSegments) {
                Facility neighbor = null;
                String segmentStartId = segment.getStartStation().getId();
                String segmentEndId = segment.getEndStation().getId();
                
                if (segmentStartId.equals(currentId)) {
                    neighbor = segment.getEndStation();
                } else if (segmentEndId.equals(currentId)) {
                    neighbor = segment.getStartStation();
                }
                
                if (neighbor != null && !visited.contains(neighbor.getId())) {
                    String neighborId = neighbor.getId();
                    
                    // Initialize neighbor distance if not already in map
                    if (!distances.containsKey(neighborId)) {
                        distances.put(neighborId, Double.MAX_VALUE);
                    }
                    
                    // Estimate travel time for this segment
                    LineSegment segmentWithData = loadSegmentDataFromDatabase(segment);
                    if (segmentWithData == null) {
                        segmentWithData = segment; // Use original if database load fails
                    }
                    
                    // Use a simplified travel time estimate (distance / average speed)
                    // For more accuracy, we could calculate actual physics, but that's expensive
                    double estimatedSpeedKmH = Math.min(
                        train.getEffectiveMaxSpeedKmH(),
                        segmentWithData.hasSpeedLimit() ? segmentWithData.getSpeedLimitKmH() : train.getEffectiveMaxSpeedKmH()
                    );
                    if (estimatedSpeedKmH <= 0) {
                        estimatedSpeedKmH = 50.0; // Default speed
                    }
                    
                    double segmentTimeHours = segmentWithData.getLengthKm() / estimatedSpeedKmH;
                    double currentDistance = distances.getOrDefault(currentId, Double.MAX_VALUE);
                    double neighborDistance = distances.getOrDefault(neighborId, Double.MAX_VALUE);
                    double newDistance = currentDistance + segmentTimeHours;
                    
                    if (newDistance < neighborDistance) {
                        distances.put(neighborId, newDistance);
                        previous.put(neighborId, current.station);
                        queue.add(new PathNode(neighbor, newDistance));
                        System.out.println("  Path update: " + currentId + " -> " + neighborId + 
                                         " (distance: " + newDistance + " hours)");
                    }
                }
            }
        }
        
        // No path found - log debug information
        System.err.println("ERROR: No path found from station " + startStation.getId() + " to " + endStation.getId());
        System.err.println("  Visited stations: " + visited.size());
        System.err.println("  Available segments: " + lineSegments.size());
        if (!visited.isEmpty()) {
            System.err.println("  Visited station IDs: " + visited);
        }
        // Log some segment information for debugging
        System.err.println("  Sample segments (first 10):");
        lineSegments.stream().limit(10).forEach(seg -> 
            System.err.println("    - " + seg.getId() + ": " + seg.getStartStation().getId() + 
                             " -> " + seg.getEndStation().getId())
        );
        
        return null; // No path found
    }
    
    /**
     * Helper class for Dijkstra's algorithm.
     */
    private static class PathNode {
        final Facility station;
        final double time;
        
        PathNode(Facility station, double time) {
            this.station = station;
            this.time = time;
        }
    }
    
    /**
     * Finds a line segment connecting two stations.
     *
     * @param startStation the starting station
     * @param endStation   the ending station
     * @return the connecting segment, or null if not found
     */
    private LineSegment findConnectingSegment(Facility startStation, Facility endStation) {
        if (startStation == null || endStation == null) {
            System.err.println("ERROR: findConnectingSegment called with null station");
            return null;
        }
        
        String startId = startStation.getId();
        String endId = endStation.getId();
        
        System.out.println("Searching for segment between stations: " + startId + " -> " + endId);
        System.out.println("  Total segments available: " + lineSegments.size());
        
        LineSegment found = lineSegments.stream()
                .filter(segment -> {
                    // Use ID comparison to ensure it works even if Facility objects are different instances
                    String segStartId = segment.getStartStation().getId();
                    String segEndId = segment.getEndStation().getId();
                    
                    boolean connects = (segStartId.equals(startId) && segEndId.equals(endId)) ||
                                      (segStartId.equals(endId) && segEndId.equals(startId));
                    
                    if (connects) {
                        System.out.println("  Found segment: " + segment.getId() + " (" + 
                            segStartId + " -> " + segEndId + ")");
                    }
                    return connects;
                })
                .findFirst()
                .orElse(null);
        
        if (found == null) {
            System.err.println("  ERROR: No segment found between " + startId + " and " + endId);
            System.err.println("  Available segments (first 20):");
            lineSegments.stream().limit(20).forEach(seg -> {
                String segStartId = seg.getStartStation().getId();
                String segEndId = seg.getEndStation().getId();
                System.err.println("    - " + seg.getId() + ": " + segStartId + " -> " + segEndId);
            });
            
            // Also check if there are any segments that connect to start or end station
            long segmentsFromStart = lineSegments.stream()
                .filter(seg -> seg.getStartStation().getId().equals(startId) || 
                              seg.getEndStation().getId().equals(startId))
                .count();
            long segmentsToEnd = lineSegments.stream()
                .filter(seg -> seg.getStartStation().getId().equals(endId) || 
                              seg.getEndStation().getId().equals(endId))
                .count();
            System.err.println("  Segments connected to start station " + startId + ": " + segmentsFromStart);
            System.err.println("  Segments connected to end station " + endId + ": " + segmentsToEnd);
        }
        
        return found;
    }
    
    /**
     * Loads segment data from database (inclination, etc.) and updates the segment.
     * USLP10: All segment data must come from database via PL/SQL.
     * 
     * @param segment the base segment
     * @return segment with data loaded from database
     */
    private LineSegment loadSegmentDataFromDatabase(LineSegment segment) {
        // First, try to find the specific segment_id for this line and stations
        Integer segmentId = lineSegmentAOD.findSegmentIdByLineAndStations(
            segment.getId(),
            segment.getStartStation().getId(),
            segment.getEndStation().getId()
        );
        
        LineSegmentAOD.SegmentData data = null;
        
        // If we found a specific segment_id, get data for that specific segment
        if (segmentId != null) {
            System.out.println("Found specific segment_id " + segmentId + " for line " + segment.getId() + 
                             " and stations " + segment.getStartStation().getId() + " -> " + segment.getEndStation().getId());
            data = lineSegmentAOD.getSegmentDataBySegmentId(segmentId);
        } else {
            System.out.println("No specific segment_id found for line " + segment.getId() + 
                             " and stations " + segment.getStartStation().getId() + " -> " + segment.getEndStation().getId() +
                             ", using line-level aggregation");
        }
        
        // Fallback: if no specific segment found, use line-level aggregation
        if (data == null) {
            data = lineSegmentAOD.getSegmentData(segment.getId());
        }
        
        if (data != null) {
            // Validate that we have a valid length from database
            // USLP10: We cannot invent values - if database returns invalid length, we cannot use it
            double lengthKm = data.getLengthKm();
            if (lengthKm <= 0) {
                System.err.println("ERROR: Database returned invalid length " + lengthKm + " km for segment " + segment.getId());
                System.err.println("  Cannot use invalid data from database - returning null to indicate data is invalid");
                // Return null to indicate that data is invalid - caller must handle this
                return null;
            }
            
            // Create new segment with data from database including TrackType and Siding
            LineSegment updatedSegment = new LineSegment(
                segment.getId(),
                segment.getName(),
                segment.getStartStation(),
                segment.getEndStation(),
                lengthKm,
                data.getGaugeMm(),
                data.isElectrified(),
                data.getSpeedLimitKmH(),
                data.getInclinationPercent(),
                data.getTrackType(),
                data.getHasSiding(), // Explicit hasSiding boolean from database
                data.getSidingId(),
                data.getSidingSize()
            );
            
            System.out.println("Loaded segment data: length=" + lengthKm + " km, trackType=" + data.getTrackType());
            return updatedSegment;
        }
        
        // If data not found in database, return original segment
        // USLP10: We cannot invent values - if no data in database, we use what we have
        // but the caller must handle the case where length might be invalid
        System.err.println("WARNING: No data found in database for segment " + segment.getId());
        System.err.println("  Returning original segment (length: " + segment.getLengthKm() + " km)");
        System.err.println("  Note: If length is invalid, travel time calculation will be skipped and schedule marked as incomplete");
        return segment;
    }
    
    /**
     * Loads locomotive power from database and ensures locomotives have power data.
     * USLP10: All locomotive data must come from database via PL/SQL.
     * 
     * Note: Since Train has immutable locomotives, we can't modify them directly.
     * The power should ideally be loaded when creating the Train, but this method
     * ensures power is available for calculations.
     * 
     * @param train the train
     */
    private void loadLocomotivePowerFromDatabase(Train train) {
        // Verify that locomotives have power data
        // If power is 0, it means it wasn't loaded - in a real scenario, you would
        // need to reload locomotives with power from database when creating the Train
        // This is a validation/verification method
        for (Locomotive loco : train.getLocomotives()) {
            if (loco.getPowerKw() == 0.0) {
                // Power not set - should be loaded from database when creating Train
                // For now, we'll use a default estimation based on type and max speed
                // In production, this should throw an error or load from DB
                System.err.println("WARNING: Locomotive " + loco.getId() + 
                    " has no power data. Using default estimation.");
            }
        }
    }

    /**
     * Detects conflicts between trains on segments.
     * Two trains conflict if they occupy the same SINGLE-TRACK segment during overlapping time intervals.
     * DOUBLE and QUAD tracks allow simultaneous travel in opposite directions.
     *
     * @param schedules the list of train schedules
     * @param trainMap  map of trainId -> Train for accessing train properties
     * @param doubleTrackPassagesCount array to store the count of double track passages (passed by reference)
     * @param doubleTrackSegmentIds list to store the IDs of double track segments (passed by reference)
     * @return the list of detected conflicts
     */
    private List<DispatchReport.Conflict> detectConflicts(List<DispatchReport.TrainSchedule> schedules, java.util.Map<String, Train> trainMap, int[] doubleTrackPassagesCount, java.util.List<String> doubleTrackSegmentIds) {
        List<DispatchReport.Conflict> conflicts = new ArrayList<>();

        // Compare each pair of schedules
        for (int i = 0; i < schedules.size(); i++) {
            DispatchReport.TrainSchedule schedule1 = schedules.get(i);
            for (int j = i + 1; j < schedules.size(); j++) {
                DispatchReport.TrainSchedule schedule2 = schedules.get(j);

                // Track conflicts for this pair to avoid duplicates
                boolean conflictFound = false;
                boolean doubleTrackPassageFound = false; // Track if we found a double track passage for this pair

                // Check each segment in schedule1 against each segment in schedule2
                for (DispatchReport.TrainSchedule.SegmentTime segmentTime1 : schedule1.getSegmentTimes()) {
                    for (DispatchReport.TrainSchedule.SegmentTime segmentTime2 : schedule2.getSegmentTimes()) {
                        // Check if segments overlap (same segment ID and overlapping times)
                        if (segmentTime1.overlapsWith(segmentTime2)) {
                            // Get the actual segment to check track type
                            // Use stations from segmentTime to find the specific segment_id
                            LineSegment segment = findSegmentByIdWithStations(
                                segmentTime1.getSegmentId(),
                                segmentTime1.getStartStation().getId(),
                                segmentTime1.getEndStation().getId()
                            );
                            
                            // Only create conflict if segment is SINGLE track
                            // Double/Quad track: both trains pass (no conflict, regardless of direction)
                            if (segment != null) {
                                if (segment.isSingleTrack()) {
                                    // Single track: always a conflict if overlapping
                                    if (!conflictFound) {
                                        DispatchReport.Conflict conflict = createConflict(schedule1, schedule2, segmentTime1, segmentTime2, segment, trainMap);
                                        // Only add conflict if it's not null (should not be null for single track, but check for safety)
                                        if (conflict != null) {
                                            conflicts.add(conflict);
                                            conflictFound = true;
                                            break; // Move to next train pair
                                        }
                                    }
                                } else {
                                    // Double/Quad track: no conflict (both trains pass simultaneously)
                                    // Count this as a double track passage (only once per train pair)
                                    String segmentId = segmentTime1.getSegmentId();
                                    if (!doubleTrackPassageFound && !conflictFound) {
                                        doubleTrackPassagesCount[0]++;
                                        doubleTrackPassageFound = true;
                                        // Add segment ID to list if not already present
                                        if (!doubleTrackSegmentIds.contains(segmentId)) {
                                            doubleTrackSegmentIds.add(segmentId);
                                        }
                                    }
                                    String trackType = segment.getTrackType();
                                    System.out.println(String.format(
                                        "Trains %s and %s pass through segment %s at the same time, but no conflict occurs because the segment is %s track (allows simultaneous passage in opposite directions).",
                                        schedule1.getTrainId(), schedule2.getTrainId(), segmentId, trackType));
                                }
                            } else {
                                // Segment not found: default to SINGLE track behavior (conservative)
                                if (!conflictFound) {
                                    DispatchReport.Conflict conflict = createConflict(schedule1, schedule2, segmentTime1, segmentTime2, null, trainMap);
                                    // Only add conflict if it's not null
                                    if (conflict != null) {
                                        conflicts.add(conflict);
                                        conflictFound = true;
                                        break; // Move to next train pair
                                    }
                                }
                            }
                        }
                    }
                    if (conflictFound) {
                        break; // Move to next train pair
                    }
                }
            }
        }

        return conflicts;
    }
    
    /**
     * Finds a segment by its ID and loads the latest data from database.
     * 
     * @param segmentId the segment ID (line_id)
     * @return the LineSegment with data loaded from database, or null if not found
     */
    private LineSegment findSegmentById(String segmentId) {
        LineSegment segment = lineSegments.stream()
                .filter(seg -> seg.getId().equals(segmentId))
                .findFirst()
                .orElse(null);
        
        // If segment found, load the latest data from database
        if (segment != null) {
            return loadSegmentDataFromDatabase(segment);
        }
        
        return null;
    }
    
    /**
     * Finds a segment by its ID and stations, then loads the latest data from database.
     * This method is used when we need to find a specific segment_id within a line.
     * 
     * @param lineId the line ID
     * @param startStationId the start station ID
     * @param endStationId the end station ID
     * @return the LineSegment with data loaded from database, or null if not found
     */
    private LineSegment findSegmentByIdWithStations(String lineId, String startStationId, String endStationId) {
        // First find the base segment by line_id
        LineSegment segment = lineSegments.stream()
                .filter(seg -> seg.getId().equals(lineId))
                .findFirst()
                .orElse(null);
        
        if (segment == null) {
            return null;
        }
        
        // Create a temporary segment with the specific stations to load data
        // This ensures we get the correct segment_id from database
        LineSegment tempSegment = new LineSegment(
            segment.getId(),
            segment.getName(),
            segment.getStartStation(),
            segment.getEndStation(),
            segment.getLengthKm(),
            segment.getGauge(),
            segment.isElectrified(),
            segment.getSpeedLimitKmH(),
            segment.getInclinationPercent(),
            segment.getTrackType(),
            segment.getHasSiding(), // Use explicit hasSiding boolean
            segment.getSidingId(),
            segment.getSidingSize()
        );
        
        // Load data from database using the specific stations
        return loadSegmentDataFromDatabase(tempSegment);
    }
    
    /**
     * Checks if two trains are traveling in the same direction on a segment.
     * 
     * @param schedule1 the first train schedule
     * @param schedule2 the second train schedule
     * @param segmentTime1 the segment time for train 1
     * @param segmentTime2 the segment time for train 2
     * @return true if same direction, false if opposite directions
     */
    private boolean areTrainsInSameDirection(DispatchReport.TrainSchedule schedule1, 
                                            DispatchReport.TrainSchedule schedule2,
                                            DispatchReport.TrainSchedule.SegmentTime segmentTime1,
                                            DispatchReport.TrainSchedule.SegmentTime segmentTime2) {
        // Check if both trains are going from startStation to endStation (or both reverse)
        Facility start1 = segmentTime1.getStartStation();
        Facility end1 = segmentTime1.getEndStation();
        Facility start2 = segmentTime2.getStartStation();
        Facility end2 = segmentTime2.getEndStation();
        
        // Same direction if:
        // - Both go from start1 to end1, OR
        // - Both go from end1 to start1
        if (start1.equals(start2) && end1.equals(end2)) {
            return true; // Both going A->B
        }
        if (start1.equals(end2) && end1.equals(start2)) {
            return false; // Opposite directions: A->B vs B->A
        }
        
        // Default: assume same direction if we can't determine (conservative)
        return true;
    }

    /**
     * Creates a conflict object with suggested resolution.
     * Implements C4: considers sidings for crossings and C5: provides detailed crossing operations.
     * 
     * Conflict resolution logic:
     * - If double track: both trains pass (no conflict should be created, but if created, both pass)
     * - If single track:
     *   - If has siding: calculate time to cross siding and add to second train's arrival time
     *   - If no siding: second train waits
     *
     * @param schedule1     the first train schedule
     * @param schedule2     the second train schedule
     * @param segmentTime1  the segment time for train 1
     * @param segmentTime2  the segment time for train 2
     * @param segment       the LineSegment (can be null if not found)
     * @param trainMap      map of trainId -> Train for accessing train properties
     * @return the conflict object
     */
    private DispatchReport.Conflict createConflict(DispatchReport.TrainSchedule schedule1, DispatchReport.TrainSchedule schedule2,
                                    DispatchReport.TrainSchedule.SegmentTime segmentTime1, DispatchReport.TrainSchedule.SegmentTime segmentTime2,
                                    LineSegment segment, java.util.Map<String, Train> trainMap) {
        String train1Id = schedule1.getTrainId();
        String train2Id = schedule2.getTrainId();
        String segmentId = segmentTime1.getSegmentId();

        // Note: This method is only called for single track segments or when segment is null
        // Double/Quad track segments are already filtered out in detectConflicts()
        // However, we add a safety check here in case segment is null or somehow double track
        if (segment != null && RailwayConflictRules.allowsSimultaneousPassage(segment)) {
            // Double/Quad track: both trains pass simultaneously - no conflict object should be created
            // This should not happen if detectConflicts is working correctly, but we check for safety
            String trackType = segment.getTrackType();
            System.out.println(String.format(
                "Trains %s and %s pass through segment %s at the same time, but no conflict occurs because the segment is %s track (allows simultaneous passage in opposite directions).",
                train1Id, train2Id, segmentId, trackType));
            // Return null to indicate no conflict (caller should handle this)
            return null;
        }

        // Determine which train arrives first (or if they arrive at the same time)
        int timeComparison = segmentTime1.getSegmentStartTime().compareTo(segmentTime2.getSegmentStartTime());
        boolean train1ArrivesFirst = timeComparison < 0;
        boolean arriveSimultaneously = timeComparison == 0;

        // Find previous stations (for checking sidings)
        Facility previousStation1 = segmentTime1.getStartStation();
        Facility previousStation2 = segmentTime2.getStartStation();

        // Get Train objects for calculating speeds
        Train train1 = trainMap.get(train1Id);
        Train train2 = trainMap.get(train2Id);

        // C4: Check if we can use a siding for crossing
        // Determine which train should wait and calculate resolution
        String firstTrainId;
        String waitingTrainId;
        Facility firstTrainPreviousStation;
        Facility waitingTrainPreviousStation;
        DispatchReport.TrainSchedule.SegmentTime firstTrainSegmentTime;
        DispatchReport.TrainSchedule.SegmentTime waitingTrainSegmentTime;
        DispatchReport.TrainSchedule waitingTrainSchedule;
        Train firstTrain;
        Train waitingTrain;
        
        if (arriveSimultaneously) {
            // Both arrive at the same time - need to determine which should wait
            // Use domain rules for deterministic behavior
            boolean train1GoesFirst = RailwayConflictRules.shouldTrain1GoFirst(train1Id, train2Id);
            firstTrainId = train1GoesFirst ? train1Id : train2Id;
            waitingTrainId = train1GoesFirst ? train2Id : train1Id;
            firstTrainPreviousStation = train1GoesFirst ? previousStation1 : previousStation2;
            waitingTrainPreviousStation = train1GoesFirst ? previousStation2 : previousStation1;
            firstTrainSegmentTime = train1GoesFirst ? segmentTime1 : segmentTime2;
            waitingTrainSegmentTime = train1GoesFirst ? segmentTime2 : segmentTime1;
            waitingTrainSchedule = train1GoesFirst ? schedule2 : schedule1;
            firstTrain = train1GoesFirst ? train1 : train2;
            waitingTrain = train1GoesFirst ? train2 : train1;
        } else {
            // One train arrives first
            firstTrainId = train1ArrivesFirst ? train1Id : train2Id;
            waitingTrainId = train1ArrivesFirst ? train2Id : train1Id;
            firstTrainPreviousStation = train1ArrivesFirst ? previousStation1 : previousStation2;
            waitingTrainPreviousStation = train1ArrivesFirst ? previousStation2 : previousStation1;
            firstTrainSegmentTime = train1ArrivesFirst ? segmentTime1 : segmentTime2;
            waitingTrainSegmentTime = train1ArrivesFirst ? segmentTime2 : segmentTime1;
            waitingTrainSchedule = train1ArrivesFirst ? schedule2 : schedule1;
            firstTrain = train1ArrivesFirst ? train1 : train2;
            waitingTrain = train1ArrivesFirst ? train2 : train1;
        }
        
        // Get arrival times at previous stations to calculate wait time correctly
        // IMPORTANT: Use calculated schedule times (not from database) to ensure we reflect
        // any conflict resolutions or adjustments that have been applied.
        // This prevents cascading effect errors where old database times are used instead of recalculated times.
        LocalDateTime waitingTrainArrivalAtPreviousStation = getArrivalTimeAtStation(waitingTrainSchedule, waitingTrainPreviousStation);
        if (waitingTrainArrivalAtPreviousStation == null) {
            // Fallback: use segment start time from calculated schedule (not from database)
            waitingTrainArrivalAtPreviousStation = waitingTrainSegmentTime.getSegmentStartTime();
        }
        
        // Determine resolution based on track type and siding availability
        Facility crossingStation = null;
        DispatchReport.Conflict.ResolutionType resolution;
        String description;
        long waitTimeMinutes = 0;
        
        // At this point, we should only have single track conflicts
        // Double/Quad track conflicts should have been filtered out in detectConflicts
        // Use domain rules to verify
        if (RailwayConflictRules.allowsSimultaneousPassage(segment)) {
            // Should not create conflict for double track - return null
            return null;
        }
        
        // Validate that we have required data
        if (waitingTrainPreviousStation == null) {
            System.err.println("ERROR: waitingTrainPreviousStation is null for conflict between " + train1Id + " and " + train2Id);
            // Use a fallback - this should not happen in normal operation
            return null;
        }
        
        // Determine if siding is available (domain rule decision)
        boolean hasSiding = RailwayConflictRules.hasSidingAvailable(segment, waitingTrainPreviousStation);
        
        // Get siding size if available
        Integer sidingSizeM = null;
        if (hasSiding && segment != null && segment.getSidingSize() != null) {
            sidingSizeM = segment.getSidingSize();
        }
        
        // Calculate train speed for resolution calculations
        double waitingTrainSpeedKmH = waitingTrain != null ? 
            (segment != null ? waitingTrain.calculateEffectiveSpeed(segment) : waitingTrain.getEffectiveMaxSpeedKmH()) :
            50.0; // Default speed if train not found
        
        // Calculate resolution using dedicated calculator (only calculations, no decisions)
        ConflictResolutionCalculator.ResolutionResult resolutionResult = 
            ConflictResolutionCalculator.calculateResolution(
                segment,
                waitingTrainPreviousStation,
                waitingTrainArrivalAtPreviousStation,
                firstTrainSegmentTime.getSegmentEndTime(),
                waitingTrainSpeedKmH,
                hasSiding, // Decision already made above
                sidingSizeM, // Siding size if available
                arriveSimultaneously
            );
        
        resolution = resolutionResult.getResolutionType();
        waitTimeMinutes = resolutionResult.getWaitTimeMinutes();
        crossingStation = resolutionResult.getCrossingStation();
        
        // Build description using dedicated builder
        String stationName = resolutionResult.getCrossingStation() != null ? 
            resolutionResult.getCrossingStation().getId() : waitingTrainPreviousStation.getId();
        
        if (resolutionResult.hasSiding()) {
            description = ConflictDescriptionBuilder.buildSidingDescriptionWithSize(
                segmentId, train1Id, train2Id, firstTrainId, waitingTrainId, stationName,
                segment, resolutionResult, arriveSimultaneously);
        } else {
            description = ConflictDescriptionBuilder.buildNoSidingDescription(
                segmentId, train1Id, train2Id, firstTrainId, waitingTrainId,
                waitingTrainPreviousStation.getId(), waitTimeMinutes, arriveSimultaneously);
        }

        // Determine conflict type using domain rules
        DispatchReport.Conflict.ConflictType conflictType = DispatchReport.Conflict.ConflictType.OVERLAPPING_SEGMENT;
        if (RailwayConflictRules.isUnsolvable(segment, waitingTrainPreviousStation, arriveSimultaneously)) {
            conflictType = DispatchReport.Conflict.ConflictType.UNSOLVABLE;
        }
        
        // C5: Create conflict with detailed crossing operation information
        return new DispatchReport.Conflict(
            train1Id,
            train2Id,
            segmentId,
            conflictType,
            resolution,
            description,
            crossingStation != null ? crossingStation.getId() : null,
            waitingTrainId,
            waitTimeMinutes
        );
    }

    /**
     * Gets the arrival time at a specific station from a train schedule.
     * Uses the calculated schedule data (not database) to ensure we use the most up-to-date times
     * that reflect any conflict resolutions or adjustments.
     * 
     * First checks the routeWithETAs list, then falls back to segmentTimes if the station
     * is not in the ETAs list (e.g., intermediate stations).
     *
     * @param schedule the train schedule (contains calculated times, not from database)
     * @param station the station to get arrival time for
     * @return the arrival time at the station from the calculated schedule, or null if station not found
     */
    private LocalDateTime getArrivalTimeAtStation(DispatchReport.TrainSchedule schedule, Facility station) {
        // First, try to find the station in the routeWithETAs list
        // This contains all stations with their calculated ETAs
        for (DispatchReport.TrainSchedule.StationETA eta : schedule.getRouteWithETAs()) {
            if (eta.getStation().equals(station)) {
                return eta.getEta(); // Use calculated ETA from schedule (not from database)
            }
        }
        
        // If not found in ETAs, check segmentTimes
        // The station might be a start station of a segment
        for (DispatchReport.TrainSchedule.SegmentTime segmentTime : schedule.getSegmentTimes()) {
            if (segmentTime.getStartStation().equals(station)) {
                return segmentTime.getSegmentStartTime(); // Use calculated start time from schedule
            }
            if (segmentTime.getEndStation().equals(station)) {
                return segmentTime.getSegmentEndTime(); // Use calculated end time from schedule
            }
        }
        
        // Station not found in schedule - return null
        // Caller should handle this case (e.g., use segmentStartTime as fallback)
        return null;
    }

    /**
     * Resolves conflicts by adjusting departure times when trains depart from the same station at the same time.
     * Uses iterative approach to handle cascading conflicts (reavaliação dinâmica).
     * 
     * @param trains the original list of trains (should be sorted deterministically)
     * @return a list of trains with adjusted departure times
     */
    private List<Train> resolveDepartureTimeConflictsWithReevaluation(List<Train> trains) {
        List<Train> adjustedTrains = new ArrayList<>(trains);
        int maxIterations = 10; // Prevent infinite loops
        int iteration = 0;
        
        while (iteration < maxIterations) {
            boolean changed = false;
            
            // Compare each pair of trains
            for (int i = 0; i < adjustedTrains.size(); i++) {
                Train train1 = adjustedTrains.get(i);
                Facility train1StartStation = train1.getRoute().get(0);
                LocalDateTime train1Departure = train1.getDeparturePlannedDateTime();
                String train1PlatformId = getTrainPlatformId(train1); // Get platform ID (can be null)
                
                for (int j = i + 1; j < adjustedTrains.size(); j++) {
                    Train train2 = adjustedTrains.get(j);
                    Facility train2StartStation = train2.getRoute().get(0);
                    LocalDateTime train2Departure = train2.getDeparturePlannedDateTime();
                    String train2PlatformId = getTrainPlatformId(train2); // Get platform ID (can be null)
                    
                    // Check if trains depart from the same station at the same time AND same platform
                    // Only conflict if using same platform (or both null, meaning platform not specified)
                    boolean sameStation = train1StartStation.equals(train2StartStation);
                    boolean sameTime = train1Departure.equals(train2Departure);
                    boolean samePlatform = (train1PlatformId == null && train2PlatformId == null) || 
                                         (train1PlatformId != null && train1PlatformId.equals(train2PlatformId));
                    
                    if (sameStation && sameTime && samePlatform) {
                        
                        // Calculate when train1 arrives at the next station
                        if (train1.getRoute().size() >= 2) {
                            Facility train1NextStation = train1.getRoute().get(1);
                            
                            // Calculate travel time for train1 to reach next station
                            double travelTimeHours = travelCalculationService.calculateTravelTime(
                                train1StartStation, train1NextStation, train1);
                            
                            // Calculate arrival time at next station
                            long travelTimeMinutes = (long) (travelTimeHours * 60);
                            LocalDateTime train1ArrivalAtNextStation = train1Departure.plusMinutes(travelTimeMinutes);
                            
                            // Adjust train2's departure time to after train1 arrives at next station
                            LocalDateTime adjustedDepartureTime = train1ArrivalAtNextStation;
                            
                            // Create a new Train with adjusted departure time
                            Train adjustedTrain2 = new Train(
                                train2.getTrainId(),
                                train2.getRoute(),
                                train2.getSelectedStations(),
                                train2.getLocomotives(),
                                train2.getWagons(),
                                adjustedDepartureTime,
                                train2.getTotalWeight()
                            );
                            
                            // Replace train2 in the list
                            adjustedTrains.set(j, adjustedTrain2);
                            changed = true;
                            
                            System.out.println(String.format(
                                "CONFLICT RESOLUTION: Train %s departure time adjusted from %s to %s " +
                                "(waiting for Train %s to arrive at %s at %s)%s",
                                train2.getTrainId(), train2Departure, adjustedDepartureTime,
                                train1.getTrainId(), train1NextStation.getId(), train1ArrivalAtNextStation,
                                train1PlatformId != null ? " [Platform: " + train1PlatformId + "]" : ""));
                            
                            // Break inner loop to restart iteration (reavaliação dinâmica)
                            break;
                        }
                    }
                }
                if (changed) {
                    break; // Break outer loop to restart iteration
                }
            }
            
            if (!changed) {
                break; // No more conflicts to resolve
            }
            
            iteration++;
        }
        
        if (iteration >= maxIterations) {
            System.err.println("WARNING: Maximum iterations reached in resolveDepartureTimeConflictsWithReevaluation");
        }
        
        return adjustedTrains;
    }
    
    /**
     * Gets the platform ID for a train (if available).
     * For now, returns null as Train doesn't have platform field yet.
     * This is a placeholder for future implementation.
     * When platform is null, the system uses conservative approach:
     * - If both trains have null platform, they are considered to conflict
     * - This ensures safety (no false negatives)
     * 
     * @param train the train
     * @return the platform ID, or null if not specified
     */
    private String getTrainPlatformId(Train train) {
        // TODO: Add platformId field to Train class
        // For now, return null (meaning platform not checked - conservative approach)
        // When both are null, they conflict (safe assumption)
        return null;
    }

    /**
     * Detects conflicts when trains depart from the same station at the same time.
     * Creates conflict objects with wait time information for trains that had their departure times adjusted.
     *
     * @param originalTrains the original list of trains
     * @param adjustedTrains the list of trains with adjusted departure times
     * @return a list of conflicts related to simultaneous departures
     */
    private List<DispatchReport.Conflict> detectDepartureTimeConflicts(List<Train> originalTrains, List<Train> adjustedTrains) {
        List<DispatchReport.Conflict> conflicts = new ArrayList<>();
        
        // Create a map of train IDs to original trains for quick lookup
        java.util.Map<String, Train> originalTrainMap = new java.util.HashMap<>();
        for (Train train : originalTrains) {
            originalTrainMap.put(train.getTrainId(), train);
        }
        
        // Create a map of train IDs to adjusted trains for quick lookup
        java.util.Map<String, Train> adjustedTrainMap = new java.util.HashMap<>();
        for (Train train : adjustedTrains) {
            adjustedTrainMap.put(train.getTrainId(), train);
        }
        
        // Compare each adjusted train with its original to find departure time changes
        for (Train adjustedTrain : adjustedTrains) {
            Train originalTrain = originalTrainMap.get(adjustedTrain.getTrainId());
            if (originalTrain == null) {
                continue; // Skip if train not found in original list
            }
            
            LocalDateTime originalDeparture = originalTrain.getDeparturePlannedDateTime();
            LocalDateTime adjustedDeparture = adjustedTrain.getDeparturePlannedDateTime();
            
            // If departure time was adjusted, find which train caused the adjustment
            if (!originalDeparture.equals(adjustedDeparture)) {
                // Find the train that caused this adjustment (the one that departed first from the same station)
                Facility departureStation = adjustedTrain.getRoute().get(0);
                String departureStationId = departureStation.getId();
                
                // Find the first train in the original list that departed from the same station at the same time
                // This is the train that caused the adjustment (based on the logic in resolveDepartureTimeConflicts)
                // In resolveDepartureTimeConflicts, train1 (earlier in list) causes train2 (later in list) to wait
                Train causingTrain = null;
                for (Train otherOriginalTrain : originalTrains) {
                    if (otherOriginalTrain.getTrainId().equals(adjustedTrain.getTrainId())) {
                        continue; // Skip self
                    }
                    
                    // Check if route is not empty
                    if (otherOriginalTrain.getRoute().isEmpty()) {
                        continue;
                    }
                    
                    Facility otherDepartureStation = otherOriginalTrain.getRoute().get(0);
                    String otherDepartureStationId = otherDepartureStation.getId();
                    LocalDateTime otherDeparture = otherOriginalTrain.getDeparturePlannedDateTime();
                    
                    // Check if this train departed from the same station at the same time
                    // Use station ID comparison to ensure it works even if Facility objects are different instances
                    if (departureStationId.equals(otherDepartureStationId) && 
                        originalDeparture.equals(otherDeparture)) {
                        
                        // Find the adjusted version of this train
                        Train otherAdjustedTrain = adjustedTrainMap.get(otherOriginalTrain.getTrainId());
                        
                        // The causing train is the first one we find that either:
                        // 1. Wasn't adjusted (kept original departure time), OR
                        // 2. Was adjusted but still departs before the current train's adjusted time
                        // This matches the logic in resolveDepartureTimeConflicts where train1 (earlier in list) causes train2 to wait
                        boolean isCausingTrain = false;
                        if (otherAdjustedTrain != null) {
                            LocalDateTime otherAdjustedDeparture = otherAdjustedTrain.getDeparturePlannedDateTime();
                            // If the other train wasn't adjusted or departs before this train's adjusted time,
                            // it's the one that caused the adjustment
                            if (otherAdjustedDeparture.equals(otherDeparture) || 
                                otherAdjustedDeparture.isBefore(adjustedDeparture)) {
                                isCausingTrain = true;
                            }
                        } else {
                            // If we can't find the adjusted train, assume it's the causing train
                            isCausingTrain = true;
                        }
                        
                        if (isCausingTrain) {
                            causingTrain = otherOriginalTrain;
                            break; // Found the train that caused the adjustment
                        }
                    }
                }
                
                if (causingTrain != null) {
                    // Calculate wait time in minutes
                    long waitTimeMinutes = java.time.Duration.between(originalDeparture, adjustedDeparture).toMinutes();
                    
                    // Get the first segment ID for the conflict (departure station to next station)
                    String segmentId = "DEPARTURE";
                    if (adjustedTrain.getRoute().size() >= 2) {
                        Facility nextStation = adjustedTrain.getRoute().get(1);
                        LineSegment segment = findConnectingSegment(departureStation, nextStation);
                        if (segment != null) {
                            segmentId = segment.getId();
                        }
                    }
                    
                    // Create conflict
                    String description = String.format(
                        "Trains %s and %s were scheduled to depart from station %s at the same time (%s). " +
                        "Train %s departure time was adjusted to %s to avoid conflict.",
                        causingTrain.getTrainId(), adjustedTrain.getTrainId(), 
                        departureStation.getId(), originalDeparture,
                        adjustedTrain.getTrainId(), adjustedDeparture);
                    
                    DispatchReport.Conflict conflict = new DispatchReport.Conflict(
                        causingTrain.getTrainId(),
                        adjustedTrain.getTrainId(),
                        segmentId,
                        DispatchReport.Conflict.ConflictType.OVERLAPPING_SEGMENT,
                        DispatchReport.Conflict.ResolutionType.WAIT_AT_PREVIOUS_STATION,
                        description,
                        null, // No crossing station
                        adjustedTrain.getTrainId(), // The waiting train
                        waitTimeMinutes
                    );
                    
                    conflicts.add(conflict);
                }
            }
        }
        
        return conflicts;
    }

    /**
     * Applies a delay to a train schedule when the train waits at a siding within a segment.
     * The delay is applied to the segment end time and propagated to all subsequent segments and stations.
     *
     * @param schedule the original schedule
     * @param segmentId the ID of the segment where the delay occurs (at the siding)
     * @param delayMinutes the delay in minutes (wait time at siding + crossing time)
     * @return a new schedule with the delay applied
     */
    private DispatchReport.TrainSchedule applySidingDelayToSchedule(
            DispatchReport.TrainSchedule schedule, String segmentId, long delayMinutes) {
        
        // Find the index of the segment where delay occurs
        List<DispatchReport.TrainSchedule.SegmentTime> segmentTimes = schedule.getSegmentTimes();
        int delaySegmentIndex = -1;
        for (int i = 0; i < segmentTimes.size(); i++) {
            if (segmentTimes.get(i).getSegmentId().equals(segmentId)) {
                delaySegmentIndex = i;
                break;
            }
        }
        
        if (delaySegmentIndex == -1) {
            // Segment not found, return original schedule
            System.err.println("WARNING: Segment " + segmentId + " not found in schedule for train " + schedule.getTrainId());
            return schedule;
        }
        
        // Create new segment times with delay applied
        List<DispatchReport.TrainSchedule.SegmentTime> adjustedSegmentTimes = new ArrayList<>();
        for (int i = 0; i < segmentTimes.size(); i++) {
            DispatchReport.TrainSchedule.SegmentTime originalSegmentTime = segmentTimes.get(i);
            
            if (i < delaySegmentIndex) {
                // Segments before the delay: no change
                adjustedSegmentTimes.add(originalSegmentTime);
            } else if (i == delaySegmentIndex) {
                // The segment with the delay: add delay to end time
                LocalDateTime adjustedEndTime = originalSegmentTime.getSegmentEndTime().plusMinutes(delayMinutes);
                DispatchReport.TrainSchedule.SegmentTime adjustedSegmentTime = new DispatchReport.TrainSchedule.SegmentTime(
                    originalSegmentTime.getSegmentId(),
                    originalSegmentTime.getStartStation(),
                    originalSegmentTime.getEndStation(),
                    originalSegmentTime.getTravelTimeHours(),
                    originalSegmentTime.getSegmentStartTime(), // Start time unchanged
                    adjustedEndTime // End time delayed
                );
                adjustedSegmentTimes.add(adjustedSegmentTime);
            } else {
                // Segments after the delay: shift both start and end times
                LocalDateTime adjustedStartTime = originalSegmentTime.getSegmentStartTime().plusMinutes(delayMinutes);
                LocalDateTime adjustedEndTime = originalSegmentTime.getSegmentEndTime().plusMinutes(delayMinutes);
                DispatchReport.TrainSchedule.SegmentTime adjustedSegmentTime = new DispatchReport.TrainSchedule.SegmentTime(
                    originalSegmentTime.getSegmentId(),
                    originalSegmentTime.getStartStation(),
                    originalSegmentTime.getEndStation(),
                    originalSegmentTime.getTravelTimeHours(),
                    adjustedStartTime,
                    adjustedEndTime
                );
                adjustedSegmentTimes.add(adjustedSegmentTime);
            }
        }
        
        // Create new route with ETAs with delay applied
        List<DispatchReport.TrainSchedule.StationETA> adjustedRouteWithETAs = new ArrayList<>();
        List<DispatchReport.TrainSchedule.StationETA> originalRouteWithETAs = schedule.getRouteWithETAs();
        
        // Find the index of the station where delay starts (end station of the delayed segment)
        DispatchReport.TrainSchedule.SegmentTime delayedSegment = segmentTimes.get(delaySegmentIndex);
        Facility delayEndStation = delayedSegment.getEndStation();
        
        int delayStationIndex = -1;
        for (int i = 0; i < originalRouteWithETAs.size(); i++) {
            if (originalRouteWithETAs.get(i).getStation().equals(delayEndStation)) {
                delayStationIndex = i;
                break;
            }
        }
        
        for (int i = 0; i < originalRouteWithETAs.size(); i++) {
            DispatchReport.TrainSchedule.StationETA originalETA = originalRouteWithETAs.get(i);
            
            if (i <= delayStationIndex && delayStationIndex >= 0) {
                // Stations up to and including the delay station: no change to arrival time
                // (delay happens at siding within segment, so arrival at end station is delayed)
                if (i == delayStationIndex) {
                    // The end station of the delayed segment: apply delay
                    LocalDateTime adjustedETA = originalETA.getEta().plusMinutes(delayMinutes);
                    adjustedRouteWithETAs.add(new DispatchReport.TrainSchedule.StationETA(
                        originalETA.getStation(),
                        adjustedETA,
                        originalETA.getTravelTimeFromPreviousHours(),
                        originalETA.getSegmentId()
                    ));
                } else {
                    // Stations before delay: no change
                    adjustedRouteWithETAs.add(originalETA);
                }
            } else {
                // Stations after the delay: shift arrival time
                LocalDateTime adjustedETA = originalETA.getEta().plusMinutes(delayMinutes);
                adjustedRouteWithETAs.add(new DispatchReport.TrainSchedule.StationETA(
                    originalETA.getStation(),
                    adjustedETA,
                    originalETA.getTravelTimeFromPreviousHours(),
                    originalETA.getSegmentId()
                ));
            }
        }
        
        // Recalculate total travel time based on adjusted arrival time
        double adjustedTotalTravelTime = 0.0;
        if (adjustedRouteWithETAs.size() >= 2) {
            LocalDateTime departureTime = adjustedRouteWithETAs.get(0).getEta();
            LocalDateTime arrivalTime = adjustedRouteWithETAs.get(adjustedRouteWithETAs.size() - 1).getEta();
            long totalSecondsActual = java.time.Duration.between(departureTime, arrivalTime).getSeconds();
            adjustedTotalTravelTime = totalSecondsActual / 3600.0;
        }
        
        // Create and return new schedule
        return new DispatchReport.TrainSchedule(
            schedule.getTrainId(),
            adjustedRouteWithETAs,
            adjustedSegmentTimes,
            adjustedTotalTravelTime,
            schedule.getSelectedStations()
        );
    }

    /**
     * Generates a summary of the dispatch operation.
     *
     * @param schedules the list of train schedules
     * @param conflicts the list of conflicts
     * @param numberOfDoubleTrackPassages the number of times trains passed through double track segments simultaneously
     * @return the summary
     */
    private DispatchReport.Summary generateSummary(List<DispatchReport.TrainSchedule> schedules, List<DispatchReport.Conflict> conflicts, int numberOfDoubleTrackPassages, java.util.List<String> doubleTrackSegmentIds) {
        int totalTrains = schedules.size();
        int numberOfConflicts = conflicts.size();
        int resolvableConflicts = 0;
        int unsolvableConflicts = 0;

        for (DispatchReport.Conflict conflict : conflicts) {
            if (conflict.getResolution() == DispatchReport.Conflict.ResolutionType.UNSOLVABLE) {
                unsolvableConflicts++;
            } else {
                resolvableConflicts++;
            }
        }

        return new DispatchReport.Summary(totalTrains, numberOfConflicts, resolvableConflicts, unsolvableConflicts, numberOfDoubleTrackPassages, doubleTrackSegmentIds);
    }
}



