package Domain;

import java.util.*;

/**
 * 2D-tree (KD-tree with k=2) for efficient geographical range queries.
 * This is a basic implementation that can be replaced with a balanced version from USEI07.
 * 
 * Supports range queries (rectangular region) and handles multiple stations
 * with the same coordinates (sorted by name).
 * 
 * @author Development Team
 * @version 1.0
 */
public class KDTree {
    
    private KDNode root;
    private int size;
    private int height;
    
    /**
     * Constructs an empty KD-tree.
     */
    public KDTree() {
        this.root = null;
        this.size = 0;
        this.height = 0;
    }
    
    /**
     * Constructs a KD-tree from a list of stations.
     * This is a simple insertion-based build. For balanced tree, use bulkBuild().
     *
     * @param stations list of stations to insert
     */
    public KDTree(List<Station> stations) {
        this();
        for (Station station : stations) {
            insert(station);
        }
    }
    
    /**
     * Inserts a station into the tree.
     * If multiple stations share the same coordinates, they are stored
     * in a list sorted by name.
     *
     * @param station the station to insert
     */
    public void insert(Station station) {
        if (station == null) {
            return;
        }
        
        root = insert(root, station, 0);
        size++;
    }
    
    /**
     * Recursive insertion helper.
     *
     * @param node    current node
     * @param station station to insert
     * @param depth   current depth (determines which dimension to compare)
     * @return the updated node
     */
    private KDNode insert(KDNode node, Station station, int depth) {
        if (node == null) {
            return new KDNode(station, depth % 2 == 0);
        }
        
        double nodeCoord = node.isSplitOnX ? node.station.getLatitude() : node.station.getLongitude();
        double stationCoord = node.isSplitOnX ? station.getLatitude() : station.getLongitude();
        
        // If coordinates match exactly, add to the list (sorted by name)
        if (Double.compare(node.station.getLatitude(), station.getLatitude()) == 0 &&
            Double.compare(node.station.getLongitude(), station.getLongitude()) == 0) {
            node.addStation(station);
            return node;
        }
        
        // Compare based on current dimension
        if (stationCoord < nodeCoord) {
            node.left = insert(node.left, station, depth + 1);
        } else {
            node.right = insert(node.right, station, depth + 1);
        }
        
        return node;
    }
    
    /**
     * Performs a range query (rectangular region search).
     * Returns all stations within the specified latitude/longitude bounds.
     *
     * @param latMin minimum latitude (inclusive)
     * @param latMax maximum latitude (inclusive)
     * @param lonMin minimum longitude (inclusive)
     * @param lonMax maximum longitude (inclusive)
     * @return list of stations in the range, sorted by name
     */
    public List<Station> rangeQuery(double latMin, double latMax, double lonMin, double lonMax) {
        List<Station> results = new ArrayList<>();
        rangeQuery(root, latMin, latMax, lonMin, lonMax, 0, results);
        
        // Sort all results by name
        results.sort(Comparator.comparing(Station::getName));
        return results;
    }
    
    /**
     * Recursive range query helper with pruning.
     *
     * @param node   current node
     * @param latMin minimum latitude
     * @param latMax maximum latitude
     * @param lonMin minimum longitude
     * @param lonMax maximum longitude
     * @param depth  current depth
     * @param results list to collect results
     */
    private void rangeQuery(KDNode node, double latMin, double latMax, 
                           double lonMin, double lonMax, int depth, List<Station> results) {
        if (node == null) {
            return;
        }
        
        double lat = node.station.getLatitude();
        double lon = node.station.getLongitude();
        
        // Check if current node's station is in range
        if (lat >= latMin && lat <= latMax && lon >= lonMin && lon <= lonMax) {
            // Add all stations at this node (including duplicates with same coordinates)
            results.addAll(node.getAllStations());
        }
        
        boolean isSplitOnX = (depth % 2 == 0);
        
        if (isSplitOnX) {
            // Split on latitude (X dimension)
            // Left subtree: all points with latitude < current node's latitude
            // Right subtree: all points with latitude >= current node's latitude
            if (latMin <= lat) {
                // Search region overlaps with left subtree
                rangeQuery(node.left, latMin, latMax, lonMin, lonMax, depth + 1, results);
            }
            if (latMax >= lat) {
                // Search region overlaps with right subtree
                rangeQuery(node.right, latMin, latMax, lonMin, lonMax, depth + 1, results);
            }
        } else {
            // Split on longitude (Y dimension)
            // Left subtree: all points with longitude < current node's longitude
            // Right subtree: all points with longitude >= current node's longitude
            if (lonMin <= lon) {
                // Search region overlaps with left subtree
                rangeQuery(node.left, latMin, latMax, lonMin, lonMax, depth + 1, results);
            }
            if (lonMax >= lon) {
                // Search region overlaps with right subtree
                rangeQuery(node.right, latMin, latMax, lonMin, lonMax, depth + 1, results);
            }
        }
    }
    
    /**
     * Gets the number of facilities in the tree.
     *
     * @return the size
     */
    public int size() {
        return size;
    }
    
    /**
     * Gets the height of the tree.
     *
     * @return the height
     */
    public int height() {
        return calculateHeight(root);
    }
    
    /**
     * Calculates the height of the tree.
     *
     * @param node current node
     * @return the height
     */
    private int calculateHeight(KDNode node) {
        if (node == null) {
            return 0;
        }
        return 1 + Math.max(calculateHeight(node.left), calculateHeight(node.right));
    }
    
    /**
     * Checks if the tree is empty.
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return root == null;
    }
    
    /**
     * Internal node class for the KD-tree.
     */
    private static class KDNode {
        Station station;
        List<Station> duplicateStations; // For stations with same coordinates
        boolean isSplitOnX; // true if split on latitude (X), false if on longitude (Y)
        KDNode left;
        KDNode right;
        
        KDNode(Station station, boolean isSplitOnX) {
            this.station = station;
            this.isSplitOnX = isSplitOnX;
            this.duplicateStations = new ArrayList<>();
            this.left = null;
            this.right = null;
        }
        
        void addStation(Station station) {
            duplicateStations.add(station);
            // Keep sorted by name
            duplicateStations.sort(Comparator.comparing(Station::getName));
        }
        
        List<Station> getAllStations() {
            List<Station> all = new ArrayList<>();
            all.add(station);
            all.addAll(duplicateStations);
            // Ensure sorted by name
            all.sort(Comparator.comparing(Station::getName));
            return all;
        }
    }
    
    /**
     * Bulk build method for balanced tree construction.
     * This is a placeholder that can be replaced with the balanced build from USEI07.
     * For now, it just inserts all stations (which may not be balanced).
     *
     * @param stations list of stations to build from
     */
    public void bulkBuild(List<Station> stations) {
        // Clear existing tree
        this.root = null;
        this.size = 0;
        
        // Simple insertion-based build (not balanced)
        // TODO: Replace with balanced build from USEI07
        for (Station station : stations) {
            insert(station);
        }
    }
}

