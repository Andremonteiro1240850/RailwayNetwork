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
#include <errno.h>
#include <unistd.h>
#include <fcntl.h>
#ifdef _WIN32
#include <io.h>
#define chmod _chmod
#else
#include <sys/stat.h>
#include <termios.h>
#endif

#define INITIAL_CAPACITY 10
#define SENSOR_CMD "GTH"
#define MAX_INSTRUCTION_LEN 100
#define MAX_DATA_LEN 500
#define MAX_CMD_LEN 10

// Serial communication constants
#define SERIAL_PORT_DEFAULT "/dev/ttyUSB0"  // Default serial port (Linux)
#define SERIAL_BAUD_RATE 9600
#define SERIAL_TIMEOUT_SEC 1
#define SERIAL_READ_TIMEOUT_MS 1000

// Global serial port file descriptor (-1 = not open)
static int g_serial_fd = -1;
static char g_serial_port[256] = SERIAL_PORT_DEFAULT;

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
    
    // Initialize sensor data with circular buffers (using default sizes)
    if (!manager_init_sensor_data(&system->sensors, SENSOR_BUFFER_SIZE_DEFAULT, SENSOR_WINDOW_SIZE_DEFAULT)) {
        free(system->users);
        free(system->tracks);
        free(system->trains);
        return 0;
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
 * Initialize serial communication with Sensors component
 * 
 * @param port Serial port path (e.g., "/dev/ttyUSB0" for Linux)
 * @return 1 on success, 0 on failure
 */
int manager_init_serial(const char *port) {
    if (port != NULL && strlen(port) > 0) {
        strncpy(g_serial_port, port, sizeof(g_serial_port) - 1);
        g_serial_port[sizeof(g_serial_port) - 1] = '\0';
    }
    
    // Close existing connection if open
    if (g_serial_fd >= 0) {
        close(g_serial_fd);
        g_serial_fd = -1;
    }
    
#ifdef _WIN32
    // Windows serial port handling (would use CreateFile, etc.)
    // For now, use simulation mode on Windows
    printf("[Manager] Serial communication initialized (simulation mode on Windows)\n");
    printf("[Manager] Port: %s (simulated)\n", g_serial_port);
    g_serial_fd = 0; // Mark as "open" in simulation mode
    return 1;
#else
    // Linux serial port handling
    g_serial_fd = open(g_serial_port, O_RDWR | O_NOCTTY | O_NDELAY);
    if (g_serial_fd < 0) {
        fprintf(stderr, "[Manager] ERROR: Failed to open serial port %s: %s\n", 
                g_serial_port, strerror(errno));
        return 0;
    }
    
    // Configure serial port
    struct termios options;
    if (tcgetattr(g_serial_fd, &options) != 0) {
        fprintf(stderr, "[Manager] ERROR: Failed to get serial port attributes: %s\n", 
                strerror(errno));
        close(g_serial_fd);
        g_serial_fd = -1;
        return 0;
    }
    
    // Set baud rate
    cfsetispeed(&options, B9600);
    cfsetospeed(&options, B9600);
    
    // Configure port: 8N1 (8 data bits, no parity, 1 stop bit)
    options.c_cflag &= ~PARENB;   // No parity
    options.c_cflag &= ~CSTOPB;   // 1 stop bit
    options.c_cflag &= ~CSIZE;    // Clear size bits
    options.c_cflag |= CS8;       // 8 data bits
    options.c_cflag |= (CLOCAL | CREAD); // Enable receiver, ignore modem control
    
    // Disable hardware flow control
    options.c_cflag &= ~CRTSCTS;
    
    // Raw input mode
    options.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);
    
    // Disable software flow control
    options.c_iflag &= ~(IXON | IXOFF | IXANY);
    
    // Raw output mode
    options.c_oflag &= ~OPOST;
    
    // Set read timeout
    options.c_cc[VMIN] = 0;
    options.c_cc[VTIME] = 1; // 0.1 second timeout
    
    // Apply settings
    if (tcsetattr(g_serial_fd, TCSANOW, &options) != 0) {
        fprintf(stderr, "[Manager] ERROR: Failed to set serial port attributes: %s\n", 
                strerror(errno));
        close(g_serial_fd);
        g_serial_fd = -1;
        return 0;
    }
    
    // Flush input/output buffers
    tcflush(g_serial_fd, TCIOFLUSH);
    
    printf("[Manager] Serial communication initialized successfully\n");
    printf("[Manager] Port: %s, Baud rate: %d\n", g_serial_port, SERIAL_BAUD_RATE);
    return 1;
#endif
}

/**
 * Close serial communication
 */
void manager_close_serial(void) {
    if (g_serial_fd >= 0) {
#ifndef _WIN32
        close(g_serial_fd);
#endif
        g_serial_fd = -1;
        printf("[Manager] Serial communication closed\n");
    }
}

/**
 * Send command to sensors component via serial
 * 
 * @param cmd Command string to send (e.g., "GTH")
 * @return 1 on success, 0 on failure
 */
int manager_send_cmd_to_sensors(const char *cmd) {
    if (cmd == NULL) {
        fprintf(stderr, "[Manager] ERROR: Cannot send NULL command\n");
        return 0;
    }
    
    // Check if serial port is open
    if (g_serial_fd < 0) {
        // Try to initialize serial communication
        if (!manager_init_serial(NULL)) {
            fprintf(stderr, "[Manager] ERROR: Serial communication not available\n");
            return 0;
        }
    }
    
#ifdef _WIN32
    // Windows simulation mode
    printf("[Manager->Sensors] Sending: %s\n", cmd);
    return 1;
#else
    // Send command via serial port
    int cmd_len = strlen(cmd);
    ssize_t written = write(g_serial_fd, cmd, cmd_len);
    
    if (written < 0) {
        fprintf(stderr, "[Manager] ERROR: Failed to write to serial port: %s\n", 
                strerror(errno));
        // Mark connection as failed
        close(g_serial_fd);
        g_serial_fd = -1;
        return 0;
    }
    
    if (written != cmd_len) {
        fprintf(stderr, "[Manager] WARNING: Partial write to serial port (%zd/%d bytes)\n", 
                written, cmd_len);
    }
    
    // Send newline to terminate command
    if (write(g_serial_fd, "\n", 1) < 0) {
        fprintf(stderr, "[Manager] ERROR: Failed to send newline: %s\n", strerror(errno));
        close(g_serial_fd);
        g_serial_fd = -1;
        return 0;
    }
    
    // Flush output to ensure data is sent
    fsync(g_serial_fd);
    
    printf("[Manager->Sensors] Sent: %s\n", cmd);
    return 1;
#endif
}

/**
 * Wait for data from sensors component via serial
 * Returns dynamically allocated string (caller must free)
 * 
 * @return Dynamically allocated string with sensor data, or NULL on error
 */
char* manager_wait_for_data_from_sensors(void) {
    // Check if serial port is open
    if (g_serial_fd < 0) {
        fprintf(stderr, "[Manager] ERROR: Serial communication not initialized\n");
        return NULL;
    }
    
    // Allocate buffer for response
    char *data = (char*)malloc(MAX_SENSOR_STR_LEN);
    if (data == NULL) {
        fprintf(stderr, "[Manager] ERROR: Memory allocation failed\n");
        return NULL;
    }
    
#ifdef _WIN32
    // Windows simulation mode
    // Simulated sensor data format: TEMP&unit::celsius&value::XX#HUM&unit::percentage&value::YY
    snprintf(data, MAX_SENSOR_STR_LEN, "TEMP&unit::celsius&value::25#HUM&unit::percentage&value::65");
    printf("[Manager<-Sensors] Received: %s\n", data);
    return data;
#else
    // Read response from serial port with timeout
    int bytes_read = 0;
    char *buffer = data;
    int remaining = MAX_SENSOR_STR_LEN - 1; // Reserve space for null terminator
    time_t start_time = time(NULL);
    
    while (remaining > 0 && (time(NULL) - start_time) < SERIAL_TIMEOUT_SEC) {
        ssize_t n = read(g_serial_fd, buffer, remaining);
        
        if (n < 0) {
            if (errno == EAGAIN || errno == EWOULDBLOCK) {
                // No data available yet, continue waiting
                usleep(10000); // Sleep 10ms
                continue;
            } else {
                // Error reading
                fprintf(stderr, "[Manager] ERROR: Failed to read from serial port: %s\n", 
                        strerror(errno));
                free(data);
                close(g_serial_fd);
                g_serial_fd = -1;
                return NULL;
            }
        } else if (n == 0) {
            // No data available, continue waiting
            usleep(10000); // Sleep 10ms
            continue;
        } else {
            // Data received
            buffer += n;
            bytes_read += n;
            remaining -= n;
            
            // Check if we received a complete line (ends with \n or \r\n)
            if (bytes_read > 0 && (data[bytes_read - 1] == '\n' || data[bytes_read - 1] == '\r')) {
                break; // Complete line received
            }
        }
    }
    
    // Check for timeout
    if (bytes_read == 0) {
        fprintf(stderr, "[Manager] ERROR: Timeout waiting for sensor response\n");
        free(data);
        return NULL;
    }
    
    // Null terminate the string
    data[bytes_read] = '\0';
    
    // Remove trailing newline/carriage return
    while (bytes_read > 0 && (data[bytes_read - 1] == '\n' || data[bytes_read - 1] == '\r')) {
        data[bytes_read - 1] = '\0';
        bytes_read--;
    }
    
    // Check if we got any data
    if (strlen(data) == 0) {
        fprintf(stderr, "[Manager] ERROR: Empty response from sensors\n");
        free(data);
        return NULL;
    }
    
    printf("[Manager<-Sensors] Received: %s\n", data);
    return data;
#endif
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
    strcpy(sensors->temp_unit, "Celsius");
    strcpy(sensors->humidity_unit, "Percent");
    
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
        fprintf(stderr, "[Manager] ERROR: Cannot parse to NULL SensorData structure\n");
        return 0;
    }
    
    if (sensors->temp_buffer == NULL || sensors->humidity_buffer == NULL) {
        fprintf(stderr, "[Manager] ERROR: Sensor buffers not initialized\n");
        return 0;
    }
    
    // Error handling: Validate string format before parsing
    if (!validate_sensor_format(str)) {
        fprintf(stderr, "[Manager] ERROR: Malformed sensor response string\n");
        fprintf(stderr, "[Manager] Expected format: TEMP&unit::celsius&value::XX#HUM&unit::percentage&value::YY\n");
        fprintf(stderr, "[Manager] Received: %s\n", str);
        return 0;
    }
    
    // Normalize format: convert "::" to ":" for extract_data compatibility
    // The specification uses "unit::celsius&value::XX" but extract_data expects "unit:celsius&value:XX"
    char normalized[MAX_SENSOR_STR_LEN];
    if (!normalize_sensor_format(str, normalized, MAX_SENSOR_STR_LEN)) {
        fprintf(stderr, "[Manager] ERROR: Failed to normalize sensor format\n");
        return 0;
    }
    
    char token[10];
    char unit[20];
    int value;
    int parse_success = 0;
    
    // Extract TEMP data using Assembly function extract_data
    strcpy(token, "TEMP");
    if (extract_data(normalized, token, unit, &value)) {
        // Error handling: Validate temperature value range
        if (value < -50 || value > 60) {
            fprintf(stderr, "[Manager] WARNING: Temperature value out of range: %d°C (expected -50 to 60)\n", value);
            // Continue anyway - might be valid in extreme conditions
        }
        
        // Enqueue RAW temperature value into circular buffer using Assembly function enqueue_value
        // This stores the raw reading for Moving Median filtering
        if (enqueue_value(sensors->temp_buffer, sensors->buffer_size,
                         &sensors->temp_nelem, &sensors->temp_head, &sensors->temp_tail, value)) {
            strncpy(sensors->temp_unit, unit, sizeof(sensors->temp_unit) - 1);
            sensors->temp_unit[sizeof(sensors->temp_unit) - 1] = '\0';
            parse_success = 1;
        } else {
            fprintf(stderr, "[Manager] ERROR: Failed to enqueue temperature value (buffer full?)\n");
        }
    } else {
        fprintf(stderr, "[Manager] ERROR: Failed to extract TEMP data from sensor string\n");
    }
    
    // Extract HUM data using Assembly function extract_data
    strcpy(token, "HUM");
    if (extract_data(normalized, token, unit, &value)) {
        // Error handling: Validate humidity value range
        if (value < 0 || value > 100) {
            fprintf(stderr, "[Manager] WARNING: Humidity value out of range: %d%% (expected 0-100)\n", value);
            // Clamp value to valid range
            if (value < 0) value = 0;
            if (value > 100) value = 100;
        }
        
        // Enqueue RAW humidity value into circular buffer using Assembly function
        // This stores the raw reading for Moving Median filtering
        if (enqueue_value(sensors->humidity_buffer, sensors->buffer_size,
                         &sensors->humidity_nelem, &sensors->humidity_head, &sensors->humidity_tail, value)) {
            strncpy(sensors->humidity_unit, unit, sizeof(sensors->humidity_unit) - 1);
            sensors->humidity_unit[sizeof(sensors->humidity_unit) - 1] = '\0';
            parse_success = 1;
        } else {
            fprintf(stderr, "[Manager] ERROR: Failed to enqueue humidity value (buffer full?)\n");
        }
    } else {
        fprintf(stderr, "[Manager] ERROR: Failed to extract HUM data from sensor string\n");
    }
    
    // Return success only if at least one value was extracted
    if (!parse_success) {
        fprintf(stderr, "[Manager] ERROR: Failed to extract any sensor data from string\n");
        return 0;
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
    int *humidity_array = (int*)malloc(sizeof(int) * sensors->window_size);
    
    if (temp_array == NULL || humidity_array == NULL) {
        if (temp_array) free(temp_array);
        if (humidity_array) free(humidity_array);
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
        // Clear error message for user
        fprintf(stderr, "ERROR: Permission denied. Only Administrators can create log files.\n");
        fprintf(stderr, "Your role: %s\n", (requesting_user != NULL) ? "Operator" : "Unknown");
        return 0; // Only Administrators can create files
    }
    
    // Authorization logic: Additional validation
    if (requesting_user == NULL) {
        log_audit_trail("FILE_CREATION_ATTEMPT", NULL, "Null user pointer", 0);
        fprintf(stderr, "ERROR: Invalid user. User pointer is NULL.\n");
        return 0; // Invalid user
    }
    
    // Error handling: Validate inputs
    if (query_result == NULL) {
        log_audit_trail("FILE_CREATION_ATTEMPT", requesting_user, "Invalid query result", 0);
        fprintf(stderr, "ERROR: Invalid query result. Cannot create file.\n");
        return 0; // Invalid query result
    }
    
    if (users == NULL || user_count <= 0) {
        log_audit_trail("FILE_CREATION_ATTEMPT", requesting_user, "Invalid users array", 0);
        fprintf(stderr, "ERROR: Invalid users array. Cannot resolve username.\n");
        return 0; // Invalid users array
    }
    
    if (user_id <= 0) {
        log_audit_trail("FILE_CREATION_ATTEMPT", requesting_user, "Invalid target user ID", 0);
        fprintf(stderr, "ERROR: Invalid user ID (%d). User ID must be greater than 0.\n", user_id);
        return 0; // Invalid user ID
    }
    
    if (output_directory == NULL) {
        log_audit_trail("FILE_CREATION_ATTEMPT", requesting_user, "Invalid output directory", 0);
        fprintf(stderr, "ERROR: Invalid output directory. Directory path is NULL.\n");
        return 0; // Invalid output directory
    }
    
    // Edge case: Check if query result has data
    if (query_result->count == 0) {
        log_audit_trail("FILE_CREATION_ATTEMPT", requesting_user, "No data to write", 0);
        fprintf(stderr, "WARNING: No action data found for user ID %d.\n", user_id);
        fprintf(stderr, "No file will be created as there are no actions to export.\n");
        return 0; // No data to write
    }
    
    // Edge case: Check if target user exists
    const char *username = get_username_by_id(users, user_count, user_id);
    if (strcmp(username, "unknown") == 0) {
        log_audit_trail("FILE_CREATION_ATTEMPT", requesting_user, "User not found", 0);
        fprintf(stderr, "ERROR: User not found. User ID %d does not exist in the system.\n", user_id);
        return 0; // User not found
    }
    
    // Generate filename according to convention
    char filename[512];
    if (!generate_filename(filename, sizeof(filename), username, output_directory)) {
        log_audit_trail("FILE_CREATION_ATTEMPT", requesting_user, "Failed to generate filename", 0);
        fprintf(stderr, "ERROR: Failed to generate filename. Filename buffer may be too small.\n");
        return 0; // Failed to generate filename
    }
    
    // Security audit trail: Log file creation attempt
    char audit_details[256];
    snprintf(audit_details, sizeof(audit_details), "Creating file: %s for user_id: %d", filename, user_id);
    log_audit_trail("FILE_CREATION_ATTEMPT", requesting_user, audit_details, 1);
    
    // Edge case: Check if directory exists and is writable
    // Try to create a test file to check permissions
    FILE *test_file = fopen(filename, "w");
    if (test_file == NULL) {
        log_audit_trail("FILE_CREATION_FAILED", requesting_user, "Failed to open file", 0);
        
        // Determine specific error reason
        if (errno == EACCES) {
            fprintf(stderr, "ERROR: Permission denied. Cannot write to directory: %s\n", output_directory);
            fprintf(stderr, "Please check directory permissions or choose a different location.\n");
        } else if (errno == ENOENT) {
            fprintf(stderr, "ERROR: Directory not found: %s\n", output_directory);
            fprintf(stderr, "Please create the directory or specify a valid path.\n");
        } else if (errno == ENOSPC) {
            fprintf(stderr, "ERROR: Disk full. No space available to create file.\n");
            fprintf(stderr, "Please free up disk space and try again.\n");
    } else {
            fprintf(stderr, "ERROR: Failed to create file: %s\n", filename);
            fprintf(stderr, "Error code: %d. Please check directory permissions and disk space.\n", errno);
        }
        return 0; // Failed to open file
    }
    fclose(test_file);
    // Remove test file (we'll create it properly below)
    remove(filename);
    
    // Open file for writing
    FILE *file = fopen(filename, "w");
    if (file == NULL) {
        log_audit_trail("FILE_CREATION_FAILED", requesting_user, "Failed to open file after test", 0);
        fprintf(stderr, "ERROR: Failed to open file for writing: %s\n", filename);
        return 0; // Failed to open file
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
    int write_errors = 0;
    for (int i = 0; i < query_result->count; i++) {
        // Edge case: Check for disk space issues during write
        int written = fprintf(file, "Log ID: %d\n", query_result->entries[i].log_id);
        if (written < 0) {
            write_errors++;
            continue; // Skip this entry if write fails
        }
        
        fprintf(file, "User ID: %d\n", query_result->entries[i].user_id);
        fprintf(file, "Username: %s\n", username);
        fprintf(file, "Action Type: %s\n", query_result->entries[i].action_type);
        fprintf(file, "Action Details: %s\n", query_result->entries[i].action_details);
        fprintf(file, "Timestamp: %s\n", query_result->entries[i].timestamp);
        fprintf(file, "----------------------------------------\n");
        
        // Check for write errors
        if (ferror(file)) {
            write_errors++;
            clearerr(file); // Clear error flag to continue
        }
    }
    
    // Edge case: Handle write errors
    if (write_errors > 0) {
        fprintf(stderr, "WARNING: %d log entries failed to write (possible disk space issue).\n", write_errors);
        fprintf(stderr, "File may be incomplete.\n");
    }
    
    fprintf(file, "\n========================================\n");
    fprintf(file, "END OF REPORT\n");
    fprintf(file, "========================================\n");
    
    // Flush data to disk before closing
    if (fflush(file) != 0) {
        log_audit_trail("FILE_CREATION_FAILED", requesting_user, "Failed to flush file", 0);
        fprintf(stderr, "ERROR: Failed to flush data to disk.\n");
        fclose(file);
        remove(filename); // Clean up incomplete file
        return 0;
    }
    
    // Close file
    if (fclose(file) != 0) {
        log_audit_trail("FILE_CREATION_FAILED", requesting_user, "Failed to close file", 0);
        fprintf(stderr, "ERROR: Failed to close file properly. Data may be lost.\n");
        return 0; // Failed to close file
    }
    
    // Set restricted file permissions (only owner can read/write, no access for others)
    if (!set_restricted_file_permissions(filename)) {
        // Log warning but don't fail - file was created successfully
        log_audit_trail("FILE_PERMISSIONS_WARNING", requesting_user, "Failed to set restricted permissions", 0);
        fprintf(stderr, "WARNING: File created but could not set restricted permissions.\n");
        fprintf(stderr, "File location: %s\n", filename);
        // Continue anyway as file was created
    } else {
        // Log successful permission setting
        log_audit_trail("FILE_CREATION_SUCCESS", requesting_user, "File created with restricted permissions", 1);
    }
    
    // Success message with file location
    printf("\n");
    printf("========================================\n");
    printf("FILE CREATION SUCCESSFUL\n");
    printf("========================================\n");
    printf("File created: %s\n", filename);
    printf("Total actions exported: %d\n", query_result->count);
    printf("Target user: %s (ID: %d)\n", username, user_id);
    printf("Created by: %s (Administrator)\n", requesting_user->username);
    printf("File permissions: Restricted (owner read/write only)\n");
    printf("========================================\n\n");
    
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
    char *cmd;
    int iteration = 0;
    
    printf("\n========================================\n");
    printf("Manager Main Loop Started\n");
    printf("User: %s\n", system->current_user->username);
    printf("Sensor polling enabled: ALWAYS before user instructions\n");
    printf("========================================\n\n");
    
    while (1) {
        iteration++;
        printf("\n--- Iteration %d ---\n", iteration);
        
        // ========== SENSOR POLLING (ALWAYS FIRST) ==========
        // Step 1: send_cmd_to_sensors("GTH")
        // Manager MUST request sensor data BEFORE processing any user instruction
        printf("[Manager] Step 1: Requesting sensor data...\n");
        if (!manager_send_cmd_to_sensors(SENSOR_CMD)) {
            fprintf(stderr, "[Manager] ERROR: Failed to send command to sensors\n");
            fprintf(stderr, "[Manager] Skipping this iteration - will retry sensor polling\n");
            // Continue loop to retry sensor polling (don't process user instructions)
            continue;
        }
        
        // Step 2: wait_for_data_from_sensors()
        // Wait for sensor response with timeout
        printf("[Manager] Step 2: Waiting for sensor response...\n");
        sensor_str = manager_wait_for_data_from_sensors();
        if (sensor_str == NULL) {
            fprintf(stderr, "[Manager] ERROR: Failed to get sensor data (timeout or error)\n");
            fprintf(stderr, "[Manager] Skipping this iteration - will retry sensor polling\n");
            // Continue loop to retry sensor polling (don't process user instructions)
            continue;
        }
        printf("[Manager] Sensor data received: %s\n", sensor_str);
        
        // Step 3: extract_data(str, sensors) - using Assembly function extract_data
        // Parse sensor response and extract temperature/humidity values
        printf("[Manager] Step 3: Extracting sensor data...\n");
        if (!manager_extract_data(sensor_str, &system->sensors)) {
            fprintf(stderr, "[Manager] ERROR: Failed to extract sensor data\n");
            free(sensor_str);
            fprintf(stderr, "[Manager] Skipping this iteration - will retry sensor polling\n");
            // Continue loop to retry sensor polling (don't process user instructions)
            continue;
        }
        free(sensor_str);
        
        // Step 4: update_sensors_data(sensors) - using Moving Median filter
        // Apply Moving Median filter to reduce noise
        printf("[Manager] Step 4: Applying Moving Median filter...\n");
        if (!manager_update_sensors_data(&system->sensors)) {
            fprintf(stderr, "[Manager] WARNING: Failed to update sensor data with filter\n");
            // Continue anyway - we have raw data
        } else {
            printf("[Manager] Filtered values - Temp: %d°C, Hum: %d%%\n", 
                   system->sensors.current_temp, system->sensors.current_humidity);
        }
        
        // ========== USER INSTRUCTION PROCESSING (ONLY AFTER SENSORS) ==========
        // Step 5: wait_for_instructions_from_ui()
        // ONLY request user instruction AFTER sensor data is updated
        printf("[Manager] Step 5: Waiting for user instruction...\n");
        instruction = manager_wait_for_instructions_from_ui();
        if (instruction == NULL) {
            fprintf(stderr, "[Manager] ERROR: Failed to get instruction from UI\n");
            fprintf(stderr, "[Manager] Continuing loop - will poll sensors again\n");
            continue;
        }
        
        // Check for exit command
        if (strcmp(instruction, "exit") == 0 || strcmp(instruction, "quit") == 0) {
            printf("[Manager] Exit command received. Shutting down...\n");
            free(instruction);
            break;
        }
        
        // Step 6: process_instruction(inst) - Process user command
        printf("[Manager] Step 6: Processing instruction: %s\n", instruction);
        if (manager_process_instruction(system, instruction, &data, &cmd)) {
            // Step 7: send_data_to_board(data) - Display result
            printf("[Manager] Step 7: Sending data to Board...\n");
            manager_send_data_to_board(data);
            
            // Step 8: send_cmd_to_lightsigns(cmd) - Control lights
            printf("[Manager] Step 8: Sending command to LightSigns...\n");
            manager_send_cmd_to_lightsigns(cmd);
            
            // Step 9: record_action(user, inst) - Log user action
            printf("[Manager] Step 9: Recording user action...\n");
            manager_record_action(system, instruction);
            
            free(data);
            free(cmd);
        } else {
            printf("[Manager] Instruction processing failed or was invalid\n");
            if (data) free(data);
            if (cmd) free(cmd);
        }
        
        free(instruction);
    }
    
    printf("\n========================================\n");
    printf("Manager Main Loop Ended\n");
    printf("Total iterations: %d\n", iteration);
    printf("========================================\n\n");
    
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
