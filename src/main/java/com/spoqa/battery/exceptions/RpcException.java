/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
 */

package com.spoqa.battery.exceptions;

public class RpcException extends Throwable {

    private String mMessage;

    public RpcException(String message) {
        mMessage = message;
    }

    public String getMessage() {
        return mMessage;
    }

    @Override
    public String toString() {
        return String.format("RpcException: %1$s", mMessage);
    }

}
