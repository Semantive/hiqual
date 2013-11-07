package com.semantive.hiqual.core;

import com.semantive.commons.PropertySetter;
import com.semantive.commons.Utils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.validator.GenericValidator;
import org.hibernate.transform.BasicTransformerAdapter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jacek Lewandowski
 *         TODO testy i dokumentacja
 */
public class AliasToBeanCustomTransformer extends BasicTransformerAdapter {

    public static final char DOT = '.';

    public static final char UNDERSCORE = '_';

    private Class beanClass;

    private PropertySetter[] propertySetters;

    private String skipComponent;

    private char pathSeparator = DOT;

    private Map<String, Class> classReplacements;

    public static AliasToBeanCustomTransformer build(Class beanClass) {
        return new AliasToBeanCustomTransformer(beanClass);
    }


    public AliasToBeanCustomTransformer skipLeadingComponent(String component) {
        checkIfNotInitializedYet();
        if (GenericValidator.isBlankOrNull(component))
            throw new IllegalArgumentException("The component cannot be blank or null.");
        this.skipComponent = component;
        return this;
    }


    public AliasToBeanCustomTransformer usePathSeparator(char separator) {
        checkIfNotInitializedYet();
        this.pathSeparator = separator;
        return this;
    }


    public AliasToBeanCustomTransformer setClassReplacement(String propertyPath, Class clazz) {
        if (classReplacements == null) classReplacements = new HashMap<String, Class>();
        classReplacements.put(propertyPath, clazz);
        return this;
    }

    private AliasToBeanCustomTransformer(Class beanClass) {
        this.beanClass = beanClass;
    }

    private void initialize(String[] paths) {
        try {
            if (classReplacements != null && pathSeparator != '.') {
                Map<String, Class> oldReplacements = classReplacements;
                classReplacements = new HashMap<String, Class>(classReplacements.size());
                for (Map.Entry<String, Class> entry : oldReplacements.entrySet()) {
                    classReplacements.put(entry.getKey().replace(DOT, pathSeparator), entry.getValue());
                }
            }

            propertySetters = new PropertySetter[paths.length];
            for (int i = 0; i < paths.length; i++) {

                if (skipComponent != null && paths[i].startsWith(skipComponent))
                    paths[i] = Utils.trimLeadingCharacter(paths[i].substring(skipComponent.length()), pathSeparator);

                Map<Integer, Class> replacements = MapUtils.EMPTY_MAP;
                if (classReplacements != null) {
                    replacements = new HashMap<Integer, Class>();
                    for (Map.Entry<String, Class> entry : classReplacements.entrySet()) {
                        if (paths[i].startsWith(entry.getKey()))
                            replacements.put(Utils.countOccurrencesOf(entry.getKey(), String.valueOf(pathSeparator)), entry.getValue());
                    }
                }
                propertySetters[i] = new PropertySetter(paths[i], beanClass, "\\" + pathSeparator, replacements);
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to initialize the AliasToBeanCustomTransformer for class %s and property paths %s.", beanClass, Arrays.toString(paths)), e);
        }
    }

    @Override
    public Object transformTuple(Object[] tuple, String[] aliases) {
        if (propertySetters == null) initialize(aliases);

        Object bean;
        try {
            bean = beanClass.newInstance();
            for (int i = 0; i < tuple.length; i++) {
                propertySetters[i].setProperty(bean, tuple[i]);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return bean;
    }

    @Override
    public List transformList(List list) {
        return list;
    }

    private void checkIfNotInitializedYet() {
        if (propertySetters != null)
            throw new IllegalStateException("This method can be called only before initialization.");
    }

    public AliasToBeanCustomTransformer setClassReplacements(Map<String, Class> replacements) {
        this.classReplacements = replacements;
        return this;
    }
}
