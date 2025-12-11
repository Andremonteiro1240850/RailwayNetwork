package Sprint2.Controller;

import Sprint2.Domain.Station;
import Sprint2.Domain.KDTree;
import Sprint2.Domain.SearchFilters;
import Sprint2.Repository.StationRepository;
import Sprint2.Repository.KDTreeRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
     * Note: This constructor builds a new tree using KDTreeRepository.
     * For USEI08, prefer using the constructor that accepts KDTreeRepository
     * to use the tree built in USEI07.
     *
     * @param stations list of stations to build the tree from
     * @param stationRepository the StationRepository (can be null if not needed for tree building)
     */
    public GeographicalSearchController(List<Station> stations, StationRepository stationRepository) {
        if (stations == null || stations.isEmpty()) {
            throw new IllegalArgumentException("Stations list cannot be null or empty");
        }
        this.allStations = new ArrayList<>(stations);
        // Build tree using KDTreeRepository
        // Note: This creates a new tree, not using the one from USEI07
        if (stationRepository != null) {
            KDTreeRepository tempRepo = new KDTreeRepository(stationRepository);
            this.kdTree = tempRepo.buildTree(stations);
        } else {
            // Fallback: create empty tree if no repository provided
            // This should not be used in production
            this.kdTree = new KDTree();
        }
    }
    
    /**
     * Constructs a new GeographicalSearchController with a KD-tree.
     * @deprecated Use constructor with StationRepository parameter instead.
     * For USEI08, use constructor that accepts KDTreeRepository.
     *
     * @param stations list of stations to build the tree from
     */
    @Deprecated
    public GeographicalSearchController(List<Station> stations) {
        this(stations, null);
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
     * This constructor delegates to KDTreeRepository internally.
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
        
        // Use the existing KD-tree from USEI07 (obtained from KDTreeRepository via controller)
        this.kdTree = kdTreeController.getKDTree();
        
        // Get all stations from repository
        this.allStations = stationRepository.getAllStations();
    }
    
    /**
     * Constructs a new GeographicalSearchController from a KDTreeRepository.
     * Uses the 2D-tree built in USEI07 - does NOT build a new tree.
     * This is the preferred constructor for USEI08 as it directly uses the repository.
     *
     * @param kdTreeRepository the KDTreeRepository from USEI07 (must have tree built)
     * @param stationRepository the StationRepository to get station list
     */
    public GeographicalSearchController(KDTreeRepository kdTreeRepository, StationRepository stationRepository) {
        if (kdTreeRepository == null) {
            throw new IllegalArgumentException("KDTreeRepository cannot be null");
        }
        if (stationRepository == null) {
            throw new IllegalArgumentException("StationRepository cannot be null");
        }
        if (!kdTreeRepository.hasCachedTree()) {
            throw new IllegalStateException(
                "KD-tree not built yet. Please build the KD-tree in USEI07 (option 2) first."
            );
        }
        
        // Use the existing KD-tree from USEI07 (directly from repository)
        KDTree tree = kdTreeRepository.getCachedTree();
        if (tree == null || tree.isEmpty()) {
            throw new IllegalStateException(
                "KD-tree is null or empty. Please build the KD-tree in USEI07 (option 2) first."
            );
        }
        this.kdTree = tree;
        
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
     * Time Complexity (Worst Case):
     * - Without filters: O(√n + k log k) where n = total stations, k = results
     *   (KD-tree range query with spatial pruning)
     * - With filters: O(√n + m log m) where k = results in range, m ≤ k = filtered results
     *   (Optimized: filter before sort, reducing sorting from O(k log k) to O(m log m))
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
        
        // Optimized: Apply filters during collection to avoid creating intermediate lists
        // This reduces memory allocation and improves worst-case complexity
        if (filters != null && filters.hasFilters()) {
            // Pre-allocate with estimated capacity to reduce reallocations
            // Estimate: typically 10-20% of stations match a range query
            int estimatedCapacity = Math.max(16, (int)(allStations.size() * 0.15));
            List<Station> filteredResults = new ArrayList<>(estimatedCapacity);
            
            // Get all stations in range WITHOUT sorting first (optimization)
            // Filter first, then sort only the filtered results
            List<Station> stationsInRange = kdTree.rangeQueryUnsorted(latMin, latMax, lonMin, lonMax);
            
            // Filter during single pass - O(k) where k = results in range
            for (Station station : stationsInRange) {
                if (filters.matches(station)) {
                    filteredResults.add(station);
                }
            }
            
            // Sort only the filtered results - O(m log m) where m = filtered results (m ≤ k)
            // This is better than O(k log k) when m << k
            filteredResults.sort(Comparator.comparing(Station::getName));
            return filteredResults;
        }
        
        // No filters: direct range query - O(√n + k log k)
        return kdTree.rangeQuery(latMin, latMax, lonMin, lonMax);
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
     * Time Complexity (Worst Case):
     * - Without filters: O(n log n) where n = parameter (number of nearest stations)
     *   (KD-tree nearest neighbor search with branch-and-bound pruning)
     * - With filters: O(n log n + m) where n = parameter, m = filtered results
     *   (Filtering applied with direct loop, avoiding stream overhead)
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
        
        // Get nearest stations from KD-tree - O(n log n) where n = parameter
        List<KDTree.StationDistance> nearestStations = kdTree.nearestN(targetLat, targetLon, n);
        
        // Optimized: Apply filters with direct loop instead of stream to reduce overhead
        // This avoids stream pipeline creation and intermediate operations
        if (filters != null && filters.hasFilters()) {
            // Pre-allocate with exact size to avoid reallocations
            List<KDTree.StationDistance> filteredResults = new ArrayList<>(nearestStations.size());
            
            // Single pass filtering - O(m) where m = nearestStations.size()
            for (KDTree.StationDistance sd : nearestStations) {
                if (filters.matches(sd.getStation())) {
                    filteredResults.add(sd);
                }
            }
            
            return filteredResults;
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
     * Time Complexity (Worst Case):
     * - Without filters: O(n + k log k) where n = total stations, k = results in radius
     *   (KD-tree radius search with spatial pruning)
     * - With filters: O(n + m log m) where k = results in radius, m ≤ k = filtered results
     *   (Optimized: filter before sort, reducing sorting from O(k log k) to O(m log m))
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
        
        // Optimized: Apply filters with direct loop instead of stream to reduce overhead
        // This avoids stream pipeline creation and intermediate operations
        if (filters != null && filters.hasFilters()) {
            // Get stations in radius WITHOUT sorting first (optimization)
            // Filter first, then sort only the filtered results
            List<KDTree.StationDistance> stationsInRadius = kdTree.radiusSearchUnsorted(targetLat, targetLon, radiusKm);
            
            // Pre-allocate with estimated capacity to reduce reallocations
            // Estimate: typically 50-80% of stations in radius match filters
            int estimatedCapacity = Math.max(16, (int)(stationsInRadius.size() * 0.7));
            List<KDTree.StationDistance> filteredResults = new ArrayList<>(estimatedCapacity);
            
            // Single pass filtering - O(k) where k = stationsInRadius.size()
            for (KDTree.StationDistance sd : stationsInRadius) {
                if (filters.matches(sd.getStation())) {
                    filteredResults.add(sd);
                }
            }
            
            // Sort only the filtered results - O(m log m) where m = filtered results (m ≤ k)
            // This is better than O(k log k) when m << k
            filteredResults.sort(Comparator.comparing(KDTree.StationDistance::getDistance));
            return filteredResults;
        }
        
        // No filters: direct radius search - O(n + k log k)
        return kdTree.radiusSearch(targetLat, targetLon, radiusKm);
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

