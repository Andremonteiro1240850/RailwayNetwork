.section .text
.global decrypt_data

decrypt_data:

    addi sp, sp, -4
    sw s0, 0(sp)
    mv s0, a2                # s0 = a2 (Guarda o endereço inicial do buffer de saída)

    li t0, 1                 # t0 = 1
    li t1, 26                # t1 = 26


    beq a0, x0, handle_error     # input == NULL
    beq a2, x0, handle_error     # output == NULL
    blt a1, t0, handle_error     # key < 1
    bgt a1, t1, handle_error     # key > 26

    li t2, 'A'           # t2 = 'A' (Limite inferior)
    li t3, 'Z'           # t3 = 'Z' (Limite superior)

loop:
    lb t4, 0(a0)         # Carrega char de entrada (t4 = *a0)
    beq t4, x0, end_loop # Fim da string

    blt t4, t2, handle_invalid_char
    bgt t4, t3, handle_invalid_char


    sub t5, t4, a1           # t5 = char - key
    blt t5, t2, wrap         # Se t5 < 'A', faz wrap-around
    j store                  # Armazena e avança

wrap:
    li t6, 26
    add t5, t5, t6           # t5 = t5 + 26
    j store

handle_invalid_char:
    mv a2, s0            # a2 = s0 (Volta a a2 para o início do buffer)
    j handle_error

store:
    sb t5, 0(a2)         # Armazena t5 na saída
    addi a0, a0, 1       # Próximo char de entrada
    addi a2, a2, 1       # Próximo char de saída
    j loop

end_loop:
    sb x0, 0(a2)         # Termina string de saída
    li a0, 1             # Retorna 1 (sucesso)
    j restore_and_ret

handle_error:

    sb x0, 0(a2)         # Limpa o primeiro byte do buffer de saída (*a2 = 0)
    li a0, 0             # Retorna 0 (falha)

restore_and_ret:
    lw s0, 0(sp)
    addi sp, sp, 4
    ret