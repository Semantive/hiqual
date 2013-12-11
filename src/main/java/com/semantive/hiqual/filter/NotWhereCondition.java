package com.semantive.hiqual.filter;

/**
 * @author Piotr Jędruszuk
 */
public class NotWhereCondition extends AbstractWhereCondition {

    private AbstractWhereCondition whereCondition;

    public NotWhereCondition() {
    }

    public NotWhereCondition(AbstractWhereCondition whereCondition) {
        this.whereCondition = whereCondition;
    }

    public AbstractWhereCondition getWhereCondition() {
        return whereCondition;
    }

    public void setWhereCondition(AbstractWhereCondition whereCondition) {
        this.whereCondition = whereCondition;
    }
}
