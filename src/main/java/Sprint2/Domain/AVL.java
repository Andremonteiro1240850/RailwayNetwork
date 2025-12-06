package Sprint2.Domain;

import java.util.Comparator;

public class AVL<E extends Comparable<E>> extends BST<E> {

    public AVL() {
        super();
    }

    public AVL(Comparator<E> comparator) {
        super(comparator);
    }
    public Node<E> getRoot() {
        return root;
    }


    private int balanceFactor(Node<E> node) {
        if (node == null) return 0;
        return height(node.getLeft()) - height(node.getRight());
    }

    private Node<E> rightRotation(Node<E> node) {
        Node<E> temp = node.getLeft();
        node.setLeft(temp.getRight());
        temp.setRight(node);
        return temp;
    }

    private Node<E> leftRotation(Node<E> node) {
        Node<E> temp = node.getRight();
        node.setRight(temp.getLeft());
        temp.setLeft(node);
        return temp;
    }

    private Node<E> balanceNode(Node<E> node) {
        int balance = balanceFactor(node);

        // Left-heavy (LL or LR)
        if (balance > 1) {
            if (balanceFactor(node.getLeft()) < 0) {
                // LR case → rotate left first
                node.setLeft(leftRotation(node.getLeft()));
            }
            return rightRotation(node);
        }

        // Right-heavy (RR or RL)
        if (balance < -1) {
            if (balanceFactor(node.getRight()) > 0) {
                // RL case → rotate right first
                node.setRight(rightRotation(node.getRight()));
            }
            return leftRotation(node);
        }

        return node;
    }

    @Override
    public void insert(E element) {
        root = insert(element, root);
    }

    private Node<E> insert(E element, Node<E> node) {
        if (node == null) {
            return new Node<>(element, null, null);
        }

        // USAR O COMPARATOR DO BST
        int cmp = compare(element, node.getElement());

        if (cmp < 0) {
            node.setLeft(insert(element, node.getLeft()));
        }
        else {
            node.setRight(insert(element, node.getRight()));
        }

        return balanceNode(node);
    }

    @Override
    public void remove(E element) {
        root = remove(element, root);
    }

    private Node<E> remove(E element, Node<E> node) {
        if (node == null) return null;

        int cmp = compare(element, node.getElement());

        if (cmp == 0) {
            // no children
            if (node.getLeft() == null && node.getRight() == null) {
                return null;
            }

            // only right
            if (node.getLeft() == null) {
                return node.getRight();
            }

            // only left
            if (node.getRight() == null) {
                return node.getLeft();
            }

            // 2 children → substitute
            E min = smallestElement(node.getRight());
            node.setElement(min);
            node.setRight(remove(min, node.getRight()));
        }
        else if (cmp < 0) {
            node.setLeft(remove(element, node.getLeft()));
        }
        else {
            node.setRight(remove(element, node.getRight()));
        }

        return balanceNode(node);
    }
}
