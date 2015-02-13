/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
 */

package com.spoqa.battery;

import com.spoqa.battery.exceptions.IncompatibleTypeException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CodecUtils {
    public static final String TAG = "CodecUtils";

    private static final Class PRIMITIVE_TYPE_STRING = String.class;
    private static final Class PRIMITIVE_TYPE_INTEGER = int.class;
    private static final Class PRIMITIVE_TYPE_INTEGER_BOXED = Integer.class;
    private static final Class PRIMITIVE_TYPE_LONG = long.class;
    private static final Class PRIMITIVE_TYPE_LONG_BOXED = Long.class;
    private static final Class PRIMITIVE_TYPE_FLOAT = float.class;
    private static final Class PRIMITIVE_TYPE_FLOAT_BOXED = Float.class;
    private static final Class PRIMITIVE_TYPE_DOUBLE = double.class;
    private static final Class PRIMITIVE_TYPE_DOUBLE_BOXED = Double.class;
    private static final Class PRIMITIVE_TYPE_BOOLEAN = boolean.class;
    private static final Class PRIMITIVE_TYPE_BOOLEAN_BOXED = Boolean.class;
    private static final Class PRIMITIVE_TYPE_LIST = List.class;
    private static final Class PRIMITIVE_TYPE_MAP = Map.class;

    public static Iterable<KeyValuePair<String, Object>> traverseObject(
            ReflectionCache cache, Class<Annotation> annotationFilter, Object o) {
        List<KeyValuePair<String, Object>> list = new ArrayList<KeyValuePair<String, Object>>();

        for (Field field : getAllFields(cache, o.getClass())) {
            if (annotationFilter != null && field.getAnnotation(annotationFilter) == null) {
                /* if annotationFilter is given, filter by annotations */
                continue;
            }

            try {
                list.add(new KeyValuePair<String, Object>(field.getName(), field.get(o)));
            } catch (IllegalAccessException e) {
                Logger.error(TAG, e.toString());
            }
        }

        return list;
    }

    public static boolean isSubclassOf(Class clazz, Class what) {
        while (clazz != null) {
            if (clazz == what)
                return true;

            clazz = clazz.getSuperclass();
        }

        return false;
    }

    public static boolean isString(Object object) {
        return isSubclassOf(object.getClass(), PRIMITIVE_TYPE_STRING);
    }

    public static boolean isString(Class clazz) {
        return isSubclassOf(clazz, PRIMITIVE_TYPE_STRING);
    }

    public static boolean isInteger(Class clazz) {
        return clazz == PRIMITIVE_TYPE_INTEGER ||
                clazz == PRIMITIVE_TYPE_INTEGER_BOXED;
    }

    public static boolean isLong(Class clazz) {
        return clazz == PRIMITIVE_TYPE_LONG ||
                clazz == PRIMITIVE_TYPE_LONG_BOXED;
    }

    public static boolean isFloat(Class clazz) {
        return clazz == PRIMITIVE_TYPE_FLOAT ||
                clazz == PRIMITIVE_TYPE_FLOAT_BOXED;
    }

    public static boolean isDouble(Class clazz) {
        return clazz == PRIMITIVE_TYPE_DOUBLE ||
                clazz == PRIMITIVE_TYPE_DOUBLE_BOXED;
    }

    public static boolean isBoolean(Class clazz) {
        return clazz == PRIMITIVE_TYPE_BOOLEAN ||
                clazz == PRIMITIVE_TYPE_BOOLEAN_BOXED;
    }

    public static boolean isList(Class clazz) {
        return isSubclassOf(clazz, PRIMITIVE_TYPE_LIST);
    }

    public static boolean isMap(Class clazz) {
        return isSubclassOf(clazz, PRIMITIVE_TYPE_MAP);
    }

    public static boolean isPrimitive(Class clazz) {
        if (isInteger(clazz) || isLong(clazz) || isBoolean(clazz) ||
                isFloat(clazz) || isDouble(clazz))
            return true;

        return false;
    }

    public static Class getGenericTypeOfField(Class clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            Class declaringType = field.getType();
            int genericTypePosition;
            if (isSubclassOf(declaringType, PRIMITIVE_TYPE_LIST)) {
                genericTypePosition = 0;
            } else if (isSubclassOf(declaringType, PRIMITIVE_TYPE_MAP)) {
                genericTypePosition = 1;
            } else {
                Logger.error(TAG, String.format("Field %1$s is neither list nor map.", declaringType.getName()));
                return null;
            }
            ParameterizedType type = (ParameterizedType) field.getGenericType();
            return (Class) type.getActualTypeArguments()[genericTypePosition];
        } catch (NoSuchFieldException e) {
            Logger.error(TAG, String.format("No such field %1$s in %2$s", fieldName, clazz.getName()));
            e.printStackTrace();
            return null;
        }
    }

    public static Class getGenericTypeOfMethod(Class clazz, String methodName, Class paramType) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, paramType);
            int genericTypePosition;
            if (isSubclassOf(paramType, PRIMITIVE_TYPE_LIST)) {
                genericTypePosition = 0;
            } else if (isSubclassOf(paramType, PRIMITIVE_TYPE_MAP)) {
                genericTypePosition = 1;
            } else {
                Logger.error(TAG, String.format("Field %1$s is neither list nor map.", paramType.getName()));
                return null;
            }
            ParameterizedType type = (ParameterizedType) method.getGenericParameterTypes()[0];
            return (Class) type.getActualTypeArguments()[genericTypePosition];
        } catch (NoSuchMethodException e) {
            Logger.error(TAG, String.format("No such field %1$s in %2$s", methodName, clazz.getName()));
            e.printStackTrace();
            return null;
        }
    }

    public static List<Field> getAnnotatedFields(ReflectionCache cache,
                                                 Class<? extends Annotation> annotationType,
                                                 Class baseClass) {
        List<Field> fields;

        if (cache != null) {
            fields = cache.queryCachedAnnotatedFields(annotationType, baseClass);
            if (fields != null)
                return fields;
        }

        fields = new ArrayList<Field>();

        for (Field f : baseClass.getFields()) {
            if (f.isAnnotationPresent(annotationType)) {
                Logger.debug(TAG, f.getName());
                fields.add(f);
            }
        }

        if (cache != null)
            cache.cacheAnnotatedFields(annotationType, baseClass, fields);

        return fields;
    }

    public static List<Field> getAllFields(ReflectionCache cache, Class baseClass) {
        List<Field> fields;

        if (cache != null) {
            fields = cache.queryCachedFields(baseClass);
            if (fields != null)
                return fields;
        }

        fields = new ArrayList<Field>();

        for (Field f : baseClass.getFields()) {
            fields.add(f);
        }

        if (cache != null)
            cache.cacheFields(baseClass, fields);

        return fields;
    }

    public static List<Method> getAnnotatedSetterMethods(ReflectionCache cache,
                                                   Class<? extends Annotation> annotationType,
                                                   Class baseClass) {
        Class curClass = baseClass;
        List<Method> methods;

        if (cache != null) {
            methods = cache.queryCachedAnnotatedSetterMethods(annotationType, baseClass);
            if (methods != null)
                return methods;
        }

        methods = new ArrayList<Method>();

        while (curClass != Object.class && curClass != null) {
            for (Method m : curClass.getMethods()) {
                if (m.isAnnotationPresent(annotationType)) {
                    if (m.getReturnType() != void.class || m.getParameterTypes().length != 1) {
                        Logger.warn(TAG, String.format("%1$s.%2$s() is not a setter",
                                curClass.getName(), m.getName()));
                        continue;
                    }
                    methods.add(m);
                }
            }
            curClass = curClass.getSuperclass();
        }

        if (cache != null)
            cache.cacheAnnotatedSetterMethods(annotationType, baseClass, methods);

        return methods;
    }

    public static List<Method> getAllSetterMethods(ReflectionCache cache, Class baseClass) {
        Class curClass = baseClass;
        List<Method> methods;

        if (cache != null) {
            methods = cache.queryCachedSetterMethods(baseClass);
            if (methods != null)
                return methods;
        }

        methods = new ArrayList<Method>();

        while (curClass != Object.class && curClass != null) {
            for (Method m : curClass.getMethods()) {
                /* this method automatically filter out setter methods only starting with "set-" prefix */
                String methodName = m.getName().toLowerCase();
                if (m.getReturnType() == void.class && m.getParameterTypes().length == 1 &&
                        methodName.startsWith("set"))
                    methods.add(m);
            }
            curClass = curClass.getSuperclass();
        }

        if (cache != null)
            cache.cacheSetterMethods(baseClass, methods);

        return methods;
    }

    public static String parseString(Object o) {
        return o.toString();
    }

    public static int parseInteger(String fieldName, Object o) throws IncompatibleTypeException {
        if (o instanceof Integer) {
            return (Integer) o;
        } else if (o instanceof String) {
            try {
                return Integer.parseInt((String) o);
            } catch (NumberFormatException e) {
                throw new IncompatibleTypeException(fieldName, Integer.class.getName(), (String) o);
            }
        } else {
            throw new IncompatibleTypeException(fieldName, Integer.class.getName(), (String) o);
        }
    }

    public static long parseLong(String fieldName, Object o) throws IncompatibleTypeException {
        if (o instanceof Long) {
            return (Long) o;
        } else if (o instanceof String) {
            try {
                return Long.parseLong((String) o);
            } catch (NumberFormatException e) {
                throw new IncompatibleTypeException(fieldName, Long.class.getName(), (String) o);
            }
        } else {
            throw new IncompatibleTypeException(fieldName, Long.class.getName(), (String) o);
        }
    }

    public static float parseFloat(String fieldName, Object o) throws IncompatibleTypeException {
        if (o instanceof Float) {
            return (Float) o;
        } else if (o instanceof Double) {
            return ((Double) o).floatValue();
        } else if (o instanceof Integer) {
            return ((Integer) o).floatValue();
        } else if (o instanceof String) {
            try {
                return Float.parseFloat((String) o);
            } catch (NumberFormatException e) {
                throw new IncompatibleTypeException(fieldName, Float.class.getName(), (String) o);
            }
        } else {
            throw new IncompatibleTypeException(fieldName, Float.class.getName(), (String) o);
        }
    }

    public static double parseDouble(String fieldName, Object o) throws IncompatibleTypeException {
        if (o instanceof Float) {
            return ((Float) o).doubleValue();
        } else if (o instanceof Double) {
            return (Double) o;
        } else if (o instanceof Integer) {
            return ((Integer) o).doubleValue();
        } else if (o instanceof String) {
            try {
                return Double.parseDouble((String) o);
            } catch (NumberFormatException e) {
                throw new IncompatibleTypeException(fieldName, Double.class.getName(), (String) o);
            }
        } else {
            throw new IncompatibleTypeException(fieldName, Double.class.getName(), (String) o);
        }
    }

    public static boolean parseBoolean(String fieldName, Object o) throws IncompatibleTypeException {
        if (o instanceof Boolean) {
            return (Boolean) o;
        } else if (o instanceof String) {
           return Boolean.parseBoolean((String) o);
        } else {
            throw new IncompatibleTypeException(fieldName, Boolean.class.getName(), (String) o);
        }
    }

    public static String normalizeSetterName(String name) {
        if (name.startsWith("set_")) {
            return name.substring(4);
        } else if (name.startsWith("set")) {
            name = name.substring(3);
            return name.substring(0, 1).toLowerCase() + name.substring(1);
        } else if (name.startsWith("Set")) {
            return name.substring(3);
        } else {
            return name;
        }
    }

}
