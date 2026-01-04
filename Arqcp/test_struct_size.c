#include <stdio.h>

#define MAX_NAME_LEN 100
#define MAX_USERNAME_LEN 50
#define MAX_PASSWORD_LEN 100

typedef struct {
    int user_id;
    char name[MAX_NAME_LEN];
    char username[MAX_USERNAME_LEN];
    char password[MAX_PASSWORD_LEN];
    int key;
    int role;
} User;

int main() {
    printf("sizeof(User) = %zu\n", sizeof(User));
    printf("Offset of user_id = %zu\n", offsetof(User, user_id));
    printf("Offset of name = %zu\n", offsetof(User, name));
    printf("Offset of username = %zu\n", offsetof(User, username));
    printf("Offset of password = %zu\n", offsetof(User, password));
    printf("Offset of key = %zu\n", offsetof(User, key));
    printf("Offset of role = %zu\n", offsetof(User, role));
    return 0;
}
