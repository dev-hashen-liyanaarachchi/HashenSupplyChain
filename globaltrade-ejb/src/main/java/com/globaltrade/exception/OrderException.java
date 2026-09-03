package com.globaltrade.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class OrderException extends GlobalTradeException {

    private final String orderNumber;

    public OrderException(String orderNumber, String reason) {
        super("Order Processing Failure for Order #" + orderNumber + ": " + reason);
        this.orderNumber = orderNumber;
    }

    public String getOrderNumber() {
        return orderNumber;
    }
}
