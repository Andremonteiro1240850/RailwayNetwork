package Sprint1.Domain;

import java.util.*;

/**
 * The type Bay.
 */
public class Bay {
    private final BayId id;
    private final int capacityBoxes;
    private final Map<String, TreeSet<Box>> bySku;
    private String dedicatedSku; // SKU dedicado a este bay (null se vazio)

    /**
     * Instantiates a new Bay.
     *
     * @param id            the id
     * @param capacityBoxes the capacity boxes
     */
    public Bay(BayId id, int capacityBoxes) {
        this.id = id;
        this.capacityBoxes = capacityBoxes;
        this.bySku = new HashMap<>();
        this.dedicatedSku = null; // Inicialmente vazio
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public BayId getId() { return id; }

    /**
     * Gets capacity boxes.
     *
     * @return the capacity boxes
     */
    public int getCapacityBoxes() { return capacityBoxes; }

    /**
     * Insert box.
     *
     * @param box the box
     */
    public void insertBox(Box box) {
        // Verificar se o bay está vazio ou se o SKU é compatível
        if (dedicatedSku != null && !dedicatedSku.equals(box.getSku())) {
            throw new IllegalArgumentException("Bay " + id + " is dedicated to SKU " + dedicatedSku + 
                                               ", cannot store SKU " + box.getSku());
        }
        
        // Se o bay está vazio, definir o SKU dedicado
        if (dedicatedSku == null) {
            dedicatedSku = box.getSku();
        }
        
        // Add box to the bay's internal structure
        bySku.computeIfAbsent(box.getSku(), k -> new TreeSet<>(Box.FEFO_COMPARATOR))
                .add(box);
        
        // Note: box location is set by the repository before calling this method
    }

    /**
     * Peek front box.
     *
     * @param sku the sku
     * @return the box
     */
    public Box peekFront(String sku) {
        TreeSet<Box> boxes = bySku.get(sku);
        return (boxes == null || boxes.isEmpty()) ? null : boxes.first();
    }

    /**
     * Poll front box.
     *
     * @param sku the sku
     * @return the box
     */
    public Box pollFront(String sku) {
        TreeSet<Box> boxes = bySku.get(sku);
        return (boxes == null || boxes.isEmpty()) ? null : boxes.pollFirst();
    }

    /**
     * Remove box.
     *
     * @param box the box
     */
    public void removeBox(Box box) {
        TreeSet<Box> boxes = bySku.get(box.getSku());
        if (boxes != null) {
            boxes.remove(box);
            // Se não há mais boxes deste SKU, remover a entrada
            if (boxes.isEmpty()) {
                bySku.remove(box.getSku());
            }
        }
        
        // Se o bay ficou completamente vazio, limpar o SKU dedicado
        if (totalBoxes() == 0) {
            dedicatedSku = null;
        }
    }

    /**
     * Is full boolean.
     *
     * @return the boolean
     */
    public boolean isFull() {
        return totalBoxes() >= capacityBoxes;
    }

    /**
     * Total boxes int.
     *
     * @return the int
     */
    public int totalBoxes() {
        return bySku.values().stream().mapToInt(Set::size).sum();
    }

    /**
     * Gets all boxes.
     *
     * @return the all boxes
     */
    public Collection<Box> getAllBoxes() {
        List<Box> result = new ArrayList<>();
        bySku.values().forEach(result::addAll);
        return result;
    }

    /**
     * Gets box count for sku.
     *
     * @param sku the sku
     * @return the box count for sku
     */
    public int getBoxCountForSku(String sku) {
        TreeSet<Box> boxes = bySku.get(sku);
        return boxes == null ? 0 : boxes.size();
    }

    /**
     * Gets total quantity for sku.
     *
     * @param sku the sku
     * @return the total quantity for sku
     */
    public int getTotalQuantityForSku(String sku) {
        TreeSet<Box> boxes = bySku.get(sku);
        if (boxes == null) return 0;
        return boxes.stream().mapToInt(Box::getQty).sum();
    }

    /**
     * Has sku boolean.
     *
     * @param sku the sku
     * @return the boolean
     */
    public boolean hasSku(String sku) {
        TreeSet<Box> boxes = bySku.get(sku);
        return boxes != null && !boxes.isEmpty();
    }

    /**
     * Gets available skus.
     *
     * @return the available skus
     */
    public List<String> getAvailableSkus() {
        return new ArrayList<>(bySku.keySet());
    }

    /**
     * Gets dedicated sku.
     *
     * @return the dedicated sku
     */
// Métodos para gerenciar SKU dedicado
    public String getDedicatedSku() {
        return dedicatedSku;
    }

    /**
     * Is empty boolean.
     *
     * @return the boolean
     */
    public boolean isEmpty() {
        return totalBoxes() == 0;
    }

    /**
     * Can accept sku boolean.
     *
     * @param sku the sku
     * @return the boolean
     */
    public boolean canAcceptSku(String sku) {
        return dedicatedSku == null || dedicatedSku.equals(sku);
    }

    /**
     * Is dedicated to sku boolean.
     *
     * @param sku the sku
     * @return the boolean
     */
    public boolean isDedicatedToSku(String sku) {
        return dedicatedSku != null && dedicatedSku.equals(sku);
    }

    @Override
    public String toString() {
        return "Bay{" + id + ", boxes=" + totalBoxes() + ", dedicatedSku=" + dedicatedSku + "}";
    }
}
