package Domain;

import java.util.Objects;

/**
 * Represents a railway station in the graph with its geographical coordinates
 * and visualization coordinates (x, y) for SVG rendering.
 * 
 * This is a domain model class following Clean Architecture principles.
 * It contains no dependencies on UI or infrastructure layers.
 *
 * @author Development Team
 * @version 1.0
 */
public class Station {
    private final String id;
    private final String name;
    private final double latitude;
    private final double longitude;
    private final double x;
    private final double y;
    
    /**
     * Constructs a new Station.
     *
     * @param id        the unique station identifier (cannot be null or empty)
     * @param name      the station name (cannot be null or empty)
     * @param latitude  the latitude coordinate (must be in range [-90, 90])
     * @param longitude the longitude coordinate (must be in range [-180, 180])
     * @param x         the X coordinate for SVG visualization
     * @param y         the Y coordinate for SVG visualization
     * @throws IllegalArgumentException if any validation fails
     */
    public Station(String id, String name, double latitude, double longitude, double x, double y) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Station ID cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Station name cannot be null or empty");
        }
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be in range [-90, 90]");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be in range [-180, 180]");
        }
        
        this.id = id.trim();
        this.name = name.trim();
        this.latitude = latitude;
        this.longitude = longitude;
        this.x = x;
        this.y = y;
    }
    
    /**
     * Gets the station ID.
     *
     * @return the station ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Gets the station name.
     *
     * @return the station name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the latitude coordinate.
     *
     * @return the latitude
     */
    public double getLatitude() {
        return latitude;
    }
    
    /**
     * Gets the longitude coordinate.
     *
     * @return the longitude
     */
    public double getLongitude() {
        return longitude;
    }
    
    /**
     * Gets the X coordinate for SVG visualization.
     *
     * @return the X coordinate
     */
    public double getX() {
        return x;
    }
    
    /**
     * Gets the Y coordinate for SVG visualization.
     *
     * @return the Y coordinate
     */
    public double getY() {
        return y;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Station station = (Station) obj;
        return id.equals(station.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("Station{id='%s', name='%s', lat=%.6f, lon=%.6f, x=%.2f, y=%.2f}",
                id, name, latitude, longitude, x, y);
    }
}

