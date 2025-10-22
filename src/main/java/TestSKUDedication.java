package Domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Test class to demonstrate the new SKU dedication policy
 */
public class TestSKUDedication {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        System.out.println("=== TESTING SKU DEDICATION POLICY ===\n");
        
        // Create test bays
        List<Bay> bays = Arrays.asList(
            new Bay(new BayId("W1", 1, 1), 3), // Bay 1-1 with capacity 3
            new Bay(new BayId("W1", 1, 2), 3), // Bay 1-2 with capacity 3
            new Bay(new BayId("W1", 1, 3), 3)  // Bay 1-3 with capacity 3
        );
        
        // Create test boxes
        List<Box> boxes = Arrays.asList(
            new Box("BOX001", "SKU001", 10, LocalDate.of(2025, 12, 31), LocalDateTime.now()),
            new Box("BOX002", "SKU001", 15, LocalDate.of(2025, 12, 30), LocalDateTime.now()),
            new Box("BOX003", "SKU002", 20, LocalDate.of(2025, 12, 29), LocalDateTime.now()),
            new Box("BOX004", "SKU001", 25, LocalDate.of(2025, 12, 28), LocalDateTime.now()),
            new Box("BOX005", "SKU002", 30, LocalDate.of(2025, 12, 27), LocalDateTime.now())
        );
        
        // Create warehouse manager
        Map<String, Item> items = new HashMap<>();
        Map<String, List<Box>> stock = new HashMap<>();
        stock.put("SKU001", new ArrayList<>());
        stock.put("SKU002", new ArrayList<>());
        
        WarehouseManager warehouseManager = new WarehouseManager(items, stock, bays);
        
        System.out.println("Initial state:");
        printBayStatus(bays);
        
        System.out.println("\n=== TESTING BOX ASSIGNMENT ===");
        
        // Test 1: Assign first SKU001 box (should go to first empty bay)
        System.out.println("\n1. Assigning BOX001 (SKU001) to first empty bay:");
        try {
            bays.get(0).insertBox(boxes.get(0));
            System.out.println("✓ BOX001 assigned to " + bays.get(0).getId());
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
        printBayStatus(bays);
        
        // Test 2: Assign second SKU001 box (should go to same bay)
        System.out.println("\n2. Assigning BOX002 (SKU001) to same dedicated bay:");
        try {
            bays.get(0).insertBox(boxes.get(1));
            System.out.println("✓ BOX002 assigned to " + bays.get(0).getId());
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
        printBayStatus(bays);
        
        // Test 3: Try to assign SKU002 to bay dedicated to SKU001 (should fail)
        System.out.println("\n3. Trying to assign BOX003 (SKU002) to bay dedicated to SKU001:");
        try {
            bays.get(0).insertBox(boxes.get(2));
            System.out.println("✓ BOX003 assigned to " + bays.get(0).getId());
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
        printBayStatus(bays);
        
        // Test 4: Assign SKU002 to next empty bay
        System.out.println("\n4. Assigning BOX003 (SKU002) to next empty bay:");
        try {
            bays.get(1).insertBox(boxes.get(2));
            System.out.println("✓ BOX003 assigned to " + bays.get(1).getId());
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
        printBayStatus(bays);
        
        // Test 5: Assign another SKU001 box (should go to first dedicated bay)
        System.out.println("\n5. Assigning BOX004 (SKU001) to dedicated bay:");
        try {
            bays.get(0).insertBox(boxes.get(3));
            System.out.println("✓ BOX004 assigned to " + bays.get(0).getId());
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
        printBayStatus(bays);
        
        // Test 6: Remove all boxes from first bay and test re-dedication
        System.out.println("\n6. Removing all boxes from first bay to test re-dedication:");
        bays.get(0).removeBox(boxes.get(0));
        bays.get(0).removeBox(boxes.get(1));
        bays.get(0).removeBox(boxes.get(3));
        printBayStatus(bays);
        
        // Test 7: Now assign SKU002 to the now-empty bay
        System.out.println("\n7. Assigning BOX005 (SKU002) to now-empty bay:");
        try {
            bays.get(0).insertBox(boxes.get(4));
            System.out.println("✓ BOX005 assigned to " + bays.get(0).getId());
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
        printBayStatus(bays);
        
        System.out.println("\n=== TEST COMPLETED ===");
    }
    
    private static void printBayStatus(List<Bay> bays) {
        for (Bay bay : bays) {
            System.out.println("  " + bay.toString());
        }
    }
}
