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
        List<PickingItems> itensASeremPegos = new ArrayList<>();
        Map<Integer, Double> percTrol = new HashMap<>();

        trolley.setCurrentLoad(0.0);

        // --- FIRST FIT (FF) ---
        if (trolley.getOptionHeuristic().equals("FF")) {

            for (AllocationRow a : alocacoes) {
                double pesoItem = a.getQty() * warehouseRepository.getItemBySku(a.getSku()).getWeight();

                if (trolley.getCurrentLoad() + pesoItem <= trolley.getMaxCapacity()) {
                    PickingItems item = new PickingItems(a.getOrderId(), a.getLineNo(), a.getAisle(),
                            a.getBay(), a.getBoxId(), a.getSku(), a.getQty());
                    itensASeremPegos.add(item);
                    trolley.setCurrentLoad(trolley.getCurrentLoad() + pesoItem);
                } else {
                    Trolley trolleyAtual = new Trolley(trolley.getMaxCapacity(), trolley.getCurrentLoad(),
                            (int)(Math.random()*10000)+1, new ArrayList<>(itensASeremPegos),
                            trolley.getOptionHeuristic());

                    trolleyList.add(trolleyAtual);
                    double percent = (trolleyAtual.getCurrentLoad() / trolleyAtual.getMaxCapacity()) * 100;
                    percTrol.put(trolleyAtual.getId(), percent);

                    itensASeremPegos.clear();
                    trolley.setCurrentLoad(0.0);

                    PickingItems novoItem = new PickingItems(a.getOrderId(), a.getLineNo(), a.getAisle(),
                            a.getBay(), a.getBoxId(), a.getSku(), a.getQty());
                    itensASeremPegos.add(novoItem);
                    trolley.setCurrentLoad(pesoItem);
                }
            }

            if (!itensASeremPegos.isEmpty()) {
                Trolley ultimo = new Trolley(trolley.getMaxCapacity(), trolley.getCurrentLoad(),
                        (int)(Math.random()*10000)+1, new ArrayList<>(itensASeremPegos),
                        trolley.getOptionHeuristic());
                trolleyList.add(ultimo);
                double percent = (ultimo.getCurrentLoad() / ultimo.getMaxCapacity()) * 100;
                percTrol.put(ultimo.getId(), percent);
            }
            trolleyRepository.saveTrolley(trolleyList);
            System.out.println("Heurística FF executada. Total de trolleys: " + trolleyList.size());
        }

        // --- FIRST FIT DECREASING (FFD) ---
        else if (trolley.getOptionHeuristic().equals("FFD")) {
            alocacoes.sort((a1, a2) -> {
                Item item1 = warehouseRepository.getItemBySku(a1.getSku());
                Item item2 = warehouseRepository.getItemBySku(a2.getSku());
                double weight1 = a1.getQty() * item1.getWeight();
                double weight2 = a2.getQty() * item2.getWeight();
                return Double.compare(weight2, weight1);
            });

            for (AllocationRow a : alocacoes) {
                double pesoItem = a.getQty() * warehouseRepository.getItemBySku(a.getSku()).getWeight();

                if (trolley.getCurrentLoad() + pesoItem <= trolley.getMaxCapacity()) {
                    PickingItems item = new PickingItems(a.getOrderId(), a.getLineNo(), a.getAisle(),
                            a.getBay(), a.getBoxId(), a.getSku(), a.getQty());
                    itensASeremPegos.add(item);
                    trolley.setCurrentLoad(trolley.getCurrentLoad() + pesoItem);
                } else {
                    Trolley trolleyAtual = new Trolley(trolley.getMaxCapacity(), trolley.getCurrentLoad(),
                            (int)(Math.random()*10000)+1, new ArrayList<>(itensASeremPegos),
                            trolley.getOptionHeuristic());
                    trolleyList.add(trolleyAtual);
                    double percent = (trolleyAtual.getCurrentLoad() / trolleyAtual.getMaxCapacity()) * 100;
                    percTrol.put(trolleyAtual.getId(), percent);

                    itensASeremPegos.clear();
                    trolley.setCurrentLoad(0.0);

                    PickingItems novoItem = new PickingItems(a.getOrderId(), a.getLineNo(), a.getAisle(),
                            a.getBay(), a.getBoxId(), a.getSku(), a.getQty());
                    itensASeremPegos.add(novoItem);
                    trolley.setCurrentLoad(pesoItem);
                }
            }

            if (!itensASeremPegos.isEmpty()) {
                Trolley ultimo = new Trolley(trolley.getMaxCapacity(), trolley.getCurrentLoad(),
                        (int)(Math.random()*10000)+1, new ArrayList<>(itensASeremPegos),
                        trolley.getOptionHeuristic());
                trolleyList.add(ultimo);
                double percent = (ultimo.getCurrentLoad() / ultimo.getMaxCapacity()) * 100;
                percTrol.put(ultimo.getId(), percent);
            }
            trolleyRepository.saveTrolley(trolleyList);

            System.out.println("Heurística FFD executada. Total de trolleys: " + trolleyList.size());
        }

        // --- BEST FIT DECREASING (BFD) ---
        else if (trolley.getOptionHeuristic().equals("BFD")) {
            alocacoes.sort((a1, a2) -> {
                Item item1 = warehouseRepository.getItemBySku(a1.getSku());
                Item item2 = warehouseRepository.getItemBySku(a2.getSku());
                double weight1 = a1.getQty() * item1.getWeight();
                double weight2 = a2.getQty() * item2.getWeight();
                return Double.compare(weight2, weight1);
            });

            for (AllocationRow a : alocacoes) {
                double pesoItem = a.getQty() * warehouseRepository.getItemBySku(a.getSku()).getWeight();
                PickingItems item = new PickingItems(a.getOrderId(), a.getLineNo(), a.getAisle(),
                        a.getBay(), a.getBoxId(), a.getSku(), a.getQty());

                Trolley melhorTrolley = null;
                double menorEspacoSobrante = Double.MAX_VALUE;

                for (Trolley t : trolleyList) {
                    double espacoRestante = t.getMaxCapacity() - t.getCurrentLoad();
                    if (pesoItem <= espacoRestante && (espacoRestante - pesoItem) < menorEspacoSobrante) {
                        melhorTrolley = t;
                        menorEspacoSobrante = espacoRestante - pesoItem;
                    }
                }

                if (melhorTrolley != null) {
                    melhorTrolley.getPickingItems().add(item);
                    melhorTrolley.setCurrentLoad(melhorTrolley.getCurrentLoad() + pesoItem);
                } else {
                    List<PickingItems> novosItens = new ArrayList<>();
                    novosItens.add(item);
                    Trolley novoTrolley = new Trolley(
                            trolley.getMaxCapacity(), pesoItem,
                            (int)(Math.random() * 10000) + 1,
                            novosItens, trolley.getOptionHeuristic());
                    trolleyList.add(novoTrolley);
                }
            }

            for (Trolley t : trolleyList) {
                double percent = (t.getCurrentLoad() / t.getMaxCapacity()) * 100;
                percTrol.put(t.getId(), percent);
            }
            trolleyRepository.saveTrolley(trolleyList);

            System.out.println("Heurística BFD executada. Total de trolleys: " + trolleyList.size());
        }
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
     * @param capacity the trolley capacity
     * @param heuristic the packing heuristic
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
