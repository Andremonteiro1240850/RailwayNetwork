package Controller;

import Domain.Station;
import Domain.KDTree;
import Domain.SearchFilters;
import Domain.ENUM.Country;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for geographical search operations (USEI08).
 * Handles range queries on railway stations using KD-tree for efficient search.
 * 
 * @author Development Team
 * @version 1.0
 */
public class GeographicalSearchController {
    
    private final KDTree kdTree;
    private final List<Station> allStations;
    
    /**
     * Constructs a new GeographicalSearchController with a KD-tree.
     *
     * @param stations list of stations to build the tree from
     */
    public GeographicalSearchController(List<Station> stations) {
        this.allStations = new ArrayList<>(stations);
        this.kdTree = new KDTree(stations);
    }
    
    /**
     * Constructs a new GeographicalSearchController with an existing KD-tree.
     * This allows using a balanced tree built in USEI07.
     *
     * @param kdTree the KD-tree to use (can be balanced from USEI07)
     * @param stations list of all stations (for fallback or additional operations)
     */
    public GeographicalSearchController(KDTree kdTree, List<Station> stations) {
        this.kdTree = kdTree;
        this.allStations = new ArrayList<>(stations);
    }
    
    /**
     * Searches for stations within a geographical rectangle with optional filters.
     * Uses KD-tree pruning to avoid scanning the whole dataset.
     *
     * @param latMin minimum latitude (inclusive)
     * @param latMax maximum latitude (inclusive)
     * @param lonMin minimum longitude (inclusive)
     * @param lonMax maximum longitude (inclusive)
     * @param filters optional filters (isCity, isMainStation, country)
     * @return list of matching stations, sorted by name
     * @throws IllegalArgumentException if range is invalid
     */
    public List<Station> searchByGeographicalArea(double latMin, double latMax, 
                                                   double lonMin, double lonMax,
                                                   SearchFilters filters) {
        // Validate range
        if (latMin < -90 || latMax > 90 || latMin > latMax) {
            throw new IllegalArgumentException("Invalid latitude range: [" + latMin + ", " + latMax + "]");
        }
        if (lonMin < -180 || lonMax > 180 || lonMin > lonMax) {
            throw new IllegalArgumentException("Invalid longitude range: [" + lonMin + ", " + lonMax + "]");
        }
        
        // Perform range query using KD-tree (efficient pruning)
        List<Station> results = kdTree.rangeQuery(latMin, latMax, lonMin, lonMax);
        
        // Apply filters if provided
        if (filters != null && filters.hasFilters()) {
            List<Station> filteredResults = new ArrayList<>();
            for (Station station : results) {
                if (filters.matches(station)) {
                    filteredResults.add(station);
                }
            }
            return filteredResults;
        }
        
        return results;
    }
    
    /**
     * Searches for stations within a geographical rectangle without filters.
     *
     * @param latMin minimum latitude (inclusive)
     * @param latMax maximum latitude (inclusive)
     * @param lonMin minimum longitude (inclusive)
     * @param lonMax maximum longitude (inclusive)
     * @return list of matching stations, sorted by name
     */
    public List<Station> searchByGeographicalArea(double latMin, double latMax, 
                                                   double lonMin, double lonMax) {
        return searchByGeographicalArea(latMin, latMax, lonMin, lonMax, null);
    }
    
    /**
     * Gets tree statistics for analysis.
     *
     * @return string with tree size and height
     */
    public String getTreeStatistics() {
        return String.format("KD-Tree: Size=%d, Height=%d", kdTree.size(), kdTree.height());
    }
    
    /**
     * Gets the KD-tree (for integration with USEI07).
     *
     * @return the KD-tree
     */
    public KDTree getKDTree() {
        return kdTree;
    }
    
    /**
     * Gets all stations.
     *
     * @return list of all stations
     */
    public List<Station> getAllStations() {
        return new ArrayList<>(allStations);
    }
    
    /**
     * Gets the number of stations.
     *
     * @return number of stations
     */
    public int getStationsCount() {
        return allStations.size();
    }
}

