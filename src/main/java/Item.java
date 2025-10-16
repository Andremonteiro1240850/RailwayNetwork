public class Item {
    String sku, name, category;
    double volume, weight;
    Item(String sku, String name, String category, double volume, double weight) {
        this.sku = sku; this.name = name; this.category = category;
        this.volume = volume; this.weight = weight;
    }
    public String getSku() {
        return sku;
    }
    public void setSku(String sku) {
        this.sku = sku;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public double getVolume() {
        return volume;
    }
    public void setVolume(double volume) {
        this.volume = volume;
    }
    public double getWeight() {
        return weight;
    }
    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Item{" +
                "SKU: '" + sku + '\'' +
                ", Name: " + name + '\'' +
                ", Category: " + category + '\'' +
                ", Volume: " + volume +
                ", Weight: " + weight +
                '}';
    }
}
