package Domain;

import java.util.Objects;

/**
 * Represents a connection (edge) between two stations in the graph.
 * 
 * This is a domain model class following Clean Architecture principles.
 * It contains no dependencies on UI or infrastructure layers.
 *
 * @author Development Team
 * @version 1.0
 */
public class Connection {
    private final String originId;
    private final String destinationId;
    private final double distance;
    
    /**
     * Constructs a new Connection.
     *
     * @param originId      the ID of the origin station (cannot be null or empty)
     * @param destinationId the ID of the destination station (cannot be null or empty)
     * @param distance      the distance between stations (must be non-negative)
     * @throws IllegalArgumentException if any validation fails
     */
    public Connection(String originId, String destinationId, double distance) {
        if (originId == null || originId.trim().isEmpty()) {
            throw new IllegalArgumentException("Origin station ID cannot be null or empty");
        }
        if (destinationId == null || destinationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Destination station ID cannot be null or empty");
        }
        if (originId.equals(destinationId)) {
            throw new IllegalArgumentException("Origin and destination stations cannot be the same");
        }
        if (distance < 0) {
            throw new IllegalArgumentException("Distance cannot be negative");
        }
        
        this.originId = originId.trim();
        this.destinationId = destinationId.trim();
        this.distance = distance;
    }
    
    /**
     * Gets the origin station ID.
     *
     * @return the origin station ID
     */
    public String getOriginId() {
        return originId;
    }
    
    /**
     * Gets the destination station ID.
     *
     * @return the destination station ID
     */
    public String getDestinationId() {
        return destinationId;
    }
    
    /**
     * Gets the distance between the stations.
     *
     * @return the distance
     */
    public double getDistance() {
        return distance;
    }
    
    /**
     * Creates a reversed connection (swaps origin and destination).
     * Useful for undirected graphs where edges are bidirectional.
     *
     * @return a new Connection with swapped origin and destination
     */
    public Connection reverse() {
        return new Connection(destinationId, originId, distance);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Connection that = (Connection) obj;
        return Objects.equals(originId, that.originId) &&
               Objects.equals(destinationId, that.destinationId) &&
               Double.compare(that.distance, distance) == 0;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(originId, destinationId, distance);
    }
    
    @Override
    public String toString() {
        return String.format("Connection{origin='%s', destination='%s', distance=%.2f}",
                originId, destinationId, distance);
    }
}

