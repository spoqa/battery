/**
 * Copyright (c) 2014-2015 Spoqa, All Rights Reserved.
 */

package com.spoqa.battery;

import java.util.List;

public interface FieldNameTransformer {

    public List<String> decode(String input);
    public String encode(List<String> parts);

}
