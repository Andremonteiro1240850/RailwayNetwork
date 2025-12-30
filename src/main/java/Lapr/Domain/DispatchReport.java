package Lapr.Domain;

import Sprint1.Domain.Facility;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete dispatch report containing train schedules, conflicts, and summary.
 *
 * @author Development Team
 * @version 1.0
 */
public class DispatchReport {
    
    private final List<DispatchReport.TrainSchedule> trainSchedules;
    private final List<Conflict> conflicts;
    private final Summary summary;

    /**
     * Represents a conflict detected between two trains on a single-track segment.
     */
    public static class Conflict {
        
        public enum ConflictType {
            OVERLAPPING_SEGMENT,
            UNSOLVABLE
        }

        public enum ResolutionType {
            WAIT_AT_PREVIOUS_STATION,
            CROSS_AT_STATION_WITH_SIDING,
            REVERSE_ORDER,
            UNSOLVABLE,
            NONE
        }

        private final String train1Id;
        private final String train2Id;
        private final String segmentId;
        private final ConflictType conflictType;
        private final ResolutionType resolution;
        private final String description;
        private final String crossingStationId; // Station where crossing should occur (if applicable)
        private final String waitingTrainId; // Train that should wait (if applicable)
        private final Long waitTimeMinutes; // Time the waiting train should wait (if applicable)

        /**
         * Constructs a new Conflict with the specified parameters.
         *
         * @param train1Id      the ID of the first train
         * @param train2Id      the ID of the second train
         * @param segmentId     the ID of the conflicting segment
         * @param conflictType  the type of conflict
         * @param resolution    the suggested resolution
         * @param description   a description of the conflict
         * @throws IllegalArgumentException if parameters are invalid
         */
        public Conflict(String train1Id, String train2Id, String segmentId,
                       ConflictType conflictType, ResolutionType resolution, String description) {
            this(train1Id, train2Id, segmentId, conflictType, resolution, description, null, null, null);
        }

        /**
         * Constructs a new Conflict with all parameters including crossing operation details.
         *
         * @param train1Id          the ID of the first train
         * @param train2Id          the ID of the second train
         * @param segmentId         the ID of the conflicting segment
         * @param conflictType      the type of conflict
         * @param resolution        the suggested resolution
         * @param description       a description of the conflict
         * @param crossingStationId the station where crossing should occur (if applicable)
         * @param waitingTrainId    the train that should wait (if applicable)
         * @param waitTimeMinutes   the time the waiting train should wait in minutes (if applicable)
         * @throws IllegalArgumentException if parameters are invalid
         */
        public Conflict(String train1Id, String train2Id, String segmentId,
                       ConflictType conflictType, ResolutionType resolution, String description,
                       String crossingStationId, String waitingTrainId, Long waitTimeMinutes) {
            if (train1Id == null || train1Id.trim().isEmpty()) {
                throw new IllegalArgumentException("Train 1 ID cannot be null or empty");
            }
            if (train2Id == null || train2Id.trim().isEmpty()) {
                throw new IllegalArgumentException("Train 2 ID cannot be null or empty");
            }
            if (segmentId == null || segmentId.trim().isEmpty()) {
                throw new IllegalArgumentException("Segment ID cannot be null or empty");
            }
            if (conflictType == null) {
                throw new IllegalArgumentException("Conflict type cannot be null");
            }
            if (resolution == null) {
                throw new IllegalArgumentException("Resolution cannot be null");
            }
            if (description == null || description.trim().isEmpty()) {
                throw new IllegalArgumentException("Description cannot be null or empty");
            }
            
            this.train1Id = train1Id.trim();
            this.train2Id = train2Id.trim();
            this.segmentId = segmentId.trim();
            this.conflictType = conflictType;
            this.resolution = resolution;
            this.description = description.trim();
            this.crossingStationId = crossingStationId != null ? crossingStationId.trim() : null;
            this.waitingTrainId = waitingTrainId != null ? waitingTrainId.trim() : null;
            this.waitTimeMinutes = waitTimeMinutes;
        }

        /**
         * Gets the ID of the first train.
         *
         * @return the train 1 ID
         */
        public String getTrain1Id() {
            return train1Id;
        }

        /**
         * Gets the ID of the second train.
         *
         * @return the train 2 ID
         */
        public String getTrain2Id() {
            return train2Id;
        }

        /**
         * Gets the ID of the conflicting segment.
         *
         * @return the segment ID
         */
        public String getSegmentId() {
            return segmentId;
        }

        /**
         * Gets the type of conflict.
         *
         * @return the conflict type
         */
        public ConflictType getConflictType() {
            return conflictType;
        }

        /**
         * Gets the suggested resolution.
         *
         * @return the resolution
         */
        public ResolutionType getResolution() {
            return resolution;
        }

        /**
         * Gets the description of the conflict.
         *
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Gets the station ID where crossing should occur (if applicable).
         *
         * @return the crossing station ID, or null if not applicable
         */
        public String getCrossingStationId() {
            return crossingStationId;
        }

        /**
         * Gets the ID of the train that should wait (if applicable).
         *
         * @return the waiting train ID, or null if not applicable
         */
        public String getWaitingTrainId() {
            return waitingTrainId;
        }

        /**
         * Gets the wait time in minutes for the waiting train (if applicable).
         *
         * @return the wait time in minutes, or null if not applicable
         */
        public Long getWaitTimeMinutes() {
            return waitTimeMinutes;
        }

        /**
         * Checks if this conflict has crossing operation details.
         *
         * @return true if crossing details are available, false otherwise
         */
        public boolean hasCrossingDetails() {
            return crossingStationId != null || waitingTrainId != null || waitTimeMinutes != null;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Conflict conflict = (Conflict) obj;
            return train1Id.equals(conflict.train1Id) &&
                   train2Id.equals(conflict.train2Id) &&
                   segmentId.equals(conflict.segmentId);
        }

        @Override
        public int hashCode() {
            return train1Id.hashCode() + train2Id.hashCode() + segmentId.hashCode();
        }

        @Override
        public String toString() {
            return String.format("Conflict[Train1: %s, Train2: %s, Segment: %s, Type: %s, Resolution: %s]",
                               train1Id, train2Id, segmentId, conflictType, resolution);
        }
    }

    /**
     * Represents a complete schedule for a train, including ETAs for each station.
     */
    public static class TrainSchedule {
        
        private final String trainId;
        private final List<StationETA> routeWithETAs;
        private final List<TrainSchedule.SegmentTime> segmentTimes;
        private final double totalTravelTimeHours;
        private final List<Facility> selectedStations; // Only stations selected by user (without intermediates)

        /**
         * Represents an ETA entry for a station in the route.
         */
        public static class StationETA {
            private final Facility station;
            private final LocalDateTime eta;
            private final double travelTimeFromPreviousHours;
            private final String segmentId;

            public StationETA(Facility station, LocalDateTime eta, double travelTimeFromPreviousHours, String segmentId) {
                this.station = station;
                this.eta = eta;
                this.travelTimeFromPreviousHours = travelTimeFromPreviousHours;
                this.segmentId = segmentId;
            }

            public Facility getStation() {
                return station;
            }

            public LocalDateTime getEta() {
                return eta;
            }

            public double getTravelTimeFromPreviousHours() {
                return travelTimeFromPreviousHours;
            }

            public String getSegmentId() {
                return segmentId;
            }

            @Override
            public String toString() {
                return String.format("StationETA[%s, ETA: %s, TravelTime: %.2fh, Segment: %s]",
                                   station.getId(), eta, travelTimeFromPreviousHours, segmentId);
            }
        }

        /**
         * Represents the time information for a single segment in a train's route.
         * This includes the segment, travel time, and arrival/departure times.
         */
        public static class SegmentTime {
            private final String segmentId;
            private final Facility startStation;
            private final Facility endStation;
            private final double travelTimeHours;
            private final LocalDateTime segmentStartTime;
            private final LocalDateTime segmentEndTime;

            /**
             * Constructs a new SegmentTime with the specified parameters.
             *
             * @param segmentId        the unique identifier for the line segment
             * @param startStation     the starting station
             * @param endStation       the ending station
             * @param travelTimeHours  the travel time in hours
             * @param segmentStartTime the time when the train enters this segment
             * @param segmentEndTime   the time when the train exits this segment
             * @throws IllegalArgumentException if parameters are invalid
             */
            public SegmentTime(String segmentId, Facility startStation, Facility endStation,
                              double travelTimeHours, LocalDateTime segmentStartTime, LocalDateTime segmentEndTime) {
                if (segmentId == null || segmentId.trim().isEmpty()) {
                    throw new IllegalArgumentException("Segment ID cannot be null or empty");
                }
                if (startStation == null) {
                    throw new IllegalArgumentException("Start station cannot be null");
                }
                if (endStation == null) {
                    throw new IllegalArgumentException("End station cannot be null");
                }
                if (travelTimeHours < 0) {
                    throw new IllegalArgumentException("Travel time cannot be negative");
                }
                if (segmentStartTime == null) {
                    throw new IllegalArgumentException("Segment start time cannot be null");
                }
                if (segmentEndTime == null) {
                    throw new IllegalArgumentException("Segment end time cannot be null");
                }
                if (segmentEndTime.isBefore(segmentStartTime)) {
                    throw new IllegalArgumentException("Segment end time cannot be before start time");
                }
                
                this.segmentId = segmentId.trim();
                this.startStation = startStation;
                this.endStation = endStation;
                this.travelTimeHours = travelTimeHours;
                this.segmentStartTime = segmentStartTime;
                this.segmentEndTime = segmentEndTime;
            }

            /**
             * Gets the segment ID.
             *
             * @return the segment ID
             */
            public String getSegmentId() {
                return segmentId;
            }

            /**
             * Gets the starting station.
             *
             * @return the starting station
             */
            public Facility getStartStation() {
                return startStation;
            }

            /**
             * Gets the ending station.
             *
             * @return the ending station
             */
            public Facility getEndStation() {
                return endStation;
            }

            /**
             * Gets the travel time in hours.
             *
             * @return the travel time in hours
             */
            public double getTravelTimeHours() {
                return travelTimeHours;
            }

            /**
             * Gets the time when the train enters this segment.
             *
             * @return the segment start time
             */
            public LocalDateTime getSegmentStartTime() {
                return segmentStartTime;
            }

            /**
             * Gets the time when the train exits this segment.
             *
             * @return the segment end time
             */
            public LocalDateTime getSegmentEndTime() {
                return segmentEndTime;
            }

            /**
             * Checks if this segment overlaps in time with another segment time.
             *
             * @param other the other segment time to check
             * @return true if the segments overlap in time, false otherwise
             */
            public boolean overlapsWith(SegmentTime other) {
                if (other == null || !segmentId.equals(other.segmentId)) {
                    return false;
                }
                
                // Check if time intervals overlap
                return !segmentEndTime.isBefore(other.segmentStartTime) && 
                       !other.segmentEndTime.isBefore(segmentStartTime);
            }

            @Override
            public String toString() {
                return String.format("SegmentTime[%s: %s -> %s, Time: %.2fh, %s to %s]",
                                   segmentId, startStation.getId(), endStation.getId(),
                                   travelTimeHours, segmentStartTime, segmentEndTime);
            }
        }

        /**
         * Constructs a new TrainSchedule with the specified parameters.
         *
         * @param trainId              the train ID
         * @param routeWithETAs        the ordered route with ETAs for each station
         * @param segmentTimes         the list of segment times
         * @param totalTravelTimeHours the total travel time in hours
         * @param selectedStations     the stations selected by user (without intermediate stations)
         * @throws IllegalArgumentException if parameters are invalid
         */
        public TrainSchedule(String trainId, List<StationETA> routeWithETAs,
                            List<TrainSchedule.SegmentTime> segmentTimes, double totalTravelTimeHours,
                            List<Facility> selectedStations) {
            if (trainId == null || trainId.trim().isEmpty()) {
                throw new IllegalArgumentException("Train ID cannot be null or empty");
            }
            if (routeWithETAs == null || routeWithETAs.isEmpty()) {
                throw new IllegalArgumentException("Route with ETAs cannot be null or empty");
            }
            if (segmentTimes == null) {
                throw new IllegalArgumentException("Segment times cannot be null");
            }
            if (totalTravelTimeHours < 0) {
                throw new IllegalArgumentException("Total travel time cannot be negative");
            }
            if (selectedStations == null || selectedStations.isEmpty()) {
                throw new IllegalArgumentException("Selected stations cannot be null or empty");
            }
            
            this.trainId = trainId.trim();
            this.routeWithETAs = new ArrayList<>(routeWithETAs);
            this.selectedStations = new ArrayList<>(selectedStations);
            this.segmentTimes = new ArrayList<>(segmentTimes);
            this.totalTravelTimeHours = totalTravelTimeHours;
        }

        /**
         * Gets the train ID.
         *
         * @return the train ID
         */
        public String getTrainId() {
            return trainId;
        }

        /**
         * Gets the route with ETAs for each station.
         *
         * @return the route with ETAs
         */
        public List<StationETA> getRouteWithETAs() {
            return new ArrayList<>(routeWithETAs);
        }
        
        /**
         * Gets the stations selected by the user (without intermediate stations).
         *
         * @return the selected stations
         */
        public List<Facility> getSelectedStations() {
            return new ArrayList<>(selectedStations);
        }

        /**
         * Gets the list of segment times.
         *
         * @return the segment times
         */
        public List<TrainSchedule.SegmentTime> getSegmentTimes() {
            return new ArrayList<>(segmentTimes);
        }

        /**
         * Gets the total travel time in hours.
         *
         * @return the total travel time in hours
         */
        public double getTotalTravelTimeHours() {
            return totalTravelTimeHours;
        }

        @Override
        public String toString() {
            return String.format("TrainSchedule[%s, Stations: %d, TotalTime: %.2fh]",
                               trainId, routeWithETAs.size(), totalTravelTimeHours);
        }
    }

    /**
     * Represents a summary of the dispatch operation.
     */
    public static class Summary {
        private final int totalTrainsDispatched;
        private final int numberOfConflicts;
        private final int numberOfResolvableConflicts;
        private final int numberOfUnsolvableConflicts;

        public Summary(int totalTrainsDispatched, int numberOfConflicts,
                      int numberOfResolvableConflicts, int numberOfUnsolvableConflicts) {
            this.totalTrainsDispatched = totalTrainsDispatched;
            this.numberOfConflicts = numberOfConflicts;
            this.numberOfResolvableConflicts = numberOfResolvableConflicts;
            this.numberOfUnsolvableConflicts = numberOfUnsolvableConflicts;
        }

        public int getTotalTrainsDispatched() {
            return totalTrainsDispatched;
        }

        public int getNumberOfConflicts() {
            return numberOfConflicts;
        }

        public int getNumberOfResolvableConflicts() {
            return numberOfResolvableConflicts;
        }

        public int getNumberOfUnsolvableConflicts() {
            return numberOfUnsolvableConflicts;
        }

        @Override
        public String toString() {
            return String.format("Summary[Trains: %d, Conflicts: %d (Resolvable: %d, Unsolvable: %d)]",
                               totalTrainsDispatched, numberOfConflicts,
                               numberOfResolvableConflicts, numberOfUnsolvableConflicts);
        }
    }

    /**
     * Constructs a new DispatchReport with the specified parameters.
     *
     * @param trainSchedules the list of train schedules
     * @param conflicts      the list of detected conflicts
     * @param summary        the summary of the dispatch operation
     * @throws IllegalArgumentException if parameters are invalid
     */
    public DispatchReport(List<DispatchReport.TrainSchedule> trainSchedules, List<Conflict> conflicts, Summary summary) {
        if (trainSchedules == null) {
            throw new IllegalArgumentException("Train schedules cannot be null");
        }
        if (conflicts == null) {
            throw new IllegalArgumentException("Conflicts cannot be null");
        }
        if (summary == null) {
            throw new IllegalArgumentException("Summary cannot be null");
        }
        
        this.trainSchedules = new ArrayList<>(trainSchedules);
        this.conflicts = new ArrayList<>(conflicts);
        this.summary = summary;
    }

    /**
     * Gets the list of train schedules.
     *
     * @return the train schedules
     */
    public List<DispatchReport.TrainSchedule> getTrainSchedules() {
        return new ArrayList<>(trainSchedules);
    }

    /**
     * Gets the list of conflicts.
     *
     * @return the conflicts
     */
    public List<Conflict> getConflicts() {
        return new ArrayList<>(conflicts);
    }

    /**
     * Gets the summary of the dispatch operation.
     *
     * @return the summary
     */
    public Summary getSummary() {
        return summary;
    }

    /**
     * Gets a train schedule by train ID.
     *
     * @param trainId the train ID
     * @return the train schedule, or null if not found
     */
    public DispatchReport.TrainSchedule getTrainSchedule(String trainId) {
        return trainSchedules.stream()
                .filter(schedule -> schedule.getTrainId().equals(trainId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return String.format("DispatchReport[Schedules: %d, Conflicts: %d, %s]",
                           trainSchedules.size(), conflicts.size(), summary);
    }
}

