package com.globaltrade.exception;

public class GlobalTradeException extends RuntimeException {

    public GlobalTradeException(String message) {
        super(message);
    }

    public GlobalTradeException(String message, Throwable cause) {
        super(message, cause);
    }
}
