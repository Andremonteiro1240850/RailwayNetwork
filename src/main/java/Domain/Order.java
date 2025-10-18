package Domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order implements Comparable<Order> {
    String id;
    LocalDateTime dueDate;
    int priority;
    List<OrderLine> lines = new ArrayList<>();
    Order(String id, LocalDateTime dueDate, int priority) {
        this.id = id; this.dueDate = dueDate; this.priority = priority;
    }
    public String getId() {
        return id;
    }
    public LocalDateTime getDueDate() {
        return dueDate;
    }
    public int getPriority() {
        return priority;
    }
    public List<OrderLine> getLines() {
        return lines;
    }
    public void setLines(List<OrderLine> lines) {
        this.lines = lines;
    }
    public void addLine(OrderLine line) {
        lines.add(line);
    }
    
    @Override
    public int compareTo(Order other) {
        // Priority ASC, dueDate ASC, orderId ASC
        int priorityCmp = Integer.compare(this.priority, other.priority);
        if (priorityCmp != 0) return priorityCmp;
        
        int dateCmp = this.dueDate.compareTo(other.dueDate);
        if (dateCmp != 0) return dateCmp;
        
        return this.id.compareTo(other.id);
    }
}
