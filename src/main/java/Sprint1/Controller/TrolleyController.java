package Sprint1.Controller;

import Sprint1.Domain.Item;
import Sprint1.Domain.PickingPlan;
import Sprint1.Domain.Trolley;
import Sprint1.Domain.ENUM.PackingHeuristic;
import Sprint1.Repository.TrolleyRepository;
import Sprint1.Repository.WarehouseRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Trolley controller.
 */
public class TrolleyController {

    private Trolley trolley;
    private WarehouseRepository warehouseRepository;
    private Item item;
    private List<Trolley> trolleyList;
    private TrolleyRepository trolleyRepository;


    /**
     * Instantiates a new Trolley controller.
     *
     * @param warehouseRepository the warehouse repository
     * @param trolleyRepository   the trolley repository
     */
    public TrolleyController(WarehouseRepository warehouseRepository, TrolleyRepository trolleyRepository) {
        if (warehouseRepository == null || trolleyRepository == null) {
            throw new IllegalArgumentException("Repositórios não podem ser nulos.");
        }
        this.warehouseRepository = warehouseRepository;
        this.trolleyRepository = trolleyRepository;
        this.trolleyList = new ArrayList<>();
        this.trolley = null; // só será criado mais tarde via createTrolley()
    }


    /**
     * Create trolley trolley.
     *
     * @param maxCapacity     the max capacity
     * @param optionHeuristic the option heuristic
     * @return the trolley
     */
    public Trolley createTrolley(int maxCapacity, String optionHeuristic) {
        Trolley trolley = new Trolley(maxCapacity, 0.0,
                (int)(Math.random()*10000)+1, new ArrayList<>(), optionHeuristic);
        this.trolley = trolley;
        trolleyList.add(trolley);
        return trolley;
    }

    /**
     * Reduce load double.
     *
     * @param weight the weight
     * @return the double
     */
    public double reduceLoad(double weight) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Weight to reduce must be greater than zero.");
        }
        double currentLoad = trolley.getCurrentLoad();
        if (weight > currentLoad) {
            throw new IllegalArgumentException("Weight to reduce exceeds current load.");
        }
        double newLoad = currentLoad - weight;
        trolley.setCurrentLoad(newLoad);
        return newLoad;
    }

    /**
     * Gets trolley.
     *
     * @return the trolley
     */
    public Trolley getTrolley() {
        return trolley;
    }

    /**
     * Sets trolley.
     *
     * @param trolley the trolley
     */
    public void setTrolley(Trolley trolley) {
        this.trolley = trolley;
    }

    /**
     * Gets trolley list.
     *
     * @return the trolley list
     */
    public List<Trolley> getTrolleyList() {
        return trolleyList;
    }

    /**
     * USEI03 - Create picking plan using new implementation
     *
     * @param capacity       the trolley capacity
     * @param heuristic      the packing heuristic
     * @param allowSplitting whether to allow splitting
     * @return the picking plan
     */
    public PickingPlan createPickingPlan(int capacity, PackingHeuristic heuristic, boolean allowSplitting) {
        PickingPlanController pickingPlanController = new PickingPlanController(warehouseRepository);
        return pickingPlanController.createPickingPlan(capacity, heuristic, allowSplitting);
    }

    @Override
    public String toString() {
        return "TrolleyController => " +
                "trolley: " + trolley +
                '.';
    }

    public void packing() {
    }
}
