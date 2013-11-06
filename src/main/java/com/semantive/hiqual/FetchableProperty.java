package com.semantive.hiqual;

import com.semantive.commons.functional.Option;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static com.semantive.commons.functional.Option.none;
import static com.semantive.commons.functional.Option.some;

/**
 * Klasa reprezentuje zbiór informacji o atrybucie, który ma zostać pobrany
 * z bazy danych.
 *
 * @author Jacek Lewandowski
 */
public class FetchableProperty {
    /**
     * Nazwa atrybutu w bean'ie
     */
    private final String name;

    /**
     * Wyrażenie HQL do pobrania atrybutu - typowo po prostu atrybut - to samo co w name
     */
    private final String expression;

    /**
     * Ścieżka do ewentualnego left outer join'a wymaganego do pobrania atrybutu
     */
    private final Option<String> join;

    public FetchableProperty(String expression, String name, Option<String> join) {
        this.name = name;
        this.expression = expression;
        this.join = join;
    }

    public FetchableProperty(String expression, String name) {
        this(expression, name, none);
    }

    public FetchableProperty(String name, Option<String> join) {
        this(name, name, join);
    }

    public FetchableProperty(String name) {
        this(name, name, none);
    }

    public FetchableProperty withExpression(String expression) {
        return new FetchableProperty(expression, name, join);
    }

    public FetchableProperty withJoin(String join) {
        return new FetchableProperty(expression, name, some(join));
    }

    public FetchableProperty withJoin(Option<String> join) {
        return new FetchableProperty(expression, name, join);
    }

    public FetchableProperty withoutJoin() {
        return new FetchableProperty(expression, name, none);
    }

    public String getName() {
        return name;
    }

    public String getExpression() {
        return expression;
    }

    public Option<String> getJoin() {
        return join;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FetchableProperty that = (FetchableProperty) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("expression", expression)
                .append("join", join)
                .toString();
    }
}
