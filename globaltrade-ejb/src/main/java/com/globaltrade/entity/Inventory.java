package com.globaltrade.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "inventories")
public class Inventory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

    @Column(name = "unit_price")
    private Double unitPrice = 0.0;

    @Column(name = "available_qty", nullable = false)
    private Integer availableQty;

    @Column(name = "reserved_qty", nullable = false)
    private Integer reservedQty = 0;

    @Column(name = "reorder_threshold", nullable = false)
    private Integer reorderThreshold = 100;

    public Inventory() {}

    public Inventory(Warehouse warehouse, Product product, Integer availableQty, Integer reorderThreshold) {
        this.warehouse = warehouse;
        this.product = product;
        this.availableQty = availableQty;
        this.reorderThreshold = reorderThreshold;
        if (product != null) {
            this.unitPrice = product.getPrice();
        }
    }

    public Inventory(Warehouse warehouse, Product product, Vendor vendor, Double unitPrice, Integer availableQty, Integer reorderThreshold) {
        this.warehouse = warehouse;
        this.product = product;
        this.vendor = vendor;
        this.unitPrice = unitPrice;
        this.availableQty = availableQty;
        this.reorderThreshold = reorderThreshold;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Warehouse getWarehouse() { return warehouse; }
    public void setWarehouse(Warehouse warehouse) { this.warehouse = warehouse; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Vendor getVendor() { return vendor; }
    public void setVendor(Vendor vendor) { this.vendor = vendor; }

    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

    public Integer getAvailableQty() { return availableQty; }
    public void setAvailableQty(Integer availableQty) { this.availableQty = availableQty; }

    public Integer getReservedQty() { return reservedQty; }
    public void setReservedQty(Integer reservedQty) { this.reservedQty = reservedQty; }

    public Integer getReorderThreshold() { return reorderThreshold; }
    public void setReorderThreshold(Integer reorderThreshold) { this.reorderThreshold = reorderThreshold; }
}
