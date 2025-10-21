package Domain;

import Controller.WarehouseController;
import UI.TrolleyUI;

import java.util.List;

/**public class WarehouseLoader {
    public static void main(String[] args) {
        Warehouse warehouse = new Warehouse();

        warehouse.loadItems("train_station_dataset/items.csv");
        warehouse.loadWagons("train_station_dataset\\wagons.csv");
        warehouse.loadOrders("train_station_dataset\\orders.csv", "train_station_dataset\\order_lines.csv");

        // Prints para todos os mapas gerados
        System.out.println("\n=== MAPA: ITEMS ===");
        System.out.println("Total items: " + warehouse.getItems().size());
        for (java.util.Map.Entry<String, Item> e : warehouse.getItems().entrySet()) {
            Item it = e.getValue();
            System.out.println("SKU=" + e.getKey() + ", name=" + it.getName() + ", category=" + it.getCategory() + ", volume=" + it.getVolume() + ", weight=" + it.getWeight());
        }

        System.out.println("\n=== MAPA: STOCK (SKU -> caixas/quantidade) ===");
        int totalCaixas = 0;
        int totalQty = 0;
        for (java.util.Map.Entry<String, java.util.List<Box>> e : warehouse.getStock().entrySet()) {
            int caixas = e.getValue() == null ? 0 : e.getValue().size();
            int qty = 0;
            if (e.getValue() != null) {
                for (Box b : e.getValue()) qty += b.getQty();
            }
            totalCaixas += caixas;
            totalQty += qty;
            System.out.println("SKU=" + e.getKey() + ": caixas=" + caixas + ", qtyTotal=" + qty);
        }
        System.out.println("Totais: caixas=" + totalCaixas + ", qtyTotal=" + totalQty);

        System.out.println("\n=== MAPA: ORDERS ===");
        System.out.println("Total orders: " + warehouse.getOrders().size());
        for (java.util.Map.Entry<String, Order> e : warehouse.getOrders().entrySet()) {
            Order o = e.getValue();
            System.out.println("orderId=" + e.getKey() + ", priority=" + o.getPriority() + ", dueDate=" + o.getDueDate() + ", numLines=" + o.getLines().size());
        }
        /**WarehouseController warehouseController = new WarehouseController(warehouse);
        List<EligibilityResult> estados = warehouseController.estadoEncomenda();
        for (EligibilityResult er : estados) {
            System.out.println(
                    "order=" + er.getOrderId() +
                    ", lineNo=" + er.getLineNo() +
                    ", sku=" + er.getSku() +
                    ", requested=" + er.getRequestedQty() +
                    ", allocated=" + er.getAllocatedQty() +
                    ", status=" + er.getStatus()
            );
        }
        List<AllocationRow> resultados = warehouseController.createAllocationRows();
        for (AllocationRow r : resultados) {
            System.out.println(
                    "ALLOC order=" + r.getOrderId() +
                    ", lineNo=" + r.getLineNo() +
                    ", sku=" + r.getSku() +
                    ", qty=" + r.getQty() +
                    ", boxId=" + r.getBoxId() +
                    ", aisle=" + r.getAisle() +
                    ", bay=" + r.getBay()
            );
        }

    }
}
}
}*/