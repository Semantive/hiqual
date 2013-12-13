package com.semantive.hiqual;

import com.google.common.collect.Range;
import com.semantive.hiqual.filter.WhereConditions;
import com.semantive.hiqual.pagination.PageDefinition;
import org.hibernate.criterion.Order;

import java.util.List;
import java.util.Map;

/**
 * Result set configuration interface. It provides information about which rows and which properties should be fetched. It acts like Hibernate criteria,
 * but it works on the higher level of abstraction.
 *
 * @author Jacek Lewandowski
 */
public interface IResultSetConfig {

    /**
     * Default config for fetching all the results.
     */
    public static final IResultSetConfig ALL = new ResultSetConfigBuilder().setAllResults().build();

    /**
     * Default config for fetching the first result.
     */
    public static final IResultSetConfig FIRST = new ResultSetConfigBuilder().setOneResult().build();

    /**
     * Default config for fetching no results - nevertheless, it retrieves the total number of elements.
     */
    public static final IResultSetConfig COUNT_ONLY = new ResultSetConfigBuilder().setNoResult().build();

    /**
     * @return page configuration
     */
    PageDefinition pageDefinition();

    /**
     * @return list of properties which the results should be sorted by
     */
    List<Order> orders();

    /**
     * @return full text search configuration
     */
    TextSearchExpressions textSearchExpressions();

    /**
     * @return additional filters
     */
    WhereConditions whereConditions();

    /**
     * @return search string, when full text search is used
     */
    String searchString();

    /**
     * @return map of properties which are to be fetched
     */
    Map<String, FetchableProperty> propertiesToFetch();

    /**
     * @return page configuration in a form of Range object
     */
    Range<Integer> range();
}
