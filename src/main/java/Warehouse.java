import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Warehouse {
    Map<String, Item> items = new HashMap<>();
    Map<String, List<Box>> stock = new HashMap<>();
    Map<String, Order> orders = new HashMap<>();

    // ---------- ITEMS ----------
    public void loadItems(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 6) continue;

                String sku = p[0].trim();
                String name = p[1].trim();
                String category = p[2].trim();
                double volume = Double.parseDouble(p[4]);
                double weight = Double.parseDouble(p[5]);

                items.put(sku, new Item(sku, name, category, volume, weight));
            }
        } catch (Exception e) {
            System.out.println("Erro a ler items: " + e.getMessage());
        }
    }

    // ---------- WAGONS ----------
    public void loadWagons(String filePath) {
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dateTimeFmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // header
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 6) continue;

                String boxId = p[1].trim();
                String sku = p[2].trim();
                int qty = Integer.parseInt(p[3].trim());
                LocalDate expiry = p[4].isEmpty() ? null : LocalDate.parse(p[4], dateFmt);
                LocalDateTime receivedAt = LocalDateTime.parse(p[5], dateTimeFmt);

                if (!items.containsKey(sku)) {
                    System.out.println("⚠️ SKU " + sku + " não existe no catálogo (ignorado)");
                    continue;
                }

                Box box = new Box(boxId, sku, qty, expiry, receivedAt);
                stock.computeIfAbsent(sku, k -> new ArrayList<>()).add(box);
            }
        } catch (Exception e) {
            System.out.println("Erro a ler wagons: " + e.getMessage());
        }
    }

    // ---------- ORDERS ----------
    public void loadOrders(String ordersFile, String linesFile) {
        DateTimeFormatter dateTimeFmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        // Ler orders
        try (BufferedReader br = new BufferedReader(new FileReader(ordersFile))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 3) continue;

                String id = p[0].trim();
                LocalDateTime dueDate = LocalDateTime.parse(p[1], dateTimeFmt);
                int priority = Integer.parseInt(p[2]);

                orders.put(id, new Order(id, dueDate, priority));
            }
        } catch (Exception e) {
            System.out.println("Erro a ler orders: " + e.getMessage());
        }

        // Ler order_lines
        try (BufferedReader br = new BufferedReader(new FileReader(linesFile))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 4) continue;

                String orderId = p[0].trim();
                String sku = p[2].trim();
                int qty = Integer.parseInt(p[3]);

                if (!items.containsKey(sku)) {
                    System.out.println("⚠️ SKU " + sku + " não existe no catálogo (ignorado)");
                    continue;
                }

                Order order = orders.get(orderId);
                if (order != null) {
                    order.lines.add(new OrderLine(sku, qty));
                }
            }
        } catch (Exception e) {
            System.out.println("Erro a ler order_lines: " + e.getMessage());
        }
    }
    public Map<String, Order> getOrders() {
        return orders;
    }
    public Map<String, List<Box>> getStock() {
        return stock;
    }
    public Map<String, Item> getItems() {
        return items;
    }
}
