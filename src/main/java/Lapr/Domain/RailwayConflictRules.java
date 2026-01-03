package Lapr.Domain;

import Sprint1.Domain.Facility;
import Sprint2.Domain.LineSegment;

/**
 * Centralizes railway domain rules for conflict resolution.
 * This class contains the business logic for determining how conflicts should be resolved
 * based on track type, siding availability, and train arrival times.
 * 
 * Separates domain rules from service logic to improve maintainability and testability.
 * 
 * @author Development Team
 * @version 1.0
 */
public class RailwayConflictRules {
    
    /**
     * Determines if a segment allows simultaneous passage (no conflict).
     * 
     * @param segment the line segment to check
     * @return true if segment allows simultaneous passage (DOUBLE or QUAD track), false otherwise
     */
    public static boolean allowsSimultaneousPassage(LineSegment segment) {
        if (segment == null) {
            return false; // Conservative: assume single track if unknown
        }
        return segment.isDoubleTrack() || segment.isQuadTrack();
    }
    
    /**
     * Determines if a segment has siding available for crossing.
     * Checks both station siding and segment siding.
     * 
     * @param segment the line segment
     * @param station the station to check for siding
     * @return true if siding is available, false otherwise
     */
    public static boolean hasSidingAvailable(LineSegment segment, Facility station) {
        boolean hasStationSiding = station != null && station.hasSiding();
        boolean hasSegmentSiding = segment != null && Boolean.TRUE.equals(segment.getHasSiding());
        return hasStationSiding || hasSegmentSiding;
    }
    
    /**
     * Determines which train should wait when both arrive simultaneously.
     * Uses deterministic rule: train with lexicographically smaller ID goes first.
     * 
     * @param train1Id the first train ID
     * @param train2Id the second train ID
     * @return true if train1 should go first, false if train2 should go first
     */
    public static boolean shouldTrain1GoFirst(String train1Id, String train2Id) {
        return train1Id.compareTo(train2Id) < 0;
    }
    
    /**
     * Determines if a conflict is unsolvable.
     * A conflict is unsolvable if:
     * - Single track segment (already verified before calling this)
     * - No siding available
     * - Trains arrive simultaneously
     * 
     * @param segment the line segment (must be single track)
     * @param station the station where trains would wait
     * @param arriveSimultaneously true if trains arrive at the same time
     * @return true if conflict is unsolvable, false otherwise
     */
    public static boolean isUnsolvable(LineSegment segment, Facility station, boolean arriveSimultaneously) {
        if (!arriveSimultaneously) {
            return false; // Can resolve by having one train wait
        }
        // Single track, simultaneous arrival, check for siding
        return !hasSidingAvailable(segment, station);
    }
    
    /**
     * Checks if a segment is single track.
     * 
     * @param segment the line segment
     * @return true if single track, false otherwise
     */
    public static boolean isSingleTrack(LineSegment segment) {
        if (segment == null) {
            return true; // Conservative: assume single track if unknown
        }
        return segment.isSingleTrack();
    }
    
    /**
     * Checks if there is a conflict between two trains on a segment.
     * A conflict exists if:
     * - Segment is single track (not double/quad)
     * - Trains have overlapping times on the same segment
     * 
     * Note: This method only checks if a conflict CAN exist based on track type.
     * Time overlap must be verified separately.
     * 
     * @param segment the line segment
     * @return true if conflict can exist (single track), false otherwise
     */
    public static boolean isConflict(LineSegment segment) {
        return isSingleTrack(segment);
    }
}

