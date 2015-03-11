/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
 */

package com.spoqa.battery;

import java.util.HashMap;
import java.util.Map;

public class ExecutionContext<C> {

    static final private String TAG = "ExecutionContext";

    private String mDefaultUriPrefix;
    private RequestPreprocessor mRequestPreprocessor;
    private ResponseValidator mResponseValidator;
    private RequestSerializer mRequestSerializer;
    private Map<Class<? extends Throwable>, ExceptionCallback<C, ? extends Throwable>> mExceptionCallbacks;
    private Map<Class<?>, FieldCodec> mFieldCodecs;

    public ExecutionContext() {
        mExceptionCallbacks = new HashMap<Class<? extends Throwable>,
                ExceptionCallback<C, ? extends Throwable>>();
        mFieldCodecs = new HashMap<Class<?>, FieldCodec>();
    }

    public String getDefaultUriPrefix() {
        return mDefaultUriPrefix;
    }

    public RequestPreprocessor getRequestPreprocessor() {
        return mRequestPreprocessor;
    }

    public ResponseValidator getResponseValidator() {
        return mResponseValidator;
    }

    public RequestSerializer getRequestSerializer() {
        return mRequestSerializer;
    }

    public void setDefaultUriPrefix(String prefix) {
        if (prefix.startsWith("http://") || prefix.startsWith("https://"))
            mDefaultUriPrefix = prefix;

        // strip out trailing slash
        if (mDefaultUriPrefix.endsWith("/"))
            mDefaultUriPrefix = mDefaultUriPrefix.substring(0, mDefaultUriPrefix.length() - 1);
    }

    public void setRequestPreprocessor(RequestPreprocessor preprocessor) {
        mRequestPreprocessor = preprocessor;
    }

    public void setResponseValidator(ResponseValidator validator) {
        mResponseValidator = validator;
    }

    public void setRequestSerializer(RequestSerializer serializer) {
        mRequestSerializer = serializer;
    }

    public <T extends Throwable> void registerExceptionCallback(Class<T> clazz, ExceptionCallback<C, T> handler) {
        mExceptionCallbacks.put(clazz, handler);
    }

    public void registerFieldCodec(FieldCodec codec) {
        mFieldCodecs.put(codec.getType(), codec);
    }

    public <T extends Throwable> boolean dispatchErrorHandler(C frontendContext, T ex) {
        Class<T> clazz = (Class<T>) ex.getClass();

        if (Config.DEBUG_DUMP_RESPONSE) {
            Logger.debug(TAG, "got exception: " + clazz.getName());
        }

        while (clazz != null) {
            if (mExceptionCallbacks.containsKey(clazz)) {
                if (Config.DEBUG_DUMP_RESPONSE) {
                    Logger.debug(TAG, "   handling: " + clazz.getName());
                }

                ExceptionCallback<C, T> callback = (ExceptionCallback<C, T>) mExceptionCallbacks.get(clazz);
                boolean ret = callback.onException(frontendContext, ex);

                if (ret)
                    return true;
            }

            /* find for superclass */
            Class<?> super_ = clazz.getSuperclass();
            if (!Throwable.class.isAssignableFrom(super_))
                break;

            clazz = (Class<T>) super_;
        }

        return false;
    }

    public FieldCodec<?> queryFieldCodec(Class<?> type) {
        if (mFieldCodecs.containsKey(type))
            return mFieldCodecs.get(type);

        return null;
    }

    public boolean containsFieldCodec(Class<?> type) {
        return mFieldCodecs.containsKey(type);
    }

}
