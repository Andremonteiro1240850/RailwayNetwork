package Controller;

import Domain.KDTree;
import Domain.Station;
import Repository.StationRepository;

import java.util.*;

/**
 * Controller for KD-tree operations.
 * Handles building balanced KD-trees and retrieving metrics.
 * 
 * @author Development Team
 * @version 1.0
 */
public class KDTreeController {
    
    private KDTree kdTree;
    private StationRepository stationRepository;
    
    /**
     * Constructs a new KDTreeController.
     *
     * @param stationRepository the station repository to use
     */
    public KDTreeController(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
        this.kdTree = null;
    }
    
    /**
     * Builds a balanced KD-tree from the station repository.
     * Uses AVL trees from USEI06 - does NOT read CSV.
     *
     * @return true if build was successful
     */
    public boolean buildTree() {
        if (stationRepository == null) {
            System.err.println("[ERROR] StationRepository is null. Cannot build KD-tree.");
            return false;
        }
        
        try {
            this.kdTree = new KDTree();
            this.kdTree.buildKdTree(stationRepository);
            return true;
        } catch (IllegalStateException e) {
            System.err.println("[ERROR] " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("[ERROR] Error building KD-tree: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Builds a balanced KD-tree from a list of stations.
     *
     * @param stations list of stations
     * @return true if build was successful
     */
    public boolean buildTree(List<Station> stations) {
        if (stations == null || stations.isEmpty()) {
            return false;
        }
        
        try {
            this.kdTree = new KDTree(stations);
            return true;
        } catch (Exception e) {
            System.err.println("Error building KD-tree: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets the KD-tree.
     *
     * @return the KD-tree, or null if not built
     */
    public KDTree getKDTree() {
        return kdTree;
    }
    
    /**
     * Checks if the tree has been built.
     *
     * @return true if tree exists
     */
    public boolean isTreeBuilt() {
        return kdTree != null && !kdTree.isEmpty();
    }
    
    /**
     * Gets tree size information.
     *
     * @return array with [numNodes, numStations], or null if tree not built
     */
    public int[] getTreeSize() {
        if (!isTreeBuilt()) {
            return null;
        }
        return kdTree.getSize();
    }
    
    /**
     * Gets the tree height.
     *
     * @return tree height, or -1 if tree not built
     */
    public int getTreeHeight() {
        if (!isTreeBuilt()) {
            return -1;
        }
        return kdTree.height();
    }
    
    /**
     * Gets bucket statistics.
     *
     * @return map of bucket size to count, or null if tree not built
     */
    public Map<Integer, Integer> getBucketStats() {
        if (!isTreeBuilt()) {
            return null;
        }
        return kdTree.getBucketStats();
    }
    
    /**
     * Gets all tree metrics.
     * Returns: Size of the tree, its height, and all distinct bucket sizes.
     *
     * @return KDTreeMetrics object with all metrics, or null if tree not built
     */
    public KDTreeMetrics getAllMetrics() {
        if (!isTreeBuilt()) {
            return null;
        }
        
        int[] sizeInfo = kdTree.getSize();
        int numNodes = sizeInfo[0];
        int numStations = sizeInfo[1];
        int height = kdTree.height();
        Map<Integer, Integer> bucketStats = kdTree.getBucketStats();
        
        return new KDTreeMetrics(numNodes, numStations, height, bucketStats);
    }
    
    /**
     * Gets a formatted string with tree metrics.
     *
     * @return formatted metrics string
     */
    public String getMetricsString() {
        KDTreeMetrics metrics = getAllMetrics();
        if (metrics == null) {
            return "KD-tree not built yet.";
        }
        return metrics.toString();
    }
}

