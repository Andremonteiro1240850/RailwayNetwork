package Lapr.UI;

import Lapr.Controller.AssignRouteController;
import Lapr.Domain.Freight;

public class RoutePlannerUI implements Runnable{
    private final AssignRouteController assignRouteController;
    public RoutePlannerUI(AssignRouteController assignRouteController) {
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
