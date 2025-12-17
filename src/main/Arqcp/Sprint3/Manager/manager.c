#include "manager.h"
#include "Board.h"
#include "../Sprint2/asm.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <ctype.h>
#include <sys/stat.h>
#include <sys/types.h>
#ifdef _WIN32
#include <io.h>
#define chmod _chmod
#else
#include <sys/stat.h>
#endif

#define INITIAL_CAPACITY 10
#define SENSOR_CMD "GTH"
#define MAX_INSTRUCTION_LEN 100
#define MAX_DATA_LEN 500
#define MAX_CMD_LEN 10

// ==================== SYSTEM INITIALIZATION ====================

int manager_init(ManagerSystem *system) {
    if (system == NULL) return 0;
    
    // Initialize users array
    system->user_capacity = INITIAL_CAPACITY;
    system->users = (User*)malloc(sizeof(User) * system->user_capacity);
    if (system->users == NULL) return 0;
    system->user_count = 0;
    
    // Initialize tracks array
    system->track_capacity = INITIAL_CAPACITY;
    system->tracks = (Track*)malloc(sizeof(Track) * system->track_capacity);
    if (system->tracks == NULL) {
        free(system->users);
        return 0;
    }
    system->track_count = 0;
    
    // Initialize trains array
    system->train_capacity = INITIAL_CAPACITY;
    system->trains = (Train*)malloc(sizeof(Train) * system->train_capacity);
    if (system->trains == NULL) {
        free(system->users);
        free(system->tracks);
        return 0;
    }
    system->train_count = 0;
    
    // Initialize sensor data with circular buffers
    system->sensors.temp_buffer = (int*)malloc(sizeof(int) * SENSOR_BUFFER_SIZE);
    system->sensors.humidity_buffer = (int*)malloc(sizeof(int) * SENSOR_BUFFER_SIZE);
    if (system->sensors.temp_buffer == NULL || system->sensors.humidity_buffer == NULL) {
        free(system->users);
        free(system->tracks);
        free(system->trains);
        return 0;
    }
    system->sensors.temp_nelem = 0;
    system->sensors.humidity_nelem = 0;
    system->sensors.temp_head = 0;
    system->sensors.temp_tail = 0;
    system->sensors.humidity_head = 0;
    system->sensors.humidity_tail = 0;
    system->sensors.current_temp = 0;
    system->sensors.current_humidity = 0;
    strcpy(system->sensors.temp_unit, "Celsius");
    strcpy(system->sensors.humidity_unit, "Percent");
    
    system->current_user = NULL;
    
    return 1;
}

void manager_cleanup(ManagerSystem *system) {
    if (system == NULL) return;
    
    if (system->users) free(system->users);
    if (system->tracks) free(system->tracks);
    if (system->trains) free(system->trains);
    if (system->sensors.temp_buffer) free(system->sensors.temp_buffer);
    if (system->sensors.humidity_buffer) free(system->sensors.humidity_buffer);
    
    memset(system, 0, sizeof(ManagerSystem));
}


// ==================== MANAGER ALGORITHM FUNCTIONS ====================

/**
 * Login function - uses decrypt_data Assembly function
 */
User* manager_login(ManagerSystem *system, const char *username, const char *password) {
    if (system == NULL || username == NULL || password == NULL) return NULL;
    
    // Find user by username
    for (int i = 0; i < system->user_count; i++) {
        if (strcmp(system->users[i].username, username) == 0) {
            // Decrypt stored password using Assembly function
            char decrypted_password[MAX_PASSWORD_LEN];
            // Note: Assembly function symbol is decrypt_data (without space)
            // The asm.h declares it with space for compatibility
            if (decrypt_data(system->users[i].password, system->users[i].key, decrypted_password)) {
                // Convert input password to uppercase for comparison
                char upper_password[MAX_PASSWORD_LEN];
                int j = 0;
                while (password[j] != '\0' && j < MAX_PASSWORD_LEN - 1) {
                    upper_password[j] = toupper(password[j]);
                    j++;
                }
                upper_password[j] = '\0';
                
                if (strcmp(decrypted_password, upper_password) == 0) {
                    system->current_user = &system->users[i];
                    return system->current_user;
                }
            }
        }
    }
    return NULL;
}

/**
 * Send command to sensors component
 */
int manager_send_cmd_to_sensors(const char *cmd) {
    // In real implementation, this would send via serial/communication
    // For now, just validate and return success
    if (cmd == NULL) return 0;
    printf("[Manager->Sensors] Sending: %s\n", cmd);
    return 1;
}

/**
 * Wait for data from sensors component
 * Returns dynamically allocated string (caller must free)
 */
char* manager_wait_for_data_from_sensors(void) {
    // In real implementation, this would read from serial/communication
    // For simulation, return a sample string
    char *data = (char*)malloc(MAX_SENSOR_STR_LEN);
    if (data == NULL) return NULL;
    
    // Simulated sensor data format: TOKEN&unit:xxxxxxx&value:xx#TOKEN&unit:xxxxxxxx&value:xx
    sprintf(data, "TEMP&unit:Celsius&value:25#HUM&unit:Percent&value:65");
    return data;
}

/**
 * Extract data from sensor string using Assembly function extract_data
 */
int manager_extract_data(const char *str, SensorData *sensors) {
    if (str == NULL || sensors == NULL) return 0;
    
    char token[10];
    char unit[20];
    int value;
    
    // Extract TEMP data using Assembly function extract_data
    strcpy(token, "TEMP");
    if (extract_data(str, token, unit, &value)) {
        // Enqueue temperature value using Assembly function enqueue_value
        enqueue_value(sensors->temp_buffer, SENSOR_BUFFER_SIZE,
                     &sensors->temp_nelem, &sensors->temp_head, &sensors->temp_tail, value);
        strncpy(sensors->temp_unit, unit, sizeof(sensors->temp_unit) - 1);
    }
    
    // Extract HUM data
    strcpy(token, "HUM");
    if (extract_data(str, token, unit, &value)) {
        // Enqueue humidity value using Assembly function
        enqueue_value(sensors->humidity_buffer, SENSOR_BUFFER_SIZE,
                     &sensors->humidity_nelem, &sensors->humidity_head, &sensors->humidity_tail, value);
        strncpy(sensors->humidity_unit, unit, sizeof(sensors->humidity_unit) - 1);
    }
    
    return 1;
}

/**
 * Update sensors data using Moving Median 2 filter
 * Uses Assembly functions: move_n_to_array, sort_array, median
 */
int manager_update_sensors_data(SensorData *sensors) {
    if (sensors == NULL) return 0;
    
    int temp_array[SENSOR_BUFFER_SIZE];
    int humidity_array[SENSOR_BUFFER_SIZE];
    int temp_median, humidity_median;
    
    // Move temperature values to array using Assembly function move_n_to_array
    if (sensors->temp_nelem > 0) {
        move_n_to_array(sensors->temp_buffer, SENSOR_BUFFER_SIZE,
                       &sensors->temp_nelem, &sensors->temp_tail, &sensors->temp_head,
                       sensors->temp_nelem, temp_array);
        
        // Sort array using Assembly function (ascending order)
        sort_array(temp_array, sensors->temp_nelem, 1);
        
        // Get median using Assembly function
        if (median(temp_array, sensors->temp_nelem, &temp_median)) {
            sensors->current_temp = temp_median;
        }
    }
    
    // Move humidity values to array using Assembly function
    if (sensors->humidity_nelem > 0) {
        move_n_to_array(sensors->humidity_buffer, SENSOR_BUFFER_SIZE,
                       &sensors->humidity_nelem, &sensors->humidity_tail, &sensors->humidity_head,
                       sensors->humidity_nelem, humidity_array);
        
        // Sort array using Assembly function (ascending order)
        sort_array(humidity_array, sensors->humidity_nelem, 1);
        
        // Get median using Assembly function
        if (median(humidity_array, sensors->humidity_nelem, &humidity_median)) {
            sensors->current_humidity = humidity_median;
        }
    }
    
    return 1;
}

/**
 * Wait for instructions from UI component
 * Returns dynamically allocated string (caller must free)
 */
char* manager_wait_for_instructions_from_ui(void) {
    // In real implementation, this would read from UI
    char *instruction = (char*)malloc(MAX_INSTRUCTION_LEN);
    if (instruction == NULL) return NULL;
    
    printf("Enter instruction: ");
    if (fgets(instruction, MAX_INSTRUCTION_LEN, stdin) != NULL) {
        // Remove newline
        size_t len = strlen(instruction);
        if (len > 0 && instruction[len-1] == '\n') {
            instruction[len-1] = '\0';
        }
        return instruction;
    }
    
    free(instruction);
    return NULL;
}

/**
 * Process instruction and return data for board and command for lightsigns
 * Returns 1 on success, 0 on failure
 */
int manager_process_instruction(ManagerSystem *system, const char *inst, char **data_out, char **cmd_out) {
    if (system == NULL || inst == NULL || data_out == NULL || cmd_out == NULL) return 0;
    
    *data_out = (char*)malloc(MAX_DATA_LEN);
    *cmd_out = (char*)malloc(MAX_CMD_LEN);
    if (*data_out == NULL || *cmd_out == NULL) {
        if (*data_out) free(*data_out);
        if (*cmd_out) free(*cmd_out);
        return 0;
    }
    
    // Simple instruction processing examples
    if (strncmp(inst, "GE", 2) == 0) {
        // Green LED command
        int track_num = 1; // Default track
        if (strlen(inst) > 2) {
            track_num = atoi(inst + 2);
        }
        // Format command using Assembly function format_command
        if (format_command("GE", track_num, *cmd_out)) {
            sprintf(*data_out, "Track %d: Green (Free)", track_num);
            return 1;
        }
    } else if (strncmp(inst, "RE", 2) == 0) {
        // Red LED command
        int track_num = 1;
        if (strlen(inst) > 2) {
            track_num = atoi(inst + 2);
        }
        if (format_command("RE", track_num, *cmd_out)) {
            sprintf(*data_out, "Track %d: Red (Busy)", track_num);
            return 1;
        }
    } else if (strncmp(inst, "YE", 2) == 0) {
        // Yellow LED command
        int track_num = 1;
        if (strlen(inst) > 2) {
            track_num = atoi(inst + 2);
        }
        if (format_command("YE", track_num, *cmd_out)) {
            sprintf(*data_out, "Track %d: Yellow (Assigned)", track_num);
            return 1;
        }
    } else if (strncmp(inst, "RB", 2) == 0) {
        // Red Blinking command
        int track_num = 1;
        if (strlen(inst) > 2) {
            track_num = atoi(inst + 2);
        }
        if (format_command("RB", track_num, *cmd_out)) {
            sprintf(*data_out, "Track %d: Red Blinking (Inoperative)", track_num);
        return 1;
        }
    } else {
        // Unknown instruction
        strcpy(*data_out, "Unknown instruction");
        strcpy(*cmd_out, "");
        return 0;
    }
    
    return 0;
}

/**
 * Send data to Board component
 */
int manager_send_data_to_board(const char *data) {
    if (data == NULL) return 0;
    // Board component displays the data
    printf("\n=== BOARD DISPLAY ===\n");
    printf("%s\n", data);
    printf("====================\n\n");
    return 1;
}

/**
 * Send command to LightSigns component
 */
int manager_send_cmd_to_lightsigns(const char *cmd) {
    if (cmd == NULL) return 0;
    // In real implementation, this would send via serial/communication
    printf("[Manager->LightSigns] Sending: %s\n", cmd);
        return 1;
}

/**
 * Record user action in log
 */
int manager_record_action(ManagerSystem *system, const char *instruction) {
    if (system == NULL || system->current_user == NULL || instruction == NULL) return 0;
    
    // TODO: Implement log recording
        return 1;
}

// ==================== LOG QUERY FUNCTIONS ====================

// Global log storage (data source) - simplified for query functionality
static LogStorage g_log_storage = {NULL, 0, 0};
#define INITIAL_LOG_CAPACITY 100

/**
 * Initialize log storage if needed
 */
static int ensure_log_storage(void) {
    if (g_log_storage.logs != NULL) {
        return 1; // Already initialized
    }
    
    g_log_storage.capacity = INITIAL_LOG_CAPACITY;
    g_log_storage.logs = (Log*)malloc(sizeof(Log) * g_log_storage.capacity);
    
    if (g_log_storage.logs == NULL) {
        return 0; // Allocation failed
    }
    
    g_log_storage.count = 0;
    return 1;
}

/**
 * Parse timestamp string (YYYY-MM-DD HH:MM:SS) to struct tm
 * Returns 1 on success, 0 on failure
 */
int parse_timestamp(const char *timestamp_str, struct tm *tm_out) {
    if (timestamp_str == NULL || tm_out == NULL) return 0;
    
    // Format: YYYY-MM-DD HH:MM:SS
    if (sscanf(timestamp_str, "%d-%d-%d %d:%d:%d",
               &tm_out->tm_year, &tm_out->tm_mon, &tm_out->tm_mday,
               &tm_out->tm_hour, &tm_out->tm_min, &tm_out->tm_sec) != 6) {
        return 0;
    }
    
    // Adjust for struct tm format (year since 1900, month 0-11)
    tm_out->tm_year -= 1900;
    tm_out->tm_mon -= 1;
    tm_out->tm_isdst = -1; // Unknown DST
    
        return 1;
}

/**
 * Compare two timestamp strings (YYYY-MM-DD HH:MM:SS)
 * Returns: -1 if ts1 < ts2, 0 if ts1 == ts2, 1 if ts1 > ts2
 */
int compare_timestamps(const char *ts1, const char *ts2) {
    if (ts1 == NULL || ts2 == NULL) return 0;
    
    struct tm tm1, tm2;
    if (!parse_timestamp(ts1, &tm1) || !parse_timestamp(ts2, &tm2)) {
        return 0; // Invalid timestamps
    }
    
    time_t time1 = mktime(&tm1);
    time_t time2 = mktime(&tm2);
    
    if (time1 < time2) return -1;
    if (time1 > time2) return 1;
    return 0;
}

/**
 * Check if timestamp is within date range
 * Returns 1 if within range, 0 otherwise
 */
static int is_in_date_range(const char *timestamp, const char *start_date, const char *end_date) {
    if (timestamp == NULL) return 0;
    
    // If no date range specified, include all
    if (start_date == NULL && end_date == NULL) return 1;
    
    // Check start date
    if (start_date != NULL) {
        if (compare_timestamps(timestamp, start_date) < 0) {
            return 0; // Before start date
        }
    }
    
    // Check end date
    if (end_date != NULL) {
        if (compare_timestamps(timestamp, end_date) > 0) {
            return 0; // After end date
        }
    }
    
    return 1; // Within range
}

/**
 * Query user actions from data source
 * 
 * @param user_id User ID to filter by (must be > 0)
 * @param start_date Optional start date filter (format: YYYY-MM-DD HH:MM:SS, NULL for no filter)
 * @param end_date Optional end date filter (format: YYYY-MM-DD HH:MM:SS, NULL for no filter)
 * @param result Output structure to store query results (must be initialized)
 * @return 1 on success, 0 on failure
 */
int query_user_actions(int user_id, const char *start_date, const char *end_date, LogQueryResult *result) {
    // Error handling: Validate inputs
    if (user_id <= 0) {
        return 0; // Invalid user ID
    }
    
    if (result == NULL) {
        return 0; // Invalid result pointer
    }
    
    // Error handling: Ensure data source is available
    if (!ensure_log_storage()) {
        return 0; // Data source initialization failed
    }
    
    // Error handling: Validate date formats if provided
    if (start_date != NULL) {
        struct tm tm_test;
        if (!parse_timestamp(start_date, &tm_test)) {
            return 0; // Invalid start_date format
        }
    }
    
    if (end_date != NULL) {
        struct tm tm_test;
        if (!parse_timestamp(end_date, &tm_test)) {
            return 0; // Invalid end_date format
        }
    }
    
    // Error handling: Validate date range (start_date <= end_date)
    if (start_date != NULL && end_date != NULL) {
        if (compare_timestamps(start_date, end_date) > 0) {
            return 0; // Invalid date range (start > end)
        }
    }
    
    // Initialize result structure
    result->count = 0;
    result->capacity = 0;
    result->entries = NULL;
    
    // Count matching entries
    int matching_count = 0;
    for (int i = 0; i < g_log_storage.count; i++) {
        if (g_log_storage.logs[i].user_id == user_id) {
            if (is_in_date_range(g_log_storage.logs[i].timestamp, start_date, end_date)) {
                matching_count++;
            }
        }
    }
    
    // Error handling: Check if user exists (has any logs)
    if (matching_count == 0) {
        // User might not exist or have no actions in date range
        // Return success with empty result (not an error)
        return 1;
    }
    
    // Allocate result array
    result->capacity = matching_count;
    result->entries = (Log*)malloc(sizeof(Log) * matching_count);
    
    if (result->entries == NULL) {
        return 0; // Memory allocation failed
    }
    
    // Copy matching entries to result
    int result_idx = 0;
    for (int i = 0; i < g_log_storage.count; i++) {
        if (g_log_storage.logs[i].user_id == user_id) {
            if (is_in_date_range(g_log_storage.logs[i].timestamp, start_date, end_date)) {
                // Copy log entry according to format specification
                result->entries[result_idx].log_id = g_log_storage.logs[i].log_id;
                result->entries[result_idx].user_id = g_log_storage.logs[i].user_id;
                strncpy(result->entries[result_idx].timestamp, g_log_storage.logs[i].timestamp, MAX_TIMESTAMP_LEN - 1);
                result->entries[result_idx].timestamp[MAX_TIMESTAMP_LEN - 1] = '\0';
                strncpy(result->entries[result_idx].action_type, g_log_storage.logs[i].action_type, MAX_ACTION_TYPE_LEN - 1);
                result->entries[result_idx].action_type[MAX_ACTION_TYPE_LEN - 1] = '\0';
                strncpy(result->entries[result_idx].action_details, g_log_storage.logs[i].action_details, MAX_ACTION_LEN - 1);
                result->entries[result_idx].action_details[MAX_ACTION_LEN - 1] = '\0';
                result_idx++;
            }
        }
    }
    
    result->count = matching_count;
    return 1; // Success
}

/**
 * Free log query result structure
 */
void free_log_query_result(LogQueryResult *result) {
    if (result == NULL) return;
    
    if (result->entries != NULL) {
        free(result->entries);
        result->entries = NULL;
    }
    
    result->count = 0;
    result->capacity = 0;
}

/**
 * Get username by user_id
 */
static const char* get_username_by_id(User *users, int user_count, int user_id) {
    if (users == NULL) return "unknown";
    
    for (int i = 0; i < user_count; i++) {
        if (users[i].user_id == user_id) {
            return users[i].username;
        }
    }
    return "unknown";
}

/**
 * Generate filename according to convention: user_actions_{username}_{date}.txt
 */
static int generate_filename(char *filename_buffer, int buffer_size, const char *username, const char *output_directory) {
    if (filename_buffer == NULL || username == NULL || output_directory == NULL) {
        return 0;
    }
    
    // Get current date in format YYYYMMDD
    time_t rawtime;
    struct tm *timeinfo;
    time(&rawtime);
    timeinfo = localtime(&rawtime);
    
    char date_str[9]; // YYYYMMDD + null terminator
    strftime(date_str, sizeof(date_str), "%Y%m%d", timeinfo);
    
    // Format: {directory}/user_actions_{username}_{date}.txt
    int written = snprintf(filename_buffer, buffer_size, "%s/user_actions_%s_%s.txt", 
                          output_directory, username, date_str);
    
    if (written < 0 || written >= buffer_size) {
        return 0; // Buffer too small or error
    }
    
    return 1;
}

/**
 * Check if user is Administrator
 */
int is_administrator(const User *user) {
    if (user == NULL) return 0;
    return user->role == USER_ROLE_ADMINISTRATOR;
}

/**
 * Log security audit trail entry
 */
static void log_audit_trail(const char *action, const User *user, const char *details, int success) {
    // In a real implementation, this would write to a secure audit log file
    // For now, we'll use a simple approach - could be enhanced to write to dedicated audit log
    time_t rawtime;
    struct tm *timeinfo;
    time(&rawtime);
    timeinfo = localtime(&rawtime);
    char timestamp[50];
    strftime(timestamp, sizeof(timestamp), "%Y-%m-%d %H:%M:%S", timeinfo);
    
    const char *username = (user != NULL) ? user->username : "UNKNOWN";
    const char *role = (user != NULL && is_administrator(user)) ? "Administrator" : "Operator";
    const char *status = success ? "SUCCESS" : "FAILED";
    
    // Audit trail output (in production, this would go to secure log file)
    printf("[AUDIT] %s | User: %s (%s) | Action: %s | Details: %s | Status: %s\n",
           timestamp, username, role, action, (details != NULL) ? details : "N/A", status);
}

/**
 * Set file permissions to restricted (read-only for owner, no access for others)
 * On Unix/Linux: 0600 (rw-------)
 * On Windows: uses _chmod equivalent
 */
static int set_restricted_file_permissions(const char *filename) {
    if (filename == NULL) return 0;
    
#ifdef _WIN32
    // Windows: Set file to read-only for owner only
    // Note: Windows permissions are more limited, this sets basic read-only
    if (_chmod(filename, _S_IREAD) != 0) {
        return 0; // Failed to set permissions
    }
#else
    // Unix/Linux: Set permissions to 0600 (rw-------)
    // Only owner can read/write, no access for group/others
    if (chmod(filename, S_IRUSR | S_IWUSR) != 0) {
        return 0; // Failed to set permissions
    }
#endif
    
    return 1; // Success
}

/**
 * Write user actions to .txt file
 * Formats data according to specification and saves to designated directory
 * Only Administrators can trigger file creation
 * 
 * @param requesting_user The user requesting the file creation (must be Administrator)
 * @param query_result The query result containing user actions (from query_user_actions)
 * @param users Array of all users (to get username)
 * @param user_count Number of users in the array
 * @param user_id User ID to get username
 * @param output_directory Directory where file should be saved (e.g., "./logs" or "/tmp")
 * @return 1 on success, 0 on failure
 */
int write_user_actions_to_file(const User *requesting_user, const LogQueryResult *query_result, 
                                User *users, int user_count, int user_id, const char *output_directory) {
    // Authentication check: Verify user has Administrator role
    if (!is_administrator(requesting_user)) {
        // Security audit trail: Log unauthorized access attempt
        log_audit_trail("FILE_CREATION_ATTEMPT", requesting_user, "Unauthorized access - not Administrator", 0);
        return 0; // Only Administrators can create files
    }
    
    // Authorization logic: Additional validation
    if (requesting_user == NULL) {
        log_audit_trail("FILE_CREATION_ATTEMPT", NULL, "Null user pointer", 0);
        return 0; // Invalid user
    }
    
    // Error handling: Validate inputs
    if (query_result == NULL) {
        log_audit_trail("FILE_CREATION_ATTEMPT", requesting_user, "Invalid query result", 0);
        return 0; // Invalid query result
    }
    
    if (users == NULL || user_count <= 0) {
        log_audit_trail("FILE_CREATION_ATTEMPT", requesting_user, "Invalid users array", 0);
        return 0; // Invalid users array
    }
    
    if (user_id <= 0) {
        log_audit_trail("FILE_CREATION_ATTEMPT", requesting_user, "Invalid target user ID", 0);
        return 0; // Invalid user ID
    }
    
    if (output_directory == NULL) {
        log_audit_trail("FILE_CREATION_ATTEMPT", requesting_user, "Invalid output directory", 0);
        return 0; // Invalid output directory
    }
    
    // Error handling: Check if query result has data
    if (query_result->count == 0) {
        log_audit_trail("FILE_CREATION_ATTEMPT", requesting_user, "No data to write", 0);
        return 0; // No data to write
    }
    
    // Get username
    const char *username = get_username_by_id(users, user_count, user_id);
    
    // Generate filename according to convention
    char filename[512];
    if (!generate_filename(filename, sizeof(filename), username, output_directory)) {
        log_audit_trail("FILE_CREATION_ATTEMPT", requesting_user, "Failed to generate filename", 0);
        return 0; // Failed to generate filename
    }
    
    // Security audit trail: Log file creation attempt
    char audit_details[256];
    snprintf(audit_details, sizeof(audit_details), "Creating file: %s for user_id: %d", filename, user_id);
    log_audit_trail("FILE_CREATION_ATTEMPT", requesting_user, audit_details, 1);
    
    // Open file for writing
    FILE *file = fopen(filename, "w");
    if (file == NULL) {
        log_audit_trail("FILE_CREATION_FAILED", requesting_user, "Failed to open file", 0);
        return 0; // Failed to open file (permissions or directory doesn't exist)
    }
    
    // Write header according to specification
    fprintf(file, "========================================\n");
    fprintf(file, "USER ACTION LOG REPORT\n");
    fprintf(file, "========================================\n\n");
    fprintf(file, "User ID: %d\n", user_id);
    fprintf(file, "Username: %s\n", username);
    fprintf(file, "Total Actions: %d\n", query_result->count);
    
    // Get current timestamp for report generation
    time_t rawtime;
    struct tm *timeinfo;
    time(&rawtime);
    timeinfo = localtime(&rawtime);
    char current_timestamp[50];
    strftime(current_timestamp, sizeof(current_timestamp), "%Y-%m-%d %H:%M:%S", timeinfo);
    fprintf(file, "Generated on: %s\n", current_timestamp);
    
    fprintf(file, "\n========================================\n");
    fprintf(file, "LOG ENTRIES\n");
    fprintf(file, "========================================\n\n");
    
    // Write log entries according to specification format
    for (int i = 0; i < query_result->count; i++) {
        fprintf(file, "Log ID: %d\n", query_result->entries[i].log_id);
        fprintf(file, "User ID: %d\n", query_result->entries[i].user_id);
        fprintf(file, "Username: %s\n", username);
        fprintf(file, "Action Type: %s\n", query_result->entries[i].action_type);
        fprintf(file, "Action Details: %s\n", query_result->entries[i].action_details);
        fprintf(file, "Timestamp: %s\n", query_result->entries[i].timestamp);
        fprintf(file, "----------------------------------------\n");
    }
    
    fprintf(file, "\n========================================\n");
    fprintf(file, "END OF REPORT\n");
    fprintf(file, "========================================\n");
    
    // Close file
    if (fclose(file) != 0) {
        log_audit_trail("FILE_CREATION_FAILED", requesting_user, "Failed to close file", 0);
        return 0; // Failed to close file
    }
    
    // Set restricted file permissions (only owner can read/write, no access for others)
    if (!set_restricted_file_permissions(filename)) {
        // Log warning but don't fail - file was created successfully
        log_audit_trail("FILE_PERMISSIONS_WARNING", requesting_user, "Failed to set restricted permissions", 0);
        // Continue anyway as file was created
    } else {
        // Log successful permission setting
        log_audit_trail("FILE_CREATION_SUCCESS", requesting_user, "File created with restricted permissions", 1);
    }
    
    return 1; // Success
}


/**
 * Main Manager algorithm as specified
 */
int manager_main_loop(ManagerSystem *system) {
    if (system == NULL || system->current_user == NULL) return 0;
    
    char *sensor_str;
    char *instruction;
    char *data;
    char *cmd;
    
    while (1) {
        // Step 3: send_cmd_to_sensors(cmd)
        manager_send_cmd_to_sensors(SENSOR_CMD);
        
        // Step 4: str = wait_for_data_from_sensors()
        sensor_str = manager_wait_for_data_from_sensors();
        if (sensor_str == NULL) {
            printf("Error: Failed to get sensor data\n");
            continue;
        }
        
        // Step 5: extract_data(str, data) - using Assembly function
        manager_extract_data(sensor_str, &system->sensors);
        free(sensor_str);
        
        // Step 6: update_sensors_data(data) - using Moving Median 2 filter
        manager_update_sensors_data(&system->sensors);
        
        // Step 7: inst = wait_for_instructions_from_ui()
        instruction = manager_wait_for_instructions_from_ui();
        if (instruction == NULL) {
            printf("Error: Failed to get instruction\n");
            continue;
        }
        
        // Check for exit command
        if (strcmp(instruction, "exit") == 0 || strcmp(instruction, "quit") == 0) {
            free(instruction);
            break;
        }
        
        // Step 8: {data, cmd} = process(inst)
        if (manager_process_instruction(system, instruction, &data, &cmd)) {
            // Step 9: send_data_to_board(data)
            manager_send_data_to_board(data);
            
            // Step 10: send_cmd_to_lightsigns(cmd)
            manager_send_cmd_to_lightsigns(cmd);
            
            // Step 11: record(user, inst)
            manager_record_action(system, instruction);
            
            free(data);
            free(cmd);
        } else {
            printf("Error: Failed to process instruction\n");
            if (data) free(data);
            if (cmd) free(cmd);
        }
        
        free(instruction);
    }
    
    return 1;
}

/**
 * Main entry point
 */
int main(void) {
    ManagerSystem system;
    
    // Initialize system
    if (!manager_init(&system)) {
        printf("Error: Failed to initialize system\n");
        return 1;
    }
    
    // TODO: Add default users, tracks, trains here
    
    // Step 1: user = login(username, password)
    // For now, simulate login
    printf("=== Railway Station Management System ===\n");
    printf("Login required\n");
    // In real implementation, get username/password from UI
    // User* user = manager_login(&system, "admin", "PASSWORD");
    
    // Step 2: while(1) { ... }
    // manager_main_loop(&system);
    
    // Cleanup
    manager_cleanup(&system);
    
    return 0;
}
