/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
 */

package com.spoqa.battery;

import com.spoqa.battery.exceptions.ContextException;

public interface RequestPreprocessor {
    public void validateContext(Object forWhat) throws ContextException;

    public void processHttpRequest(HttpRequest req);

}
