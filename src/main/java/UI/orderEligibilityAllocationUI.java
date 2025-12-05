package UI;

import Controller.WarehouseController;
import Domain.AllocationRow;
import Domain.AllocationResult;
import Domain.EligibilityResult;
import Domain.ENUM.AllocationMode;
import Repository.BoxRepository;

/**
 * The type Order eligibility allocation ui.
 */
public class orderEligibilityAllocationUI implements Runnable {
    /**
     * The Warehouse controller.
     */
    WarehouseController warehouseController;
    private final BoxRepository boxRepository;

    /**
     * Instantiates a new Order eligibility allocation ui.
     *
     * @param warehouseController the warehouse controller
     * @param boxRepository       the box repository
     */
    public  orderEligibilityAllocationUI(WarehouseController warehouseController, BoxRepository boxRepository) {
        this.warehouseController = warehouseController;
        this.boxRepository = boxRepository;
    }
    @Override
    public void run() {
        // USEI02 - Process order eligibility and allocation with STRICT mode (default)
        AllocationResult result = warehouseController.processOrderEligibilityAndAllocation(boxRepository, AllocationMode.STRICT);
        
        System.out.println("=== ORDER ELIGIBILITY RESULTS ===");
        for (EligibilityResult er : result.getEligibilityResults()) {
            System.out.println(
                    "order=" + er.getOrderId() +
                            ", lineNo=" + er.getLineNo() +
                            ", sku=" + er.getSku() +
                            ", requested=" + er.getRequestedQty() +
                            ", allocated=" + er.getAllocatedQty() +
                            ", status=" + er.getStatus()
            );
        }
        
        System.out.println("\n=== ALLOCATION ROWS ===");
        for (AllocationRow r : result.getAllocationRows()) {
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
