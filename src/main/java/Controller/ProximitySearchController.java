package Controller;

import Domain.Station;
import Domain.KDTree;
import Domain.SearchFilters;
import Repository.KDTreeRepository;
import Repository.StationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for proximity search operations (Nearest-N search).
 * Handles finding the N nearest stations to a target coordinate with optional filters.
 * 
 * @author Development Team
 * @version 1.0
 */
public class ProximitySearchController {
    
    private final KDTree kdTree;
    private final List<Station> allStations;
    
    /**
     * Constructs a new ProximitySearchController with a KD-tree.
     * Builds the tree using KDTreeRepository.
     *
     * @param stations list of stations to build the tree from
     */
    public ProximitySearchController(List<Station> stations) {
        this.allStations = new ArrayList<>(stations);
        
        // Build KD-tree using KDTreeRepository
        StationRepository tempRepo = new StationRepository(stations);
        KDTreeRepository kdTreeRepository = new KDTreeRepository(tempRepo);
        this.kdTree = kdTreeRepository.buildTree(stations);
    }
    
    /**
     * Constructs a new ProximitySearchController with an existing KD-tree.
     * This allows using a balanced tree.
     *
     * @param kdTree the KD-tree to use (can be balanced)
     * @param stations list of all stations (for fallback or additional operations)
     */
    public ProximitySearchController(KDTree kdTree, List<Station> stations) {
        this.kdTree = kdTree;
        this.allStations = new ArrayList<>(stations);
    }
    
    /**
     * Constructs a new ProximitySearchController from an existing KD-tree.
     * Uses the 2D-tree built in USEI07 - does NOT build a new tree.
     *
     * @param kdTree the KD-tree built in USEI07
     * @param stationRepository the StationRepository to get station list
     */
    public ProximitySearchController(KDTree kdTree, StationRepository stationRepository) {
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
     * Constructs a new ProximitySearchController from a KDTreeController.
     * Uses the 2D-tree built in USEI07 - does NOT build a new tree.
     * This is the preferred constructor for USEI09.
     *
     * @param kdTreeController the KDTreeController from USEI07 (must have tree built)
     * @param stationRepository the StationRepository to get station list
     */
    public ProximitySearchController(KDTreeController kdTreeController, StationRepository stationRepository) {
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

