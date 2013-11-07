package com.semantive.hiqual;

import com.google.common.collect.Range;
import com.semantive.commons.functional.Option;
import com.semantive.hiqual.filter.*;
import com.semantive.hiqual.pagination.PageDefinition;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.validator.GenericValidator;
import org.hibernate.criterion.Order;

import java.util.*;

/**
 * Klasa narzędziowa do tworzenia konfiguracji wyników.
 *
 * @author Jacek Lewandowski
 */
// TODO doc
public class ResultSetConfigBuilder {

    private Integer offset;

    private Integer fetchSize;

    private List<Order> orders = new ArrayList<Order>();

    private TextSearchExpressions textSearchExpressions;

    private String searchString;

    private Map<String, FetchableProperty> propertiesToFetch;

    private WhereConditions whereConditions;

    public static final IResultSetConfig ALL = new ResultSetConfigBuilder().setAllResults().build();

    public static final IResultSetConfig FIRST = new ResultSetConfigBuilder().setOneResult().build();

    public static final IResultSetConfig COUNT_ONLY = new ResultSetConfigBuilder().setNoResult().build();

    public ResultSetConfig build() {
        PageDefinition pd = null;
        if (offset != null || fetchSize != null) {
            pd = new PageDefinition(offset != null ? offset : 0, fetchSize != null ? fetchSize : Integer.MAX_VALUE);
        }
        if (GenericValidator.isBlankOrNull(searchString)) searchString = null;

        return new ResultSetConfig(pd, orders, textSearchExpressions, whereConditions, propertiesToFetch, searchString);
    }

    public ResultSetConfigBuilder baseOnExisting(IResultSetConfig resultSetConfig) {
        if (resultSetConfig.pageDefinition() != null) {
            offset = resultSetConfig.pageDefinition().pageStart;
            fetchSize = resultSetConfig.pageDefinition().pageSize;
        } else {
            offset = null;
            fetchSize = null;
        }
        orders = resultSetConfig.orders();
        textSearchExpressions = resultSetConfig.textSearchExpressions();
        propertiesToFetch = resultSetConfig.propertiesToFetch();
        searchString = resultSetConfig.searchString();
        whereConditions = resultSetConfig.whereConditions();
        return this;
    }

    public ResultSetConfigBuilder setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public ResultSetConfigBuilder setFetchSize(Integer fetchSize) {
        this.fetchSize = fetchSize;
        return this;
    }

    public ResultSetConfigBuilder setPageDefinition(PageDefinition pageDefinition) {
        this.offset = pageDefinition.pageStart;
        this.fetchSize = pageDefinition.pageSize;
        return this;
    }

    public ResultSetConfigBuilder setOrder(Order order) {
        this.orders.clear();
        this.orders.add(order);
        return this;
    }

    public ResultSetConfigBuilder addOrder(Order order) {
        this.orders.add(order);
        return this;
    }

    public ResultSetConfigBuilder clearOrder() {
        this.orders.clear();
        return this;
    }

    public ResultSetConfigBuilder setTextSearchExpressions(TextSearchExpressions textSearchExpressions) {
        this.textSearchExpressions = textSearchExpressions;
        return this;
    }

    public ResultSetConfigBuilder setWhereConditions(WhereConditions whereConditions) {
        this.whereConditions = whereConditions;
        return this;
    }

    public ResultSetConfigBuilder setPropertiesToFetch(Set<String> propertiesToFetch) {
        this.propertiesToFetch = new HashMap<String, FetchableProperty>(propertiesToFetch.size());
        for (String prop : propertiesToFetch) addPropertyToFetch(prop);
        return this;
    }

    public ResultSetConfigBuilder setPropertiesToFetch(String... propertiesToFetch) {
        for (String prop : propertiesToFetch) addPropertyToFetch(prop);
        return this;
    }

    public ResultSetConfigBuilder setPropertiesToFetch(Map<String, String> propertiesToFetch) {
        for (Map.Entry<String, String> entry : propertiesToFetch.entrySet()) {
            addPropertyToFetch(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public ResultSetConfigBuilder addPropertyToFetch(String propertyName) {
        if (propertyName.startsWith("this."))
            addPropertyToFetch(propertyName, propertyName.substring(5));
        else
            addPropertyToFetch("this." + propertyName, propertyName);
        return this;
    }

    public ResultSetConfigBuilder addPropertyToFetch(String expression, String propertyName) {
        addPropertyToFetch(new FetchableProperty(expression, propertyName));
        return this;
    }


    public ResultSetConfigBuilder addPropertyToFetch(String expression, String propertyName, Option<String> join) {
        addPropertyToFetch(new FetchableProperty(expression, propertyName, join));
        return this;
    }


    public ResultSetConfigBuilder addPropertyToFetch(String propertyName, Option<String> join) {
        if (propertyName.startsWith("this."))
            addPropertyToFetch(propertyName, propertyName.substring(5), join);
        else
            addPropertyToFetch("this." + propertyName, propertyName, join);
        return this;
    }


    public ResultSetConfigBuilder addPropertyToFetch(FetchableProperty property) {
        if (this.propertiesToFetch == null) this.propertiesToFetch = new HashMap<String, FetchableProperty>();
        this.propertiesToFetch.put(property.getExpression(), property);
        return this;
    }


    public ResultSetConfigBuilder setOneResult() {
        this.offset = 0;
        this.fetchSize = 1;
        return this;
    }

    public ResultSetConfigBuilder setAllResults() {
        this.offset = 0;
        this.fetchSize = Integer.MAX_VALUE;
        return this;
    }

    public ResultSetConfigBuilder setNoResult() {
        this.offset = 0;
        this.fetchSize = 0;
        return this;
    }

    private Order toOrder(String sortField, String sortOrder) {
        if (!sortField.startsWith("this.")) sortField = "this." + sortField;

        Order order;
        if ("asc".equalsIgnoreCase(sortOrder)) {
            order = Order.asc(sortField);
        } else if ("desc".equalsIgnoreCase(sortOrder)) {
            order = Order.desc(sortField);
        } else {
            throw new IllegalArgumentException(String.format("Sort order must be either 'asc' or 'desc'. The given sort order for field %s is %s.", sortField, sortOrder));
        }

        return order;
    }

    public ResultSetConfigBuilder setOrder(String sortField, String sortOrder) {
        return setOrder(toOrder(sortField, sortOrder));
    }


    public ResultSetConfigBuilder addOrder(String sortField, String sortOrder) {
        return addOrder(toOrder(sortField, sortOrder));
    }

    public ResultSetConfigBuilder setSearchString(String searchString) {
        this.searchString = searchString;
        return this;
    }

    public TextSearchExpressions getTextSearchExpressions() {
        return this.textSearchExpressions;
    }

    public WhereConditions getWhereConditions() {
        return whereConditions;
    }

    public ResultSetConfigBuilder andCondition(AbstractWhereCondition condition) {
        WhereConditions wc = getWhereConditions();
        if (wc == null) {
            wc = new WhereConditions(condition);
        } else {
            wc = wc.setWhereCondition(new ComplexWhereCondition(LogicalOperator.AND).addWhereCondition(wc.getWhereCondition()).addWhereCondition(condition));
        }
        setWhereConditions(wc);
        return this;
    }

    public ResultSetConfigBuilder andNotCondition(AbstractWhereCondition condition) {
        return andCondition(new NotWhereCondition(condition));
    }

    /**
     * Ustawia filtr tak, że pobierany jest obiekt o podanym id. Istniejące filtry są usuwane.
     */
    public ResultSetConfigBuilder byId(Long id) {
        whereConditions = new WhereConditions(new SimpleWhereCondition("id", Operator.EQ, "entityId", id));
        return this;
    }

    /**
     * Ustawia filtr tak, że pobierane są obiekty o podanych identyfikatorach. Istniejące filtry są usuwane.
     */
    public ResultSetConfigBuilder byIds(Long... ids) {
        whereConditions = new WhereConditions(new SimpleWhereCondition("id", Operator.IN, "entityId", ids));
        return this;
    }

    /**
     * Ustawia filtr tak, że pobierane są obiekty o podanych identyfikatorach. Istniejące filtry są usuwane.
     */
    public ResultSetConfigBuilder byIds(Collection<Long> ids) {
        whereConditions = new WhereConditions(new SimpleWhereCondition("id", Operator.IN, "entityId", ids));
        return this;
    }

    /**
     * Ustawia filtr tak, że pobierany jest obiekt o podanym kodzie. Istniejące filtry są usuwane.
     */
    public ResultSetConfigBuilder byCode(String code) {
        whereConditions = new WhereConditions(new SimpleWhereCondition("code", Operator.EQ, "entityCode", code));
        return this;
    }

    /**
     * Ustawia filtr tak, że pobierane są obiekty o podanych kodach. Istniejące filtry są usuwane.
     */
    public ResultSetConfigBuilder byCodes(String... codes) {
        whereConditions = new WhereConditions(new SimpleWhereCondition("code", Operator.IN, "entityCode", codes));
        return this;
    }

    /**
     * Ustawia filtr tak, że pobierane są obiekty o podanych kodach. Istniejące filtry są usuwane.
     */
    public ResultSetConfigBuilder byCodes(Collection<String> codes) {
        whereConditions = new WhereConditions(new SimpleWhereCondition("code", Operator.IN, "entityCode", codes));
        return this;
    }

    public static ResultSetConfigBuilder baseOn(IResultSetConfig resultSetConfig) {
        return new ResultSetConfigBuilder().baseOnExisting(resultSetConfig);
    }


    private static class ResultSetConfig implements IResultSetConfig {
        private final PageDefinition _pageDefinition;

        private final List<Order> _orders;

        private final TextSearchExpressions _textSearchExpressions;

        private final WhereConditions _whereConditions;

        private final Map<String, FetchableProperty> _propertiesToFetch;

        private final String _searchString;

        public ResultSetConfig(PageDefinition pageDefinition, List<Order> orders, TextSearchExpressions textSearchExpressions, WhereConditions whereConditions, Map<String, FetchableProperty> propertiesToFetch, String searchString) {
            _pageDefinition = pageDefinition;
            _orders = orders;
            _textSearchExpressions = textSearchExpressions;
            _whereConditions = whereConditions;
            _propertiesToFetch = propertiesToFetch;
            _searchString = searchString;
        }

        @Override
        public Range<Integer> range() {
            if (_pageDefinition != null) {
                return Range.closedOpen(_pageDefinition.pageStart, _pageDefinition.pageStart + _pageDefinition.pageSize);
            } else {
                return Range.closedOpen(0, Integer.MAX_VALUE);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ResultSetConfig that = (ResultSetConfig) o;

            if (_orders != null ? !_orders.equals(that._orders) : that._orders != null) return false;
            if (_pageDefinition != null ? !_pageDefinition.equals(that._pageDefinition) : that._pageDefinition != null)
                return false;
            if (_propertiesToFetch != null ? !_propertiesToFetch.equals(that._propertiesToFetch) : that._propertiesToFetch != null)
                return false;
            if (_searchString != null ? !_searchString.equals(that._searchString) : that._searchString != null)
                return false;
            if (_textSearchExpressions != null ? !_textSearchExpressions.equals(that._textSearchExpressions) : that._textSearchExpressions != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = _pageDefinition != null ? _pageDefinition.hashCode() : 0;
            result = 31 * result + (_orders != null ? _orders.hashCode() : 0);
            result = 31 * result + (_textSearchExpressions != null ? _textSearchExpressions.hashCode() : 0);
            result = 31 * result + (_propertiesToFetch != null ? _propertiesToFetch.hashCode() : 0);
            result = 31 * result + (_searchString != null ? _searchString.hashCode() : 0);
            return result;
        }

        @Override
        public PageDefinition pageDefinition() {
            return _pageDefinition;
        }

        @Override
        public List<Order> orders() {
            return _orders;
        }

        @Override
        public TextSearchExpressions textSearchExpressions() {
            return _textSearchExpressions;
        }

        @Override
        public Map<String, FetchableProperty> propertiesToFetch() {
            return _propertiesToFetch;
        }

        @Override
        public String searchString() {
            return _searchString;
        }

        @Override
        public WhereConditions whereConditions() {
            return _whereConditions;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("_pageDefinition", _pageDefinition)
                    .append("_orders", _orders)
                    .append("_searchString", _searchString)
                    .append("_propertiesToFetch", _propertiesToFetch != null ? _propertiesToFetch.values() : null)
                    .toString();
        }
    }

}
