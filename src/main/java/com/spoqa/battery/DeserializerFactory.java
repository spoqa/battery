/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
 */

package com.spoqa.battery;

import com.spoqa.battery.annotations.Response;
import com.spoqa.battery.annotations.ResponseObject;
import com.spoqa.battery.codecs.JsonCodec;
import com.spoqa.battery.exceptions.DeserializationException;
import com.spoqa.battery.exceptions.IncompatibleTypeException;
import com.spoqa.battery.exceptions.MissingFieldException;
import com.spoqa.battery.exceptions.RpcException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public final class DeserializerFactory {
    private static final String TAG = "DeserializerFactory";

    private static Map<String, ResponseDeserializer> sDeserializerMap;

    static {
        sDeserializerMap = new HashMap<String, ResponseDeserializer>();
        registerDeserializer(new JsonCodec());
    }

    public static void registerDeserializer(ResponseDeserializer deserializer) {
        try {
            sDeserializerMap.put(deserializer.deserializationContentType(), deserializer);
        } catch (Exception e) {
            Logger.error(TAG, "Could not register deserializer class");
            e.printStackTrace();
        }
    }

    public static void deserialize(String contentType, String input,
                                   Object object, FieldNameTranslator translator)
            throws DeserializationException {
        String mime = extractMime(contentType);

        if (Config.DEBUG_DUMP_RESPONSE) {
            Logger.debug(TAG, "Mime: " + mime);
            Logger.debug(TAG, "Response: " + input);
        }
        
        if (!sDeserializerMap.containsKey(mime)) {
            RpcException e = new RpcException(String.format("No deserializer associated with MIME type %1$s", mime));
            throw new DeserializationException(e);
        }

        ReflectionCache cache = new ReflectionCache();

        List<Field> responseObjects = CodecUtils.getAnnotatedFields(cache, ResponseObject.class,
                object.getClass());

        if (responseObjects == null || responseObjects.size() == 0) {
            deserializeObject(cache, sDeserializerMap.get(mime), input, object, translator, true);
        } else if (responseObjects.size() > 1) {
            RpcException e = new RpcException(String.format("Object '%1$s' has more than one ResponseObject declarations",
                    object.getClass().getName()));
            throw new DeserializationException(e);
        } else {
            Field destField = responseObjects.get(0);
            try {
                List<Field> responseFields = CodecUtils.getAnnotatedFields(cache, Response.class, object.getClass());
                if (responseFields != null && responseFields.size() > 0) {
                    RpcException e = new RpcException(
                            String.format("Object '%1$s' has both ResponseObject and Response declarations",
                                    object.getClass().getName()));
                    throw new DeserializationException(e);
                }

                Object dest = destField.getType().newInstance();
                deserializeObject(cache, sDeserializerMap.get(mime), input, dest, translator, false);
                destField.set(object, dest);
            } catch (InstantiationException e) {
                throw new DeserializationException(e);
            } catch (IllegalAccessException e) {
                throw new DeserializationException(e);
            }
        }
    }

    private static String extractMime(String contentType) {
        String[] parts = contentType.split(";");
        if (parts.length == 0)
            return null;
        return parts[0].trim();
    }

    private static void deserializeObject(ReflectionCache cache,
                                          ResponseDeserializer deserializer, String input, Object object,
                                          FieldNameTranslator translator, boolean filterByAnnotation)
            throws DeserializationException {
        /* Let's assume the root element is always an object */
        Object internalObject = deserializer.parseInput(input);

        visitObject(cache, deserializer, internalObject, object, translator, filterByAnnotation);
    }

    private static void visitObject(ReflectionCache cache, ResponseDeserializer deserializer,
                                    Object internalObject, Object dest, FieldNameTranslator translator,
                                    boolean filterByAnnotation)
            throws DeserializationException {
        List<Field> fields;

        if (filterByAnnotation)
            fields = CodecUtils.getAnnotatedFields(cache, Response.class, dest.getClass());
        else
            fields = CodecUtils.getAllFields(cache, dest.getClass());

        try {
            for (Field f : fields) {
                String fieldName = f.getName();
                String docName = null;
                boolean explicit = false;
                boolean hasValue = false;
                Class fieldType = f.getType();

                if (Config.DEBUG_DUMP_RESPONSE) {
                    Logger.debug(TAG, "read field " + fieldName);
                }

                Response annotation;
                if (cache.containsFieldAnnotation(f, Response.class)) {
                    annotation = (Response) cache.queryFieldAnnotation(f, Response.class);
                } else {
                    annotation = f.getAnnotation(Response.class);
                    cache.cacheFieldAnnotation(f, Response.class, annotation);
                }

                if (annotation != null) {
                    if (annotation.fieldName().length() > 0) {
                        docName = annotation.fieldName();
                        explicit = true;
                    }
                }

                if (docName == null)
                    docName = translator.localToRemote(fieldName);

                /* check for field names */
                Object value = null;
                if (explicit && docName.contains(".")) {
                    try {
                        value = findChild(deserializer, internalObject, docName);
                        hasValue = true;
                    } catch (NoSuchElementException e) {
                        value = null;
                    }
                } else if (internalObject != null) {
                    value = deserializer.queryObjectChild(internalObject, docName);
                    hasValue = deserializer.containsChild(internalObject, docName);
                }

                if (!explicit && !hasValue) {
                    /* fall back to the untransformed name */
                    if (deserializer.containsChild(internalObject, fieldName)) {
                        value = deserializer.queryObjectChild(internalObject, fieldName);
                        hasValue = true;
                    }
                }

                if (annotation != null && annotation.mandatory() && !hasValue) {
                    /* check for mandatory field */
                    throw new DeserializationException(new MissingFieldException(f.getName()));
                }

                if (hasValue) {
                    if (internalObject == null) {
                        f.set(dest, null);
                    } else if (FieldCodecCollections.contains(fieldType)) {
                        FieldCodec codec = FieldCodecCollections.query(fieldType);
                        f.set(dest, codec.decode(value.toString()));
                    } else if (CodecUtils.isString(fieldType)) {
                        f.set(dest, value.toString());
                    } else if (CodecUtils.isInteger(fieldType)) {
                        f.setInt(dest, CodecUtils.parseInteger(fieldName, value));
                    } else if (CodecUtils.isLong(fieldType)) {
                        f.setLong(dest, CodecUtils.parseLong(fieldName, value));
                    } else if (CodecUtils.isList(fieldType)) {
                        if (fieldType != List.class && fieldType != ArrayList.class) {
                            Logger.error(TAG, String.format("field '%1$s' is not ArrayList or its superclass.",
                                    fieldName));
                            continue;
                        }
                        if (!deserializer.isArray(value.getClass())) {
                            Logger.error(TAG, String.format("internal class of '%1$s' is not an array",
                                    fieldName));
                        }
                        List newList = ArrayList.class.newInstance();
                        visitArray(cache, deserializer, value, newList, dest.getClass(), f, translator);
                        f.set(dest, newList);
                    } else if (CodecUtils.isMap(fieldType)) {
                        Map newMap = (Map) fieldType.newInstance();
                        visitMap(cache, deserializer, value, newMap);
                        f.set(dest, newMap);
                    } else if (CodecUtils.isBoolean(fieldType)) {
                        f.setBoolean(dest, CodecUtils.parseBoolean(fieldName, value));
                    } else if (CodecUtils.isFloat(fieldType)) {
                        f.setFloat(dest, CodecUtils.parseFloat(fieldName, value));
                    } else if (CodecUtils.isDouble(fieldType)) {
                        f.setDouble(dest, CodecUtils.parseDouble(fieldName, value));
                    } else {
                        /* or it should be a POJO... */
                        Object newObject = fieldType.newInstance();
                        visitObject(cache, deserializer, value, newObject, translator, false);
                        f.set(dest, newObject);
                    }
                } else {
                    if (!CodecUtils.isPrimitive(fieldType))
                        f.set(dest, null);
                }
            }
        } catch (Exception e) {
            throw new DeserializationException(e);
        } catch (IncompatibleTypeException e) {
            throw new DeserializationException(e);
        }
    }

    private static void visitArray(ReflectionCache cache, ResponseDeserializer deserializer,
                            Object internalArray, List<?> output, Class body, Field field,
                            FieldNameTranslator translator) throws DeserializationException {
        Class innerType = CodecUtils.getGenericTypeOfField(body, field.getName());

        try {
            Method add = List.class.getDeclaredMethod("add", Object.class);
            Integer index = 0;
            for (Object element : deserializer.queryArrayChildren(internalArray)) {
                if (CodecUtils.isList(innerType)) {
                    /* TODO implement nested list */
                } else if (CodecUtils.isMap(innerType)) {
                    /* TODO implement nested map */
                } else if (deserializer.isObject(element.getClass())) {
                    Object o = innerType.newInstance();
                    visitObject(cache, deserializer, element, o, translator, false);
                    add.invoke(output, o);
                } else {
                    Object newElem = element;
                    try {
                        if (CodecUtils.isString(innerType))
                            newElem = CodecUtils.parseString(element);
                        else if (CodecUtils.isInteger(innerType))
                            newElem = CodecUtils.parseInteger(index.toString(), element);
                        else if (CodecUtils.isBoolean(innerType))
                            newElem = CodecUtils.parseBoolean(index.toString(), element);
                        else if (CodecUtils.isDouble(innerType))
                            newElem = CodecUtils.parseDouble(index.toString(), element);
                        else if (CodecUtils.isFloat(innerType))
                            newElem = CodecUtils.parseFloat(index.toString(), element);
                        else if (CodecUtils.isLong(innerType))
                            newElem = CodecUtils.parseLong(index.toString(), element);
                    } catch (IncompatibleTypeException e) {
                        throw new DeserializationException(e);
                    }

                    add.invoke(output, newElem);

                    ++index;
                }
            }
        } catch (NoSuchMethodException e) {
            // there's no List without add()
        } catch (InvocationTargetException e) {
            throw new DeserializationException(e);
        } catch (IllegalAccessException e) {
            throw new DeserializationException(e);
        } catch (InstantiationException e) {
            throw new DeserializationException(e);
        }
    }

    private static void visitMap(ReflectionCache cache, ResponseDeserializer deserializer,
                                 Object internalObject, Map<?, ?> m) throws DeserializationException {
        /* TODO implement deserialization into Map<?,?> */
    }

    private static Object findChild(ResponseDeserializer deserializer, Object internalObject,
                             String path) throws NoSuchElementException {
        String[] frags = path.split("\\.");

        // Return one child if the key's not meant to be a path
        if (deserializer.containsChild(internalObject, path))
            return deserializer.queryObjectChild(internalObject, path);

        int i = 0;
        for (String frag : frags) {
            if (++i == frags.length) {
                // if the last object
                if (!deserializer.containsChild(internalObject, frag))
                    throw new NoSuchElementException();
                return deserializer.queryObjectChild(internalObject, frag);
            } else {
                internalObject = deserializer.queryObjectChild(internalObject, frag);
                if (internalObject == null)
                    break;
            }
        }

        throw new NoSuchElementException();
    }

}
