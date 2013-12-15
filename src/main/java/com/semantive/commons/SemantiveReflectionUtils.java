package com.semantive.commons;

import com.semantive.commons.functional.ListWrapper;
import com.semantive.commons.functional.Option;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;

import static com.semantive.commons.functional.Option.none;
import static com.semantive.commons.functional.Option.some;

/**
 * @author Piotr Jędruszuk
 */
public class SemantiveReflectionUtils {

    /**
     * Zwraca klasę będącą najniżej w hierarchii.
     * Zakłada, że wszystkie przekazane klasy są klasami w tej samej hierarchii dziedziczenia w linii prostej, bez rozgałęzień
     *
     * @param classes klasy do analizy
     * @return klasa najniższa w hierarchii
     */
    public static Class<?> getClassHierarchyLeaf(Collection<Class<?>> classes) {
        Class<?> hierarchyLeaf = null;
        for (Class<?> clazz : classes) {
            if (hierarchyLeaf == null) {
                hierarchyLeaf = clazz;
            } else if (clazz != null) {
                if (hierarchyLeaf.isAssignableFrom(clazz)) {
                    hierarchyLeaf = clazz;
                }
            }

        }
        return hierarchyLeaf;
    }

    /**
     * Zwraca set pól klasy i wszystkich jej nadklas
     *
     * @param clazz klasa do przeskanowania
     * @return set wszystkich pól klasy
     */
    public static Set<Field> getAllFields(Class<?> clazz) {
        Set<Field> fieldSet = new HashSet<Field>();
        do {
            fieldSet.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        } while (clazz != null);
        return fieldSet;
    }

    public static Option<Field> getSingleField(Class<?> clazz, String fieldName) {
        while (!clazz.equals(Object.class))
            try {
                return some(clazz.getDeclaredField(fieldName));
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        return none;
    }

    public static Field getField(Class<?> clazz, final String fieldPath) {
        StringTokenizer tokenizer = new StringTokenizer(fieldPath, ".");

        Field field = null;
        Class<?> classToCheck = clazz;
        while (tokenizer.hasMoreTokens() && classToCheck != null) {
            final String fieldToCheck = tokenizer.nextToken();

            Set<Field> fields = getAllFields(classToCheck);
            field = (Field) CollectionUtils.find(fields, new Predicate() {

                @Override
                public boolean evaluate(Object object) {
                    Field field = (Field) object;
                    return field.getName().equals(fieldToCheck);
                }
            });

            classToCheck = field != null ? field.getType() : null;
            if (classToCheck != null && Collection.class.isAssignableFrom(classToCheck)) {
                ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                classToCheck = (Class<?>) parameterizedType.getActualTypeArguments()[0];
            }
        }

        if (field == null) {
            throw new NoSuchFieldError("Field is not found: " + fieldPath);
        }

        return field;
    }


    public static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Sprawdza czy wartość typu {@code fromClass} można przypisać do pola jednego z typów {@code #toClasses}.
     *
     * @param fromClass  klasa wartości przypisywanej
     * @param autoboxing czy typy proste powinny być przed sprawdzeniem przekształcone do wersji boxed
     * @param toClasses  potencjalne klasy pola
     * @return {@value true} jeżeli wartość typu {@code fromClass} można przypisać do wartości jednego z typów {@code toClass}
     */
    public static boolean isAssignableToAny(Class<?> fromClass, boolean autoboxing, Class<?>... toClasses) {
        for (Class<?> toClass : toClasses) {
            if (ClassUtils.isAssignable(fromClass, toClass, autoboxing)) return true;
        }

        return false;
    }

    public static ListWrapper<Method> getAllMethods(Class<?> clazz) {
        LinkedList<Method> methods = new LinkedList<Method>();

        while (!clazz.equals(Object.class)) {
            CollectionUtils.addAll(methods, clazz.getDeclaredMethods());
            clazz = clazz.getSuperclass();
        }

        return new ListWrapper<Method>(methods);
    }
}
