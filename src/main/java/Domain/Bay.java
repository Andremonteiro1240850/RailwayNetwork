package Domain;

import java.util.*;

public class Bay {
    private final BayId id;
    private final int capacityBoxes;
    private final Map<String, TreeSet<Box>> bySku;

    public Bay(BayId id, int capacityBoxes) {
        this.id = id;
        this.capacityBoxes = capacityBoxes;
        this.bySku = new HashMap<>();
    }

    public BayId getId() { return id; }
    public int getCapacityBoxes() { return capacityBoxes; }

    public void insertBox(Box box) {
        // Set the box location to this bay
        box.setLocation(this.id);
        bySku.computeIfAbsent(box.getSku(), k -> new TreeSet<>(Box.FEFO_COMPARATOR))
                .add(box);
    }

    public Box peekFront(String sku) {
        TreeSet<Box> boxes = bySku.get(sku);
        return (boxes == null || boxes.isEmpty()) ? null : boxes.first();
    }

    public Box pollFront(String sku) {
        TreeSet<Box> boxes = bySku.get(sku);
        return (boxes == null || boxes.isEmpty()) ? null : boxes.pollFirst();
    }

    public void removeBox(Box box) {
        TreeSet<Box> boxes = bySku.get(box.getSku());
        if (boxes != null) boxes.remove(box);
    }

    public boolean isFull() {
        return totalBoxes() >= capacityBoxes;
    }

    public int totalBoxes() {
        return bySku.values().stream().mapToInt(Set::size).sum();
    }

    public Collection<Box> getAllBoxes() {
        List<Box> result = new ArrayList<>();
        bySku.values().forEach(result::addAll);
        return result;
    }
    
    public int getBoxCountForSku(String sku) {
        TreeSet<Box> boxes = bySku.get(sku);
        return boxes == null ? 0 : boxes.size();
    }
    
    public int getTotalQuantityForSku(String sku) {
        TreeSet<Box> boxes = bySku.get(sku);
        if (boxes == null) return 0;
        return boxes.stream().mapToInt(Box::getQty).sum();
    }
    
    public boolean hasSku(String sku) {
        TreeSet<Box> boxes = bySku.get(sku);
        return boxes != null && !boxes.isEmpty();
    }
    
    public List<String> getAvailableSkus() {
        return new ArrayList<>(bySku.keySet());
    }

    @Override
    public String toString() {
        return "Bay{" + id + ", boxes=" + totalBoxes() + "}";
    }
}
