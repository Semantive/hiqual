package com.semantive.commons;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jacek Lewandowski
 */
public class SemantiveObjectUtils {


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


    public static <T> T coalesce(T... values) {
        for (T value : values) {
            if (value != null) return value;
        }

        return null;
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


}
