package com.semantive.commons;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.validator.GenericValidator;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author Jacek Lewandowski
 */
public class SemantiveStringUtils {
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

    public static String trimFromHash(Object propertyId) {
        int pos = String.valueOf(propertyId).indexOf('#');
        if (pos >= 0) propertyId = String.valueOf(propertyId).substring(0, pos);
        return String.valueOf(propertyId);
    }

    public static <T> String arrayToDelimitedString(T[] array, String left, String delimiter, String right) {
        return arrayToDelimitedString(array, 0, array.length, left, delimiter, right);
    }

    public static <T> String arrayToDelimitedString(T[] array, int offset, int length, String delimiter) {
        return arrayToDelimitedString(array, offset, length, "", delimiter, "");
    }

    public static <T> String arrayToDelimitedString(T[] array, String delimiter) {
        return arrayToDelimitedString(array, 0, array.length, "", delimiter, "");
    }

    public static <T> String arrayToString(T[] array) {
        return arrayToDelimitedString(array, 0, array.length, "", "", "");
    }

    public static <T> String arrayToDelimitedString(T[] array, int offset, int length, String left, String delimiter, String right) {
        StringBuilder buf = new StringBuilder();
        for (int i = offset; i < offset + length - 1; i++) {
            buf.append(left).append(array[i]).append(right).append(delimiter);
        }
        if (length > 0) buf.append(left).append(array[offset + length - 1]).append(right);
        return buf.toString();
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

    public static String safeTrim(String s) {
        return GenericValidator.isBlankOrNull(s) ? null : s.trim();
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

    public static interface EnhancedResourceBundle {
        String getString(String key);

        String getString(String key, Object... args);
    }
}
