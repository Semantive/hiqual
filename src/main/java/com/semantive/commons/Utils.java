package com.semantive.commons;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.semantive.commons.functional.F0;
import com.semantive.commons.functional.F2;
import com.semantive.commons.functional.ListWrapper;
import com.semantive.commons.functional.Void2;
import com.semantive.hiqual.IDataObject;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.validator.GenericValidator;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

/**
 * @author Jacek Lewandowski
 */
public class Utils {


    private static final String SQL_WILDCARD_CHARACTER = "%";

    /**
     * Metoda konwertuje podaną listę obiektów na tablicę wywołań {@link String#valueOf(Object)} dla tych obiektów.
     *
     * @param returnEmptyIfNull jeśli lista obiektów jest null'em to zwrócona zostanie pusta lista, a nie null
     * @param cutNulls          obiekty, które są null'ami, nie będą dołączone do tablicy wynikowej
     * @param nullReplacement   każdy obiekt, który jest nullem zostanie zastąpiony przez to wyrażenie
     * @param objects           lista obiektów
     * @return tablica String'ów
     */
    private static String[] fullStrArr(boolean returnEmptyIfNull, boolean cutNulls, String nullReplacement, Object[] objects) {
        if (objects == null) {
            if (returnEmptyIfNull) {
                return new String[]{};
            } else {
                return null;
            }
        }

        if (cutNulls) {
            List<String> list = new ArrayList(objects.length);
            for (Object o : objects) {
                if (o != null) {
                    list.add(String.valueOf(o));
                }
            }

            return list.toArray(new String[list.size()]);
        } else {
            String[] arr = new String[objects.length];
            for (int i = 0; i < objects.length; i++) {
                if (objects[i] == null) {
                    arr[i] = nullReplacement;
                } else {
                    arr[i] = String.valueOf(objects[i]);
                }
            }

            return arr;
        }
    }

    public static String[] strArr(Object... objects) {
        return fullStrArr(true, true, null, objects);
    }

    public static String[] strArr2(Object[] objects) {
        return fullStrArr(true, true, null, objects);
    }

    public static String[] strArr(String nullReplacement, Object... objects) {
        return fullStrArr(true, false, nullReplacement, objects);
    }

    /**
     * Funkcja usuwa z podanej tablicy podane obiekty zastępując je wartościami <code>null</code>.
     *
     * @param array   tablica, z której mają być usunięte obiekty
     * @param objects lista obiektów, które mają być usunięte
     * @return podana tablica <code>array</code>
     */
    public static <T> T[] arrCut(T[] array, T... objects) {
        if (array == null || objects == null || objects.length == 0) {
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

    /**
     * Wyszukuje pierwszą pozycje na, której znajduje się numer (zakładamy,że w fullNumber nie znajdują się białe znaki)
     *
     * @param fullNumber
     * @return zwraca indeks pierwszej napotkanej nielitery, jeśli nie ma takie to zrwaca fullNumber.length()
     */
    public static int findNumber(String fullNumber) {
        int i = 0;
        while (i < fullNumber.length()) {
            if (Character.isLetter(fullNumber.charAt(i))) {
                i++;
                continue;
            }
            break;
        }

        return i;
    }


    /**
     * Zwraca reprezentacje wszystkich obiektow w postaci stringa. Jesli ktorys z obiektow jest nuller, to zwraca nulla.
     *
     * @param objects
     * @return reprezentacja wszystkich obiektow w postaci stringa, lub null.
     */
    public static String nnString(Object... objects) {
        StringBuilder buf = new StringBuilder();
        for (Object o : objects) {
            if (o != null) {
                buf.append(o);
            } else {
                return null;
            }
        }

        return buf.toString();
    }

    /**
     * Zastepuje wartosc null podana wartoscia
     *
     * @param value     wartosc do sprawdzenia
     * @param nullValue wartosc wstawiana, jesli @param vale rowna null
     * @param <T>
     * @return
     */
    public static <T> T nvl(T value, T nullValue) {
        if (value != null) {
            return value;
        }
        return nullValue;
    }

    /**
     * Zwraca reprezentacje wszystkich obiektow w postaci stringa, rozdzielajac kazdy obiekt separatorem. Wszystkie obiekty bedace null'ami sa pomijane.
     *
     * @param separator String rozdzielajacy wszystkie obiekty
     * @param objects
     * @return reprezentacja obiektow w postaci Stringa
     */
    public static String separate(String separator, final Object... objects) {
        StringBuffer buf = new StringBuffer();
        if (separator == null) separator = "";
        for (Object o : objects) {
            if (o != null) {
                String s = o.toString();
                if (GenericValidator.isBlankOrNull(s)) {
                    continue;
                }
                buf.append(s).append(separator);
            }
        }
        return buf.substring(0, buf.length() - separator.length());
    }

    /**
     * Metoda tworzony mapę z kolekcji, w taki sposób że wartością jest bean z kolekcji, a kluczem wartość zwrócona przez podany transformer.
     */
    public static <K, V> Map<K, V> toIdKeyedMap(List<V> beans, Transformer idExtractor) {
        Map<K, V> map = new HashMap<K, V>(beans.size());
        for (V bean : beans) {
            map.put((K) idExtractor.transform(bean), bean);
        }

        return map;
    }

    public static <T> T unproxy(T object) {
        if (object != null) {
            if (object instanceof HibernateProxy) {
                return (T) ((HibernateProxy) object).getHibernateLazyInitializer().getImplementation();
            }
        }
        return object;
    }

    /**
     * Powoduje rzutowanie obiektu na rządzaną klasę zdejmując jeśli potrzeba proxy założone przez Hibernate.
     * Uwaga: Zdjęcie proxy zablokuje dostęp do właściwości lazy, które nie są załadowane przed wykonaniem tego rzutowania (chyba, że mają one własne proxy - jak kolekcje).
     * Należy więc najpierw zadbać o doładowanie niezbędnych właściwości.
     *
     * @param object obiekt, który należy rzutować na daną klasę
     * @param <T>    klasa, na którą należy rzutować
     * @return rzutowany obiekt
     */
    public static <T> T proxySafeCast(Object object) {
        object = unproxy(object);
        return (T) object;
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

    public static void fillArray(Object[] dest, Object src) {
        if (src != null) {
            for (int i = 0; i < Array.getLength(src); i++) {
                dest[i] = Array.get(src, i);
            }
        }
    }

    public static <K, V, T> Map<K, V> toMap(Collection<T> collection, Function<T, Map.Entry<K, V>> entryMaker) {
        Map<K, V> map = new HashMap<K, V>(collection.size());
        for (T t : collection) {
            Map.Entry<K, V> entry = entryMaker.apply(t);
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    public static String trimFromHash(Object propertyId) {
        int pos = String.valueOf(propertyId).indexOf('#');
        if (pos >= 0) propertyId = String.valueOf(propertyId).substring(0, pos);
        return String.valueOf(propertyId);
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

    public static <T> T coalesce(T... values) {
        for (T value : values) {
            if (value != null) return value;
        }

        return null;
    }

    public static <T> ListWrapper<T> wrapList(List<T> objects) {
        return new ListWrapper<T>(objects);
    }

    public static <T> String arrayToDelimitedString(T[] array, String delimiter) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < array.length - 1; i++) {
            buf.append(array[i]).append(delimiter);
        }
        if (array.length > 0) buf.append(array[array.length - 1]);
        return buf.toString();
    }

    public static interface EnhancedResourceBundle {
        String getString(String key);

        String getString(String key, Object... args);
    }

    public static EnhancedResourceBundle enhanceBundle(final ResourceBundle bundle) {
        return new EnhancedResourceBundle() {
            public String getString(String key) {
                return bundle.getString(key);
            }

            public String getString(String key, Object... args) {
                return MessageFormat.format(bundle.getString(key), args);
            }
        };
    }


    /**
     * Zaokragla wartosc, do wyznaczonego miejsca po przecinku
     *
     * @param d     wartosc zaokraglana
     * @param radix miejsce po przecinku
     * @return double zaokraglony do odpowiedniego miejsca po przecinku
     */
    public static double roundDecimal(double d, int radix) {
        NumberFormat format = NumberFormat.getInstance();
        format.setGroupingUsed(false);
        format.setMaximumFractionDigits(radix);

        double res;
        try {
            res = format.parse(format.format(d)).doubleValue();
        } catch (ParseException e) {
            res = Double.NaN;
        }
        return res;
    }

    // TODO napisać do tego testy i dokumentację
    public static String clearWildcards(String str) {
        if (str == null) {
            return null;
        } else {
            return str.replaceAll("[\\*%_]", " ");
        }
    }

    // TODO napisać do tego testy i dokumentację
    public static String addSQLWildcard(String str, boolean before, boolean after) {
        if (str == null) {
            return null;
        } else {
            if (before) str = SQL_WILDCARD_CHARACTER + str;
            if (after) str = str + SQL_WILDCARD_CHARACTER;
            return str;
        }
    }

    // TODO napisać do tego testy i dokumentację
    public static String[] tokenizeAndWildcard(String str, boolean before, boolean after, int limit) {
        if (str == null) {
            return new String[0];
        } else {
            String[] tokens = str.split("[\\s\\.\\,\\(\\)\\[\\]\\{\\}\\\"\\'\\<\\>\\:\\;\\|\\\\\\?\\/\\+\\_]+", limit);
            int k = 0;
            for (int i = 0; i < tokens.length; i++) {
                String s = clearWildcards(tokens[i]).trim();
                if (!GenericValidator.isBlankOrNull(s)) {
                    tokens[k++] = addSQLWildcard(s, before, after);
                }
            }

            return ArrayUtils.subarray(tokens, 0, k);
        }
    }

    // TODO napisać do tego testy i dokumentację
    public static String makeCall(String str, String function) {
        if (str == null || function == null) {
            return null;
        } else {
            return function + "(" + str + ")";
        }
    }

    /**
     * Jeżeli podany atrybut w obiekcie źródłowym nie jest nullem to zostaje przepisany do obiektu docelowego.
     *
     * @param from     obiekt źródłowy
     * @param to       obiekt docelowy
     * @param property nazwa atrybutu
     * @param <T>      typ obiektów źródłowego i docelowego
     */
    public static <T> void setIfNotNull(T from, T to, String property) {
        try {
            Field field = from.getClass().getDeclaredField(property);
            field.setAccessible(true);
            Object value = field.get(from);
            if (value != null) field.set(to, value);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @see {@link #setIfNotNull(Object, Object, String)}
     */
    public static <T> void multiSetIfNotNull(T from, T to, String... properties) {
        for (String property : properties) {
            setIfNotNull(from, to, property);
        }
    }

    public static String safeTrim(String s) {
        return GenericValidator.isBlankOrNull(s) ? null : s.trim();
    }

    public static void initializePath(Object object, String path) {
        String[] pathElements = path.split("\\.");
        initializePathInternal(object, pathElements, 0);
    }

    private static void initializePathInternal(Object object, String[] path, int offset) {
        for (int i = offset; i < path.length; i++) {
            if (object != null) {
                if (object instanceof Iterable) {
                    if (object instanceof Collection) {
                        ((Collection) object).size();
                    }
                    for (Object item : (Iterable) object) {
                        initializePathInternal(item, path, i);
                    }
                    return;
                } else if (object instanceof Map) {
                    ((Map) object).size();
                    for (Object item : ((Map) object).values()) {
                        initializePathInternal(item, path, i);
                    }
                    return;
                } else {
                    try {
                        PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(object, path[i]);
                        if (propertyDescriptor == null) {
                            // LOG.warn("PropertyDescriptor is null for initialization. Property is probably missing");
                        } else {
                            object = propertyDescriptor.getReadMethod().invoke(object);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("When accessing property: " + path[i], e);
                    }
                    Hibernate.initialize(object);
                }
            } else {
                break;
            }

        }

    }

    @Deprecated // use newMap
    public static <K, V> Map<K, V> makeMap(Object... keysAndValues) {
        Map map = new HashMap(keysAndValues.length >> 1);
        for (int i = 0; i < keysAndValues.length; i += 2) {
            Object key = keysAndValues[i];
            Object value = keysAndValues[i + 1];
            map.put(key, value);
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


    public static Map<Integer, Class> makeMappings(String key, Map<String, Class> mappings) {
        Map<Integer, Class> m = new HashMap<Integer, Class>();
        for (Map.Entry<String, Class> entry : mappings.entrySet()) {
            int idx = key.indexOf(entry.getKey());
            if (idx == 0 && (entry.getKey().length() == key.length() || key.charAt(entry.getKey().length()) == '.')) {
                m.put(StringUtils.countMatches(entry.getKey(), "."), entry.getValue());
            }
        }
        if (m.isEmpty()) return MapUtils.EMPTY_MAP;
        else return m;
    }

    public static String trimLeadingCharacter(String str, char leadingCharacter) {
        if (GenericValidator.isBlankOrNull(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() > 0 && sb.charAt(0) == leadingCharacter) {
            sb.deleteCharAt(0);
        }
        return sb.toString();
    }

    public static int countOccurrencesOf(String str, String sub) {
        if (str == null || sub == null || str.length() == 0 || sub.length() == 0) {
            return 0;
        }
        int count = 0;
        int pos = 0;
        int idx;
        while ((idx = str.indexOf(sub, pos)) != -1) {
            ++count;
            pos = idx + sub.length();
        }
        return count;
    }


    /**
     * Metoda koduje long'a wraz z 8 bitową sumą kontrolną w tekst 14 znakowy zawierający cyfry i wielkie litery alfabetu.
     * Dla wartości <code>null</code> rzuca NPE.
     *
     * @param id identyfikator do zakodowania
     * @return kod 14 znakowy
     * @see #decodeSafeLongCode(String)
     */
    public static String generateSafeLongCode(Long id) {
        if (id < 0) throw new IllegalArgumentException("id cannot be negative.");

        ByteBuffer buf = ByteBuffer.allocate(9).putLong(id);
        return StringUtils.leftPad(new BigInteger(buf.put(CRC8.calc(buf.array(), 8)).array()).toString(36), 14, '0').toUpperCase();
    }


    /**
     * Metoda dekoduje bezpiecznie zakodowanego long'a. Jeżeli długość kodu jest różna od 14 lub nie zgadza się suma kontrolna to rzucany jest {@link IllegalArgumentException}.
     *
     * @param safeLongCode kod 14 znakowy
     * @return odkodowana wartość
     * @throws IllegalArgumentException kiedy kod jest niepoprawny
     * @see #generateSafeLongCode(Long)
     */
    public static Long decodeSafeLongCode(String safeLongCode) {
        if (safeLongCode.length() != 14) throw new IllegalArgumentException("Code length is invalid.");

        byte[] array;
        try {
            array = new BigInteger(safeLongCode, 36).toByteArray();
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(ex);
        }
        ByteBuffer buf = ByteBuffer.allocate(9);
        for (int i = 0; i < (9 - array.length); i++) buf.put((byte) 0);
        buf.put(array);
        if (CRC8.calc(buf.array(), 8) != buf.get(8)) throw new IllegalArgumentException("CRC is invalid.");
        return buf.getLong(0);
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


    public static Transformer dataObjectToIdTransformer() {
        return new Transformer() {
            @Override
            public Object transform(Object input) {
                return Utils.<IDataObject>proxySafeCast(input).getId();
            }
        };
    }

    public static <InputType, OutputType> OutputType transform(Collection<InputType> inputCollection, F0<OutputType> outputFactory, Void2<OutputType, InputType> modifier) {
        OutputType outputType = outputFactory.apply();
        for (InputType input : inputCollection) {
            modifier.apply(outputType, input);
        }
        return outputType;
    }


}
