// java
package Domain.ENUM;

import Domain.QuarantineInspection;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class QuarantineInspectionTest {

    @Test
    void constructorWithParametersSetsFields() {
        LocalDateTime ts = LocalDateTime.of(2025, 9, 22, 14, 32);
        // obtém a constante do enum dinamicamente (falha com mensagem clara se não existir)
        QAAction restock = assertDoesNotThrow(() -> QAAction.valueOf("RESTOCKED"),
                "Enum QAAction must define a constant named RESTOCKED");
        QuarantineInspection qi = new QuarantineInspection("INS-1", "R105", ts, restock);

        assertEquals("INS-1", qi.getInspectionId());
        assertEquals("R105", qi.getReturnId());
        assertEquals(ts, qi.getInspectedAt());
        assertEquals(restock, qi.getAction());
    }

    @Test
    void emptyConstructorAndSettersGettersWork() {
        QuarantineInspection qi = new QuarantineInspection();
        LocalDateTime now = LocalDateTime.now();

        QAAction discarded = assertDoesNotThrow(() -> QAAction.valueOf("DISCARDED"),
                "Enum QAAction must define a constant named DISCARDED");

        qi.setInspectionId("INS-2");
        qi.setReturnId("R106");
        qi.setInspectedAt(now);
        qi.setAction(discarded);

        assertEquals("INS-2", qi.getInspectionId());
        assertEquals("R106", qi.getReturnId());
        assertEquals(now, qi.getInspectedAt());
        assertEquals(discarded, qi.getAction());
    }

    @Test
    void toStringContainsKeyFields() {
        LocalDateTime ts = LocalDateTime.of(2025, 9, 22, 14, 40);
        QAAction discarded = assertDoesNotThrow(() -> QAAction.valueOf("DISCARDED"),
                "Enum QAAction must define a constant named DISCARDED");

        QuarantineInspection qi = new QuarantineInspection("INS-3", "R107", ts, discarded);

        String s = qi.toString();
        assertTrue(s.contains("INS-3"));
        assertTrue(s.contains("R107"));
        assertTrue(s.contains("DISCARDED") || s.toLowerCase().contains("discard"));
    }
}
