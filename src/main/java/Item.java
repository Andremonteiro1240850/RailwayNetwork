public class Item {
    String sku, name, category;
    double volume, weight;
    Item(String sku, String name, String category, double volume, double weight) {
        this.sku = sku; this.name = name; this.category = category;
        this.volume = volume; this.weight = weight;
    }
}
