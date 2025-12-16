package Lapr.UI;

import Lapr.Controller.RouteController;
import Lapr.Domain.Freight;

public class RoutePlannerUI implements Runnable{
    private final RouteController assignRouteController;
    public RoutePlannerUI(RouteController assignRouteController) {
        this.assignRouteController = assignRouteController;
    }
    @Override
    public void run() {


    }
    private void showDismissedFreights(){
        System.out.println("==Freights not assigned==");
        for(Freight f: assignRouteController.verifyFreighAssigned()){
            System.out.println(f);
        }
    }



}
