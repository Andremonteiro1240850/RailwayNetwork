#include "ui.h"
#include "../Sprint2/asm.h"
#include <limits.h>

// Global UI state
static UIState g_ui_state = {NULL, 0, 0, ""};

// Forward declarations
static void ui_display_header(void);
static void ui_display_separator(void);
static int ui_process_operator_command(const char *command);
static int ui_process_admin_command(const char *command);
static void ui_display_track_details(int track_id);
static void ui_display_train_details(int train_id);

/**
 * Initialize UI component
 */
int ui_init(ManagerSystem *system) {
    if (system == NULL) {
        return 0;
    }
    
    g_ui_state.system = system;
    g_ui_state.running = 1;
    g_ui_state.current_menu_level = 0;
    g_ui_state.last_error[0] = '\0';
    
    // Initialize Board component
    if (!board_init()) {
        return 0;
    }
    
    return 1;
}

/**
 * Cleanup UI resources
 */
void ui_cleanup(void) {
    board_cleanup();
    memset(&g_ui_state, 0, sizeof(UIState));
}

/**
 * Clear screen
 */
void ui_clear_screen(void) {
    board_clear_screen();
}

/**
 * Display header
 */
static void ui_display_header(void) {
    printf("\n");
    printf("========================================\n");
    printf("  RAILWAY STATION MANAGEMENT SYSTEM\n");
    printf("========================================\n");
    printf("\n");
}

/**
 * Display separator
 */
static void ui_display_separator(void) {
    printf("----------------------------------------\n");
}

/**
 * Display error message
 */
void ui_show_error(const char *message) {
    if (message == NULL) return;
    
    printf("\n");
    printf("*** ERROR: %s ***\n", message);
    printf("\n");
    
    // Also send to board
    char buffer[UI_BUFFER_SIZE];
    int pos = 0;
    const char *prefix = "ERROR: ";
    int i = 0;
    while (prefix[i] != '\0' && pos < UI_BUFFER_SIZE - 1) {
        buffer[pos++] = prefix[i++];
    }
    i = 0;
    while (message[i] != '\0' && pos < UI_BUFFER_SIZE - 1) {
        buffer[pos++] = message[i++];
    }
    buffer[pos] = '\0';
    board_send_message(buffer);
}

/**
 * Display success message
 */
void ui_show_success(const char *message) {
    if (message == NULL) return;
    
    printf("\n");
    printf(">>> SUCCESS: %s <<<\n", message);
    printf("\n");
}

/**
 * Display information message
 */
void ui_show_info(const char *message) {
    if (message == NULL) return;
    
    printf("\n");
    printf("INFO: %s\n", message);
    printf("\n");
}

/**
 * Wait for user to press Enter
 */
void ui_wait_for_enter(void) {
    printf("\nPress Enter to continue...");
    fflush(stdout);
    while (getchar() != '\n' && getchar() != EOF);
}

/**
 * Get user input with validation
 */
int ui_get_input(const char *prompt, char *buffer, int buffer_size) {
    if (prompt == NULL || buffer == NULL || buffer_size <= 0) {
        return 0;
    }
    
    printf("%s", prompt);
    fflush(stdout);
    
    if (fgets(buffer, buffer_size, stdin) == NULL) {
        return 0;
    }
    
    // Remove newline
    size_t len = strlen(buffer);
    if (len > 0 && buffer[len - 1] == '\n') {
        buffer[len - 1] = '\0';
    }
    
    // Check for empty input
    if (strlen(buffer) == 0) {
        return 0;
    }
    
    return 1;
}

/**
 * Get integer input with validation
 */
int ui_get_int(const char *prompt, int *value, int min, int max) {
    if (prompt == NULL || value == NULL) {
        return 0;
    }
    
    char buffer[UI_MAX_INPUT_LEN];
    if (!ui_get_input(prompt, buffer, sizeof(buffer))) {
        return 0;
    }
    
    // Convert to integer
    char *endptr;
    long int result = strtol(buffer, &endptr, 10);
    
    // Check for conversion errors
    if (*endptr != '\0' && *endptr != '\n') {
        ui_show_error("Invalid number format");
        return 0;
    }
    
    // Check range
    if (result < min || result > max) {
        char error_msg[128];
        // Build error message using basic string manipulation
        const char *prefix = "Value must be between ";
        int pos = 0;
        int i = 0;
        while (prefix[i] != '\0' && pos < 127) {
            error_msg[pos++] = prefix[i++];
        }
        // Convert min to string
        char min_str[20];
        char max_str[20];
        int min_pos = 0, max_pos = 0;
        long int temp_min = min;
        long int temp_max = max;
        if (temp_min < 0) {
            error_msg[pos++] = '-';
            temp_min = -temp_min;
        }
        do {
            min_str[min_pos++] = '0' + (temp_min % 10);
            temp_min /= 10;
        } while (temp_min > 0);
        for (i = min_pos - 1; i >= 0 && pos < 127; i--) {
            error_msg[pos++] = min_str[i];
        }
        const char *and = " and ";
        i = 0;
        while (and[i] != '\0' && pos < 127) {
            error_msg[pos++] = and[i++];
        }
        if (temp_max < 0) {
            error_msg[pos++] = '-';
            temp_max = -temp_max;
        }
        do {
            max_str[max_pos++] = '0' + (temp_max % 10);
            temp_max /= 10;
        } while (temp_max > 0);
        for (i = max_pos - 1; i >= 0 && pos < 127; i--) {
            error_msg[pos++] = max_str[i];
        }
        error_msg[pos] = '\0';
        ui_show_error(error_msg);
        return 0;
    }
    
    *value = (int)result;
    return 1;
}

/**
 * Confirm action
 */
int ui_confirm_action(const char *message) {
    if (message == NULL) {
        return 0;
    }
    
    printf("\n%s (y/n): ", message);
    fflush(stdout);
    
    char buffer[10];
    if (fgets(buffer, sizeof(buffer), stdin) == NULL) {
        return 0;
    }
    
    // Check first character
    if (buffer[0] == 'y' || buffer[0] == 'Y') {
        return 1;
    }
    
    return 0;
}

/**
 * Display login screen and authenticate user
 */
int ui_login(void) {
    if (g_ui_state.system == NULL) {
        return 0;
    }
    
    ui_clear_screen();
    ui_display_header();
    
    printf("Please login to continue\n");
    ui_display_separator();
    
    char username[UI_MAX_INPUT_LEN];
    char password[UI_MAX_INPUT_LEN];
    
    // Get username
    if (!ui_get_input("Username: ", username, sizeof(username))) {
        return 0;
    }
    
    // Get password (hide input in real system, but for console we show it)
    if (!ui_get_input("Password: ", password, sizeof(password))) {
        return 0;
    }
    
    // Attempt login
    User *user = manager_login(g_ui_state.system, username, password);
    
    if (user == NULL) {
        ui_show_error("Invalid username or password");
        ui_wait_for_enter();
        return 0;
    }
    
    ui_show_success("Login successful");
    
    // Display user info
    printf("Logged in as: %s (%s)\n", user->name, 
           user->role == USER_ROLE_ADMINISTRATOR ? "Administrator" : "Operator");
    printf("\n");
    
    ui_wait_for_enter();
    return 1;
}

/**
 * Display main menu based on user role
 */
void ui_show_main_menu(void) {
    if (g_ui_state.system == NULL || g_ui_state.system->current_user == NULL) {
        return;
    }
    
    ui_clear_screen();
    ui_display_header();
    
    User *user = g_ui_state.system->current_user;
    printf("Welcome, %s\n", user->name);
    printf("Role: %s\n", user->role == USER_ROLE_ADMINISTRATOR ? "Administrator" : "Operator");
    ui_display_separator();
    
    if (user->role == USER_ROLE_ADMINISTRATOR) {
        ui_show_administrator_menu();
    } else {
        ui_show_operator_menu();
    }
}

/**
 * Display operator menu
 */
void ui_show_operator_menu(void) {
    printf("\nOPERATOR MENU\n");
    ui_display_separator();
    printf("1. Assign track to arriving train\n");
    printf("2. Set track as free\n");
    printf("3. Set track as inoperational\n");
    printf("4. Give departure order to train\n");
    printf("5. View sensor data\n");
    printf("6. View track status\n");
    printf("7. View train status\n");
    printf("0. Logout\n");
    printf("\n");
}

/**
 * Display administrator menu
 */
void ui_show_administrator_menu(void) {
    printf("\nADMINISTRATOR MENU\n");
    ui_display_separator();
    printf("1. Assign track to arriving train\n");
    printf("2. Set track as free\n");
    printf("3. Set track as inoperational\n");
    printf("4. Give departure order to train\n");
    printf("5. View sensor data\n");
    printf("6. View track status\n");
    printf("7. View train status\n");
    printf("8. Export user action logs\n");
    printf("9. View all logs\n");
    printf("10. System status\n");
    printf("0. Logout\n");
    printf("\n");
}

/**
 * Process operator command
 */
static int ui_process_operator_command(const char *command) {
    if (command == NULL) {
        return 0;
    }
    
    int choice;
    if (sscanf(command, "%d", &choice) != 1) {
        return 0;
    }
    
    switch (choice) {
        case 1:
            return ui_assign_track_to_train();
        case 2:
            return ui_set_track_free();
        case 3:
            return ui_set_track_inoperational();
        case 4:
            return ui_departure_train();
        case 5:
            return ui_view_sensor_data();
        case 6:
            return ui_view_track_status();
        case 7:
            return ui_view_train_status();
        case 0:
            g_ui_state.system->current_user = NULL;
            return 1; // Signal logout
        default:
            ui_show_error("Invalid option");
            return 0;
    }
}

/**
 * Process administrator command
 */
static int ui_process_admin_command(const char *command) {
    if (command == NULL) {
        return 0;
    }
    
    int choice;
    if (sscanf(command, "%d", &choice) != 1) {
        return 0;
    }
    
    switch (choice) {
        case 1:
            return ui_assign_track_to_train();
        case 2:
            return ui_set_track_free();
        case 3:
            return ui_set_track_inoperational();
        case 4:
            return ui_departure_train();
        case 5:
            return ui_view_sensor_data();
        case 6:
            return ui_view_track_status();
        case 7:
            return ui_view_train_status();
        case 8:
            return ui_export_user_logs();
        case 9:
            return ui_view_all_logs();
        case 10:
            return ui_system_status();
        case 0:
            g_ui_state.system->current_user = NULL;
            return 1; // Signal logout
        default:
            ui_show_error("Invalid option");
            return 0;
    }
}

/**
 * Main UI loop
 */
int ui_main_loop(void) {
    if (g_ui_state.system == NULL) {
        return 0;
    }
    
    // Login loop
    while (g_ui_state.running) {
        if (!ui_login()) {
            // User cancelled or login failed
            char buffer[UI_MAX_INPUT_LEN];
            if (ui_get_input("Try again? (y/n): ", buffer, sizeof(buffer))) {
                if (buffer[0] != 'y' && buffer[0] != 'Y') {
                    break; // Exit
                }
            } else {
                break; // Exit
            }
            continue;
        }
        
        // Main menu loop
        while (g_ui_state.system->current_user != NULL && g_ui_state.running) {
            ui_show_main_menu();
            
            char command[UI_MAX_INPUT_LEN];
            if (!ui_get_input("Select option: ", command, sizeof(command))) {
                continue;
            }
            
            int result = 0;
            if (g_ui_state.system->current_user->role == USER_ROLE_ADMINISTRATOR) {
                result = ui_process_admin_command(command);
            } else {
                result = ui_process_operator_command(command);
            }
            
            if (result == 1 && g_ui_state.system->current_user == NULL) {
                // Logged out
                break;
            }
            
            if (result == 0) {
                ui_wait_for_enter();
            }
        }
    }
    
    return 1;
}

/**
 * Assign track to arriving train
 */
int ui_assign_track_to_train(void) {
    if (g_ui_state.system == NULL) {
        return 0;
    }
    
    ui_clear_screen();
    printf("ASSIGN TRACK TO ARRIVING TRAIN\n");
    ui_display_separator();
    
    int train_id, track_id;
    
    if (!ui_select_train(&train_id)) {
        return 0;
    }
    
    if (!ui_select_track(&track_id)) {
        return 0;
    }
    
    // Confirm action
    char confirm_msg[128];
    // Build confirmation message
    const char *prefix = "Assign train ";
    int pos = 0;
    int i = 0;
    while (prefix[i] != '\0' && pos < 127) {
        confirm_msg[pos++] = prefix[i++];
    }
    // Convert train_id to string
    char train_str[20];
    int train_pos = 0;
    int temp_train = train_id;
    if (temp_train < 0) {
        confirm_msg[pos++] = '-';
        temp_train = -temp_train;
    }
    do {
        train_str[train_pos++] = '0' + (temp_train % 10);
        temp_train /= 10;
    } while (temp_train > 0);
    for (i = train_pos - 1; i >= 0 && pos < 127; i--) {
        confirm_msg[pos++] = train_str[i];
    }
    const char *to = " to track ";
    i = 0;
    while (to[i] != '\0' && pos < 127) {
        confirm_msg[pos++] = to[i++];
    }
    // Convert track_id to string
    char track_str[20];
    int track_pos = 0;
    int temp_track = track_id;
    if (temp_track < 0) {
        confirm_msg[pos++] = '-';
        temp_track = -temp_track;
    }
    do {
        track_str[track_pos++] = '0' + (temp_track % 10);
        temp_track /= 10;
    } while (temp_track > 0);
    for (i = track_pos - 1; i >= 0 && pos < 127; i--) {
        confirm_msg[pos++] = track_str[i];
    }
    confirm_msg[pos] = '\0';
    
    if (!ui_confirm_action(confirm_msg)) {
        ui_show_info("Operation cancelled");
        return 0;
    }
    
    // Call manager function
    int result = assignArrivingTrain(g_ui_state.system, train_id, track_id);
    
    if (result) {
        ui_show_success("Train assigned successfully");
    } else {
        ui_show_error("Failed to assign train. Check track availability.");
    }
    
    return result;
}

/**
 * Set track as free
 */
int ui_set_track_free(void) {
    if (g_ui_state.system == NULL) {
        return 0;
    }
    
    ui_clear_screen();
    printf("SET TRACK AS FREE\n");
    ui_display_separator();
    
    int track_id;
    if (!ui_select_track(&track_id)) {
        return 0;
    }
    
    char confirm_msg[128];
    const char *prefix = "Set track ";
    int pos = 0;
    int i = 0;
    while (prefix[i] != '\0' && pos < 127) {
        confirm_msg[pos++] = prefix[i++];
    }
    char track_str[20];
    int track_pos = 0;
    int temp_track = track_id;
    if (temp_track < 0) {
        confirm_msg[pos++] = '-';
        temp_track = -temp_track;
    }
    do {
        track_str[track_pos++] = '0' + (temp_track % 10);
        temp_track /= 10;
    } while (temp_track > 0);
    for (i = track_pos - 1; i >= 0 && pos < 127; i--) {
        confirm_msg[pos++] = track_str[i];
    }
    const char *suffix = " as free?";
    i = 0;
    while (suffix[i] != '\0' && pos < 127) {
        confirm_msg[pos++] = suffix[i++];
    }
    confirm_msg[pos] = '\0';
    
    if (!ui_confirm_action(confirm_msg)) {
        ui_show_info("Operation cancelled");
        return 0;
    }
    
    int result = setTrackFree(g_ui_state.system, track_id);
    
    if (result) {
        ui_show_success("Track set as free successfully");
    } else {
        ui_show_error("Failed to set track as free");
    }
    
    return result;
}

/**
 * Set track as inoperational
 */
int ui_set_track_inoperational(void) {
    if (g_ui_state.system == NULL) {
        return 0;
    }
    
    ui_clear_screen();
    printf("SET TRACK AS INOPERATIONAL\n");
    ui_display_separator();
    
    int track_id;
    if (!ui_select_track(&track_id)) {
        return 0;
    }
    
    char confirm_msg[128];
    const char *prefix = "Set track ";
    int pos = 0;
    int i = 0;
    while (prefix[i] != '\0' && pos < 127) {
        confirm_msg[pos++] = prefix[i++];
    }
    char track_str[20];
    int track_pos = 0;
    int temp_track = track_id;
    if (temp_track < 0) {
        confirm_msg[pos++] = '-';
        temp_track = -temp_track;
    }
    do {
        track_str[track_pos++] = '0' + (temp_track % 10);
        temp_track /= 10;
    } while (temp_track > 0);
    for (i = track_pos - 1; i >= 0 && pos < 127; i--) {
        confirm_msg[pos++] = track_str[i];
    }
    const char *suffix = " as inoperational?";
    i = 0;
    while (suffix[i] != '\0' && pos < 127) {
        confirm_msg[pos++] = suffix[i++];
    }
    confirm_msg[pos] = '\0';
    
    if (!ui_confirm_action(confirm_msg)) {
        ui_show_info("Operation cancelled");
        return 0;
    }
    
    int result = setTrackINOP(g_ui_state.system, track_id);
    
    if (result) {
        ui_show_success("Track set as inoperational successfully");
    } else {
        ui_show_error("Failed to set track as inoperational");
    }
    
    return result;
}

/**
 * Give departure order to train
 */
int ui_departure_train(void) {
    if (g_ui_state.system == NULL) {
        return 0;
    }
    
    ui_clear_screen();
    printf("GIVE DEPARTURE ORDER TO TRAIN\n");
    ui_display_separator();
    
    int train_id, track_id;
    
    if (!ui_select_train(&train_id)) {
        return 0;
    }
    
    if (!ui_select_track(&track_id)) {
        return 0;
    }
    
    char confirm_msg[128];
    const char *prefix = "Give departure order to train ";
    int pos = 0;
    int i = 0;
    while (prefix[i] != '\0' && pos < 127) {
        confirm_msg[pos++] = prefix[i++];
    }
    char train_str[20];
    int train_pos = 0;
    int temp_train = train_id;
    if (temp_train < 0) {
        confirm_msg[pos++] = '-';
        temp_train = -temp_train;
    }
    do {
        train_str[train_pos++] = '0' + (temp_train % 10);
        temp_train /= 10;
    } while (temp_train > 0);
    for (i = train_pos - 1; i >= 0 && pos < 127; i--) {
        confirm_msg[pos++] = train_str[i];
    }
    const char *suffix = " from track ";
    i = 0;
    while (suffix[i] != '\0' && pos < 127) {
        confirm_msg[pos++] = suffix[i++];
    }
    char track_str[20];
    int track_pos = 0;
    int temp_track = track_id;
    if (temp_track < 0) {
        confirm_msg[pos++] = '-';
        temp_track = -temp_track;
    }
    do {
        track_str[track_pos++] = '0' + (temp_track % 10);
        temp_track /= 10;
    } while (temp_track > 0);
    for (i = track_pos - 1; i >= 0 && pos < 127; i--) {
        confirm_msg[pos++] = track_str[i];
    }
    confirm_msg[pos] = '\0';
    
    if (!ui_confirm_action(confirm_msg)) {
        ui_show_info("Operation cancelled");
        return 0;
    }
    
    int result = departureTrain(g_ui_state.system, train_id, track_id);
    
    if (result) {
        ui_show_success("Departure order given successfully");
    } else {
        ui_show_error("Failed to give departure order");
    }
    
    return result;
}

/**
 * View sensor data
 */
int ui_view_sensor_data(void) {
    if (g_ui_state.system == NULL) {
        return 0;
    }
    
    ui_clear_screen();
    printf("SENSOR DATA\n");
    ui_display_separator();
    
    ui_display_sensor_data();
    
    ui_wait_for_enter();
    return 1;
}

/**
 * View track status
 */
int ui_view_track_status(void) {
    if (g_ui_state.system == NULL) {
        return 0;
    }
    
    ui_clear_screen();
    printf("TRACK STATUS\n");
    ui_display_separator();
    
    ui_display_track_list();
    
    ui_wait_for_enter();
    return 1;
}

/**
 * View train status
 */
int ui_view_train_status(void) {
    if (g_ui_state.system == NULL) {
        return 0;
    }
    
    ui_clear_screen();
    printf("TRAIN STATUS\n");
    ui_display_separator();
    
    ui_display_train_list();
    
    ui_wait_for_enter();
    return 1;
}

/**
 * Export user action logs
 */
int ui_export_user_logs(void) {
    if (g_ui_state.system == NULL || g_ui_state.system->current_user == NULL) {
        return 0;
    }
    
    if (!is_administrator(g_ui_state.system->current_user)) {
        ui_show_error("Only administrators can export logs");
        return 0;
    }
    
    ui_clear_screen();
    printf("EXPORT USER ACTION LOGS\n");
    ui_display_separator();
    
    int user_id;
    if (!ui_get_int("Enter user ID: ", &user_id, 1, INT_MAX)) {
        return 0;
    }
    
    char output_dir[UI_MAX_INPUT_LEN];
    if (!ui_get_input("Enter output directory (default: ./logs): ", output_dir, sizeof(output_dir))) {
        strcpy(output_dir, "./logs");
    }
    
    // Query user actions
    LogQueryResult result;
    if (!query_user_actions(user_id, NULL, NULL, &result)) {
        ui_show_error("Failed to query user actions");
        return 0;
    }
    
    if (result.count == 0) {
        ui_show_info("No actions found for this user");
        free_log_query_result(&result);
        return 0;
    }
    
    // Write to file
    int success = write_user_actions_to_file(
        g_ui_state.system->current_user,
        &result,
        g_ui_state.system->users,
        g_ui_state.system->user_count,
        user_id,
        output_dir
    );
    
    free_log_query_result(&result);
    
    if (success) {
        ui_show_success("Logs exported successfully");
    } else {
        ui_show_error("Failed to export logs");
    }
    
    return success;
}

/**
 * View all logs
 */
int ui_view_all_logs(void) {
    if (g_ui_state.system == NULL || g_ui_state.system->current_user == NULL) {
        return 0;
    }
    
    if (!is_administrator(g_ui_state.system->current_user)) {
        ui_show_error("Only administrators can view all logs");
        return 0;
    }
    
    ui_clear_screen();
    printf("ALL SYSTEM LOGS\n");
    ui_display_separator();
    
    // This would require access to the log storage
    // For now, show a message
    ui_show_info("Log viewing functionality requires direct access to log storage");
    ui_show_info("Use export function to view logs in detail");
    
    ui_wait_for_enter();
    return 1;
}

/**
 * System status
 */
int ui_system_status(void) {
    if (g_ui_state.system == NULL) {
        return 0;
    }
    
    ui_clear_screen();
    printf("SYSTEM STATUS\n");
    ui_display_separator();
    
    printf("Users: %d\n", g_ui_state.system->user_count);
    printf("Tracks: %d\n", g_ui_state.system->track_count);
    printf("Trains: %d\n", g_ui_state.system->train_count);
    
    printf("\nSensor Status:\n");
    ui_display_sensor_data();
    
    printf("\nTrack Status:\n");
    ui_display_track_list();
    
    ui_wait_for_enter();
    return 1;
}

/**
 * Display track list
 */
void ui_display_track_list(void) {
    if (g_ui_state.system == NULL) {
        return;
    }
    
    printf("\nID | State      | Train ID\n");
    ui_display_separator();
    
    for (int i = 0; i < g_ui_state.system->track_count; i++) {
        Track *track = &g_ui_state.system->tracks[i];
        const char *state_str;
        
        switch (track->state) {
            case TRACK_FREE:
                state_str = "FREE";
                break;
            case TRACK_BUSY:
                state_str = "BUSY";
                break;
            case TRACK_ASSIGNED:
                state_str = "ASSIGNED";
                break;
            case TRACK_INOP:
                state_str = "INOPERATIONAL";
                break;
            default:
                state_str = "UNKNOWN";
                break;
        }
        
        printf("%-3d| %-11s| %d\n", track->id, state_str, track->trainId);
    }
    printf("\n");
}

/**
 * Display train list
 */
void ui_display_train_list(void) {
    if (g_ui_state.system == NULL) {
        return;
    }
    
    printf("\nID | State      | Position\n");
    ui_display_separator();
    
    for (int i = 0; i < g_ui_state.system->train_count; i++) {
        Train *train = &g_ui_state.system->trains[i];
        const char *state_str;
        
        switch (train->state) {
            case TRAIN_ARRIVING:
                state_str = "ARRIVING";
                break;
            case TRAIN_STOPPED:
                state_str = "STOPPED";
                break;
            case TRAIN_DEPARTING:
                state_str = "DEPARTING";
                break;
            default:
                state_str = "UNKNOWN";
                break;
        }
        
        printf("%-3d| %-11s| %d\n", train->id, state_str, train->position);
    }
    printf("\n");
}

/**
 * Display sensor data
 */
void ui_display_sensor_data(void) {
    if (g_ui_state.system == NULL) {
        return;
    }
    
    char *sensor_display = manager_format_sensor_display(g_ui_state.system);
    if (sensor_display != NULL) {
        printf("%s", sensor_display);
        free(sensor_display);
    } else {
        ui_show_error("Sensor data not available");
    }
}

/**
 * Select track
 */
int ui_select_track(int *track_id) {
    if (track_id == NULL || g_ui_state.system == NULL) {
        return 0;
    }
    
    ui_display_track_list();
    
    return ui_get_int("Select track ID: ", track_id, 1, INT_MAX);
}

/**
 * Select train
 */
int ui_select_train(int *train_id) {
    if (train_id == NULL || g_ui_state.system == NULL) {
        return 0;
    }
    
    ui_display_train_list();
    
    return ui_get_int("Select train ID: ", train_id, 1, INT_MAX);
}

/**
 * Display track details
 */
static void ui_display_track_details(int track_id) {
    if (g_ui_state.system == NULL) {
        return;
    }
    
    Track *track = findTrack(g_ui_state.system, track_id);
    if (track == NULL) {
        ui_show_error("Track not found");
        return;
    }
    
    printf("\nTrack Details:\n");
    ui_display_separator();
    printf("ID: %d\n", track->id);
    
    const char *state_str;
    switch (track->state) {
        case TRACK_FREE:
            state_str = "FREE";
            break;
        case TRACK_BUSY:
            state_str = "BUSY";
            break;
        case TRACK_ASSIGNED:
            state_str = "ASSIGNED";
            break;
        case TRACK_INOP:
            state_str = "INOPERATIONAL";
            break;
        default:
            state_str = "UNKNOWN";
            break;
    }
    
    printf("State: %s\n", state_str);
    printf("Train ID: %d\n", track->trainId);
    printf("\n");
}

/**
 * Display train details
 */
static void ui_display_train_details(int train_id) {
    if (g_ui_state.system == NULL) {
        return;
    }
    
    Train *train = findTrain(g_ui_state.system, train_id);
    if (train == NULL) {
        ui_show_error("Train not found");
        return;
    }
    
    printf("\nTrain Details:\n");
    ui_display_separator();
    printf("ID: %d\n", train->id);
    
    const char *state_str;
    switch (train->state) {
        case TRAIN_ARRIVING:
            state_str = "ARRIVING";
            break;
        case TRAIN_STOPPED:
            state_str = "STOPPED";
            break;
        case TRAIN_DEPARTING:
            state_str = "DEPARTING";
            break;
        default:
            state_str = "UNKNOWN";
            break;
    }
    
    printf("State: %s\n", state_str);
    printf("Position: %d\n", train->position);
    printf("\n");
}

