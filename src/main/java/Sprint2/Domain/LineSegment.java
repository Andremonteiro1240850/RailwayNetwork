package Sprint2.Domain;

import Sprint1.Domain.Facility;

/**
 * Represents a railway line segment connecting two stations.
 * A line segment has specifications including length, gauge, and electrification status.
 *
 * @author Development Team
 * @version 1.0
 */
public class LineSegment {
    
    private final String id;
    private final String name;
    private final Facility startFacility;
    private final Facility endFacility;
    private final double lengthKm;
    private final double gauge;
    private final boolean electrified;
    private final Double speedLimitKmH; // Speed limit of the track segment (null if unlimited)

    /**
     * Constructs a new LineSegment with the specified parameters.
     *
     * @param id           the unique identifier for the line segment
     * @param startFacility the starting station
     * @param endFacility   the ending station
     * @param lengthKm     the length of the segment in kilometers
     * @param gauge        the track gauge in millimeters
     * @param electrified  whether the segment is electrified
     * @throws IllegalArgumentException if parameters are invalid
     */
    public LineSegment(String id, Facility startFacility, Facility endFacility,
                       double lengthKm, double gauge, boolean electrified) {
        this(id, "", startFacility, endFacility, lengthKm, gauge, electrified, null);
    }

    /**
     * Constructs a new LineSegment with the specified parameters including name.
     *
     * @param id           the unique identifier for the line segment
     * @param name         the name of the line segment
     * @param startFacility the starting station
     * @param endFacility   the ending station
     * @param lengthKm     the length of the segment in kilometers
     * @param gauge        the track gauge in millimeters
     * @param electrified  whether the segment is electrified
     * @throws IllegalArgumentException if parameters are invalid
     */
    public LineSegment(String id, String name, Facility startFacility, Facility endFacility,
                       double lengthKm, double gauge, boolean electrified) {
        this(id, name, startFacility, endFacility, lengthKm, gauge, electrified, null);
    }

    /**
     * Constructs a new LineSegment with all parameters including speed limit.
     *
     * @param id           the unique identifier for the line segment
     * @param name         the name of the line segment
     * @param startFacility the starting station
     * @param endFacility   the ending station
     * @param lengthKm     the length of the segment in kilometers
     * @param gauge        the track gauge in millimeters
     * @param electrified  whether the segment is electrified
     * @param speedLimitKmH the speed limit in km/h (null if unlimited)
     * @throws IllegalArgumentException if parameters are invalid
     */
    public LineSegment(String id, String name, Facility startFacility, Facility endFacility,
                       double lengthKm, double gauge, boolean electrified, Double speedLimitKmH) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Line segment ID cannot be null or empty");
        }
        if (startFacility == null) {
            throw new IllegalArgumentException("Start station cannot be null");
        }
        if (endFacility == null) {
            throw new IllegalArgumentException("End station cannot be null");
        }
        if (startFacility.equals(endFacility)) {
            throw new IllegalArgumentException("Start and end stations cannot be the same");
        }
        if (lengthKm <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }
        if (gauge <= 0) {
            throw new IllegalArgumentException("Gauge must be positive");
        }
        if (speedLimitKmH != null && speedLimitKmH <= 0) {
            throw new IllegalArgumentException("Speed limit must be positive if specified");
        }
        
        this.id = id.trim();
        this.name = name != null ? name.trim() : "";
        this.startFacility = startFacility;
        this.endFacility = endFacility;
        this.lengthKm = lengthKm;
        this.gauge = gauge;
        this.electrified = electrified;
        this.speedLimitKmH = speedLimitKmH;
    }

    /**
     * Gets the line segment ID.
     *
     * @return the line segment ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the line segment name.
     *
     * @return the line segment name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the starting station.
     *
     * @return the starting station
     */
    public Facility getStartStation() {
        return startFacility;
    }

    /**
     * Gets the ending station.
     *
     * @return the ending station
     */
    public Facility getEndStation() {
        return endFacility;
    }

    /**
     * Gets the length of the segment in kilometers.
     *
     * @return the length in km
     */
    public double getLengthKm() {
        return lengthKm;
    }

    /**
     * Gets the track gauge in millimeters.
     *
     * @return the gauge in mm
     */
    public double getGauge() {
        return gauge;
    }

    /**
     * Checks if the segment is electrified.
     *
     * @return true if electrified, false otherwise
     */
    public boolean isElectrified() {
        return electrified;
    }

    /**
     * Gets the speed limit of the track segment.
     *
     * @return the speed limit in km/h, or null if unlimited
     */
    public Double getSpeedLimitKmH() {
        return speedLimitKmH;
    }

    /**
     * Checks if this segment has a speed limit.
     *
     * @return true if speed limit is set, false otherwise
     */
    public boolean hasSpeedLimit() {
        return speedLimitKmH != null;
    }

    /**
     * Checks if this segment connects the two given stations.
     *
     * @param facility1 the first station
     * @param facility2 the second station
     * @return true if this segment connects the stations, false otherwise
     */
    public boolean connectsStations(Facility facility1, Facility facility2) {
        if (facility1 == null || facility2 == null) {
            return false;
        }
        
        return (startFacility.equals(facility1) && endFacility.equals(facility2)) ||
               (startFacility.equals(facility2) && endFacility.equals(facility1));
    }

    /**
     * Checks if this segment's gauge is compatible with the given gauge.
     *
     * @param otherGauge the gauge to check compatibility with
     * @return true if gauges are compatible (within 1mm tolerance), false otherwise
     */
    public boolean isGaugeCompatible(double otherGauge) {
        return Math.abs(this.gauge - otherGauge) <= 1.0;
    }

    /**
     * Gets the other station in this segment.
     *
     * @param facility one of the stations in this segment
     * @return the other station, or null if the given station is not part of this segment
     */
    public Facility getOtherStation(Facility facility) {
        if (facility == null) {
            return null;
        }
        
        if (startFacility.equals(facility)) {
            return endFacility;
        } else if (endFacility.equals(facility)) {
            return startFacility;
        }
        
        return null;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LineSegment that = (LineSegment) obj;
        return id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        String speedLimitStr = hasSpeedLimit() ? String.format(", speed limit: %.0f km/h", speedLimitKmH) : "";
        if (!name.isEmpty()) {
            return String.format("%s (%s): %s -> %s (%.2f km, %.0fmm gauge, %s%s)", 
                               id, name, startFacility.getName(), endFacility.getName(),
                               lengthKm, gauge, electrified ? "electrified" : "non-electrified", speedLimitStr);
        } else {
            return String.format("%s: %s -> %s (%.2f km, %.0fmm gauge, %s%s)", 
                               id, startFacility.getName(), endFacility.getName(),
                               lengthKm, gauge, electrified ? "electrified" : "non-electrified", speedLimitStr);
        }
    }
}
