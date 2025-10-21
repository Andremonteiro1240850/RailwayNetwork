package Domain;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataLoader {
    public Map<String, Item> items = new HashMap<>();
    public Map<String, List<Box>> stock = new HashMap<>(); // Keep for backward compatibility
    public Map<String, Order> orders = new HashMap<>();
    public List<Bay> bays = new ArrayList<>();
    public List<Return> returns = new ArrayList<>();
    public BoxRepository boxRepository = new BoxRepository(); // New repository for boxes
    // Contadores não necessários quando ficheiro já fornece lineNo

    // Indexes auxiliares para construir Bays
    private final Map<String, Warehouse> warehouseById = new HashMap<>();
    private final Map<String, Map<Integer, Aisle>> aislesByWarehouse = new HashMap<>();

    // Helpers de parsing seguros
    private static final DateTimeFormatter[] SUPPORTED_DATE_FORMATS = new DateTimeFormatter[] {
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };

    private static final DateTimeFormatter[] SUPPORTED_DATETIME_FORMATS = new DateTimeFormatter[] {
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    };

    private static String safeGet(String[] parts, int idx) {
        if (idx < 0 || idx >= parts.length) return null;
        String v = parts[idx];
        return v == null ? null : v.trim();
    }

    private static Integer parseInteger(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        try { return Integer.parseInt(raw.trim()); } catch (Exception ignored) { return null; }
    }

    private static Double parseDoubleValue(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        try { return Double.parseDouble(raw.trim()); } catch (Exception ignored) { return null; }
    }

    private static LocalDate parseLocalDateFlexible(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        String value = raw.trim();
        for (DateTimeFormatter f : SUPPORTED_DATE_FORMATS) {
            try { return LocalDate.parse(value, f); } catch (Exception ignored) { }
        }
        return null;
    }

    private static LocalDateTime parseLocalDateTimeFlexible(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        String value = raw.trim();
        for (DateTimeFormatter f : SUPPORTED_DATETIME_FORMATS) {
            try { return LocalDateTime.parse(value, f); } catch (Exception ignored) { }
        }
        return null;
    }

    // ---------- ITEMS ----------
    public void loadItems(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 6) continue;

                String sku = safeGet(p, 0);
                String name = safeGet(p, 1);
                String category = safeGet(p, 2);
                String unit=safeGet(p, 3);
                Double volume = parseDoubleValue(safeGet(p, 4));
                Double weight = parseDoubleValue(safeGet(p, 5));

                if (sku == null || sku.isEmpty()) {
                    System.out.println("⚠️ Linha items com SKU inválido (ignorado)");
                    continue;
                }
                if (volume == null || weight == null) {
                    System.out.println("⚠️ Item " + sku + " com volume/peso inválidos (ignorado)");
                    continue;
                }

                items.put(sku, new Item(sku, name, category,unit, volume, weight));
            }
        } catch (Exception e) {
            System.out.println("Erro a ler items: " + e.getMessage());
        }
    }

    // ---------- WAGONS ----------
    public void loadWagons(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // header
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 6) continue;

                String wagonId = safeGet(p, 0);
                String boxId = safeGet(p, 1);
                String sku = safeGet(p, 2);
                Integer qty = parseInteger(safeGet(p, 3));
                LocalDate expiry = parseLocalDateFlexible(safeGet(p, 4));
                LocalDateTime receivedAt = parseLocalDateTimeFlexible(safeGet(p, 5));

                if (!items.containsKey(sku)) {
                    System.out.println("⚠️ SKU " + sku + " não existe no catálogo (ignorado)");
                    continue;
                }
                if (boxId == null || boxId.isEmpty()) {
                    System.out.println("⚠️ BoxId inválido (ignorado)");
                    continue;
                }
                if (qty == null || qty <= 0) {
                    System.out.println("⚠️ Quantidade inválida para box " + boxId + " (ignorado)");
                    continue;
                }
                if (receivedAt == null) {
                    System.out.println("⚠️ Data/hora de receção inválida para box " + boxId + " (ignorado)");
                    continue;
                }

                // Create box without location initially
                Box box = new Box(boxId, sku, qty, expiry, receivedAt);
                
                // Add to both old structure (for backward compatibility) and new repository
                stock.computeIfAbsent(sku, k -> new ArrayList<>()).add(box);
                boxRepository.addUnassignedBox(box);
            }
        } catch (Exception e) {
            System.out.println("Erro a ler wagons: " + e.getMessage());
        }
    }

    // ---------- ORDERS ----------
    public void loadOrders(String ordersFile, String linesFile) {
        // Ler orders
        try (BufferedReader br = new BufferedReader(new FileReader(ordersFile))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 3) continue;

                String id = safeGet(p, 0);
                LocalDateTime dueDate = parseLocalDateTimeFlexible(safeGet(p, 1));
                Integer priority = parseInteger(safeGet(p, 2));

                if (id == null || id.isEmpty()) {
                    System.out.println("⚠️ Order com ID inválido (ignorado)");
                    continue;
                }
                if (dueDate == null) {
                    System.out.println("⚠️ Order " + id + " com dueDate inválido (ignorado)");
                    continue;
                }
                if (priority == null) {
                    System.out.println("⚠️ Order " + id + " com prioridade inválida (ignorado)");
                    continue;
                }

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

                String orderId = safeGet(p, 0);
                Integer lineNo = parseInteger(safeGet(p, 1));
                String sku = safeGet(p, 2);
                Integer qty = parseInteger(safeGet(p, 3));

                if (!items.containsKey(sku)) {
                    System.out.println("⚠️ SKU " + sku + " não existe no catálogo (ignorado)");
                    continue;
                }
                if (orderId == null || orderId.isEmpty()) {
                    System.out.println("⚠️ Linha de encomenda com orderId inválido (ignorado)");
                    continue;
                }
                if (lineNo == null || lineNo <= 0) {
                    System.out.println("⚠️ Linha de encomenda " + orderId + " com lineNo inválido (ignorado)");
                    continue;
                }
                if (qty == null || qty <= 0) {
                    System.out.println("⚠️ Linha de encomenda " + orderId + " com quantidade inválida (ignorado)");
                    continue;
                }

                Order order = orders.get(orderId);
                if (order != null) {
                    order.addLine(new OrderLine(lineNo, sku, qty));
                }
            }
        } catch (Exception e) {
            System.out.println("Erro a ler order_lines: " + e.getMessage());
        }
    }

    // ---------- BAYS ----------
    public void loadBays(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // header
            String line;
            while ((line = br.readLine()) != null) {
                // ficheiro separado por ';'
                String[] p = line.split(";");
                if (p.length < 4) continue;

                String warehouseId = safeGet(p, 0);
                Integer aisleNo = parseInteger(safeGet(p, 1));
                Integer bayId = parseInteger(safeGet(p, 2));
                Integer capacity = parseInteger(safeGet(p, 3));

                if (warehouseId == null || warehouseId.isEmpty() || aisleNo == null || bayId == null || capacity == null) {
                    System.out.println("⚠️ Bay com campos inválidos (ignorado)");
                    continue;
                }
                if (capacity <= 0) {
                    System.out.println("⚠️ Bay " + aisleNo + "-" + bayId + " tem capacidade inválida: " + capacity);
                    continue;
                }

                BayId bayIdObj = new BayId(warehouseId, aisleNo, bayId);
                Bay b = new Bay(bayIdObj, capacity);
                bays.add(b);
            }
        } catch (Exception e) {
            System.out.println("Erro a ler bays: " + e.getMessage());
        }
    }

    // ---------- RETURNS ----------
    public void loadReturns(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // header
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 6) continue;

                String returnId = safeGet(p, 0);
                String sku = safeGet(p, 1);
                Integer qty = parseInteger(safeGet(p, 2));
                String reason = safeGet(p, 3);
                LocalDateTime timestamp = parseLocalDateTimeFlexible(safeGet(p, 4));
                LocalDate expiry = parseLocalDateFlexible(safeGet(p, 5));

                // validações
                if (!items.containsKey(sku)) {
                    System.out.println("⚠️ SKU " + sku + " não existe no catálogo (ignorado)");
                    continue;
                }
                if (returnId == null || returnId.isEmpty()) {
                    System.out.println("⚠️ Devolução com ID inválido (ignorado)");
                    continue;
                }
                if (qty == null || qty <= 0) {
                    System.out.println("⚠️ Devolução " + returnId + " tem quantidade inválida: " + qty);
                    continue;
                }
                if (timestamp == null) {
                    System.out.println("⚠️ Devolução " + returnId + " com timestamp inválido (ignorado)");
                    continue;
                }

                Return r = new Return(returnId, sku, qty, reason, timestamp, expiry);
                returns.add(r);
            }
        } catch (Exception e) {
            System.out.println("Erro a ler returns: " + e.getMessage());
        }
    }
    public List<Bay> getBays() {
        return bays;
    }
    
    public BoxRepository getBoxRepository() {
        return boxRepository;
    }
}


