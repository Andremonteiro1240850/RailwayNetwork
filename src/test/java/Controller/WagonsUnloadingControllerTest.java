package Controller;

import Domain.*;
import Repository.BoxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class WagonsUnloadingControllerTest {

    private DataLoader dataLoader;
    private WagonsUnloadingController controller;

    @BeforeEach
    void setUp() {
        dataLoader = new DataLoader();
        
        // Initialize items
        dataLoader.items.put("SKU-A", new Item("SKU-A", "Product A", "Category1", "unit", 1.0, 1.0));
        dataLoader.items.put("SKU-B", new Item("SKU-B", "Product B", "Category2", "unit", 2.0, 2.0));
        
        // Initialize bays
        dataLoader.bays.add(new Bay(new BayId("WH1", 1, 1), 5));
        dataLoader.bays.add(new Bay(new BayId("WH1", 1, 2), 5));
        dataLoader.bays.add(new Bay(new BayId("WH1", 2, 1), 3));
        
        // Initialize BoxRepository
        dataLoader.boxRepository = new BoxRepository();
        
        controller = new WagonsUnloadingController(dataLoader);
    }

    // ---------- Constructor ----------
    @Test
    @DisplayName("Constructor initializes controller with dataLoader")
    void constructor() {
        assertNotNull(controller);
        assertTrue(controller.isDataLoaded());
    }

    // ---------- unloadWagons ----------
    @Test
    @DisplayName("unloadWagons executes without error")
    void unloadWagons() {
        Box b1 = new Box("B1", "SKU-A", 5, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 10, 0));
        dataLoader.boxRepository.addUnassignedBox(b1);
        
        // Should not throw exception
        assertDoesNotThrow(() -> controller.unloadWagons());
    }

    @Test
    @DisplayName("unloadWagons assigns boxes to bays")
    void unloadWagonsAssignsBoxes() {
        Box b1 = new Box("B1", "SKU-A", 5, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 10, 0));
        dataLoader.boxRepository.addUnassignedBox(b1);
        
        controller.unloadWagons();
        
        assertNotNull(b1.getLocation());
    }

    // ---------- dispatchStock ----------
    @Test
    @DisplayName("dispatchStock dispatches boxes from warehouse")
    void dispatchStock() {
        // Add box to a bay
        Bay bay1 = dataLoader.bays.get(0);
        Box b1 = new Box("D1", "SKU-A", 5, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 10, 0));
        bay1.insertBox(b1);
        
        List<Box> dispatched = controller.dispatchStock("SKU-A", 5);
        
        assertEquals(1, dispatched.size());
        assertEquals("D1", dispatched.get(0).getBoxId());
    }

    @Test
    @DisplayName("dispatchStock returns empty list for unknown SKU")
    void dispatchStockUnknownSku() {
        List<Box> dispatched = controller.dispatchStock("UNKNOWN", 5);
        
        assertTrue(dispatched.isEmpty());
    }

    @Test
    @DisplayName("dispatchStock returns empty list for zero quantity")
    void dispatchStockZeroQuantity() {
        List<Box> dispatched = controller.dispatchStock("SKU-A", 0);
        
        assertTrue(dispatched.isEmpty());
    }

    @Test
    @DisplayName("dispatchStock returns empty list for negative quantity")
    void dispatchStockNegativeQuantity() {
        List<Box> dispatched = controller.dispatchStock("SKU-A", -1);
        
        assertTrue(dispatched.isEmpty());
    }

    @Test
    @DisplayName("dispatchStock handles partial dispatch")
    void dispatchStockPartial() {
        Bay bay1 = dataLoader.bays.get(0);
        Box b1 = new Box("D2", "SKU-A", 10, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 10, 0));
        bay1.insertBox(b1);
        
        List<Box> dispatched = controller.dispatchStock("SKU-A", 4);
        
        assertEquals(1, dispatched.size());
        assertTrue(dispatched.get(0).getBoxId().contains("PARTIAL"));
        assertEquals(4, dispatched.get(0).getQty());
    }

    // ---------- relocateBox ----------
    @Test
    @DisplayName("relocateBox moves box to new bay")
    void relocateBox() {
        Bay bay1 = dataLoader.bays.get(0);
        Bay bay2 = dataLoader.bays.get(1);
        
        Box b1 = new Box("R1", "SKU-A", 5, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 10, 0));
        bay1.insertBox(b1);
        
        boolean result = controller.relocateBox("R1", "WH1", 1, 2);
        
        assertTrue(result);
        assertFalse(bay1.hasSku("SKU-A"));
        assertTrue(bay2.hasSku("SKU-A"));
    }

    @Test
    @DisplayName("relocateBox returns false when box not found")
    void relocateBoxNotFound() {
        boolean result = controller.relocateBox("NONEXISTENT", "WH1", 1, 1);
        
        assertFalse(result);
    }

    @Test
    @DisplayName("relocateBox returns false when target bay doesn't exist")
    void relocateBoxTargetNotFound() {
        Bay bay1 = dataLoader.bays.get(0);
        Box b1 = new Box("R2", "SKU-A", 5, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 10, 0));
        bay1.insertBox(b1);
        
        boolean result = controller.relocateBox("R2", "WH99", 99, 99);
        
        assertFalse(result);
    }

    @Test
    @DisplayName("relocateBox fails when target bay is full")
    void relocateBoxTargetFull() {
        Bay bay1 = dataLoader.bays.get(0);
        Bay bay2 = dataLoader.bays.get(2); // capacity 3
        
        Box b1 = new Box("R3", "SKU-A", 5, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 10, 0));
        bay1.insertBox(b1);
        
        // Fill target bay
        for (int i = 0; i < 3; i++) {
            Box box = new Box("F" + i, "SKU-B", 1, 
                    LocalDate.of(2026, 1, 10), 
                    LocalDateTime.of(2025, 1, 1, i + 1, 0));
            bay2.insertBox(box);
        }
        
        boolean result = controller.relocateBox("R3", "WH1", 2, 1);
        
        assertFalse(result);
    }

    @Test
    @DisplayName("relocateBox fails when target bay has different SKU")
    void relocateBoxWrongSku() {
        Bay bay1 = dataLoader.bays.get(0);
        Bay bay2 = dataLoader.bays.get(1);
        
        Box b1 = new Box("R4", "SKU-A", 5, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 10, 0));
        bay1.insertBox(b1);
        
        Box b2 = new Box("R5", "SKU-B", 3, 
                LocalDate.of(2026, 1, 15), 
                LocalDateTime.of(2025, 1, 1, 11, 0));
        bay2.insertBox(b2);
        
        boolean result = controller.relocateBox("R4", "WH1", 1, 2);
        
        assertFalse(result);
    }

    // ---------- getAvailableSkus ----------
    @Test
    @DisplayName("getAvailableSkus returns all SKUs")
    void getAvailableSkus() {
        List<String> skus = controller.getAvailableSkus();
        
        assertEquals(2, skus.size());
        assertTrue(skus.contains("SKU-A"));
        assertTrue(skus.contains("SKU-B"));
    }

    @Test
    @DisplayName("getAvailableSkus returns empty list when no items")
    void getAvailableSkusEmpty() {
        DataLoader emptyLoader = new DataLoader();
        WagonsUnloadingController emptyController = new WagonsUnloadingController(emptyLoader);
        
        List<String> skus = emptyController.getAvailableSkus();
        
        assertTrue(skus.isEmpty());
    }

    // ---------- isDataLoaded ----------
    @Test
    @DisplayName("isDataLoaded returns true when data is loaded")
    void isDataLoaded() {
        assertTrue(controller.isDataLoaded());
    }

    // ---------- getWarehouseManager ----------
    @Test
    @DisplayName("getWarehouseManager returns warehouse manager")
    void getWarehouseManager() {
        WarehouseManager manager = controller.getWarehouseManager();
        
        assertNotNull(manager);
        assertEquals(dataLoader.items, manager.getItems());
        assertEquals(dataLoader.bays, manager.getBays());
    }

    // ---------- getBoxRepository ----------
    @Test
    @DisplayName("getBoxRepository returns box repository")
    void getBoxRepository() {
        BoxRepository repository = controller.getBoxRepository();
        
        assertNotNull(repository);
        assertSame(dataLoader.boxRepository, repository);
    }

    // ---------- Edge Cases ----------
    @Test
    @DisplayName("dispatchStock with multiple boxes dispatches in order")
    void dispatchStockMultipleBoxes() {
        Bay bay1 = dataLoader.bays.get(0);
        Box b1 = new Box("M1", "SKU-A", 5, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 10, 0));
        Box b2 = new Box("M2", "SKU-A", 3, 
                LocalDate.of(2026, 1, 5), 
                LocalDateTime.of(2025, 1, 1, 8, 0));
        
        bay1.insertBox(b1);
        bay1.insertBox(b2);
        
        List<Box> dispatched = controller.dispatchStock("SKU-A", 6);
        
        assertEquals(2, dispatched.size());
        // M2 should come first (earlier expiry)
        assertTrue(dispatched.get(0).getBoxId().contains("M2"));
    }

    @Test
    @DisplayName("relocateBox succeeds to bay with same SKU")
    void relocateBoxSameSku() {
        Bay bay1 = dataLoader.bays.get(0);
        Bay bay2 = dataLoader.bays.get(1);
        
        Box b1 = new Box("R6", "SKU-A", 5, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 10, 0));
        Box b2 = new Box("R7", "SKU-A", 3, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 11, 0));
        
        bay1.insertBox(b1);
        bay2.insertBox(b2);
        
        boolean result = controller.relocateBox("R6", "WH1", 1, 2);
        
        assertTrue(result);
        assertEquals(2, bay2.totalBoxes());
    }

    @Test
    @DisplayName("dispatchStock with insufficient quantity dispatches available")
    void dispatchStockInsufficient() {
        Bay bay1 = dataLoader.bays.get(0);
        Box b1 = new Box("I1", "SKU-A", 3, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 10, 0));
        bay1.insertBox(b1);
        
        List<Box> dispatched = controller.dispatchStock("SKU-A", 10);
        
        assertEquals(1, dispatched.size());
        assertEquals("I1", dispatched.get(0).getBoxId());
    }

    private static void setDataLoaded(WagonsUnloadingController c, boolean value) {
        try {
            Field f = WagonsUnloadingController.class.getDeclaredField("dataLoaded");
            f.setAccessible(true);
            f.set(c, value);
        } catch (Exception e) {
            fail("Failed to set dataLoaded via reflection: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("unloadWagons: early return when data not loaded")
    void unloadWagonsWhenDataNotLoaded() {
        setDataLoaded(controller, false);
        assertDoesNotThrow(() -> controller.unloadWagons());
        // nada a verificar além de não lançar exceção (apenas imprime)
    }

    @Test
    @DisplayName("dispatchStock: returns empty list when data not loaded")
    void dispatchStockWhenDataNotLoaded() {
        setDataLoaded(controller, false);
        assertTrue(controller.dispatchStock("SKU-A", 5).isEmpty());
    }

    @Test
    @DisplayName("relocateBox: returns false when data not loaded")
    void relocateBoxWhenDataNotLoaded() {
        setDataLoaded(controller, false);
        assertFalse(controller.relocateBox("ANY", "WH1", 1, 1));
    }

    @Test
    @DisplayName("getAvailableSkus: returns empty list when data not loaded")
    void getAvailableSkusWhenDataNotLoaded() {
        setDataLoaded(controller, false);
        assertTrue(controller.getAvailableSkus().isEmpty());
    }
}