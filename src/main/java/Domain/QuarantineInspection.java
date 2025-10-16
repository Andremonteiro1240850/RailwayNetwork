package Domain;

import Domain.ENUM.QAAction;

public class QuarantineInspection {
    int id;
    int inspectedAt;
    int returnId;
    QAAction action;

    public QuarantineInspection(int id, QAAction action, int inspectedAt, int returnId) {
        this.id = id;
        this.action = action;
        this.inspectedAt = inspectedAt;
        this.returnId = returnId;
    }
    public int getId() {return id;}
    public int getInspectedAt() {return inspectedAt;}
    public int getReturnId() {return returnId;}
    public QAAction getAction() {return action;}
    public void setId(int id) {this.id = id;}
    public void setInspectedAt(int inspectedAt) {this.inspectedAt = inspectedAt;}
    public void setReturnId(int returnId) {this.returnId = returnId;}

    @Override
    public String toString() {
        return "Domain.QuarantineInspection{" +
                "Id: " + id +
                ", Inspected at: " + inspectedAt +
                ", Return Id: " + returnId +
                ", Action: " + action +
                '}';
    }
}
