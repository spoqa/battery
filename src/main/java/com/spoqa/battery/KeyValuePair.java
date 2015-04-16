/**
 * Copyright (c) 2014-2015 Spoqa, All Rights Reserved.
 */

package com.spoqa.battery;

public class KeyValuePair<KeyType, ValueType> {
    public KeyType key;
    public ValueType value;

    public KeyValuePair(KeyType key, ValueType value) {
        this.key = key;
        this.value = value;
    }
}
