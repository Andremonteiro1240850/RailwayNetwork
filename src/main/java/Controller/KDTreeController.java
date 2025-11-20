package Controller;

import Domain.KDTree;
import Domain.Station;
import Repository.KDTreeRepository;
import Repository.StationRepository;

import java.util.*;

/**
 * Controller for KD-tree operations.
 * Handles building balanced KD-trees and retrieving metrics.
 * Uses KDTreeRepository for tree construction, caching, and persistence.
 * 
 * @author Development Team
 * @version 2.0
 */
public class KDTreeController {
    
    private KDTreeRepository kdTreeRepository;
    
    /**
     * Constructs a new KDTreeController.
     *
     * @param stationRepository the station repository to use
     */
    public KDTreeController(StationRepository stationRepository) {
        if (stationRepository == null) {
            throw new IllegalArgumentException("StationRepository cannot be null");
        }
        this.kdTreeRepository = new KDTreeRepository(stationRepository);
    }
    
    /**
     * Builds a balanced KD-tree from the station repository.
     * Uses AVL trees from USEI06 - does NOT read CSV.
     * The tree is cached in the repository.
     *
     * @return true if build was successful
     */
    public boolean buildTree() {
        try {
            kdTreeRepository.buildTree();
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
     * The tree is cached in the repository.
     *
     * @param stations list of stations
     * @return true if build was successful
     */
    public boolean buildTree(List<Station> stations) {
        if (stations == null || stations.isEmpty()) {
            return false;
        }
        
        try {
            kdTreeRepository.buildTree(stations);
            return true;
        } catch (Exception e) {
            System.err.println("Error building KD-tree: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets the KD-tree from the repository.
     * Returns the cached tree if available, otherwise null.
     *
     * @return the KD-tree, or null if not built
     */
    public KDTree getKDTree() {
        return kdTreeRepository.getCachedTree();
    }
    
    /**
     * Gets or builds the KD-tree.
     * If a tree is cached, returns it. Otherwise, builds a new one.
     *
     * @return the KD-tree
     * @throws IllegalStateException if building fails
     */
    public KDTree getOrBuildTree() {
        return kdTreeRepository.getOrBuildTree();
    }
    
    /**
     * Checks if the tree has been built and cached.
     *
     * @return true if tree exists
     */
    public boolean isTreeBuilt() {
        return kdTreeRepository.hasCachedTree();
    }
    
    /**
     * Gets tree size information.
     *
     * @return array with [numNodes, numStations], or null if tree not built
     */
    public int[] getTreeSize() {
        KDTree tree = getKDTree();
        if (tree == null || tree.isEmpty()) {
            return null;
        }
        return tree.getSize();
    }
    
    /**
     * Gets the tree height.
     *
     * @return tree height, or -1 if tree not built
     */
    public int getTreeHeight() {
        KDTree tree = getKDTree();
        if (tree == null || tree.isEmpty()) {
            return -1;
        }
        return tree.height();
    }
    
    /**
     * Gets bucket statistics.
     *
     * @return map of bucket size to count, or null if tree not built
     */
    public Map<Integer, Integer> getBucketStats() {
        KDTree tree = getKDTree();
        if (tree == null || tree.isEmpty()) {
            return null;
        }
        return tree.getBucketStats();
    }
    
    /**
     * Gets all tree metrics.
     * Returns: Size of the tree, its height, and all distinct bucket sizes.
     *
     * @return KDTreeMetrics object with all metrics, or null if tree not built
     */
    public KDTreeMetrics getAllMetrics() {
        KDTree tree = getKDTree();
        if (tree == null || tree.isEmpty()) {
            return null;
        }
        
        int[] sizeInfo = tree.getSize();
        int numNodes = sizeInfo[0];
        int numStations = sizeInfo[1];
        int height = tree.height();
        Map<Integer, Integer> bucketStats = tree.getBucketStats();
        
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
    
    /**
     * Gets the KDTreeRepository used by this controller.
     * This allows direct access to the repository for operations that need it.
     *
     * @return the KDTreeRepository instance
     */
    public KDTreeRepository getRepository() {
        return kdTreeRepository;
    }
    
    /**
     * Gets the KDTreeRepository used by this controller.
     * Alias for getRepository() for consistency.
     *
     * @return the KDTreeRepository instance
     */
    public KDTreeRepository getKDTreeRepository() {
        return kdTreeRepository;
    }
}

