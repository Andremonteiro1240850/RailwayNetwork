package Lapr.Controller;

import Lapr.DataBaseAssociation.FreightAOD;
import Lapr.DataBaseAssociation.RouteAOD;
import Lapr.Domain.Freight;
import Lapr.Domain.Route;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for USLP08 - Route Planner.
 * 
 * Manages the creation and assignment of routes (simple or complex) 
 * to accomplish a list of pending freights.
 * 
 * @author Development Team
 * @version 1.0
 */
public class RouteController {
    private final FreightAOD freightAOD;
    private final RouteAOD routeAOD;

    public RouteController() {
        this.freightAOD = new FreightAOD();
        this.routeAOD = new RouteAOD();
    }

    public RouteController(FreightAOD freightAOD, RouteAOD routeAOD) {
        this.freightAOD = freightAOD;
        this.routeAOD = routeAOD;
    }

    /**
     * Gets all freights that are not assigned to any route.
     * Uses PL/SQL function if available.
     * 
     * @return list of pending freights
     */
    public List<Freight> getPendingFreights() {
        return freightAOD.getPendingFreights();
    }

    /**
     * Verifies which freights are not assigned to any route.
     * This method checks all freights and filters those without a train assignment.
     * 
     * @return list of freights not assigned
     */
    public List<Freight> verifyFreighAssigned() {
        List<Freight> freightsNotAssigned = new ArrayList<>();
        List<Route> allRoutes = routeAOD.getAllRoutes();
        
        for (Freight f : freightAOD.getAllFreights()) {
            boolean isAssigned = false;
            for (Route route : allRoutes) {
                if (f.getTrainID() == route.getTrainID() && f.getTrainID() != 0) {
                    isAssigned = true;
                    break;
                }
            }
            if (!isAssigned) {
                freightsNotAssigned.add(f);
            }
        }
        return freightsNotAssigned;
    }

    /**
     * Creates a simple route for a single freight.
     * A simple route has one freight and a sequence of stations.
     * 
     * @param trainId the train ID for the route
     * @param stationIds list of station IDs in order (sequence of stations)
     * @param freightId the freight ID to assign to this route
     * @return true if successful, false otherwise
     */
    public boolean createSimpleRoute(int trainId, List<Integer> stationIds, int freightId) {
        if (stationIds == null || stationIds.isEmpty()) {
            System.err.println("ERROR: Station list cannot be empty");
            return false;
        }
        if (trainId <= 0) {
            System.err.println("ERROR: Train ID must be positive");
            return false;
        }
        if (freightId <= 0) {
            System.err.println("ERROR: Freight ID must be positive");
            return false;
        }
        
        return routeAOD.createSimpleRoute(trainId, stationIds, freightId);
    }

    /**
     * Creates a complex route for multiple freights.
     * A complex route has multiple freights and a sequence of stations.
     * 
     * @param trainId the train ID for the route
     * @param stationIds list of station IDs in order (sequence of stations)
     * @param freightIds list of freight IDs to assign to this route
     * @return true if successful, false otherwise
     */
    public boolean createComplexRoute(int trainId, List<Integer> stationIds, List<Integer> freightIds) {
        if (stationIds == null || stationIds.isEmpty()) {
            System.err.println("ERROR: Station list cannot be empty");
            return false;
        }
        if (freightIds == null || freightIds.isEmpty()) {
            System.err.println("ERROR: Freight list cannot be empty");
            return false;
        }
        if (trainId <= 0) {
            System.err.println("ERROR: Train ID must be positive");
            return false;
        }
        
        return routeAOD.createComplexRoute(trainId, stationIds, freightIds);
    }

    /**
     * Gets all routes from the database.
     * 
     * @return list of all routes
     */
    public List<Route> getAllRoutes() {
        return routeAOD.getAllRoutes();
    }
}
