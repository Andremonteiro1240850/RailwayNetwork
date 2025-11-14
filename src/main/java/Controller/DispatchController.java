package Controller;

import Domain.*;
import java.util.List;

/**
 * Controller for managing train dispatch operations.
 * This controller handles the business logic coordination for USPL07 functionality.
 *
 * @author Development Team
 * @version 1.0
 */
public class DispatchController {
    
    private final SchedulerService schedulerService;
    private final TravelTimeController travelTimeController;

    /**
     * Constructs a new DispatchController.
     * This controller reuses the TravelTimeController to access the TravelCalculationService (USPL03).
     */
    public DispatchController() {
        this.travelTimeController = new TravelTimeController();
        
        // Reuse the TravelCalculationService from USPL03
        TravelCalculationService travelCalculationService = travelTimeController.getService();
        List<LineSegment> lineSegments = travelTimeController.getLineSegments();
        
        // Create the scheduler service that reuses USPL03's travel calculation
        this.schedulerService = new SchedulerService(travelCalculationService, lineSegments);
    }

    /**
     * Constructs a new DispatchController with a custom TravelTimeController.
     * This is useful for testing or when you want to inject dependencies.
     *
     * @param travelTimeController the travel time controller to use
     */
    public DispatchController(TravelTimeController travelTimeController) {
        if (travelTimeController == null) {
            throw new IllegalArgumentException("Travel time controller cannot be null");
        }
        
        this.travelTimeController = travelTimeController;
        
        // Reuse the TravelCalculationService from USPL03
        TravelCalculationService travelCalculationService = travelTimeController.getService();
        List<LineSegment> lineSegments = travelTimeController.getLineSegments();
        
        // Create the scheduler service that reuses USPL03's travel calculation
        this.schedulerService = new SchedulerService(travelCalculationService, lineSegments);
    }

    /**
     * Schedules a list of trains and returns a dispatch report.
     *
     * @param trains the list of trains to dispatch
     * @return the dispatch report containing schedules, conflicts, and summary
     * @throws IllegalArgumentException if trains list is null or empty
     */
    public DispatchReport scheduleTrains(List<Train> trains) {
        if (trains == null || trains.isEmpty()) {
            throw new IllegalArgumentException("Trains list cannot be null or empty");
        }
        
        return schedulerService.scheduleTrains(trains);
    }

    /**
     * Gets the scheduler service.
     *
     * @return the scheduler service
     */
    public SchedulerService getSchedulerService() {
        return schedulerService;
    }

    /**
     * Gets the travel time controller (for accessing USPL03 functionality).
     *
     * @return the travel time controller
     */
    public TravelTimeController getTravelTimeController() {
        return travelTimeController;
    }

    /**
     * Gets all available stations.
     *
     * @return list of stations
     */
    public List<Facility> getStations() {
        return travelTimeController.getStations();
    }

    /**
     * Gets a station by its ID.
     *
     * @param id the station ID
     * @return the station or null if not found
     */
    public Facility getStationById(String id) {
        return travelTimeController.getStationById(id);
    }
}

