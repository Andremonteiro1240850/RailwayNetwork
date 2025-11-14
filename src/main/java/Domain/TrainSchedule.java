package Domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete schedule for a train, including ETAs for each station.
 * This class is used for USPL07 - Train Dispatch Scheduler.
 *
 * @author Development Team
 * @version 1.0
 */
public class TrainSchedule {
    
    private final String trainId;
    private final List<StationETA> routeWithETAs;
    private final List<SegmentTime> segmentTimes;
    private final double totalTravelTimeHours;

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
     * Constructs a new TrainSchedule with the specified parameters.
     *
     * @param trainId              the train ID
     * @param routeWithETAs        the ordered route with ETAs for each station
     * @param segmentTimes         the list of segment times
     * @param totalTravelTimeHours the total travel time in hours
     * @throws IllegalArgumentException if parameters are invalid
     */
    public TrainSchedule(String trainId, List<StationETA> routeWithETAs,
                        List<SegmentTime> segmentTimes, double totalTravelTimeHours) {
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
        
        this.trainId = trainId.trim();
        this.routeWithETAs = new ArrayList<>(routeWithETAs);
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
     * Gets the list of segment times.
     *
     * @return the segment times
     */
    public List<SegmentTime> getSegmentTimes() {
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

