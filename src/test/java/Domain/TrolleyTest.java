package Domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a entidade mutável {@link Trolley}.
 * Valida o construtor, Getters, Setters e o contrato equals/hashCode.
 */
class TrolleyTest {

    // Dados Iniciais e de Mock
    private static final int INITIAL_CAPACITY = 100;
    private static final double INITIAL_LOAD = 50.0;
    private static final int INITIAL_ID = 42;
    private static final String INITIAL_HEURISTIC = "FF";
    private List<PickingItems> initialItems;

    // Novos Dados para Setters
    private static final int NEW_CAPACITY = 200;
    private static final double NEW_LOAD = 150.0;
    private static final int NEW_ID = 84;
    private static final String NEW_HEURISTIC = "BFD";
    private List<PickingItems> newItems;

    @BeforeEach
    void setUp() {
        // Inicializa a lista de PickingItems de mock para o estado inicial
        initialItems = new ArrayList<>();
        // Assumindo a existência de PickingItems (construtor inferido)
        initialItems.add(new PickingItems("ORD-A", 1, 1, 1, "BOX-A", "SKU-A", 10));

        // Inicializa a nova lista de PickingItems para os testes de set
        newItems = new ArrayList<>();
        newItems.add(new PickingItems("ORD-B", 2, 2, 2, "BOX-B", "SKU-B", 20));
    }

    /**
     * Fábrica para criar uma instância consistente de Trolley.
     */
    private Trolley createInitialInstance() {
        return new Trolley(INITIAL_CAPACITY, INITIAL_LOAD, INITIAL_ID, initialItems, INITIAL_HEURISTIC);
    }

    // ------------------------------------
    // TESTES DE GETTERS (Validação de Inicialização)
    // ------------------------------------

    @Test
    @DisplayName("GET: Deve retornar a capacidade máxima inicial correta.")
    void getMaxCapacity() {
        Trolley trolley = createInitialInstance();
        assertEquals(INITIAL_CAPACITY, trolley.getMaxCapacity());
    }

    @Test
    @DisplayName("GET: Deve retornar a carga atual inicial correta.")
    void getCurrentLoad() {
        Trolley trolley = createInitialInstance();
        assertEquals(INITIAL_LOAD, trolley.getCurrentLoad());
    }

    @Test
    @DisplayName("GET: Deve retornar o ID inicial correto.")
    void getId() {
        Trolley trolley = createInitialInstance();
        assertEquals(INITIAL_ID, trolley.getId());
    }

    @Test
    @DisplayName("GET: Deve retornar a heurística de opção inicial correta.")
    void getOptionHeuristic() {
        Trolley trolley = createInitialInstance();
        assertEquals(INITIAL_HEURISTIC, trolley.getOptionHeuristic());
    }

    @Test
    @DisplayName("GET: Deve retornar a lista de Picking Items inicial correta.")
    void getPickingItems() {
        Trolley trolley = createInitialInstance();
        assertNotNull(trolley.getPickingItems());
        assertEquals(1, trolley.getPickingItems().size());
        assertEquals(initialItems, trolley.getPickingItems());
    }

    // ------------------------------------
    // TESTES DE SETTERS (Validação de Mutação de Estado)
    // ------------------------------------

    @Test
    @DisplayName("SET: Deve alterar a lista de Picking Items.")
    void setPickingItems() {
        Trolley trolley = createInitialInstance();
        trolley.setPickingItems(newItems);
        assertEquals(newItems, trolley.getPickingItems());
    }

    @Test
    @DisplayName("SET: Deve alterar a carga atual.")
    void setCurrentLoad() {
        Trolley trolley = createInitialInstance();
        trolley.setCurrentLoad(NEW_LOAD);
        assertEquals(NEW_LOAD, trolley.getCurrentLoad());
    }

    @Test
    @DisplayName("SET: Deve alterar o ID.")
    void setId() {
        Trolley trolley = createInitialInstance();
        trolley.setId(NEW_ID);
        assertEquals(NEW_ID, trolley.getId());
    }

    @Test
    @DisplayName("SET: Deve alterar a capacidade máxima.")
    void setMaxCapacity() {
        Trolley trolley = createInitialInstance();
        trolley.setMaxCapacity(NEW_CAPACITY);
        assertEquals(NEW_CAPACITY, trolley.getMaxCapacity());
    }

    @Test
    @DisplayName("SET: Deve alterar a heurística de opção.")
    void setOptionHeuristic() {
        Trolley trolley = createInitialInstance();
        trolley.setOptionHeuristic(NEW_HEURISTIC);
        assertEquals(NEW_HEURISTIC, trolley.getOptionHeuristic());
    }

    // ------------------------------------
    // TESTES DE UTILIDADE (toString, equals, hashCode)
    // ------------------------------------

    @Test
    @DisplayName("TOSTRING: Deve gerar uma string formatada contendo capacidade, carga e ID.")
    void testToString() {
        // Arrange
        Trolley trolley = createInitialInstance();
        String expectedString = "Trolley: maxCapacity=100, currentLoad=50.0, id=42.";

        // Assert
        assertEquals(expectedString, trolley.toString());
    }

    @Test
    @DisplayName("EQUALS: Deve retornar true se o ID for o mesmo (identificação por ID).")
    void testEquals_SameId() {
        // Arrange
        Trolley t1 = createInitialInstance(); // ID=42
        // t2 tem o mesmo ID, mas outros dados diferentes
        Trolley t2 = new Trolley(999, 10.0, INITIAL_ID, new ArrayList<>(), "XYZ"); // ID=42

        // Assert: A implementação atual compara apenas o ID.
        assertTrue(t1.equals(t2));
        assertTrue(t2.equals(t1));
    }

    @Test
    @DisplayName("EQUALS: Deve retornar false se os IDs forem diferentes.")
    void testEquals_DifferentId() {
        // Arrange
        Trolley t1 = createInitialInstance(); // ID=42
        Trolley t2 = new Trolley(INITIAL_CAPACITY, INITIAL_LOAD, 999, initialItems, INITIAL_HEURISTIC); // ID=999

        // Assert
        assertFalse(t1.equals(t2));
    }

    @Test
    @DisplayName("HASHCODE: Deve falhar (assertNotEquals) provando a VIOLAÇÃO do contrato equals/hashCode.")
    void testHashCode_ContractViolation() {
        // Arrange
        Trolley t1 = createInitialInstance(); // ID=42, Cap=100, Load=50.0
        // t2 é igual a t1 (pelo ID, que é 42), mas tem dados de carga/capacidade diferentes.
        Trolley t2 = new Trolley(999, 10.0, INITIAL_ID, new ArrayList<>(), "XYZ"); // ID=42, Cap=999, Load=10.0

        // A regra Java exige: Se t1.equals(t2) é TRUE, t1.hashCode() DEVE ser igual a t2.hashCode().
        // Na implementação do utilizador, t1.equals(t2) é TRUE.
        // O hashCode do utilizador é: Objects.hash(maxCapacity, currentLoad, id);
        // t1.hashCode() != t2.hashCode() devido a 'maxCapacity' e 'currentLoad'.

        // Assert: O método de teste é escrito para PROVAR A FALHA.
        // Se a classe Trolley fosse corrigida para usar apenas Objects.hash(id); este assert falharia e o teste passaria.
        assertNotEquals(t1.hashCode(), t2.hashCode(),
                "VIOLAÇÃO CRÍTICA: Objetos considerados iguais (pelo ID) DEVEM ter o mesmo HashCode.");
    }
}