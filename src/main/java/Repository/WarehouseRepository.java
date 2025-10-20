package Repository;

import Controller.WarehouseController;
import Domain.AllocationRow;
import Domain.EligibilityResult;

import java.util.ArrayList;
import java.util.List;

public class WarehouseRepository {
    private final List<EligibilityResult> eligibilityResults = new ArrayList<>();
    private final List<AllocationRow> allocationRows = new ArrayList<>();
    public WarehouseRepository() {
    }
    public void saveEligibilityResults(List<EligibilityResult> results) {
        eligibilityResults.addAll(results);
    }
    public List<EligibilityResult> getEligibilityResults() {
        return new ArrayList<>(eligibilityResults);
    }
    public void saveAllocationRows(List<AllocationRow> rows) {
        allocationRows.addAll(rows);
    }
    public List<AllocationRow> getAllocationRows() {
        return new ArrayList<>(allocationRows);
    }


}
