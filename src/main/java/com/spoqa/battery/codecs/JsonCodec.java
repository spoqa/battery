/**
 * Copyright (c) 2014-2015 Spoqa, All Rights Reserved.
 */

package com.spoqa.battery.codecs;

import com.spoqa.battery.CodecUtils;
import com.spoqa.battery.FieldNameTranslator;
import com.spoqa.battery.Logger;
import com.spoqa.battery.RequestSerializer;
import com.spoqa.battery.ResponseDeserializer;
import com.spoqa.battery.annotations.RequestBody;
import com.spoqa.battery.exceptions.DeserializationException;
import com.spoqa.battery.exceptions.SerializationException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;

public class JsonCodec implements RequestSerializer, ResponseDeserializer {
    private static final String TAG = "JsonCodec";

    public static final String MIME_TYPE = "application/json";

    public JsonCodec() {

    }

    /* serializer */

    @Override
    public byte[] serializeObject(Object o, FieldNameTranslator translator)
            throws SerializationException {
        JSONObject body = visitObject(o, translator);

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

    @Override
    public boolean supportsCompositeType() {
        return true;
    }

    private JSONObject visitObject(Object o, FieldNameTranslator translator) throws SerializationException {
        Iterable<Field> fields = CodecUtils.getAnnotatedFields(null, RequestBody.class, o.getClass());

        JSONObject body = new JSONObject();

        for (Field f : fields) {
            RequestBody annotation = f.getAnnotation(RequestBody.class);
            Class type = f.getType();
            String localName = f.getName();
            String foreignName;
            if (annotation != null && annotation.value().length() > 0) {
                foreignName = annotation.value();
            } else {
                if (translator != null)
                    foreignName = translator.localToRemote(localName);
                else
                    foreignName = localName;
            }

            try {
                Object element = f.get(o);

                if (element == null)
                    body.put(foreignName, null);
                else if (CodecUtils.isString(type))
                    body.put(foreignName, (String) element);
                else if (CodecUtils.isFloat(type))
                    body.put(foreignName, (Float) element);
                else if (CodecUtils.isDouble(type))
                    body.put(foreignName, (Double) element);
                else if (CodecUtils.isBoolean(type))
                    body.put(foreignName, (Boolean) element);
                else if (CodecUtils.isInteger(type))
                    body.put(foreignName, (Integer) element);
                else if (CodecUtils.isLong(type))
                    body.put(foreignName, (Long) element);
                else if (CodecUtils.isSubclassOf(element.getClass(), List.class))
                    body.put(foreignName, visitArray((List<Object>) element, translator));
                else if (element.getClass().isEnum())
                    body.put(foreignName, element.toString());
                else
                    body.put(foreignName, visitObject(element, translator));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }

        if (body.length() > 0)
            return body;
        else
            return null;
    }

    private JSONArray visitArray(Iterable<Object> a, FieldNameTranslator translator) throws SerializationException {
        JSONArray array = new JSONArray();

        for (Object element : a) {
            Class type = element.getClass();
            if (CodecUtils.isString(type))
                array.put((String) element);
            else if (CodecUtils.isFloat(type))
                array.put((Float) element);
            else if (CodecUtils.isDouble(type))
                array.put((Double) element);
            else if (CodecUtils.isBoolean(type))
                array.put((Boolean) element);
            else if (CodecUtils.isInteger(type))
                array.put((Integer) element);
            else if (CodecUtils.isLong(type))
                array.put((Long) element);
            else if (CodecUtils.isSubclassOf(element.getClass(), List.class))
                array.put(visitArray((List<Object>) element, translator));
            else if (element.getClass().isEnum())
                array.put(element.toString());
            else
                array.put(visitObject(element, translator));
        }

        return array;
    }

    /* deserializer */

    @Override
    public Object parseInput(String input) throws DeserializationException {
        try {
            return new JSONObject(input);
        } catch (JSONException e) {
            throw new DeserializationException(e);
        }
    }

    @Override
    public boolean containsChild(Object internalObject, String key) {
        assert internalObject instanceof JSONObject;

        final JSONObject jsonObject = (JSONObject) internalObject;
        return jsonObject.has(key);
    }

    @Override
    public Object queryObjectChild(Object internalObject, String key) {
        assert internalObject instanceof JSONObject;

        final JSONObject jsonObject = (JSONObject) internalObject;

        Object o = jsonObject.opt(key);
        if (o == JSONObject.NULL)
            return null;

        return o;
    }

    @Override
    public Iterable<Object> queryArrayChildren(Object internalArray) {
        assert internalArray instanceof JSONArray;

        final JSONArray jsonArray = (JSONArray) internalArray;

        return new Iterable<Object>() {
            private int index = 0;

            @Override
            public Iterator<Object> iterator() {
                return new Iterator<Object>() {
                    @Override
                    public boolean hasNext() {
                        return index < jsonArray.length();
                    }

                    @Override
                    public Object next() {
                        Object o = jsonArray.opt(index++);

                        if (o == JSONObject.NULL)
                            return null;

                        return o;
                    }

                    @Override
                    public void remove() {
                        /* do not implement */
                    }
                };
            }
        };
    }

    @Override
    public boolean isObject(Class<?> internalClass) {
        return internalClass == JSONObject.class;
    }

    @Override
    public boolean isArray(Class<?> internalClass) {
        return internalClass == JSONArray.class;
    }

    @Override
    public String deserializationContentType() {
        return MIME_TYPE;
    }

}
