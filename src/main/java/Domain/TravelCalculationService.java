package Domain;

import java.util.List;

/**
 * Service class responsible for calculating travel time between railway stations.
 * This service implements the business logic for USLP03 - Calculate Estimated Travel Time.
 * <p>
 * The calculation assumes instantaneous acceleration and only considers directly connected stations.
 *
 * @author Development Team
 * @version 1.0
 */
public class TravelCalculationService {
    
    private final List<LineSegment> lineSegments;

    /**
     * Constructs a new TravelCalculationService with the given line segments.
     *
     * @param lineSegments the list of available line segments
     * @throws IllegalArgumentException if lineSegments is null
     */
    public TravelCalculationService(List<LineSegment> lineSegments) {
        if (lineSegments == null) {
            throw new IllegalArgumentException("Line segments list cannot be null");
        }
        this.lineSegments = lineSegments;
    }

    /**
     * Calculates the estimated travel time between two stations using a specific locomotive.
     * <p>
     * The calculation formula: travelTime = lineSegmentLengthKm / locomotiveMaxSpeedKmH
     *
     * @param startStation the starting station
     * @param endStation   the ending station
     * @param locomotive   the locomotive to use for the calculation
     * @return the travel time in hours
     * @throws IllegalArgumentException   if any parameter is null
     * @throws IncompatibleGaugeException if locomotive gauge is incompatible with segment gauge
     * @throws IllegalStateException      if no direct connection exists between the stations
     */
    public double calculateTravelTime(Station startStation, Station endStation, Locomotive locomotive) {
        if (startStation == null) {
            throw new IllegalArgumentException("Start station cannot be null");
        }
        if (endStation == null) {
            throw new IllegalArgumentException("End station cannot be null");
        }
        if (locomotive == null) {
            throw new IllegalArgumentException("Locomotive cannot be null");
        }
        
        // Find the line segment connecting the two stations
        LineSegment segment = findConnectingSegment(startStation, endStation);
        if (segment == null) {
            throw new IllegalStateException("No direct connection found between stations");
        }
        
        // Check gauge compatibility
        if (!locomotive.isGaugeCompatible(segment.getGauge())) {
            throw new IncompatibleGaugeException(locomotive.getGauge(), segment.getGauge());
        }
        
        // Calculate travel time: distance / speed
        return segment.getLengthKm() / locomotive.getMaxSpeedKmH();
    }

    /**
     * Calculates the estimated travel time using a specific line segment and locomotive.
     * This method is useful when the line segment is already known.
     *
     * @param startStation the starting station
     * @param endStation   the ending station
     * @param locomotive   the locomotive to use for the calculation
     * @param segment      the specific line segment to use
     * @return the travel time in hours
     * @throws IllegalArgumentException   if any parameter is null
     * @throws IncompatibleGaugeException if locomotive gauge is incompatible with segment gauge
     * @throws IllegalStateException      if the segment doesn't connect the specified stations
     */
    public double calculateTravelTime(Station startStation, Station endStation,
                                   Locomotive locomotive, LineSegment segment) {
        if (startStation == null) {
            throw new IllegalArgumentException("Start station cannot be null");
        }
        if (endStation == null) {
            throw new IllegalArgumentException("End station cannot be null");
        }
        if (locomotive == null) {
            throw new IllegalArgumentException("Locomotive cannot be null");
        }
        if (segment == null) {
            throw new IllegalArgumentException("Line segment cannot be null");
        }
        
        // Verify the segment connects the specified stations
        if (!segment.connectsStations(startStation, endStation)) {
            throw new IllegalStateException("Line segment does not connect the specified stations");
        }
        
        // Check gauge compatibility
        if (!locomotive.isGaugeCompatible(segment.getGauge())) {
            throw new IncompatibleGaugeException(locomotive.getGauge(), segment.getGauge());
        }
        
        // Calculate travel time: distance / speed
        return segment.getLengthKm() / locomotive.getMaxSpeedKmH();
    }

    /**
     * Finds a line segment that connects the two specified stations.
     *
     * @param startStation the starting station
     * @param endStation   the ending station
     * @return the connecting line segment, or null if no direct connection exists
     */
    public LineSegment findConnectingSegment(Station startStation, Station endStation) {
        if (startStation == null || endStation == null) {
            return null;
        }
        
        return lineSegments.stream()
                .filter(segment -> segment.connectsStations(startStation, endStation))
                .findFirst()
                .orElse(null);
    }

    /**
     * Checks if two stations are directly connected.
     *
     * @param startStation the starting station
     * @param endStation   the ending station
     * @return true if stations are directly connected, false otherwise
     */
    public boolean areStationsConnected(Station startStation, Station endStation) {
        return findConnectingSegment(startStation, endStation) != null;
    }

    /**
     * Gets all available line segments.
     *
     * @return the list of line segments
     */
    public List<LineSegment> getLineSegments() {
        return lineSegments;
    }

    /**
     * Formats travel time in a human-readable format.
     *
     * @param travelTimeHours the travel time in hours
     * @return formatted string (e.g., "1h 30m" or "45m")
     */
    public static String formatTravelTime(double travelTimeHours) {
        if (travelTimeHours < 0) {
            return "0m";
        }
        
        int hours = (int) travelTimeHours;
        int minutes = (int) Math.round((travelTimeHours - hours) * 60);
        
        // Handle minute overflow
        if (minutes >= 60) {
            hours++;
            minutes = 0;
        }
        
        if (hours > 0) {
            if (minutes > 0) {
                return String.format("%dh %dm", hours, minutes);
            } else {
                return String.format("%dh", hours);
            }
        } else {
            return String.format("%dm", minutes);
        }
    }
}
