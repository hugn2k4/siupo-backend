package com.siupo.restaurant.exception.business;

import com.siupo.restaurant.exception.base.BaseException;
import com.siupo.restaurant.exception.base.ErrorCode;

// 401 Unauthorized
public class UnauthorizedException extends BaseException {
    public UnauthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
