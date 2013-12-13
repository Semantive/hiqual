package com.semantive.hiqual.pagination;

import com.google.common.collect.Range;
import com.semantive.commons.functional.AbstractElementCollection;
import com.semantive.commons.functional.ElementCollection;
import com.semantive.commons.functional.ElementCollectionFactory;
import com.semantive.hiqual.IResultSetConfig;

import java.io.Serializable;
import java.util.*;

/**
 * A wrapper that decorates a plain list of elements with pagination data and functional features.
 *
 * @author Jacek Lewandowski
 */
public class PaginationAwareListWrapper<T> extends AbstractElementCollection<T, PaginationAwareListWrapper> implements List<T>, IPaginationAware, Serializable {
    private final List<T> _baseList;

    private final int _offset;

    private final int _totalSize;

    public PaginationAwareListWrapper(List<T> baseList, int offset, int totalSize) {
        this._baseList = baseList;
        this._offset = offset;
        this._totalSize = totalSize;
    }

    public PaginationAwareListWrapper(List<T> baseList, IResultSetConfig config, int totalSize) {
        this._baseList = baseList;
        this._totalSize = totalSize;
        if (config != null && config.pageDefinition() != null)
            this._offset = config.pageDefinition().pageStart;
        else
            this._offset = 0;
    }

    public Range<Integer> range() {
        return Range.closedOpen(_offset, _offset + size());
    }

    @Override
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

    @Override
    public boolean isEmpty() {
        return _baseList.isEmpty();
    }

    @Override
    public T random() {
        return _baseList.get(random.nextInt(_baseList.size()));
    }

    @Override
    public boolean contains(Object o) {
        return _baseList.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return _baseList.iterator();
    }

    @Override
    public Object[] toArray() {
        return _baseList.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return _baseList.toArray(a);
    }

    @Override
    public boolean add(T t) {
        return _baseList.add(t);
    }

    @Override
    public boolean remove(Object o) {
        return _baseList.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return _baseList.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return _baseList.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return _baseList.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return _baseList.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return _baseList.retainAll(c);
    }

    @Override
    public void clear() {
        _baseList.clear();
    }

    @Override
    public T get(int index) {
        return _baseList.get(index);
    }

    @Override
    public T set(int index, T element) {
        return _baseList.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        _baseList.add(index, element);
    }

    @Override
    public T remove(int index) {
        return _baseList.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return _baseList.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return _baseList.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return _baseList.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return _baseList.listIterator(index);
    }

    @Override
    public PaginationAwareListWrapper<T> subList(int fromIndex, int toIndex) {
        return new PaginationAwareListWrapper<T>(_baseList.subList(fromIndex, toIndex), _offset + fromIndex, _totalSize);
    }

    @Override
    public int totalSize() {
        return _totalSize;
    }

    @Override
    public int offset() {
        return _offset;
    }

}
