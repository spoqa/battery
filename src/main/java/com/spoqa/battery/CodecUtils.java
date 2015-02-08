/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
 */

package com.spoqa.battery;

import com.spoqa.battery.exceptions.IncompatibleTypeException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
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
            Class<Annotation> annotationFilter, Object o) {
        List<KeyValuePair<String, Object>> list = new ArrayList<KeyValuePair<String, Object>>();

        for (Field field : getAllFields(o.getClass())) {
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

    public static boolean isInteger(Object object) {
        return object.getClass() == PRIMITIVE_TYPE_INTEGER ||
                object.getClass() == PRIMITIVE_TYPE_INTEGER_BOXED;
    }

    public static boolean isInteger(Class clazz) {
        return clazz == PRIMITIVE_TYPE_INTEGER ||
                clazz == PRIMITIVE_TYPE_INTEGER_BOXED;
    }

    public static boolean isLong(Object object) {
        return object.getClass() == PRIMITIVE_TYPE_LONG ||
                object.getClass() == PRIMITIVE_TYPE_LONG_BOXED;
    }

    public static boolean isLong(Class clazz) {
        return clazz == PRIMITIVE_TYPE_LONG ||
                clazz == PRIMITIVE_TYPE_LONG_BOXED;
    }

    public static boolean isFloat(Object object) {
        return object.getClass() == PRIMITIVE_TYPE_FLOAT ||
                object.getClass() == PRIMITIVE_TYPE_FLOAT_BOXED;
    }

    public static boolean isFloat(Class clazz) {
        return clazz == PRIMITIVE_TYPE_FLOAT ||
                clazz == PRIMITIVE_TYPE_FLOAT_BOXED;
    }

    public static boolean isDouble(Object object) {
        return object.getClass() == PRIMITIVE_TYPE_DOUBLE ||
                object.getClass() == PRIMITIVE_TYPE_DOUBLE_BOXED;
    }

    public static boolean isDouble(Class clazz) {
        return clazz == PRIMITIVE_TYPE_DOUBLE ||
                clazz == PRIMITIVE_TYPE_DOUBLE_BOXED;
    }

    public static boolean isBoolean(Object object) {
        return object.getClass() == PRIMITIVE_TYPE_BOOLEAN ||
                object.getClass() == PRIMITIVE_TYPE_BOOLEAN_BOXED;
    }

    public static boolean isBoolean(Class clazz) {
        return clazz == PRIMITIVE_TYPE_BOOLEAN ||
                clazz == PRIMITIVE_TYPE_BOOLEAN_BOXED;
    }

    public static boolean isList(Object object) {
        return isSubclassOf(object.getClass(), PRIMITIVE_TYPE_LIST);
    }

    public static boolean isList(Class clazz) {
        return isSubclassOf(clazz, PRIMITIVE_TYPE_LIST);
    }

    public static boolean isMap(Object object) {
        return isSubclassOf(object.getClass(), PRIMITIVE_TYPE_MAP);
    }

    public static boolean isMap(Class clazz) {
        return isSubclassOf(clazz, PRIMITIVE_TYPE_MAP);
    }

    public static boolean isPrimitive(Class clazz) {
        if (clazz == PRIMITIVE_TYPE_BOOLEAN ||
                clazz == PRIMITIVE_TYPE_FLOAT ||
                clazz == PRIMITIVE_TYPE_DOUBLE ||
                clazz == PRIMITIVE_TYPE_INTEGER ||
                clazz == PRIMITIVE_TYPE_LONG)
            return true;

        return false;
    }

    public static Class getGenericTypeOfField(Class clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            Class declaringType = field.getType();
            int genericTypePosition = -1;
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

    public static List<Field> getAnnotatedFields(Class<? extends Annotation> annotationType, Class baseClass) {
        Class curClass = baseClass;
        List<Field> fields = new ArrayList<Field>();

        while (curClass != Object.class && curClass != null) {
            for (Field f : curClass.getDeclaredFields()) {
                if (f.isAnnotationPresent(annotationType)) {
                    fields.add(f);
                }
            }
            curClass = curClass.getSuperclass();
        }

        return fields;
    }

    public static List<Field> getAllFields(Class baseClass) {
        Class curClass = baseClass;
        List<Field> fields = new ArrayList<Field>();

        while (curClass != Object.class && curClass != null) {
            for (Field f : curClass.getDeclaredFields()) {
                fields.add(f);
            }
            curClass = curClass.getSuperclass();
        }

        return fields;
    }

    public static String parseString(Object o) {
        return o.toString();
    }

    public static int parseInteger(String fieldName, Object o) throws IncompatibleTypeException {
        if (o instanceof Integer) {
            return ((Integer) o).intValue();
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
            return ((Long) o).longValue();
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
            return ((Float) o).floatValue();
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
            return ((Double) o).doubleValue();
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
            return ((Boolean) o).booleanValue();
        } else if (o instanceof String) {
           return Boolean.parseBoolean((String) o);
        } else {
            throw new IncompatibleTypeException(fieldName, Boolean.class.getName(), (String) o);
        }
    }

}
