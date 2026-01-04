.section .text
.global encrypt_data

# int encrypt_data(char* in, int key, char *out)
encrypt_data:
    # Validação: se in é NULL
    beqz a0, error_case

    # Validação: se out é NULL
    beqz a2, error_return_zero

    # Validação: se key < 1 ou key > 26
    li t0, 1
    blt a1, t0, error_case
    li t0, 26
    bgt a1, t0, error_case

    # Validação: se in é string vazia - CORREÇÃO: string vazia é válida se passar outras validações
    lb t0, 0(a0)
    beqz t0, process_empty_string  # String vazia é permitida

    # Inicialização para string não vazia
    mv t1, a0           # t1 = ponteiro in
    mv t2, a2           # t2 = ponteiro out
    li t3, 'A'          # 'A'
    li t4, 'Z'          # 'Z'
    li t5, 26           # 26 para módulo

loop:
    lb t6, 0(t1)        # Carrega caractere
    beqz t6, success    # Se zero, fim da string

    # Valida caractere A-Z
    blt t6, t3, error_case
    bgt t6, t4, error_case

    # Cifra de César: (char - 'A' + key) % 26 + 'A'
    sub t6, t6, t3      # char - 'A'
    add t6, t6, a1      # + key
    remu t6, t6, t5     # % 26
    add t6, t6, t3      # + 'A'

    sb t6, 0(t2)        # Armazena em out

    addi t1, t1, 1      # in++
    addi t2, t2, 1      # out++
    j loop

process_empty_string:
    # String vazia é válida - apenas coloca '\0' em out e retorna sucesso
    sb zero, 0(a2)
    li a0, 1
    ret

success:
    sb zero, 0(t2)      # Terminador nulo
    li a0, 1            # Retorna 1
    ret

error_case:
    # Só define out como vazio se out não for NULL
    beqz a2, error_return_zero
    sb zero, 0(a2)      # out[0] = '\0'

error_return_zero:
    li a0, 0            # Retorna 0
    ret

