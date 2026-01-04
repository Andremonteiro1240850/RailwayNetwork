.section .text
.global enqueue_value

# int enqueue_value(int* buffer, int length, int* nelem, int* head, int* tail, int value)
# a0 = buffer (int*)
# a1 = length (int)
# a2 = nelem (int*)
# a3 = head (int*)    --> rd nos testes
# a4 = tail (int*)    --> wr nos testes
# a5 = value (int)

enqueue_value:
    # Validações
    beqz a0, return_zero
    beqz a2, return_zero
    beqz a3, return_zero
    beqz a4, return_zero
    blez a1, return_zero

    # Carregar estado atual
    lw t0, 0(a2)        # t0 = *nelem
    lw t1, 0(a3)        # t1 = *head (rd)
    lw t2, 0(a4)        # t2 = *tail (wr)

    # Verificar se buffer está cheio
    bne t0, a1, not_full

    # Buffer CHEIO: remover elemento mais antigo (no head)
    # Inserir no tail atual (vai sobrescrever)
    slli t3, t2, 2      # offset = tail * 4
    add t3, a0, t3      # endereço = buffer + offset
    sw a5, 0(t3)        # buffer[tail] = value

    # Avançar head (remove elemento mais antigo)
    addi t1, t1, 1      # head++
    blt t1, a1, head_ok
    li t1, 0            # wrap-around
head_ok:
    sw t1, 0(a3)        # atualiza *head

    # Avançar tail
    addi t2, t2, 1      # tail++
    blt t2, a1, tail_ok
    li t2, 0            # wrap-around
tail_ok:
    sw t2, 0(a4)        # atualiza *tail

    # nelem mantém-se o mesmo
    li a0, 1            # retorna 1
    ret

not_full:
    # Buffer NÃO CHEIO: inserir normalmente
    slli t3, t2, 2      # offset = tail * 4
    add t3, a0, t3      # endereço = buffer + offset
    sw a5, 0(t3)        # buffer[tail] = value

    # Avançar tail
    addi t2, t2, 1      # tail++
    blt t2, a1, tail_ok2
    li t2, 0            # wrap-around
tail_ok2:
    sw t2, 0(a4)        # atualiza *tail

    # Incrementar nelem
    addi t0, t0, 1
    sw t0, 0(a2)        # atualiza *nelem

    # Verificar se ficou cheio
    bne t0, a1, return_zero
    li a0, 1
    ret

return_zero:
    li a0, 0
    ret


