package Controller;

import Domain.AllocationRow;
import Domain.Item;
import Domain.PickingItems;
import Domain.Trolley;
import Repository.WarehouseRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrolleyController {

    private Trolley trolley;
    private WarehouseRepository warehouseRepository;
    private Item item;
    private List<Trolley> trolleyList;


    public TrolleyController(Trolley trolley, WarehouseRepository warehouseRepository) {
        this.trolley = trolley;
        this.warehouseRepository = warehouseRepository;
        this.trolleyList = new ArrayList<>();
    }


    public Trolley createTrolley(int maxCapacity, String optionHeuristic) {
        Trolley trolley = new Trolley(maxCapacity, 0.0,
                (int)(Math.random()*10000)+1, new ArrayList<>(), optionHeuristic);
        this.trolley = trolley;
        trolleyList.add(trolley);
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

    public void packing() {
        List<AllocationRow> alocacoes = warehouseRepository.getAllocationRows();
        List<PickingItems> itensASeremPegos = new ArrayList<>();
        Map<Integer, Double> percTrol = new HashMap<>();

        trolleyList.clear(); // limpa lista de trolleys anteriores
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

            System.out.println("Heurística BFD executada. Total de trolleys: " + trolleyList.size());
        }
    }

    public Trolley getTrolley() {
        return trolley;
    }

    public void setTrolley(Trolley trolley) {
        this.trolley = trolley;
    }

    public List<Trolley> getTrolleyList() {
        return trolleyList;
    }

    @Override
    public String toString() {
        return "TrolleyController => " +
                "trolley: " + trolley +
                '.';
    }
}
