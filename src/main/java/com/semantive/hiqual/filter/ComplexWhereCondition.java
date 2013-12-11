package com.semantive.hiqual.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Piotr Jędruszuk
 */
public class ComplexWhereCondition extends AbstractWhereCondition {

    private List<AbstractWhereCondition> whereConditions = new ArrayList<AbstractWhereCondition>();

    private LogicalOperator logicalOperator;

    public ComplexWhereCondition(LogicalOperator logicalOperator) {
        this.logicalOperator = logicalOperator;
    }

    public ComplexWhereCondition addWhereCondition(AbstractWhereCondition abstractWhereCondition) {
        whereConditions.add(abstractWhereCondition);
        return this;
    }

    public List<AbstractWhereCondition> getWhereConditions() {
        return Collections.unmodifiableList(whereConditions);
    }

    public LogicalOperator getLogicalOperator() {
        return logicalOperator;
    }
}
