package com.globaltrade.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class InsufficientStockException extends InventoryException {

    public InsufficientStockException(Long productId, int requestedQty, int availableQty) {
        super("PROD-" + productId, requestedQty, availableQty);
    }
}
