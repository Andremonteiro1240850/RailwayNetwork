package UI;

import Controller.WarehouseController;
import Domain.AllocationRow;
import Domain.EligibilityResult;
import Domain.Warehouse;

import java.util.List;

public class orderEligibilityAllocationUI implements Runnable {
    WarehouseController warehouseController;
    public  orderEligibilityAllocationUI(WarehouseController warehouseController) {
        this.warehouseController = warehouseController;
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
        List<AllocationRow> resultados = warehouseController.createAllocationRows();
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
