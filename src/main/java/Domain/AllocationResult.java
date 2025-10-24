package Domain;

import java.util.List;

/**
 * The type Allocation result.
 */
public class AllocationResult {
    private List<EligibilityResult> eligibilityResults;
    private List<AllocationRow> allocationRows;

    /**
     * Instantiates a new Allocation result.
     *
     * @param eligibilityResults the eligibility results
     * @param allocationRows     the allocation rows
     */
    public AllocationResult(List<EligibilityResult> eligibilityResults,
                            List<AllocationRow> allocationRows) {
        this.eligibilityResults = eligibilityResults;
        this.allocationRows = allocationRows;
    }

    /**
     * Gets eligibility results.
     *
     * @return the eligibility results
     */
// Getters
    public List<EligibilityResult> getEligibilityResults() { return eligibilityResults; }

    /**
     * Gets allocation rows.
     *
     * @return the allocation rows
     */
    public List<AllocationRow> getAllocationRows() { return allocationRows; }

    /**
     * Sets eligibility results.
     *
     * @param eligibilityResults the eligibility results
     */
// Setters (opcionais)
    public void setEligibilityResults(List<EligibilityResult> eligibilityResults) {
        this.eligibilityResults = eligibilityResults;
    }

    /**
     * Sets allocation rows.
     *
     * @param allocationRows the allocation rows
     */
    public void setAllocationRows(List<AllocationRow> allocationRows) {
        this.allocationRows = allocationRows;
    }
}
