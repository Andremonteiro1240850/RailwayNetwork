package Domain;

import Repository.StationRepository;

import java.util.*;

/**
 * Balanced 2D-tree (KD-tree with k=2) for efficient geographical range queries.
 * 
 * This implementation provides a deterministic bulk-build algorithm that produces a balanced tree.
 * Stations with identical coordinates are stored in buckets sorted by station name (ascending).
 * 
 * @author Development Team
 * @version 2.0
 */
public class KDTree {
    
    private KDNode root;
    private int numNodes;
    private int numStations;
    
    /**
     * Internal node class for the KD-tree.
     * Each node stores a bucket of stations (for duplicate coordinates) and split information.
     */
    public static class KDNode {
        private int axis; // 0 for latitude, 1 for longitude
        private double splitValue; // The coordinate value used for splitting
        private List<Station> bucket; // Stations at this coordinate (sorted by name)
        private KDNode left;
        private KDNode right;
        
        public KDNode(int axis, double splitValue, List<Station> bucket) {
            this.axis = axis;
            this.splitValue = splitValue;
            this.bucket = new ArrayList<>(bucket);
            this.left = null;
            this.right = null;
        }
        
        public int getAxis() { return axis; }
        public double getSplitValue() { return splitValue; }
        public List<Station> getBucket() { return new ArrayList<>(bucket); }
        public KDNode getLeft() { return left; }
        public KDNode getRight() { return right; }
        void setLeft(KDNode left) { this.left = left; }
        void setRight(KDNode right) { this.right = right; }
    }
    
    /**
     * Constructs an empty KD-tree.
     */
    public KDTree() {
        this.root = null;
        this.numNodes = 0;
        this.numStations = 0;
    }
    
    /**
     * Constructs a KD-tree from a list of stations using balanced bulk-build.
     *
     * @param stations list of stations to build from
     */
    public KDTree(List<Station> stations) {
        this();
        buildKdTree(stations);
    }
    
    /**
     * Builds a balanced KD-tree from a list of stations using StationRepository.
     * This method uses the StationRepository to obtain sorted sequences by latitude and longitude.
     * 
     * @param stationRepository the StationRepository instance that provides sorted data
     * @return the root node of the built tree
     */
    public KDNode buildKdTree(StationRepository stationRepository) {
        if (stationRepository == null) {
            return null;
        }
        
        // Get all stations (already validated)
        List<Station> allStations = new ArrayList<>();
        for (Station station : stationRepository.getAllStationsOrderedByLatitude()) {
            // Re-validate defensively
            if (isValidCoordinate(station)) {
                allStations.add(station);
            }
        }
        
        return buildKdTree(allStations);
    }
    
    /**
     * Builds a balanced KD-tree from a list of stations.
     * Uses deterministic bulk-build algorithm with median selection.
     * 
     * @param stations list of stations to build from
     * @return the root node of the built tree
     */
    public KDNode buildKdTree(List<Station> stations) {
        if (stations == null || stations.isEmpty()) {
            this.root = null;
            this.numNodes = 0;
            this.numStations = 0;
            return null;
        }
        
        // Filter out invalid stations
        List<Station> validStations = new ArrayList<>();
        for (Station station : stations) {
            if (isValidCoordinate(station) && station.getName() != null && !station.getName().trim().isEmpty()) {
                validStations.add(station);
            }
        }
        
        if (validStations.isEmpty()) {
            this.root = null;
            this.numNodes = 0;
            this.numStations = 0;
            return null;
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
        this.root = bulkBuild(sortedByLat, sortedByLon, 0);
        
        // Calculate metrics
        this.numNodes = countNodes(this.root);
        this.numStations = countStations(this.root);
        
        return this.root;
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
    private KDNode bulkBuild(List<Station> sortedByLat, List<Station> sortedByLon, int depth) {
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
        KDNode node = new KDNode(axis, medianValue, bucket);
        
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
     * Returns the tree size (number of nodes) and total number of stations.
     * 
     * @return a pair (numNodes, numStations)
     */
    public int[] getSize() {
        return new int[]{this.numNodes, this.numStations};
    }
    
    /**
     * Returns the number of nodes in the tree.
     * 
     * @return number of nodes
     */
    public int size() {
        return this.numNodes;
    }
    
    /**
     * Returns the height of the tree.
     * Root has height 1.
     * 
     * @return the tree height
     */
    public int height() {
        return height(this.root);
    }
    
    /**
     * Recursive helper to calculate tree height.
     * 
     * @param node the current node
     * @return the height of the subtree rooted at node
     */
    private int height(KDNode node) {
        if (node == null) {
            return 0;
        }
        return 1 + Math.max(height(node.getLeft()), height(node.getRight()));
    }
    
    /**
     * Returns the distinct bucket sizes and their frequencies.
     * 
     * @return a map from bucket size to count of nodes with that bucket size
     */
    public Map<Integer, Integer> getBucketStats() {
        Map<Integer, Integer> stats = new HashMap<>();
        collectBucketStats(this.root, stats);
        return stats;
    }
    
    /**
     * Recursive helper to collect bucket statistics.
     * 
     * @param node the current node
     * @param stats the map to update
     */
    private void collectBucketStats(KDNode node, Map<Integer, Integer> stats) {
        if (node == null) {
            return;
        }
        
        int bucketSize = node.getBucket().size();
        stats.put(bucketSize, stats.getOrDefault(bucketSize, 0) + 1);
        
        collectBucketStats(node.getLeft(), stats);
        collectBucketStats(node.getRight(), stats);
    }
    
    /**
     * Counts the total number of nodes in the tree.
     * 
     * @param node the root node
     * @return the number of nodes
     */
    private int countNodes(KDNode node) {
        if (node == null) {
            return 0;
        }
        return 1 + countNodes(node.getLeft()) + countNodes(node.getRight());
    }
    
    /**
     * Counts the total number of stations in the tree.
     * 
     * @param node the root node
     * @return the number of stations
     */
    private int countStations(KDNode node) {
        if (node == null) {
            return 0;
        }
        return node.getBucket().size() + countStations(node.getLeft()) + countStations(node.getRight());
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
        rangeQueryRecursive(this.root, latMin, latMax, lonMin, lonMax, 0, results);
        
        // Sort all results by name
        results.sort(Comparator.comparing(Station::getName));
        return results;
    }
    
    /**
     * Recursive range query helper.
     * 
     * @param node current node
     * @param latMin minimum latitude
     * @param latMax maximum latitude
     * @param lonMin minimum longitude
     * @param lonMax maximum longitude
     * @param depth current depth
     * @param results list to collect results
     */
    private void rangeQueryRecursive(KDNode node, double latMin, double latMax, 
                                    double lonMin, double lonMax, int depth, List<Station> results) {
        if (node == null) {
            return;
        }
        
        // Check if any station in the bucket is in range
        for (Station station : node.getBucket()) {
            double lat = station.getLatitude();
            double lon = station.getLongitude();
            if (lat >= latMin && lat <= latMax && lon >= lonMin && lon <= lonMax) {
                results.add(station);
            }
        }
        
        int axis = node.getAxis();
        double splitValue = node.getSplitValue();
        
        if (axis == 0) {
            // Split on latitude
            if (latMin <= splitValue) {
                rangeQueryRecursive(node.getLeft(), latMin, latMax, lonMin, lonMax, depth + 1, results);
            }
            if (latMax >= splitValue) {
                rangeQueryRecursive(node.getRight(), latMin, latMax, lonMin, lonMax, depth + 1, results);
            }
        } else {
            // Split on longitude
            if (lonMin <= splitValue) {
                rangeQueryRecursive(node.getLeft(), latMin, latMax, lonMin, lonMax, depth + 1, results);
            }
            if (lonMax >= splitValue) {
                rangeQueryRecursive(node.getRight(), latMin, latMax, lonMin, lonMax, depth + 1, results);
            }
        }
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
     * Gets the root node of the tree.
     * 
     * @return the root node
     */
    public KDNode getRoot() {
        return this.root;
    }
    
    /**
     * Calculates the distance between two points using Haversine formula.
     * 
     * @param lat1 latitude of first point
     * @param lon1 longitude of first point
     * @param lat2 latitude of second point
     * @param lon2 longitude of second point
     * @return distance in kilometers
     */
    public static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in kilometers
        
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
}
