package Domain;

import java.util.List;

public class WarehouseLoader {
    public static void main(String[] args) {
        Warehouse warehouse = new Warehouse();

        warehouse.loadItems("train_station_dataset/items.csv");
        warehouse.loadWagons("train_station_dataset\\wagons.csv");
        warehouse.loadOrders("train_station_dataset\\orders.csv", "train_station_dataset\\order_lines.csv");

        System.out.println("\n✅ Catalogo: " + warehouse.items.size() + " itens");
        System.out.println("✅ Stock: " + warehouse.stock.values().stream().mapToInt(List::size).sum() + " caixas");
        System.out.println("✅ Encomendas: " + warehouse.orders.size() + " orders");

        WarehouseController warehouseController = new WarehouseController(warehouse);


    }
}
