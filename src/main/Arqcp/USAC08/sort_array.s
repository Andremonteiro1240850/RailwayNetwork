.section .text
    .align  2
    .global sort_array

# int sort_array(int* vec, int length, char order)
# a0 = vec, a1 = length, a2 = order
# returns a0 = 1 (success) or 0 (error)

sort_array:
    addi    sp, sp, -16
    sw      ra, 12(sp)

    # Parameter validation
    beqz    a0, error
    blez    a1, error
    li      t0, 1
    bgt     a2, t0, error
    blt     a2, zero, error

    # Bubble sort with proper bounds: j = 0 to j < (length - i - 1)
    addi    t0, a1, -1        # i_max = length - 1
    li      t1, 0             # i = 0

outer_loop:
    bge     t1, t0, success   # i >= length-1? done

    # Calculate end pointer for this pass: vec + (length - i - 1) * 4
    sub     t3, a1, t1        # length - i
    addi    t3, t3, -1        # length - i - 1
    slli    t3, t3, 2         # byte offset = (length - i - 1) * 4
    add     t3, a0, t3        # end_ptr = vec + (length - i - 1) * 4

    # Setup current pointer
    mv      t2, a0            # current_ptr = vec
    li      t9, 0             # swap_flag = 0

inner_loop:
    # Compare current_ptr[0] and current_ptr[1]
    lw      t6, 0(t2)         # vec[j]
    lw      t7, 4(t2)         # vec[j+1]

    # Check swap condition
    beqz    a2, descending
    bgt     t6, t7, do_swap   # Ascending: swap if greater
    j       no_swap

descending:
    blt     t6, t7, do_swap   # Descending: swap if less
    j       no_swap

do_swap:
    sw      t7, 0(t2)
    sw      t6, 4(t2)
    li      t9, 1             # swap occurred

no_swap:
    addi    t2, t2, 4         # current_ptr += 4
    blt     t2, t3, inner_loop # Continue until current_ptr < end_ptr

check_swaps:
    beqz    t9, success       # Early exit if no swaps

next_outer:
    addi    t1, t1, 1         # i++
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