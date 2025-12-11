package Sprint2.Domain;

import Sprint1.Domain.Station;

import java.io.Serializable;
import java.util.*;

/**
 * Balanced 2D-tree (KD-tree with k=2) for efficient geographical range queries.
 * 
 * This class contains only query logic and statistics.
 * Tree construction is handled by KDTreeRepository.
 * Stations with identical coordinates are stored in buckets sorted by station name (ascending).
 * 
 * @author Development Team
 * @version 3.0
 */
public class KDTree implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private KDNode root;
    private int numNodes;
    private int numStations;
    
    /**
     * Internal node class for the KD-tree.
     * Each node stores a bucket of stations (for duplicate coordinates) and split information.
     */
    public static class KDNode implements Serializable {
        private static final long serialVersionUID = 1L;
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
        public void setLeft(KDNode left) { this.left = left; }
        public void setRight(KDNode right) { this.right = right; }
    }
    
    /**
     * Constructs an empty KD-tree.
     * Tree construction should be done via KDTreeRepository.
     */
    public KDTree() {
        this.root = null;
        this.numNodes = 0;
        this.numStations = 0;
    }
    
    /**
     * Constructs a KD-tree with a pre-built root node.
     * This constructor should only be used by KDTreeRepository.
     * 
     * @param root the root node of the tree
     */
    public KDTree(KDNode root) {
        if (root == null) {
            this.root = null;
            this.numNodes = 0;
            this.numStations = 0;
        } else {
            this.root = root;
            // Calculate metrics
            this.numNodes = countNodes(this.root);
            this.numStations = countStations(this.root);
        }
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
     * Performs a range query without sorting the results.
     * Useful when results will be filtered before sorting to reduce complexity.
     *
     * @param latMin minimum latitude (inclusive)
     * @param latMax maximum latitude (inclusive)
     * @param lonMin minimum longitude (inclusive)
     * @param lonMax maximum longitude (inclusive)
     * @return list of stations in the range, NOT sorted
     */
    public List<Station> rangeQueryUnsorted(double latMin, double latMax, double lonMin, double lonMax) {
        List<Station> results = new ArrayList<>();
        rangeQueryRecursive(this.root, latMin, latMax, lonMin, lonMax, 0, results);
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
     * Finds the N nearest stations to a target point.
     * Uses a priority queue to maintain the N closest stations during tree traversal.
     *
     * @param targetLat target latitude
     * @param targetLon target longitude
     * @param n number of nearest stations to find
     * @return list of pairs (station, distance in km), sorted by distance (ascending)
     */
    public List<StationDistance> nearestN(double targetLat, double targetLon, int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("n must be positive");
        }
        if (root == null) {
            return new ArrayList<>();
        }
        
        // Use a priority queue to keep track of the N nearest stations
        // Max heap: we want to remove the farthest station when we have more than N
        PriorityQueue<StationDistance> nearestQueue = new PriorityQueue<>(
            (a, b) -> Double.compare(b.getDistance(), a.getDistance()) // Reverse order for max heap
        );
        
        nearestNRecursive(root, targetLat, targetLon, n, 0, nearestQueue);
        
        // Convert to list and sort by distance (ascending)
        List<StationDistance> results = new ArrayList<>(nearestQueue);
        results.sort(Comparator.comparing(StationDistance::getDistance));
        
        return results;
    }
    
    /**
     * Recursive helper for nearest neighbor search.
     * Uses branch-and-bound pruning to avoid exploring unnecessary subtrees.
     *
     * @param node current node
     * @param targetLat target latitude
     * @param targetLon target longitude
     * @param n number of nearest stations to find
     * @param depth current depth
     * @param nearestQueue priority queue maintaining N nearest stations
     */
    private void nearestNRecursive(KDNode node, double targetLat, double targetLon, int n, 
                                   int depth, PriorityQueue<StationDistance> nearestQueue) {
        if (node == null) {
            return;
        }
        
        // Check all stations in the bucket
        for (Station station : node.getBucket()) {
            double distance = haversineDistance(targetLat, targetLon, 
                                               station.getLatitude(), station.getLongitude());
            
            if (nearestQueue.size() < n) {
                nearestQueue.offer(new StationDistance(station, distance));
            } else if (distance < nearestQueue.peek().getDistance()) {
                // Replace the farthest station with this one
                nearestQueue.poll();
                nearestQueue.offer(new StationDistance(station, distance));
            }
        }
        
        int axis = node.getAxis();
        double splitValue = node.getSplitValue();
        double targetCoord = (axis == 0) ? targetLat : targetLon;
        
        // Decide which subtree to explore first (closer one)
        KDNode nearChild, farChild;
        if (targetCoord <= splitValue) {
            nearChild = node.getLeft();
            farChild = node.getRight();
        } else {
            nearChild = node.getRight();
            farChild = node.getLeft();
        }
        
        // Always explore the near child first
        nearestNRecursive(nearChild, targetLat, targetLon, n, depth + 1, nearestQueue);
        
        // Check if we need to explore the far child
        // We only explore if:
        // 1. We don't have N stations yet, OR
        // 2. The distance to the split plane is less than the farthest distance in our queue
        double distanceToSplit = Math.abs(targetCoord - splitValue);
        double maxDistance = nearestQueue.isEmpty() ? Double.MAX_VALUE : nearestQueue.peek().getDistance();
        
        // Convert distance along axis to approximate distance in km
        // This is a heuristic: 1 degree latitude ≈ 111 km, 1 degree longitude ≈ 111*cos(lat) km
        double axisDistanceKm;
        if (axis == 0) {
            axisDistanceKm = distanceToSplit * 111.0; // Latitude
        } else {
            axisDistanceKm = distanceToSplit * 111.0 * Math.cos(Math.toRadians(targetLat)); // Longitude
        }
        
        if (nearestQueue.size() < n || axisDistanceKm < maxDistance) {
            nearestNRecursive(farChild, targetLat, targetLon, n, depth + 1, nearestQueue);
        }
    }
    
    /**
     * Finds all stations within a specified radius of a target point.
     * Uses spatial pruning to efficiently search the KD-tree.
     *
     * @param targetLat target latitude
     * @param targetLon target longitude
     * @param radiusKm search radius in kilometers
     * @return list of pairs (station, distance in km), sorted by distance (ascending)
     */
    public List<StationDistance> radiusSearch(double targetLat, double targetLon, double radiusKm) {
        if (radiusKm < 0) {
            throw new IllegalArgumentException("Radius must be non-negative");
        }
        if (root == null) {
            return new ArrayList<>();
        }
        
        List<StationDistance> results = new ArrayList<>();
        radiusSearchRecursive(root, targetLat, targetLon, radiusKm, 0, results);
        
        // Sort by distance (ascending)
        results.sort(Comparator.comparing(StationDistance::getDistance));
        
        return results;
    }
    
    /**
     * Finds all stations within a specified radius without sorting the results.
     * Useful when results will be filtered before sorting to reduce complexity.
     *
     * @param targetLat target latitude
     * @param targetLon target longitude
     * @param radiusKm search radius in kilometers
     * @return list of pairs (station, distance in km), NOT sorted
     */
    public List<StationDistance> radiusSearchUnsorted(double targetLat, double targetLon, double radiusKm) {
        if (radiusKm < 0) {
            throw new IllegalArgumentException("Radius must be non-negative");
        }
        if (root == null) {
            return new ArrayList<>();
        }
        
        List<StationDistance> results = new ArrayList<>();
        radiusSearchRecursive(root, targetLat, targetLon, radiusKm, 0, results);
        return results;
    }
    
    /**
     * Recursive helper for radius search.
     * Uses spatial pruning to avoid exploring subtrees that cannot contain stations within the radius.
     *
     * @param node current node
     * @param targetLat target latitude
     * @param targetLon target longitude
     * @param radiusKm search radius in kilometers
     * @param depth current depth
     * @param results list to collect results
     */
    private void radiusSearchRecursive(KDNode node, double targetLat, double targetLon, 
                                       double radiusKm, int depth, List<StationDistance> results) {
        if (node == null) {
            return;
        }
        
        // Check all stations in the bucket
        for (Station station : node.getBucket()) {
            double distance = haversineDistance(targetLat, targetLon, 
                                               station.getLatitude(), station.getLongitude());
            if (distance <= radiusKm) {
                results.add(new StationDistance(station, distance));
            }
        }
        
        int axis = node.getAxis();
        double splitValue = node.getSplitValue();
        double targetCoord = (axis == 0) ? targetLat : targetLon;
        
        // Calculate distance from target to split plane
        double distanceToSplit = Math.abs(targetCoord - splitValue);
        
        // Convert distance along axis to approximate distance in km
        double axisDistanceKm;
        if (axis == 0) {
            axisDistanceKm = distanceToSplit * 111.0; // Latitude
        } else {
            axisDistanceKm = distanceToSplit * 111.0 * Math.cos(Math.toRadians(targetLat)); // Longitude
        }
        
        // Decide which subtrees to explore
        if (targetCoord <= splitValue) {
            // Target is on the left side
            // Always explore left subtree
            radiusSearchRecursive(node.getLeft(), targetLat, targetLon, radiusKm, depth + 1, results);
            
            // Only explore right subtree if the split plane is within the search radius
            if (axisDistanceKm <= radiusKm) {
                radiusSearchRecursive(node.getRight(), targetLat, targetLon, radiusKm, depth + 1, results);
            }
        } else {
            // Target is on the right side
            // Always explore right subtree
            radiusSearchRecursive(node.getRight(), targetLat, targetLon, radiusKm, depth + 1, results);
            
            // Only explore left subtree if the split plane is within the search radius
            if (axisDistanceKm <= radiusKm) {
                radiusSearchRecursive(node.getLeft(), targetLat, targetLon, radiusKm, depth + 1, results);
            }
        }
    }
    
    /**
     * Helper class to store a station with its distance from a target point.
     */
    public static class StationDistance implements Comparable<StationDistance> {
        private final Station station;
        private final double distance; // in kilometers
        
        public StationDistance(Station station, double distance) {
            this.station = station;
            this.distance = distance;
        }
        
        public Station getStation() {
            return station;
        }
        
        public double getDistance() {
            return distance;
        }
        
        @Override
        public int compareTo(StationDistance other) {
            // First compare by distance (ascending)
            int distanceCompare = Double.compare(this.distance, other.distance);
            if (distanceCompare != 0) {
                return distanceCompare;
            }
            
            // If distances are equal, compare by station name (descending)
            String name1 = this.station.getName();
            String name2 = other.station.getName();
            return name2.compareTo(name1); // Reverse order for descending
        }
        
        @Override
        public String toString() {
            return String.format("%s (%.2f km)", station.getName(), distance);
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
