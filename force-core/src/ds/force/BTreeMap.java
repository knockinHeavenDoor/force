package ds.force;

import ds.force.util.ArrayUtil;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.function.ToIntBiFunction;

public class BTreeMap<K,V> implements Map<K,V>{
    /**
     * default minmum degree
     */
    private static final int DEFAULT_DEGREE = 2;
    /**
     * minmum degree,min t-1,max 2t-1
     */
    private int degree;

    /**
     * The comparator used to maintain order in this map, or
     * null if it uses the natural ordering of its keys.
     *
     * @serial
     */
    private final Comparator<? super K> comparator;

    transient BTreeNode<K,V> root;

    private int size;

    public BTreeMap(){
        this.comparator = null;
        this.degree = DEFAULT_DEGREE;
        this.root = new BTreeNode<>(degree,null);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean containsKey(Object key) {
        BTreeNode<K,V> target;
        K k = (K) key;
        if (comparator != null)
            target = getNode(k,comparator::compare);
        else target = getNode(k,(k1, k2) -> ((Comparable<? super K>)k1).compareTo(k2));
        return target != null;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        BTreeNode<K,V> target;
        K k = (K) key;
        if (comparator != null)
            target = getNode(k,comparator::compare);
        else target = getNode(k,(k1, k2) -> ((Comparable<? super K>)k1).compareTo(k2));
        if (target != null) return getEntry(target,k).value;
        return null;
    }

    private BTreeNode<K,V> getNode(K key,final ToIntBiFunction<K,K> compare){
        BTreeNode<K,V> node = this.root;
        while(node != null){
            int index = 0;
            for (int i = index; i < node.keys.length; i++) {
                if (node.keys[i] == null) break;
                int cmp = compare.applyAsInt(key, node.keys[i].key);
                if (cmp < 0) break;
                else if (cmp > 0) index++;
                else return node;
            }
            node = node.childes[index];
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V put(K key, V value) {
       if (comparator != null)
           return putUsingFunction(key, value, comparator::compare);
       return putUsingFunction(key, value, (k, k2) -> ((Comparable<? super K>)k).compareTo(k2));
    }

    private V putUsingComparator(K key, V value){
        BTreeNode<K,V> node = this.root, preNode = this.root;
        int index = 0;
        while(node != null){
            index = 0;
            if (node.keys[node.keys.length-1] != null){
                int preIndex = split(node);
                node = node.parent;
                index = preIndex;
            }
            for (int i = 0; i < node.keys.length; i++) {
                if (node.keys[i] == null) break;
                int cmp = comparator.compare(key, node.keys[i].key);
                if (cmp < 0) break;
                else if (cmp > 0) index++;
            }
            preNode = node;
            node = node.childes[index];
        }
        NodeEntry<K,V> newNode = new NodeEntry<>(key,value);
        ArrayUtil.add(preNode.keys,index,newNode);
        size++;
        return null;
    }

    private V putUsingFunction(K key, V value, final ToIntBiFunction<K,K> compare){
        BTreeNode<K,V> node = this.root, preNode = this.root;
        int index = 0;
        while(node != null){
            index = 0;
            if (node.keys[node.keys.length-1] != null){
                int preIndex = split(node);
                node = preNode;
//                continue;
                index = preIndex;
            }
            for (int i = index; i < node.keys.length; i++) {
                if (node.keys[i] == null) break;
                int cmp = compare.applyAsInt(key, node.keys[i].key);
                if (cmp < 0) break;
                else if (cmp > 0) index++;
                else return node.keys[i].setValue(value);
            }
            preNode = node;
            node = node.childes[index];
        }
        NodeEntry<K,V> newNode = new NodeEntry<>(key,value);
        ArrayUtil.add(preNode.keys,index,newNode);
        size++;
        return null;
    }

    private V putComparable(K key, V value){
        BTreeNode<K,V> node = this.root, preNode = this.root;
        int index = 0;
        Comparable<? super K> k = (Comparable<? super K>) key;
        while(node != null){
            index = 0;
            if (node.keys[node.keys.length-1] != null){
                int preIndex = split(node);
                node = node.parent;
                index = preIndex;
            }
            for (int i = 0; i < node.keys.length; i++) {
                if (node.keys[i] == null) break;
                int cmp = k.compareTo(node.keys[i].key);
                if (cmp < 0) break;
                else if (cmp > 0) index++;
            }
            preNode = node;
            node = node.childes[index];
        }
        NodeEntry<K,V> newNode = new NodeEntry<>(key,value);
        ArrayUtil.add(preNode.keys,index,newNode);
        return null;
    }

    final int split(BTreeNode<K,V> node){
        BTreeNode<K,V> left = new BTreeNode<>(degree,null);
        int keysHalf = node.keys.length >> 1;
        int childesHalf = node.childes.length >> 1;
        System.arraycopy(node.keys, 0, left.keys, 0,
               keysHalf);
        System.arraycopy(node.childes, 0, left.childes, 0,
                childesHalf);
        BTreeNode<K,V> right = new BTreeNode<>(degree,null);
        System.arraycopy(node.keys, keysHalf+1, right.keys, 0,
                keysHalf);
        System.arraycopy(node.childes, keysHalf+1, right.childes, 0,
                childesHalf);
        NodeEntry<K,V> half = node.keys[keysHalf];
        for (int i = 0; i < childesHalf; i++){
            if (left.childes[i] != null) left.childes[i].parent = left;
            if (right.childes[i] != null) right.childes[i].parent = right;
        }
        if (node.parent == null){
            node.clear();
            node.keys[0] = half;
            node.childes[0] = left;
            node.childes[1] = right;
            left.parent = right.parent = node;
            return 0;
        }
        int insertIndex = keyPoint(node.parent,half.key);
        ArrayUtil.add(node.parent.keys,insertIndex,half);
        node.parent.childes[insertIndex] = null;
        ArrayUtil.addAll(node.parent.childes,insertIndex,new BTreeNode[]{left,right});
        left.parent = right.parent = node.parent;
        return insertIndex;
    }

    final void merge(BTreeNode<K,V> node,int keyPoint){
        NodeEntry<K,V> k = node.keys[keyPoint];
        BTreeNode<K,V> left = node.childes[keyPoint];
        BTreeNode<K,V> right = node.childes[keyPoint+1];
        ArrayUtil.remove(node.keys,keyPoint);
        System.arraycopy(right.keys, 0, left.keys, degree,
                degree-1);
        left.keys[degree-1] = k;
        System.arraycopy(right.childes, 0, left.childes, degree,
                degree);
        ArrayUtil.remove(node.childes,keyPoint+1);
    }

    @Override
    public V remove(Object key) {
        BTreeNode<K,V> target = null;
        final ToIntBiFunction<K,K> compare = null;
        K k = (K) key;
        BTreeNode<K,V> node = this.root;
        while(node != null){
            int index = 0;
            if (node != root && node.keys[degree-1] == null){
                int nodePoint = nodePoint(node);
                BTreeNode<K,V>[] childes = node.parent.childes;
                if (childes[nodePoint-1].keys[degree-1] != null){

                }
                else if (childes[nodePoint+1].keys[degree-1] != null){

                }
                else {

                }
            }
            for (int i = index; i < node.keys.length; i++) {
                if (node.keys[i] == null) break;
                int cmp = compare.applyAsInt(k, node.keys[i].key);
                if (cmp < 0) break;
                else if (cmp > 0) index++;
                else break;
            }
            node = node.childes[index];
        }
        if (target != null){
            int keyPoint = keyPoint(target,k);
            if (target.isLeaf()){

            } else {
                if (target.childes[keyPoint].keys[degree-1] != null){

                }
                else if (target.childes[keyPoint+1].keys[degree-1] != null){

                }
                else {
                    merge(target,keyPoint);
                }
            }
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {

    }

    @Override
    public void clear() {
        this.root.clear();
        this.size = 0;
    }

    @Override
    public Set<K> keySet() {
        return null;
    }

    @Override
    public Collection<V> values() {
        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }

    int nodePoint(BTreeNode<K,V> node){
        BTreeNode<K,V>[] childes = node.parent.childes;
        for (int i = 0; i < childes.length; i++) {
            if (childes[i] == node) return i;
        }
        return -1;
    }

    int keyPoint(BTreeNode<K,V> node, K key){
        if (comparator != null)
            return keyPointUsingComparator(node,key);
        return keyPointComparable(node,key);
    }

    private int keyPointComparable(BTreeNode<K,V> node, K key){
        int index = 0;
        @SuppressWarnings("unchecked")
        Comparable<? super K> k = (Comparable<? super K>) key;
        for (int i = 0; i < node.keys.length; i++) {
            if (node.keys[i] == null) break;
            int cmp = k.compareTo(node.keys[i].key);
            if (cmp < 0) break;
            else if (cmp > 0) index++;
            else break;
        }
        return index;
    }

    private int keyPointUsingComparator(BTreeNode<K,V> node, K key){
        int index = 0;
        for (int i = 0; i < node.keys.length; i++) {
            if (node.keys[i] == null) break;
            int cmp = comparator.compare(key, node.keys[i].key);
            if (cmp < 0) break;
            else if (cmp > 0) index++;
            else break;
        }
        return index;
    }

    private static class NodeEntry<K,V> implements Map.Entry<K,V> {

        K key;

        V value;

        NodeEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }
    }

    private NodeEntry<K,V> getEntry(BTreeNode<K,V> node,K key){
        if (comparator != null) return getEntryUsingComparator(node,key);
        return getEntryComparable(node,key);
    }

    private NodeEntry<K,V> getEntryComparable(BTreeNode<K,V> node,K key){
        @SuppressWarnings("unchecked")
        Comparable<? super K> k = (Comparable<? super K>) key;
        for (int i = 0; i < node.keys.length; i++) {
            if (key == node.keys[i].key || k.compareTo(node.keys[i].key) == 0)
                return node.keys[i];
        }
        return null;
    }

    private NodeEntry<K,V> getEntryUsingComparator(BTreeNode<K,V> node,K key){
        for (int i = 0; i < node.keys.length; i++) {
            if (key == node.keys[i].key || comparator.compare(node.keys[i].key,key) == 0)
                return node.keys[i];
        }
        return null;
    }

    private static class BTreeNode<K,V> {

        NodeEntry<K,V>[] keys;

        BTreeNode<K,V>[] childes;

        BTreeNode<K,V> parent;

        @SuppressWarnings("unchecked")
        BTreeNode(int degree, BTreeNode<K,V> parent) {
            this.keys = new NodeEntry[(degree<<1) - 1];
            this.childes = new BTreeNode[degree<<1];
            this.parent = parent;
        }

        void clear(){
            for (int i = 0; i < keys.length; i++) {
                keys[i] = null;
                childes[i] = null;
            }
            childes[keys.length] = null;
        }

        final boolean isLeaf(){
            return childes[0] == null;
        }
    }
}
