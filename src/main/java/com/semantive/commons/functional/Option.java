package com.semantive.commons.functional;

import com.google.common.collect.Iterators;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.iterators.SingletonIterator;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Jacek Lewandowski
 */
public abstract class Option<V> extends AbstractElementCollection<V, Option> {

    public abstract V get();

    public abstract V orNull();

    @Override
    public <R> Option<R> map(F1<V, R> mappingFunction) {
        return super.map(mappingFunction);
    }

    @Override
    public Option<V> forEach(Void1<V> action) {
        return super.forEach(action);
    }

    @Override
    public Option<V> forEach(F1<V, ?> action) {
        return super.forEach(action);
    }

    @Override
    public Option<V> filter(F1<V, Boolean> predicate) {
        return super.filter(predicate);
    }

    @Override
    public boolean forAll(F1<V, Boolean> predicate) {
        return super.forAll(predicate);
    }

    @Override
    public <R, O extends Option<R>> Option<R> collect(F1<V, O> collectFunction) {
        return super.collect(collectFunction);
    }

    @Override
    public V head() {
        return super.head();
    }

    @Override
    public Option<V> headOption() {
        return super.headOption();
    }

    @Override
    public V last() {
        return super.last();
    }

    @Override
    public Option<V> lastOption() {
        return super.lastOption();
    }

    @Override
    public Option<V> tail() {
        return super.tail();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public boolean isDefined() {
        return super.isDefined();
    }

    public Box<V> box() {
        return new Box<V>(orNull());
    }

    @Override
    public V random() {
        return get();
    }

    public final ElementCollectionFactory<V, Option> factory = new ElementCollectionFactory<V, Option>() {
        @Override
        public ElementCollection<V, Option> build(Collection<V> values) {
            if (values.isEmpty()) {
                return none;
            } else {
                return new Some<V>((V) CollectionUtils.get(values, 0));
            }
        }
    };

    public static <V> Option<V> option(V value) {
        if (value != null) {
            return new Some(value);
        } else {
            return none;
        }
    }

    public static <V> Some<V> some(V value) {
        if (value != null) {
            return new Some(value);
        } else {
            throw new NullPointerException();
        }
    }

    public static final Option none = new Option() {

        @Override
        public Object get() {
            throw new NullPointerException("Empty!");
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean isDefined() {
            return false;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public Object orNull() {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == none;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public Iterator values() {
            return Iterators.emptyIterator();
        }

        @Override
        public ElementCollectionFactory factory() {
            return factory;
        }

        @Override
        public Option map(F1 mappingFunction) {
            return none;
        }

        @Override
        public Option flatMap(F1 mappingFunction) {
            return none;
        }

        @Override
        public Option forEach(Void1 action) {
            return this;
        }

        @Override
        public Option forEach(F1 action) {
            return this;
        }

        @Override
        public Option filter(F1 predicate) {
            return none;
        }

        @Override
        public boolean forAll(F1 predicate) {
            return true;
        }

        @Override
        public Option collect(F1 collectFunction) {
            return none;
        }

        @Override
        public Object head() {
            return get();
        }

        @Override
        public Option headOption() {
            return this;
        }

        @Override
        public Object last() {
            return get();
        }

        @Override
        public Option lastOption() {
            return this;
        }

        @Override
        public Option tail() {
            return none;
        }
    };


    public final static class Some<V> extends Option<V> {
        private final V v;

        public Some(V v) {
            if (v == null) throw new IllegalArgumentException("Value cannot be null.");
            this.v = v;
        }

        @Override
        public V get() {
            return v;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean isDefined() {
            return true;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public V orNull() {
            return v;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Some some = (Some) o;

            return v == some.v || v.equals(some.v);
        }

        @Override
        public int hashCode() {
            return v.hashCode();
        }

        @Override
        public Iterator<V> values() {
            return new SingletonIterator(v);
        }

        @Override
        public ElementCollectionFactory factory() {
            return factory;
        }

        @Override
        public <R> Option<R> map(F1<V, R> mappingFunction) {
            return new Some<R>(mappingFunction.apply(v));
        }

        @Override
        public <R> Option flatMap(F1<V, Option<R>> mappingFunction) {
            return mappingFunction.apply(get());
        }

        @Override
        public Option<V> forEach(Void1<V> action) {
            action.apply(v);
            return this;
        }

        @Override
        public Option<V> forEach(F1<V, ?> action) {
            action.apply(v);
            return this;
        }

        @Override
        public Option<V> filter(F1<V, Boolean> predicate) {
            if (predicate.apply(v)) return this;
            else return none;
        }

        @Override
        public boolean forAll(F1<V, Boolean> predicate) {
            return predicate.apply(v);
        }

        @Override
        public <R, O extends Option<R>> Option<R> collect(F1<V, O> collectFunction) {
            return collectFunction.apply(v);
        }

        @Override
        public V head() {
            return get();
        }

        @Override
        public Option<V> headOption() {
            return this;
        }

        @Override
        public V last() {
            return get();
        }

        @Override
        public Option<V> lastOption() {
            return this;
        }

        @Override
        public Option<V> tail() {
            return none;
        }
    }
}
