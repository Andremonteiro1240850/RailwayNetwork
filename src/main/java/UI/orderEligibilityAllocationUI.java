package UI;

import Controller.WarehouseController;
import Domain.AllocationRow;
import Domain.EligibilityResult;
import Domain.Warehouse;
import Repository.BoxRepository;

import java.util.List;

public class orderEligibilityAllocationUI implements Runnable {
    WarehouseController warehouseController;
    private final BoxRepository boxRepository;
    public  orderEligibilityAllocationUI(WarehouseController warehouseController, BoxRepository boxRepository) {
        this.warehouseController = warehouseController;
        this.boxRepository = boxRepository;
    }
    @Override
    public void run() {
        List<EligibilityResult> estados = warehouseController.estadoEncomenda();
        for (EligibilityResult er : estados) {
            System.out.println(
                    "order=" + er.getOrderId() +
                            ", lineNo=" + er.getLineNo() +
                            ", sku=" + er.getSku() +
                            ", requested=" + er.getRequestedQty() +
                            ", allocated=" + er.getAllocatedQty() +
                            ", status=" + er.getStatus()
            );
        }
        System.out.println();
        List<AllocationRow> resultados = warehouseController.createAllocationRows(boxRepository);
        for (AllocationRow r : resultados) {
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
