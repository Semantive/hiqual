package com.semantive.hiqual.filter;

/**
 * @author Piotr Jędruszuk
 */
public class BetweenWhereCondition extends AbstractWhereCondition {

    private String propertyName;

    private String lowerBoundParameterName;

    private Object lowerBoundValue;

    private String upperBoundParameterName;

    private Object upperBoundValue;

    public BetweenWhereCondition(String propertyName, String lowerBoundParameterName, Object lowerBoundValue, String upperBoundParameterName, Object upperBoundValue) {
        this.propertyName = propertyName;
        this.lowerBoundParameterName = lowerBoundParameterName;
        this.lowerBoundValue = lowerBoundValue;
        this.upperBoundParameterName = upperBoundParameterName;
        this.upperBoundValue = upperBoundValue;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getLowerBoundParameterName() {
        return lowerBoundParameterName;
    }

    public void setLowerBoundParameterName(String lowerBoundParameterName) {
        this.lowerBoundParameterName = lowerBoundParameterName;
    }

    public Object getLowerBoundValue() {
        return lowerBoundValue;
    }

    public void setLowerBoundValue(Object lowerBoundValue) {
        this.lowerBoundValue = lowerBoundValue;
    }

    public String getUpperBoundParameterName() {
        return upperBoundParameterName;
    }

    public void setUpperBoundParameterName(String upperBoundParameterName) {
        this.upperBoundParameterName = upperBoundParameterName;
    }

    public Object getUpperBoundValue() {
        return upperBoundValue;
    }

    public void setUpperBoundValue(Object upperBoundValue) {
        this.upperBoundValue = upperBoundValue;
    }
}
