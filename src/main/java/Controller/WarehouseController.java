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

        // Ordenar encomendas: priority ASC, dueDate ASC, orderId ASC
        java.util.List<Order> sortedOrders = new java.util.ArrayList<>(warehouse.getOrders().values());
        sortedOrders.sort(java.util.Comparator
                .comparingInt(Order::getPriority)
                .thenComparing(Order::getDueDate)
                .thenComparing(Order::getId)
        );

        for (Order order : sortedOrders) {
            // Ordenar linhas por lineNo ASC
            java.util.List<OrderLine> lines = new java.util.ArrayList<>(order.getLines());
            lines.sort(java.util.Comparator.comparingInt(OrderLine::getLineNo));

            for (OrderLine line : lines) {
                int requested = line.getRequestedQty();
                String sku = line.getSku();
                int allocatedTotal = 0;

                // Obter boxes deste SKU ordenadas por FEFO/FIFO
                java.util.List<Box> boxes = stock.get(sku);
                if (boxes != null) {
                    // Ordenar por: ExpiryDate (menor primeiro, nulos no fim), ReceivedAt (mais antigo primeiro), BoxId (ascendente)
                    boxes.sort(java.util.Comparator
                            .comparing(Box::getExpiryDate, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))
                            .thenComparing(Box::getReceivedAt)
                            .thenComparing(Box::getBoxId)
                    );

                    // Percorrer boxes consumindo stock até satisfazer a quantidade requisitada
                    for (Box box : boxes) {
                        if (allocatedTotal >= requested) break;

                        int availableInBox = box.getQty();
                        if (availableInBox <= 0) continue;

                        int toAllocate = Math.min(requested - allocatedTotal, availableInBox);

                        // Consumir da box
                        box.setQty(availableInBox - toAllocate);
                        allocatedTotal += toAllocate;
                    }
                }

                AllocationStatus status;
                if (allocatedTotal == 0) {
                    status = AllocationStatus.UNDISPATCHABLE;
                } else if (allocatedTotal == requested) {
                    status = AllocationStatus.ELIGIBLE;
                } else {
                    status = AllocationStatus.PARTIAL;
                }

                line.setAllocatedQty(allocatedTotal);
                line.setStatus(status.name());

                estados.add(new EligibilityResult(
                        order.getId(),
                        line.getLineNo(),
                        sku,
                        requested,
                        allocatedTotal,
                        status
                ));
            }
        }

        return estados;
    }
    public List<AllocationRow> createAllocationRows(){
        List<AllocationRow> allocationRows = new ArrayList<>();
        java.util.Map<String, java.util.List<Box>> stock = warehouse.getStock();

        // Ordenar encomendas: priority ASC, dueDate ASC, orderId ASC
        java.util.List<Order> sortedOrders = new java.util.ArrayList<>(warehouse.getOrders().values());
        java.util.Collections.sort(sortedOrders);

        for (Order order : sortedOrders) {
            // Ordenar linhas por lineNo ASC
            java.util.List<OrderLine> lines = new java.util.ArrayList<>(order.getLines());
            lines.sort(java.util.Comparator.comparingInt(OrderLine::getLineNo));

            for (OrderLine line : lines) {
                String sku = line.getSku();
                int requested = line.getRequestedQty();
                int remaining = requested;

                // Obter boxes deste SKU ordenadas por FEFO/FIFO
                java.util.List<Box> boxes = stock.get(sku);
                if (boxes != null) {
                    // Ordenar por: ExpiryDate (menor primeiro, nulos no fim), ReceivedAt (mais antigo primeiro), BoxId (ascendente)
                    boxes.sort(java.util.Comparator
                            .comparing(Box::getExpiryDate, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))
                            .thenComparing(Box::getReceivedAt)
                            .thenComparing(Box::getBoxId)
                    );

                    for (Box box : boxes) {
                        if (remaining <= 0) break;

                        int availableInBox = box.getQty();
                        if (availableInBox <= 0) continue;

                        int allocatedFromBox = Math.min(remaining, availableInBox);

                        // Criar linha de alocação
                        // Para compatibilidade, usar valores padrão já que getAisle() e getBay() retornam null
                        allocationRows.add(new AllocationRow(
                                order.getId(),
                                line.getLineNo(),
                                sku,
                                allocatedFromBox,
                                box.getBoxId(),
                                box.getLocation().getAisle(), // aisleID - valor padrão para compatibilidade
                                box.getLocation().getBay()  // bayID - valor padrão para compatibilidade
                        ));

                        // Atualizar stock da box (consumir)
                        box.setQty(availableInBox - allocatedFromBox);
                        remaining -= allocatedFromBox;
                    }
                }
            }
        }

        return allocationRows;
    }


    }



