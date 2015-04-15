package com.spoqa.battery.fields;

import com.spoqa.battery.TypeAdapter;
import com.spoqa.battery.exceptions.DeserializationException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Rfc1123DateAdapter implements TypeAdapter<Date> {

    private SimpleDateFormat mDateFormat;
    private SimpleDateFormat mDateWithMicrosecFormat;

    public Rfc1123DateAdapter() {
        mDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.getDefault());
        mDateWithMicrosecFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss.SSS Z", Locale.getDefault());
    }

    @Override
    public Class getType() {
        return Date.class;
    }

    @Override
    public Date decode(String s) throws DeserializationException {
        try {
            return mDateFormat.parse(s);
        } catch (ParseException e) {
            try {
                return mDateWithMicrosecFormat.parse(s);
            } catch (ParseException e2) {
                throw new DeserializationException(e);
            }
        }
    }

    @Override
    public String encode(Date object) {
        return mDateFormat.format(object);
    }
}
