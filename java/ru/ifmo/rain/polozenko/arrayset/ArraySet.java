package ru.ifmo.rain.polozenko.arrayset;

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