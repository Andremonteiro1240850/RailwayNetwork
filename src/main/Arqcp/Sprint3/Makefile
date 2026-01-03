CC = gcc
CFLAGS = -Wall -g -std=c99 -I.
TARGET_LINUX = station_management_linux
ARDUINO_DIR = LightSins
SENSORS_DIR = Sensors

# Linux target (RV32IM compatible)
$(TARGET_LINUX): main.c UI/ui.c Manager/manager.c Board/Board.c setup/setup.c ../Sprint2/*.s
	$(CC) $(CFLAGS) -march=rv32im -o $@ $^ -lm

# Arduino target (copy files to Arduino directory)
arduino: $(ARDUINO_DIR)/LightSins.ino $(SENSORS_DIR)/Sensors_arduino.h $(SENSORS_DIR)/Sensors_arduino.cpp

$(ARDUINO_DIR)/LightSins.ino: LightSins/LightSins.c LightSins/LightSins.h
	@echo "Creating Arduino .ino file..."
	@cp LightSins/LightSins.c $(ARDUINO_DIR)/LightSins.ino

$(SENSORS_DIR)/Sensors_arduino.h: Sensors/Sensors.h
	@echo "Creating Arduino header..."
	@cp Sensors/Sensors.h $(SENSORS_DIR)/Sensors_arduino.h

$(SENSORS_DIR)/Sensors_arduino.cpp: Sensors/Sensors.c
	@echo "Creating Arduino cpp file..."
	@cp Sensors/Sensors_new.c $(SENSORS_DIR)/Sensors_arduino.cpp

# Test compilation
test-linux: $(TARGET_LINUX)
	@echo "Linux compilation successful!"

test-arduino: arduino
	@echo "Arduino files prepared successfully!"

# Clean
clean:
	rm -f $(TARGET_LINUX) *.o
	rm -f $(ARDUINO_DIR)/*.ino
	rm -f $(SENSORS_DIR)/Sensors_arduino.*

# Help
help:
	@echo "Targets:"
	@echo "  $(TARGET_LINUX) - Build for Linux RV32IM"
	@echo "  arduino        - Prepare Arduino files"
	@echo "  test-linux    - Test Linux compilation"
	@echo "  test-arduino  - Test Arduino file preparation"
	@echo "  clean         - Clean build artifacts"

.PHONY: arduino test-linux test-arduino clean help
