.section .text
.global move_n_to_array

# Arguments:
# a0: int* buffer
# a1: int length
# a2: int* nelem
# a3: int* tail
# a4: int* head
# a5: int n
# a6: int* array

move_n_to_array:


    beqz a0, return_zero
    beqz a2, return_zero
    beqz a3, return_zero
    beqz a4, return_zero
    beqz a6, return_zero

    blez a1, return_zero        # if (length <= 0) return 0
    bltz a5, return_zero        # if (n < 0) return 0


    lw t0, 0(a2)                # t0 = *nelem
    lw t1, 0(a3)                # t1 = *tail

    blt t0, a5, return_zero     # if (*nelem < n) return 0
    beqz a5, return_one         # if (n == 0) return 1

    bltz t1, return_zero        # if (tail < 0) return 0
    bge  t1, a1, return_zero    # if (tail >= length) return 0


    sub t2, a1, t1              # t2 = room = length - tail
    mv t3, a5                   # t3 = n
    ble a5, t2, set_firstchunk  # if (n <= room)
    mv t3, t2                   # else: first_chunk = room

set_firstchunk:
    mv t4, t3                   # t4 = first_chunk


    li t5, 0                    # i = 0

first_loop:
    bge t5, t4, end_first_loop  # while (i < first_chunk)
    add t6, t1, t5              # index = tail + i
    slli t7, t6, 2              # offset = index * 4
    add t8, a0, t7              # &buffer[index]
    lw t9, 0(t8)                # valor = buffer[index]

    slli t10, t5, 2             # offset = i * 4
    add t11, a6, t10            # &array[i]
    sw t9, 0(t11)               # array[i] = valor

    addi t5, t5, 1              # i++
    j first_loop

end_first_loop:


    sub t12, a5, t4             # second_chunk = n - first_chunk
    li t5, 0                    # i = 0

second_loop:
    bge t5, t12, end_second_loop
    slli t13, t5, 2
    add t14, a0, t13            # &buffer[i]
    lw t15, 0(t14)              # valor = buffer[i]

    add t16, t4, t5             # first_chunk + i
    slli t17, t16, 2
    add t18, a6, t17            # &array[first_chunk + i]
    sw t15, 0(t18)

    addi t5, t5, 1
    j second_loop

end_second_loop:


    add t19, t1, a5             # new_tail = tail + n
    blt t19, a1, no_wrap
    sub t19, t19, a1            # new_tail -= length

no_wrap:
    sw t19, 0(a3)               # *tail = new_tail
    sub t20, t0, a5             # *nelem = cur - n
    sw t20, 0(a2)


    li a0, 1
    ret


return_zero:
    li a0, 0
    ret

return_one:
    li a0, 1
    ret
