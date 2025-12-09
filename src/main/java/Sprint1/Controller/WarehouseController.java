package Sprint1.Controller;

import Sprint1.Domain.*;
import Sprint1.Domain.ENUM.AllocationStatus;
import Sprint1.Domain.ENUM.AllocationMode;
import Sprint1.Repository.BoxRepository;
import Sprint1.Repository.WarehouseRepository;

import java.util.*;

/**
 * The type Warehouse controller.
 */
public class WarehouseController {
    private final Warehouse warehouse;
    private final WarehouseRepository warehouseRepository;

    /**
     * Instantiates a new Warehouse controller.
     *
     * @param warehouse           the warehouse
     * @param warehouseRepository the warehouse repository
     */
    public WarehouseController(Warehouse warehouse, WarehouseRepository warehouseRepository) {
        this.warehouse = warehouse;
        this.warehouseRepository = warehouseRepository;
    }

    /**
     * USEI02 - Process order eligibility and allocation
     * Main method that implements the complete USEI02 user story
     *
     * @param boxRepository the box repository
     * @param mode          the allocation mode (STRICT or PARTIAL)
     * @return the allocation result containing both eligibility results and allocation rows
     */
    public AllocationResult processOrderEligibilityAndAllocation(BoxRepository boxRepository, AllocationMode mode) {
        // Get all assigned boxes (boxes with bay locations)
        Map<String, List<Box>> assignedBoxes = boxRepository.getAllAssignedBoxes();
        
        // Get all orders and sort them according to acceptance criteria
        List<Order> allOrders = warehouseRepository.getOrders();
        List<Order> sortedOrders = new ArrayList<>(allOrders);
        sortedOrders.sort(Comparator
                .comparingInt(Order::getPriority)
                .thenComparing(Order::getDueDate)
                .thenComparing(Order::getId)
        );

        // Create working copy of stock for allocation simulation
        Map<String, List<Box>> workingStock = deepCopyStock(assignedBoxes);
        
        List<EligibilityResult> eligibilityResults = new ArrayList<>();
        List<AllocationRow> allocationRows = new ArrayList<>();

        for (Order order : sortedOrders) {
            // Sort lines by lineNo ASC (input order)
            List<OrderLine> lines = new ArrayList<>(order.getLines());
            lines.sort(Comparator.comparingInt(OrderLine::getLineNo));

            for (OrderLine line : lines) {
                String sku = line.getSku();
                int requestedQty = line.getRequestedQty();
                int allocatedQty = 0;
                int remainingQty = requestedQty;

                // Get boxes for this SKU and sort by FEFO/FIFO
                List<Box> boxes = workingStock.get(sku);
                if (boxes != null) {
                    boxes.sort(Box.FEFO_COMPARATOR);

                    // Allocate from boxes in FEFO/FIFO order
                    for (Box box : boxes) {
                        if (remainingQty <= 0) break;

                        int availableInBox = box.getQty();
                        if (availableInBox <= 0) continue;

                        int toAllocate = Math.min(remainingQty, availableInBox);
                        
                        // Create allocation row
                        allocationRows.add(new AllocationRow(
                                order.getId(),
                                line.getLineNo(),
                                sku,
                                toAllocate,
                                box.getBoxId(),
                                box.getLocation().getAisle(),
                                box.getLocation().getBay()
                        ));

                        // Update working stock
                        box.setQty(availableInBox - toAllocate);
                        allocatedQty += toAllocate;
                        remainingQty -= toAllocate;
                    }
                }

                // Determine status based on mode
                AllocationStatus status;
                if (mode == AllocationMode.STRICT) {
                    if (allocatedQty == requestedQty) {
                        status = AllocationStatus.ELIGIBLE;
                    } else {
                        status = AllocationStatus.UNDISPATCHABLE;
                        // Remove allocations for strict mode when not fully satisfied
                        allocationRows.removeIf(row -> 
                            row.getOrderId().equals(order.getId()) && 
                            row.getLineNo() == line.getLineNo()
                        );
                        allocatedQty = 0;
                    }
                } else { // PARTIAL mode
                    if (allocatedQty == 0) {
                        status = AllocationStatus.UNDISPATCHABLE;
                    } else if (allocatedQty == requestedQty) {
                        status = AllocationStatus.ELIGIBLE;
                    } else {
                        status = AllocationStatus.PARTIAL;
                    }
                }

                // Create eligibility result
                eligibilityResults.add(new EligibilityResult(
                        order.getId(),
                        line.getLineNo(),
                        sku,
                        requestedQty,
                        allocatedQty,
                        status
                ));
            }
        }

        // Save results to repository
        warehouseRepository.saveEligibilityResults(eligibilityResults);
        warehouseRepository.saveAllocationRows(allocationRows);

        return new AllocationResult(eligibilityResults, allocationRows);
    }

    /**
     * Estado encomenda list.
     *
     * @return the list
     */
    private Map<String, List<Box>> deepCopyStock(Map<String, List<Box>> originalStock) {
        Map<String, List<Box>> copy = new HashMap<>();
        for (Map.Entry<String, List<Box>> entry : originalStock.entrySet()) {
            List<Box> boxCopies = new ArrayList<>();
            for (Box originalBox : entry.getValue()) {
                // Criar nova instância com os mesmos valores
                Box boxCopy = new Box(
                        originalBox.getBoxId(),
                        originalBox.getSku(),
                        originalBox.getQty(), // Quantidade original
                        originalBox.getExpiryDate(),
                        originalBox.getReceivedAt()
                );
                // Copiar location se existir
                if (originalBox.getLocation() != null) {
                    boxCopy.setLocation(originalBox.getLocation());
                }
                boxCopies.add(boxCopy);
            }
            copy.put(entry.getKey(), boxCopies);
        }
        return copy;
    }

}