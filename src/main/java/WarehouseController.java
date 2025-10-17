import java.util.*;

public class WarehouseController {

    private final Warehouse warehouse;

    public WarehouseController(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    /**
     * Processa as encomendas (US EI02) — aloca quantidades de caixas (boxes)
     * segundo as regras FEFO/FIFO já aplicadas na US EI01.
     *
     * @param partialMode  true = permite linhas parciais; false = apenas completas (strict)
     * @return AllocationResult com listas de EligibilityResult e AllocationRow
     */
    public AllocationResult processOrderAllocation(boolean partialMode) {

        Map<String, List<Box>> stock = warehouse.getStock();
        List<Order> orders = new ArrayList<>(warehouse.getOrders().values());

        List<EligibilityResult> eligibilityResults = new ArrayList<>();
        List<AllocationRow> allocationRows = new ArrayList<>();

        // Percorrer as encomendas por ordem natural (podes ordenar se quiseres)
        for (Order order : orders) {
            int lineNo = 1;
            for (OrderLine line : order.getLines()) {
                // Obter apenas as boxes do SKU certo
                List<Box> boxesForSku = stock.getOrDefault(line.getSku(), new ArrayList<>());

                processOrderLine(
                        order.getId(),
                        lineNo,
                        line,
                        boxesForSku,
                        partialMode,
                        eligibilityResults,
                        allocationRows
                );

                lineNo++;
            }
        }

        return new AllocationResult(eligibilityResults, allocationRows);
    }

    /**
     * Processa uma linha de encomenda individual — aloca quantidades de caixas.
     */
    private void processOrderLine(String orderId,
                                  int lineNo,
                                  OrderLine line,
                                  List<Box> boxes,
                                  boolean partialMode,
                                  List<EligibilityResult> eligibilityResults,
                                  List<AllocationRow> allocationRows) {

        int remainingQty = line.getQty();
        List<AllocationRow> tempAllocations = new ArrayList<>();

        // Percorrer boxes do SKU (já ordenadas FEFO/FIFO)
        for (Box box : boxes) {
            if (remainingQty == 0) break;

            if (box.getQty() > 0) {
                int qtyToAllocate = Math.min(remainingQty, box.getQty());

                tempAllocations.add(new AllocationRow(
                        orderId,
                        lineNo,
                        line.getSku(),
                        qtyToAllocate,
                        box.getBoxId(),
                        box.getAisleId(),
                        box.getBayId()
                ));

                box.reduceQty(qtyToAllocate);
                remainingQty -= qtyToAllocate;
            }
        }

        int totalAllocated = line.getQty() - remainingQty;
        AllocationStatus status = calculateStatus(totalAllocated, line.getQty(), partialMode);

        eligibilityResults.add(new EligibilityResult(
                orderId,
                lineNo,
                line.getSku(),
                line.getQty(),
                totalAllocated,
                status
        ));

        // Adicionar as alocações apenas se for elegível ou modo parcial ativo
        if (partialMode || status == AllocationStatus.ELIGIBLE) {
            allocationRows.addAll(tempAllocations);
        }
    }
    private AllocationStatus calculateStatus(int allocatedQty, int requestedQty, boolean partialMode) {
        if (allocatedQty == requestedQty) {
            return AllocationStatus.ELIGIBLE;
        } else if (allocatedQty == 0) {
            return AllocationStatus.UNDISPATCHABLE;
        } else {
            return partialMode ? AllocationStatus.PARTIAL : AllocationStatus.UNDISPATCHABLE;
        }
    }
}