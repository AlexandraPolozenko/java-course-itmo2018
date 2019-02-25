package ru.ifmo.rain.polozenko.arrayset;

/*import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {

    private final List<T> sortedData;
    private final Comparator<? super T> comparator;

    private ArraySet(List<T> list, Comparator<? super T> comp) {
        sortedData = list;
        comparator = comp;
    }

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    public ArraySet(Collection<? extends T> collection, Comparator<? super T> comp) {
        comparator = comp;
        TreeSet<T> set = new TreeSet<>(comp);
        set.addAll(Objects.requireNonNull(collection));
        sortedData = new ArrayList<>(set);
    }

    public ArraySet(Collection<? extends T> collection) {
        this(collection, null);
    }

    public ArraySet(ArraySet<T> other) {
        sortedData = other.sortedData;
        comparator = other.comparator;
    }

    private static final String setIsEmptyMessage = "ArraySet is empty, cannot get ";

    public T first() {
        if (!sortedData.isEmpty()) {
            return sortedData.get(0);
        }
        throw new NoSuchElementException(setIsEmptyMessage + "first element");
    }

    public T last() {
        if (!sortedData.isEmpty()) {
            return sortedData.get(sortedData.size() - 1);
        }
        throw new NoSuchElementException(setIsEmptyMessage + "last element");
    }

    public int size() {
        return sortedData.size();
    }

    private static final String unsupportedOperationMessage = "ArraySet is immutable, cannot perform ";

    public T pollFirst() {
        throw new UnsupportedOperationException(unsupportedOperationMessage + "pollFirst");
    }

    public T pollLast() {
        throw new UnsupportedOperationException(unsupportedOperationMessage + "pollLast");
    }

    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(sortedData, (T) o, comparator) >= 0;
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(sortedData).iterator();
    }

    private boolean valid(int index) {
        return (index >= 0 && index < size());
    }

    private T getAnswer(T element, int addIfFound, int addIfNotFound) {
        int index = searchForPosition(element, addIfFound, addIfNotFound);

        if (valid(index)) {
            return sortedData.get(index);
        }
        return null;
    }

    private int searchForPosition(T element, int addIfFound, int addIfNotFound) {
        int index = Collections.binarySearch(sortedData, element, comparator);
        if (index >= 0) {
            //found
            return index + addIfFound;
        }
        index = (index + 1);
        index *= -1;
        return index + addIfNotFound;
    }

    @Override
    public T lower(T element) {
        return getAnswer(element, -1, -1);
    }

    @Override
    public T floor(T element) {
        return getAnswer(element, 0, -1);
    }

    @Override
    public T ceiling(T element) {
        return getAnswer(element, 0, 0);
    }

    @Override
    public T higher(T element) {
        return getAnswer(element, 1, 0);
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        int addIfFound = fromInclusive ? 0 : 1;
        int addIfNotFound = 0;
        int leftBorder = searchForPosition(fromElement, addIfFound, addIfNotFound);

        addIfFound = toInclusive ? 0 : -1;
        addIfNotFound = -1;
        int rightBorder = searchForPosition(toElement, addIfFound, addIfNotFound) + 1;

        if (leftBorder - rightBorder >= 0) {
            return Collections.emptyNavigableSet();
        }
        return new ArraySet<>(sortedData.subList(leftBorder, rightBorder), comparator);
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        if (sortedData.isEmpty()) {
            return Collections.emptyNavigableSet();
        }
        return subSet(first(), true, toElement, inclusive);
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        if (sortedData.isEmpty()) {
            return Collections.emptyNavigableSet();
        }
        return subSet(fromElement, inclusive, last(), true);
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return headSet(toElement, false);
    }

    private class ReversedList<E> extends AbstractList<E> {
        private final boolean reversed;
        private final List<E> data;

        ReversedList(List<E> other) {
            if (other instanceof ReversedList) {
                ReversedList<E> tmp = (ReversedList<E>)other;
                reversed = !tmp.reversed;
                data = tmp.data;
            } else {
                reversed = true;
                data = other;
            }
        }

        @Override
        public int size() {
            return data.size();
        }

        @Override
        public E get(int index) {
            if (!reversed) {
                return data.get(index);
            }
            return data.get(size() - 1 - index);
        }
    }

    @Override
    public NavigableSet<T> descendingSet() {
        ReversedList<T> reversed = new ReversedList<>(sortedData);
        return new ArraySet<>(reversed, Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }
}*/

import java.util.*;

public class ArraySet<T> extends AbstractSet<T> implements SortedSet<T> {
    private List<T> list;
    private Comparator<? super T> comparator;

    public ArraySet() {
        list = Collections.emptyList();
        comparator = null;
    }

    public ArraySet(Comparator<? super T> comp) {
        list = Collections.emptyList();
        comparator = comp;
    }

    public ArraySet(Collection<T> coll) {
        list = new ArrayList<>(new TreeSet<>(coll));
        comparator = null;
    }

    public ArraySet(Collection<T> coll, Comparator<? super T> comp) {
        comparator = comp;

        TreeSet<T> treeSet = new TreeSet<>(comp);
        treeSet.addAll(coll);
        list = new ArrayList<>(treeSet);
    }

    public ArraySet(List<T> coll, Comparator<? super T> comp) {
        list = coll;
        comparator = comp;
    }

    @Override
    public ArraySet<T> headSet(T toElement) {
        if (!list.isEmpty()) {
            return sSet(list.get(0), toElement, false);
        } else {
            return this;
        }
    }

    @Override
    public SortedSet tailSet(T fromElement) {
        if (!list.isEmpty()) {
            return sSet(fromElement, list.get(list.size() - 1), true);
        } else {
            return this;
        }
    }

    @Override
    public ArraySet<T> subSet(T fromElement, T toElement) {
        if (!list.isEmpty()) {
            return sSet(fromElement, toElement, false);
        } else {
            return this;
        }
    }

    @Override
    public Comparator comparator() {
        return comparator;
    }

    @Override
    public T first() {
        checkEmpty();
        return list.get(0);
    }

    @Override
    public Iterator iterator() {
        return Collections.unmodifiableList(list).iterator();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return Collections.binarySearch(list, (T) o, comparator) >= 0;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public T last() {
        checkEmpty();
        return list.get(list.size() - 1);
    }

    private int find(T element) {
        int e = Collections.binarySearch(list, element, comparator);

        if (e < 0) {
            return -(e + 1);
        } else {
            return e;
        }
    }

    private ArraySet<T> sSet(T fromElement, T toElement, boolean include) {
        ArraySet arraySet;

        int l = find(fromElement);
        int r = find(toElement);

        if (include) {
            r++;
        }

        arraySet = new ArraySet(list.subList(l, r), comparator);

        return arraySet;
    }

    private void checkEmpty() {
        if (list.isEmpty()) {
            throw new NoSuchElementException("Empty list");
        }
    }
}