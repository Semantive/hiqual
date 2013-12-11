package com.semantive.hiqual.filter;

/**
 * @author Piotr Jędruszuk
 */
public class SimpleWhereCondition extends AbstractWhereCondition {

    private String propertyName;

    private Operator operator;

    private String parameterName;

    private Object value;

    public SimpleWhereCondition(String propertyName, Operator operator, String parameterName, Object value) {
        this.propertyName = propertyName;
        this.operator = operator;
        this.parameterName = parameterName;
        this.value = value;
    }

    public SimpleWhereCondition(String propertyName, Operator operator, Object value) {
        this.propertyName = propertyName;
        this.operator = operator;
        this.parameterName = propertyName.replace('.', '_');
        this.value = value;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
