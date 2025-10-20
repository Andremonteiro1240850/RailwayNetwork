package Domain;

import java.util.*;
import java.util.stream.Collectors;

public class WarehouseManager {
    private final Map<String, Item> items;
    private final Map<String, List<Box>> stock;
    private final List<Bay> bays;
    private final Map<BayId, Bay> bayMap;

    public WarehouseManager(Map<String, Item> items, Map<String, List<Box>> stock, List<Bay> bays) {
        this.items = items;
        this.stock = stock;
        this.bays = bays;
        this.bayMap = bays.stream().collect(Collectors.toMap(Bay::getId, bay -> bay));
    }

    /**
     * Unload wagons and assign boxes to bays using FEFO/FIFO logic
     */
    public void unloadWagons() {
        System.out.println("=== UNLOADING WAGONS ===");
        
        for (Map.Entry<String, List<Box>> entry : stock.entrySet()) {
            String sku = entry.getKey();
            List<Box> boxes = entry.getValue();
            
            // Sort boxes by FEFO/FIFO before assigning to bays
            boxes.sort(Box.FEFO_COMPARATOR);
            
            System.out.println("Processing " + boxes.size() + " boxes for SKU: " + sku);
            
            for (Box box : boxes) {
                if (box.getLocation() == null) {
                    assignBoxToBay(box);
                }
            }
        }
        
        System.out.println("Wagon unloading completed!");
    }

    /**
     * Assign a box to the first non-full bay in ascending order (aisle/bay)
     * This enforces the example distribution policy for USEI01: start at aisle 1, bay 1.
     */
    private void assignBoxToBay(Box box) {
        String sku = box.getSku();

        // Choose the first non-full bay by ascending BayId
        List<Bay> candidateBays = bays.stream()
                .filter(bay -> !bay.isFull())
                .sorted(Comparator.comparing(Bay::getId))
                .collect(Collectors.toList());

        if (candidateBays.isEmpty()) {
            System.out.println("⚠️ No available bays for SKU: " + sku + " (Box: " + box.getBoxId() + ")");
            return;
        }

        Bay targetBay = candidateBays.get(0);
        targetBay.insertBox(box);
        System.out.println("✓ Assigned box " + box.getBoxId() + " to " + targetBay.getId());
    }

    /**
     * Insert a returned box into inventory and place it according to FEFO/FIFO (USEI01 rules).
     */
    public void insertReturnedBox(String boxId, String sku, int qty,
                                  java.time.LocalDate expiryDate,
                                  java.time.LocalDateTime receivedAt) {
        Box box = new Box(boxId, sku, qty, expiryDate, receivedAt);
        stock.computeIfAbsent(sku, k -> new ArrayList<>()).add(box);
        // Place the new box in a bay following the same policy used for wagon unloading
        assignBoxToBay(box);
    }

    /**
     * Dispatch stock for a specific SKU and quantity
     */
    public List<Box> dispatchStock(String sku, int requestedQty) {
        List<Box> dispatchedBoxes = new ArrayList<>();
        int remainingQty = requestedQty;
        
        // Get all bays that have this SKU, sorted by bay ID
        List<Bay> baysWithSku = bays.stream()
                .filter(bay -> bay.hasSku(sku))
                .sorted(Comparator.comparing(Bay::getId))
                .collect(Collectors.toList());
        
        for (Bay bay : baysWithSku) {
            if (remainingQty <= 0) break;
            
            while (remainingQty > 0) {
                Box frontBox = bay.peekFront(sku);
                if (frontBox == null) break;
                
                if (frontBox.getQty() <= remainingQty) {
                    // Take the entire box
                    Box dispatchedBox = bay.pollFront(sku);
                    dispatchedBoxes.add(dispatchedBox);
                    remainingQty -= dispatchedBox.getQty();
                    System.out.println("✓ Dispatched entire box " + dispatchedBox.getBoxId() + 
                                     " (qty: " + dispatchedBox.getQty() + ") from " + bay.getId());
                } else {
                    // Take partial quantity
                    Box partialBox = new Box(frontBox.getBoxId() + "_PARTIAL", 
                                           frontBox.getSku(), 
                                           remainingQty, 
                                           frontBox.getExpiryDate(), 
                                           frontBox.getReceivedAt(), 
                                           frontBox.getLocation());
                    dispatchedBoxes.add(partialBox);
                    
                    // Reduce the original box quantity
                    frontBox.reduceQty(remainingQty);
                    remainingQty = 0;
                    System.out.println("✓ Dispatched partial box " + partialBox.getBoxId() + 
                                     " (qty: " + partialBox.getQty() + ") from " + bay.getId());
                }
            }
        }
        
        if (remainingQty > 0) {
            System.out.println("⚠️ Insufficient stock for SKU: " + sku + 
                             " (requested: " + requestedQty + ", dispatched: " + (requestedQty - remainingQty) + ")");
        }
        
        return dispatchedBoxes;
    }

    /**
     * Relocate a box to a different bay
     */
    public boolean relocateBox(String boxId, BayId newBayId) {
        // Find the box
        Box boxToMove = null;
        Bay currentBay = null;
        
        for (Bay bay : bays) {
            for (Box box : bay.getAllBoxes()) {
                if (box.getBoxId().equals(boxId)) {
                    boxToMove = box;
                    currentBay = bay;
                    break;
                }
            }
            if (boxToMove != null) break;
        }
        
        if (boxToMove == null) {
            System.out.println("⚠️ Box " + boxId + " not found");
            return false;
        }
        
        Bay targetBay = bayMap.get(newBayId);
        if (targetBay == null) {
            System.out.println("⚠️ Target bay " + newBayId + " not found");
            return false;
        }
        
        if (targetBay.isFull()) {
            System.out.println("⚠️ Target bay " + newBayId + " is full");
            return false;
        }
        
        // Remove from current bay and add to new bay
        currentBay.removeBox(boxToMove);
        targetBay.insertBox(boxToMove);
        
        System.out.println("✓ Relocated box " + boxId + " from " + currentBay.getId() + " to " + newBayId);
        return true;
    }


    public Map<String, Item> getItems() { return items; }
    public Map<String, List<Box>> getStock() { return stock; }
    public List<Bay> getBays() { return bays; }
    public Map<BayId, Bay> getBayMap() { return bayMap; }
}
