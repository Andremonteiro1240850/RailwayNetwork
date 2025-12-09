package Sprint1.Domain;

import java.util.List;
import java.util.Objects;

/**
 * The type Trolley.
 */
public class Trolley {
    private int maxCapacity;
    private double currentLoad;
    private int id;
    /**
     * The Picking items.
     */
    List<PickingItems> pickingItems;
    private String optionHeuristic;

    /**
     * Instantiates a new Trolley.
     *
     * @param maxCapacity     the max capacity
     * @param currentLoad     the current load
     * @param id              the id
     * @param pickingItems    the picking items
     * @param optionHeuristic the option heuristic
     */
    public  Trolley(int maxCapacity, double currentLoad, int id, List<PickingItems> pickingItems, String optionHeuristic) {
        this.maxCapacity = maxCapacity;
        this.currentLoad = currentLoad;
        this.id = id;
        this.pickingItems = pickingItems;
        this.optionHeuristic = optionHeuristic;
    }

    /**
     * Gets max capacity.
     *
     * @return the max capacity
     */
    public int getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * Gets current load.
     *
     * @return the current load
     */
    public double getCurrentLoad() {
        return currentLoad;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Gets option heuristic.
     *
     * @return the option heuristic
     */
    public String getOptionHeuristic() {
        return optionHeuristic;
    }

    /**
     * Gets picking items.
     *
     * @return the picking items
     */
    public List<PickingItems> getPickingItems() {
        return pickingItems;
    }

    /**
     * Sets picking items.
     *
     * @param pickingItems the picking items
     */
    public void setPickingItems(List<PickingItems> pickingItems) {
        this.pickingItems = pickingItems;
    }

    /**
     * Sets current load.
     *
     * @param currentLoad the current load
     */
    public void setCurrentLoad(double currentLoad) {
        this.currentLoad = currentLoad;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets max capacity.
     *
     * @param maxCapacity the max capacity
     */
    public  void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    /**
     * Sets option heuristic.
     *
     * @param optionHeuristic the option heuristic
     */
    public void setOptionHeuristic(String optionHeuristic) {
        this.optionHeuristic = optionHeuristic;
    }

    @Override
    public String toString() {
        return "Trolley: " +
                "maxCapacity=" + maxCapacity +
                ", currentLoad=" + currentLoad +
                ", id=" + id +
                '.';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Trolley trolley = (Trolley) o;
        return id == trolley.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxCapacity, currentLoad, id);
    }
}
