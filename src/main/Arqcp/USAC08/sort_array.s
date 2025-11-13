    .section .text
    .align  2
    .global sort_array
    .type   sort_array, @function

# int sort_array(int* vec, int length, char order)
# a0 = vec
# a1 = length
# a2 = order
# return a0 = 1 (ok) or 0 (error)

sort_array:
    addi    sp, sp, -16
    sw      ra, 12(sp)
    sw      s0, 8(sp)
    mv      s0, sp

    beqz    a0, .err
    blez    a1, .err

    # outer i = 0 .. length-2
    addi    t0, zero, 0         # i = 0
    addi    t1, a1, -1          # limit = length-1
.outer:
    bge     t0, t1, .done
    addi    t2, zero, 0         # j = 0
    sub     t3, t1, t0          # inner limit = length-1-i
.inner:
    bge     t2, t3, .next_outer
    slli    t4, t2, 2           # offset j*4
    add     t5, a0, t4          # &vec[j]
    lw      t6, 0(t5)           # vj
    lw      t7, 4(t5)           # vj1

    # if order==1 ascending: swap if vj > vj1
    beqz    a2, .check_desc
    bge     t6, t7, .swap
    j       .no_swap

.check_desc:
    # descending: swap if vj < vj1
    ble     t6, t7, .swap
    j       .no_swap

.swap:
    sw      t7, 0(t5)
    sw      t6, 4(t5)

.no_swap:
    addi    t2, t2, 1
    j       .inner

.next_outer:
    addi    t0, t0, 1
    j       .outer

.done:
    li      a0, 1
    j       .finish

.err:
    li      a0, 0

.finish:
    lw      ra, 12(sp)
    lw      s0, 8(sp)
    addi    sp, sp, 16
    ret
