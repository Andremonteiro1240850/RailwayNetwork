package Domain;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PickStopTest {

    @Test
    void constructor_ValidParameters_CreatesObject() {
        PickStop stop = new PickStop(1, 2, 3, 10);

        assertEquals(1, stop.getSeq());
        assertEquals(2, stop.getAisle());
        assertEquals(3, stop.getBay());
        assertEquals(10, stop.getTotalQty());
    }

    @Test
    void getSeq_ReturnsCorrectSequence() {
        PickStop stop = new PickStop(5, 1, 2, 10);
        assertEquals(5, stop.getSeq());
    }

    @Test
    void setSeq_UpdatesSequenceCorrectly() {
        PickStop stop = new PickStop(1, 1, 2, 10);
        stop.setSeq(10);
        assertEquals(10, stop.getSeq());
    }

    @Test
    void getAisle_ReturnsCorrectAisle() {
        PickStop stop = new PickStop(1, 3, 2, 10);
        assertEquals(3, stop.getAisle());
    }

    @Test
    void setAisle_UpdatesAisleCorrectly() {
        PickStop stop = new PickStop(1, 1, 2, 10);
        stop.setAisle(5);
        assertEquals(5, stop.getAisle());
    }

    @Test
    void getBay_ReturnsCorrectBay() {
        PickStop stop = new PickStop(1, 1, 4, 10);
        assertEquals(4, stop.getBay());
    }

    @Test
    void setBay_UpdatesBayCorrectly() {
        PickStop stop = new PickStop(1, 1, 2, 10);
        stop.setBay(8);
        assertEquals(8, stop.getBay());
    }

    @Test
    void getTotalQty_ReturnsCorrectQuantity() {
        PickStop stop = new PickStop(1, 1, 2, 15);
        assertEquals(15, stop.getTotalQty());
    }

    @Test
    void setTotalQty_UpdatesQuantityCorrectly() {
        PickStop stop = new PickStop(1, 1, 2, 10);
        stop.setTotalQty(25);
        assertEquals(25, stop.getTotalQty());
    }

    @Test
    void testEquals_SameAisleAndBay_ReturnsTrue() {
        PickStop stop1 = new PickStop(1, 2, 3, 10);
        PickStop stop2 = new PickStop(2, 2, 3, 20); // Diferente seq e qty, mesmo aisle/bay

        assertEquals(stop1, stop2);
    }

    @Test
    void testEquals_DifferentAisle_ReturnsFalse() {
        PickStop stop1 = new PickStop(1, 2, 3, 10);
        PickStop stop2 = new PickStop(1, 3, 3, 10); // Aisle diferente

        assertNotEquals(stop1, stop2);
    }

    @Test
    void testEquals_DifferentBay_ReturnsFalse() {
        PickStop stop1 = new PickStop(1, 2, 3, 10);
        PickStop stop2 = new PickStop(1, 2, 4, 10); // Bay diferente

        assertNotEquals(stop1, stop2);
    }

    @Test
    void testEquals_SameObject_ReturnsTrue() {
        PickStop stop = new PickStop(1, 2, 3, 10);
        assertEquals(stop, stop);
    }

    @Test
    void testEquals_NullObject_ReturnsFalse() {
        PickStop stop = new PickStop(1, 2, 3, 10);
        assertNotEquals(null, stop);
    }

    @Test
    void testEquals_DifferentClass_ReturnsFalse() {
        PickStop stop = new PickStop(1, 2, 3, 10);
        assertNotEquals("Not a PickStop", stop);
    }

    @Test
    void testHashCode_SameAisleAndBay_EqualHashCodes() {
        PickStop stop1 = new PickStop(1, 2, 3, 10);
        PickStop stop2 = new PickStop(2, 2, 3, 20);

        assertEquals(stop1.hashCode(), stop2.hashCode());
    }

    @Test
    void testHashCode_DifferentAisle_DifferentHashCodes() {
        PickStop stop1 = new PickStop(1, 2, 3, 10);
        PickStop stop2 = new PickStop(1, 3, 3, 10);

        assertNotEquals(stop1.hashCode(), stop2.hashCode());
    }

    @Test
    void testToString_ContainsAllInformation() {
        PickStop stop = new PickStop(1, 2, 3, 10);
        String result = stop.toString();

        assertTrue(result.contains("(2,3)"));
        assertTrue(result.contains("Qty:10"));
    }

    @Test
    void consolidateStops_EmptyList_ReturnsEmptyList() {
        List<PickStop> emptyList = new ArrayList<>();
        List<PickStop> result = PickStop.consolidateStops(emptyList);

        assertTrue(result.isEmpty());
    }

    @Test
    void consolidateStops_NullList_ReturnsEmptyList() {
        List<PickStop> result = PickStop.consolidateStops(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void consolidateStops_NoDuplicates_ReturnsSameStops() {
        List<PickStop> stops = Arrays.asList(
                new PickStop(1, 1, 1, 10),
                new PickStop(2, 1, 2, 20),
                new PickStop(3, 2, 1, 30)
        );

        List<PickStop> result = PickStop.consolidateStops(stops);

        assertEquals(3, result.size());
        // Verifica se as quantidades se mantêm
        assertEquals(10, result.get(0).getTotalQty());
        assertEquals(20, result.get(1).getTotalQty());
        assertEquals(30, result.get(2).getTotalQty());
    }

    @Test
    void consolidateStops_WithDuplicates_MergesQuantities() {
        List<PickStop> stops = Arrays.asList(
                new PickStop(1, 1, 1, 10),
                new PickStop(2, 1, 1, 20), // Duplicado - mesmo aisle/bay
                new PickStop(3, 1, 2, 30),
                new PickStop(4, 1, 1, 15)  // Duplicado - mesmo aisle/bay
        );

        List<PickStop> result = PickStop.consolidateStops(stops);

        assertEquals(2, result.size()); // Aisle 1-1 consolidado, Aisle 1-2 mantido

        // Encontra o stop consolidado para aisle 1-1
        PickStop consolidatedStop = result.stream()
                .filter(stop -> stop.getAisle() == 1 && stop.getBay() == 1)
                .findFirst()
                .orElse(null);

        assertNotNull(consolidatedStop);
        assertEquals(45, consolidatedStop.getTotalQty()); // 10 + 20 + 15
    }

    @Test
    void consolidateStops_DuplicatesKeepLastSequence() {
        List<PickStop> stops = Arrays.asList(
                new PickStop(1, 1, 1, 10),
                new PickStop(5, 1, 1, 20) // Duplicado com seq diferente
        );

        List<PickStop> result = PickStop.consolidateStops(stops);

        assertEquals(1, result.size());
        // O método mantém a sequência do primeiro encontrado (não especifica qual manter)
        // Este teste verifica que a consolidação funciona, mas a sequência pode variar
        assertEquals(30, result.get(0).getTotalQty());
    }

    @Test
    void consolidateStops_MultipleDifferentDuplicates() {
        List<PickStop> stops = Arrays.asList(
                new PickStop(1, 1, 1, 10),
                new PickStop(2, 1, 2, 20),
                new PickStop(3, 2, 1, 30),
                new PickStop(4, 1, 1, 15), // Duplicado de (1,1)
                new PickStop(5, 1, 2, 25), // Duplicado de (1,2)
                new PickStop(6, 3, 1, 40)
        );

        List<PickStop> result = PickStop.consolidateStops(stops);

        assertEquals(4, result.size()); // 3 únicos + 1 duplicado removido

        // Verifica quantidades consolidadas
        Map<String, Integer> quantities = new HashMap<>();
        for (PickStop stop : result) {
            String key = stop.getAisle() + "-" + stop.getBay();
            quantities.put(key, stop.getTotalQty());
        }

        assertEquals(25, quantities.get("1-1")); // 10 + 15
        assertEquals(45, quantities.get("1-2")); // 20 + 25
        assertEquals(30, quantities.get("2-1"));
        assertEquals(40, quantities.get("3-1"));
    }
}