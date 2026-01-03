package Lapr.Domain;

import Sprint1.Domain.Facility;
import Sprint2.Domain.LineSegment;
import Lapr.Domain.ConflictResolutionCalculator.ResolutionResult;

/**
 * Builds human-readable descriptions for conflicts.
 * Separates presentation logic from business logic.
 * 
 * @author Development Team
 * @version 1.0
 */
public class ConflictDescriptionBuilder {
    
    /**
     * Builds a description for a conflict with siding resolution.
     * 
     * @param segmentId the segment ID
     * @param train1Id the first train ID
     * @param train2Id the second train ID
     * @param firstTrainId the train that goes first
     * @param waitingTrainId the train that waits
     * @param stationName the station name where waiting occurs
     * @param resolutionResult the resolution calculation result
     * @param arriveSimultaneously true if trains arrive simultaneously
     * @return formatted description string
     */
    public static String buildSidingDescription(String segmentId,
                                               String train1Id,
                                               String train2Id,
                                               String firstTrainId,
                                               String waitingTrainId,
                                               String stationName,
                                               ResolutionResult resolutionResult,
                                               boolean arriveSimultaneously) {
        // This method is deprecated - use buildSidingDescriptionWithSize instead
        // which requires siding size to be passed explicitly
        throw new UnsupportedOperationException("Use buildSidingDescriptionWithSize instead, which requires explicit siding size");
    }
    
    /**
     * Builds a description for a conflict without siding (simple wait).
     * 
     * @param segmentId the segment ID
     * @param train1Id the first train ID
     * @param train2Id the second train ID
     * @param firstTrainId the train that goes first
     * @param waitingTrainId the train that waits
     * @param stationName the station name where waiting occurs
     * @param waitTimeMinutes the wait time in minutes
     * @param arriveSimultaneously true if trains arrive simultaneously
     * @return formatted description string
     */
    public static String buildNoSidingDescription(String segmentId,
                                                 String train1Id,
                                                 String train2Id,
                                                 String firstTrainId,
                                                 String waitingTrainId,
                                                 String stationName,
                                                 long waitTimeMinutes,
                                                 boolean arriveSimultaneously) {
        if (arriveSimultaneously) {
            return String.format(
                "Segment %s is SINGLE track (no siding). Both trains %s and %s arrive simultaneously. Train %s waits at station %s for %d minutes before entering the single-track segment to allow train %s to pass first.",
                segmentId, train1Id, train2Id, waitingTrainId, stationName, waitTimeMinutes, firstTrainId);
        } else {
            return String.format(
                "Segment %s is SINGLE track (no siding). Train %s arrives first. Train %s waits at station %s for %d minutes before entering the single-track segment.",
                segmentId, firstTrainId, waitingTrainId, stationName, waitTimeMinutes);
        }
    }
    
    /**
     * Builds a description with siding size information.
     * Helper method that includes siding size in the description.
     * 
     * @param segmentId the segment ID
     * @param train1Id the first train ID
     * @param train2Id the second train ID
     * @param firstTrainId the train that goes first
     * @param waitingTrainId the train that waits
     * @param stationName the station name
     * @param segment the line segment (for getting siding size)
     * @param resolutionResult the resolution calculation result
     * @param arriveSimultaneously true if trains arrive simultaneously
     * @return formatted description string
     */
    public static String buildSidingDescriptionWithSize(String segmentId,
                                                        String train1Id,
                                                        String train2Id,
                                                        String firstTrainId,
                                                        String waitingTrainId,
                                                        String stationName,
                                                        LineSegment segment,
                                                        ResolutionResult resolutionResult,
                                                        boolean arriveSimultaneously) {
        // Get siding size from segment, or use default 500m if not specified
        Integer sidingSizeM = segment != null && segment.getSidingSize() != null ?
            segment.getSidingSize() : 500;
        
        if (arriveSimultaneously) {
            return String.format(
                "Segment %s is SINGLE track with siding (size: %dm). Both trains %s and %s arrive simultaneously. Train %s enters the segment and waits at the siding while train %s passes. Total delay at siding: %d minutes (including %d minutes for siding traversal).",
                segmentId, sidingSizeM, train1Id, train2Id, waitingTrainId, firstTrainId, 
                resolutionResult.getWaitTimeMinutes(), resolutionResult.getCrossingTimeMinutes());
        } else {
            return String.format(
                "Segment %s is SINGLE track with siding (size: %dm). Train %s arrives first. Train %s enters the segment and waits at the siding for %d minutes while train %s passes. Siding traversal time: %d minutes.",
                segmentId, sidingSizeM, firstTrainId, waitingTrainId, resolutionResult.getBaseWaitMinutes(), 
                firstTrainId, resolutionResult.getCrossingTimeMinutes());
        }
    }
}

