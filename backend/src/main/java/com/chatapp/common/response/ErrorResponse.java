package com.chatapp.common.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ErrorResponse {

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private Integer status;

    /**
     * HTTP error name.
     * Example: "Bad Request", "Not Found", "Internal Server Error"
     */
    private String error;

    /**
     *  Example: RESOURCE_NOT_FOUND, USER_NOT_FOUND,...
     */
    private String code;

    /**
     * User/developer-friendly error message.
     */
    private String message;

    /**
     * Request path that caused the error.
     */
    private String path;

    /**
     * Validation errors for request field validation.
     */
    private List<ValidationError> validationErrors;

    @Data
    @Builder
    public static class ValidationError {

        private String field;

        private String message;

        /**
         * Rejected value.
         *
         * Be careful when exposing this field because
         * it may contain sensitive information.
         */
        private String rejectedValue;
    }
}

