package Lapr.Domain;

import Sprint1.Domain.Facility;
import Sprint2.Domain.LineSegment;
import java.time.LocalDateTime;

/**
 * Calculates conflict resolution details including wait times and crossing times.
 * Separates calculation logic from description building and service orchestration.
 * 
 * @author Development Team
 * @version 1.0
 */
public class ConflictResolutionCalculator {
    
    /**
     * Result of conflict resolution calculation.
     */
    public static class ResolutionResult {
        private final DispatchReport.Conflict.ResolutionType resolutionType;
        private final long waitTimeMinutes;
        private final long baseWaitMinutes;
        private final long crossingTimeMinutes;
        private final Facility crossingStation;
        private final boolean hasSiding;
        
        public ResolutionResult(DispatchReport.Conflict.ResolutionType resolutionType,
                               long waitTimeMinutes,
                               long baseWaitMinutes,
                               long crossingTimeMinutes,
                               Facility crossingStation,
                               boolean hasSiding) {
            this.resolutionType = resolutionType;
            this.waitTimeMinutes = waitTimeMinutes;
            this.baseWaitMinutes = baseWaitMinutes;
            this.crossingTimeMinutes = crossingTimeMinutes;
            this.crossingStation = crossingStation;
            this.hasSiding = hasSiding;
        }
        
        public DispatchReport.Conflict.ResolutionType getResolutionType() {
            return resolutionType;
        }
        
        public long getWaitTimeMinutes() {
            return waitTimeMinutes;
        }
        
        public long getBaseWaitMinutes() {
            return baseWaitMinutes;
        }
        
        public long getCrossingTimeMinutes() {
            return crossingTimeMinutes;
        }
        
        public Facility getCrossingStation() {
            return crossingStation;
        }
        
        public boolean hasSiding() {
            return hasSiding;
        }
    }
    
    /**
     * Calculates the time needed to cross a siding in minutes.
     * 
     * @param sidingSizeM the siding size in meters
     * @param trainSpeedKmH the train speed in km/h
     * @return crossing time in minutes (minimum 1 minute)
     */
    public static long calculateSidingCrossingTime(int sidingSizeM, double trainSpeedKmH) {
        // Convert speed to m/s and calculate time to cross siding
        double trainSpeedMs = trainSpeedKmH / 3.6; // km/h to m/s
        double crossingTimeSeconds = sidingSizeM / trainSpeedMs;
        return Math.max(1, Math.round(crossingTimeSeconds / 60.0)); // Convert to minutes, minimum 1 minute
    }
    
    /**
     * Calculates conflict resolution for a single-track segment conflict.
     * 
     * PRECONDITION: This method assumes:
     * - The segment is SINGLE track (conflict exists)
     * - There is a conflict between two trains
     * - The decision of which train waits has already been made
     * 
     * This method only calculates times, it does NOT decide rules.
     * 
     * @param segment the line segment (single track, with conflict)
     * @param waitingTrainPreviousStation the station where the waiting train is
     * @param waitingTrainArrivalTime the time the waiting train arrives at the previous station
     * @param firstTrainSegmentEndTime the time the first train finishes passing the segment
     * @param waitingTrainSpeedKmH the speed of the waiting train in km/h
     * @param hasSiding true if siding is available (decision already made by caller)
     * @param sidingSizeM the siding size in meters (if hasSiding is true)
     * @param arriveSimultaneously true if trains arrive at the same time
     * @return resolution result with calculated times and resolution type
     */
    public static ResolutionResult calculateResolution(LineSegment segment,
                                                      Facility waitingTrainPreviousStation,
                                                      LocalDateTime waitingTrainArrivalTime,
                                                      LocalDateTime firstTrainSegmentEndTime,
                                                      double waitingTrainSpeedKmH,
                                                      boolean hasSiding,
                                                      Integer sidingSizeM,
                                                      boolean arriveSimultaneously) {
        Facility crossingStation = null;
        if (hasSiding && waitingTrainPreviousStation != null && waitingTrainPreviousStation.hasSiding()) {
            crossingStation = waitingTrainPreviousStation;
        }
        
        if (hasSiding) {
            // Has siding: calculate crossing time
            // Use provided siding size or default 500m if not specified
            int effectiveSidingSizeM = (sidingSizeM != null && sidingSizeM > 0) ? sidingSizeM : 500;
            
            long crossingTimeMinutes = calculateSidingCrossingTime(effectiveSidingSizeM, waitingTrainSpeedKmH);
            
            // Calculate base wait time (time until first train passes)
            long baseWaitMinutes = 0;
            if (!arriveSimultaneously) {
                baseWaitMinutes = Math.max(0, java.time.Duration.between(
                    waitingTrainArrivalTime, 
                    firstTrainSegmentEndTime
                ).toMinutes());
            }
            
            // Total wait time
            long waitTimeMinutes = arriveSimultaneously ? crossingTimeMinutes : (baseWaitMinutes + crossingTimeMinutes);
            
            return new ResolutionResult(
                DispatchReport.Conflict.ResolutionType.CROSS_AT_STATION_WITH_SIDING,
                waitTimeMinutes,
                baseWaitMinutes,
                crossingTimeMinutes,
                crossingStation,
                true
            );
        } else {
            // No siding: waiting train must wait until first train passes
            long waitTimeMinutes = Math.max(0, java.time.Duration.between(
                waitingTrainArrivalTime,
                firstTrainSegmentEndTime
            ).toMinutes());
            
            return new ResolutionResult(
                DispatchReport.Conflict.ResolutionType.WAIT_AT_PREVIOUS_STATION,
                waitTimeMinutes,
                waitTimeMinutes,
                0,
                null,
                false
            );
        }
    }
}

