package com.spoqa.battery;

import com.spoqa.battery.exceptions.DeserializationException;

public interface ResponseDeserializer {

    public void deserializeObject(String input, Object object, FieldNameTransformer transformer)
            throws DeserializationException;

    public String deserializationContentType();

}
