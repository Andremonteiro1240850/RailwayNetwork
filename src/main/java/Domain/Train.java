package Domain;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a train that needs to be dispatched.
 * A train contains route information, locomotives, and scheduling details.
 * This class is used for USPL07 - Train Dispatch Scheduler.
 *
 * @author Development Team
 * @version 1.0
 */
public class Train {
    
    private final String trainId;
    private final List<Facility> route;
    private final List<Locomotive> locomotives;
    private final LocalDateTime departurePlannedDateTime;
    private final double totalWeight;
    private final Integer priority;

    /**
     * Constructs a new Train with the specified parameters.
     *
     * @param trainId                  the unique identifier for the train
     * @param route                    the ordered list of stations (each pair directly connected)
     * @param locomotives              the list of locomotives (with speed and power)
     * @param departurePlannedDateTime the planned departure date and time
     * @param totalWeight              the total weight of the train in kg
     * @param priority                 the priority of the train (optional, can be null)
     * @throws IllegalArgumentException if parameters are invalid
     */
    public Train(String trainId, List<Facility> route, List<Locomotive> locomotives,
                 LocalDateTime departurePlannedDateTime, double totalWeight, Integer priority) {
        if (trainId == null || trainId.trim().isEmpty()) {
            throw new IllegalArgumentException("Train ID cannot be null or empty");
        }
        if (route == null || route.size() < 2) {
            throw new IllegalArgumentException("Route must contain at least 2 stations");
        }
        if (locomotives == null || locomotives.isEmpty()) {
            throw new IllegalArgumentException("Train must have at least one locomotive");
        }
        if (departurePlannedDateTime == null) {
            throw new IllegalArgumentException("Departure planned date time cannot be null");
        }
        if (totalWeight < 0) {
            throw new IllegalArgumentException("Total weight cannot be negative");
        }
        
        this.trainId = trainId.trim();
        this.route = List.copyOf(route); // Immutable copy
        this.locomotives = List.copyOf(locomotives); // Immutable copy
        this.departurePlannedDateTime = departurePlannedDateTime;
        this.totalWeight = totalWeight;
        this.priority = priority;
    }

    /**
     * Constructs a new Train without priority (priority set to null).
     *
     * @param trainId                  the unique identifier for the train
     * @param route                    the ordered list of stations
     * @param locomotives              the list of locomotives
     * @param departurePlannedDateTime the planned departure date and time
     * @param totalWeight              the total weight of the train in kg
     */
    public Train(String trainId, List<Facility> route, List<Locomotive> locomotives,
                 LocalDateTime departurePlannedDateTime, double totalWeight) {
        this(trainId, route, locomotives, departurePlannedDateTime, totalWeight, null);
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
     * Gets the route (ordered list of stations).
     *
     * @return the route
     */
    public List<Facility> getRoute() {
        return route;
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
     * Gets the priority of the train.
     *
     * @return the priority, or null if not set
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * Checks if this train has a priority set.
     *
     * @return true if priority is set, false otherwise
     */
    public boolean hasPriority() {
        return priority != null;
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
        return String.format("Train[%s, Route: %d stations, Locomotives: %d, Departure: %s, Weight: %.0f kg%s]",
                           trainId, route.size(), locomotives.size(), departurePlannedDateTime,
                           totalWeight, priority != null ? ", Priority: " + priority : "");
    }
}

