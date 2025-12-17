    .section .text
    .globl dequeue_value
# a0=buffer, a1=length, a2=&nelem, a3=&tail, a4=&head, a5=&value
dequeue_value:
    # if (*nelem == 0) return 0;
    lw      t0, 0(a2)          # t0 = *nelem
    beqz    t0, .empty

    # idx = *head
    lw      t1, 0(a3)          # t1 = *head

    # *value = buffer[idx]
    slli    t2, t1, 2          # t2 = idx * 4  (int = 4 bytes)
    add     t3, a0, t2         # t3 = &buffer[idx]
    lw      t4, 0(t3)          # t4 = buffer[idx]
    sw      t4, 0(a5)          # *value = t4

    # *head = (idx + 1) % length
    addi    t1, t1, 1          # t1 = idx + 1
    bne     t1, a1, .no_wrap   # if (t1 != length) skip reset
    li      t1, 0              # t1 = 0 (wrap)
.no_wrap:
    sw      t1, 0(a3)          # *head = t1

    # *nelem -= 1
    addi    t0, t0, -1
    sw      t0, 0(a2)

    # return 1 (success)
    li      a0, 1
    ret

.empty:
    li      a0, 0
    ret
