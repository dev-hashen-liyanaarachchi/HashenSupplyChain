package com.globaltrade.ejb.beans;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.globaltrade.ejb.interfaces.InventoryService;
import com.globaltrade.ejb.interfaces.OrderService;
import com.globaltrade.entity.Customer;
import com.globaltrade.entity.Order;
import com.globaltrade.entity.OrderItem;
import com.globaltrade.entity.Product;
import com.globaltrade.dto.OrderDTO;
import com.globaltrade.enums.OrderStatus;
import com.globaltrade.exception.InventoryException;
import com.globaltrade.exception.OrderException;
import com.globaltrade.repository.OrderRepository;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
public class OrderServiceBean implements OrderService {

    private static final Logger LOGGER = Logger.getLogger(OrderServiceBean.class.getName());

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    @Inject
    private OrderRepository orderRepository;

    @EJB
    private InventoryService inventoryService;

    // CMT REQUIRED: Standard atomic order transaction
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Order placeOrder(OrderDTO dto) throws OrderException, InventoryException {
        LOGGER.info("[CMT REQUIRED] Placing new customer order for Customer ID: " + dto.getCustomerId());

        Customer customer = em.find(Customer.class, dto.getCustomerId());
        if (customer == null) {
            throw new OrderException("INVALID", "Customer not found with ID: " + dto.getCustomerId());
        }

        double totalAmount = 0.0;
        for (OrderDTO.OrderItemDTO itemDto : dto.getItems()) {
            totalAmount += (itemDto.getUnitPrice() * itemDto.getQuantity());
        }

        String orderNo = "GTO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Order order = new Order(orderNo, customer, totalAmount);
        orderRepository.save(order);

        for (OrderDTO.OrderItemDTO itemDto : dto.getItems()) {
            Product product = em.find(Product.class, itemDto.getProductId());
            OrderItem item = new OrderItem(order, product, itemDto.getQuantity(), itemDto.getUnitPrice());
            em.persist(item);

            // Reserve stock (CMT MANDATORY)
            inventoryService.reserveStock(1L, itemDto.getProductId(), itemDto.getQuantity());
        }

        LOGGER.info("[CMT REQUIRED SUCCESS] Order placed successfully with Order #" + orderNo);
        return order;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Order updateOrderStatus(Long orderId, OrderStatus status) throws OrderException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException("NOT_FOUND", "Order not found with ID: " + orderId));
        order.setStatus(status);
        return orderRepository.update(order);
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomer(customerId);
    }
}
