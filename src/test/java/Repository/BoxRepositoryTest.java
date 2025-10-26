package Repository;

import Domain.BayId;
import Domain.Box;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class BoxRepositoryTest {

    private BoxRepository repository;
    private Box box1, box2, box3, box4;

    @BeforeEach
    void setUp() {
        repository = new BoxRepository();
        
        box1 = new Box("B1", "SKU-A", 10, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 10, 0));
        box2 = new Box("B2", "SKU-A", 5, 
                LocalDate.of(2026, 1, 5), 
                LocalDateTime.of(2025, 1, 1, 8, 0));
        box3 = new Box("B3", "SKU-B", 15, 
                LocalDate.of(2026, 1, 15), 
                LocalDateTime.of(2025, 1, 1, 12, 0));
        box4 = new Box("B4", "SKU-B", 7, 
                LocalDate.of(2026, 1, 20), 
                LocalDateTime.of(2025, 1, 1, 14, 0));
    }

    // ---------- addUnassignedBox ----------
    @Test
    @DisplayName("addUnassignedBox adds box without location")
    void addUnassignedBox() {
        repository.addUnassignedBox(box1);
        
        List<Box> unassigned = repository.getUnassignedBoxes("SKU-A");
        assertEquals(1, unassigned.size());
        assertEquals("B1", unassigned.get(0).getBoxId());
        assertNull(unassigned.get(0).getLocation());
    }

    @Test
    @DisplayName("addUnassignedBox throws exception if box has location")
    void addUnassignedBoxWithLocation() {
        BayId bayId = new BayId("WH1", 1, 1);
        Box box = new Box("B5", "SKU-A", 5, 
                LocalDate.of(2026, 1, 10), 
                LocalDateTime.of(2025, 1, 1, 10, 0), bayId);
        
        assertThrows(IllegalArgumentException.class, 
                () -> repository.addUnassignedBox(box));
    }

    @Test
    @DisplayName("addUnassignedBox groups boxes by SKU")
    void addUnassignedBoxGroupsBySku() {
        repository.addUnassignedBox(box1);
        repository.addUnassignedBox(box2);
        repository.addUnassignedBox(box3);
        
        assertEquals(2, repository.getUnassignedBoxes("SKU-A").size());
        assertEquals(1, repository.getUnassignedBoxes("SKU-B").size());
    }

    // ---------- assignBoxToBay ----------
    @Test
    @DisplayName("assignBoxToBay moves box from unassigned to assigned")
    void assignBoxToBay() {
        BayId bayId = new BayId("WH1", 1, 1);
        repository.addUnassignedBox(box1);
        
        repository.assignBoxToBay(box1, bayId);
        
        assertEquals(0, repository.getUnassignedBoxes("SKU-A").size());
        assertEquals(1, repository.getAssignedBoxes("SKU-A").size());
        assertEquals(bayId, box1.getLocation());
    }

    @Test
    @DisplayName("assignBoxToBay throws exception if box already has location")
    void assignBoxToBayWithExistingLocation() {
        BayId bayId1 = new BayId("WH1", 1, 1);
        BayId bayId2 = new BayId("WH1", 1, 2);
        repository.addUnassignedBox(box1);
        repository.assignBoxToBay(box1, bayId1);
        
        assertThrows(IllegalArgumentException.class, 
                () -> repository.assignBoxToBay(box1, bayId2));
    }

    @Test
    @DisplayName("assignBoxToBay creates new assigned SKU list if needed")
    void assignBoxToBayNewSku() {
        BayId bayId = new BayId("WH1", 1, 1);
        repository.addUnassignedBox(box3);
        
        repository.assignBoxToBay(box3, bayId);
        
        assertEquals(1, repository.getAssignedBoxes("SKU-B").size());
        assertTrue(repository.getAssignedSkus().contains("SKU-B"));
    }

    // ---------- removeAssignedBox ----------
    @Test
    @DisplayName("removeAssignedBox removes box from assigned")
    void removeAssignedBox() {
        BayId bayId = new BayId("WH1", 1, 1);
        repository.addUnassignedBox(box1);
        repository.assignBoxToBay(box1, bayId);
        
        repository.removeAssignedBox(box1);
        
        assertEquals(0, repository.getAssignedBoxes("SKU-A").size());
        assertNull(repository.getBoxById("B1"));
    }

    @Test
    @DisplayName("removeAssignedBox throws exception if box not assigned")
    void removeAssignedBoxNotAssigned() {
        assertThrows(IllegalArgumentException.class, 
                () -> repository.removeAssignedBox(box1));
    }

    @Test
    @DisplayName("removeAssignedBox removes SKU from assigned if empty")
    void removeAssignedBoxRemovesSkuWhenEmpty() {
        BayId bayId = new BayId("WH1", 1, 1);
        repository.addUnassignedBox(box1);
        repository.assignBoxToBay(box1, bayId);
        
        repository.removeAssignedBox(box1);
        
        assertFalse(repository.getAssignedSkus().contains("SKU-A"));
    }

    // ---------- getUnassignedBoxes ----------
    @Test
    @DisplayName("getUnassignedBoxes returns empty list for unknown SKU")
    void getUnassignedBoxesUnknownSku() {
        List<Box> boxes = repository.getUnassignedBoxes("UNKNOWN");
        assertTrue(boxes.isEmpty());
    }

    @Test
    @DisplayName("getUnassignedBoxes returns all boxes for SKU")
    void getUnassignedBoxesReturnsAll() {
        repository.addUnassignedBox(box1);
        repository.addUnassignedBox(box2);
        
        List<Box> boxes = repository.getUnassignedBoxes("SKU-A");
        assertEquals(2, boxes.size());
    }

    // ---------- getAssignedBoxes ----------
    @Test
    @DisplayName("getAssignedBoxes returns empty list for unknown SKU")
    void getAssignedBoxesUnknownSku() {
        List<Box> boxes = repository.getAssignedBoxes("UNKNOWN");
        assertTrue(boxes.isEmpty());
    }

    @Test
    @DisplayName("getAssignedBoxes returns all assigned boxes for SKU")
    void getAssignedBoxesReturnsAll() {
        BayId bayId = new BayId("WH1", 1, 1);
        repository.addUnassignedBox(box1);
        repository.addUnassignedBox(box2);
        repository.assignBoxToBay(box1, bayId);
        repository.assignBoxToBay(box2, bayId);
        
        List<Box> boxes = repository.getAssignedBoxes("SKU-A");
        assertEquals(2, boxes.size());
    }

    // ---------- getAllUnassignedBoxes ----------
    @Test
    @DisplayName("getAllUnassignedBoxes returns map with all unassigned")
    void getAllUnassignedBoxes() {
        repository.addUnassignedBox(box1);
        repository.addUnassignedBox(box2);
        repository.addUnassignedBox(box3);
        
        Map<String, List<Box>> all = repository.getAllUnassignedBoxes();
        
        assertEquals(2, all.size());
        assertEquals(2, all.get("SKU-A").size());
        assertEquals(1, all.get("SKU-B").size());
    }

    @Test
    @DisplayName("getAllUnassignedBoxes returns empty map when none")
    void getAllUnassignedBoxesEmpty() {
        Map<String, List<Box>> all = repository.getAllUnassignedBoxes();
        assertTrue(all.isEmpty());
    }

    // ---------- getAllAssignedBoxes ----------
    @Test
    @DisplayName("getAllAssignedBoxes returns map with all assigned")
    void getAllAssignedBoxes() {
        BayId bayId = new BayId("WH1", 1, 1);
        repository.addUnassignedBox(box1);
        repository.addUnassignedBox(box3);
        repository.assignBoxToBay(box1, bayId);
        repository.assignBoxToBay(box3, bayId);
        
        Map<String, List<Box>> all = repository.getAllAssignedBoxes();
        
        assertEquals(2, all.size());
        assertEquals(1, all.get("SKU-A").size());
        assertEquals(1, all.get("SKU-B").size());
    }

    // ---------- getBoxById ----------
    @Test
    @DisplayName("getBoxById returns box by ID")
    void getBoxById() {
        repository.addUnassignedBox(box1);
        
        Box found = repository.getBoxById("B1");
        assertNotNull(found);
        assertEquals("B1", found.getBoxId());
    }

    @Test
    @DisplayName("getBoxById returns null for unknown ID")
    void getBoxByIdUnknown() {
        Box found = repository.getBoxById("UNKNOWN");
        assertNull(found);
    }

    // ---------- getAllBoxesForSku ----------
    @Test
    @DisplayName("getAllBoxesForSku returns both assigned and unassigned")
    void getAllBoxesForSku() {
        BayId bayId = new BayId("WH1", 1, 1);
        repository.addUnassignedBox(box1);
        repository.addUnassignedBox(box2);
        repository.assignBoxToBay(box1, bayId);
        
        List<Box> all = repository.getAllBoxesForSku("SKU-A");
        
        assertEquals(2, all.size());
    }

    // ---------- getUnassignedBoxesSorted ----------
    @Test
    @DisplayName("getUnassignedBoxesSorted returns sorted by FEFO")
    void getUnassignedBoxesSorted() {
        repository.addUnassignedBox(box1); // expiry 2026-01-10, received 10:00
        repository.addUnassignedBox(box2); // expiry 2026-01-05, received 08:00
        
        List<Box> sorted = repository.getUnassignedBoxesSorted("SKU-A");
        
        assertEquals("B2", sorted.get(0).getBoxId()); // Earlier expiry
        assertEquals("B1", sorted.get(1).getBoxId());
    }

    // ---------- getAssignedBoxesSorted ----------
    @Test
    @DisplayName("getAssignedBoxesSorted returns sorted by FEFO")
    void getAssignedBoxesSorted() {
        BayId bayId = new BayId("WH1", 1, 1);
        repository.addUnassignedBox(box3);
        repository.addUnassignedBox(box4);
        repository.assignBoxToBay(box3, bayId);
        repository.assignBoxToBay(box4, bayId);
        
        List<Box> sorted = repository.getAssignedBoxesSorted("SKU-B");
        
        assertEquals("B3", sorted.get(0).getBoxId()); // Earlier expiry
        assertEquals("B4", sorted.get(1).getBoxId());
    }

    // ---------- getStats ----------
    @Test
    @DisplayName("getStats returns correct counts")
    void getStats() {
        BayId bayId = new BayId("WH1", 1, 1);
        repository.addUnassignedBox(box1);
        repository.addUnassignedBox(box2);
        repository.addUnassignedBox(box3);
        repository.assignBoxToBay(box1, bayId);
        
        BoxRepository.RepositoryStats stats = repository.getStats();
        
        assertEquals(2, stats.totalUnassigned);
        assertEquals(1, stats.totalAssigned);
        assertEquals(2, stats.unassignedSkus);
        assertEquals(1, stats.assignedSkus);
    }

    @Test
    @DisplayName("getStats returns zeros when empty")
    void getStatsEmpty() {
        BoxRepository.RepositoryStats stats = repository.getStats();
        
        assertEquals(0, stats.totalUnassigned);
        assertEquals(0, stats.totalAssigned);
        assertEquals(0, stats.unassignedSkus);
        assertEquals(0, stats.assignedSkus);
    }

    // ---------- getUnassignedSkus ----------
    @Test
    @DisplayName("getUnassignedSkus returns set of SKUs")
    void getUnassignedSkus() {
        repository.addUnassignedBox(box1);
        repository.addUnassignedBox(box3);
        
        Set<String> skus = repository.getUnassignedSkus();
        
        assertEquals(2, skus.size());
        assertTrue(skus.contains("SKU-A"));
        assertTrue(skus.contains("SKU-B"));
    }

    // ---------- getAssignedSkus ----------
    @Test
    @DisplayName("getAssignedSkus returns set of SKUs")
    void getAssignedSkus() {
        BayId bayId = new BayId("WH1", 1, 1);
        repository.addUnassignedBox(box1);
        repository.addUnassignedBox(box3);
        repository.assignBoxToBay(box1, bayId);
        repository.assignBoxToBay(box3, bayId);
        
        Set<String> skus = repository.getAssignedSkus();
        
        assertEquals(2, skus.size());
        assertTrue(skus.contains("SKU-A"));
        assertTrue(skus.contains("SKU-B"));
    }

    // ---------- hasUnassignedBoxes ----------
    @Test
    @DisplayName("hasUnassignedBoxes returns false when empty")
    void hasUnassignedBoxesEmpty() {
        assertFalse(repository.hasUnassignedBoxes());
    }

    @Test
    @DisplayName("hasUnassignedBoxes returns true when present")
    void hasUnassignedBoxes() {
        repository.addUnassignedBox(box1);
        assertTrue(repository.hasUnassignedBoxes());
    }

    @Test
    @DisplayName("hasUnassignedBoxes(string) returns true for specific SKU")
    void testHasUnassignedBoxes() {
        repository.addUnassignedBox(box1);
        
        assertTrue(repository.hasUnassignedBoxes("SKU-A"));
        assertFalse(repository.hasUnassignedBoxes("SKU-Z"));
    }

    // ---------- getAllBoxes ----------
    @Test
    @DisplayName("getAllBoxes returns all boxes")
    void getAllBoxes() {
        BayId bayId = new BayId("WH1", 1, 1);
        repository.addUnassignedBox(box1);
        repository.addUnassignedBox(box2);
        repository.assignBoxToBay(box1, bayId);
        
        Collection<Box> all = repository.getAllBoxes();
        
        assertEquals(2, all.size());
    }

    // ---------- clear ----------
    @Test
    @DisplayName("clear removes all boxes")
    void clear() {
        BayId bayId = new BayId("WH1", 1, 1);
        repository.addUnassignedBox(box1);
        repository.assignBoxToBay(box1, bayId);
        
        repository.clear();
        
        assertEquals(0, repository.getAllBoxes().size());
        assertEquals(0, repository.getUnassignedBoxes("SKU-A").size());
        assertEquals(0, repository.getAssignedBoxes("SKU-A").size());
    }

    // ---------- printStatus ----------
    @Test
    @DisplayName("printStatus executes without exception")
    void printStatus() {
        repository.addUnassignedBox(box1);
        
        // Just verify it doesn't throw
        assertDoesNotThrow(() -> repository.printStatus());
    }

    // ---------- Edge Cases ----------
    @Test
    @DisplayName("Multiple boxes with same SKU maintain insertion order")
    void multipleBoxesSameSku() {
        repository.addUnassignedBox(box1);
        repository.addUnassignedBox(box2);
        
        List<Box> boxes = repository.getUnassignedBoxes("SKU-A");
        assertEquals(2, boxes.size());
        assertEquals("B1", boxes.get(0).getBoxId());
        assertEquals("B2", boxes.get(1).getBoxId());
    }

    @Test
    @DisplayName("Assign and then remove all boxes for a SKU")
    void assignAndRemoveAll() {
        BayId bayId = new BayId("WH1", 1, 1);
        repository.addUnassignedBox(box1);
        repository.assignBoxToBay(box1, bayId);
        repository.removeAssignedBox(box1);
        
        assertFalse(repository.hasUnassignedBoxes("SKU-A"));
        assertFalse(repository.getAssignedSkus().contains("SKU-A"));
    }

    @Test
    @DisplayName("Box can be retrieved after assignment")
    void boxRetrievableAfterAssignment() {
        BayId bayId = new BayId("WH1", 1, 1);
        repository.addUnassignedBox(box1);
        repository.assignBoxToBay(box1, bayId);
        
        Box found = repository.getBoxById("B1");
        assertNotNull(found);
        assertEquals(bayId, found.getLocation());
    }
}
