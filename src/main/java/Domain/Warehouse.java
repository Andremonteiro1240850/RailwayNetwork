package Domain;

import java.util.List;
import java.util.Map;

public class Warehouse {
    private final DataLoader dataLoader;

    public Warehouse() {
        this.dataLoader = new DataLoader();
    }

    // Delegações de carregamento
    public void loadItems(String filePath) { dataLoader.loadItems(filePath); }
    public void loadWagons(String filePath) { dataLoader.loadWagons(filePath); }
    public void loadOrders(String ordersFile, String linesFile) { dataLoader.loadOrders(ordersFile, linesFile); }
    public void loadBays(String filePath) { dataLoader.loadBays(filePath); }
    public void loadReturns(String filePath) { dataLoader.loadReturns(filePath); }

    // Acesso aos dados carregados
    public Map<String, Item> getItems() { return dataLoader.items; }
    public Map<String, List<Box>> getStock() { return dataLoader.stock; }
    public Map<String, Order> getOrders() { return dataLoader.orders; }
    public List<Bay> getBays() { return dataLoader.bays; }
    public List<Return> getReturns() { return dataLoader.returns; }
}
