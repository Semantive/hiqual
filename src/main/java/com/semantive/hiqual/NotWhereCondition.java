package com.semantive.hiqual;

/**
 * <br/>User: Piotr Jędruszuk (pjedruszuk@semantive.pl)
 * <br/>Date: 09.05.13 16:06
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
