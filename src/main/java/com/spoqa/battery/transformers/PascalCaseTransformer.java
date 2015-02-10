/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
 */

package com.spoqa.battery.transformers;

import com.spoqa.battery.FieldNameTransformer;

public class PascalCaseTransformer implements FieldNameTransformer {

    @Override
    public String remoteToLocal(String input) {
        String first = input.substring(0, 1);
        String rest = input.substring(1);

        return first.toLowerCase() + rest;
    }

    @Override
    public String localToRemote(String input) {
        String first = input.substring(0, 1);
        String rest = input.substring(1);

        return first.toUpperCase() + rest;
    }
}
