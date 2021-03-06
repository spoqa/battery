/**
 * Copyright (c) 2014-2015 Spoqa, All Rights Reserved.
 */

package com.spoqa.battery.exceptions;

public class ContextException extends Throwable {
    private Throwable mWhy;

    public ContextException(Throwable why) {
        mWhy = why;
    }

    public Throwable why() {
        return mWhy;
    }
}
