#include "DHT.h"

// =====================================================
// Hardware Configuration
// =====================================================
#define DHTPIN 28
#define DHTTYPE DHT11

#define SERIAL_BAUD_RATE 9600
#define SERIAL_TIMEOUT_MS 1000

#define NUM_TRACKS 2

// Track pins: [track][color]
// color: 0=RED, 1=YELLOW, 2=GREEN
const int TRACK_PINS[NUM_TRACKS][3] = {
  {15, 14, 13},  // Track 1
  {16, 17, 18}   // Track 2
};

#define RED     0
#define YELLOW 1
#define GREEN  2

// =====================================================
// Global State
// =====================================================
DHT dht(DHTPIN, DHTTYPE);

// Blink management
bool isBlinking[NUM_TRACKS] = {false, false};
bool blinkState = false;
unsigned long lastBlinkTime = 0;

// =====================================================
// SETUP
// =====================================================
void setup() {
  Serial.begin(SERIAL_BAUD_RATE);
  Serial.setTimeout(SERIAL_TIMEOUT_MS);

  dht.begin();
  delay(2000); // Sensor stabilization

  // Initialize GPIOs
  for (int i = 0; i < NUM_TRACKS; i++) {
    for (int c = 0; c < 3; c++) {
      pinMode(TRACK_PINS[i][c], OUTPUT);
      digitalWrite(TRACK_PINS[i][c], LOW);
    }
  }

  Serial.println("[PICO] Ready");
}

// =====================================================
// LOOP
// =====================================================
void loop() {
  // Handle incoming commands
  if (Serial.available() > 0) {
    String cmd = Serial.readStringUntil('\n');
    cmd.trim();
    processCommand(cmd);
  }

  // Handle blinking (RB command)
  if (millis() - lastBlinkTime >= 500) {
    lastBlinkTime = millis();
    blinkState = !blinkState;

    for (int i = 0; i < NUM_TRACKS; i++) {
      if (isBlinking[i]) {
        digitalWrite(TRACK_PINS[i][RED], blinkState);
      }
    }
  }
}

// =====================================================
// Command Processing
// =====================================================
void processCommand(String command) {

  // ---------- GTH ----------
  if (command == "GTH") {
    sendRawSensorData();
    return;
  }

  // ---------- Light commands ----------
  // Format: RE,1 | YE,2 | GE,1 | RB,2
  int comma = command.indexOf(',');
  if (comma == -1) return;

  String type = command.substring(0, comma);
  int track = command.substring(comma + 1).toInt() - 1;

  if (track < 0 || track >= NUM_TRACKS) return;

  if      (type == "RE") setTrack(track, RED);
  else if (type == "YE") setTrack(track, YELLOW);
  else if (type == "GE") setTrack(track, GREEN);
  else if (type == "RB") setBlink(track);
}

// =====================================================
// Sensor
// =====================================================
void sendRawSensorData() {
  float t = dht.readTemperature();
  float h = dht.readHumidity();

  if (isnan(t) || isnan(h)) {
    Serial.println("TEMP&unit::celsius&value::0#HUM&unit::percentage&value::0");
    return;
  }

  Serial.print("TEMP&unit::celsius&value::");
  Serial.print((int)t);
  Serial.print("#HUM&unit::percentage&value::");
  Serial.println((int)h);
}


void setTrack(int track, int color) {
  isBlinking[track] = false;

  for (int c = 0; c < 3; c++) {
    digitalWrite(TRACK_PINS[track][c], LOW);
  }

  digitalWrite(TRACK_PINS[track][color], HIGH);
}

void setBlink(int track) {
  isBlinking[track] = true;
}
