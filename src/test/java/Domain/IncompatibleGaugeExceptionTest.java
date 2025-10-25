package Domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the IncompatibleGaugeException class.
 */
class IncompatibleGaugeExceptionTest {

    private final double LOCO_GAUGE = 1668.0;
    private final double SEGMENT_GAUGE = 1435.0;

    // --- Constructor Tests ---

    @Test
    void testConstructor_DefaultMessage() {
        IncompatibleGaugeException exception =
                new IncompatibleGaugeException(LOCO_GAUGE, SEGMENT_GAUGE);

        String expectedMessage =
                String.format("Incompatible gauge: Locomotive gauge %.0fmm vs Segment gauge %.0fmm", LOCO_GAUGE, SEGMENT_GAUGE);

        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(LOCO_GAUGE, exception.getLocomotiveGauge(), 0.001);
        assertEquals(SEGMENT_GAUGE, exception.getSegmentGauge(), 0.001);
    }

    @Test
    void testConstructor_CustomMessage() {
        String customMessage = "Gauge mismatch detected.";
        IncompatibleGaugeException exception =
                new IncompatibleGaugeException(customMessage, LOCO_GAUGE, SEGMENT_GAUGE);

        assertEquals(customMessage, exception.getMessage());
        assertEquals(LOCO_GAUGE, exception.getLocomotiveGauge(), 0.001);
        assertEquals(SEGMENT_GAUGE, exception.getSegmentGauge(), 0.001);
    }

    // --- Getter Tests ---

    @Test
    void testGetLocomotiveGauge() {
        IncompatibleGaugeException exception =
                new IncompatibleGaugeException(LOCO_GAUGE, SEGMENT_GAUGE);
        assertEquals(LOCO_GAUGE, exception.getLocomotiveGauge(), 0.001);
    }

    @Test
    void testGetSegmentGauge() {
        IncompatibleGaugeException exception =
                new IncompatibleGaugeException(LOCO_GAUGE, SEGMENT_GAUGE);
        assertEquals(SEGMENT_GAUGE, exception.getSegmentGauge(), 0.001);
    }

    @Test
    void testGetGaugeDifference() {
        IncompatibleGaugeException exception =
                new IncompatibleGaugeException(LOCO_GAUGE, SEGMENT_GAUGE);

        double expectedDifference = Math.abs(LOCO_GAUGE - SEGMENT_GAUGE);
        assertEquals(expectedDifference, exception.getGaugeDifference(), 0.001);

        // Test with reverse order
        IncompatibleGaugeException exception2 =
                new IncompatibleGaugeException(SEGMENT_GAUGE, LOCO_GAUGE);
        assertEquals(expectedDifference, exception2.getGaugeDifference(), 0.001);

        // Test with same gauge
        IncompatibleGaugeException exception3 =
                new IncompatibleGaugeException(1000.0, 1000.0);
        assertEquals(0.0, exception3.getGaugeDifference(), 0.001);
    }
}