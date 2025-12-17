package com.siupo.restaurant.exception.business;

import com.siupo.restaurant.exception.base.BaseException;
import com.siupo.restaurant.exception.base.ErrorCode;

// 401 InvalidCredentialsException
public class InvalidCredentialsException extends BaseException {
    public InvalidCredentialsException(ErrorCode errorCode) {
        super(errorCode);
    }
}
