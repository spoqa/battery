package com.spoqa.battery;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReflectionCache {

    private static class AnnotatedFieldKey {
        public static AnnotatedFieldKey instance = null;

        static {
            instance = new AnnotatedFieldKey();
        }

        public Class<? extends Annotation> annotation;
        public Class baseClass;

        public AnnotatedFieldKey() {
        }

        public AnnotatedFieldKey(Class<? extends Annotation> annotation, Class baseClass) {
            this.annotation = annotation;
            this.baseClass = baseClass;
        }

        @Override
        public int hashCode() {
            return annotation.hashCode() ^ baseClass.hashCode();
            //return String.format("%1$s/%2$s", annotation.getName(), baseClass.getName()).hashCode();
        }

        @Override
        public boolean equals(Object other_) {
            if (!(other_ instanceof AnnotatedFieldKey))
                return false;

            AnnotatedFieldKey other = (AnnotatedFieldKey) other_;
            return other.annotation == annotation && other.baseClass == baseClass;
        }
    }

    private static class FieldAnnotationKey {
        static public FieldAnnotationKey instance = null;

        static {
            instance = new FieldAnnotationKey();
        }

        public Field field;
        public Class<? extends Annotation> annotation;

        public FieldAnnotationKey() {
        }

        public FieldAnnotationKey(Field field, Class<? extends Annotation> annotation) {
            this.field = field;
            this.annotation = annotation;
        }

        @Override
        public int hashCode() {
            return field.hashCode() ^ annotation.hashCode();
            /*return String.format("%1$s %2$s %3$s", field.getName(),
                    field.getDeclaringClass().getName(), annotation.getName()).hashCode();*/
        }

        @Override
        public boolean equals(Object other_) {
            if (!(other_ instanceof FieldAnnotationKey))
                return false;

            FieldAnnotationKey other = (FieldAnnotationKey) other_;
            return other.field == field && other.annotation == annotation;
        }
    }

    private Map<AnnotatedFieldKey, List<Field>> mAnnotatedFieldsCache;
    private Map<Class, List<Field>> mFieldsCache;
    private Map<FieldAnnotationKey, Annotation> mFieldAnnotationCache;

    public ReflectionCache() {
        mAnnotatedFieldsCache = new HashMap<AnnotatedFieldKey, List<Field>>();
        mFieldsCache = new HashMap<Class, List<Field>>();
        mFieldAnnotationCache = new HashMap<FieldAnnotationKey, Annotation>();
    }

    public List<Field> queryCachedAnnotatedFields(Class<? extends Annotation> annotation, Class baseClass) {
        AnnotatedFieldKey key = AnnotatedFieldKey.instance;
        key.annotation = annotation;
        key.baseClass = baseClass;

        if (mAnnotatedFieldsCache.containsKey(key))
            return mAnnotatedFieldsCache.get(key);

        return null;
    }

    public List<Field> queryCachedFields(Class baseClass) {
        if (mFieldsCache.containsKey(baseClass))
            return mFieldsCache.get(baseClass);

        return null;
    }

    public Annotation queryFieldAnnotation(Field field, Class<? extends Annotation> annotationClass) {
        FieldAnnotationKey key = FieldAnnotationKey.instance;
        key.field = field;
        key.annotation = annotationClass;

        if (mFieldAnnotationCache.containsKey(key)) {
            return mFieldAnnotationCache.get(key);
        }

        return null;
    }

    public void cacheAnnotatedFields(Class<? extends Annotation> annotation, Class baseClass, List<Field> fields) {
        mAnnotatedFieldsCache.put(new AnnotatedFieldKey(annotation, baseClass), fields);
    }

    public void cacheFields(Class baseClass, List<Field> fields) {
        mFieldsCache.put(baseClass, fields);
    }

    public void cacheFieldAnnotation(Field field, Class<? extends Annotation> annotationClass, Annotation a) {
        mFieldAnnotationCache.put(new FieldAnnotationKey(field, annotationClass), a);
    }

    public boolean containsFieldAnnotation(Field field, Class<? extends Annotation> annotationClass) {
        return mFieldAnnotationCache.containsKey(new FieldAnnotationKey(field, annotationClass));
    }

}
