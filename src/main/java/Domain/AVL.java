package Domain;

import java.util.Comparator;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *

/**
 *
 * @author DEI-ESINF
 * @param <E>
 */
public class AVL <E extends Comparable<E>> extends BST<E> {

    public AVL() {
        super();
    }
    public AVL(Comparator<E> comparator) {
        super(comparator);
    }

    private int balanceFactor(Node<E> node){
        if (node == null)
            return 0;
        return height(node.getLeft()) - height(node.getRight());
    }

    private Node<E> rightRotation(Node<E> node){
        Node<E> temp = node.getLeft();
        node.setLeft(temp.getRight());
        temp.setRight(node);
        return temp;

    }

    private Node<E> leftRotation(Node<E> node){
        Node<E> temp = node.getRight();
        node.setRight(temp.getLeft());
        temp.setLeft(node);
        return temp;

    }

    private Node<E> twoRotations(Node<E> node){

        if (balanceFactor(node) > 1) {
            node.setLeft( rightRotation(node.getLeft()) );
            return leftRotation(node);
        }
        else {
            node.setRight( leftRotation(node.getRight()) );
            return rightRotation(node);
        }
    }

    private Node<E> balanceNode(Node<E> node)
    {

        int balance = balanceFactor(node);
        if (balance > 1 || balance < -1) {
            node = twoRotations(node);
        }
        return node;
    }

    @Override
    public void insert(E element){
        root = insert(element, root);
    }
    private Node<E> insert(E element, Node<E> node){

        if (node == null) {
            node = new Node<>(element, null, null);
        }
        else if (element.compareTo(node.getElement()) < 0) {
            node.setLeft( insert(element, node.getLeft()) );
            node = balanceNode(node);
        }
        else {
            node.setRight( insert(element, node.getRight()) );
            node = balanceNode(node);
        }
        return node;
    }

    @Override
    public void remove(E element){
        root = remove(element, root());
    }

    private Node<E> remove(E element, Node<E> node) {
        if (node == null) {
            return null;
        }
        if (element.compareTo(node.getElement())==0) { // node is the Node to be removed

            if (node.getLeft() == null && node.getRight() == null) { //node is a leaf (has no childs)
                return null;
            }
            if (node.getLeft() == null) {   //has only right child
                return node.getRight();
            }
            if (node.getRight() == null) {  //has only left child
                return node.getLeft();
            }
            //has two child trees
            //replace the elem with the smallest of right subtree and remove it
            E smallElem = smallestElement(node.getRight());
            node.setElement(smallElem);
            node.setRight( remove(smallElem, node.getRight()) );
            node = balanceNode(node);
        }
        else if (element.compareTo(node.getElement()) < 0) {
            node.setLeft( remove(element, node.getLeft()) );
            node = balanceNode(node);
        }
        else {
            node.setRight( remove(element, node.getRight()) );
            node = balanceNode(node);
        }
        return node;
    }


    public boolean equals(Object otherObj) {

        if (this == otherObj)
            return true;

        if (otherObj == null || this.getClass() != otherObj.getClass())
            return false;

        AVL<E> second = (AVL<E>) otherObj;
        return equals(root, second.root);
    }

    public boolean equals(Node<E> root1, Node<E> root2) {
        if (root1 == null && root2 == null)
            return true;
        else if (root1 != null && root2 != null) {
            if (root1.getElement().compareTo(root2.getElement()) == 0) {
                return equals(root1.getLeft(), root2.getLeft())
                        && equals(root1.getRight(), root2.getRight());
            } else
                return false;
        }
        else return false;
    }

}