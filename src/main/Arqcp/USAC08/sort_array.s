    .section .text
    .align  2
    .global sort_array

# int sort_array(int* vec, int length, char order)
# a0 = vec, a1 = length, a2 = order
# return a0 = 1 (success) or 0 (error)

sort_array:
    addi    sp, sp, -8
    sw      ra, 4(sp)
    
    # Validação de parâmetros
    beqz    a0, error         # vec == NULL?
    blez    a1, error         # length <= 0?
    li      t0, 1
    bgt     a2, t0, error     # order > 1?
    blt     a2, zero, error   # order < 0?
    
    # Bubble sort otimizado
    addi    t0, a1, -1        # i_max = length-1
    li      t1, 0             # i = 0
    
outer_loop:
    bge     t1, t0, success   # i >= i_max? done
    li      t2, 0             # j = 0
    sub     t3, t0, t1        # j_max = i_max - i
    
inner_loop:
    bge     t2, t3, next_outer
    slli    t4, t2, 2         # j * 4
    add     t5, a0, t4        # &vec[j]
    lw      t6, 0(t5)         # vec[j]
    lw      t7, 4(t5)         # vec[j+1]
    
    # Verificar se precisa swap
    beqz    a2, descending
    bgt     t6, t7, do_swap   # Ascending: swap if vec[j] > vec[j+1]
    j       no_swap
    
descending:
    blt     t6, t7, do_swap   # Descending: swap if vec[j] < vec[j+1]
    j       no_swap
    
do_swap:
    sw      t7, 0(t5)
    sw      t6, 4(t5)
    
no_swap:
    addi    t2, t2, 1         # j++
    j       inner_loop
    
next_outer:
    addi    t1, t1, 1         # i++
    j       outer_loop
    
success:
    li      a0, 1
    j       exit
    
error:
    li      a0, 0
    
exit:
    lw      ra, 4(sp)
    addi    sp, sp, 8
    ret