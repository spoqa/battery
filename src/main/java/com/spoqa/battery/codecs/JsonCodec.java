/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
 */

package com.spoqa.battery.codecs;

import com.spoqa.battery.CodecUtils;
import com.spoqa.battery.Config;
import com.spoqa.battery.FieldNameTransformer;
import com.spoqa.battery.Logger;
import com.spoqa.battery.RequestSerializer;
import com.spoqa.battery.ResponseDeserializer;
import com.spoqa.battery.annotations.RequestBody;
import com.spoqa.battery.annotations.Response;
import com.spoqa.battery.exceptions.DeserializationException;
import com.spoqa.battery.exceptions.IncompatibleTypeException;
import com.spoqa.battery.exceptions.MissingFieldException;
import com.spoqa.battery.exceptions.SerializationException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class JsonCodec implements RequestSerializer, ResponseDeserializer {
    private static final String TAG = "JsonCodec";

    public static final String MIME_TYPE = "application/json";

    public JsonCodec() {

    }

    @Override
    public byte[] serializeObject(Object o, FieldNameTransformer transformer)
            throws SerializationException {
        JSONObject body = visitObject(o);

        if (body != null) {
            try {
                return body.toString().getBytes("utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public String serializationContentType() {
        return MIME_TYPE;
    }

    private JSONObject visitObject(Object o) throws SerializationException {
        Iterable<Field> fields = CodecUtils.getAnnotatedFields(RequestBody.class, o.getClass());

        JSONObject body = new JSONObject();

        try {


        } catch (Exception e) {

        }

        if (body.length() > 0)
            return body;
        else
            return null;
    }

    @Override
    public void deserializeObject(String input, Object object, FieldNameTransformer transformer)
            throws DeserializationException {
        /* FIXME: currently the input is assumed to be a JSONObject */
        try {
            JSONObject jsonObj = new JSONObject(input);
            visitObject(jsonObj, object, transformer, true);
        } catch (JSONException e) {
            throw new DeserializationException(e);
        }
    }

    @Override
    public String deserializationContentType() {
        return MIME_TYPE;
    }

    private void visitObject(JSONObject object, Object o, FieldNameTransformer transformer,
                             boolean filterByAnnotation) throws DeserializationException {
        List<Field> fields;

        if (filterByAnnotation)
            fields = CodecUtils.getAnnotatedFields(Response.class, o.getClass());
        else
            fields = CodecUtils.getAllFields(o.getClass());

        try {
            for (Field f : fields) {
                String fieldName = f.getName();
                String jsonName = null;
                boolean explicit = false;
                boolean hasValue = false;
                Class fieldType = f.getType();

                if (Config.DEBUG_DUMP_RESPONSE) {
                    Logger.debug(TAG, "read field " + fieldName);
                }

                Response annotation = f.getAnnotation(Response.class);
                if (annotation != null) {
                    if (annotation.fieldName().length() > 0) {
                        jsonName = annotation.fieldName();
                        explicit = true;
                    }
                }

                if (jsonName == null)
                    jsonName = transformer.localToRemote(fieldName);

                /* check for field names */
                Object value = null;
                if (explicit && jsonName.contains(".")) {
                    try {
                        value = findChild(object, jsonName);
                        hasValue = true;
                    } catch (NoSuchElementException e) {
                        value = null;
                    }
                } else if (object != null) {
                    value = object.opt(jsonName);
                    hasValue = object.has(jsonName);
                }

                if (!explicit && !hasValue) {
                    /* fall back to the untransformed name */
                    if (object.has(fieldName)) {
                        value = object.opt(fieldName);
                        hasValue = true;
                    }
                }

                if (annotation != null && annotation.mandatory() && !hasValue) {
                    /* check for mandatory field */
                    throw new DeserializationException(new MissingFieldException(f.getName()));
                }

                if (hasValue) {
                    if (object == null) {
                        f.set(o, null);
                    } else if (CodecUtils.isString(fieldType)) {
                        f.set(o, value.toString());
                    } else if (CodecUtils.isInteger(fieldType)) {
                        f.setInt(o, CodecUtils.parseInteger(fieldName, value));
                    } else if (CodecUtils.isLong(fieldType)) {
                        f.setLong(o, CodecUtils.parseLong(fieldName, value));
                    } else if (CodecUtils.isList(fieldType)) {
                        if (fieldType != List.class && fieldType != ArrayList.class) {
                            Logger.error(TAG, String.format("field '%1$s' is not ArrayList or its superclass."));
                            continue;
                        }
                        List newList = ArrayList.class.newInstance();
                        visitArray((JSONArray) value, newList, o.getClass(), f, transformer);
                        f.set(o, newList);
                    } else if (CodecUtils.isMap(fieldType)) {
                        Map newMap = (Map) fieldType.newInstance();
                        visitMap((JSONObject) value, newMap);
                        f.set(o, newMap);
                    } else if (CodecUtils.isBoolean(fieldType)) {
                        f.setBoolean(o, CodecUtils.parseBoolean(fieldName, value));
                    } else if (CodecUtils.isFloat(fieldType)) {
                        f.setFloat(o, CodecUtils.parseFloat(fieldName, value));
                    } else if (CodecUtils.isDouble(fieldType)) {
                        f.setDouble(o, CodecUtils.parseDouble(fieldName, value));
                    } else if (CodecUtils.isDateTime(fieldType)) {
                        f.set(o, CodecUtils.parseDateTime(fieldName, value));
                    } else {
                        /* or it should be a POJO... */
                        Object newObject = fieldType.newInstance();
                        visitObject((JSONObject) value, newObject, transformer, false);
                        f.set(o, newObject);
                    }
                } else {
                    if (!CodecUtils.isPrimitive(fieldType))
                        f.set(o, null);
                }
            }
        } catch (Exception e) {
            throw new DeserializationException(e);
        } catch (IncompatibleTypeException e) {
            throw new DeserializationException(e);
        }
    }

    private void visitArray(JSONArray array, List<?> output, Class body, Field field,
                            FieldNameTransformer transformer) throws DeserializationException {
        Class innerType = CodecUtils.getGenericTypeOfField(body, field.getName());

        try {
            Method add = List.class.getDeclaredMethod("add", Object.class);
            for (int i = 0; i < array.length(); ++i) {
                Object element = array.get(i);
                if (CodecUtils.isFloat(innerType) || CodecUtils.isBoolean(innerType) ||
                        CodecUtils.isInteger(innerType) || CodecUtils.isString(innerType)) {
                    add.invoke(output, element);
                } else if (CodecUtils.isList(innerType)) {
                    /* TODO implement nested list */
                } else if (CodecUtils.isMap(innerType)) {
                    /* TODO implement nested map */
                } else if (element instanceof JSONObject) {
                    Object o = innerType.newInstance();
                    visitObject((JSONObject) element, o, transformer, false);
                    add.invoke(output, o);
                }
            }
        } catch (JSONException e) {
            throw new DeserializationException(e);
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

    private void visitMap(JSONObject object, Map<?, ?> m) throws DeserializationException {
        /* TODO implement deserialization into Map<?,?> */
    }

    private Object findChild(JSONObject object, String path) throws NoSuchElementException {
        String[] frags = path.split("\\.");

        // Return one child if the key's not meant to be a path
        if (object.has(path))
            return object.opt(path);

        int i = 0;
        for (String frag : frags) {
            if (++i == frags.length) {
                // if the last object
                return object.opt(frag);
            } else {
                object = object.optJSONObject(frag);
                if (object == null)
                    break;
            }
        }

        throw new NoSuchElementException();
    }

}
