package com.spoqa.battery;

import java.util.HashMap;
import java.util.Map;

public class TypeAdapterCollection {

    private Map<Class<?>, TypeAdapter> mTypeAdapters;

    public TypeAdapterCollection() {
        mTypeAdapters = new HashMap<Class<?>, TypeAdapter>();
    }

    public void register(TypeAdapter adapter) {
        mTypeAdapters.put(adapter.getType(), adapter);
    }

    public boolean contains(Class<?> clazz) {
        return mTypeAdapters.containsKey(clazz);
    }

    public TypeAdapter query(Class<?> clazz) {
        if (mTypeAdapters.containsKey(clazz))
            return mTypeAdapters.get(clazz);

        return null;
    }

}
