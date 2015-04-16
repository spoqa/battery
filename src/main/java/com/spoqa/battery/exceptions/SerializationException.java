/**
 * Copyright (c) 2014-2015 Spoqa, All Rights Reserved.
 */

package com.spoqa.battery.exceptions;

public class SerializationException extends Throwable {

    private Throwable mForWhat;

    public SerializationException() {

    }

    public SerializationException(Throwable forWhat) {
        mForWhat = forWhat;
    }

    public Throwable forWhat() {
        return mForWhat;
    }

    @Override
    public String toString() {
        return String.format("Error while serializing: %1$s", mForWhat.toString());
    }

}
