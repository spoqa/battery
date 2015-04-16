/**
 * Copyright (c) 2014-2015 Spoqa, All Rights Reserved.
 */

package com.spoqa.battery.transformers;

import com.spoqa.battery.FieldNameTransformer;
import com.spoqa.battery.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class PascalCaseTransformer implements FieldNameTransformer {

    @Override
    public List<String> decode(String input) {
        return StringUtils.splitByCase(input);
    }

    @Override
    public String encode(List<String> parts) {
        if (parts.size() == 0)
            return "";

        List<String> output = new ArrayList<String>();

        for (int i = 0; i < parts.size(); ++i) {
            output.add(StringUtils.uppercaseFirst(parts.get(i)));
        }

        return StringUtils.join(output, "");
    }
}
