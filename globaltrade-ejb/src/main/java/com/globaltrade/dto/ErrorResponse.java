package com.globaltrade.dto;

import java.io.Serializable;

public class ErrorResponse implements Serializable {
    private String error;
    private String message;
    private int statusCode;

    public ErrorResponse() {
    }

    public ErrorResponse(String error, String message, int statusCode) {
        this.error = error;
        this.message = message;
        this.statusCode = statusCode;
    }

    public static ErrorResponse of(String error, String message, int statusCode) {
        return new ErrorResponse(error, message, statusCode);
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
