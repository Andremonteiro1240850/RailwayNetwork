package Domain;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class responsible for scheduling trains and detecting conflicts.
 * This service implements the business logic for USPL07 - Train Dispatch Scheduler.
 * <p>
 * This service reuses the TravelCalculationService (USPL03) to calculate travel times
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
     * @param travelCalculationService the service for calculating travel times (USPL03)
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
     * 1. Calculates travel times for each segment using USPL03's TravelCalculationService
     * 2. Accumulates ETAs for each station in each train's route
     * 3. Detects single-track conflicts (overlapping segment time intervals)
     * 4. Suggests basic resolutions for conflicts
     *
     * @param trains the list of trains to schedule
     * @return a DispatchReport containing schedules, conflicts, and summary
     * @throws IllegalArgumentException if trains list is null or empty
     */
    public DispatchReport scheduleTrains(List<Train> trains) {
        if (trains == null || trains.isEmpty()) {
            throw new IllegalArgumentException("Trains list cannot be null or empty");
        }

        List<TrainSchedule> schedules = new ArrayList<>();
        List<Conflict> conflicts = new ArrayList<>();

        // Step 1: Calculate schedules for each train
        for (Train train : trains) {
            try {
                TrainSchedule schedule = calculateTrainSchedule(train);
                schedules.add(schedule);
            } catch (Exception e) {
                // Log error but continue with other trains
                System.err.println("Error scheduling train " + train.getTrainId() + ": " + e.getMessage());
            }
        }

        // Step 2: Detect conflicts between trains
        conflicts.addAll(detectConflicts(schedules));

        // Step 3: Generate summary
        DispatchReport.Summary summary = generateSummary(schedules, conflicts);

        return new DispatchReport(schedules, conflicts, summary);
    }

    /**
     * Calculates the schedule for a single train.
     * This method reuses USPL03's TravelCalculationService for each segment.
     *
     * @param train the train to schedule
     * @return the train schedule with ETAs
     * @throws IllegalStateException if route segments are not directly connected
     */
    private TrainSchedule calculateTrainSchedule(Train train) {
        List<TrainSchedule.StationETA> routeWithETAs = new ArrayList<>();
        List<SegmentTime> segmentTimes = new ArrayList<>();
        
        LocalDateTime currentTime = train.getDeparturePlannedDateTime();
        Locomotive primaryLocomotive = train.getPrimaryLocomotive();
        List<Facility> route = train.getRoute();
        
        double totalTravelTime = 0.0;

        // Add departure station (first station)
        routeWithETAs.add(new TrainSchedule.StationETA(
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

            // REUSE USPL03 METHOD: Calculate travel time using existing service
            double travelTimeHours = travelCalculationService.calculateTravelTime(
                startStation, endStation, primaryLocomotive);

            totalTravelTime += travelTimeHours;

            // Calculate segment start and end times
            LocalDateTime segmentStartTime = currentTime;
            // Convert hours to minutes for more accurate time calculation
            long totalMinutes = (long) (travelTimeHours * 60);
            LocalDateTime segmentEndTime = currentTime.plusMinutes(totalMinutes);

            // Create segment time
            SegmentTime segmentTime = new SegmentTime(
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
            routeWithETAs.add(new TrainSchedule.StationETA(
                endStation,
                segmentEndTime,
                travelTimeHours,
                segment.getId()
            ));
        }

        return new TrainSchedule(train.getTrainId(), routeWithETAs, segmentTimes, totalTravelTime);
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
    private List<Conflict> detectConflicts(List<TrainSchedule> schedules) {
        List<Conflict> conflicts = new ArrayList<>();

        // Compare each pair of schedules
        for (int i = 0; i < schedules.size(); i++) {
            TrainSchedule schedule1 = schedules.get(i);
            for (int j = i + 1; j < schedules.size(); j++) {
                TrainSchedule schedule2 = schedules.get(j);

                // Track conflicts for this pair to avoid duplicates
                boolean conflictFound = false;

                // Check each segment in schedule1 against each segment in schedule2
                for (SegmentTime segmentTime1 : schedule1.getSegmentTimes()) {
                    for (SegmentTime segmentTime2 : schedule2.getSegmentTimes()) {
                        // Check if segments overlap (same segment ID and overlapping times)
                        if (segmentTime1.overlapsWith(segmentTime2)) {
                            // Only create one conflict per train pair per segment
                            if (!conflictFound) {
                                Conflict conflict = createConflict(schedule1, schedule2, segmentTime1, segmentTime2);
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
     *
     * @param schedule1     the first train schedule
     * @param schedule2     the second train schedule
     * @param segmentTime1  the segment time for train 1
     * @param segmentTime2  the segment time for train 2
     * @return the conflict object
     */
    private Conflict createConflict(TrainSchedule schedule1, TrainSchedule schedule2,
                                    SegmentTime segmentTime1, SegmentTime segmentTime2) {
        String train1Id = schedule1.getTrainId();
        String train2Id = schedule2.getTrainId();
        String segmentId = segmentTime1.getSegmentId();

        // Determine which train arrives first
        boolean train1ArrivesFirst = segmentTime1.getSegmentStartTime()
            .isBefore(segmentTime2.getSegmentStartTime());

        // Suggest resolution based on priority and arrival order
        Conflict.ResolutionType resolution;
        String description;

        // Simple resolution logic:
        // 1. If one train has priority, suggest waiting for it
        // 2. Otherwise, suggest waiting at previous station for the later train
        // 3. If both trains are already in transit, mark as unsolvable

        if (train1ArrivesFirst) {
            resolution = Conflict.ResolutionType.WAIT_AT_PREVIOUS_STATION;
            description = String.format(
                "Train %s arrives first at segment %s. Train %s should wait at previous station.",
                train1Id, segmentId, train2Id);
        } else {
            resolution = Conflict.ResolutionType.WAIT_AT_PREVIOUS_STATION;
            description = String.format(
                "Train %s arrives first at segment %s. Train %s should wait at previous station.",
                train2Id, segmentId, train1Id);
        }

        // Check if conflict is unsolvable (both trains already in transit)
        if (segmentTime1.getSegmentStartTime().equals(segmentTime2.getSegmentStartTime())) {
            resolution = Conflict.ResolutionType.UNSOLVABLE;
            description = String.format(
                "Both trains %s and %s arrive simultaneously at segment %s. Conflict is unsolvable.",
                train1Id, train2Id, segmentId);
        }

        return new Conflict(
            train1Id,
            train2Id,
            segmentId,
            Conflict.ConflictType.OVERLAPPING_SEGMENT,
            resolution,
            description
        );
    }

    /**
     * Generates a summary of the dispatch operation.
     *
     * @param schedules the list of train schedules
     * @param conflicts the list of conflicts
     * @return the summary
     */
    private DispatchReport.Summary generateSummary(List<TrainSchedule> schedules, List<Conflict> conflicts) {
        int totalTrains = schedules.size();
        int numberOfConflicts = conflicts.size();
        int resolvableConflicts = 0;
        int unsolvableConflicts = 0;

        for (Conflict conflict : conflicts) {
            if (conflict.getResolution() == Conflict.ResolutionType.UNSOLVABLE) {
                unsolvableConflicts++;
            } else {
                resolvableConflicts++;
            }
        }

        return new DispatchReport.Summary(totalTrains, numberOfConflicts, resolvableConflicts, unsolvableConflicts);
    }
}

