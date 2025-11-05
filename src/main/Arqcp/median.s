.section .text
.global median

# int median(int* vec, int length, int* me)
# a0 = vec
# a1 = length
# a2 = me
median:


    beqz a0, return_zero        # if (!vec) return 0
    beqz a2, return_zero        # if (!me) return 0
    blez a1, return_zero        # if (length <= 0) return 0


    li t0, 0                    # t0 = i
outer_loop:
    addi t1, a1, -1
    bge t0, t1, end_outer_loop  # while (i < length - 1)

    # j = 0
    li t2, 0                    # t2 = j
inner_loop:
    sub t3, a1, t0              # t3 = length - i
    addi t3, t3, -1             # t3 = length - i - 1
    bge t2, t3, end_inner_loop  # while (j < length - i - 1)

    # comparar vec[j] e vec[j+1]
    slli t4, t2, 2              # offset j * 4
    add t5, a0, t4              # &vec[j]
    lw t6, 0(t5)                # t6 = vec[j]

    addi t2, t2, 1              # j+1
    slli t7, t2, 2
    add t8, a0, t7              # &vec[j+1]
    lw t9, 0(t8)                # t9 = vec[j+1]

    # restaurar j original para continuar o loop corretamente
    addi t2, t2, -1

    ble t6, t9, skip_swap       # if (vec[j] <= vec[j+1]) skip

    # swap: temp = vec[j]; vec[j] = vec[j+1]; vec[j+1] = temp;
    mv t10, t6                  # temp = vec[j]
    sw t9, 0(t5)                # vec[j] = vec[j+1]
    sw t10, 0(t8)               # vec[j+1] = temp

skip_swap:
    addi t2, t2, 1              # j++
    j inner_loop

end_inner_loop:
    addi t0, t0, 1              # i++
    j outer_loop

end_outer_loop:

    # --- calcular mediana ---
    rem t11, a1, 2              # t11 = length % 2
    beqz t11, even_case         # se for par, vai para even_case

# ímpar: *me = vec[length / 2]
odd_case:
    div t12, a1, 2              # t12 = length / 2
    slli t13, t12, 2
    add t14, a0, t13
    lw t15, 0(t14)
    sw t15, 0(a2)
    j return_one

# par: *me = (vec[mid-1] + vec[mid]) / 2
even_case:
    div t16, a1, 2              # t16 = mid
    addi t17, t16, -1           # mid - 1
    slli t18, t17, 2
    add t19, a0, t18            # &vec[mid - 1]
    lw t20, 0(t19)              # vec[mid - 1]

    slli t21, t16, 2
    add t22, a0, t21            # &vec[mid]
    lw t23, 0(t22)              # vec[mid]

    add t24, t20, t23           # soma dos dois do meio
    srai t25, t24, 1            # dividir por 2
    sw t25, 0(a2)               # *me = media
    j return_one

# retorno
return_zero:
    li a0, 0
    ret

return_one:
    li a0, 1
    ret
