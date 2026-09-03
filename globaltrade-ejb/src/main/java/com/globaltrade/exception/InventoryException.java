package com.globaltrade.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class InventoryException extends GlobalTradeException {

    private final String sku;
    private final int requestedQty;
    private final int availableQty;

    public InventoryException(String sku, int requestedQty, int availableQty) {
        super("Inventory shortage for SKU: " + sku + ". Requested: " + requestedQty + ", Available: " + availableQty);
        this.sku = sku;
        this.requestedQty = requestedQty;
        this.availableQty = availableQty;
    }

    public String getSku() { return sku; }
    public int getRequestedQty() { return requestedQty; }
    public int getAvailableQty() { return availableQty; }
}
