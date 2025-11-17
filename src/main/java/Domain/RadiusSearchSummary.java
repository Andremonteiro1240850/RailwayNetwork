package Domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a summary of radius search results aggregated by country and isCity.
 * This class provides statistics about the distribution of stations found within a radius.
 * 
 * @author Development Team
 * @version 1.0
 */
public class RadiusSearchSummary {
    
    private final Map<String, Integer> stationsByCountry;
    private final int stationsByCity;
    private final int stationsByNonCity;
    private final int totalStations;
    
    /**
     * Constructs a new RadiusSearchSummary.
     *
     * @param stationsByCountry map of country code to count of stations
     * @param stationsByCity count of stations that are cities
     * @param stationsByNonCity count of stations that are not cities
     * @param totalStations total number of stations
     */
    public RadiusSearchSummary(Map<String, Integer> stationsByCountry, 
                                int stationsByCity, 
                                int stationsByNonCity, 
                                int totalStations) {
        this.stationsByCountry = new HashMap<>(stationsByCountry);
        this.stationsByCity = stationsByCity;
        this.stationsByNonCity = stationsByNonCity;
        this.totalStations = totalStations;
    }
    
    /**
     * Gets the distribution of stations by country.
     *
     * @return map of country code to count of stations
     */
    public Map<String, Integer> getStationsByCountry() {
        return new HashMap<>(stationsByCountry);
    }
    
    /**
     * Gets the count of stations that are cities.
     *
     * @return count of city stations
     */
    public int getStationsByCity() {
        return stationsByCity;
    }
    
    /**
     * Gets the count of stations that are not cities.
     *
     * @return count of non-city stations
     */
    public int getStationsByNonCity() {
        return stationsByNonCity;
    }
    
    /**
     * Gets the total number of stations.
     *
     * @return total count
     */
    public int getTotalStations() {
        return totalStations;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RadiusSearchSummary{");
        sb.append("total=").append(totalStations);
        sb.append(", cities=").append(stationsByCity);
        sb.append(", non-cities=").append(stationsByNonCity);
        sb.append(", byCountry=").append(stationsByCountry);
        sb.append("}");
        return sb.toString();
    }
}

