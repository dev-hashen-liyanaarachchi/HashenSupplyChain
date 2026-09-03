package com.globaltrade.exception;

public class GlobalTradeException extends Exception {

    public GlobalTradeException(String message) {
        super(message);
    }

    public GlobalTradeException(String message, Throwable cause) {
        super(message, cause);
    }
}
