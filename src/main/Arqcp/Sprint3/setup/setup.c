#include "setup.h"
#include <ctype.h>

#define INITIAL_CAPACITY 10
#define MAX_LINE_LENGTH 512
#define MAX_STRING_LENGTH 256

// Portable strtok_r alternative for systems that don't have it
#ifdef _WIN32
// Windows doesn't have strtok_r, implement our own
static char* my_strtok_r(char *str, const char *delim, char **saveptr) {
    if (str != NULL) {
        *saveptr = str;
    }
    if (*saveptr == NULL) {
        return NULL;
    }
    char *start = *saveptr;
    char *end = start;
    
    // Skip leading delimiters
    while (*end != '\0' && strchr(delim, *end) != NULL) {
        end++;
    }
    if (*end == '\0') {
        *saveptr = NULL;
        return NULL;
    }
    start = end;
    
    // Find end of token
    while (*end != '\0' && strchr(delim, *end) == NULL) {
        end++;
    }
    
    if (*end != '\0') {
        *end = '\0';
        *saveptr = end + 1;
    } else {
        *saveptr = NULL;
    }
    
    return start;
}
#define strtok_r my_strtok_r
#endif

// Helper function to trim whitespace
static void trim_string(char *str) {
    char *start = str;
    char *end;
    
    // Trim leading whitespace
    while (isspace((unsigned char)*start)) {
        start++;
    }
    
    // If all whitespace
    if (*start == '\0') {
        *str = '\0';
        return;
    }
    
    // Trim trailing whitespace
    end = start + strlen(start) - 1;
    while (end > start && isspace((unsigned char)*end)) {
        end--;
    }
    
    // Write new null terminator
    end[1] = '\0';
    
    // Move trimmed string to start
    if (start != str) {
        memmove(str, start, end - start + 2);
    }
}

// Helper function to check if line is empty or comment
static int is_empty_or_comment(const char *line) {
    trim_string((char*)line);
    return (line[0] == '\0' || line[0] == '#');
}

// Caesar Cipher encryption for password (capital letters only)
static int encrypt_password(const char *plain, int key, char *encrypted) {
    if (!plain || !encrypted) {
        return 0;
    }
    
    // Validate that password contains only capital letters
    for (int i = 0; plain[i] != '\0'; i++) {
        if (plain[i] < 'A' || plain[i] > 'Z') {
            return 0;
        }
    }
    
    // Use assembly function encrypt_data
    extern int encrypt_data(char* in, int key, char *out);
    return encrypt_data((char*)plain, key, encrypted);
}

// Parse user line: USER:name:username:password:key
int parse_user_line(const char *line, User *user) {
    if (!line || !user) {
        return 0;
    }
    
    char line_copy[MAX_LINE_LENGTH];
    strncpy(line_copy, line, MAX_LINE_LENGTH - 1);
    line_copy[MAX_LINE_LENGTH - 1] = '\0';
    
    // Check if line starts with "USER:"
    if (strncmp(line_copy, "USER:", 5) != 0) {
        return 0;
    }
    
    char *token = line_copy + 5; // Skip "USER:"
    char *tokens[4];
    int token_count = 0;
    
    // Tokenize by ':'
    char *saveptr;
    token = strtok_r(token, ":", &saveptr);
    while (token != NULL && token_count < 4) {
        trim_string(token);
        tokens[token_count++] = token;
        token = strtok_r(NULL, ":", &saveptr);
    }
    
    if (token_count != 4) {
        return 0;
    }
    
    // Allocate memory for strings with exact size needed
    user->name = (char*)malloc(strlen(tokens[0]) + 1);
    user->username = (char*)malloc(strlen(tokens[1]) + 1);
    // Password size: encrypted password has same length as plain text for Caesar Cipher
    int password_len = strlen(tokens[2]);
    user->password = (char*)malloc(password_len + 1);
    
    if (!user->name || !user->username || !user->password) {
        free(user->name);
        free(user->username);
        free(user->password);
        return 0;
    }
    
    strcpy(user->name, tokens[0]);
    strcpy(user->username, tokens[1]);
    
    // Parse key
    user->caesar_key = atoi(tokens[3]);
    if (user->caesar_key < 1 || user->caesar_key > 26) {
        free(user->name);
        free(user->username);
        free(user->password);
        return 0;
    }
    
    // Encrypt password
    if (!encrypt_password(tokens[2], user->caesar_key, user->password)) {
        free(user->name);
        free(user->username);
        free(user->password);
        return 0;
    }
    
    return 1;
}

// Parse sensor line: SENSOR:temp_buffer:temp_median:humidity_buffer:humidity_median
int parse_sensor_line(const char *line, SensorConfig *config) {
    if (!line || !config) {
        return 0;
    }
    
    char line_copy[MAX_LINE_LENGTH];
    strncpy(line_copy, line, MAX_LINE_LENGTH - 1);
    line_copy[MAX_LINE_LENGTH - 1] = '\0';
    
    // Check if line starts with "SENSOR:"
    if (strncmp(line_copy, "SENSOR:", 7) != 0) {
        return 0;
    }
    
    char *token = line_copy + 7; // Skip "SENSOR:"
    int values[4];
    int value_count = 0;
    
    // Tokenize by ':'
    char *saveptr;
    token = strtok_r(token, ":", &saveptr);
    while (token != NULL && value_count < 4) {
        trim_string(token);
        values[value_count++] = atoi(token);
        token = strtok_r(NULL, ":", &saveptr);
    }
    
    if (value_count != 4) {
        return 0;
    }
    
    config->temp_buffer_length = values[0];
    config->temp_median_window = values[1];
    config->humidity_buffer_length = values[2];
    config->humidity_median_window = values[3];
    
    // Validate positive values
    if (config->temp_buffer_length <= 0 || config->temp_median_window <= 0 ||
        config->humidity_buffer_length <= 0 || config->humidity_median_window <= 0) {
        return 0;
    }
    
    return 1;
}

// Parse track line: TRACK:id:state:trainId
int parse_track_line(const char *line, Track *track) {
    if (!line || !track) {
        return 0;
    }
    
    char line_copy[MAX_LINE_LENGTH];
    strncpy(line_copy, line, MAX_LINE_LENGTH - 1);
    line_copy[MAX_LINE_LENGTH - 1] = '\0';
    
    // Check if line starts with "TRACK:"
    if (strncmp(line_copy, "TRACK:", 6) != 0) {
        return 0;
    }
    
    char *token = line_copy + 6; // Skip "TRACK:"
    int values[3];
    int value_count = 0;
    
    // Tokenize by ':'
    char *saveptr;
    token = strtok_r(token, ":", &saveptr);
    while (token != NULL && value_count < 3) {
        trim_string(token);
        values[value_count++] = atoi(token);
        token = strtok_r(NULL, ":", &saveptr);
    }
    
    if (value_count < 2) { // At least id and state required
        return 0;
    }
    
    track->id = values[0];
    track->state = values[1];
    
    // Validate state
    if (track->state < TRACK_FREE || track->state > TRACK_INOP) {
        return 0;
    }
    
    // Train ID is optional (default to 0 if not provided)
    track->trainId = (value_count >= 3) ? values[2] : 0;
    
    // Initialize lastOperationTimestamp to 0 (will be updated when operations occur)
    track->lastOperationTimestamp = 0;
    
    return 1;
}

// Parse train line: TRAIN:id
int parse_train_line(const char *line, Train *train) {
    if (!line || !train) {
        return 0;
    }
    
    char line_copy[MAX_LINE_LENGTH];
    strncpy(line_copy, line, MAX_LINE_LENGTH - 1);
    line_copy[MAX_LINE_LENGTH - 1] = '\0';
    
    // Check if line starts with "TRAIN:"
    if (strncmp(line_copy, "TRAIN:", 6) != 0) {
        return 0;
    }
    
    char *token = line_copy + 6; // Skip "TRAIN:"
    trim_string(token);
    
    train->id = atoi(token);
    
    // Initialize state and position to default values
    train->state = TRAIN_ARRIVING;  // Default state
    train->position = 0;            // Default position
    
    return 1;
}

// Parse timestamp string (format: YYYY-MM-DD HH:MM:SS or Unix timestamp)
unsigned long parse_timestamp(const char *timestamp_str) {
    if (!timestamp_str) {
        return 0;
    }
    
    // Try to parse as Unix timestamp (number)
    char *endptr;
    unsigned long timestamp = strtoul(timestamp_str, &endptr, 10);
    if (*endptr == '\0') {
        return timestamp;
    }
    
    // Try to parse as date format: YYYY-MM-DD HH:MM:SS or YYYY-MM-DD
    struct tm tm = {0};
    int year, month, day, hour = 0, min = 0, sec = 0;
    
    // Try format: YYYY-MM-DD HH:MM:SS
    if (sscanf(timestamp_str, "%d-%d-%d %d:%d:%d", &year, &month, &day, &hour, &min, &sec) == 6 ||
        sscanf(timestamp_str, "%d-%d-%d", &year, &month, &day) == 3) {
        tm.tm_year = year - 1900;
        tm.tm_mon = month - 1;
        tm.tm_mday = day;
        tm.tm_hour = hour;
        tm.tm_min = min;
        tm.tm_sec = sec;
        tm.tm_isdst = -1; // Let system determine DST
        
        time_t result = mktime(&tm);
        if (result != -1) {
            return (unsigned long)result;
        }
    }
    
    return 0;
}

// Parse log line: LOG:id:user_identification:action:timestamp
int parse_log_line(const char *line, Log *log) {
    if (!line || !log) {
        return 0;
    }
    
    char line_copy[MAX_LINE_LENGTH];
    strncpy(line_copy, line, MAX_LINE_LENGTH - 1);
    line_copy[MAX_LINE_LENGTH - 1] = '\0';
    
    // Check if line starts with "LOG:"
    if (strncmp(line_copy, "LOG:", 4) != 0) {
        return 0;
    }
    
    char *token = line_copy + 4; // Skip "LOG:"
    char *tokens[4];
    int token_count = 0;
    
    // Tokenize by ':'
    char *saveptr;
    token = strtok_r(token, ":", &saveptr);
    while (token != NULL && token_count < 4) {
        trim_string(token);
        tokens[token_count++] = token;
        token = strtok_r(NULL, ":", &saveptr);
    }
    
    if (token_count != 4) {
        return 0;
    }
    
    log->id = atoi(tokens[0]);
    
    // Allocate memory for strings
    log->user_identification = (char*)malloc(strlen(tokens[1]) + 1);
    log->action = (char*)malloc(strlen(tokens[2]) + 1);
    
    if (!log->user_identification || !log->action) {
        free(log->user_identification);
        free(log->action);
        return 0;
    }
    
    strcpy(log->user_identification, tokens[1]);
    strcpy(log->action, tokens[2]);
    
    // Parse timestamp
    log->timestamp = parse_timestamp(tokens[3]);
    if (log->timestamp == 0) {
        free(log->user_identification);
        free(log->action);
        return 0;
    }
    
    return 1;
}

// Resize array helper
// Parameters:
//   array: ponteiro para o array existente (pode ser NULL na primeira alocação)
//   old_size: tamanho atual do array (número de elementos)
//   new_size: novo tamanho desejado (número de elementos)
//   elem_size: tamanho de cada elemento em bytes
// Returns: ponteiro para o novo array ou NULL em caso de erro
static void* resize_array(void *array, size_t old_size, size_t new_size, size_t elem_size) {
    if (new_size == 0) {
        // Se new_size é 0, libera a memória
        free(array);
        return NULL;
    }
    
    // Realloc pode retornar NULL em caso de falha
    // IMPORTANTE: Se realloc falhar, o ponteiro original ainda é válido
    void *new_array = realloc(array, new_size * elem_size);
    
    if (new_array == NULL) {
        // realloc falhou - retorna NULL
        // O ponteiro original (array) ainda é válido, mas não devemos usá-lo
        // O caller deve tratar este caso e decidir o que fazer
        return NULL;
    }
    
    // Inicializa novos elementos a zero quando o array cresce
    if (new_size > old_size) {
        if (old_size == 0) {
            // Primeira alocação - inicializa tudo a zero
            memset(new_array, 0, new_size * elem_size);
        } else {
            // Array cresceu - inicializa apenas os novos elementos a zero
            size_t new_elements_offset = old_size * elem_size;
            size_t new_elements_size = (new_size - old_size) * elem_size;
            memset((char*)new_array + new_elements_offset, 0, new_elements_size);
        }
    }
    // Se new_size <= old_size, não há novos elementos para inicializar
    
    return new_array;
}

// Initialize system data structure
static void init_system_data(SystemData *system) {
    system->users = NULL;
    system->num_users = 0;
    system->max_users = 0;
    
    system->tracks = NULL;
    system->num_tracks = 0;
    system->max_tracks = 0;
    
    system->trains = NULL;
    system->num_trains = 0;
    system->max_trains = 0;
    
    system->logs = NULL;
    system->num_logs = 0;
    system->max_logs = 0;
    
    // Initialize sensor config to zero
    memset(&system->sensor_config, 0, sizeof(SensorConfig));
}

// Free all allocated memory in system data
void free_system_data(SystemData *system) {
    if (!system) {
        return;
    }
    
    // Free users
    if (system->users) {
        for (int i = 0; i < system->num_users; i++) {
            free(system->users[i].name);
            free(system->users[i].username);
            free(system->users[i].password);
        }
        free(system->users);
    }
    
    // Free tracks (no dynamic strings)
    if (system->tracks) {
        free(system->tracks);
    }
    
    // Free trains (no dynamic strings)
    if (system->trains) {
        free(system->trains);
    }
    
    // Free logs
    if (system->logs) {
        for (int i = 0; i < system->num_logs; i++) {
            free(system->logs[i].user_identification);
            free(system->logs[i].action);
        }
        free(system->logs);
    }
    
    init_system_data(system);
}

// Main function to load initial setup from text file
int load_initial_setup(const char *filename, SystemData *system) {
    if (!filename || !system) {
        return 0;
    }
    
    FILE *file = fopen(filename, "r");
    if (!file) {
        return 0;
    }
    
    // Initialize system data
    init_system_data(system);
    
    char line[MAX_LINE_LENGTH];
    int line_number = 0;
    int sensor_config_loaded = 0;
    
    while (fgets(line, sizeof(line), file)) {
        line_number++;
        
        // Remove newline
        line[strcspn(line, "\n\r")] = '\0';
        
        // Skip empty lines and comments
        if (is_empty_or_comment(line)) {
            continue;
        }
        
        // Parse based on line type
        if (strncmp(line, "USER:", 5) == 0) {
            // Resize users array if needed
            if (system->num_users >= system->max_users) {
                int new_max = (system->max_users == 0) ? INITIAL_CAPACITY : system->max_users * 2;
                User *new_users = (User*)resize_array(system->users, 
                                                      system->max_users, 
                                                      new_max, 
                                                      sizeof(User));
                if (!new_users) {
                    fclose(file);
                    free_system_data(system);
                    return 0;
                }
                system->users = new_users;
                system->max_users = new_max;
            }
            
            if (!parse_user_line(line, &system->users[system->num_users])) {
                continue; // Skip invalid user line
            }
            system->num_users++;
            
        } else if (strncmp(line, "SENSOR:", 7) == 0) {
            if (!sensor_config_loaded) {
                if (!parse_sensor_line(line, &system->sensor_config)) {
                    continue; // Skip invalid sensor line
                }
                sensor_config_loaded = 1;
            }
            // Ignore duplicate sensor configs
            
        } else if (strncmp(line, "TRACK:", 6) == 0) {
            // Resize tracks array if needed
            if (system->num_tracks >= system->max_tracks) {
                int new_max = (system->max_tracks == 0) ? INITIAL_CAPACITY : system->max_tracks * 2;
                Track *new_tracks = (Track*)resize_array(system->tracks, 
                                                        system->max_tracks, 
                                                        new_max, 
                                                        sizeof(Track));
                if (!new_tracks) {
                    fclose(file);
                    free_system_data(system);
                    return 0;
                }
                system->tracks = new_tracks;
                system->max_tracks = new_max;
            }
            
            if (!parse_track_line(line, &system->tracks[system->num_tracks])) {
                continue; // Skip invalid track line
            }
            system->num_tracks++;
            
        } else if (strncmp(line, "TRAIN:", 6) == 0) {
            // Resize trains array if needed
            if (system->num_trains >= system->max_trains) {
                int new_max = (system->max_trains == 0) ? INITIAL_CAPACITY : system->max_trains * 2;
                Train *new_trains = (Train*)resize_array(system->trains, 
                                                        system->max_trains, 
                                                        new_max, 
                                                        sizeof(Train));
                if (!new_trains) {
                    fclose(file);
                    free_system_data(system);
                    return 0;
                }
                system->trains = new_trains;
                system->max_trains = new_max;
            }
            
            if (!parse_train_line(line, &system->trains[system->num_trains])) {
                continue; // Skip invalid train line
            }
            system->num_trains++;
            
        } else if (strncmp(line, "LOG:", 4) == 0) {
            // Resize logs array if needed
            if (system->num_logs >= system->max_logs) {
                int new_max = (system->max_logs == 0) ? INITIAL_CAPACITY : system->max_logs * 2;
                Log *new_logs = (Log*)resize_array(system->logs, 
                                                  system->max_logs, 
                                                  new_max, 
                                                  sizeof(Log));
                if (!new_logs) {
                    fclose(file);
                    free_system_data(system);
                    return 0;
                }
                system->logs = new_logs;
                system->max_logs = new_max;
            }
            
            if (!parse_log_line(line, &system->logs[system->num_logs])) {
                continue; // Skip invalid log line
            }
            system->num_logs++;
        }
    }
    
    fclose(file);
    return 1;
}

// Note: transfer_setup_to_manager is implemented in setup_integration.c
// to avoid circular includes between setup.h and manager.h

