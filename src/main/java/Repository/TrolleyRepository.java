package Repository;

import Domain.Item;
import Domain.Trolley;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Trolley repository.
 */
public class TrolleyRepository {

    private final List<Trolley> trolleyList = new ArrayList<>();

    /**
     * Save trolley.
     *
     * @param trolleyList the trolley list
     */
    public void saveTrolley(List<Trolley> trolleyList) {
        this.trolleyList.clear();
        this.trolleyList.addAll(trolleyList);
    }

    /**
     * Gets trolley.
     *
     * @return the trolley
     */
    public List<Trolley> getTrolley() {
        return new ArrayList<>(trolleyList);
    }

}
