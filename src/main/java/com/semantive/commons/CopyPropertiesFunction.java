package com.semantive.commons;

import com.semantive.commons.functional.F2;
import org.apache.commons.collections.MapUtils;

/**
 * @author Jacek Lewandowski
 */
public class CopyPropertiesFunction<FromType, ToType> implements F2<FromType, ToType, Void> {
    private PropertyAccessor<FromType>[] readers;

    private PropertyAccessor<ToType>[] writers;

    public CopyPropertiesFunction(Class<FromType> fromClass, Class<ToType> toClass, String... properties) {
        readers = new PropertyAccessor[properties.length];

        for (int i = 0; i < properties.length; i++) {
            readers[i] = new PropertyAccessor<FromType>(properties[i], fromClass, MapUtils.EMPTY_MAP);
        }
        if (fromClass.equals(toClass)) {
            writers = (PropertyAccessor<ToType>[]) readers;
        } else {
            writers = new PropertyAccessor[properties.length];
            for (int i = 0; i < properties.length; i++) {
                writers[i] = new PropertyAccessor<ToType>(properties[i], toClass, MapUtils.EMPTY_MAP);
            }
        }
    }

    @Override
    public Void apply(FromType from, ToType to) {
        for (int i = 0; i < readers.length; i++) {
            try {
                writers[i].setProperty(to, readers[i].getProperty(from));
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }

        return null;
    }
}
