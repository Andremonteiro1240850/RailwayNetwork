package Domain;

/**
 * Represents a conflict detected between two trains on a single-track segment.
 * This class is used for USPL07 - Train Dispatch Scheduler.
 *
 * @author Development Team
 * @version 1.0
 */
public class Conflict {
    
    public enum ConflictType {
        OVERLAPPING_SEGMENT,
        UNSOLVABLE
    }

    public enum ResolutionType {
        WAIT_AT_PREVIOUS_STATION,
        REVERSE_ORDER,
        UNSOLVABLE,
        NONE
    }

    private final String train1Id;
    private final String train2Id;
    private final String segmentId;
    private final ConflictType conflictType;
    private final ResolutionType resolution;
    private final String description;

    /**
     * Constructs a new Conflict with the specified parameters.
     *
     * @param train1Id      the ID of the first train
     * @param train2Id      the ID of the second train
     * @param segmentId     the ID of the conflicting segment
     * @param conflictType  the type of conflict
     * @param resolution    the suggested resolution
     * @param description   a description of the conflict
     * @throws IllegalArgumentException if parameters are invalid
     */
    public Conflict(String train1Id, String train2Id, String segmentId,
                   ConflictType conflictType, ResolutionType resolution, String description) {
        if (train1Id == null || train1Id.trim().isEmpty()) {
            throw new IllegalArgumentException("Train 1 ID cannot be null or empty");
        }
        if (train2Id == null || train2Id.trim().isEmpty()) {
            throw new IllegalArgumentException("Train 2 ID cannot be null or empty");
        }
        if (segmentId == null || segmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Segment ID cannot be null or empty");
        }
        if (conflictType == null) {
            throw new IllegalArgumentException("Conflict type cannot be null");
        }
        if (resolution == null) {
            throw new IllegalArgumentException("Resolution cannot be null");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        
        this.train1Id = train1Id.trim();
        this.train2Id = train2Id.trim();
        this.segmentId = segmentId.trim();
        this.conflictType = conflictType;
        this.resolution = resolution;
        this.description = description.trim();
    }

    /**
     * Gets the ID of the first train.
     *
     * @return the train 1 ID
     */
    public String getTrain1Id() {
        return train1Id;
    }

    /**
     * Gets the ID of the second train.
     *
     * @return the train 2 ID
     */
    public String getTrain2Id() {
        return train2Id;
    }

    /**
     * Gets the ID of the conflicting segment.
     *
     * @return the segment ID
     */
    public String getSegmentId() {
        return segmentId;
    }

    /**
     * Gets the type of conflict.
     *
     * @return the conflict type
     */
    public ConflictType getConflictType() {
        return conflictType;
    }

    /**
     * Gets the suggested resolution.
     *
     * @return the resolution
     */
    public ResolutionType getResolution() {
        return resolution;
    }

    /**
     * Gets the description of the conflict.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Conflict conflict = (Conflict) obj;
        return train1Id.equals(conflict.train1Id) &&
               train2Id.equals(conflict.train2Id) &&
               segmentId.equals(conflict.segmentId);
    }

    @Override
    public int hashCode() {
        return train1Id.hashCode() + train2Id.hashCode() + segmentId.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Conflict[Train1: %s, Train2: %s, Segment: %s, Type: %s, Resolution: %s]",
                           train1Id, train2Id, segmentId, conflictType, resolution);
    }
}

