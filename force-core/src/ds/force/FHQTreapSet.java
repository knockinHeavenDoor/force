package ds.force;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Random;
import java.util.SortedSet;

public class FHQTreapSet<E> implements NavigableSet<E> {

    /**
     * The comparator used to maintain order in this tree map, or
     * null if it uses the natural ordering of its keys.
     *
     * @serial
     */
    private final Comparator<? super E> comparator;

    transient Random random = new Random();

    transient Node<E> root;

    /**
     * The number of entries in the treap
     */
    private int size = 0;

    public FHQTreapSet(){
        comparator = null;
    }

    public FHQTreapSet(Comparator<? super E> comparator){
        this.comparator = comparator;
    }

    public Node<E> split(E item){
        return comparator == null ? splitComparable(item) : splitUsingComparator(item);
    }

    @SuppressWarnings("unchecked")
    private Node<E> splitComparable(E item){
        Node<E> node = this.root;
        Node<E> newNode = null, splitPoint = null;
        Deque<Node<E>> stack = new ArrayDeque<>();
        while (node != null){
            stack.push(node);
            if (((Comparable<? super E>)node.element).compareTo(item) <= 0){
                node = node.right;
            } else {
                node = node.left;
            }
        }
        while (!stack.isEmpty()){
            Node<E> current = stack.pop();
            if (((Comparable<? super E>)current.element).compareTo(item) <= 0){
                current.right = splitPoint;
                splitPoint = current;
            } else {
                current.left = newNode;
                newNode = current;
            }
            update(current);
        }
        return newNode == root ? splitPoint : newNode;
    }

    private Node<E> splitUsingComparator(E item){
        Node<E> node = this.root;
        Node<E> newNode = null, splitPoint = null;
        Deque<Node<E>> stack = new ArrayDeque<>();
        while (node != null){
            stack.push(node);
            if (comparator.compare(node.element,item) <= 0){
                node = node.right;
            } else {
                node = node.left;
            }
        }
        while (!stack.isEmpty()){
            Node<E> current = stack.pop();
            if (comparator.compare(current.element,item) <= 0){
                current.right = splitPoint;
                splitPoint = current;
            } else {
                current.left = newNode;
                newNode = current;
            }
            update(current);
        }
        return newNode == root ? splitPoint : newNode;
    }

    private boolean merge1(Node<E> a, Node<E> b){
        if (a == null || b == null) return true;
        Node<E> small, big;
        if (compareTo(a.element,b.element) <= 0){
            small = a;big = b;
        } else{
            small = b;big = a;
        }
        Deque<Node<E>> stack = new ArrayDeque<>();
        while(small != null && big != null){
            if (compareTo(small.element,big.element) == 0) return false;
            if (small.priority < big.priority){
                stack.push(small);
                small = small.right;
            } else {
                stack.push(big);
                big = big.left;
            }
        }
        Node<E> child = small == null ? big : small;
        while (!stack.isEmpty()){
            Node<E> node = stack.pop();
            if (child.priority < node.priority){
                node.right = child;
            } else {
                node.left = child;
            }
            update(node);
            child = node;
        }
        this.root = child;
        return true;
    }

    public Node<E> merge(Node<E> a, Node<E> b){
        if (a == null || b == null) return a == null ? b : a;
        if (a.priority < b.priority){
            a.right = merge(a.right,b);
            update(a);
            return a;
        } else {
            b.left = merge(a,b.left);
            update(b);
            return b;
        }
    }

    @SuppressWarnings("unchecked")
    private int compareTo(E a, E b){
        if (comparator == null){
            return ((Comparable<? super E>)a).compareTo(b);
        } else {
            return comparator.compare(a,b);
        }
    }

    private void update(Node<E> node){
        node.size = sizeOf(node.left) + sizeOf(node.right) + 1;
    }

    private static <E> int sizeOf(Node<E> node){
        return node == null ? 0 : node.size;
    }

    @Override
    public E lower(E e) {
        return null;
    }

    @Override
    public E floor(E e) {
        return null;
    }

    @Override
    public E ceiling(E e) {
        return null;
    }

    @Override
    public E higher(E e) {
        return null;
    }

    @Override
    public E pollFirst() {
        return null;
    }

    @Override
    public E pollLast() {
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return null;
    }

    @Override
    public boolean add(E e) {
        Node<E> t = root;
        if (t == null){
            compareTo(e, e); // type (and possibly null) check

            root = new Node<>(e, random.nextInt(1000));
            size = 1;
            return true;
        }
        Node<E> splittingNode = split(e);
        Node<E> newNode = new Node<>(e, random.nextInt(1000));
//        this.root = merge(this.root,newNode);
//        if (!result) return false;
//        this.root = merge(this.root,splittingNode);
        boolean result = merge1(this.root,newNode);
        result &= merge1(this.root,splittingNode);
        return result;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public NavigableSet<E> descendingSet() {
        return null;
    }

    @Override
    public Iterator<E> descendingIterator() {
        return null;
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return null;
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return null;
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return null;
    }

    @Override
    public Comparator<? super E> comparator() {
        return null;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return null;
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return null;
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return null;
    }

    @Override
    public E first() {
        return null;
    }

    @Override
    public E last() {
        return null;
    }

    static class Node<E> {

        E element;

        int priority;

        Node<E> left,right;

        int size;

        Node(E element,int priority){
            this.element = element;
            this.priority = priority;
            this.size = 1;
        }

        public void setElement(E element) {
            this.element = element;
        }
    }
}
