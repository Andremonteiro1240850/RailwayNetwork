.section .text
.global encrypt_data

# int encrypt_data(char* in, int key, char* out)
# a0 = in (char*)
# a1 = key (int)
# a2 = out (char*)
encrypt_data:

    # Validação: se in é NULL, retorna 0 e out = ""
    beqz a0, set_empty_and_return_zero
    
    # Validação: se out é NULL, retorna 0
    beqz a2, return_zero
    
    # Validação: se key < 1 ou key > 26, retorna 0 e out = ""
    li t0, 1
    blt a1, t0, set_empty_and_return_zero
    li t0, 26
    bgt a1, t0, set_empty_and_return_zero
    
    # Validação: se in é string vazia, retorna 0 e out = ""
    lb t0, 0(a0)                    # t0 = in[0]
    beqz t0, set_empty_and_return_zero
    
    # Salvar registos que vamos usar
    mv t1, a0                       # t1 = ponteiro para in (preserva a0)
    mv t2, a2                       # t2 = ponteiro para out (preserva a2)
    li t3, 'A'                      # t3 = 'A' (65)
    li t4, 'Z'                      # t4 = 'Z' (90)
    li t5, 26                       # t5 = 26 (para módulo)
    
loop:
    lb t6, 0(t1)                    # t6 = *in (caractere atual)
    beqz t6, end_loop                # Se '\0', termina
    
    # Validação: se não é A-Z, retorna 0 e out = ""
    blt t6, t3, set_empty_and_return_zero  # if (char < 'A')
    bgt t6, t4, set_empty_and_return_zero  # if (char > 'Z')
    
    # Aplicar cifra de César: (char - 'A' + key) % 26 + 'A'
    sub t7, t6, t3                  # t7 = char - 'A'
    add t7, t7, a1                  # t7 = char - 'A' + key
    rem t7, t7, t5                  # t7 = (char - 'A' + key) % 26
    add t7, t7, t3                  # t7 = (char - 'A' + key) % 26 + 'A'
    
    # Escrever caractere cifrado em out
    sb t7, 0(t2)                    # *out = caractere_cifrado
    
    # Avançar ponteiros
    addi t1, t1, 1                  # in++
    addi t2, t2, 1                  # out++
    
    j loop

end_loop:
    # Adicionar '\0' no final de out
    sb zero, 0(t2)                  # *out = '\0'
    
    # Retornar 1 (sucesso)
    li a0, 1
    ret

set_empty_and_return_zero:
    # Se out não é NULL, definir como string vazia
    beqz a2, return_zero
    sb zero, 0(a2)                  # out[0] = '\0'
    
return_zero:
    li a0, 0
    ret

