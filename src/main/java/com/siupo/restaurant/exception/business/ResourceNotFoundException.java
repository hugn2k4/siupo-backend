package com.siupo.restaurant.exception.business;

import com.siupo.restaurant.exception.base.BaseException;
import com.siupo.restaurant.exception.base.ErrorCode;

// 404 Resource Not Found Exception
public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
