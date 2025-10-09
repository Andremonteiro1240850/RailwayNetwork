import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    String id;
    LocalDateTime dueDate;
    int priority;
    List<OrderLine> lines = new ArrayList<>();
    Order(String id, LocalDateTime dueDate, int priority) {
        this.id = id; this.dueDate = dueDate; this.priority = priority;
    }
}
