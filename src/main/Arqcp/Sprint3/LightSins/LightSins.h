#ifndef LIGHTSIGNS_H
#define LIGHTSIGNS_H

#ifdef ARDUINO
    #include <Arduino.h>
#else
    // Arduino compatibility for Linux testing
    #include <stdint.h>
    typedef uint8_t byte;
    typedef bool boolean;
    typedef uint16_t word;
    typedef unsigned long unsigned_long;
    #define HIGH 1
    #define LOW 0
    #define INPUT 0
    #define OUTPUT 1
    #define INPUT_PULLUP 2
#endif

#define MAX_TRACKS 4  // número razoável para o Pico

// Comandos
#define CMD_RE 0   // Red ON
#define CMD_YE 1   // Yellow ON
#define CMD_GE 2   // Green ON
#define CMD_RB 3   // Red Blinking

// Estados dos LEDs
#define LED_OFF 0
#define LED_ON 1
#define LED_BLINKING 2

// Estrutura de estado de um trilho
typedef struct {
    int trackNumber;
    int redState;
    int yellowState;
    int greenState;
} TrackLight;

// Estrutura de pinos de um trilho
typedef struct {
    int redPin;
    int yellowPin;
    int greenPin;
} TrackPins;

// Estrutura principal do sistema
typedef struct {
    TrackLight tracks[MAX_TRACKS];
    TrackPins pinConfig[MAX_TRACKS];
    int numTracksConfigured;
    unsigned long lastBlinkTime;
    int initialized;
} LightSignsSystem;

// Funções públicas
void lightsigns_init(LightSignsSystem* system);
void lightsigns_process_command(LightSignsSystem* system, const char* cmd);
void lightsigns_update(LightSignsSystem* system);

#endif
