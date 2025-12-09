package Sprint1.Domain;

/**
 * Represents a railway wagon (freight car) in the railway system.
 * A wagon has specifications including type, empty weight, and maximum load capacity.
 *
 * @author Development Team
 * @version 1.0
 */
public class Wagon {
    
    private final String wagonId;
    private final String type;
    private final double emptyWeightKg;
    private final double maxLoadWeightKg;

    /**
     * Constructs a new Wagon with the specified parameters.
     *
     * @param wagonId         the unique identifier for the wagon
     * @param type            the type of wagon (e.g., "freight", "tank", "flat")
     * @param emptyWeightKg   the empty weight of the wagon in kilograms
     * @param maxLoadWeightKg the maximum load capacity in kilograms
     * @throws IllegalArgumentException if parameters are invalid
     */
    public Wagon(String wagonId, String type, double emptyWeightKg, double maxLoadWeightKg) {
        if (wagonId == null || wagonId.trim().isEmpty()) {
            throw new IllegalArgumentException("Wagon ID cannot be null or empty");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Wagon type cannot be null or empty");
        }
        if (emptyWeightKg <= 0) {
            throw new IllegalArgumentException("Empty weight must be positive");
        }
        if (maxLoadWeightKg <= 0) {
            throw new IllegalArgumentException("Maximum load weight must be positive");
        }
        
        this.wagonId = wagonId.trim();
        this.type = type.trim().toLowerCase();
        this.emptyWeightKg = emptyWeightKg;
        this.maxLoadWeightKg = maxLoadWeightKg;
    }

    /**
     * Gets the wagon ID.
     *
     * @return the wagon ID
     */
    public String getWagonId() {
        return wagonId;
    }

    /**
     * Gets the wagon type.
     *
     * @return the wagon type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the empty weight of the wagon in kilograms.
     *
     * @return the empty weight in kg
     */
    public double getEmptyWeightKg() {
        return emptyWeightKg;
    }

    /**
     * Gets the maximum load capacity in kilograms.
     *
     * @return the maximum load weight in kg
     */
    public double getMaxLoadWeightKg() {
        return maxLoadWeightKg;
    }

    /**
     * Calculates the total weight when the wagon is fully loaded.
     *
     * @return the total weight (empty + max load) in kg
     */
    public double getFullyLoadedWeightKg() {
        return emptyWeightKg + maxLoadWeightKg;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Wagon that = (Wagon) obj;
        return wagonId.equals(that.wagonId);
    }
    
    @Override
    public int hashCode() {
        return wagonId.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s, empty: %.0f kg, max load: %.0f kg)", 
                           wagonId, type, emptyWeightKg, maxLoadWeightKg);
    }
}

