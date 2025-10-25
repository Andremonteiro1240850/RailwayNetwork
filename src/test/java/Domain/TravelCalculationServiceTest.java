package Domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for the TravelCalculationService class, using the real Locomotive
 * and IncompatibleGaugeException classes.
 */
class TravelCalculationServiceTest {

    private Station station1;
    private Station station2;
    private Station station3;
    private Station station4; // Not connected
    private LineSegment segment12; // 10 km, 1668mm gauge, electrified
    private LineSegment segment23; // 5 km, 1435mm gauge, non-electrified

    // Agora usamos a classe real Locomotive
    private Locomotive locoWideGauge; // 1668mm, 100 km/h
    private Locomotive locoStandardGauge; // 1435mm, 120 km/h
    private Locomotive locoIncompatibleGauge; // 1000mm, 80 km/h

    private TravelCalculationService serviceWithSegments;
    private TravelCalculationService serviceWithoutSegments;


    @BeforeEach
    void setUp() {
        station1 = new Station("S1", "Station 1");
        station2 = new Station("S2", "Station 2");
        station3 = new Station("S3", "Station 3");
        station4 = new Station("S4", "Station 4"); // Unconnected station

        segment12 = new LineSegment("L12", "Line 1-2", station1, station2, 10.0, 1668.0, true);
        segment23 = new LineSegment("L23", "Line 2-3", station2, station3, 5.0, 1435.0, false);

        // Instanciação das Locomotivas reais
        locoWideGauge = new Locomotive("LW", "Electric", 100.0, 1668.0);
        locoStandardGauge = new Locomotive("LS", "Diesel", 120.0, 1435.0);
        locoIncompatibleGauge = new Locomotive("LI", "Diesel", 80.0, 1000.0);

        List<LineSegment> segments = Arrays.asList(segment12, segment23);
        serviceWithSegments = new TravelCalculationService(segments);
        serviceWithoutSegments = new TravelCalculationService(new ArrayList<>()); // Empty list
    }

    // --- Constructor Tests ---
    @Test
    void testConstructor_ValidList() {
        assertNotNull(serviceWithSegments);
        assertEquals(2, serviceWithSegments.getLineSegments().size());
    }

    @Test
    void testConstructor_NullList_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new TravelCalculationService(null),
                "Constructor should throw IllegalArgumentException for null list");
    }

    // --- calculateTravelTime(start, end, loco) Tests ---

    @Test
    void testCalculateTravelTime_ValidConnection_CompatibleGauge() {
        // station1 -> station2 (10km) with locoWideGauge (100km/h) -> 10/100 = 0.1 hours
        double expectedTime12 = 10.0 / 100.0;
        double actualTime12 = serviceWithSegments.calculateTravelTime(station1, station2, locoWideGauge);
        assertEquals(expectedTime12, actualTime12, 0.0001, "Travel time calculation 1-2 is incorrect");

        // station2 -> station3 (5km) with locoStandardGauge (120km/h) -> 5/120 hours
        double expectedTime23 = 5.0 / 120.0;
        double actualTime23 = serviceWithSegments.calculateTravelTime(station2, station3, locoStandardGauge);
        assertEquals(expectedTime23, actualTime23, 0.0001, "Travel time calculation 2-3 is incorrect");
    }

    @Test
    void testCalculateTravelTime_IncompatibleGauge_ThrowsException() {
        // Agora usamos a classe real IncompatibleGaugeException
        assertThrows(IncompatibleGaugeException.class, () ->
                        serviceWithSegments.calculateTravelTime(station1, station2, locoStandardGauge),
                "Should throw IncompatibleGaugeException for gauge mismatch");
    }

    @Test
    void testCalculateTravelTime_NoDirectConnection_ThrowsException() {
        assertThrows(IllegalStateException.class, () ->
                        serviceWithSegments.calculateTravelTime(station1, station3, locoWideGauge),
                "Should throw IllegalStateException when no direct connection exists");
    }

    // --- calculateTravelTime(start, end, loco, segment) Tests ---

    @Test
    void testCalculateTravelTimeWithSegment_Valid() {
        // Use segment12 (10km) with locoWideGauge (100km/h) -> 0.1 hours
        double expectedTime = 10.0 / 100.0;
        double actualTime = serviceWithSegments.calculateTravelTime(station1, station2, locoWideGauge, segment12);
        assertEquals(expectedTime, actualTime, 0.0001);
    }

    @Test
    void testCalculateTravelTimeWithSegment_IncompatibleGauge_ThrowsException() {
        // Agora usamos a classe real IncompatibleGaugeException
        assertThrows(IncompatibleGaugeException.class, () ->
                serviceWithSegments.calculateTravelTime(station1, station2, locoStandardGauge, segment12));
    }

    // --- formatTravelTime() Tests (static method) ---
    // (Mantido, pois o método é estático e não tem dependências de classe)

    @Test
    void testFormatTravelTime_HoursAndMinutes() {
        assertEquals("1h 30m", TravelCalculationService.formatTravelTime(1.5));
        assertEquals("25m", TravelCalculationService.formatTravelTime(25.0/60.0));
    }
}