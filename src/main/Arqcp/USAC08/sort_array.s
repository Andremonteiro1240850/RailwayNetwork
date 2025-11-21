.section .text
    .align  2
    .global sort_array

# int sort_array(int* vec, int length, char order)
# a0 = vec, a1 = length, a2 = order

sort_array:
    addi    sp, sp, -16
    sw      ra, 12(sp)

    # validar parâmetros
    beqz    a0, error
    blez    a1, error
    li      t0, 1
    bgt     a2, t0, error     # order > 1
    blt     a2, zero, error   # order < 0

    addi    t0, a1, -1        # i_max = length - 1
    li      t1, 0             # i = 0

outer_loop:
    bge     t1, t0, success

    # end_ptr = vec + (length - i - 1)*4
    sub     t3, a1, t1        # length - i
    addi    t3, t3, -1        # length - i - 1
    slli    t3, t3, 2
    add     t3, a0, t3        # end_ptr

    mv      t2, a0            # current_ptr = vec
    li      t4, 0             # swap_flag = 0   (t4 substitui t9)

inner_loop:
    lw      t5, 0(t2)         # vec[j]
    lw      t6, 4(t2)         # vec[j+1]

    beqz    a2, descending

    # ASCENDING
    bgt     t5, t6, do_swap
    j       no_swap

descending:
    blt     t5, t6, do_swap

no_swap:
    addi    t2, t2, 4
    blt     t2, t3, inner_loop
    j       check_swaps

do_swap:
    sw      t6, 0(t2)
    sw      t5, 4(t2)
    li      t4, 1
    addi    t2, t2, 4
    blt     t2, t3, inner_loop

check_swaps:
    beqz    t4, success
    addi    t1, t1, 1
    j       outer_loop

success:
    li      a0, 1
    j       exit

error:
    li      a0, 0

exit:
    lw      ra, 12(sp)
    addi    sp, sp, 16
    ret
