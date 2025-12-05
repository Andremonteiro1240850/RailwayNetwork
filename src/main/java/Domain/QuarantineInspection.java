package Domain;

import Domain.ENUM.QAAction;
import java.time.LocalDateTime;

/**
 * The type Quarantine inspection.
 */
public class QuarantineInspection {
    private String inspectionId;
    private LocalDateTime inspectedAt;
    private String returnId;
    private QAAction action;

    /**
     * Instantiates a new Quarantine inspection.
     *
     * @param inspectionId the inspection id
     * @param returnId     the return id
     * @param inspectedAt  the inspected at
     * @param action       the action
     */
// Construtor
    public QuarantineInspection(String inspectionId, String returnId, LocalDateTime inspectedAt, QAAction action) {
        this.inspectionId = inspectionId;
        this.returnId = returnId;
        this.inspectedAt = inspectedAt;
        this.action = action;
    }

    /**
     * Instantiates a new Quarantine inspection.
     */
// Construtor vazio
    public QuarantineInspection() {}

    /**
     * Gets inspection id.
     *
     * @return the inspection id
     */
// Getters e Setters
    public String getInspectionId() { return inspectionId; }

    /**
     * Sets inspection id.
     *
     * @param inspectionId the inspection id
     */
    public void setInspectionId(String inspectionId) { this.inspectionId = inspectionId; }

    /**
     * Gets inspected at.
     *
     * @return the inspected at
     */
    public LocalDateTime getInspectedAt() { return inspectedAt; }

    /**
     * Sets inspected at.
     *
     * @param inspectedAt the inspected at
     */
    public void setInspectedAt(LocalDateTime inspectedAt) { this.inspectedAt = inspectedAt; }

    /**
     * Gets return id.
     *
     * @return the return id
     */
    public String getReturnId() { return returnId; }

    /**
     * Sets return id.
     *
     * @param returnId the return id
     */
    public void setReturnId(String returnId) { this.returnId = returnId; }

    /**
     * Gets action.
     *
     * @return the action
     */
    public QAAction getAction() { return action; }

    /**
     * Sets action.
     *
     * @param action the action
     */
    public void setAction(QAAction action) { this.action = action; }

    @Override
    public String toString() {
        return "Domain.QuarantineInspection{" +
                "inspectionId='" + inspectionId + '\'' +
                ", inspectedAt=" + inspectedAt +
                ", returnId='" + returnId + '\'' +
                ", action=" + action +
                '}';
    }
}