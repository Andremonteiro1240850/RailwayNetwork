package Controller;

import Domain.Station;
import Domain.KDTree;
import Domain.SearchFilters;
import Domain.ENUM.Country;
import Repository.StationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for geographical search operations.
 * Handles range queries, nearest neighbor search, and radius search on railway stations using KD-tree for efficient search.
 * 
 * @author Development Team
 * @version 2.0
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
     * Constructs a new GeographicalSearchController from an existing KD-tree.
     * Uses the 2D-tree built in USEI07 - does NOT build a new tree.
     *
     * @param kdTree the KD-tree built in USEI07
     * @param stationRepository the StationRepository to get station list
     */
    public GeographicalSearchController(KDTree kdTree, StationRepository stationRepository) {
        if (kdTree == null) {
            throw new IllegalArgumentException("KDTree cannot be null");
        }
        if (stationRepository == null) {
            throw new IllegalArgumentException("StationRepository cannot be null");
        }
        
        // Use the existing KD-tree from USEI07
        this.kdTree = kdTree;
        
        // Get all stations from repository
        this.allStations = stationRepository.getAllStations();
    }
    
    /**
     * Constructs a new GeographicalSearchController from a KDTreeController.
     * Uses the 2D-tree built in USEI07 - does NOT build a new tree.
     * This is the preferred constructor for USEI08.
     *
     * @param kdTreeController the KDTreeController from USEI07 (must have tree built)
     * @param stationRepository the StationRepository to get station list
     */
    public GeographicalSearchController(KDTreeController kdTreeController, StationRepository stationRepository) {
        if (kdTreeController == null) {
            throw new IllegalArgumentException("KDTreeController cannot be null");
        }
        if (stationRepository == null) {
            throw new IllegalArgumentException("StationRepository cannot be null");
        }
        if (!kdTreeController.isTreeBuilt()) {
            throw new IllegalStateException(
                "KD-tree not built yet. Please build the KD-tree in USEI07 (option 2) first."
            );
        }
        
        // Use the existing KD-tree from USEI07
        this.kdTree = kdTreeController.getKDTree();
        
        // Get all stations from repository
        this.allStations = stationRepository.getAllStations();
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
    
    /**
     * Finds the N nearest stations to a target point.
     * Uses KD-tree for efficient nearest neighbor search.
     *
     * @param targetLat target latitude (must be between -90 and 90)
     * @param targetLon target longitude (must be between -180 and 180)
     * @param n number of nearest stations to find (must be positive)
     * @param filters optional filters to refine the search (can be null)
     * @return list of pairs (station, distance in km), sorted by distance (ascending)
     * @throws IllegalArgumentException if parameters are invalid
     */
    public List<KDTree.StationDistance> findNearestStations(double targetLat, double targetLon, 
                                                           int n, SearchFilters filters) {
        if (targetLat < -90 || targetLat > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (targetLon < -180 || targetLon > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
        if (n <= 0) {
            throw new IllegalArgumentException("n must be positive");
        }
        
        // Get nearest stations from KD-tree
        List<KDTree.StationDistance> nearestStations = kdTree.nearestN(targetLat, targetLon, n);
        
        // Apply filters if provided
        if (filters != null && filters.hasFilters()) {
            return nearestStations.stream()
                .filter(sd -> filters.matches(sd.getStation()))
                .collect(Collectors.toList());
        }
        
        return nearestStations;
    }
    
    /**
     * Finds the N nearest stations to a target point without filters.
     *
     * @param targetLat target latitude
     * @param targetLon target longitude
     * @param n number of nearest stations to find
     * @return list of pairs (station, distance in km), sorted by distance (ascending)
     */
    public List<KDTree.StationDistance> findNearestStations(double targetLat, double targetLon, int n) {
        return findNearestStations(targetLat, targetLon, n, null);
    }
    
    /**
     * Finds all stations within a specified radius of a target point.
     * Uses KD-tree for efficient radius search.
     *
     * @param targetLat target latitude (must be between -90 and 90)
     * @param targetLon target longitude (must be between -180 and 180)
     * @param radiusKm search radius in kilometers (must be non-negative)
     * @param filters optional filters to refine the search (can be null)
     * @return list of pairs (station, distance in km), sorted by distance (ascending)
     * @throws IllegalArgumentException if parameters are invalid
     */
    public List<KDTree.StationDistance> findStationsInRadius(double targetLat, double targetLon, 
                                                              double radiusKm, SearchFilters filters) {
        if (targetLat < -90 || targetLat > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (targetLon < -180 || targetLon > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
        if (radiusKm < 0) {
            throw new IllegalArgumentException("Radius must be non-negative");
        }
        
        // Get stations in radius from KD-tree
        List<KDTree.StationDistance> stationsInRadius = kdTree.radiusSearch(targetLat, targetLon, radiusKm);
        
        // Apply filters if provided
        if (filters != null && filters.hasFilters()) {
            return stationsInRadius.stream()
                .filter(sd -> filters.matches(sd.getStation()))
                .collect(Collectors.toList());
        }
        
        return stationsInRadius;
    }
    
    /**
     * Finds all stations within a specified radius of a target point without filters.
     *
     * @param targetLat target latitude
     * @param targetLon target longitude
     * @param radiusKm search radius in kilometers
     * @return list of pairs (station, distance in km), sorted by distance (ascending)
     */
    public List<KDTree.StationDistance> findStationsInRadius(double targetLat, double targetLon, double radiusKm) {
        return findStationsInRadius(targetLat, targetLon, radiusKm, null);
    }
}

