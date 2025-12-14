package Lapr.Controller;

import Lapr.DataBaseAssociation.FreightAOD;
import Lapr.DataBaseAssociation.RouteAOD;
import Lapr.Domain.Freight;
import Lapr.Domain.Route;

import java.util.ArrayList;
import java.util.List;

public class AssignRouteController {
    private final FreightAOD freightAOD;
    private final RouteAOD routeAOD;

    public AssignRouteController(FreightAOD freightAOD, RouteAOD routeAOD) {
        this.freightAOD = freightAOD;
        this.routeAOD = routeAOD;
    }

   public List<Freight> verifyFreighAssigned () {
        List<Freight> freightsNotAssigned = new ArrayList<>();
        int count = 0;
        for ( Freight f : freightAOD.getAllFreights()){
            for ( Route route : routeAOD.getAllRoutes()){
                if(f.getTrainID() == route.getTrainID()){
                    count++;
                }
            }
            if(count == 0){
                freightsNotAssigned.add(f);
            }
        }
        return freightsNotAssigned;
   }


}
