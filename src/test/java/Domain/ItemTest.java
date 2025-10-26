package Domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    @Test
    @DisplayName("getSku returns the correct SKU")
    void getSku() {
        Item item = new Item("SKU001", "Product A", "Category1", "unit", 1.5, 2.0);
        assertEquals("SKU001", item.getSku());
    }

    @Test
    @DisplayName("getName returns the correct name")
    void getName() {
        Item item = new Item("SKU002", "Product B", "Category2", "liter", 0.5, 1.2);
        assertEquals("Product B", item.getName());
    }

    @Test
    @DisplayName("setName updates the name")
    void setName() {
        Item item = new Item("SKU003", "Old Name", "Category1", "unit", 1.0, 1.5);
        item.setName("New Name");
        assertEquals("New Name", item.getName());
    }

    @Test
    @DisplayName("getCategory returns the correct category")
    void getCategory() {
        Item item = new Item("SKU004", "Product", "Electronics", "piece", 2.0, 3.0);
        assertEquals("Electronics", item.getCategory());
    }

    @Test
    @DisplayName("setCategory updates the category")
    void setCategory() {
        Item item = new Item("SKU005", "Product", "OldCategory", "unit", 1.0, 1.0);
        item.setCategory("NewCategory");
        assertEquals("NewCategory", item.getCategory());
    }

    @Test
    @DisplayName("getUnit returns the correct unit")
    void getUnit() {
        Item item = new Item("SKU006", "Product", "Cat", "kg", 1.0, 1.0);
        assertEquals("kg", item.getUnit());
    }

    @Test
    @DisplayName("setUnit updates the unit")
    void setUnit() {
        Item item = new Item("SKU007", "Product", "Cat", "unit", 1.0, 1.0);
        item.setUnit("liter");
        assertEquals("liter", item.getUnit());
    }

    @Test
    @DisplayName("name method works the same as getName")
    void name() {
        Item item = new Item("SKU008", "Test Product", "Cat", "unit", 1.0, 1.0);
        assertEquals("Test Product", item.getName());
    }

    @Test
    @DisplayName("getVolume returns the correct volume")
    void getVolume() {
        Item item = new Item("SKU009", "Product", "Cat", "unit", 1.75, 2.0);
        assertEquals(1.75, item.getVolume(), 0.001);
    }

    @Test
    @DisplayName("setVolume updates the volume")
    void setVolume() {
        Item item = new Item("SKU010", "Product", "Cat", "unit", 1.0, 1.0);
        item.setVolume(2.5);
        assertEquals(2.5, item.getVolume(), 0.001);
    }

    @Test
    @DisplayName("getWeight returns the correct weight")
    void getWeight() {
        Item item = new Item("SKU011", "Product", "Cat", "unit", 1.0, 3.5);
        assertEquals(3.5, item.getWeight(), 0.001);
    }

    @Test
    @DisplayName("setWeight updates the weight")
    void setWeight() {
        Item item = new Item("SKU012", "Product", "Cat", "unit", 1.0, 1.0);
        item.setWeight(4.5);
        assertEquals(4.5, item.getWeight(), 0.001);
    }

    @Test
    @DisplayName("setSku updates the SKU")
    void setSku() {
        Item item = new Item("SKU001", "Product", "Cat", "unit", 1.0, 1.0);
        item.setSku("NEW_SKU");
        assertEquals("NEW_SKU", item.getSku());
    }

    @Test
    @DisplayName("testEquals returns true for equal items")
    void testEquals() {
        Item item1 = new Item("SKU001", "Product", "Category", "unit", 1.0, 2.0);
        Item item2 = new Item("SKU001", "Different Product", "Different Category", "different", 5.0, 10.0);
        
        assertEquals(item1, item2);
        assertEquals(item2, item1);
        
        // Same reference
        assertEquals(item1, item1);
    }

    @Test
    @DisplayName("testEquals returns false for different SKUs")
    void testEqualsDifferentSku() {
        Item item1 = new Item("SKU001", "Product", "Cat", "unit", 1.0, 1.0);
        Item item2 = new Item("SKU002", "Product", "Cat", "unit", 1.0, 1.0);
        
        assertNotEquals(item1, item2);
    }

    @Test
    @DisplayName("testEquals returns false for null and different types")
    void testEqualsNullAndType() {
        Item item = new Item("SKU003", "Product", "Cat", "unit", 1.0, 1.0);
        
        assertNotEquals(item, null);
        assertNotEquals(item, "not an item");
        assertNotEquals(item, new Object());
    }

    @Test
    @DisplayName("testHashCode returns same value for equal items")
    void testHashCode() {
        Item item1 = new Item("SKU001", "Product A", "Category1", "unit1", 1.0, 2.0);
        Item item2 = new Item("SKU001", "Product B", "Category2", "unit2", 3.0, 4.0);
        
        assertEquals(item1.hashCode(), item2.hashCode());
    }

    @Test
    @DisplayName("testHashCode is consistent")
    void testHashCodeConsistent() {
        Item item = new Item("SKU005", "Product", "Cat", "unit", 1.0, 1.0);
        
        int hash1 = item.hashCode();
        int hash2 = item.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test
    @DisplayName("testToString includes SKU and name")
    void testToString() {
        Item item = new Item("SKU006", "Test Product", "Category", "unit", 1.0, 1.0);
        
        String str = item.toString();
        assertTrue(str.contains("SKU006"));
        assertTrue(str.contains("Test Product"));
        assertTrue(str.contains("Item{"));
    }

    @Test
    @DisplayName("Constructor sets all fields correctly")
    void constructor() {
        Item item = new Item("SKU007", "Name", "Category", "kg", 1.5, 2.5);
        
        assertEquals("SKU007", item.getSku());
        assertEquals("Name", item.getName());
        assertEquals("Category", item.getCategory());
        assertEquals("kg", item.getUnit());
        assertEquals(1.5, item.getVolume(), 0.001);
        assertEquals(2.5, item.getWeight(), 0.001);
    }

    @Test
    @DisplayName("Item with zero values is handled correctly")
    void zeroValues() {
        Item item = new Item("SKU008", "Product", "Cat", "unit", 0.0, 0.0);
        
        assertEquals(0.0, item.getVolume(), 0.001);
        assertEquals(0.0, item.getWeight(), 0.001);
    }

    @Test
    @DisplayName("Item with decimal values is handled correctly")
    void decimalValues() {
        Item item = new Item("SKU009", "Product", "Cat", "unit", 0.123, 4.567);
        
        assertEquals(0.123, item.getVolume(), 0.001);
        assertEquals(4.567, item.getWeight(), 0.001);
    }
}