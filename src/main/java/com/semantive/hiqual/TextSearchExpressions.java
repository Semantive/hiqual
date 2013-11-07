package com.semantive.hiqual;

import com.google.common.base.Function;
import com.semantive.commons.IDescribed;
import com.semantive.commons.functional.F1;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Jacek Lewandowski
 */
public class TextSearchExpressions implements Serializable {

    private Set<PropertyExpression> propertyExpressions = new HashSet<PropertyExpression>();

    public static enum PropertyExpressionTypeDict implements IDescribed {

        PLAIN, DATE, ID, NUMERIC, DICT;

        @Override
        public String getDescriptionKey() {
            return "dict." + getDeclaringClass().getSimpleName() + "." + name();
        }
    }

    public static class PropertyExpression {
        private final String name;
        private final String expression;
        private final PropertyExpressionTypeDict type;

        protected PropertyExpression(String name, String expression, PropertyExpressionTypeDict type) {
            this.name = name;
            this.expression = expression;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public String getExpression() {
            return expression;
        }

        public PropertyExpressionTypeDict getType() {
            return type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PropertyExpression that = (PropertyExpression) o;

            return name.equals(that.name) && type == that.type;
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + type.hashCode();
            return result;
        }
    }

    public TextSearchExpressions addProperty(PropertyExpression pe) {
        propertyExpressions.add(pe);
        return this;
    }

    public TextSearchExpressions addPlainProperty(String name) {
        PropertyExpression pe = new PropertyExpression(name, "this." + name, PropertyExpressionTypeDict.PLAIN);
        return addProperty(pe);
    }

    public TextSearchExpressions addNumericProperty(String name) {
        PropertyExpression pe = new PropertyExpression(name, "str(this." + name + ")", PropertyExpressionTypeDict.NUMERIC);
        return addProperty(pe);
    }

    public TextSearchExpressions addDateProperty(String name) {
        PropertyExpression pe = new PropertyExpression(name, String.format(
                "(trim(str(year(this.%1$s))) " +
                        "|| '-' || (case when month(this.%1$s) < 10 then '0' else '' end) || trim(str(month(this.%1$s))) " +
                        "|| '-' || (case when day(this.%1$s) < 10 then '0' else '' end) || trim(str(day(this.%1$s))))", name), PropertyExpressionTypeDict.DATE);
        return addProperty(pe);
    }

    @Deprecated
    public <T extends Enum> TextSearchExpressions addDictProperty(String name, Class<T> dict, Function<T, String> translator) {
        T[] enums;
        try {
            Method m = dict.getMethod("values");
            enums = (T[]) m.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Cannot add dict property %s to TextSearchExpression for dict %s.", name, dict.getName()), e);
        }

        StringBuilder buf = new StringBuilder();
        buf.append("(case ").append("this.").append(name);
        for (T anEnum : enums) {
            buf.append(" when '").append(anEnum).append("' then '").append(translator.apply(anEnum)).append("' ");
        }
        buf.append(" end)");
        PropertyExpression pe = new PropertyExpression(name, buf.toString(), PropertyExpressionTypeDict.DICT);
        return addProperty(pe);
    }

    public <T extends Enum> TextSearchExpressions addDictProperty(String name, Class<T> dict, final F1<String, String> dictionary) {
        T[] enums;
        try {
            Method m = dict.getMethod("values");
            enums = (T[]) m.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Cannot add dict property %s to TextSearchExpression for dict %s.", name, dict.getName()), e);
        }

        StringBuilder buf = new StringBuilder();
        buf.append("(case ").append("this.").append(name);

        Function<IDescribed, String> function = new Function<IDescribed, String>() {
            @Override
            public String apply(IDescribed from) {
                return dictionary.apply(from.getDescriptionKey());
            }
        };

        for (T anEnum : enums) {
            buf.append(" when '").append(anEnum).append("' then '").append(function.apply((IDescribed) anEnum)).append("' ");
        }
        buf.append(" end)");
        PropertyExpression pe = new PropertyExpression(name, buf.toString(), PropertyExpressionTypeDict.DICT);
        return addProperty(pe);
    }

    public TextSearchExpressions addIdProperty(String name) {
        PropertyExpression pe = new PropertyExpression(name, String.format("'ID' || trim(str(this.%s))", name), PropertyExpressionTypeDict.ID);
        return addProperty(pe);
    }

    public Set<PropertyExpression> getPropertyExpressions() {
        return propertyExpressions;
    }
}
