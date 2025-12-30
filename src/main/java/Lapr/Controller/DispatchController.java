package Lapr.Controller;

import Sprint1.Controller.TravelTimeController;
import Sprint1.Domain.Facility;
import Sprint1.Domain.Locomotive;
import Lapr.Domain.DispatchReport;
import Sprint2.Domain.LineSegment;
import Lapr.Domain.SchedulerService;
import Sprint1.Domain.TravelCalculationService;
import Sprint1.Domain.Wagon;
import Lapr.Domain.Train;

import java.util.Collections;
import java.util.List;

/**
 * Controller for managing train dispatch operations.
 * This controller handles the business logic coordination for train dispatch functionality.
 *
 * @author Development Team
 * @version 1.0
 */
public class DispatchController {
    
    private final SchedulerService schedulerService;
    private final TravelTimeController travelTimeController;

    /**
     * Represents a train configuration loaded from CSV.
     * Contains the train ID, assigned locomotives, and wagons.
     * The route will be defined manually by the Freight Manager.
     */
    public static class TrainConfiguration {
        
        private final String trainId;
        private final List<Locomotive> locomotives;
        private final List<Wagon> wagons;

        /**
         * Constructs a new TrainConfiguration.
         *
         * @param trainId     the train ID
         * @param locomotives the list of locomotives assigned to this train
         * @param wagons      the list of wagons assigned to this train
         * @throws IllegalArgumentException if parameters are invalid
         */
        public TrainConfiguration(String trainId, List<Locomotive> locomotives, List<Wagon> wagons) {
            if (trainId == null || trainId.trim().isEmpty()) {
                throw new IllegalArgumentException("Train ID cannot be null or empty");
            }
            if (locomotives == null || locomotives.isEmpty()) {
                throw new IllegalArgumentException("Train must have at least one locomotive");
            }
            if (wagons == null) {
                throw new IllegalArgumentException("Wagons list cannot be null");
            }
            
            this.trainId = trainId.trim();
            this.locomotives = Collections.unmodifiableList(locomotives);
            this.wagons = Collections.unmodifiableList(wagons);
        }

        /**
         * Gets the train ID.
         *
         * @return the train ID
         */
        public String getTrainId() {
            return trainId;
        }

        /**
         * Gets the locomotives assigned to this train.
         *
         * @return the list of locomotives
         */
        public List<Locomotive> getLocomotives() {
            return locomotives;
        }

        /**
         * Gets the wagons assigned to this train.
         *
         * @return the list of wagons
         */
        public List<Wagon> getWagons() {
            return wagons;
        }

        @Override
        public String toString() {
            return String.format("TrainConfiguration[%s, Locomotives: %d, Wagons: %d]", 
                               trainId, locomotives.size(), wagons.size());
        }
    }

    /**
     * Constructs a new DispatchController.
     * This controller reuses the TravelTimeController to access the TravelCalculationService.
     */
    public DispatchController() {
        this(new TravelTimeController());
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
        
        // Reuse the TravelCalculationService
        TravelCalculationService travelCalculationService = travelTimeController.getService();
        List<LineSegment> lineSegments = travelTimeController.getLineSegments();
        
        // Create the scheduler service that reuses travel calculation
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
     * Gets the travel time controller.
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

