package Domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete dispatch report containing train schedules, conflicts, and summary.
 * This class is used for USPL07 - Train Dispatch Scheduler.
 *
 * @author Development Team
 * @version 1.0
 */
public class DispatchReport {
    
    private final List<TrainSchedule> trainSchedules;
    private final List<Conflict> conflicts;
    private final Summary summary;

    /**
     * Represents a summary of the dispatch operation.
     */
    public static class Summary {
        private final int totalTrainsDispatched;
        private final int numberOfConflicts;
        private final int numberOfResolvableConflicts;
        private final int numberOfUnsolvableConflicts;

        public Summary(int totalTrainsDispatched, int numberOfConflicts,
                      int numberOfResolvableConflicts, int numberOfUnsolvableConflicts) {
            this.totalTrainsDispatched = totalTrainsDispatched;
            this.numberOfConflicts = numberOfConflicts;
            this.numberOfResolvableConflicts = numberOfResolvableConflicts;
            this.numberOfUnsolvableConflicts = numberOfUnsolvableConflicts;
        }

        public int getTotalTrainsDispatched() {
            return totalTrainsDispatched;
        }

        public int getNumberOfConflicts() {
            return numberOfConflicts;
        }

        public int getNumberOfResolvableConflicts() {
            return numberOfResolvableConflicts;
        }

        public int getNumberOfUnsolvableConflicts() {
            return numberOfUnsolvableConflicts;
        }

        @Override
        public String toString() {
            return String.format("Summary[Trains: %d, Conflicts: %d (Resolvable: %d, Unsolvable: %d)]",
                               totalTrainsDispatched, numberOfConflicts,
                               numberOfResolvableConflicts, numberOfUnsolvableConflicts);
        }
    }

    /**
     * Constructs a new DispatchReport with the specified parameters.
     *
     * @param trainSchedules the list of train schedules
     * @param conflicts      the list of detected conflicts
     * @param summary        the summary of the dispatch operation
     * @throws IllegalArgumentException if parameters are invalid
     */
    public DispatchReport(List<TrainSchedule> trainSchedules, List<Conflict> conflicts, Summary summary) {
        if (trainSchedules == null) {
            throw new IllegalArgumentException("Train schedules cannot be null");
        }
        if (conflicts == null) {
            throw new IllegalArgumentException("Conflicts cannot be null");
        }
        if (summary == null) {
            throw new IllegalArgumentException("Summary cannot be null");
        }
        
        this.trainSchedules = new ArrayList<>(trainSchedules);
        this.conflicts = new ArrayList<>(conflicts);
        this.summary = summary;
    }

    /**
     * Gets the list of train schedules.
     *
     * @return the train schedules
     */
    public List<TrainSchedule> getTrainSchedules() {
        return new ArrayList<>(trainSchedules);
    }

    /**
     * Gets the list of conflicts.
     *
     * @return the conflicts
     */
    public List<Conflict> getConflicts() {
        return new ArrayList<>(conflicts);
    }

    /**
     * Gets the summary of the dispatch operation.
     *
     * @return the summary
     */
    public Summary getSummary() {
        return summary;
    }

    /**
     * Gets a train schedule by train ID.
     *
     * @param trainId the train ID
     * @return the train schedule, or null if not found
     */
    public TrainSchedule getTrainSchedule(String trainId) {
        return trainSchedules.stream()
                .filter(schedule -> schedule.getTrainId().equals(trainId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return String.format("DispatchReport[Schedules: %d, Conflicts: %d, %s]",
                           trainSchedules.size(), conflicts.size(), summary);
    }
}

