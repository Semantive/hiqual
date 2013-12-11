package com.semantive.hiqual.core;

import com.semantive.commons.Utils;
import com.semantive.hiqual.IResultSetConfig;
import com.semantive.hiqual.TextSearchExpressions;
import org.hibernate.Query;
import org.hibernate.criterion.Order;

import java.util.Iterator;

/**
 * @author Jacek Lewandowski
 */
public class HibernateQueryUtils {

    // TODO napisać do tego testy i dokumentację
    public static String lower(String str, boolean condition) {
        if (condition)
            return Utils.makeCall(str, "LOWER");
        else
            return str;
    }

    @Deprecated
    public static String generateAutocompleteQuery(String query, boolean caseSensitive, boolean beforeWildcard, boolean afterWildcard, String... fields) {
        if (query == null) return null;
        String[] tokens = Utils.tokenizeAndWildcard(query, beforeWildcard, afterWildcard, 10);

        if (tokens.length == 0 || fields.length == 0) return null;

        for (int i = 0; i < tokens.length; i++) {

            String[] conditionParts = new String[fields.length];
            for (int j = 0; j < fields.length; j++) {
                StringBuilder buf = new StringBuilder();
                buf.append("(").append(lower(fields[j], !caseSensitive)).append(" LIKE ").append(lower("'" + tokens[i] + "'", !caseSensitive)).append(")");
                conditionParts[j] = buf.toString();
            }

            tokens[i] = "(" + Utils.arrayToDelimitedString(conditionParts, " OR ") + ")";
        }

        return " (" + Utils.arrayToDelimitedString(tokens, " AND ") + ") ";
    }


    public static String generateAutocompleteQuery(IResultSetConfig resultSetConfig) {
        if (resultSetConfig == null || resultSetConfig.searchString() == null || resultSetConfig.textSearchExpressions() == null || resultSetConfig.textSearchExpressions().getPropertyExpressions().isEmpty())
            return null;

        boolean beforeWildcard = false;
        boolean afterWildcard = true;
        boolean caseSensitive = false;
        int tokenLimit = 5;

        String[] tokens = Utils.tokenizeAndWildcard(resultSetConfig.searchString(), beforeWildcard, afterWildcard, tokenLimit);

        if (tokens.length == 0) return null;

        for (int i = 0; i < tokens.length; i++) {

            String[] conditionParts = new String[resultSetConfig.textSearchExpressions().getPropertyExpressions().size()];
            Iterator<TextSearchExpressions.PropertyExpression> it = resultSetConfig.textSearchExpressions().getPropertyExpressions().iterator();
            for (int j = 0; j < conditionParts.length; j++) {
                StringBuilder buf = new StringBuilder();
                buf.append("(").append(lower(it.next().getExpression(), !caseSensitive)).append(" LIKE ").append(lower("'" + tokens[i] + "'", !caseSensitive)).append(")");
                conditionParts[j] = buf.toString();
            }

            tokens[i] = "(" + Utils.arrayToDelimitedString(conditionParts, " OR ") + ")";
        }

        return " (" + Utils.arrayToDelimitedString(tokens, " AND ") + ") ";
    }

    @Deprecated
    public static String formatDate(String fieldName) {
        return String.format(
                "(trim(str(year(%1$s))) " +
                        "|| '-' || (case when month(%1$s) < 10 then '0' else '' end) || trim(str(month(%1$s))) " +
                        "|| '-' || (case when day(%1$s) < 10 then '0' else '' end) || trim(str(day(%1$s))))", fieldName);
    }

    @Deprecated
    public static String formatId(String fieldName) {
        return String.format("'ID' || trim(str(%s))", fieldName);
    }

    public static String applySorting(IResultSetConfig config) {
        StringBuilder buf = new StringBuilder();
        if (config != null && config.orders() != null && !config.orders().isEmpty()) {
            buf.append(" order by ");
            boolean isFirst = true;
            for (Order order : config.orders()) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    buf.append(", ");
                }
                buf.append(order.toString());
            }
        }
        buf.append(" ");

        return buf.toString();
    }


    public static void applyPaging(Query query, IResultSetConfig config) {
        if (config != null && config.pageDefinition() != null) {
            query.setFirstResult(config.pageDefinition().pageStart);
            query.setFirstResult(config.pageDefinition().pageSize);
        }
    }

}
