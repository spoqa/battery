/**
 * Copyright (c) 2014-2015 Spoqa, All Rights Reserved.
 */

package com.spoqa.battery;

public interface ExceptionHandler<C> {

    public boolean onException(C context, Throwable error);

}
