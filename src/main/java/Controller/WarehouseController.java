package Controller;

import Domain.Box;

import java.util.ArrayList;
import java.util.List;

public class WarehouseController {
    Warehouse warehouse;
    public WarehouseController(Warehouse warehouse) {
        this.warehouse = warehouse;
    }
    public List modoLinha (){
        List<Box> boxes = new ArrayList<>();
        for (List<Box> boxList : warehouse.getStock().values()) {
            boxes.addAll(boxList);
        }
        if()

    }


}
