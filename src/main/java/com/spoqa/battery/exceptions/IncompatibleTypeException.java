/**
 * Copyright (c) 2014-2015 Spoqa, All Rights Reserved.
 */

package com.spoqa.battery.exceptions;

public class IncompatibleTypeException extends Throwable {
    private String mFieldName;
    private String mExpectedType;
    private String mValue;

    public IncompatibleTypeException(String fieldName, String expectedType, String value) {
        mFieldName = fieldName;
        mExpectedType = expectedType;
        mValue = value;
    }

    @Override
    public String toString() {
        return String.format("field %1$s: %2$s expected, value is %3$s.", mFieldName, mExpectedType,
                mValue);
    }
}
