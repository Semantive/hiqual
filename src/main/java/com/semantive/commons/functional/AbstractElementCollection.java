package com.semantive.commons.functional;

import com.google.common.collect.Iterators;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * @author Jacek Lewandowski
 */
public abstract class AbstractElementCollection<V, T extends ElementCollection> implements ElementCollection<V, T> {
    protected final Random random = new Random(System.currentTimeMillis());

    @Override
    public <R> T map(F1<V, R> mappingFunction) {
        List<R> lst = new LinkedList<R>();
        Iterator<V> it = values();
        while (it.hasNext()) {
            V next = it.next();
            lst.add(mappingFunction.apply(next));
        }
        return (T) factory().build(lst);
    }

    @Override
    public <R> T flatMap(F1<V, Option<R>> mappingFunction) {
        List<R> lst = new LinkedList<R>();
        Iterator<V> it = values();
        while (it.hasNext()) {
            V next = it.next();
            Option<R> r = mappingFunction.apply(next);
            if (r != null && r.isDefined()) lst.add(r.get());
        }
        return (T) factory().build(lst);
    }


    @Override
    public <R> R foldLeft(R initialValue, F2<R, V, R> foldFunction) {
        R value = initialValue;
        Iterator<V> it = values();
        while (it.hasNext()) {
            value = foldFunction.apply(value, it.next());
        }
        return value;
    }


    @Override
    public T forEach(Void1<V> action) {
        Iterator<V> it = values();
        while (it.hasNext()) {
            V next = it.next();
            action.apply(next);
        }
        return (T) this;
    }

    @Override
    public T forEach(F1<V, ?> action) {
        Iterator<V> it = values();
        while (it.hasNext()) {
            V next = it.next();
            action.apply(next);
        }
        return (T) this;
    }

    @Override
    public boolean contains(F1<V, Boolean> predicate) {
        Iterator<V> it = values();

        boolean contains = false;
        while (it.hasNext() && !contains) {
            contains = predicate.apply(it.next());
        }

        return contains;
    }

    @Override
    public V find(F1<V, Boolean> predicate) {
        Iterator<V> it = values();

        V element = null;
        while (it.hasNext() && element == null) {
            V next = it.next();
            element = predicate.apply(next) ? next : null;
        }

        return element;
    }

    @Override
    public T filter(F1<V, Boolean> predicate) {
        List<V> lst = new LinkedList<V>();
        Iterator<V> it = values();
        while (it.hasNext()) {
            V next = it.next();
            if (predicate.apply(next)) {
                lst.add(next);
            }
        }

        return (T) factory().build(lst);
    }

    @Override
    public boolean forAll(F1<V, Boolean> predicate) {
        Iterator<V> it = values();
        while (it.hasNext()) {
            V next = it.next();
            if (!predicate.apply(next)) return false;

        }

        return true;
    }

    @Override
    public <R, O extends Option<R>> T collect(F1<V, O> collectFunction) {
        List<R> lst = new LinkedList<R>();
        Iterator<V> it = values();
        while (it.hasNext()) {
            V next = it.next();
            O o = collectFunction.apply(next);
            if (o.isDefined()) lst.add(o.get());
        }
        return (T) factory().build(lst);
    }

    @Override
    public V head() {
        return headOption().get();
    }

    @Override
    public Option<V> headOption() {
        Iterator<V> it = values();
        if (it.hasNext()) {
            return new Option.Some<V>(it.next());
        } else {
            return Option.none;
        }
    }

    @Override
    public V last() {
        return lastOption().get();
    }

    @Override
    public Option<V> lastOption() {
        Iterator<V> it = values();
        while (it.hasNext()) {
            V v = it.next();
            if (it.hasNext()) {
                return new Option.Some<V>(v);
            }
        }
        return Option.none;
    }

    @Override
    public T tail() {
        List<V> lst = new LinkedList<V>();
        Iterator<V> it = values();
        if (it.hasNext()) it.next();
        Iterators.addAll(lst, it);
        return (T) factory().build(lst);
    }

    @Override
    public boolean isEmpty() {
        return !values().hasNext();
    }

    @Override
    public boolean isDefined() {
        return !isEmpty();
    }
}
