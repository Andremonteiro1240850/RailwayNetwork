import Domain.QuarantineInspection;
import Domain.ENUM.QAAction;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import Controller.ItemController;

public class InspectionController {

    private List<QuarantineInspection> quarantineInspections = new ArrayList<>();
    private List<String> auditLogs = new ArrayList<>();

    /**
     * US05 - Processa quarentena
     */
    public String processQuarantine(String sku, int quantity, String returnReason, LocalDate expiryDate) {
        try {
            // 1. Cria inspeção de quarentena
            QuarantineInspection inspection = createQuarantineInspection();

            // 2. Determina ação baseada no motivo
            QAAction action = determineAction(returnReason);
            inspection.setAction(action);

            // 3. Processa conforme a ação
            if (action == QAAction.RESTOCK) {
                processRestock(inspection, sku, quantity, expiryDate);
            } else {
                processDiscard(inspection, sku, quantity);
            }

            // 4. Adiciona à lista de inspeções (ordem inversa - mais recente primeiro)
            quarantineInspections.add(0, inspection);

            // 5. Cria audit log
            createAuditLog(inspection, sku, quantity, action, returnReason);

            return String.format(
                    "✅ QUARENTENA PROCESSADA\n" +
                            "📋 Inspection ID: %s\n" +
                            "📦 SKU: %s\n" +
                            "🔢 Quantidade: %d\n" +
                            "⚡ Ação: %s\n" +
                            "📝 Motivo: %s\n" +
                            "📅 Data: %s",
                    inspection.getInspectionId(), sku, quantity, action, returnReason, LocalDateTime.now()
            );

        } catch (Exception e) {
            return "❌ ERRO na quarentena: " + e.getMessage();
        }
    }

    /**
     * Cria nova inspeção de quarentena
     */
    private QuarantineInspection createQuarantineInspection() {
        QuarantineInspection inspection = new QuarantineInspection();
        inspection.setInspectionId(generateId("INSP"));
        inspection.setReturnId(generateId("RET"));
        inspection.setInspectedAt(LocalDateTime.now());
        return inspection;
    }

    /**
     * Determina ação baseada no motivo (conforme US05)
     */
    private QAAction determineAction(String returnReason) {
        switch (returnReason.toUpperCase()) {
            case "REMORSE":
            case "CYCLE_COUNT":
                return QAAction.RESTOCK;
            case "DAMAGED":
            case "EXPIRED":
                return QAAction.DISCARD;
            default:
                return QAAction.DISCARD;
        }
    }

    /**
     * Processa restock - cria nova box para inventory
     */
    private void processRestock(QuarantineInspection inspection, String sku, int quantity, LocalDate expiryDate) {
        // Aqui criarias uma nova Domain.Box para o inventory
        // seguindo as regras FEFO/FIFO da US01
        System.out.println("📥 RESTOCK: Criando box para SKU " + sku + ", quantidade: " + quantity);
    }

    /**
     * Processa discard
     */
    private void processDiscard(QuarantineInspection inspection, String sku, int quantity) {
        System.out.println("🗑️ DISCARD: Descartando SKU " + sku + ", quantidade: " + quantity);
    }

    /**
     * Cria log de auditoria (formato da US05)
     */
    private void createAuditLog(QuarantineInspection inspection, String sku, int quantity,
                                QAAction action, String returnReason) {
        String timestamp = LocalDateTime.now().toString();
        String logEntry = String.format("%s | returnId=%s | sku=%s | action=%s | qty=%d",
                timestamp, inspection.getReturnId(), sku, action, quantity);

        auditLogs.add(logEntry);
        System.out.println("📋 AUDIT LOG: " + logEntry);
    }

    /**
     * Gera ID único
     */
    private String generateId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Obtém inspeções por SKU
     */
    public List<QuarantineInspection> getInspectionsBySku(String sku) {
        // Para simplificar, retorna todas
        // Na implementação real, filtraria por SKU
        return new ArrayList<>(quarantineInspections);
    }

    /**
     * Obtém logs de auditoria
     */
    public List<String> getAuditLogs() {
        return new ArrayList<>(auditLogs);
    }
}