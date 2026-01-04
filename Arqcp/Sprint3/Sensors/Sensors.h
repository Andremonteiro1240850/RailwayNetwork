#ifndef SENSORS_H
#define SENSORS_H

// Sensor component for Arduino/Raspberry Pi Pico
// Communicates with Manager via Serial

// Communication protocol constants
#define SENSOR_CMD_GTH "GTH"
#define SERIAL_BAUD_RATE 9600
#define SERIAL_TIMEOUT_MS 1000

// Response format: TEMP&unit:celsius&value:XX#HUM&unit:percentage&value:YY
#define MAX_RESPONSE_LEN 100

// Moving Median Filter configuration
#define MEDIAN_WINDOW_SIZE 5  // Configurable window size for median filter

// Sensor reading structure (raw values for filtering)
typedef struct {
    int temperature;      // Raw temperature reading in Celsius
    int humidity;         // Raw humidity reading as percentage (0-100)
    int valid;            // 1 if reading is valid, 0 if error
} SensorReading;

// Moving Median Filter structure
typedef struct {
    int buffer[MEDIAN_WINDOW_SIZE];  // Circular buffer
    int head;                        // Head pointer
    int count;                       // Number of elements
    int sorted[MEDIAN_WINDOW_SIZE];  // Sorted buffer for median calculation
} MedianFilter;

// Function declarations
void sensors_init(void);
void sensors_loop(void);
int sensors_process_command(const char *command);
void sensors_send_response(const char *response);

// Sensor reading functions
SensorReading sensors_read_raw(void);
int sensors_read_temperature(void);
int sensors_read_humidity(void);
int sensors_format_response(const SensorReading *reading, char *response, int max_len);

// Moving Median Filter functions
void median_filter_init(MedianFilter *filter);
void median_filter_add(MedianFilter *filter, int value);
int median_filter_get_median(MedianFilter *filter);

#endif // SENSORS_H

