package com.semantive.hiqual.filter;

/**
 * @author Piotr Jędruszuk
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
