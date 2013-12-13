package com.semantive.hiqual.pagination;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * A bean which includes information about the current page when a pagination is used.
 *
 * @author Jacek Lewandowski
 */
public class PageDefinition implements Serializable {
    /**
     * Offset of the page.
     */
    public final int pageStart;

    /**
     * Number of records on the page.
     */
    public final int pageSize;

    public PageDefinition(Integer pageStart, Integer pageSize) {
        if (pageStart == null)
            this.pageStart = 0;
        else
            this.pageStart = pageStart;

        if (pageSize == null)
            this.pageSize = Integer.MAX_VALUE;
        else
            this.pageSize = pageSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PageDefinition that = (PageDefinition) o;
        return pageSize == that.pageSize && pageStart == that.pageStart;
    }

    @Override
    public int hashCode() {
        int result = pageStart;
        result = 31 * result + pageSize;
        return result;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("pageStart", pageStart)
                .append("pageSize", pageSize)
                .toString();
    }
}
