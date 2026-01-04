#include "Manager/manager.h"
#include "UI/ui.h"
#include <stdio.h>
#include <stdlib.h>

int main(void) {
    // Initialize the system
    ManagerSystem* system = get_manager_system();
    
    if (!manager_init(system)) {
        printf("Failed to initialize manager system\n");
        return 1;
    }
    
    if (!ui_init(system)) {
        printf("Failed to initialize UI\n");
        manager_cleanup(system);
        return 1;
    }
    
    // Run the UI main loop
    ui_main_loop();
    
    // Cleanup
    ui_cleanup();
    manager_cleanup(system);
    
    return 0;
}
