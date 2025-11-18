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

        // Step 1: Resolve conflicts by adjusting departure times
        List<Train> adjustedTrains = resolveDepartureTimeConflicts(trains);

        List<DispatchReport.TrainSchedule> schedules = new ArrayList<>();
        List<DispatchReport.Conflict> conflicts = new ArrayList<>();

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
        conflicts.addAll(detectConflicts(schedules));

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
        Locomotive primaryLocomotive = train.getPrimaryLocomotive();
        List<Facility> route = train.getRoute();
        
        double totalTravelTime = 0.0;

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

            totalTravelTime += travelTimeHours;

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

        return new DispatchReport.TrainSchedule(train.getTrainId(), routeWithETAs, segmentTimes, totalTravelTime, train.getSelectedStations());
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

        if (train1ArrivesFirst) {
            // Train 1 arrives first, train 2 should wait
            if (previousStation2.hasSiding()) {
                // Can cross at previous station of train 2 (which has siding)
                crossingStation = previousStation2;
                waitingTrainId = train2Id;
                resolution = DispatchReport.Conflict.ResolutionType.CROSS_AT_STATION_WITH_SIDING;
                
                // Calculate wait time: train 2 should wait until train 1 passes
                long minutesBetween = java.time.Duration.between(
                    segmentTime2.getSegmentStartTime(), 
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
                
                // Calculate wait time: train 1 should wait until train 2 passes
                long minutesBetween = java.time.Duration.between(
                    segmentTime1.getSegmentStartTime(), 
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

