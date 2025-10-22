package Repository;

import Domain.Item;
import Domain.Trolley;

import java.util.ArrayList;
import java.util.List;

public class TrolleyRepository {

    private final List<Trolley> trolleyList = new ArrayList<>();
    public void saveTrolley(List<Trolley> trolleyList) {
        this.trolleyList.clear();
        this.trolleyList.addAll(trolleyList);
    }
    public List<Trolley> getTrolley() {
        return new ArrayList<>(trolleyList);
    }

}
