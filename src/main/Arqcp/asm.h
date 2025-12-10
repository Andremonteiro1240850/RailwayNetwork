int decrypt data(char* in, int key, char *out);
int encrypt data(char* in, int key, char *out);
int extract data(char* str, char* token, char* unit, int* value);
int enqueue value(int* buffer, int length, int *nelem, int* tail, int* head, int value);
int move n to array(int* buffer, int length, int *nelem, int *tail, int *head, int n, int* array);
int median(int* vec, int length, int *me);
int format command(char* op, int n, char *cmd);
int sort array(int* vec, int length, char order);