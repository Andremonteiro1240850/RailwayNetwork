#ifndef UI_H
#define UI_H

#include "../Manager/manager.h"
#include "../Board/Board.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// UI Constants
#define UI_MAX_INPUT_LEN 256
#define UI_MAX_MENU_OPTIONS 20
#define UI_BUFFER_SIZE 512

// Menu option structure
typedef struct {
    int id;
    char label[64];
    char description[128];
} MenuOption;

// UI State structure
typedef struct {
    ManagerSystem *system;
    int running;
    int current_menu_level;
    char last_error[256];
} UIState;

// Function declarations

/**
 * Initialize UI component
 * Must be called before any other UI function
 * 
 * @param system Initialized ManagerSystem
 * @return 1 on success, 0 on failure
 */
int ui_init(ManagerSystem *system);

/**
 * Cleanup UI resources
 * Should be called on shutdown
 */
void ui_cleanup(void);

/**
 * Main UI loop
 * Handles user interaction and menu navigation
 * 
 * @return 1 on normal exit, 0 on error
 */
int ui_main_loop(void);

/**
 * Main UI loop (alias for ui_main_loop)
 * Maintains backward compatibility
 * 
 * @return 1 on normal exit, 0 on error
 */
int ui_run(void);

/**
 * Display login screen and authenticate user
 * 
 * @return 1 on successful login, 0 on failure or cancel
 */
int ui_login(void);

/**
 * Display main menu based on user role
 */
void ui_show_main_menu(void);

/**
 * Display operator menu
 */
void ui_show_operator_menu(void);

/**
 * Display administrator menu
 */
void ui_show_administrator_menu(void);

/**
 * Get user input with validation
 * 
 * @param prompt Prompt to display
 * @param buffer Buffer to store input
 * @param buffer_size Size of buffer
 * @return 1 on success, 0 on cancel/error
 */
int ui_get_input(const char *prompt, char *buffer, int buffer_size);

/**
 * Get integer input with validation
 * 
 * @param prompt Prompt to display
 * @param value Output parameter for integer value
 * @param min Minimum allowed value (use INT_MIN for no limit)
 * @param max Maximum allowed value (use INT_MAX for no limit)
 * @return 1 on success, 0 on cancel/error
 */
int ui_get_int(const char *prompt, int *value, int min, int max);

/**
 * Display error message
 * 
 * @param message Error message to display
 */
void ui_show_error(const char *message);

/**
 * Display success message
 * 
 * @param message Success message to display
 */
void ui_show_success(const char *message);

/**
 * Display information message
 * 
 * @param message Information message to display
 */
void ui_show_info(const char *message);

/**
 * Clear screen
 */
void ui_clear_screen(void);

/**
 * Wait for user to press Enter
 */
void ui_wait_for_enter(void);

// Operator functions
int ui_assign_track_to_train(void);
int ui_set_track_free(void);
int ui_set_track_inoperational(void);
int ui_departure_train(void);
int ui_view_sensor_data(void);
int ui_view_track_status(void);
int ui_view_train_status(void);

// Administrator functions
int ui_export_user_logs(void);
int ui_view_all_logs(void);
int ui_configure_sensors(void);
int ui_manage_users(void);
int ui_system_status(void);

// Helper functions
void ui_display_track_list(void);
void ui_display_train_list(void);
void ui_display_sensor_data(void);
int ui_select_track(int *track_id);
int ui_select_train(int *train_id);
int ui_confirm_action(const char *message);

#endif // UI_H

