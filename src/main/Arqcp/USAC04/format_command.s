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

    # PHASE 1: Process and convert to uppercase
    mv      t0, a0           # src pointer
    mv      t1, s0           # dest pointer
    li      t2, 0            # length counter
    li      t3, 0            # found_non_space flag (0 = only whitespace so far)

.process_input:
    lbu     t4, 0(t0)
    beqz    t4, .process_done

    # Check if whitespace (any char ≤ 0x20)
    li      t5, 0x20
    ble     t4, t5, .is_whitespace
    j       .not_whitespace

.is_whitespace:
    # Only copy whitespace if we already found non-whitespace chars
    beqz    t3, .next_char    # ignore leading whitespace
    j       .store_char       # keep trailing whitespace temporarily

.not_whitespace:
    # First non-whitespace character - set flag
    li      t3, 1

    # Convert to uppercase if lowercase
    li      t5, 'a'
    blt     t4, t5, .store_char
    li      t5, 'z'
    bgt     t4, t5, .store_char
    addi    t4, t4, -32      # convert to uppercase

.store_char:
    sb      t4, 0(t1)
    addi    t1, t1, 1
    addi    t2, t2, 1
    li      t5, 16
    bge     t2, t5, .error    # too long (more than 15 chars)

.next_char:
    addi    t0, t0, 1
    j       .process_input

.process_done:
    beqz    t2, .error       # empty string after initial trimming
    beqz    t3, .error       # only contained whitespace (flag never set)

    # PHASE 2: Remove trailing whitespace
    mv      t0, s0           # start of buffer
    add     t1, t0, t2       # t1 points after last character

.remove_trailing:
    beq     t1, t0, .all_whitespace  # backtracked to start = only whitespace

    addi    t1, t1, -1       # move back one character
    lbu     t3, 0(t1)

    # Check if whitespace (char ≤ 0x20)
    li      t4, 0x20
    ble     t3, t4, .remove_trailing  # still whitespace, continue

    # t1 now points to last non-whitespace character
    addi    t1, t1, 1
    sb      zero, 0(t1)      # null terminate after last non-whitespace char
    sub     t2, t1, t0       # new actual length
    j       .check_commands

.all_whitespace:
    j       .error           # string contained only whitespace

.check_commands:
    # t2 has final length without leading/trailing whitespace
    lbu     t3, 0(s0)        # first character

    # Check GTH (exactly 3 characters)
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

    # Valid GTH - write result (ignore n)
    li      t4, 'G'
    sb      t4, 0(a2)
    li      t4, 'T'
    sb      t4, 1(a2)
    li      t4, 'H'
    sb      t4, 2(a2)
    sb      zero, 3(a2)      # null terminator
    li      a0, 1
    j       .exit

.check_2char:
    li      t4, 2
    bne     t2, t4, .error

    lbu     t4, 1(s0)        # second character

    # Check valid 2-character commands
    li      t5, 'R'
    li      t6, 'E'
    beq     t3, t5, .check_re
    li      t5, 'Y'
    li      t6, 'E'
    beq     t3, t5, .check_ye
    li      t5, 'G'
    li      t6, 'E'
    beq     t3, t5, .check_ge
    li      t5, 'R'
    li      t6, 'B'
    beq     t3, t5, .check_rb
    j       .error

.check_re:
    beq     t4, t6, .valid_2char
    j       .error
.check_ye:
    beq     t4, t6, .valid_2char
    j       .error
.check_ge:
    beq     t4, t6, .valid_2char
    j       .error
.check_rb:
    beq     t4, t6, .valid_2char
    j       .error

.valid_2char:
    # Verify n is between 0-99
    bltz    a1, .error
    li      t5, 99
    bgt     a1, t5, .error

    # --- SOFTWARE DIVISION (no M extension) ---
    # Divide n by 10 to get tens and units digits
    li      t5, 10           # divisor
    mv      t6, a1           # dividend (n)
    li      t7, 0            # quotient (tens)

.div_loop:
    blt     t6, t5, .div_done
    sub     t6, t6, t5       # remainder = remainder - divisor
    addi    t7, t7, 1        # quotient++
    j       .div_loop
.div_done:
    # t7 = tens digit (0-9), t6 = units digit (0-9)
    # --- END DIVISION ---

    # Write command in format "CMD,DD"
    sb      t3, 0(a2)        # first letter
    sb      t4, 1(a2)        # second letter
    li      t5, ','
    sb      t5, 2(a2)        # comma
    li      t5, '0'
    add     t8, t5, t7       # tens digit as char
    sb      t8, 3(a2)
    add     t8, t5, t6       # units digit as char
    sb      t8, 4(a2)
    sb      zero, 5(a2)      # null terminator
    li      a0, 1
    j       .exit

.error:
    # On error, ensure cmd is empty string
    beqz    a2, .exit
    sb      zero, 0(a2)
    li      a0, 0
    
.exit:
    lw      s0, 24(sp)
    lw      ra, 28(sp)
    addi    sp, sp, 32
    ret