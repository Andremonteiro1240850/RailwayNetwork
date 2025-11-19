package Domain;

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

        // Step 1: Resolve conflicts by adjusting departure times and detect departure conflicts
        List<Train> adjustedTrains = resolveDepartureTimeConflicts(trains);
        List<DispatchReport.Conflict> departureConflicts = detectDepartureTimeConflicts(trains, adjustedTrains);

        List<DispatchReport.TrainSchedule> schedules = new ArrayList<>();
        List<DispatchReport.Conflict> conflicts = new ArrayList<>();
        
        // Add departure conflicts first
        conflicts.addAll(departureConflicts);

        // Step 2: Calculate schedules for each train (with adjusted departure times)
        for (Train train : adjustedTrains) {
            try {
                DispatchReport.TrainSchedule schedule = calculateTrainSchedule(train);
                schedules.add(schedule);
            } catch (Exception e) {
                // Log error but continue with other trains
                System.err.println("Error scheduling train " + train.getTrainId() + ": " + e.getMessage());
            }
        }

        // Step 3: Detect remaining conflicts between trains
        // Filter out conflicts that are already covered by departure time conflicts
        // A departure conflict covers the case where trains depart from the same station at the same time
        // and one train's departure is adjusted. We don't need to show a segment conflict for the same train pair
        // if it's already covered by a departure conflict (which has the correct wait time).
        List<DispatchReport.Conflict> segmentConflicts = detectConflicts(schedules);
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
            }
        }

        // Step 4: Generate summary
        DispatchReport.Summary summary = generateSummary(schedules, conflicts);

        return new DispatchReport(schedules, conflicts, summary);
    }

    /**
     * Calculates the schedule for a single train.
     * This method reuses TravelCalculationService for each segment.
     *
     * @param train the train to schedule
     * @return the train schedule with ETAs
     * @throws IllegalStateException if route segments are not directly connected
     */
    private DispatchReport.TrainSchedule calculateTrainSchedule(Train train) {
        List<DispatchReport.TrainSchedule.StationETA> routeWithETAs = new ArrayList<>();
        List<DispatchReport.TrainSchedule.SegmentTime> segmentTimes = new ArrayList<>();
        
        LocalDateTime currentTime = train.getDeparturePlannedDateTime();
        List<Facility> route = train.getRoute();

        // Add departure station (first station)
        routeWithETAs.add(new DispatchReport.TrainSchedule.StationETA(
            route.get(0),
            currentTime,
            0.0,
            null // No segment for departure station
        ));

        // Calculate travel time for each consecutive pair of stations
        for (int i = 0; i < route.size() - 1; i++) {
            Facility startStation = route.get(i);
            Facility endStation = route.get(i + 1);

            // Find the connecting segment
            LineSegment segment = findConnectingSegment(startStation, endStation);
            if (segment == null) {
                throw new IllegalStateException(
                    String.format("No direct connection found between stations %s and %s for train %s",
                                startStation.getId(), endStation.getId(), train.getTrainId()));
            }

            // Calculate travel time using existing service
            // Use the enhanced method that considers all factors
            double travelTimeHours = travelCalculationService.calculateTravelTime(
                startStation, endStation, train);

            // Calculate segment start and end times
            LocalDateTime segmentStartTime = currentTime;
            // Convert hours to minutes for more accurate time calculation
            long totalMinutes = (long) (travelTimeHours * 60);
            LocalDateTime segmentEndTime = currentTime.plusMinutes(totalMinutes);

            // Create segment time
            DispatchReport.TrainSchedule.SegmentTime segmentTime = new DispatchReport.TrainSchedule.SegmentTime(
                segment.getId(),
                startStation,
                endStation,
                travelTimeHours,
                segmentStartTime,
                segmentEndTime
            );
            segmentTimes.add(segmentTime);

            // Update current time for next segment
            currentTime = segmentEndTime;

            // Add ETA for the end station
            routeWithETAs.add(new DispatchReport.TrainSchedule.StationETA(
                endStation,
                segmentEndTime,
                travelTimeHours,
                segment.getId()
            ));
        }

        // Calculate total travel time based on actual arrival time to ensure consistency
        // This ensures the displayed total travel time matches the actual time difference
        LocalDateTime departureTime = routeWithETAs.get(0).getEta();
        LocalDateTime arrivalTime = routeWithETAs.get(routeWithETAs.size() - 1).getEta();
        long totalMinutesActual = java.time.Duration.between(departureTime, arrivalTime).toMinutes();
        double totalTravelTimeActual = totalMinutesActual / 60.0;

        return new DispatchReport.TrainSchedule(train.getTrainId(), routeWithETAs, segmentTimes, totalTravelTimeActual, train.getSelectedStations());
    }

    /**
     * Finds a line segment connecting two stations.
     *
     * @param startStation the starting station
     * @param endStation   the ending station
     * @return the connecting segment, or null if not found
     */
    private LineSegment findConnectingSegment(Facility startStation, Facility endStation) {
        return lineSegments.stream()
                .filter(segment -> segment.connectsStations(startStation, endStation))
                .findFirst()
                .orElse(null);
    }

    /**
     * Detects conflicts between trains on single-track segments.
     * Two trains conflict if they occupy the same segment during overlapping time intervals.
     *
     * @param schedules the list of train schedules
     * @return the list of detected conflicts
     */
    private List<DispatchReport.Conflict> detectConflicts(List<DispatchReport.TrainSchedule> schedules) {
        List<DispatchReport.Conflict> conflicts = new ArrayList<>();

        // Compare each pair of schedules
        for (int i = 0; i < schedules.size(); i++) {
            DispatchReport.TrainSchedule schedule1 = schedules.get(i);
            for (int j = i + 1; j < schedules.size(); j++) {
                DispatchReport.TrainSchedule schedule2 = schedules.get(j);

                // Track conflicts for this pair to avoid duplicates
                boolean conflictFound = false;

                // Check each segment in schedule1 against each segment in schedule2
                for (DispatchReport.TrainSchedule.SegmentTime segmentTime1 : schedule1.getSegmentTimes()) {
                    for (DispatchReport.TrainSchedule.SegmentTime segmentTime2 : schedule2.getSegmentTimes()) {
                        // Check if segments overlap (same segment ID and overlapping times)
                        if (segmentTime1.overlapsWith(segmentTime2)) {
                            // Only create one conflict per train pair per segment
                            if (!conflictFound) {
                                DispatchReport.Conflict conflict = createConflict(schedule1, schedule2, segmentTime1, segmentTime2);
                                conflicts.add(conflict);
                                conflictFound = true;
                                break; // Move to next train pair
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
     * Creates a conflict object with suggested resolution.
     * Implements C4: considers sidings for crossings and C5: provides detailed crossing operations.
     *
     * @param schedule1     the first train schedule
     * @param schedule2     the second train schedule
     * @param segmentTime1  the segment time for train 1
     * @param segmentTime2  the segment time for train 2
     * @return the conflict object
     */
    private DispatchReport.Conflict createConflict(DispatchReport.TrainSchedule schedule1, DispatchReport.TrainSchedule schedule2,
                                    DispatchReport.TrainSchedule.SegmentTime segmentTime1, DispatchReport.TrainSchedule.SegmentTime segmentTime2) {
        String train1Id = schedule1.getTrainId();
        String train2Id = schedule2.getTrainId();
        String segmentId = segmentTime1.getSegmentId();

        // Determine which train arrives first
        boolean train1ArrivesFirst = segmentTime1.getSegmentStartTime()
            .isBefore(segmentTime2.getSegmentStartTime());

        // Find previous stations (for checking sidings)
        Facility previousStation1 = segmentTime1.getStartStation();
        Facility previousStation2 = segmentTime2.getStartStation();

        // Check if conflict is unsolvable (both trains already in transit)
        if (segmentTime1.getSegmentStartTime().equals(segmentTime2.getSegmentStartTime())) {
            return new DispatchReport.Conflict(
                train1Id,
                train2Id,
                segmentId,
                DispatchReport.Conflict.ConflictType.OVERLAPPING_SEGMENT,
                DispatchReport.Conflict.ResolutionType.UNSOLVABLE,
                String.format(
                    "Both trains %s and %s arrive simultaneously at segment %s. Conflict is unsolvable.",
                    train1Id, train2Id, segmentId)
            );
        }

        // C4: Check if we can use a siding for crossing
        // Check if previous station of the later train has a siding
        Facility crossingStation = null;
        String waitingTrainId = null;
        Long waitTimeMinutes = null;
        DispatchReport.Conflict.ResolutionType resolution;
        String description;

        // Get arrival times at previous stations to calculate wait time correctly
        // If previous station is the departure station, use segment start time (when train enters the segment)
        LocalDateTime train1ArrivalAtPreviousStation = getArrivalTimeAtStation(schedule1, previousStation1);
        if (train1ArrivalAtPreviousStation == null) {
            // If station not found in route, use segment start time
            train1ArrivalAtPreviousStation = segmentTime1.getSegmentStartTime();
        }
        
        LocalDateTime train2ArrivalAtPreviousStation = getArrivalTimeAtStation(schedule2, previousStation2);
        if (train2ArrivalAtPreviousStation == null) {
            // If station not found in route, use segment start time
            train2ArrivalAtPreviousStation = segmentTime2.getSegmentStartTime();
        }

        if (train1ArrivesFirst) {
            // Train 1 arrives first, train 2 should wait
            if (previousStation2.hasSiding()) {
                // Can cross at previous station of train 2 (which has siding)
                crossingStation = previousStation2;
                waitingTrainId = train2Id;
                resolution = DispatchReport.Conflict.ResolutionType.CROSS_AT_STATION_WITH_SIDING;
                
                // Calculate wait time: train 2 should wait from arrival at previous station until train 1 passes
                long minutesBetween = java.time.Duration.between(
                    train2ArrivalAtPreviousStation, 
                    segmentTime1.getSegmentEndTime()
                ).toMinutes();
                waitTimeMinutes = Math.max(0, minutesBetween);
                
                description = String.format(
                    "Train %s arrives first at segment %s. Train %s should wait at station %s (has siding) for %d minutes to allow crossing.",
                    train1Id, segmentId, train2Id, crossingStation.getId(), waitTimeMinutes);
            } else {
                // No siding available, train 2 must wait at previous station
                waitingTrainId = train2Id;
                resolution = DispatchReport.Conflict.ResolutionType.WAIT_AT_PREVIOUS_STATION;
                
                // Calculate wait time: train 2 should wait from arrival at previous station until train 1 passes
                long minutesBetween = java.time.Duration.between(
                    train2ArrivalAtPreviousStation, 
                    segmentTime1.getSegmentEndTime()
                ).toMinutes();
                waitTimeMinutes = Math.max(0, minutesBetween);
                
                description = String.format(
                    "Train %s arrives first at segment %s. Train %s should wait at previous station %s (no siding available).",
                    train1Id, segmentId, train2Id, previousStation2.getId());
            }
        } else {
            // Train 2 arrives first, train 1 should wait
            if (previousStation1.hasSiding()) {
                // Can cross at previous station of train 1 (which has siding)
                crossingStation = previousStation1;
                waitingTrainId = train1Id;
                resolution = DispatchReport.Conflict.ResolutionType.CROSS_AT_STATION_WITH_SIDING;
                
                // Calculate wait time: train 1 should wait from arrival at previous station until train 2 passes
                // Wait time = when train 2 finishes - when train 1 arrives at previous station
                long minutesBetween = java.time.Duration.between(
                    train1ArrivalAtPreviousStation, 
                    segmentTime2.getSegmentEndTime()
                ).toMinutes();
                waitTimeMinutes = Math.max(0, minutesBetween);
                
                description = String.format(
                    "Train %s arrives first at segment %s. Train %s should wait at station %s (has siding) for %d minutes to allow crossing.",
                    train2Id, segmentId, train1Id, crossingStation.getId(), waitTimeMinutes);
            } else {
                // No siding available, train 1 must wait at previous station
                waitingTrainId = train1Id;
                resolution = DispatchReport.Conflict.ResolutionType.WAIT_AT_PREVIOUS_STATION;
                
                // Calculate wait time: train 1 should wait from arrival at previous station until train 2 passes
                // Wait time = when train 2 finishes - when train 1 arrives at previous station
                long minutesBetween = java.time.Duration.between(
                    train1ArrivalAtPreviousStation, 
                    segmentTime2.getSegmentEndTime()
                ).toMinutes();
                waitTimeMinutes = Math.max(0, minutesBetween);
                
                description = String.format(
                    "Train %s arrives first at segment %s. Train %s should wait at previous station %s (no siding available).",
                    train2Id, segmentId, train1Id, previousStation1.getId());
            }
        }

        // C5: Create conflict with detailed crossing operation information
        return new DispatchReport.Conflict(
            train1Id,
            train2Id,
            segmentId,
            DispatchReport.Conflict.ConflictType.OVERLAPPING_SEGMENT,
            resolution,
            description,
            crossingStation != null ? crossingStation.getId() : null,
            waitingTrainId,
            waitTimeMinutes
        );
    }

    /**
     * Gets the arrival time at a specific station from a train schedule.
     *
     * @param schedule the train schedule
     * @param station the station to get arrival time for
     * @return the arrival time at the station, or null if station not found
     */
    private LocalDateTime getArrivalTimeAtStation(DispatchReport.TrainSchedule schedule, Facility station) {
        for (DispatchReport.TrainSchedule.StationETA eta : schedule.getRouteWithETAs()) {
            if (eta.getStation().equals(station)) {
                return eta.getEta();
            }
        }
        return null;
    }

    /**
     * Resolves conflicts by adjusting departure times when trains depart from the same station at the same time.
     * When multiple trains depart from the same station at the same time, each subsequent train's departure time
     * is adjusted to after the previous train arrives at the next station.
     *
     * @param trains the original list of trains
     * @return a list of trains with adjusted departure times
     */
    private List<Train> resolveDepartureTimeConflicts(List<Train> trains) {
        List<Train> adjustedTrains = new ArrayList<>(trains);
        
        // Group trains by departure station and time
        // Then process each group to adjust departure times sequentially
        boolean changed = true;
        while (changed) {
            changed = false;
            
            // Compare each pair of trains
            for (int i = 0; i < adjustedTrains.size(); i++) {
                Train train1 = adjustedTrains.get(i);
                Facility train1StartStation = train1.getRoute().get(0);
                LocalDateTime train1Departure = train1.getDeparturePlannedDateTime();
                
                for (int j = i + 1; j < adjustedTrains.size(); j++) {
                    Train train2 = adjustedTrains.get(j);
                    Facility train2StartStation = train2.getRoute().get(0);
                    LocalDateTime train2Departure = train2.getDeparturePlannedDateTime();
                    
                    // Check if trains depart from the same station at the same time
                    if (train1StartStation.equals(train2StartStation) && 
                        train1Departure.equals(train2Departure)) {
                        
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
                                "(waiting for Train %s to arrive at %s at %s)",
                                train2.getTrainId(), train2Departure, adjustedDepartureTime,
                                train1.getTrainId(), train1NextStation.getId(), train1ArrivalAtNextStation));
                        }
                    }
                }
            }
        }
        
        return adjustedTrains;
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
     * Generates a summary of the dispatch operation.
     *
     * @param schedules the list of train schedules
     * @param conflicts the list of conflicts
     * @return the summary
     */
    private DispatchReport.Summary generateSummary(List<DispatchReport.TrainSchedule> schedules, List<DispatchReport.Conflict> conflicts) {
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

        return new DispatchReport.Summary(totalTrains, numberOfConflicts, resolvableConflicts, unsolvableConflicts);
    }
}

