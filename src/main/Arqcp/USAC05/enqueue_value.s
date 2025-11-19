.section .text
.global enqueue_value

# int enqueue_value(int* buffer, int length, int* nelem, int* tail, int* head, int value)
# a0 = buffer (int*)
# a1 = length (int)
# a2 = nelem (int*)
# a3 = tail (int*)
# a4 = head (int*)
# a5 = value (int)

enqueue_value:

    # -------------------------
    #  Validation of arguments
    # -------------------------
    beqz a0, return_zero            # if (!buffer) return 0
    beqz a2, return_zero            # if (!nelem) return 0
    beqz a3, return_zero            # if (!tail) return 0
    beqz a4, return_zero            # if (!head) return 0
    blez a1, return_zero            # if (length <= 0) return 0

    # -------------------------
    #  Load current state
    # -------------------------
    lw t0, 0(a2)                    # t0 = *nelem
    lw t1, 0(a3)                    # t1 = *tail
    lw t2, 0(a4)                    # t2 = *head

    # -------------------------
    #  Validate values
    # -------------------------
    bltz t0, return_zero            # if (*nelem < 0) return 0
    bge t0, a1, return_zero         # if (*nelem >= length) return 0
    bltz t1, return_zero            # if (*tail < 0) return 0
    bge t1, a1, return_zero         # if (*tail >= length) return 0
    bltz t2, return_zero            # if (*head < 0) return 0
    bge t2, a1, return_zero         # if (*head >= length) return 0

    # -------------------------
    #  If the buffer is full:
    #  remove the oldest element
    # -------------------------
    bne t0, a1, not_full            # if (*nelem != length) skip removal

    # increment head with wrap-around
    addi t2, t2, 1
    blt t2, a1, head_no_wrap
    li t2, 0
head_no_wrap:
    sw t2, 0(a4)                    # *head = new_head
    addi t0, t0, -1                 # nelem--

not_full:
    # -------------------------
    #  Insert value at buffer[tail]
    # -------------------------
    slli t3, t1, 2                  # offset = tail * 4
    add t4, a0, t3                  # address = &buffer[tail]
    sw a5, 0(t4)                    # buffer[tail] = value

    # -------------------------
    #  Increment tail (wrap)
    # -------------------------
    addi t1, t1, 1
    blt t1, a1, tail_no_wrap
    li t1, 0
tail_no_wrap:
    sw t1, 0(a3)                    # *tail = new_tail

    # -------------------------
    #  Increment nelem
    # -------------------------
    addi t0, t0, 1
    sw t0, 0(a2)                    # *nelem = new_nelem

    # -------------------------
    #  Return 1 if buffer is FULL
    # -------------------------
    beq t0, a1, return_one          # if (*nelem == length) return 1

    # otherwise return 0
    li a0, 0
    ret

return_one:
    li a0, 1
    ret

return_zero:
    li a0, 0
    ret


