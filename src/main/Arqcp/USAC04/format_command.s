.section .text
    .align  2
    .global format_command

# int format_command(char *op, int n, char *cmd)
# a0 = op, a1 = n, a2 = cmd
# returns 1 (success) or 0 (error)

format_command:
    addi    sp, sp, -32
    sw      ra, 28(sp)
    sw      s0, 24(sp)

    # Basic parameter validation
    beqz    a0, .error
    beqz    a2, .error

    # Temporary buffer on stack (16 bytes)
    addi    s0, sp, 8    #s0 aponta para inicio do buffer

    # PHASE 1: Process and convert to uppercase, trim leading whitespace
    mv      t0, a0           # src pointer (input)
    mv      t1, s0           # pointer de escrita (buffer on stack)
    li      t2, 0            # length counter (chars copied)
    li      t3, 0            # found_non_space flag (0 = only whitespace so far)

.process_input:
    lbu     t4, 0(t0)        # load byte from src
    beqz    t4, .process_done

    # Check if whitespace (any char <= 0x20)
    li      t5, 0x20     #codigo ASCII espaço
    ble     t4, t5, .is_whitespace      #t4 menor que t5, slat para whitespace
    # not whitespace
    li      t3, 1            # found non-space, muda a flag, indicando que apanhou caracter e não espaço
    # Convert to uppercase if lowercase 'a'..'z'
    li      t5, 'a'    #Carrega valor ASCII DE 'a'
    blt     t4, t5, .store_char    #t4 menor que t5, não é minuscula, store char
    li      t5, 'z'
    bgt     t4, t5, .store_char
    addi    t4, t4, -32      # subtrai 32 para converter para maiuscula

.store_char:
    sb      t4, 0(t1)      #armazena o byte apontado por t1 em t4
    addi    t1, t1, 1       #aumenta o ponteir do buffer
    addi    t2, t2, 1       #aumaneta o contador de caracteres copiados
    li      t5, 16          #carrega 16 em t5, O buffer tem 16 bytes, mas precisamos de espaço para '\0', então máximo 15 caracteres
    bge     t2, t5, .error   # t2 maior ou igual a t5, error
    addi    t0, t0, 1       #aumenta pointer de input, passar para o proximo byte
    j       .process_input

.is_whitespace:
    # IGNORE ALL whitespace (leading, middle, trailing)
    addi    t0, t0, 1
    j       .process_input

.process_done:
    beqz    t2, .error       # string vazia(comprimento = 0)
    beqz    t3, .error       # contem apenas espaços (flag = 0)

    # PHASE 2: Remove trailing whitespace
    mv      t0, s0           # start of buffer
    add     t1, t0, t2       # t1 points para depois do ultimo char do buffer

.remove_trailing:
    beq     t1, t0, .all_whitespace    #se t1 = t0, whitespace(buffer vazio)
    addi    t1, t1, -1     #move o pointer para char anterior
    lbu     t3, 0(t1)
    li      t4, 0x20
    ble     t3, t4, .remove_trailing   (remover espaço final)
    # t1 now points at last non-whitespace char
    addi    t1, t1, 1       # Incrementa em 1 para apontar para a posição onde deve colocar o '\0'
    sb      zero, 0(t1)      # armazena zero para terminar
    sub     t2, t1, t0       # new length
    j       .check_commands

.all_whitespace:
    j       .error

.check_commands:
    # t2 = final length
    lbu     t3, 0(s0)        # se buffer contem GTH, carrega G, primeiro caracter

    # Check for "GTH" exact (3 chars)
    li      t4, 3
    bne     t2, t4, .check_2char     t2 diferente de 3, não é GHT
    lbu     t4, 1(s0)       #carrega o segundo caracter
    lbu     t5, 2(s0)       #carrega o terceiro caracter
    li      t6, 'G'
    bne     t3, t6, .check_2char      #t3 diferente de G, check char
    li      t6, 'T'
    bne     t4, t6, .check_2char
    li      t6, 'H'
    bne     t5, t6, .check_2char

    # Valid "GTH" — write to cmd (ignore n)
    li      t4, 'G'
    sb      t4, 0(a2)  #Escreve G em cmd[0]
    li      t4, 'T'
    sb      t4, 1(a2)
    li      t4, 'H'
    sb      t4, 2(a2)
    sb      zero, 3(a2)     #escreve /0 em cmd[3]
    li      a0, 1
    j       .exit

.check_2char:
    li      t4, 2
    bne     t2, t4, .error

    lbu     t4, 1(s0)        # second char

    # Branch on first char (t3). Accept:
    #  - R with second E or B
    #  - Y with second E
    #  - G with second E
    li      t5, 'R'
    beq     t3, t5, .check_r        #Contem priemiro caracter do comnado linha 82
    li      t5, 'Y'
    beq     t3, t5, .check_ye
    li      t5, 'G'
    beq     t3, t5, .check_ge
    j       .error

.check_r:
    li      t5, 'E'
    beq     t4, t5, .valid_2char   #Segundo caracter dom comando linha 111
    li      t5, 'B'
    beq     t4, t5, .valid_2char
    j       .error

.check_ye:
    li      t5, 'E'
    beq     t4, t5, .valid_2char
    j       .error

.check_ge:
    li      t5, 'E'
    beq     t4, t5, .valid_2char
    j       .error

.valid_2char:
    # Verify n in 0..99
    bltz    a1, .error      #n menor que 0, error
    li      t5, 99
    bgt     a1, t5, .error  #n maior que 99, error

    # Compute tens and units by simple subtraction (no div instruction)
    li      t5, 10       # divisor
    mv      t6, a1       # remainder/temp (will become units)
    li      t4, 0        #  contador dezenas

.div_loop:
    blt     t6, t5, .div_done    #t6 menor que 10, div done
    sub     t6, t6, t5      #t6 = n - 10
    addi    t4, t4, 1       #contador dezenas ++
    j       .div_loop
.div_done:
    # t4 = tens (0..9), t6 = units (0..9)

    # Write "CMD,DD" to a2
    lbu     t0, 1(s0)        # segundo caracter do buffer em t0
    sb      t3, 0(a2)        # armazena a primeira letra em t3
    sb      t0, 1(a2)        # segunda letra
    li      t0, ','          # carrega uma virgula em t0
    sb      t0, 2(a2)        # terceira letra
    li      t0, '0'          # carrega(adiciona) 0 em t0
    add     t1, t0, t4       # t1 = '0' + contador dezenas
    sb      t1, 3(a2)        # primeiro numero
    add     t1, t0, t6       # t1 = '0' + units
    sb      t1, 4(a2)        # segundo numero
    sb      zero, 5(a2)      #zero para acabar
    li      a0, 1
    j       .exit

.error:
    beqz    a2, .exit       # a2 igual a null, exit
    sb      zero, 0(a2)     # se não for escreve 0 em cmd
    li      a0, 0

.exit:
    lw      s0, 24(sp)
    lw      ra, 28(sp)
    addi    sp, sp, 32
    ret
