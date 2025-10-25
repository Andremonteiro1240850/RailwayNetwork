package Repository;

import Domain.Trolley;
import Domain.PickingItems; // Necessário para a assinatura do construtor
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe {@link TrolleyRepository},
 * focados nas operações de salvaguarda e acesso em memória,
 * verificando o uso da lista interna e cópias defensivas.
 */
class TrolleyRepositoryTest {

    private TrolleyRepository repository;
    private Trolley trolleyA;
    private Trolley trolleyB;
    private List<Trolley> initialTrolleys;

    // Dados de mock para PickingItems (já que não temos a classe implementada)
    private final List<PickingItems> DUMMY_PICKING_ITEMS = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // Inicializa o repositório antes de cada teste
        repository = new TrolleyRepository();

        // Cria objetos Trolley válidos usando o construtor completo
        trolleyA = new Trolley(
                100,
                50.0,
                1,
                DUMMY_PICKING_ITEMS,
                "FF"
        );
        trolleyB = new Trolley(
                200,
                100.0,
                2,
                DUMMY_PICKING_ITEMS,
                "BFD"
        );

        // Lista inicial para salvaguarda
        initialTrolleys = new ArrayList<>();
        initialTrolleys.add(trolleyA);
        initialTrolleys.add(trolleyB);
    }

    // ------------------------------------
    // TESTES DE saveTrolley()
    // ------------------------------------

    @Test
    @DisplayName("SAVE: Deve adicionar a lista de carrinhos ao repositório.")
    void saveTrolley_AddsAll() {
        // Act
        repository.saveTrolley(initialTrolleys);

        // Assert
        assertEquals(2, repository.getTrolley().size(),
                "O repositório deve ter 2 carrinhos após a salvaguarda inicial.");
        assertTrue(repository.getTrolley().contains(trolleyA));
    }

    @Test
    @DisplayName("SAVE: Deve adicionar elementos a uma lista existente (append behavior).")
    void saveTrolley_AppendsToExisting() {
        // Arrange
        repository.saveTrolley(List.of(trolleyA)); // Salva 1

        Trolley trolleyC = new Trolley(150, 75.0, 3, DUMMY_PICKING_ITEMS, "RANDOM");
        List<Trolley> secondBatch = List.of(trolleyB, trolleyC);

        // Act
        repository.saveTrolley(secondBatch); // Salva 2 adicionais

        // Assert
        List<Trolley> allTrolleys = repository.getTrolley();
        assertEquals(3, allTrolleys.size(),
                "O tamanho total deve ser 3 após duas operações de salvaguarda.");
        assertTrue(allTrolleys.contains(trolleyA));
        assertTrue(allTrolleys.contains(trolleyB));
        assertTrue(allTrolleys.contains(trolleyC));
    }

    @Test
    @DisplayName("SAVE: Deve suportar o salvamento de uma lista vazia sem erro.")
    void saveTrolley_EmptyList() {
        // Arrange: O repositório está vazio

        // Act & Assert
        assertDoesNotThrow(() -> repository.saveTrolley(new ArrayList<>()));
        assertTrue(repository.getTrolley().isEmpty());
    }

    // ------------------------------------
    // TESTES DE getTrolley()
    // ------------------------------------

    @Test
    @DisplayName("GET: getTrolley deve retornar uma lista vazia quando o repositório está vazio.")
    void getTrolley_Empty() {
        // Act
        List<Trolley> result = repository.getTrolley();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("GET: getTrolley deve retornar uma CÓPIA defensiva da lista interna.")
    void getTrolley_DefensiveCopyCheck() {
        // Arrange
        repository.saveTrolley(initialTrolleys);
        int initialSize = repository.getTrolley().size(); // 2

        // Act
        List<Trolley> externalList = repository.getTrolley();
        Trolley externalTrolley = new Trolley(50, 50, 999, DUMMY_PICKING_ITEMS, "NEW");
        externalList.add(externalTrolley); // Modifica a cópia externa

        // Assert: A lista interna não deve ser afetada pela modificação da cópia
        List<Trolley> internalListCheck = repository.getTrolley();
        assertEquals(initialSize, internalListCheck.size(),
                "A lista interna deve manter o seu tamanho original.");
        assertFalse(internalListCheck.contains(externalTrolley),
                "O carrinho adicionado à cópia externa não deve estar na lista interna.");
        assertNotSame(externalList, internalListCheck, "Deve retornar uma nova instância de lista.");
    }


    // ------------------------------------
    // TESTES DE getAllTrolleys()
    // ------------------------------------

    @Test
    @DisplayName("GET: getAllTrolleys deve retornar uma CÓPIA defensiva, como getTrolley.")
    void getAllTrolleys_DefensiveCopyCheck() {
        // Arrange
        repository.saveTrolley(initialTrolleys);
        int initialSize = repository.getAllTrolleys().size(); // 2

        // Act
        List<Trolley> externalList = repository.getAllTrolleys();
        externalList.clear(); // Limpa a cópia externa

        // Assert: A lista interna deve permanecer inalterada
        List<Trolley> internalListCheck = repository.getAllTrolleys();
        assertEquals(initialSize, internalListCheck.size(),
                "A lista interna deve manter o seu tamanho original após a modificação da cópia externa.");
        assertFalse(internalListCheck.isEmpty());
        assertNotSame(externalList, internalListCheck, "Deve retornar uma nova instância de lista.");
    }
}