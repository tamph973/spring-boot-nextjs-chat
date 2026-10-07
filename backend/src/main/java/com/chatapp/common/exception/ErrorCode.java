package com.chatapp.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    BAD_REQUEST("BAD_REQUEST"),
    NOT_FOUND("NOT_FOUND"),
    FORBIDDEN("FORBIDDEN"),
    VALIDATION_ERROR("VALIDATION_ERROR"),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND"),
    METHOD_NOT_ALLOWED("METHOD_NOT_ALLOWED"),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR");

    private final String code;

    ErrorCode(String code) {
        this.code = code;
    }
}