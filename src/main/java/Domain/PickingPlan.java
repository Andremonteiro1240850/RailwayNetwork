package Domain;

import java.util.List;

/**
 * The type Picking plan.
 */
public class PickingPlan {
    private final List<Trolley> trolleys;
    private final int totalTrolleys;
    private final double averageUtilization;
    private final String heuristic;

    /**
     * Instantiates a new Picking plan.
     *
     * @param trolleys  the trolleys
     * @param heuristic the heuristic used
     */
    public PickingPlan(List<Trolley> trolleys, String heuristic) {
        this.trolleys = trolleys;
        this.totalTrolleys = trolleys.size();
        this.heuristic = heuristic;
        
        // Calculate average utilization
        double totalUtilization = 0.0;
        for (Trolley trolley : trolleys) {
            double utilization = (trolley.getCurrentLoad() / trolley.getMaxCapacity()) * 100;
            totalUtilization += utilization;
        }
        this.averageUtilization = totalTrolleys > 0 ? totalUtilization / totalTrolleys : 0.0;
    }

    /**
     * Gets trolleys.
     *
     * @return the trolleys
     */
    public List<Trolley> getTrolleys() {
        return trolleys;
    }

    /**
     * Gets total trolleys.
     *
     * @return the total trolleys
     */
    public int getTotalTrolleys() {
        return totalTrolleys;
    }

    /**
     * Gets average utilization.
     *
     * @return the average utilization
     */
    public double getAverageUtilization() {
        return averageUtilization;
    }

    /**
     * Gets heuristic.
     *
     * @return the heuristic
     */
    public String getHeuristic() {
        return heuristic;
    }
}
