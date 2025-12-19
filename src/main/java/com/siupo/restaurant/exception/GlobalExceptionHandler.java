package com.siupo.restaurant.exception;

import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.exception.base.BaseException;
import com.siupo.restaurant.exception.base.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private <T> ResponseEntity<ApiResponse<T>> buildErrorResponse(ErrorCode errorCode) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .success(false)
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .data(null)
                .build();
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        return buildErrorResponse(errorCode);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleOther(Exception ex) {
        log.error("Unhandled exception occurred: ", ex);
        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
        return buildErrorResponse(errorCode);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AuthorizationDeniedException ex) {
        log.error("Access Denied Error: {}", ex.getMessage());
        ErrorCode errorCode = ErrorCode.FORBIDDEN;
        return buildErrorResponse(errorCode);
    }
}
