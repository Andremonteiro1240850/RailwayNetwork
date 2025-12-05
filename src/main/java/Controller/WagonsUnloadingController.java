package Controller;

import Domain.*;
import Repository.BoxRepository;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The type Wagons unloading controller.
 */
public class WagonsUnloadingController {
    private final Map<String, Item> items;
    private final List<Bay> bays;
    private final Map<BayId, Bay> bayMap;
    private final BoxRepository boxRepository;
    private final DataLoader dataLoader;
    private boolean dataLoaded = false;

    /**
     * Instantiates a new Wagons unloading controller.
     *
     * @param dataLoader the data loader
     */
    public WagonsUnloadingController(DataLoader dataLoader) {
        this.dataLoader = dataLoader;
        this.dataLoaded = true;
        this.items = dataLoader.items;
        this.bays = dataLoader.bays;
        this.bayMap = bays.stream().collect(Collectors.toMap(Bay::getId, bay -> bay));
        this.boxRepository = dataLoader.boxRepository;
    }


    /**
     * Execute wagon unloading process
     * Unload wagons and assign boxes to bays using FEFO/FIFO logic
     */
    public void unloadWagons() {
        if (!dataLoaded) {
            return;
        }
        
        // Get all unassigned boxes by SKU
        Map<String, List<Box>> unassignedBoxes = boxRepository.getAllUnassignedBoxes();
        
        for (Map.Entry<String, List<Box>> entry : unassignedBoxes.entrySet()) {
            String sku = entry.getKey();
            List<Box> boxes = entry.getValue();
            
            // Sort boxes by FEFO/FIFO before assigning to bays
            boxes.sort(Box.FEFO_COMPARATOR);
            
            for (Box box : boxes) {
                assignBoxToBay(box);
            }
        }
    }
    
    /**
     * Assign a box to a bay following SKU dedication policy using BoxRepository:
     * 1. First try to find a bay already dedicated to this SKU
     * 2. If none found, find the first empty bay
     * 3. If no empty bays, find the first bay that can accept this SKU (should not happen with dedication)
     */
    private void assignBoxToBay(Box box) {
        String sku = box.getSku();

        // 1. First priority: Find bays already dedicated to this SKU that are not full
        List<Bay> dedicatedBays = bays.stream()
                .filter(bay -> bay.isDedicatedToSku(sku) && !bay.isFull())
                .sorted(Comparator.comparing(Bay::getId))
                .collect(Collectors.toList());

        if (!dedicatedBays.isEmpty()) {
            Bay targetBay = dedicatedBays.get(0);
            boxRepository.assignBoxToBay(box, targetBay.getId());
            targetBay.insertBox(box);
            return;
        }

        // 2. Second priority: Find empty bays (no dedicated SKU)
        List<Bay> emptyBays = bays.stream()
                .filter(bay -> bay.isEmpty())
                .sorted(Comparator.comparing(Bay::getId))
                .collect(Collectors.toList());

        if (!emptyBays.isEmpty()) {
            Bay targetBay = emptyBays.get(0);
            boxRepository.assignBoxToBay(box, targetBay.getId());
            targetBay.insertBox(box);
            return;
        }

        // 3. No available bays - box remains unassigned
    }

    /**
     * Dispatch stock for a specific SKU
     * The operation dispatch must always consume stock from the "front" of the bay list, 
     * guaranteeing FEFO/FIFO behaviour.
     *
     * @param sku      the sku
     * @param quantity the quantity
     * @return the list
     */
    public List<Box> dispatchStock(String sku, int quantity) {
        if (!dataLoaded) {
            return new ArrayList<>();
        }
        
        if (!items.containsKey(sku)) {
            return new ArrayList<>();
        }
        
        if (quantity <= 0) {
            return new ArrayList<>();
        }
        
        List<Box> dispatchedBoxes = new ArrayList<>();
        int remainingQty = quantity;
        
        // Get all bays dedicated to this SKU, sorted by bay ID
        List<Bay> baysWithSku = bays.stream()
                .filter(bay -> bay.isDedicatedToSku(sku) && bay.hasSku(sku))
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
                    // Update repository - remove the box
                    boxRepository.removeAssignedBox(dispatchedBox);
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
                    // Note: Box remains in repository with updated quantity
                    remainingQty = 0;
                }
            }
        }
        
        return dispatchedBoxes;
    }

    /**
     * Relocate a box to a different bay
     * The operation relocation must update a box's warehouseId/aisle/bay only; 
     * do not re-sort its new bay if its expiryDate/receivedAt is unchanged 
     * except for inserting it into the new bay's FEFO position.
     *
     * @param boxId       the box id
     * @param warehouseId the warehouse id
     * @param aisle       the aisle
     * @param bay         the bay
     * @return the boolean
     */
    public boolean relocateBox(String boxId, String warehouseId, int aisle, int bay) {
        if (!dataLoaded) {
            return false;
        }
        
        BayId newBayId = new BayId(warehouseId, aisle, bay);
        
        // Find the box
        Box boxToMove = boxRepository.getBoxById(boxId);
        if (boxToMove == null) {
            return false;
        }
        
        Bay currentBay = bayMap.get(boxToMove.getLocation());
        if (currentBay == null) {
            return false;
        }
        
        Bay targetBay = bayMap.get(newBayId);
        if (targetBay == null) {
            return false;
        }
        
        if (targetBay.isFull()) {
            return false;
        }
        
        // Check if target bay can accept this SKU (must be empty or dedicated to same SKU)
        if (!targetBay.canAcceptSku(boxToMove.getSku())) {
            return false;
        }
        
        // Remove from current bay and add to new bay
        currentBay.removeBox(boxToMove);
        targetBay.insertBox(boxToMove);
        
        // Update repository - update box location
        boxRepository.updateBoxLocation(boxToMove, newBayId);
        
        return true;
    }

    /**
     * Get available SKUs
     *
     * @return the available skus
     */
    public List<String> getAvailableSkus() {
        if (!dataLoaded) {
            return new ArrayList<>();
        }
        
        return new ArrayList<>(items.keySet());
    }

    /**
     * Check if data is loaded
     *
     * @return the boolean
     */
    public boolean isDataLoaded() {
        return dataLoaded;
    }

    /**
     * Get box repository (for box management operations)
     *
     * @return the box repository
     */
    public BoxRepository getBoxRepository() {
        return boxRepository;
    }
    
    /**
     * Get items map
     *
     * @return the items
     */
    public Map<String, Item> getItems() {
        return items;
    }
    
    /**
     * Get bays list
     *
     * @return the bays
     */
    public List<Bay> getBays() {
        return bays;
    }
    
    /**
     * Get bay map
     *
     * @return the bay map
     */
    public Map<BayId, Bay> getBayMap() {
        return bayMap;
    }
    
    /**
     * Insert a returned box into inventory and place it according to FEFO/FIFO (USEI01 rules).
     * This method is used by InspectionController for restocking returned items.
     *
     * @param boxId      the box id
     * @param sku        the sku
     * @param qty        the qty
     * @param expiryDate the expiry date
     * @param receivedAt the received at
     */
    public void insertReturnedBox(String boxId, String sku, int qty,
                                  java.time.LocalDate expiryDate,
                                  java.time.LocalDateTime receivedAt) {
        Box box = new Box(boxId, sku, qty, expiryDate, receivedAt);
        // Add to repository as unassigned box
        boxRepository.addUnassignedBox(box);
        // Place the new box in a bay following the same policy used for wagon unloading
        assignBoxToBay(box);
    }
}
