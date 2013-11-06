package com.semantive.hiqual;

import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import com.semantive.commons.functional.AbstractElementCollection;
import com.semantive.commons.functional.ElementCollection;
import com.semantive.commons.functional.ElementCollectionFactory;

import java.io.Serializable;
import java.util.*;

/**
 * @author Jacek Lewandowski
 */
public class PaginationAwareListWrapper<T> extends AbstractElementCollection<T, PaginationAwareListWrapper> implements List<T>, IPaginationAware, Serializable {
    private List<T> _baseList;

    private int _offset;

    private int _totalSize;

    public PaginationAwareListWrapper(List<T> baseList, int offset, int totalSize) {
        this._baseList = baseList;
        this._offset = offset;
        this._totalSize = totalSize;
    }

    public PaginationAwareListWrapper(List<T> baseList, IResultSetConfig config, int totalSize) {
        this._baseList = baseList;
        this._offset = 0;
        this._totalSize = totalSize;
        if (config != null && config.pageDefinition() != null) this._offset = config.pageDefinition().getPageStart();
    }

    public Range<Integer> range() {
        return Ranges.closedOpen(_offset, _offset + size());
    }

    public int size() {
        return _baseList.size();
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
                return new PaginationAwareListWrapper(new ArrayList(values), 0, values.size());
            }
        };
    }

    public boolean isEmpty() {
        return _baseList.isEmpty();
    }

    @Override
    public T random() {
        return _baseList.get(random.nextInt(_baseList.size()));
    }

    public boolean contains(Object o) {
        return _baseList.contains(o);
    }

    public Iterator<T> iterator() {
        return _baseList.iterator();
    }

    public Object[] toArray() {
        return _baseList.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return _baseList.toArray(a);
    }

    public boolean add(T t) {
        return _baseList.add(t);
    }

    public boolean remove(Object o) {
        return _baseList.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return _baseList.containsAll(c);
    }

    public boolean addAll(Collection<? extends T> c) {
        return _baseList.addAll(c);
    }

    public boolean addAll(int index, Collection<? extends T> c) {
        return _baseList.addAll(index, c);
    }

    public boolean removeAll(Collection<?> c) {
        return _baseList.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return _baseList.retainAll(c);
    }

    public void clear() {
        _baseList.clear();
    }

    public T get(int index) {
        return _baseList.get(index);
    }

    public T set(int index, T element) {
        return _baseList.set(index, element);
    }

    public void add(int index, T element) {
        _baseList.add(index, element);
    }

    public T remove(int index) {
        return _baseList.remove(index);
    }

    public int indexOf(Object o) {
        return _baseList.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return _baseList.lastIndexOf(o);
    }

    public ListIterator<T> listIterator() {
        return _baseList.listIterator();
    }

    public ListIterator<T> listIterator(int index) {
        return _baseList.listIterator(index);
    }

    public PaginationAwareListWrapper<T> subList(int fromIndex, int toIndex) {
        return new PaginationAwareListWrapper<T>(_baseList.subList(fromIndex, toIndex), _offset + fromIndex, _totalSize);
    }

    public int totalSize() {
        return _totalSize;
    }

    public int offset() {
        return _offset;
    }

}
