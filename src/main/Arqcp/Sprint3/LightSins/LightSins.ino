#include "DHT.h"

// --- Configuração de Hardware ---
#define DHTPIN 28
#define DHTTYPE DHT11

// Configuração das Linhas (Tracks)
#define NUM_TRACKS 2

// Matriz de Pinos: [IndiceLinha][IndiceCor] -> 0=Vermelho, 1=Amarelo, 2=Verde
// Linha 1 (Índice 0): GP15, GP14, GP13
// Linha 2 (Índice 1): GP16, GP17, GP18
const int TRACK_PINS[NUM_TRACKS][3] = {
  {15, 14, 13},
  {16, 17, 18}
};

// Índices de Cores
#define RED 0
#define YELLOW 1
#define GREEN 2

// --- Estado do Sistema ---
// Configuração da Mediana Móvel
#define WINDOW_SIZE 5
float tempHistory[WINDOW_SIZE] = {0};
float humHistory[WINDOW_SIZE] = {0};
int historyIndex = 0;
bool bufferFull = false;

// Gestão do Estado de Piscar (Blink)
bool isBlinking[NUM_TRACKS] = {false, false};
unsigned long lastBlinkTime = 0;
bool blinkState = false;

DHT dht(DHTPIN, DHTTYPE);

void setup() {
  Serial.begin(9600);
  dht.begin();

  // Inicializar todos os pinos das linhas como SAÍDA e desligados (LOW)
  for (int i = 0; i < NUM_TRACKS; i++) {
    for (int color = 0; color < 3; color++) {
      pinMode(TRACK_PINS[i][color], OUTPUT);
      digitalWrite(TRACK_PINS[i][color], LOW);
    }
  }
}

void loop() {
  // 1. Ler Sensores periodicamente (a cada 2 segundos)
  static unsigned long lastSensorRead = 0;
  if (millis() - lastSensorRead > 2000) {
    updateSensors();
    lastSensorRead = millis();
  }

  // 2. Processar Comandos via Série
  if (Serial.available() > 0) {
    String cmd = Serial.readStringUntil('\n');
    cmd.trim();
    processCommand(cmd);
  }

  // 3. Gerir Lógica de Piscar
  if (millis() - lastBlinkTime > 500) { // Alternar a cada 500ms
    blinkState = !blinkState;
    lastBlinkTime = millis();

    for (int i = 0; i < NUM_TRACKS; i++) {
      if (isBlinking[i]) {
        digitalWrite(TRACK_PINS[i][RED], blinkState);
      }
    }
  }
}

// --- Funções Auxiliares ---

void updateSensors() {
  float t = dht.readTemperature();
  float h = dht.readHumidity();

  // Verificar falha de leitura
  if (isnan(t) || isnan(h)) {
    // Tratamento de erro (opcional: sinalizar erro via LED específico)
  }
  else {
    // Atualizar buffer circular
    tempHistory[historyIndex] = t;
    humHistory[historyIndex] = h;
    historyIndex = (historyIndex + 1) % WINDOW_SIZE;
    if (historyIndex == 0) bufferFull = true;
  }
}

float calculateMedian(float* arr, int n) {
  float sorted[n];
  // Copiar array para evitar modificar o buffer original
  for(int i=0; i<n; i++) sorted[i] = arr[i];

  // Ordenação Simples (Bubble Sort)
  for(int i=0; i<n-1; i++) {
    for(int j=0; j<n-i-1; j++) {
      if(sorted[j] > sorted[j+1]) {
        float temp = sorted[j];
        sorted[j] = sorted[j+1];
        sorted[j+1] = temp;
      }
    }
  }
  return sorted[n/2];
}

void setTrackLight(int trackIndex, int colorCode) {
  // Validar limites do índice
  if (trackIndex < 0 || trackIndex >= NUM_TRACKS) return;

  // 1. Resetar estado de piscar para esta linha
  isBlinking[trackIndex] = false;

  // 2. Desligar todos os LEDs desta linha (Garantir exclusividade)
  digitalWrite(TRACK_PINS[trackIndex][RED], LOW);
  digitalWrite(TRACK_PINS[trackIndex][YELLOW], LOW);
  digitalWrite(TRACK_PINS[trackIndex][GREEN], LOW);

  // 3. Ligar o LED requisitado
  if (colorCode >= 0 && colorCode <= 2) {
    digitalWrite(TRACK_PINS[trackIndex][colorCode], HIGH);
  }
  // 4. Tratar estado Vermelho a Piscar (Código 3)
  else if (colorCode == 3) {
    isBlinking[trackIndex] = true;
  }
}

void processCommand(String command) {
  // --- Comando GTH (Obter Temp & Humidade) ---
  if (command.equals("GTH")) {
    int count = bufferFull ? WINDOW_SIZE : historyIndex;

    // Tratar caso de buffer vazio
    if (count == 0) {
      Serial.println("TEMP&unit:celsius&value:0#HUM&unit:percentage&value:0");
      return;
    }

    // Calcular valores da Mediana
    float mTemp = calculateMedian(tempHistory, count);
    float mHum = calculateMedian(humHistory, count);

    // Enviar resposta formatada
    Serial.print("TEMP&unit:celsius&value:"); Serial.print((int)mTemp);
    Serial.print("#HUM&unit:percentage&value:"); Serial.println((int)mHum);
    return;
  }

  // --- Comandos de Linha (RE,x | YE,x | GE,x | RB,x) ---
  // Formato esperado: "CMD, x" onde x é o número da linha

  int commaIndex = command.indexOf(',');
  if (commaIndex == -1) return; // Formato inválido

  String type = command.substring(0, commaIndex);
  String trackStr = command.substring(commaIndex + 1);
  int trackNum = trackStr.toInt();

  // Converter número da linha (base 1) para índice (base 0)
  int trackIndex = trackNum - 1;

  // Executar comando se o índice da linha for válido
  if (trackIndex >= 0 && trackIndex < NUM_TRACKS) {
    if (type.equals("RE"))      setTrackLight(trackIndex, RED);
    else if (type.equals("YE")) setTrackLight(trackIndex, YELLOW);
    else if (type.equals("GE")) setTrackLight(trackIndex, GREEN);
    else if (type.equals("RB")) setTrackLight(trackIndex, 3); // 3 = Código de Piscar
  }
}
