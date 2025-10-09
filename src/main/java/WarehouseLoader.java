import java.util.List;

public class WarehouseLoader {
    public static void main(String[] args) {
        Warehouse warehouse = new Warehouse();

        warehouse.loadItems("C:\\Users\\Joao Silva\\IdeaProjects\\sem3-pi-25-26-g044-repo\\train_station_dataset\\items.csv");
        warehouse.loadWagons("C:\\Users\\Joao Silva\\IdeaProjects\\sem3-pi-25-26-g044-repo\\train_station_dataset\\wagons.csv");
        warehouse.loadOrders("C:\\Users\\Joao Silva\\IdeaProjects\\sem3-pi-25-26-g044-repo\\train_station_dataset\\orders.csv", "C:\\Users\\Joao Silva\\IdeaProjects\\sem3-pi-25-26-g044-repo\\train_station_dataset\\order_lines.csv");

        System.out.println("\n✅ Catalogo: " + warehouse.items.size() + " itens");
        System.out.println("✅ Stock: " + warehouse.stock.values().stream().mapToInt(List::size).sum() + " caixas");
        System.out.println("✅ Encomendas: " + warehouse.orders.size() + " orders");
    }
}
