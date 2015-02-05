/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
 */

package com.spoqa.battery;

public interface RequestPreprocessor {
    public Throwable validateContext(Object forWhat);

    public void processHttpRequest(HttpRequest req);

}
