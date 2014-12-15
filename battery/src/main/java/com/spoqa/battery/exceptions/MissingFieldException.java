/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
 */

package com.spoqa.battery.exceptions;

public class MissingFieldException extends Throwable {

    private String mFieldName;

    public MissingFieldException(String fieldName) {
        mFieldName = fieldName;
    }

    public String getFieldName() {
        return mFieldName;
    }

    public String toString() {
        return String.format("Field %1$s: missing", mFieldName);
    }

}
