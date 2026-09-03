package com.globaltrade.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.globaltrade.entity.Order;
import com.globaltrade.enums.OrderStatus;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class OrderRepository {

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order update(Order order) {
        return em.merge(order);
    }

    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(em.find(Order.class, id));
    }

    public Optional<Order> findByOrderNumber(String orderNumber) {
        return em.createQuery("SELECT o FROM Order o WHERE o.orderNumber = :oNum", Order.class)
                .setParameter("oNum", orderNumber)
                .getResultStream()
                .findFirst();
    }

    public List<Order> findByCustomer(Long customerId) {
        return em.createQuery("SELECT o FROM Order o WHERE o.customer.id = :cId", Order.class)
                .setParameter("cId", customerId)
                .getResultList();
    }

    public List<Order> findByStatus(OrderStatus status) {
        return em.createQuery("SELECT o FROM Order o WHERE o.status = :status", Order.class)
                .setParameter("status", status)
                .getResultList();
    }
}
