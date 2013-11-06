package com.semantive.hiqual;

import java.io.Serializable;

/**
 * A bean which includes information about the current page when a pagination is used.
 *
 * @author Jacek Lewandowski
 */
public class PageDefinition implements Serializable {
    private int pageStart;

    private int pageSize;

    public PageDefinition(int pageStart, int pageSize) {
        this.pageStart = pageStart;
        this.pageSize = pageSize;
    }

    public int getPageStart() {
        return pageStart;
    }

    public void setPageStart(int pageStart) {
        this.pageStart = pageStart;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }


    public String toString() {
        return "PageDefinition{" +
                "pageStart=" + pageStart +
                ", pageSize=" + pageSize +
                '}';
    }
}
