package Domain;

import java.util.*;

/**
 * Entidade de domínio PickStop - representa um local de picking no armazém.
 * Um PickStop é identificado por um par (aisle, bay) e tem uma quantidade total a recolher.
 * Contém a regra de negócio para consolidar stops duplicados.
 */
public class PickStop {
    private int seq;
    private int aisle;
    private int bay;
    private int totalQty;

    /**
     * Construtor para criar um PickStop.
     * Representa um local único no armazém onde é necessário recolher items.
     *
     * @param seq Número de sequência (pode ser alterado após ordenação)
     * @param aisle Número do corredor (aisle)
     * @param bay Número do bay dentro do corredor
     * @param totalQty Quantidade total a recolher neste local
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

    /**
     * Compara dois PickStops para verificar se são iguais.
     * Dois stops são iguais se tiverem o mesmo aisle e bay (mesmo local no armazém).
     * Usado para consolidar stops duplicados.
     *
     * @param obj Objeto a comparar
     * @return true se tiverem mesmo aisle e bay, false caso contrário
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PickStop other = (PickStop) obj;
        return aisle == other.aisle && bay == other.bay;
    }

    /**
     * Calcula código hash baseado em aisle e bay.
     * Necessário para usar PickStop em estruturas como HashMap.
     * Dois stops com mesmo aisle/bay têm o mesmo hash.
     *
     * @return Código hash baseado em aisle e bay
     */
    @Override
    public int hashCode() {
        return Objects.hash(aisle, bay);
    }

    @Override
    public String toString() {
        return "(" + aisle + "," + bay + ") Qty:" + totalQty;
    }

    /**
     * Consolida stops duplicados (mesmo aisle/bay) somando as quantidades.
     * Esta é uma REGRA DE NEGÓCIO que pertence ao domínio.
     * 
     * Se dois stops têm o mesmo aisle e bay, são consolidados num único stop
     * com a quantidade total (soma das quantidades).
     * 
     * Exemplo: [(1,2,qty:5), (1,2,qty:3), (2,3,qty:2)] → [(1,2,qty:8), (2,3,qty:2)]
     *
     * @param originalStops Lista original com possíveis duplicados (mesmo aisle/bay)
     * @return Lista consolidada sem duplicados (quantidades somadas)
     */
    public static List<PickStop> consolidateStops(List<PickStop> originalStops) {
        if (originalStops == null || originalStops.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, PickStop> consolidatedMap = new HashMap<>();

        for (PickStop stop : originalStops) {
            String key = stop.getAisle() + "-" + stop.getBay();

            if (consolidatedMap.containsKey(key)) {
                PickStop existing = consolidatedMap.get(key);
                existing.setTotalQty(existing.getTotalQty() + stop.getTotalQty());
            } else {
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
