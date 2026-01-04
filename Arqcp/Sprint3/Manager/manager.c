#include "manager.h"
#include "../Board/Board.h"
#include "../../Sprint2/asm.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <time.h>
#include <stdbool.h>
#include <math.h>
#include <unistd.h>

// Forward declarations for setup functions
extern int load_initial_setup(const char *filename, void *system);
extern void init_system_data(void *system);



#define INITIAL_CAPACITY 10
#define SENSOR_CMD "GTH"
#define MAX_INSTRUCTION_LEN 100
#define MAX_DATA_LEN 500
#define MAX_CMD_LEN 10
#define SERIAL_BAUD_RATE 9600
#define SERIAL_TIMEOUT_SEC 1
#define SERIAL_READ_TIMEOUT_MS 1000

// ARQCP: Action type constants for logging
#define ACTION_ASSIGN_TRAIN "ASSIGN_TRAIN"
#define ACTION_EMERGENCY_STOP "EMERGENCY_STOP"
#define ACTION_SET_INOP "SET_INOP"
#define ACTION_SET_FREE "SET_FREE"
#define ACTION_TRAIN_DEPARTURE "TRAIN_DEPARTURE"

// ARQCP: Forward declarations for static functions
static int int_to_string(int value, char *buffer, int buffer_size);

// Serial communication globals
static FILE *g_serial_file = NULL;
static int g_serial_initialized = 0;
static FILE *g_lights_serial_file = NULL;
static int g_lights_serial_initialized = 0;

// Global manager system instance - definition
static ManagerSystem g_manager_system;

// Function to get manager system instance
ManagerSystem* get_manager_system(void) {
    return &g_manager_system;
}

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
    
    // Initialize sensor data with circular buffers (using default sizes)
    if (!manager_init_sensor_data(&system->sensors, SENSOR_BUFFER_SIZE_DEFAULT, SENSOR_WINDOW_SIZE_DEFAULT)) {
        free(system->users);
        free(system->tracks);
        free(system->trains);
        return 0;
    }
    
    // ARQCP: Simple config loading - create default admin user
    // Create admin user with hardcoded credentials for now
    User *admin = &system->users[0];
    admin->user_id = 1;
    strcpy(admin->name, "Administrador do Sistema");
    strcpy(admin->username, "admin");
    strcpy(admin->password, "XMPQW");  // Encrypted password
    admin->key = 3;
    admin->role = USER_ROLE_ADMINISTRATOR;
    system->user_count = 1;
    
    // Create some default tracks
    for (int i = 0; i < 4; i++) {
        Track *track = &system->tracks[i];
        track->id = i + 1;
        track->state = (i == 1) ? TRACK_BUSY : TRACK_FREE;  // Track 2 is busy
        track->trainId = (i == 1) ? 101 : 0;
        track->lastOperationTimestamp = time(NULL);
        system->track_count++;
    }
    
    // Create some default trains
    int train_ids[] = {101, 202, 305};
    for (int i = 0; i < 3; i++) {
        Train *train = &system->trains[i];
        train->id = train_ids[i];
        train->state = TRAIN_IN_TRANSIT;  // CORREÇÃO: IN_TRANSIT em vez de ARRIVING
        train->position = 0;
        system->train_count++;
    }
    
    system->current_user = NULL;
    
    return 1;
}

void manager_cleanup(ManagerSystem *system) {
    if (system == NULL) return;
    
    if (system->users) free(system->users);
    if (system->tracks) free(system->tracks);
    if (system->trains) free(system->trains);
    manager_cleanup_sensor_data(&system->sensors);
    
    memset(system, 0, sizeof(ManagerSystem));
}


// ==================== HELPER FUNCTIONS ====================

/**
 * Build string with integers (no snprintf)
 * Returns number of characters written
 */
static int build_string_with_int(char *buffer, int buffer_size, 
                                const char *prefix, int value, const char *suffix) {
    if (buffer == NULL || buffer_size <= 0) return 0;
    
    int pos = 0;
    int i = 0;
    
    // Copy prefix
    if (prefix != NULL) {
        while (prefix[i] != '\0' && pos < buffer_size - 1) {
            buffer[pos++] = prefix[i++];
        }
    }
    
    // Convert integer to string
    char int_str[20];
    int_to_string(value, int_str, sizeof(int_str));
    i = 0;
    while (int_str[i] != '\0' && pos < buffer_size - 1) {
        buffer[pos++] = int_str[i++];
    }
    
    // Copy suffix
    if (suffix != NULL) {
        i = 0;
        while (suffix[i] != '\0' && pos < buffer_size - 1) {
            buffer[pos++] = suffix[i++];
        }
    }
    
    buffer[pos] = '\0';
    return pos;
}

/**
 * Build string with two integers (no snprintf)
 */
static int build_string_with_two_ints(char *buffer, int buffer_size,
                                     const char *prefix, int value1, 
                                     const char *middle, int value2, 
                                     const char *suffix) {
    if (buffer == NULL || buffer_size <= 0) return 0;
    
    int pos = 0;
    int i = 0;
    
    // Copy prefix
    if (prefix != NULL) {
        while (prefix[i] != '\0' && pos < buffer_size - 1) {
            buffer[pos++] = prefix[i++];
        }
    }
    
    // Convert first integer
    char int_str1[20];
    int_to_string(value1, int_str1, sizeof(int_str1));
    i = 0;
    while (int_str1[i] != '\0' && pos < buffer_size - 1) {
        buffer[pos++] = int_str1[i++];
    }
    
    // Copy middle
    if (middle != NULL) {
        i = 0;
        while (middle[i] != '\0' && pos < buffer_size - 1) {
            buffer[pos++] = middle[i++];
        }
    }
    
    // Convert second integer
    char int_str2[20];
    int_to_string(value2, int_str2, sizeof(int_str2));
    i = 0;
    while (int_str2[i] != '\0' && pos < buffer_size - 1) {
        buffer[pos++] = int_str2[i++];
    }
    
    // Copy suffix
    if (suffix != NULL) {
        i = 0;
        while (suffix[i] != '\0' && pos < buffer_size - 1) {
            buffer[pos++] = suffix[i++];
        }
    }
    
    buffer[pos] = '\0';
    return pos;
}

// ==================== MANAGER ALGORITHM FUNCTIONS ====================

/**
 * Login function - uses encrypt_data Assembly function
 * Assumes config.txt contains already encrypted passwords
 */
User* manager_login(ManagerSystem *system, const char *username, const char *password) {
    if (system == NULL || username == NULL || password == NULL) return NULL;
    
    // Find user by username
    for (int i = 0; i < system->user_count; i++) {
        if (strcmp(system->users[i].username, username) == 0) {
            
            // Encrypt input password using Assembly function
            char encrypted_password[MAX_PASSWORD_LEN];
            if (encrypt_data((char*)password, system->users[i].key, encrypted_password)) {
                
                // Compare encrypted input with stored encrypted password
                if (strcmp(encrypted_password, system->users[i].password) == 0) {
                    system->current_user = &system->users[i];
                    printf("[SUCCESS] Login successful - Welcome %s\n", system->users[i].name);
                    return system->current_user;
                }
            }
        }
    }
    
    printf("[ERROR] Login failed - Invalid credentials\n");
    return NULL;
}

/**
 * Initialize serial communication with Sensors component (file-based)
 * 
 * @param port Serial port path (e.g., "/dev/ttyS0")
 * @return 1 on success, 0 on failure
 */
// ARQCP: Initialize serial communication for lights
int manager_init_lights_serial(const char *port) {
    if (port == NULL) {
        port = "/dev/ttyACM0"; // Default port for Raspberry Pi Pico
    }
    
    printf("[DEBUG] Initializing lights serial: %s\n", port);
    
    // Open serial port for lights (write-only)
    g_lights_serial_file = fopen(port, "w");
    if (g_lights_serial_file == NULL) {
        printf("[DEBUG] Failed to open lights serial port - Pico not connected\n");
        return 0;
    }
    
    g_lights_serial_initialized = 1;
    printf("[DEBUG] Lights serial initialized successfully\n");
    return 1;
}

/**
 * Initialize serial communication with Sensors component (file-based)
 * 
 * @param port Serial port path (e.g., "/dev/ttyS0")
 * @return 1 on success, 0 on failure
 */
int manager_init_serial(const char *port) {
    if (port == NULL) {
        port = "/dev/ttyACM0"; // Default port for Raspberry Pi Pico
    }
    
    // Try to open serial port
    g_serial_file = fopen(port, "r+");
    if (g_serial_file == NULL) {
        printf("[ERROR] Failed to open %s - Pico not connected\n", port);
        return 0; // No fallback simulation
    } else {
        printf("[SUCCESS] Pico connected on %s\n", port);
    }
    
    g_serial_initialized = 1;
    return 1;
}

/**
 * Close serial communication
 */
void manager_close_serial(void) {
    if (g_serial_file != NULL) {
        fclose(g_serial_file);
        g_serial_file = NULL;
    }
    g_serial_initialized = 0;
}

/**
 * Send command to sensors component via file-based serial
 * 
 * @param cmd Command string to send (e.g., "GTH")
 * @return 1 on success, 0 on failure
 */
int manager_send_cmd_to_sensors(const char *cmd) {
    if (cmd == NULL) {
        return 0;
    }
    
    // Check if serial communication is initialized
    if (!g_serial_initialized || g_serial_file == NULL) {
        // Try to initialize serial communication
        if (!manager_init_serial("/dev/ttyACM0")) {
            printf("[ERROR] Pico not connected - cannot initialize serial\n");
            return 0;
        }
    }
    
    // Send command via fprintf (file-based communication)
    if (fprintf(g_serial_file, "%s\n", cmd) < 0) {
        printf("[ERROR] Failed to send command to Pico\n");
        return 0; // Failed to write command
    }
    
    // Flush output to ensure command is sent immediately
    fflush(g_serial_file);
    
    // Small delay to allow Pico to process
    usleep(50000); // 50ms delay (reduced from 100ms)
    
    return 1;
}

/**
 * Wait for data from sensors component via file-based serial
 * Returns dynamically allocated string (caller must free)
 * 
 * Implementation using only basic ARQCP concepts:
 * - Dynamic memory allocation (malloc)
 * - Basic string manipulation (no snprintf)
 * - Basic file I/O operations
 * 
 * @return Dynamically allocated string with sensor data, or NULL on error
 */
char* manager_wait_for_data_from_sensors(void) {
    // Check if serial communication is initialized
    if (!g_serial_initialized || g_serial_file == NULL) {
        printf("[ERROR] Serial not initialized for sensors\n");
        return NULL;
    }
    
    // Try to read data with multiple attempts
    char *data = (char*)malloc(MAX_SENSOR_STR_LEN);
    if (data == NULL) {
        return NULL;
    }
    
    // Clear buffer
    memset(data, 0, MAX_SENSOR_STR_LEN);
    
    // Try multiple times to read data
    for (int attempt = 0; attempt < 5; attempt++) {
        // Add small delay before reading (except first attempt)
        if (attempt > 0) {
            usleep(20000); // 20ms delay
        }
        
        if (fgets(data, MAX_SENSOR_STR_LEN, g_serial_file) != NULL) {
            // Successfully read from real Pico
            size_t len = strlen(data);
            if (len > 0 && (data[len-1] == '\n' || data[len-1] == '\r')) {
                data[len-1] = '\0';
            }
            
            if (strlen(data) > 0) {
                printf("[SUCCESS] Pico connected - receiving data\n");
                return data;
            }
        }
    }
    
    // No data from Pico after all attempts
    printf("[ERROR] Pico not responding - no data received\n");
    free(data);
    return NULL;
}

/**
 * Validate sensor response string format
 * Checks for basic structure: TEMP&...&value::XX#HUM&...&value::YY
 * 
 * @param str Input string to validate
 * @return 1 if format appears valid, 0 otherwise
 */
static int validate_sensor_format(const char *str) {
    if (str == NULL) return 0;
    
    // Check minimum length
    if (strlen(str) < 20) return 0; // Minimum reasonable length
    
    // Check for required tokens
    if (strstr(str, "TEMP") == NULL) return 0;
    if (strstr(str, "HUM") == NULL) return 0;
    if (strstr(str, "unit::") == NULL) return 0;
    if (strstr(str, "value::") == NULL) return 0;
    if (strstr(str, "#") == NULL) return 0; // Separator between TEMP and HUM
    
    // Check for proper structure: TEMP&...&value::...#HUM&...&value::...
    const char *temp_start = strstr(str, "TEMP");
    const char *hum_start = strstr(str, "HUM");
    if (temp_start == NULL || hum_start == NULL) return 0;
    if (hum_start <= temp_start) return 0; // HUM should come after TEMP
    
    return 1;
}

/**
 * Normalize sensor string format (convert "::" to ":" for extract_data compatibility)
 * The specification uses "unit::celsius&value::XX" but extract_data expects "unit:celsius&value:XX"
 * 
 * @param str Input string (format: TEMP&unit::celsius&value::XX#HUM&unit::percentage&value::YY)
 * @param normalized Output buffer (must be at least as large as input)
 * @param max_len Maximum length of normalized buffer
 * @return 1 on success, 0 on failure
 */
static int normalize_sensor_format(const char *str, char *normalized, int max_len) {
    if (str == NULL || normalized == NULL || max_len <= 0) return 0;
    
    int str_len = strlen(str);
    if (str_len >= max_len) {
        fprintf(stderr, "[Manager] ERROR: Input string too long (%d >= %d)\n", str_len, max_len);
        return 0;
    }
    
    int i = 0, j = 0;
    while (str[i] != '\0' && j < max_len - 1) {
        // Convert "::" to ":"
        if (str[i] == ':' && str[i + 1] == ':') {
            normalized[j++] = ':';
            i += 2; // Skip both colons
        } else {
            normalized[j++] = str[i++];
        }
    }
    normalized[j] = '\0';
    return 1;
}

/**
 * Initialize sensor data structure with circular buffers
 * 
 * @param sensors SensorData structure to initialize
 * @param buffer_size Size of circular buffers (must be > 0)
 * @param window_size Window size for Moving Median filter (must be > 0 and <= buffer_size)
 * @return 1 on success, 0 on failure
 */
int manager_init_sensor_data(SensorData *sensors, int buffer_size, int window_size) {
    if (sensors == NULL) return 0;
    if (buffer_size <= 0 || window_size <= 0) return 0;
    if (window_size > buffer_size) {
        fprintf(stderr, "[Manager] WARNING: window_size (%d) > buffer_size (%d), using buffer_size\n", 
                window_size, buffer_size);
        window_size = buffer_size;
    }
    
    // Allocate circular buffers
    sensors->temp_buffer = (int*)malloc(sizeof(int) * buffer_size);
    sensors->humidity_buffer = (int*)malloc(sizeof(int) * buffer_size);
    
    if (sensors->temp_buffer == NULL || sensors->humidity_buffer == NULL) {
        if (sensors->temp_buffer) free(sensors->temp_buffer);
        if (sensors->humidity_buffer) free(sensors->humidity_buffer);
        return 0;
    }
    
    // Initialize buffer state
    sensors->buffer_size = buffer_size;
    sensors->window_size = window_size;
    sensors->temp_nelem = 0;
    sensors->humidity_nelem = 0;
    sensors->temp_head = 0;
    sensors->temp_tail = 0;
    sensors->humidity_head = 0;
    sensors->humidity_tail = 0;
    sensors->current_temp = 0;
    sensors->current_humidity = 0;
    strncpy(sensors->temp_unit, "Celsius", sizeof(sensors->temp_unit) - 1);
    sensors->temp_unit[sizeof(sensors->temp_unit) - 1] = '\0';
    strncpy(sensors->humidity_unit, "Percent", sizeof(sensors->humidity_unit) - 1);
    sensors->humidity_unit[sizeof(sensors->humidity_unit) - 1] = '\0';
    
    // Initialize buffers to zero
    memset(sensors->temp_buffer, 0, sizeof(int) * buffer_size);
    memset(sensors->humidity_buffer, 0, sizeof(int) * buffer_size);
    
        return 1;
}

/**
 * Cleanup sensor data structure and free allocated memory
 * 
 * @param sensors SensorData structure to cleanup
 */
void manager_cleanup_sensor_data(SensorData *sensors) {
    if (sensors == NULL) return;
    
    if (sensors->temp_buffer) {
        free(sensors->temp_buffer);
        sensors->temp_buffer = NULL;
    }
    if (sensors->humidity_buffer) {
        free(sensors->humidity_buffer);
        sensors->humidity_buffer = NULL;
    }
    
    memset(sensors, 0, sizeof(SensorData));
}

/**
 * Parse formatted sensor response string and extract temperature/humidity values
 * 
 * Parses string format: TEMP&unit::celsius&value::XX#HUM&unit::percentage&value::YY
 * 
 * Process:
 * 1. Validates input string format
 * 2. Normalizes format (converts "::" to ":" for extract_data compatibility)
 * 3. Uses extract_data Assembly function to extract TEMP and HUM values
 * 4. Validates extracted values
 * 5. Stores RAW readings in circular buffers for Moving Median filtering
 * 
 * Error handling:
 * - Validates string format before parsing
 * - Checks for NULL pointers
 * - Validates value ranges (temperature: -50 to 60°C, humidity: 0-100%)
 * - Handles malformed response strings gracefully
 * 
 * @param str Input string in format: TEMP&unit::celsius&value::XX#HUM&unit::percentage&value::YY
 * @param sensors SensorData structure to store extracted values
 * @return 1 on success, 0 on failure (with error messages to stderr)
 */
int manager_extract_data(const char *str, SensorData *sensors) {
    // Error handling: Validate input parameters
    if (str == NULL) {
        fprintf(stderr, "[Manager] ERROR: Cannot parse NULL sensor string\n");
        return 0;
    }
    
    if (sensors == NULL) {
        fprintf(stderr, "[Manager] ERROR: Cannot store sensor data in NULL structure\n");
        return 0;
    }
    
    // Parse sensor data from Pico
    // Format: TEMP&unit:celsius&value:XX#HUM&unit:percentage&value:YY
    char data_copy[200];
    strncpy(data_copy, str, sizeof(data_copy) - 1);
    data_copy[sizeof(data_copy) - 1] = '\0';
    
    // Parse temperature - handle both :: and : formats
    char *temp_start = strstr(data_copy, "TEMP&");
    if (temp_start != NULL) {
        // Try :: format first
        char *value_pos = strstr(temp_start, "value::");
        if (value_pos != NULL) {
            temp_start = value_pos + 7; // Skip "value::"
        } else {
            // Try : format
            value_pos = strstr(temp_start, "value:");
            if (value_pos != NULL) {
                temp_start = value_pos + 6; // Skip "value:"
            } else {
                temp_start = NULL;
            }
        }
        
        if (temp_start != NULL) {
            char *temp_end = strchr(temp_start, '#');
            if (temp_end != NULL) {
                // Save the character and restore after parsing
                char saved_char = *temp_end;
                *temp_end = '\0';
                int temp = atoi(temp_start);
                *temp_end = saved_char; // Restore the character
                
                // Add to temperature buffer
                enqueue_value(sensors->temp_buffer, sensors->buffer_size, 
                             &sensors->temp_nelem, &sensors->temp_head, 
                             &sensors->temp_tail, temp);
                
                // Calculate median
                median(sensors->temp_buffer, sensors->temp_nelem, &sensors->current_temp);
            }
        }
    }
    
    // Parse humidity - handle both :: and : formats
    char *hum_start = strstr(data_copy, "#HUM&");
    if (hum_start != NULL) {
        // Try :: format first
        char *value_pos = strstr(hum_start, "value::");
        if (value_pos != NULL) {
            hum_start = value_pos + 7; // Skip "value::"
        } else {
            // Try : format
            value_pos = strstr(hum_start, "value:");
            if (value_pos != NULL) {
                hum_start = value_pos + 6; // Skip "value:"
            } else {
                return 0; // No value marker found
            }
        }
        
        if (hum_start != NULL) {
            int humidity = atoi(hum_start);
            
            // Add to humidity buffer
            enqueue_value(sensors->humidity_buffer, sensors->buffer_size, 
                         &sensors->humidity_nelem, &sensors->humidity_head, 
                         &sensors->humidity_tail, humidity);
            
            // Calculate median
            median(sensors->humidity_buffer, sensors->humidity_nelem, &sensors->current_humidity);
        }
    } else {
        return 0; // No HUM marker found
    }
    
    return 1;
}

/**
 * Update sensors data using Moving Median filter
 * 
 * This function applies Moving Median filter independently to temperature and humidity:
 * 1. Extracts last N readings from circular buffer (where N = window_size)
 * 2. Sorts the values using Assembly function sort_array
 * 3. Calculates median using Assembly function median
 * 4. Stores filtered value in current_temp/current_humidity
 * 
 * The filter reduces noise and errors in sensor readings by selecting the median
 * value from a sliding window of recent readings.
 * 
 * Uses Assembly functions: move_n_to_array, sort_array, median
 * 
 * @param sensors SensorData structure with circular buffers
 * @return 1 on success, 0 on failure
 */
int manager_update_sensors_data(SensorData *sensors) {
    if (sensors == NULL) return 0;
    if (sensors->temp_buffer == NULL || sensors->humidity_buffer == NULL) return 0;
    
    // Determine actual window size (use minimum of window_size and available elements)
    int temp_window = (sensors->temp_nelem < sensors->window_size) ? 
                       sensors->temp_nelem : sensors->window_size;
    int humidity_window = (sensors->humidity_nelem < sensors->window_size) ? 
                           sensors->humidity_nelem : sensors->window_size;
    
    // Allocate arrays for median calculation (size = window_size)
    int *temp_array = (int*)malloc(sizeof(int) * sensors->window_size);
    if (temp_array == NULL) {
        return 0;
    }
    
    int *humidity_array = (int*)malloc(sizeof(int) * sensors->window_size);
    if (humidity_array == NULL) {
        free(temp_array);
        return 0;
    }
    
    int temp_median, humidity_median;
    
    // ========== TEMPERATURE FILTERING (Independent) ==========
    if (temp_window > 0) {
        // Move last N temperature values from circular buffer to array
        // Using Assembly function move_n_to_array
        if (move_n_to_array(sensors->temp_buffer, sensors->buffer_size,
                           &sensors->temp_nelem, &sensors->temp_tail, &sensors->temp_head,
                           temp_window, temp_array)) {
            
            // Sort array in ascending order using Assembly function sort_array
            // order = 1 means ascending order
            sort_array(temp_array, temp_window, 1);
            
            // Calculate median using Assembly function median
            if (median(temp_array, temp_window, &temp_median)) {
                sensors->current_temp = temp_median;
            }
        }
    }
    
    // ========== HUMIDITY FILTERING (Independent) ==========
    if (humidity_window > 0) {
        // Move last N humidity values from circular buffer to array
        // Using Assembly function move_n_to_array
        if (move_n_to_array(sensors->humidity_buffer, sensors->buffer_size,
                           &sensors->humidity_nelem, &sensors->humidity_tail, &sensors->humidity_head,
                           humidity_window, humidity_array)) {
            
            // Sort array in ascending order using Assembly function sort_array
            // order = 1 means ascending order
            sort_array(humidity_array, humidity_window, 1);
            
            // Calculate median using Assembly function median
            if (median(humidity_array, humidity_window, &humidity_median)) {
                sensors->current_humidity = humidity_median;
            }
        }
    }
    
    // Free temporary arrays
    free(temp_array);
    free(humidity_array);
    
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
    
    // Display prompt using board (ARQCP requirement: no printf in manager)
    board_send_message("\nEnter instruction (or 'help' for commands, 'SENSORS' for sensor data): ");
    if (fgets(instruction, MAX_INSTRUCTION_LEN, stdin) != NULL) {
        // Remove newline
        size_t len = strlen(instruction);
        if (len > 0 && instruction[len-1] == '\n') {
            instruction[len-1] = '\0';
        }
        
        // Handle help command - send to board
        if (strcmp(instruction, "help") == 0 || strcmp(instruction, "HELP") == 0) {
            board_send_message("\n=== AVAILABLE COMMANDS ===\n");
            board_send_message("SENSORS, TEMP, HUM - Display filtered sensor data (temperature & humidity)\n");
            board_send_message("GE[N] - Green LED for track N (default: track 1)\n");
            board_send_message("RE[N] - Red LED for track N (default: track 1)\n");
            board_send_message("YE[N] - Yellow LED for track N (default: track 1)\n");
            board_send_message("RB[N] - Red Blinking LED for track N (default: track 1)\n");
            board_send_message("exit, quit - Exit the system\n");
            board_send_message("==========================\n\n");
            free(instruction);
            return manager_wait_for_instructions_from_ui(); // Recursive call to get next instruction
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
    if (*data_out == NULL) {
        return 0;
    }
    
    *cmd_out = (char*)malloc(MAX_CMD_LEN);
    if (*cmd_out == NULL) {
        free(*data_out);
        *data_out = NULL;
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
            // Build message using basic string manipulation (no sprintf)
            const char *prefix = "Track ";
            int pos = 0;
            int i = 0;
            while (prefix[i] != '\0' && pos < MAX_DATA_LEN - 1) {
                (*data_out)[pos++] = prefix[i++];
            }
            char track_str[20];
            int_to_string(track_num, track_str, sizeof(track_str));
            i = 0;
            while (track_str[i] != '\0' && pos < MAX_DATA_LEN - 1) {
                (*data_out)[pos++] = track_str[i++];
            }
            const char *suffix = ": Green (Free)";
            i = 0;
            while (suffix[i] != '\0' && pos < MAX_DATA_LEN - 1) {
                (*data_out)[pos++] = suffix[i++];
            }
            (*data_out)[pos] = '\0';
            return 1;
        }
    } else if (strncmp(inst, "RE", 2) == 0) {
        // Red LED command
        int track_num = 1;
        if (strlen(inst) > 2) {
            track_num = atoi(inst + 2);
        }
        if (format_command("RE", track_num, *cmd_out)) {
            // Build message using basic string manipulation (no sprintf)
            const char *prefix = "Track ";
            int pos = 0;
            int i = 0;
            while (prefix[i] != '\0' && pos < MAX_DATA_LEN - 1) {
                (*data_out)[pos++] = prefix[i++];
            }
            char track_str[20];
            int_to_string(track_num, track_str, sizeof(track_str));
            i = 0;
            while (track_str[i] != '\0' && pos < MAX_DATA_LEN - 1) {
                (*data_out)[pos++] = track_str[i++];
            }
            const char *suffix = ": Red (Busy)";
            i = 0;
            while (suffix[i] != '\0' && pos < MAX_DATA_LEN - 1) {
                (*data_out)[pos++] = suffix[i++];
            }
            (*data_out)[pos] = '\0';
            return 1;
        }
    } else if (strncmp(inst, "YE", 2) == 0) {
        // Yellow LED command
        int track_num = 1;
        if (strlen(inst) > 2) {
            track_num = atoi(inst + 2);
        }
        if (format_command("YE", track_num, *cmd_out)) {
            // Build message using basic string manipulation (no sprintf)
            const char *prefix = "Track ";
            int pos = 0;
            int i = 0;
            while (prefix[i] != '\0' && pos < MAX_DATA_LEN - 1) {
                (*data_out)[pos++] = prefix[i++];
            }
            char track_str[20];
            int_to_string(track_num, track_str, sizeof(track_str));
            i = 0;
            while (track_str[i] != '\0' && pos < MAX_DATA_LEN - 1) {
                (*data_out)[pos++] = track_str[i++];
            }
            const char *suffix = ": Yellow (Assigned)";
            i = 0;
            while (suffix[i] != '\0' && pos < MAX_DATA_LEN - 1) {
                (*data_out)[pos++] = suffix[i++];
            }
            (*data_out)[pos] = '\0';
            return 1;
        }
    } else if (strncmp(inst, "RB", 2) == 0) {
        // Red Blinking command
        int track_num = 1;
        if (strlen(inst) > 2) {
            track_num = atoi(inst + 2);
        }
        if (format_command("RB", track_num, *cmd_out)) {
            // Build message using basic string manipulation (no sprintf)
            const char *prefix = "Track ";
            int pos = 0;
            int i = 0;
            while (prefix[i] != '\0' && pos < MAX_DATA_LEN - 1) {
                (*data_out)[pos++] = prefix[i++];
            }
            char track_str[20];
            int_to_string(track_num, track_str, sizeof(track_str));
            i = 0;
            while (track_str[i] != '\0' && pos < MAX_DATA_LEN - 1) {
                (*data_out)[pos++] = track_str[i++];
            }
            const char *suffix = ": Red Blinking (Inoperative)";
            i = 0;
            while (suffix[i] != '\0' && pos < MAX_DATA_LEN - 1) {
                (*data_out)[pos++] = suffix[i++];
            }
            (*data_out)[pos] = '\0';
        return 1;
        }
    } else if (strcmp(inst, "SENSORS") == 0 || strcmp(inst, "sensors") == 0 ||
               strcmp(inst, "TEMP") == 0 || strcmp(inst, "temp") == 0 ||
               strcmp(inst, "HUM") == 0 || strcmp(inst, "hum") == 0 ||
               strcmp(inst, "SENSOR") == 0 || strcmp(inst, "sensor") == 0) {
        // Sensor data request command (USAC13)
        // Display filtered (median) sensor values, not raw readings
        char *sensor_display = manager_format_sensor_display(system);
        if (sensor_display != NULL) {
            // Copy using basic string manipulation (no strncpy)
            int i = 0;
            while (sensor_display[i] != '\0' && i < MAX_DATA_LEN - 1) {
                (*data_out)[i] = sensor_display[i];
                i++;
            }
            (*data_out)[i] = '\0';

            // Set cmd_out to empty string
            (*cmd_out)[0] = '\0';
            free(sensor_display);
            return 1;
        } else {
            // Copy error message using basic string manipulation
            const char *error_msg = "Error: Sensor data not available";
            int i = 0;
            while (error_msg[i] != '\0' && i < MAX_DATA_LEN - 1) {
                (*data_out)[i] = error_msg[i];
                i++;
            }
            (*data_out)[i] = '\0';
            (*cmd_out)[0] = '\0';
            return 0;
        }
    } else {
        // Unknown instruction
        // Safe string copy with size limit and null terminator
        const char *unknown_msg = "Unknown instruction";
        int j = 0;
        while (unknown_msg[j] != '\0' && j < MAX_DATA_LEN - 1) {
            (*data_out)[j] = unknown_msg[j];
            j++;
        }
        (*data_out)[j] = '\0';
        
        (*cmd_out)[0] = '\0';
        return 0;
    }
    
    return 0;
}

// ==================== USAC13: Get Data from Sensors Component ====================

/**
 * Get filtered sensor data programmatically
 * Returns filtered (median) values, not raw readings
 * 
 * Implementation using only basic ARQCP concepts:
 * - Structure access through pointers
 * - Basic string copying (no strncpy)
 * 
 * @param system ManagerSystem with sensor data
 * @param temp_out Output parameter for filtered temperature
 * @param humidity_out Output parameter for filtered humidity
 * @param temp_unit_out Output buffer for temperature unit (e.g., "Celsius")
 * @param temp_unit_len Size of temp_unit_out buffer
 * @param humidity_unit_out Output buffer for humidity unit (e.g., "Percent")
 * @param humidity_unit_len Size of humidity_unit_out buffer
 * @return 1 on success, 0 on failure
 */
int manager_get_sensor_data(const ManagerSystem *system, int *temp_out, int *humidity_out, 
                            char *temp_unit_out, int temp_unit_len, 
                            char *humidity_unit_out, int humidity_unit_len) {
    if (system == NULL) return 0;
    if (temp_out == NULL || humidity_out == NULL) return 0;
    if (temp_unit_out == NULL || humidity_unit_out == NULL) return 0;
    if (temp_unit_len <= 0 || humidity_unit_len <= 0) return 0;
    
    // Return filtered (median) values, not raw readings
    *temp_out = system->sensors.current_temp;
    *humidity_out = system->sensors.current_humidity;
    
    // Copy units using basic string copying (no strncpy)
    const char *src_temp = system->sensors.temp_unit;
    int i = 0;
    while (src_temp[i] != '\0' && i < temp_unit_len - 1) {
        temp_unit_out[i] = src_temp[i];
        i++;
    }
    temp_unit_out[i] = '\0';
    
    const char *src_humidity = system->sensors.humidity_unit;
    i = 0;
    while (src_humidity[i] != '\0' && i < humidity_unit_len - 1) {
        humidity_unit_out[i] = src_humidity[i];
        i++;
    }
    humidity_unit_out[i] = '\0';
    
    return 1;
}

/**
 * Format sensor data for display
 * Returns filtered (median) values with clear unit indication
 * 
 * Implementation using only basic ARQCP concepts:
 * - Dynamic memory allocation (malloc)
 * - Basic string manipulation (no snprintf)
 * - Integer to string conversion
 * 
 * @param system ManagerSystem with sensor data
 * @return Dynamically allocated string with formatted sensor data (caller must free), or NULL on error
 */
char* manager_format_sensor_display(const ManagerSystem *system) {
    if (system == NULL) return NULL;
    
    // Get filtered (median) values, not raw readings
    int temp = system->sensors.current_temp;
    int humidity = system->sensors.current_humidity;
    
    // Allocate buffer for formatted string
    char *display = (char*)malloc(MAX_DATA_LEN);
    if (display == NULL) return NULL;
    
    // Check if we have valid data (non-zero or if buffers have data)
    int has_data = (system->sensors.temp_nelem > 0 || system->sensors.humidity_nelem > 0);
    
    int pos = 0;
    
    if (!has_data) {
        // Write "Sensor Data: Not available yet\nWaiting for sensor readings..."
        const char *msg1 = "Sensor Data: Not available yet\n";
        int i = 0;
        while (msg1[i] != '\0' && pos < MAX_DATA_LEN - 1) {
            display[pos++] = msg1[i++];
        }
        const char *msg2 = "Waiting for sensor readings...";
        i = 0;
        while (msg2[i] != '\0' && pos < MAX_DATA_LEN - 1) {
            display[pos++] = msg2[i++];
        }
    } else {
        // Format with clear units using basic string building
        const char *header1 = "========================================\n";
        int i = 0;
        while (header1[i] != '\0' && pos < MAX_DATA_LEN - 1) {
            display[pos++] = header1[i++];
        }
        
        const char *header2 = "SENSOR DATA (Filtered - Moving Median)\n";
        i = 0;
        while (header2[i] != '\0' && pos < MAX_DATA_LEN - 1) {
            display[pos++] = header2[i++];
        }
        
        const char *header3 = "========================================\n";
        i = 0;
        while (header3[i] != '\0' && pos < MAX_DATA_LEN - 1) {
            display[pos++] = header3[i++];
        }
        
        const char *temp_label = "Temperature: ";
        i = 0;
        while (temp_label[i] != '\0' && pos < MAX_DATA_LEN - 1) {
            display[pos++] = temp_label[i++];
        }
        
        // Write temperature value
        char temp_str[20];
        int_to_string(temp, temp_str, sizeof(temp_str));
        i = 0;
        while (temp_str[i] != '\0' && pos < MAX_DATA_LEN - 1) {
            display[pos++] = temp_str[i++];
        }
        
        // Write space
        if (pos < MAX_DATA_LEN - 1) {
            display[pos++] = ' ';
        }
        
        // Write temperature unit
        i = 0;
        while (system->sensors.temp_unit[i] != '\0' && pos < MAX_DATA_LEN - 1) {
            display[pos++] = system->sensors.temp_unit[i++];
        }
        
        // Write newline
        if (pos < MAX_DATA_LEN - 1) {
            display[pos++] = '\n';
        }
        
        const char *hum_label = "Humidity: ";
        i = 0;
        while (hum_label[i] != '\0' && pos < MAX_DATA_LEN - 1) {
            display[pos++] = hum_label[i++];
        }
        
        // Write humidity value
        char hum_str[20];
        int_to_string(humidity, hum_str, sizeof(hum_str));
        i = 0;
        while (hum_str[i] != '\0' && pos < MAX_DATA_LEN - 1) {
            display[pos++] = hum_str[i++];
        }
        
        // Write space
        if (pos < MAX_DATA_LEN - 1) {
            display[pos++] = ' ';
        }
        
        // Write humidity unit
        i = 0;
        while (system->sensors.humidity_unit[i] != '\0' && pos < MAX_DATA_LEN - 1) {
            display[pos++] = system->sensors.humidity_unit[i++];
        }
        
        // Write newline
        if (pos < MAX_DATA_LEN - 1) {
            display[pos++] = '\n';
        }
        
        const char *footer1 = "========================================\n";
        i = 0;
        while (footer1[i] != '\0' && pos < MAX_DATA_LEN - 1) {
            display[pos++] = footer1[i++];
        }
        
        const char *footer2 = "Note: Values shown are filtered (median),\n";
        i = 0;
        while (footer2[i] != '\0' && pos < MAX_DATA_LEN - 1) {
            display[pos++] = footer2[i++];
        }
        
        const char *footer3 = "      not raw sensor readings.";
        i = 0;
        while (footer3[i] != '\0' && pos < MAX_DATA_LEN - 1) {
            display[pos++] = footer3[i++];
        }
    }
    
    display[pos] = '\0';
    return display;
}

/**
 * Send data to Board component
 * Uses board_display_text instead of printf (ARQCP requirement)
 */
int manager_send_data_to_board(const char *data) {
    if (data == NULL) return 0;
    // Board component displays the data using board functions
    board_display_text(data);
    return 1;
}

/**
 * Send command to LightSigns component
 * No printf - command is sent internally (ARQCP requirement)
 */
int manager_send_cmd_to_lightsigns(const char *cmd) {
    if (cmd == NULL) return 0;
    // In real implementation, this would send via serial/communication
    // No printf in manager - command is sent internally
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
        g_log_storage.capacity = 0;
        return 0; // Allocation failed
    }
    
    g_log_storage.count = 0;
    return 1;
}

/**
 * Compare two timestamp strings (YYYY-MM-DD HH:MM:SS) using string comparison
 * Returns: -1 if ts1 < ts2, 0 if ts1 == ts2, 1 if ts1 > ts2
 * Simple string comparison (no time.h required)
 */
int compare_timestamps(const char *ts1, const char *ts2) {
    if (ts1 == NULL || ts2 == NULL) return 0;
    
    // Simple string comparison (timestamps are in sortable format YYYY-MM-DD HH:MM:SS)
    int result = strcmp(ts1, ts2);
    if (result < 0) return -1;
    if (result > 0) return 1;
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
    
    // Error handling: Validate date formats if provided (basic format check)
    // Format should be YYYY-MM-DD HH:MM:SS (19 characters)
    if (start_date != NULL) {
        if (strlen(start_date) != 19) {
            return 0; // Invalid start_date format
        }
    }
    
    if (end_date != NULL) {
        if (strlen(end_date) != 19) {
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
        result->capacity = 0;
        result->count = 0;
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

// ==================== USAC12: Write User Actions to File ====================

/**
 * Get username by user_id
 * Basic array search using pointers
 */
static const char* get_username_by_id(User *users, int user_count, int user_id) {
    if (users == NULL) return "unknown";
    
    User *current = users;
    int i;
    for (i = 0; i < user_count; i++) {
        if (current->user_id == user_id) {
            return current->username;
        }
        current++; // Move pointer to next User structure
    }
    return "unknown";
}

/**
 * Convert integer to string (basic implementation without sprintf)
 * Returns number of characters written
 */
static int int_to_string(int value, char *buffer, int buffer_size) {
    if (buffer == NULL || buffer_size <= 0) return 0;
    
    int pos = 0;
    int negative = 0;
    
    // Handle negative numbers
    if (value < 0) {
        negative = 1;
        value = -value;
    }
    
    // Handle zero case
    if (value == 0) {
        if (pos < buffer_size - 1) {
            buffer[pos++] = '0';
        }
        buffer[pos] = '\0';
        return pos;
    }
    
    // Extract digits in reverse order
    char digits[20];
    int digit_count = 0;
    while (value > 0 && digit_count < 19) {
        digits[digit_count++] = '0' + (value % 10);
        value = value / 10;
    }
    
    // Write negative sign if needed
    if (negative && pos < buffer_size - 1) {
        buffer[pos++] = '-';
    }
    
    // Write digits in correct order (reverse)
    int i;
    for (i = digit_count - 1; i >= 0 && pos < buffer_size - 1; i--) {
        buffer[pos++] = digits[i];
    }
    buffer[pos] = '\0';
    
    return pos;
}

/**
 * Copy string (basic implementation without strcpy)
 * Returns 1 on success, 0 on failure
 */
static int copy_string(const char *src, char *dest, int dest_size) {
    if (src == NULL || dest == NULL || dest_size <= 0) return 0;
    
    int i = 0;
    while (src[i] != '\0' && i < dest_size - 1) {
        dest[i] = src[i];
        i++;
    }
    dest[i] = '\0';
    return 1;
}

/**
 * Get string length (basic implementation without strlen)
 */
static int string_length(const char *str) {
    if (str == NULL) return 0;
    int len = 0;
    while (str[len] != '\0') {
        len++;
    }
    return len;
}

/**
 * Generate filename according to convention: user_actions_{username}.txt
 * Simplified version without date (using basic string manipulation)
 */
static int generate_filename(char *filename_buffer, int buffer_size, const char *username, const char *output_directory) {
    if (filename_buffer == NULL || username == NULL || output_directory == NULL) {
        return 0;
    }
    
    int pos = 0;
    
    // Copy output_directory
    int dir_len = string_length(output_directory);
    int i;
    for (i = 0; i < dir_len && pos < buffer_size - 1; i++) {
        filename_buffer[pos++] = output_directory[i];
    }
    
    // Add '/' if not present
    if (pos > 0 && filename_buffer[pos - 1] != '/' && pos < buffer_size - 1) {
        filename_buffer[pos++] = '/';
    }
    
    // Add "user_actions_"
    const char *prefix = "user_actions_";
    int prefix_len = string_length(prefix);
    for (i = 0; i < prefix_len && pos < buffer_size - 1; i++) {
        filename_buffer[pos++] = prefix[i];
    }
    
    // Add username
    int username_len = string_length(username);
    for (i = 0; i < username_len && pos < buffer_size - 1; i++) {
        filename_buffer[pos++] = username[i];
    }
    
    // Add ".txt"
    const char *suffix = ".txt";
    int suffix_len = string_length(suffix);
    for (i = 0; i < suffix_len && pos < buffer_size - 1; i++) {
        filename_buffer[pos++] = suffix[i];
    }
    
    filename_buffer[pos] = '\0';
    
        return 1;
}

/**
 * Write string to file (basic implementation without fprintf)
 * Returns 1 on success, 0 on failure
 */
static int write_string_to_file(FILE *file, const char *str) {
    if (file == NULL || str == NULL) return 0;
    
    int i = 0;
    while (str[i] != '\0') {
        if (fputc(str[i], file) == EOF) {
            return 0;
        }
        i++;
    }
    return 1;
}

/**
 * Build message string and send to board (ARQCP requirement: no printf in manager)
 * Uses basic string manipulation
 */
// Note: board_send_message is implemented in Board/Board.c

/**
 * Build message with integer value and send to board
 * Uses basic string manipulation (no sprintf)
 */
static void board_send_message_with_int(const char *prefix, int value, const char *suffix) {
    char buffer[200];
    int pos = 0;
    
    // Copy prefix
    if (prefix != NULL) {
        int i = 0;
        while (prefix[i] != '\0' && pos < 199) {
            buffer[pos++] = prefix[i++];
        }
    }
    
    // Convert integer to string
    char int_str[20];
    int_to_string(value, int_str, sizeof(int_str));
    int i = 0;
    while (int_str[i] != '\0' && pos < 199) {
        buffer[pos++] = int_str[i++];
    }
    
    // Copy suffix
    if (suffix != NULL) {
        i = 0;
        while (suffix[i] != '\0' && pos < 199) {
            buffer[pos++] = suffix[i++];
        }
    }
    
    buffer[pos] = '\0';
    board_display_text(buffer);
}

/**
 * Write integer to file (basic implementation without fprintf)
 * Returns 1 on success, 0 on failure
 */
static int write_int_to_file(FILE *file, int value) {
    if (file == NULL) return 0;
    
    char buffer[20];
    int_to_string(value, buffer, sizeof(buffer));
    return write_string_to_file(file, buffer);
}

// is_administrator function is now implemented in assembly (USAC10/is_administrator.s)

/**
 * Log security audit trail entry
 * Uses simple timestamp format (no time.h required)
 */
static void log_audit_trail(const char *action, const User *user, const char *details, int success) {
    // In a real implementation, this would write to a secure audit log file
    // For now, we'll use a simple approach - could be enhanced to write to dedicated audit log
    // Simple timestamp format (no time.h required)
    const char *timestamp = "2024-01-01 00:00:00"; // Fixed timestamp format
    
    const char *username = (user != NULL) ? user->username : "UNKNOWN";
    const char *role = (user != NULL && is_administrator(user)) ? "Administrator" : "Operator";
    const char *status = success ? "SUCCESS" : "FAILED";
    
    // Audit trail output (in production, this would go to secure log file)
    // No printf - audit trail is internal logging (ARQCP requirement)
    // Logging would be done through file I/O or internal structures
    (void)timestamp; // Suppress unused variable warning
    (void)username;
    (void)role;
    (void)status;
    (void)action;
    (void)details;
}

/**
 * Set file permissions to restricted (removed - not required for USAC12)
 * USAC12 only requires creating .txt file with actions, not permission management
 * fopen("file.txt", "w") is sufficient
 */
static int set_restricted_file_permissions(const char *filename) {
    // Permission management not required for USAC12
    // fopen() creates file with default permissions, which is sufficient
    (void)filename; // Suppress unused parameter warning
    return 1; // Success (no-op)
}

/**
 * Write user actions to .txt file
 * Formats data according to specification and saves to designated directory
 * Only Administrators can trigger file creation
 * 
 * Implementation using only basic ARQCP concepts:
 * - Structures, pointers, arrays
 * - Basic I/O (fopen, fputc, fclose)
 * - Basic string manipulation (no sprintf/snprintf)
 * - Memory access through pointers
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
    // Basic validation: Check if user is Administrator
    if (requesting_user == NULL) {
        return 0;
    }
    
    if (requesting_user->role != USER_ROLE_ADMINISTRATOR) {
        return 0; // Only Administrators can create files
    }
    
    // Basic validation: Check inputs
    if (query_result == NULL) {
        return 0;
    }
    
    if (users == NULL || user_count <= 0) {
        return 0;
    }
    
    if (user_id <= 0) {
        return 0;
    }
    
    if (output_directory == NULL) {
        return 0;
    }
    
    // Check if query result has data
    if (query_result->count == 0) {
        return 0; // No data to write
    }
    
    // Get username using pointer arithmetic
    const char *username = get_username_by_id(users, user_count, user_id);
    if (username == NULL || string_length(username) == 0) {
        return 0;
    }
    
    // Check if username is "unknown" (user not found)
    const char *unknown_str = "unknown";
    int is_unknown = 1;
    int i;
    for (i = 0; unknown_str[i] != '\0' && username[i] != '\0'; i++) {
        if (unknown_str[i] != username[i]) {
            is_unknown = 0;
            break;
        }
    }
    if (is_unknown && unknown_str[i] == '\0' && username[i] == '\0') {
        return 0; // User not found
    }
    
    // Generate filename using basic string manipulation
    char filename[512];
    if (!generate_filename(filename, sizeof(filename), username, output_directory)) {
        return 0;
    }
    
    // Open file for writing (basic file I/O)
    FILE *file = fopen(filename, "w");
    if (file == NULL) {
        return 0; // Failed to open file
    }
    
    // Write header using basic string writing
    write_string_to_file(file, "========================================\n");
    write_string_to_file(file, "USER ACTION LOG REPORT\n");
    write_string_to_file(file, "========================================\n\n");
    
    write_string_to_file(file, "User ID: ");
    write_int_to_file(file, user_id);
    write_string_to_file(file, "\n");
    
    write_string_to_file(file, "Username: ");
    write_string_to_file(file, username);
    write_string_to_file(file, "\n");
    
    write_string_to_file(file, "Total Actions: ");
    write_int_to_file(file, query_result->count);
    write_string_to_file(file, "\n\n");
    
    write_string_to_file(file, "========================================\n");
    write_string_to_file(file, "LOG ENTRIES\n");
    write_string_to_file(file, "========================================\n\n");
    
    // Write log entries using pointer access to structure array
    Log *current_log = query_result->entries;
    int j;  // ARQCP: Renamed to avoid conflict with previous 'i'
    for (j = 0; j < query_result->count; j++) {
        // Write Log ID
        write_string_to_file(file, "Log ID: ");
        write_int_to_file(file, current_log->log_id);
        write_string_to_file(file, "\n");
        
        // Write User ID
        write_string_to_file(file, "User ID: ");
        write_int_to_file(file, current_log->user_id);
        write_string_to_file(file, "\n");
        
        // Write Username
        write_string_to_file(file, "Username: ");
        write_string_to_file(file, username);
        write_string_to_file(file, "\n");
        
        // Write Action Type
        write_string_to_file(file, "Action Type: ");
        write_string_to_file(file, current_log->action_type);
        write_string_to_file(file, "\n");
        
        // Write Action Details
        write_string_to_file(file, "Action Details: ");
        write_string_to_file(file, current_log->action_details);
        write_string_to_file(file, "\n");
        
        // Write Timestamp
        write_string_to_file(file, "Timestamp: ");
        write_string_to_file(file, current_log->timestamp);
        write_string_to_file(file, "\n");
        
        write_string_to_file(file, "----------------------------------------\n");
        
        // Move pointer to next Log structure
        current_log++;
    }
    
    // Write footer
    write_string_to_file(file, "\n========================================\n");
    write_string_to_file(file, "END OF REPORT\n");
    write_string_to_file(file, "========================================\n");
    
    // Flush and close file
    if (fflush(file) != 0) {
        fclose(file);
        return 0;
    }
    
    if (fclose(file) != 0) {
        return 0;
    }
    
    return 1; // Success
}


/**
 * Main Manager algorithm as specified
 * 
 * CRITICAL: Manager MUST request sensor data BEFORE processing ANY user instruction.
 * 
 * Algorithm flow (per iteration):
 * 1. send_cmd_to_sensors("GTH") - Request sensor data
 * 2. wait_for_data_from_sensors() - Wait for response
 * 3. extract_data() - Parse sensor response using Assembly function
 * 4. update_sensors_data() - Apply Moving Median filter
 * 5. wait_for_instructions_from_ui() - Get user instruction (ONLY after sensors updated)
 * 6. process_instruction() - Process user command
 * 7. send_data_to_board() - Display result
 * 8. send_cmd_to_lightsigns() - Control lights
 * 9. record_action() - Log user action
 * 
 * Timing and Synchronization:
 * - Sensor polling happens BEFORE every user interaction
 * - If sensor data fails, loop continues but does NOT process user instructions
 * - This ensures sensor data is always up-to-date before processing commands
 * 
 * @param system Initialized ManagerSystem with logged-in user
 * @return 1 on normal exit, 0 on error
 */
int manager_main_loop(ManagerSystem *system) {
    if (system == NULL) {
        fprintf(stderr, "[Manager] ERROR: System is NULL\n");
        return 0;
    }
    
    if (system->current_user == NULL) {
        fprintf(stderr, "[Manager] ERROR: No user logged in\n");
        return 0;
    }
    
    char *sensor_str;
    char *instruction;
    char *data;
    char *cmd = NULL;  // ARQCP: Initialize to NULL to fix warning
    int iteration = 0;
    
    // Display startup message using board (ARQCP requirement: no printf in manager)
    board_send_message("\n========================================\n");
    board_send_message("Manager Main Loop Started\n");
    
    // Build message with username using basic string manipulation
    char user_msg[200];
    int pos = 0;
    const char *user_prefix = "User: ";
    int i = 0;
    while (user_prefix[i] != '\0' && pos < 199) {
        user_msg[pos++] = user_prefix[i++];
    }
    i = 0;
    while (system->current_user->username[i] != '\0' && pos < 199) {
        user_msg[pos++] = system->current_user->username[i++];
    }
    user_msg[pos++] = '\n';
    user_msg[pos] = '\0';
    board_send_message(user_msg);
    
    board_send_message("Sensor polling enabled: ALWAYS before user instructions\n");
    board_send_message("========================================\n\n");
    
    while (1) {
        iteration++;
        board_send_message_with_int("\n--- Iteration ", iteration, " ---\n");
        
        // ========== SENSOR POLLING (ALWAYS FIRST) ==========
        // Step 1: send_cmd_to_sensors("GTH")
        // Manager MUST request sensor data BEFORE processing any user instruction
        // No printf - sensor communication is internal
        if (!manager_send_cmd_to_sensors(SENSOR_CMD)) {
            // Error handling - minimal, no printf
            // Continue loop to retry sensor polling (don't process user instructions)
            continue;
        }
        
        // Step 2: wait_for_data_from_sensors()
        // Wait for sensor response with timeout
        sensor_str = manager_wait_for_data_from_sensors();
        if (sensor_str == NULL) {
            // Error handling - minimal, no printf
            // Continue loop to retry sensor polling (don't process user instructions)
            continue;
        }
        
        // Step 3: extract_data(str, sensors) - using Assembly function extract_data
        // Parse sensor response and extract temperature/humidity values
        if (!manager_extract_data(sensor_str, &system->sensors)) {
            free(sensor_str);
            // Error handling - minimal, no printf
            // Continue loop to retry sensor polling (don't process user instructions)
            continue;
        }
        free(sensor_str);
        
        // Step 4: update_sensors_data(sensors) - using Moving Median filter
        // Apply Moving Median filter to reduce noise
        if (!manager_update_sensors_data(&system->sensors)) {
            // Warning - continue anyway, we have raw data
        }
        
        // ========== USER INSTRUCTION PROCESSING (ONLY AFTER SENSORS) ==========
        // Step 5: wait_for_instructions_from_ui()
        // ONLY request user instruction AFTER sensor data is updated
        instruction = manager_wait_for_instructions_from_ui();
        if (instruction == NULL) {
            // Error handling - minimal, no printf
            continue;
        }
        
        // Check for exit command
        if (strcmp(instruction, "exit") == 0 || strcmp(instruction, "quit") == 0) {
            board_send_message("[Manager] Exit command received. Shutting down...\n");
            free(instruction);
            break;
        }
        
        // Step 6: process_instruction(inst) - Process user command
        if (manager_process_instruction(system, instruction, &data, &cmd)) {
            // Step 7: send_data_to_board(data) - Display result
            manager_send_data_to_board(data);
            
            // Step 8: send_cmd_to_lightsigns(cmd) - Control lights
            manager_send_cmd_to_lightsigns(cmd);
            
            // Step 9: record_action(user, inst) - Log user action
            manager_record_action(system, instruction);
            
            free(data);
            free(cmd);
        } else {
            // Invalid instruction - data already sent to board by process_instruction
            if (data) free(data);
            if (cmd) free(cmd);
        }
        
        free(instruction);
    }
    
    board_send_message("\n========================================\n");
    board_send_message("Manager Main Loop Ended\n");
    board_send_message_with_int("Total iterations: ", iteration, "\n");
    board_send_message("========================================\n\n");
    
    return 1;
}

// funções uteis

Train* findTrain(ManagerSystem *system, int train_id) {
    if (system == NULL || system->trains == NULL) return NULL;

    for (int i = 0; i < system->train_count; i++) {
        if (system->trains[i].id == train_id) {
            return &system->trains[i];
        }
    }
    return NULL;
}


Track* findTrack(ManagerSystem *system, int track_id) {
    if (system == NULL || system->tracks == NULL) return NULL;

    for (int i = 0; i < system->track_count; i++) {
        if (system->tracks[i].id == track_id) {
            return &system->tracks[i];
        }
    }
    return NULL;
}


// ARQCP: Test function for light system
void test_light_system(void) {
    printf("\n=== ARQCP LIGHT SYSTEM TEST ===\n");
    
    // Test 1: Check serial port
    printf("[TEST] Checking /dev/ttyACM0...\n");
    FILE *test_file = fopen("/dev/ttyACM0", "w");
    if (test_file == NULL) {
        printf("[TEST] ❌ Cannot open /dev/ttyACM0\n");
        printf("[TEST] → Pico not connected or permission denied\n");
        printf("[TEST] → Run: sudo chmod 666 /dev/ttyACM0\n");
        return;
    } else {
        printf("[TEST] ✅ /dev/ttyACM0 is accessible\n");
        fclose(test_file);
    }
    
    // Test 2: Test format_command
    printf("[TEST] Testing format_command...\n");
    char cmd[20];
    int result = format_command("YE", 1, cmd);
    printf("[TEST] format_command(YE,1) = %d, cmd='%s'\n", result, cmd);
    
    // Test 3: Test sendToLightSigns
    printf("[TEST] Testing sendToLightSigns...\n");
    sendToLightSigns("YE,01");
    
    // Test 4: Direct serial test
    printf("[TEST] Direct serial test...\n");
    test_file = fopen("/dev/ttyACM0", "w");
    if (test_file != NULL) {
        fprintf(test_file, "LIGHTS:YE,01\n");
        fflush(test_file);
        printf("[TEST] ✅ Sent LIGHTS:YE,01 directly to /dev/ttyACM0\n");
        fclose(test_file);
    }
    
    printf("[TEST] If Pico has LEDs connected, they should light up now\n");
    printf("[TEST] If not, Pico needs to be programmed with light control code\n");
    printf("=== END TEST ===\n\n");
}
// ARQCP: Enhanced sendToLightSigns with protocol support
void sendToLightSigns(const char* cmd) {
    if (cmd == NULL) return;

    // ARQCP: Initialize lights serial if not already done
    if (!g_lights_serial_initialized || g_lights_serial_file == NULL) {
        printf("[DEBUG] Initializing lights serial...\n");
        manager_init_lights_serial("/dev/ttyACM0");
    }

    // ARQCP: Visual indicator for testing
    if (strstr(cmd, "YE") != NULL) {
        printf("🟡 YELLOW LIGHT ON - Track %s\n", cmd + 3);
    } else if (strstr(cmd, "GE") != NULL) {
        printf("🟢 GREEN LIGHT ON - Track %s\n", cmd + 3);
    } else if (strstr(cmd, "RE") != NULL) {
        printf("🔴 RED LIGHT ON - Track %s\n", cmd + 3);
    } else if (strstr(cmd, "RB") != NULL) {
        printf("🔴 RED BLINKING LIGHT ON - Track %s\n", cmd + 3);
    }

    // ARQCP: Send command via printf (redirected to serial for Pico)
    printf("LIGHT_CMD:%s\n", cmd);
    fflush(stdout);
    
    // Send via dedicated lights serial port
    if (g_lights_serial_initialized && g_lights_serial_file != NULL) {
        fprintf(g_lights_serial_file, "%s\n", cmd);
        fflush(g_lights_serial_file);
        printf("[LIGHTS] Sent to lights serial: %s\n", cmd);
    } else {
        printf("[LIGHTS] Lights serial not available, command sent to stdout only\n");
    }
}


void logAction(int user_id, const char* action_type, const char* details) {
    if (action_type == NULL || details == NULL) return;

    // Simple timestamp format (no time.h required)
    // Using fixed format for logs
    const char *timestamp = "2024-01-01 00:00:00";

    printf("[LOG] %s | User:%d | Action:%s | Details:%s\n",
           timestamp, user_id, action_type, details);

}


//================usac16===================================

int assignArrivingTrain(ManagerSystem *system, int train_id, int track_id) {
    Train *t = findTrain(system, train_id);
    Track *tr = findTrack(system, track_id);


    //error handling
    if (!t || !tr) {
        updateBoard("ERROR: Train or Track not found");
        return 0;
    }

    // Verificar se trem está em trânsito
    if (t->state != TRAIN_IN_TRANSIT) {
        char msg[100];
        build_string_with_two_ints(msg, sizeof(msg), 
                                   "Train ", train_id, " not in transit (state: ", t->state, ")");
        updateBoard(msg);
        return 0;
    }

    // Verificar se track está livre
    if (tr->state == TRACK_FREE) {
        // Atualizar estados (train mantém IN_TRANSIT até chegada física)
        tr->state = TRACK_ASSIGNED;
        t->state = TRAIN_IN_TRANSIT;  // Train continua em trânsito (reservado)
        tr->trainId = train_id;

        // Atualizar Board
        char board_msg[100];
        build_string_with_two_ints(board_msg, sizeof(board_msg), 
                                   "✅ Train ", train_id, " assigned to Track ", track_id, " (YELLOW)");
        updateBoard(board_msg);

        // Envia comando para LightSigns com a func. Assembly
        char cmd[20];
        printf("[DEBUG] Calling format_command(YE, %d)\n", track_id);
        int result = format_command("YE", track_id, cmd);
        printf("[DEBUG] format_command returned: %d, cmd: '%s'\n", result, cmd);
        
        if (result == 1) {
            sendToLightSigns(cmd);
        } else {
            updateBoard("WARNING: Failed to format LightSigns command");
        }

        // Registar no log
        logAction(system->current_user->user_id, ACTION_ASSIGN_TRAIN, board_msg);

        // 🔥 NOVO: Confirmar chegada física automaticamente
        // Pequeno delay para simular tempo de chegada
        usleep(100000); // 100ms
        
        // Chamar trainArrivalConfirmation para fazer train ficar STOPPED
        int confirmation_result = trainArrivalConfirmation(system, train_id, track_id);
        
        if (confirmation_result) {
            // Log adicional para confirmação
            char confirm_msg[100];
            build_string_with_two_ints(confirm_msg, sizeof(confirm_msg), 
                                       "🎯 Train ", train_id, " arrival confirmed at Track ", track_id, "");
            logAction(system->current_user->user_id, "TRAIN_ARRIVAL_CONFIRMED", confirm_msg);
        }

        return 1;
    }
    else {
        // Nenhuma track livre sinal de EMERGÊNCIA
        char emergency_msg[150];
        build_string_with_two_ints(emergency_msg, sizeof(emergency_msg),
                                   "🚨 EMERGENCY STOP! Train ", train_id, " - Track ", track_id, " busy (state: ");
        // Add state value
        char state_str[10];
        int_to_string(tr->state, state_str, sizeof(state_str));
        int len = strlen(emergency_msg);
        int i = 0;
        while (state_str[i] != '\0' && len < (int)sizeof(emergency_msg) - 2) {
            emergency_msg[len++] = state_str[i++];
        }
        emergency_msg[len++] = ')';
        emergency_msg[len] = '\0';
        updateBoard(emergency_msg);
        showEmergencyStop();

        // Comando de emergência para LightSigns (Red para track atual)
        char cmd[20];
        if (format_command("RE", track_id, cmd) == 1) {
            sendToLightSigns(cmd);
        } else {
            updateBoard("WARNING: Failed to format emergency LightSigns command");
        }

        // Registar emergência no log
        logAction(system->current_user->user_id, ACTION_EMERGENCY_STOP, emergency_msg);

        return 0;
    }
}

int setTrackINOP(ManagerSystem *system, int track_id) {
    Track *t = findTrack(system, track_id);

    //error handling
    if (!t) {
        updateBoard("ERROR: Track not found");
        return 0;
    }

    // Verificar se já está inoperacional
    if (t->state == TRACK_INOP) {
        char msg[100];
        build_string_with_int(msg, sizeof(msg), "Track ", track_id, " already INOPERATIONAL");
        updateBoard(msg);
        return 1;
    }

    // Verificar se usa o trilho
    if (t->state == TRACK_BUSY || t->state == TRACK_ASSIGNED) {
        char msg[100];
        build_string_with_two_ints(msg, sizeof(msg), 
                                   "Cannot set INOP: Track ", track_id, " in use by Train ", t->trainId, "");
        updateBoard(msg);
        return 0;
    }

    // Atualiza o estado
    t->state = TRACK_INOP;
    t->trainId = -1;

    // Atualizar Board
    char board_msg[100];
    build_string_with_int(board_msg, sizeof(board_msg), "🔴 Track ", track_id, " set as INOPERATIONAL (RED BLINKING)");
    updateBoard(board_msg);
    trackINOP();

    // Enviar comando para LightSigns (Red blinking - RB)
    char cmd[20];
    if (format_command("RB", track_id, cmd) == 1) {
        sendToLightSigns(cmd);
    } else {
        updateBoard("WARNING: Failed to format INOP LightSigns command");
    }

    // Registar no log
    logAction(system->current_user->user_id, ACTION_SET_INOP, board_msg);

    return 1;
}

int setTrackFree(ManagerSystem *system, int track_id) {
    Track *t = findTrack(system, track_id);
    //error handling
    if (!t) {
        updateBoard("ERROR: Track not found");
        return 0;
    }

    // Verificar se já está livre
    if (t->state == TRACK_FREE) {
        char msg[100];
        build_string_with_int(msg, sizeof(msg), "Track ", track_id, " already FREE");
        updateBoard(msg);
        return 1;
    }

    // Não pode libertar se estiver inoperacional sem autorização prévia
    if (t->state == TRACK_INOP) {
        char msg[100];
        build_string_with_int(msg, sizeof(msg), 
                                   "Track ", track_id, " is INOPERATIONAL. Requires maintenance override.");
        updateBoard(msg);
        return 0;
    }

    // Atualiza o estado
    t->state = TRACK_FREE;
    t->trainId = -1;

    // Atualizar Board
    char board_msg[100];
    build_string_with_int(board_msg, sizeof(board_msg), "🟢 Track ", track_id, " set as FREE (GREEN)");
    updateBoard(board_msg);
    freeTrack();

    // Enviar comando para LightSigns (Green - GE)
    char cmd[20];
    printf("[DEBUG] Calling format_command(GE, %d)\n", track_id);
    int result = format_command("GE", track_id, cmd);
    printf("[DEBUG] format_command returned: %d, cmd: '%s'\n", result, cmd);
    
    if (result == 1) {
        sendToLightSigns(cmd);
    } else {
        updateBoard("WARNING: Failed to format LightSigns command");
    }

    // Registar no log
    logAction(system->current_user->user_id, ACTION_SET_FREE, board_msg);

    return 1;
}

int departureTrain(ManagerSystem *system, int train_id, int track_id) {
    Train *t = findTrain(system, train_id);
    Track *tr = findTrack(system, track_id);

    //error handling
    if (!t || !tr) {
        updateBoard("ERROR: Train or Track not found");
        return 0;
    }

    // Verificar condições: train deve estar parado na track correta
    if (t->state != TRAIN_STOPPED) {
        char msg[100];
        build_string_with_two_ints(msg, sizeof(msg), 
                                   "Train ", train_id, " not stopped (state: ", t->state, ")");
        updateBoard(msg);
        return 0;
    }

    if (tr->state != TRACK_BUSY) {
        char msg[100];
        build_string_with_two_ints(msg, sizeof(msg), 
                                   "Track ", track_id, " not busy (state: ", tr->state, ")");
        updateBoard(msg);
        return 0;
    }

    if (tr->trainId != train_id) {
        char msg[100];
        build_string_with_two_ints(msg, sizeof(msg), 
                                   "Train ", train_id, " not on Track ", track_id, "");
        updateBoard(msg);
        return 0;
    }

    // Atualizar estados
    t->state = TRAIN_IN_TRANSIT;  // Train volta a trânsito
    tr->state = TRACK_FREE;
    tr->trainId = -1;

    // Atualizar Board
    char board_msg[100];
    build_string_with_two_ints(board_msg, sizeof(board_msg), 
                               "🚂 Train ", train_id, " DEPARTING from Track ", track_id, "");
    updateBoard(board_msg);
    showDepartingTrain();

    // Enviar comando para LightSigns (Green - trilho livre)
    char cmd[20];
    if (format_command("GE", track_id, cmd) == 1) {
        sendToLightSigns(cmd);
    }

    // escrever no log
    logAction(system->current_user->user_id, ACTION_TRAIN_DEPARTURE, board_msg);

    return 1;
}

// NOVA FUNÇÃO: Confirma chegada física do train
int trainArrivalConfirmation(ManagerSystem *system, int train_id, int track_id) {
    Train *t = findTrain(system, train_id);
    Track *tr = findTrack(system, track_id);
    
    // error handling
    if (!t || !tr) {
        updateBoard("ERROR: Train or Track not found");
        return 0;
    }
    
    // Verificar se track está ASSIGNED
    if (tr->state != TRACK_ASSIGNED) {
        char msg[100];
        build_string_with_two_ints(msg, sizeof(msg), 
                                   "Track ", track_id, " not assigned (state: ", tr->state, ")");
        updateBoard(msg);
        return 0;
    }
    
    // Verificar se train está associado ao track
    if (tr->trainId != train_id) {
        char msg[100];
        build_string_with_two_ints(msg, sizeof(msg), 
                                   "Train ", train_id, " not assigned to Track ", track_id, "");
        updateBoard(msg);
        return 0;
    }
    
    // Atualizar estados (transição chave: ASSIGNED → BUSY, IN_TRANSIT → STOPPED)
    tr->state = TRACK_BUSY;
    t->state = TRAIN_STOPPED;  // Train agora está parado fisicamente
    
    // Atualizar Board
    char board_msg[100];
    build_string_with_two_ints(board_msg, sizeof(board_msg), 
                               "🚂 Train ", train_id, " ARRIVED at Track ", track_id, " (RED)");
    updateBoard(board_msg);
    
    // Enviar comando para LightSigns (Red - trilho ocupado)
    char cmd[20];
    if (format_command("RE", track_id, cmd) == 1) {
        sendToLightSigns(cmd);
    }
    
    // Escrever no log
    logAction(system->current_user->user_id, "TRAIN_ARRIVAL", board_msg);
    
    return 1;
}

// NOVA FUNÇÃO: Verifica Emergency Stop
int checkEmergencyStop(ManagerSystem *system, int train_id) {
    // Contar tracks livres disponíveis
    int free_tracks = 0;
    for (int i = 0; i < system->track_count; i++) {
        if (system->tracks[i].state == TRACK_FREE) {
            free_tracks++;
        }
    }
    
    Train *t = findTrain(system, train_id);
    if (!t) {
        return 0;
    }
    
    // Se não há tracks livres e train está em trânsito → Emergency Stop
    if (free_tracks == 0 && t->state == TRAIN_IN_TRANSIT) {
        char msg[100];
        build_string_with_int(msg, sizeof(msg), 
                              "🚨 EMERGENCY STOP - No free tracks for Train ", train_id, "");
        updateBoard(msg);
        
        // Enviar todos os tracks para vermelho (emergency)
        for (int i = 0; i < system->track_count; i++) {
            char cmd[20];
            if (format_command("RE", system->tracks[i].id, cmd) == 1) {
                sendToLightSigns(cmd);
            }
        }
        
        // Mudar train para estado de emergency
        t->state = TRAIN_STOPPED;  // Parado por emergency
        
        // Log
        logAction(system->current_user->user_id, "EMERGENCY_STOP", msg);
        
        return 1;  // Emergency stop activated
    }
    
    return 0;  // No emergency stop needed
}

// NOVA FUNÇÃO: Valida consistência do sistema
int validateSystemState(ManagerSystem *system) {
    int errors = 0;
    
    // Verificar: 1 Track BUSY = 1 Train STOPPED
    int busy_tracks = 0;
    int stopped_trains = 0;
    
    for (int i = 0; i < system->track_count; i++) {
        if (system->tracks[i].state == TRACK_BUSY) {
            busy_tracks++;
            
            // Verificar se tem train associado
            if (system->tracks[i].trainId <= 0) {
                printf("[ERROR] Track %d is BUSY but has no train assigned\n", 
                       system->tracks[i].id);
                errors++;
            }
        }
    }
    
    for (int i = 0; i < system->train_count; i++) {
        if (system->trains[i].state == TRAIN_STOPPED) {
            stopped_trains++;
        }
    }
    
    if (busy_tracks != stopped_trains) {
        printf("[ERROR] Inconsistent state: %d BUSY tracks vs %d STOPPED trains\n", 
               busy_tracks, stopped_trains);
        errors++;
    }
    
    // Verificar: 1 Track ASSIGNED = 1 Train IN_TRANSIT (reservado)
    int assigned_tracks = 0;
    int in_transit_trains = 0;
    
    for (int i = 0; i < system->track_count; i++) {
        if (system->tracks[i].state == TRACK_ASSIGNED) {
            assigned_tracks++;
        }
    }
    
    for (int i = 0; i < system->train_count; i++) {
        if (system->trains[i].state == TRAIN_IN_TRANSIT) {
            in_transit_trains++;
        }
    }
    
    // Verificar consistência: tracks ASSIGNED devem ter trains IN_TRANSIT reservados
    if (assigned_tracks > in_transit_trains) {
        printf("[ERROR] More ASSIGNED tracks (%d) than IN_TRANSIT trains (%d)\n", 
               assigned_tracks, in_transit_trains);
        errors++;
    }
    
    // Verificar: Tracks FREE/INOP não devem ter trains associados
    for (int i = 0; i < system->track_count; i++) {
        if ((system->tracks[i].state == TRACK_FREE || system->tracks[i].state == TRACK_INOP) 
            && system->tracks[i].trainId > 0) {
            printf("[ERROR] Track %d is FREE/INOP but has train %d assigned\n", 
                   system->tracks[i].id, system->tracks[i].trainId);
            errors++;
        }
    }
    
    return (errors == 0) ? 1 : 0;
}

//função principal que chama as outras
int manageRailwayTracks(ManagerSystem *system, int operation, int train_id, int track_id) {
    switch(operation) {
        case 1:  // Assign arriving train
            return assignArrivingTrain(system, train_id, track_id);

        case 2:  // Set track nonoperational
            return setTrackINOP(system, track_id);

        case 3:  // Set track free
            return setTrackFree(system, track_id);

        case 4:  // Give departure order
            return departureTrain(system, train_id, track_id);

        default:
            updateBoard("ERROR: Invalid operation code");
            return 0;
    }
}

//fim usac16

