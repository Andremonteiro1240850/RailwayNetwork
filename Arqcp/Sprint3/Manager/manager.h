#ifndef MANAGER_H
#define MANAGER_H

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#define TRACK_FREE 0
#define TRACK_BUSY 1
#define TRACK_ASSIGNED 2
#define TRACK_INOP 3

typedef struct {
    int id;
    int state;                      // usa valores das defines acima
    int trainId;
    unsigned long lastOperationTimestamp;
} Track;

#define TRAIN_IN_TRANSIT 0    // Em trânsito (sem atribuição)
#define TRAIN_STOPPED 1       // Parado na estação

typedef struct {
    int id;
    int state;        // usa valores das defines acima
    int position;
} Train;

// User structure
#define MAX_NAME_LEN 100
#define MAX_USERNAME_LEN 50
#define MAX_PASSWORD_LEN 100

#define USER_ROLE_OPERATOR 0
#define USER_ROLE_ADMINISTRATOR 1

typedef struct {
    int user_id;
    char name[MAX_NAME_LEN];
    char username[MAX_USERNAME_LEN];
    char password[MAX_PASSWORD_LEN];  // encrypted with Caesar Cipher (uppercase only)
    int key;                          // Caesar Cipher key
    int role;                         // 0 = Operator, 1 = Administrator
} User;

// Log structure according to specification
#define MAX_ACTION_LEN 200
#define MAX_TIMESTAMP_LEN 50
#define MAX_ACTION_TYPE_LEN 50

typedef struct {
    int log_id;
    int user_id;
    char timestamp[MAX_TIMESTAMP_LEN];      // Format: YYYY-MM-DD HH:MM:SS
    char action_type[MAX_ACTION_TYPE_LEN];  // UPPERCASE, underscore-separated
    char action_details[MAX_ACTION_LEN];    // Free text description
} Log;

// Log storage structure for data source
typedef struct {
    Log *logs;
    int count;
    int capacity;
} LogStorage;

// Query result structure
typedef struct {
    Log *entries;
    int count;
    int capacity;
} LogQueryResult;

// Sensor data structure (for Moving Median 2 filter)
#define SENSOR_BUFFER_SIZE_DEFAULT 10  // Default circular buffer size
#define SENSOR_WINDOW_SIZE_DEFAULT 5  // Default window size for median calculation
#define MAX_SENSOR_STR_LEN 200

typedef struct {
    int *temp_buffer;          // Circular buffer for temperature values (dynamically allocated)
    int *humidity_buffer;      // Circular buffer for humidity values (dynamically allocated)
    int buffer_size;           // Size of circular buffers (configurable, default: SENSOR_BUFFER_SIZE_DEFAULT)
    int window_size;           // Window size for Moving Median filter (configurable, default: SENSOR_WINDOW_SIZE_DEFAULT)
    int temp_nelem;            // Number of elements in temp buffer
    int humidity_nelem;        // Number of elements in humidity buffer
    int temp_head;             // Head pointer for temp buffer (circular queue)
    int temp_tail;             // Tail pointer for temp buffer (circular queue)
    int humidity_head;         // Head pointer for humidity buffer (circular queue)
    int humidity_tail;         // Tail pointer for humidity buffer (circular queue)
    int current_temp;           // Current filtered temperature (median value)
    int current_humidity;       // Current filtered humidity (median value)
    char temp_unit[10];        // Temperature unit (e.g., "Celsius")
    char humidity_unit[10];    // Humidity unit (e.g., "Percent")
} SensorData;

// Manager system structure
typedef struct {
    User *users;               // Dynamically allocated array of users
    int user_count;
    int user_capacity;
    
    Track *tracks;             // Dynamically allocated array of tracks
    int track_count;
    int track_capacity;
    
    Train *trains;             // Dynamically allocated array of trains
    int train_count;
    int train_capacity;
    
    SensorData sensors;        // Sensor data with filters
    
    User *current_user;        // Currently logged in user
    int system_status;         // System status
    int new_field;             // New field added
} ManagerSystem;

// Function declarations for Manager algorithm
int manager_init(ManagerSystem *system);
void manager_cleanup(ManagerSystem *system);
ManagerSystem* get_manager_system(void);
User* manager_login(ManagerSystem *system, const char *username, const char *password);

// Serial communication functions
int manager_init_serial(const char *port);
void manager_close_serial(void);
int manager_send_cmd_to_sensors(const char *cmd);
char* manager_wait_for_data_from_sensors(void);
int manager_extract_data(const char *str, SensorData *sensors);
int manager_update_sensors_data(SensorData *sensors);
int manager_init_sensor_data(SensorData *sensors, int buffer_size, int window_size);
void manager_cleanup_sensor_data(SensorData *sensors);
char* manager_wait_for_instructions_from_ui(void);
int manager_process_instruction(ManagerSystem *system, const char *inst, char **data_out, char **cmd_out);
int manager_send_data_to_board(const char *data);
int manager_send_cmd_to_lightsigns(const char *cmd);
int manager_record_action(ManagerSystem *system, const char *instruction);

// Sensor data access functions
int manager_get_sensor_data(const ManagerSystem *system, int *temp_out, int *humidity_out, 
                            char *temp_unit_out, int temp_unit_len, 
                            char *humidity_unit_out, int humidity_unit_len);
char* manager_format_sensor_display(const ManagerSystem *system);

// Log query functions
int query_user_actions(int user_id, const char *start_date, const char *end_date, LogQueryResult *result);
void free_log_query_result(LogQueryResult *result);
int parse_timestamp(const char *timestamp_str, struct tm *tm_out);
int compare_timestamps(const char *ts1, const char *ts2);

// Log file export function
// Returns: 1 on success, 0 on failure
// Error messages are printed to console/stderr for user feedback
int write_user_actions_to_file(const User *requesting_user, const LogQueryResult *query_result, 
                                User *users, int user_count, int user_id, const char *output_directory);
extern int is_administrator(const User *user);

//usac16
Train* findTrain(ManagerSystem *system, int train_id);
Track* findTrack(ManagerSystem *system, int track_id);
void updateBoard(const char* message);  // Atualiza o Board (UI)
void sendToLightSigns(const char* cmd); // Envia comando para LightSigns
void logAction(int user_id, const char* action_type, const char* details); // Regista no log
int assignArrivingTrain(ManagerSystem *system, int train_id, int track_id);
int setTrackINOP(ManagerSystem *system, int track_id);
int setTrackFree(ManagerSystem *system, int track_id);
int departureTrain(ManagerSystem *system, int train_id, int track_id);
int manageRailwayTracks(ManagerSystem *system, int operation, int train_id, int track_id);

// Novas funções para correção de estados
int trainArrivalConfirmation(ManagerSystem *system, int train_id, int track_id);
int checkEmergencyStop(ManagerSystem *system, int train_id);
int validateSystemState(ManagerSystem *system);

// Função Assembly do Sprint 2
int asm_format_command(const char* op, int n, char* cmd);
//fim usac16

#endif // MANAGER_H