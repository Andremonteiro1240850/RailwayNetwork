package Sprint1.Domain;

import Sprint2.Domain.LineSegment;
import Sprint2.Domain.Train;

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
     * @param startFacility the starting station
     * @param endFacility   the ending station
     * @param locomotive   the locomotive to use for the calculation
     * @return the travel time in hours
     * @throws IllegalArgumentException   if any parameter is null
     * @throws IncompatibleGaugeException if locomotive gauge is incompatible with segment gauge
     * @throws IllegalStateException      if no direct connection exists between the stations
     */
    public double calculateTravelTime(Facility startFacility, Facility endFacility, Locomotive locomotive) {
        if (startFacility == null) {
            throw new IllegalArgumentException("Start station cannot be null");
        }
        if (endFacility == null) {
            throw new IllegalArgumentException("End station cannot be null");
        }
        if (locomotive == null) {
            throw new IllegalArgumentException("Locomotive cannot be null");
        }
        
        // Find the line segment connecting the two stations
        LineSegment segment = findConnectingSegment(startFacility, endFacility);
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
     * Finds a line segment that connects the two specified stations.
     *
     * @param startFacility the starting station
     * @param endFacility   the ending station
     * @return the connecting line segment, or null if no direct connection exists
     */
    public LineSegment findConnectingSegment(Facility startFacility, Facility endFacility) {
        if (startFacility == null || endFacility == null) {
            return null;
        }
        
        return lineSegments.stream()
                .filter(segment -> segment.connectsStations(startFacility, endFacility))
                .findFirst()
                .orElse(null);
    }

    /**
     * Checks if two stations are directly connected.
     *
     * @param startFacility the starting station
     * @param endFacility   the ending station
     * @return true if stations are directly connected, false otherwise
     */
    public boolean areStationsConnected(Facility startFacility, Facility endFacility) {
        return findConnectingSegment(startFacility, endFacility) != null;
    }
    
    /**
     * Finds a path between two stations using BFS (Breadth-First Search).
     * Returns a list of stations representing the path, or null if no path exists.
     *
     * @param startFacility the starting station
     * @param endFacility   the ending station
     * @return list of stations representing the path (including start and end), or null if no path exists
     */
    public List<Facility> findPath(Facility startFacility, Facility endFacility) {
        if (startFacility == null || endFacility == null) {
            return null;
        }
        
        if (startFacility.getId().equals(endFacility.getId())) {
            return java.util.Arrays.asList(startFacility);
        }
        
        // Check if directly connected first
        if (areStationsConnected(startFacility, endFacility)) {
            return java.util.Arrays.asList(startFacility, endFacility);
        }
        
        // BFS to find path
        java.util.Map<String, Facility> visited = new java.util.HashMap<>();
        java.util.Map<String, Facility> parent = new java.util.HashMap<>();
        java.util.Queue<Facility> queue = new java.util.LinkedList<>();
        
        queue.add(startFacility);
        visited.put(startFacility.getId(), startFacility);
        parent.put(startFacility.getId(), null);
        
        while (!queue.isEmpty()) {
            Facility current = queue.poll();
            
            // Find all neighbors (stations directly connected to current)
            for (LineSegment segment : lineSegments) {
                Facility neighbor = null;
                if (segment.getStartStation().getId().equals(current.getId())) {
                    neighbor = segment.getEndStation();
                } else if (segment.getEndStation().getId().equals(current.getId())) {
                    neighbor = segment.getStartStation();
                }
                
                if (neighbor != null && !visited.containsKey(neighbor.getId())) {
                    visited.put(neighbor.getId(), neighbor);
                    parent.put(neighbor.getId(), current);
                    queue.add(neighbor);
                    
                    // Found destination
                    if (neighbor.getId().equals(endFacility.getId())) {
                        // Reconstruct path
                        java.util.List<Facility> path = new java.util.ArrayList<>();
                        Facility node = neighbor;
                        while (node != null) {
                            path.add(0, node);
                            node = parent.get(node.getId());
                        }
                        return path;
                    }
                }
            }
        }
        
        return null; // No path found
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
     * Calculates the estimated travel time for a train between two stations.
     * This method considers track limitations, combined power of locomotives, and weight.
     * <p>
     * The calculation considers:
     * - Track speed limit (if present)
     * - Combined power of all locomotives
     * - Total weight of the train (locomotives + wagons)
     *
     * @param startFacility the starting station
     * @param endFacility   the ending station
     * @param train        the train with locomotives and weight information
     * @return the travel time in hours
     * @throws IllegalArgumentException   if any parameter is null
     * @throws IncompatibleGaugeException if locomotive gauge is incompatible with segment gauge
     * @throws IllegalStateException      if no direct connection exists between the stations
     */
    public double calculateTravelTime(Facility startFacility, Facility endFacility, Train train) {
        if (startFacility == null) {
            throw new IllegalArgumentException("Start station cannot be null");
        }
        if (endFacility == null) {
            throw new IllegalArgumentException("End station cannot be null");
        }
        if (train == null) {
            throw new IllegalArgumentException("Train cannot be null");
        }
        
        // Find the line segment connecting the two stations
        LineSegment segment = findConnectingSegment(startFacility, endFacility);
        if (segment == null) {
            throw new IllegalStateException("No direct connection found between stations");
        }
        
        // Check gauge compatibility (using primary locomotive)
        Locomotive primaryLocomotive = train.getPrimaryLocomotive();
        if (!primaryLocomotive.isGaugeCompatible(segment.getGauge())) {
            throw new IncompatibleGaugeException(primaryLocomotive.getGauge(), segment.getGauge());
        }
        
        // Calculate effective speed considering all factors (C3)
        double effectiveSpeed = train.calculateEffectiveSpeed(segment);
        
        // Calculate travel time: distance / effective speed
        return segment.getLengthKm() / effectiveSpeed;
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
