#include "Sensors.h"
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>
#include <time.h>

// Arduino compatibility for dual deployment
#ifdef ARDUINO
    // Real Arduino environment
    #include <Arduino.h>
#else
    // Linux testing environment
    #include <stdio.h>
    #include <stdlib.h>
    #include <string.h>
    #include <time.h>
    
    // Arduino function stubs for Linux testing
    void Serial_begin(long baud) {
        printf("[STUB] Serial.begin(%ld)\n", baud);
    }
    
    int Serial_available(void) {
        return 0; // No input in simulation mode
    }
    
    void Serial_print(const char* str) {
        printf("%s", str);
    }
    
    void Serial_println(const char* str) {
        printf("%s\n", str);
    }
    
    String Serial_readStringUntil(char delimiter) {
        return String(""); // Return empty string in simulation
    }
    
    // DHT simulation
    void DHT_begin(void) {
        printf("[STUB] DHT sensor initialized\n");
    }
    
    float DHT_readTemperature(void) {
        return 20.0 + (rand() % 10);
    }
    
    float DHT_readHumidity(void) {
        return 40.0 + (rand() % 20);
    }
    
    bool DHT_isnan(float value) {
        return 0;
    }
    
    // String class simulation (simplified)
    typedef struct {
        char* data;
        int length;
    } String_impl;
    
    String String(const char* str) {
        String_impl* s = (String_impl*)malloc(sizeof(String_impl));
        if (str) {
            s->length = strlen(str);
            s->data = (char*)malloc(s->length + 1);
            strcpy(s->data, str);
        } else {
            s->length = 0;
            s->data = (char*)malloc(1);
            s->data[0] = '\0';
        }
        return (String)s;
    }
    
    int String_equals(String this, const char* str) {
        String_impl* s = (String_impl*)this;
        return strcmp(s->data, str) == 0;
    }
    
    void String_trim(String this) {
        String_impl* s = (String_impl*)this;
        char* start = s->data;
        while (*start == ' ' || *start == '\t' || *start == '\n' || *start == '\r') start++;
        
        char* end = s->data + s->length - 1;
        while (end > start && (*end == ' ' || *end == '\t' || *end == '\n' || *end == '\r')) end--;
        
        int new_len = end - start + 1;
        if (new_len != s->length) {
            memmove(s->data, start, new_len);
            s->data[new_len] = '\0';
            s->length = new_len;
        }
    }
    
    // DHT class simulation
    typedef struct {
        int pin;
        int type;
    } DHT_impl;
    
    DHT DHT_constructor(int pin, int type) {
        DHT_impl* dht = (DHT_impl*)malloc(sizeof(DHT_impl));
        dht->pin = pin;
        dht->type = type;
        return (DHT)dht;
    }
    
    void DHT_begin_impl(DHT dht) {
        printf("[STUB] DHT initialized\n");
    }
    
    float DHT_readTemperature_impl(DHT dht) {
        return 20.0 + (rand() % 10);
    }
    
    float DHT_readHumidity_impl(DHT dht) {
        return 40.0 + (rand() % 20);
    }
    
    // Macros for Arduino compatibility
    #define Serial_begin(x) Serial_begin_impl(x)
    #define Serial_available() Serial_available_impl()
    #define Serial_readStringUntil(x) Serial_readStringUntil_impl(x)
    #define Serial_print(x) Serial_print_impl(x)
    #define Serial_println(x) Serial_println_impl(x)
    #define DHT(x, y) DHT_constructor(x, y)
    #define dht.begin() DHT_begin_impl(dht)
    #define dht.readTemperature() DHT_readTemperature_impl(dht)
    #define dht.readHumidity() DHT_readHumidity_impl(dht)
    #define isnan(x) DHT_isnan(x)
    
    // String macros
    #define String_equals(x, y) String_equals(x, y)
    #define String_trim(x) String_trim(x)
    
#endif

// Sensor state for simulation
static int g_sensor_initialized = 0;
static int g_read_counter = 0; // Simple counter instead of time_t

// DHT sensor configuration
#define DHTPIN 28
#define DHTTYPE DHT11

// DHT sensor instance (for Arduino)
#ifdef ARDUINO
    DHT dht(DHTPIN, DHTTYPE);
#endif

/**
 * Read raw temperature from sensor
 * Returns temperature in Celsius as integer
 * 
 * @return Temperature value in Celsius, or -999 on error
 */
int sensors_read_temperature(void) {
    if (!g_sensor_initialized) {
        return -999; // Sensor not initialized
    }
    
#ifdef ARDUINO
    // Real Arduino implementation
    float temp = dht.readTemperature();
    if (isnan(temp)) return -999;
    return (int)round(temp);
#else
    // Linux simulation
    static int base_temp = 22;
    g_read_counter++;
    
    // Add realistic variations
    if (g_read_counter % 5 == 0) {
        base_temp += (rand() % 3) - 1;  // -1, 0, or +1
        if (base_temp < 15) base_temp = 15;
        if (base_temp > 35) base_temp = 35;
    }
    
    return base_temp + (rand() % 3) - 1;  // Small noise
#endif
}

/**
 * Read raw humidity from sensor
 * Returns humidity as percentage (0-100)
 * 
 * @return Humidity value as percentage (0-100), or -1 on error
 */
int sensors_read_humidity(void) {
    if (!g_sensor_initialized) {
        return -1; // Sensor not initialized
    }
    
#ifdef ARDUINO
    // Real Arduino implementation
    float hum = dht.readHumidity();
    if (isnan(hum)) return -1;
    return (int)round(hum);
#else
    // Linux simulation
    static int base_humidity = 45;
    
    // Add realistic variations
    if (g_read_counter % 5 == 0) {
        base_humidity += (rand() % 5) - 2;  // -2 to +2
        if (base_humidity < 20) base_humidity = 20;
        if (base_humidity > 80) base_humidity = 80;
    }
    
    return base_humidity + (rand() % 5) - 2;  // Small noise
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
        // Return error format or default values - manual string building
        strcpy(response, "TEMP&unit::celsius&value::0#HUM&unit::percentage&value::0");
        return 0; // Invalid reading
    }
    
    // Format according to specification:
    // TEMP&unit::celsius&value::XX#HUM&unit::percentage&value::YY
    // Units must be exactly: "celsius" and "percentage" with single :: before value
    
    // Manual string building (no snprintf)
    strcpy(response, "TEMP&unit::celsius&value::");
    
    // Convert temperature to string and append
    char temp_str[10];
    sprintf(temp_str, "%d", reading->temperature); // sprintf is allowed for basic conversion
    strcat(response, temp_str);
    
    strcat(response, "#HUM&unit::percentage&value::");
    
    // Convert humidity to string and append
    char hum_str[10];
    sprintf(hum_str, "%d", reading->humidity);
    strcat(response, hum_str);
    
    return 1; // Success
}

/**
 * Initialize Sensors component
 * Sets up Serial communication and sensor hardware
 */
void sensors_init(void) {
#ifdef ARDUINO
    // Real Arduino implementation
    Serial.begin(SERIAL_BAUD_RATE);
    dht.begin();
    printf("[Sensors] Arduino: Serial and DHT initialized\n");
#else
    // Linux testing environment
    printf("[Sensors] Linux: Simulation mode initialized\n");
#endif
    
    // Initialize sensor state
    g_sensor_initialized = 1;
    g_read_counter = 0;
    
    // Initialize random seed for realistic simulation
    srand((unsigned int)time(NULL));
    
    printf("[Sensors] Sensor hardware ready\n");
    printf("[Sensors] Waiting for commands...\n");
}

/**
 * Main loop for Sensors component
 * Continuously checks for incoming commands via Serial
 */
void sensors_loop(void) {
#ifdef ARDUINO
    // Real Arduino implementation
    if (Serial.available() > 0) {
        String cmd = Serial.readStringUntil('\n');
        String_trim(cmd);
        sensors_process_command(cmd.c_str());
    }
#else
    // Linux testing environment - periodic sensor readings
    static unsigned long last_read_time = 0;
    unsigned long current_time = (unsigned long)time(NULL) * 1000;
    
    // Simulate periodic sensor readings every 2 seconds
    if (current_time - last_read_time >= 2000) {
        SensorReading reading = sensors_read_raw();
        g_read_counter++;
        last_read_time = current_time;
        
        printf("[Sensors] Auto reading #%d: Temp=%d°C, Hum=%d%% %s\n", 
               g_read_counter,
               reading.temperature, 
               reading.humidity,
               reading.valid ? "[VALID]" : "[ERROR]");
    }
#endif
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
            // Send error response with zero values - manual formatting
            char error_response[MAX_RESPONSE_LEN];
            strcpy(error_response, "TEMP&unit::celsius&value::0#HUM&unit::percentage&value::0");
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
    
#ifdef ARDUINO
    // Real Arduino implementation
    Serial.println(response);
#else
    // Linux testing environment
    printf("[Sensors->Manager] Sending: %s\n", response);
#endif
}

