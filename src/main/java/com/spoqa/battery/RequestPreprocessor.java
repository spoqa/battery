/**
 * Copyright (c) 2014-2015 Spoqa, All Rights Reserved.
 */

package com.spoqa.battery;

import com.spoqa.battery.exceptions.ContextException;

public interface RequestPreprocessor {
    public void validateContext(Object forWhat) throws ContextException;

    public void processHttpRequest(HttpRequest req);

}
