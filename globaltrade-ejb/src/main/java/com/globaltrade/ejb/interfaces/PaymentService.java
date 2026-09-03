package com.globaltrade.ejb.interfaces;

import jakarta.ejb.Local;
import com.globaltrade.entity.Payment;
import com.globaltrade.enums.PaymentMethod;
import com.globaltrade.exception.PaymentException;

@Local
public interface PaymentService {
    Payment processPayment(Long orderId, Double amount, PaymentMethod method) throws PaymentException;

    Payment getPaymentByOrderId(Long orderId);
}
