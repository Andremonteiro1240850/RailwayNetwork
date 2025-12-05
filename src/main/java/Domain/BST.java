package Domain;

import Interfaces.BSTInterface;
import java.util.*;

public class BST<E extends Comparable<E>> implements BSTInterface<E> {
    protected Comparator<E> comparator;
    protected Node<E> root = null;

    public static class Node<E> {
        private E element;
        private Node<E> left;
        private Node<E> right;

        public Node(E e, Node<E> leftChild, Node<E> rightChild) {
            element = e;
            left = leftChild;
            right = rightChild;
        }

        public E getElement() { return element; }
        public Node<E> getLeft() { return left; }
        public Node<E> getRight() { return right; }

        public void setElement(E e) { element = e; }
        public void setLeft(Node<E> leftChild) { left = leftChild; }
        public void setRight(Node<E> rightChild) { right = rightChild; }
    }

    public BST() {
        this.comparator = null;
    }

    public BST(Comparator<E> comparator) {
        this.comparator = comparator;
    }

    /** Método ESSENCIAL — usa comparator se existir */
    protected int compare(E a, E b) {
        if (comparator != null)
            return comparator.compare(a, b);
        return a.compareTo(b);
    }

    protected Node<E> root() { return root; }

    public boolean isEmpty() { return root == null; }

    protected Node<E> find(Node<E> node, E element) {
        if (node == null) return null;

        int cmp = compare(element, node.getElement());

        if (cmp == 0) return node;
        if (cmp < 0) return find(node.getLeft(), element);
        return find(node.getRight(), element);
    }

    public void insert(E element) {
        root = insert(element, root);
    }

    private Node<E> insert(E element, Node<E> node) {
        if (node == null) {
            return new Node<>(element, null, null);
        }

        int cmp = compare(element, node.getElement());

        if (cmp < 0)
            node.setLeft(insert(element, node.getLeft()));
        else if (cmp > 0)
            node.setRight(insert(element, node.getRight()));

        return node;
    }

    public void remove(E element) {
        root = remove(element, root);
    }

    private Node<E> remove(E element, Node<E> node) {
        if (node == null) return null;

        int cmp = compare(element, node.getElement());

        if (cmp == 0) {
            if (node.getLeft() == null && node.getRight() == null)
                return null;

            if (node.getLeft() == null)
                return node.getRight();

            if (node.getRight() == null)
                return node.getLeft();

            // 2 children
            E min = smallestElement(node.getRight());
            node.setElement(min);
            node.setRight(remove(min, node.getRight()));
        }
        else if (cmp < 0)
            node.setLeft(remove(element, node.getLeft()));
        else
            node.setRight(remove(element, node.getRight()));

        return node;
    }

    public int size() { return size(root); }

    private int size(Node<E> node) {
        if (node == null) return 0;
        return 1 + size(node.getLeft()) + size(node.getRight());
    }

    public int height() { return height(root); }

    protected int height(Node<E> node) {
        if (node == null) return 0;
        return 1 + Math.max(height(node.getLeft()), height(node.getRight()));
    }

    public E smallestElement() { return smallestElement(root); }

    protected E smallestElement(Node<E> node) {
        if (node == null) return null;
        while (node.getLeft() != null)
            node = node.getLeft();
        return node.getElement();
    }

    public Iterable<E> inOrder() {
        List<E> snapshot = new ArrayList<>();
        inOrderSubtree(root, snapshot);
        return snapshot;
    }

    private void inOrderSubtree(Node<E> node, List<E> snapshot) {
        if (node == null) return;
        inOrderSubtree(node.getLeft(), snapshot);
        snapshot.add(node.getElement());
        inOrderSubtree(node.getRight(), snapshot);
    }
/**
     * Returns an iterable collection of elements of the tree, reported in
     * pre-order.
     *
     * @return iterable collection of the tree's elements reported in pre-order
     */
    public Iterable<E> preOrder() {

        List<E> snapshot = new ArrayList<>();

        if (root != null) {
            preOrderSubtree(root, snapshot);   // fill the snapshot recursively
        }
        return snapshot;
    }

    /**
     * Adds elements of the subtree rooted at Node node to the given snapshot
     * using an pre-order traversal
     *
     * @param node Node serving as the root of a subtree
     * @param snapshot a list to which results are appended
     */
    private void preOrderSubtree(Node<E> node, List<E> snapshot) {

        if (node == null) {
            return;
        }

        snapshot.add(node.getElement());
        preOrderSubtree(node.getLeft(), snapshot);
        preOrderSubtree(node.getRight(), snapshot);
    }

    /**
     * Returns an iterable collection of elements of the tree, reported in
     * post-order.
     *
     * @return iterable collection of the tree's elements reported in post-order
     */
    public Iterable<E> posOrder() {

        List<E> snapshot = new ArrayList<>();

        if (root != null) {
            posOrderSubtree(root, snapshot);   // fill the snapshot recursively
        }
        return snapshot;
    }

    /**
     * Adds positions of the subtree rooted at Node node to the given snapshot
     * using an post-order traversal
     *
     * @param node Node serving as the root of a subtree
     * @param snapshot a list to which results are appended
     */
    private void posOrderSubtree(Node<E> node, List<E> snapshot) {

        if (node == null) {
            return;
        }

        posOrderSubtree(node.getLeft(), snapshot);
        posOrderSubtree(node.getRight(), snapshot);
        snapshot.add(node.getElement());
    }


    /*
     * Returns a map with a list of nodes by each tree level.
     * @return a map with a list of nodes by each tree level
     */
    public Map<Integer,List<E>> nodesByLevel(){
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void processBstByLevel(Node<E> node, Map<Integer,List<E>> result, int level){
        throw new UnsupportedOperationException("Not supported yet.");
    }

//#########################################################################

    /**
     * Returns a string representation of the tree.
     * Draw the tree horizontally
     */
    public String toString(){
        StringBuilder sb = new StringBuilder();
        toStringRec(root, 0, sb);
        return sb.toString();
    }

    private void toStringRec(Node<E> root, int level, StringBuilder sb){
        if(root==null)
            return;
        toStringRec(root.getRight(), level+1, sb);
        if (level!=0){
            for(int i=0;i<level-1;i++)
                sb.append("|\t");
            sb.append("|-------"+root.getElement()+"\n");
        }
        else
            sb.append(root.getElement()+"\n");
        toStringRec(root.getLeft(), level+1, sb);
    }

} //----------- end of BST class -----------






