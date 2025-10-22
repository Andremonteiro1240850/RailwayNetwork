package Controller;

import Domain.*;
import Domain.ENUM.AllocationStatus;
import Repository.BoxRepository;
import Repository.WarehouseRepository;

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
     * Estado encomenda list.
     *
     * @return the list
     */
    public List<EligibilityResult> estadoEncomenda() {
        List<EligibilityResult> estados = new ArrayList<>();


        Map<String, List<Box>> stock = warehouseRepository.getAllStock();

        // Calcular stock total por SKU (soma de todas as Box com esse SKU)
        Map<String, Integer> stockTotal = new HashMap<>();
        for (Map.Entry<String, List<Box>> entry : stock.entrySet()) {
            int total = 0;
            if (entry.getValue() != null) {
                for (Box b : entry.getValue()) {
                    total += b.getQty();
                }
            }
            stockTotal.put(entry.getKey(), total);
        }


        List<Order> allOrders = warehouseRepository.getOrders();
        Map<String, Order> ordersMap = new HashMap<>();
        for (Order order : allOrders) {
            ordersMap.put(order.getId(), order);
        }

        // Ordenar encomendas: priority ASC, dueDate ASC, orderId ASC
        List<Order> sortedOrders = new ArrayList<>(ordersMap.values());
        sortedOrders.sort(Comparator
                .comparingInt(Order::getPriority)
                .thenComparing(Order::getDueDate)
                .thenComparing(Order::getId)
        );


        Map<String, List<Box>> workingStock = deepCopyStock(stock);

        for (Order order : sortedOrders) {
            // Ordenar linhas por lineNo ASC
            List<OrderLine> lines = new ArrayList<>(order.getLines());
            lines.sort(Comparator.comparingInt(OrderLine::getLineNo));

            for (OrderLine line : lines) {
                int requested = line.getRequestedQty();
                String sku = line.getSku();
                int allocatedTotal = 0;

                // Obter boxes deste SKU ordenadas por FEFO/FIFO
                List<Box> boxes = workingStock.get(sku);
                if (boxes != null) {
                    // Ordenar por: ExpiryDate (menor primeiro, nulos no fim), ReceivedAt (mais antigo primeiro), BoxId (ascendente)
                    boxes.sort(Comparator
                            .comparing(Box::getExpiryDate, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(Box::getReceivedAt)
                            .thenComparing(Box::getBoxId)
                    );

                    // Percorrer boxes consumindo stock até satisfazer a quantidade requisitada
                    for (Box box : boxes) {
                        if (allocatedTotal >= requested) break;

                        int availableInBox = box.getQty();
                        if (availableInBox <= 0) continue;

                        int toAllocate = Math.min(requested - allocatedTotal, availableInBox);

                        // Consumir da box (na cópia de trabalho)
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


        warehouseRepository.saveEligibilityResults(estados);
        return estados;
    }

    /**
     * Create allocation rows list.
     *
     * @param boxRepository the box repository
     * @return the list
     */
    public List<AllocationRow> createAllocationRows(BoxRepository boxRepository) {
        List<AllocationRow> allocationRows = new ArrayList<>();

        // Buscar apenas as boxes já atribuídas a bays (ou seja, disponíveis para picking)
        Map<String, List<Box>> assignedBoxes = boxRepository.getAllAssignedBoxes();

        // Obter todas as encomendas do warehouse
        List<Order> allOrders = warehouseRepository.getOrders();
        Map<String, Order> ordersMap = new HashMap<>();
        for (Order order : allOrders) {
            ordersMap.put(order.getId(), order);
        }

        // Ordenar encomendas: priority ASC, dueDate ASC, orderId ASC
        List<Order> sortedOrders = new ArrayList<>(ordersMap.values());
        Collections.sort(sortedOrders);

        // Criar uma cópia independente do stock atribuído (para simular o consumo)
        Map<String, List<Box>> workingStock = deepCopyStock(assignedBoxes);

        for (Order order : sortedOrders) {
            // Ordenar linhas da encomenda (lineNo ASC)
            List<OrderLine> lines = new ArrayList<>(order.getLines());
            lines.sort(Comparator.comparingInt(OrderLine::getLineNo));

            for (OrderLine line : lines) {
                String sku = line.getSku();
                int requested = line.getRequestedQty();
                int remaining = requested;

                // Obter todas as boxes atribuídas com este SKU
                List<Box> boxes = workingStock.get(sku);
                if (boxes != null) {
                    // Ordenar por FEFO/FIFO
                    boxes.sort(Comparator
                            .comparing(Box::getExpiryDate, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(Box::getReceivedAt)
                            .thenComparing(Box::getBoxId)
                    );

                    for (Box box : boxes) {
                        if (remaining <= 0) break;

                        int availableInBox = box.getQty();
                        if (availableInBox <= 0) continue;

                        int allocatedFromBox = Math.min(remaining, availableInBox);

                        // Criar a linha de alocação
                        allocationRows.add(new AllocationRow(
                                order.getId(),
                                line.getLineNo(),
                                sku,
                                allocatedFromBox,
                                box.getBoxId(),
                                box.getLocation().getAisle(),
                                box.getLocation().getBay()
                        ));

                        // Atualizar a quantidade restante dessa box
                        box.setQty(availableInBox - allocatedFromBox);
                        remaining -= allocatedFromBox;
                    }
                }
            }
        }

        // Guardar as linhas criadas no repositório do warehouse (mantendo compatibilidade)
        warehouseRepository.saveAllocationRows(allocationRows);
        return allocationRows;
    }



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