# Assembly Code (Arqcp)

This folder contains assembly language implementations and embedded systems code for various algorithms and data operations.

---

## Overview

The `Arqcp` folder contains:
- **Sprint 2**: Assembly language (`.s`) files for RISC-V architecture
- **Sprint 3**: Embedded systems code for station management (C/C++)

These are low-level implementations that can be called from C/C++ code for performance-critical operations.

---

## Sprint 2 - Assembly Code

### Header File

#### `Sprint2/asm.h`
Header file containing function declarations for all assembly functions. This file should be included in C/C++ code that needs to call these assembly functions.

**Declared Functions:**
- `encrypt_data(char* in, int key, char *out)` - Encrypts data using a key
- `decrypt_data(char* in, int key, char *out)` - Decrypts data using a key
- `extract_data(char* in, int n, char *out)` - Extracts n characters from input
- `format_command(char* op, int n, char *cmd)` - Formats a command string
- `enqueue_value(int* buffer, int length, int *nelem, int *tail, int *head, int value)` - Enqueues a value
- `dequeu(int* buffer, int length, int *nelem, int *tail, int *head, int *value)` - Dequeues a value
- `move_n_to_array(int* buffer, int length, int *nelem, int *tail, int *head, int n, int* array)` - Moves n elements to array
- `sort_array(int* vec, int length, char order)` - Sorts an array
- `median(int* vec, int length, int *me)` - Calculates median of a vector

---

### User Story Assembly Scripts

#### USAC01
**Folder:** `Sprint2/USAC01/`

- **`encrypt_data.s`** - Assembly implementation for data encryption.
- **`encrypt_data.o`** - Compiled object file

#### USAC02
**Folder:** `Sprint2/USAC02/`

- **`decrypt_data.s`** - Assembly implementation for data decryption.
- **`decrypt_data.o`** - Compiled object file

#### USAC03
**Folder:** `Sprint2/USAC03/`

- **`extract_data.s`** - Assembly implementation for data extraction.
- **`extract_data.o`** - Compiled object file

#### USAC04
**Folder:** `Sprint2/USAC04/`

- **`format_command.s`** - Assembly implementation for command formatting.
- **`format_command.o`** - Compiled object file

#### USAC05
**Folder:** `Sprint2/USAC05/`

- **`enqueue_value.s`** - Assembly implementation for enqueue operation (queue data structure).
- **`enqueue_value.o`** - Compiled object file

#### USAC06
**Folder:** `Sprint2/USAC06/`

- **`dequeu.s`** - Assembly implementation for dequeue operation (queue data structure).
- **`dequeu.o`** - Compiled object file

#### USAC07
**Folder:** `Sprint2/USAC07/`

- **`move_n_to_array.s`** - Assembly implementation for moving n elements to an array.
- **`move_n_to_array.o`** - Compiled object file

#### USAC08
**Folder:** `Sprint2/USAC08/`

- **`sort_array.s`** - Assembly implementation for array sorting.
- **`sort_array.o`** - Compiled object file

#### USAC09
**Folder:** `Sprint2/USAC09/`

- **`median.s`** - Assembly implementation for calculating the median of a vector.
- **`median.o`** - Compiled object file

---

## Sprint 3 - Embedded Systems

### Station Management System

**Folder:** `Sprint3/`

Embedded systems code for railway station management using Raspberry Pi Pico.

#### Main Components

- **`main.c`** - Main program entry point
- **`Makefile`** - Build configuration
- **`config.txt`** - Configuration file

#### Module Structure

- **`Board/`** - Board control and display
  - `Board.c`, `Board.h` - Board management
  - `Desenhos.md` - Display documentation

- **`LightSins/`** - Light signal control
  - `LightSins.c`, `LightSins.h` - Light signal management
  - `lights_protocol.c` - Communication protocol

- **`Manager/`** - Station manager
  - `manager.c`, `manager.h` - Manager logic

- **`Sensors/`** - Sensor integration
  - `Sensors.c`, `Sensors.h` - Sensor management

- **`setup/`** - System setup
  - `setup.c`, `setup.h` - Initialization code

- **`UI/`** - User interface
  - `ui.c`, `ui.h` - UI management

- **`Pico/`** - Raspberry Pi Pico specific code
  - `pico/` - Pico firmware (`.ino` files)

#### Build Output

- **`station_management.elf`** - Compiled executable

---

## Folder Structure

```
Arqcp/
├── Sprint2/
│   ├── asm.h                    # Header file with function declarations
│   ├── USAC01/
│   │   ├── encrypt_data.s
│   │   └── encrypt_data.o
│   ├── USAC02/
│   │   ├── decrypt_data.s
│   │   └── decrypt_data.o
│   ├── USAC03/
│   │   ├── extract_data.s
│   │   └── extract_data.o
│   ├── USAC04/
│   │   ├── format_command.s
│   │   └── format_command.o
│   ├── USAC05/
│   │   ├── enqueue_value.s
│   │   └── enqueue_value.o
│   ├── USAC06/
│   │   ├── dequeu.s
│   │   └── dequeu.o
│   ├── USAC07/
│   │   ├── move_n_to_array.s
│   │   └── move_n_to_array.o
│   ├── USAC08/
│   │   ├── sort_array.s
│   │   └── sort_array.o
│   └── USAC09/
│       ├── median.s
│       └── median.o
└── Sprint3/
    ├── main.c
    ├── main.o
    ├── Makefile
    ├── config.txt
    ├── station_management.elf
    ├── Board/
    ├── LightSins/
    ├── Manager/
    ├── Sensors/
    ├── setup/
    ├── UI/
    └── Pico/
```

---

## Usage

### Assembly Functions (Sprint 2)

Include the header and link the object files:

```c
#include "Sprint2/asm.h"

// Use assembly functions
char encrypted[100];
encrypt_data(input, key, encrypted);
```

### Embedded System (Sprint 3)

Build and run:

```bash
cd Sprint3
make
./station_management.elf
```

---

## Architecture

- **RISC-V**: Assembly code targets RISC-V architecture
- **Raspberry Pi Pico**: Embedded system uses Pico microcontroller
- **C/C++**: High-level code in C/C++

---

## Related Documentation

- [Main README](../README.md)
- [Java Source Code](../src/main/java/Readme.md)
- [Test Documentation](../src/test/Readme.md)

---

## Notes

- Assembly code is optimized for performance
- Object files (`.o`) are pre-compiled for convenience
- Embedded code requires appropriate hardware (Raspberry Pi Pico)
- Makefile handles compilation and linking
