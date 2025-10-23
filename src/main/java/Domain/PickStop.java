package Domain;

import java.util.*;

/**
 * The type Pick stop.
 */
public class PickStop {
    private int seq;
    private int aisle;
    private int bay;
    private int totalQty;

    /**
     * Instantiates a new Pick stop.
     *
     * @param seq      the seq
     * @param aisle    the aisle
     * @param bay      the bay
     * @param totalQty the total qty
     */
    public PickStop(int seq, int aisle, int bay, int totalQty) {
        this.seq = seq;
        this.aisle = aisle;
        this.bay = bay;
        this.totalQty = totalQty;
    }

    /**
     * Gets seq.
     *
     * @return the seq
     */
    public int getSeq() {
        return seq;
    }

    /**
     * Sets seq.
     *
     * @param seq the seq
     */
    public void setSeq(int seq) {
        this.seq = seq;
    }

    /**
     * Gets aisle.
     *
     * @return the aisle
     */
    public int getAisle() {
        return aisle;
    }

    /**
     * Sets aisle.
     *
     * @param aisle the aisle
     */
    public void setAisle(int aisle) {
        this.aisle = aisle;
    }

    /**
     * Gets bay.
     *
     * @return the bay
     */
    public int getBay() {
        return bay;
    }

    /**
     * Sets bay.
     *
     * @param bay the bay
     */
    public void setBay(int bay) {
        this.bay = bay;
    }

    /**
     * Gets total qty.
     *
     * @return the total qty
     */
    public int getTotalQty() {
        return totalQty;
    }

    /**
     * Sets total qty.
     *
     * @param totalQty the total qty
     */
    public void setTotalQty(int totalQty) {
        this.totalQty = totalQty;
    }

    // Para merge de duplicados - equals e hashCode baseados em aisle e bay
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PickStop other = (PickStop) obj;
        return aisle == other.aisle && bay == other.bay;
    }

    @Override
    public int hashCode() {
        return Objects.hash(aisle, bay);
    }

    @Override
    public String toString() {
        return "(" + aisle + "," + bay + ") Qty:" + totalQty;
    }

    /**
     * Consolida stops duplicados (mesmo aisle/bay) somando as quantidades
     * Esta é uma regra de negócio que pertence ao domínio
     *
     * @param originalStops Lista original com possíveis duplicados
     * @return Lista consolidada sem duplicados
     */
    public static List<PickStop> consolidateStops(List<PickStop> originalStops) {
        if (originalStops == null || originalStops.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, PickStop> consolidatedMap = new HashMap<>();

        for (PickStop stop : originalStops) {
            String key = stop.getAisle() + "-" + stop.getBay();

            if (consolidatedMap.containsKey(key)) {
                // Já existe - soma a quantidade
                PickStop existing = consolidatedMap.get(key);
                existing.setTotalQty(existing.getTotalQty() + stop.getTotalQty());
            } else {
                // Novo - cria uma cópia
                consolidatedMap.put(key, new PickStop(
                        stop.getSeq(),
                        stop.getAisle(),
                        stop.getBay(),
                        stop.getTotalQty()
                ));
            }
        }

        return new ArrayList<>(consolidatedMap.values());
    }
}
