#ifndef UI_H
#define UI_H

// Forward declaration to avoid circular include
typedef struct ManagerSystem ManagerSystem;

/**
 * USAC14: Console UI for controlling track sign lights
 * Robust input validation against malicious/error input
 * 
 * @param system Manager system instance
 * @return 0 on success, 1 on error
 */
int ui_usac14_control_light(ManagerSystem *system);

/**
 * Initialize UI system
 * @param system Manager system instance
 * @return 1 on success, 0 on failure
 */
int ui_init(ManagerSystem *system);

/**
 * Main UI loop
 */
void ui_main_loop(void);

/**
 * Cleanup UI system
 */
void ui_cleanup(void);

#endif // UI_H

