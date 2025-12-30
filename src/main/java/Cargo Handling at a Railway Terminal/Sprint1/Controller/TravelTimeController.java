package Sprint1.Controller;

import Sprint1.Domain.Facility;
import Sprint1.Domain.RailwayDataLoader;
import Sprint1.Domain.TravelCalculationService;
import Sprint2.Domain.LineSegment;

import java.util.*;
import java.io.IOException;

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
    private final List<Facility> facilities;
    private final RailwayDataLoader dataLoader;

    /**
     * Constructs a new TravelTimeController with real railway data from CSV files.
     */
    public TravelTimeController() {
        this.dataLoader = new RailwayDataLoader();
        this.facilities = new ArrayList<>();
        this.lineSegments = new ArrayList<>();
        
        try {
            System.out.println("=== LOADING RAILWAY DATA FROM CSV ===");
            dataLoader.loadAllData();
            this.facilities.addAll(dataLoader.getStations());
            this.lineSegments.addAll(dataLoader.getLineSegments());
            System.out.println("  CSV data loaded successfully!");
            System.out.println("   Stations: " + this.facilities.size());
            System.out.println("   Line segments: " + this.lineSegments.size());
        } catch (IOException e) {
            System.err.println("CRITICAL ERROR: Cannot load railway data from CSV files!");
            System.err.println("Error details: " + e.getMessage());
            System.err.println("Please check that the following files exist:");
            System.err.println("  - train_station_dataset/facility.csv");
            System.err.println("  - train_station_dataset/line.csv");
            throw new RuntimeException("Failed to load railway data", e);
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR: Unexpected error loading railway data!");
            System.err.println("Error details: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to load railway data", e);
        }
        
        this.service = new TravelCalculationService(lineSegments);
    }
    
    /**
     * Constructs a new TravelTimeController with pre-loaded data from database.
     * USLP10: Use this constructor to load data from database instead of CSV.
     * 
     * @param facilities list of facilities loaded from database
     * @param lineSegments list of line segments loaded from database
     */
    public TravelTimeController(List<Facility> facilities, List<LineSegment> lineSegments) {
        if (facilities == null || lineSegments == null) {
            throw new IllegalArgumentException("Facilities and line segments cannot be null");
        }
        
        this.dataLoader = null; // No CSV loader needed
        this.facilities = new ArrayList<>(facilities);
        this.lineSegments = new ArrayList<>(lineSegments);
        this.service = new TravelCalculationService(lineSegments);
        
        System.out.println("=== LOADING RAILWAY DATA FROM DATABASE ===");
        System.out.println("  Database data loaded successfully!");
        System.out.println("   Stations: " + this.facilities.size());
        System.out.println("   Line segments: " + this.lineSegments.size());
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
     * Gets all available stations.
     *
     * @return list of stations
     */
    public List<Facility> getStations() {
        return facilities;
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
     * Gets a station by its ID.
     *
     * @param id the station ID
     * @return the station or null if not found
     */
    public Facility getStationById(String id) {
        if (dataLoader != null) {
            return dataLoader.getStationById(id);
        } else {
            // Search in facilities list when loaded from database
            return facilities.stream()
                .filter(f -> f.getId().equals(id))
                .findFirst()
                .orElse(null);
        }
    }

    /**
     * Gets the railway data loader.
     *
     * @return the data loader.
     */
    public RailwayDataLoader getDataLoader() {
        return dataLoader;
    }
    
}
