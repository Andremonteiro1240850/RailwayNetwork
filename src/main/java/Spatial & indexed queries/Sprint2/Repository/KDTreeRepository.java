package Sprint2.Repository;

import Sprint2.Domain.KDTree;
import Sprint2.Domain.Station;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Repository for KD-tree operations.
 * Handles bulk-build, caching, and persistence of KD-trees.
 * 
 * @author Development Team
 * @version 1.0
 */
public class KDTreeRepository {
    
    private KDTree cachedTree;
    private StationRepository stationRepository;
    
    /**
     * Constructs a new KDTreeRepository.
     *
     * @param stationRepository the station repository to use for building trees
     */
    public KDTreeRepository(StationRepository stationRepository) {
        if (stationRepository == null) {
            throw new IllegalArgumentException("StationRepository cannot be null");
        }
        this.stationRepository = stationRepository;
        this.cachedTree = null;
    }
    
    /**
     * Builds a balanced KD-tree using bulk-build algorithm.
     * Uses AVL trees from StationRepository (USEI06) - does NOT read CSV.
     * The built tree is cached for future use.
     *
     * @return the built KD-tree
     * @throws IllegalStateException if the AVL trees are not populated
     */
    public KDTree buildTree() {
        if (stationRepository.totalStations() == 0) {
            throw new IllegalStateException(
                "Cannot build KD-tree: AVL trees are empty. " +
                "Please populate the StationRepository with stations first (complete option 1)."
            );
        }
        
        // Get stations from AVL trees - already sorted by latitude and longitude
        List<Station> sortedByLat = new ArrayList<>();
        List<Station> sortedByLon = new ArrayList<>();
        
        // Collect valid stations from latitude AVL tree (already sorted by latitude)
        for (Station station : stationRepository.getAllStationsOrderedByLatitude()) {
            if (isValidCoordinate(station) && station.getName() != null && !station.getName().trim().isEmpty()) {
                sortedByLat.add(station);
            }
        }
        
        // Collect valid stations from longitude AVL tree (already sorted by longitude)
        for (Station station : stationRepository.getAllStationsOrderedByLongitude()) {
            if (isValidCoordinate(station) && station.getName() != null && !station.getName().trim().isEmpty()) {
                sortedByLon.add(station);
            }
        }
        
        if (sortedByLat.isEmpty() || sortedByLon.isEmpty()) {
            throw new IllegalStateException(
                "Cannot build KD-tree: No valid stations found in AVL trees. " +
                "Please ensure option 1 has been completed and stations are properly validated."
            );
        }
        
        // Ensure both lists contain the same stations (intersection of valid stations)
        Set<Station> validStationsSet = new HashSet<>(sortedByLat);
        validStationsSet.retainAll(new HashSet<>(sortedByLon));
        
        // Rebuild both lists with only the common valid stations, maintaining order from AVL trees
        List<Station> finalSortedByLat = new ArrayList<>();
        List<Station> finalSortedByLon = new ArrayList<>();
        
        for (Station station : stationRepository.getAllStationsOrderedByLatitude()) {
            if (validStationsSet.contains(station)) {
                finalSortedByLat.add(station);
            }
        }
        
        for (Station station : stationRepository.getAllStationsOrderedByLongitude()) {
            if (validStationsSet.contains(station)) {
                finalSortedByLon.add(station);
            }
        }
        
        // Sort by secondary criteria to ensure deterministic ordering for stations with same primary coordinate
        finalSortedByLat.sort(Comparator.comparingDouble(Station::getLatitude)
                .thenComparingDouble(Station::getLongitude)
                .thenComparing(Station::getName));
        
        finalSortedByLon.sort(Comparator.comparingDouble(Station::getLongitude)
                .thenComparingDouble(Station::getLatitude)
                .thenComparing(Station::getName));
        
        // Build tree using bulk-build algorithm
        KDTree.KDNode root = bulkBuild(finalSortedByLat, finalSortedByLon, 0);
        
        // Create KDTree with the built root
        this.cachedTree = new KDTree(root);
        
        return this.cachedTree;
    }
    
    /**
     * Builds a balanced KD-tree from a list of stations.
     * Uses deterministic bulk-build algorithm with median selection.
     * The built tree is cached for future use.
     * 
     * @param stations list of stations to build from
     * @return the built KD-tree
     */
    public KDTree buildTree(List<Station> stations) {
        if (stations == null || stations.isEmpty()) {
            this.cachedTree = new KDTree();
            return this.cachedTree;
        }
        
        // Filter out invalid stations
        List<Station> validStations = new ArrayList<>();
        for (Station station : stations) {
            if (isValidCoordinate(station) && station.getName() != null && !station.getName().trim().isEmpty()) {
                validStations.add(station);
            }
        }
        
        if (validStations.isEmpty()) {
            this.cachedTree = new KDTree();
            return this.cachedTree;
        }
        
        // Create sorted arrays by latitude and longitude
        List<Station> sortedByLat = new ArrayList<>(validStations);
        List<Station> sortedByLon = new ArrayList<>(validStations);
        
        sortedByLat.sort(Comparator.comparingDouble(Station::getLatitude)
                .thenComparingDouble(Station::getLongitude)
                .thenComparing(Station::getName));
        
        sortedByLon.sort(Comparator.comparingDouble(Station::getLongitude)
                .thenComparingDouble(Station::getLatitude)
                .thenComparing(Station::getName));
        
        // Build tree using bulk-build algorithm
        KDTree.KDNode root = bulkBuild(sortedByLat, sortedByLon, 0);
        
        // Create KDTree with the built root
        this.cachedTree = new KDTree(root);
        
        return this.cachedTree;
    }
    
    /**
     * Recursive bulk-build algorithm that creates a balanced tree.
     * Uses median selection for deterministic balancing.
     * 
     * @param sortedByLat stations sorted by latitude
     * @param sortedByLon stations sorted by longitude
     * @param depth current depth (determines which axis to split on)
     * @return the root node of the subtree
     */
    private KDTree.KDNode bulkBuild(List<Station> sortedByLat, List<Station> sortedByLon, int depth) {
        if (sortedByLat.isEmpty() || sortedByLon.isEmpty()) {
            return null;
        }
        
        int axis = depth % 2; // 0 for latitude, 1 for longitude
        List<Station> sortedByAxis = (axis == 0) ? sortedByLat : sortedByLon;
        
        // Select median
        int medianIndex = sortedByAxis.size() / 2;
        Station medianStation = sortedByAxis.get(medianIndex);
        double medianValue = (axis == 0) ? medianStation.getLatitude() : medianStation.getLongitude();
        
        // Collect all stations with the exact same (lat, lon) as median
        List<Station> bucket = new ArrayList<>();
        List<Station> leftByAxis = new ArrayList<>();
        List<Station> rightByAxis = new ArrayList<>();
        
        double medianLat = medianStation.getLatitude();
        double medianLon = medianStation.getLongitude();
        
        for (Station station : sortedByAxis) {
            double coord = (axis == 0) ? station.getLatitude() : station.getLongitude();
            int cmp = Double.compare(coord, medianValue);
            
            // Check if exact same (lat, lon) pair
            boolean exactMatch = Double.compare(station.getLatitude(), medianLat) == 0 &&
                                 Double.compare(station.getLongitude(), medianLon) == 0;
            
            if (exactMatch) {
                bucket.add(station);
            } else if (cmp < 0) {
                leftByAxis.add(station);
            } else {
                // cmp >= 0 but not exact match
                rightByAxis.add(station);
            }
        }
        
        // Sort bucket by name (ascending)
        bucket.sort(Comparator.comparing(Station::getName));
        
        // Partition the other array (excluding stations in bucket)
        List<Station> leftByOther = new ArrayList<>();
        List<Station> rightByOther = new ArrayList<>();
        List<Station> sortedByOther = (axis == 0) ? sortedByLon : sortedByLat;
        
        // Create a set of (lat, lon) pairs for bucket stations (not using equals which includes name)
        Set<String> bucketCoords = new HashSet<>();
        for (Station s : bucket) {
            bucketCoords.add(s.getLatitude() + "," + s.getLongitude());
        }
        
        for (Station station : sortedByOther) {
            // Check if this station has the same coordinates as bucket stations
            String stationCoord = station.getLatitude() + "," + station.getLongitude();
            if (bucketCoords.contains(stationCoord)) {
                continue; // Skip stations in bucket (same coordinates)
            }
            
            // Partition based on the split axis coordinate
            double coord = (axis == 0) ? station.getLatitude() : station.getLongitude();
            int cmp = Double.compare(coord, medianValue);
            
            if (cmp < 0) {
                leftByOther.add(station);
            } else {
                rightByOther.add(station);
            }
        }
        
        // Create node
        KDTree.KDNode node = new KDTree.KDNode(axis, medianValue, bucket);
        
        // Recursively build left and right subtrees
        if (axis == 0) {
            node.setLeft(bulkBuild(leftByAxis, leftByOther, depth + 1));
            node.setRight(bulkBuild(rightByAxis, rightByOther, depth + 1));
        } else {
            node.setLeft(bulkBuild(leftByOther, leftByAxis, depth + 1));
            node.setRight(bulkBuild(rightByOther, rightByAxis, depth + 1));
        }
        
        return node;
    }
    
    /**
     * Validates that a station has valid coordinates.
     * 
     * @param station the station to validate
     * @return true if coordinates are valid
     */
    private boolean isValidCoordinate(Station station) {
        if (station == null) {
            return false;
        }
        double lat = station.getLatitude();
        double lon = station.getLongitude();
        return lat >= -90.0 && lat <= 90.0 && lon >= -180.0 && lon <= 180.0;
    }
    
    /**
     * Gets the cached KD-tree.
     * Returns null if no tree has been built yet.
     *
     * @return the cached KD-tree, or null if not built
     */
    public KDTree getCachedTree() {
        return cachedTree;
    }
    
    /**
     * Checks if a tree is cached.
     *
     * @return true if a tree is cached
     */
    public boolean hasCachedTree() {
        return cachedTree != null && !cachedTree.isEmpty();
    }
    
    /**
     * Clears the cached tree.
     */
    public void clearCache() {
        this.cachedTree = null;
    }
    
    /**
     * Gets or builds the KD-tree.
     * If a tree is cached, returns it. Otherwise, tries to load from file.
     * If no file exists, builds a new one.
     *
     * @return the KD-tree
     * @throws IllegalStateException if building fails
     */
    public KDTree getOrBuildTree() {
        if (hasCachedTree()) {
            return cachedTree;
        }
        
        // Try to load from file first
        try {
            KDTree loadedTree = loadTree();
            if (loadedTree != null && !loadedTree.isEmpty()) {
                this.cachedTree = loadedTree;
                return cachedTree;
            }
        } catch (Exception e) {
            // If loading fails, continue to build
        }
        
        // If no cached tree and loading failed, build new one
        return buildTree();
    }
    
    /**
     * Saves the KD-tree to a file using Java serialization.
     * The file is saved as "kdtree.ser" in the project root directory.
     *
     * @return true if save was successful, false otherwise
     */
    public boolean saveTree() {
        return saveTree("kdtree.ser");
    }
    
    /**
     * Saves the KD-tree to a file using Java serialization.
     *
     * @param filePath the path to the file where the tree should be saved
     * @return true if save was successful, false otherwise
     */
    public boolean saveTree(String filePath) {
        if (cachedTree == null || cachedTree.isEmpty()) {
            return false;
        }
        
        try {
            Path path = Paths.get(filePath);
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(filePath))) {
                oos.writeObject(cachedTree);
            }
            return true;
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to save KD-tree to file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Loads a KD-tree from a file using Java deserialization.
     * The default file is "kdtree.ser" in the project root directory.
     *
     * @return the loaded KD-tree, or null if loading failed
     */
    public KDTree loadTree() {
        return loadTree("kdtree.ser");
    }
    
    /**
     * Loads a KD-tree from a file using Java deserialization.
     *
     * @param filePath the path to the file containing the serialized tree
     * @return the loaded KD-tree, or null if loading failed
     */
    public KDTree loadTree(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                return null;
            }
            
            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(filePath))) {
                KDTree loadedTree = (KDTree) ois.readObject();
                this.cachedTree = loadedTree;
                return loadedTree;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[ERROR] Failed to load KD-tree from file: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Checks if a persisted KD-tree file exists.
     *
     * @return true if the default persistence file exists
     */
    public boolean hasPersistedTree() {
        return hasPersistedTree("kdtree.ser");
    }
    
    /**
     * Checks if a persisted KD-tree file exists.
     *
     * @param filePath the path to check
     * @return true if the file exists
     */
    public boolean hasPersistedTree(String filePath) {
        return Files.exists(Paths.get(filePath));
    }
    
    /**
     * Deletes the persisted KD-tree file.
     *
     * @return true if deletion was successful, false otherwise
     */
    public boolean deletePersistedTree() {
        return deletePersistedTree("kdtree.ser");
    }
    
    /**
     * Deletes the persisted KD-tree file.
     *
     * @param filePath the path to the file to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deletePersistedTree(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                return true;
            }
            return false;
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to delete KD-tree file: " + e.getMessage());
            return false;
        }
    }
}

