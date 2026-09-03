package com.globaltrade.ejb.interfaces;

import jakarta.ejb.Local;
import com.globaltrade.entity.Order;
import com.globaltrade.dto.OrderDTO;
import com.globaltrade.enums.OrderStatus;
import com.globaltrade.exception.InventoryException;
import com.globaltrade.exception.OrderException;

import java.util.List;

@Local
public interface OrderService {
    Order placeOrder(OrderDTO dto) throws OrderException, InventoryException;

    Order updateOrderStatus(Long orderId, OrderStatus status) throws OrderException;

    Order getOrderById(Long id);

    List<Order> getOrdersByCustomer(Long customerId);
}
