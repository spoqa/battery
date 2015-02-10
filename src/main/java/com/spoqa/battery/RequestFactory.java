/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
 */

package com.spoqa.battery;

import com.spoqa.battery.annotations.QueryString;
import com.spoqa.battery.annotations.RpcObject;
import com.spoqa.battery.exceptions.ContextException;
import com.spoqa.battery.exceptions.SerializationException;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RequestFactory {
    private static final String TAG = "RequestFactory";

    public static HttpRequest createRequest(ExecutionContext context, Object object)
            throws SerializationException, ContextException {
        /* validate current preprocessor context (if exists) */
        if (context.getRequestPreprocessor() != null) {
            Throwable error = context.getRequestPreprocessor().validateContext(object);
            if (error != null)
                throw new ContextException(error);
        }

        RpcObject annotation = object.getClass().getAnnotation(RpcObject.class);
        if (annotation == null) {
            Logger.error(TAG, String.format("Attempted to create a request from non-RpcObject"));
            return null;
        }

        FieldNameTransformer transformer;
        try {
            transformer = (FieldNameTransformer) annotation.nameTransformer().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            Logger.error(TAG, "Failed to create request.");
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Logger.error(TAG, "Failed to create request.");
            return null;
        }

        Map<String, Object> parameters = new HashMap<String, Object>();
        int method = annotation.method();
        String uri = buildUri(context, object, annotation, parameters, transformer);
        if (uri == null) {
            Logger.error(TAG, "Failed to create request.");
            return null;
        }

        HttpRequest request = new HttpRequest(method, uri);
        request.setNameTransformer(transformer);
        request.putParameters(parameters);
        request.setRequestObject(object);

        /* set request body */
        Class serializerCls = annotation.requestSerializer();
        if ((method == HttpRequest.Methods.POST || method == HttpRequest.Methods.PUT) &&
                serializerCls != RpcObject.NULL.class) {
            if (serializerCls != null) {
                try {
                    RequestSerializer serializer = (RequestSerializer) serializerCls.newInstance();
                    request.putHeader(HttpRequest.HEADER_CONTENT_TYPE, serializer.serializationContentType());
                    request.setRequestBody(serializer.serializeObject(object, transformer));
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        /* apply custom request processing middleware */
        if (context.getRequestPreprocessor() != null)
            context.getRequestPreprocessor().processHttpRequest(request);

        return request;
    }

    private static String buildUri(ExecutionContext context, Object object, RpcObject rpcObjectDecl,
                                   Map<String, Object> params, FieldNameTransformer transformer) {
        String uri = rpcObjectDecl.uri();
        if (!uri.startsWith("http://") && !uri.startsWith("https://")) {
            if (context.getDefaultUriPrefix() == null) {
                Logger.error(TAG, String.format("No URI prefix given."));
                return null;
            }
            if (uri.startsWith("/"))
                uri = uri.substring(1);
            uri = context.getDefaultUriPrefix() + "/" + uri;
        }

        /* Build REST URI fragment */
        if (rpcObjectDecl.uriParams() != null) {
            Class self = object.getClass();
            String[] restParams = rpcObjectDecl.uriParams();
            Object[] parameters = new Object[restParams.length];
            for (int i = 0; i < restParams.length; ++i) {
                String paramName = restParams[i];
                try {
                    Field field = self.getField(paramName);
                    Object fieldObject = field.get(object);
                    if (fieldObject == null ||
                            (!CodecUtils.isPrimitive(fieldObject.getClass()) &&
                             !CodecUtils.isString(fieldObject))) {
                        Logger.error(TAG, String.format("Type '%1$s' of field '%2$s' could not be built into URI.",
                                fieldObject.getClass().getName(), paramName));
                        return null;
                    }

                    if (fieldObject instanceof String) {
                        try {
                            parameters[i] = URLEncoder.encode((String) fieldObject, "utf-8");
                        } catch (UnsupportedEncodingException e) {}
                    } else {
                        parameters[i] = fieldObject;
                    }
                } catch (NoSuchFieldException e) {
                    Logger.error(TAG, String.format("No such field '%1$s' in class %2$s", paramName, self.getName()));
                    return null;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            uri = String.format(uri, parameters);
        }

        /* append query string */
        List<Field> queryStringParams = CodecUtils.getAnnotatedFields(QueryString.class, object.getClass());
        for (Field field : queryStringParams) {
            String fieldName = field.getName();
            Class fieldType = field.getType();

            if (!CodecUtils.isString(fieldType) &&
                    !CodecUtils.isInteger(fieldType) &&
                    !CodecUtils.isBoolean(fieldType) &&
                    !CodecUtils.isFloat(fieldType) &&
                    !CodecUtils.isDouble(fieldType) &&
                    !CodecUtils.isLong(fieldType)) {
                Logger.error(TAG, String.format("Type '%1$s' of field '%2$s' could not be built into URI.",
                        field.getClass().getName(), field.getName()));
                continue;
            }

            /* override field name if optional value is supplied */
            QueryString annotation = field.getAnnotation(QueryString.class);
            if (annotation.fieldName().length() > 0)
                fieldName = annotation.fieldName();
            else
                fieldName = transformer.localToRemote(fieldName);

            try {
                params.put(fieldName, field.get(object));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return uri;
    }
}
