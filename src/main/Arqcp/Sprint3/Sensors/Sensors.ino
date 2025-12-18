/*
 * Sensors Component for Arduino/Raspberry Pi Pico
 * 
 * This is the Arduino sketch file for the Sensors component.
 * It receives "GTH" commands from Manager and responds with RAW sensor data.
 * 
 * Format: TEMP&unit::celsius&value::XX#HUM&unit::percentage&value::YY
 * 
 * Note: These are RAW readings before filtering.
 * The Manager component applies Moving Median Filter to these values.
 */

// Include sensor library (DHT11/DHT22)
// For Arduino: Install DHT sensor library by Adafruit
// For Raspberry Pi Pico: Use appropriate DHT library
#include <DHT.h>

// Hardware configuration
#define DHTPIN 28          // Pin connected to DHT sensor (GP28 for Raspberry Pi Pico)
#define DHTTYPE DHT11      // DHT11 or DHT22

// Communication settings
#define SERIAL_BAUD_RATE 9600
#define SERIAL_TIMEOUT_MS 1000

// Command constants
#define CMD_GTH "GTH"

// DHT sensor object
DHT dht(DHTPIN, DHTTYPE);

void setup() {
    // Initialize Serial communication
    Serial.begin(SERIAL_BAUD_RATE);
    Serial.setTimeout(SERIAL_TIMEOUT_MS);
    
    // Initialize DHT sensor
    dht.begin();
    
    // Wait for serial port to connect (for USB serial - optional)
    // Uncomment for Arduino with USB serial
    // while (!Serial) {
    //     delay(10);
    // }
    
    // Small delay to allow sensor to stabilize
    delay(2000);
    
    // Ready message
    Serial.println("[Sensors] Initialized");
    Serial.println("[Sensors] DHT sensor ready");
    Serial.println("[Sensors] Waiting for commands...");
}

void loop() {
    // Check for incoming commands
    if (Serial.available() > 0) {
        String command = Serial.readStringUntil('\n');
        command.trim(); // Remove whitespace
        
        // Process command
        if (command.equals(CMD_GTH)) {
            processGTHCommand();
        } else {
            // Unknown command
            Serial.print("[Sensors] Unknown command: ");
            Serial.println(command);
        }
    }
}

/**
 * Process GTH (Get Temperature and Humidity) command
 * Reads RAW sensor values and sends formatted response
 * 
 * Note: These are RAW readings before filtering.
 * The Manager component will apply Moving Median Filter to these values.
 */
void processGTHCommand() {
    // Read RAW sensor values from physical sensor
    // These are raw readings that will be filtered by Manager
    float temperature = dht.readTemperature();
    float humidity = dht.readHumidity();
    
    // Check if readings are valid
    if (isnan(temperature) || isnan(humidity)) {
        // Send error response with zero values
        Serial.println("TEMP&unit::celsius&value::0#HUM&unit::percentage&value::0");
        Serial.println("[Sensors] ERROR: Failed to read sensor");
        return;
    }
    
    // Validate reading ranges
    if (temperature < -50.0 || temperature > 60.0) {
        Serial.println("TEMP&unit::celsius&value::0#HUM&unit::percentage&value::0");
        Serial.println("[Sensors] ERROR: Temperature out of valid range");
        return;
    }
    
    if (humidity < 0.0 || humidity > 100.0) {
        Serial.println("TEMP&unit::celsius&value::0#HUM&unit::percentage&value::0");
        Serial.println("[Sensors] ERROR: Humidity out of valid range");
        return;
    }
    
    // Format response according to specification:
    // TEMP&unit::celsius&value::XX#HUM&unit::percentage&value::YY
    // Units must be exactly: "celsius" (not "Celsius" or "C") 
    // and "percentage" (not "Percent" or "%")
    // Values are sent as integers (raw readings)
    Serial.print("TEMP&unit::celsius&value::");
    Serial.print((int)round(temperature));
    Serial.print("#HUM&unit::percentage&value::");
    Serial.println((int)round(humidity));
    
    // Debug output (can be removed in production)
    Serial.print("[Sensors] Raw reading - Temp: ");
    Serial.print((int)round(temperature));
    Serial.print("°C, Hum: ");
    Serial.print((int)round(humidity));
    Serial.println("%");
}

