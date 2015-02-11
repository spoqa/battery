/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
 */

package com.spoqa.battery.codecs;

import com.spoqa.battery.FieldNameTranslator;
import com.spoqa.battery.RequestSerializer;

public class MultipartFormDataEncoder implements RequestSerializer {
    /* TODO: implement */

    private static final String MIME_TYPE = "multipart/form-data";
    private static final String TAG = "MultipartFormDataEncoder";

    public MultipartFormDataEncoder() {

    }

    @Override
    public byte[] serializeObject(Object o, FieldNameTranslator translator) {


        return null;
    }

    @Override
    public String serializationContentType() {
        return MIME_TYPE;
    }

    @Override
    public boolean supportsCompositeType() {
        return false;
    }
}
