package Domain;

/**
 * Represents a railway line connecting two stations with distance, capacity, and cost.
 *
 * @author Development Team
 * @version 1.0
 */
public class Line {
    private final String departureStationId;
    private final String arrivalStationId;
    private final double distance; // Distance in kilometers
    private final int capacity;    // Maximum flow of trains per planning day
    private final double cost;     // Combined metric (may be negative)
    
    /**
     * Constructs a new Line.
     *
     * @param departureStationId the ID of the departure station
     * @param arrivalStationId   the ID of the arrival station
     * @param distance           the distance in kilometers
     * @param capacity           the maximum flow capacity per planning day
     * @param cost               the cost metric (may be negative)
     */
    public Line(String departureStationId, String arrivalStationId, double distance, int capacity, double cost) {
        if (departureStationId == null || departureStationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Departure station ID cannot be null or empty");
        }
        if (arrivalStationId == null || arrivalStationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Arrival station ID cannot be null or empty");
        }
        if (departureStationId.equals(arrivalStationId)) {
            throw new IllegalArgumentException("Departure and arrival stations cannot be the same");
        }
        if (distance < 0) {
            throw new IllegalArgumentException("Distance cannot be negative");
        }
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity cannot be negative");
        }
        
        this.departureStationId = departureStationId.trim();
        this.arrivalStationId = arrivalStationId.trim();
        this.distance = distance;
        this.capacity = capacity;
        this.cost = cost;
    }
    
    /**
     * Gets the departure station ID.
     *
     * @return the departure station ID
     */
    public String getDepartureStationId() {
        return departureStationId;
    }
    
    /**
     * Gets the arrival station ID.
     *
     * @return the arrival station ID
     */
    public String getArrivalStationId() {
        return arrivalStationId;
    }
    
    /**
     * Gets the distance in kilometers.
     *
     * @return the distance
     */
    public double getDistance() {
        return distance;
    }
    
    /**
     * Gets the maximum flow capacity per planning day.
     *
     * @return the capacity
     */
    public int getCapacity() {
        return capacity;
    }
    
    /**
     * Gets the cost metric.
     *
     * @return the cost (may be negative)
     */
    public double getCost() {
        return cost;
    }

    @Override
    public String toString() {
        // Importa java.util.Locale no topo da classe
        return String.format(java.util.Locale.US, "Line{departure='%s', arrival='%s', dist=%.2f km, capacity=%d, cost=%.2f}",
                departureStationId, arrivalStationId, distance, capacity, cost);
    }
}
