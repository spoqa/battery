/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
 */

package com.spoqa.battery;

public interface FieldNameTransformer {

    public String localToRemote(String key);
    public String remoteToLocal(String key);

}
