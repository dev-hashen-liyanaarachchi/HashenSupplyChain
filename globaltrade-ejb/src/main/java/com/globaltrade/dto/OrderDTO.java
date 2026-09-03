package com.globaltrade.dto;

import java.io.Serializable;
import java.util.List;

public class OrderDTO implements Serializable {
    private Long customerId;
    private Long shippingAddressId;
    private String phone;
    private Double itemsSubtotal;
    private Double shippingCost;
    private Double totalAmount;
    private List<OrderItemDTO> items;

    public OrderDTO() {}

    public OrderDTO(Long customerId, Long shippingAddressId, String phone, Double itemsSubtotal, Double shippingCost, Double totalAmount, List<OrderItemDTO> items) {
        this.customerId = customerId;
        this.shippingAddressId = shippingAddressId;
        this.phone = phone;
        this.itemsSubtotal = itemsSubtotal;
        this.shippingCost = shippingCost;
        this.totalAmount = totalAmount;
        this.items = items;
    }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getShippingAddressId() { return shippingAddressId; }
    public void setShippingAddressId(Long shippingAddressId) { this.shippingAddressId = shippingAddressId; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Double getItemsSubtotal() { return itemsSubtotal; }
    public void setItemsSubtotal(Double itemsSubtotal) { this.itemsSubtotal = itemsSubtotal; }

    public Double getShippingCost() { return shippingCost; }
    public void setShippingCost(Double shippingCost) { this.shippingCost = shippingCost; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }

    public static class OrderItemDTO implements Serializable {
        private Long productId;
        private Integer quantity;
        private Double unitPrice;

        public OrderItemDTO() {}

        public OrderItemDTO(Long productId, Integer quantity, Double unitPrice) {
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public Double getUnitPrice() { return unitPrice; }
        public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
    }
}
