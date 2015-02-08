package com.spoqa.battery;

import com.spoqa.battery.exceptions.DeserializationException;
import com.spoqa.battery.exceptions.SerializationException;

public interface FieldCodec<T> {

    public Class getType();
    public T decode(String s) throws DeserializationException;
    public String encode(T object) throws SerializationException;

}
