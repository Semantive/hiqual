package com.semantive.commons.functional;

import com.google.common.base.Function;

/**
 * @author Jacek Lewandowski
 */
public interface F1<V, R> extends Function<V, R> {
    R apply(V v);

}
