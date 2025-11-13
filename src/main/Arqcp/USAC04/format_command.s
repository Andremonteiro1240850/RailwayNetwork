    .section .text
    .align  2
    .global format_command
    .type   format_command, @function

# int format_command(char *op, int n, char *cmd)
# a0 -> op
# a1 -> n
# a2 -> cmd
# return in a0 (1 ok, 0 erro)

format_command:
    addi    sp, sp, -64
    sw      ra, 60(sp)
    sw      s0, 56(sp)
    mv      s0, sp

    beqz    a0, .err_null
    beqz    a2, .err_null

    # trim left: find first ch >= 0x21
    mv      t0, a0
.trim_left:
    lbu     t1, 0(t0)
    beqz    t1, .err_empty
    li      t2, 0x21
    blt     t1, t2, .inc_left
    j       .found_start
.inc_left:
    addi    t0, t0, 1
    j       .trim_left

.found_start:
    # find end (point to '\0')
    mv      t3, t0
.find_end:
    lbu     t1, 0(t3)
    beqz    t1, .got_end
    addi    t3, t3, 1
    j       .find_end
.got_end:
    # backtrim whitespace
.backtrim:
    beq     t3, t0, .err_empty
    addi    t4, t3, -1
    lbu     t1, 0(t4)
    li      t2, 0x21
    blt     t1, t2, .dec_end
    j       .have_len
.dec_end:
    mv      t3, t4
    j       .backtrim

.have_len:
    sub     t5, t3, t0    # length
    beqz    t5, .err_empty
    li      t6, 16
    ble     t5, t6, .copy_tmp
    j       .err_too_long

.copy_tmp:
    mv      t7, zero
.copy_loop:
    beq     t7, t5, .null_tmp
    add     t8, t0, t7
    lbu     t1, 0(t8)
    # uppercase if 'a'..'z'
    li      t9, 'a'
    blt     t1, t9, .storech
    li      t9, 'z'
    bgt     t1, t9, .storech
    addi    t1, t1, -0x20
.storech:
    add     t8, s0, t7
    sb      t1, 0(t8)
    addi    t7, t7, 1
    j       .copy_loop
.null_tmp:
    add     t8, s0, t7
    sb      zero, 0(t8)

    # check GTH (len == 3)
    li      t1, 3
    bne     t5, t1, .check_len2
    lb      t2, 0(s0)
    li      t3, 'G'
    bne     t2, t3, .check_len2
    lb      t2, 1(s0)
    li      t3, 'T'
    bne     t2, t3, .check_len2
    lb      t2, 2(s0)
    li      t3, 'H'
    bne     t2, t3, .check_len2
    # write "GTH"
    sb      zero, 0(a2)     # safe start
    li      t4, 'G'
    sb      t4, 0(a2)
    li      t4, 'T'
    sb      t4, 1(a2)
    li      t4, 'H'
    sb      t4, 2(a2)
    sb      zero, 3(a2)
    li      a0, 1
    j       .epilogue

.check_len2:
    li      t1, 2
    bne     t5, t1, .unknown
    lb      t2, 0(s0)
    lb      t3, 1(s0)
    # compare allowed pairs: RE YE GE RB
    li      t4, 'R'
    li      t5, 'E'
    beq     t2, t4, .cmp_RE
    li      t4, 'Y'
    beq     t2, t4, .cmp_YE
    li      t4, 'G'
    beq     t2, t4, .cmp_GE
    li      t4, 'R'
    beq     t2, t4, .cmp_RB   # RB share R with RE; but RE checked above
    j       .unknown

.cmp_RE:
    li      t5, 'E'
    beq     t3, t5, .two_ok
    j       .unknown
.cmp_YE:
    li      t5, 'E'
    beq     t3, t5, .two_ok
    j       .unknown
.cmp_GE:
    li      t5, 'E'
    beq     t3, t5, .two_ok
    j       .unknown
.cmp_RB:
    li      t5, 'B'
    beq     t3, t5, .two_ok
    j       .unknown

.two_ok:
    # check n in 0..99
    mv      t6, a1
    bltz    t6, .err_nrange
    li      t7, 99
    bgt     t6, t7, .err_nrange
    # div/rem by 10 (uses M extension). If unavailable, could use loop.
    li      t8, 10
    div     t9, t6, t8
    rem     t10, t6, t8
    # write CMD,xx
    sb      t2, 0(a2)
    sb      t3, 1(a2)
    li      t11, ','
    sb      t11, 2(a2)
    li      t11, '0'
    add     t11, t11, t9
    sb      t11, 3(a2)
    li      t11, '0'
    add     t11, t11, t10
    sb      t11, 4(a2)
    sb      zero, 5(a2)
    li      a0, 1
    j       .epilogue

.unknown:
    sb      zero, 0(a2)
    li      a0, 0
    j       .epilogue

.err_nrange:
    sb      zero, 0(a2)
    li      a0, 0
    j       .epilogue

.err_too_long:
    sb      zero, 0(a2)
    li      a0, 0
    j       .epilogue

.err_empty:
    sb      zero, 0(a2)
    li      a0, 0
    j       .epilogue

.err_null:
    li      a0, 0
    j       .epilogue

.epilogue:
    lw      ra, 60(sp)
    lw      s0, 56(sp)
    addi    sp, sp, 64
    ret
