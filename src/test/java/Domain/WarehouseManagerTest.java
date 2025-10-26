package Domain;

import Repository.BoxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class WarehouseManagerTest {

    private Map<String, Item> items;
    private Map<String, List<Box>> stock;
    private List<Bay> bays;
    private BoxRepository boxRepository;
    private WarehouseManager warehouseManager;
    private WarehouseManager warehouseManagerWithoutRepo;

    @BeforeEach
    void setUp() {
        // Initialize items
        items = new HashMap<>();
        items.put("SKU-A", new Item("SKU-A", "Product A", "Category1", "unit", 1.0, 1.0));
        items.put("SKU-B", new Item("SKU-B", "Product B", "Category2", "unit", 2.0, 2.0));

        // Initialize stock
        stock = new HashMap<>();
        
        // Initialize bays
        bays = new ArrayList<>();
        bays.add(new Bay(new BayId("WH1", 1, 1), 5));
        bays.add(new Bay(new BayId("WH1", 1, 2), 5));
        bays.add(new Bay(new BayId("WH1", 2, 1), 3));

        // Initialize BoxRepository
        boxRepository = new BoxRepository();

        // Create managers
        warehouseManager = new WarehouseManager(items, stock, bays, boxRepository);
        warehouseManagerWithoutRepo = new WarehouseManager(items, stock, bays);
    }

    // ---------- unloadWagons ----------
    @Test
    @DisplayName("unloadWagons with BoxRepository assigns boxes to bays")
    void unloadWagonsWithRepository() {
        // Add unassigned boxes
        Box b1 = new Box("B1", "SKU-A", 5, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 10, 0));
        Box b2 = new Box("B2", "SKU-A", 3, 
                LocalDate.of(2026, 1, 5), 
                LocalDateTime.of(2025, 1, 1, 8, 0));
        
        boxRepository.addUnassignedBox(b1);
        boxRepository.addUnassignedBox(b2);

        // Unload wagons
        warehouseManager.unloadWagons();

        // Check boxes are assigned
        assertNotNull(b1.getLocation());
        assertNotNull(b2.getLocation());
        assertEquals(b1.getLocation(), b2.getLocation()); // Same bay (dedicated)
    }

    @Test
    @DisplayName("unloadWagons without BoxRepository uses stock")
    void unloadWagonsWithoutRepository() {
        Box b1 = new Box("B3", "SKU-B", 4, 
                LocalDate.of(2026, 1, 15), 
                LocalDateTime.of(2025, 1, 1, 12, 0));
        
        stock.put("SKU-B", new ArrayList<>(Collections.singletonList(b1)));

        // Unload wagons
        warehouseManagerWithoutRepo.unloadWagons();

        // Check box is assigned to a bay (insertBox doesn't set location without repository)
        boolean foundInBay = false;
        for (Bay bay : bays) {
            if (bay.hasSku("SKU-B")) {
                foundInBay = true;
                break;
            }
        }
        assertTrue(foundInBay);
    }

    @Test
    @DisplayName("unloadWagons assigns to dedicated bay if available")
    void unloadWagonsAssignsToDedicatedBay() {
        Bay bay1 = bays.get(0);
        Bay bay2 = bays.get(1);
        
        // First box assigns bay1 to SKU-A
        Box b1 = new Box("B4", "SKU-A", 2, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 10, 0));
        boxRepository.addUnassignedBox(b1);
        warehouseManager.unloadWagons();

        // Second box should go to same bay
        Box b2 = new Box("B5", "SKU-A", 3, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 11, 0));
        boxRepository.addUnassignedBox(b2);
        warehouseManager.unloadWagons();

        assertEquals(b1.getLocation(), b2.getLocation());
        assertEquals(2, bay1.totalBoxes());
    }

    @Test
    @DisplayName("unloadWagons finds new empty bay when dedicated bay is full")
    void unloadWagonsFindsNewBayWhenFull() {
        Bay bay1 = bays.get(0);
        
        // Fill bay1 with boxes
        for (int i = 0; i < 5; i++) {
            Box box = new Box("B" + i, "SKU-A", 1, 
                    LocalDate.of(2026, 1, 10), 
                    LocalDateTime.of(2025, 1, 1, i + 1, 0));
            boxRepository.addUnassignedBox(box);
        }
        warehouseManager.unloadWagons();

        // Next box should go to different bay
        Box b6 = new Box("B6", "SKU-A", 1, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 12, 0));
        boxRepository.addUnassignedBox(b6);
        warehouseManager.unloadWagons();

        assertEquals(5, bay1.totalBoxes());
        assertNotEquals(bay1.getId(), b6.getLocation());
    }

    // ---------- insertReturnedBox ----------
    @Test
    @DisplayName("insertReturnedBox adds box to stock and assigns to bay")
    void insertReturnedBox() {
        warehouseManagerWithoutRepo.insertReturnedBox(
            "RB1", "SKU-A", 5, 
            LocalDate.of(2026, 1, 15), 
            LocalDateTime.of(2025, 1, 2, 10, 0));

        // Check stock contains the box
        assertTrue(stock.containsKey("SKU-A"));
        assertTrue(stock.get("SKU-A").stream()
                .anyMatch(b -> b.getBoxId().equals("RB1")));
    }

    @Test
    @DisplayName("insertReturnedBox assigns box to a bay")
    void insertReturnedBoxAssignsToBay() {
        Box box = new Box("RB2", "SKU-B", 3, 
                LocalDate.of(2026, 1, 20), 
                LocalDateTime.of(2025, 1, 2, 11, 0));
        
        warehouseManagerWithoutRepo.insertReturnedBox(
            box.getBoxId(), box.getSku(), box.getQty(), 
            box.getExpiryDate(), box.getReceivedAt());

        // Find the box in the bay
        boolean found = false;
        for (Bay bay : bays) {
            if (bay.hasSku("SKU-B")) {
                Collection<Box> bayBoxes = bay.getAllBoxes();
                found = bayBoxes.stream().anyMatch(b -> b.getBoxId().equals("RB2"));
                break;
            }
        }
        assertTrue(found);
    }

    // ---------- dispatchStock ----------
    @Test
    @DisplayName("dispatchStock dispatches correct quantity")
    void dispatchStock() {
        Bay bay1 = bays.get(0);
        
        // Add boxes to bay
        Box b1 = new Box("D1", "SKU-A", 5, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 10, 0));
        Box b2 = new Box("D2", "SKU-A", 3, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 11, 0));
        
        bay1.insertBox(b1);
        bay1.insertBox(b2);

        List<Box> dispatched = warehouseManager.dispatchStock("SKU-A", 6);

        assertEquals(2, dispatched.size());
        assertEquals("D1", dispatched.get(0).getBoxId());
        // Second box will be partial because D1 has 5 and we need 6 total, so D2 gives 1 unit
        assertTrue(dispatched.get(1).getBoxId().contains("D2"));
        assertEquals(1, dispatched.get(1).getQty());
    }

    @Test
    @DisplayName("dispatchStock creates partial box when needed")
    void dispatchStockPartialBox() {
        Bay bay1 = bays.get(0);
        
        Box b1 = new Box("D3", "SKU-A", 10, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 10, 0));
        bay1.insertBox(b1);

        List<Box> dispatched = warehouseManager.dispatchStock("SKU-A", 4);

        assertEquals(1, dispatched.size());
        assertTrue(dispatched.get(0).getBoxId().contains("PARTIAL"));
        assertEquals(4, dispatched.get(0).getQty());
        assertEquals(6, b1.getQty()); // Original box reduced
    }

    @Test
    @DisplayName("dispatchStock continues to next bay when current is empty")
    void dispatchStockContinuesToNextBay() {
        Bay bay1 = bays.get(0);
        Bay bay2 = bays.get(1);
        
        Box b1 = new Box("D4", "SKU-A", 2, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 10, 0));
        Box b2 = new Box("D5", "SKU-A", 3, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 11, 0));
        
        bay1.insertBox(b1);
        bay2.insertBox(b2);

        List<Box> dispatched = warehouseManager.dispatchStock("SKU-A", 4);

        assertEquals(2, dispatched.size());
    }

    @Test
    @DisplayName("dispatchStock returns empty list when no stock available")
    void dispatchStockNoStock() {
        List<Box> dispatched = warehouseManager.dispatchStock("SKU-C", 10);
        assertTrue(dispatched.isEmpty());
    }

    // ---------- relocateBox ----------
    @Test
    @DisplayName("relocateBox moves box to new bay")
    void relocateBox() {
        Bay bay1 = bays.get(0);
        Bay bay2 = bays.get(1);
        
        Box b1 = new Box("R1", "SKU-A", 5, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 10, 0));
        bay1.insertBox(b1);

        boolean result = warehouseManager.relocateBox("R1", bay2.getId());

        assertTrue(result);
        assertFalse(bay1.hasSku("SKU-A"));
        assertTrue(bay2.hasSku("SKU-A"));
        // With insertBox (without repository), location may not be set
        // Just verify the box moved
        assertTrue(bay2.getAllBoxes().stream().anyMatch(b -> b.getBoxId().equals("R1")));
    }

    @Test
    @DisplayName("relocateBox fails when box not found")
    void relocateBoxNotFound() {
        boolean result = warehouseManager.relocateBox("NONEXISTENT", new BayId("WH1", 1, 1));
        assertFalse(result);
    }

    @Test
    @DisplayName("relocateBox fails when target bay not found")
    void relocateBoxTargetNotFound() {
        Bay bay1 = bays.get(0);
        
        Box b1 = new Box("R2", "SKU-A", 5, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 10, 0));
        bay1.insertBox(b1);

        boolean result = warehouseManager.relocateBox("R2", new BayId("WH2", 99, 99));
        assertFalse(result);
    }

    @Test
    @DisplayName("relocateBox fails when target bay is full")
    void relocateBoxTargetFull() {
        Bay bay1 = bays.get(0);
        Bay bay2 = bays.get(2); // capacity 3
        
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

        boolean result = warehouseManager.relocateBox("R3", bay2.getId());
        assertFalse(result);
    }

    @Test
    @DisplayName("relocateBox fails when target bay dedicated to different SKU")
    void relocateBoxWrongSku() {
        Bay bay1 = bays.get(0);
        Bay bay2 = bays.get(1);
        
        Box b1 = new Box("R4", "SKU-A", 5, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 10, 0));
        bay1.insertBox(b1);

        Box b2 = new Box("R5", "SKU-B", 3, 
                LocalDate.of(2026, 1, 15), 
                LocalDateTime.of(2025, 1, 1, 11, 0));
        bay2.insertBox(b2);

        boolean result = warehouseManager.relocateBox("R4", bay2.getId());
        assertFalse(result);
    }

    @Test
    @DisplayName("relocateBox succeeds when target bay has same SKU")
    void relocateBoxSameSku() {
        Bay bay1 = bays.get(0);
        Bay bay2 = bays.get(1);
        
        Box b1 = new Box("R6", "SKU-A", 5, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 10, 0));
        Box b2 = new Box("R7", "SKU-A", 3, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 11, 0));
        
        bay1.insertBox(b1);
        bay2.insertBox(b2);

        boolean result = warehouseManager.relocateBox("R6", bay2.getId());
        assertTrue(result);
        assertEquals(2, bay2.totalBoxes());
    }

    // ---------- Getters ----------
    @Test
    @DisplayName("getItems returns items map")
    void getItems() {
        Map<String, Item> result = warehouseManager.getItems();
        assertSame(items, result);
        assertTrue(result.containsKey("SKU-A"));
        assertTrue(result.containsKey("SKU-B"));
    }

    @Test
    @DisplayName("getStock returns stock map")
    void getStock() {
        Map<String, List<Box>> result = warehouseManager.getStock();
        assertSame(stock, result);
    }

    @Test
    @DisplayName("getBays returns bays list")
    void getBays() {
        List<Bay> result = warehouseManager.getBays();
        assertSame(bays, result);
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("getBayMap returns bay map")
    void getBayMap() {
        Map<BayId, Bay> result = warehouseManager.getBayMap();
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.containsKey(bays.get(0).getId()));
        assertTrue(result.containsKey(bays.get(1).getId()));
        assertTrue(result.containsKey(bays.get(2).getId()));
    }

    // ---------- Edge Cases ----------
    @Test
    @DisplayName("unloadWagons with no boxes")
    void unloadWagonsNoBoxes() {
        warehouseManager.unloadWagons();
        // Should not throw exception
    }

    @Test
    @DisplayName("dispatchStock with zero quantity")
    void dispatchStockZeroQuantity() {
        List<Box> dispatched = warehouseManager.dispatchStock("SKU-A", 0);
        assertTrue(dispatched.isEmpty());
    }

    @Test
    @DisplayName("dispatchStock with exact quantity available")
    void dispatchStockExactQuantity() {
        Bay bay1 = bays.get(0);
        
        Box b1 = new Box("E1", "SKU-A", 5, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 10, 0));
        bay1.insertBox(b1);

        List<Box> dispatched = warehouseManager.dispatchStock("SKU-A", 5);

        assertEquals(1, dispatched.size());
        assertEquals(0, bay1.totalBoxes());
    }

    @Test
    @DisplayName("relocateBox with same source and target")
    void relocateBoxSameBay() {
        Bay bay1 = bays.get(0);
        
        Box b1 = new Box("S1", "SKU-A", 5, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 10, 0));
        bay1.insertBox(b1);

        boolean result = warehouseManager.relocateBox("S1", bay1.getId());
        
        // Should succeed (no-op or re-insert)
        assertTrue(result);
        assertTrue(bay1.hasSku("SKU-A"));
    }
}