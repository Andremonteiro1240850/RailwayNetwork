package Domain;

import java.util.List;
import java.util.Objects;

public class Trolley {
    private int maxCapacity;
    private double currentLoad;
    private int id;
    List<PickingItems> pickingItems;
    private String optionHeuristic;

    public  Trolley(int maxCapacity, double currentLoad, int id, List<PickingItems> pickingItems, String optionHeuristic) {
        this.maxCapacity = maxCapacity;
        this.currentLoad = currentLoad;
        this.id = id;
        this.pickingItems = pickingItems;
        this.optionHeuristic = optionHeuristic;
    }
    public int getMaxCapacity() {
        return maxCapacity;
    }
    public double getCurrentLoad() {
        return currentLoad;
    }
    public int getId() {
        return id;
    }
    public String getOptionHeuristic() {
        return optionHeuristic;
    }
    public List<PickingItems> getPickingItems() {
        return pickingItems;
    }
    public void setPickingItems(List<PickingItems> pickingItems) {
        this.pickingItems = pickingItems;
    }
    public void setCurrentLoad(double currentLoad) {
        this.currentLoad = currentLoad;
    }
    public void setId(int id) {
        this.id = id;
    }
    public  void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
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
