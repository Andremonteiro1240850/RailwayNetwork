package Domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a train that needs to be dispatched.
 * A train contains route information, locomotives, and scheduling details.
 *
 * @author Development Team
 * @version 1.0
 */
public class Train {
    
    private final String trainId;
    private final List<Facility> route; // Full route including intermediate stations
    private final List<Facility> selectedStations; // Only stations selected by user (without intermediates)
    private final List<Locomotive> locomotives;
    private final List<Wagon> wagons;
    private final LocalDateTime departurePlannedDateTime;
    private final double totalWeight;

    /**
     * Constructs a new Train with the specified parameters.
     *
     * @param trainId                  the unique identifier for the train
     * @param route                    the ordered list of stations (full route including intermediate stations)
     * @param selectedStations         the ordered list of stations selected by user (without intermediate stations)
     * @param locomotives              the list of locomotives (with speed and power)
     * @param wagons                   the list of wagons (can be empty)
     * @param departurePlannedDateTime the planned departure date and time
     * @param totalWeight              the total weight of the train in kg
     * @throws IllegalArgumentException if parameters are invalid
     */
    public Train(String trainId, List<Facility> route, List<Facility> selectedStations, 
                 List<Locomotive> locomotives, List<Wagon> wagons, 
                 LocalDateTime departurePlannedDateTime, double totalWeight) {
        if (trainId == null || trainId.trim().isEmpty()) {
            throw new IllegalArgumentException("Train ID cannot be null or empty");
        }
        if (route == null || route.size() < 2) {
            throw new IllegalArgumentException("Route must contain at least 2 stations");
        }
        if (selectedStations == null || selectedStations.size() < 2) {
            throw new IllegalArgumentException("Selected stations must contain at least 2 stations");
        }
        if (locomotives == null || locomotives.isEmpty()) {
            throw new IllegalArgumentException("Train must have at least one locomotive");
        }
        if (wagons == null) {
            throw new IllegalArgumentException("Wagons list cannot be null");
        }
        if (departurePlannedDateTime == null) {
            throw new IllegalArgumentException("Departure planned date time cannot be null");
        }
        if (totalWeight < 0) {
            throw new IllegalArgumentException("Total weight cannot be negative");
        }
        
        this.trainId = trainId.trim();
        this.route = Collections.unmodifiableList(new ArrayList<>(route)); // Immutable copy
        this.selectedStations = Collections.unmodifiableList(new ArrayList<>(selectedStations)); // Immutable copy
        this.locomotives = Collections.unmodifiableList(new ArrayList<>(locomotives)); // Immutable copy
        this.wagons = Collections.unmodifiableList(new ArrayList<>(wagons)); // Immutable copy
        this.departurePlannedDateTime = departurePlannedDateTime;
        this.totalWeight = totalWeight;
    }
    
    /**
     * Constructs a new Train with the specified parameters (backward compatibility).
     * Uses the route as selectedStations if not provided separately.
     *
     * @param trainId                  the unique identifier for the train
     * @param route                    the ordered list of stations
     * @param locomotives              the list of locomotives (with speed and power)
     * @param wagons                   the list of wagons (can be empty)
     * @param departurePlannedDateTime the planned departure date and time
     * @param totalWeight              the total weight of the train in kg
     * @throws IllegalArgumentException if parameters are invalid
     */
    public Train(String trainId, List<Facility> route, List<Locomotive> locomotives,
                 List<Wagon> wagons, LocalDateTime departurePlannedDateTime, double totalWeight) {
        this(trainId, route, route, locomotives, wagons, departurePlannedDateTime, totalWeight);
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
     * Gets the route (ordered list of stations, including intermediate stations).
     *
     * @return the full route
     */
    public List<Facility> getRoute() {
        return route;
    }
    
    /**
     * Gets the stations selected by the user (without intermediate stations).
     *
     * @return the selected stations
     */
    public List<Facility> getSelectedStations() {
        return selectedStations;
    }

    /**
     * Gets the locomotives assigned to this train.
     *
     * @return the list of locomotives
     */
    public List<Locomotive> getLocomotives() {
        return locomotives;
    }

    /**
     * Gets the wagons assigned to this train.
     *
     * @return the list of wagons
     */
    public List<Wagon> getWagons() {
        return wagons;
    }

    /**
     * Gets the primary locomotive (first in the list).
     * This is typically used for travel time calculations.
     *
     * @return the primary locomotive
     */
    public Locomotive getPrimaryLocomotive() {
        return locomotives.get(0);
    }

    /**
     * Gets the planned departure date and time.
     *
     * @return the departure planned date time
     */
    public LocalDateTime getDeparturePlannedDateTime() {
        return departurePlannedDateTime;
    }

    /**
     * Gets the total weight of the train.
     *
     * @return the total weight in kg
     */
    public double getTotalWeight() {
        return totalWeight;
    }


    /**
     * Calculates the effective maximum speed considering all locomotives.
     * Uses the maximum speed of the fastest locomotive.
     *
     * @return the effective maximum speed in km/h
     */
    public double getEffectiveMaxSpeedKmH() {
        return locomotives.stream()
                .mapToDouble(Locomotive::getMaxSpeedKmH)
                .max()
                .orElse(0.0);
    }

    /**
     * Calculates the effective speed for a given segment, considering:
     * - Track speed limit
     * - Locomotive maximum speed
     *
     * @param segment the line segment to calculate speed for
     * @return the effective speed in km/h
     */
    public double calculateEffectiveSpeed(LineSegment segment) {
        // Start with the maximum speed of the fastest locomotive
        double maxLocomotiveSpeed = getEffectiveMaxSpeedKmH();
        
        // Apply track speed limit if present
        double effectiveSpeed = maxLocomotiveSpeed;
        if (segment.hasSpeedLimit()) {
            effectiveSpeed = Math.min(effectiveSpeed, segment.getSpeedLimitKmH());
        }
        
        return Math.max(1.0, effectiveSpeed); // Ensure minimum speed of 1 km/h
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Train train = (Train) obj;
        return trainId.equals(train.trainId);
    }

    @Override
    public int hashCode() {
        return trainId.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Train[%s, Route: %d stations, Locomotives: %d, Wagons: %d, Departure: %s, Weight: %.0f kg]",
                           trainId, route.size(), locomotives.size(), wagons.size(), departurePlannedDateTime, totalWeight);
    }
}

