    .data
unit_pattern:
    .asciz "&unit:"
value_pattern:
    .asciz "&value:"

    .text
    .globl extract_data
    .type extract_data, @function

extract_data:
    addi    sp, sp, -20          # prólogo
    sw      ra, 16(sp)           # guardar ra
    sw      s3, 12(sp)           # guardar s3
    sw      s2, 8(sp)            # guardar s2
    sw      s1, 4(sp)            # guardar s1
    sw      s0, 0(sp)            # guardar s0

    mv      s0, a0               # str
    mv      s1, a1               # token
    mv      s2, a2               # unit
    mv      s3, a3               # value

    li      t0, 0
    sw      t0, 0(s3)            # *value = 0
    sb      t0, 0(s2)            # unit="" vazio

    mv      t0, s0               # cursor str

token_outer_loop:
    lb      t1, 0(t0)            # ler char
    beq     t1, zero, not_found_return0   # fim

    mv      t2, t0               # ptr str
    mv      t3, s1               # ptr token

token_match_loop:
    lb      t4, 0(t3)            # token char
    beq     t4, zero, token_found   # token completo

    lb      t5, 0(t2)            # str char
    beq     t5, zero, token_advance
    bne     t4, t5, token_advance  # mismatch

    addi    t2, t2, 1            # avança str
    addi    t3, t3, 1            # avança token
    j       token_match_loop

token_advance:
    addi    t0, t0, 1            # próxima posição
    j       token_outer_loop

token_found:
    beq     t0, s0, token_ok     # ok se no início

    addi    t1, t0, -1           # char anterior
    lb      t3, 0(t1)            # ler char anterior
    li      t4, 35               # '#'
    bne     t3, t4, token_advance   # não é delimitado

token_ok:
    mv      t6, t2               # após token

    la      t3, unit_pattern     # padrão unit
    mv      t0, t6

find_unit_outer:
    lb      t1, 0(t0)
    beq     t1, zero, not_found_return0
    li      t2, '#'
    beq     t1, t2, not_found_return0

    li      t2, '&'
    bne     t1, t2, find_unit_next_char

    mv      t4, t0               # ptr str
    mv      t5, t3               # ptr padrão

unit_match_loop:
    lb      t1, 0(t5)
    beq     t1, zero, unit_found  # padrão completo

    lb      t2, 0(t4)
    beq     t2, zero, unit_fail_match
    bne     t1, t2, unit_fail_match

    addi    t4, t4, 1
    addi    t5, t5, 1
    j       unit_match_loop

unit_fail_match:
    addi    t0, t0, 1
    j       find_unit_outer

find_unit_next_char:
    addi    t0, t0, 1
    j       find_unit_outer

unit_found:
    mv      t1, t4               # origem unidade
    mv      t2, s2               # destino unidade

copy_unit_loop:
    lb      t3, 0(t1)
    beq     t3, zero, end_copy_unit
    li      t4, '&'
    beq     t3, t4, end_copy_unit
    li      t4, '#'
    beq     t3, t4, end_copy_unit

    sb      t3, 0(t2)            # copiar
    addi    t1, t1, 1
    addi    t2, t2, 1
    j       copy_unit_loop

end_copy_unit:
    li      t3, 0
    sb      t3, 0(t2)            # termina string unidade

    la      t3, value_pattern    # padrão value
    mv      t0, t1

find_value_outer:
    lb      t1, 0(t0)
    beq     t1, zero, not_found_return0
    li      t2, '#'
    beq     t1, t2, not_found_return0
    li      t2, '&'
    bne     t1, t2, find_value_next_char

    mv      t4, t0
    mv      t5, t3

value_match_loop:
    lb      t1, 0(t5)
    beq     t1, zero, value_found
    lb      t2, 0(t4)
    beq     t2, zero, value_fail_match
    bne     t1, t2, value_fail_match
    addi    t4, t4, 1
    addi    t5, t5, 1
    j       value_match_loop

value_fail_match:
    addi    t0, t0, 1
    j       find_value_outer

find_value_next_char:
    addi    t0, t0, 1
    j       find_value_outer

value_found:
    mv      t1, t4               # ptr dígitos
    li      t6, 0                # acumulador

parse_digits_loop:
    lb      t0, 0(t1)
    li      t3, 48
    blt     t0, t3, end_parse_digits
    li      t4, 57
    blt     t4, t0, end_parse_digits

    addi    t0, t0, -48          # ASCII→int

    li      t2, 10
    mul     t5, t6, t2           # value*=10
    add     t6, t5, t0           # value+=digito

    addi    t1, t1, 1
    j       parse_digits_loop

end_parse_digits:
    sw      t6, 0(s3)            # guardar value
    li      a0, 1                # return 1
    j       epilogue

not_found_return0:
    li      a0, 0                # return 0

epilogue:
    lw      s0, 0(sp)            # restaurar regs
    lw      s1, 4(sp)
    lw      s2, 8(sp)
    lw      s3, 12(sp)
    lw      ra, 16(sp)
    addi    sp, sp, 20
    ret                           # retorno final
