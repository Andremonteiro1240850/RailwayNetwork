package Lapr.Domain;

import Sprint1.Domain.Facility;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a route planned by the Freight Manager.
 * A route can be simple (single wagon) or complex (multiple wagons).
 * Routes are NOT associated with Trains.
 * 
 * @author Development Team
 * @version 1.0
 */
public class RoutePlannerRoute {
    private int routeId;
    private String routeName;
    private LocalDateTime createdDate;
    private boolean isSimple; // true for simple route, false for complex
    private List<RouteStation> stations;
    private List<RouteWagon> wagons;
    
    /**
     * Constructs a new RoutePlannerRoute.
     * 
     * @param routeId the unique route identifier
     * @param routeName the name of the route
     * @param createdDate the creation date/time
     * @param isSimple true if simple route, false if complex
     */
    public RoutePlannerRoute(int routeId, String routeName, LocalDateTime createdDate, boolean isSimple) {
        this.routeId = routeId;
        this.routeName = routeName;
        this.createdDate = createdDate;
        this.isSimple = isSimple;
        this.stations = new ArrayList<>();
        this.wagons = new ArrayList<>();
    }
    
    public int getRouteId() { return routeId; }
    public void setRouteId(int routeId) { this.routeId = routeId; }
    
    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public boolean isSimple() { return isSimple; }
    public void setSimple(boolean simple) { isSimple = simple; }
    
    public List<RouteStation> getStations() { return new ArrayList<>(stations); }
    public void setStations(List<RouteStation> stations) { this.stations = new ArrayList<>(stations); }
    public void addStation(RouteStation station) { this.stations.add(station); }
    
    public List<RouteWagon> getWagons() { return new ArrayList<>(wagons); }
    public void setWagons(List<RouteWagon> wagons) { this.wagons = new ArrayList<>(wagons); }
    public void addWagon(RouteWagon wagon) { this.wagons.add(wagon); }
    
    /**
     * Represents a station in the route with its order.
     */
    public static class RouteStation {
        private Facility facility;
        private int order; // Order in the route sequence
        
        public RouteStation(Facility facility, int order) {
            this.facility = facility;
            this.order = order;
        }
        
        public Facility getFacility() { return facility; }
        public void setFacility(Facility facility) { this.facility = facility; }
        
        public int getOrder() { return order; }
        public void setOrder(int order) { this.order = order; }
    }
    
    /**
     * Represents a wagon in the route with its load/unload information.
     */
    public static class RouteWagon {
        private int wagonId;
        private String cargoType;
        private List<LoadUnloadInfo> loadUnloadInfo;
        
        public RouteWagon(int wagonId, String cargoType) {
            this.wagonId = wagonId;
            this.cargoType = cargoType;
            this.loadUnloadInfo = new ArrayList<>();
        }
        
        public int getWagonId() { return wagonId; }
        public void setWagonId(int wagonId) { this.wagonId = wagonId; }
        
        public String getCargoType() { return cargoType; }
        public void setCargoType(String cargoType) { this.cargoType = cargoType; }
        
        public List<LoadUnloadInfo> getLoadUnloadInfo() { return new ArrayList<>(loadUnloadInfo); }
        public void setLoadUnloadInfo(List<LoadUnloadInfo> loadUnloadInfo) { 
            this.loadUnloadInfo = new ArrayList<>(loadUnloadInfo); 
        }
        public void addLoadUnloadInfo(LoadUnloadInfo info) { this.loadUnloadInfo.add(info); }
    }
    
    /**
     * Represents load/unload information for a wagon at a specific station.
     */
    public static class LoadUnloadInfo {
        private Facility facility;
        private int stationOrder; // Order of station in route
        private LoadUnloadType type;
        private double quantity; // Optional: quantity to load/unload (if needed)
        
        public LoadUnloadInfo(Facility facility, int stationOrder, LoadUnloadType type) {
            this.facility = facility;
            this.stationOrder = stationOrder;
            this.type = type;
            this.quantity = 0.0;
        }
        
        public Facility getFacility() { return facility; }
        public void setFacility(Facility facility) { this.facility = facility; }
        
        public int getStationOrder() { return stationOrder; }
        public void setStationOrder(int stationOrder) { this.stationOrder = stationOrder; }
        
        public LoadUnloadType getType() { return type; }
        public void setType(LoadUnloadType type) { this.type = type; }
        
        public double getQuantity() { return quantity; }
        public void setQuantity(double quantity) { this.quantity = quantity; }
    }
    
    /**
     * Enum for load/unload operation type.
     */
    public enum LoadUnloadType {
        LOAD,
        UNLOAD,
        PARTIAL_UNLOAD
    }
}

