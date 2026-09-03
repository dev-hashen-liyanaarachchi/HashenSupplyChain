package com.globaltrade.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class PaymentSettlementException extends PaymentException {

    public PaymentSettlementException(String transactionRef, String message) {
        super(transactionRef, message);
    }
}
