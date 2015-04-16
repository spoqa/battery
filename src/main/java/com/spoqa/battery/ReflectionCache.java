/**
 * Copyright (c) 2014-2015 Spoqa, All Rights Reserved.
 */

package com.spoqa.battery;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReflectionCache {

    private static class AnnotatedPropertyKey {
        public static AnnotatedPropertyKey instance = null;

        static {
            instance = new AnnotatedPropertyKey();
        }

        public Class<? extends Annotation> annotation;
        public Class baseClass;

        public AnnotatedPropertyKey() {
        }

        public AnnotatedPropertyKey(Class<? extends Annotation> annotation, Class baseClass) {
            this.annotation = annotation;
            this.baseClass = baseClass;
        }

        @Override
        public int hashCode() {
            return annotation.hashCode() ^ baseClass.hashCode();
        }

        @Override
        public boolean equals(Object other_) {
            if (!(other_ instanceof AnnotatedPropertyKey))
                return false;

            AnnotatedPropertyKey other = (AnnotatedPropertyKey) other_;
            return other.annotation == annotation && other.baseClass == baseClass;
        }
    }

    private static class MemberAnnotationKey<T> {
        public T member;
        public Class<? extends Annotation> annotation;

        public MemberAnnotationKey(T member, Class<? extends Annotation> annotation) {
            this.member = member;
            this.annotation = annotation;
        }

        @Override
        public int hashCode() {
            return member.hashCode() ^ annotation.hashCode();
        }

        @Override
        public boolean equals(Object other_) {
            if (!(other_ instanceof MemberAnnotationKey))
                return false;

            MemberAnnotationKey other = (MemberAnnotationKey) other_;
            return other.member == member && other.annotation == annotation;
        }
    }

    private static class FieldAnnotationKey extends MemberAnnotationKey<Field> {
        static public FieldAnnotationKey instance = null;

        static {
            instance = new FieldAnnotationKey(null, null);
        }

        public FieldAnnotationKey(Field field, Class<? extends Annotation> annotation) {
            super(field, annotation);
        }
    }

    private static class MethodAnnotationKey extends MemberAnnotationKey<Method> {
        static public MethodAnnotationKey instance = null;

        static {
            instance = new MethodAnnotationKey(null, null);
        }

        public MethodAnnotationKey(Method method, Class<? extends Annotation> annotation) {
            super(method, annotation);
        }
    }

    private Map<AnnotatedPropertyKey, List<Field>> mAnnotatedFieldsCache;
    private Map<AnnotatedPropertyKey, List<Method>> mAnnotatedSetterMethodsCache;
    private Map<Class, List<Field>> mFieldsCache;
    private Map<Class, List<Method>> mSetterMethodsCache;
    private Map<FieldAnnotationKey, Annotation> mFieldAnnotationCache;
    private Map<MethodAnnotationKey, Annotation> mMethodAnnotationCache;

    public ReflectionCache() {
        mAnnotatedFieldsCache = new HashMap<AnnotatedPropertyKey, List<Field>>();
        mAnnotatedSetterMethodsCache = new HashMap<AnnotatedPropertyKey, List<Method>>();
        mFieldsCache = new HashMap<Class, List<Field>>();
        mSetterMethodsCache = new HashMap<Class, List<Method>>();
        mFieldAnnotationCache = new HashMap<FieldAnnotationKey, Annotation>();
        mMethodAnnotationCache = new HashMap<MethodAnnotationKey, Annotation>();
    }

    public List<Field> queryCachedAnnotatedFields(Class<? extends Annotation> annotation,
                                                  Class baseClass) {
        AnnotatedPropertyKey key = AnnotatedPropertyKey.instance;
        key.annotation = annotation;
        key.baseClass = baseClass;

        if (mAnnotatedFieldsCache.containsKey(key))
            return mAnnotatedFieldsCache.get(key);

        return null;
    }

    public List<Method> queryCachedAnnotatedSetterMethods(Class<? extends Annotation> annotation,
                                                          Class baseClass) {
        AnnotatedPropertyKey key = AnnotatedPropertyKey.instance;
        key.annotation = annotation;
        key.baseClass = baseClass;

        if (mAnnotatedSetterMethodsCache.containsKey(key))
            return mAnnotatedSetterMethodsCache.get(key);

        return null;
    }

    public List<Field> queryCachedFields(Class baseClass) {
        if (mFieldsCache.containsKey(baseClass))
            return mFieldsCache.get(baseClass);

        return null;
    }

    public List<Method> queryCachedSetterMethods(Class baseClass) {
        if (mSetterMethodsCache.containsKey(baseClass))
            return mSetterMethodsCache.get(baseClass);

        return null;
    }

    public Annotation queryFieldAnnotation(Field field, Class<? extends Annotation> annotationClass) {
        FieldAnnotationKey key = FieldAnnotationKey.instance;
        key.member = field;
        key.annotation = annotationClass;

        if (mFieldAnnotationCache.containsKey(key)) {
            return mFieldAnnotationCache.get(key);
        }

        return null;
    }

    public Annotation queryMethodAnnotation(Method method, Class<? extends Annotation> annotationClass) {
        MethodAnnotationKey key = MethodAnnotationKey.instance;
        key.member = method;
        key.annotation = annotationClass;

        if (mFieldAnnotationCache.containsKey(key)) {
            return mFieldAnnotationCache.get(key);
        }

        return null;
    }

    public void cacheAnnotatedFields(Class<? extends Annotation> annotation, Class baseClass,
                                     List<Field> fields) {
        mAnnotatedFieldsCache.put(new AnnotatedPropertyKey(annotation, baseClass), fields);
    }

    public void cacheAnnotatedSetterMethods(Class<? extends Annotation> annotation, Class baseClass,
                                            List<Method> methods) {
        mAnnotatedSetterMethodsCache.put(new AnnotatedPropertyKey(annotation, baseClass), methods);
    }

    public void cacheFields(Class baseClass, List<Field> fields) {
        mFieldsCache.put(baseClass, fields);
    }

    public void cacheSetterMethods(Class baseClass, List<Method> methods) {
        mSetterMethodsCache.put(baseClass, methods);
    }

    public void cacheFieldAnnotation(Field field, Class<? extends Annotation> annotationClass, Annotation a) {
        mFieldAnnotationCache.put(new FieldAnnotationKey(field, annotationClass), a);
    }

    public void cacheMethodAnnotation(Method method, Class<? extends Annotation> annotationClass, Annotation a) {
        mMethodAnnotationCache.put(new MethodAnnotationKey(method, annotationClass), a);
    }

    public boolean containsFieldAnnotation(Field field, Class<? extends Annotation> annotationClass) {
        FieldAnnotationKey key = FieldAnnotationKey.instance;
        key.member = field;
        key.annotation = annotationClass;

        return mFieldAnnotationCache.containsKey(key);
    }

    public boolean containsMethodAnnotation(Method method, Class<? extends Annotation> annotationClass) {
        MethodAnnotationKey key = MethodAnnotationKey.instance;
        key.member = method;
        key.annotation = annotationClass;

        return mFieldAnnotationCache.containsKey(key);
    }

}
