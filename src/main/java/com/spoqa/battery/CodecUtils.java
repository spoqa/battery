/**
 * Copyright (c) 2014-2015 Spoqa, All Rights Reserved.
 */

package com.spoqa.battery;

import com.spoqa.battery.annotations.Response;
import com.spoqa.battery.annotations.ResponseObject;
import com.spoqa.battery.exceptions.IncompatibleTypeException;
import com.spoqa.battery.exceptions.RpcException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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

    private static String[] EXCLUDED_OBJECT_PACKAGE_PREFIXES = {
            "java.",
            "android."
    };

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

    public static boolean isIntegerPrimitive(Class clazz) {
        return clazz == PRIMITIVE_TYPE_INTEGER;
    }

    public static boolean isIntegerBoxed(Class clazz) {
        return clazz == PRIMITIVE_TYPE_INTEGER_BOXED;
    }

    public static boolean isLong(Class clazz) {
        return clazz == PRIMITIVE_TYPE_LONG ||
                clazz == PRIMITIVE_TYPE_LONG_BOXED;
    }

    public static boolean isLongPrimitive(Class clazz) {
        return clazz == PRIMITIVE_TYPE_LONG;
    }

    public static boolean isLongBoxed(Class clazz) {
        return clazz == PRIMITIVE_TYPE_LONG_BOXED;
    }

    public static boolean isFloat(Class clazz) {
        return clazz == PRIMITIVE_TYPE_FLOAT ||
                clazz == PRIMITIVE_TYPE_FLOAT_BOXED;
    }

    public static boolean isFloatPrimitive(Class clazz) {
        return clazz == PRIMITIVE_TYPE_FLOAT;
    }

    public static boolean isFloatBoxed(Class clazz) {
        return clazz == PRIMITIVE_TYPE_FLOAT_BOXED;
    }

    public static boolean isDouble(Class clazz) {
        return clazz == PRIMITIVE_TYPE_DOUBLE ||
                clazz == PRIMITIVE_TYPE_DOUBLE_BOXED;
    }

    public static boolean isDoublePrimitive(Class clazz) {
        return clazz == PRIMITIVE_TYPE_DOUBLE;
    }

    public static boolean isDoubleBoxed(Class clazz) {
        return clazz == PRIMITIVE_TYPE_DOUBLE_BOXED;
    }

    public static boolean isBoolean(Class clazz) {
        return clazz == PRIMITIVE_TYPE_BOOLEAN ||
                clazz == PRIMITIVE_TYPE_BOOLEAN_BOXED;
    }

    public static boolean isBooleanPrimitive(Class clazz) {
        return clazz == PRIMITIVE_TYPE_BOOLEAN;
    }

    public static boolean isBooleanBoxed(Class clazz) {
        return clazz == PRIMITIVE_TYPE_BOOLEAN_BOXED;
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

    public static boolean isBuiltIn(Class clazz) {
        if (isString(clazz) || isPrimitive(clazz))
            return true;

        return false;
    }

    public static Class getGenericTypeOfField(Class clazz, String fieldName) {
        try {
            Field field = clazz.getField(fieldName);
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
            Object o = type.getActualTypeArguments()[genericTypePosition];
            if (o instanceof TypeVariable) {
                return resolveActualTypeArgs(clazz, (TypeVariable) o);
            } else {
                return (Class) type.getActualTypeArguments()[genericTypePosition];
            }
        } catch (NoSuchFieldException e) {
            Logger.error(TAG, String.format("No such field %1$s in %2$s", fieldName, clazz.getName()));
            e.printStackTrace();
            return null;
        }
    }

    public static <T> Class resolveActualTypeArgs (Class<? extends T> offspring, TypeVariable tv, Type... actualArgs) {
        assert offspring != null;
        assert actualArgs.length == 0 || actualArgs.length == offspring.getTypeParameters().length;

        Class base = (Class) tv.getGenericDeclaration();

        if (actualArgs.length == 0) {
            actualArgs = offspring.getTypeParameters();
        }

        Map<String, Type> typeVariables = new HashMap<String, Type>();
        for (int i = 0; i < actualArgs.length; i++) {
            TypeVariable<?> typeVariable = (TypeVariable<?>) offspring.getTypeParameters()[i];
            if (typeVariable.getName().equals(tv.getName()))
                return (Class) actualArgs[i];
            typeVariables.put(typeVariable.getName(), actualArgs[i]);
        }

        List<Type> ancestors = new LinkedList<Type>();
        if (offspring.getGenericSuperclass() != null) {
            ancestors.add(offspring.getGenericSuperclass());
        }
        for (Type t : offspring.getGenericInterfaces()) {
            ancestors.add(t);
        }

        for (Type type : ancestors) {
            if (type instanceof Class<?>) {
                Class<?> ancestorClass = (Class<?>) type;
                if (base.isAssignableFrom(ancestorClass)) {
                    Logger.debug(TAG, "recurse 1");
                    Class result = resolveActualTypeArgs((Class<? extends T>) ancestorClass, tv);
                    if (result != null) {
                        return result;
                    }
                }
            }
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type rawType = parameterizedType.getRawType();
                if (rawType instanceof Class<?>) {
                    Class<?> rawTypeClass = (Class<?>) rawType;
                    if (base.isAssignableFrom(rawTypeClass)) {

                        List<Type> resolvedTypes = new LinkedList<Type>();
                        for (Type t : parameterizedType.getActualTypeArguments()) {
                            if (t instanceof TypeVariable<?>) {
                                Type resolvedType = typeVariables.get(((TypeVariable<?>) t).getName());
                                resolvedTypes.add(resolvedType != null ? resolvedType : t);
                            } else {
                                resolvedTypes.add(t);
                            }
                        }

                        Logger.debug(TAG, "recurse 2");
                        Class result = resolveActualTypeArgs((Class<? extends T>) rawTypeClass, tv, resolvedTypes.toArray(new Type[] {}));
                        if (result != null) {
                            return result;
                        }
                    }
                }
            }
        }

        return null;
    }

    public static Class getGenericTypeOfMethod(Class clazz, String methodName, Class paramType) {
        try {
            Method method = clazz.getMethod(methodName, paramType);
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
        List<Method> methods;

        if (cache != null) {
            methods = cache.queryCachedAnnotatedSetterMethods(annotationType, baseClass);
            if (methods != null)
                return methods;
        }

        methods = new ArrayList<Method>();

        for (Method m : baseClass.getMethods()) {
            if (m.isAnnotationPresent(annotationType)) {
                if (m.getReturnType() != void.class || m.getParameterTypes().length != 1) {
                    Logger.warn(TAG, String.format("%1$s.%2$s() is not a setter",
                            baseClass.getName(), m.getName()));
                    continue;
                }
                methods.add(m);
            }
        }

        if (cache != null)
            cache.cacheAnnotatedSetterMethods(annotationType, baseClass, methods);

        return methods;
    }

    public static List<Method> getAllSetterMethods(ReflectionCache cache, Class baseClass) {
        List<Method> methods;

        if (cache != null) {
            methods = cache.queryCachedSetterMethods(baseClass);
            if (methods != null)
                return methods;
        }

        methods = new ArrayList<Method>();

        for (Method m : baseClass.getMethods()) {
                /* this method automatically filter out setter methods only starting with "set-" prefix */
            String methodName = m.getName().toLowerCase();
            if (m.getReturnType() == void.class && m.getParameterTypes().length == 1 &&
                    methodName.startsWith("set"))
                methods.add(m);
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
        } else if (o instanceof Long) {
            return ((Long) o).intValue();
        } else if (o instanceof Float) {
            return ((Float) o).intValue();
        } else if (o instanceof Double) {
            return ((Double) o).intValue();
        } else if (o instanceof String) {
            try {
                return Integer.parseInt((String) o);
            } catch (NumberFormatException e) {
                throw new IncompatibleTypeException(fieldName, Integer.class.getName(), (String) o);
            }
        } else {
            throw new IncompatibleTypeException(fieldName, Integer.class.getName(), o.toString());
        }
    }

    public static long parseLong(String fieldName, Object o) throws IncompatibleTypeException {
        if (o instanceof Long) {
            return (Long) o;
        } else if (o instanceof Integer) {
            return ((Integer) o).longValue();
        } else if (o instanceof Float) {
            return ((Float) o).longValue();
        } else if (o instanceof Double) {
            return ((Double) o).longValue();
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
        } else if (o instanceof Integer) {
            return ((Integer) o).floatValue();
        } else if (o instanceof Double) {
            return ((Double) o).floatValue();
        } else if (o instanceof Long) {
            return ((Long) o).floatValue();
        } else if (o instanceof String) {
            try {
                return Float.parseFloat((String) o);
            } catch (NumberFormatException e) {
                throw new IncompatibleTypeException(fieldName, Float.class.getName(), (String) o);
            }
        } else {
            throw new IncompatibleTypeException(fieldName, Float.class.getName(), o.toString());
        }
    }

    public static double parseDouble(String fieldName, Object o) throws IncompatibleTypeException {
        if (o instanceof Float) {
            return ((Float) o).doubleValue();
        }  else if (o instanceof Integer) {
            return ((Integer) o).doubleValue();
        } else if (o instanceof Double) {
            return (Double) o;
        } else if (o instanceof Long) {
            return ((Long) o).doubleValue();
        } else if (o instanceof String) {
            try {
                return Double.parseDouble((String) o);
            } catch (NumberFormatException e) {
                throw new IncompatibleTypeException(fieldName, Double.class.getName(), (String) o);
            }
        } else {
            throw new IncompatibleTypeException(fieldName, Double.class.getName(), o.toString());
        }
    }

    public static boolean parseBoolean(String fieldName, Object o) throws IncompatibleTypeException {
        if (o instanceof Boolean) {
            return (Boolean) o;
        } else if (o instanceof String) {
           return Boolean.parseBoolean((String) o);
        } else {
            throw new IncompatibleTypeException(fieldName, Boolean.class.getName(), o.toString());
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

    public static boolean shouldBeExcluded(Class clazz) {
        String package_ = clazz.getPackage().getName();
        for (String i : EXCLUDED_OBJECT_PACKAGE_PREFIXES)
            if (package_.startsWith(i))
                return true;

        return false;
    }

    public static Object parseEnum(Class enumType, String value) {
        if (!enumType.isEnum()) {
            Logger.error(TAG, String.format("type %1$s is not enum", enumType.getName()));
            return null;
        }

        String valueLowercase = value.toLowerCase();

        for (Object o : enumType.getEnumConstants()) {
            if (o.toString().toLowerCase().equals(valueLowercase)) {
                return o;
            }
        }

        Logger.warn(TAG, String.format("Could not found value '%1$s' for enum %2$s",
                value, enumType.getName()));

        return null;
    }

    public static Object getResponseObject(ReflectionCache cache, Object object, boolean overwrite) throws RpcException {
        List<Field> responseObjects = CodecUtils.getAnnotatedFields(cache, ResponseObject.class,
                object.getClass());

        if (responseObjects == null || responseObjects.size() == 0) {
            return null;
        } else if (responseObjects.size() > 1) {
            RpcException e = new RpcException(String.format("Object '%1$s' has more than one ResponseObject declarations",
                    object.getClass().getName()));
            throw e;
        } else {
            Field destField = responseObjects.get(0);
            try {
                List<Field> responseFields = CodecUtils.getAnnotatedFields(cache, Response.class, object.getClass());
                if (responseFields != null && responseFields.size() > 0) {
                    RpcException e = new RpcException(
                            String.format("Object '%1$s' has both ResponseObject and Response declarations",
                                    object.getClass().getName()));
                    throw e;
                }

                Object dest = destField.get(object);
                if (dest == null || overwrite) {
                    dest = destField.getType().newInstance();
                    destField.set(object, dest);
                }
                return dest;
            } catch (InstantiationException e) {
                throw new RpcException(String.format("Could not instantiate ResponseObject %1$s",
                        destField.getName()));
            } catch (IllegalAccessException e) {
                throw new RpcException(String.format("Could not instantiate ResponseObject %1$s",
                        destField.getName()));
            }
        }
    }

}
