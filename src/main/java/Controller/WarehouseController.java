package Controller;

import Domain.*;

import Domain.ENUM.AllocationStatus;
import java.util.ArrayList;
import java.util.List;

public class WarehouseController {
    Warehouse warehouse;
    public WarehouseController(Warehouse warehouse) {
        this.warehouse = warehouse;
    }
    public List<EligibilityResult> estadoEncomenda (){
        List<EligibilityResult> estados = new ArrayList<>();
        java.util.Map<String, java.util.List<Box>> stock = warehouse.getStock();
        
        // Calcular stock total por SKU (soma de todas as Box com esse SKU)
        java.util.Map<String, Integer> stockTotal = new java.util.HashMap<>();
        for (java.util.Map.Entry<String, java.util.List<Box>> entry : stock.entrySet()) {
            int total = 0;
            if (entry.getValue() != null) {
                for (Box b : entry.getValue()) {
                    total += b.getQty();
                }
            }
            stockTotal.put(entry.getKey(), total);
        }
        
        // Consome stock à medida que alocamos
        java.util.Map<String, Integer> availableStock = new java.util.HashMap<>(stockTotal);
        
        for (Order order : warehouse.getOrders().values()) {
            for (OrderLine line : order.getLines()) {
                int requested = line.getRequestedQty();
                String sku = line.getSku();
                
                int available = availableStock.getOrDefault(sku, 0);
                int allocated = Math.min(requested, available);
                
                // Consumir o stock alocado
                availableStock.put(sku, available - allocated);

                AllocationStatus status;

                if (allocated == 0) {
                    status = AllocationStatus.UNDISPATCHABLE;
                } else if (allocated == requested) {
                    status = AllocationStatus.ELIGIBLE;
                } else {
                    status = AllocationStatus.PARTIAL;
                }


                line.setAllocatedQty(allocated);
                line.setStatus(status.name());

                estados.add(new EligibilityResult(
                        order.getId(),
                        line.getLineNo(),
                        sku,
                        requested,
                        allocated,
                        status
                ));
            }
        }

        return estados;
    }


    }



