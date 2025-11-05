.section .text
.global dequeue_value
dequeue_value:
li t0, 0
li t1, 1

lw t7, 0(a2)        # carrega o valor de nelemm
beqz t7, return_zero

lw t2, 0(a3)        # carrega o valor de tail em t2

slli t3, t2, 2      # multiplica tail por 4 (tamanho de cada elemento)
add t4, a0, t3      # calcula o endereço do elemento
lw t5, 0(t4)        # carrega o valor do elemento
sw t5, 0(a5)        # guarda o valor removido em *value

add t2, t2, t1      # tail + 1
rem t6, t2, a1      # tail = (tail + 1) % capacity
sw t6, 0(a3)        # atualiza o valor de tail

sub t7, t7, t1      # nelem - 1
sw t7, 0(a2)        # atualiza o valor de nelem
j return_one


return_zero:
mv a0, t0
jr ra

return_one:
mv a0, t1
jr ra


