/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
 */

package com.spoqa.battery;

import com.spoqa.battery.exceptions.SerializationException;

public interface RequestSerializer {

    public byte[] serializeObject(Object o, FieldNameTranslator translator)
            throws SerializationException;

    public String serializationContentType();

    public boolean supportsCompositeType();

}
