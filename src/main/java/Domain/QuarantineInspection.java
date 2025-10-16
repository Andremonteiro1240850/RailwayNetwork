package Domain;

import Domain.ENUM.QAAction;
import java.time.LocalDateTime;

public class QuarantineInspection {
    private String inspectionId;
    private LocalDateTime inspectedAt;
    private String returnId;
    private QAAction action;

    // Construtor
    public QuarantineInspection(String inspectionId, String returnId, LocalDateTime inspectedAt, QAAction action) {
        this.inspectionId = inspectionId;
        this.returnId = returnId;
        this.inspectedAt = inspectedAt;
        this.action = action;
    }

    // Construtor vazio
    public QuarantineInspection() {}

    // Getters e Setters
    public String getInspectionId() { return inspectionId; }
    public void setInspectionId(String inspectionId) { this.inspectionId = inspectionId; }

    public LocalDateTime getInspectedAt() { return inspectedAt; }
    public void setInspectedAt(LocalDateTime inspectedAt) { this.inspectedAt = inspectedAt; }

    public String getReturnId() { return returnId; }
    public void setReturnId(String returnId) { this.returnId = returnId; }

    public QAAction getAction() { return action; }
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