package UI;

import Controller.InspectionController;
import Domain.QuarantineInspection;

import java.util.List;

/**
 * The type Returns ui.
 */
public class ReturnsUI {

    private final InspectionController inspectionController;

    /**
     * Instantiates a new Returns ui.
     *
     * @param inspectionController the inspection controller
     */
    public ReturnsUI(InspectionController inspectionController) {
        this.inspectionController = inspectionController;
    }

    /**
     * Show dashboard.
     */
    public void showDashboard() {
        println();
        println(center("Returns Dashboard", 86));
        println();
        renderInspectionsTable();
        System.out.println();
        renderAuditLogTable();
    }

    private void renderInspectionsTable() {
        List<QuarantineInspection> inspections = inspectionController.getInspectionsBySku("");
        String[] cols = {"InspectionID", "ReturnID", "Action", "InspectedAt"};
        int[] widths = {16, 12, 10, 24};
        printTableHeader("Inspections", cols, widths);
        if (inspections.isEmpty()) {
            printEmptyRow(widths, "No inspections available");
            printTableFooter(widths);
            return;
        }
        for (QuarantineInspection i : inspections) {
            printRow(widths,
                    safe(i.getInspectionId()),
                    safe(i.getReturnId()),
                    i.getAction() == null ? "-" : i.getAction().name(),
                    i.getInspectedAt() == null ? "-" : i.getInspectedAt().toString());
        }
        printTableFooter(widths);
    }

    private void renderAuditLogTable() {
        List<String> logs = inspectionController.getAuditLogs();
        String[] cols = {"Timestamp", "ReturnId", "SKU", "Action", "Qty"};
        int[] widths = {17, 12, 10, 10, 6};
        printTableHeader("Audit Log", cols, widths);
        if (logs.isEmpty()) {
            printEmptyRow(widths, "No audit entries");
            printTableFooter(widths);
            return;
        }
        for (String l : logs) {
            String ts = "-", returnId = "-", sku = "-", action = "-", qty = "-";
            String[] parts = l.split("\\|");
            if (parts.length >= 1) {
                ts = parts[0].trim();
                for (int i = 1; i < parts.length; i++) {
                    String p = parts[i].trim();
                    int eq = p.indexOf('=');
                    if (eq > 0) {
                        String key = p.substring(0, eq).trim();
                        String val = p.substring(eq + 1).trim();
                        String k = key.toLowerCase();
                        if ("returnid".equals(k)) returnId = val;
                        else if ("sku".equals(k)) sku = val;
                        else if ("action".equals(k)) action = val;
                        else if ("qty".equals(k) || "quantity".equals(k)) qty = val;
                        // else ignore
                    }
                }
            }
            printRow(widths, ts, returnId, sku, action, qty);
        }
        printTableFooter(widths);
    }

    private void printTableHeader(String title, String[] cols, int[] widths) {
        String border = borderTop(widths);
        println(border);
        println(titleRow(title, widths));
        println(borderSep(widths));
        printRow(widths, cols);
        println(borderSep(widths));
    }

    private void printTableFooter(int[] widths) {
        println(borderBottom(widths));
    }

    private void printRow(int[] widths, String... values) {
        StringBuilder sb = new StringBuilder();
        sb.append('│');
        for (int i = 0; i < widths.length; i++) {
            String v = i < values.length ? values[i] : "";
            sb.append(" ").append(padRight(v, widths[i])).append(" │");
        }
        println(sb.toString());
    }

    private void printEmptyRow(int[] widths, String message) {
        StringBuilder sb = new StringBuilder();
        sb.append('│');
        int total = 0;
        for (int w : widths) total += (w + 3);
        sb.append(" ").append(padRight(message, total - 3)).append("│");
        println(sb.toString());
    }

    private String borderTop(int[] widths) { return border(widths, '┌', '┬', '┐'); }
    private String borderSep(int[] widths) { return border(widths, '├', '┼', '┤'); }
    private String borderBottom(int[] widths) { return border(widths, '└', '┴', '┘'); }

    private String border(int[] widths, char left, char mid, char right) {
        StringBuilder sb = new StringBuilder();
        sb.append(left);
        for (int i = 0; i < widths.length; i++) {
            sb.append(repeat('─', widths[i] + 2));
            sb.append(i == widths.length - 1 ? right : mid);
        }
        return sb.toString();
    }

    private String titleRow(String title, int[] widths) {
        int total = 0; for (int w : widths) total += (w + 3); total += 1; // leading │
        String centered = center(title, total - 2);
        return new StringBuilder().append('│').append(centered).append('│').toString();
    }

    private String safe(String v) {
        return v == null ? "-" : v;
    }

    private String repeat(char c, int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }

    private String padRight(String s, int n) {
        if (s == null) s = "";
        if (s.length() > n) return s.substring(0, n);
        StringBuilder sb = new StringBuilder(n);
        sb.append(s);
        while (sb.length() < n) sb.append(' ');
        return sb.toString();
    }

    private String center(String s, int n) {
        if (s == null) s = "";
        if (s.length() >= n) return s.substring(0, n);
        int left = (n - s.length()) / 2;
        int right = n - s.length() - left;
        StringBuilder sb = new StringBuilder(n);
        sb.append(repeat(' ', left));
        sb.append(s);
        sb.append(repeat(' ', right));
        return sb.toString();
    }


    private void println() { System.out.println(); }
    private void println(String s) { System.out.println(s); }
}


