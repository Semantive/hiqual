package com.semantive.hiqual;

import java.io.Serializable;

/**
 * @author Jacek Lewandowski
 */
public interface DataObject<ID_TYPE extends Serializable> extends Serializable {
    ID_TYPE getId();

    void setId(ID_TYPE id);

}
