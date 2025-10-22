package Controller;

import Domain.*;
import java.util.*;

/**
 * Controller for managing travel time calculations.
 * This controller handles the business logic coordination for USLP03 functionality.
 * 
 * @author Development Team
 * @version 1.0
 */
public class TravelTimeController {
    
    private final TravelCalculationService service;
    private final List<LineSegment> lineSegments;
    
    /**
     * Constructs a new TravelTimeController with mock railway data.
     */
    public TravelTimeController() {
        this.lineSegments = createMockLineSegments();
        this.service = new TravelCalculationService(lineSegments);
    }
    
    /**
     * Gets the travel calculation service.
     * 
     * @return the travel calculation service
     */
    public TravelCalculationService getService() {
        return service;
    }
    
    /**
     * Gets all available line segments.
     * 
     * @return list of line segments
     */
    public List<LineSegment> getLineSegments() {
        return lineSegments;
    }
    
    /**
     * Creates mock line segment data for testing and demonstration.
     * 
     * @return list of mock line segments
     */
    private List<LineSegment> createMockLineSegments() {
        // Create stations
        Station lisbon = new Station("ST001", "Lisbon Terminal", "Lisbon");
        Station porto = new Station("ST002", "Porto Cargo Hub", "Porto");
        Station coimbra = new Station("ST003", "Coimbra Station", "Coimbra");
        Station aveiro = new Station("ST004", "Aveiro Central", "Aveiro");
        Station braga = new Station("ST005", "Braga Station", "Braga");
        Station faro = new Station("ST006", "Faro Terminal", "Faro");
        Station evora = new Station("ST007", "Évora Station", "Évora");
        Station leiria = new Station("ST008", "Leiria Hub", "Leiria");
        
        // Create line segments (standard gauge 1435mm)
        return Arrays.asList(
            new LineSegment("LS001", lisbon, porto, 210.0, 1435.0, true),      // Lisbon-Porto (electrified)
            new LineSegment("LS002", porto, coimbra, 120.0, 1435.0, false),   // Porto-Coimbra (non-electrified)
            new LineSegment("LS003", coimbra, aveiro, 45.0, 1435.0, true),    // Coimbra-Aveiro (electrified)
            new LineSegment("LS004", porto, braga, 55.0, 1435.0, false),      // Porto-Braga (non-electrified)
            new LineSegment("LS005", lisbon, leiria, 140.0, 1435.0, true),   // Lisbon-Leiria (electrified)
            new LineSegment("LS006", leiria, coimbra, 80.0, 1435.0, false),   // Leiria-Coimbra (non-electrified)
            new LineSegment("LS007", lisbon, evora, 130.0, 1435.0, true),    // Lisbon-Évora (electrified)
            new LineSegment("LS008", evora, faro, 180.0, 1435.0, false),      // Évora-Faro (non-electrified)
            
            // Narrow gauge segments (1000mm)
            new LineSegment("LS009", aveiro, braga, 35.0, 1000.0, true),     // Aveiro-Braga (narrow gauge, electrified)
            new LineSegment("LS010", coimbra, leiria, 25.0, 1000.0, false),  // Coimbra-Leiria (narrow gauge, non-electrified)
            
            // Russian gauge segment (1520mm)
            new LineSegment("LS011", braga, faro, 300.0, 1520.0, false)       // Braga-Faro (Russian gauge, non-electrified)
        );
    }
}
