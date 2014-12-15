/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
 */

package com.spoqa.battery.android;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

import com.spoqa.battery.ExecutionContext;

public class AndroidExecutionContext extends ExecutionContext<Context> {

    private RequestQueue mRequestQueue;

    public AndroidExecutionContext(Context androidApplicationContext) {
        super();
        HurlStack stack = new OkHttpStack();
        mRequestQueue = Volley.newRequestQueue(androidApplicationContext, stack);
    }

    public RequestQueue requestQueue() {
        return mRequestQueue;
    }

}
