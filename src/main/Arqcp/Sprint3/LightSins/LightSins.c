#include "LightSigns.h"
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <time.h>

// Para compatibilidade com Arduino/Pico
#ifdef ARDUINO
#include <Arduino.h>
#else
// Funções simuladas para ambiente não-Arduino
#define LOW 0
#define HIGH 1
#define INPUT 0
#define OUTPUT 1

unsigned long millis(void) {
    return (unsigned long)clock() * 1000 / CLOCKS_PER_SEC;
}

void pinMode(int pin, int mode) {
    printf("pinMode(%d, %d)\n", pin, mode);
}

void digitalWrite(int pin, int value) {
    printf("digitalWrite(%d, %d)\n", pin, value);
}

int digitalRead(int pin) {
    return LOW;
}
#endif

// Função auxiliar: limpar todos LEDs de um trilho
static void clear_all_leds(LightSignsSystem* system, int trackIdx) {
    if (trackIdx < 0 || trackIdx >= system->numTracksConfigured) return;

    TrackPins* pins = &system->pinConfig[trackIdx];

    // Desliga fisicamente
    digitalWrite(pins->redPin, LOW);
    digitalWrite(pins->yellowPin, LOW);
    digitalWrite(pins->greenPin, LOW);

    // Atualiza estado na struct
    system->tracks[trackIdx].redState = LED_OFF;
    system->tracks[trackIdx].yellowState = LED_OFF;
    system->tracks[trackIdx].greenState = LED_OFF;
}

// Função auxiliar: analisar comando no formato "CMD,xx"
static int parse_command(const char* cmd, int* trackNum) {
    char commandStr[10];

    // Formato esperado: "CMD,xx" onde xx são 2 dígitos
    if (sscanf(cmd, "%3[^,],%d", commandStr, trackNum) != 2) {
        return -1; // Formato inválido
    }

    // Verificar se trackNum tem 2 dígitos (01-99)
    if (*trackNum < 1 || *trackNum > 99) {
        return -1;
    }

    // Identificar comando
    if (strcmp(commandStr, "RE") == 0) return CMD_RE;
    if (strcmp(commandStr, "YE") == 0) return CMD_YE;
    if (strcmp(commandStr, "GE") == 0) return CMD_GE;
    if (strcmp(commandStr, "RB") == 0) return CMD_RB;

    return -1; // Comando desconhecido
}

// Inicialização do sistema
void lightsigns_init(LightSignsSystem* system) {
    if (system == NULL) return;

    // Inicializar struct
    system->numTracksConfigured = 2;
    system->lastBlinkTime = 0;
    system->initialized = 0;

    // Configuração dos pinos
    // Trilho 1: Pinos 2, 3, 4
    system->pinConfig[0].redPin = 2;
    system->pinConfig[0].yellowPin = 3;
    system->pinConfig[0].greenPin = 4;

    // Trilho 2: Pinos 5, 6, 7
    system->pinConfig[1].redPin = 5;
    system->pinConfig[1].yellowPin = 6;
    system->pinConfig[1].greenPin = 7;

    // Inicializar todos os trilhos
    for (int i = 0; i < system->numTracksConfigured; i++) {
        system->tracks[i].trackNumber = i + 1;
        system->tracks[i].redState = LED_OFF;
        system->tracks[i].yellowState = LED_OFF;
        system->tracks[i].greenState = LED_OFF;

        // Configurar pinos como OUTPUT
        pinMode(system->pinConfig[i].redPin, OUTPUT);
        pinMode(system->pinConfig[i].yellowPin, OUTPUT);
        pinMode(system->pinConfig[i].greenPin, OUTPUT);

        // Inicialmente todos desligados
        digitalWrite(system->pinConfig[i].redPin, LOW);
        digitalWrite(system->pinConfig[i].yellowPin, LOW);
        digitalWrite(system->pinConfig[i].greenPin, LOW);
    }

    system->initialized = 1;
    printf("LightSigns System Ready\n");
    printf("Format: CMD,NN (RE/YE/GE/RB, 01-99)\n");
}

// Processar comando recebido
void lightsigns_process_command(LightSignsSystem* system, const char* cmd) {
    if (system == NULL || !system->initialized) {
        printf("Error: System not initialized\n");
        return;
    }

    int command;
    int trackNum;

    command = parse_command(cmd, &trackNum);

    if (command == -1) {
        printf("Error: Invalid command format: %s\n", cmd);
        printf("Expected: CMD,NN (e.g., RE,01)\n");
        return;
    }

    int trackIdx = trackNum - 1; // Converter para índice 0-based

    if (trackIdx < 0 || trackIdx >= system->numTracksConfigured) {
        printf("Error: Track %d not configured\n", trackNum);
        return;
    }

    // 1. Primeiro apaga todos LEDs deste trilho
    clear_all_leds(system, trackIdx);

    // 2. Acende o LED correspondente
    TrackPins* pins = &system->pinConfig[trackIdx];

    switch(command) {
        case CMD_RE: // Vermelho fixo
            digitalWrite(pins->redPin, HIGH);
            system->tracks[trackIdx].redState = LED_ON;
            printf("Track %d: Red ON (Busy)\n", trackNum);
            break;

        case CMD_YE: // Amarelo fixo
            digitalWrite(pins->yellowPin, HIGH);
            system->tracks[trackIdx].yellowState = LED_ON;
            printf("Track %d: Yellow ON (Assigned)\n", trackNum);
            break;

        case CMD_GE: // Verde fixo
            digitalWrite(pins->greenPin, HIGH);
            system->tracks[trackIdx].greenState = LED_ON;
            printf("Track %d: Green ON (Free)\n", trackNum);
            break;

        case CMD_RB: // Vermelho piscando
            system->tracks[trackIdx].redState = LED_BLINKING;
            printf("Track %d: Red BLINKING (Inoperative)\n", trackNum);
            break;

        default:
            printf("Error: Unknown command for track %d\n", trackNum);
            break;
    }
}

// Atualizar estados (para blinking)
void lightsigns_update(LightSignsSystem* system) {
    if (system == NULL || !system->initialized) return;

    unsigned long currentTime = millis();

    // Verifica se passaram 500ms desde o último blink
    if (currentTime - system->lastBlinkTime >= 500) {
        system->lastBlinkTime = currentTime;

        // Percorre todos os trilhos configurados
        for (int i = 0; i < system->numTracksConfigured; i++) {
            if (system->tracks[i].redState == LED_BLINKING) {
                // Alterna estado do LED vermelho
                int currentState = digitalRead(system->pinConfig[i].redPin);
                digitalWrite(system->pinConfig[i].redPin, !currentState);
            }
        }
    }
}

// Função para uso no Arduino/Pico
#ifdef ARDUINO
LightSignsSystem lightsys;

void setup() {
    // Alocação estática da struct
    lightsigns_init(&lightsys);
}

void loop() {
    // Verificar comandos na serial
    if (Serial.available() > 0) {
        char buffer[20];
        int index = 0;

        // Ler até newline ou buffer cheio
        while (Serial.available() > 0 && index < sizeof(buffer)-1) {
            char c = Serial.read();
            if (c == '\n' || c == '\r') break;
            buffer[index++] = c;
        }
        buffer[index] = '\0';

        // Processar se não estiver vazio
        if (strlen(buffer) > 0) {
            lightsigns_process_command(&lightsys, buffer);
        }
    }

    // Atualizar estados (blinking)
    lightsigns_update(&lightsys);

    delay(10); // Pequena pausa
}
#endif