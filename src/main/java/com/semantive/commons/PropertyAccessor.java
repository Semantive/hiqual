package com.semantive.commons;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * PropertyAccessor is a class which can be used to read or write properties pointed by path that starts in some class.
 * It provides features similar to {@code BeanUtils} from Apache Commons, however it has two significant improvements:
 * <ul>
 * <li>it provides a special behavior for null values on intermediate elements in the path - it can simply
 * initialize them</li>
 * <li>it can handle interface types of intermediate elements in the path</li>
 * <li>it can automatically convert collections to the property type, so that a different type of collection can
 * be set on the property than the actual type of the property is</li>
 * </ul>
 * <p/>
 * The another feature of the property accessor is that, it <quote>pre-compiles</quote> the property path on
 * initialization. Therefore, invocation of reader or writer methods are really fast.
 *
 * @author Jacek Lewandowski
 */
public class PropertyAccessor<T> {

    private final static Log log = LogFactory.getLog(PropertyAccessor.class);

    /**
     * Possible collection handling modes.
     */
    public static enum CollectionProcessingMode {
        /**
         * No special collection processing is enabled.
         */
        NONE,

        /**
         * Collections are copied regardless of their kind compatibility. It means that, for example, if the property
         * is of {@link Set} type, you can set a value of array type and vice-versa.
         */
        COPY
    }

    /**
     * Possible null value handling modes.
     */
    public static enum NullValueProcessingMode {
        /**
         * No special processing of null values. If a null value is to be set and there is null value somewhere on the
         * path, the setter exits immediately leaving the rest of the path uninitialized.
         */
        NONE,

        /**
         * With this mode, if a null value is to be set, the whole path to the property is initialized and then the
         * null value is set on object pointed by penultimate element on the path.
         */
        INITIALIZE,

        /**
         * Similar behavior to the {@link #INITIALIZE}, however, if the property is of a collection type, an empty
         * collection is initialized even when null value is issued.
         */
        INITIALIZE_COLLECTION
    }

    /**
     * An array of setter methods for each element on the property path.
     */
    private final Method[] writeMethods;

    /**
     * An array of getter methods for each element on the property path.
     */
    private final Method[] readMethods;

    /**
     * An array of classes for each element on the property path.
     */
    private final Class[] classes;

    /**
     * An array of property names along the property path.
     */
    private final String[] pathElems;

    /**
     * A class where the property path starts from.
     */
    private final Class<T> modelClass;

    /**
     * The default collection processing mode. It is used, when no collection processing mode is provided with the
     * setter method call.
     */
    private final CollectionProcessingMode defaultCollectionProcessingMode;

    /**
     * The default null value processing mode. It is used, when no null value processing mode is provided with setter
     * method call.
     */
    private final NullValueProcessingMode defaultNullValueProcessingMode;

    /**
     * A hash code which is precomputed on initialization.
     */
    private final int hash;


    /**
     * Initializes this property accessor with {@link #PropertyAccessor(String, Class, Map)} with no explicit types for
     * intermediate properties.
     */
    public PropertyAccessor(String path, Class<T> modelClass) {
        //noinspection unchecked
        this(path, modelClass, MapUtils.EMPTY_MAP);
    }

    /**
     * Initializes this property accessor with {@link #PropertyAccessor(String, String, Class, Map)} by providing a dot
     * as a path separator.
     */
    public PropertyAccessor(String path, Class<T> modelClass, Map<Integer, Class> classesMapping) {
        this(path, "\\.", modelClass, classesMapping);
    }

    /**
     * Initializes this property accessor with
     * {@link #PropertyAccessor(String, String, Class, Map, NullValueProcessingMode, CollectionProcessingMode)} by
     * providing {@link NullValueProcessingMode#NONE} and {@link CollectionProcessingMode#COPY} for default null value
     * processing mode and default collection processing mode respectively.
     */
    public PropertyAccessor(String path, String pathSeparator, Class<T> modelClass, Map<Integer, Class> classesMapping) {
        this(path, pathSeparator, modelClass, classesMapping, NullValueProcessingMode.NONE, CollectionProcessingMode.COPY);
    }

    /**
     * Initializes this property accessor with the given parameters. The constructor eagerly discovers all classes
     * along the path that are not explicitly specified by {@code classesMapping} parameter. It is done by creating
     * instances of all intermediate path elements.
     * <p/>
     * If any explicit class mapping is provided, the constructor performs type checking to find out if the real type
     * of the property is assignable from the specified one.
     * <p/>
     * Because the constructor does the most of the work, the setter method works very efficiently.
     *
     * @param path                     a path to the property, separated by specified separator
     * @param pathSeparator            a path separator specified as a regular expression
     * @param modelClass               a model class is the class which this property path start from
     * @param classesMapping           a collection of explicit property types of intermediate path elements (indexes
     *                                 starts from 0) - for example, if the second element of the path is an interface
     *                                 and you want to specify a concrete implementation to be initialized, just put
     *                                 a pair (1, SomeConcreteImplementation.class) to this map; this class will be
     *                                 used to instantiate that path element
     * @param nullValueProcessingMode  a default null value processing mode
     * @param collectionProcessingMode a default collection processing mode
     */
    public PropertyAccessor(String path, String pathSeparator, Class<T> modelClass, Map<Integer, Class> classesMapping, NullValueProcessingMode nullValueProcessingMode, CollectionProcessingMode collectionProcessingMode) {
        // initialize variables
        this.modelClass = modelClass;
        pathElems = path.split(pathSeparator);
        writeMethods = new Method[pathElems.length];
        readMethods = new Method[pathElems.length];
        classes = new Class[pathElems.length];
        hash = Arrays.hashCode(readMethods);
        defaultCollectionProcessingMode = collectionProcessingMode;
        defaultNullValueProcessingMode = nullValueProcessingMode;

        // property getters and setters early introspection
        log.debug(String.format("\nInitializing property accessor for path %s and class %s.", path, modelClass.getName()));
        try {
            // setting provided property types
            for (Map.Entry<Integer, Class> entry : classesMapping.entrySet())
                if (entry.getKey() < classes.length) classes[entry.getKey()] = entry.getValue();

            // the rest of property types are discovered by instantiating classes of properties
            Object o = modelClass.newInstance();
            for (int i = 0; i < pathElems.length; i++) {
                PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(o, pathElems[i]);
                writeMethods[i] = pd.getWriteMethod();
                readMethods[i] = pd.getReadMethod();

                log.debug(String.format("Property descriptor for path element %s is: class %s, read method %s, write method %s", pathElems[i], pd.getPropertyType().getName(), pd.getReadMethod(), pd.getWriteMethod()));
                if (classes[i] != null) {
                    // if the type of property has been explicitly specified, we have to check if the property type is assignable from the provided type
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
            throw new RuntimeException(String.format("Failed to create the PropertyAccessor for class %s and path %s.", modelClass, path), ex);
        }
    }

    /**
     * Invokes {@link #setProperty(Object, Object, NullValueProcessingMode, CollectionProcessingMode)} with default
     * null processing mode and default collection processing mode.
     */
    public void setProperty(Object bean, Object value) {
        setProperty(bean, value, defaultNullValueProcessingMode, defaultCollectionProcessingMode);
    }

    /**
     * Invokes {@link #setProperty(Object, Object, NullValueProcessingMode, CollectionProcessingMode)} with default
     * null processing mode.
     */
    public void setProperty(Object bean, Object value, CollectionProcessingMode collectionProcessingMode) {
        setProperty(bean, value, defaultNullValueProcessingMode, collectionProcessingMode);
    }

    /**
     * Invokes {@link #setProperty(Object, Object, NullValueProcessingMode, CollectionProcessingMode)} with default
     * collection processing mode.
     */
    public void setProperty(Object bean, Object value, NullValueProcessingMode nullValueProcessingMode) {
        setProperty(bean, value, nullValueProcessingMode, defaultCollectionProcessingMode);
    }

    /**
     * Sets the given value on the property represented by this property accessor. The method instantiates all
     * uninitialized intermediate properties on the path (depending on null value processing mode), so that you will
     * not get {@link NullPointerException}. However you surely get {@link NullPointerException} if you provide null
     * model object.
     * <p/>
     * If the property represented by this accessor is a collection or array, it will be processed according to the
     * specified collection processing mode.
     *
     * @param bean  a model object
     * @param value a value to be set
     */
    public void setProperty(Object bean, Object value, NullValueProcessingMode nullValueProcessingMode, CollectionProcessingMode collectionProcessingMode) {
        try {
            // initialize a path to the property so that bean points to the object where the property is directly included
            for (int i = 0; i < readMethods.length - 1; i++) {
                Object v = readMethods[i].invoke(bean);
                if (v == null) {
                    // if any intermediate property is null and value to be set is also null, there is nothing to do
                    if (value == null && nullValueProcessingMode == NullValueProcessingMode.NONE) {
                        return;
                    } else {
                        // once some intermediate property is null, we have initialize all remaining classes on the path
                        v = classes[i].newInstance();
                        writeMethods[i].invoke(bean, v);
                    }

                }
                bean = v;
            }

            if (Is.collectionInterfaceOrArray(classes[classes.length - 1])
                    && (nullValueProcessingMode == NullValueProcessingMode.INITIALIZE_COLLECTION || (collectionProcessingMode == CollectionProcessingMode.COPY && value != null))) {

                // convert the collection to desired type
                Class<?> collectionClass = classes[classes.length - 1];
                Object processedCollection = Utils.copyCollectionOrArray(value, collectionClass);
                writeMethods[writeMethods.length - 1].invoke(bean, processedCollection);
            } else {
                writeMethods[writeMethods.length - 1].invoke(bean, value);
            }

        } catch (Exception e) {
            throw new RuntimeException(String.format("Error setting property on bean: %s, value: %s, writeMethod: %s", bean, value, writeMethods[writeMethods.length - 1]));
        }
    }

    /**
     * Returns a value of the property represented by this accessor. The method does not throw any exception - whatever
     * unexpected behavior is caught and {@code null} is returned.
     *
     * @param bean an instance of the model class
     * @return a value of the property pointed by the path defined in this accessor
     */
    public <T> T getProperty(Object bean) {
        try {
            for (Method readMethod : readMethods) bean = readMethod.invoke(bean);
            //noinspection unchecked
            return (T) bean;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns a model class for this property accessor.
     *
     * @return a model class
     */
    public Class<T> getModelClass() {
        return modelClass;
    }

    /**
     * Returns a class of the property represented by this accessor.
     *
     * @return a type of the property
     */
    public Class<?> getPropertyClass() {
        return classes[classes.length - 1];
    }

    /**
     * Returns a class of the parent property, that is, the property represented by the penultimate element on the
     * property path.
     *
     * @return a type of the parent property
     */
    public Class<?> getPropertyParentClass() {
        if (classes.length > 1)
            return classes[classes.length - 2];
        else
            return modelClass;
    }

    /**
     * Returns the property name without the whole path, that is, it returns the last element on the property path.
     *
     * @return a property name
     */
    public String getPropertyName() {
        return pathElems[pathElems.length - 1];
    }

    /**
     * Returns the {@link Field} object for the property represented by this accessor. The field is contained in the
     * class of object which is penultimate element on the path.
     *
     * @return a field for the underlying property
     */
    public Field getField() {
        return ReflectionUtils.getSingleField(getPropertyParentClass(), getPropertyName()).get();
    }

    /**
     * Returns a path to the parent property delimited by dots. This is the shorthand method that performs
     * {@link #getPropertyParentPathArray()} and then converts the resulting array to the string delimited by dots.
     *
     * @return a path to the parent property
     */
    public String getPropertyParentPath() {
        return Utils.arrayToDelimitedString(pathElems, 0, pathElems.length - 1, ".");
    }

    /**
     * Returns a path to the parent property. In other words it is a path to the property except the last element. You
     * can also use shorthand method {@link #getPropertyParentPath()}.
     *
     * @return a path to the parent property
     */
    public String[] getPropertyParentPathArray() {
        return ArrayUtils.subarray(pathElems, 0, pathElems.length - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PropertyAccessor that = (PropertyAccessor) o;

        return Arrays.equals(readMethods, that.readMethods) && Arrays.equals(classes, that.classes);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("modelClass", modelClass)
                .append("pathElems", pathElems)
                .append("classes", classes)
                .toString();
    }
}
