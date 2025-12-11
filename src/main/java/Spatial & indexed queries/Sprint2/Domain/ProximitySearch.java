package Sprint2.Domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the result of a proximity search (Nearest-N search).
 * Contains the target point, number of stations requested, and the list of nearest stations with distances.
 * 
 * @author Development Team
 * @version 1.0
 */
public class ProximitySearch {
    
    private final double targetLatitude;
    private final double targetLongitude;
    private final int requestedN;
    private final List<KDTree.StationDistance> nearestStations;
    private final SearchFilters appliedFilters;
    
    /**
     * Constructs a new ProximitySearch result.
     *
     * @param targetLatitude target latitude coordinate
     * @param targetLongitude target longitude coordinate
     * @param requestedN number of nearest stations requested
     * @param nearestStations list of nearest stations with distances (sorted by distance)
     * @param appliedFilters filters applied to the search (can be null)
     */
    public ProximitySearch(double targetLatitude, double targetLongitude, 
                          int requestedN, 
                          List<KDTree.StationDistance> nearestStations,
                          SearchFilters appliedFilters) {
        this.targetLatitude = targetLatitude;
        this.targetLongitude = targetLongitude;
        this.requestedN = requestedN;
        this.nearestStations = new ArrayList<>(nearestStations);
        this.appliedFilters = appliedFilters;
    }
    
    /**
     * Constructs a new ProximitySearch result without filters.
     *
     * @param targetLatitude target latitude coordinate
     * @param targetLongitude target longitude coordinate
     * @param requestedN number of nearest stations requested
     * @param nearestStations list of nearest stations with distances (sorted by distance)
     */
    public ProximitySearch(double targetLatitude, double targetLongitude, 
                          int requestedN, 
                          List<KDTree.StationDistance> nearestStations) {
        this(targetLatitude, targetLongitude, requestedN, nearestStations, null);
    }
    
    /**
     * Gets the target latitude.
     *
     * @return target latitude
     */
    public double getTargetLatitude() {
        return targetLatitude;
    }
    
    /**
     * Gets the target longitude.
     *
     * @return target longitude
     */
    public double getTargetLongitude() {
        return targetLongitude;
    }
    
    /**
     * Gets the number of nearest stations requested.
     *
     * @return requested N
     */
    public int getRequestedN() {
        return requestedN;
    }
    
    /**
     * Gets the list of nearest stations with distances.
     * The list is sorted by distance (ascending).
     *
     * @return list of station-distance pairs
     */
    public List<KDTree.StationDistance> getNearestStations() {
        return new ArrayList<>(nearestStations);
    }
    
    /**
     * Gets the number of stations actually found.
     *
     * @return number of stations found
     */
    public int getFoundCount() {
        return nearestStations.size();
    }
    
    /**
     * Gets the filters applied to this search.
     *
     * @return applied filters, or null if no filters were applied
     */
    public SearchFilters getAppliedFilters() {
        return appliedFilters;
    }
    
    /**
     * Checks if filters were applied to this search.
     *
     * @return true if filters were applied
     */
    public boolean hasFilters() {
        return appliedFilters != null && appliedFilters.hasFilters();
    }
    
    /**
     * Gets the distance to the nearest station.
     *
     * @return distance in kilometers, or -1 if no stations found
     */
    public double getNearestDistance() {
        if (nearestStations.isEmpty()) {
            return -1.0;
        }
        return nearestStations.get(0).getDistance();
    }
    
    /**
     * Gets the distance to the farthest station found.
     *
     * @return distance in kilometers, or -1 if no stations found
     */
    public double getFarthestDistance() {
        if (nearestStations.isEmpty()) {
            return -1.0;
        }
        return nearestStations.get(nearestStations.size() - 1).getDistance();
    }
    
    /**
     * Checks if the search found all requested stations.
     *
     * @return true if found count equals requested N
     */
    public boolean foundAllRequested() {
        return nearestStations.size() == requestedN;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ProximitySearch{");
        sb.append("target=[").append(targetLatitude).append(", ").append(targetLongitude).append("]");
        sb.append(", requested=").append(requestedN);
        sb.append(", found=").append(getFoundCount());
        if (hasFilters()) {
            sb.append(", filters=").append(appliedFilters);
        }
        if (!nearestStations.isEmpty()) {
            sb.append(", nearestDistance=").append(String.format("%.2f km", getNearestDistance()));
            sb.append(", farthestDistance=").append(String.format("%.2f km", getFarthestDistance()));
        }
        sb.append("}");
        return sb.toString();
    }
}

