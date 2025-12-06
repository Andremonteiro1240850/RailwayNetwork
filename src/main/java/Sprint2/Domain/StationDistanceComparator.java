package Sprint2.Domain;

import java.util.Comparator;

/**
 * Comparator for StationDistance objects.
 * Orders by distance (ascending) and then by station name (descending).
 * 
 * This comparator is used to create a BST/AVL tree sorted according to USEI10 requirements:
 * - Primary sort: distance (ASC)
 * - Secondary sort: station name (DESC)
 * 
 * @author Development Team
 * @version 1.0
 */
public class StationDistanceComparator implements Comparator<KDTree.StationDistance> {
    
    @Override
    public int compare(KDTree.StationDistance sd1, KDTree.StationDistance sd2) {
        // First compare by distance (ascending)
        int distanceCompare = Double.compare(sd1.getDistance(), sd2.getDistance());
        if (distanceCompare != 0) {
            return distanceCompare;
        }
        
        // If distances are equal, compare by station name (descending)
        String name1 = sd1.getStation().getName();
        String name2 = sd2.getStation().getName();
        return name2.compareTo(name1); // Reverse order for descending
    }
}

