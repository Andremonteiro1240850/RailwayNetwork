/*
 * Sensors Component for Arduino/Raspberry Pi Pico
 * 
 * This is the Arduino sketch file for the Sensors component.
 * It receives "GTH" commands from Manager and responds with sensor data.
 * 
 * Format: TEMP&unit::celsius&value::XX#HUM&unit::percentage&value::YY
 */

// Include sensor library (DHT11/DHT22)
// For Arduino: #include <DHT.h>
// For Raspberry Pi Pico: use appropriate sensor library

// Hardware configuration
#define DHTPIN 28          // Pin connected to DHT sensor
#define DHTTYPE DHT11      // DHT11 or DHT22

// Communication settings
#define SERIAL_BAUD_RATE 9600
#define SERIAL_TIMEOUT_MS 1000

// Command constants
#define CMD_GTH "GTH"

// DHT sensor object (uncomment for Arduino)
// DHT dht(DHTPIN, DHTTYPE);

void setup() {
    // Initialize Serial communication
    Serial.begin(SERIAL_BAUD_RATE);
    Serial.setTimeout(SERIAL_TIMEOUT_MS);
    
    // Initialize sensor (uncomment for Arduino)
    // dht.begin();
    
    // Wait for serial port to connect (for USB serial)
    // while (!Serial) {
    //     delay(10);
    // }
    
    // Ready message
    Serial.println("[Sensors] Initialized");
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
 * Reads sensor values and sends formatted response
 */
void processGTHCommand() {
    // Read sensor values (uncomment for real sensor)
    // float temperature = dht.readTemperature();
    // float humidity = dht.readHumidity();
    
    // For testing/simulation (remove when using real sensor)
    float temperature = 25.0;
    float humidity = 65.0;
    
    // Check if readings are valid (uncomment for real sensor)
    // if (isnan(temperature) || isnan(humidity)) {
    //     Serial.println("[Sensors] ERROR: Failed to read sensor");
    //     return;
    // }
    
    // Format response according to specification:
    // TEMP&unit::celsius&value::XX#HUM&unit::percentage&value::YY
    Serial.print("TEMP&unit::celsius&value::");
    Serial.print((int)temperature);
    Serial.print("#HUM&unit::percentage&value::");
    Serial.println((int)humidity);
}

