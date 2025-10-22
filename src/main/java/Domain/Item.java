package Domain;

import java.util.Collection;
import java.util.Objects;

/**
 * The type Item.
 */
public class Item {
    private final String sku;
    private String name, category, unit;
    private double volume, weight;

    /**
     * Instantiates a new Item.
     *
     * @param sku      the sku
     * @param name     the name
     * @param category the category
     * @param unit     the unit
     * @param volume   the volume
     * @param weight   the weight
     */
    public Item(String sku, String name, String category, String unit, double volume, double weight) {
        this.sku = sku; 
        this.name = name; 
        this.category = category; 
        this.unit = unit; 
        this.volume = volume; 
        this.weight = weight;
    }

    /**
     * Gets sku.
     *
     * @return the sku
     */
    public String getSku() {
        return sku;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets category.
     *
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets category.
     *
     * @param category the category
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Gets unit.
     *
     * @return the unit
     */
    public String getUnit() {return unit;}

    /**
     * Sets unit.
     *
     * @param unit the unit
     */
    public void setUnit(String unit) {this.unit = unit;}

    /**
     * Gets volume.
     *
     * @return the volume
     */
    public double getVolume() {
        return volume;
    }

    /**
     * Sets volume.
     *
     * @param volume the volume
     */
    public void setVolume(double volume) {
        this.volume = volume;
    }

    /**
     * Gets weight.
     *
     * @return the weight
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Sets weight.
     *
     * @param weight the weight
     */
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
