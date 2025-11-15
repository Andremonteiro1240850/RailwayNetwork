package Controller;

import Domain.KDTree;
import Domain.Station;
import Repository.StationRepository;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controller for KD-tree operations.
 * Handles building balanced KD-trees and generating reports.
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
     *
     * @return true if build was successful
     */
    public boolean buildTree() {
        if (stationRepository == null) {
            return false;
        }
        
        try {
            this.kdTree = new KDTree();
            this.kdTree.buildKdTree(stationRepository);
            return true;
        } catch (Exception e) {
            System.err.println("Error building KD-tree: " + e.getMessage());
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
     * Generates and saves a build report.
     *
     * @param outputPath path to save the report
     * @return true if report was generated successfully
     */
    public boolean generateReport(String outputPath) {
        if (!isTreeBuilt()) {
            return false;
        }
        
        if (outputPath == null || outputPath.isEmpty()) {
            outputPath = "kdtree_report.txt";
        }
        
        try (FileWriter writer = new FileWriter(outputPath)) {
            // Build timestamp
            String timestamp = Instant.now().atOffset(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ISO_INSTANT);
            
            // Get metrics
            int[] sizeInfo = kdTree.getSize();
            int numNodes = sizeInfo[0];
            int numStations = sizeInfo[1];
            int height = kdTree.height();
            Map<Integer, Integer> bucketStats = kdTree.getBucketStats();
            
            // Write report
            writer.write("Balanced 2D-tree (KD-tree) Build Report\n");
            writer.write("========================================\n\n");
            writer.write("Build Timestamp: " + timestamp + "\n");
            writer.write("Number of Stations Processed: " + numStations + "\n");
            writer.write("Number of Nodes: " + numNodes + "\n");
            writer.write("Tree Height: " + height + "\n\n");
            
            writer.write("Distinct Bucket Sizes (sorted descending by count):\n");
            writer.write("----------------------------------------------------\n");
            
            // Sort by count descending, then by bucket size
            List<Map.Entry<Integer, Integer>> sortedStats = new ArrayList<>(bucketStats.entrySet());
            sortedStats.sort((a, b) -> {
                int cmp = b.getValue().compareTo(a.getValue());
                if (cmp != 0) return cmp;
                return b.getKey().compareTo(a.getKey());
            });
            
            for (Map.Entry<Integer, Integer> entry : sortedStats) {
                writer.write("Bucket size " + entry.getKey() + ": " + entry.getValue() + " nodes\n");
            }
            
            writer.write("\n");
            writer.write("Complexity Analysis:\n");
            writer.write("-------------------\n");
            writer.write("Time Complexity: O(n log n)\n");
            writer.write("  - Sorting: O(n log n) for both latitude and longitude arrays\n");
            writer.write("  - Bulk build: O(n log n) - each level processes O(n) elements, log n levels\n");
            writer.write("  - Median selection: O(1) per level (using sorted arrays)\n");
            writer.write("  - Partitioning: O(n) per level\n");
            writer.write("  - Overall: O(n log n)\n\n");
            
            writer.write("Space Complexity: O(n)\n");
            writer.write("  - Tree nodes: O(n) in worst case (one node per unique coordinate)\n");
            writer.write("  - Buckets: O(n) total (all stations stored)\n");
            writer.write("  - Sorted arrays: O(n) temporary space during build\n");
            writer.write("  - Overall: O(n)\n\n");
            
            writer.write("Tree Height Analysis:\n");
            writer.write("  - Worst-case height: O(n) if tree is unbalanced (should not occur with median selection)\n");
            writer.write("  - Expected height: O(log n) for balanced tree with median selection\n");
            writer.write("  - Actual height: " + height + " (for " + numStations + " stations)\n");
            if (numStations > 0) {
                writer.write("  - Optimal height: " + (int)Math.ceil(Math.log(numStations) / Math.log(2)) + " (ceiling of log2(n))\n");
            }
            writer.write("\n");
            
            writer.write("Determinism:\n");
            writer.write("  - The algorithm is deterministic: same input produces same tree structure\n");
            writer.write("  - Uses median selection from sorted arrays\n");
            writer.write("  - Buckets are sorted by station name (ascending) for consistent ordering\n");
            
            return true;
        } catch (IOException e) {
            System.err.println("Error writing report: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets a formatted string with tree metrics.
     *
     * @return formatted metrics string
     */
    public String getMetricsString() {
        if (!isTreeBuilt()) {
            return "KD-tree not built yet.";
        }
        
        int[] sizeInfo = kdTree.getSize();
        int numNodes = sizeInfo[0];
        int numStations = sizeInfo[1];
        int height = kdTree.height();
        Map<Integer, Integer> bucketStats = kdTree.getBucketStats();
        
        StringBuilder sb = new StringBuilder();
        sb.append("KD-Tree Metrics:\n");
        sb.append("===============\n");
        sb.append("Number of Stations: ").append(numStations).append("\n");
        sb.append("Number of Nodes: ").append(numNodes).append("\n");
        sb.append("Tree Height: ").append(height).append("\n");
        sb.append("\nDistinct Bucket Sizes:\n");
        
        List<Map.Entry<Integer, Integer>> sortedStats = new ArrayList<>(bucketStats.entrySet());
        sortedStats.sort((a, b) -> {
            int cmp = b.getValue().compareTo(a.getValue());
            if (cmp != 0) return cmp;
            return b.getKey().compareTo(a.getKey());
        });
        
        for (Map.Entry<Integer, Integer> entry : sortedStats) {
            sb.append("  Bucket size ").append(entry.getKey())
              .append(": ").append(entry.getValue()).append(" nodes\n");
        }
        
        return sb.toString();
    }
}

