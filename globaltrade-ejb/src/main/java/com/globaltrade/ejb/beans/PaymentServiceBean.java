package com.globaltrade.ejb.beans;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.globaltrade.ejb.interfaces.PaymentService;
import com.globaltrade.entity.Order;
import com.globaltrade.entity.Payment;
import com.globaltrade.enums.OrderStatus;
import com.globaltrade.enums.PaymentMethod;
import com.globaltrade.enums.PaymentStatus;
import com.globaltrade.exception.PaymentException;
import java.util.UUID;

@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
public class PaymentServiceBean implements PaymentService {

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Payment processPayment(Long orderId, Double amount, PaymentMethod method) throws PaymentException {
        Order order = em.find(Order.class, orderId);
        if (order == null) {
            throw new PaymentException("NO_ORDER", "Order not found with ID: " + orderId);
        }

        if (amount == null || amount <= 0 || !amount.equals(order.getTotalAmount())) {
            throw new PaymentException("AMOUNT_MISMATCH", "Payment amount must match order total amount.");
        }

        String ref = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Payment payment = new Payment(order, ref, method, amount);
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        em.persist(payment);

        order.setStatus(OrderStatus.CONFIRMED);
        em.merge(order);

        return payment;
    }

    @Override
    public Payment getPaymentByOrderId(Long orderId) {
        return em.createQuery("SELECT p FROM Payment p WHERE p.order.id = :oId", Payment.class)
                .setParameter("oId", orderId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
}
