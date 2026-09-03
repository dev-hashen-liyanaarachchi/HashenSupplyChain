package com.globaltrade.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.globaltrade.entity.Order;
import com.globaltrade.entity.OrderItem;
import com.globaltrade.entity.Product;
import com.globaltrade.entity.Shipment;
import com.globaltrade.entity.ShipmentItem;

import java.util.List;
import java.util.logging.Logger;

@Stateless
public class ShipmentItemService {

    private static final Logger LOGGER = Logger.getLogger(ShipmentItemService.class.getName());

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    public void createShipmentItemsForShipment(Shipment shipment) {
        if (shipment == null || shipment.getOrder() == null) return;

        Order order = shipment.getOrder();
        List<OrderItem> orderItems = em.createQuery(
                "SELECT oi FROM OrderItem oi WHERE oi.order.id = :oid", OrderItem.class)
                .setParameter("oid", order.getId())
                .getResultList();

        if (orderItems.isEmpty()) {
            List<Product> products = em.createQuery("SELECT p FROM Product p", Product.class).getResultList();
            if (!products.isEmpty()) {
                Product p = products.get(0);
                OrderItem oi = new OrderItem(order, p, 1, p.getPrice() != null ? p.getPrice() : 4890.00);
                em.persist(oi);
                orderItems = List.of(oi);
            }
        }

        for (OrderItem oi : orderItems) {
            ShipmentItem si = new ShipmentItem(shipment, oi, oi.getQuantity());
            em.persist(si);
            LOGGER.info("[SHIPMENT ITEM SAVED] Saved shipment item for Shipment #" + shipment.getTrackingNumber() + ", Product: " + (oi.getProduct() != null ? oi.getProduct().getName() : "Item") + ", Qty: " + oi.getQuantity());
        }
    }

    public List<ShipmentItem> getAllShipmentItems() {
        List<ShipmentItem> list = em.createQuery(
                "SELECT DISTINCT si FROM ShipmentItem si LEFT JOIN FETCH si.shipment s LEFT JOIN FETCH si.orderItem oi LEFT JOIN FETCH oi.product p ORDER BY si.id DESC", ShipmentItem.class)
                .getResultList();

        if (list.isEmpty()) {
            LOGGER.info("[SHIPMENT ITEMS SEED] Seeding shipment_items database table for existing shipments...");

            List<Shipment> shipments = em.createQuery("SELECT s FROM Shipment s", Shipment.class).getResultList();
            for (Shipment s : shipments) {
                createShipmentItemsForShipment(s);
            }

            list = em.createQuery(
                    "SELECT DISTINCT si FROM ShipmentItem si LEFT JOIN FETCH si.shipment s LEFT JOIN FETCH si.orderItem oi LEFT JOIN FETCH oi.product p ORDER BY si.id DESC", ShipmentItem.class)
                    .getResultList();
        }
        return list;
    }

    public List<ShipmentItem> getShipmentItemsByShipment(Long shipmentId) {
        return em.createQuery(
                "SELECT DISTINCT si FROM ShipmentItem si LEFT JOIN FETCH si.shipment s LEFT JOIN FETCH si.orderItem oi LEFT JOIN FETCH oi.product p WHERE si.shipment.id = :sid ORDER BY si.id ASC", ShipmentItem.class)
                .setParameter("sid", shipmentId)
                .getResultList();
    }
}
