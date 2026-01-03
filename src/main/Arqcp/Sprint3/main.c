#include "UI/ui.h"
#include "Manager/manager.h"
#include <stdio.h>
#include <stdlib.h>

// Global manager system instance
static ManagerSystem g_manager_system;

// Function to get manager system instance
ManagerSystem* get_manager_system(void) {
    return &g_manager_system;
}

int main(void) {
    // Initialize the system
    if (!manager_init(&g_manager_system)) {
        printf("Failed to initialize manager system\n");
        return 1;
    }
    
    if (!ui_init(&g_manager_system)) {
        printf("Failed to initialize UI\n");
        manager_cleanup(&g_manager_system);
        return 1;
    }
    
    // Run the UI main loop
    ui_main_loop();
    
    // Cleanup
    ui_cleanup();
    manager_cleanup(&g_manager_system);
    
    return 0;
}
