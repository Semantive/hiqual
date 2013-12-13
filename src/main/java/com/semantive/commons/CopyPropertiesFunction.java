package com.semantive.commons;

import com.semantive.commons.functional.F2;
import org.apache.commons.collections.MapUtils;

/**
 * @author Jacek Lewandowski
 */
public class CopyPropertiesFunction<FromType, ToType> implements F2<FromType, ToType, Void> {
    private PropertySetter<FromType>[] readers;

    private PropertySetter<ToType>[] writers;

    public CopyPropertiesFunction(Class<FromType> fromClass, Class<ToType> toClass, String... properties) {
        readers = new PropertySetter[properties.length];

        for (int i = 0; i < properties.length; i++) {
            readers[i] = new PropertySetter<FromType>(properties[i], fromClass, MapUtils.EMPTY_MAP);
        }
        if (fromClass.equals(toClass)) {
            writers = (PropertySetter<ToType>[]) readers;
        } else {
            writers = new PropertySetter[properties.length];
            for (int i = 0; i < properties.length; i++) {
                writers[i] = new PropertySetter<ToType>(properties[i], toClass, MapUtils.EMPTY_MAP);
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
