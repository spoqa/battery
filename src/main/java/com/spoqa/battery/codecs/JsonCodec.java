/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
 */

package com.spoqa.battery.codecs;

import com.spoqa.battery.CodecUtils;
import com.spoqa.battery.FieldNameTranslator;
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
import java.util.Iterator;

public class JsonCodec implements RequestSerializer, ResponseDeserializer {
    public static final String MIME_TYPE = "application/json";

    public JsonCodec() {

    }

    /* serializer */

    @Override
    public byte[] serializeObject(Object o, FieldNameTranslator translator)
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

    @Override
    public boolean supportsCompositeType() {
        return true;
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

    private JSONArray visitArray(Iterable<Object> a) throws SerializationException {
        return null;
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
        return jsonObject.opt(key);
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
                        return jsonArray.opt(index++);
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
