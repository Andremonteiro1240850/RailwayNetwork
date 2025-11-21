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
    addi    s0, sp, 8

    # PHASE 1: Process and convert to uppercase, trim leading whitespace
    mv      t0, a0           # src pointer (input)
    mv      t1, s0           # dest pointer (buffer on stack)
    li      t2, 0            # length counter (chars copied)
    li      t3, 0            # found_non_space flag (0 = only whitespace so far)

.process_input:
    lbu     t4, 0(t0)        # load byte from src
    beqz    t4, .process_done

    # Check if whitespace (any char <= 0x20)
    li      t5, 0x20
    ble     t4, t5, .is_whitespace
    # not whitespace
    li      t3, 1            # found non-space
    # Convert to uppercase if lowercase 'a'..'z'
    li      t5, 'a'
    blt     t4, t5, .store_char
    li      t5, 'z'
    bgt     t4, t5, .store_char
    addi    t4, t4, -32      # to uppercase

.store_char:
    sb      t4, 0(t1)
    addi    t1, t1, 1
    addi    t2, t2, 1
    li      t5, 16
    bge     t2, t5, .error   # too long (more than 15 chars)
    addi    t0, t0, 1
    j       .process_input

.is_whitespace:
    # IGNORE ALL whitespace (leading, middle, trailing)
    addi    t0, t0, 1
    j       .process_input

.process_done:
    beqz    t2, .error       # empty string after trimming
    beqz    t3, .error       # only whitespace

    # PHASE 2: Remove trailing whitespace
    mv      t0, s0           # start of buffer
    add     t1, t0, t2       # t1 points after last copied char

.remove_trailing:
    beq     t1, t0, .all_whitespace
    addi    t1, t1, -1
    lbu     t3, 0(t1)
    li      t4, 0x20
    ble     t3, t4, .remove_trailing
    # t1 now points at last non-whitespace char
    addi    t1, t1, 1
    sb      zero, 0(t1)      # null terminate
    sub     t2, t1, t0       # new length
    j       .check_commands

.all_whitespace:
    j       .error

.check_commands:
    # t2 = final length
    lbu     t3, 0(s0)        # first char of trimmed uppercased input

    # Check for "GTH" exact (3 chars)
    li      t4, 3
    bne     t2, t4, .check_2char
    lbu     t4, 1(s0)
    lbu     t5, 2(s0)
    li      t6, 'G'
    bne     t3, t6, .check_2char
    li      t6, 'T'
    bne     t4, t6, .check_2char
    li      t6, 'H'
    bne     t5, t6, .check_2char

    # Valid "GTH" — write to cmd (ignore n)
    li      t4, 'G'
    sb      t4, 0(a2)
    li      t4, 'T'
    sb      t4, 1(a2)
    li      t4, 'H'
    sb      t4, 2(a2)
    sb      zero, 3(a2)
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
    beq     t3, t5, .check_r
    li      t5, 'Y'
    beq     t3, t5, .check_ye
    li      t5, 'G'
    beq     t3, t5, .check_ge
    j       .error

.check_r:
    li      t5, 'E'
    beq     t4, t5, .valid_2char
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
    bltz    a1, .error
    li      t5, 99
    bgt     a1, t5, .error

    # Compute tens and units by simple subtraction (no div instruction)
    li      t5, 10       # divisor
    mv      t6, a1       # remainder/temp (will become units)
    li      t4, 0        # tens = 0

.div_loop:
    blt     t6, t5, .div_done
    sub     t6, t6, t5
    addi    t4, t4, 1
    j       .div_loop
.div_done:
    # t4 = tens (0..9), t6 = units (0..9)

    # Write "CMD,DD" to a2
    lbu     t0, 1(s0)        # second character (uppercase)
    sb      t3, 0(a2)        # first letter
    sb      t0, 1(a2)        # second letter
    li      t0, ','
    sb      t0, 2(a2)
    li      t0, '0'
    add     t1, t0, t4       # t1 = '0' + tens
    sb      t1, 3(a2)
    add     t1, t0, t6       # t1 = '0' + units
    sb      t1, 4(a2)
    sb      zero, 5(a2)
    li      a0, 1
    j       .exit

.error:
    beqz    a2, .exit
    sb      zero, 0(a2)
    li      a0, 0

.exit:
    lw      s0, 24(sp)
    lw      ra, 28(sp)
    addi    sp, sp, 32
    ret
