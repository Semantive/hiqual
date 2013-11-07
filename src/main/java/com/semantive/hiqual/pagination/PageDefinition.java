package com.semantive.hiqual.pagination;

import com.google.common.base.Objects;

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

    public PageDefinition(int pageStart, int pageSize) {
        this.pageStart = pageStart;
        this.pageSize = pageSize;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("pageStart", pageStart)
                .add("pageSize", pageSize)
                .toString();
    }
}
