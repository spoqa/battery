/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
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
