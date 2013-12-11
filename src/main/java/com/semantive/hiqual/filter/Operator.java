package com.semantive.hiqual.filter;

/**
 * @author Piotr Jędruszuk
 */
public enum Operator {
    GT(" %1$s > %2$s "),
    LT(" %1$s < %2$s "),
    EQ(" %1$s = %2$s "),
    NOT_EQ(" %1$s != %2$s "),
    GT_OR_EQ(" %1$s >= %2$s "),
    LT_OR_EQ(" %1$s <= %2$s "),
    LIKE(" %1$s like %2$s "),
    IN(" %1$s in (%2$s) "),
    CONTAINS(" %2$s in elements(%1$s) ");

    private final String representation;

    private Operator(String representation) {
        this.representation = representation;
    }

    public String getRepresentation() {
        return representation;
    }
}
