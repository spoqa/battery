/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
 */

package com.spoqa.battery.android;

import android.util.Log;

import com.spoqa.battery.Logger;

public class AndroidLogger implements Logger.LoggerImpl {
    @Override
    public void debug(String tag, String msg) {
        Log.d(tag, msg);
    }

    @Override
    public void error(String tag, String msg) {
        Log.e(tag, msg);
    }

    @Override
    public void info(String tag, String msg) {
        Log.i(tag, msg);
    }

    @Override
    public void verbose(String tag, String msg) {
        Log.v(tag, msg);
    }

    @Override
    public void warn(String tag, String msg) {
        Log.w(tag, msg);
    }
}
