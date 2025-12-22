#ifndef BOARD_H
#define BOARD_H

// Board component functions for displaying data
// These functions replace printf/fprintf in the Manager component

void showTrailStatus(void);
void showTemperatureAndHumidity(void);
void showEmergencyStop(void);
void freeTrack(void);
void trackINOP(void);
void showFreeTrack(void);
void showDepartingTrain(void);

// Generic function to display text data on board
// This should be used instead of printf for all user-facing output
void board_display_text(const char *text);

#endif // BOARD_H

