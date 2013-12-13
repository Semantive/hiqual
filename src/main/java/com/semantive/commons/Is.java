package com.semantive.commons;

import com.semantive.hiqual.IDataObject;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serializable;
import java.util.*;

/**
 * @author Jacek Lewandowski
 */
public class Is {
    public static boolean collectionInterfaceOrArray(Class<?> c) {
        return c.isAssignableFrom(SortedSet.class) || c.isAssignableFrom(SortedMap.class) || c.isAssignableFrom(List.class) || c.isArray();
    }

    public static boolean equal(Object o1, Object o2, String propertyPath) {
        if (o1 != null && o2 != null) {
            if (o1 == o2) return true;

            PropertySetter ps1 = new PropertySetter(propertyPath, o1.getClass());
            PropertySetter ps2 = new PropertySetter(propertyPath, o2.getClass());

            return ObjectUtils.equals(ps1.getProperty(o1), ps2.getProperty(o2));
        } else {
            return o1 == o2;
        }
    }

    public static boolean nullValue(Object o, String propertyPath) {
        if (o == null) return true;
        PropertySetter ps = new PropertySetter(propertyPath, o.getClass());
        return ps.getProperty(o) == null;
    }

    /**
     * Sprawdza czy wsrod obiektow znajduje sie obiekt
     *
     * @param what  obiekt szukany
     * @param where obiekty wsrod ktorych jest poszukiwany
     * @return true jeśli znajdziemy obiekt, false jesli nie znajdziemy.
     */
    public static boolean included(Object what, Object... where) {
        for (Object o : where) {
            if (o.equals(what)) {
                return true;
            }
        }
        return false;
    }

    public static boolean proxySafeEqual(Object first, Object second) {
        return ObjectUtils.equals(Utils.proxySafeCast(first), Utils.proxySafeCast(second));
    }

    public static <T extends Serializable> boolean idNotNull(IDataObject<T> dataObject) {
        return dataObject != null && dataObject.getId() != null;
    }

    public static <T, V> boolean anyKeyIncluded(Map<T, V> map, Collection<T> keys) {
        for (T key : keys) {
            if (map.containsKey(key)) return true;
        }
        return false;
    }


}
