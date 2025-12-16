#ifndef LIGHTSIGNS_H
#define LIGHTSIGNS_H

#define MAX_TRACKS 99

// Comandos conforme especificação
#define CMD_RE 0   // RE - Red ON
#define CMD_YE 1   // YE - Yellow ON
#define CMD_GE 2   // GE - Green ON
#define CMD_RB 3   // RB - Red Blinking

// Estados dos LEDs
#define LED_OFF 0
#define LED_ON 1
#define LED_BLINKING 2

// Estrutura para um trilho
typedef struct {
    int trackNumber;
    int redState;
    int yellowState;
    int greenState;
} TrackLight;

// Estrutura para configuração dos pinos
typedef struct {
    int redPin;
    int yellowPin;
    int greenPin;
} TrackPins;

// Estrutura principal do sistema LightSigns
typedef struct {
    TrackLight tracks[MAX_TRACKS];
    TrackPins pinConfig[MAX_TRACKS];
    int numTracksConfigured;
    unsigned long lastBlinkTime;
    int initialized;
} LightSignsSystem;


void lightsigns_init(LightSignsSystem* system);
void lightsigns_process_command(LightSignsSystem* system, const char* cmd);
void lightsigns_update(LightSignsSystem* system);

#endif