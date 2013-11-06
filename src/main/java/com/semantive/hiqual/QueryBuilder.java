package com.semantive.hiqual;

import com.semantive.commons.Utils;
import com.semantive.commons.functional.Void1;
import org.apache.commons.validator.GenericValidator;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

import java.lang.reflect.Array;
import java.util.*;

/**
 * @author Jacek Lewandowski
 */
public class QueryBuilder<T> {

    private final IResultSetConfig resultSetConfig;

    private final Class<? extends T> targetEntityClass;

    private LinkedHashMap<String, String> substitutions;

    private Map<String, String> expressionsByPropertyName;

    private Map<String, String> expressionsInSearchByPropertyName;

    private Map<String, String> expressionsInWhereByPropertyName;

    private String fromClause;

    private String whereClauseCustomPart;

    private String defaultSelectClause;

    private List<String> whereClauseConditions;

    private Map<String, Object> parametersToSet;

    private Order defaultOrder;

    private boolean propertyExpressionsInitialized = false;

    private Map<String, Class> replacements;

    private Map<String, String> additionalProperties;

    private LinkedHashSet<String> leftOuterJoins;

    public QueryBuilder(Class<? extends T> targetEntityClass, IResultSetConfig config) {
        this.targetEntityClass = targetEntityClass;
        this.resultSetConfig = config;
    }

    private void initializePropertyExpressions() {
        if (!propertyExpressionsInitialized) {
            expressionsByPropertyName = null;
            if (resultSetConfig != null) {
                if (resultSetConfig.propertiesToFetch() != null) {
                    for (FetchableProperty fetchableProperty : resultSetConfig.propertiesToFetch().values()) {
                        fetchableProperty.getJoin().forEach(new Void1<String>() {
                            @Override
                            public void apply(String s) {
                                addLeftOuterJoin(withThis(s));
                            }
                        });
                    }

                    for (String propExpr : resultSetConfig.propertiesToFetch().keySet()) {
                        addPropertyExpression(propExpr, transformPropertyExpression(propExpr));
                    }
                }
                if (resultSetConfig.textSearchExpressions() != null) {
                    for (TextSearchExpressions.PropertyExpression propertyExpression : resultSetConfig.textSearchExpressions().getPropertyExpressions()) {
                        addPropertyInSearchExpression(propertyExpression.getName(), transformPropertyExpression(propertyExpression.getExpression()));
                    }
                }

                if (resultSetConfig.whereConditions() != null) {
                    processWhereConditionForPropertyExpressions(resultSetConfig.whereConditions().getWhereCondition());
                }

                if (resultSetConfig.orders() != null) {
                    for (Order order : resultSetConfig.orders()) {
                        addPropertyExpression(order.toString(), transformPropertyExpression(withThis(order.toString())));
                    }
                }
            }
            propertyExpressionsInitialized = true;
        }
    }


    private String withThis(String propertyName) {
        return propertyName.startsWith("this.") ? propertyName : ("this." + propertyName);
    }

    private String withoutThis(String propertyName) {
        return propertyName.startsWith("this.") ? propertyName.substring(5) : propertyName;
    }


    private void processWhereConditionForPropertyExpressions(AbstractWhereCondition abstractWhereCondition) {
        if (abstractWhereCondition instanceof SimpleWhereCondition) {
            SimpleWhereCondition simpleWhereCondition = (SimpleWhereCondition) abstractWhereCondition;
            addPropertyInWhereExpression(withoutThis(simpleWhereCondition.getPropertyName()), transformPropertyExpression(withThis(simpleWhereCondition.getPropertyName())));
        } else if (abstractWhereCondition instanceof ComplexWhereCondition) {
            ComplexWhereCondition complexWhereCondition = (ComplexWhereCondition) abstractWhereCondition;
            for (AbstractWhereCondition condition : complexWhereCondition.getWhereConditions()) {
                processWhereConditionForPropertyExpressions(condition);
            }
        } else if (abstractWhereCondition instanceof NotWhereCondition) {
            AbstractWhereCondition notWhereConditionContent = ((NotWhereCondition) abstractWhereCondition).getWhereCondition();
            processWhereConditionForPropertyExpressions(notWhereConditionContent);
        } else if (abstractWhereCondition instanceof BetweenWhereCondition) {
            BetweenWhereCondition betweenWhereCondition = (BetweenWhereCondition) abstractWhereCondition;
            addPropertyInWhereExpression(withoutThis(betweenWhereCondition.getPropertyName()), transformPropertyExpression(withThis(betweenWhereCondition.getPropertyName())));
        }
    }

    public QueryBuilder<T> addPropertyClassReplacement(String path, Class clazz) {
        if (replacements == null) replacements = new HashMap<String, Class>();
        replacements.put(path, clazz);
        return this;
    }

    public QueryBuilder<T> addDefaultSubstitution(String defaultEntityName) {
        return addSubstitution("\\bthis\\.([\\w\\.]+)\\b", defaultEntityName + ".$1");
    }

    public QueryBuilder<T> addSubstitution(String from, String to) {
        if (this.substitutions == null) this.substitutions = new LinkedHashMap<String, String>();
        this.substitutions.put(from, to);
        propertyExpressionsInitialized = false;
        return this;
    }

    public QueryBuilder<T> setFromClause(String fromClause) {
        this.fromClause = fromClause;
        return this;
    }

    public QueryBuilder<T> setWhereClauseCustomPart(String whereClauseCustomPart) {
        this.whereClauseCustomPart = whereClauseCustomPart;
        return this;
    }

    public QueryBuilder<T> setDefaultSelectClause(String defaultSelectClause) {
        this.defaultSelectClause = defaultSelectClause;
        return this;
    }

    public QueryBuilder<T> addUnconditionalWhereCondition(String condition) {
        if (whereClauseConditions == null) whereClauseConditions = new ArrayList<String>();
        whereClauseConditions.add(condition);
        return this;
    }

    public QueryBuilder<T> addConditionalWhereCondition(String condition, String name, Object value) {
        if (value != null) {
            if (whereClauseConditions == null) whereClauseConditions = new ArrayList<String>();
            whereClauseConditions.add(condition);
            if (parametersToSet == null) parametersToSet = new HashMap<String, Object>();
            parametersToSet.put(name, value);
        }
        return this;
    }

    public QueryBuilder<T> setDefaultOrder(Order defaultOrder) {
        this.defaultOrder = defaultOrder;
        return this;
    }

    public QueryBuilder<T> addAdditionalProperty(String expression, String alias) {
        if (this.additionalProperties == null) this.additionalProperties = new HashMap<String, String>();
        this.additionalProperties.put(expression, alias);
        return this;
    }

    public QueryBuilder<T> addLeftOuterJoin(String joinProperty) {
        if (this.leftOuterJoins == null) this.leftOuterJoins = new LinkedHashSet<String>();
        String transformed = transformPropertyExpression(joinProperty);
        this.leftOuterJoins.add(transformed);
        String what = "^" + transformed.replaceAll("\\.", "\\\\.") + "\\.";
        String to = joinPropertyToAlias(transformed) + ".";
        addSubstitution(what, to);
        return this;
    }

    private String joinPropertyToAlias(String joinProperty) {
        return "_" + joinProperty.replaceAll("\\.", "_");
    }

    private void addPropertyExpression(String prop, String expr) {
        if (this.expressionsByPropertyName == null) this.expressionsByPropertyName = new HashMap<String, String>();
        this.expressionsByPropertyName.put(prop, expr);
    }

    private void addPropertyInSearchExpression(String prop, String expr) {
        if (this.expressionsInSearchByPropertyName == null)
            this.expressionsInSearchByPropertyName = new HashMap<String, String>();
        this.expressionsInSearchByPropertyName.put(prop, expr);
    }

    private void addPropertyInWhereExpression(String prop, String expr) {
        if (this.expressionsInWhereByPropertyName == null)
            this.expressionsInWhereByPropertyName = new HashMap<String, String>();
        this.expressionsInWhereByPropertyName.put(prop, expr);
    }

    private String transformPropertyExpression(String propertyExpression) {
        String result = propertyExpression;
        if (substitutions != null) for (Map.Entry<String, String> substitution : substitutions.entrySet()) {
            result = result.replaceAll(substitution.getKey(), substitution.getValue());
        }
        return result;
    }

    private String transformPropertyNameToAlias(String propertyName) {
        if (propertyName.contains("_"))
            throw new IllegalArgumentException("Property name cannot include any underscore.");
        return propertyName.replace('.', '_');
    }

    public String generateAutocompleteCondition() {
        initializePropertyExpressions();
        if (resultSetConfig == null || resultSetConfig.searchString() == null || resultSetConfig.textSearchExpressions() == null || resultSetConfig.textSearchExpressions().getPropertyExpressions().isEmpty())
            return null;

        boolean beforeWildcard = true;
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
                buf.append("(").append(HibernateQueryUtils.lower(expressionsInSearchByPropertyName.get(it.next().getName()), !caseSensitive)).append(" LIKE ").append(HibernateQueryUtils.lower("'" + tokens[i] + "'", !caseSensitive)).append(")");
                conditionParts[j] = buf.toString();
            }

            tokens[i] = "(" + Utils.arrayToDelimitedString(conditionParts, " OR ") + ")";
        }

        return " (" + Utils.arrayToDelimitedString(tokens, " AND ") + ") ";
    }

    public String generateSelectClause() {
        initializePropertyExpressions();
        StringBuilder buf = new StringBuilder(" ");
        if (resultSetConfig != null && resultSetConfig.propertiesToFetch() != null && !resultSetConfig.propertiesToFetch().isEmpty()) {
            buf.append("select ");
            Iterator<String> it = resultSetConfig.propertiesToFetch().keySet().iterator();
            while (it.hasNext()) {
                String propName = it.next();
                buf.append(expressionsByPropertyName.get(propName)).append(" as ").append(transformPropertyNameToAlias(resultSetConfig.propertiesToFetch().get(propName).getName()));
                if (it.hasNext() || (additionalProperties != null && !additionalProperties.isEmpty())) buf.append(", ");
            }
            if (additionalProperties != null) {
                Iterator<Map.Entry<String, String>> it2 = additionalProperties.entrySet().iterator();
                while (it2.hasNext()) {
                    Map.Entry<String, String> additionalProp = it2.next();
                    buf.append(additionalProp.getKey()).append(" as ").append(transformPropertyNameToAlias(additionalProp.getValue()));
                    if (it2.hasNext()) buf.append(", ");
                }
            }
            buf.append(" ");
        } else {
            buf.append(defaultSelectClause).append(" ");
        }

        return buf.toString();
    }

    public String generateWhereClause() {
        initializePropertyExpressions();
        StringBuilder buf = new StringBuilder(" where ");
        buf.append(" 1 = 1 ");
        if (whereClauseConditions != null) for (String condition : whereClauseConditions) {
            buf.append(" and ").append(condition).append(" ");
        }

        if (resultSetConfig.whereConditions() != null) {
            buf.append(" and ");
            processWhereCondition(resultSetConfig.whereConditions().getWhereCondition(), buf);
        }

        String autocompleteCondition = generateAutocompleteCondition();
        if (autocompleteCondition != null) {
            buf.append(" and ").append(autocompleteCondition).append(" ");
        }
        if (!GenericValidator.isBlankOrNull(whereClauseCustomPart)) {
            buf.append(" and (").append(whereClauseCustomPart).append(") ");
        }
        return buf.toString();
    }

    private void processWhereCondition(AbstractWhereCondition condition, StringBuilder buf) {
        if (condition instanceof SimpleWhereCondition) {
            processSimpleWhereCondition((SimpleWhereCondition) condition, buf);
        } else if (condition instanceof ComplexWhereCondition) {
            processComplexWhereCondition((ComplexWhereCondition) condition, buf);
        } else if (condition instanceof NotWhereCondition) {
            processNotWhereCondition((NotWhereCondition) condition, buf);
        } else if (condition instanceof BetweenWhereCondition) {
            processBetweenWhereCondition((BetweenWhereCondition) condition, buf);
        }
    }

    private void processBetweenWhereCondition(BetweenWhereCondition condition, StringBuilder buf) {
        buf.append(expressionsInWhereByPropertyName.get(withoutThis(condition.getPropertyName())))
                .append(" between ")
                .append(":").append(condition.getLowerBoundParameterName())
                .append(" and ")
                .append(":").append(condition.getUpperBoundParameterName());
        if (parametersToSet == null) parametersToSet = new HashMap<String, Object>();
        parametersToSet.put(condition.getLowerBoundParameterName(), condition.getLowerBoundValue());
        parametersToSet.put(condition.getUpperBoundParameterName(), condition.getUpperBoundValue());
    }

    private void processNotWhereCondition(NotWhereCondition condition, StringBuilder buf) {
        buf.append(" not(");
        processWhereCondition(condition.getWhereCondition(), buf);
        buf.append(")");
    }

    private void processComplexWhereCondition(ComplexWhereCondition condition, StringBuilder buf) {
        List<AbstractWhereCondition> conditions = condition.getWhereConditions();
        Iterator<AbstractWhereCondition> iterator = conditions.iterator();

        buf.append("(");
        while (iterator.hasNext()) {
            AbstractWhereCondition subCondition = iterator.next();
            processWhereCondition(subCondition, buf);
            if (iterator.hasNext()) {
                buf.append(" ").append(condition.getLogicalOperator().name().toLowerCase()).append(" ");
            }
        }
        buf.append(")");
    }

    private void processSimpleWhereCondition(SimpleWhereCondition condition, StringBuilder buf) {
        if (condition.getValue() != null) {
            buf.append(String.format(condition.getOperator().getRepresentation(),
                    expressionsInWhereByPropertyName.get(withoutThis(condition.getPropertyName())),
                    ":" + condition.getParameterName()));
            if (parametersToSet == null) parametersToSet = new HashMap<String, Object>();
            parametersToSet.put(condition.getParameterName(), condition.getValue());
        } else if (condition.getValue() == null && condition.getOperator() == Operator.EQ) {
            buf.append(expressionsInWhereByPropertyName.get(withoutThis(condition.getPropertyName())))
                    .append(" IS NULL ");
        } else if (condition.getValue() == null && condition.getOperator() == Operator.NOT_EQ) {
            buf.append(expressionsInWhereByPropertyName.get(withoutThis(condition.getPropertyName())))
                    .append(" IS NOT NULL ");
        } else {
            throw new NullPointerException("Value cannot be null for operators other than EQ and NOT_EQ.");
        }
    }

    public String generateOrderByClause() {
        initializePropertyExpressions();
        StringBuilder buf = new StringBuilder();
        if (resultSetConfig != null && resultSetConfig.orders() != null && !resultSetConfig.orders().isEmpty()) {
            buf.append(" order by ");
            boolean isFirst = true;
            for (Order order : resultSetConfig.orders()) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    buf.append(", ");
                }
                buf.append(expressionsByPropertyName.get(order.toString()));
            }
        } else if (defaultOrder != null) {
            buf.append(" order by ").append(defaultOrder.toString()).append(" ");
        }
        buf.append(" ");

        return buf.toString();
    }

    public String generateFromClause() {
        StringBuilder buf = new StringBuilder();
        buf.append(" ").append(fromClause).append(" ");
        if (leftOuterJoins != null) {
            for (String leftOuterJoin : leftOuterJoins) {
                buf.append(" left outer join ").append(leftOuterJoin).append(" as ").append(joinPropertyToAlias(leftOuterJoin)).append(" ");
            }
        }

        return buf.toString();
    }

    public Query generateMainQuery(Session session) {
        initializePropertyExpressions();
        StringBuilder buf = new StringBuilder();

        buf.append(generateSelectClause())
                .append(generateFromClause())
                .append(generateWhereClause())
                .append(generateOrderByClause());

        System.out.println("Query: " + buf.toString());
        Query query = session.createQuery(buf.toString());

        if (resultSetConfig != null && resultSetConfig.pageDefinition() != null) {
            query.setFirstResult(resultSetConfig.pageDefinition().getPageStart());
            query.setMaxResults(resultSetConfig.pageDefinition().getPageSize());
        }

        if (parametersToSet != null) {
            for (Map.Entry<String, Object> paramDef : parametersToSet.entrySet()) {
                setParameterInternal(query, paramDef);
            }
        }

        if (resultSetConfig.propertiesToFetch() != null && !resultSetConfig.propertiesToFetch().isEmpty())
            query.setResultTransformer(CustomResultTransformer.build(targetEntityClass).usePathSeparator(CustomResultTransformer.UNDERSCORE).setClassReplacements(replacements));

        return query;
    }

    private void setParameterInternal(Query query, Map.Entry<String, Object> paramDef) {
        if (paramDef.getValue() != null && paramDef.getValue() instanceof Collection) {
            if (((Collection) paramDef.getValue()).isEmpty()) {
                query.setParameterList(paramDef.getKey(), new Object[]{null});
            } else {
                query.setParameterList(paramDef.getKey(), (Collection) paramDef.getValue());
            }
        } else if (paramDef.getValue() != null && paramDef.getValue().getClass().isArray()) {
            if (Array.getLength(paramDef.getValue()) == 0) {
                query.setParameterList(paramDef.getKey(), new Object[]{null});
            } else {
                query.setParameterList(paramDef.getKey(), (Object[]) paramDef.getValue());
            }
        } else {
            query.setParameter(paramDef.getKey(), paramDef.getValue());
        }
    }

    public Query generateCountQuery(Session session) {
        initializePropertyExpressions();
        StringBuilder buf = new StringBuilder();

        buf.append("select count(*) ")
                .append(generateFromClause())
                .append(generateWhereClause());

        Query query = session.createQuery(buf.toString());

        if (parametersToSet != null) {
            for (Map.Entry<String, Object> paramDef : parametersToSet.entrySet()) {
                setParameterInternal(query, paramDef);
            }
        }

        return query;
    }

    public PaginationAwareListWrapper<T> generatePaginatedList(Session session) {
        initializePropertyExpressions();
        Query mainQuery = generateMainQuery(session);

        if (resultSetConfig != null && resultSetConfig.pageDefinition() != null) {
            Query countQuery = generateCountQuery(session);
            int totalSize = ((Number) countQuery.uniqueResult()).intValue();
            return new PaginationAwareListWrapper<T>(mainQuery.list(), resultSetConfig.pageDefinition().getPageStart(), totalSize);
        } else {
            List<T> list = mainQuery.list();
            return new PaginationAwareListWrapper<T>(list, 0, list.size());
        }
    }

    public List<T> generatePlainList(Session session) {
        initializePropertyExpressions();
        Query mainQuery = generateMainQuery(session);
        return mainQuery.list();
    }

    public QueryBuilder<T> setParameter(String parameterName, Object parameterValue) {
        if (parametersToSet == null) parametersToSet = new HashMap<String, Object>();
        parametersToSet.put(parameterName, parameterValue);
        return this;
    }

    public QueryBuilder<T> setMainQueryParameter(String parameterName, Object parameterValue) {
        if (parametersToSet == null) parametersToSet = new HashMap<String, Object>();
        parametersToSet.put(parameterName, parameterValue);
        if (whereClauseConditions == null) whereClauseConditions = new ArrayList<String>();
        whereClauseConditions.add(String.format(":%s = :%s", parameterName, parameterName));
        return this;
    }


}
