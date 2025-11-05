package Controller;

import Domain.*;
import Domain.ENUM.AllocationMode;
import Domain.ENUM.AllocationStatus;
import Repository.BoxRepository;
import Repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WarehouseControllerTest {

    // Repositórios concretos reais
    private WarehouseRepository warehouseRepository;
    private BoxRepository boxRepository;
    private WarehouseController controller;

    // Dados de domínio
    private Order orderHighPriority;
    private Order orderLowPriority;
    private Box boxA;
    private Box boxB;
    private Box boxC;

    @BeforeEach
    void setUp() {
        // 1. Inicializar os repositórios reais (concretos)
        warehouseRepository = new WarehouseRepository();
        boxRepository = new BoxRepository();

        // Limpar o estado anterior para garantir isolamento
        warehouseRepository.clearAllData();
        boxRepository.clear();

        // O controller é construído com o repositório real do Warehouse.
        // O construtor de Domain.Warehouse agora requer o WarehouseRepository.
        controller = new WarehouseController(new Warehouse(warehouseRepository), warehouseRepository);

        // 2. Criação dos Dados de Teste

        BayId bayId1 = new BayId("W1", 1, 1);

        // CORREÇÃO: Usar LocalDate para a data de validade (Expira)
        LocalDate earlierExpiry = LocalDate.now().plusDays(5); // FEFO
        LocalDate laterExpiry = LocalDate.now().plusDays(10);
        LocalDateTime receivedNow = LocalDateTime.now(); // Usar LocalDateTime para a data de Recebimento

        // Boxes (Criadas sem localização)
        // Construtor: (BoxId, SKU, Qty, ExpiryDate (LocalDate), ReceivedDate (LocalDateTime))
        boxA = new Box("BOX-A", "SKU-1", 10, earlierExpiry, receivedNow);
        boxB = new Box("BOX-B", "SKU-1", 20, laterExpiry, receivedNow);
        boxC = new Box("BOX-C", "SKU-2", 5, earlierExpiry, receivedNow);

        // 3. Injetar Boxes no BoxRepository e atribuir localização (Assigned Boxes)

        // SKU-1: Adicionar e atribuir
        boxRepository.addUnassignedBox(boxA);
        boxRepository.assignBoxToBay(boxA, bayId1); // Atribuído ao bayId1

        boxRepository.addUnassignedBox(boxB);
        boxRepository.assignBoxToBay(boxB, new BayId("W1", 1, 2)); // Atribuído a outro bay

        // SKU-2: Adicionar e atribuir
        boxRepository.addUnassignedBox(boxC);
        boxRepository.assignBoxToBay(boxC, bayId1); // Atribuído ao bayId1

        // O stock total para estadoEncomenda (WarehouseRepository) é o mesmo stock atribuído
        Map<String, List<Box>> assignedStock = boxRepository.getAllAssignedBoxes();
        warehouseRepository.saveStock(assignedStock);


        // 4. Criação e Gravação das Encomendas (Orders)

        // ORD-H (Pri 1): Requer 5 (SKU-1) e 10 (SKU-2 - parcial)
        OrderLine line1 = new OrderLine(1, "SKU-1", 5);
        OrderLine line3 = new OrderLine(3, "SKU-2", 10);
        orderHighPriority = new Order("ORD-H", LocalDateTime.now().plusDays(1), 1);
        orderHighPriority.setLines(Arrays.asList(line1, line3));

        // ORD-L (Pri 2): Requer 15 (SKU-1)
        OrderLine line2 = new OrderLine(2, "SKU-1", 15);
        orderLowPriority = new Order("ORD-L", LocalDateTime.now().plusDays(5), 2);
        orderLowPriority.setLines(Collections.singletonList(line2));

        warehouseRepository.saveOrders(Arrays.asList(orderHighPriority, orderLowPriority));
    }

    // ----------------------------------------------------------------------------------
    // TESTES DA FUNÇÃO PRINCIPAL: processOrderEligibilityAndAllocation (USEI02)
    // ----------------------------------------------------------------------------------

    @Test
    @DisplayName("USEI02: Alocação STRICT - Deve falhar alocação parcial (SKU-2) e zerar a linha (Allocated=0).")
    void processOrderEligibilityAndAllocation_Strict() {
        // ACT
        controller.processOrderEligibilityAndAllocation(boxRepository, AllocationMode.STRICT);

        // ASSERT: Verificar o estado final no repositório
        List<EligibilityResult> results = warehouseRepository.getEligibilityResults();

        // ORD-H (Linha 3): Req: 10 (SKU-2). Stock: 5. Falha em STRICT. Alloc: 0.
        EligibilityResult resH3 = results.stream()
                .filter(r -> r.getOrderId().equals("ORD-H") && r.getLineNo() == 3)
                .findFirst().orElseThrow();
        assertEquals(AllocationStatus.UNDISPATCHABLE, resH3.getStatus());
        assertEquals(0, resH3.getAllocatedQty());

        // Total de Allocation Rows: (ORD-H, L1) + (ORD-L, L2) = 3 linhas (L3 zerada)
        assertEquals(3, warehouseRepository.getAllocationRows().size(), "Deve haver 3 linhas de alocação gravadas.");
    }

    @Test
    @DisplayName("USEI02: Alocação PARTIAL - Deve permitir alocação parcial e usar FEFO/FIFO.")
    void processOrderEligibilityAndAllocation_Partial() {
        // ARRANGE: Re-setup para garantir isolamento
        setUp();

        // ACT
        controller.processOrderEligibilityAndAllocation(boxRepository, AllocationMode.PARTIAL);

        // ASSERT: Verificar o estado final no repositório
        List<EligibilityResult> results = warehouseRepository.getEligibilityResults();

        // ORD-H (Linha 3): Req: 10, Stock: 5. Alocado: 5. Status: PARTIAL.
        EligibilityResult resH3 = results.stream()
                .filter(r -> r.getOrderId().equals("ORD-H") && r.getLineNo() == 3)
                .findFirst().orElseThrow();
        assertEquals(AllocationStatus.PARTIAL, resH3.getStatus());
        assertEquals(5, resH3.getAllocatedQty());

        // Total de Allocation Rows gravadas: 4 (todas as linhas tiveram alguma alocação)
        List<AllocationRow> allRows = warehouseRepository.getAllocationRows();
        assertEquals(4, allRows.size(), "Deve haver 4 linhas de alocação gravadas.");

        // Verificar FEFO/FIFO para ORD-H (L1)
        AllocationRow rowH1 = allRows.stream()
                .filter(r -> r.getOrderId().equals("ORD-H") && r.getLineNo() == 1)
                .findFirst().orElseThrow();
        // A BOX-A expira mais cedo, deve ser a primeira a ser usada
        assertEquals("BOX-A", rowH1.getBoxId(), "A BOX-A (FEFO) deve ser usada primeiro.");
    }

    @Test
    @DisplayName("ESTADO: estadoEncomenda deve calcular o estado corretamente (simula PARTIAL) e gravar resultados.")
    void estadoEncomenda() {
        // ACT
        List<EligibilityResult> estados = controller.estadoEncomenda();

        // ASSERT: Deve ter o resultado do modo PARTIAL
        assertEquals(3, estados.size(), "Deve ter 3 estados de elegibilidade.");

        // Verificação: ORD-H (Linha 3) deve estar PARTIAL (5/10)
        EligibilityResult resH3 = estados.stream()
                .filter(r -> r.getOrderId().equals("ORD-H") && r.getLineNo() == 3)
                .findFirst().orElseThrow();

        assertEquals(AllocationStatus.PARTIAL, resH3.getStatus());
        assertEquals(5, resH3.getAllocatedQty());

        // O repositório deve ter gravado os resultados
        assertEquals(3, warehouseRepository.getEligibilityResults().size(), "O repositório deve ter gravado 3 resultados de elegibilidade.");
    }

    @Test
    @DisplayName("CREATE ROWS: createAllocationRows deve gerar as linhas e gravá-las, simulando alocação PARTIAL e ordem FEFO.")
    void createAllocationRows() {
        // ACT
        controller.createAllocationRows(boxRepository);

        // ASSERTIONS GERAIS
        List<AllocationRow> savedRows = warehouseRepository.getAllocationRows();

        // Total esperado: 4 linhas de alocação (idêntico ao modo PARTIAL)
        assertEquals(4, savedRows.size(), "O repositório deve ter gravado 4 linhas de alocação.");

        // Verifica a alocação de ORD-H/L1
        AllocationRow rowH1 = savedRows.stream()
                .filter(r -> r.getOrderId().equals("ORD-H") && r.getLineNo() == 1)
                .findFirst().orElseThrow();
        assertEquals("BOX-A", rowH1.getBoxId(), "A BOX-A (FEFO) deve ser usada para a primeira linha.");
        assertEquals(5, rowH1.getQty());
    }
}