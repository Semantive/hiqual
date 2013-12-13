package com.semantive.hiqual.pagination;

/**
 * Interface implemented by the collections which contains a single page of elements rather than all of them.
 *
 * @author Jacek Lewandowski
 */
public interface IPaginationAware {
    /**
     * @return returns the total number of elements - not only on the page represented by this object.
     */
    int totalSize();

    /**
     * @return returns the offset of this partial collection in the collection of all elements.
     */
    int offset();
}
