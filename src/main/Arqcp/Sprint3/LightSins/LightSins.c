#include "LightSins.h"
#include <stdlib.h>
#include <string.h>
#include <stdio.h>

// --- Configuração de Hardware ---

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
// Gestão do Estado de Piscar (Blink)
bool isBlinking[NUM_TRACKS] = {false, false};
unsigned long lastBlinkTime = 0;
bool blinkState = false;

DHT dht(DHTPIN, DHTTYPE);

void setup() {
  Serial.begin(9600);

  // Inicializar todos os pinos das linhas como SAIDA e desligados (LOW)
  for (int i = 0; i < NUM_TRACKS; i++) {
    for (int color = 0; color < 3; color++) {
      pinMode(TRACK_PINS[i][color], OUTPUT);
      digitalWrite(TRACK_PINS[i][color], LOW);
    }
  }
}

void loop() {
  // 1. Processar Comandos via Série
  if (Serial.available() > 0) {
    String cmd = Serial.readStringUntil('\n');
    cmd.trim();
    processCommand(cmd);
  }

  // 2. Gerir Lógica de Piscar
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

int String_indexOf(String this, char ch) {
    String_impl* s = (String_impl*)this;
    char* ptr = strchr(s->data, ch);
    return ptr ? (ptr - s->data) : -1;
}

String String_substring(String this, int start) {
    String_impl* s = (String_impl*)this;
    if (start >= s->length) return String("");
    
    char* new_data = (char*)malloc(s->length - start + 1);
    strncpy(new_data, s->data + start, s->length - start);
    new_data[s->length - start] = '\0';
    
    String result = String(new_data);
    free(new_data);
    return result;
}

int String_toInt(String this) {
    String_impl* s = (String_impl*)this;
    return atoi(s->data);
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
  if (String_equals(command, "GTH")) {
    // LightSigns nao le sensores, apenas controla LEDs
    // Responder com erro para indicar que comando nao e suportado
    Serial.println("ERROR: GTH command not supported by LightSigns");
    return;
  }

  // --- Comandos de Linha (RE,x | YE,x | GE,x | RB,x) ---
  // Formato esperado: "CMD, x" onde x e o numero da linha

  int commaIndex = String_indexOf(command, ',');
  if (commaIndex == -1) return; // Formato invalido

  String type = String_substring(command, 0, commaIndex);
  String trackStr = String_substring(command, commaIndex + 1);
  int trackNum = String_toInt(trackStr);

  // Converter numero da linha (base 1) para indice (base 0)
  int trackIndex = trackNum - 1;

  // Executar comando se o indice da linha for valido
  if (trackIndex >= 0 && trackIndex < NUM_TRACKS) {
    if (String_equals(type, "RE"))      setTrackLight(trackIndex, RED);
    else if (String_equals(type, "YE")) setTrackLight(trackIndex, YELLOW);
    else if (String_equals(type, "GE")) setTrackLight(trackIndex, GREEN);
    else if (String_equals(type, "RB")) setTrackLight(trackIndex, 3); // 3 = Codigo de Piscar
  }
}