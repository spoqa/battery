/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
 */

package com.spoqa.battery.android;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import com.spoqa.battery.CodecUtils;
import com.spoqa.battery.Config;
import com.spoqa.battery.RpcContext;
import com.spoqa.battery.FieldNameTranslator;
import com.spoqa.battery.HttpRequest;
import com.spoqa.battery.Logger;
import com.spoqa.battery.ObjectBuilder;
import com.spoqa.battery.OnResponse;
import com.spoqa.battery.RequestFactory;
import com.spoqa.battery.annotations.RpcObject;
import com.spoqa.battery.exceptions.ContextException;
import com.spoqa.battery.exceptions.DeserializationException;
import com.spoqa.battery.exceptions.ResponseValidationException;
import com.spoqa.battery.exceptions.RpcException;
import com.spoqa.battery.exceptions.SerializationException;

import java.io.UnsupportedEncodingException;

import rx.Observable;
import rx.subjects.PublishSubject;

public class AndroidRpcContext extends RpcContext<Context> {

    private static final String TAG = "AndroidExecutionContext";

    private RequestQueue mRequestQueue;
    private Context mAndroidContext;

    public AndroidRpcContext(Context androidApplicationContext, RequestQueue requestQueue) {
        super();
        mAndroidContext = androidApplicationContext;
        mRequestQueue = requestQueue;

        /* set up android logger */
        Logger.registerLogger(new AndroidLogger());
    }

    public AndroidRpcContext(Context androidApplicationContext) {
        this(androidApplicationContext,
                Volley.newRequestQueue(androidApplicationContext, new OkHttpStack()));
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public Context androidApplicationContext() {
        return mAndroidContext;
    }

    public <T> void invokeAsync(final T rpcObject, final OnResponse<T> onResponse) {
        invokeAsync(rpcObject, onResponse, mAndroidContext);
    }

    public <T> void invokeAsync(final T rpcObject, final OnResponse<T> onResponse, final Context currentContext) {
        HttpRequest request = null;
        try {
            request = RequestFactory.createRequest(this, rpcObject);
        } catch (SerializationException e) {
            onResponse.onFailure(e);
            return;
        } catch (ContextException e) {
            onResponse.onFailure(e.why());
            return;
        }

        if (request == null) {
            Logger.error(TAG, "Could not make call due to error(s) while creating request object.");
            return;
        }

        final RpcObject rpcObjectDecl = rpcObject.getClass().getAnnotation(RpcObject.class);
        if (rpcObjectDecl.context() != RpcObject.NULL.class) {
            Class<?> contextSpec = rpcObjectDecl.context();
            if (!CodecUtils.isSubclassOf(contextSpec, RpcContext.class)) {
                Logger.error(TAG, String.format("Context attribute of RpcObject %1$s is not a " +
                        "subclass of ExecutionContext", rpcObject.getClass().getName()));
                return;
            }
            if (getClass() != contextSpec) {
                Logger.error(TAG, String.format("RpcObject context mismatch. context: %1$s, " +
                        "expected: %2$s", getClass().getName(), contextSpec.getName()));
                return;
            }
        }

        final FieldNameTranslator nameTranslator = request.getFieldNameTranslator();
        Response.Listener<ResponseDelegate> onVolleyResponse = new Response.Listener<ResponseDelegate>() {
            @Override
            public void onResponse(ResponseDelegate s) {
                try {
                    /* force content type if declared by RpcObject */
                    String contentType = rpcObjectDecl.expectedContentType();
                    if (contentType == null || contentType.length() == 0)
                        contentType = s.contentType();
                    ObjectBuilder.build(AndroidRpcContext.this, contentType, s.data(),
                            rpcObject, nameTranslator);

                    if (getResponseValidator() != null) {
                        try {
                            Object responseObject = null;
                            try {
                                responseObject = CodecUtils.getResponseObject(null, rpcObject, false);
                            } catch (RpcException e) {
                                e.printStackTrace();
                            }
                            if (responseObject == null)
                                responseObject = rpcObject;
                            getResponseValidator().validate(responseObject);
                        } catch (ResponseValidationException e) {
                            if (!dispatchErrorHandler(currentContext, e)) {
                                onResponse.onFailure(e);
                                return;
                            }
                        }
                    }

                    onResponse.onResponse(rpcObject);
                } catch (DeserializationException e) {
                    if (!dispatchErrorHandler(currentContext, e))
                        onResponse.onFailure(e);
                }
            }
        };

        Response.ErrorListener onVolleyErrorResponse = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Logger.error(TAG, "Error while RPC call: " + volleyError.getMessage());
                if (Config.DEBUG_DUMP_RESPONSE) {
                    try {
                        if (volleyError.networkResponse != null)
                            Logger.error(TAG, "Error response: " +
                                    new String(volleyError.networkResponse.data, "utf-8"));
                    } catch (UnsupportedEncodingException e) {}
                }

                Throwable e = volleyError.getCause();
                if (e == null) {
                    if (volleyError instanceof ServerError) {
                        e = new RpcException("Server Error");
                    } else {
                        e = new RpcException(volleyError.toString());
                    }
                }
                if (!dispatchErrorHandler(currentContext, volleyError) &&
                        !dispatchErrorHandler(currentContext, e))
                    onResponse.onFailure(volleyError);
            }
        };

        VolleyRequest req = new VolleyRequest(request, onVolleyResponse, onVolleyErrorResponse);
        mRequestQueue.add(req);
    }

    public <T> Observable<T> invokeObservable(final T rpcObject) {
        return invokeObservable(rpcObject, mAndroidContext);
    }

    public <T> Observable<T> invokeObservable(final T rpcObject, Context currentContext) {
        final PublishSubject<T> subject = PublishSubject.create();

        invokeAsync(rpcObject, new OnResponse<T>() {
            @Override
            public void onResponse(T responseBody) {
                subject.onNext(responseBody);
                subject.onCompleted();
            }

            @Override
            public void onFailure(Throwable why) {
                subject.onError(why);
            }
        }, currentContext);

        return subject.asObservable();
    }

}
