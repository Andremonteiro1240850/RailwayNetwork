package Domain;

import java.util.Objects;

public class Item {
    private final String sku;
    private String name, category, unit;
    private double volume, weight;
    
    public Item(String sku, String name, String category, String unit, double volume, double weight) {
        this.sku = sku; 
        this.name = name; 
        this.category = category; 
        this.unit = unit; 
        this.volume = volume; 
        this.weight = weight;
    }
    public String getSku() {
        return sku;
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
    public String getUnit() {return unit;}
    public void setUnit(String unit) {this.unit = unit;}
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item other = (Item) o;
        return Objects.equals(this.sku, other.sku);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sku);
    }

    @Override
    public String toString() {
        return "Item{" + sku + " - " + name + "}";
    }
}
