/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
 */

package com.spoqa.battery.android;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;

import java.io.UnsupportedEncodingException;

import com.spoqa.battery.Config;
import com.spoqa.battery.DeserializerFactory;
import com.spoqa.battery.FieldNameTransformer;
import com.spoqa.battery.HttpRequest;
import com.spoqa.battery.Logger;
import com.spoqa.battery.OnResponse;
import com.spoqa.battery.RequestFactory;
import com.spoqa.battery.exceptions.ContextException;
import com.spoqa.battery.exceptions.DeserializationException;
import com.spoqa.battery.exceptions.ResponseValidationException;
import com.spoqa.battery.exceptions.RpcException;
import com.spoqa.battery.exceptions.SerializationException;

final public class Rpc {

    private static final String TAG = "Rpc";

    public static <T> void invokeAsync(
            final Context context, final AndroidExecutionContext rpcContext,
            final T rpcObject, final OnResponse<T> onResponse) {
        HttpRequest request = null;
        try {
            request = RequestFactory.createRequest(rpcContext, rpcObject);
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

        final FieldNameTransformer nameTransformer = request.getNameTransformer();
        Response.Listener<ResponseDelegate> onVolleyResponse = new Response.Listener<ResponseDelegate>() {
            @Override
            public void onResponse(ResponseDelegate s) {
                try {
                    DeserializerFactory.deserialize(s.contentType(), s.data(), rpcObject, nameTransformer);

                    if (rpcContext.getResponseValidator() != null) {
                        try {
                            rpcContext.getResponseValidator().validate(rpcObject);
                        } catch (ResponseValidationException e) {
                            if (!rpcContext.dispatchErrorHandler(context, e)) {
                                onResponse.onFailure(e);
                                return;
                            }
                        }
                    }

                    onResponse.onResponse(rpcObject);
                } catch (DeserializationException e) {
                    if (!rpcContext.dispatchErrorHandler(context, e))
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
                        if (volleyError != null && volleyError.networkResponse != null)
                            Logger.error(TAG, "Error response: " + new String(volleyError.networkResponse.data, "utf-8"));
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
                if (!rpcContext.dispatchErrorHandler(context, volleyError) &&
                        !rpcContext.dispatchErrorHandler(context, e))
                    onResponse.onFailure(volleyError);
            }
        };

        VolleyRequest req = new VolleyRequest(request, onVolleyResponse, onVolleyErrorResponse);
        rpcContext.requestQueue().add(req);
    }

}
