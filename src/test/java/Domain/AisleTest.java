package Domain;

import Repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AisleTest {

    private WarehouseRepository warehouseRepository;
    private Warehouse mockWarehouse; // Usamos a classe concreta, mas como mock/stub implícito
    private Aisle aisle;

    @BeforeEach
    void setUp() {
        // ARRANGE: Configuração inicial para todos os testes.

        // Como a classe Warehouse requer um WarehouseRepository,
        // instanciamos o seu repositório concreto (in-memory)
        warehouseRepository = new WarehouseRepository();

        // Instanciamos o Warehouse, que será injetado no Aisle.
        mockWarehouse = new Warehouse(warehouseRepository);

        // Instanciamos o Aisle com um ID inicial
        int initialId = 101;
        aisle = new Aisle(initialId, mockWarehouse);
    }

    @Test
    @DisplayName("Teste: O método getId deve retornar o ID correto do Aisle.")
    void getId_ShouldReturnCorrectInitialId() {
        // ACT
        int resultId = aisle.getId();

        // ASSERT
        assertEquals(101, resultId, "O ID retornado deve ser 101, conforme definido no setup.");
    }

    @Test
    @DisplayName("Teste: O método setId deve atualizar o ID do Aisle.")
    void setId_ShouldUpdateAisleId() {
        // ARRANGE
        int newId = 202;

        // ACT
        aisle.setId(newId);
        int resultId = aisle.getId();

        // ASSERT
        assertEquals(newId, resultId, "O ID deve ser atualizado para 202 após a chamada a setId.");
    }

    @Test
    @DisplayName("Teste: O método getWarehouse deve retornar a instância de Warehouse injetada.")
    void getWarehouse_ShouldReturnInjectedWarehouseInstance() {
        // ACT
        Warehouse resultWarehouse = aisle.getWarehouse();

        // ASSERT
        assertNotNull(resultWarehouse, "O Warehouse retornado não deve ser nulo.");
        assertSame(mockWarehouse, resultWarehouse, "O objeto Warehouse retornado deve ser a mesma instância injetada no construtor.");
    }
}