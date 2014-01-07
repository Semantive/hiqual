package com.semantive.commons;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.semantive.commons.functional.*;
import com.semantive.hiqual.IDataObject;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;

/**
 * @author Jacek Lewandowski
 */
public class SemantiveCollectionUtils {
    /**
     * Funkcja usuwa z podanej tablicy podane obiekty zastępując je wartościami <code>null</code>.
     *
     * @param array   tablica, z której mają być usunięte obiekty
     * @param objects lista obiektów, które mają być usunięte
     * @return podana tablica <code>array</code>
     */
    public static <T> T[] arrCut(T[] array, T... objects) {
        if (array == null || array.length == 0 || objects == null || objects.length == 0) {
            return array;
        }

        for (int i = 0; i < array.length; i++) {
            for (T t : objects) {
                if (array[i] != null && (array[i] == t || array[i].equals(t))) {
                    array[i] = null;
                }
            }
        }

        return array;
    }

    public static <K, V> Map<K, Set<V>> groupByToSet(Collection<V> items, Transformer keyExtractor) {
        Map<K, Set<V>> map = new HashMap<K, Set<V>>();
        for (V item : items) {
            K key = (K) keyExtractor.transform(item);
            Set<V> set = map.get(key);
            if (set == null) {
                set = new HashSet<V>();
                map.put(key, set);
            }
            set.add(item);
        }
        return map;
    }

    public static Object safeGetFromMap(Map map, Object... keys) {
        if (map == null) return null;

        for (int i = 0; i < keys.length - 1; i++) {
            Object key = keys[i];
            map = (Map) map.get(key);
            if (map == null) return null;
        }

        return map.get(keys[keys.length - 1]);
    }

    public static int getCollectionOrArraySize(Object value) {
        if (value.getClass().isArray()) return Array.getLength(value);
        else if (value instanceof Collection) return ((Collection) value).size();
        else if (value instanceof Map) return ((Map) value).size();
        else throw new IllegalArgumentException("Not a collection: " + value.getClass().getName());

    }

    public static void fillCollection(Collection<?> dest, Object src) {
        if (src != null) {
            Collection c = (Collection) src;
            dest.addAll(c);
        }
    }

    public static void fillMap(Map<?, ?> dest, Object src) {
        if (src != null) {
            Map m = (Map) src;
            dest.putAll(m);
        }
    }

    public static void fillArray(Object dest, Object src) {
        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(src, 0, dest, 0, Array.getLength(src));
    }

    public static <K, V, T> Map<K, V> toMap(Collection<T> collection, Function<T, Map.Entry<K, V>> entryMaker) {
        Map<K, V> map = new HashMap<K, V>(collection.size());
        for (T t : collection) {
            Map.Entry<K, V> entry = entryMaker.apply(t);
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    public static <SourceType, DestinationType> void enrich(Collection<DestinationType> collection, F2<SourceType, DestinationType, ?> enrichmentFunction, Collection<SourceType> sourceData, Function<Object, Object> identifierExtractor) {
        Map<Object, SourceType> map = new HashMap<Object, SourceType>(collection.size());
        for (SourceType o : sourceData) {
            map.put(identifierExtractor.apply(o), o);
        }

        for (DestinationType o : collection) {
            SourceType src = map.get(identifierExtractor.apply(o));
            if (src != null) {
                enrichmentFunction.apply(src, o);
            }
        }
    }

    public static <T> ListWrapper<T> wrapList(List<T> objects) {
        return new ListWrapper<T>(objects);
    }

    public static <K, V> Map<K, V> makeMap(Object... keysAndValues) {
        Map<K, V> map = new HashMap<K, V>(keysAndValues.length >> 1);
        for (int i = 0; i < keysAndValues.length; i += 2) {
            Object key = keysAndValues[i];
            Object value = keysAndValues[i + 1];
            //noinspection unchecked
            map.put((K) key, (V) value);
        }

        return map;
    }

    public static <K, V> Map<K, V> newMap(Pair<K, V>... keyValuePairs) {
        Map<K, V> map = new HashMap<K, V>(keyValuePairs.length);
        for (Pair<K, V> pair : keyValuePairs) {
            map.put(pair.getKey(), pair.getValue());
        }

        return map;
    }

    public static <V extends Comparable<V>> Set<V> newTreeSet(V... values) {
        Set<V> set = new TreeSet<V>();
        Collections.addAll(set, values);

        return set;
    }

    public static <T, C extends Collection<T>> List<T> flatten(Collection<C> colOfCols) {
        int size = 0;
        for (C col : colOfCols) {
            size += col.size();
        }
        ArrayList<T> list = new ArrayList<T>(size);
        for (C col : colOfCols) {
            list.addAll(col);
        }
        return list;
    }

    public static <T> T[] filterArray(T[] array, Predicate<T> predicate) {
        ArrayList<T> list = new ArrayList<T>(array.length);
        for (T t : array) {
            if (predicate.apply(t)) list.add(t);
        }
        return list.toArray(Arrays.copyOf(array, list.size()));
    }

    public static <T_Result, T_Collection> T_Result foldLeft(Collection<T_Collection> collection, F2<T_Result, T_Collection, T_Result> function) {
        return foldLeft(collection, function, null);
    }

    public static <T_Result, T_Collection> T_Result foldLeft(Collection<T_Collection> collection, F2<T_Result, T_Collection, T_Result> function, T_Result initialValue) {
        T_Result currentValue = initialValue;
        for (T_Collection element : collection) {
            currentValue = function.apply(currentValue, element);
        }
        return currentValue;
    }

    public static <InV, OutV, C extends Collection<OutV>> C collect(C output, Collection<InV> input, Function<InV, OutV> transformation) {
        if (output == null || input == null) return null;
        for (InV inV : input) {
            output.add(transformation.apply(inV));
        }
        return output;
    }

    public static <T extends Serializable> T[] toIdArray(Collection<? extends IDataObject<T>> objects, Class<T> idClass) {
        //noinspection unchecked
        T[] result = (T[]) Array.newInstance(idClass, objects.size());

        int i = 0;
        for (IDataObject<T> object : objects) {
            result[i++] = object.getId();
        }
        return result;
    }

    public static final F1<IDataObject<?>, ?> idExtractor = new F1<IDataObject<?>, Object>() {
        @Override
        public Object apply(IDataObject<?> input) {
            return SemantiveObjectUtils.<IDataObject>proxySafeCast(input).getId();
        }
    };

    public static <InputType, OutputType> OutputType transform(Collection<InputType> inputCollection, F0<OutputType> outputFactory, Void2<OutputType, InputType> modifier) {
        OutputType outputType = outputFactory.apply();
        for (InputType input : inputCollection) {
            modifier.apply(outputType, input);
        }
        return outputType;
    }

    public static Object copyCollectionOrArray(Object collectionOrArray, Class<?> desiredCollectionType) {
        int size = (collectionOrArray != null) ? getCollectionOrArraySize(collectionOrArray) : 0;

        if (desiredCollectionType.isAssignableFrom(Set.class)) {
            Set set = new HashSet(size);
            fillCollection(set, collectionOrArray);
            return set;

        } else if (desiredCollectionType.isAssignableFrom(SortedSet.class)) {
            SortedSet set = new TreeSet();
            fillCollection(set, collectionOrArray);
            return set;

        } else if (desiredCollectionType.isAssignableFrom(List.class)) {
            List list = new ArrayList(size);
            fillCollection(list, collectionOrArray);
            return list;

        } else if (desiredCollectionType.isAssignableFrom(Map.class)) {
            Map map = new HashMap(size);
            fillMap(map, collectionOrArray);
            return map;

        } else if (desiredCollectionType.isAssignableFrom(SortedMap.class)) {
            SortedMap map = new TreeMap();
            fillMap(map, collectionOrArray);
            return map;

        } else if (desiredCollectionType.isArray()) {
            Object array = Array.newInstance(desiredCollectionType.getComponentType(), Array.getLength(collectionOrArray));
            fillArray(array, collectionOrArray);
            return array;

        } else {
            throw new IllegalArgumentException("Unknown collection");
        }
    }
}
