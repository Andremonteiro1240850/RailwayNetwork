.section .text
    .align  2
    .global sort_array

# int sort_array(int* vec, int length, char order)
# a0 = vec, a1 = length, a2 = order

sort_array:
    addi    sp, sp, -16
    sw      ra, 12(sp)

    # validar parâmetros
    beqz    a0, error         # vec == NULL
    blez    a1, error         # length <= 0
    li      t0, 1
    bgt     a2, t0, error     # order > 1
    blt     a2, zero, error   # order < 0

    addi    t0, a1, -1        # i_max = length - 1
    li      t1, 0             # i = 0

outer_loop:
    bge     t1, t0, success   # i >= i_max - array ordenado

    # end_ptr = vec + (length - i - 1) * 4
    sub     t3, a1, t1        # length - i
    addi    t3, t3, -1        # length - i - 1
    slli    t3, t3, 2         # *4 porque cada int tem 4 bytes
    add     t3, a0, t3        # end_ptr - aponta para o último elemento a ser comparado neste loop

    mv      t2, a0            # current_ptr = vec
    li      t4, 0             # swap_flag = 0

inner_loop:
    lw      t5, 0(t2)         # vec[j]
    lw      t6, 4(t2)         # vec[j+1]

    beqz    a2, descending    # se order == 0 → vai para descending

    # ASCENDING
    bgt     t5, t6, do_swap
    j       no_swap

descending:
    blt     t5, t6, do_swap

no_swap:
    addi    t2, t2, 4
    blt     t2, t3, inner_loop      #t2 < t3, verificação se já chegou ao fim da comparação, se não volta ao loop
    j       check_swaps

do_swap:
    sw      t6, 0(t2)  # vec[j] = t6
    sw      t5, 4(t2)  # vec[j+1] = t5 (troca)
    li      t4, 1      # swap_flag = 1
    addi    t2, t2, 4
    blt     t2, t3, inner_loop  #avança para o proximo par

check_swaps:
    beqz    t4, success  # swap_flag == 0 - nenhuma troca
    addi    t1, t1, 1  #incrementa i e volta ao loop externo
    j       outer_loop

success:
    li      a0, 1
    j       exit

error:
    li      a0, 0

exit:
    lw      ra, 12(sp)   #restaura ra
    addi    sp, sp, 16   #liberta a stack
    ret
