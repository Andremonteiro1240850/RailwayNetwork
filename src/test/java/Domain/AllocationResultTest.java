package Domain;

import Domain.ENUM.AllocationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AllocationResultTest {

    // Listas de dummy/placeholder para usar no setup
    private List<EligibilityResult> initialEligibilityResults;
    private List<AllocationRow> initialAllocationRows;
    private AllocationResult allocationResult;

    @BeforeEach
    void setUp() {
        // ARRANGE: Configuração inicial para cada teste.

        // 1. Setup para EligibilityResult (CORRIGIDO: 6 argumentos na ordem correta)
        // Construtor correto: (orderId, lineNo, sku, requestedQty, allocatedQty, status)
        int requestedQty = 10;
        int allocatedQty = 10;
        EligibilityResult dummyResult = new EligibilityResult(
                "ORD-H",
                1,
                "SKU-1",
                requestedQty,
                allocatedQty,
                AllocationStatus.ELIGIBLE
        );

        // 2. Setup para AllocationRow (7 argumentos)
        // Construtor: (orderId, lineNo, sku, qty, boxId, aisleID, bayID)
        AllocationRow dummyRow = new AllocationRow("ORD-H", 1, "SKU-1", 10, "BOX-A", 1, 5);

        initialEligibilityResults = List.of(dummyResult);
        initialAllocationRows = List.of(dummyRow);

        allocationResult = new AllocationResult(initialEligibilityResults, initialAllocationRows);
    }

    // ----------------------------------------------------------------------
    // TESTES DE GETTERS
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("GET: getEligibilityResults deve retornar a lista de resultados de elegibilidade fornecida no construtor.")
    void getEligibilityResults() {
        // ACT
        List<EligibilityResult> actualResults = allocationResult.getEligibilityResults();

        // ASSERT
        // Verifica se é a mesma instância de lista (referência)
        assertSame(initialEligibilityResults, actualResults, "Deve retornar a mesma instância da lista de EligibilityResult.");
        assertEquals(1, actualResults.size());
    }

    @Test
    @DisplayName("GET: getAllocationRows deve retornar a lista de linhas de alocação fornecida no construtor.")
    void getAllocationRows() {
        // ACT
        List<AllocationRow> actualRows = allocationResult.getAllocationRows();

        // ASSERT
        // Verifica se é a mesma instância de lista (referência)
        assertSame(initialAllocationRows, actualRows, "Deve retornar a mesma instância da lista de AllocationRow.");
        assertEquals(1, actualRows.size());
    }

    // ----------------------------------------------------------------------
    // TESTES DE SETTERS
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("SET: setEligibilityResults deve atualizar a lista de resultados de elegibilidade.")
    void setEligibilityResults() {
        // ARRANGE
        List<EligibilityResult> newResults = Collections.emptyList();

        // ACT
        allocationResult.setEligibilityResults(newResults);

        // ASSERT
        List<EligibilityResult> actualResults = allocationResult.getEligibilityResults();

        // Verifica se a nova lista foi guardada
        assertSame(newResults, actualResults, "A lista de EligibilityResults deve ser atualizada para a nova instância.");
        assertTrue(actualResults.isEmpty());
    }

    @Test
    @DisplayName("SET: setAllocationRows deve atualizar a lista de linhas de alocação.")
    void setAllocationRows() {
        // ARRANGE
        List<AllocationRow> newRows = Collections.emptyList();

        // ACT.
        allocationResult.setAllocationRows(newRows);

        // ASSERT
        List<AllocationRow> actualRows = allocationResult.getAllocationRows();

        // Verifica se a nova lista foi guardada
        assertSame(newRows, actualRows, "A lista de AllocationRows deve ser atualizada para a nova instância.");
        assertTrue(actualRows.isEmpty());
    }
}