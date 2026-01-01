#ifndef ASM_H
#define ASM_H

int decrypt_data(char* in, int key, char* out);
int encrypt_data(char* in, int key, char* out);
int extract_data(char* str, char* token, char* unit, int* value);
int enqueue_value(int* buffer, int length, int* nelem, int* tail, int* head, int value);
int move_n_to_array(int* buffer, int length, int* nelem, int* tail, int* head, int n, int* array);
int median(int* vec, int length, int* me);
int format_command(char* op, int n, char* cmd);
int sort_array(int* vec, int length, char order);

#endif