package com.semantive.commons.functional;

import java.util.Iterator;

/**
 * @author Jacek Lewandowski
 */
public interface ElementCollection<V, T extends ElementCollection> {
    Iterator<V> values();

    ElementCollectionFactory factory();

    <R> T map(F1<V, R> mappingFunction);

    <R> T flatMap(F1<V, Option<R>> mappingFunction);

    T forEach(Void1<V> action);

    T forEach(F1<V, ?> action);

    boolean contains(F1<V, Boolean> predicate);

    V find(F1<V, Boolean> predicate);

    T filter(F1<V, Boolean> predicate);

    boolean forAll(F1<V, Boolean> predicate);

    <R, O extends Option<R>> T collect(F1<V, O> collectFunction);

    V head();

    Option<V> headOption();

    V last();

    Option<V> lastOption();

    T tail();

    boolean isEmpty();

    boolean isDefined();

    <R> R foldLeft(R initialValue, F2<R, V, R> foldFunction);

    V random();

    int size();
}
