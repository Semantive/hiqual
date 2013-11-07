package com.semantive.commons;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Jacek Lewandowski
 */

// TODO testy i dokumentacja
public class PropertySetter<T> {
    private Method[] writeMethods;
    private Method[] readMethods;
    private Class[] classes;
    private int hash;
    private boolean traverseCollections;
    private boolean setEmptyCollectionIfNull;
    private String[] pathElems;
    private Class<T> modelClass;

    public PropertySetter(String path, Class<T> c) {
        this(path, c, MapUtils.EMPTY_MAP);
    }

    public PropertySetter(String path, Class<T> c, Map<Integer, Class> classesMapping) {
        this(path, c, "\\.", classesMapping);
    }

    public PropertySetter(String path, Class<T> c, String pathSeparator, Map<Integer, Class> classesMapping) {
        this(path, c, pathSeparator, classesMapping, null);
    }

    public PropertySetter(String path, Class<T> c, String pathSeparator, Map<Integer, Class> classesMapping, Boolean setEmptyCollectionIfNull) {
        this.modelClass = c;
        if (setEmptyCollectionIfNull != null) {
            this.traverseCollections = true;
            this.setEmptyCollectionIfNull = setEmptyCollectionIfNull;
        }
        pathElems = path.split(pathSeparator);
        writeMethods = new Method[pathElems.length];
        readMethods = new Method[pathElems.length];
        classes = new Class[pathElems.length];
        hash = Arrays.hashCode(readMethods);

        // System.out.println(String.format("\nInitializing property setter for path %s and class %s.", path, c.getName()));
        try {
            for (Map.Entry<Integer, Class> entry : classesMapping.entrySet())
                if (entry.getKey() < classes.length) classes[entry.getKey()] = entry.getValue();

            Object o = c.newInstance();
            for (int i = 0; i < pathElems.length; i++) {
                PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(o, pathElems[i]);
                writeMethods[i] = pd.getWriteMethod();
                readMethods[i] = pd.getReadMethod();


                // System.out.println(String.format("Property descriptor for path element %s is: class %s, read method %s, write method %s", pathElems[i], pd.getPropertyType().getName(), pd.getReadMethod(), pd.getWriteMethod()));
                if (classes[i] != null) {
                    if (!pd.getPropertyType().isAssignableFrom(classes[i])) {
                        throw new IllegalArgumentException(String.format("Property %s:%s in path %s is not assignable from %s.", pathElems[i], pd.getPropertyType().getCanonicalName(), path, classes[i].getCanonicalName()));
                    }
                } else {
                    classes[i] = pd.getPropertyType();
                }
                if (i < pathElems.length - 1) {
                    o = classes[i].newInstance();
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(String.format("Failed to create the PropertySetter for class %s and path %s.", c, path), ex);
        }
    }


    /**
     * Ustawia wartość atrybutu na podaną wartość w podanym obiekcie modelowym.
     *
     * @param bean  obiekt modelowy
     * @param value wartość
     */
    public void setProperty(Object bean, Object value) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        int i = 0;
        for (; i < readMethods.length - 1; i++) {
            Object v = readMethods[i].invoke(bean);
            if (v == null) {
                if (value == null)
                    return;
                else
                    break;
            }
            bean = v;
        }
        for (; i < readMethods.length - 1; i++) {
            Object v = classes[i].newInstance();
            writeMethods[i].invoke(bean, v);
            bean = v;
        }
        try {
            if (traverseCollections && Utils.isCollectionInterfaceOrArray(classes[classes.length - 1])) {
                Class<?> c = classes[classes.length - 1];

                if (value != null || setEmptyCollectionIfNull) {
                    int size = (value != null) ? Utils.getCollectionOrArraySize(value) : 0;
                    if (c.isAssignableFrom(Set.class)) {
                        Set set = new HashSet(size);
                        Utils.fillCollection(set, value);
                        writeMethods[writeMethods.length - 1].invoke(bean, set);

                    } else if (c.isAssignableFrom(SortedSet.class)) {
                        SortedSet set = new TreeSet();
                        Utils.fillCollection(set, value);
                        writeMethods[writeMethods.length - 1].invoke(bean, set);

                    } else if (c.isAssignableFrom(List.class)) {
                        List list = new ArrayList(size);
                        Utils.fillCollection(list, value);
                        writeMethods[writeMethods.length - 1].invoke(bean, list);

                    } else if (c.isAssignableFrom(Map.class)) {
                        Map map = new HashMap(size);
                        Utils.fillMap(map, value);
                        writeMethods[writeMethods.length - 1].invoke(bean, map);

                    } else if (c.isAssignableFrom(SortedMap.class)) {
                        SortedMap map = new TreeMap();
                        Utils.fillMap(map, value);
                        writeMethods[writeMethods.length - 1].invoke(bean, map);

                    } else if (c.isArray()) {
                        Object[] array = new Object[size];
                        Utils.fillArray(array, value);
                        writeMethods[writeMethods.length - 1].invoke(bean, array);

                    } else {
                        throw new RuntimeException("Impossible case!");
                    }

                } else { // probably impossible case
                    // System.out.println(String.format("Calling plain setter method because traversing is not possible for bean: %s, value %s, class %s", bean, value, classes[classes.length - 1].getName()));
                    writeMethods[writeMethods.length - 1].invoke(bean, value);
                }
            } else {
                // System.out.println(String.format("Calling plain setter method for bean: %s, value %s, class %s", bean, value, classes[classes.length - 1].getName()));
                writeMethods[writeMethods.length - 1].invoke(bean, value);
            }
        } catch (Exception e) {
            System.err.println(String.format("bean: %s, value: %s, writeMethod: %s", bean, value, writeMethods[writeMethods.length - 1]));
            e.printStackTrace(System.err);
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PropertySetter that = (PropertySetter) o;

        return Arrays.equals(readMethods, that.readMethods) && Arrays.equals(classes, that.classes);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    /**
     * @param bean obiekt modelowy
     * @return Wartość reprezentowanego atrybutu w podanym obiekcie modelowym
     */
    public Object getProperty(Object bean) {
        try {
            for (Method readMethod : readMethods) bean = readMethod.invoke(bean);
            return bean;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @return Typ reprezentowanego atrybutu
     */
    public Class<T> getPropertyClass() {
        return (Class<T>) classes[classes.length - 1];
    }

    /**
     * @return Klasa w której bezpośrednio się znajduje reprezentowany atrybut
     */
    public Class<?> getPropertyParentClass() {
        if (classes.length > 1)
            return classes[classes.length - 2];
        else
            return modelClass;
    }

    /**
     * @return Nazwa atrybutu
     */
    public String getPropertyName() {
        return pathElems[pathElems.length - 1];
    }

    /**
     * @return Pole związane z reprezentowanym atrybutem
     */
    public Field getField() {
        return ReflectionUtils.getSingleField(getPropertyParentClass(), getPropertyName()).get();
    }

    public String getPropertyParentPath() {
        return Utils.arrayToDelimitedString(ArrayUtils.subarray(pathElems, 0, pathElems.length - 1), ".");
    }
}
