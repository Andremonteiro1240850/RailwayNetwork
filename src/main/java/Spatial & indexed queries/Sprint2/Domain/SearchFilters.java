package Sprint2.Domain;

import Sprint2.Domain.ENUM.Country;

/**
 * Represents optional filters for geographical search (USEI08).
 * All filters are optional - null means "no filter" (all values accepted).
 * 
 * @author Development Team
 * @version 1.0
 */
public class SearchFilters {
    
    private final Boolean isCity;
    private final Boolean isMainStation;
    private final Country country;
    
    /**
     * Constructs SearchFilters with all optional filters.
     *
     * @param isCity        filter by city status (true/false/null for all)
     * @param isMainStation filter by main station status (true/false/null for all)
     * @param country       filter by country (PT/ES/null for all)
     */
    public SearchFilters(Boolean isCity, Boolean isMainStation, Country country) {
        this.isCity = isCity;
        this.isMainStation = isMainStation;
        this.country = country;
    }
    
    /**
     * Constructs SearchFilters with no filters (all null).
     */
    public SearchFilters() {
        this(null, null, null);
    }
    
    /**
     * Gets the city filter.
     *
     * @return true/false for specific filter, null for no filter
     */
    public Boolean getIsCity() {
        return isCity;
    }
    
    /**
     * Gets the main station filter.
     *
     * @return true/false for specific filter, null for no filter
     */
    public Boolean getIsMainStation() {
        return isMainStation;
    }
    
    /**
     * Gets the country filter.
     *
     * @return Country enum for specific country, null for all countries
     */
    public Country getCountry() {
        return country;
    }
    
    /**
     * Checks if a station matches all active filters.
     * A station matches if:
     * - All specified filters are satisfied, OR
     * - No filters are set (all filters are null)
     * 
     * Filter logic:
     * - Country filter: Station's country must match the specified country
     * - City filter: Station's city status must match the specified value
     * - Main station filter: Station's main station status must match the specified value
     *
     * @param station the station to check against the filters
     * @return true if the station matches all active filters, false otherwise
     */
    public boolean matches(Station station) {
        if (station == null) {
            return false;
        }
        
        if (country != null) {
            Country stationCountry = Country.fromString(station.getCountry());
            if (stationCountry != country) {
                return false;
            }
        }
        
        if (isCity != null && station.isCity() != isCity) {
            return false;
        }
        
        if (isMainStation != null && station.isMainStation() != isMainStation) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Checks if any filters are active.
     *
     * @return true if at least one filter is set
     */
    public boolean hasFilters() {
        return isCity != null || isMainStation != null || country != null;
    }
    
    /**
     * Returns a human-readable string representation of the filters.
     * Shows only the active (non-null) filters.
     *
     * @return string describing the active filters
     */
    @Override
    public String toString() {
        if (!hasFilters()) {
            return "No filters applied";
        }
        
        StringBuilder description = new StringBuilder("Active filters: ");
        boolean isFirstFilter = true;
        
        if (country != null) {
            description.append("Country=").append(country);
            isFirstFilter = false;
        }
        
        if (isCity != null) {
            if (!isFirstFilter) {
                description.append(", ");
            }
            description.append("City=").append(isCity ? "Yes" : "No");
            isFirstFilter = false;
        }
        
        if (isMainStation != null) {
            if (!isFirstFilter) {
                description.append(", ");
            }
            description.append("Main Station=").append(isMainStation ? "Yes" : "No");
        }
        
        return description.toString();
    }
}

