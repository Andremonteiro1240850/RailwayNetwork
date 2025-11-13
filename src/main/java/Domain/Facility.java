package Domain;

/**
 * Represents a railway station in the logistics system.
 * A station is identified by a unique ID and has a name and optional location.
 *
 * @author Development Team
 * @version 1.0
 */
public class Facility {
    
    private final String id;
    private final String name;
    private final String location;

    /**
     * Constructs a new Station with the specified parameters.
     *
     * @param id       the unique identifier for the station
     * @param name     the name of the station
     * @param location the location of the station (optional)
     * @throws IllegalArgumentException if id or name are null or empty
     */
    public Facility(String id, String name, String location) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Station ID cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Station name cannot be null or empty");
        }
        
        this.id = id.trim();
        this.name = name.trim();
        this.location = location != null ? location.trim() : "";
    }

    /**
     * Constructs a new Station with ID and name only.
     *
     * @param id   the unique identifier for the station
     * @param name the name of the station
     */
    public Facility(String id, String name) {
        this(id, name, null);
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
     * Gets the station location.
     *
     * @return the station location, empty string if not specified
     */
    public String getLocation() {
        return location;
    }

    /**
     * Checks if this station has a location specified.
     *
     * @return true if location is specified, false otherwise
     */
    public boolean hasLocation() {
        return !location.isEmpty();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Facility facility = (Facility) obj;
        return id.equals(facility.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        if (hasLocation()) {
            return String.format("%s (%s) - %s", name, id, location);
        }
        return String.format("%s (%s)", name, id);
    }
}
