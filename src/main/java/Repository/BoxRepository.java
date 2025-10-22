package Repository;

import Domain.BayId;
import Domain.Box;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository for managing boxes in different states:
 * - Unassigned boxes (from wagons, without bay location)
 * - Assigned boxes (with bay location)
 */
public class BoxRepository {
    
    // Boxes without bay assignment (from wagons)
    private final Map<String, List<Box>> unassignedBoxes;
    
    // Boxes with bay assignment
    private final Map<String, List<Box>> assignedBoxes;
    
    // All boxes by boxId for quick lookup
    private final Map<String, Box> allBoxes;

    /**
     * Instantiates a new Box repository.
     */
    public BoxRepository() {
        this.unassignedBoxes = new HashMap<>();
        this.assignedBoxes = new HashMap<>();
        this.allBoxes = new HashMap<>();
    }

    /**
     * Add a box from wagon (without bay assignment)
     *
     * @param box the box
     */
    public void addUnassignedBox(Box box) {
        if (box.getLocation() != null) {
            throw new IllegalArgumentException("Box " + box.getBoxId() + " already has a location");
        }
        
        String sku = box.getSku();
        unassignedBoxes.computeIfAbsent(sku, k -> new ArrayList<>()).add(box);
        allBoxes.put(box.getBoxId(), box);
        
        // Box added silently - debug messages only in UI
    }

    /**
     * Move a box from unassigned to assigned state
     *
     * @param box   the box
     * @param bayId the bay id
     */
    public void assignBoxToBay(Box box, BayId bayId) {
        if (box.getLocation() != null) {
            throw new IllegalArgumentException("Box " + box.getBoxId() + " is already assigned to " + box.getLocation());
        }

        // Set the location
        box.setLocation(bayId);

        // Move from unassigned to assigned
        String sku = box.getSku();
        List<Box> unassignedList = unassignedBoxes.get(sku);
        if (unassignedList != null) {
            unassignedList.remove(box);
            if (unassignedList.isEmpty()) {
                unassignedBoxes.remove(sku);
            }
        }

        assignedBoxes.computeIfAbsent(sku, k -> new ArrayList<>()).add(box);

        // Box assigned silently - debug messages only in UI
    }

    /**
     * Remove a box from assigned state (when dispatched)
     *
     * @param box the box
     */
    public void removeAssignedBox(Box box) {
        if (box.getLocation() == null) {
            throw new IllegalArgumentException("Box " + box.getBoxId() + " is not assigned to any bay");
        }
        
        String sku = box.getSku();
        List<Box> assignedList = assignedBoxes.get(sku);
        if (assignedList != null) {
            assignedList.remove(box);
            if (assignedList.isEmpty()) {
                assignedBoxes.remove(sku);
            }
        }
        
        allBoxes.remove(box.getBoxId());
        
        // Box removed silently - debug messages only in UI
    }

    /**
     * Get all unassigned boxes for a specific SKU
     *
     * @param sku the sku
     * @return the unassigned boxes
     */
    public List<Box> getUnassignedBoxes(String sku) {
        return unassignedBoxes.getOrDefault(sku, new ArrayList<>());
    }

    /**
     * Get all assigned boxes for a specific SKU
     *
     * @param sku the sku
     * @return the assigned boxes
     */
    public List<Box> getAssignedBoxes(String sku) {
        return assignedBoxes.getOrDefault(sku, new ArrayList<>());
    }

    /**
     * Get all unassigned boxes (all SKUs)
     *
     * @return the all unassigned boxes
     */
    public Map<String, List<Box>> getAllUnassignedBoxes() {
        Map<String, List<Box>> result = new HashMap<>();

        for (Map.Entry<String, List<Box>> entry : unassignedBoxes.entrySet()) {
            String sku = entry.getKey();
            List<Box> boxes = entry.getValue().stream()
                    .filter(b -> b.getLocation() == null) // apenas boxes sem localização
                    .collect(Collectors.toList());

            if (!boxes.isEmpty()) {
                result.put(sku, boxes);
            }
        }

        return result;
    }


    /**
     * Get all assigned boxes (all SKUs)
     *
     * @return the all assigned boxes
     */
    public Map<String, List<Box>> getAllAssignedBoxes() {
        return new HashMap<>(assignedBoxes);
    }

    /**
     * Get a box by its ID
     *
     * @param boxId the box id
     * @return the box by id
     */
    public Box getBoxById(String boxId) {
        return allBoxes.get(boxId);
    }

    /**
     * Get all boxes (assigned and unassigned) for a SKU
     *
     * @param sku the sku
     * @return the all boxes for sku
     */
    public List<Box> getAllBoxesForSku(String sku) {
        List<Box> allBoxesForSku = new ArrayList<>();
        allBoxesForSku.addAll(getUnassignedBoxes(sku));
        allBoxesForSku.addAll(getAssignedBoxes(sku));
        return allBoxesForSku;
    }

    /**
     * Get all unassigned boxes sorted by FEFO/FIFO
     *
     * @param sku the sku
     * @return the unassigned boxes sorted
     */
    public List<Box> getUnassignedBoxesSorted(String sku) {
        List<Box> boxes = getUnassignedBoxes(sku);
        boxes.sort(Box.FEFO_COMPARATOR);
        return boxes;
    }

    /**
     * Get all assigned boxes sorted by FEFO/FIFO
     *
     * @param sku the sku
     * @return the assigned boxes sorted
     */
    public List<Box> getAssignedBoxesSorted(String sku) {
        List<Box> boxes = getAssignedBoxes(sku);
        boxes.sort(Box.FEFO_COMPARATOR);
        return boxes;
    }

    /**
     * Get statistics about the repository
     *
     * @return the stats
     */
    public RepositoryStats getStats() {
        int totalUnassigned = unassignedBoxes.values().stream()
                .mapToInt(List::size).sum();
        int totalAssigned = assignedBoxes.values().stream()
                .mapToInt(List::size).sum();
        
        return new RepositoryStats(
            totalUnassigned,
            totalAssigned,
            unassignedBoxes.size(),
            assignedBoxes.size()
        );
    }

    /**
     * Get all SKUs that have unassigned boxes
     *
     * @return the unassigned skus
     */
    public Set<String> getUnassignedSkus() {
        return unassignedBoxes.keySet();
    }

    /**
     * Get all SKUs that have assigned boxes
     *
     * @return the assigned skus
     */
    public Set<String> getAssignedSkus() {
        return assignedBoxes.keySet();
    }

    /**
     * Check if there are any unassigned boxes
     *
     * @return the boolean
     */
    public boolean hasUnassignedBoxes() {
        return !unassignedBoxes.isEmpty();
    }

    /**
     * Check if there are any unassigned boxes for a specific SKU
     *
     * @param sku the sku
     * @return the boolean
     */
    public boolean hasUnassignedBoxes(String sku) {
        List<Box> boxes = unassignedBoxes.get(sku);
        return boxes != null && !boxes.isEmpty();
    }

    /**
     * Get all boxes in the repository
     *
     * @return the all boxes
     */
    public Collection<Box> getAllBoxes() {
        return allBoxes.values();
    }

    /**
     * Clear all data
     */
    public void clear() {
        unassignedBoxes.clear();
        assignedBoxes.clear();
        allBoxes.clear();
    }

    /**
     * Print repository status
     */
    public void printStatus() {
        System.out.println("\n=== BOX REPOSITORY STATUS ===");
        RepositoryStats stats = getStats();
        System.out.println("📦 Unassigned boxes: " + stats.totalUnassigned + " (SKUs: " + stats.unassignedSkus + ")");
        System.out.println("✅ Assigned boxes: " + stats.totalAssigned + " (SKUs: " + stats.assignedSkus + ")");
        System.out.println("📊 Total boxes: " + (stats.totalUnassigned + stats.totalAssigned));
        
        if (!unassignedBoxes.isEmpty()) {
            System.out.println("\nUnassigned boxes by SKU:");
            unassignedBoxes.forEach((sku, boxes) -> 
                System.out.println("  " + sku + ": " + boxes.size() + " boxes"));
        }
        
        if (!assignedBoxes.isEmpty()) {
            System.out.println("\nAssigned boxes by SKU:");
            assignedBoxes.forEach((sku, boxes) -> 
                System.out.println("  " + sku + ": " + boxes.size() + " boxes"));
        }
    }

    /**
     * Statistics class for repository
     */
    public static class RepositoryStats {
        /**
         * The Total unassigned.
         */
        public final int totalUnassigned;
        /**
         * The Total assigned.
         */
        public final int totalAssigned;
        /**
         * The Unassigned skus.
         */
        public final int unassignedSkus;
        /**
         * The Assigned skus.
         */
        public final int assignedSkus;

        /**
         * Instantiates a new Repository stats.
         *
         * @param totalUnassigned the total unassigned
         * @param totalAssigned   the total assigned
         * @param unassignedSkus  the unassigned skus
         * @param assignedSkus    the assigned skus
         */
        public RepositoryStats(int totalUnassigned, int totalAssigned, int unassignedSkus, int assignedSkus) {
            this.totalUnassigned = totalUnassigned;
            this.totalAssigned = totalAssigned;
            this.unassignedSkus = unassignedSkus;
            this.assignedSkus = assignedSkus;
        }
    }
}


