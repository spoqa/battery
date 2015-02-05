/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
 */

package com.spoqa.battery.transformers;

import com.spoqa.battery.FieldNameTransformer;

public class UnderscoreNameTransformer implements FieldNameTransformer {

    private static final String TAG = "UnderscoreNameTransformer";

    public UnderscoreNameTransformer() {

    }

    @Override
    public String remoteToLocal(String input) {
        StringBuilder sb = new StringBuilder();
        boolean underscoreFlag = false;
        for (char c : input.toCharArray()) {
            if (c == '_') {
                underscoreFlag = true;
            } else {
                if (underscoreFlag) {
                    sb.append(Character.toUpperCase(c));
                    underscoreFlag = false;
                } else {
                    sb.append(c);
                }
            }
        }

        return sb.toString();
    }

    @Override
    public String localToRemote(String input) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (char c : input.toCharArray()) {
            if (Character.isUpperCase(c)) {
                if (i > 0)
                    sb.append('_');
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
            ++i;
        }
        return sb.toString();
    }
}
