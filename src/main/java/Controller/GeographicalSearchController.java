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
     * This allows using a balanced tree.
     *
     * @param kdTree the KD-tree to use (can be balanced)
     * @param stations list of all stations (for fallback or additional operations)
     */
    public GeographicalSearchController(KDTree kdTree, List<Station> stations) {
        this.kdTree = kdTree;
        this.allStations = new ArrayList<>(stations);
    }
    
    /**
     * Searches for stations within a geographical rectangle with optional filters.
     * Uses KD-tree pruning to efficiently find stations without scanning the entire dataset.
     * 
     * The search process:
     * 1. Validates that the geographical boundaries are within valid ranges
     * 2. Performs a range query on the KD-tree (uses spatial pruning for efficiency)
     * 3. Applies optional filters (country, city status, main station status) if provided
     * 4. Returns the filtered and sorted list of matching stations
     *
     * @param latMin minimum latitude (inclusive, must be between -90 and 90)
     * @param latMax maximum latitude (inclusive, must be between -90 and 90)
     * @param lonMin minimum longitude (inclusive, must be between -180 and 180)
     * @param lonMax maximum longitude (inclusive, must be between -180 and 180)
     * @param filters optional filters to refine the search (can be null)
     * @return list of matching stations, sorted alphabetically by name
     * @throws IllegalArgumentException if the geographical range is invalid
     */
    public List<Station> searchByGeographicalArea(double latMin, double latMax, 
                                                   double lonMin, double lonMax,
                                                   SearchFilters filters) {
        if (latMin < -90 || latMax > 90 || latMin > latMax) {
            throw new IllegalArgumentException(
                "Invalid latitude range: [" + latMin + ", " + latMax + "]. " +
                "Latitude must be between -90 and 90, and minimum must be <= maximum.");
        }
        
        if (lonMin < -180 || lonMax > 180 || lonMin > lonMax) {
            throw new IllegalArgumentException(
                "Invalid longitude range: [" + lonMin + ", " + lonMax + "]. " +
                "Longitude must be between -180 and 180, and minimum must be <= maximum.");
        }
        
        List<Station> stationsInRange = kdTree.rangeQuery(latMin, latMax, lonMin, lonMax);
        
        if (filters != null && filters.hasFilters()) {
            List<Station> filteredStations = new ArrayList<>();
            for (Station station : stationsInRange) {
                if (filters.matches(station)) {
                    filteredStations.add(station);
                }
            }
            return filteredStations;
        }
        
        return stationsInRange;
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
     * Gets tree statistics for analysis and debugging.
     * Provides information about the KD-tree structure.
     *
     * @return formatted string with tree size (number of nodes) and height
     */
    public String getTreeStatistics() {
        int treeSize = kdTree.size();
        int treeHeight = kdTree.height();
        return String.format("Size: %d nodes, Height: %d levels", treeSize, treeHeight);
    }
    
    /**
     * Gets the KD-tree.
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

