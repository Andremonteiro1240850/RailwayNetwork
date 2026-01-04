.section .text
.global move_n_to_array

# Arguments (Registos preservados):
# a0: int* buffer
# a1: int length
# a2: int* nelem
# a3: int* tail
# a4: int* head
# a5: int n
# a6: int* array

move_n_to_array:

    addi sp, sp, -12
    sw s0, 0(sp)
    sw s1, 4(sp)
    sw s2, 8(sp)

    # s0: a1 (length)
    # s1: a5 (n)
    # s2: a6 (array)
    mv s0, a1
    mv s1, a5
    mv s2, a6


    # t0: *nelem
    # t1: *tail
    # t2: room / new_tail
    # t3: first_chunk
    # t4: i (loop counter)
    # t5: offset (for array indexing)
    # t6: valor (value loaded from buffer)


    beqz a0, return_zero
    beqz a2, return_zero
    beqz a3, return_zero
    beqz a4, return_zero
    beqz a6, return_zero

    blez s0, return_zero        # if (length <= 0) return 0 (usando s0)
    bltz s1, return_zero        # if (n < 0) return 0 (usando s1)

    lw t0, 0(a2)                # t0 = *nelem
    lw t1, 0(a3)                # t1 = *tail

    blt t0, s1, return_zero     # if (*nelem < n) return 0
    beqz s1, return_one         # if (n == 0) return 1

    bltz t1, return_zero        # if (tail < 0) return 0
    bge  t1, s0, return_zero    # if (tail >= length) return 0 (usando s0)


    sub t2, s0, t1              # t2 = room = length - tail (usando s0)
    mv t3, s1                   # t3 = n (usando s1)
    ble s1, t2, set_firstchunk  # if (n <= room)
    mv t3, t2                   # else: first_chunk = room

set_firstchunk:
    # t3 já contém first_chunk
    # t4 = i (counter)
    li t4, 0

first_loop:
    bge t4, t3, end_first_loop  # while (i < first_chunk)

    add t5, t1, t4              # t5 = index = tail + i
    slli t5, t5, 2              # t5 = offset = index * 4
    add t5, a0, t5              # t5 = &buffer[index]
    lw t6, 0(t5)                # t6 = valor = buffer[index]

    slli t5, t4, 2              # t5 = offset = i * 4
    add t5, s2, t5              # t5 = &array[i] (usando s2)
    sw t6, 0(t5)                # array[i] = valor

    addi t4, t4, 1              # i++
    j first_loop

end_first_loop:



    sub t2, s1, t3              # t2 = second_chunk = n - first_chunk (usando s1)
    li t4, 0                    # t4 = i = 0

second_loop:
    bge t4, t2, end_second_loop # while (i < second_chunk)

    slli t5, t4, 2              # t5 = offset = i * 4
    add t5, a0, t5              # t5 = &buffer[i]
    lw t6, 0(t5)                # t6 = valor = buffer[i]

    add t5, t3, t4              # t5 = first_chunk + i
    slli t5, t5, 2              # t5 = (first_chunk + i) * 4
    add t5, s2, t5              # t5 = &array[first_chunk + i] (usando s2)
    sw t6, 0(t5)

    addi t4, t4, 1
    j second_loop

end_second_loop:



    add t2, t1, s1              # t2 = new_tail = *tail + n (usando s1)
    blt t2, s0, no_wrap         # if (new_tail < length) (usando s0)
    sub t2, t2, s0              # new_tail -= length (usando s0)

no_wrap:
    sw t2, 0(a3)                # *tail = new_tail
    sub t0, t0, s1              # t0 = *nelem - n
    sw t0, 0(a2)                # *nelem = new_nelem


    li a0, 1                    # Retorna 1 (sucesso)


    lw s2, 8(sp)
    lw s1, 4(sp)
    lw s0, 0(sp)
    addi sp, sp, 12
    ret


return_zero:

    lw s2, 8(sp)
    lw s1, 4(sp)
    lw s0, 0(sp)
    addi sp, sp, 12
    li a0, 0
    ret

return_one:

    lw s2, 8(sp)
    lw s1, 4(sp)
    lw s0, 0(sp)
    addi sp, sp, 12
    li a0, 1
    ret