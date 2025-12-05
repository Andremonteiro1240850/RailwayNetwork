package Controller;

import java.util.Map;

/**
 * Data class containing all KD-tree metrics and complexity analysis.
 * 
 * @author Development Team
 * @version 1.0
 */
public class KDTreeMetrics {
    private final int numNodes;
    private final int numStations;
    private final int height;
    private final Map<Integer, Integer> bucketStats;
    
    public KDTreeMetrics(int numNodes, int numStations, int height, 
                        Map<Integer, Integer> bucketStats) {
        this.numNodes = numNodes;
        this.numStations = numStations;
        this.height = height;
        this.bucketStats = bucketStats;
    }
    
    public int getNumNodes() {
        return numNodes;
    }
    
    public int getNumStations() {
        return numStations;
    }
    
    public int getHeight() {
        return height;
    }
    
    public Map<Integer, Integer> getBucketStats() {
        return bucketStats;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("KD-Tree Data Structure Information:\n");
        sb.append("====================================\n\n");
        
        // Estrutura de dados da KD-tree
        sb.append("Data Structure: Balanced 2D-tree (KD-tree, k=2)\n");
        sb.append("  - Type: Binary tree with alternating axis splits (latitude/longitude)\n");
        sb.append("  - Split Strategy: Median selection for balanced tree\n");
        sb.append("  - Node Structure: Each node contains:\n");
        sb.append("    * Axis (0=latitude, 1=longitude)\n");
        sb.append("    * Split value (coordinate value)\n");
        sb.append("    * Bucket (list of stations with same coordinates, sorted by name)\n");
        sb.append("    * Left and right child nodes\n");
        sb.append("  - Bucket Handling: Stations with identical (lat, lon) are stored in buckets\n");
        sb.append("  - Build Algorithm: Deterministic bulk-build with O(n log n) complexity\n\n");
        
        sb.append("Tree Statistics:\n");
        sb.append("---------------\n");
        sb.append("Total Number of Stations: ").append(numStations).append("\n");
        sb.append("Total Number of Nodes: ").append(numNodes).append("\n");
        sb.append("Tree Height: ").append(height).append("\n\n");
        
        sb.append("Bucket Size Distribution:\n");
        sb.append("------------------------\n");
        
        if (bucketStats.isEmpty()) {
            sb.append("  No buckets found.\n");
        } else {
            bucketStats.entrySet().stream()
                .sorted((a, b) -> {
                    int cmp = b.getValue().compareTo(a.getValue());
                    return cmp != 0 ? cmp : b.getKey().compareTo(a.getKey());
                })
                .forEach(entry -> sb.append("  Bucket size ")
                    .append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append(" node(s)\n"));
        }
        
        return sb.toString();
    }
}

