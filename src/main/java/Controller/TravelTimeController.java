package Controller;

import Domain.*;
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
        return dataLoader.getStationById(id);
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
