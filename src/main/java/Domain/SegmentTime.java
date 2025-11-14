package Domain;

import java.time.LocalDateTime;

/**
 * Represents the time information for a single segment in a train's route.
 * This includes the segment, travel time, and arrival/departure times.
 * This class is used for USPL07 - Train Dispatch Scheduler.
 *
 * @author Development Team
 * @version 1.0
 */
public class SegmentTime {
    
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

