/**
 * Copyright (c) 2014-2015 Spoqa, All Rights Reserved.
 */

package com.spoqa.battery.codecs;

import com.spoqa.battery.CodecUtils;
import com.spoqa.battery.FieldNameTranslator;
import com.spoqa.battery.Logger;
import com.spoqa.battery.RequestSerializer;
import com.spoqa.battery.ResponseDeserializer;
import com.spoqa.battery.TypeAdapterCollection;
import com.spoqa.battery.annotations.RequestBody;
import com.spoqa.battery.annotations.RequestObject;
import com.spoqa.battery.exceptions.DeserializationException;
import com.spoqa.battery.exceptions.SerializationException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

public class JsonCodec implements RequestSerializer, ResponseDeserializer {
    private static final String TAG = "JsonCodec";

    public static final String MIME_TYPE = "application/json";

    public JsonCodec() {

    }

    /* serializer */

    @Override
    public byte[] serializeObject(Object o, FieldNameTranslator translator,
                                  TypeAdapterCollection typeAdapters)
            throws SerializationException {
        List<Field> fields = CodecUtils.getAnnotatedFields(null, RequestObject.class, o.getClass());
        List<Method> getters = CodecUtils.getAnnotatedGetterMethods(null, RequestObject.class, o.getClass());
        int count = fields.size() + getters.size();
        boolean filterAnnotated = true;

        if (count > 1) {
            Logger.error(TAG, String.format("Object %1$s has more than one @RequestObject fields.", o.getClass().getName()));
        } else if (count == 1) {
            try {
                if (fields.size() == 1)
                    o = fields.get(0).get(o);
                else if (getters.size() == 1)
                    o = getters.get(0).invoke(o);
                filterAnnotated = false;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        JSONObject body = visitObject(o, translator, typeAdapters, filterAnnotated);

        if (body != null) {
            try {
                return body.toString().getBytes("utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return "null".getBytes();
    }

    @Override
    public String serializationContentType() {
        return MIME_TYPE;
    }

    @Override
    public boolean supportsCompositeType() {
        return true;
    }

    private JSONObject visitObject(Object o, FieldNameTranslator translator,
                                   TypeAdapterCollection typeAdapters, boolean filterAnnotated) throws SerializationException {
        Iterable<Field> fields;
        Iterable<Method> getters;
        if (filterAnnotated) {
            fields = CodecUtils.getAnnotatedFields(null, RequestBody.class, o.getClass());
            getters = CodecUtils.getAnnotatedGetterMethods(null, RequestBody.class, o.getClass());
        } else {
            fields = CodecUtils.getAllFields(null, o.getClass());
            getters = CodecUtils.getAllGetterMethods(null, o.getClass());
        }

        JSONObject body = new JSONObject();

        for (Field f : fields) {
            RequestBody annotation = f.getAnnotation(RequestBody.class);
            Class type = f.getType();
            String localName = f.getName();
            String foreignName;

            if (localName.equals("serialVersionUID")) {
                continue;
            }

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
                else if (CodecUtils.isList(type))
                    body.put(foreignName, visitArray((List<Object>) element, translator, typeAdapters));
                else if (type.isEnum())
                    body.put(foreignName, element.toString());
                else if (typeAdapters.contains(element.getClass()))
                    body.put(foreignName, typeAdapters.query(element.getClass()).encode(element));
                else
                    body.put(foreignName, visitObject(element, translator, typeAdapters, false));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }

        for (Method m : getters) {
            RequestBody annotation = m.getAnnotation(RequestBody.class);
            Class type = m.getReturnType();
            String localName = CodecUtils.normalizeGetterName(m.getName());
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
                Object element = m.invoke(o);

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
                else if (CodecUtils.isList(type))
                    body.put(foreignName, visitArray((List<Object>) element, translator, typeAdapters));
                else if (type.isEnum())
                    body.put(foreignName, element.toString());
                else if (typeAdapters.contains(element.getClass()))
                    body.put(foreignName, typeAdapters.query(element.getClass()).encode(element));
                else
                    body.put(foreignName, visitObject(element, translator, typeAdapters, false));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                continue;
            }
        }

        if (body.length() > 0)
            return body;
        else
            return null;
    }

    private JSONArray visitArray(Iterable<Object> a, FieldNameTranslator translator,
                                 TypeAdapterCollection typeAdapters) throws SerializationException {
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
            else if (CodecUtils.isList(type))
                array.put(visitArray((List<Object>) element, translator, typeAdapters));
            else if (type.isEnum())
                array.put(element.toString());
            else if (typeAdapters.contains(element.getClass()))
                array.put(typeAdapters.query(element.getClass()).encode(element));
            else
                array.put(visitObject(element, translator, typeAdapters, false));
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
