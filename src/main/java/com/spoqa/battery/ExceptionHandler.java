/**
 * Copyright (c) 2014 Park Joon-Kyu, All Rights Reserved.
 */

package com.spoqa.battery;

public interface ExceptionHandler<C, T extends Throwable> {

    public boolean onException(C context, T error);

}
