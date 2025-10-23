/**package Controller;

import Domain.*;
import Domain.ENUM.AllocationStatus;
import Repository.BoxRepository;
import Repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WarehouseControllerTest {

    private Warehouse warehouse;
    private WarehouseRepository warehouseRepository;
    private BoxRepository boxRepository;
    private WarehouseController warehouseController;

    @BeforeEach
    void setUp() {
        warehouse = mock(Warehouse.class);
        warehouseRepository = mock(WarehouseRepository.class);
        boxRepository = mock(BoxRepository.class);
        warehouseController = new WarehouseController(warehouse, warehouseRepository);
    }

    @Test
    void testEstadoEncomenda_EmptyStockAndOrders() {
        // Arrange
        when(warehouseRepository.getAllStock()).thenReturn(new HashMap<>());
        when(warehouseRepository.getOrders()).thenReturn(new ArrayList<>());

        // Act
        List<EligibilityResult> results = warehouseController.estadoEncomenda();

        // Assert
        assertTrue(results.isEmpty());
        verify(warehouseRepository).saveEligibilityResults(results);
    }

    @Test
    void testEstadoEncomenda_WithOrdersButNoStock() {
        // Arrange
        Map<String, List<Box>> stock = new HashMap<>();
        stock.put("SKU1", new ArrayList<>());

        Order order = new Order("ORDER1", 1, LocalDate.now().plusDays(1));
        OrderLine line = new OrderLine(1, "SKU1", 10);
        order.addLine(line);

        when(warehouseRepository.getAllStock()).thenReturn(stock);
        when(warehouseRepository.getOrders()).thenReturn(Arrays.asList(order));

        // Act
        List<EligibilityResult> results = warehouseController.estadoEncomenda();

        // Assert
        assertEquals(1, results.size());
        EligibilityResult result = results.get(0);
        assertEquals("ORDER1", result.getOrderId());
        assertEquals(1, result.getLineNo());
        assertEquals("SKU1", result.getSku());
        assertEquals(10, result.getRequestedQty());
        assertEquals(0, result.getAllocatedQty());
        assertEquals(AllocationStatus.UNDISPATCHABLE, result.getStatus());
        verify(warehouseRepository).saveEligibilityResults(results);
    }

    @Test
    void testEstadoEncomenda_FullAllocation() {
        // Arrange
        Box box = new Box("BOX1", "SKU1", 15, LocalDate.now().plusDays(30), LocalDateTime.now().minusDays(1));

        Map<String, List<Box>> stock = new HashMap<>();
        stock.put("SKU1", Arrays.asList(box));

        Order order = new Order("ORDER1", 1, LocalDate.now().plusDays(1));
        OrderLine line = new OrderLine(1, "SKU1", 10);
        order.addLine(line);

        when(warehouseRepository.getAllStock()).thenReturn(stock);
        when(warehouseRepository.getOrders()).thenReturn(Arrays.asList(order));

        // Act
        List<EligibilityResult> results = warehouseController.estadoEncomenda();

        // Assert
        assertEquals(1, results.size());
        EligibilityResult result = results.get(0);
        assertEquals("ORDER1", result.getOrderId());
        assertEquals(1, result.getLineNo());
        assertEquals("SKU1", result.getSku());
        assertEquals(10, result.getRequestedQty());
        assertEquals(10, result.getAllocatedQty());
        assertEquals(AllocationStatus.ELIGIBLE, result.getStatus());
        verify(warehouseRepository).saveEligibilityResults(results);
    }

    @Test
    void testEstadoEncomenda_PartialAllocation() {
        // Arrange
        Box box = new Box("BOX1", "SKU1", 5, LocalDate.now().plusDays(30), LocalDateTime.now().minusDays(1));

        Map<String, List<Box>> stock = new HashMap<>();
        stock.put("SKU1", Arrays.asList(box));

        Order order = new Order("ORDER1", 1, LocalDate.now().plusDays(1));
        OrderLine line = new OrderLine(1, "SKU1", 10);
        order.addLine(line);

        when(warehouseRepository.getAllStock()).thenReturn(stock);
        when(warehouseRepository.getOrders()).thenReturn(Arrays.asList(order));

        // Act
        List<EligibilityResult> results = warehouseController.estadoEncomenda();

        // Assert
        assertEquals(1, results.size());
        EligibilityResult result = results.get(0);
        assertEquals("ORDER1", result.getOrderId());
        assertEquals(1, result.getLineNo());
        assertEquals("SKU1", result.getSku());
        assertEquals(10, result.getRequestedQty());
        assertEquals(5, result.getAllocatedQty());
        assertEquals(AllocationStatus.PARTIAL, result.getStatus());
        verify(warehouseRepository).saveEligibilityResults(results);
    }

    @Test
    void testEstadoEncomenda_MultipleOrdersAndSKUs() {
        // Arrange
        Box box1 = new Box("BOX1", "SKU1", 10, LocalDate.now().plusDays(30), LocalDateTime.now().minusDays(2));
        Box box2 = new Box("BOX2", "SKU2", 5, LocalDate.now().plusDays(20), LocalDateTime.now().minusDays(1));

        Map<String, List<Box>> stock = new HashMap<>();
        stock.put("SKU1", Arrays.asList(box1));
        stock.put("SKU2", Arrays.asList(box2));

        Order order1 = new Order("ORDER1", 1, LocalDate.now().plusDays(1));
        order1.addLine(new OrderLine(1, "SKU1", 5));

        Order order2 = new Order("ORDER2", 2, LocalDate.now().plusDays(2));
        order2.addLine(new OrderLine(1, "SKU2", 3));

        when(warehouseRepository.getAllStock()).thenReturn(stock);
        when(warehouseRepository.getOrders()).thenReturn(Arrays.asList(order1, order2));

        // Act
        List<EligibilityResult> results = warehouseController.estadoEncomenda();

        // Assert
        assertEquals(2, results.size());
        verify(warehouseRepository).saveEligibilityResults(results);
    }

    @Test
    void testEstadoEncomenda_OrderSorting() {
        // Arrange
        Order order1 = new Order("ORDER1", 2, LocalDate.now().plusDays(2));
        Order order2 = new Order("ORDER2", 1, LocalDate.now().plusDays(1));
        Order order3 = new Order("ORDER3", 1, LocalDate.now().plusDays(1));

        when(warehouseRepository.getAllStock()).thenReturn(new HashMap<>());
        when(warehouseRepository.getOrders()).thenReturn(Arrays.asList(order1, order2, order3));

        // Act
        List<EligibilityResult> results = warehouseController.estadoEncomenda();

        // Assert
        // Verificar que o método executa sem erros (a ordenação é interna)
        assertNotNull(results);
        verify(warehouseRepository).saveEligibilityResults(results);
    }

    @Test
    void testCreateAllocationRows_EmptyAssignedBoxes() {
        // Arrange
        when(boxRepository.getAllAssignedBoxes()).thenReturn(new HashMap<>());
        when(warehouseRepository.getOrders()).thenReturn(new ArrayList<>());

        // Act
        List<AllocationRow> results = warehouseController.createAllocationRows(boxRepository);

        // Assert
        assertTrue(results.isEmpty());
        verify(warehouseRepository).saveAllocationRows(results);
    }

    @Test
    void testCreateAllocationRows_WithAssignedBoxes() {
        // Arrange
        Location location = new Location("A1", "B1");
        Box box = new Box("BOX1", "SKU1", 10, LocalDate.now().plusDays(30), LocalDateTime.now().minusDays(1));
        box.setLocation(location);

        Map<String, List<Box>> assignedBoxes = new HashMap<>();
        assignedBoxes.put("SKU1", Arrays.asList(box));

        Order order = new Order("ORDER1", 1, LocalDate.now().plusDays(1));
        OrderLine line = new OrderLine(1, "SKU1", 5);
        order.addLine(line);

        when(boxRepository.getAllAssignedBoxes()).thenReturn(assignedBoxes);
        when(warehouseRepository.getOrders()).thenReturn(Arrays.asList(order));

        // Act
        List<AllocationRow> results = warehouseController.createAllocationRows(boxRepository);

        // Assert
        assertEquals(1, results.size());
        AllocationRow row = results.get(0);
        assertEquals("ORDER1", row.getOrderId());
        assertEquals(1, row.getLineNo());
        assertEquals("SKU1", row.getSku());
        assertEquals(5, row.getAllocatedQty());
        assertEquals("BOX1", row.getBoxId());
        assertEquals("A1", row.getAisle());
        assertEquals("B1", row.getBay());
        verify(warehouseRepository).saveAllocationRows(results);
    }

    @Test
    void testCreateAllocationRows_MultipleBoxesForSameSKU() {
        // Arrange
        Location location1 = new Location("A1", "B1");
        Location location2 = new Location("A1", "B2");

        Box box1 = new Box("BOX1", "SKU1", 5, LocalDate.now().plusDays(30), LocalDateTime.now().minusDays(2));
        box1.setLocation(location1);

        Box box2 = new Box("BOX2", "SKU1", 3, LocalDate.now().plusDays(25), LocalDateTime.now().minusDays(1));
        box2.setLocation(location2);

        Map<String, List<Box>> assignedBoxes = new HashMap<>();
        assignedBoxes.put("SKU1", Arrays.asList(box1, box2));

        Order order = new Order("ORDER1", 1, LocalDate.now().plusDays(1));
        OrderLine line = new OrderLine(1, "SKU1", 7);
        order.addLine(line);

        when(boxRepository.getAllAssignedBoxes()).thenReturn(assignedBoxes);
        when(warehouseRepository.getOrders()).thenReturn(Arrays.asList(order));

        // Act
        List<AllocationRow> results = warehouseController.createAllocationRows(boxRepository);

        // Assert
        assertEquals(2, results.size());
        assertEquals(5, results.get(0).getAllocatedQty());
        assertEquals(2, results.get(1).getAllocatedQty());
        verify(warehouseRepository).saveAllocationRows(results);
    }

    @Test
    void testCreateAllocationRows_BoxSortingFEFO() {
        // Arrange
        Box box1 = new Box("BOX1", "SKU1", 5, LocalDate.now().plusDays(30), LocalDateTime.now().minusDays(2)); // Mais antiga
        Box box2 = new Box("BOX2", "SKU1", 5, LocalDate.now().plusDays(25), LocalDateTime.now().minusDays(1)); // Expira primeiro

        Map<String, List<Box>> assignedBoxes = new HashMap<>();
        assignedBoxes.put("SKU1", Arrays.asList(box1, box2));

        Order order = new Order("ORDER1", 1, LocalDate.now().plusDays(1));
        OrderLine line = new OrderLine(1, "SKU1", 5);
        order.addLine(line);

        when(boxRepository.getAllAssignedBoxes()).thenReturn(assignedBoxes);
        when(warehouseRepository.getOrders()).thenReturn(Arrays.asList(order));

        // Act
        List<AllocationRow> results = warehouseController.createAllocationRows(boxRepository);

        // Assert
        // BOX2 deve ser usada primeiro por ter expiry date mais próximo
        assertEquals(1, results.size());
        assertEquals("BOX2", results.get(0).getBoxId());
        verify(warehouseRepository).saveAllocationRows(results);
    }

    @Test
    void testDeepCopyStock() {
        // Arrange
        Map<String, List<Box>> originalStock = new HashMap<>();

        Location location = new Location("A1", "B1");
        Box originalBox = new Box("BOX1", "SKU1", 10, LocalDate.now().plusDays(30), LocalDateTime.now().minusDays(1));
        originalBox.setLocation(location);

        originalStock.put("SKU1", Arrays.asList(originalBox));

        // Act
        Map<String, List<Box>> copy = warehouseController.deepCopyStock(originalStock);

        // Assert
        assertNotSame(originalStock, copy);
        assertEquals(1, copy.size());
        assertTrue(copy.containsKey("SKU1"));

        List<Box> copiedBoxes = copy.get("SKU1");
        assertEquals(1, copiedBoxes.size());

        Box copiedBox = copiedBoxes.get(0);
        assertNotSame(originalBox, copiedBox);
        assertEquals(originalBox.getBoxId(), copiedBox.getBoxId());
        assertEquals(originalBox.getSku(), copiedBox.getSku());
        assertEquals(originalBox.getQty(), copiedBox.getQty());
        assertEquals(originalBox.getExpiryDate(), copiedBox.getExpiryDate());
        assertEquals(originalBox.getReceivedAt(), copiedBox.getReceivedAt());
        assertNotNull(copiedBox.getLocation());
        assertEquals(originalBox.getLocation().getAisle(), copiedBox.getLocation().getAisle());
        assertEquals(originalBox.getLocation().getBay(), copiedBox.getLocation().getBay());
    }

    @Test
    void testCreateAllocationRows_OrderLineSorting() {
        // Arrange
        Location location = new Location("A1", "B1");
        Box box = new Box("BOX1", "SKU1", 20, LocalDate.now().plusDays(30), LocalDateTime.now().minusDays(1));
        box.setLocation(location);

        Map<String, List<Box>> assignedBoxes = new HashMap<>();
        assignedBoxes.put("SKU1", Arrays.asList(box));

        Order order = new Order("ORDER1", 1, LocalDate.now().plusDays(1));
        OrderLine line2 = new OrderLine(2, "SKU1", 5);
        OrderLine line1 = new OrderLine(1, "SKU1", 5);
        order.addLine(line2);
        order.addLine(line1);

        when(boxRepository.getAllAssignedBoxes()).thenReturn(assignedBoxes);
        when(warehouseRepository.getOrders()).thenReturn(Arrays.asList(order));

        // Act
        List<AllocationRow> results = warehouseController.createAllocationRows(boxRepository);

        // Assert
        assertEquals(2, results.size());
        // Verificar que as linhas estão ordenadas por lineNo
        assertEquals(1, results.get(0).getLineNo());
        assertEquals(2, results.get(1).getLineNo());
        verify(warehouseRepository).saveAllocationRows(results);
    }
}*/