package ds.force.treap;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.SortedSet;

public abstract class AbstractTreapMap<K,V> extends AbstractMap<K,V> implements Map<K,V> {

    protected abstract AbstractEntry<K,V> getFirstEntry();

    protected abstract AbstractEntry<K,V> getLastEntry();

    protected abstract AbstractEntry<K,V> successor(AbstractEntry<K,V> entry);

    protected abstract AbstractEntry<K,V> predecessor(AbstractEntry<K,V> entry);

    protected abstract AbstractEntry<K,V> getEntry(Object key);

    private void deleteEntry(Map.Entry<K,V> entry){
        remove(entry.getKey());
    }

    /**
     * Test two values for equality.  Differs from o1.equals(o2) only in
     * that it copes with {@code null} o1 properly.
     */
    static final boolean valEquals(Object o1, Object o2) {
        return (o1==null ? o2==null : o1.equals(o2));
    }

    static abstract class AbstractEntry<K,V> implements Map.Entry<K,V> {
        K key;

        V value;

        int priority;

        AbstractEntry<K,V> left,right;

        AbstractEntry(K key, V value, int priority){
            this.key = key;
            this.value = value;
            this.priority = priority;
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

    class Values extends AbstractCollection<V> {
        public Iterator<V> iterator() {
            return new ValueIterator(getFirstEntry());
        }

        public int size() {
            return AbstractTreapMap.this.size();
        }

        public boolean contains(Object o) {
            return AbstractTreapMap.this.containsValue(o);
        }

        public boolean remove(Object o) {
            for (AbstractEntry<K,V> e = getFirstEntry(); e != null; e = successor(e)) {
                if (valEquals(e.getValue(), o)) {
                    deleteEntry(e);
                    return true;
                }
            }
            return false;
        }

        public void clear() {
            AbstractTreapMap.this.clear();
        }
    }

    class EntrySet extends AbstractSet<Map.Entry<K,V>> {
        public Iterator<Map.Entry<K,V>> iterator() {
            return new EntryIterator(getFirstEntry());
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> entry = (Map.Entry<?,?>) o;
            Object value = entry.getValue();
            Map.Entry<K,V> p = getEntry(entry.getKey());
            return p != null && valEquals(p.getValue(), value);
        }

        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> entry = (Map.Entry<?,?>) o;
            Object value = entry.getValue();
            Map.Entry<K,V> p = getEntry(entry.getKey());
            if (p != null && valEquals(p.getValue(), value)) {
                deleteEntry(p);
                return true;
            }
            return false;
        }

        public int size() {
            return AbstractTreapMap.this.size();
        }

        public void clear() {
            AbstractTreapMap.this.clear();
        }

    }

    Iterator<K> keyIterator() {
        return new KeyIterator(getFirstEntry());
    }

    Iterator<K> descendingKeyIterator() {
        return new DescendingKeyIterator(getLastEntry());
    }

    static final class KeySet<E> extends AbstractSet<E> implements NavigableSet<E> {
        private final NavigableMap<E, ?> m;
        KeySet(NavigableMap<E,?> map) { m = map; }

        public Iterator<E> iterator() {
            return ((FHQTreapMap<E,?>)m).keyIterator();
        }

        public Iterator<E> descendingIterator() {
            return ((FHQTreapMap<E,?>)m).descendingKeyIterator();
        }

        public int size() { return m.size(); }
        public boolean isEmpty() { return m.isEmpty(); }
        public boolean contains(Object o) { return m.containsKey(o); }
        public void clear() { m.clear(); }
        public E lower(E e) { return m.lowerKey(e); }
        public E floor(E e) { return m.floorKey(e); }
        public E ceiling(E e) { return m.ceilingKey(e); }
        public E higher(E e) { return m.higherKey(e); }
        public E first() { return m.firstKey(); }
        public E last() { return m.lastKey(); }
        public Comparator<? super E> comparator() { return m.comparator(); }
        public E pollFirst() {
            Map.Entry<E,?> e = m.pollFirstEntry();
            return (e == null) ? null : e.getKey();
        }
        public E pollLast() {
            Map.Entry<E,?> e = m.pollLastEntry();
            return (e == null) ? null : e.getKey();
        }
        public boolean remove(Object o) {
            int oldSize = size();
            m.remove(o);
            return size() != oldSize;
        }
        public NavigableSet<E> subSet(E fromElement, boolean fromInclusive,
                                      E toElement,   boolean toInclusive) {
            return new KeySet<>(m.subMap(fromElement, fromInclusive,
                    toElement,   toInclusive));
        }
        public NavigableSet<E> headSet(E toElement, boolean inclusive) {
            return new KeySet<>(m.headMap(toElement, inclusive));
        }
        public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
            return new KeySet<>(m.tailMap(fromElement, inclusive));
        }
        public SortedSet<E> subSet(E fromElement, E toElement) {
            return subSet(fromElement, true, toElement, false);
        }
        public SortedSet<E> headSet(E toElement) {
            return headSet(toElement, false);
        }
        public SortedSet<E> tailSet(E fromElement) {
            return tailSet(fromElement, true);
        }
        public NavigableSet<E> descendingSet() {
            return new KeySet<>(m.descendingMap());
        }
    }

    /**
     * Base class for TreeMap Iterators
     */
    abstract class TreapEntryIterator<T> implements Iterator<T> {
        AbstractEntry<K,V> next;
        AbstractEntry<K,V> lastReturned;

        TreapEntryIterator(AbstractEntry<K,V> first) {
            lastReturned = null;
            next = first;
        }

        public final boolean hasNext() {
            return next != null;
        }

        final AbstractEntry<K,V> nextEntry() {
            AbstractEntry<K,V> e = next;
            if (e == null)
                throw new NoSuchElementException();
            next = successor(e);
            lastReturned = e;
            return e;
        }

        final AbstractEntry<K,V> prevEntry() {
            AbstractEntry<K,V> e = next;
            if (e == null)
                throw new NoSuchElementException();
            next = predecessor(e);
            lastReturned = e;
            return e;
        }

        public void remove() {
            if (lastReturned == null)
                throw new IllegalStateException();
            // deleted entries are replaced by their successors
            if (lastReturned.left != null && lastReturned.right != null)
                next = lastReturned;
            deleteEntry(lastReturned);
            lastReturned = null;
        }
    }

    final class EntryIterator extends TreapEntryIterator<Map.Entry<K,V>> {
        EntryIterator(AbstractEntry<K,V> first) {
            super(first);
        }
        public AbstractEntry<K,V> next() {
            return nextEntry();
        }
    }

    final class ValueIterator extends TreapEntryIterator<V> {
        ValueIterator(AbstractEntry<K,V> first) {
            super(first);
        }
        public V next() {
            return nextEntry().value;
        }
    }

    final class KeyIterator extends TreapEntryIterator<K> {
        KeyIterator(AbstractEntry<K,V> first) {
            super(first);
        }
        public K next() {
            return nextEntry().key;
        }
    }

    final class DescendingKeyIterator extends TreapEntryIterator<K> {
        DescendingKeyIterator(AbstractEntry<K,V> first) {
            super(first);
        }
        public K next() {
            return prevEntry().key;
        }
        public void remove() {
            if (lastReturned == null)
                throw new IllegalStateException();
            deleteEntry(lastReturned);
            lastReturned = null;
        }
    }
}