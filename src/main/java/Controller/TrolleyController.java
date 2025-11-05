package Controller;

import Domain.AllocationRow;
import Domain.Item;
import Domain.PickingItems;
import Domain.PickingPlan;
import Domain.Trolley;
import Domain.ENUM.PackingHeuristic;
import Repository.TrolleyRepository;
import Repository.WarehouseRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * Packing.
     */
    public void packing() {
        List<AllocationRow> alocacoes = warehouseRepository.getAllocationRows();
        if (alocacoes.isEmpty()) {
            return;
        }

        List<Trolley> trolleys = new ArrayList<>();
        int capacity = trolley.getMaxCapacity();
        String heuristic = trolley.getOptionHeuristic();

        switch (heuristic) {
            case "FF":
                for (AllocationRow item : alocacoes) {
                    double itemWeight = item.getQty() * warehouseRepository.getItemBySku(item.getSku()).getWeight();
                    boolean placed = false;

                    // tenta colocar em trolleys existentes
                    for (Trolley t : trolleys) {
                        if (t.getCurrentLoad() + itemWeight <= capacity) {
                            t.getPickingItems().add(new PickingItems(item.getOrderId(), item.getLineNo(), item.getAisle(),
                                    item.getBay(), item.getBoxId(), item.getSku(), item.getQty()));
                            t.setCurrentLoad(t.getCurrentLoad() + itemWeight);
                            placed = true;
                            break;
                        }
                    }

                    // se não couber, cria novo trolley
                    if (!placed) {
                        Trolley newTrolley = new Trolley(capacity, 0, (int)(Math.random() * 10000) + 1,
                                new ArrayList<>(), heuristic);
                        newTrolley.getPickingItems().add(new PickingItems(item.getOrderId(), item.getLineNo(), item.getAisle(),
                                item.getBay(), item.getBoxId(), item.getSku(), item.getQty()));
                        newTrolley.setCurrentLoad(itemWeight);
                        trolleys.add(newTrolley);
                    }
                }
                break;

            case "FFD":
                // Ordena decrescente pelo peso
                alocacoes.sort((a1, a2) -> {
                    double w1 = a1.getQty() * warehouseRepository.getItemBySku(a1.getSku()).getWeight();
                    double w2 = a2.getQty() * warehouseRepository.getItemBySku(a2.getSku()).getWeight();
                    return Double.compare(w2, w1);
                });
                // reaproveita lógica FF
                for (AllocationRow item : alocacoes) {
                    double itemWeight = item.getQty() * warehouseRepository.getItemBySku(item.getSku()).getWeight();
                    boolean placed = false;
                    for (Trolley t : trolleys) {
                        if (t.getCurrentLoad() + itemWeight <= capacity) {
                            t.getPickingItems().add(new PickingItems(item.getOrderId(), item.getLineNo(), item.getAisle(),
                                    item.getBay(), item.getBoxId(), item.getSku(), item.getQty()));
                            t.setCurrentLoad(t.getCurrentLoad() + itemWeight);
                            placed = true;
                            break;
                        }
                    }
                    if (!placed) {
                        Trolley newTrolley = new Trolley(capacity, 0, (int)(Math.random() * 10000) + 1,
                                new ArrayList<>(), heuristic);
                        newTrolley.getPickingItems().add(new PickingItems(item.getOrderId(), item.getLineNo(), item.getAisle(),
                                item.getBay(), item.getBoxId(), item.getSku(), item.getQty()));
                        newTrolley.setCurrentLoad(itemWeight);
                        trolleys.add(newTrolley);
                    }
                }
                break;

            case "BFD":
                // Ordena decrescente pelo peso
                alocacoes.sort((a1, a2) -> {
                    double w1 = a1.getQty() * warehouseRepository.getItemBySku(a1.getSku()).getWeight();
                    double w2 = a2.getQty() * warehouseRepository.getItemBySku(a2.getSku()).getWeight();
                    return Double.compare(w2, w1);
                });

                for (AllocationRow item : alocacoes) {
                    double itemWeight = item.getQty() * warehouseRepository.getItemBySku(item.getSku()).getWeight();
                    Trolley bestTrolley = null;
                    double smallestRemaining = Double.MAX_VALUE;

                    for (Trolley t : trolleys) {
                        double remaining = capacity - t.getCurrentLoad();
                        if (itemWeight <= remaining && remaining < smallestRemaining) {
                            bestTrolley = t;
                            smallestRemaining = remaining;
                        }
                    }

                    if (bestTrolley != null) {
                        bestTrolley.getPickingItems().add(new PickingItems(item.getOrderId(), item.getLineNo(), item.getAisle(),
                                item.getBay(), item.getBoxId(), item.getSku(), item.getQty()));
                        bestTrolley.setCurrentLoad(bestTrolley.getCurrentLoad() + itemWeight);
                    } else {
                        Trolley newTrolley = new Trolley(capacity, 0, (int)(Math.random() * 10000) + 1,
                                new ArrayList<>(), heuristic);
                        newTrolley.getPickingItems().add(new PickingItems(item.getOrderId(), item.getLineNo(), item.getAisle(),
                                item.getBay(), item.getBoxId(), item.getSku(), item.getQty()));
                        newTrolley.setCurrentLoad(itemWeight);
                        trolleys.add(newTrolley);
                    }
                }
                break;

            default:
                System.out.println("Heurística desconhecida: " + heuristic);
                return;
        }

        // Salva todos os trolleys
        trolleyList.clear();
        trolleyList.addAll(trolleys);
        trolleyRepository.saveTrolley(trolleyList);
        System.out.println("Heurística " + heuristic + " executada. Total de trolleys: " + trolleyList.size());
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
}
