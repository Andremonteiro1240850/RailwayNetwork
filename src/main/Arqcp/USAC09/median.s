.section .text
.global median

# int median(int* vec, int length, int* me)
# a0 = vec
# a1 = length
# a2 = me
median:

    addi sp, sp, -4
    sw s0, 0(sp)
    mv s0, a1                   # s0 = length (a1)

    beqz a0, return_zero        # if (!vec) return 0
    beqz a2, return_zero        # if (!me) return 0
    blez s0, return_zero        # if (length <= 0) return 0 (usando s0)



    li t0, 0                    # t0 = i
outer_loop:
    addi t3, s0, -1             # t3 = length - 1 (usando s0)
    bge t0, t3, end_outer_loop  # while (i < length - 1)

    # j = 0
    li t1, 0                    # t1 = j
inner_loop:
    sub t2, s0, t0              # t2 = length - i (usando s0)
    addi t2, t2, -1             # t2 = length - i - 1
    bge t1, t2, end_inner_loop  # while (j < length - i - 1)


    slli t3, t1, 2              # t3 = offset j * 4
    add t4, a0, t3              # t4 = &vec[j]
    lw t5, 0(t4)                # t5 = vec[j] (valor 1)


    addi t3, t1, 1              # t3 = j+1
    slli t3, t3, 2              # t3 = offset (j+1) * 4
    add t6, a0, t3              # t6 = &vec[j+1]
    lw t2, 0(t6)                # t2 = vec[j+1] (valor 2)


    ble t5, t2, skip_swap


    sw t2, 0(t4)                # vec[j] = vec[j+1] (usando t4 = &vec[j])
    sw t5, 0(t6)                # vec[j+1] = vec[j] (usando t6 = &vec[j+1])

skip_swap:
    addi t1, t1, 1              # j++
    j inner_loop

end_inner_loop:
    addi t0, t0, 1              # i++
    j outer_loop

end_outer_loop:



    li t3, 2                    # t3 = 2 (divisor)
    rem t2, s0, t3              # t2 = length % 2 (usando s0)
    beqz t2, even_case          # se for par, vai para even_case


odd_case:
    div t2, s0, t3              # t2 = index = length / 2

    slli t3, t2, 2              # t3 = offset = index * 4
    add t4, a0, t3              # t4 = &vec[index]
    lw t5, 0(t4)                # t5 = vec[index]

    sw t5, 0(a2)                # *me = mediana
    j return_one


even_case:
    div t2, s0, t3              # t2 = mid = length / 2


    addi t3, t2, -1             # t3 = mid - 1
    slli t4, t3, 2              # t4 = offset (mid-1) * 4
    add t5, a0, t4              # t5 = &vec[mid - 1]
    lw t6, 0(t5)                # t6 = vec[mid - 1] (valor 1)


    slli t4, t2, 2              # t4 = offset mid * 4
    add t5, a0, t4              # t5 = &vec[mid]
    lw t2, 0(t5)                # t2 = vec[mid] (valor 2)


    add t3, t6, t2              # t3 = soma = vec[mid-1] + vec[mid]
    srai t3, t3, 1              # t3 = soma / 2

    sw t3, 0(a2)                # *me = media
    j return_one


return_zero:
    li a0, 0
    j restore_and_ret

return_one:
    li a0, 1

restore_and_ret:
    # Restaura registo s0 e retorna
    lw s0, 0(sp)
    addi sp, sp, 4
    ret