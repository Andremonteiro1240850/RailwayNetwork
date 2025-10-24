package Repository;

import Domain.Trolley;

import java.util.ArrayList;
import java.util.List;

/**
 * Repositório em memória simples para testes do PickPathController
 * Implementa apenas os métodos necessários para os testes
 */
public class InMemoryTrolleyRepository {
    
    private final List<Trolley> trolleyList = new ArrayList<>();

    /**
     * Save trolley.
     *
     * @param trolleyList the trolley list
     */
    public void saveTrolley(List<Trolley> trolleyList) {
        this.trolleyList.addAll(trolleyList);
    }

    /**
     * Gets trolley.
     *
     * @return the trolley
     */
    public List<Trolley> getTrolley() {
        return new ArrayList<>(trolleyList);
    }
    
    /**
     * Gets all trolleys.
     *
     * @return the all trolleys
     */
    public List<Trolley> getAllTrolleys() {
        return new ArrayList<>(trolleyList);
    }

    /**
     * Clear all trolleys (useful for test cleanup)
     */
    public void clear() {
        trolleyList.clear();
    }

    /**
     * Get trolley count
     *
     * @return the number of trolleys
     */
    public int size() {
        return trolleyList.size();
    }

    /**
     * Check if repository is empty
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return trolleyList.isEmpty();
    }
}