package com.spoqa.battery.fields;

import com.spoqa.battery.TypeAdapter;
import com.spoqa.battery.exceptions.DeserializationException;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

public class TimestampDateAdapter implements TypeAdapter<Date> {

    private boolean mMsec;

    public TimestampDateAdapter(boolean msec) {
        mMsec = msec;
    }

    @Override
    public Class getType() {
        return Date.class;
    }

    @Override
    public Date decode(String s) throws DeserializationException {
        long l = Long.parseLong(s);
        if (!mMsec)
            l *= 1000;

        Timestamp timestamp = new Timestamp(l);
        return new Date(timestamp.getTime());
    }

    @Override
    public String encode(Date object) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(object);

        if (mMsec)
            return Long.toString(calendar.getTimeInMillis());
        else
            return Long.toString(calendar.getTimeInMillis() / 1000);
    }
}
