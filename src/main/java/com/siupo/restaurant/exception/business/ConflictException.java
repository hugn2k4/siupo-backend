package com.siupo.restaurant.exception.business;

import com.siupo.restaurant.exception.base.BaseException;
import com.siupo.restaurant.exception.base.ErrorCode;

// 409 Conflict
public class ConflictException extends BaseException {
    public ConflictException(ErrorCode errorCode) {
        super(errorCode);
    }
}
