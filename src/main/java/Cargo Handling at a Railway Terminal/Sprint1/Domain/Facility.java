package Sprint1.Domain;

import Sprint2.Domain.ENUM.Country;

/**
 * Represents a railway station in the logistics system.
 * A station is identified by a unique ID and has a name and optional location.
 * Extended for USEI08 to support geographical search with coordinates and filters.
 *
 * @author Development Team
 * @version 2.0
 */
public class Facility {
    
    private final String id;
    private final String name;
    private final String location;
    
    // Geographical data for USEI08
    private Double latitude;
    private Double longitude;
    private Country country;
    private Boolean isCity;
    private Boolean isMainStation;
    private String timeZone;
    private String timeZoneGroup;

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
     * Constructs a new Station with full geographical data for USEI08.
     *
     * @param id            the unique identifier for the station
     * @param name          the name of the station
     * @param location      the location of the station (optional)
     * @param latitude      the latitude coordinate [-90, 90]
     * @param longitude     the longitude coordinate [-180, 180]
     * @param country       the country (PT, ES, or null for all)
     * @param isCity        true if station is in a city
     * @param isMainStation true if this is a main station
     * @param timeZone      the time zone
     * @param timeZoneGroup the time zone group
     * @throws IllegalArgumentException if id or name are null/empty, or coordinates are invalid
     */
    public Facility(String id, String name, String location, 
                   Double latitude, Double longitude, Country country,
                   Boolean isCity, Boolean isMainStation,
                   String timeZone, String timeZoneGroup) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Station ID cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Station name cannot be null or empty");
        }
        
        // Validate coordinates if provided
        if (latitude != null && (latitude < -90 || latitude > 90)) {
            throw new IllegalArgumentException("Latitude must be in range [-90, 90]");
        }
        if (longitude != null && (longitude < -180 || longitude > 180)) {
            throw new IllegalArgumentException("Longitude must be in range [-180, 180]");
        }
        
        this.id = id.trim();
        this.name = name.trim();
        this.location = location != null ? location.trim() : "";
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country;
        this.isCity = isCity;
        this.isMainStation = isMainStation;
        this.timeZone = timeZone;
        this.timeZoneGroup = timeZoneGroup;
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
    
    // Getters for geographical data (USEI08)
    
    /**
     * Gets the latitude coordinate.
     *
     * @return the latitude, or null if not set
     */
    public Double getLatitude() {
        return latitude;
    }
    
    /**
     * Gets the longitude coordinate.
     *
     * @return the longitude, or null if not set
     */
    public Double getLongitude() {
        return longitude;
    }
    
    /**
     * Gets the country.
     *
     * @return the country, or null if not set
     */
    public Country getCountry() {
        return country;
    }
    
    /**
     * Checks if this station is in a city.
     *
     * @return true if in a city, false otherwise, or null if not set
     */
    public Boolean isCity() {
        return isCity;
    }
    
    /**
     * Checks if this is a main station.
     *
     * @return true if main station, false otherwise, or null if not set
     */
    public Boolean isMainStation() {
        return isMainStation;
    }
    
    /**
     * Gets the time zone.
     *
     * @return the time zone, or null if not set
     */
    public String getTimeZone() {
        return timeZone;
    }
    
    /**
     * Gets the time zone group.
     *
     * @return the time zone group, or null if not set
     */
    public String getTimeZoneGroup() {
        return timeZoneGroup;
    }
    
    /**
     * Checks if this station has geographical coordinates.
     *
     * @return true if both latitude and longitude are set
     */
    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }
    
    /**
     * Checks if this station has a siding (desvio) for train crossings.
     * A siding allows trains to pass each other on single-track segments.
     * By default, main stations are assumed to have sidings.
     *
     * @return true if the station has a siding, false otherwise
     */
    public boolean hasSiding() {
        // Main stations typically have sidings for train crossings
        return isMainStation != null && isMainStation;
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
    
    /**
     * Returns a detailed string representation including geographical data.
     *
     * @return detailed string with coordinates and country
     */
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s (%s)", name, id));
        
        if (hasCoordinates()) {
            sb.append(String.format(" - [%.6f, %.6f]", latitude, longitude));
        }
        
        if (country != null) {
            sb.append(" - ").append(country);
        }
        
        if (isCity != null && isCity) {
            sb.append(" [City]");
        }
        
        if (isMainStation != null && isMainStation) {
            sb.append(" [Main]");
        }
        
        return sb.toString();
    }
}
