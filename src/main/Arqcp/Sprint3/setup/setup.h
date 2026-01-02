#ifndef SETUP_H
#define SETUP_H

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <stddef.h>
#include "../../Sprint2/asm.h"

// User Data Structure
typedef struct {
    char *name;
    char *username;
    char *password;  // Encrypted password (capital letters only)
    int caesar_key;  // Caesar Cipher key
} User;

// Struct offset definitions for Assembly access (RV32IM - 4-byte pointers and ints)
// These offsets must match the actual C memory layout
#define USER_NAME_OFFSET offsetof(User, name)
#define USER_USERNAME_OFFSET offsetof(User, username)
#define USER_PASSWORD_OFFSET offsetof(User, password)
#define USER_CAESAR_KEY_OFFSET offsetof(User, caesar_key)

// Sensor Data Structure
typedef struct {
    // Temperature sensor configuration
    int temp_buffer_length;      // Circular Buffer length
    int temp_median_window;       // Moving median window length
    
    // Humidity sensor configuration
    int humidity_buffer_length;  // Circular Buffer length
    int humidity_median_window;   // Moving median window length
} SensorConfig;

// Track Data Structure
// Note: manager.h has a Track with lastOperationTimestamp field
// This is the basic Track structure for initial loading
#define TRACK_FREE 0
#define TRACK_BUSY 1
#define TRACK_ASSIGNED 2
#define TRACK_INOP 3

typedef struct {
    int id;                       // Track identifier (integer)
    int state;                    // Track state (uses defines above)
    int trainId;                  // Train using the track (0 if none)
    unsigned long lastOperationTimestamp;  // Timestamp of last operation (initialized to 0)
} Track;

// Track struct offset definitions for Assembly access
#define TRACK_ID_OFFSET offsetof(Track, id)
#define TRACK_STATE_OFFSET offsetof(Track, state)
#define TRACK_TRAINID_OFFSET offsetof(Track, trainId)
#define TRACK_TIMESTAMP_OFFSET offsetof(Track, lastOperationTimestamp)

// Train Data Structure
// Note: manager.h has Train with state and position fields
#define TRAIN_ARRIVING 0
#define TRAIN_STOPPED  1
#define TRAIN_DEPARTING 2

typedef struct {
    int id;                       // Train identifier (integer)
    int state;                    // Train state (uses defines above, initialized to TRAIN_ARRIVING)
    int position;                 // Train position (initialized to 0)
} Train;

// Log Data Structure
typedef struct {
    int id;                       // Log identifier (integer)
    char *user_identification;   // User identification (username)
    char *action;                 // Action taken
    unsigned long timestamp;       // Timestamp
} Log;

// System Data Structure (contains all data)
typedef struct {
    User *users;                  // Dynamic array of users
    int num_users;
    int max_users;
    
    SensorConfig sensor_config;   // Single sensor configuration
    
    Track *tracks;                // Dynamic array of tracks
    int num_tracks;
    int max_tracks;
    
    Train *trains;                // Dynamic array of trains
    int num_trains;
    int max_trains;
    
    Log *logs;                    // Dynamic array of logs
    int num_logs;
    int max_logs;
} SystemData;

// Function declarations
int load_initial_setup(const char *filename, SystemData *system);
void free_system_data(SystemData *system);
int parse_user_line(const char *line, User *user);
int parse_sensor_line(const char *line, SensorConfig *config);
int parse_track_line(const char *line, Track *track);
int parse_train_line(const char *line, Train *train);
int parse_log_line(const char *line, Log *log);
unsigned long parse_timestamp(const char *timestamp_str);

// Integration function: Transfer setup data to Manager system
// Note: Implemented in setup_integration.c to avoid circular includes
// Forward declaration - ManagerSystem is defined in manager.h
struct ManagerSystem;
int transfer_setup_to_manager(const SystemData *setup_data, struct ManagerSystem *manager);

#endif // SETUP_H

