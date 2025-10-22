package Domain;

/**
 * Represents a locomotive in the railway system.
 * A locomotive has specifications including type, maximum speed, and track gauge compatibility.
 * 
 * @author Development Team
 * @version 1.0
 */
public class Locomotive {
    
    private final String id;
    private final String type;
    private final double maxSpeedKmH;
    private final double gauge;
    
    /**
     * Constructs a new Locomotive with the specified parameters.
     * 
     * @param id the unique identifier for the locomotive
     * @param type the type of locomotive ("diesel" or "electric")
     * @param maxSpeedKmH the maximum speed in kilometers per hour
     * @param gauge the track gauge in millimeters
     * @throws IllegalArgumentException if parameters are invalid
     */
    public Locomotive(String id, String type, double maxSpeedKmH, double gauge) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Locomotive ID cannot be null or empty");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Locomotive type cannot be null or empty");
        }
        if (maxSpeedKmH <= 0) {
            throw new IllegalArgumentException("Maximum speed must be positive");
        }
        if (gauge <= 0) {
            throw new IllegalArgumentException("Gauge must be positive");
        }
        
        this.id = id.trim();
        this.type = type.trim().toLowerCase();
        this.maxSpeedKmH = maxSpeedKmH;
        this.gauge = gauge;
    }
    
    /**
     * Gets the locomotive ID.
     * 
     * @return the locomotive ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Gets the locomotive type.
     * 
     * @return the locomotive type
     */
    public String getType() {
        return type;
    }
    
    /**
     * Gets the maximum speed in kilometers per hour.
     * 
     * @return the maximum speed in km/h
     */
    public double getMaxSpeedKmH() {
        return maxSpeedKmH;
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
     * Checks if this locomotive is electric.
     * 
     * @return true if electric, false otherwise
     */
    public boolean isElectric() {
        return "electric".equals(type);
    }
    
    /**
     * Checks if this locomotive is diesel.
     * 
     * @return true if diesel, false otherwise
     */
    public boolean isDiesel() {
        return "diesel".equals(type);
    }
    
    /**
     * Checks if this locomotive's gauge is compatible with the given gauge.
     * 
     * @param otherGauge the gauge to check compatibility with
     * @return true if gauges are compatible (within 1mm tolerance), false otherwise
     */
    public boolean isGaugeCompatible(double otherGauge) {
        return Math.abs(this.gauge - otherGauge) <= 1.0;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Locomotive that = (Locomotive) obj;
        return id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s, %.0f km/h, %.0fmm gauge)", 
                           id, type, maxSpeedKmH, gauge);
    }
}
