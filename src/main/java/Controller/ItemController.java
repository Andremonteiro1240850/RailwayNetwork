package Controller;

import Domain.QuarantineInspection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import Controller.


public class ItemController {

    InspectionController inspectionController;

    public ItemController(InspectionController inspectionController) {
        this.inspectionController = inspectionController;
    }

    /**
     * US05 - Processa uma devolução e encaminha para quarentena
     */
    public String processReturn(String sku, int quantity, String returnReason, LocalDate expiryDate) {
        try {
            // Validações
            if (sku == null || sku.trim().isEmpty()) {
                return "ERRO: SKU não pode estar vazio";
            }

            if (quantity <= 0) {
                return "ERRO: Quantidade deve ser maior que zero";
            }

            if (returnReason == null || returnReason.trim().isEmpty()) {
                return "ERRO: Motivo da devolução não pode estar vazio";
            }

            // Valida motivos permitidos
            List<String> validReasons = Arrays.asList("REMORSE", "DAMAGED", "EXPIRED", "CYCLE_COUNT");
            if (!validReasons.contains(returnReason.toUpperCase())) {
                return "ERRO: Motivo inválido. Use: REMORSE, DAMAGED, EXPIRED ou CYCLE_COUNT";
            }

            // Encaminha para quarentena
            String result = inspectionController.processQuarantine(sku, quantity, returnReason, expiryDate);

            return result;

        } catch (Exception e) {
            return "ERRO ao processar devolução: " + e.getMessage();
        }
    }

    /**
     * Lista devoluções por SKU
     */
    public List<QuarantineInspection> getReturnsBySku(String sku) {
        return inspectionController.getInspectionsBySku(sku);
    }
}