package com.semantive.hiqual;

import com.google.common.collect.Range;
import com.semantive.hiqual.filter.WhereConditions;
import com.semantive.hiqual.pagination.PageDefinition;
import org.hibernate.criterion.Order;

import java.util.List;
import java.util.Map;

/**
 * Konfiguracja listy wyników - zawiera komplet informacji o tym w jakiej postaci mają zostać zwrócone dane.
 *
 * @author Jacek Lewandowski
 */
public interface IResultSetConfig {

    PageDefinition pageDefinition();

    List<Order> orders();

    TextSearchExpressions textSearchExpressions();

    WhereConditions whereConditions();

    String searchString();

    Map<String, FetchableProperty> propertiesToFetch();

    Range<Integer> range();
}
