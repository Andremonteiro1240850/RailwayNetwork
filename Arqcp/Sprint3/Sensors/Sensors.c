#include "Sensors.h"
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>
#include <stdbool.h>
#include <math.h>

// Global sensor state
static int g_sensor_initialized = 0;
static int g_read_counter = 0;

// Moving Median Filters for temperature and humidity
static MedianFilter g_temp_filter;
static MedianFilter g_humidity_filter;

// Simple bubble sort for median calculation
static void bubble_sort(int arr[], int n) {
    for (int i = 0; i < n - 1; i++) {
        for (int j = 0; j < n - i - 1; j++) {
            if (arr[j] > arr[j + 1]) {
                int temp = arr[j];
                arr[j] = arr[j + 1];
                arr[j + 1] = temp;
            }
        }
    }
}

// Initialize Moving Median Filter
void median_filter_init(MedianFilter *filter) {
    filter->head = 0;
    filter->count = 0;
    for (int i = 0; i < MEDIAN_WINDOW_SIZE; i++) {
        filter->buffer[i] = 0;
        filter->sorted[i] = 0;
    }
}

// Add value to Moving Median Filter
void median_filter_add(MedianFilter *filter, int value) {
    filter->buffer[filter->head] = value;
    filter->head = (filter->head + 1) % MEDIAN_WINDOW_SIZE;
    
    if (filter->count < MEDIAN_WINDOW_SIZE) {
        filter->count++;
    }
    
    // Copy to sorted buffer and sort
    for (int i = 0; i < filter->count; i++) {
        filter->sorted[i] = filter->buffer[i];
    }
    bubble_sort(filter->sorted, filter->count);
}

// Get median value from Moving Median Filter
int median_filter_get_median(MedianFilter *filter) {
    if (filter->count == 0) {
        return 0;
    }
    
    // Return middle value (median)
    return filter->sorted[filter->count / 2];
}

// Arduino compatibility for dual deployment
#ifdef ARDUINO
    // Real Arduino environment
    #include <Arduino.h>
#else
    // RISC-V/Linux environment - REAL PICO MODE
    #include <stdio.h>
    #include <stdlib.h>
    #include <string.h>
    #include <time.h>
    
    // Pico simulation functions
    void Serial_begin(long baud) {
        printf("[SENSORS] Pico serial initialized at %ld baud\n", baud);
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
    
    // DHT simulation with realistic variations
    void DHT_begin(void) {
        printf("[SENSORS] DHT sensor initialized\n");
    }
    
    float DHT_readTemperature(void) {
        // Simulate realistic temperature readings
        static float base_temp = 22.0;
        static int call_count = 0;
        call_count++;
        
        // Change base temperature every few calls
        if (call_count % 3 == 0) {
            base_temp += ((float)(rand() % 100) / 50.0) - 1.0; // -1 to +1
            if (base_temp < 15.0) base_temp = 15.0;
            if (base_temp > 35.0) base_temp = 35.0;
        }
        
        // Add small noise
        float noise = ((float)(rand() % 100) / 200.0) - 0.25; // -0.25 to +0.25
        return base_temp + noise;
    }
    
    float DHT_readHumidity(void) {
        // Simulate realistic humidity readings
        static float base_humidity = 45.0;
        static int call_count = 0;
        call_count++;
        
        // Change base humidity every few calls
        if (call_count % 3 == 0) {
            base_humidity += ((float)(rand() % 100) / 25.0) - 2.0; // -2 to +2
            if (base_humidity < 20.0) base_humidity = 20.0;
            if (base_humidity > 80.0) base_humidity = 80.0;
        }
        
        // Add small noise
        float noise = ((float)(rand() % 100) / 100.0) - 0.5; // -0.5 to +0.5
        return base_humidity + noise;
    }
    
    bool DHT_isnan(float value) {
        return 0;
    }
    
    // String class simulation (simplified)
    typedef struct {
        char* data;
        int length;
    } String_impl;
    
    typedef String_impl* String;
    
    String String_constructor(const char* str) {
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
        return s;
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
    
    typedef DHT_impl* DHT;
    
    DHT DHT_constructor(int pin, int type) {
        DHT_impl* dht = (DHT_impl*)malloc(sizeof(DHT_impl));
        dht->pin = pin;
        dht->type = type;
        return dht;
    }
    
    void DHT_begin_impl(DHT dht) {
        printf("[SENSORS] DHT initialized\n");
    }
    
    float DHT_readTemperature_impl(DHT dht) {
        return DHT_readTemperature();
    }
    
    float DHT_readHumidity_impl(DHT dht) {
        return DHT_readHumidity();
    }
    
    // Macros for Arduino compatibility
    #define Serial_begin(x) Serial_begin_impl(x)
    #define Serial_available() Serial_available_impl()
    #define Serial_print(x) Serial_print_impl(x)
    #define Serial_println(x) Serial_println_impl(x)
    #define String(str) String_constructor(str)
    #define DHT(x, y) DHT_constructor(x, y)
    #define dht_begin() DHT_begin_impl(dht)
    #define dht_readTemperature() DHT_readTemperature_impl(dht)
    #define dht_readHumidity() DHT_readHumidity_impl(dht)
    #define isnan(x) DHT_isnan(x)
    
    // String macros
    #define String_equals(x, y) String_equals(x, y)
    #define String_trim(x) String_trim(x)
    
#endif

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
    // RISC-V/Linux environment - Use improved simulation
    return (int)round(DHT_readTemperature());
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
    // RISC-V/Linux environment - Use improved simulation
    return (int)round(DHT_readHumidity());
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
        // Return error format with zero values
        strcpy(response, "TEMP&unit:celsius&value:0#HUM&unit:percentage&value:0");
        return 0; // Invalid reading
    }
    
    // Format according to specification:
    // TEMP&unit:celsius&value:XX#HUM&unit:percentage&value:YY
    // Using single : as required
    
    // Manual string building (no snprintf)
    strcpy(response, "TEMP&unit:celsius&value:");
    
    // Convert temperature to string and append
    char temp_str[10];
    sprintf(temp_str, "%d", reading->temperature);
    strcat(response, temp_str);
    
    strcat(response, "#HUM&unit:percentage&value:");
    
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
    // Initialize Moving Median Filters
    median_filter_init(&g_temp_filter);
    median_filter_init(&g_humidity_filter);
    
#ifdef ARDUINO
    // Real Arduino implementation
    Serial.begin(SERIAL_BAUD_RATE);
    dht.begin();
#else
    // Linux testing environment
    printf("[SENSORS] Pico connected and ready\n");
#endif
    
    // Initialize sensor state
    g_sensor_initialized = 1;
    g_read_counter = 0;
    
    // Initialize random seed for realistic simulation
    srand((unsigned int)time(NULL));
    
#ifdef ARDUINO
    printf("[SENSORS] Hardware ready\n");
#endif
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
    
    // Create a clean copy of command (remove whitespace)
    char cmd_clean[50];
    strncpy(cmd_clean, command, sizeof(cmd_clean) - 1);
    cmd_clean[sizeof(cmd_clean) - 1] = '\0';
    
    // Remove trailing whitespace
    int len = strlen(cmd_clean);
    while (len > 0 && (cmd_clean[len-1] == ' ' || cmd_clean[len-1] == '\t' || 
                       cmd_clean[len-1] == '\n' || cmd_clean[len-1] == '\r')) {
        cmd_clean[--len] = '\0';
    }
    
    // Check if command is "GTH" (Get Temperature and Humidity)
    if (strcmp(cmd_clean, SENSOR_CMD_GTH) == 0) {
        // Read RAW sensor values
        SensorReading raw_reading = sensors_read_raw();
        
        if (!raw_reading.valid) {
            // Send error response with zero values
            char error_response[MAX_RESPONSE_LEN];
            strcpy(error_response, "TEMP&unit:celsius&value:0#HUM&unit:percentage&value:0");
            sensors_send_response(error_response);
            return 1; // Command processed (even if reading failed)
        }
        
        // Apply Moving Median Filter to raw values
        median_filter_add(&g_temp_filter, raw_reading.temperature);
        median_filter_add(&g_humidity_filter, raw_reading.humidity);
        
        // Get filtered (median) values
        SensorReading filtered_reading;
        filtered_reading.temperature = median_filter_get_median(&g_temp_filter);
        filtered_reading.humidity = median_filter_get_median(&g_humidity_filter);
        filtered_reading.valid = 1;
        
        // Format response with FILTERED values
        char response[MAX_RESPONSE_LEN];
        if (sensors_format_response(&filtered_reading, response, sizeof(response))) {
            // Send formatted response via Serial
            sensors_send_response(response);
        } else {
            return 0;
        }
        
        return 1; // Command processed successfully
    }
    
    // Unknown command - ignore safely
    return 0;
}

/**
 * Send response to Manager
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

