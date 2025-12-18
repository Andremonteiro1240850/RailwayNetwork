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

// Function declarations
void sensors_init(void);
void sensors_loop(void);
int sensors_process_command(const char *command);
void sensors_send_response(const char *response);

#endif // SENSORS_H

