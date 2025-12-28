#ifndef BOARD_H
#define BOARD_H




#define BOARD_MAX_MESSAGE_LEN 512
#define BOARD_MAX_TRACKS 10
#define BOARD_DISPLAY_WIDTH 50

// Track states (matching Manager's definitions)
typedef enum {
    TRACK_FREE = 0,
    TRACK_ASSIGNED = 1,
    TRACK_BUSY = 2,
    TRACK_INOP = 3
} TrackState;

// Train states (matching Manager's definitions)
typedef enum {
    TRAIN_ARRIVING = 0,
    TRAIN_STOPPED = 1,
    TRAIN_DEPARTING = 2
} TrainState;


typedef struct {
    int temperature;           // Temperatura filtrada (Celsius)
    int humidity;              // Humidade filtrada (Percent)
    char last_event[256];      // Último evento registado
    int initialized;           // Flag de inicialização
} BoardState;


/**
 * Inicializa o componente Board
 * Deve ser chamada antes de qualquer outra função
 *
 * @return 1 em sucesso, 0 em falha
 */
int board_init(void);

/**
 * Limpa recursos do Board
 * Deve ser chamada no shutdown do sistema
 */
void board_cleanup(void);

/**
 * Envia mensagem simples para o Board
 * Usada pelo Manager para output direto
 *
 * @param message Mensagem a apresentar (string terminada em \0)
 */
void board_send_message(const char *message);

/**
 * Apresenta texto formatado no Board
 * Função principal para apresentar dados do Manager
 *
 * @param data Dados formatados a apresentar
 */
void board_display_text(const char *data);

/**
 * Atualiza o Board com dados de sensores
 * Apresenta temperatura e humidade com formatação visual
 *
 * @param temperature Temperatura em Celsius
 * @param humidity Humidade em percentagem
 */
void board_update_sensors(int temperature, int humidity);

/**
 * Atualiza Board com mensagem genérica (USAC16)
 * Usada para eventos operacionais
 *
 * @param msg Mensagem a apresentar
 */
void updateBoard(const char *msg);

/**
 * Apresenta alerta de paragem de emergência (USAC16)
 * Visual destacado com símbolos de alerta
 */
void showEmergencyStop(void);

/**
 * Apresenta via marcada como inoperacional (USAC16)
 * Visual com indicação de manutenção necessária
 */
void trackINOP(void);

/**
 * Apresenta via libertada (USAC16)
 * Visual com indicação de via disponível
 */
void freeTrack(void);

/**
 * Apresenta comboio em partida (USAC16)
 * Visual com animação de movimento
 */
void showDepartingTrain(void);

/**
 * Redesenha o Board completo
 * Apresenta estado completo do sistema
 */
void board_redraw(void);

/**
 * Limpa a tela do console
 * Usada antes de redesenhar o Board
 */
void board_clear_screen(void);

#endif