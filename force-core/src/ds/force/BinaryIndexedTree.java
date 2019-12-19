package ds.force;

import java.util.Arrays;
import java.util.function.BinaryOperator;

public class BinaryIndexedTree<E> {

    /**
     * Default initial capacity.
     */
    private static final int DEFAULT_CAPACITY = 10;

    /**
     * Shared empty array instance used for empty instances.
     */
    private static final Object[] EMPTY_ELEMENTDATA = {};

    /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    final transient BinaryOperator<E> plusFunction;

    final transient BinaryOperator<E> subFunction;

    transient Object[] dataTable;

    transient Object[] treeArray;

    private int size;

    public BinaryIndexedTree(BinaryOperator<E> plusFunction,BinaryOperator<E> subFunction){
        this(DEFAULT_CAPACITY,plusFunction,subFunction);
    }

    public BinaryIndexedTree(int initCapacity,BinaryOperator<E> plusFunction,BinaryOperator<E> subFunction){
        if (initCapacity > 0) {
            this.dataTable = new Object[initCapacity];
            this.treeArray = new Object[initCapacity];
        } else if (initCapacity == 0) {
            this.dataTable = EMPTY_ELEMENTDATA;
            this.treeArray = EMPTY_ELEMENTDATA;
        } else {
            throw new IllegalArgumentException("Illegal Capacity: "+
                    initCapacity);
        }
        this.plusFunction = plusFunction;
        this.subFunction = subFunction;
    }

    /**
     * @throws NullPointerException if the specified array is null
     * @param array
     */
    @SuppressWarnings("unchecked")
    public BinaryIndexedTree(Object[] array,BinaryOperator<E> biFunction,BinaryOperator<E> subFunction){
        this.dataTable = array;
        this.size = array.length;
        this.treeArray = new Object[size];
        this.plusFunction = biFunction;
        this.subFunction = subFunction;
        for (int i = 0; i < array.length; i++){
            int index = i;
            while(index < size){
                treeArray[index] = biFunction.apply((E)treeArray[index],(E)dataTable[i]);
                index += lowBit(index);
            }
        }
    }

    private void ensureCapacityInternal(int minCapacity) {
        if (dataTable == EMPTY_ELEMENTDATA) {
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }

        ensureExplicitCapacity(minCapacity);
    }

    private void ensureExplicitCapacity(int minCapacity) {
        // overflow-conscious code
        if (minCapacity - dataTable.length > 0)
            grow(minCapacity);
    }

    /**
     * Increases the capacity to ensure that it can hold at least the
     * number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = dataTable.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        this.dataTable = Arrays.copyOf(dataTable, newCapacity);
        this.treeArray = Arrays.copyOf(treeArray, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
                Integer.MAX_VALUE :
                MAX_ARRAY_SIZE;
    }

    /**
     * important function
     * @param index index in array
     * @return the position of the first 1 in a binary representation
     */
    final int lowBit(int index){
        int sequence = index + 1;
        return sequence & (-sequence);
    }

    /**
     * return the raw data by index
     * @param index index in array
     * @return raw data
     */
    @SuppressWarnings("unchecked")
    public E get(int index){
        rangeCheckForInsert(index);
        return (E) dataTable[index];
    }

    /**
     * replace old value for index of array to new value
     * @param index index in array
     * @param value new value
     */
    @SuppressWarnings("unchecked")
    public void set(int index, E value){
        E oldValue = (E) dataTable[index];
        dataTable[index] = value;
        E diff = subFunction.apply(oldValue,value);
        while(index < size){
            treeArray[index] = subFunction.apply(oldValue,value);
            index += lowBit(index);
        }
    }

    /**
     * add value into index of array
     * @param index index in array
     * @param value add's value
     */
    @SuppressWarnings("unchecked")
    public void add(int index, E value){
        dataTable[index] = plusFunction.apply((E) dataTable[index],value);
        while(index < size){
            treeArray[index] = plusFunction.apply((E) treeArray[index],value);
            index += lowBit(index);
        }
    }

    /**
     * append a value to data array
     * @param value
     * @return
     */
    public boolean insert(E value){
        ensureCapacityInternal(size + 1);
        dataTable[size] = value;
        int index = size;
        E newSum = plusFunction.apply(getSum(size) ,value);
        size = size + 1;
        treeArray[index] = subFunction.apply(newSum,getSum(size - lowBit(index)));
        return true;
    }

    /**
     * Checks if the given index is in range.  If not, throws an appropriate
     * runtime exception.  This method does *not* check if the index is
     * negative: It is always used immediately prior to an array access,
     * which throws an ArrayIndexOutOfBoundsException if index is negative.
     */
    private void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * A version of rangeCheck used by insert and addAll.
     */
    private void rangeCheckForInsert(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    /**
     * Constructs an IndexOutOfBoundsException detail message.
     * Of the many possible refactorings of the error handling code,
     * this "outlining" performs best with both server and client VMs.
     */
    private String outOfBoundsMsg(int index) {
        return "Index: "+index+", Size: "+size;
    }

    /**
     * inserts a data in the specified position of the array
     * @param index
     * @param value
     */
    @SuppressWarnings("unchecked")
    public void insert(int index, E value){
        rangeCheckForInsert(index);
        ensureCapacityInternal(size + 1);
        System.arraycopy(dataTable, index, dataTable, index + 1,
                size - index);
        System.arraycopy(treeArray, index, treeArray, index + 1,
                size - index);
        dataTable[index] = value;
        size++;
        for (int i = index; i < size; i++){
            treeArray[i] = 0;
            E newSum = plusFunction.apply(getSum(i), (E) dataTable[i]);
            treeArray[i] = subFunction.apply(newSum,getSum(i - lowBit(i) + 1));
        }
    }

    /**
     * delete data at specified location
     * @param index index
     * @throws IndexOutOfBoundsException when index >= size
     * @return old value
     */
    @SuppressWarnings("unchecked")
    public E remove(int index){
        rangeCheck(index);
        E oldValue = (E) dataTable[index];
        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(dataTable, index + 1, dataTable, index,
                    numMoved);
        }
        size--;
        for (int i = index; i < size; i++){
            treeArray[i] = 0;
            E newSum = plusFunction.apply(getSum(i), (E) dataTable[i]);
            treeArray[i] = subFunction.apply(newSum,getSum(i - lowBit(i) + 1));
        }
        treeArray[size] = 0;
        dataTable[size] = 0;
        return oldValue;
    }

    /**
     * return the sum of interval from 0 to index
     * @param index the end's index
     * @throws IndexOutOfBoundsException when index >= size
     * @return sum
     */
    @SuppressWarnings("unchecked")
    public E getSum(int index){
        E sum = null;
        int tail = index - 1;
        while(tail >= 0){
            sum = plusFunction.apply(sum, (E) treeArray[tail]);
            tail -= lowBit(tail);
        }
        return sum;
    }

    /**
     * return the sum of interval from start to end
     * @param start the start's index
     * @param end the end's index
     * @throws IndexOutOfBoundsException when index >= size
     * @return the sum
     */
    @SuppressWarnings("unchecked")
    public E getIntervalSum(int start, int end){
        E sum = null;
        E redundant = null;
        int head = start - 1,tail = end - 1;
        while(head >= 0){
            redundant = plusFunction.apply(redundant, (E) treeArray[head]);
            head -= lowBit(head);
        }
        while(tail >= 0){
            sum = plusFunction.apply(sum, (E) treeArray[tail]);
            tail -= lowBit(tail);
        }
        return subFunction.apply(sum,redundant);
    }

    /**
     * return the array's size
     * @return
     */
    public int size(){
        return size;
    }
}
