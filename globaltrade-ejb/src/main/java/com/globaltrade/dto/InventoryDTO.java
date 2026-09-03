package com.globaltrade.dto;

import java.io.Serializable;

public class InventoryDTO implements Serializable {
    private Long warehouseId;
    private Long productId;
    private Integer availableQty;
    private Integer reorderThreshold;

    public InventoryDTO() {}

    public InventoryDTO(Long warehouseId, Long productId, Integer availableQty, Integer reorderThreshold) {
        this.warehouseId = warehouseId;
        this.productId = productId;
        this.availableQty = availableQty;
        this.reorderThreshold = reorderThreshold;
    }

    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getAvailableQty() { return availableQty; }
    public void setAvailableQty(Integer availableQty) { this.availableQty = availableQty; }

    public Integer getReorderThreshold() { return reorderThreshold; }
    public void setReorderThreshold(Integer reorderThreshold) { this.reorderThreshold = reorderThreshold; }
}
