package Lapr.Controller;

import Lapr.DataBaseAssociation.VehicleAOD;
import Lapr.DataBaseAssociation.RouteAOD;
import Lapr.Domain.Route;
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
 * distinguishing between vehicles in transit and parked vehicles.
 * 
 * @author Development Team
 * @version 1.0
 */
public class TrainAssemblyController {
    private VehicleAOD vehicleAOD;
    private RouteAOD routeAOD;
    private LocomotiveAOD locomotiveAOD;
    private WagonAOD wagonAOD;
    
    public TrainAssemblyController() {
        this.vehicleAOD = new VehicleAOD();
        this.routeAOD = new RouteAOD();
        this.locomotiveAOD = new LocomotiveAOD();
        this.wagonAOD = new WagonAOD();
    }
    
    /**
     * Gets all available routes from the database.
     * 
     * @return list of routes
     */
    public List<Route> getAvailableRoutes() {
        return routeAOD.getAllRoutes();
    }
    
    /**
     * Gets locomotives with their status for a given route.
     * Distinguishes between in transit (shows final destination) and parked (shows station).
     * For parked locomotives, orders by distance from route start (descending).
     * 
     * @param routeStartStationId the starting station ID of the route
     * @return list of locomotive information, ordered appropriately
     */
    public List<VehicleAOD.VehicleInfo> getLocomotivesForRoute(String routeStartStationId) {
        List<VehicleAOD.VehicleInfo> locomotives = vehicleAOD.getLocomotivesWithStatus(routeStartStationId);
        
        // Sort: in transit first, then parked by distance (descending)
        return locomotives.stream()
            .sorted(Comparator
                .comparing((VehicleAOD.VehicleInfo v) -> v.getStatus().equals("IN_TRANSIT") ? 0 : 1)
                .thenComparing((VehicleAOD.VehicleInfo v) -> v.getStatus().equals("PARKED") ? 
                    v.getDistanceFromStart() : 0, Comparator.reverseOrder()))
            .collect(Collectors.toList());
    }
    
    /**
     * Gets wagons with their status for a given route.
     * Distinguishes between in transit (shows final destination) and parked (shows station).
     * For parked wagons, orders by distance from route start (descending).
     * 
     * @param routeStartStationId the starting station ID of the route
     * @return list of wagon information, ordered appropriately
     */
    public List<VehicleAOD.VehicleInfo> getWagonsForRoute(String routeStartStationId) {
        List<VehicleAOD.VehicleInfo> wagons = vehicleAOD.getWagonsWithStatus(routeStartStationId);
        
        // Sort: in transit first, then parked by distance (descending)
        return wagons.stream()
            .sorted(Comparator
                .comparing((VehicleAOD.VehicleInfo v) -> v.getStatus().equals("IN_TRANSIT") ? 0 : 1)
                .thenComparing((VehicleAOD.VehicleInfo v) -> v.getStatus().equals("PARKED") ? 
                    v.getDistanceFromStart() : 0, Comparator.reverseOrder()))
            .collect(Collectors.toList());
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
    
    /**
     * Assembles a train by assigning selected locomotives and wagons to a route.
     * 
     * @param routeId the route ID
     * @param selectedLocomotiveIds list of selected locomotive IDs
     * @param selectedWagonIds list of selected wagon IDs
     * @return true if successful, false otherwise
     */
    public boolean assignTrainToRoute(String routeId, List<String> selectedLocomotiveIds, 
                                      List<String> selectedWagonIds) {
        // TODO: Implement assignment logic via PL/SQL function
        // This would call a PL/SQL function to assign the train to the route
        try {
            // Placeholder for PL/SQL function call
            // routeAOD.assignTrainToRoute(routeId, selectedLocomotiveIds, selectedWagonIds);
            return true;
        } catch (Exception e) {
            System.err.println("Error assigning train to route: " + e.getMessage());
            return false;
        }
    }
}

