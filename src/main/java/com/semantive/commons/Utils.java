package com.semantive.commons;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.semantive.commons.functional.ListWrapper;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.GenericValidator;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

/**
 * @author <a href="mailto:lewandowski.jacek@gmail.com">Jacek Lewandowski</a> Warsaw University of Technology, Faculty of Electronics and Computer Science,
 *         2008
 */
public class Utils {

    public final static int[] TIME_SET = new int[]{Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND};

    public final static int[] DATE_SET = new int[]{Calendar.ERA, Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH};

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
     * Oblicza ile roznice w miesiacach pomiedzy dayWhenCalc i birthDate. Wykorzysytwane do sprawdzenia wieku zwierzecia w dniu uboju
     *
     * @param birthDate
     * @param dayWhenCalc
     * @return roznica w miesiacach pomiedzy dayWhenCalc i birthDate
     */
    public static int calcMonthAgeInDate(Date birthDate, Date dayWhenCalc) {
        Calendar curCal = Calendar.getInstance();
        if (dayWhenCalc != null) {
            curCal.setTime(dayWhenCalc);
        }

        Calendar birthCal = Calendar.getInstance();
        birthCal.setTime(birthDate);

        return curCal.get(Calendar.YEAR) * 12 + curCal.get(Calendar.MONTH) - (birthCal.get(Calendar.YEAR) * 12 + birthCal.get(Calendar.MONTH));
    }

    public static int calcMonthAge(Date birthDate) {
        return calcMonthAgeInDate(birthDate, null);
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
     * Zeruje czas w dacie.
     */
    public static Date clearTime(Date dateTime) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(dateTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * Ustawia date na koniec tego dnia
     */
    public static Date setEndOfDayDate(Date dateTime) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(dateTime);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 99);
        return calendar.getTime();
    }

    /**
     * Wylicza dokladny wiek w miesiacach, od urodzenia do teraz
     *
     * @param birthDate data narodzin
     * @return wiek podany w double
     */
    public static double calcAccurateMonthAge(Date birthDate) {
        Calendar cur = GregorianCalendar.getInstance();
        double curTime = (double) cur.get(Calendar.YEAR) * 12d + (double) cur.get(Calendar.MONTH) + (double) (cur.get(Calendar.DAY_OF_MONTH) - 1) / (double) cur.getActualMaximum(Calendar.DAY_OF_MONTH);
        Calendar birth = GregorianCalendar.getInstance();
        birth.setTime(birthDate);
        double birthTime = (double) birth.get(Calendar.YEAR) * 12d + (double) birth.get(Calendar.MONTH) + (double) (birth.get(Calendar.DAY_OF_MONTH) - 1) / (double) birth.getActualMaximum(Calendar.DAY_OF_MONTH);
        return curTime - birthTime;
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

    public static boolean isCollectionInterfaceOrArray(Class c) {
        return c.isAssignableFrom(SortedSet.class) || c.isAssignableFrom(SortedMap.class) || c.isAssignableFrom(List.class) || c.isArray();
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


    public static <SourceType, DestinationType> void enrich(Collection<DestinationType> collection, Function2<Void, SourceType, DestinationType> enrichmentFunction, Collection<SourceType> sourceData, Function<Object, Object> identifierExtractor) {
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


    public static boolean equals(Object o1, Object o2, String propertyPath) {
        if (o1 != null && o2 != null) {
            if (o1 == o2) return true;

            PropertySetter ps1 = new PropertySetter(propertyPath, o1.getClass());
            PropertySetter ps2 = new PropertySetter(propertyPath, o2.getClass());

            return ObjectUtils.equals(ps1.getProperty(o1), ps2.getProperty(o2));
        } else {
            return o1 == o2;
        }
    }

    public static boolean isNull(Object o, String propertyPath) {
        if (o == null) return true;
        PropertySetter ps = new PropertySetter(propertyPath, o.getClass());
        return ps.getProperty(o) == null;
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
     * Metoda podmienia podane pola z Calendar w dacie baseDate na pola z sourceDate.
     *
     * @param baseDate   data w której mają być podmienione pola
     * @param sourceDate data z której mają być wzięte nowe wartości pól
     * @param fields     pola - Calendar.xxx
     * @return data baseDate z podmienionymi polami
     */
    public static Date replaceDateFields(Date baseDate, Date sourceDate, int... fields) {
        Calendar base = Calendar.getInstance();
        base.setTime(baseDate);

        Calendar source = Calendar.getInstance();
        source.setTime(sourceDate);

        for (int field : fields) {
            base.set(field, source.get(field));
        }

        return base.getTime();
    }

    /**
     * Sprawdza czy wsrod obiektow znajduje sie obiekt
     *
     * @param what  obiekt szukany
     * @param where obiekty wsrod ktorych jest poszukiwany
     * @return true jeśli znajdziemy obiekt, false jesli nie znajdziemy.
     */
    public static boolean contains(Object what, Object... where) {
        for (Object o : where) {
            if (o.equals(what)) {
                return true;
            }
        }
        return false;
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
            return str.replaceAll("[\\*\\%\\_]", " ");
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

            return (String[]) ArrayUtils.subarray(tokens, 0, k);
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

    public static <K, V> Map<K, V> makeMap(Object... keysAndValues) {
        Map map = new HashMap(keysAndValues.length >> 1);
        for (int i = 0; i < keysAndValues.length; i += 2) {
            Object key = keysAndValues[i];
            Object value = keysAndValues[i + 1];
            map.put(key, value);
        }

        return map;
    }

    public static <V> Set<V> makeSet(V... values) {
        Set set = new HashSet(values.length);
        Collections.addAll(set, values);

        return set;
    }

    public static <V extends Comparable<V>> Set<V> makeSortedSet(V... values) {
        Set<V> set = new TreeSet<V>();
        Collections.addAll(set, values);

        return set;
    }

    public static <V> List<V> makeList(Object... values) {
        List list = new ArrayList(values.length);
        Collections.addAll(list, values);

        return list;
    }

    public static boolean equals(Object first, Object second) {
        if (first == null || second == null) {
            return first == null && second == null;
        } else {
            return first.equals(second);
        }
    }

    public static boolean proxySafeEquals(Object first, Object second) {
        return equals(proxySafeCast(first), proxySafeCast(second));
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

    public static <T_Result, T_Collection> T_Result foldLeft(Collection<T_Collection> collection, Function2<T_Result, T_Result, T_Collection> function) {
        return foldLeft(collection, function, null);
    }

    public static <T_Result, T_Collection> T_Result foldLeft(Collection<T_Collection> collection, Function2<T_Result, T_Result, T_Collection> function, T_Result initialValue) {
        T_Result currentValue = initialValue;
        for (T_Collection element : collection) {
            currentValue = function.apply(currentValue, element);
        }
        return currentValue;
    }

    public static interface Function2<T_Result, T_FirstArgument, T_SecondArgument> {

        T_Result apply(T_FirstArgument firstArgument, T_SecondArgument secondArgument);

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

}
