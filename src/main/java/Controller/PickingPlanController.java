package Controller;

import Domain.*;
import Domain.ENUM.PackingHeuristic;
import Repository.WarehouseRepository;

import java.util.*;

/**
 * The type Picking plan controller.
 * Implements USEI03 - Picking Plans
 */
public class PickingPlanController {
    private final WarehouseRepository warehouseRepository;

    /**
     * Instantiates a new Picking plan controller.
     *
     * @param warehouseRepository the warehouse repository
     */
    public PickingPlanController(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    /**
     * USEI03 - Create picking plan using specified heuristic
     *
     * @param capacity       the trolley capacity in kg
     * @param heuristic      the packing heuristic to use
     * @param allowSplitting whether to allow splitting items that don't fit
     * @return the picking plan
     */
    public PickingPlan createPickingPlan(int capacity, PackingHeuristic heuristic, boolean allowSplitting) {
        List<AllocationRow> allocationRows = warehouseRepository.getAllocationRows();
        if (allocationRows.isEmpty()) {
            return new PickingPlan(new ArrayList<>(), heuristic.name());
        }

        List<Trolley> trolleys = new ArrayList<>();
        List<AllocationRow> remainingItems = new ArrayList<>(allocationRows);

        switch (heuristic) {
            case FF:
                trolleys = firstFit(remainingItems, capacity, allowSplitting);
                break;
            case FFD:
                trolleys = firstFitDecreasing(remainingItems, capacity, allowSplitting);
                break;
            case BFD:
                trolleys = bestFitDecreasing(remainingItems, capacity, allowSplitting);
                break;
        }

        return new PickingPlan(trolleys, heuristic.name());
    }

    /**
     * First Fit (FF) heuristic
     */
    private List<Trolley> firstFit(List<AllocationRow> items, int capacity, boolean allowSplitting) {
        List<Trolley> trolleys = new ArrayList<>();
        List<AllocationRow> remainingItems = new ArrayList<>(items);

        for (AllocationRow item : items) {
            double itemWeight = calculateItemWeight(item);
            boolean placed = false;

            // Try to place in existing trolley
            for (Trolley trolley : trolleys) {
                if (trolley.getCurrentLoad() + itemWeight <= capacity) {
                    addItemToTrolley(trolley, item, itemWeight);
                    remainingItems.remove(item);
                    placed = true;
                    break;
                }
            }

            // If not placed and splitting is allowed, try to split
            if (!placed && allowSplitting && itemWeight > capacity) {
                // Split the item
                double remainingWeight = itemWeight;
                while (remainingWeight > 0) {
                    Trolley trolley = findOrCreateTrolley(trolleys, capacity);
                    double weightToAdd = Math.min(remainingWeight, capacity - trolley.getCurrentLoad());

                    if (weightToAdd > 0) {
                        AllocationRow splitItem = createSplitItem(item, weightToAdd);
                        addItemToTrolley(trolley, splitItem, weightToAdd);
                        remainingWeight -= weightToAdd;
                    } else {
                        // Create new trolley for remaining weight
                        trolley = new Trolley(capacity, 0, trolleys.size() + 1, new ArrayList<>(), "FF");
                        trolleys.add(trolley);
                    }
                }
                remainingItems.remove(item);
                placed = true;
            }

            // If still not placed, create new trolley
            if (!placed) {
                Trolley newTrolley = new Trolley(capacity, 0, trolleys.size() + 1, new ArrayList<>(), "FF");
                addItemToTrolley(newTrolley, item, itemWeight);
                trolleys.add(newTrolley);
                remainingItems.remove(item);
            }
        }
        return trolleys;
    }

    /**
     * First Fit Decreasing (FFD) heuristic
     */
    private List<Trolley> firstFitDecreasing(List<AllocationRow> items, int capacity, boolean allowSplitting) {
        // Sort items by weight (decreasing)
        List<AllocationRow> sortedItems = new ArrayList<>(items);
        sortedItems.sort((a, b) -> Double.compare(calculateItemWeight(b), calculateItemWeight(a)));

        return firstFit(sortedItems, capacity, allowSplitting);
    }

    /**
     * Best Fit Decreasing (BFD) heuristic
     */
    private List<Trolley> bestFitDecreasing(List<AllocationRow> items, int capacity, boolean allowSplitting) {
        List<Trolley> trolleys = new ArrayList<>();
        List<AllocationRow> remainingItems = new ArrayList<>(items);

        // Sort items by weight (decreasing)
        remainingItems.sort((a, b) -> Double.compare(calculateItemWeight(b), calculateItemWeight(a)));

        for (AllocationRow item : remainingItems) {
            double itemWeight = calculateItemWeight(item);
            Trolley bestTrolley = null;
            double bestRemainingCapacity = Double.MAX_VALUE;

            // Find the trolley with the smallest remaining capacity that can fit the item
            for (Trolley trolley : trolleys) {
                double remainingCapacity = capacity - trolley.getCurrentLoad();
                if (itemWeight <= remainingCapacity && remainingCapacity < bestRemainingCapacity) {
                    bestTrolley = trolley;
                    bestRemainingCapacity = remainingCapacity;
                }
            }

            if (bestTrolley != null) {
                addItemToTrolley(bestTrolley, item, itemWeight);
            } else {
                // Create new trolley
                Trolley newTrolley = new Trolley(capacity, 0, trolleys.size() + 1, new ArrayList<>(), "BFD");
                addItemToTrolley(newTrolley, item, itemWeight);
                trolleys.add(newTrolley);
            }
        }

        return trolleys;
    }

    /**
     * Calculate item weight based on quantity and item weight
     */
    private double calculateItemWeight(AllocationRow item) {
        Item itemInfo = warehouseRepository.getItemBySku(item.getSku());
        if (itemInfo == null) {
            return 0.0; // Default weight if item not found
        }
        return item.getQty() * itemInfo.getWeight();
    }

    /**
     * Add item to trolley and update load
     */
    private void addItemToTrolley(Trolley trolley, AllocationRow item, double weight) {
        PickingItems pickingItem = new PickingItems(
                item.getOrderId(),
                item.getLineNo(),
                item.getAisle(),
                item.getBay(),
                item.getBoxId(),
                item.getSku(),
                item.getQty()
        );
        trolley.getPickingItems().add(pickingItem);
        trolley.setCurrentLoad(trolley.getCurrentLoad() + weight);
    }

    /**
     * Find existing trolley or create new one
     */
    private Trolley findOrCreateTrolley(List<Trolley> trolleys, int capacity) {
        for (Trolley trolley : trolleys) {
            if (trolley.getCurrentLoad() < capacity) {
                return trolley;
            }
        }
        return new Trolley(capacity, 0, trolleys.size() + 1, new ArrayList<>(), "FF");
    }

    /**
     * Create a split item with reduced quantity
     */
    private AllocationRow createSplitItem(AllocationRow originalItem, double weightToAdd) {
        Item itemInfo = warehouseRepository.getItemBySku(originalItem.getSku());
        double unitWeight = itemInfo != null ? itemInfo.getWeight() : 1.0;
        int quantityToAdd = (int) Math.floor(weightToAdd / unitWeight);
        
        return new AllocationRow(
                originalItem.getOrderId(),
                originalItem.getLineNo(),
                originalItem.getSku(),
                quantityToAdd,
                originalItem.getBoxId(),
                originalItem.getAisle(),
                originalItem.getBay()
        );
    }
}
