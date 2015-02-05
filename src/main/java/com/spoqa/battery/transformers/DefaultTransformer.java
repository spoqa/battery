/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
 */

package com.spoqa.battery.transformers;

import com.spoqa.battery.FieldNameTransformer;

public class DefaultTransformer implements FieldNameTransformer {

    private static final String TAG = "DefaultTransformer";

    public DefaultTransformer() {

    }

    @Override
    public String localToRemote(String key) {
        return key; /* do not transform */
    }

    @Override
    public String remoteToLocal(String key) {
        return key; /* do not transform */
    }
}
