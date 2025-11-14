package Domain.ENUM;

/**
 * Enum representing countries for geographical search (USEI08).
 * 
 * @author Development Team
 * @version 1.0
 */
public enum Country {
    PT("Portugal"),
    ES("Spain");
    
    private final String name;
    
    Country(String name) {
        this.name = name;
    }
    
    /**
     * Gets the full country name.
     *
     * @return the country name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Parses a country code string to Country enum.
     *
     * @param code the country code (PT, ES, or "all")
     * @return the Country enum, or null if "all" or invalid
     */
    public static Country fromString(String code) {
        if (code == null || code.trim().isEmpty() || code.equalsIgnoreCase("all")) {
            return null;
        }
        
        try {
            return Country.valueOf(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    @Override
    public String toString() {
        return name();
    }
}

