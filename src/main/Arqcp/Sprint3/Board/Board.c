#include "board.h"
#include <stdio.h>
#include <string.h>

// ==================== GLOBAL STATE ====================

static BoardState g_board_state = {
    .temperature = 0,
    .humidity = 0,
    .last_event = "",
    .initialized = 0
};

// ==================== PRIVATE HELPER FUNCTIONS ====================

/**
 * Copia string com limite de tamanho (seguro)
 */
static void safe_string_copy(char *dest, const char *src, int max_len) {
    if (dest == NULL || src == NULL || max_len <= 0) return;

    int i = 0;
    while (src[i] != '\0' && i < max_len - 1) {
        dest[i] = src[i];
        i++;
    }
    dest[i] = '\0';
}

/**
 * Desenha linha horizontal decorativa
 */
static void draw_line(char border_char, int width) {
    for (int i = 0; i < width; i++) {
        putchar(border_char);
    }
    putchar('\n');
}

/**
 * Desenha linha de caixa com texto centrado
 */
static void draw_box_line(const char *text, int width) {
    int text_len = strlen(text);
    int padding = (width - text_len - 4) / 2;

    printf("║ ");
    for (int i = 0; i < padding; i++) putchar(' ');
    printf("%s", text);
    for (int i = 0; i < padding; i++) putchar(' ');

    // Ajustar para largura par/ímpar
    if ((width - text_len - 4) % 2 != 0) putchar(' ');
    printf(" ║\n");
}

/**
 * Desenha topo da caixa ASCII
 */
static void draw_box_top(int width) {
    printf("╔");
    for (int i = 0; i < width - 2; i++) printf("═");
    printf("╗\n");
}

/**
 * Desenha fundo da caixa ASCII
 */
static void draw_box_bottom(int width) {
    printf("╚");
    for (int i = 0; i < width - 2; i++) printf("═");
    printf("╝\n");
}

/**
 * Desenha separador dentro da caixa
 */
static void draw_box_separator(int width) {
    printf("╠");
    for (int i = 0; i < width - 2; i++) printf("═");
    printf("╣\n");
}

/**
 * Desenha linha de texto dentro da caixa (alinhada à esquerda)
 */
static void draw_box_text_left(const char *text, int width) {
    int text_len = strlen(text);
    int padding = width - text_len - 4;

    printf("║ %s", text);
    for (int i = 0; i < padding; i++) putchar(' ');
    printf(" ║\n");
}

// ==================== PUBLIC INTERFACE IMPLEMENTATION ====================

int board_init(void) {
    if (g_board_state.initialized) {
        return 1; // Já inicializado
    }

    // Inicializar estado
    g_board_state.temperature = 0;
    g_board_state.humidity = 0;
    safe_string_copy(g_board_state.last_event, "System started", sizeof(g_board_state.last_event));
    g_board_state.initialized = 1;

    // Limpar ecrã e mostrar banner inicial
    board_clear_screen();

    draw_box_top(BOARD_DISPLAY_WIDTH);
    draw_box_line("🚉 RAILWAY STATION SYSTEM", BOARD_DISPLAY_WIDTH);
    draw_box_line("Board Component Initialized", BOARD_DISPLAY_WIDTH);
    draw_box_bottom(BOARD_DISPLAY_WIDTH);
    printf("\n");

    return 1;
}

void board_cleanup(void) {
    if (!g_board_state.initialized) return;

    printf("\n");
    draw_box_top(BOARD_DISPLAY_WIDTH);
    draw_box_line("Board Component Shutdown", BOARD_DISPLAY_WIDTH);
    draw_box_bottom(BOARD_DISPLAY_WIDTH);

    g_board_state.initialized = 0;
}

void board_send_message(const char *message) {
    if (message == NULL) return;

    // Output direto sem formatação especial
    printf("%s", message);
    fflush(stdout);
}

void board_display_text(const char *data) {
    if (data == NULL) return;

    // Apresentar dados com formatação mínima
    printf("%s\n", data);
    fflush(stdout);
}

void board_update_sensors(int temperature, int humidity) {
    // Atualizar estado interno
    g_board_state.temperature = temperature;
    g_board_state.humidity = humidity;

    // Apresentar com formatação visual
    printf("\n");
    draw_box_top(BOARD_DISPLAY_WIDTH);
    draw_box_line("📊 SENSOR DATA UPDATE", BOARD_DISPLAY_WIDTH);
    draw_box_separator(BOARD_DISPLAY_WIDTH);

    char temp_line[100];
    snprintf(temp_line, sizeof(temp_line), "🌡️  Temperature: %d°C", temperature);
    draw_box_text_left(temp_line, BOARD_DISPLAY_WIDTH);

    char hum_line[100];
    snprintf(hum_line, sizeof(hum_line), "💧 Humidity:    %d%%", humidity);
    draw_box_text_left(hum_line, BOARD_DISPLAY_WIDTH);

    draw_box_bottom(BOARD_DISPLAY_WIDTH);
    printf("\n");
    fflush(stdout);
}

void updateBoard(const char *msg) {
    if (msg == NULL) return;

    // Guardar como último evento
    safe_string_copy(g_board_state.last_event, msg, sizeof(g_board_state.last_event));

    // Apresentar mensagem formatada
    printf("\n");
    draw_box_top(BOARD_DISPLAY_WIDTH);
    draw_box_line("📢 OPERATIONAL UPDATE", BOARD_DISPLAY_WIDTH);
    draw_box_separator(BOARD_DISPLAY_WIDTH);
    draw_box_text_left(msg, BOARD_DISPLAY_WIDTH);
    draw_box_bottom(BOARD_DISPLAY_WIDTH);
    printf("\n");
    fflush(stdout);
}

void showEmergencyStop(void) {
    printf("\n");
    printf("╔════════════════════════════════════════════════╗\n");
    printf("║                                                ║\n");
    printf("║     🚨 🚨 🚨  EMERGENCY STOP  🚨 🚨 🚨        ║\n");
    printf("║                                                ║\n");
    printf("║          ⚠️  ALL OPERATIONS HALTED  ⚠️         ║\n");
    printf("║                                                ║\n");
    printf("║      Track occupied - Cannot assign train     ║\n");
    printf("║                                                ║\n");
    printf("╚════════════════════════════════════════════════╝\n");
    printf("\n");
    fflush(stdout);

    safe_string_copy(g_board_state.last_event, "EMERGENCY STOP ACTIVATED",
                     sizeof(g_board_state.last_event));
}

void trackINOP(void) {
    printf("\n");
    printf("╔════════════════════════════════════════════════╗\n");
    printf("║                                                ║\n");
    printf("║         🔴 TRACK INOPERATIONAL 🔴             ║\n");
    printf("║                                                ║\n");
    printf("║          🔧 Maintenance Required 🔧            ║\n");
    printf("║                                                ║\n");
    printf("║          Status: RED BLINKING (RB)            ║\n");
    printf("║                                                ║\n");
    printf("╚════════════════════════════════════════════════╝\n");
    printf("\n");
    fflush(stdout);

    safe_string_copy(g_board_state.last_event, "Track set to INOPERATIONAL",
                     sizeof(g_board_state.last_event));
}

void freeTrack(void) {
    printf("\n");
    printf("╔════════════════════════════════════════════════╗\n");
    printf("║                                                ║\n");
    printf("║            🟢 TRACK FREE 🟢                   ║\n");
    printf("║                                                ║\n");
    printf("║           ✅ Available for use ✅              ║\n");
    printf("║                                                ║\n");
    printf("║            Status: GREEN (GE)                 ║\n");
    printf("║                                                ║\n");
    printf("╚════════════════════════════════════════════════╝\n");
    printf("\n");
    fflush(stdout);

    safe_string_copy(g_board_state.last_event, "Track set to FREE",
                     sizeof(g_board_state.last_event));
}

void showDepartingTrain(void) {
    printf("\n");
    printf("╔════════════════════════════════════════════════╗\n");
    printf("║                                                ║\n");
    printf("║          🚂💨 TRAIN DEPARTING 💨              ║\n");
    printf("║                                                ║\n");
    printf("║              ═══════════════►                 ║\n");
    printf("║                                                ║\n");
    printf("║         Safe travels! Track cleared.          ║\n");
    printf("║                                                ║\n");
    printf("╚════════════════════════════════════════════════╝\n");
    printf("\n");
    fflush(stdout);

    safe_string_copy(g_board_state.last_event, "Train departed successfully",
                     sizeof(g_board_state.last_event));
}

void board_redraw(void) {
    if (!g_board_state.initialized) {
        board_init();
    }

    board_clear_screen();

    // Desenhar Board completo com estado atual
    printf("\n");
    draw_box_top(BOARD_DISPLAY_WIDTH);
    draw_box_line("🚉 STATION SYNOPTIC BOARD", BOARD_DISPLAY_WIDTH);
    draw_box_separator(BOARD_DISPLAY_WIDTH);

    // Secção de sensores
    char temp_line[100];
    snprintf(temp_line, sizeof(temp_line), "🌡️  Temperature: %d°C", g_board_state.temperature);
    draw_box_text_left(temp_line, BOARD_DISPLAY_WIDTH);

    char hum_line[100];
    snprintf(hum_line, sizeof(hum_line), "💧 Humidity:    %d%%", g_board_state.humidity);
    draw_box_text_left(hum_line, BOARD_DISPLAY_WIDTH);

    draw_box_separator(BOARD_DISPLAY_WIDTH);

    // Último evento
    char event_header[] = "⚠️  Last Event:";
    draw_box_text_left(event_header, BOARD_DISPLAY_WIDTH);

    char event_line[100];
    snprintf(event_line, sizeof(event_line), "   %s", g_board_state.last_event);
    draw_box_text_left(event_line, BOARD_DISPLAY_WIDTH);

    draw_box_bottom(BOARD_DISPLAY_WIDTH);
    printf("\n");
    fflush(stdout);
}

void board_clear_screen(void) {
    // ANSI escape code para limpar ecrã
    // Compatível com terminais Linux/UNIX
    printf("\033[2J\033[H");
    fflush(stdout);
}