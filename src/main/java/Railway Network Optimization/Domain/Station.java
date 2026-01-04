package Domain;

/**
 * Represents a railway station with its geographical coordinates and XY coordinates for visualization.
 *
 * @author Development Team
 * @version 1.0
 */
public class Station {
    private final String id;
    private final String name;
    private final double latitude;
    private final double longitude;
    private final double coordX;
    private final double coordY;
    
    /**
     * Constructs a new Station.
     *
     * @param id        the unique station identifier
     * @param name      the station name
     * @param latitude  the latitude coordinate
     * @param longitude the longitude coordinate
     * @param coordX    the X coordinate for SVG visualization
     * @param coordY    the Y coordinate for SVG visualization
     */
    public Station(String id, String name, double latitude, double longitude, double coordX, double coordY) {
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
        this.coordX = coordX;
        this.coordY = coordY;
    }
    private Station st(String id) {
        return new Station(
                id,
                "Station " + id,
                0.0, 0.0,   // latitude, longitude
                0.0, 0.0    // coordX, coordY
        );
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
    public double getCoordX() {
        return coordX;
    }
    
    /**
     * Gets the Y coordinate for SVG visualization.
     *
     * @return the Y coordinate
     */
    public double getCoordY() {
        return coordY;
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
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("Station{id='%s', name='%s', lat=%.6f, lon=%.6f, x=%.2f, y=%.2f}",
                id, name, latitude, longitude, coordX, coordY);
    }
}
