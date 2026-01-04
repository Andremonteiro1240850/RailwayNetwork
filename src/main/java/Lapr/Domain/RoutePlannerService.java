package Lapr.Domain;

import Lapr.DataBaseAssociation.AvailableWagonDAO;
import Lapr.DataBaseAssociation.RoutePlannerDAO;
import Lapr.DataBaseAssociation.LineSegmentAOD;
import Sprint1.Domain.Facility;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service class for Route Planner functionality (USLP08).
 * 
 * Allows Freight Manager to:
 * - View available wagons grouped by cargo type
 * - Define routes (simple or complex) with stations
 * - Assign load/unload operations for wagons at stations
 * - Persist routes to database
 * 
 * @author Development Team
 * @version 1.0
 */
public class RoutePlannerService {
    private final AvailableWagonDAO availableWagonDAO;
    private final RoutePlannerDAO routePlannerDAO;
    private final LineSegmentAOD facilityDAO;
    
    public RoutePlannerService() {
        this.availableWagonDAO = new AvailableWagonDAO();
        this.routePlannerDAO = new RoutePlannerDAO();
        this.facilityDAO = new LineSegmentAOD();
    }
    
    /**
     * Gets all available wagons grouped by cargo type.
     * 
     * @return map of cargo type to list of available wagons
     */
    public Map<String, List<AvailableWagonDAO.AvailableWagon>> getAvailableWagonsByCargoType() {
        return availableWagonDAO.getAvailableWagonsByCargoType();
    }
    
    /**
     * Gets all available wagons.
     * 
     * @return list of all available wagons
     */
    public List<AvailableWagonDAO.AvailableWagon> getAvailableWagons() {
        return availableWagonDAO.getAvailableWagons();
    }
    
    /**
     * Gets all facilities (stations) from database.
     * 
     * @return list of all facilities
     */
    public List<Facility> getAllFacilities() {
        return facilityDAO.getAllFacilities();
    }
    
    /**
     * Creates a route with the specified parameters.
     * 
     * @param routeName the name of the route
     * @param isSimple true for simple route, false for complex
     * @param stationIds list of facility IDs in order
     * @param wagonIds list of wagon IDs to assign to the route
     * @param loadUnloadInfo map of wagonId -> list of load/unload operations
     *                      Each operation is: {facilityId, stationOrder, type, quantity}
     * @return the created route, or null if failed
     */
    public RoutePlannerRoute createRoute(String routeName, boolean isSimple,
                                        List<Integer> stationIds, List<Integer> wagonIds,
                                        Map<Integer, List<LoadUnloadOperation>> loadUnloadInfo) {
        // Validate inputs
        if (routeName == null || routeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Route name cannot be empty");
        }
        if (stationIds == null || stationIds.size() < 2) {
            throw new IllegalArgumentException("Route must have at least 2 stations");
        }
        if (wagonIds == null || wagonIds.isEmpty()) {
            throw new IllegalArgumentException("Route must have at least 1 wagon");
        }
        if (isSimple && wagonIds.size() != 1) {
            throw new IllegalArgumentException("Simple route must have exactly 1 wagon");
        }
        
        // Load facilities
        List<Facility> facilities = getAllFacilities();
        Map<String, Facility> facilityMap = new java.util.HashMap<>();
        for (Facility f : facilities) {
            facilityMap.put(f.getId(), f);
        }
        
        // Create route object
        RoutePlannerRoute route = new RoutePlannerRoute(
            0, // Will be set after persistence
            routeName,
            LocalDateTime.now(),
            isSimple
        );
        
        // Add stations
        for (int i = 0; i < stationIds.size(); i++) {
            int facilityId = stationIds.get(i);
            Facility facility = facilityMap.get(String.valueOf(facilityId));
            if (facility == null) {
                throw new IllegalArgumentException("Facility ID " + facilityId + " not found");
            }
            RoutePlannerRoute.RouteStation station = 
                new RoutePlannerRoute.RouteStation(facility, i + 1); // Order starts at 1
            route.addStation(station);
        }
        
        // Get available wagons to validate and get cargo types
        List<AvailableWagonDAO.AvailableWagon> availableWagons = getAvailableWagons();
        Map<Integer, AvailableWagonDAO.AvailableWagon> wagonMap = new java.util.HashMap<>();
        for (AvailableWagonDAO.AvailableWagon w : availableWagons) {
            wagonMap.put(w.getWagonId(), w);
        }
        
        // Add wagons and load/unload info
        for (int wagonId : wagonIds) {
            AvailableWagonDAO.AvailableWagon availableWagon = wagonMap.get(wagonId);
            if (availableWagon == null) {
                throw new IllegalArgumentException("Wagon ID " + wagonId + " is not available");
            }
            
            RoutePlannerRoute.RouteWagon routeWagon = 
                new RoutePlannerRoute.RouteWagon(wagonId, availableWagon.getCargoType());
            
            // Add load/unload info for this wagon
            if (loadUnloadInfo != null && loadUnloadInfo.containsKey(wagonId)) {
                List<LoadUnloadOperation> operations = loadUnloadInfo.get(wagonId);
                for (LoadUnloadOperation op : operations) {
                    Facility facility = facilityMap.get(String.valueOf(op.facilityId));
                    if (facility == null) {
                        throw new IllegalArgumentException("Facility ID " + op.facilityId + " not found");
                    }
                    
                    RoutePlannerRoute.LoadUnloadType type;
                    switch (op.type) {
                        case "LOAD": type = RoutePlannerRoute.LoadUnloadType.LOAD; break;
                        case "UNLOAD": type = RoutePlannerRoute.LoadUnloadType.UNLOAD; break;
                        case "PARTIAL_UNLOAD": type = RoutePlannerRoute.LoadUnloadType.PARTIAL_UNLOAD; break;
                        default: type = RoutePlannerRoute.LoadUnloadType.LOAD;
                    }
                    
                    RoutePlannerRoute.LoadUnloadInfo info = 
                        new RoutePlannerRoute.LoadUnloadInfo(facility, op.stationOrder, type);
                    info.setQuantity(op.quantity);
                    routeWagon.addLoadUnloadInfo(info);
                }
            }
            
            route.addWagon(routeWagon);
        }
        
        // Persist route
        if (routePlannerDAO.persistRoute(route)) {
            return route;
        } else {
            return null;
        }
    }
    
    /**
     * Gets all routes from database.
     * 
     * @return list of all routes
     */
    public List<RoutePlannerRoute> getAllRoutes() {
        return routePlannerDAO.getAllRoutes();
    }
    
    /**
     * Gets a route by ID.
     * 
     * @param routeId the route ID
     * @return the route, or null if not found
     */
    public RoutePlannerRoute getRouteById(int routeId) {
        return routePlannerDAO.getRouteById(routeId);
    }
    
    /**
     * Represents a load/unload operation for a wagon.
     */
    public static class LoadUnloadOperation {
        public int facilityId;
        public int stationOrder;
        public String type; // "LOAD", "UNLOAD", "PARTIAL_UNLOAD"
        public double quantity;
        
        public LoadUnloadOperation(int facilityId, int stationOrder, String type, double quantity) {
            this.facilityId = facilityId;
            this.stationOrder = stationOrder;
            this.type = type;
            this.quantity = quantity;
        }
    }
    
    /**
     * Formats a route as a string showing the sequence of stations and cargos to be loaded/unloaded.
     * 
     * @param route the route to format
     * @return formatted string
     */
    public String formatRouteDetails(RoutePlannerRoute route) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Route: ").append(route.getRouteName()).append(" ===\n");
        sb.append("Route ID: ").append(route.getRouteId()).append("\n");
        sb.append("Type: ").append(route.isSimple() ? "Simple" : "Complex").append("\n");
        sb.append("Created: ").append(route.getCreatedDate()).append("\n\n");
        
        sb.append("Sequence of Stations:\n");
        List<RoutePlannerRoute.RouteStation> stations = route.getStations();
        for (int i = 0; i < stations.size(); i++) {
            RoutePlannerRoute.RouteStation station = stations.get(i);
            sb.append("  ").append(i + 1).append(". ")
              .append(station.getFacility().getName())
              .append(" (ID: ").append(station.getFacility().getId())
              .append(", Order: ").append(station.getOrder()).append(")\n");
        }
        
        sb.append("\nCargos to be Loaded/Unloaded:\n");
        List<RoutePlannerRoute.RouteWagon> wagons = route.getWagons();
        if (wagons.isEmpty()) {
            sb.append("  No wagons assigned to this route.\n");
        } else {
            for (RoutePlannerRoute.RouteWagon wagon : wagons) {
                sb.append("\n  Wagon ID: ").append(wagon.getWagonId())
                  .append(" | Cargo Type: ").append(wagon.getCargoType()).append("\n");
                
                List<RoutePlannerRoute.LoadUnloadInfo> loadUnloadInfo = wagon.getLoadUnloadInfo();
                if (loadUnloadInfo.isEmpty()) {
                    sb.append("    No load/unload operations defined.\n");
                } else {
                    for (RoutePlannerRoute.LoadUnloadInfo info : loadUnloadInfo) {
                        sb.append("    - ").append(info.getType())
                          .append(" at ").append(info.getFacility().getName())
                          .append(" (Station Order: ").append(info.getStationOrder()).append(")");
                        if (info.getQuantity() > 0) {
                            sb.append(" | Quantity: ").append(info.getQuantity());
                        }
                        sb.append("\n");
                    }
                }
            }
        }
        
        return sb.toString();
    }
}

