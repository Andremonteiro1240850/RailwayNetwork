#include "Sensors.h"
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>
#include <math.h>

// For Arduino/Raspberry Pi Pico compatibility
// In real Arduino: #include <Arduino.h> and use Serial object
// For simulation/testing: use standard C I/O

// Simulation mode flag (set to 1 for simulation, 0 for real hardware)
// In C version, we use simulation mode since real hardware requires Arduino environment
#define USE_SIMULATION 1

// Sensor state for simulation
static int g_sensor_initialized = 0;
static time_t g_last_read_time = 0;

/**
 * Read raw temperature from sensor
 * Returns temperature in Celsius as integer
 * 
 * @return Temperature value in Celsius, or -999 on error
 */
int sensors_read_temperature(void) {
#if USE_SIMULATION
    // Simulated temperature reading with some variation
    // In real implementation, this would call: dht.readTemperature()
    if (!g_sensor_initialized) {
        return -999; // Sensor not initialized
    }
    
    // Simulate temperature variation (20-30°C)
    static int base_temp = 25;
    time_t now = time(NULL);
    if (now != g_last_read_time) {
        // Add small random variation
        int variation = (now % 10) - 5; // -5 to +5
        base_temp = 25 + variation;
        g_last_read_time = now;
    }
    return base_temp;
#else
    // Real sensor reading (for Arduino/Raspberry Pi Pico)
    // float temp = dht.readTemperature();
    // if (isnan(temp)) return -999;
    // return (int)round(temp);
    return -999; // Not implemented for real hardware in C version
#endif
}

/**
 * Read raw humidity from sensor
 * Returns humidity as percentage (0-100)
 * 
 * @return Humidity value as percentage (0-100), or -1 on error
 */
int sensors_read_humidity(void) {
#if USE_SIMULATION
    // Simulated humidity reading with some variation
    // In real implementation, this would call: dht.readHumidity()
    if (!g_sensor_initialized) {
        return -1; // Sensor not initialized
    }
    
    // Simulate humidity variation (60-70%)
    static int base_hum = 65;
    time_t now = time(NULL);
    if (now != g_last_read_time) {
        // Add small random variation
        int variation = (now % 10) - 5; // -5 to +5
        base_hum = 65 + variation;
        if (base_hum < 0) base_hum = 0;
        if (base_hum > 100) base_hum = 100;
        g_last_read_time = now;
    }
    return base_hum;
#else
    // Real sensor reading (for Arduino/Raspberry Pi Pico)
    // float hum = dht.readHumidity();
    // if (isnan(hum)) return -1;
    // return (int)round(hum);
    return -1; // Not implemented for real hardware in C version
#endif
}

/**
 * Read raw sensor values (temperature and humidity)
 * Returns both values in a SensorReading structure
 * These are RAW readings before any filtering
 * 
 * @return SensorReading structure with raw values
 */
SensorReading sensors_read_raw(void) {
    SensorReading reading;
    reading.temperature = sensors_read_temperature();
    reading.humidity = sensors_read_humidity();
    
    // Validate readings
    if (reading.temperature < -50 || reading.temperature > 60) {
        reading.valid = 0; // Invalid temperature range
    } else if (reading.humidity < 0 || reading.humidity > 100) {
        reading.valid = 0; // Invalid humidity range
    } else {
        reading.valid = 1; // Valid readings
    }
    
    return reading;
}

/**
 * Format sensor reading according to specification
 * Format: TEMP&unit::celsius&value::XX#HUM&unit::percentage&value::YY
 * 
 * @param reading Raw sensor reading
 * @param response Output buffer for formatted string
 * @param max_len Maximum length of response buffer
 * @return 1 on success, 0 on failure
 */
int sensors_format_response(const SensorReading *reading, char *response, int max_len) {
    if (reading == NULL || response == NULL || max_len <= 0) {
        return 0;
    }
    
    if (!reading->valid) {
        // Return error format or default values
        snprintf(response, max_len, 
                 "TEMP&unit::celsius&value::0#HUM&unit::percentage&value::0");
        return 0; // Invalid reading
    }
    
    // Format according to specification:
    // TEMP&unit::celsius&value::XX#HUM&unit::percentage&value::YY
    // Units must be: "celsius" (not "Celsius" or "C") and "percentage" (not "Percent" or "%")
    int written = snprintf(response, max_len, 
                            "TEMP&unit::celsius&value::%d#HUM&unit::percentage&value::%d",
                            reading->temperature, reading->humidity);
    
    if (written < 0 || written >= max_len) {
        return 0; // Formatting failed or buffer too small
    }
    
    return 1; // Success
}

/**
 * Initialize Sensors component
 * Sets up Serial communication and sensor hardware
 */
void sensors_init(void) {
    // In real Arduino implementation:
    // Serial.begin(SERIAL_BAUD_RATE);
    // dht.begin();
    
    // Initialize sensor state
    g_sensor_initialized = 1;
    g_last_read_time = 0;
    
    // For simulation/testing:
    printf("[Sensors] Initializing...\n");
    printf("[Sensors] Serial communication ready (baud rate: %d)\n", SERIAL_BAUD_RATE);
#if USE_SIMULATION
    printf("[Sensors] Running in SIMULATION MODE\n");
#else
    printf("[Sensors] Running with REAL HARDWARE\n");
#endif
    printf("[Sensors] Sensor hardware initialized\n");
    printf("[Sensors] Waiting for commands...\n");
}

/**
 * Main loop for Sensors component
 * Continuously checks for incoming commands via Serial
 */
void sensors_loop(void) {
    // In real Arduino implementation:
    // if (Serial.available() > 0) {
    //     String cmd = Serial.readStringUntil('\n');
    //     cmd.trim();
    //     sensors_process_command(cmd.c_str());
    // }
    
    // For simulation/testing - this would be called from main loop
    // In real implementation, this runs continuously in Arduino loop()
}

/**
 * Process incoming command from Manager
 * 
 * @param command Command string received (e.g., "GTH")
 * @return 1 if command recognized and processed, 0 otherwise
 */
int sensors_process_command(const char *command) {
    if (command == NULL) {
        return 0;
    }
    
    // Remove trailing whitespace/newline
    char cmd_clean[10];
    int i = 0;
    while (command[i] != '\0' && command[i] != '\n' && command[i] != '\r' && i < 9) {
        cmd_clean[i] = command[i];
        i++;
    }
    cmd_clean[i] = '\0';
    
    // Check if command is "GTH" (Get Temperature and Humidity)
    if (strcmp(cmd_clean, SENSOR_CMD_GTH) == 0) {
        printf("[Sensors] Received command: %s\n", SENSOR_CMD_GTH);
        
        // Read RAW sensor values (before any filtering)
        // These raw values will be filtered by the Manager using Moving Median Filter
        SensorReading raw_reading = sensors_read_raw();
        
        if (!raw_reading.valid) {
            printf("[Sensors] WARNING: Invalid sensor reading\n");
            // Send error response with zero values
            char error_response[MAX_RESPONSE_LEN];
            snprintf(error_response, sizeof(error_response),
                     "TEMP&unit::celsius&value::0#HUM&unit::percentage&value::0");
            sensors_send_response(error_response);
            return 1; // Command processed (even if reading failed)
        }
        
        // Format response according to specification:
        // TEMP&unit::celsius&value::XX#HUM&unit::percentage&value::YY
        // Units must be exactly: "celsius" and "percentage"
        char response[MAX_RESPONSE_LEN];
        if (sensors_format_response(&raw_reading, response, sizeof(response))) {
            // Send formatted response via Serial
            sensors_send_response(response);
            printf("[Sensors] Raw values - Temp: %d°C, Hum: %d%%\n", 
                   raw_reading.temperature, raw_reading.humidity);
        } else {
            printf("[Sensors] ERROR: Failed to format response\n");
            return 0;
        }
        
        return 1; // Command recognized and processed
    }
    
    // Unknown command
    printf("[Sensors] Unknown command: %s\n", cmd_clean);
    return 0;
}

/**
 * Send response to Manager via Serial
 * 
 * @param response Response string to send
 */
void sensors_send_response(const char *response) {
    if (response == NULL) {
        return;
    }
    
    // In real Arduino implementation:
    // Serial.println(response);
    
    // For simulation/testing:
    printf("[Sensors->Manager] Sending: %s\n", response);
}

