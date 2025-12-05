package Domain;

/**
 * Exception thrown when a locomotive's gauge is incompatible with a line segment's gauge.
 * This exception indicates that the locomotive cannot operate on the specified track segment
 * due to gauge mismatch.
 *
 * @author Development Team
 * @version 1.0
 */
public class IncompatibleGaugeException extends RuntimeException {
    
    private final double locomotiveGauge;
    private final double segmentGauge;

    /**
     * Constructs a new IncompatibleGaugeException with the specified gauges.
     *
     * @param locomotiveGauge the locomotive's gauge in millimeters
     * @param segmentGauge    the line segment's gauge in millimeters
     */
    public IncompatibleGaugeException(double locomotiveGauge, double segmentGauge) {
        super(String.format("Incompatible gauge: Locomotive gauge %.0fmm vs Segment gauge %.0fmm", 
                           locomotiveGauge, segmentGauge));
        this.locomotiveGauge = locomotiveGauge;
        this.segmentGauge = segmentGauge;
    }

    /**
     * Constructs a new IncompatibleGaugeException with a custom message and gauges.
     *
     * @param message         the detail message
     * @param locomotiveGauge the locomotive's gauge in millimeters
     * @param segmentGauge    the line segment's gauge in millimeters
     */
    public IncompatibleGaugeException(String message, double locomotiveGauge, double segmentGauge) {
        super(message);
        this.locomotiveGauge = locomotiveGauge;
        this.segmentGauge = segmentGauge;
    }

    /**
     * Gets the locomotive's gauge.
     *
     * @return the locomotive gauge in millimeters
     */
    public double getLocomotiveGauge() {
        return locomotiveGauge;
    }

    /**
     * Gets the segment's gauge.
     *
     * @return the segment gauge in millimeters
     */
    public double getSegmentGauge() {
        return segmentGauge;
    }

    /**
     * Gets the gauge difference.
     *
     * @return the absolute difference between gauges in millimeters
     */
    public double getGaugeDifference() {
        return Math.abs(locomotiveGauge - segmentGauge);
    }
}
