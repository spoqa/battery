/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
 */

package com.spoqa.battery;

import com.spoqa.battery.annotations.Response;
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

    public static void deserialize(String mime, String input, Object object,
                                   FieldNameTransformer transformer) throws DeserializationException {
        if (Config.DEBUG_DUMP_RESPONSE) {
            Logger.debug(TAG, "Mime: " + mime);
            Logger.debug(TAG, "Response: " + input);
        }

        if (!sDeserializerMap.containsKey(mime)) {
            RpcException e = new RpcException(String.format("No deserializer associated with MIME type %1$s", mime));
            throw new DeserializationException(e);
        }

        deserializeObject(sDeserializerMap.get(mime), input, object, transformer);
    }

    private static void deserializeObject(ResponseDeserializer deserializer, String input, Object object,
                                          FieldNameTransformer transformer)
            throws DeserializationException {
        /* Let's assume the root element is always an object */
        Object internalObject = deserializer.parseInput(input);

        visitObject(deserializer, internalObject, object, transformer, true);
    }

    private static void visitObject(ResponseDeserializer deserializer, Object internalObject,
                                    Object dest, FieldNameTransformer transformer, boolean filterByAnnotation)
            throws DeserializationException {
        List<Field> fields;

        if (filterByAnnotation)
            fields = CodecUtils.getAnnotatedFields(Response.class, dest.getClass());
        else
            fields = CodecUtils.getAllFields(dest.getClass());

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

                Response annotation = f.getAnnotation(Response.class);
                if (annotation != null) {
                    if (annotation.fieldName().length() > 0) {
                        docName = annotation.fieldName();
                        explicit = true;
                    }
                }

                if (docName == null)
                    docName = transformer.localToRemote(fieldName);

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
                        if (deserializer.isArray(value.getClass())) {
                            Logger.error(TAG, String.format("internal class of '%1$s' is not an array",
                                    fieldName));
                        }
                        List newList = ArrayList.class.newInstance();
                        visitArray(deserializer, value, newList, dest.getClass(), f, transformer);
                        f.set(dest, newList);
                    } else if (CodecUtils.isMap(fieldType)) {
                        Map newMap = (Map) fieldType.newInstance();
                        visitMap(deserializer, value, newMap);
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
                        visitObject(deserializer, value, newObject, transformer, false);
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

    private static void visitArray(ResponseDeserializer deserializer, Object internalArray,
                            List<?> output, Class body, Field field,
                            FieldNameTransformer transformer) throws DeserializationException {
        Class innerType = CodecUtils.getGenericTypeOfField(body, field.getName());

        try {
            Method add = List.class.getDeclaredMethod("add", Object.class);
            for (Object element : deserializer.queryArrayChildren(internalArray)) {
                if (CodecUtils.isList(innerType)) {
                    /* TODO implement nested list */
                } else if (CodecUtils.isMap(innerType)) {
                    /* TODO implement nested map */
                } else if (deserializer.isObject(element.getClass())) {
                    Object o = innerType.newInstance();
                    visitObject(deserializer, element, o, transformer, false);
                    add.invoke(output, o);
                } else {
                    add.invoke(output, element);
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

    private static void visitMap(ResponseDeserializer deserializer, Object internalObject,
                          Map<?, ?> m) throws DeserializationException {
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
                return deserializer.queryObjectChild(internalObject, frag);
            } else {
                internalObject = deserializer.queryObjectChild(internalObject, frag);
                if (internalObject == null || deserializer.isObject(internalObject.getClass()))
                    break;
            }
        }

        throw new NoSuchElementException();
    }

}
