/**
 * Copyright (c) 2014-2015 Spoqa, All Rights Reserved.
 */

package com.spoqa.battery;

import com.spoqa.battery.exceptions.ResponseValidationException;

public interface ResponseValidator {

    public void validate(Object object) throws ResponseValidationException;

}
