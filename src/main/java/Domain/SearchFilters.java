package Domain;

import Domain.ENUM.Country;

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
     *
     * @param station the station to check
     * @return true if station matches all filters (or if no filters are set)
     */
    public boolean matches(Station station) {
        if (station == null) {
            return false;
        }
        
        // Check country filter
        if (country != null) {
            Country stationCountry = Country.fromString(station.getCountry());
            if (stationCountry != country) {
                return false;
            }
        }
        
        // Check isCity filter
        if (isCity != null && station.isCity() != isCity) {
            return false;
        }
        
        // Check isMainStation filter
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
    
    @Override
    public String toString() {
        if (!hasFilters()) {
            return "No filters";
        }
        
        StringBuilder sb = new StringBuilder("Filters: ");
        boolean first = true;
        
        if (country != null) {
            sb.append("Country=").append(country);
            first = false;
        }
        
        if (isCity != null) {
            if (!first) sb.append(", ");
            sb.append("isCity=").append(isCity);
            first = false;
        }
        
        if (isMainStation != null) {
            if (!first) sb.append(", ");
            sb.append("isMainStation=").append(isMainStation);
        }
        
        return sb.toString();
    }
}

