package com.semantive.commons.functional;

import java.util.*;

/**
 * @author Jacek Lewandowski
 */
public class ListWrapper<T> extends AbstractElementCollection<T, ListWrapper> implements List<T> {

    private List<T> base;

    public ListWrapper(Collection<T> base) {
        if (base instanceof List) this.base = (List<T>) base;
        else this.base = new ArrayList<T>(base);
    }

    @Override
    public Iterator<T> values() {
        return iterator();
    }

    @Override
    public ElementCollectionFactory factory() {
        return new ElementCollectionFactory() {
            @Override
            public ElementCollection build(Collection values) {
                return new ListWrapper(values);
            }
        };
    }

    @Override
    public T random() {
        return base.get(random.nextInt(size()));
    }

    @Override
    public int size() {
        return base.size();
    }

    @Override
    public boolean contains(Object o) {
        return base.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return base.iterator();
    }

    @Override
    public Object[] toArray() {
        return base.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return base.toArray(a);
    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return base.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T get(int index) {
        return base.get(index);
    }

    @Override
    public T set(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        return base.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return base.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return base.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return base.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return base.subList(fromIndex, toIndex);
    }
}
