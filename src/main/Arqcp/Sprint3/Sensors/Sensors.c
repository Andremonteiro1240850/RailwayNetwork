#include "Sensors.h"
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

// For Arduino/Raspberry Pi Pico compatibility
// In real Arduino: #include <Arduino.h> and use Serial object
// For simulation/testing: use standard C I/O

// Sensor reading simulation (in real implementation, use DHT sensor library)
static int read_temperature(void) {
    // Simulated temperature reading (in real: dht.readTemperature())
    // Return value in Celsius
    return 25; // Placeholder - replace with actual sensor reading
}

static int read_humidity(void) {
    // Simulated humidity reading (in real: dht.readHumidity())
    // Return value as percentage (0-100)
    return 65; // Placeholder - replace with actual sensor reading
}

/**
 * Initialize Sensors component
 * Sets up Serial communication and sensor hardware
 */
void sensors_init(void) {
    // In real Arduino implementation:
    // Serial.begin(SERIAL_BAUD_RATE);
    // dht.begin();
    
    // For simulation/testing:
    printf("[Sensors] Initializing...\n");
    printf("[Sensors] Serial communication ready (baud rate: %d)\n", SERIAL_BAUD_RATE);
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
        
        // Read sensor values
        int temp = read_temperature();
        int hum = read_humidity();
        
        // Format response according to specification:
        // TEMP&unit::celsius&value::XX#HUM&unit::percentage&value::YY
        char response[MAX_RESPONSE_LEN];
        snprintf(response, sizeof(response), 
                 "TEMP&unit::celsius&value::%d#HUM&unit::percentage&value::%d", 
                 temp, hum);
        
        // Send response via Serial
        sensors_send_response(response);
        
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

