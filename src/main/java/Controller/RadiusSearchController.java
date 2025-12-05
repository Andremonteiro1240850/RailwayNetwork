package Controller;

import Domain.KDTree;
import Domain.Station;
import Domain.StationDistanceComparator;
import Domain.RadiusSearchSummary;
import Domain.AVL;
import Repository.StationRepository;
import Repository.KDTreeRepository;

import java.util.*;

/**
 * Controller for radius search operations with density summary (USEI10).
 * Handles radius search using the 2D-tree and provides summary statistics
 * by country and isCity.
 * 
 * @author Development Team
 * @version 1.0
 */
public class RadiusSearchController {
    
    private final KDTree kdTree;
    private final List<Station> allStations;
    
    /**
     * Constructs a new RadiusSearchController from a KDTreeRepository.
     * Uses the 2D-tree built in USEI07 and cached in the repository - does NOT build a new tree.
     * This is the preferred constructor for USEI10.
     *
     * @param kdTreeRepository the KDTreeRepository from USEI07 (must have tree built and cached)
     * @param stationRepository the StationRepository to get station list
     */
    public RadiusSearchController(KDTreeRepository kdTreeRepository, StationRepository stationRepository) {
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
        
        // Use the existing KD-tree from KDTreeRepository (built in USEI07)
        this.kdTree = kdTreeRepository.getCachedTree();
        
        // Get all stations from repository
        this.allStations = stationRepository.getAllStations();
    }
    
    /**
     * Performs a radius search and returns results in a BST/AVL tree
     * sorted by distance (ASC) and station name (DESC), along with a summary.
     * 
     * Uses Haversine distance (km) with Earth radius between two (lat,lon) points.
     *
     * @param targetLat target latitude (must be between -90 and 90)
     * @param targetLon target longitude (must be between -180 and 180)
     * @param radiusKm search radius in kilometers (must be non-negative)
     * @return a RadiusSearchResult containing the AVL tree and summary
     * @throws IllegalArgumentException if parameters are invalid
     */
    public RadiusSearchResult searchRadiusWithSummary(double targetLat, double targetLon, double radiusKm) {
        if (targetLat < -90 || targetLat > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (targetLon < -180 || targetLon > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
        if (radiusKm < 0) {
            throw new IllegalArgumentException("Radius must be non-negative");
        }
        
        // Get stations in radius from KD-tree (already uses Haversine distance)
        List<KDTree.StationDistance> stationsInRadius = kdTree.radiusSearch(targetLat, targetLon, radiusKm);
        
        // Create AVL tree sorted by distance (ASC) and station name (DESC)
        StationDistanceComparator comparator = new StationDistanceComparator();
        AVL<KDTree.StationDistance> resultTree = new AVL<>(comparator);
        
        for (KDTree.StationDistance sd : stationsInRadius) {
            resultTree.insert(sd);
        }
        
        // Calculate summary by country and isCity
        RadiusSearchSummary summary = calculateSummary(stationsInRadius);
        
        return new RadiusSearchResult(resultTree, summary);
    }
    
    /**
     * Calculates summary statistics from the radius search results.
     * Aggregates stations by country and by isCity status.
     *
     * @param stationsInRadius list of station-distance pairs found in radius
     * @return RadiusSearchSummary with aggregated statistics
     */
    private RadiusSearchSummary calculateSummary(List<KDTree.StationDistance> stationsInRadius) {
        Map<String, Integer> stationsByCountry = new HashMap<>();
        int stationsByCity = 0;
        int stationsByNonCity = 0;
        
        for (KDTree.StationDistance sd : stationsInRadius) {
            Station station = sd.getStation();
            
            // Count by country
            String country = station.getCountry();
            stationsByCountry.put(country, stationsByCountry.getOrDefault(country, 0) + 1);
            
            // Count by isCity
            if (station.isCity()) {
                stationsByCity++;
            } else {
                stationsByNonCity++;
            }
        }
        
        return new RadiusSearchSummary(
            stationsByCountry,
            stationsByCity,
            stationsByNonCity,
            stationsInRadius.size()
        );
    }
    
    /**
     * Gets the KD-tree used for spatial queries.
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
     * Result container for radius search with summary.
     * Contains both the AVL tree and the summary statistics.
     */
    public static class RadiusSearchResult {
        private final AVL<KDTree.StationDistance> resultTree;
        private final RadiusSearchSummary summary;
        
        public RadiusSearchResult(AVL<KDTree.StationDistance> resultTree, RadiusSearchSummary summary) {
            this.resultTree = resultTree;
            this.summary = summary;
        }
        
        public AVL<KDTree.StationDistance> getResultTree() {
            return resultTree;
        }
        
        public RadiusSearchSummary getSummary() {
            return summary;
        }
    }
}

