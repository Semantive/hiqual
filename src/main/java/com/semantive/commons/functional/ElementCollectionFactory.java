package com.semantive.commons.functional;

import java.util.Collection;

/**
 * @author Jacek Lewandowski
 */
public interface ElementCollectionFactory<V, T extends ElementCollection> {
    ElementCollection<V, T> build(Collection<V> values);
}
