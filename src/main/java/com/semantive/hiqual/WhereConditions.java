package com.semantive.hiqual;

/**
 * <br/>User: Piotr Jędruszuk (pjedruszuk@semantive.pl)
 * <br/>Date: 23.04.13 13:21
 */
public class WhereConditions {

    private AbstractWhereCondition whereCondition;

    public WhereConditions(AbstractWhereCondition whereCondition) {
        this.whereCondition = whereCondition;
    }

    public AbstractWhereCondition getWhereCondition() {
        return whereCondition;
    }

    public WhereConditions setWhereCondition(AbstractWhereCondition condition) {
        this.whereCondition = condition;
        return this;
    }
}
