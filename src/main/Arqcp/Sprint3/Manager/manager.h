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

#define TRAIN_ARRIVING 0
#define TRAIN_STOPPED  1
#define TRAIN_DEPARTING 2

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
#define SENSOR_BUFFER_SIZE 10
#define MAX_SENSOR_STR_LEN 200

typedef struct {
    int *temp_buffer;          // Circular buffer for temperature values
    int *humidity_buffer;      // Circular buffer for humidity values
    int temp_nelem;            // Number of elements in temp buffer
    int humidity_nelem;        // Number of elements in humidity buffer
    int temp_head;             // Head pointer for temp buffer
    int temp_tail;             // Tail pointer for temp buffer
    int humidity_head;         // Head pointer for humidity buffer
    int humidity_tail;         // Tail pointer for humidity buffer
    int current_temp;           // Current filtered temperature
    int current_humidity;       // Current filtered humidity
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
} ManagerSystem;

// Function declarations for Manager algorithm
int manager_init(ManagerSystem *system);
void manager_cleanup(ManagerSystem *system);
User* manager_login(ManagerSystem *system, const char *username, const char *password);
int manager_send_cmd_to_sensors(const char *cmd);
char* manager_wait_for_data_from_sensors(void);
int manager_extract_data(const char *str, SensorData *sensors);
int manager_update_sensors_data(SensorData *sensors);
char* manager_wait_for_instructions_from_ui(void);
int manager_process_instruction(ManagerSystem *system, const char *inst, char **data_out, char **cmd_out);
int manager_send_data_to_board(const char *data);
int manager_send_cmd_to_lightsigns(const char *cmd);
int manager_record_action(ManagerSystem *system, const char *instruction);

// Log query functions
int query_user_actions(int user_id, const char *start_date, const char *end_date, LogQueryResult *result);
void free_log_query_result(LogQueryResult *result);
int parse_timestamp(const char *timestamp_str, struct tm *tm_out);
int compare_timestamps(const char *ts1, const char *ts2);

// Log file export function
int write_user_actions_to_file(const User *requesting_user, const LogQueryResult *query_result, 
                                User *users, int user_count, int user_id, const char *output_directory);
int is_administrator(const User *user);