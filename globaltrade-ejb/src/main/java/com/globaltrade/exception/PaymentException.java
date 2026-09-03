package com.globaltrade.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class PaymentException extends GlobalTradeException {

    private final String transactionRef;

    public PaymentException(String transactionRef, String reason) {
        super("Payment Transaction Failed for Ref [" + transactionRef + "]: " + reason);
        this.transactionRef = transactionRef;
    }

    public String getTransactionRef() { return transactionRef; }
}
