package Sprint1.Domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Order.
 */
public class Order implements Comparable<Order> {
    /**
     * The Id.
     */
    String id;
    /**
     * The Due date.
     */
    LocalDateTime dueDate;
    /**
     * The Priority.
     */
    int priority;
    /**
     * The Lines.
     */
    List<OrderLine> lines = new ArrayList<>();

    /**
     * Instantiates a new Order.
     *
     * @param id       the id
     * @param dueDate  the due date
     * @param priority the priority
     */
    public Order(String id, LocalDateTime dueDate, int priority) {
        this.id = id; this.dueDate = dueDate; this.priority = priority;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Gets due date.
     *
     * @return the due date
     */
    public LocalDateTime getDueDate() {
        return dueDate;
    }

    /**
     * Gets priority.
     *
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Gets lines.
     *
     * @return the lines
     */
    public List<OrderLine> getLines() {
        return lines;
    }

    /**
     * Sets lines.
     *
     * @param lines the lines
     */
    public void setLines(List<OrderLine> lines) {
        this.lines = lines;
    }

    /**
     * Add line.
     *
     * @param line the line
     */
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
