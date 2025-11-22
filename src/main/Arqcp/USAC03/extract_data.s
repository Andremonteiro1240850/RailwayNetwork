
    .data
unit_pattern:
    .asciz "&unit:"
value_pattern:
    .asciz "&value:"

    .text
    .globl extract_data
    .type extract_data, @function

# int extract_data(char* str, char* token, char* unit, int* value)
# a0 = str
# a1 = token
# a2 = unit
# a3 = value
extract_data:
    # Prologue: reservar espaço e guardar s0-s3, ra
    addi    sp, sp, -20
    sw      ra, 16(sp)
    sw      s3, 12(sp)
    sw      s2, 8(sp)
    sw      s1, 4(sp)
    sw      s0, 0(sp)

    # Guardar argumentos em s0-s3
    mv      s0, a0        # s0 = str
    mv      s1, a1        # s1 = token
    mv      s2, a2        # s2 = unit
    mv      s3, a3        # s3 = value

    # Valores por defeito: *value = 0; unit[0] = '\0'
    li      t0, 0
    sw      t0, 0(s3)     # *value = 0
    sb      t0, 0(s2)     # unit[0] = '\0'

    ############################################
    # 1. Procurar token em str (substring search)
    ############################################
    mv      t0, s0        # t0 = ptr atual em str (scan pointer)

token_outer_loop:
    lb      t1, 0(t0)     # t1 = *t0
    beq     t1, zero, not_found_return0  # fim da string -> token não encontrado

    # tentar corresponder token a partir de t0
    mv      t2, t0        # t2 = ptr na str
    mv      t3, s1        # t3 = ptr no token

token_match_loop:
    lb      t4, 0(t3)     # t4 = *token
    beq     t4, zero, token_found   # fim do token -> match completo

    lb      t5, 0(t2)     # t5 = *str
    beq     t5, zero, token_advance # fim da str -> não bate

    bne     t4, t5, token_advance   # chars diferentes -> falhou neste ponto

    addi    t2, t2, 1     # avançar str
    addi    t3, t3, 1     # avançar token
    j       token_match_loop

token_advance:
    addi    t0, t0, 1     # avançar ponto de tentativa na str
    j       token_outer_loop

token_found:
    # t2 aponta para o primeiro char após o token em str
    mv      t6, t2        # t6 = ponteiro após o token

    ############################################
    # 2. Procurar "&unit:" a partir de t6
    ############################################
    la      t3, unit_pattern   # t3 = "&unit:"

    mv      t0, t6         # t0 = scan ptr para encontrar "&unit:"

find_unit_outer:
    lb      t1, 0(t0)
    beq     t1, zero, not_found_return0  # fim da str -> falhou
    li      t2, '#'
    beq     t1, t2, not_found_return0    # encontrou '#' antes de &unit:

    li      t2, '&'
    bne     t1, t2, find_unit_next_char  # não é '&', continuar a avançar

    # Tentativa de match ao padrão "&unit:"
    mv      t4, t0        # t4 = ptr na str
    mv      t5, t3        # t5 = ptr no padrão "&unit:"

unit_match_loop:
    lb      t1, 0(t5)     # char do padrão
    beq     t1, zero, unit_found    # fim do padrão -> match completo

    lb      t2, 0(t4)     # char da str
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
    # t4 aponta para o primeiro char após "&unit:"
    mv      t1, t4        # t1 = src da unidade
    mv      t2, s2        # t2 = dest unit[]

    ############################################
    # 3. Copiar unit até '&', '#', ou '\0'
    ############################################
copy_unit_loop:
    lb      t3, 0(t1)
    beq     t3, zero, end_copy_unit

    li      t4, '&'
    beq     t3, t4, end_copy_unit

    li      t4, '#'
    beq     t3, t4, end_copy_unit

    sb      t3, 0(t2)     # copiar char
    addi    t1, t1, 1
    addi    t2, t2, 1
    j       copy_unit_loop

end_copy_unit:
    li      t3, 0
    sb      t3, 0(t2)     # terminar string unit com '\0'

    # t1 aponta agora para o delimitador ('&', '#', ou '\0')
    ############################################
    # 4. Procurar "&value:" a partir de t1
    ############################################
    la      t3, value_pattern  # "&value:"
    mv      t0, t1             # t0 = scan ptr para value

find_value_outer:
    lb      t1, 0(t0)
    beq     t1, zero, not_found_return0
    li      t2, '#'
    beq     t1, t2, not_found_return0

    li      t2, '&'
    bne     t1, t2, find_value_next_char

    # tentar corresponder "&value:"
    mv      t4, t0        # t4 = ptr na str
    mv      t5, t3        # t5 = ptr no padrão

value_match_loop:
    lb      t1, 0(t5)
    beq     t1, zero, value_found   # fim do padrão -> match

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
    # t4 aponta para o primeiro char após "&value:"
    mv      t1, t4        # t1 = ptr para dígitos
    li      t6, 0         # acumulador do inteiro (value = 0)

    ############################################
    # 5. Converter dígitos ASCII para inteiro
    ############################################
parse_digits_loop:
    lb      t0, 0(t1)     # t0 = char atual

    li      t3, 48        # '0'
    blt     t0, t3, end_parse_digits

    li      t4, 57        # '9'
    blt     t4, t0, end_parse_digits   # se t0 > '9' -> sair

    # dígito válido em t0: '0'..'9'
    addi    t0, t0, -48   # converter para 0..9

    # t6 = t6*10 + t0
    li      t2, 10
    mul     t5, t6, t2
    add     t6, t5, t0

    addi    t1, t1, 1
    j       parse_digits_loop

end_parse_digits:
    sw      t6, 0(s3)     # *value = value
    li      a0, 1         # return 1 (sucesso)
    j       epilogue

############################################
# Falha em qualquer ponto: já temos unit=""
# e *value=0 definidos no início.
############################################
not_found_return0:
    li      a0, 0         # return 0

epilogue:
    # Restaurar s0-s3, ra e libertar stack
    lw      s0, 0(sp)
    lw      s1, 4(sp)
    lw      s2, 8(sp)
    lw      s3, 12(sp)
    lw      ra, 16(sp)
    addi    sp, sp, 20
    jr      ra
