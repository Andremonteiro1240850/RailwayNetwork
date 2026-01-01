#ifndef UI_H
#define UI_H

#include "../Manager/manager.h"

/**
 * USAC14: Console UI for controlling track sign lights
 * Robust input validation against malicious/error input
 * 
 * @param system Manager system instance
 * @return 0 on success, 1 on error
 */
int ui_usac14_control_light(ManagerSystem *system);

#endif // UI_H

