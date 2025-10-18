import Domain.AllocationRow;
import Domain.EligibilityResult;

import java.util.List;

public class AllocationResult {
    private List<EligibilityResult> eligibilityResults;
    private List<AllocationRow> allocationRows;

    public AllocationResult(List<EligibilityResult> eligibilityResults,
                            List<AllocationRow> allocationRows) {
        this.eligibilityResults = eligibilityResults;
        this.allocationRows = allocationRows;
    }

    // Getters
    public List<EligibilityResult> getEligibilityResults() { return eligibilityResults; }
    public List<AllocationRow> getAllocationRows() { return allocationRows; }

    // Setters (opcionais)
    public void setEligibilityResults(List<EligibilityResult> eligibilityResults) {
        this.eligibilityResults = eligibilityResults;
    }
    public void setAllocationRows(List<AllocationRow> allocationRows) {
        this.allocationRows = allocationRows;
    }
}