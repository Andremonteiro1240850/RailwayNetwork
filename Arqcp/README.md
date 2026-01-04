# Assembly Code (Arqcp)

This folder contains assembly language implementations for various algorithms and data operations.

---

## Overview

The `Arqcp` folder contains assembly language (`.s`) files organized by user story (USAC). These are low-level implementations that can be called from C/C++ code.

---

## Header File

### `asm.h`
Header file containing function declarations for all assembly functions. This file should be included in C/C++ code that needs to call these assembly functions.

**Declared Functions:**
- `decrypt_data(char* in, int key, char *out)` - Decrypts data using a key
- `move_n_to_array(int* buffer, int length, int *nelem, int *tail, int *head, int n, int* array)` - Moves n elements to array
- `median(int* vec, int length, int *me)` - Calculates median of a vector
- `format_command(char* op, int n, char *cmd)` - Formats a command string
- `sort_array(int* vec, int length, char order)` - Sorts an array

---

## User Story Scripts

### USAC01
**Folder:** `USAC01/`

- **`encrypt_data.s`** - Assembly implementation for data encryption.

### USAC02
**Folder:** `USAC02/`

- **`decrypt_data.s`** - Assembly implementation for data decryption.

### USAC03
**Folder:** `USAC03/`

- **`extract_data.s`** - Assembly implementation for data extraction.

### USAC04
**Folder:** `USAC04/`

- **`format_command.s`** - Assembly implementation for command formatting.

### USAC05
**Folder:** `USAC05/`

- **`enqueue_value.s`** - Assembly implementation for enqueue operation (queue data structure).

### USAC06
**Folder:** `USAC06/`

- **`dequeu.s`** - Assembly implementation for dequeue operation (queue data structure).

### USAC07
**Folder:** `USAC07/`

- **`move_n_to_array.s`** - Assembly implementation for moving n elements to an array.

### USAC08
**Folder:** `USAC08/`

- **`sort_array.s`** - Assembly implementation for array sorting.

### USAC09
**Folder:** `USAC09/`

- **`median.s`** - Assembly implementation for calculating the median of a vector.


---

## Folder Structure

```
Arqcp/
├── asm.h                    # Header file with function declarations
├── USAC01/
│   └── encrypt_data.s
├── USAC02/
│   └── decrypt_data.s
├── USAC03/
│   └── extract_data.s
├── USAC04/
│   └── format_command.s
├── USAC05/
│   └── enqueue_value.s
├── USAC06/
│   └── dequeu.s
├── USAC07/
│   └── move_n_to_array.s
├── USAC08/
│   └── sort_array.s
└── USAC09/
    └── median.s
```



