/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
 */

package com.spoqa.battery.android;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

import com.spoqa.battery.ExecutionContext;
import com.spoqa.battery.Logger;

public class AndroidExecutionContext extends ExecutionContext<Context> {

    private RequestQueue mRequestQueue;
    private Context mAndroidContext;

    public AndroidExecutionContext(Context androidApplicationContext) {
        super();
        mAndroidContext = androidApplicationContext;
        HurlStack stack = new OkHttpStack();
        mRequestQueue = Volley.newRequestQueue(androidApplicationContext, stack);

        /* set up android logger */
        Logger.registerLogger(new AndroidLogger());
    }

    public RequestQueue requestQueue() {
        return mRequestQueue;
    }

    public Context androidApplicationContext() {
        return mAndroidContext;
    }

}
