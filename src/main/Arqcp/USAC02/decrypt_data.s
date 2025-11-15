.section .text
.global decrypt_data

decrypt_data:
    li t0, 1
    li t1, 26

    # Verifica entradas
    beq a0, x0, return_zero
    beq a2, x0, return_zero
    blt a1, t0, return_zero
    bgt a1, t1, return_zero

    li t2, 'A'           # Limite inferior
    li t3, 'Z'           # Limite superior

loop:
    lb t4, 0(a0)         # Carrega char de entrada
    beq t4, x0, end_loop # Fim da string

    # Verifica se é letra maiúscula
    blt t4, t2, faz
    bgt t4, t3, faz

    # Descriptografia: decrypted = c - key
    sub t5, t4, a1
    blt t5, t2, wrap     # Se < 'A', faz wrap-around
    j store

wrap:
    li t6, 26
    add t5, t5, t6

store:
    sb t5, 0(a2)         # Armazena na saída
    addi a0, a0, 1       # Próximo char de entrada
    addi a2, a2, 1       # Próximo char de saída
    j loop

end_loop:
    sb x0, 0(a2)         # Termina string de saída
    li a0, 1             # Retorna 1 (sucesso)
    ret

faz:
    beq a2, x0, return_zero
    sb x0, 0(a2)         # String vazia em caso de erro

return_zero:
    li a0, 0
    ret



