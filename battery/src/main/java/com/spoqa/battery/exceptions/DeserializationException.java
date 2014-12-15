/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
 */

package com.spoqa.battery.exceptions;

public class DeserializationException extends Throwable {

    private Throwable mForWhat;

    public DeserializationException() {

    }

    public DeserializationException(Throwable forWhat) {
        mForWhat = forWhat;
    }

    public Throwable forWhat() {
        return mForWhat;
    }

    @Override
    public String toString() {
        return String.format("Error while deserializing: %1$s", mForWhat.toString());
    }

}
