package Sprint1.Repository;

import Sprint1.Domain.Trolley;

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

    /**
     * Gets all trolleys.
     *
     * @return the all trolleys
     */
    public List<Trolley> getAllTrolleys() {
        return new ArrayList<>(trolleyList);
    }

}
