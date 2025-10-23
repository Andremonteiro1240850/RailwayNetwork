package pt.ipp.isep.dei;

import Domain.*;
import Controller.TravelTimeController;
import UI.TravelTimeUI;

/**
 * Demonstration script for USLP03 - Travel Time Calculation
 * This class demonstrates the key functionality without requiring the full application.
 * 
 * @author Development Team
 * @version 1.0
 */
public class TravelTimeDemo {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("USLP03 - TRAVEL TIME CALCULATION DEMONSTRATION");
        System.out.println("=".repeat(60));
        
        // Create controller with mock data
        TravelTimeController controller = new TravelTimeController();
        TravelCalculationService service = controller.getService();
        
        // Create test stations and locomotive
        Station lisbon = new Station("ST001", "Lisbon Terminal", "Lisbon");
        Station porto = new Station("ST002", "Porto Cargo Hub", "Porto");
        Locomotive electricLoco = new Locomotive("L001", "electric", 120.0, 1435.0);
        
        System.out.println("\n📋 TEST SCENARIOS:");
        System.out.println("-".repeat(40));
        
        // Test 1: Successful calculation
        System.out.println("\n✅ Test 1: Successful Travel Time Calculation");
        try {
            double travelTime = service.calculateTravelTime(lisbon, porto, electricLoco);
            String formattedTime = TravelCalculationService.formatTravelTime(travelTime);
            
            System.out.printf("From: %s%n", lisbon.getName());
            System.out.printf("To: %s%n", porto.getName());
            System.out.printf("Locomotive: %s (%s, %.0f km/h)%n", 
                             electricLoco.getId(), electricLoco.getType(), electricLoco.getMaxSpeedKmH());
            System.out.printf("Estimated Travel Time: %s%n", formattedTime);
            
            LineSegment segment = service.findConnectingSegment(lisbon, porto);
            if (segment != null) {
                System.out.printf("Line Segment: %s (%.2f km, %.0fmm gauge)%n", 
                                 segment.getId(), segment.getLengthKm(), segment.getGauge());
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
        
        // Test 2: Gauge incompatibility
        System.out.println("\n❌ Test 2: Gauge Incompatibility Test");
        try {
            Locomotive narrowGaugeLoco = new Locomotive("L003", "electric", 80.0, 1000.0);
            service.calculateTravelTime(lisbon, porto, narrowGaugeLoco);
            System.out.println("Unexpected: No exception thrown!");
        } catch (IncompatibleGaugeException e) {
            System.out.println("✅ Expected exception caught: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Unexpected error: " + e.getMessage());
        }
        
        // Test 3: Non-connected stations
        System.out.println("\n❌ Test 3: Non-Connected Stations Test");
        try {
            Station faro = new Station("ST006", "Faro Terminal", "Faro");
            service.calculateTravelTime(lisbon, faro, electricLoco);
            System.out.println("Unexpected: No exception thrown!");
        } catch (IllegalStateException e) {
            System.out.println("✅ Expected exception caught: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Unexpected error: " + e.getMessage());
        }
        
        // Test 4: Time formatting edge cases
        System.out.println("\n⏰ Test 4: Time Formatting Examples");
        System.out.printf("1.75 hours = %s%n", TravelCalculationService.formatTravelTime(1.75));
        System.out.printf("2.0 hours = %s%n", TravelCalculationService.formatTravelTime(2.0));
        System.out.printf("0.5 hours = %s%n", TravelCalculationService.formatTravelTime(0.5));
        System.out.printf("0.0 hours = %s%n", TravelCalculationService.formatTravelTime(0.0));
        System.out.printf("1.99 hours = %s%n", TravelCalculationService.formatTravelTime(1.99));
        
        // Test 5: Available data summary
        System.out.println("\n📊 Test 5: Available Data Summary");
        System.out.printf("Available Line Segments: %d%n", service.getLineSegments().size());
        System.out.println("Sample segments:");
        service.getLineSegments().stream()
                .limit(3)
                .forEach(segment -> System.out.printf("  - %s%n", segment));
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ DEMONSTRATION COMPLETED SUCCESSFULLY");
        System.out.println("=".repeat(60));
        System.out.println("\nTo run the full interactive application:");
        System.out.println("1. Compile the project");
        System.out.println("2. Run: pt.ipp.isep.dei.Main");
        System.out.println("3. Select option 6: Travel Time Calculation");
    }
}
