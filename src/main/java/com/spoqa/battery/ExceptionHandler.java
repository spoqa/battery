/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
 */

package com.spoqa.battery;

public interface ExceptionHandler<C> {

    public boolean onException(C context, Throwable error);

}
