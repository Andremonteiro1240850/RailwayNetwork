CC = riscv32-linux-gcc
CFLAGS = -Wall -Wextra -fanalyzer -g
ARCHFLAGS = -march=rv32imd -mabi=ilp32d
LIBRV32 = ${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

# =====================================================
# Source Files Discovery
# =====================================================
C_SOURCES = $(wildcard *.c) $(wildcard */*.c)
H_SOURCES = $(wildcard *.h) $(wildcard */*.h)
S_SOURCES = $(wildcard ../Sprint2/*/*.s)
INO_SOURCES = $(wildcard *.ino) $(wildcard */*.ino)

# Object files
OBJECTS = $(C_SOURCES:.c=.o) $(S_SOURCES:.s=.o)

# =====================================================
# Main Target
# =====================================================
station_management.elf: $(OBJECTS)
	$(CC) $(CFLAGS) $(ARCHFLAGS) $(OBJECTS) -o station_management.elf -lm

# =====================================================
# C Object Files
# =====================================================
main.o: main.c Manager/manager.h UI/ui.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c main.c

Manager/manager.o: Manager/manager.c Manager/manager.h ../Sprint2/asm.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c Manager/manager.c

UI/ui.o: UI/ui.c UI/ui.h Manager/manager.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c UI/ui.c

Board/Board.o: Board/Board.c Board/Board.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c Board/Board.c

setup/setup.o: setup/setup.c setup/setup.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c setup/setup.c

# =====================================================
# Assembly Object Files
# =====================================================
../Sprint2/USAC01/encrypt_data.o: ../Sprint2/USAC01/encrypt_data.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c ../Sprint2/USAC01/encrypt_data.s

../Sprint2/USAC02/decrypt_data.o: ../Sprint2/USAC02/decrypt_data.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c ../Sprint2/USAC02/decrypt_data.s

../Sprint2/USAC03/extract_data.o: ../Sprint2/USAC03/extract_data.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c ../Sprint2/USAC03/extract_data.s

../Sprint2/USAC04/format_command.o: ../Sprint2/USAC04/format_command.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c ../Sprint2/USAC04/format_command.s

../Sprint2/USAC05/enqueue_value.o: ../Sprint2/USAC05/enqueue_value.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c ../Sprint2/USAC05/enqueue_value.s

../Sprint2/USAC06/dequeu.o: ../Sprint2/USAC06/dequeu.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c ../Sprint2/USAC06/dequeu.s

../Sprint2/USAC07/move_n_to_array.o: ../Sprint2/USAC07/move_n_to_array.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c ../Sprint2/USAC07/move_n_to_array.s

../Sprint2/USAC08/sort_array.o: ../Sprint2/USAC08/sort_array.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c ../Sprint2/USAC08/sort_array.s

../Sprint2/USAC09/median.o: ../Sprint2/USAC09/median.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c ../Sprint2/USAC09/median.s

# Generic rule for any .s file
../Sprint2/%.o: ../Sprint2/%.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $<

# =====================================================
# Utility Targets
# =====================================================
clean:
	rm -f *.o *.elf */*.o ../Sprint2/*/*.o

run: station_management.elf
	qemu-riscv32 -L $(LIBRV32) ./station_management.elf

# Arduino targets
arduino: $(INO_SOURCES)
	@echo "Arduino files found: $(INO_SOURCES)"
	@echo "Use Arduino IDE to compile .ino files"

# Show project info
info:
	@echo "========================================"
	@echo "Project Information:"
	@echo "========================================"
	@echo "C Sources: $(C_SOURCES)"
	@echo "Headers: $(H_SOURCES)"
	@echo "Assembly: $(S_SOURCES)"
	@echo "Arduino: $(INO_SOURCES)"
	@echo "Objects: $(OBJECTS)"
	@echo "========================================"

# Check if all required files exist
check-files:
	@echo "Checking required files..."
	@test -f main.c || (echo "❌ main.c not found" && exit 1)
	@test -f Manager/manager.c || (echo "❌ Manager/manager.c not found" && exit 1)
	@test -f Manager/manager.h || (echo "❌ Manager/manager.h not found" && exit 1)
	@test -f UI/ui.c || (echo "❌ UI/ui.c not found" && exit 1)
	@test -f Board/Board.c || (echo "❌ Board/Board.c not found" && exit 1)
	@test -f setup/setup.c || (echo "❌ setup/setup.c not found" && exit 1)
	@test -d ../Sprint2 || (echo "❌ ../Sprint2 directory not found" && exit 1)
	@echo "✅ All required files found"

.PHONY: clean run arduino info check-files
