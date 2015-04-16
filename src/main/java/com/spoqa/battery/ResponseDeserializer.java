/**
 * Copyright (c) 2014-2015 Spoqa, All Rights Reserved.
 */

package com.spoqa.battery;

import com.spoqa.battery.exceptions.DeserializationException;

public interface ResponseDeserializer {

    public Object parseInput(String input) throws DeserializationException;

    public boolean containsChild(Object internalObject, String key);

    public Object queryObjectChild(Object internalObject, String key);

    public Iterable<Object> queryArrayChildren(Object internalArray);

    public boolean isObject(Class<?> internalClass);

    public boolean isArray(Class<?> internalClass);

    public String deserializationContentType();

}
