package Lapr.Controller;

import Lapr.DataBaseAssociation.VehicleAOD;
import Lapr.DataBaseAssociation.RouteAOD;
import Lapr.DataBaseAssociation.TrainAOD;
import Lapr.DataBaseAssociation.RoutePlannerDAO;
import Lapr.Domain.Route;
import Lapr.Domain.RoutePlannerRoute;
import Sprint1.Domain.Facility;
import Sprint1.Domain.Locomotive;
import Sprint1.Domain.Wagon;
import Lapr.DataBaseAssociation.LocomotiveAOD;
import Lapr.DataBaseAssociation.WagonAOD;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for USLP09 - Assemble and Assign Train to Route.
 * 
 * Manages the selection of locomotives and wagons for train assembly,
 * and assigns trains to routes created in US08.
 * 
 * @author Development Team
 * @version 2.0
 */
public class TrainAssemblyController {
    private VehicleAOD vehicleAOD;
    private RouteAOD routeAOD;
    private TrainAOD trainAOD;
    private RoutePlannerDAO routePlannerDAO;
    private LocomotiveAOD locomotiveAOD;
    private WagonAOD wagonAOD;
    
    public TrainAssemblyController() {
        this.vehicleAOD = new VehicleAOD();
        this.routeAOD = new RouteAOD();
        this.trainAOD = new TrainAOD();
        this.routePlannerDAO = new RoutePlannerDAO();
        this.locomotiveAOD = new LocomotiveAOD();
        this.wagonAOD = new WagonAOD();
    }
    
    /**
     * Gets all available routes from US08 (RoutePlannerRoute).
     * These are routes created in US08 that are not yet assigned to a train.
     * 
     * @return list of routes from US08
     */
    public List<RoutePlannerRoute> getAvailableRoutesFromUS08() {
        return routePlannerDAO.getAllRoutes();
    }
    
    /**
     * Gets all available locomotives from the database.
     * 
     * @return list of all locomotives
     */
    public List<Locomotive> getAvailableLocomotives() {
        return locomotiveAOD.getAllLocomotives();
    }
    
    /**
     * Gets locomotives with status information for a given route.
     * 
     * @param routeStartFacilityId the starting facility ID of the route
     * @param routeEndFacilityId the ending facility ID of the route
     * @return list of locomotives with status (in transit or parked)
     */
    public List<TrainAOD.LocomotiveWithStatus> getLocomotivesWithStatus(int routeStartFacilityId, int routeEndFacilityId) {
        List<Locomotive> allLocomotives = locomotiveAOD.getAllLocomotives();
        return trainAOD.getLocomotivesWithStatus(routeStartFacilityId, routeEndFacilityId, allLocomotives);
    }
    
    /**
     * Gets wagons with status information for a given route.
     * 
     * @param routeStartFacilityId the starting facility ID of the route
     * @param routeEndFacilityId the ending facility ID of the route
     * @param wagonIds list of wagon IDs
     * @return list of wagons with status (in transit or parked)
     */
    public List<TrainAOD.WagonWithStatus> getWagonsWithStatus(int routeStartFacilityId, int routeEndFacilityId, 
                                                              List<Integer> wagonIds) {
        return trainAOD.getWagonsWithStatus(routeStartFacilityId, routeEndFacilityId, wagonIds);
    }
    
    /**
     * Gets wagons from a selected route.
     * 
     * @param route the route to get wagons from
     * @return list of wagon IDs
     */
    public List<Integer> getWagonsFromRoute(RoutePlannerRoute route) {
        List<Integer> wagonIds = new ArrayList<>();
        if (route != null && route.getWagons() != null) {
            for (RoutePlannerRoute.RouteWagon wagon : route.getWagons()) {
                wagonIds.add(wagon.getWagonId());
            }
        }
        return wagonIds;
    }
    
    /**
     * Creates a train and assigns it to a route.
     * Uses a single transaction to ensure all operations are committed together.
     * 
     * @param operatorId the operator ID
     * @param maxLength the maximum train length
     * @param locomotiveIds list of locomotive IDs to add
     * @param route the route to assign
     * @return the created train ID, or -1 if failed
     */
    public int createTrainAndAssignToRoute(String operatorId, double maxLength, 
                                          List<Integer> locomotiveIds, RoutePlannerRoute route) {
        if (locomotiveIds == null || locomotiveIds.isEmpty()) {
            System.err.println("ERROR: At least one locomotive is required");
            return -1;
        }
        if (route == null) {
            System.err.println("ERROR: Route cannot be null");
            return -1;
        }
        
        // Get station IDs from route
        List<Integer> stationIds = new ArrayList<>();
        for (RoutePlannerRoute.RouteStation station : route.getStations()) {
            stationIds.add(Integer.parseInt(station.getFacility().getId()));
        }
        
        // Use single transaction method to ensure all operations are committed together
        return trainAOD.createTrainAndAssignRouteInTransaction(
            operatorId, maxLength, locomotiveIds, route.getRouteId(), stationIds);
    }
    
    /**
     * Gets full locomotive details by ID.
     * 
     * @param locomotiveId the locomotive ID
     * @return locomotive object, or null if not found
     */
    public Locomotive getLocomotiveById(String locomotiveId) {
        List<Locomotive> allLocomotives = locomotiveAOD.getAllLocomotives();
        return allLocomotives.stream()
            .filter(l -> l.getId().equals(locomotiveId))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Gets full wagon details by ID.
     * 
     * @param wagonId the wagon ID
     * @return wagon object, or null if not found
     */
    public Wagon getWagonById(String wagonId) {
        List<Wagon> allWagons = wagonAOD.getAllWagons();
        return allWagons.stream()
            .filter(w -> w.getWagonId().equals(wagonId))
            .findFirst()
            .orElse(null);
    }
}

