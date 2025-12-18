#ifndef SENSORS_H
#define SENSORS_H

// Sensor component for Arduino/Raspberry Pi Pico
// Communicates with Manager via Serial

// Communication protocol constants
#define SENSOR_CMD_GTH "GTH"
#define SERIAL_BAUD_RATE 9600
#define SERIAL_TIMEOUT_MS 1000

// Response format: TEMP&unit::celsius&value::XX#HUM&unit::percentage&value::YY
#define MAX_RESPONSE_LEN 100

// Sensor reading structure (raw values for filtering)
typedef struct {
    int temperature;      // Raw temperature reading in Celsius
    int humidity;         // Raw humidity reading as percentage (0-100)
    int valid;            // 1 if reading is valid, 0 if error
} SensorReading;

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

#endif // SENSORS_H

