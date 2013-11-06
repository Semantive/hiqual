package com.semantive.commons.functional;

import com.google.common.base.Supplier;
import com.google.gson.annotations.Expose;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Jacek Lewandowski
 */
public class Box<V> implements F0<V>, Supplier<V> {
    @Expose
    private V value;

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public Box() {
    }

    public Box(V value) {
        this.value = value;
    }

    public Box(Box box) {
        this.value = (V) box.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Box box = (Box) o;

        return !(value != null ? !value.equals(box.value) : box.value != null);
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("value", value)
                .toString();
    }

    @Override
    public V apply() {
        return value;
    }


    @Override
    public V get() {
        return value;
    }

    public Option<V> option() {
        return Option.option(value);
    }
}
