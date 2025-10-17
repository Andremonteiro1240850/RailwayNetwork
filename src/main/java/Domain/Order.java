package Domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Order implements Comparable<Order> {
    private final String id;
    private final LocalDateTime dueDate;
    private final int priority;
    private final List<OrderLine> lines = new ArrayList<>();

    public Order(String id, LocalDateTime dueDate, int priority) {
        this.id = id;
        this.dueDate = dueDate;
        this.priority = priority;
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
        return new ArrayList<>(lines);
    }

    public void setLines(List<OrderLine> newLines) {
        lines.clear();
        if (newLines != null) {
            lines.addAll(newLines);
        }
    }

    public void addLine(OrderLine line) {
        if (line != null) {
            lines.add(line);
        }
    }

    @Override
    public int compareTo(Order other) {
        if (other == null) return 1;
        return Comparator
                .comparingInt(Order::getPriority)
                .thenComparing(Order::getDueDate)
                .thenComparing(Order::getId)
                .compare(this, other);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", dueDate=" + dueDate +
                ", priority=" + priority +
                ", lines=" + lines.size() +
                '}';
    }
}
