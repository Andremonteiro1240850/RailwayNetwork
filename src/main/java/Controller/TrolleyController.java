package Controller;

import Domain.AllocationRow;
import Domain.Trolley;
import Repository.WarehouseRepository;

import java.util.List;

public class TrolleyController {
    private Trolley trolley;
    private List<AllocationRow> allocationRows ;
    private WarehouseRepository warehouseRepository;

    public TrolleyController(Trolley trolley) {
        this.trolley = trolley;
    }

    public Trolley createTrolley(int maxCapacity, String optionHeuristic) {
        Trolley trolley = new Trolley(maxCapacity, 0.0, (int)(Math.random()*10000)+1, null, optionHeuristic);
        this.trolley = trolley;
        return trolley;
    }

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
    public void packing () {
        if(trolley.getOptionHeuristic()=="FF"){
            //First Fit
        } else if (trolley.getOptionHeuristic()=="FFD") {
            //First Fit Decreasing
        } else if (trolley.getOptionHeuristic()=="BFD") {
            //Best Fit Decreasing
        }


    }
    public Trolley getTrolley() {
        return trolley;
    }
    public void setTrolley(Trolley trolley) {
        this.trolley = trolley;
    }

    @Override
    public String toString() {
        return "TrolleyController{" +
                "trolley=" + trolley +
                '}';
    }
}
