#ifndef UI_H
#define UI_H

// Include the complete definition to avoid type conflicts
#include "../Manager/manager.h"
#include "../Board/Board.h"

// ARQCP: UI State structure
#define UI_MAX_INPUT_LEN 100

typedef struct {
    ManagerSystem *system;
    int running;
    int current_menu_level;
    char last_error[256];
} UIState;

/**
 * USAC14: Console UI for controlling track sign lights
 * Robust input validation against malicious/error input
 * 
 * @param system Manager system instance
 * @return 0 on success, 1 on error
 */
int ui_usac14_control_light(ManagerSystem *system);

/**
 * Initialize UI system
 * @param system Manager system instance
 * @return 1 on success, 0 on failure
 */
int ui_init(ManagerSystem *system);

/**
 * Main UI loop
 */
int ui_main_loop(void);

/**
 * Cleanup UI system
 */
void ui_cleanup(void);

// ARQCP: Forward declarations for UI functions
void ui_show_error(const char *message);
void ui_show_info(const char *message);
void ui_show_administrator_menu(void);
void ui_show_operator_menu(void);
int ui_assign_track_to_train(void);
int ui_set_track_free(void);
int ui_set_track_inoperational(void);
int ui_departure_train(void);
int ui_view_sensor_data(void);
int ui_view_track_status(void);
int ui_view_train_status(void);
int ui_export_user_logs(void);
int ui_view_all_logs(void);
int ui_system_status(void);
void ui_display_track_list(void);
void ui_display_train_list(void);
void ui_display_sensor_data(void);
int ui_select_track(int *track_id);
int ui_select_train(int *train_id);

#endif // UI_H

